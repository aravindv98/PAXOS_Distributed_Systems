import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

/**
 * An RMI Client that fetches the remote object and invokes the required server methods using this object.
 */
public class Client extends AbstractClientFunctionClass{
  public static void main(String[] args){
    try {
      String serverAddress = args[0];
      int clientPort = Integer.valueOf(args[1]);
      Registry registry = LocateRegistry.getRegistry(serverAddress, clientPort);
      // Get the required object to access the server methods.
      RMIServer stub = (RMIServer) registry.lookup("RMIServer"+clientPort);
      System.out.println(getCurrentTime()+" Client is connected to server running on: "+clientPort);
      // Menu to provide interactive mode / run by file options.
      System.out.println(getCurrentTime()+" This is a menu driven program with the following commands: PUT/GET/DELETE/file");
      System.out.println(getCurrentTime()+" Enter 'exit' to exit");
      Scanner sc = new Scanner(System.in);
      // Loop until the client wishes to exit
      while (true) {
        String clientMessage = "";
        clientMessage += sc.nextLine();
        switch (clientMessage.toUpperCase()) {
          // File is executed by reading the contents of the file.
          case "FILE": {
            File file = new File("commands.txt");
            try (FileInputStream fileInputStream = new FileInputStream(file);
                 InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                 BufferedReader reader = new BufferedReader(inputStreamReader)) {
              String line;
              while ((line = reader.readLine()) != null) {
                String toServer = clientRead(line);
                stub.proposeOperation(toServer, "", serverAddress, String.valueOf(clientPort));
                System.out.println(getCurrentTime() + " Received from server: " + stub.getResponse());
              }
              break;
            }
          }
          case "EXIT" :{
            System.out.println(getCurrentTime() + " Client disconnected");
            break;
          }
          // Interactive commands entered here.
          default: {
            String toServer = clientRead(clientMessage);
            stub.proposeOperation(toServer, "", serverAddress, String.valueOf(clientPort));
            System.out.println(getCurrentTime() + " Received from server: " + stub.getResponse());
          }
        }
        // To terminate the execution of the client when exited.
        if (clientMessage.equalsIgnoreCase("exit")) {
          break;
        }
      }
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }
}
