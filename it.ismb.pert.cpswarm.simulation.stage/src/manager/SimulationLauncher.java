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
import be.iminds.iot.ros.util.NativeRosNode.VERBOSITY_LEVELS;


@Component(factory = "it.ismb.pert.cpswarm.stageSimulationLauncher.factory"
)
public class SimulationLauncher implements Runnable {

	private boolean canRun = true;
	private String packageName = null;
	private SimulationManager parent = null;
	private ComponentInstance commandInstance = null;
	private RosCommand roslaunch = null;
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
			 roslaunch = (RosCommand) commandInstance.getInstance();
			roslaunch.startSimulation();
		} catch (Exception e) {
			e.printStackTrace();
		}/* finally {
			if (commandInstance != null) {
				commandInstance.dispose();
			}
		}*/
	}

	@Deactivate
	void deactivate() {		
		if(SimulationManager.CURRENT_VERBOSITY_LEVEL.equals(SimulationManager.VERBOSITY_LEVELS.ALL)) {
			System.out.println("Simulation launcher is deactived");
		}
		
	/*	Process proc;
		try {
			ProcessBuilder builder = new ProcessBuilder(new String[] { "/bin/bash", "-c", "killall -2 roslaunch"});
				proc = builder.start();
				proc.waitFor();
				proc = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		try {
			Thread.sleep(25000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}*/
	/*	if (roslaunch != null) {
			roslaunch.deactivate();
		}*/
		if (commandInstance != null) {
			commandInstance.dispose();
		}
	/*	try {
			ProcessBuilder builder = new ProcessBuilder(new String[] { "/bin/bash", "-c", "ps -aux; killall -2 stageros; killall -2 python;killall -s SIGINT record"});
		
				builder.inheritIO();
				proc = builder.start();
				proc.waitFor();
				proc = null;
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		Process proc;
		try {
			ProcessBuilder builder = new ProcessBuilder(new String[] { "/bin/bash", "-c", "killall -s SIGINT roscore"});	
				builder.inheritIO();
				proc = builder.start();
				proc.waitFor();
				proc = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	/*	
		
		try {
			System.out.println("killing rosnode\n");
			proc = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "ps -aux;rosnode kill -a; ps -aux;killall -s SIGINT roscore; ps -aux;killall -s SIGINT rosmaster;ps -aux;"});
			String line="";
			BufferedReader input =  
					new BufferedReader  
					(new InputStreamReader(proc.getInputStream()));  
			while ((line = input.readLine()) != null) { 
				//    if(line.contains("record"))
				    	System.out.println(line);
			}
			int exit = proc.waitFor();
			if(exit!=0)
				System.out.println("killall -9 python failed");
		//	proc.destroy;
			proc = null;
			input.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}	*/	
		
	}

	public void setCanRun(boolean canRun) {
		this.canRun = canRun;
	}

}
