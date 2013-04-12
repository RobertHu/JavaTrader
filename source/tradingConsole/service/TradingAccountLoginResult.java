package tradingConsole.service;

import framework.Guid;

public class TradingAccountLoginResult
{
	private Guid _userId;

	public Guid get_UserId()
	{
		return this._userId;
	}

	public TradingAccountLoginResult(Object[] results)
	{
		this._userId = (Guid) results[0];
	}
}
