# Usage of CPSWarm stage-simulation-manager Image
The [`cpswarm/stage-simulation-manager`](https://cloud.docker.com/u/cpswarm/repository/docker/cpswarm/stage-simulation-manager) image provides a Stage Simulation Manager that includes a XMPP client and interfaces with a Stage simulator. It is a Cpswarm pre-build images using Ubuntu16.04 and Openjdk-8 and allows a further dockerization together with your ros simulations starting from it.

Similar steps are available for other OS and JDK versions.

### Structure of example folder
``` java
    example/
        resources/
            manager.xml               -- Configuration file for connecting to XMPP server and simulation tool capability
        Dockerfile-Stage-Simulation   -- Dockerfile for creating the stage-simulation image
        JVM-Certifivcation.pem        -- Certificate extracted from the XMPP server
        launch_SM.sh                  -- Script for launching the simulation manager
        Ros-simulation-package/       -- This is the Ros packages folder
```


The `example` folder provides an example about how to create a docker image based on above pre-build stage-simulation-manager image that will include the Ros packages which can be used to simulate and optimize it using the CPSwarm Simulation and Optimization Environment.

Before dockerizing the ros simulation package starting from the stage-simulation-manager image, follow the steps:

1.  Change the configuration file `manager.xml` in `resources` folder according to the real use case. This file can be used to change some system parameters used by the Stage simulation manager to communicate with other components in the CPSWarm simulation environment.
2.  place the ros simulation packages here in the `example` folder, it will be moved to an existing ros workspace `/home/catkin_ws/src/` folder coming from the `cpswarm/stage-simulation-manager` image.
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

*  Build stage-simulation image in the example folder
   ``` bash
   sudo docker build . --tag=stage-simulation:latest -f Dockerfile-Stage-Simulation
   ```
*  Run stage-simulation image and start Stage simulation manager

   The "launch_SM.sh" script which launches the Stage simulation manager will be executed by default once the image is run, the properties set by `-D` option will be delivered to the `stageManager.jar` coming from the cpswarm/stage-simulation-manager image.
   ```bash
   sudo docker run -it stage-simulation:latest -Dverbosity=2
   ```

*  Run stage-simulation image and enter the `bash` shell
   ```bash
   $ sudo docker run -it stage-simulation:latest --entrypoint /bin/bash
   ~# ./launch_SM.sh -Dverbosity=2
   ```
   
*  Update the image in dockerhub and then deploy it using kubernetes

   This can be done using the features provided by the Simulation and Optimization Orchestrator. To deploy the image that you have created you can use the file deployment_stage_example.json, changing the name of the container to be deployed in /template/spec/containers/image and the number of containers to be deployed in /spec/replicas. This snippet of code deploys only stage simulation images, please consider that you can add in the same file also more than one deployment, as in the example deployment_stage_ot_example.json, where also the optimization tool is deployed. 
   Note 1: the format used is a simplified version of the one used to describe a Kubernetes deployment. If you don't want to use the SOO to deploy the images, but directly work on the Kubernetes API you have to use a different file.
   Note 2: in the example, the containers have a node selector set in /template/spec/containers/nodeSelector, this allow to select the nodes in which it can be installed, for example in this case, it will be installed in all the nodes with the label component:stage (the label can be  add with the command: kubectl label node <node_to_be_labeled> component=stage). If nodeSelector is not used the image will be installed in one of the nodes of the cluster with enough resources to run it.
   Note 3: the previous deployment file are designed to run the stage simulation without GUI for optimization purposes, if you want to deploy it with Kubernetes and in the same time be able to access to the Stage GUI, you have to add to the simulation image also VNC server and then deploy also the service as shown in deployment_stage_vnc_example.json
