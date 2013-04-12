package Util;

public class IntegerHelper
{
	private static final int ByteLength = 8;
	private static final int Mask = 0x000000ff;
	private static final int CustomerBytesLength = 4;

	public static byte[] toCustomerBytes(int source)
	{
		byte[] result = new byte[CustomerBytesLength];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = getTheLowerByte(source);
			source >>= ByteLength;
		}
		return result;

	}

	public static int toCustomerInt(byte[] source)
	{
		int result = 0;
		for (int i = 0; i < source.length; i++)
		{
			result += (source[i] & 0xff) << (i * ByteLength);
		}
		return result;
	}

	private static byte getTheLowerByte(int source)
	{
		return (byte) (source & Mask);
	}
}
