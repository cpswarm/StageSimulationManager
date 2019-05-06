package eu.cpswarm.optimization.messages;

public class OptimizationCancelledMessage extends ReplyMessage {
	public static final String TYPE_NAME = "OptimizationCancelled";

	public OptimizationCancelledMessage(String id, String description, ReplyMessage.Status operationStatus) {
		super(TYPE_NAME, id, description, operationStatus);
	}
	public OptimizationCancelledMessage() {
		this.type = TYPE_NAME;
	}
}
