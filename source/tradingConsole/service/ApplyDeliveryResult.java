package tradingConsole.service;

import tradingConsole.common.TransactionError;
import nu.xom.Element;

public class ApplyDeliveryResult
{
	private TransactionError _transactionError;
	private String _balance;
	private String _usableMargin;

	public TransactionError get_TransactionError()
	{
		return this._transactionError;
	}

	public String get_Balance()
	{
		return this._balance;
	}

	public String get_UsableMargin()
	{
		return this._usableMargin;
	}

	public ApplyDeliveryResult(Object[] results)
	{
		this._transactionError = ( (TransactionError) (results[0]));
		this._balance = ( (String) (results[1]));
		this._usableMargin = ( (String) (results[2]));
	}

	public ApplyDeliveryResult(Element element)
	{
	    this._balance=element.getFirstChildElement("balance").getValue();
		this._usableMargin=element.getFirstChildElement("usableMargin").getValue();
		this._transactionError = TransactionError.valueOf(TransactionError.class,element.getFirstChildElement("transactionError").getValue());
	}


}
