import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;

public class MekanumClient extends Thread
{
   private static final Client serverConnection = new Client();
   public final int clientId = (int) ((Math.random() + 555) * 555);
   public String clientName;
   public boolean registeredAtServer = false;
   public boolean hasRobot = false;

   @Override
   public synchronized void start()
   {
      try
      {
         KryonetMessages.register(serverConnection.getEndPoint());
         //InetAddress gameServer = cli.MekanumClient.serverConnection.discoverHost(5555,5555); //TODO: gameserver discovery
         //System.out.println("Server found at " + gameServer.toString());
         ServerListenerForClient serverListenerForClient = new ServerListenerForClient();
         serverConnection.addListener(serverListenerForClient);
         new Thread(serverConnection).start();

         serverConnection.connect(1500, "127.0.0.1", 5555, 5554); //TODO: gameserver discovery
         serverConnection.sendTCP(new KryonetMessages.Message.ClientServerMessage.Join(clientName, clientId));
      } catch (IOException ioException)
      {
         ioException.printStackTrace();
      }
   }

   public void sendChatMessage(String message)
   {
      if (registeredAtServer)
         serverConnection.sendTCP(new KryonetMessages.Message.ClientServerMessage.ChatMessage(message, clientName));
   }

   public void sendJoystickData(KryonetMessages.Message.JoystickData joystickData)
   {
      if (registeredAtServer && hasRobot)
         serverConnection.sendUDP(joystickData);
   }

   public void logOut()
   {
      serverConnection.stop();
   }

   public void tryToSelectRobot(int robotId)
   {
      if (registeredAtServer && !hasRobot)
         serverConnection.sendTCP(new KryonetMessages.Message.ClientServerMessage.TakeOwnershipRequest(robotId, clientId));
   }

   public class ServerListenerForClient extends Listener
   {
      @Override
      public void connected(Connection connection)
      {
         System.out.println("[CLIENT] Connected to gameserver");
      }

      @Override
      public void disconnected(Connection connection)
      {
         System.out.println("[CLIENT] Disconnected from gameserver");
      }

      @Override
      public void received(Connection connection, Object o)
      {
         if (o instanceof KryonetMessages.Message.ClientServerMessage.TakeOwnershipResponse)
         {
            KryonetMessages.Message.ClientServerMessage.TakeOwnershipResponse takeOwnershipResponse = (KryonetMessages.Message.ClientServerMessage.TakeOwnershipResponse) o;
            if (takeOwnershipResponse.success)
            {
               System.out.println("[CLIENT] this client now owns the robot with RID:" + takeOwnershipResponse.robotId);
               hasRobot = true;
            }
         }
         if (o instanceof KryonetMessages.Message.ClientServerMessage.JoinResponse)
         {
            KryonetMessages.Message.ClientServerMessage.JoinResponse joinResponse = (KryonetMessages.Message.ClientServerMessage.JoinResponse) o;
            if (joinResponse.success)
            {
               System.out.println("[CLIENT] registered at gameserver");
               registeredAtServer = true;
            }
         }
         if (o instanceof KryonetMessages.Message.ClientServerMessage.ChatMessage && registeredAtServer)
         {
            KryonetMessages.Message.ClientServerMessage.ChatMessage chatMessage = (KryonetMessages.Message.ClientServerMessage.ChatMessage) o;
            if (chatMessage.sender.contentEquals("Server") &&
                    (chatMessage.messageContent.startsWith("-----Robots available-----") || chatMessage.messageContent.startsWith("No robots available right now")))
            {
               MekanumClientStarter.lastAvailableRobotsString.set(chatMessage.messageContent);
            } else
            {
               System.out.println("[CHAT] " + chatMessage.sender + ":" + chatMessage.messageContent);
            }
         }
      }
   }
}
