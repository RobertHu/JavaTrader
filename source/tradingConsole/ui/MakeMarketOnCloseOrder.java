package tradingConsole.ui;

import framework.DateTime;
import framework.TimeSpan;

import tradingConsole.Instrument;
import tradingConsole.TradingConsoleServer;
import tradingConsole.settings.SettingsManager;
import tradingConsole.ui.language.Language;
import tradingConsole.enumDefine.OrderTypeMask;
import tradingConsole.TradingConsole;
import tradingConsole.Account;
import tradingConsole.TradePolicyDetail;
import java.util.Iterator;

public class MakeMarketOnCloseOrder extends MakeLimitOrder
{

	public MakeMarketOnCloseOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		super(tradingConsole, settingsManager, instrument);
	}

	public Object[] isAcceptTime()
	{
		return this.isAllowTime(this._settingsManager, this._instrument, false);
	}

	public static Object[] isAllowTime(SettingsManager settingsManager, Instrument instrument, boolean isCancelLMTOrder)
	{
		Object[] result = new Object[]
			{false, ""};

		if (!instrument.get_IsActive())
		{
			result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt5;
			return result;
		}

		DateTime openTime = instrument.get_TradingBeginTime();
		DateTime closeTime = instrument.get_MOCTime();// instrument.get_TradingEndTime();
		if (openTime == null || closeTime == null)
		{
			return result;
		}

		DateTime serverTime = TradingConsoleServer.appTime();
		double mooMocAcceptDuration = settingsManager.get_SystemParameter().get_MooMocAcceptDuration();
		double mooMocCancelDuration = settingsManager.get_SystemParameter().get_MooMocCancelDuration();
		DateTime tradeEndTime = null;
		if (isCancelLMTOrder)
		{
			TimeSpan timeSpan = new TimeSpan(0, ( (Double)mooMocCancelDuration).intValue(), 0);
			tradeEndTime = DateTime.substract(closeTime, timeSpan);
		}
		else
		{
			TimeSpan timeSpan = new TimeSpan(0, ( (Double)mooMocAcceptDuration).intValue(), 0);
			tradeEndTime = DateTime.substract(closeTime, timeSpan);
		}

		boolean canPlacePendingOrderAtAnyTime = instrument.get_CanPlacePendingOrderAtAnyTime();
		if ( (openTime.after(tradeEndTime) && (!serverTime.before(tradeEndTime) || canPlacePendingOrderAtAnyTime) && !serverTime.after(openTime))
			|| (!openTime.after(tradeEndTime) && !serverTime.after(tradeEndTime) && (!serverTime.before(openTime) || canPlacePendingOrderAtAnyTime))
			|| (serverTime.after(instrument.get_MOCTime()) && canPlacePendingOrderAtAnyTime))
		{
			result[0] = true;
		}
		else
		{
			result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt3;
			result[0] = false;
		}

		return result;
	}

	public Object[] isAccept()
	{
		return this.isAccept(OrderTypeMask.MarketOnClose);
	}

}
