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
package be.iminds.iot.ros.msgs.generator;

import org.osgi.service.component.annotations.Component;
import org.ros.internal.message.GenerateInterfaces;

/**
 * Wrap a gogo command around the rosjava message generator
 * 
 * @author tverbele
 *
 */
@Component(service = {Object.class},
	property = {"osgi.command.scope=ros", 
	"osgi.command.function=generate"},
	immediate=true)
public class MessageGenerator {
	
	public void generate(){
		try {
			GenerateInterfaces.main(new String[]{"generated_msgs"});
		} catch(Throwable t){
			t.printStackTrace();
		}
	}

	public void generate(String packagePath){
		try {
			GenerateInterfaces.main(new String[]{"generated_msgs","--package-path="+packagePath});
		} catch(Throwable t){
			t.printStackTrace();
		}
	}
}
