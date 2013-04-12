package tradingConsole.ui;

import java.math.*;

import tradingConsole.*;

public class IfDoneInfo
{
	private Price _limitPriceForIfLimitDone;
	private Price _stopPirceForIfLimitDone;
	private Price _limitPriceForIfStopDone;
	private Price _stopPirceForIfStopDone;
	private boolean _isBuy;
	private BigDecimal _lot;

	public IfDoneInfo(Price limitPriceForIfLimitDone, Price stopPirceForIfLimitDone, Price limitPriceForIfStopDone,
					  Price stopPirceForIfStopDone, boolean isBuy, BigDecimal lot)
	{
		this._limitPriceForIfLimitDone = limitPriceForIfLimitDone;
		this._stopPirceForIfLimitDone = stopPirceForIfLimitDone;
		this._limitPriceForIfStopDone = limitPriceForIfStopDone;
		this._stopPirceForIfStopDone = stopPirceForIfStopDone;
		this._isBuy = isBuy;
		this._lot = lot;
	}

	public Price get_LimitPriceForIfLimitDone()
	{
		return this._limitPriceForIfLimitDone;
	}

	public Price get_StopPirceForIfLimitDone()
	{
		return this._stopPirceForIfLimitDone;
	}

	public Price get_LimitPriceForIfStopDone()
	{
		return this._limitPriceForIfStopDone;
	}

	public Price get_StopPirceForIfStopDone()
	{
		return this._stopPirceForIfStopDone;
	}

	public boolean get_IsBuy()
	{
		return this._isBuy;
	}

	public BigDecimal get_Lot()
	{
		return this._lot;
	}

	public String getMakeOrderConfirmXml()
	{
		return "";
	}
}
