package tradingConsole.service;

import nu.xom.Element;
import Util.CommandConstants;

public class UpdatePasswordResult
{
	private boolean _isSucceed;
	private String _message;

	public boolean get_IsSucceed()
	{
		return this._isSucceed;
	}

	public String get_Message()
	{
		return this._message;
	}

	public UpdatePasswordResult(Element response)
	{
		this._message= response.getFirstChildElement("message").getValue();
		String success = response.getFirstChildElement("isSucceed").getValue();
		this._isSucceed= success.equals("1")?true:false;
	}

}
