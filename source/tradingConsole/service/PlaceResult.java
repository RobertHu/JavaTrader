package tradingConsole.service;

import tradingConsole.common.TransactionError;
import nu.xom.Element;

public class PlaceResult
{
	private TransactionError _transactionError;
	private String _tranCode;

	public TransactionError get_TransactionError()
	{
		return this._transactionError;
	}

	public String get_TranCode()
	{
		return this._tranCode;
	}

	public PlaceResult(Element data){
		this._tranCode = data.getFirstChildElement("tranCode").getValue();
		String tranError = data.getFirstChildElement("transactionError").getValue();
		this._transactionError=TransactionError.valueOf(TransactionError.class,tranError);
	}

	public PlaceResult(Object[] results)
	{
		this._tranCode = ( (String) (results[1]));
		this._transactionError = ( (TransactionError) (results[0]));
	}

}
