import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An interface containing the methods to implement the operations of a Coordinator.
 */
public interface Coordinator extends Remote {
  /**
   * Method to call the prepare methods of all other participants and to see if the transaction can be committed.
   * @param clientMessage
   * @param serverResponse
   * @param clientAddress
   * @param clientPort
   * @return the server response.
   * @throws RemoteException
   */
  String prepareTransaction(String clientMessage, String serverResponse, String clientAddress, String clientPort) throws RemoteException;

  /**
   * Method to connect the coordinator to all the participants.
   * @throws RemoteException
   */
  void connectParticipants() throws RemoteException;
}
