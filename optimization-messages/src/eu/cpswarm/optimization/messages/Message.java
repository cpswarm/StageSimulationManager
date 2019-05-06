package eu.cpswarm.optimization.messages;

import com.google.gson.annotations.SerializedName;

public abstract class Message {

	@SerializedName("ID")
	protected String id;

	public void setId(String id) {
		this.id = id;
	}

	@SerializedName("type")
	protected String type;

	protected String description;

	protected Message(String type, String id, String description) {
		this.type = type;
		this.id = id;
		this.description = description;
	}

	public Message() {

	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}
}
