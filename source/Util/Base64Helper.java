package Util;
import org.apache.commons.codec.binary.*;
public class Base64Helper
{
	public static byte[] decode(String base64String)
	{
		return Base64.decodeBase64(base64String);
	}
}
