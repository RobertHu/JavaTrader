package tradingConsole.bursa;

import framework.data.*;
import tradingConsole.enumDefine.*;

public class SystemParameter
{
	private boolean _allowPlaceMOO;
	private boolean _allowPlaceMTL;
	private boolean _allowPlaceStopLimit;
	private boolean _allowPlaceFAKMarket;
	private boolean _allowPlaceLimit;
	private boolean _allowPlaceStop;

	public void setValue(DataRow dataRow)
	{
		this._allowPlaceMOO = (Boolean)dataRow.get_Item("AllowMOO");
		this._allowPlaceMTL = (Boolean)dataRow.get_Item("AllowMTL");
		this._allowPlaceStopLimit = (Boolean)dataRow.get_Item("AllowStopLimit");
		this._allowPlaceFAKMarket = (Boolean)dataRow.get_Item("AllowFAKMarket");
		this._allowPlaceLimit = (Boolean)dataRow.get_Item("AllowLimit");
		this._allowPlaceStop = (Boolean)dataRow.get_Item("AllowStop");
	}

	public boolean isAllowOrderType(OrderType orderType)
	{
		if(orderType == OrderType.Market || orderType == OrderType.MarketToLimit
			|| orderType == OrderType.Limit || orderType == OrderType.StopLimit)
		{
			return true;
		}
		else
		{
			return false;
		}
		/*if(orderType == OrderType.MarketOnOpen)
		{
			return this._allowPlaceMOO;
		}
		else if(orderType == OrderType.MarketToLimit)
		{
			return this._allowPlaceMTL;
		}
		else if(orderType == OrderType.StopLimit)
		{
			return this._allowPlaceStopLimit;
		}
		else if(orderType == OrderType.FAK_Market)
		{
			return this._allowPlaceFAKMarket;
		}
		else if(orderType == OrderType.Limit)
		{
			return this._allowPlaceLimit;
		}
		else if(orderType == OrderType.Stop)
		{
			return this._allowPlaceStop;
		}
		else
		{
			return false;
		}*/
	}

	public boolean isAllowOrderType(OrderTypeMask orderTypeMask)
	{
		if(orderTypeMask == OrderTypeMask.Market || orderTypeMask == OrderTypeMask.MarketToLimit
			|| orderTypeMask == OrderTypeMask.Limit || orderTypeMask == OrderTypeMask.StopLimit)
		{
			return true;
		}
		else
		{
			return false;
		}

		/*if(orderTypeMask == OrderTypeMask.MarketOnOpen)
		{
			return this._allowPlaceMOO;
		}
		else if(orderTypeMask == OrderTypeMask.MarketToLimit)
		{
			return this._allowPlaceMTL;
		}
		else if(orderTypeMask == OrderTypeMask.StopLimit)
		{
			return this._allowPlaceStopLimit;
		}
		else if(orderTypeMask == OrderTypeMask.FAK_Market)
		{
			return this._allowPlaceFAKMarket;
		}
		else if(orderTypeMask == OrderTypeMask.Limit)
		{
			return this._allowPlaceLimit;
		}
		else
		{
			return false;
		}*/
	}
}
