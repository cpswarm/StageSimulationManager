package eu.cpswarm.optimization.messages;

public class OptimizationProgressMessage extends ReplyMessage {
	public static final String TYPE_NAME = "OptimizationProgress";
	protected double progress;
	protected double fitnessValue;
	protected String candidate;

	public OptimizationProgressMessage(String id, String description, ReplyMessage.Status operationStatus,
			double progress, double fitnessValue, String candidate) {
		super(TYPE_NAME, id, description, operationStatus);
		this.progress = progress;
		this.fitnessValue = fitnessValue;
		this.candidate = candidate;
	}

	public OptimizationProgressMessage() {
		this.type = TYPE_NAME;
	}

	public double getProgress() {
		return progress;
	}

	public double getFitnessValue() {
		return fitnessValue;
	}

	public String getCandidate() {
		return candidate;
	}

	public String toString() {
		return new String(
				"type=" + type + ", id=" + id + ", description= " + description + ", status= " + operationStatus
						+ ", progress=" + progress + ", fitness=" + fitnessValue + ", candidate =..." + candidate);
	}
}
