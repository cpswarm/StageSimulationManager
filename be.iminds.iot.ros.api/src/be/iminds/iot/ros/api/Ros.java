/*******************************************************************************
 *  ROSOSGi - Bridging the gap between Robot Operating System (ROS) and OSGi
 *  Copyright (C) 2015, 2016  imec - IDLab - UGent
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
package be.iminds.iot.ros.api;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.ros.node.NodeMain;

public interface Ros {

	URI masterURI();
	
	String masterHost();
	
	int masterPort();
	
	String distro();
	
	String namespace();
	
	
	File root();
	
	List<File> packagePath();
	

	URI nodeURI(String node);
	
	Collection<String> nodes();
	
	Collection<String> topics();
	
	Collection<String> publishers(String topic);
	
	Collection<String> subscribers(String topic);
	
	String topicType(String topic);
	
	Collection<String> services();
	
	Collection<String> providers(String service);
	
	String env();
	
	void setParameter(String key, Object value);
	
	<T> T getParameter(String key, Class<T> type);
	
	
	void addNode(NodeMain node);
	
	void removeNode(NodeMain node);
}
