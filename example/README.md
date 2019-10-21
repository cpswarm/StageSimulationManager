# Usage of CPSWarm stage-simulation-manager Image
This repository provides a Stage Simulation Manager that includes a XMPP client and interfaces with a Stage simulator. In this example folder, a `Dockerfile-Stage-Manager` file starting from Cpswarm pre-build images using Ubuntu16.04 and Openjdk-8 has been used to dockerize it as a [`stage-simulation-manager`](https://cloud.docker.com/u/cpswarm/repository/docker/cpswarm/stage-simulation-manager) image for a further dockerization together with your ros simulations.

Similar steps are available for other operating systems and JDK may work.

Build stage-simulation-manager image :
```bash
sudo docker build . --tag=stage-simulation-manager:latest -f Dockerfile-Stage-Manager
```

### Structure of example folder
``` java
    example/
        resources/
            manager.xml
        Dockerfile-Stage-Manager      -- Docker file for creating the stage-simulation-manager image
		Dockerfile-Stage-Simulation   -- Docker file for creating the stage-simulation image
		JVM-Certifivcation.pem
		launch_SM.sh    			  -- script for launching the simulation manager
		ws/
			build.sh                  -- script for compiling the ros simulation
			src/
				Ros-package-name/     -- Please put your Ros packages in this src folder
```


The `example` folder provides an example about how to create a docker image based on above pre-build stage-simulation-manager image that will include the Ros packages which can be used to simulate and optimize it using the CPSwarm Simulation and Optimization Environment.

Before dockerizing the ros simulation package starting from the stage-simulation-manager image, follow the steps:

1.  Change the configuration file `manager.xml` in `resources` folder according to the real use case. This file can be used to change some system parameters used by the Stage simulation manager to communicate with other components in the CPSWarm simulation environment.
2.  place the ros simulation packages in the `example/ws/src/` folder
3.  Replace the file `JVM-Certifivcation.pem` used in real case
4.  (If using default setting, skip this step) The `stageManager.jar` has some internal system properies already set inside for configuring its launching environment, so user can set individual System properties with the -D option for passing the command line parameters to override the properties listed below:

   ``` bash
   org.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog,\
   org.eclipse.jetty.LEVEL=WARN,\                           # Avoid verbose superfluous debug info printed on Stdin.
   logback.configurationFile=resources/logback.xml,\        # Configuration of ch.qos.logback.core bundle
   org.apache.felix.log.storeDebug=false,\          # Configuration of org.apache.felix.log bundle to determine whether or not debug messages will be stored in the history
   org.osgi.service.http.port=8080,\                # The default port used for Felix servlets and resources available via HTTP
   ros.core.native=true,\                      # Indicating if launching the installed ROS system or the rosjava ROScore implementation of the rosjava_core project
   verbosity=2,\                               # Selected verbosity level: 0 NO_OUTPUT, 1 ONLY_FITNESS_SCORE, 2 ALL
   ros.master.uri=http://localhost:11311,\     # It is used to manually indicate the Ros environment variable in case the user doesn't set it during the Ros installation
   Manager.config.file.manager.xml=resources/manager.xml,\     # Specify the location of the configuration file of the Stage simulation manager
   javax.net.ssl.trustStorePassword=changeit,\
   javax.net.ssl.trustStore=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/security/cacerts,\                 # Replace path of the JDK with the user's value in real use case
   org.osgi.framework.trust.repositories=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/security/cacerts      # Replace path of the JDK with the user's value in real use case
   ```
5.  Create `stage-simulation` image

*  Build stage-simulation image in repository root folder
   ``` bash
   sudo docker build example/ --tag=stage-simulation:latest -f example/Dockerfile-Stage-simulation
   ```
*  Run stage-simulation image and start Stage simulation manager

   The "launch_SM.sh" script which launches the Stage simulation manager will be executed by default once the image is run, the properties set by `-D` option will be delivered to the `stageManager.jar`.
   ```bash
   sudo docker run -it cpswarm/stage-simulation:latest -Dverbosity=2
   ```

*  Run stage-simulation image and enter the `bash` shell
   ```bash
   $ sudo docker run -it cpswarm/stage-simulation:latest --entrypoint /bin/bash
   $ ./launch_SM.sh -Dverbosity=2
   ```
