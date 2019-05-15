package pt.ulisboa.tecnico.meic.sec.HDSNotaryServer;

import java.util.concurrent.Callable;

import pt.ulisboa.tecnico.meic.sec.HDSNotaryServer.interfaces.NotaryCommunicationInterface;
import pt.ulisboa.tecnico.meic.sec.util.Interaction;

public class NotaryEchoTask implements Callable<Interaction> {

    public enum Operation {ECHO, READY}

    private NotaryCommunicationInterface notaryInterface;
    private Operation operation;
    private Interaction params;
    private int clientId;

    public NotaryEchoTask(NotaryCommunicationInterface notaryInterface, Operation operation, Interaction parms) {
        this.notaryInterface = notaryInterface;
        this.operation = operation;
        this.params = parms;
    }

    public NotaryEchoTask(NotaryCommunicationInterface notaryInterface, Operation operation, int clientId) {
        this.notaryInterface = notaryInterface;
        this.operation = operation;
        this.clientId = clientId;
    }

    @Override
    public Interaction call() throws Exception {
        switch (operation) {
            case ECHO: 
                notaryInterface.echo(params);
                break;
            case READY: 
                notaryInterface.ready(params); 
                break;
        }
        return null;
    }
}