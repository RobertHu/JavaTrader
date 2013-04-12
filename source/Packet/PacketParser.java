package Packet;

import java.io.IOException;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import Util.IntegerHelper;
import Util.XmlElementHelper;
import Util.ZlibHelper;
import java.util.zip.DataFormatException;

public class PacketParser
{
	public static ComunicationObject parse(byte[] packet) throws ValidityException, ParsingException, IOException, DataFormatException
	{

		//System.out.println(String.format("packet length: %d", packet.length));

		boolean isPrice = packet[PacketContants.IsPriceIndex] == 1 ? true
			: false;
		byte sessionLength = packet[PacketContants.SessionHeaderLengthIndex];

		byte[] contentLengthBytes = new byte[PacketContants.ContentHeaderLength];
		System.arraycopy(packet, PacketContants.ContentHeaderLengthIndex,
						 contentLengthBytes, 0, contentLengthBytes.length);
		int contentLength = IntegerHelper.toCustomerInt(contentLengthBytes);
		//System.out.println(String.format("content length: %d", contentLength));
		int contentIndex = PacketContants.HeadTotalLength + sessionLength;

		byte[] contentBytes = new byte[contentLength];
		System.arraycopy(packet, contentIndex, contentBytes, 0, contentLength);
		ComunicationObject result;
		if (isPrice)
		{
			result = new ComunicationObject(null, contentBytes);
		}
		else
		{
			contentBytes = ZlibHelper.Decompress(contentBytes);
			int sessionIndex = PacketContants.HeadTotalLength;
			byte[] sessionBytes = new byte[sessionLength];
			System.arraycopy(packet, sessionIndex, sessionBytes, 0,
							 sessionLength);
			String session = new String(sessionBytes,
										PacketContants.SessionEncoding);


			String content = new String(contentBytes,
										PacketContants.ContentEncoding);
			Element xmlContent = XmlElementHelper.parse(content);
			Element invokeIdElement = xmlContent.getFirstChildElement(PacketContants.CommandInvokeIdName);
			String invokeId = invokeIdElement==null?"":invokeIdElement.getValue();
			result = new ComunicationObject(session, invokeId.trim(), xmlContent);
			result.setRawContent(content);
		}
		return result;
	}
}
