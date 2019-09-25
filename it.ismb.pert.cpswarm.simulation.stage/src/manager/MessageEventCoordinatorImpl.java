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

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import be.iminds.iot.ros.util.RosCommand;
import eu.cpswarm.optimization.messages.MessageSerializer;
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
			if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
				System.out.println(" Instantiate a stage MessageEventCoordinatorImpl");
			}
			setSimulationManager(parent);
		}
	}

	@Reference(target = "(component.factory=it.ismb.pert.cpswarm.fitnessCalculator.factory)")
	public void getFitnessCalculatorFactory(final ComponentFactory s) {
		this.fitnessCalculatorFactory = s;

	}

	@Reference(target = "(component.factory=it.ismb.pert.cpswarm.simulationLauncher.factory)")
	public void getSimulationLauncherFactory(final ComponentFactory s) {
		this.simulationLauncherFactory = s;
	}

	@Reference(target = "(component.factory=it.ismb.pert.cpswarm.rosCommand.factory)")
	public void getRosCommandFactory(final ComponentFactory s) {
		this.rosCommandFactory = s;
	}

	@Override
	protected void handleCandidate(final EntityBareJid sender, final String candidate, final String candidateType) {
		try {
			if (fake) {
				Thread.sleep(timeout);
				ComponentInstance instance = this.fitnessCalculatorFactory.newInstance(null);
				FitnessFunctionCalculator calculator = (FitnessFunctionCalculator) instance.getInstance();
				parent.publishFitness(calculator.randomFitness(parent.getOptimizationID(), parent.getSimulationID()));
				instance.dispose();
				return;
			}
	//		packageName = parent.getOptimizationID().substring(0, parent.getOptimizationID().indexOf("!"));
			packageName = parent.getSCID();
			packageFolder = parent.getRosFolder() + packageName;
			if (sender.equals(JidCreate.entityBareFromOrThrowUnchecked(parent.getOptimizationJID()))) {
				if (candidate.equals("test")) {
					parent.setTestResult("optimization");
					return;
				}
				if (!serializeCandidate(candidate)) {
					parent.publishFitness(
							new SimulationResultMessage(parent.getOptimizationID(), false, parent.getSimulationID(), BAD_FITNESS));
					return;
				}
				if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
					System.out.println("Compiling the package, using the 'catkin build' service provided by the RosCommand factory component \n");
				}
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
				if (result) {
					if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
						System.out.println("Compilation finished, success = "+result);
					}
					runSimulation(true);
					if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
						System.out.println("simulation "+this.parent.getSimulationID()+" done");
					}
				} else {
					System.out.println("Compilation with error, success = "+result);
					return;
				}
			} else { // SOO
				if (candidate.equals("test")) {
					parent.setTestResult("simulation");
					return;
				}
				if (!serializeCandidate(candidate)) {
					if (parent.isOrchestratorAvailable()) {
						try { // send error result to SOO
							final ChatManager chatmanager = ChatManager.getInstanceFor(parent.getConnection());
							final Chat newChat = chatmanager.chatWith(parent.getOrchestratorJID().asEntityBareJidIfPossible());
							SimulationResultMessage reply = new SimulationResultMessage(parent.getOptimizationID(), false, parent.getSimulationID(), BAD_FITNESS);
							MessageSerializer serializer = new MessageSerializer();
							newChat.send(serializer.toJson(reply));
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
					return;
				}
				runSimulation(false);
			}
		} catch (IOException | InterruptedException e) {
			parent.publishFitness(new SimulationResultMessage(parent.getOptimizationID(), false, parent.getSimulationID(), BAD_FITNESS));
		}
	}

	private void runSimulation(boolean calcFitness) throws IOException, InterruptedException {
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
				if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
					System.out.println(" MessageEventCoordinator handler is timeout! ");
				}				
				executor.shutdown();
				if (calcFitness) {
					instance.dispose();
					// create an instance per request on demand by using a Factory Component
					instance = this.fitnessCalculatorFactory.newInstance(null);
					FitnessFunctionCalculator calculator = (FitnessFunctionCalculator) instance.getInstance();
					if (calculator.calcFitness(parent.getOptimizationID(), parent.getSimulationID(), packageFolder)
							.getSuccess().equals(false)) {
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
		if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println(" MessageEventCoordinator is deactived ");
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

}
