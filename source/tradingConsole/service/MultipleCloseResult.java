package tradingConsole.service;

import tradingConsole.common.TransactionError;
import framework.xml.XmlNode;

public class MultipleCloseResult
{
	private TransactionError _transactionError;
	private XmlNode _xmlTran;
	private XmlNode _xmlAccount;

	public TransactionError get_TransactionError()
	{
		return this._transactionError;
	}

	public XmlNode get_XmlTran()
	{
		return this._xmlTran;
	}

	public XmlNode get_XmlAccount()
	{
		return this._xmlAccount;
	}

	public MultipleCloseResult(Object[] result)
	{
		this._transactionError = (TransactionError)result[0];
		this._xmlTran = (XmlNode)result[1];
		this._xmlAccount = (XmlNode)result[2];
	}
}
