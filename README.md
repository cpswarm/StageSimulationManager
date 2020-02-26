# CPSwarm Stage Simulation Manager 

This is a Simulation Manager to be used in the [CPSwarm Simulation and Optimization Environment](https://github.com/cpswarm/SimulationOrchestrator/wiki/Simulation-and-Optimization-Environment). 
Specifically this Simulation Manager is designed to integrate the [Stage](https://github.com/rtv/Stage) simulator. 
This can be used in combination with the [Simulation  and Optimization Orchestrator](https://github.com/cpswarm/SimulationOrchestrator) to run a simulation 
and with an optimization tool, like [FREVO](https://github.com/cpswarm/FREVO) to run an optimiziation. 
 

## Getting Started
* Documentation: [wiki](https://github.com/cpswarm/StageSimulationManager/wiki)

## Deployment

Packages are built continuously with [Bamboo](https://pipelines.linksmart.eu/browse/CPSW-SMS/latest). The generated artifact is then packaged in a Docker image and pushed on DockerHub in the cpswarm organization.

### Compile from source

In the it.ismb.pert.cpswarm.simulation.stage folder 

```bash
bnd package stageManger.bndrun
```

## Development

### Run tests

The tests for this component are included in the [Simulation and Optimization Orchestrator](https://github.com/cpswarm/SimulationOrchestrator).

### Dependencies

* [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 
* [BND](https://bnd.bndtools.org/)


## Contributing
Contributions are welcome. 

Please fork, make your changes, and submit a pull request. For major changes, please open an issue first and discuss it with the other authors.

## Affiliation
![CPSwarm](https://github.com/cpswarm/template/raw/master/cpswarm.png)  
This work is supported by the European Commission through the [CPSwarm H2020 project](https://cpswarm.eu) under grant no. 731946.


