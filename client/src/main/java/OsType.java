public enum OsType
{
   WINDOWS(1),
   UNIX_DERIVATIVE(2),
   MAC_OS(3);
   int osNum;

   OsType(int osNum)
   {
      this.osNum = osNum;
   }
}
