package Util;

import framework.xml.XmlNode;
import tradingConsole.SlidingWindow.SequenceCommand;
import framework.xml.XmlAttribute;
import framework.xml.XmlConvert;
import nu.xom.Element;
import Packet.AliveKeeper;
import org.apache.log4j.Logger;

public final class BusinessCommandHelper
{
	private static Logger logger= Logger.getLogger(BusinessCommandHelper.class);
	public static SequenceCommand convertRawCommandToSequenceCommand(XmlNode command, boolean isReadLostCommand,Element command2)
	{
		if(command!=null){
			XmlAttribute firstSequenceAttribute = command.get_Attributes().get_ItemOf("FirstSequence");
			XmlAttribute lastSequenceAttribute = command.get_Attributes().get_ItemOf("LastSequence");
			int beginSequence = XmlConvert.toInt32(firstSequenceAttribute.get_Value());
			int endSequence = XmlConvert.toInt32(lastSequenceAttribute.get_Value());
			SequenceCommand sequenceCommand = new SequenceCommand(beginSequence, endSequence, command, isReadLostCommand);
			String str = String.format("first=%d,end=%d",beginSequence,endSequence);
			logger.debug(str);
			return sequenceCommand;
		}
		else{
			int beginSequence = XmlConvert.toInt32(command2.getAttributeValue("FirstSequence"));
			int endSequence = XmlConvert.toInt32(command2.getAttributeValue("LastSequence"));
			String str = String.format("first=%d,end=%d",beginSequence,endSequence);
			logger.debug(str);
			SequenceCommand sequenceCommand = new SequenceCommand(beginSequence, endSequence, command, isReadLostCommand);
			sequenceCommand.setCommand2(command2);
			return sequenceCommand;
		}
	}
}
