package tradingConsole.ui;

import framework.DateTime;

import framework.TimeSpan;

import tradingConsole.Instrument;
import tradingConsole.TradingConsoleServer;
import tradingConsole.settings.SettingsManager;
import tradingConsole.ui.language.Language;
import tradingConsole.enumDefine.OrderTypeMask;
import tradingConsole.TradingConsole;

public class MakeMarketOnOpenOrder extends MakeLimitOrder
{
	public MakeMarketOnOpenOrder(TradingConsole tradingConsole,SettingsManager settingsManager,Instrument instrument)
	{
		super(tradingConsole,settingsManager,instrument);
	}

	public Object[] isAcceptTime()
	{
		return MakeMarketOnOpenOrder.isAllowTime(this._settingsManager,this._instrument,false);
	}

	public Object[] isCancelTime()
	{
		return MakeMarketOnOpenOrder.isAllowTime(this._settingsManager,this._instrument,true);
	}

	public static Object[] isAllowTime(SettingsManager settingsManager, Instrument instrument,boolean isCancelLMTOrder)
	{
		Object[] result = new Object[]{false,""};

		if (!instrument.get_IsActive())
		{
			result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt5;
			return result;
		}

		boolean canPlacePendingOrderAtAnyTime = instrument.get_CanPlacePendingOrderAtAnyTime();
		DateTime[] nextTradingTime = null;
		if(canPlacePendingOrderAtAnyTime) nextTradingTime = instrument.getNextTradingTime();
		DateTime openTime = canPlacePendingOrderAtAnyTime ? nextTradingTime[0] : instrument.get_TradingBeginTime();
		DateTime closeTime = canPlacePendingOrderAtAnyTime ? nextTradingTime[1] : instrument.get_TradingEndTime();
		//??????????????
		if (openTime == null || closeTime == null)
		{
			return result;
		}
		DateTime serverTime = TradingConsoleServer.appTime();
		double mooMocAcceptDuration = settingsManager.get_SystemParameter().get_MooMocAcceptDuration();
		double mooMocCancelDuration = settingsManager.get_SystemParameter().get_MooMocCancelDuration();
		DateTime tradeEndTime = null;
		DateTime tradeBeginTime = null;

		if (isCancelLMTOrder)
		{
			TimeSpan timeSpan = new TimeSpan(0,  ( (Double) mooMocCancelDuration).intValue(), 0);
			tradeEndTime = DateTime.substract(closeTime, timeSpan);
			tradeBeginTime = DateTime.substract(openTime, timeSpan);
		}
		else
		{
			TimeSpan timeSpan = new TimeSpan(0,  ( (Double) mooMocAcceptDuration).intValue(), 0);
			tradeEndTime = DateTime.substract(closeTime, timeSpan);
			tradeBeginTime = DateTime.substract(openTime, timeSpan);
		}

		if(canPlacePendingOrderAtAnyTime)
		{
			if (!serverTime.after(tradeBeginTime))
			{
				result[0] = true;
			}
			else
			{
				result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt2;
				result[0] = false;
			}
		}
		else
		{
			if (!serverTime.before(openTime) && !serverTime.after(tradeEndTime))
			{
				result[0] = true;
			}
			else
			{
				result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt2;
				result[0] = false;
			}
		}
		return result;
	}

	public Object[] isAccept()
	{
		return this.isAccept(OrderTypeMask.MarketOnOpen);
	}
}
