import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.fail;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

public class ServerLoadTest
{


   @Test
   @PrepareForTest({ExecutorService.class, MekanumRobotStarter.class, MekanumRobot.class, MekanumClientStarter.class, MekanumClient.class, KryonetMessages.class, MekanumServer.class})
   public void serverLoadTest()
   {
      try
      {
         mockStatic(MekanumServer.class);
         mockStatic(MekanumClientStarter.class);
         mockStatic(MekanumRobotStarter.class);
         ExecutorService es = Executors.newCachedThreadPool();
         MekanumServer.main(new String[]{"5555", "5554"});
         Runnable ddosthread = () ->
         {
            for (int i = 0; i < 10; i++)
            {
               MekanumClientStarter.main(new String[]{"testmode"});
               MekanumRobotStarter.main(new String[]{"testmode"});
            }
         };
         for (int i = 0; i < 10; i++)
            es.execute(ddosthread);
         es.shutdown();
         boolean finished = es.awaitTermination(1, TimeUnit.MINUTES);
         System.out.println("10x10x2 connection test complete");
         MekanumServer.userHandler.broadcastTCPMessage(new KryonetMessages.Message.RobotServerMessage.InfraredData(), BroadcastTarget.ROBOTS_WITHOUT_CLIENTS);
         MekanumServer.userHandler.broadcastUDPMessage(new KryonetMessages.Message.ClientServerMessage.TakeOwnershipRequest(), BroadcastTarget.CLIENTS_WITHOUT_ROBOTS);
         MekanumServer.userHandler.broadcastTCPMessage(new KryonetMessages.Message.RobotServerMessage.InfraredData(), BroadcastTarget.ROBOTS_WITHOUT_CLIENTS);
         MekanumServer.userHandler.broadcastUDPMessage(new KryonetMessages.Message.ClientServerMessage.TakeOwnershipRequest(), BroadcastTarget.CLIENTS_WITHOUT_ROBOTS);
         System.out.println("400 connection Broadcast test complete");
      } catch (Exception e)
      {
         e.printStackTrace();
         fail("Server threw Exception");
      }

   }
}
