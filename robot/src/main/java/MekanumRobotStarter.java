import java.util.Scanner;

public class MekanumRobotStarter
{
   static Scanner consoleReader = new Scanner(System.in);
   public static void main(String[] args)
   {

      System.out.print("Enter mekanumshared.MekanumServer.Robot name.:");
      String robotName = consoleReader.nextLine();
      MekanumRobot mekanumRobot = new MekanumRobot();
      MekanumRobot.robotName = robotName;
      mekanumRobot.start();

      while (true)
      {
         String nextCommand = consoleReader.nextLine();
         if (nextCommand.startsWith("debug sendinfrared"))
         {

         }
      }
   }
}
