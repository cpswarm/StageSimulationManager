package it.ismb.pert.cpswarm.simulationManager.xmpp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.RunSimulationMessage;
import it.ismb.pert.cpswarm.simulationManager.api.SimulationManager;

/**
 *
 * The abstract class to be used by the simulation managers to receive the
 * candidates via chat messages
 *
 */
public abstract class AbstractMessageEventCoordinator implements IncomingChatMessageListener {

	private SimulationManager parent;
	protected static final Double BAD_FITNESS = 0.0;
	protected String packageFolder = null;

	public void setSimulationManager(final SimulationManager manager) {
		assert (parent) != null;
		this.parent = manager;
		System.out.println("A new MessageEventCoordinatorImpl bound to MA =  " + manager.getClientID());
	}

	@Override
	public void newIncomingMessage(EntityBareJid sender, Message msg, org.jivesoftware.smack.chat2.Chat chat) {
		MessageSerializer serializer = new MessageSerializer();
		RunSimulationMessage runSimulation = serializer.fromJson(msg.getBody());
		System.out.println(
				"Abstract MessageEventCoordinator Line 37...................received runSimulation message from =  "+ sender);
		parent.setOptimizationID(runSimulation.getId());
		parent.setSimulationID(runSimulation.getSid());
		parent.setSimulationConfiguration(runSimulation.getConfiguration());
		// The candidate is handled in different ways by the several simulators
		this.handleCandidate(sender, runSimulation.getCandidate());

	}

	/**
	 * Method to serialize the candidate in a file
	 * 
	 * @return the result of the serialization (true: OK, false error)
	 */
	protected boolean serializeCandidate(final String candidate) {
		try {
			String filePath = packageFolder + File.separator + "src" + File.separator + "candidate.h";
			System.out.println("Saving the candidate in " + filePath);
			Files.write(Paths.get(filePath), candidate.getBytes());
			return true;
		} catch (IOException e) {
			System.out.println("Error serializing the file");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Method to be implemented with the handler for the candidate
	 * 
	 * @param sender
	 * 
	 * @param candidate:
	 *            the candidate to be evaluated
	 */
	protected abstract void handleCandidate(EntityBareJid sender, final String candidate);
}
