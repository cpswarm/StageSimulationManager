package eu.cpswarm.optimization.messages;

public class GetProgressMessage extends Message {
	public static final String TYPE_NAME = "GetProgress";

	public GetProgressMessage(String id, String description) {
		super(TYPE_NAME, id, description);
		this.type = TYPE_NAME;
	}

	public GetProgressMessage() {
		this.type = TYPE_NAME;
	}

}
