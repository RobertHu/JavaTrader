package Util;

import Packet.SignalObject;
import Packet.SignalContainer;

public final class SignalHelper
{
	public static SignalObject add(String invokeId)
	{
		SignalObject signal = new SignalObject();
		signal.setInvokeID(invokeId);
		SignalContainer.Default.add(invokeId, signal);
		return signal;
	}
}
