package Util;

import framework.xml.XmlNode;
import tradingConsole.SlidingWindow.SequenceCommand;
import framework.xml.XmlAttribute;
import framework.xml.XmlConvert;

public final class BusinessCommandHelper
{
	public static SequenceCommand convertRawCommandToSequenceCommand(XmlNode command, boolean isReadLostCommand)
	{
		XmlAttribute firstSequenceAttribute = command.get_Attributes().get_ItemOf("FirstSequence");
		XmlAttribute lastSequenceAttribute = command.get_Attributes().get_ItemOf("LastSequence");
		int beginSequence = XmlConvert.toInt32(firstSequenceAttribute.get_Value());
		int endSequence = XmlConvert.toInt32(lastSequenceAttribute.get_Value());
		SequenceCommand sequenceCommand = new SequenceCommand(beginSequence, endSequence, command, isReadLostCommand);
		return sequenceCommand;
	}
}
