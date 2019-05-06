package eu.cpswarm.optimization.messages;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class StartOptimizationMessage extends Message {
	public static final String TYPE_NAME = "StartOptimization";

	protected String optimizationConfiguration;
	protected String simulationConfiguration;

	@SerializedName("SimulationManagers")
	protected List<String> simulationManagers;

	public StartOptimizationMessage(String id, String description, String optimizationConfiguration,
			String simulationConfiguration, List<String> simulationManagers) {
		super(TYPE_NAME, id, description);
		this.optimizationConfiguration = optimizationConfiguration;
		this.simulationConfiguration = simulationConfiguration;
		this.simulationManagers = simulationManagers;
	}

	public StartOptimizationMessage() {
		this.type = TYPE_NAME;
	}

	public String getOptimizationConfiguration() {
		return optimizationConfiguration;
	}

	public String getSimulationConfiguration() {
		return simulationConfiguration;
	}

	public List<String> getSimulationManagers() {
		return simulationManagers;
	}
}
