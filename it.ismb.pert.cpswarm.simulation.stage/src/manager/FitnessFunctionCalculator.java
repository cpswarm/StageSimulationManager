package manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.Random;
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
@Component(factory = "it.ismb.pert.cpswarm.fitnessCalculator.factory"
)
public class FitnessFunctionCalculator {
	private static ArrayList<NavigableMap<Integer, Double>> logs;
	private static final double TIMEOUT = 90;
	private static final double MAX_DISTANCE = 50.0;
	private static final double DISTANCE_THRESHOLD = 0.2;
	private static final double BAD_FITNESS_VALUE = 0.0;
	private FileLogFilter fileLogFilter = null;

	@Activate
	public void activate(BundleContext context) {
		if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println("Instantiate a fitnessFunctionCalculator");
		}
	}

	@Deactivate
	public void deactivate() {
		if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
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
	private boolean readLogs(final String packageFolder) {
		// container for data of all log files
		logs = new ArrayList<NavigableMap<Integer, Double>>();
		String logsFolder = packageFolder + File.separator + "log" + File.separator;
		if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println("Reading logs from :" + logsFolder);
		}

		// path to log directory
		File logPath = new File(logsFolder);

		// iterate through all log files
		String[] logFiles = logPath.list(fileLogFilter);
		for (int i = 0; i < logFiles.length; i++) {
			if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
	    		System.out.println("Reading log file: "+logFiles[i]);
	    	}

			// container for data of one log file
			NavigableMap<Integer, Double> log = new TreeMap<Integer, Double>();

			// read log file
			Path logFile = Paths.get(logPath + "/" + logFiles[i]);
			try {
				BufferedReader logReader = Files.newBufferedReader(logFile);

				// store every line
				String line;
				while ((line = logReader.readLine()) != null) {
					if (line.length() <= 1 || line.startsWith("#"))
						continue;

					log.put(Integer.parseInt(line.split("\t")[0]), Double.parseDouble(line.split("\t")[1]));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			// store contents of log file
			logs.add(log);
		}
		return true;
	}

	/**
	 * Calculate the fitness score of the last simulation run.
	 * 
	 * @return boolean: result of the method.
	 */
	public SimulationResultMessage calcFitness(final String optimizationID, final String simulationID,
			final String packageFolder) {

		if (!readLogs(packageFolder)) {
			return new SimulationResultMessage(optimizationID, "Error reading the logs", ReplyMessage.Status.ERROR,
					simulationID, BAD_FITNESS_VALUE);
		}

		double fitnessSum = 0;
		double totalTimeTaken = 0;
		double totalDistance = 0;
		for (NavigableMap<Integer, Double> log : logs) {
			double agentTimeTaken = TIMEOUT;
			double agentDistance = MAX_DISTANCE;
			if (log.size() > 0) {
				agentTimeTaken = Math.min(log.size(), TIMEOUT);
				agentDistance = Math.min(log.lastEntry().getValue(), MAX_DISTANCE);
			}
			totalDistance += agentDistance;

			// basic fitness is dependent on distance to exit
			double fitness = (MAX_DISTANCE - agentDistance) / MAX_DISTANCE * 100;

			// award additional points for time taken if the agent got close to exit
			if (agentDistance < DISTANCE_THRESHOLD) {
				fitness += (TIMEOUT - agentTimeTaken) / TIMEOUT * 100;
			} else {
				agentTimeTaken = TIMEOUT;
			}
			totalTimeTaken += agentTimeTaken;
			if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
		    	System.out.println("Fitness score for agent "+fitness+", distance: "+agentDistance +", time:"+ agentTimeTaken);
		    }
		    fitnessSum += fitness;
		}
		if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println("Total fitness calculated "+fitnessSum);
		}
		// overall fitness is average fitness of agents
		return new SimulationResultMessage(optimizationID,
				"distance:" + (totalDistance / logs.size()) + " time:" + (totalTimeTaken / logs.size()),
				ReplyMessage.Status.OK, simulationID, fitnessSum / logs.size());
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
		if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println("get a file log filter ");
		}		
	}

}
