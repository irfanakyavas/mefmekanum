import java.io.IOException;

public class Utils
{
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

   public static void clearConsole(boolean debugMode)
   {
      OsType hostOs = determineOs();
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
         }
         catch (IOException | InterruptedException exception)
         {
            //TODO: Error handling
         }
      }
      else if (hostOs == OsType.MAC_OS)
         System.out.println("\033[H\033[2J");
      else
         System.out.println("\033[H\033[2J");
   }

   public static boolean isValidIp4Address(String testString)
   {
      String[] ipParts = testString.trim().split("\\.");
      if (ipParts.length < 4)
         return false;
      try
      {
         for (int i = 0; i < 4; i++)
         {
            if (Integer.parseInt(ipParts[i]) > 255 || Integer.parseInt(ipParts[i]) < 0)
               return false;
         }
      }
      catch (NumberFormatException numberFormatException)
      {
         return false;
      }
      return true;
   }

   public static boolean isValidPort(String... portStr)
   {
      for (String s : portStr)
      {
         if (!(Integer.parseInt(s) > 1023 && Integer.parseInt(s) < 65536))
            return false;
      }
      return true;
   }

   public static int[] parseConnectionCommand(String... connectionStrParts)
   {
      int[] resultPorts = new int[2];
      if (connectionStrParts != null)
      {
         if (connectionStrParts.length == 3 && Utils.isValidIp4Address(connectionStrParts[0]) && Utils.isValidPort(connectionStrParts[1], connectionStrParts[2]))
         {
            resultPorts[0] = Integer.parseInt(connectionStrParts[1]);
            resultPorts[1] = Integer.parseInt(connectionStrParts[2]);
         }
      }
      else
      {
         resultPorts[0] = -1;
      }
      return resultPorts;
   }
}
