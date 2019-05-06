## StageSimulationManager

This repository contains the OSGI bundles only for creating Stage simulation manager, but before using it, you have to firstly clone and import the repository `CPswarm-common` for seting up the bnd workspce and the necessary dependency bundles 


# Installation and Configuration

1. clone and import all sub-projects in repository [`CPswarm-common`](https://git.pertforge.ismb.it/rzhao/cpswarm-common/tree/master) into eclipse.
2. following the same steps to clone and import this repository.(must be sure the box "Copy projects into workspace" is checked, so that this repository can use the bnd workspace configuration) 
3. go to project `it.ismb.pert.cpswarm.simulationOrchestrator` 
    1. open `orchestrator.bndrun`, you have to modify the following properties' values according to the actual values and ***save***
        >Orchestrator.config.file.orchestrator.xml=resources/orchestrator.xml,\
	    >Orchestrator.config.fileXSD=resources/file.xsd,\
        >conf=/home/rui/Documents/CPSwarm/SOOdata/conf-fd/,\
	    >src=/home/rui/Documents/CPSwarm/SOOdata/src-fd/,\
	    >target=/home/rui/Documents/CPSwarm/SOOdata/target-fd/,\
        >id=emergency_exit,\
     	>dim=2d,\
	    >max=8,\
	    >opt=true,\
	    >gui=false,\
	    >javax.net.ssl.trustStorePassword=changeit,\
        >javax.net.ssl.trustStore=/usr/java/jdk1.8.0_171/jre/lib/security/cacerts,\
	    >org.osgi.framework.trust.repositories=/usr/java/jdk1.8.0_171/jre/lib/security/cacerts
    2. open `SimulationOrchestrator.java`, uncomment the line:657 if the `opt=false` for only test the stage simulation, otherwise, skip this step  
    3. build and run the `orchestrator.bndrun`
        >bnd package orchestrator.bndrun,\
        >java -jar orchestrator.jar
4. go to project `it.ismb.pert.cpswarm.stageSimulationManager` 
    1. open `stageManager.bndrun`, modify the following properties' values with your path of the cacerts
        >ros.master.uri=http:\/\/localhost:11311,\
        >Manager.config.file.manager.xml=resources/manager.xml,\
	    >javax.net.ssl.trustStorePassword=changeit,\
	    >javax.net.ssl.trustStore=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/security/cacerts,\
	    >org.osgi.framework.trust.repositories=/usr/lib/jvm/java-1.8.0-openjdk-amd64/jre/lib/security/cacerts
    2. build and run the `stageManager.bndrun`
        >bnd package stageManager.bndrun,\
        >java -jar stageManger.jar