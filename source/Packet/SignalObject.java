package Packet;

import nu.xom.Element;
import Util.CommandConstants;

public class SignalObject
{
	private String _invokeID;
	private Element _result;
	public synchronized String getInvokeID()
	{
		return _invokeID;
	}

	public synchronized void setInvokeID(String _invokeID)
	{
		this._invokeID = _invokeID;
	}

	public synchronized Element getResult()
	{
		return _result;
	}

	public synchronized boolean getIsError(){
		Element error = this._result.getFirstChildElement("error");
		return error != null ? true : false;
	}

	public synchronized void setResult(Element _result)
	{
		this._result = _result;
	}

}
