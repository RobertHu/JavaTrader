package tradingConsole.service;

public class GetCustomerInfoResult
{
	private String _customerInfo;
	private String _customerName;

	public String get_CustomerInfo()
	{
		return this._customerInfo;
	}
	public String get_CusotmerName()
	{
		return this._customerName;
	}

	public GetCustomerInfoResult(Object[] results)
	{
		this._customerName = ((String) (results[1]));
		this._customerInfo = ((String) (results[0]));
	}
}
