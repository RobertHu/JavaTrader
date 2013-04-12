package tradingConsole.service;

import framework.xml.XmlNode;
import framework.DateTime;

public class AccountForCutResult
{
	private XmlNode _accountXmlNode;
	private DateTime _lastAlertTime;

	public XmlNode get_AccountXmlNode()
	{
		return this._accountXmlNode;
	}

	public DateTime get_LastAlertTime()
	{
		return this._lastAlertTime;
	}

	public AccountForCutResult(Object[] results)
	{
		this._lastAlertTime = ( (DateTime) (results[1]));
		this._accountXmlNode = ( (XmlNode) (results[0]));
	}
}
