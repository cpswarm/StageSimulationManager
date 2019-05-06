package eu.cpswarm.optimization.messages;

import com.google.gson.annotations.SerializedName;

public abstract class ReplyMessage extends Message {

	public enum Status {
		@SerializedName("ok")
		OK, 
		@SerializedName("error")
		ERROR
	}

	@SerializedName("status")
	protected Status operationStatus;

	public ReplyMessage(String type, String id, String description, Status operationStatus) {
		super(type, id, description);
		this.operationStatus = operationStatus;
	}

	public ReplyMessage() {

	}

	public Status getOperationStatus() {
		return operationStatus;
	}

}
