import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * An RMI Server to implement the remote RMI interface and extend the abstract
 * server implementation class.
 */
public class Server{
  public static void main(String[] args) {
    // Port number is taken from the terminal argument.
    int portNumber = Integer.valueOf(args[0]);
    // Starting the server.
    try  {
      int numServers = 5; // Total number of servers
      int basePort = portNumber; // Starting port number
      AbstractServerFunctionClass[] servers = new AbstractServerFunctionClass[numServers];

      // Create and bind servers
      for (int serverId = 0; serverId < numServers; serverId++) {
        int port = basePort + serverId; // Increment port for each server

        // Create server instance
        servers[serverId] = new AbstractServerFunctionClass(port, numServers, basePort);

        // Bind the server to the RMI registry
        RMIServer skeleton = (RMIServer) UnicastRemoteObject.exportObject(servers[serverId],port);
        Registry registry = LocateRegistry.createRegistry(port);
        registry.rebind("RMIServer"+port, skeleton);

        System.out.println("Server " + (serverId+1) + " is ready at port " + port);
      }

    }catch (Exception e) {
      e.printStackTrace();
    }

  }
}
