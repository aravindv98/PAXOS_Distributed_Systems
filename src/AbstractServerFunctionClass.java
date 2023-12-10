import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract server class that contains the implementation of the hashmap functions.
 */
public class AbstractServerFunctionClass extends RemoteObject implements ProposerInterface, AcceptorInterface, LearnerInterface,RMIServer,
        Serializable {
  // Map to store, get and delete key/value pairs.
  private ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
  private int numServers;
  private int basePort;
  private int serverId;
  private boolean isPromised;
  private int currentSequenceNumber;
  private int proposedSequenceNumber = 0;
  private Object acceptedValue;
  private String response;
  private List<RMIServer> acceptors;
  public AbstractServerFunctionClass(){
    // empty default constructor
  }

  /**
   * Constructor to create a Server instance.
   * @param serverId The unique ID of this server.
   * @param numServers The total number of servers in the system.
   */
  public AbstractServerFunctionClass(int serverId, int numServers, int basePort) {
    this.basePort = basePort;
    this.numServers = numServers;
    this.serverId = serverId;
    this.currentSequenceNumber = -1;
    this.isPromised = false;
    this.acceptors = new ArrayList<>();
    this.response = "";
    map.put("1","Arsenal");
    map.put("2","City");
    map.put("3","Liverpool");
    map.put("4","Chelsea");
    map.put("5","United");
  }
  public synchronized void proposeOperation(String clientMessage, String serverResponse,
                                String clientAddress, String clientPort) throws RemoteException, NotBoundException {
    this.acceptors = new ArrayList<>();
    int proposalId = generateProposalId();
    propose(proposalId, new Operation(clientMessage,serverResponse,clientAddress,clientPort));
  }
  public synchronized int prepare(int proposalId) throws RemoteException {
    // Implement Paxos prepare logic here
    // Below three lines is for simulating a failure in one of the acceptors during the prepare stage.
//    this.failure();
//    if(this.isPromised==false)
//      return 0;
    if(proposalId>this.currentSequenceNumber){
      this.currentSequenceNumber = proposalId;
      this.isPromised = true;
      System.out.println(getCurrentTime()+" Promise received from port number: "+ this.serverId);
      return 1;
    }
    System.out.println(getCurrentTime()+" Promise not received from port number: "+ this.serverId);
    return 0;
  }
  public synchronized String accept(int proposalId, Object proposalValue) throws RemoteException, NotBoundException {
    // Implement Paxos accept logic here
    System.out.println(getCurrentTime()+" Accepting the operation in all the replicas");
    for(int i = 0;i<acceptors.size();i++){
      acceptors.get(i).learn(proposalId,proposalValue);
      }
    return acceptors.get(0).getResponse();
  }

  @Override
  public synchronized void propose(int proposalId, Object proposalValue) throws RemoteException, NotBoundException {
    // Implement Paxos propose logic here
    System.out.println(getCurrentTime()+" Sending the proposal to all replicas");
    int numberOfPromises = 0;
    for(int serverPort = this.basePort;serverPort<this.basePort+this.numServers;serverPort++){
      if(serverPort!=this.serverId){
        Registry registry = LocateRegistry.getRegistry("localhost", serverPort);
        RMIServer process = (RMIServer) registry.lookup("RMIServer"+serverPort);
        this.acceptors.add(process);
        numberOfPromises+=process.prepare(proposalId);
      }
    }
    if(numberOfPromises<numServers/2){
      System.out.println(getCurrentTime()+" Operation aborted due to no majority");
    }
    else{
      System.out.println(getCurrentTime()+" Majority achieved");
      this.response = accept(proposalId,proposalValue);
      this.response = this.response.substring(0,response.length()-4) + serverId;
    }

  }
  public synchronized void learn(int proposalId, Object acceptedValue) throws RemoteException, NotBoundException {
        this.setProposalValue(acceptedValue);
        this.performOperation(((Operation) this.acceptedValue).clientMessage, ((Operation) this.acceptedValue).serverResponse,
                ((Operation) this.acceptedValue).clientAddress, String.valueOf(this.serverId));
    }

  public synchronized void setProposalValue(Object acceptedValue) throws RemoteException {
    this.acceptedValue = acceptedValue;
  }


  /**
   * Generates a unique proposal ID.
   * @return A unique proposal ID.
   */
  private int generateProposalId() {
    // Placeholder code to generate a unique proposal ID
    this.proposedSequenceNumber+=1;
    return this.proposedSequenceNumber;
  }
  //Implementing the put operation of the Map. (PUT)
  public synchronized void putCommand(String key, String value){
    map.put(key,value);
  }
  //Implementing the get operation of the Map. (GET)
  public synchronized String getCommand(String key){
    return map.get(key);
  }
  //Implementing the delete operation of the Map. (DELETE)
  public synchronized boolean deleteCommand(String key){
    if(map.containsKey(key)){
      map.remove(key);
      return true;
    }
    return false;
  }
  // Returns the current system time of the server.
  public synchronized static String getCurrentTime(){
    LocalDateTime currentDateTime = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    return currentDateTime.format(formatter);
  }
  // Helper function to invoke the delete operation of the map and to check if the delete operation
  // can be performed using the arguments provided.
  public synchronized String deleteOperation(String key, String clientAddress, String clientPort) {
    //To check the number of arguments passed for deletion.
    long startTime = System.currentTimeMillis();
    // Thread.sleep(500);
    String [] validChunks = key.split(" ");
    // If valid, then send confirmation message to client.
    if(deleteCommand(key)&&validChunks.length==1) {
      long endTime = System.currentTimeMillis();
      String responseMessage = checkTimeOut(startTime, endTime);
      if (responseMessage.equals("")) {
        System.out.println(getCurrentTime() + " Sent to client:" + " DELETE operation with key: " + key + " completed"
                + " from " + clientAddress + ":" + clientPort);
        return "DELETE operation with key: " + key + " completed" + " from " + clientAddress + ":" + clientPort;
      } else {
        return responseMessage;
      }
    }
    else {
      System.out.println(getCurrentTime() + " Sent to client:" + " Invalid DELETE operation received from client"
              + " from " + clientAddress + ":" + clientPort);
      return "Invalid DELETE operation received from client" + " from " + clientAddress + ":" + clientPort;
    }
  }
  // Helper function to invoke the get operation of the map and to check if the get operation
  // can be performed using the arguments provided.
  public synchronized String getOperation( String key, String clientAddress, String clientPort) {
    long startTime = System.currentTimeMillis();
    // Thread.sleep(500);
    //To check the number of arguments passed for get.
    String [] validChunks = key.split(" ");
    // If valid, then send confirmation message to client.
    if(getCommand(key)!=null&&validChunks.length==1) {
      String value = getCommand(key);
      long endTime = System.currentTimeMillis();
      String timeOutMessage = checkTimeOut(startTime, endTime);
      if (timeOutMessage.equals("")) {
        System.out.println(getCurrentTime() + " Sent to client:" + " GET operation with key: " + key + " gives value: " + value
                + " from " + clientAddress + ":" + clientPort);
        return "GET operation with key: " + key + " gives value: " + value + " from " + clientAddress + ":" + clientPort;
      } else {
        return timeOutMessage;
      }
    }
    else {
      System.out.println(getCurrentTime() + " Sent to client:" + " Invalid GET operation received from client"
              + " from " + clientAddress + ":" + clientPort);
      return "Invalid GET operation received from client" + " from " + clientAddress + ":" + clientPort;
    }
  }
  // Helper function to invoke the put operation of the map and to check if the put operation
  // can be performed using the arguments provided.
  public synchronized String putOperation(String key, String clientAddress, String clientPort) {
    long startTime = System.currentTimeMillis();
    // Thread.sleep(500);
    //To check the number of arguments passed for put.
    String [] validChunks = key.split(" ");
    String realKey = key.split(" ", 2)[0];
    String value = key.split(" ", 2)[1];
    // If valid, then send confirmation message to client.
    if (!realKey.equals("") && !value.equals("") && validChunks.length==2) {
      putCommand(realKey, value);
      long endTime = System.currentTimeMillis();
      String timeOutMessage = checkTimeOut(startTime, endTime);
      if (timeOutMessage.equals("")) {
        System.out.println(getCurrentTime() + " Sent to client:" + " PUT operation with key: " + realKey + " and value: " + value + " completed"
                + " from " + clientAddress + ":" + clientPort);
        return "PUT operation with key: " + realKey + " and value: " + value + " completed"
                + " from " + clientAddress + ":" + clientPort;
      }
      else {
        return timeOutMessage;
      }
    }
    // Else, client to be informed of the erroneous arguments.
    else {
      System.out.println(getCurrentTime()+" Sent to client:"+" Invalid PUT operation received from client"
              +" from "+clientAddress+":"+clientPort);
      return "Invalid PUT operation received from client"
              +" from "+clientAddress+":"+clientPort;
    }


  }
  public synchronized String checkTimeOut(long startTime, long endTime){
    if(endTime - startTime > 10){
      return getCurrentTime()+ " Request Timed out with request taking: "+
              (endTime-startTime)+" ms to process!";
    }
    return "";
  }
  // Function to redirect the requests from the client to the respective methods.
  // The client calls only this method and the server handles all the remaining operations.
  public synchronized String performOperation(String clientMessage, String serverResponse, String clientAddress, String clientPort) {
    String operation = clientMessage.split(" ", 2)[0];
    String key = clientMessage.split(" ", 2)[1];
    switch (operation) {
      case "PUT": {
        serverResponse += putOperation(key, clientAddress, clientPort);
        break;
      }
      case "GET": {
        serverResponse += getOperation(key, clientAddress, clientPort);
        break;
      }
      case "DELETE": {
        serverResponse += deleteOperation(key, clientAddress, clientPort);
        break;
      }
      default: {
        serverResponse = clientMessage;
        System.out.println(getCurrentTime() + " Sent to client: "
                + serverResponse + " from " + clientAddress + ":" + clientPort);
      }
    }
    this.response = serverResponse;
    return this.response;
  }
  public synchronized String getResponse() throws RemoteException {
    return this.response;
  }
  public void failure() {
    Random random = new Random();

    while (true) {
      try {
        // Simulate random failures by sleeping for a random period
        Thread.sleep(random.nextInt(5000) + 3000); // Sleep for 3 to 8 seconds

        // Simulate restarting the acceptor thread
        isPromised = false;
        System.out.println("Acceptor thread restarted.");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  private static class Operation implements Serializable{
    String clientMessage;
    String serverResponse;
    String clientAddress;
    String clientPort;
    Operation(String clientMessage, String serverResponse, String clientAddress, String clientPort){
      this.clientMessage = clientMessage;
      this.serverResponse = serverResponse;
      this.clientAddress = clientAddress;
      this.clientPort = clientPort;
    }
  }
}
