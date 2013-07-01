package Packet;

@ClassProduceMark public class ReformMessage
{
	private ReformMessage(){}
	@FieldProduceMark public static String Relogin = "The network has problem, please login again!";
	@FieldProduceMark public static String Disconnect = "The network has problem,Please check";
	@FieldProduceMark public static String Kickout = "The Account has Logined in some another place, you have been forced offline!";
}
