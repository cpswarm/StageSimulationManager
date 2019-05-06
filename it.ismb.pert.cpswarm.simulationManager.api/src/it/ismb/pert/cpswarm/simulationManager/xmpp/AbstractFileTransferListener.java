package it.ismb.pert.cpswarm.simulationManager.xmpp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;

import eu.cpswarm.optimization.messages.MessageSerializer;
import eu.cpswarm.optimization.messages.SimulatorConfiguredMessage;
import it.ismb.pert.cpswarm.simulationManager.api.SimulationManager;

public abstract class AbstractFileTransferListener implements FileTransferListener {

	protected SimulationManager parent = null;
	protected String dataFolder = null;
	protected String rosFolder = null;
	protected String packageName = null;

	public void setSimulationManager(final SimulationManager manager) {
		assert manager != null;
		this.parent = manager;
		this.dataFolder = parent.getDataFolder();
		this.rosFolder = parent.getRosFolder();
		System.out.println("A new FileTransferListenerImpl bound to MA =  " + manager.getClientID());
	}

	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		final IncomingFileTransfer transfer = request.accept();

		String fileToReceive = null;
		// The configuration files are stored in the simulator folder, instead the
		// candidate in the rosFolder
		if (request.getRequestor().compareTo(parent.getOrchestratorJID()) == 0) {
			fileToReceive = dataFolder + request.getFileName();
		} else {
			fileToReceive = rosFolder + request.getFileName();
		}

		try {
			transfer.receiveFile(new File(fileToReceive));

			while (!transfer.isDone()) {
				if (transfer.getStatus() == Status.refused) {
					System.out.println("Transfer refused");
				}
				Thread.sleep(1000);
			}
			System.out.println("File received " + fileToReceive);
			Thread.sleep(1000);
			// If it's the configuration from the Simulation Orchestrator
			if (request.getRequestor().compareTo(parent.getOrchestratorJID()) == 0) {
				final ChatManager chatmanager = ChatManager.getInstanceFor(parent.getConnection());
				final Chat newChat = chatmanager.chatWith(parent.getOrchestratorJID().asEntityBareJidIfPossible());
				packageName = request.getDescription();
				if (unzipFiles(fileToReceive)) {
					System.out.println("SimulationManager configured for optimization " + request.getDescription());
					parent.setOptimizationID(request.getDescription());
					SimulatorConfiguredMessage reply = new SimulatorConfiguredMessage("Simulator configured",
							request.getDescription(), eu.cpswarm.optimization.messages.ReplyMessage.Status.OK);
					MessageSerializer serializer = new MessageSerializer();
					newChat.send(serializer.toJson(reply));
				} else {
					newChat.send("error");
				}
			}
		} catch (final SmackException | IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private FileTypesOrFolderFilter filter = null;

	protected void copy(final Set<String> supportedException, String fromPath, String outputPath) {
		filter = new FileTypesOrFolderFilter(supportedException);
		File currentFolder = new File(fromPath);
		File outputFolder = new File(outputPath);
		scanFolder(supportedException, currentFolder, outputFolder);
	}

	private void scanFolder(final Set<String> supportedException, File currentFolder, File outputFolder) {
		System.out.println("Scanning folder [" + currentFolder + "]... ");
		File[] files = currentFolder.listFiles(filter);
		for (File file : files) {
			if (file.isDirectory()) {
				scanFolder(supportedException, file, outputFolder);
			} else {
				copy(file, outputFolder);
			}
		}
	}

	private void copy(File file, File outputFolder) {
		try {
			System.out.println("\tCopying [" + file + "] to folder [" + outputFolder + "]...");
			InputStream input = new FileInputStream(file);
			OutputStream out = new FileOutputStream(new File(outputFolder + File.separator + file.getName()));
			byte data[] = new byte[input.available()];
			input.read(data);
			out.write(data);
			out.flush();
			out.close();
			input.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract boolean unzipFiles(final String fileToReceive);
}
