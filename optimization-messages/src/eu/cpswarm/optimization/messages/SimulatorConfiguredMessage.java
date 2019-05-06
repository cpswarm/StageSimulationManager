package eu.cpswarm.optimization.messages;

public class SimulatorConfiguredMessage extends ReplyMessage {

	public static final String TYPE_NAME = "SimulationConfigured";

	public SimulatorConfiguredMessage(String id, String description, Status operationStatus) {
		super(TYPE_NAME, id, description, operationStatus);
	}

	public SimulatorConfiguredMessage() {
		this.type = TYPE_NAME;
	}

}
