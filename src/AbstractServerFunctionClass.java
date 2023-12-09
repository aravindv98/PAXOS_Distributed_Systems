import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract server class that contains the implementation of the hashmap functions.
 */
public class AbstractServerFunctionClass extends UnicastRemoteObject implements RMIServer, Remote {
  // Map to store, get and delete key/value pairs.
  private ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
  private String coordinatorHost;
  private int coordinatorPort;
  private Coordinator coordinator;
  private String commitResponse;
  public AbstractServerFunctionClass() throws RemoteException{

  }
  public synchronized void putCommand(String key, String value){
    map.put(key,value);
  }
  public synchronized String getCommand(String key){
    return map.get(key);
  }
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
          return "DELETE operation with key: " + key + " completed";
        } else {
          System.out.println(responseMessage);
          return responseMessage;
        }
    }
    else {
      System.out.println(getCurrentTime() + " Sent to client:" + " Invalid DELETE operation received from client"
              + " from " + clientAddress + ":" + clientPort);
      return "Invalid DELETE operation received from client";
    }
  }
  public synchronized String getOperation( String key, String clientAddress, String clientPort, boolean getFlag) {
    long startTime = System.currentTimeMillis();
    // Thread.sleep(500);
    //To check the number of arguments passed for get.
    String[] validChunks = key.split(" ");
    // If valid, then send confirmation message to client.
    if (getFlag == true) {
      if (getCommand(key) != null && validChunks.length == 1) {
        String value = getCommand(key);
        long endTime = System.currentTimeMillis();
        String timeOutMessage = checkTimeOut(startTime, endTime);
        if (timeOutMessage.equals("")) {
          System.out.println(getCurrentTime() + " Sent to client:" + " GET operation with key: " + key + " gives value: " + value
                  + " from " + clientAddress + ":" + clientPort);
          return getCurrentTime()+" GET operation with key: " + key + " gives value: " + value;
        } else {
          return timeOutMessage;
        }
      } else {
        System.out.println(getCurrentTime() + " Sent to client:" + " Invalid GET operation received from client"
                + " from " + clientAddress + ":" + clientPort);
        return getCurrentTime()+" Invalid GET operation received from client";
      }
    }
    else {
      if(validChunks.length==1){
        return getCommand(key);
      }
      else if(validChunks.length==2){
          return getCommand(validChunks[0]);
      }
      else {
        return null;
      }
    }
  }

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
        return getCurrentTime()+" PUT operation with key: " + realKey + " and value: " + value + " completed";
      }
      else {
        return timeOutMessage;
      }
    }
    // Else, client to be informed of the erroneous arguments.
    else {
      System.out.println(getCurrentTime()+" Sent to client:"+" Invalid PUT operation received from client"
              +" from "+clientAddress+":"+clientPort);
      return getCurrentTime()+" Invalid PUT operation received from client";
    }


  }
  public synchronized String checkTimeOut(long startTime, long endTime){
    long diff = endTime - startTime;
    if(endTime - startTime > 5000){
      return getCurrentTime()+ " Request Timed out with request taking: "+
              (endTime-startTime)+" ms to process!";
    }
    return "";
  }
  public synchronized String performOperation(String clientMessage, String serverResponse, String clientAddress, String clientPort) {
    String operation = clientMessage.split(" ", 2)[0];
    String key = clientMessage.split(" ", 2)[1];
    switch (operation) {
      case "PUT": {
        serverResponse += putOperation(key, clientAddress, clientPort);
        break;
      }
      case "GET": {
        serverResponse += getOperation(key, clientAddress, clientPort, true);
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
    return serverResponse;
  }
  // Initialising the hashmap and coordinator settings.
  public AbstractServerFunctionClass(String coordinatorHost, int coordinatorPort) throws RemoteException {
    this.coordinatorHost = coordinatorHost;
    this.coordinatorPort = coordinatorPort;
    map.put("1","Arsenal");
    map.put("2","City");
    map.put("3","Liverpool");
    map.put("4","Chelsea");
    map.put("5","United");
    this.commitResponse = null;
  }
  public synchronized void connectToCoordinator() throws RemoteException{
    try {
      Registry registry = LocateRegistry.getRegistry(coordinatorHost, coordinatorPort);
      coordinator = (Coordinator) registry.lookup("Coordinator");
    }
    catch (Exception e) {
      throw new RemoteException("Unable to connect to coordinator", e);
    }
  }

  public synchronized boolean prepare(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException {
    String operation = clientMessage.split(" ", 2)[0];
    String key = clientMessage.split(" ", 2)[1];
    String value = this.getOperation(key,clientAddress,clientPort,false);
    if(operation.equals("PUT")){
      return value==null;
    }
    else if(operation.equals("DELETE")){
      return value!=null;
    }
    return true; // return true for get operation
  }

  public synchronized String commit(String clientMessage,String serverResponse,String clientAddress,String clientPort) throws RemoteException {
    this.commitResponse = this.performOperation(clientMessage,serverResponse,clientAddress,clientPort);
    return this.commitResponse;
  }

  public synchronized String perform(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException{
    String response = coordinator.prepareTransaction(clientMessage,serverResponse,clientAddress,clientPort);
    if(response.equals("Abort"))
      return getCurrentTime()+" Transaction Aborted!";
    return response;
  }
}
