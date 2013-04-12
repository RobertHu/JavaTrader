package Util;

import nu.xom.Element;
import Packet.ComunicationObject;
import java.util.UUID;
import Packet.PacketContants;
import Connection.ConnectionManager;
import Packet.SignalObject;
import java.io.IOException;
import framework.data.DataSet;
import Packet.LoginInfoManager;
import framework.xml.XmlNode;
import framework.Guid;
import Packet.StringConstants;
import Packet.WaitTimeoutException;
import Packet.PacketBuilder;
import java.io.OutputStream;
import javax.net.ssl.SSLSocket;

public class RequestCommandHelper
{
	private static OutputStream outputStream;

	public static ComunicationObject newCommandWithSession(String methodName, Element root)
	{
		return newCommand(LoginInfoManager.Default.getSession(), methodName, root);
	}

	public static ComunicationObject newCommandWithNoSession(String methodName, Element root)
	{
		return newCommand("", methodName, root);
	}


	private static ComunicationObject newCommand(String session, String methodName, Element root)
	{
		UUID invokeId = UUID.randomUUID();
		root.appendChild(XmlElementHelper.create(
			PacketContants.CommandMethonName, methodName));
		ComunicationObject target = new ComunicationObject(session, invokeId.toString(), root);
		return target;
	}


	public static SignalObject request(ComunicationObject command) throws IOException, InterruptedException, WaitTimeoutException
	{
		SignalObject signal = SignalHelper.add(command.getInvokeID());
		sendCommand(command);
		WaitTimeoutHelper.wait(signal);
		return signal;
	}

	private static void sendCommand(ComunicationObject command) throws IOException
	{
		byte[] packet = PacketBuilder.Build(command);
		outputStream.write(packet);
		outputStream.flush();
	}

	public static void setOutputStream(OutputStream stream){
		outputStream = stream;
	}






	public static DataSet getDataFromResponse(Element response)
	{
		Element data = getDataByNormal(response);
		return XmlElementHelper.convertToDataset(data);
	}

	public static XmlNode getNodeFromResponse(Element response)
	{
		Element data = getDataByNormal(response);
		return XmlElementHelper.ConvertToXmlNode(data);
	}

	public static boolean getBoolFromResponse(Element response)
	{
		Element data = getDataByNormal(response);
		boolean result = data.getValue().equals(StringConstants.BoolTrueSymbol) ? true : false;
		return result;
	}

	public static Guid getGuidFromResponse(Element response){
		Element data = getDataByNormal(response);
		Guid result = new Guid(data.getValue());
		return result;
	}

	public static byte[] getBytesFromResponse(Element response){
		Element data = getDataByNormal(response);
		byte[] result = Base64Helper.decode(data.getValue());
		return result;
	}

	public static String[] getStringArrayFromResponse(Element response){
		Element data = getDataByNormal(response);
		String[] result = data.getValue().split(StringConstants.ArrayItemSeparator);
		return result;
	}

	public static Guid[] getGuidArrayFromResponse(Element response){
		String[] source = getStringArrayFromResponse(response);
		Guid[] result = new Guid[source.length];
		for(int i=0;i<source.length;i++){
			result[i] = new Guid(source[i]);
		}
		return result;
	}


	public static String getStringFromResponse(Element response){
		Element data = getDataByNormal(response);
		return data.getValue();
	}

	private static Element getDataByNormal(Element response){
		Element data = response.getFirstChildElement(StringConstants.ResultNodeName);
		return data;
	}






}
