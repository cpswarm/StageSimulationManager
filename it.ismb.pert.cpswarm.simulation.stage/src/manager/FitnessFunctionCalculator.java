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
	private static final double TIMEOUT = 90;
	private static final double MAX_DISTANCE = 50.0;
	private static final double DISTANCE_THRESHOLD = 0.2;
	private static final double BAD_FITNESS_VALUE = 0.0;
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
	 * Read the log files produced by ROS. It assumes log files with two columns,
	 * separated by tabulator. The first column must be an integer, the second a
	 * double value.
	 * 
	 * @return ArrayList<NavigableMap<Integer,Double>>: An array with one map entry
	 *         for each log file.
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
	 * @return boolean: result of the method.
	 */
	public SimulationResultMessage calcFitness(final String optimizationID, final String simulationID,
			final String dataFolder, final int timeout) {
		ProcessBuilder builder = new ProcessBuilder();
		String home = builder.environment().get("HOME");
		String rosPath = home + File.separator + ".ros" + File.separator;		
		File rosFolder = new File(rosPath); // path to ~/.ros directory
		String[] bagFiles = rosFolder.list(fileLogFilter);
		double fitnessSum = 0;
		for (int i = 0; i < bagFiles.length; i++) {
			String bagFile = rosPath+ bagFiles[i];
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
