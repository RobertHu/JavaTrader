package Packet;

import java.nio.charset.Charset;

public class PacketContants
{
	public static final int HeadTotalLength = 6;
	public static final int IsPriceLength = 1;
	public static final int SessionHeaderLength = 1;
	public static final int ContentHeaderLength = 4;
	public static final int IsPriceIndex = 0;
	public static final int SessionHeaderLengthIndex = 1;
	public static final int ContentHeaderLengthIndex = 2;
	public static final Charset InvokeIDEncoding = Charset.forName("US-ASCII");
	public static final Charset SessionEncoding = Charset.forName("US-ASCII");
	public static final Charset ContentEncoding = Charset.forName("UTF-8");

	public static final String CommandRootName = "Request";
	public static final String CommandArgumentName = "Arguments";
	public static final String CommandMethonName = "Method";
	public static final String CommandInvokeIdName= "InvokeId";

	public static final int AppType = 7;

	public static final String CollectionItemSeperator = ",";

	public static final byte KEEP_ALIVE_VALUE = 0X02;
	public static final byte PRICE_MASK = 0X01;
	public static final byte KEEP_ALIVE_SUCCESS_MASKE = 0X04;
	public static final byte PLAIN_STRING = 0X08;


	public static final String COMMAND_SEQUENCE = "C_S";


}
