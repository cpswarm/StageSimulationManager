package it.ismb.pert.cpswarm.simulationLauncher;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import it.ismb.pert.cpswarm.fitnessFunctionCalculator.FitnessFunctionCalculator;
import it.ismb.pert.cpswarm.simulationManager.api.SimulationManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import be.iminds.iot.ros.util.RosCommand;


@Component(factory = "it.ismb.pert.cpswarm.simulationLauncher.factory"
)
public class SimulationLauncher implements Runnable {

	private boolean canRun = true;
	private String packageName = null;
	private SimulationManager parent = null;
	private String packageFolder = null;
	private String rosWorkspace = null;
	private boolean calcFitness = false;
	private ComponentFactory fitnessCalculatorFactory;
	private ComponentFactory rosCommandFactory; // used to roslaunch the simulation
	private BundleContext context;

	public void setSimulationManager(final SimulationManager manager) {
		assert manager != null;
		this.parent = manager;
		System.out.println("SimulationLauncher is bound to a MA =  " + manager.getClientID());
	}

	@Reference(target = "(component.factory=it.ismb.pert.cpswarm.fitnessCalculator.factory)")
	public void getComponentFactory(final ComponentFactory s) {
		this.fitnessCalculatorFactory = s;
		System.out.println(" Simulation Launcher gets a fitnessCalculator ComponentFactory ");

	}

	@Reference(target = "(component.factory=it.ismb.pert.cpswarm.rosCommand.factory)")
	public void getRosCommandFactory(final ComponentFactory s) {
		this.rosCommandFactory = s;
		System.out.println(" Simulation Launcher gets a RosCommand ComponentFactory ");

	}

	@Activate
	public void activate(BundleContext context, Map<String, Object> properties) throws Exception {
		System.out.println(" Instantiate a Simulation Launcher");
		this.context = context;
		for (Entry<String, Object> entry : properties.entrySet()) {
			String key = entry.getKey();
			if (key.equals("SimulationManager")) {
				parent = (SimulationManager) entry.getValue();
			} else if (key.equals("rosWorkspace")) {
				this.rosWorkspace = (String) entry.getValue();
			} else if (key.equals("packageFolder")) {
				this.packageFolder = (String) entry.getValue();
			} else if (key.equals("packageName")) {
				this.packageName = (String) entry.getValue();
			} else if (key.equals("calcFitness")) {
				this.calcFitness = (boolean) entry.getValue();
			}
		}
		assert (parent) != null;
		assert (packageFolder) != null;
		assert (packageName) != null;

		if (parent != null) {
			setSimulationManager(parent);
		}
	}

	@Override
	public void run() {
		ComponentInstance commandInstance = null;
		ComponentInstance calculatorInstance = null;
		try {
			try {
				String params = parent.getSimulationConfiguration();
				System.out.println("Launching the simulation for package: " + packageName + " with params: " + params);
				// roslaunch emergency_exit stage.launch visual:=true
				Properties props = new Properties();
				props.put("rosWorkspace", rosWorkspace);
				props.put("ros.package", packageName);
				props.put("ros.node", "stage.launch");
				if (params != null) {
					props.put("ros.mappings", params);
				}

				commandInstance = this.rosCommandFactory.newInstance((Dictionary) props);
				RosCommand roslaunch = (RosCommand) commandInstance.getInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (calcFitness) {
				System.out.println("\n simulation launcher starts to calculate fitness");
				calculatorInstance = this.fitnessCalculatorFactory.newInstance(null);
				FitnessFunctionCalculator calculator = (FitnessFunctionCalculator) calculatorInstance.getInstance();
				parent.publishFitness(
						calculator.calcFitness(parent.getOptimizationID(), parent.getSimulationID(), packageFolder));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (commandInstance != null)
				commandInstance.dispose();
			if (calculatorInstance != null)
				calculatorInstance.dispose();
		}

	}

	@Deactivate
	void deactivate() {
		System.out.println("Simulation launcher is deactived");
		Process proc;
		try {
			proc = Runtime.getRuntime().exec("killall " + packageName);
			proc.waitFor();
			proc.destroy();
			proc = null;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setCanRun(boolean canRun) {
		this.canRun = canRun;
	}

}
