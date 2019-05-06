package eu.cpswarm.optimization.messages;

import com.google.gson.annotations.SerializedName;

public class RunSimulationMessage extends Message {
	public static final String TYPE_NAME = "RunSimulation";

	@SerializedName("SID")
	protected String sid;
	protected String configuration;
	protected String candidate;

	public RunSimulationMessage(String id, String description, String sid, String configuration, String candidate) {
		super(TYPE_NAME, id, description);
		this.sid = sid;
		this.configuration = configuration;
		this.candidate = candidate;
	}

	public RunSimulationMessage() {
		this.type = TYPE_NAME;

	}

	public String getSid() {
		return sid;
	}

	public String getConfiguration() {
		return configuration;
	}

	public String getCandidate() {
		return candidate;
	}

}
