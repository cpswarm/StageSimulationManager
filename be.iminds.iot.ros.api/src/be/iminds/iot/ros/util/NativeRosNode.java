/*******************************************************************************
 *  ROSOSGi - Bridging the gap between Robot Operating System (ROS) and OSGi
 *  Copyright (C) 2015, 2017  imec - IDLab - UGent
 *
 *  This file is part of DIANNE  -  Framework for distributed artificial neural networks
 *
 *  DIANNE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *   
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *  Contributors:
 *      Tim Verbelen, Steven Bohez
 *******************************************************************************/
package be.iminds.iot.ros.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import be.iminds.iot.ros.api.Ros;

public class NativeRosNode {

	protected Process process;

	protected String name;
	protected String rosPackage;
	protected String rosNode;
	protected String rosWorkspace;
	protected List<String> rosParameters = new ArrayList<>();

	public NativeRosNode() {
	}

	public NativeRosNode(String pkg, String node) {
		this.rosPackage = pkg;
		this.rosNode = node;
	}

	public NativeRosNode(String pkg, String node, String... parameters) {
		this.rosPackage = pkg;
		this.rosNode = node;
		for (String parameter : parameters) {
			System.out.println("************************* parameter: " + parameter);
			if (!parameter.contains(":=") && !parameter.contains(".world")) {
				System.out.println("Invalid parameter: " + parameter);
				continue;
			} else {
				// in case the parameter is a world file, then specify its path
				rosParameters.add(parameter);
			}
		}
	}

	@Activate
	protected void activate(Map<String, Object> properties) throws Exception {
		// this also allows to build a ROS node driven by configuration
		if (properties.containsKey("ros.buildWorkspace")) {
			rosWorkspace = (String) properties.get("ros.buildWorkspace");
		} else {
			for (Entry<String, Object> entry : properties.entrySet()) {
				String key = entry.getKey();
				// specific ros properties
				if (key.equals("rosWorkspace")) {
					rosWorkspace = (String) entry.getValue();
				} else if (key.equals("ros.package")) {
					rosPackage = (String) entry.getValue();
				} else if (key.equals("ros.node")) {
					rosNode = (String) entry.getValue();
				} else if (key.equals("ros.mappings")) {
					String extraMappings = (String) entry.getValue();
					if (extraMappings != null) {
						String[] mappings = extraMappings.split(",");
						for (String mapping : mappings) {
							rosParameters.add(mapping);
						}
					}
				} else if (key.equals("name")) {
					name = entry.getValue().toString();
					rosParameters.add("name:=" + name);
				} else if (key.equals("worldFile.path")) {
					String worldFile = entry.getValue().toString();
					rosParameters.add(worldFile);
				} else if (!key.contains(".")) { // ignore parameters with "." , most likely OSGi service props
					rosParameters.add(key + ":=" + entry.getValue());
				}
			}

			boolean roslaunch = false;
			if (rosNode.endsWith(".launch")) {
				roslaunch = true;
			}
			try {
				List<String> cmd = new ArrayList<>();
				//echo $ROS_PACKAGE_PATH ; rospack find emergency_exit ; 
				String source = "source /opt/ros/kinetic/setup.bash ; source " + rosWorkspace + "devel/setup.bash ; ";
				cmd.add("/bin/bash");
				cmd.add("-c");

				if (roslaunch) {
					source += "roslaunch ";
				} else {
					source += "rosrun ";
				}
				source += rosPackage + " " + rosNode;

				// use name for setting the node name
				if (name != null) {
					source += " __name:=" + name;
				}
				// add params to command
				for (String str : rosParameters) {
					source += " " + str;
				}

				cmd.add(source);
				System.out.println("\n=================running command = " + cmd + " ==================\n");
				ProcessBuilder builder = new ProcessBuilder(cmd);
				builder.inheritIO();
				process = builder.start();
				process.waitFor();
				System.out.println("Ros command exits \n");
			} catch (Exception e) {
				System.err.println("Error launching native ros node " + rosPackage + " " + rosNode);
				throw e;
			}
		}
	}

	public void buildWorkspace() throws Exception {
		assert (rosWorkspace) != null;
		ProcessBuilder builder = null;
		try {
			String[] cmd = new String[] { "/bin/bash", "-c", "cd " + rosWorkspace + " ; catkin build " };
			builder = new ProcessBuilder(cmd);
			builder.inheritIO();
			process = builder.start();
			process.waitFor();
			System.out.println("build workspace finished");
		} catch (Exception e) {
			throw e;
		} finally {
			builder = null;
	//		deactivate(); // automatically destroy the service instance after building.
		}
	}

	@Deactivate
	protected void deactivate() {
		System.out.println("rosComand is deactivated");
		// help ... destroy doesn't gracefully ends the child process ... might not be
		// enough :-/
		if (process != null) {
			process.destroy();
			process = null;
		}
	}

	@Reference
	void setROSEnvironment(Ros e) {
		// make sure ROS core is running before activating a native node
	}

}
