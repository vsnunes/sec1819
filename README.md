# HDSNotary (Stage 1) - Highly Dependable Systems

Group 1 - Alameda

[83531 - Miguel Belém](mailto:miguelbelem@tecnico.ulisboa.pt)
[83567 - Tiago Gonçalves](mailto:tiago.miguel.c.g@tecnico.ulisboa.pt)
[83576 - Vítor Nunes](mailto:vitor.sobrinho.nunes@tecnico.ulisboa.pt)

## Dependences
In order to build and run the project you need:
* apache-maven
* Java 1.8
* Portuguese Citizen Card Driver

## Configurations for different OSs
It is possible to build and run this project on Linux or Windows.

**This project is configured to work out-of-the-box for Linux OSs.**

**If your plan to build and run on a Linux OS you can skip this part.**

If you want/need to build/run for different OSs you need to change the following settings:

1.  Edit the file located under `HDSNotaryLib/src/main/resources/CitizenCard.cfg` and
change `library` property to point to the location of Portuguese Citizen Card
Shared Library.
2.  Edit the `pom.xml` file under the `HDSNotaryLib/` and `HDSNotary-Server/` and
change the value of the tag `project.pteidlib.location` or 
`project.pteidlib.location.windows` according to your OS to point to the 
**folder** where `pteidlib.jar` library is located.

## Compile and Run
In order to compile and be ready to run the whole project execute the following 
command on **root folder** to install all dependencies:

    mvn install
    
## Notary
To run the Notary, change to folder **HDSNotary-Server** and execute:

    cd HDSNotary-Server
    mvn exec:java
    
### Using different certifications processes
By default the Notary and Clients uses **Virtual Certificates** to sign message 
digests.

An alternative way is to use the Portuguese Citizen Card:

    cd HDSNotary-Server
    mvn exec:java -Dexec.args="CCSmartCard"
    
## Client
To run Clients:

    cd HDSNotary-Client
    mvn exec:java
    
By default the client uses ID = 1, but it is possible to change this:
    
    mvn exec:java -Dexec.args="2"

**Recall** that the client ID is any number between [1; 5]
    
If the Notary uses the Portuguese Citizen Card then the client should be launched
with the flag:

    mvn exec:java -Dexec.args="1 CCSmartCard"
    
It is also possible to change the URL to the Notary Service:

    mvn exec:java -Dexec.args="1 VirtualCertificates //ServerIP:Port/HDSNotary"
    OR
    mvn exec:java -Dexec.args="1 CCSmartCard //ServerIP:Port/HDSNotary"
        
   
## Bad-Client
The Bad-Client is a special client designed for testing the security requirements.

This clients accepts all arguments as a regular client.
