package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.interfaces.NotaryCommunicationInterface;
import pt.ulisboa.tecnico.meic.sec.util.CFGHelper;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class NotaryCommunicationService implements NotaryCommunicationInterface {
    

    
    @Override
    public void send(Interaction request) throws RemoteException {
        
    }

    @Override
    public void echo(Interaction request) throws RemoteException {

    }
}
