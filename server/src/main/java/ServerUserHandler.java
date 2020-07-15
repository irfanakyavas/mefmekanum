import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerUserHandler extends Thread
{
   //These hold the registered server-side representations of connected devices
   public static final Map<Integer, Robot> robotsWithoutClients = new ConcurrentHashMap<>();
   public static final Map<Client, Robot> robotsWithClients = new ConcurrentHashMap<>();
   public static final Map<Integer, Client> clientsWithoutRobots = new ConcurrentHashMap<>();

   //TODO: ensure each clienname and robotname is unique
   public static final HashSet<String> robotNames = new HashSet<>();
   public static final HashSet<String> clientNames = new HashSet<>();

   //Make sure there is only one (un-)/registration process done at the same time
   public static final Semaphore registrationSemaphore = new Semaphore(1);
   //Make sure there is only one thread accessing to the unmatchedrobots list while broadcasting unmatched robots
   public static final Semaphore unmatchedsSemaphore = new Semaphore(1);

   //Send list of robots without clients to the clients without robots every 3 seconds
   @Override
   public void run()
   {
      while (true)
      {
         try
         {
            broadcastUnmatchRobotsToRobotlessClients();
            Thread.sleep(3000);
         } catch (InterruptedException interruptedException)
         {

         }
      }
   }

   /**
    * Broadcasts RID's of all unmatched robots to the all robotless clients
    */
   private synchronized void broadcastUnmatchRobotsToRobotlessClients()
   {
      try
      {
         unmatchedsSemaphore.acquire();
         if (robotsWithoutClients.size() > 0)
         {
            AtomicInteger i = new AtomicInteger(0);
            String[] availableRNAMEs = new String[robotsWithoutClients.size()];
            int[] availableRIDs = new int[robotsWithoutClients.size()];
            robotsWithoutClients.values().forEach(robot ->
            {
               availableRIDs[i.get()] = robot.robotId;
               availableRNAMEs[i.getAndIncrement()] = robot.robotName;
            });
            KryonetMessages.Message.ClientServerMessage.AvailableRobotsNotification announcement =
                    new KryonetMessages.Message.ClientServerMessage.AvailableRobotsNotification(availableRIDs, availableRNAMEs);
            clientsWithoutRobots.forEach((id, client) -> broadcastTCPMessage(announcement, BroadcastTarget.CLIENTS_WITHOUT_ROBOTS));
         }
         // System.out.println("[SERVER] Broadcasted available robots to robotless clients");
      } catch (InterruptedException interruptedException)
      {
         System.out.println("[SERVER] FAIL! could not broadcast available robots to robotless clients");
         interruptedException.printStackTrace();
      } finally
      {
         unmatchedsSemaphore.release();
      }
   }

   public synchronized void broadcastTCPMessage(KryonetMessages.Message message, BroadcastTarget target)
   {
      if (target == BroadcastTarget.CLIENTS_WITHOUT_ROBOTS && message instanceof KryonetMessages.Message.ClientServerMessage)
         clientsWithoutRobots.values().forEach(c -> c.sendTCP((KryonetMessages.Message.ClientServerMessage) message));
      else if (target == BroadcastTarget.ROBOTS_WITHOUT_CLIENTS && message instanceof KryonetMessages.Message.RobotServerMessage)
         robotsWithoutClients.values().forEach(r -> r.sendTCP((KryonetMessages.Message.RobotServerMessage) message));
      else if (target == BroadcastTarget.ROBOTS_WITH_CLIENTS && message instanceof KryonetMessages.Message.RobotServerMessage)
         robotsWithClients.values().forEach(r -> r.sendTCP((KryonetMessages.Message.RobotServerMessage) message));
      else if (target == BroadcastTarget.EVERY_CLIENT && message instanceof KryonetMessages.Message.ClientServerMessage)
      {
         clientsWithoutRobots.values().forEach(c -> c.sendTCP((KryonetMessages.Message.ClientServerMessage) message));
         robotsWithClients.keySet().forEach(c -> c.sendTCP((KryonetMessages.Message.ClientServerMessage) message));
      } else if (target == BroadcastTarget.EVERY_ROBOT && message instanceof KryonetMessages.Message.RobotServerMessage)
      {
         robotsWithoutClients.values().forEach(r -> r.sendTCP((KryonetMessages.Message.RobotServerMessage) message));
         robotsWithClients.values().forEach(r -> r.sendTCP((KryonetMessages.Message.RobotServerMessage) message));
      } else
         System.out.println("[SERVER] broadcastUDPMessageTo was called with incompatible Message-BroadcastTarget pair");
   }

   public synchronized void broadcastUDPMessage(KryonetMessages.Message message, BroadcastTarget target)
   {
      if (target == BroadcastTarget.CLIENTS_WITHOUT_ROBOTS && message instanceof KryonetMessages.Message.ClientServerMessage)
         clientsWithoutRobots.values().forEach(c -> c.sendUDP((KryonetMessages.Message.ClientServerMessage) message));
      else if (target == BroadcastTarget.ROBOTS_WITHOUT_CLIENTS && message instanceof KryonetMessages.Message.RobotServerMessage)
         robotsWithoutClients.values().forEach(r -> r.sendUDP((KryonetMessages.Message.RobotServerMessage) message));
      else if (target == BroadcastTarget.ROBOTS_WITH_CLIENTS && message instanceof KryonetMessages.Message.RobotServerMessage)
         robotsWithClients.values().forEach(r -> r.sendUDP((KryonetMessages.Message.RobotServerMessage) message));
      else if (target == BroadcastTarget.EVERY_CLIENT && message instanceof KryonetMessages.Message.ClientServerMessage)
      {
         clientsWithoutRobots.values().forEach(c -> c.sendUDP((KryonetMessages.Message.ClientServerMessage) message));
         robotsWithClients.keySet().forEach(c -> c.sendUDP((KryonetMessages.Message.ClientServerMessage) message));
      } else if (target == BroadcastTarget.EVERY_ROBOT && message instanceof KryonetMessages.Message.RobotServerMessage)
      {
         robotsWithoutClients.values().forEach(r -> r.sendUDP((KryonetMessages.Message.RobotServerMessage) message));
         robotsWithClients.values().forEach(r -> r.sendUDP((KryonetMessages.Message.RobotServerMessage) message));
      } else
         System.out.println("[SERVER] broadcastUDPMessageTo was called with incompatible Message-BroadcastTarget pair");
   }

   /**
    * Registers the com.mefhg.MekanumServer.src.MekanumServer.com.mefhg.MekanumServer.src.Client representation to the hashmap. Any client must be registered before getting matched with a robot
    *
    * @param c com.mefhg.MekanumServer.src.MekanumServer.com.mefhg.MekanumServer.src.Client to register
    */
   public synchronized void registerClient(Client c)
   {
      try
      {
         registrationSemaphore.acquire();
         if (!clientsWithoutRobots.containsValue(c))
         {
            clientsWithoutRobots.put(c.clientId, c);
            c.sendTCP(new KryonetMessages.Message.ClientServerMessage.JoinResponse(true));
            System.out.println("[SERVER] Successfully registered client cname:" + c.clientName + " cid:" + c.clientId);
         } else
         {
            c.sendTCP(new KryonetMessages.Message.ClientServerMessage.JoinResponse(false));
            System.out.println("[SERVER] FAIL! could not register client cname:" + c.clientName + " cid:" + c.clientId);
         }
      } catch (InterruptedException interruptedException)
      {
         System.out.println("[SERVER] FAIL! could not register client cname:" + c.clientName + " cid:" + c.clientId + " an exception was thrown");
         interruptedException.printStackTrace();
      } finally
      {
         registrationSemaphore.release();
      }
   }

   /**
    * Unregisters the com.mefhg.MekanumServer.src.MekanumServer.com.mefhg.MekanumServer.src.Client representation from hashmap. Disconnected clients must be unregistered before being able to connect again
    *
    * @param c com.mefhg.MekanumServer.src.MekanumServer.com.mefhg.MekanumServer.src.Client to unregister
    */
   public synchronized void unregisterClient(Client c)
   {
      try
      {
         registrationSemaphore.acquire();
         clientsWithoutRobots.remove(c.clientId);
         if (robotsWithClients.containsKey(c))
         {
            Robot r = robotsWithClients.get(c);
            robotsWithClients.remove(c);
            robotsWithoutClients.put(r.robotId, r);
            System.out.println("[SERVER] Successfully UNregistered client cname:" + c.clientName + " cid:" + c.clientId);
         }
      } catch (InterruptedException interruptedException)
      {
         System.out.println("[SERVER] FAIL! could not UNregister client cname:" + c.clientName + " cid:" + c.clientId);
         interruptedException.printStackTrace();
      } finally
      {
         registrationSemaphore.release();
      }
   }

   /**
    * Registers the mekanumshared.com.mefhg.MekanumServer.src.MekanumServer.com.mefhg.MekanumServer.src.Robot representation to hashmap. Any robot must be registered before getting matched with a robot
    *
    * @param r mekanumshared.com.mefhg.MekanumServer.src.MekanumServer.com.mefhg.MekanumServer.src.Robot to register
    */
   public synchronized void registerRobot(Robot r)
   {
      try
      {
         registrationSemaphore.acquire();
         if (!robotsWithoutClients.containsValue(r))
         {
            robotsWithoutClients.put(r.robotId, r);
            r.sendTCP(new KryonetMessages.Message.RobotServerMessage.JoinResponse(true));
            System.out.println("[SERVER] Successfully registered robot rname:" + r.robotName + " rid:" + r.robotId);
         } else
         {
            System.out.println("[SERVER] FAIL! could not register robot rname:" + r.robotName + " rid:" + r.robotId);
            r.sendTCP(new KryonetMessages.Message.RobotServerMessage.JoinResponse(false));
         }
      } catch (InterruptedException interruptedException)
      {
         System.out.println("[SERVER] FAIL! could not register robot rname:" + r.robotName + " rid:" + r.robotId);
         interruptedException.printStackTrace();
      } finally
      {
         registrationSemaphore.release();
      }
   }

   /**
    * Unregisters the mekanumshared.com.mefhg.MekanumServer.src.MekanumServer.com.mefhg.MekanumServer.src.Robot representation from hashmap. Any robot must be unregistered before being able to connect again
    *
    * @param r
    */
   public synchronized void unregisterRobot(Robot r)
   {
      try
      {
         registrationSemaphore.acquire();
         robotsWithoutClients.remove(r.robotId);
         if (robotsWithClients.containsValue(r))
         {
            robotsWithClients.remove(r.getClient());
            clientsWithoutRobots.put(r.getClient().clientId, r.getClient());
         }
         System.out.println("[SERVER] Successfully unregistered robot rname:" + r.robotName + " rid:" + r.robotId);
      } catch (InterruptedException interruptedException)
      {
         System.out.println("[SERVER] FAIL! could not unregister robot rname:" + r.robotName + " rid:" + r.robotId);
         interruptedException.printStackTrace();
      } finally
      {
         registrationSemaphore.release();
      }
   }

   /**
    * Reserves the robot and client for one other. If either one of the robot or client is disconnected they must be unmatched.
    *
    * @param r Robot that will match with client
    * @param c Client that will match with robot
    */
   public synchronized void matchRobotWithClient(Robot r, Client c)
   {
      try
      {
         registrationSemaphore.acquire();
         if (!robotsWithClients.containsValue(r) && robotsWithoutClients.containsKey(r.robotId) && c.getRobot() == null)
         {
            r.setClient(c);
            c.setRobot(r);
            robotsWithoutClients.remove(r.robotId);
            robotsWithClients.put(c, r);
            c.sendTCP(new KryonetMessages.Message.ClientServerMessage.TakeOwnershipResponse(r.robotId, c.clientId, true));
            System.out.println("[SERVER] Successfully matched robot rname:" + r.robotName + " rid:" + r.robotId + " with client cname:" + c.clientName + " cid:" + c.clientId);
         } else
         {
            c.sendTCP(new KryonetMessages.Message.ClientServerMessage.TakeOwnershipResponse(r.robotId, c.clientId, false));
            System.out.println("[SERVER] FAIL! could not match robot rname:" + r.robotName + " rid:" + r.robotId + " with client cname:" + c.clientName + " cid:" + c.clientId);
         }
         registrationSemaphore.release();
      } catch (InterruptedException interruptedException)
      {
         System.out.println("[SERVER] FAIL! could not match robot rname:" + r.robotName + " rid:" + r.robotId + " with client cname:" + c.clientName + " cid:" + c.clientId);
         interruptedException.printStackTrace();
      } finally
      {
         registrationSemaphore.release();
      }
   }

   /**
    * Frees the robot and client from each other; client can select a robot and the robot can be selected by a client once again
    *
    * @param r
    * @param c
    */
   public synchronized void unMatchRobotWithClient(Robot r, Client c)
   {
      try
      {
         registrationSemaphore.acquire();
         if (robotsWithClients.containsKey(c))
         {
            c.setRobot(null);
            r.getClient().sendTCP(new KryonetMessages.Message.ClientServerMessage.ClientKickNotification());
            r.setClient(null);
            robotsWithClients.remove(c);
            clientsWithoutRobots.put(c.clientId, c);
            robotsWithoutClients.put(r.robotId, r);
            System.out.println("[SERVER] Successfully UNmatched robot rname:" + r.robotName + " rid:" + r.robotId + " with client cname:" + c.clientName + " cid:" + c.clientId);
         }
      } catch (InterruptedException interruptedException)
      {
         System.out.println("[SERVER] FAIL! could not UNmatch robot rname:" + r.robotName + " rid:" + r.robotId + " with client cname:" + c.clientName + " cid:" + c.clientId);
         interruptedException.printStackTrace();
      } finally
      {
         registrationSemaphore.release();
      }
   }
}
