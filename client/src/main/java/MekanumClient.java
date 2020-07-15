import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ThreadLocalRandom;

public class MekanumClient extends Thread
{
   public final int clientId = ThreadLocalRandom.current().nextInt(100000, 1000000);
   private final Client serverConnection = new Client();
   public String clientName;
   public boolean registeredAtServer = false;
   public boolean hasRobot = false;
   public boolean autoDiscoverGameServer = false;
   public String gameServerAddress = "127.0.0.1";
   public int tcpPort = 5555;
   public int udpPort = 5554;

   public void configureConnectionSettings(String gameServerAddress, int tcpPort, int udpPort)
   {
      this.gameServerAddress = gameServerAddress;
      this.tcpPort = tcpPort;
      this.udpPort = udpPort;
   }

   @Override
   public synchronized void start()
   {
      try
      {
         KryonetMessages.register(serverConnection.getEndPoint());
         ServerListenerForClient serverListenerForClient = new ServerListenerForClient();
         serverConnection.addListener(serverListenerForClient);
         new Thread(serverConnection).start();
         if (autoDiscoverGameServer)
         {
            InetAddress gameServer = serverConnection.discoverHost(tcpPort, udpPort);
            System.out.println("[CLIENT] Server found at " + gameServer.toString());
            serverConnection.connect(5000, gameServer.getHostAddress(), tcpPort, udpPort);
         } else
         {
            serverConnection.connect(5000, gameServerAddress, tcpPort, udpPort);
         }
         serverConnection.sendTCP(new KryonetMessages.Message.ClientServerMessage.Join(clientName, clientId));
      } catch (IOException ioException)
      {
         System.out.println("[CLIENT] FAIL! An exception occured when Client tried to initialize networking");
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
         if (o instanceof KryonetMessages.Message.ClientServerMessage.AvailableRobotsNotification)
         {
            KryonetMessages.Message.ClientServerMessage.AvailableRobotsNotification availableRobotsNotification = (KryonetMessages.Message.ClientServerMessage.AvailableRobotsNotification) o;
            assert (availableRobotsNotification.availableRIDs.length == availableRobotsNotification.availableRNAMEs.length);
            if (availableRobotsNotification.availableRIDs.length > 0)
            {
               StringBuilder availableRobotsList = new StringBuilder("-------Available Robots-------\n" +
                       " rid                      rname ");
               for (int i = 0; i < availableRobotsNotification.availableRIDs.length; i++)
               {
                  availableRobotsList.append("-")
                          .append(availableRobotsNotification.availableRIDs[i])
                          .append("   ")
                          .append(availableRobotsNotification.availableRNAMEs[i])
                          .append("\n");
               }
               MekanumClientStarter.lastAvailableRobotsString.set(availableRobotsList.toString());
            }
         }
         if (o instanceof KryonetMessages.Message.ClientServerMessage.TakeOwnershipResponse)
         {
            KryonetMessages.Message.ClientServerMessage.TakeOwnershipResponse takeOwnershipResponse = (KryonetMessages.Message.ClientServerMessage.TakeOwnershipResponse) o;
            if (takeOwnershipResponse.success)
            {
               System.out.println("[CLIENT] This client now owns the robot with RID:" + takeOwnershipResponse.robotId);
               hasRobot = true;
            }
         }
         if (o instanceof KryonetMessages.Message.ClientServerMessage.JoinResponse)
         {
            KryonetMessages.Message.ClientServerMessage.JoinResponse joinResponse = (KryonetMessages.Message.ClientServerMessage.JoinResponse) o;
            if (joinResponse.success)
            {
               System.out.println("[CLIENT] Registered at gameserver");
               registeredAtServer = true;
            }
         }
         if (o instanceof KryonetMessages.Message.ClientServerMessage.ChatMessage && registeredAtServer)
         {
            KryonetMessages.Message.ClientServerMessage.ChatMessage chatMessage = (KryonetMessages.Message.ClientServerMessage.ChatMessage) o;
            System.out.println("[CHAT] " + chatMessage.sender + ":" + chatMessage.messageContent);
         }
      }
   }
}
