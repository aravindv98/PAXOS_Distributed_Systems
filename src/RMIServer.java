import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An RMI interface containing the methods to be implemented by the server and to be exposed to the
 * client through a remote object.
 */
public interface RMIServer extends Remote{
  /**
   * Fundamental operation to put a value in the hashmap.
   * @param key key of the pair.
   * @param value value of the pair.
   * @throws RemoteException
   */
  void putCommand(String key, String value) throws RemoteException;

  /**
   * Fundamental operation to get a value from the hashmap.
   * @param key key used to fetch the value
   * @return value of the key
   * @throws RemoteException
   */
  String getCommand(String key) throws RemoteException;

  /**
   * Fundamental operation to delete a key,value pair in the hashmap.
   * @param key to be deleted.
   * @return if the key has been deleted.
   * @throws RemoteException
   */
  boolean deleteCommand(String key) throws RemoteException;

  /**
   * Helper method to perform the delete operation for a given server instance.
   * @param key to be deleted.
   * @param clientAddress address of the client
   * @param clientPort port of the client.
   * @return server response.
   * @throws RemoteException
   */
  String deleteOperation(String key, String clientAddress, String clientPort) throws RemoteException;

  /**
   * Helper method to perform the get operation for a given server instance.
   * @param key to be used for the get operation.
   * @param clientAddress
   * @param clientPort
   * @param getFlag flag used to denoted if used for TPC protocol or not.
   * @return the value of the key if present.
   * @throws RemoteException
   */
  String getOperation( String key, String clientAddress, String clientPort, boolean getFlag) throws RemoteException;

  /**
   * Helper method to perform the put operation for a given server instance.
   * @param key to be inserted in the hashmap.
   * @param clientAddress
   * @param clientPort
   * @return the server response.
   * @throws RemoteException
   */
  String putOperation(String key, String clientAddress, String clientPort) throws RemoteException;

  /**
   * Redirecting the client requests to the respective methods implementing the hashmap operations.
   * @param clientMessage request of the client
   * @param serverResponse to store the server response.
   * @param clientAddress
   * @param clientPort
   * @return the server response.
   * @throws RemoteException
   */
  String performOperation(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException;

  /**
   * To check if the given operation is getting timed out.
   * @param startTime time when the operation starts.
   * @param endTime time when the operation ends.
   * @return if the operation has timed out.
   * @throws RemoteException
   */
  String checkTimeOut(long startTime, long endTime) throws RemoteException;

  /**
   * Method to prepare the participant for committing a transaction.
   * @param clientMessage
   * @param serverResponse
   * @param clientAddress
   * @param clientPort
   * @return if the given participant can commit the transaction.
   * @throws RemoteException
   */
  boolean prepare(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException;

  /**
   * Method to committing a given transaction in a participant.
   * @param clientMessage
   * @param serverResponse
   * @param clientAddress
   * @param clientPort
   * @return the participant response.
   * @throws RemoteException
   */
  String commit(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException;

  /**
   * Method to connect each participant to the coordinator.
   * @throws RemoteException
   */
  void connectToCoordinator() throws RemoteException;

  /**
   * Method which starts the Two Phase Commit Protocol and is exposed to client to initiate a given transaction.
   * @param clientMessage
   * @param serverResponse
   * @param clientAddress
   * @param clientPort
   * @return the server response.
   * @throws RemoteException
   */
  String perform(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException;
}
