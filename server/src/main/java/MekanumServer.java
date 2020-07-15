import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import java.io.IOException;

public class MekanumServer
{
   public static final Server server = new Server();
   public static final ServerUserHandler userHandler = new ServerUserHandler();
   private static int portTcp = 5555;
   private static int portUdp = 5554;

   public static void main(String[] args) throws IOException
   {
      Log.set(Log.LEVEL_WARN); //TODO: turn off in production version
      try
      {
         portTcp = Integer.parseInt(args[0]);
         portUdp = Integer.parseInt(args[1]);
      } catch (Exception e)
      {
         System.out.println("[SERVER] Could not parse ports from commandline, proceeding with TCP:5555 UDP:5554");
         e.printStackTrace();
         portTcp = 5555;
         portUdp = 5554;
      } finally
      {
         initServer(portTcp, portUdp);
      }
   }

   public static void initServer(int porttcp, int portudp) throws IOException
   {
      KryonetMessages.register(server);
      server.addListener(new ClientAndRobotListener(server));
      System.out.println("[SERVER] Trying to bind to ports");
      server.bind(porttcp, portudp);
      new Thread(server).start();
      System.out.println("[SERVER] Server successfully started TCP:" + portTcp + " UDP:" + portUdp);
      userHandler.start();
      System.out.println("[SERVER] UserHandler successfully started");
   }
}


