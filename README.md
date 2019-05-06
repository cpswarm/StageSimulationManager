##CPswarm-Osgi-Bundles

This project is converted from CPSWarm maven project to OSGI bundles that can be run in a distributed enviornment(AIOLOS) for building a distributed optimization and simulation environment.


# Installation and Configuration

1. clone and import all sub-projects in eclipse.
2. go to project `it.ismb.pert.cpswarm.gazeboSimulationManager` 
    1. open `gazeboManager.bndrun`, modify the following properties' values with your path of the cacerts
        >javax.net.ssl.trustStore=/usr/java/jdk1.8.0_171/jre/lib/security/cacerts,\
        >org.osgi.framework.trust.repositories=/usr/java/jdk1.8.0_171/jre/lib/security/cacerts,\
        >ros.node=stage.launch
    2. run the `bnd package gazeboManager.bndrun` to build the manager bndrun file.
    3. 
3. go to project `it.ismb.pert.cpswarm.simulationOrchestrator` 
    1. open `localOrchestrator.bndrun`, modify the following properties' values with your path of the cacerts
        >javax.net.ssl.trustStore=/usr/java/jdk1.8.0_171/jre/lib/security/cacerts,\
        >org.osgi.framework.trust.repositories=/usr/java/jdk1.8.0_171/jre/lib/security/cacerts,\
        >conf=/home/rui/Documents/CPSwarm/SOOdata/conf-fd/,\
	    >src=/home/rui/Documents/CPSwarm/SOOdata/src-fd/,\
	    >target=/home/rui/Documents/CPSwarm/SOOdata/target-fd/,\
        >id=emergency_exit,\
     	>dim=2d,\
	    >max=8,\
	    >opt=false,\
	    >gui=true,\
    2. run the `bnd package localOrchestrator.bndrun` to build the orchestrator bndrun file.
4. run the generated gazeboManager.jar, the localOrchestrator.jar and the FREVO bundle in different terminals
