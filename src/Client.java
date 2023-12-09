import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * An RMI Client that fetches the remote object and invokes the required server methods using this object.
 */
public class Client extends AbstractClientFunctionClass{
  public static void main(String[] args){
    try {
      // Ipaddress to connect to the servers.
      String ipAddress = args[0];
      // Port Numbers of the participants to choose from at random.
      List<Integer> portNumbers =  Arrays.asList(3001, 3002, 3003, 3004, 3005);
      Random rand = new Random();
      int portNumber = portNumbers.get(rand.nextInt(portNumbers.size()));
      Registry registry = LocateRegistry.getRegistry(ipAddress, portNumber);
      // Get the required object to access the server methods.
      RMIServer stub = (RMIServer) registry.lookup("RMIServer");
      System.out.println(getCurrentTime()+" Client is running on port: "+portNumber);
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
                String serverResponse = stub.perform(toServer, "", ipAddress, String.valueOf(portNumber));
                System.out.println(getCurrentTime() + " Received from server: " + serverResponse);
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
            String serverResponse = stub.perform(toServer, "", ipAddress, String.valueOf(portNumber));
            System.out.println(getCurrentTime() + " Received from server: " + serverResponse);
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
