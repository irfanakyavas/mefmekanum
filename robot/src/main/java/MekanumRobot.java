import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MekanumRobot extends Thread
{
   public final int robotId = ThreadLocalRandom.current().nextInt(100000, 1000000);
   private final Client serverConnection = new Client();
   public String robotName;
   //These are read from by the com.mefhg.mekanumrobot.RobotSerialSendThread for sending to the robot over serial
   public AtomicInteger motor1Step = new AtomicInteger(0);
   //This value is written by the com.mefhg.mekanumrobot.RobotSerialReceiveThread for sending to the server over UDP
   public AtomicBoolean infrared = new AtomicBoolean(false);
   public AtomicInteger motor2Step = new AtomicInteger(0);
   public boolean isRegistered = false;

   public float joystickX = 0;
   public float joystickY = 0;

   //TODO: implement
   /*
   public static int getFloatAsInt(float f)
   {
      return (int)(f*1000000000);
   }
   public static float getAtomicIntegerAsFloat(AtomicInteger atomicInteger)
   {
      return atomicInteger.get() / 1000000000f;
   }
   public static void calculateMotorPwm(float joystickX, float joystickY)
   {
      int motor1calculated = getFloatAsInt((joystickX * joystickY) / 100f);
      motor1Step.set(motor1calculated);
   }

   public void processJoystick()
   {
      float joystickXLast=0;
      float joystickYLast=0;
      while (true)
      {
         if(joystickXLast != joystickX || joystickYLast != joystickY)
            calculateMotorPwm(joystickX,joystickY);
         //Thread.sleep(10);
      }
   }
    */
   @Override
   public synchronized void start()
   {
      try
      {
         KryonetMessages.register(serverConnection.getEndPoint());
         //InetAddress gameServer = MekanumClient.serverConnection.discoverHost(5555,5555); //TODO: gameserver discovery
         //System.out.println("Server found at " + gameServer.toString());
         ServerListenerForRobot serverListenerForClient = new ServerListenerForRobot();
         serverConnection.addListener(serverListenerForClient);
         new Thread(serverConnection).start();
         serverConnection.connect(5000, "127.0.0.1", 5555, 5554); //TODO: gameserver discovery
         serverConnection.sendTCP(new KryonetMessages.Message.RobotServerMessage.Join(robotName, robotId));

      } catch (IOException ioException)
      {
         ioException.printStackTrace();
      }
   }

   public class ServerListenerForRobot extends Listener
   {
      @Override
      public void connected(Connection connection)
      {
         System.out.println("[ROBOT] Connected to the gameserver");
      }

      @Override
      public void disconnected(Connection connection)
      {
         System.out.println("[ROBOT] Disconnected from the gameserver");
      }

      @Override
      public void received(Connection connection, Object o)
      {
         if (o instanceof KryonetMessages.Message.RobotServerMessage.JoinResponse)
         {
            KryonetMessages.Message.RobotServerMessage.JoinResponse joinResponse = (KryonetMessages.Message.RobotServerMessage.JoinResponse) o;
            if (joinResponse.success)
            {
               System.out.println("[ROBOT] Registered at the gameserver");
               isRegistered = true;
            }
         }
         if (o instanceof KryonetMessages.Message.JoystickData)
         {
            //TODO: implement
            KryonetMessages.Message.JoystickData joystickData = (KryonetMessages.Message.JoystickData) o;
            System.out.println("[ROBOT] Joystick data received from server (i am robot:" + robotName);
            //joystickX = joystickData.x;
            //joystickY = joystickData.y;
         }
      }
   }
}
