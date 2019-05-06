package be.iminds.iot.ros.util;

import java.io.File;
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate=true,
	property = {"osgi.command.scope=ros", 
				"osgi.command.function=roslaunch",
				"osgi.command.function=rosrun",
				"osgi.command.function=catkinBuild"},
	service = {RosCommandInjection.class}
)
public class RosCommandInjection {
	private ComponentFactory rosCommandFactory;

	// roslaunch command with parameters
	public void roslaunch(String rosWorkspace, String pkg, String node, String... parameters) {
		if (!rosWorkspace.endsWith(File.separator)) {
			rosWorkspace += File.separator;
		}
		Properties props = new Properties();
		props.put("rosWorkspace", rosWorkspace);
		props.put("ros.package", pkg);
		props.put("ros.node", node);
		if (parameters != null) {
			for (String parameter : parameters) {
				if (!parameter.contains(":=")) {
					System.out.println("Invalid parameter: " + parameter);
					continue;
				} else {
					String[] parts = parameter.split(":=");
					props.put(parts[0], parts[1]);
				}
			}
		}
		ComponentInstance commandInstance = null;
		try {
			commandInstance = this.rosCommandFactory.newInstance((Dictionary) props);
			RosCommand roslaunch = (RosCommand) commandInstance.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (commandInstance != null)
				commandInstance.dispose();
		}

	}

	// rosrun command with parameters and a world file
	public void rosrun(String rosWorkspace, String pkg, String node, String... parameters) {
		if (!rosWorkspace.endsWith(File.separator)) {
			rosWorkspace += File.separator;
		}
		Properties props = new Properties();
		props.put("rosWorkspace", rosWorkspace);
		props.put("ros.package", pkg);
		props.put("ros.node", node);
		if (parameters != null) {
			for (String parameter : parameters) {
				if (parameter.contains(":=")) {
					String[] parts = parameter.split(":=");
					props.put(parts[0], parts[1]);
				} else if (parameter.contains(".world")) {
					props.put("worldFile.path", parameter);
				} else {
					System.out.println("Invalid parameter: " + parameter);
					continue;
				}

			}
		}
		ComponentInstance instance = null;
		try {
			instance = this.rosCommandFactory.newInstance((Dictionary) props);
			RosCommand rosrun = (RosCommand) instance.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			instance.dispose();
		}
	}

	public void catkinBuild(String workspace) {
		System.out.println("start building workspace: " + workspace);
		if (!workspace.endsWith(File.separator)) {
			workspace += File.separator;
		}
		Properties props = new Properties();
		props.put("ros.buildWorkspace", workspace);
		ComponentInstance instance = null;
		try {
			instance = this.rosCommandFactory.newInstance((Dictionary) props);
			RosCommand catkinBuild = (RosCommand) instance.getInstance();
			catkinBuild.buildWorkspace(); // Guarantee building process finished before instance destroy
		} catch (Exception e) {
			System.err.println("Error when building workspace: " + workspace);
			e.printStackTrace();
		} finally {
			if (instance != null)
				instance.dispose();
		}

	}

	@Reference(target = "(component.factory=it.ismb.pert.cpswarm.rosCommand.factory)")
	public void getRosCommandFactory(final ComponentFactory s) {
		this.rosCommandFactory = s;
		System.out.println(" \n RosCommandInjection gets a RosCommand ComponentFactory ...");

	}
}
