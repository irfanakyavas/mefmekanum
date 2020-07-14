import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class ClientAndRobotListener extends Listener
{
   private final Server server;
   public ClientAndRobotListener(Server server)
   {
      this.server = server;
   }

   @Override
   public void received(Connection c, Object object)
   {
         //Valid Join message received from a robot try to register it
         if (object instanceof KryonetMessages.Message.RobotServerMessage.Join)
         {
            KryonetMessages.Message.RobotServerMessage.Join joinMessage = (KryonetMessages.Message.RobotServerMessage.Join) object;
            MekanumServer.userHandler.registerRobot(new Robot(c, joinMessage.robotId, joinMessage.robotName));
            return;
         }
      //Valid Join message received from a client, try to register it
         if (object instanceof KryonetMessages.Message.ClientServerMessage.Join)
         {
            KryonetMessages.Message.ClientServerMessage.Join joinMessage = (KryonetMessages.Message.ClientServerMessage.Join) object;
            if (!joinMessage.clientName.contentEquals("Server"))
               MekanumServer.userHandler.registerClient(new Client(c, joinMessage.clientId, joinMessage.clientName));
            return;
         }
   }

   @Override
   public void disconnected(Connection c)
   {

   }

}
