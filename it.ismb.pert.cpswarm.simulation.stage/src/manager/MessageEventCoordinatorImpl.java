package manager;

import java.io.IOException;
import java.util.Dictionary;
import java.util.List;
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
import org.jivesoftware.smack.util.StringUtils;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.SimulationResultMessage;
import eu.cpswarm.optimization.parameters.Parameter;
import simulation.xmpp.AbstractMessageEventCoordinator;
import simulation.SimulationManager;

//factory component for creating instance per requestor
@Component(factory = "it.ismb.pert.cpswarm.stageMessageEventCoordinatorImpl.factory"
)
public class MessageEventCoordinatorImpl extends AbstractMessageEventCoordinator {

	private SimulationManager parent = null;
	private int timeout;
	private boolean fake;
	private Process process;
	private ComponentFactory fitnessCalculatorFactory;
	private ComponentFactory stageSimulationLauncherFactory;
	private FitnessFunctionCalculator calculator = null;

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

	@Reference(target = "(component.factory=it.ismb.pert.cpswarm.stageSimulationLauncher.factory)")
	public void getSimulationLauncherFactory(final ComponentFactory s) {
		this.stageSimulationLauncherFactory = s;
	}

	@Override
	protected void handleCandidate(final EntityBareJid sender, final List<Parameter> parameters) {
		try {
			if(calculator == null) {
				ComponentInstance instance = this.fitnessCalculatorFactory.newInstance(null);
				calculator = (FitnessFunctionCalculator) instance.getInstance();
			}
			if (fake) {
				Thread.sleep(timeout);
				serializeCandidate(parameters);
			//	SimulationResultMessage result = new SimulationResultMessage(parent.getOptimizationID(), false, parent.getSimulationID(), BAD_FITNESS);
			//	parent.publishFitness(result, null, 0);
				parent.publishFitness(calculator.randomFitness(parent.getOptimizationID(), parent.getSimulationID()), null, 0);
				System.out.println("done simulation " + this.parent.getSimulationID().split("_")[2]);
				parent.setSimulationID(null);
				return;
			}
			packageName = parent.getSCID();
			if (sender.equals(JidCreate.entityBareFromOrThrowUnchecked(parent.getOptimizationJID()))) {
				if (parameters.get(0).getName().equals("test")) {
					parent.setTestResult("optimization");
					return;
				}
				if (!serializeCandidate(parameters)) {
					parent.publishFitness(
							new SimulationResultMessage(parent.getOptimizationID(), false, parent.getSimulationID(), BAD_FITNESS), null, 0);
					return;
				}				
				runSimulation(true);
			} else { // SOO
				if (parameters.get(0).getName().equals("test")) {
					parent.setTestResult("simulation");
					return;
				}
				if (!serializeCandidate(parameters)) {
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
			parent.publishFitness(new SimulationResultMessage(parent.getOptimizationID(), false, parent.getSimulationID(), BAD_FITNESS), null, 0);
		}
	}

	private void runSimulation(boolean calcFitness) throws IOException, InterruptedException {
		try {
			ProcessBuilder builder = new ProcessBuilder(new String[] { "/bin/bash", "-c", "source /opt/ros/kinetic/setup.bash; rosclean purge -y; rm "+parent.getBagPath()+"*.bag; rm "+parent.getBagPath()+"*.active" });
			builder.inheritIO();
			process = builder.start();
			process.waitFor();
			process = null;
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		String sid = null;
		if(parent.getOptimizationID() != null) {
			sid = parent.getSimulationID().split("_")[2];
		} else {
			sid = parent.getSimulationID();
		}
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Properties props = new Properties();
		props.put("SimulationManager", parent);
		props.put("packageName", packageName);

		ComponentInstance instance = this.stageSimulationLauncherFactory.newInstance((Dictionary) props);
		SimulationLauncher simulationLauncher = (SimulationLauncher) instance.getInstance();
		final Future<?> handler = executor.submit(simulationLauncher);
		try {
			handler.get(parent.getTimeout(), TimeUnit.MILLISECONDS);
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			if (SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
				System.out.println(" MessageEventCoordinator handler is timeout! ");
			}
			instance.dispose();
			executor.shutdown();
			SimulationResultMessage result = calculator.calcFitness(parent.getOptimizationID(), parent.getSimulationID(), parent.getDataFolder(), parent.getBagPath(), parent.getTimeout(), parent.getFitnessFunction(), parent.getMaxNumberOfCarts());
			if (!result.getSuccess()) {
				System.out.println("Error fitness " + sid);
			}
			if (calcFitness) {
				parent.publishFitness(result, params.toString(), calculator.counter);
			}
			System.out.println("done simulation " + sid);
			parent.setSimulationID(null);
			result = null;
			return;
		}
		instance.dispose();
		executor.shutdown();
		System.out.println("Simulation " + sid + " unnormally quit without timeout! ");
		Process proc;
		ProcessBuilder builder;
		try {
			builder = new ProcessBuilder(new String[] { "/bin/bash", "-c", "killall -2 rosout; killall -2 record" });
			builder.inheritIO();
			proc = builder.start();
			proc.waitFor();
			proc = null;
			builder = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			builder = new ProcessBuilder(new String[] { "/bin/bash", "-c", "killall -2 stageros" });
			builder.inheritIO();
			proc = builder.start();
			proc.waitFor();
			proc = null;
			builder = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		SimulationResultMessage reply = new SimulationResultMessage(parent.getOptimizationID(), false, parent.getSimulationID(), BAD_FITNESS);
		if (calcFitness) {
			System.out.println("publish ERROR result to OT");
			parent.publishFitness(reply, params.toString(), 0);
			System.out.println("done simulation " + sid);
			parent.setSimulationID(null);
		} else {
			if (parent.isOrchestratorAvailable()) {
				try { // send final result to SOO for setting simulation done
					final ChatManager chatmanager = ChatManager.getInstanceFor(parent.getConnection());
					final Chat newChat = chatmanager.chatWith(parent.getOrchestratorJID().asEntityBareJidIfPossible());
					MessageSerializer serializer = new MessageSerializer();
					newChat.send(serializer.toJson(reply));
					System.out.println("send ERROR result to SOO");
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Deactivate
	public void deactivate() {
		if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println(" MessageEventCoordinator is deactived ");
		}	
		Process proc;
		try {
			proc = Runtime.getRuntime().exec("killall -2 roslaunch");
			proc.waitFor();
			proc.destroy();
			proc = null;
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}

	}

}
