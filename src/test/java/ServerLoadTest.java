import org.junit.Test;
import org.powermock.core.classloader.MockClassLoader;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.fail;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

public class ServerLoadTest
{
   private final int ROBOTS_CLIENTS_PER_THREAD = 10;
   private final int NUM_THREADS = 10;
   private final int NUM_ROBOTS_CLIENTS = ROBOTS_CLIENTS_PER_THREAD * NUM_THREADS;

   @Test
   @PrepareForTest({ExecutorService.class, MekanumRobotStarter.class, MekanumRobot.class, MekanumClientStarter.class, MekanumClient.class, KryonetMessages.class, MekanumServer.class})
   public void serverLoadTest()
   {
      try
      {
         mockStatic(MekanumClient.class);
         mockStatic(MekanumServer.class);
         mockStatic(MekanumClientStarter.class);
         mockStatic(MekanumRobotStarter.class);
         ExecutorService es = Executors.newCachedThreadPool();
         MekanumServer.main(new String[]{"5555", "5554"});

         Runnable ddosthread = () ->
         {
            for (int i = 0; i < ROBOTS_CLIENTS_PER_THREAD; i++)
            {
               MekanumClient mekanumClient = new MekanumClient();
               mekanumClient.clientName = String.valueOf(Math.random());
               mekanumClient.start();
               MekanumRobotStarter.main(new String[]{"testmode"});
            }
         };
         for (int i = 0; i < NUM_THREADS; i++)
            es.execute(ddosthread);
         es.shutdown();
         boolean finished = es.awaitTermination(1, TimeUnit.MINUTES);
         System.out.println(NUM_ROBOTS_CLIENTS + " connection test complete");
         MekanumServer.userHandler.broadcastTCPMessage(new KryonetMessages.Message.RobotServerMessage.InfraredData(), BroadcastTarget.ROBOTS_WITHOUT_CLIENTS);
         MekanumServer.userHandler.broadcastUDPMessage(new KryonetMessages.Message.ClientServerMessage.TakeOwnershipRequest(), BroadcastTarget.CLIENTS_WITHOUT_ROBOTS);
         MekanumServer.userHandler.broadcastTCPMessage(new KryonetMessages.Message.RobotServerMessage.InfraredData(), BroadcastTarget.ROBOTS_WITHOUT_CLIENTS);
         MekanumServer.userHandler.broadcastUDPMessage(new KryonetMessages.Message.ClientServerMessage.TakeOwnershipRequest(), BroadcastTarget.CLIENTS_WITHOUT_ROBOTS);
         MekanumServer.userHandler.broadcastUDPMessage(new KryonetMessages.Message.ClientServerMessage.ChatMessage("LOAD TEST", "LOAD TEST"), BroadcastTarget.EVERY_CLIENT);
         System.out.println(2 * NUM_ROBOTS_CLIENTS + " connection Broadcast test complete");
      } catch (Exception e)
      {
         e.printStackTrace();
         fail("Server threw Exception");
      }

   }
}
