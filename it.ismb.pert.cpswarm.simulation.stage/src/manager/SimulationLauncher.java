package manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import simulation.SimulationManager;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import be.iminds.iot.ros.util.RosCommand;


@Component(factory = "it.ismb.pert.cpswarm.stageSimulationLauncher.factory"
)
public class SimulationLauncher implements Runnable {

	private boolean canRun = true;
	private String packageName = null;
	private SimulationManager parent = null;
	private ComponentInstance commandInstance = null;
	private ComponentFactory rosCommandFactory; // used to roslaunch the simulation

	@Reference(target = "(component.factory=it.ismb.pert.cpswarm.rosCommand.factory)")
	public void getRosCommandFactory(final ComponentFactory s) {
		this.rosCommandFactory = s;
	}

	@Activate
	public void activate(BundleContext context, Map<String, Object> properties) throws Exception {
		for (Entry<String, Object> entry : properties.entrySet()) {
			String key = entry.getKey();
			if (key.equals("SimulationManager")) {
				parent = (SimulationManager) entry.getValue();
			} else if (key.equals("packageName")) {
				this.packageName = (String) entry.getValue();
			}
		}
		assert (parent) != null;
		assert (packageName) != null;
		if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println(" Instantiate a Simulation Launcher");
		}	
	}

	@Override
	public void run() {
		try {
			String params = parent.getSimulationConfiguration();
			/*	if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println("Launching the simulation for package: "+packageName + " with params: "+ params);
			}*/
			// roslaunch storage stage_complete.launch gui:=true
			Properties props = new Properties();
			props.put("rosWorkspace", parent.getCatkinWS());
			props.put("ros.package", packageName);
			props.put("ros.node", parent.getLaunchFile());
			if (params != null) {
				props.put("ros.mappings", params);
			}
			commandInstance = this.rosCommandFactory.newInstance((Dictionary) props);
			RosCommand roslaunch = (RosCommand) commandInstance.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (commandInstance != null) {
				commandInstance.dispose();
			}
		}

	}

	@Deactivate
	void deactivate() {		
		if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println("Simulation launcher is deactived");
		}
		Process proc;
			try {
			proc = Runtime.getRuntime().exec("killall -9 stageros");
			proc.waitFor();
			proc.destroy();
			proc = null;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}		
		try {
			Thread.sleep(25000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}	
		try {
			proc = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "killall -9 roslaunch; killall -9 python" });
			String line="";
			BufferedReader input =  
					new BufferedReader  
					(new InputStreamReader(proc.getInputStream()));  
			while ((line = input.readLine()) != null) {  
					System.out.println(line);
			} 
			proc.waitFor();
			proc.destroy();
			proc = null;
			input.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void setCanRun(boolean canRun) {
		this.canRun = canRun;
	}

}
