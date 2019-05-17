# HDSNotary (Stage 1) - Highly Dependable Systems

Group 1 - Alameda

[83531 - Miguel Belém](mailto:miguelbelem@tecnico.ulisboa.pt)
[83567 - Tiago Gonçalves](mailto:tiago.miguel.c.g@tecnico.ulisboa.pt)
[83576 - Vítor Nunes](mailto:vitor.sobrinho.nunes@tecnico.ulisboa.pt)

## Dependencies
In order to build and run the project you need:
* apache-maven
* Java 1.8
* Portuguese Citizen Card Driver

In some system you may need to export the following variable in order
to find the pteid library:

    export LD_LIBRARY_PATH=/usr/local/lib

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
    mvn exec:java -Dexec.args="1"
    
Where 1 is the ID of the server.
    
### Using different certifications processes
By default the Notary and Clients uses **Virtual Certificates** to sign message 
digests.

An alternative way is to use the Portuguese Citizen Card:

    cd HDSNotary-Server
    mvn exec:java -Dexec.args="1 CCSmartCard"

### Number of notaries and number of byzantine faults
By default the project are prepared to work with N = 4 (4 notaries) and to handle
1 byzantine fault.

## Client
To run Clients:

    cd HDSNotary-Client
    mvn exec:java
    
By default the client uses ID = 1, but it is possible to change this:
    
    mvn exec:java -Dexec.args="2"

**Recall** that the client ID is any number between [1; 5] and in order to perform a buy good operation both clients involved in the transaction must be on. 
    
If the Notary uses the Portuguese Citizen Card then the client should be launched
with the flag:

    mvn exec:java -Dexec.args="1 CCSmartCard"
    
**Deprecated and Removed** Since we use file `Servers.cfg` under the `HDSNotaryLib/src/main/resources/` this
feature is no longer available.

~~It is also possible to change the URL to the Notary Service: ~~

    ~~mvn exec:java -Dexec.args="1 VirtualCertificates //ServerIP:Port/HDSNotary"~~
    ~~OR~~
    ~~mvn exec:java -Dexec.args="1 CCSmartCard //ServerIP:Port/HDSNotary"~~
        
   
## Bad-Client
The Bad-Client is a special client designed for testing the security requirements.

This clients accepts all arguments as a regular client and it's only available on the VirtualCertificares mode to simplify testing.

There are several options which try to break our security measures:
Attack on the Message from Client to Notary:
* Intention to Sell sented only to 2 out of 4 notaries
* Intention to Sell sented only to 3 out of 4 notaries
* BuyGood without proof of work


## Testing Security Measures
From root folder:

    cd HDSNotary-Server
    mvn exec:java
    cd ../HDSNotary-Client
    mvn exec:java -Dargs="1"
    cd ../HDSNotary-BadClient
    mvn exec:java -Dargs="2"

You just need to select the operation you want to test on the bad client, it's not necessary to prepare anything for the replay attacks and tampering attacks to work. You should see the error message on the client. In the buy good operation after sending the replayed and the tampered message you need to press Enter on the good client client for him to send the message back to the bad one.
