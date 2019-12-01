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

	private double readBag(final String dataFolder, final String bagFile, final int timeout) {
		double fitness = 0.0;
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "source /opt/ros/kinetic/setup.bash ; "
					+ " python " + dataFolder + "fitness.py " + bagFile + " " + timeout });
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Runtime.getRuntime().addShutdownHook(new Thread(proc::destroy));
		String line = "";
		BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		try {
			if ((line = input.readLine()) != null) {
				fitness = Double.valueOf(line.trim());
			}
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}

		return fitness;
	}

	/**
	 * Calculate the fitness score of the last simulation run.
	 * 
	 * @return SimulationResultMessage: simulation result last simulation run to be send back to optimization tool.
	 */
	public SimulationResultMessage calcFitness(final String optimizationID, final String simulationID,
			final String dataFolder, String bagPath, final int timeout) {
		File bagFolder = new File(bagPath); // path to ~/.ros/ directory
		String[] bagFiles = bagFolder.list(fileLogFilter);
		double fitnessSum = 0;
		for (int i = 0; i < bagFiles.length; i++) {
			String bagFile = bagPath+ bagFiles[i];
			double fitness = readBag(dataFolder, bagFile, timeout);
			if (SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
				System.out.println(bagFiles[i] + " fitness score : " + fitness);
			}
			fitnessSum += fitness;
		}
		if (SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println("Total fitness calculated " + fitnessSum);
		}
		// overall fitness is average fitness of agents
		return new SimulationResultMessage(optimizationID, "Total fitness calculated:" + fitnessSum +" for "+bagFiles.length+ " robots",
				ReplyMessage.Status.OK, simulationID, fitnessSum / bagFiles.length);
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
		if (SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println("get a file log filter ");
		}

	}

}
