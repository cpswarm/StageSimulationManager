package manager;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import be.iminds.iot.ros.util.RosCommand;
import eu.cpswarm.optimization.messages.ReplyMessage;
import eu.cpswarm.optimization.messages.SimulationResultMessage;
import simulation.xmpp.AbstractMessageEventCoordinator;
import simulation.SimulationManager;

//factory component for creating instance per requestor
@Component(factory = "it.ismb.pert.cpswarm.stageMessageEventCoordinatorImpl.factory"
)
public class MessageEventCoordinatorImpl extends AbstractMessageEventCoordinator {

	private String packageName = null;
	private SimulationManager parent = null;
	private int timeout;
	private boolean fake;
	private ComponentFactory fitnessCalculatorFactory;
	private ComponentFactory simulationLauncherFactory;
	private ComponentFactory rosCommandFactory; // used to catkin build the workspace

	@Activate
	protected void activate(Map<String, Object> properties) throws Exception {

		System.out.println(" Instantiate a stage MessageEventCoordinatorImpl");
		for (Entry<String, Object> entry : properties.entrySet()) {
			String key = entry.getKey();
			if (key.equals("SimulationManager")) {
				try {
					parent = (SimulationManager) entry.getValue();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
		assert (parent) != null;
		if (parent != null) {
			this.timeout = parent.getTimeout();
			this.fake = parent.isFake();
			setSimulationManager(parent);
		}
	}

	@Reference(target = "(component.factory=it.ismb.pert.cpswarm.fitnessCalculator.factory)")
	public void getFitnessCalculatorFactory(final ComponentFactory s) {
		this.fitnessCalculatorFactory = s;
		System.out.println(" MA MessageEventCoordinatorImpl gets a fitnessCalculator ComponentFactory ");

	}

	@Reference(target = "(component.factory=it.ismb.pert.cpswarm.simulationLauncher.factory)")
	public void getSimulationLauncherFactory(final ComponentFactory s) {
		this.simulationLauncherFactory = s;
		System.out.println(" MA MessageEventCoordinatorImpl gets a simulationLauncher ComponentFactory ");

	}

	@Reference(target = "(component.factory=it.ismb.pert.cpswarm.rosCommand.factory)")
	public void getRosCommandFactory(final ComponentFactory s) {
		this.rosCommandFactory = s;
		System.out.println(" MA MessageEventCoordinatorImpl gets a RosCommand ComponentFactory ");

	}

	@Override
	protected void handleCandidate(final EntityBareJid sender, final String candidate) {
		System.out.println(" MA MessageEventCoordinatorImpl calls  to handleCandidate() sender = "+sender);

		try {
			if (fake) {
				Thread.sleep(timeout);
				ComponentInstance instance = this.fitnessCalculatorFactory.newInstance(null);
				FitnessFunctionCalculator calculator = (FitnessFunctionCalculator) instance.getInstance();
				parent.publishFitness(calculator.randomFitness(parent.getOptimizationID(), parent.getSimulationID()));
				instance.dispose();
				return;
			}
			packageName = parent.getOptimizationID().substring(0, parent.getOptimizationID().indexOf("!"));
			packageFolder = parent.getRosFolder() + packageName;
			if (sender.equals(JidCreate.entityBareFromOrThrowUnchecked(parent.getOptimizationJID()))) {
				if (candidate.equals("test")) {
					parent.setTestResult("optimization");
					return;
				}
				if (!serializeCandidate(candidate)) {
					parent.publishFitness(
							new SimulationResultMessage(parent.getOptimizationID(), "Error serializing the candidate",
									ReplyMessage.Status.ERROR, parent.getSimulationID(), BAD_FITNESS));
					return;
				}
				System.out.println(
						"Compiling the package, using the 'catkin build' service provided by the RosCommand factory component ");
				String catkinWS = parent.getCatkinWS();
				Properties props = new Properties();
				props.put("ros.buildWorkspace", catkinWS);
				ComponentInstance instance = null;
				boolean result = true;
				try {
					instance = this.rosCommandFactory.newInstance((Dictionary) props);
					RosCommand catkinBuild = (RosCommand) instance.getInstance();
					catkinBuild.buildWorkspace();
				} catch (Exception err) {
					result = false;
					System.err.println("Error when building workspace: " + catkinWS);
					err.printStackTrace();
				} finally {
					if (instance != null)
						instance.dispose();
				}
				System.out.println("Compilation finished, " + result);

				if (result) {
					runSimulation(true);
					System.out.println("done");
				} else {
					System.out.println("Error");
					return;
				}
			} else {
				if (candidate.equals("test")) {
					parent.setTestResult("simulation");
					return;
				}
				if (!serializeCandidate(candidate)) {
					parent.publishFitness(
							new SimulationResultMessage(parent.getOptimizationID(), "Error serializing the candidate",
									ReplyMessage.Status.ERROR, parent.getSimulationID(), BAD_FITNESS));
					return;
				}
				runSimulation(false);
			}
		} catch (IOException | InterruptedException e) {
			parent.publishFitness(new SimulationResultMessage(parent.getOptimizationID(),
					"Error handling the candidate", ReplyMessage.Status.ERROR, parent.getSimulationID(), BAD_FITNESS));
		}
	}

	private void runSimulation(boolean calcFitness) throws IOException, InterruptedException {

		System.out.println("\n MessageEventCoordinator is starting to launch the simulation \n");

		ExecutorService executor = Executors.newSingleThreadExecutor();
		Properties props = new Properties();
		props.put("SimulationManager", parent);
		props.put("rosWorkspace", parent.getCatkinWS());
		props.put("packageFolder", packageFolder);
		props.put("packageName", packageName);
		props.put("calcFitness", calcFitness);

		ComponentInstance instance = this.simulationLauncherFactory.newInstance((Dictionary) props);
		SimulationLauncher simulationLauncher = (SimulationLauncher) instance.getInstance();
		try {
			final Future<?> handler = executor.submit(simulationLauncher);
			try {
				handler.get(parent.getTimeout(), TimeUnit.MILLISECONDS);
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				System.out.println(" MessageEventCoordinator handler is timeout! ");
				executor.shutdown();
				if (calcFitness) {
					instance.dispose();
					// create an instance per request on demand by using a Factory Component
					instance = this.fitnessCalculatorFactory.newInstance(null);
					FitnessFunctionCalculator calculator = (FitnessFunctionCalculator) instance.getInstance();
					if (calculator.calcFitness(parent.getOptimizationID(), parent.getSimulationID(), packageFolder)
							.getOperationStatus().equals(ReplyMessage.Status.ERROR)) {
						System.out.println("Error");
						return;
					}
				}
				Process proc;
				try {
					proc = Runtime.getRuntime().exec("killall " + packageName);
					proc.waitFor();
					proc.destroy();
					proc = null;
				} catch (IOException | InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			executor.shutdown();
		} finally {
			if (instance != null)
				instance.dispose();
		}

	}

	@Deactivate
	public void deactivate() {
		System.out.println(" MessageEventCoordinator is deactived ");
		Process proc;
		try {
			proc = Runtime.getRuntime().exec("killall " + packageName);
			proc.waitFor();
			proc.destroy();
			proc = null;
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}

	}

}
