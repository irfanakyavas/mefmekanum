public enum BroadcastTarget
{
   CLIENTS_WITHOUT_ROBOTS(1),
   ROBOTS_WITHOUT_CLIENTS(2),
   ROBOTS_WITH_CLIENTS(3);
   public final int num;

   BroadcastTarget(int num)
   {
      this.num = num;
   }
}
