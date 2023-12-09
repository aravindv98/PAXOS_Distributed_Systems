import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class CoordinatorImpl extends UnicastRemoteObject implements Coordinator{

  // To store the list of participants to link the coordinator with.
  private List<RMIServer> participants;
  // Contains the ipaddress of all the participants.
  private List<String> participantHosts;
  // Contains the list of all the ports of the participants.
  private List<Integer> participantPorts;
  public CoordinatorImpl(List<String> participantHosts, List<Integer> participantPorts) throws RemoteException {
    this.participantHosts = participantHosts;
    this.participantPorts = participantPorts;
  }

  public void connectParticipants() throws RemoteException{
    participants = new ArrayList<>();
    for (int i = 0; i < participantHosts.size(); i++) {
      try {
        Registry registry = LocateRegistry.getRegistry(participantHosts.get(i), participantPorts.get(i));
        RMIServer participant = (RMIServer) registry.lookup("RMIServer");
        participants.add(participant);
      } catch (Exception e) {
        throw new RemoteException("Unable to connect to participant", e);
      }
    }
  }

  public synchronized String prepareTransaction(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException {
    for (int i = 0; i < participantHosts.size(); i++) {
      int rmiPort = participantPorts.get(i);
      // Check to skip the server instance that the client is connected to.
      if (Integer.valueOf(clientPort)==rmiPort)
        continue;
      // To check if the transaction should be committed or aborted.
      if (!participants.get(i).prepare(clientMessage,serverResponse,clientAddress,clientPort)) {
        return "Abort";
      }
    }
    String response = "";
    for (int i = 0; i < participantHosts.size(); i++) {
      int rmiPort = participantPorts.get(i);
      if (Integer.valueOf(clientPort)==rmiPort)
        continue;
      // Committing the transaction.
      response =  participants.get(i).commit(clientMessage,serverResponse,clientAddress,String.valueOf(participantPorts.get(i)));
    }
    return response;
  }
}
