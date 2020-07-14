import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import java.io.IOException;

public class MekanumServer
{
   public static final Server server = new Server();
   public static final ServerUserHandler userHandler = new ServerUserHandler();

   public static void main(String[] args) throws IOException
   {
      Log.set(Log.LEVEL_WARN); //TODO: turn off in production version
      //initServer(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
      // args0 is tcp port arg1 is udp port
      initServer(5555,5554);
   }

   public static void initServer(int porttcp, int portudp) throws IOException
   {
      KryonetMessages.register(server);
      server.addListener(new ClientAndRobotListener(server));
      server.bind(porttcp, portudp);
      new Thread(server).start();
      System.out.println("[SERVER] Server successfully started");
      userHandler.start();
      System.out.println("[SERVER] UserHandler successfully started");
   }
}


