package manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import eu.cpswarm.optimization.messages.ParameterSet;
import eu.cpswarm.optimization.messages.ReplyMessage;
import eu.cpswarm.optimization.messages.ReplyMessage.Status;
import eu.cpswarm.optimization.messages.SimulationResultMessage;
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
	protected void handleCandidate(final EntityBareJid sender, final ParameterSet parameterSet) {
		try {
			if (fake) {
				Thread.sleep(timeout);
				if(calculator == null) {
					ComponentInstance instance = this.fitnessCalculatorFactory.newInstance(null);
					calculator = (FitnessFunctionCalculator) instance.getInstance();
				}
				parent.publishFitness(calculator.randomFitness(parent.getOptimizationID(), parent.getSimulationID()), null, 0);
				return;
			}
			packageName = parent.getOptimizationID().substring(0, parent.getOptimizationID().indexOf("!"));
			if (sender.equals(JidCreate.entityBareFromOrThrowUnchecked(parent.getOptimizationJID()))) {
				if (parameterSet.getParameters().get(0).getName().equals("test")) {
					parent.setTestResult("optimization");
					return;
				}
				if (!serializeCandidate(parameterSet)) {
					parent.publishFitness(
							new SimulationResultMessage(parent.getOptimizationID(), "Error serializing the candidate",
									ReplyMessage.Status.ERROR, parent.getSimulationID(), BAD_FITNESS), null, 0);
					return;
				}
				runSimulation(true);
			//	if (SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
					System.out.println("done simulation " + this.parent.getSimulationID());
			//	}	
			} else { // SOO
				if (parameterSet.getParameters().get(0).getName().equals("test")) {
					parent.setTestResult("simulation");
					return;
				}
				if (!serializeCandidate(parameterSet)) {
					try { // send error result to SOO
						final ChatManager chatmanager = ChatManager.getInstanceFor(parent.getConnection());
						final Chat newChat = chatmanager.chatWith(parent.getOrchestratorJID().asEntityBareJidIfPossible());
						SimulationResultMessage reply = new SimulationResultMessage(parent.getOptimizationID(), "Error serializing the candidate",
								ReplyMessage.Status.ERROR, parent.getSimulationID(), BAD_FITNESS);
						MessageSerializer serializer = new MessageSerializer();
						newChat.send(serializer.toJson(reply));
					} catch (final Exception e) {
						e.printStackTrace();
					}
					return;
				}
				runSimulation(true); // false
			}
		} catch (IOException | InterruptedException e) {
			parent.publishFitness(new SimulationResultMessage(parent.getOptimizationID(),
					"Error handling the candidate", ReplyMessage.Status.ERROR, parent.getSimulationID(), BAD_FITNESS), null, 0);
		}
	}

	private void runSimulation(boolean calcFitness) throws IOException, InterruptedException {
		
		try {
			process = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "source /opt/ros/kinetic/setup.bash; killall -2 roscore; rosclean purge -y; rm "+parent.getBagPath()+"*.bag; rm "+parent.getBagPath()+"*.active" });
		/*	String line="";
			BufferedReader input =  
					new BufferedReader  
					(new InputStreamReader(process.getErrorStream()));  
			while ((line = input.readLine()) != null) {  
					System.out.println(line);
			} */
			int exit = process.waitFor();
			//	if(exit!=0)
			//	System.out.println("clean bags failed");
			process = null;
		//	input.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
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
				if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
					System.out.println(" MessageEventCoordinator handler is timeout! ");
				}				
				instance.dispose();
				executor.shutdown();
				if (calcFitness) {
					// create an instance per request on demand by using a Factory Component
					instance = this.fitnessCalculatorFactory.newInstance(null);
					FitnessFunctionCalculator calculator = (FitnessFunctionCalculator) instance.getInstance();
					SimulationResultMessage result = calculator.calcFitness(parent.getOptimizationID(), parent.getSimulationID(), parent.getDataFolder(),parent.getBagPath(), parent.getTimeout());
					if (result.getOperationStatus().equals(ReplyMessage.Status.ERROR)) {
						System.out.println("Error fitness " +parent.getSimulationID());
					//	return;
					}// else {
						parent.publishFitness(result, params.toString(), calculator.counter);
					//}
				}
				return;
			}
			instance.dispose();
			executor.shutdown();
			if (calcFitness) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				instance = this.fitnessCalculatorFactory.newInstance(null);
				FitnessFunctionCalculator calculator = (FitnessFunctionCalculator) instance.getInstance();
				parent.publishFitness(
						calculator.calcFitness(parent.getOptimizationID(), parent.getSimulationID(), parent.getDataFolder(),parent.getBagPath(), parent.getTimeout()), params.toString(), calculator.counter);
						instance.dispose();
			}
	}

	@Deactivate
	public void deactivate() {
		if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println(" MessageEventCoordinator is deactived ");
		}	
		if(calculator != null)
			calculator = null;
		Process proc;
		try {
			proc = Runtime.getRuntime().exec("killall -9 stageros");
			proc.waitFor();
			proc.destroy();
			proc = null;
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
		}

	}

}
