package Packet;

import nu.xom.Element;
import Util.CommandConstants;

public class SignalObject
{
	private String _invokeID;
	private Element _result;
	private boolean isKeepAliveSucess;

	public synchronized boolean isKeepAliveSucess()
	{
		return isKeepAliveSucess;
	}

	public synchronized void setKeepAliveSucess(boolean isKeepAliveSucess)
	{
		this.isKeepAliveSucess = isKeepAliveSucess;
	}

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

	public synchronized boolean getIsError()
	{
		if(this._result!=null){
			Element error = this._result.getFirstChildElement("error");
			return error != null ? true : false;
		}
		else{
			return false;
		}
	}

	public synchronized void setResult(Element _result)
	{
		this._result = _result;
	}

}
