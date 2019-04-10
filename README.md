# HDSNotary - Highly Dependable Systems

Group 1 - Alameda

[83531 - Miguel Belém](mailto:miguelbelem@tecnico.ulisboa.pt)
[83567 - Tiago Gonçalves](mailto:tiago.miguel.c.g@tecnico.ulisboa.pt)
[83576 - Vítor Nunes](mailto:vitor.sobrinho.nunes@tecnico.ulisboa.pt)

## Compile and Run
In order to compile and be ready to run the whole project execute the following command on **root folder** to install
all dependencies:

    mvn install
    
## Notary
To run the Notary, change to folder **HDSNotary** and execute:

    cd HDSNotary-Server
    mvn exec:java
    
### Using different certifications processes
By default the Notary and Clients uses Virtual Certificates to sign message digests.

An alternative way is to use the Portuguese Citizen Card:

    cd HDSNotary-Server
    mvn exec:java -Dexec.args="CCSmartCard"
    
## Client
To run Clients:

    cd HDSNotary-Client
    mvn exec:java
    
By default the client uses ID = 1, but it is possible to change this:
    
    mvn exec:java -Dexec.args="2"
    
If the Notary uses the Portuguese Citizen Card then the client should be launched with the flag:

    mvn exec:java -Dexec.args="1 CCSmartCard"
    
It is also possible to change the URL to the Notary Service:

    mvn exec:java -Dexec.args="1 VirtualCertificates \\ServerIP:Port/HDSNotary"
    OR
    mvn exec:java -Dexec.args="1 CCSmartCard \\ServerIP:Port/HDSNotary"
        
   
## Bad-Client
The Bad-Client is a special client designed for testing the security requirements.

This clients accepts all arguments as a regular client.