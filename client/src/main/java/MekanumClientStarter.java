import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class MekanumClientStarter
{
   public static final AtomicReference<String> lastAvailableRobotsString = new AtomicReference<>("No available robots list has been received yet");
   private static final Scanner consoleReader = new Scanner(System.in);
   private static boolean started = false;
   private static boolean connected = false;
   private static MekanumClient mekanumClient = MekanumClient.getInstance();
   private static String nextCommand = "";

   public static void onLogout()
   {
      connected = false;
      mekanumClient.disconnect();
      mekanumClient = null;
   }

   public static void onLogin() {connected = true;}

   public static void handleCommands(String nextCommand)
   {
      if (nextCommand.startsWith("!help"))
         printCommandsHelp();
      else if (nextCommand.startsWith("!debug clearconsole"))
         Utils.clearConsole(true);
      else if (nextCommand.startsWith("!clearconsole"))
         Utils.clearConsole(false);
      if (nextCommand.startsWith("!serverauto") && mekanumClient == null && !connected)
      {
         mekanumClient = MekanumClient.getInstance();
         mekanumClient.autoDiscoverGameServer = true;
         mekanumClient.start();
      }
      if (nextCommand.startsWith("!serverconnect") && mekanumClient == null && !connected)
      {
         String[] connectParts = nextCommand.split(" ");
         int[] parsedPorts = Utils.parseConnectionCommand(connectParts[1], connectParts[2], connectParts[3]);
         if (parsedPorts[0] != -1)
         {
            System.out.print("Enter Client name:");
            mekanumClient = MekanumClient.getInstance();
            mekanumClient.configureNetworkSettings(consoleReader.nextLine(), connectParts[1], parsedPorts[0], parsedPorts[1]);
            mekanumClient.start();
         }
      }
      else if (mekanumClient != null && connected)
      {
         if (nextCommand.startsWith("!serverlogout"))
            onLogout();
         else if (mekanumClient.hasRobot && nextCommand.startsWith("!debug sendjoystick"))
            mekanumClient.sendJoystickData(new KryonetMessages.Message.JoystickData());
         else if (!mekanumClient.hasRobot && nextCommand.startsWith("!robot list"))
            System.out.println(lastAvailableRobotsString.get());
         else if (!mekanumClient.hasRobot && nextCommand.startsWith("!robot select "))
            mekanumClient.tryToSelectRobot(Integer.parseInt(nextCommand.split(" ")[2]));
         else if (!nextCommand.startsWith("!") && !nextCommand.startsWith("/"))
            mekanumClient.sendChatMessage(nextCommand);
      }
   }

   public static void printCommandsHelp()
   {
      System.out.println("--Starting arguments-- (ports are optional)");
      System.out.println("java -jar server.jar start [serveripaddress] [tcpport] [udpport]");
      System.out.println("--Program commands--");
      System.out.println("!serverconnect [ipaddress] [tcpport] [udpport]) connects to a gameserver");
      System.out.println("!serverauto tries to auto find a gameserver and connect to it");
      System.out.println("!serverlogout           -> log out from currently connected gameserver");
      System.out.println("!debug clearconsole     -> clear console (if using eclipse or intellij)");
      System.out.println("!clearconsole           -> clear console (if using windows linux etc.)");
      System.out.println("!exit                   -> quit program");
      System.out.println("!robot select [robotid] -> selects the robot with robotid");
      System.out.println("!robot list             -> shows the latest available robots list if any is available");
      System.out.println("!robot logout           -> releases your robot so you can pick one again");
      System.out.println("!debug sendjoystick     -> sends joystick data to server (debug only)");
      System.out.println("[message]               -> send chat message");
   }

   public static void main(String[] args)
   {
      if (args != null && args.length >= 1)
      {
         //Only for unit testing, do not use
         if (args[0].contentEquals("testmode"))
         {
            mekanumClient.clientName = String.valueOf(Math.random());
            mekanumClient.start();
            return;
         }
         if (args[0].contentEquals("help"))
         {
            System.out.println("--Starting arguments-- (ports are optional)");
            System.out.println("java -jar server.jar connect [serveripaddress] [tcpport] [udpport]");
            System.exit(0);
         }
         if (args[0].contentEquals("start") && Utils.isValidIp4Address(args[1]) && Utils.isValidPort(args[2], args[3]))
         {
            System.out.print("Enter Client name:");
            mekanumClient = MekanumClient.getInstance();
            mekanumClient.configureNetworkSettings(consoleReader.nextLine(), args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]));
            mekanumClient.start();
            started = true;
         }
      }
      System.out.println("Type !help to get help.");
      if (!started)
         System.out.println("Type !serverconnect [ipaddress] [tcpport*] [udpport*]) to connect to a server, fields with * are optional");
      while (!nextCommand.contentEquals("!exit"))
      {
         nextCommand = consoleReader.nextLine();
         handleCommands(nextCommand);
      }
      if (mekanumClient != null)
         onLogout();
      System.exit(0);
   }
}

