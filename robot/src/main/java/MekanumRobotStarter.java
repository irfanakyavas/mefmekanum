import java.util.Scanner;

public class MekanumRobotStarter
{
   static Scanner consoleReader = new Scanner(System.in);

   public static void main(String[] args)
   {
      if (args.length > 0 && args[0].contentEquals("testmode"))
      {
         MekanumRobot mekanumRobot = new MekanumRobot();
         mekanumRobot.robotName = String.valueOf(Math.random());
         mekanumRobot.start();
      } else
      {
         System.out.print("Enter Robot name:");
         String robotName = consoleReader.nextLine();
         MekanumRobot mekanumRobot = new MekanumRobot();
         mekanumRobot.robotName = robotName;
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
}
