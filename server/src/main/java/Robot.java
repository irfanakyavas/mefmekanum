import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.time.Instant;

/**
 * Represents the robot at the server-side
 */
public class Robot
{
   private final Connection robotConnection;
   public KryonetMessages.Message.RobotServerMessage.InfraredData lastInfraredData;

   public final int robotId;
   public String robotName;
   public Instant lastTimePingReceived;
   private Client client;

   public Client getClient()
   {
      return client;
   }

   public void setClient(Client client)
   {
      this.client = client;
      onClientChanged();
   }

   public Robot(Connection robotConnection, int robotId, String robotName)
   {
      this.robotConnection = robotConnection;
      this.robotId = robotId;
      this.robotName = robotName;
      //Instantiate a robotListener that will listen to the messages sent by client after registration(joystickData etc.)
      RobotListener robotListener = new RobotListener(this);
      robotConnection.addListener(robotListener);
   }

   public void onClientChanged()
   {

   }

   public synchronized void sendTCP(KryonetMessages.Message.RobotServerMessage robotServerMessage)
   {
      robotConnection.sendTCP(robotServerMessage);
   }

   public synchronized void sendUDP(KryonetMessages.Message.RobotServerMessage robotServerMessage)
   {
      robotConnection.sendUDP(robotServerMessage);
   }

   public class RobotListener extends Listener
   {
      Robot r;

      public RobotListener(Robot r)
      {
         this.r = r;
      }

      @Override
      public void disconnected(Connection connection)
      {
         MekanumServer.userHandler.unMatchRobotWithClient(r, client);
         MekanumServer.userHandler.unregisterRobot(r);
      }

      @Override
      public void received(Connection connection, Object o)
      {
         lastTimePingReceived = Instant.now();
         if (o instanceof KryonetMessages.Message.RobotServerMessage.InfraredData && client != null)
         {
            KryonetMessages.Message.RobotServerMessage.InfraredData infraredData = (KryonetMessages.Message.RobotServerMessage.InfraredData) o;
            System.out.println("[SERVER] Received infrared data from rname:" + robotName + " rid:" + robotId);
            //lastInfraredData = infraredData. ... //TODO: implement
         }
      }
   }
}
