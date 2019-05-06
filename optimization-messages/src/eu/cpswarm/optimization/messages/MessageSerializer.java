package eu.cpswarm.optimization.messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

public class MessageSerializer {

	protected Gson gson;
	protected Gson OptimizationPogressGson;
	protected Gson OptimizationStartedGson;
	protected Gson OptimizationCancelledGson;
	protected Gson SimulationResultGson;

	public MessageSerializer() {
		RuntimeTypeAdapterFactory<Message> typeFactory = RuntimeTypeAdapterFactory.of(Message.class, "type")
				.registerSubtype(StartOptimizationMessage.class, StartOptimizationMessage.TYPE_NAME)
				.registerSubtype(GetProgressMessage.class, GetProgressMessage.TYPE_NAME)
				.registerSubtype(CancelOptimizationMessage.class, CancelOptimizationMessage.TYPE_NAME)
				.registerSubtype(OptimizationStartedMessage.class, OptimizationStartedMessage.TYPE_NAME)
				.registerSubtype(OptimizationCancelledMessage.class, OptimizationCancelledMessage.TYPE_NAME)
				.registerSubtype(OptimizationProgressMessage.class, OptimizationProgressMessage.TYPE_NAME)
				.registerSubtype(RunSimulationMessage.class, RunSimulationMessage.TYPE_NAME)
				.registerSubtype(SimulationResultMessage.class, SimulationResultMessage.TYPE_NAME)
				.registerSubtype(SimulatorConfiguredMessage.class, SimulatorConfiguredMessage.TYPE_NAME);

		gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();

	}

	@SuppressWarnings("unchecked")
	public <T extends Message> T fromJson(String json) {

		return (T) gson.fromJson(json, Message.class);
	}

	public String toJson(Message message) {

		return gson.toJson(message);
	}

}