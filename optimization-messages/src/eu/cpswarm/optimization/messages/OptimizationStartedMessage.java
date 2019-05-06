package eu.cpswarm.optimization.messages;

public class OptimizationStartedMessage extends ReplyMessage {
	public static final String TYPE_NAME = "OptimizationStarted";

	public OptimizationStartedMessage(String id, String description, ReplyMessage.Status operationStatus) {
		super(TYPE_NAME, id, description, operationStatus);
	}

	public OptimizationStartedMessage() {
		this.type = TYPE_NAME;
	}
}
