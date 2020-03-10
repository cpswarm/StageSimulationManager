## StageManagerBundle

This repository contains the OSGI bundles only for creating Stage simulation manager, the necessary dependency bundles from the [`CPswarm-common`](https://git.pertforge.ismb.it/rzhao/cpswarm-common) repository have been adding in the `local` repository of this `cnf` project which sets up the bnd workspace.

## Setup
Install Ros system and set up the Ros environment variable `ROS_MASTER_URI=http://localhost:11311` by default in order to set up your local machine as a ROS master.
``` bash
$ source /opt/ros/kinetic/setup.bash
$ printenv | grep ROS
```
Be sure you have installed the BND tool and [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) in your system
``` bash
sudo apt-get update
sudo apt-get install bnd
```
Install BNDTool IDE in Eclipse
``` bash
Help-> Eclipse Markerplace-> search 'Bndtools'-> Installed->Restart Eclipse.
```
## Installation

You can directly import all sub-projects in this repository by using the option `Paste Repository Path or URI` from the Git Repositories View in Eclipse.

to open Bnd Repositories View:
```bash
In Eclipse: Window-> Show View-> Other-> Bndtools-> Repositories.
```
>Note: the `cnf` project is a fixed name wrote in the source code of Bnd IDE, it makes a directory a workspace with some built-in plugins and external plugins, just like the .git directory does for git. So don't change its name.\
>So when you want to import the second bnd repository which also contains a cnf project, you must manually clone and copy the other projects without the second cnf project into the first cloned Git repository folder and then import them to keep all sub-projects staying with the cnf, because only one `cnf` project is allowed in a workspace, as for the lacked dependencies, you can manually set up the cnf according to the last session of `Dependency Bundles Updation` in README.md.

Click [`here`](https://bnd.bndtools.org/chapters/123-tour-workspace.html) to see the official guid page of cnf and bnd workspce.

## Configuration

Go to project `it.ismb.pert.cpswarm.simulation.stage`
*  **stageManager.bndrun**   
   it is a run descriptor file `stageManager.bndrun` which descrips the needed Felix and all dependency bundles to launch the stage manager and with the following `-runproperties:` instruction inside for configuring the launching environment:
   
   To set individual System properties with the `-D` option to pass the command line parameters to override the properties listed in the `-runproperties:` when running the manager,

   for example:
   ``` bash
   java -Dverbosity=0 -jar stageManager.jar
   ```
   ``` bash
   -runproperties: \
        org.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog,\
	    org.eclipse.jetty.LEVEL=WARN,\                           # Avoid verbose superfluous debug info printed on Stdin.
	    logback.configurationFile=resources/logback.xml,\        # Configuration of ch.qos.logback.core bundle
	    org.apache.felix.log.storeDebug=false,\          # Configuration of org.apache.felix.log bundle to determine whether or not debug messages will be stored in the history
	    org.osgi.service.http.port=8080,\                # The default port used for Felix servlets and resources available via HTTP
	    ros.core.native=true,\                      # Indicating if launching the installed ROS system or the rosjava ROScore implementation of the rosjava_core project
	    verbosity=2,\                               # Selected verbosity level: 0 NO_OUTPUT, 1 ONLY_FITNESS_SCORE, 2 ALL
	    ros.master.uri=http://localhost:11311,\     # It is used to manually indicate the Ros environment variable in case the user doesn't set it during the Ros installation
	    Manager.config.file.manager.xml=resources/manager.xml,\     # Specify the location of the configuration file of the Gazebo simulation manager
	    javax.net.ssl.trustStorePassword=changeit,\
	    javax.net.ssl.trustStore=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/security/cacerts,\                 # Replace path of the JDK with the user's value in real use case
	    org.osgi.framework.trust.repositories=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/security/cacerts      # Replace path of the JDK with the user's value in real use case
    ```  
*  **resources/manager.xml**

   It is a configuration file can be used to change some system parameters used by the Stage simulation manager to communicate with other components in the software. 

   Here is the default values, set the values to be used in the real use case
   ``` xml
   <settings>
   <uuid>22e6dbf2-ca2f-437f-8397-49daada26042</uuid> <!-- If present, indicates the UUID to be used in the JID (it is useful to have fixed JIDs) -->
   <serverURI>123.123.123.123</serverURI>  <!-- URI of the XMPP server  -->
   <serverName>pippo.pluto.it</serverName>  <!-- name of the XMPP server  -->
   <serverPassword>server</serverPassword>  <!-- Password to be used to connect to the XMPP server -->
   <dataFolder>/home/cpswarm/Desktop/output/</dataFolder> <!-- Data folder where to store the data -->
   <dimensions>2</dimensions> <!-- dimensions supported by the wrapped simulator -->
   <maxAgents>8</maxAgents> <!-- max agents supported by the wrapped simulator -->
   <optimizationUser>frevo</optimizationUser> <!-- XMPP user of the optimization tool -->
   <orchestratorUser>orchestrator</orchestratorUser> <!-- XMPP user of the orchestrator -->
   <rosFolder>/home/cpswarm/Desktop/test/src/</rosFolder> <!-- folder of the ROS workspace, it must be the <src> folder -->
   <monitoring>true</monitoring> <!--  indication if the monitoring GUI has to be used or not  -->
   <mqttBroker>tcp://123.123.123.123:1883</mqttBroker> <!--  MQTT broker to be used if the monitoring is set to true  -->
   <timeout>90000</timeout> <!-- Timeout in milliseconds for one simulation -->
   <fake>false</fake> <!-- Indicate if real simulations need to be done or not -->
   </settings>
   ```

## Run Stage Simulation Manager

*  ***Way1:*** Run the `stageManager.bndrun` in the project folder from terminal, or with the `-D` option
   ``` bash
   $ bnd package stageManger.bndrun
   $ java -jar stageManager.jar
   ```
*  ***Way2:*** Run the `stageManager.bndrun` in Eclipse

   Right click `stageManager.bndrun` -> Run as -> Bnd OSGi Run Launcher
   
   or you can click the `Run OSGI` button in the right-top corner from `Run` tab of this bndrun file
*  ***Way3:*** Export the bndrun file as excutable jar in Eclipse

   Click the `Export` button in the stageManager.bndrun file -> Selete Export Destination which should be in the same folder with this bndrun file, because the excutable jar has to access to the `Resources/Manager.xml` configuration file. 
   
   Run the exported jar in it's folder:
   ``` bash
   $ java -jar stageManager.jar
   ```
## Debug

Set your break-point in java classes, Click the `Debug OSGI` button in the right-top corner from `Run` tab of this bndrun file, the next debug steps are the same with generic java projects.

## Dependency Bundles Updation

Usually the bundle verion is defined by the `Bundle-Version:` instruction in the bnd.bnd file for each project. when you modify something of a bundle in Eclipse, the Bndtool IDE will autocatically update the bundle jar in the `generated` folder. Double click the generated bundle, you can see its version and some other infomation.

When a project refers to the other project in the same workspace, the jar to be refered are the ones in the generated folder of each project, the version of the refered jar in the `-buildpath:` instruction in bnd.bnd file is usually equal to `latest` if you add this bundle from the `build` tab. but you can also set the version equal to the real value from `source` tab. Anyway, Bndtool will hundle any other things for you.

As we said before, the `cnf` project has some build-in plugins and external plugins, they provides lots of available bundles. we can manually add and update the provided bundles

***steps:***
*  Copy the new version of a jar into any place in Eclipse;
*  Drag and drop this jar to Local repository of the `cnf` project from the `Repositories` view in Eclipse to overwrite the old jar;
*  Extend the Local repository, if the version is different with the old one, you can see multiple versions of the jar;
*  In case there are multiple versions, you should modify the version of this new jar in the bnd.bnd file for each project which refers this dependency bundle, and the .bndrun file 

In a conclusion, if in the same workspace, just need to change the source of the refered project, Bndtool will detect where is the latest version. If subdeviding the workspace, you need to drag the new jar in local repository in the bnd Repositories View, and if the version is different, also change the version in the bnd.bnd for each project.

## Contributing
Contributions are welcome. 

Please fork, make your changes, and submit a pull request. For major changes, please open an issue first and discuss it with the other authors.

## Affiliation
![CPSwarm](https://github.com/cpswarm/template/raw/master/cpswarm.png)  
This work is supported by the European Commission through the [CPSwarm H2020 project](https://cpswarm.eu) under grant no. 731946.



