package tradingConsole.service;

import framework.xml.XmlNode;

import tradingConsole.common.TransactionError;
import nu.xom.*;
import Util.CommandConstants;
import Util.*;
public class AssignResult
{
	private TransactionError _transactionError;
	private XmlNode _xmlTransaction;
	private XmlNode _xmlAccount;
	private XmlNode _xmlInstrument;

	public TransactionError get_TransactionError()
	{
		return this._transactionError;
	}

	public XmlNode get_XmlTransaction()
	{
		return this._xmlTransaction;
	}

	public XmlNode get_XmlAccount()
	{
		return this._xmlAccount;
	}

	public XmlNode get_XmlInstrument()
	{
		return this._xmlInstrument;
	}
	public AssignResult(Element data){
		this._xmlTransaction= XmlElementHelper.ConvertToXmlNode(data.getFirstChildElement("xmlTransaction"));
		this._xmlAccount=XmlElementHelper.ConvertToXmlNode(data.getFirstChildElement("xmlAccount"));
		this._xmlInstrument=XmlElementHelper.ConvertToXmlNode(data.getFirstChildElement("xmlInstrument"));
		this._transactionError= TransactionError.valueOf(TransactionError.class,data.getFirstChildElement("transactionError").getValue());
	}

	public AssignResult(Object[] results)
	{
		this._xmlTransaction = ( (XmlNode) (results[1]));
		this._xmlAccount = ( (XmlNode) (results[2]));
		this._xmlInstrument = ( (XmlNode) (results[3]));
		this._transactionError = ( (TransactionError) (results[0]));

	}
}
