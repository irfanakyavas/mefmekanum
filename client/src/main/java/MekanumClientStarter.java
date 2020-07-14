import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
public class MekanumClientStarter
{
   static Scanner consoleReader = new Scanner(System.in);
   public static final OsType hostOs = determineOs();
   public static AtomicReference<String> lastAvailableRobotsString = new AtomicReference<>("No available robots list has been received yet");

   public static OsType determineOs()
   {
      String osString = System.getProperty("os.name");
      if (osString.contains("mac"))
      {
         return OsType.MAC_OS;
      }
      if (osString.contains("win"))
      {
         return OsType.WINDOWS;
      }
      return OsType.UNIX_DERIVATIVE;
   }

   public static void clearConsole(OsType hostOs, boolean debugMode)
   {
      if (debugMode)
      {
         System.out.println(System.lineSeparator().repeat(200));
         return;
      }
      if (hostOs == OsType.WINDOWS)
      {
         try
         {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
         } catch (IOException | InterruptedException exception)
         {
            //TODO: Error handling
         }
      } else if (hostOs == OsType.MAC_OS)
         System.out.println("\033[H\033[2J");
      else
         System.out.println("\033[H\033[2J");
   }

   public static void main(String[] args)
   {
      System.out.print("Enter MekanumServer.Client name:");
      String clientName = consoleReader.nextLine();
      MekanumClient mekanumClient = new MekanumClient();
      mekanumClient.clientName = clientName;
      mekanumClient.start();
      System.out.println("Type !help to get help.");
      while (true)
      {
         String nextCommand = consoleReader.nextLine();
         //Command syntax >robot select [RobotId]
         if (nextCommand.startsWith("!help"))
         {
            System.out.println("!exit                   -> quit program");
            System.out.println("!robot select [robotid] -> selects the robot with robotid");
            System.out.println("!robot list             -> shows the latest available robots list if any is available");
            System.out.println("!robot logout           -> releases your robot so you can pick one again");
            System.out.println("!debug sendjoystick     -> sends joystick data to server (debug only)");
            System.out.println("[message]               -> send chat message");
         }
         if (nextCommand.startsWith("!debug sendjoystick"))
         {
            mekanumClient.sendJoystickData(new KryonetMessages.Message.JoystickData());
         }
         if (nextCommand.startsWith("!robot list"))
         {
            System.out.println(lastAvailableRobotsString.get());
         }
         if (nextCommand.startsWith("!robot select "))
         {
            int robotId = Integer.parseInt(nextCommand.split(" ")[2]);
            mekanumClient.tryToSelectRobot(robotId);
         }
         if (nextCommand.startsWith("!exit"))
         {
            mekanumClient.logOut();
            System.exit(0);
            break;
         }
         mekanumClient.sendChatMessage(nextCommand);
      }
   }
}

