import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.time.Instant;

/**
 * Used only at server-side to store current clients and related objects
 * for the Class that defines client(connects to server) look at the mekanumclient.MekanumClient
 */
public class Client
{
   private final ClientListener clientListener;
   private final Connection clientConnection;
   private Robot robot;

   public final int clientId;
   public final String clientName;
   public Instant lastTimePingReceived;

   public Client(Connection clientConnection, int clientId, String clientName)
   {
      this.clientConnection = clientConnection;
      this.clientId = clientId;
      this.clientName = clientName;

      //Instantiate a clientListener that will listen to the messages sent by client after registration(joystickData etc.)
      this.clientListener = new ClientListener(this);
      clientConnection.addListener(clientListener);
      System.out.println("[SERVER] New client created, CID:" + clientId + " CNAME:" + clientName);
   }

   public Robot getRobot()
   {
      return robot;
   }

   public void setRobot(Robot robot)
   {
      this.robot = robot;
      onRobotChanged();
   }

   /**
    * Sends the specified ClientServerMessage to the client (TCP)
    *
    * @param clientServerMessage CSM to send
    */
   public void sendTCP(KryonetMessages.Message.ClientServerMessage clientServerMessage)
   {
      clientConnection.sendTCP(clientServerMessage);
   }

   /**
    * Sends the specified ClientServerMessage to the client (UDP)
    *
    * @param clientServerMessage CSM to send
    */
   public void sendUDP(KryonetMessages.Message.ClientServerMessage clientServerMessage)
   {
      clientConnection.sendUDP(clientServerMessage);
   }

   /**
    * Sends the joystick data
    *
    * @param joystickData
    */
   public void sendJoystickData(KryonetMessages.Message.JoystickData joystickData)
   {
      clientConnection.sendUDP(joystickData);
   }

   public void onRobotChanged()
   {

   }

   /**
    * Listens to the messages sent by the client program to the server's client object
    * Forwards the joystick data sent by the client to the robot
    */
   public class ClientListener extends Listener
   {
      //com.mefhg.MekanumServer.src.MekanumServer.com.mefhg.MekanumServer.src.Client(server-side) of this listener
      Client c;

      public ClientListener(Client c)
      {
         this.c = c;
      }

      @Override
      public void disconnected(Connection connection)
      {
         //Remove the client representations from HashMaps on disconnection so that they can connect and register properly again
         MekanumServer.userHandler.unMatchRobotWithClient(robot, c);
         MekanumServer.userHandler.unregisterClient(c);
         System.out.println("[SERVER] com.mefhg.MekanumServer.src.MekanumServer.com.mefhg.MekanumServer.src.Client CID:" + clientId + " has been unregistered Reason: disconnected");
      }

      @Override
      public void received(Connection connection, Object o)
      {
         lastTimePingReceived = Instant.now();

         //If the client(server-side) does not have a robot assigned and requests ownership of a robot try to match them
         if (o instanceof KryonetMessages.Message.ClientServerMessage.TakeOwnershipRequest && robot == null && ServerUserHandler.clientsWithoutRobots.containsKey(clientId))
         {
            KryonetMessages.Message.ClientServerMessage.TakeOwnershipRequest takeOwnershipRequest = (KryonetMessages.Message.ClientServerMessage.TakeOwnershipRequest) o;
            MekanumServer.userHandler.matchRobotWithClient(ServerUserHandler.robotsWithoutClients.get(takeOwnershipRequest.robotId), c);
         }
         //If the client(server-side) is matched with a robot and receives a JoystickData message so we have to forward it to the robot of this client
         if (o instanceof KryonetMessages.Message.JoystickData && robot != null)
         {
            System.out.println("[SERVER] com.mefhg.MekanumServer.src.MekanumServer.com.mefhg.MekanumServer.src.Client CID:" + c.clientId + "'s JoystickData has been received and client has a robot, server will try to will forward joystickdata to robot");
            KryonetMessages.Message.JoystickData joystickMessage = (KryonetMessages.Message.JoystickData) o;
            sendJoystickData(joystickMessage);
            System.out.println("[SERVER] com.mefhg.MekanumServer.src.MekanumServer.com.mefhg.MekanumServer.src.Client CID:" + c.clientId + "'s JoystickData has been forwarded to the mekanumshared.com.mefhg.MekanumServer.src.MekanumServer.com.mefhg.MekanumServer.src.Robot RID:" + robot.robotId);
         }
         if (o instanceof KryonetMessages.Message.ClientServerMessage.ChatMessage)
         {
            KryonetMessages.Message.ClientServerMessage.ChatMessage chatMessage = (KryonetMessages.Message.ClientServerMessage.ChatMessage) o;
            System.out.println("[CHAT]" + chatMessage.sender + ":" + chatMessage.messageContent);
         }
      }
   }
}
