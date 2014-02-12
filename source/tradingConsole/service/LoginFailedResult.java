package tradingConsole.service;

import nu.xom.Element;
import Packet.StringConstants;

public class LoginFailedResult extends LoginResult
{
	private LoginResultStatus _status;
	private boolean _disallowLogin;
	public LoginFailedResult(Element ele)
	{
		super();
		Element errorNode = ele.getFirstChildElement(StringConstants.ErrorNodeName);
		String statusName = errorNode.getAttributeValue("Status");
		_status = LoginResultStatus.valueOf(LoginResultStatus.class,statusName);
		_disallowLogin = errorNode.getAttributeValue("DisallowLogin").equals("1")?true:false;
	}
	public LoginResultStatus status(){
		return _status;
	}

	public boolean disallowLogin(){
		return _disallowLogin;
	}
}
