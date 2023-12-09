import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

/**
 * An RMI Server to implement the remote RMI interface and extend the abstract
 * server implementation class.
 */
public class Server {
  public static void main(String[] args) {
    // Starting the server.
    try  {
      // To get the initial server port (Coordinator Port)
      int serverPort = Integer.valueOf(args[0]);
      // To initialise all the participant hosts and ports.
      Coordinator coordinator = new CoordinatorImpl(
              Arrays.asList("localhost", "localhost", "localhost", "localhost", "localhost"),
              Arrays.asList(serverPort+1, serverPort+2, serverPort+3, serverPort+4, serverPort+5)
      );
      Registry coordinatorRegistry = LocateRegistry.createRegistry(serverPort);
      coordinatorRegistry.bind("Coordinator", coordinator);
      System.out.println("Coordinator is listening on port " + serverPort);

      // Start the participants
      for (int i = 0; i < 5; i++) {
        RMIServer participant = new AbstractServerFunctionClass("localhost", serverPort);
        int port = serverPort + i+1;
        Registry participantRegistry = LocateRegistry.createRegistry(port);
        participantRegistry.bind("RMIServer", participant);
        participant.connectToCoordinator();
        System.out.println("Participant is listening on port " + port);
      }
      coordinator.connectParticipants();
      System.out.println("Servers ready ");
    }catch (Exception e) {
      e.printStackTrace();
    }
  }
}
