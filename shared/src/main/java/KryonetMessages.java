import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

/**
 * Contains kryonet message definitions and register method
 */
public class KryonetMessages
{
   private KryonetMessages()
   {
   }

   public static void register(EndPoint endPoint)
   {
      Kryo kryo = endPoint.getKryo();
      kryo.register(String[].class);

      //Server-Client Communication
      kryo.register(Message.ClientServerMessage.Join.class);
      kryo.register(Message.ClientServerMessage.JoinResponse.class);
      kryo.register(Message.ClientServerMessage.ChatMessage.class);
      kryo.register(Message.ClientServerMessage.TakeOwnershipRequest.class);
      kryo.register(Message.ClientServerMessage.TakeOwnershipResponse.class);
      kryo.register(Message.ClientServerMessage.ClientKickNotification.class);

      //Server-Robot Communication
      kryo.register(Message.RobotServerMessage.Join.class);
      kryo.register(Message.RobotServerMessage.JoinResponse.class);
      kryo.register(Message.RobotServerMessage.InfraredData.class);

      //Shared Messages
      kryo.register(Message.JoystickData.class);
   }

   /**
    * Contains the kryonet messages that are used to communicate between the client and the server
    */
   public static class Message
   {
      private Message()
      {
      }

      public static class ClientServerMessage extends Message
      {
         public static class ClientKickNotification extends ClientServerMessage
         {
            public ClientKickNotification()
            {

            }
         }

         public static class TakeOwnershipRequest extends ClientServerMessage
         {
            public int robotId;
            public int newClientId;

            public TakeOwnershipRequest()
            {
            }

            public TakeOwnershipRequest(int robotId, int newClientId)
            {
               this.robotId = robotId;
               this.newClientId = newClientId;
            }
         }

         public static class TakeOwnershipResponse extends ClientServerMessage
         {
            public int robotId;
            public int newClientId;
            public boolean success;

            public TakeOwnershipResponse()
            {
            }

            public TakeOwnershipResponse(int robotId, int newClientId, boolean success)
            {
               this.robotId = robotId;
               this.newClientId = newClientId;
               this.success = success;
            }
         }

         public static class ChatMessage extends ClientServerMessage
         {
            public String messageContent;
            public String sender;

            public ChatMessage()
            {
            }

            public ChatMessage(String messageContent, String sender)
            {
               this.messageContent = messageContent;
               this.sender = sender;
            }
         }

         public static class Join extends ClientServerMessage
         {
            public String clientName;
            public int clientId;

            public Join()
            {
            }

            public Join(String robotName, int clientId)
            {
               this.clientName = robotName;
               this.clientId = clientId;
            }
         }

         public static class JoinResponse extends ClientServerMessage
         {
            public boolean success;

            public JoinResponse()
            {

            }

            public JoinResponse(boolean success)
            {
               this.success = success;
            }
         }
      }

      public static class RobotServerMessage extends Message
      {
         public static class Join extends RobotServerMessage
         {
            public String robotName;
            public int robotId;

            public Join()
            {
            }

            public Join(String robotName, int robotId)
            {
               this.robotName = robotName;
               this.robotId = robotId;
            }
         }

         public static class JoinResponse extends RobotServerMessage
         {
            public boolean success;

            public JoinResponse()
            {

            }

            public JoinResponse(boolean success)
            {
               this.success = success;
            }
         }

         public static class InfraredData extends RobotServerMessage
         {
            //TODO: implement this
         }
      }

      public static class JoystickData extends Message
      {
         //TODO: implement
      }
   }

}

