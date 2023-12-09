import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract class containing the implementation of client methods.
 */
public abstract class AbstractClientFunctionClass {
  // To check if the operation is valid or not from client side.
  protected synchronized static boolean isValidOp(String content){
    String key = content.split(" ",2)[0];
    return key.equals("PUT")||key.equals("GET")||key.equals("DELETE");
  }
  // To check if the given request is malformed or not.
  protected synchronized static String clientRead(String line) {
    if(isValidOp(line)) {
      String timestampedMessage = getCurrentTime() + " " +"Sent to server: "+ line;
      System.out.println(timestampedMessage);
      return line;
    }
    else {
      String timestampedMessage = getCurrentTime() + " " + "received malformed request of length:" +
              line.length();
      System.out.println(timestampedMessage);
      return "received malformed request of length:" + line.length();
    }
  }
  // To return the current system time of the client.
  protected synchronized static String getCurrentTime(){
    LocalDateTime currentDateTime = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    return currentDateTime.format(formatter);
  }
}
