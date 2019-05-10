## StageSimulationManager

This repository contains the OSGI bundles only for creating Stage simulation manager, the necessary dependency bundles are in the "local" repository of the ***"cnf"*** project that sets up the bnd workspace


## Installation and Configuration

1. clone and import all sub-projects in this repository into eclipse.
 
2. go to project `it.ismb.pert.cpswarm.stageSimulationManager` 
    1. open `stageManager.bndrun`, modify the following properties' values with your path of the cacerts
        >ros.core.native=true,\\\
        >ros.master.uri=http:\/\/localhost:11311,\\\
        >Manager.config.file.manager.xml=resources/manager.xml,\\\
	    >javax.net.ssl.trustStorePassword=changeit,\\\
	    >javax.net.ssl.trustStore=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/security/cacerts,\\\
	    >org.osgi.framework.trust.repositories=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/security/cacerts
    2. build and run the `stageManager.bndrun`
        >bnd package stageManager.bndrun\
        >java -jar stageManger.jar


