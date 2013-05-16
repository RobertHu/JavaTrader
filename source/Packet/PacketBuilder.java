package Packet;

import java.nio.CharBuffer;

import Util.IntegerHelper;
import Util.StringHelper;
import nu.xom.Element;
import Util.XmlElementHelper;

public class PacketBuilder
{
	public static byte[] Build(ComunicationObject target)
	{
		if(!StringHelper.IsNullOrEmpty(target.getInvokeID())
			&&
		    (!target.getIsKeepAlive())
			){
			appendInvokeIDToContent(target.getContent(), target.getInvokeID());
		}
		byte isPriceByte = target.getIsKeepAlive() ? (byte) PacketContants.KEEP_ALIVE_VALUE : (byte) 0;
		byte[] sessionBytes = getSessionBytes(target.getSession());
		byte[] contentBytes;
		if(target.getIsKeepAlive()){
			contentBytes = getKeepAliveContentBytes(target.getInvokeID());
		}
		else{
			contentBytes= getContentBytes(target.getContent().toXML());
		}
		byte[] contentLengthBytes = IntegerHelper
			.toCustomerBytes(contentBytes.length);
		byte sessionLengthByte = (byte)sessionBytes.length;
		int packetLength = PacketContants.HeadTotalLength + sessionBytes.length
			+ contentBytes.length;
		byte[] packet = new byte[packetLength];
		addHeaderToPacket(packet, isPriceByte, sessionLengthByte,
						  contentLengthBytes);
		addSessionToPacket(packet, sessionBytes, PacketContants.HeadTotalLength);
		addContentToPacket(packet, contentBytes,
						   PacketContants.HeadTotalLength + sessionBytes.length);
		return packet;

	}

	private static void appendInvokeIDToContent(Element content, String invokeID){
			Element invokeIdNode = XmlElementHelper.create(PacketContants.CommandInvokeIdName, invokeID);
			content.appendChild(invokeIdNode);
	}

	private static byte[] getKeepAliveContentBytes(String content) {
		byte[] bytes = PacketContants.SessionEncoding.encode(CharBuffer.wrap(content.toCharArray())).array();
		return bytes;
	}


	private static byte[] getSessionBytes(String session)
	{
		if (StringHelper.IsNullOrEmpty(session))
			return new byte[0];
		byte[] bytes = PacketContants.SessionEncoding.encode(
			CharBuffer.wrap(session.toCharArray())).array();
		return bytes;
	}

	private static byte[] getInvokeIdBytes(String invokeID)
	{
		if (StringHelper.IsNullOrEmpty(invokeID))
			return new byte[0];
		byte[] bytes = PacketContants.InvokeIDEncoding.encode(
			CharBuffer.wrap(invokeID.toCharArray())).array();
		return bytes;
	}

	private static byte[] getContentBytes(String xml)
	{
		byte[] bytes = PacketContants.ContentEncoding.encode(
			CharBuffer.wrap(xml.toCharArray())).array();
		return bytes;
	}

	private static void addHeaderToPacket(byte[] packet, byte isPriceByte,
										  byte sessionLengthByte, byte[] contentLengthBytes)
	{
		packet[PacketContants.IsPriceIndex] = isPriceByte;
		packet[PacketContants.SessionHeaderLengthIndex] = sessionLengthByte;
		System.arraycopy(contentLengthBytes, 0, packet,
						 PacketContants.ContentHeaderLengthIndex,
						 PacketContants.ContentHeaderLength);
	}

	private static void addSessionToPacket(byte[] packet, byte[] sessionBytes,
										   int index)
	{
		System.arraycopy(sessionBytes, 0, packet, index, sessionBytes.length);
	}

	private static void addContentToPacket(byte[] packet, byte[] contentBytes,
										   int index)
	{
		System.arraycopy(contentBytes, 0, packet, index, contentBytes.length);
	}

}
