package manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.cpswarm.optimization.messages.ReplyMessage;
import eu.cpswarm.optimization.messages.SimulationResultMessage;
import simulation.SimulationManager;

//factory component for creating instance per request
@Component(factory = "it.ismb.pert.cpswarm.fitnessCalculator.factory")
public class FitnessFunctionCalculator {
	private FileLogFilter fileLogFilter = null;
	public int counter = 0;

	@Activate
	public void activate(BundleContext context) {		
		if (SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println("Instantiate a fitnessFunctionCalculator");
		}
	}

	@Deactivate
	public void deactivate() {
		if (SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println("Stoping the fitnessFunctionCalculator");
		}
	}

	/**
	 * Read the bag files produced during simulation. The bag files contain some information on specific topics that will be read by the fitness function script to calculate fitness value.
	 * @return double: the fitness value of a bag file
	 */

	private double readBag(final String dataFolder, final String bagFile, final int timeout, final String fitnessFunction, final int maxNumberOfCarts) {
		double fitness = 0.0;
		try {
			Process proc = Runtime.getRuntime()
					.exec(new String[] { "/bin/bash", "-c", "source /opt/ros/kinetic/setup.bash ; " + " python "
							+ dataFolder + fitnessFunction+" " + bagFile + " " + timeout+" "+ maxNumberOfCarts});
			String line = "";
			BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = input.readLine()) != null ) {
				if(line.startsWith("box_count_done=")) {
					counter += Integer.valueOf(line.trim().split("=")[1]).intValue();
				}
				if(line.startsWith("fitness=")) {
					fitness = Double.valueOf(line.trim().split("=")[1]);
				}
			}
			proc.waitFor();
			proc = null;
			input.close();
			line = null;

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println(bagFile + " fitness score : " + fitness+"  box_count = "+counter);
		}

		return fitness;
	}

	/**
	 * Calculate the fitness score of the last simulation run.
	 * 
	 * @return SimulationResultMessage: simulation result last simulation run to be send back to optimization tool.
	 */
	public SimulationResultMessage calcFitness(final String optimizationID, final String simulationID,
			final String dataFolder, String bagPath, final int timeout, final String fitnessFunction, final int maxNumberOfCarts) {
		this.counter=0;
		File bagFolder = new File(bagPath); // path to ~/.ros/ directory
		String[] bagFiles = bagFolder.list(fileLogFilter);
		int counter = 2;
		while(bagFiles.length==0 && counter>0) {  // wait the simulation stops for maximum 2 times to generate the bags
			try {
				if (SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
					System.out.println("No worker bag files, Waiting another 15s for simulation stop!");
				}
				Thread.sleep(15000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			bagFiles = bagFolder.list(fileLogFilter);
			counter-=1;
		}
		bagFolder = null;
		if (bagFiles.length == 0) {
			System.out.println("Simulation " + simulationID + " Error: No any worker bag files found for all robots!");
			try {
				ProcessBuilder builder = new ProcessBuilder(new String[] { "/bin/bash", "-c",
						"ls /home/.ros/; killall -2 roslaunch; killall -2 roscore; killall -2 rosout" });
				builder.inheritIO();
				Process proc = builder.start();
				proc.waitFor();
				proc = null;

			} catch (Exception e1) {
				e1.printStackTrace();
			}
			try {
				Thread.sleep(25000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			System.out.println("fitness = 0.0 sent");
			return new SimulationResultMessage(optimizationID, "No any worker bag files found for all robots",
					ReplyMessage.Status.ERROR, simulationID, 0.0);
		}
		double fitnessSum = 0;
		String bagFile = null;
		for (int i = 0; i < bagFiles.length; i++) {
			bagFile = bagPath+ bagFiles[i];
			double fitness = readBag(dataFolder, bagFile, timeout, fitnessFunction, maxNumberOfCarts);			
			fitnessSum += fitness;
		}
		System.out.println("fitness = "+ fitnessSum + " sent for "+bagFiles.length+ " workers");
		SimulationResultMessage result = new SimulationResultMessage(optimizationID, "Total fitness calculated:" + fitnessSum +" for "+bagFiles.length+ " workers",
				ReplyMessage.Status.OK, simulationID, fitnessSum);
		bagFiles = null;
		bagFile = null;
		return result;
		
	}

	public SimulationResultMessage randomFitness(final String optimizationID, final String simulationID) {
		Random random = new Random();
		return new SimulationResultMessage(optimizationID,
				"distance:" + random.nextDouble() + " time:" + random.nextDouble(), ReplyMessage.Status.OK,
				simulationID, random.nextDouble());
	}

	@Reference
	void setFileLogFilter(FileLogFilter filter) {
		// make sure a file log filer is available before starting to calculate fitness
		this.fileLogFilter = filter;
	}

}
