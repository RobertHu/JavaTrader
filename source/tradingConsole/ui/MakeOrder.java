package tradingConsole.ui;

import java.math.*;
import java.util.*;

import framework.*;
import framework.DateTime;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.language.*;

public class MakeOrder
{
	protected TradingConsole _tradingConsole;
	protected SettingsManager _settingsManager;
	protected Instrument _instrument;
	protected Account _account;

	public MakeOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		this(tradingConsole, settingsManager, instrument, null);
	}
	public MakeOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Account account)
	{
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._instrument = instrument;
		this._account = account;
	}

	public TradingConsole get_TradingConsole()
	{
		return this._tradingConsole;
	}

	public SettingsManager get_SettingsManager()
	{
		return this._settingsManager;
	}

	public Instrument get_Instrument()
	{
		return this._instrument;
	}

	public static boolean isOperateSameInstrument(SettingsManager settingsManager, Instrument instrument)
	{
		return settingsManager.getMakeOrderWindow(instrument)!=null;
	}

	public static boolean isOverRangeOperateOrderUI(SettingsManager settingsManager)
	{
		return settingsManager.getMakeOrderWindowSize() >= Parameter.operateOrderCount;
	}

	//set isDisallowTrade from System parameter(BackOffice)
	public static boolean isDisallowTradeSetting(SettingsManager settingsManager)
	{
		return settingsManager.get_Customer().get_DisallowTrade();
	}

	public static boolean isAllowOrderType(Instrument instrument, OrderType orderType)
	{
		if(instrument.isFromBursa())
		{
			return instrument.get_SettingsManager().get_BursaSystemParameter().isAllowOrderType(orderType);
		}
		else if(orderType == OrderType.FAK_Market || orderType == OrderType.MarketToLimit || orderType == OrderType.StopLimit)
		{
			return false;
		}

		if(orderType == OrderType.Limit)
		{
			return MakeOrder.isAllowOrderTypeMask(instrument, OrderTypeMask.Limit);
		}
		else if(orderType == OrderType.Market)
		{
			return MakeOrder.isAllowOrderTypeMask(instrument, OrderTypeMask.Market);
		}
		else if(orderType == OrderType.MarketOnClose)
		{
			return MakeOrder.isAllowOrderTypeMask(instrument, OrderTypeMask.MarketOnClose);
		}
		else if(orderType == OrderType.MarketOnOpen)
		{
			return MakeOrder.isAllowOrderTypeMask(instrument, OrderTypeMask.MarketOnOpen);
		}
		else if(orderType == OrderType.OneCancelOther)
		{
			return MakeOrder.isAllowOrderTypeMask(instrument, OrderTypeMask.OneCancelOther);
		}
		else if(orderType == OrderType.SpotTrade)
		{
			return MakeOrder.isAllowOrderTypeMask(instrument, OrderTypeMask.SpotTrade);
		}
		else
		{
			return false;
		}
	}

	public static boolean isAllowOrderTypeMask(Instrument instrument, OrderTypeMask orderTypeMask)
	{
		if(instrument.isFromBursa())
		{
			return instrument.get_SettingsManager().get_BursaSystemParameter().isAllowOrderType(orderTypeMask);
		}
		else if(orderTypeMask == OrderTypeMask.FAK_Market || orderTypeMask == OrderTypeMask.MarketToLimit || orderTypeMask == OrderTypeMask.StopLimit)
		{
			return false;
		}

		if(orderTypeMask.value() == OrderTypeMask.LimitStop.value())
		{
			return (instrument.get_OrderTypeMask() & OrderTypeMask.Limit.value()) == OrderTypeMask.Limit.value();
		}
		else
		{
			return (instrument.get_OrderTypeMask() & orderTypeMask.value()) == orderTypeMask.value();
		}
	}

	public static Object[] isAcceptTime(OrderType orderType,SettingsManager settingsManager, Instrument instrument,boolean isCancelLMTOrder)
	{
		if (orderType == OrderType.SpotTrade)
		{
			return MakeSpotTradeOrder.isAllowTime(instrument);
		}
		else if (orderType == OrderType.Limit)
		{
			return MakeLimitOrder.isAllowTime(settingsManager, instrument);
		}
		else if (orderType == OrderType.Market)
		{
			return MakeMarketOrder.isAllowTime(instrument);
		}
		else if (orderType == OrderType.OneCancelOther)
		{
			return MakeOneCancelOtherOrder.isAllowTime(settingsManager, instrument);
		}
		else if (orderType == OrderType.MarketOnOpen)
		{
			return MakeMarketOnOpenOrder.isAllowTime(settingsManager,instrument,isCancelLMTOrder);
		}
		else if (orderType == OrderType.MarketOnClose)
		{
			return MakeMarketOnCloseOrder.isAllowTime(settingsManager,instrument,isCancelLMTOrder);
		}
		else
		{
			return MakeOrder.isAllowTime(instrument);
		}
	}

	public Object[] isAcceptTime()
	{
		return MakeOrder.isAllowTime(this._instrument);
	}

	//for SpotTrade,Limit,Market,OneCancelOther
	public static Object[] isAllowTime(Instrument instrument)
	{
		Object[] result = new Object[]{false, "", false};

		if(instrument.isFromBursa())
		{
			result[0] = instrument.getIsValidate();
			result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt0;
			return result;
		}

		if (!instrument.get_IsActive())
		{
			result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt5;
			return result;
		}

		DateTime openTime = instrument.get_OpenTime();
		DateTime closeTime = instrument.get_CloseTime();
		if (openTime == null || closeTime == null)
		{
			result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt0;
			return result;
		}

		DateTime appTime = TradingConsoleServer.appTime();
		int lastAcceptTimeSpan = instrument.get_LastAcceptTimeSpan();
		if (appTime.after(closeTime.addMinutes(0 - lastAcceptTimeSpan)))
		{
			result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt1;
			result[2] = true;
			return result;
		}
		if (!appTime.before(openTime) && !appTime.after(closeTime))
		{
			result[0] = true;
		}
		else
		{
			result[2] = true;
			result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt4;
		}

		return result;
	}

	public static Object[] isAllowTime(SettingsManager settingsManager, Instrument instrument)
	{
		return MakeOrder.isAllowTime(settingsManager, instrument, null);
	}
	//for SpotTrade,Limit,Market,OneCancelOther
	public static Object[] isAllowTime(SettingsManager settingsManager, Instrument instrument, Account account)
	{
		Object[] result = new Object[]{false, ""};

		if(instrument.isFromBursa())
		{
			result[0] = instrument.getIsValidate();
			result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt0;
			return result;
		}

		if (!instrument.get_IsActive())
		{
			result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt5;
			return result;
		}

		boolean canPlacePendingOrderAtAnyTime = instrument.get_CanPlacePendingOrderAtAnyTime();

		DateTime openTime = instrument.get_OpenTime();
		DateTime closeTime = instrument.get_CloseTime();
		if ((openTime == null || closeTime == null) && !canPlacePendingOrderAtAnyTime)
		{
			result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt0;
			return result;
		}

		if(canPlacePendingOrderAtAnyTime)
		{
			result[0] = true;
		}
		else
		{
			DateTime appTime = TradingConsoleServer.appTime();
			int lastAcceptTimeSpan = instrument.get_LastAcceptTimeSpan();
			if (appTime.after(closeTime.addMinutes(0 - lastAcceptTimeSpan)))
			{
				result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt1;
				return result;
			}
			if (!appTime.before(openTime) && !appTime.after(closeTime))
			{
				result[0] = true;
			}
			else
			{
				result[1] = Language.TradeConsoleIsValidOperateOrderTimePrompt4;
			}
		}
		return result;
	}

	public static boolean isMultiple(BigDecimal lot, BigDecimal multiplier)
	{
		if (multiplier.compareTo(BigDecimal.ZERO) == 0)
		{
			return true;
		}

		while(lot.compareTo(multiplier) >= 0)
		{
			lot = lot.subtract(multiplier);
		}
		return lot.compareTo(BigDecimal.ZERO) == 0;

		//???
		//return (lot.floatValue() * 10) % (multiplier * 10) == 0;
	}

	public static Object[] isAcceptEntrance(SettingsManager settingsManager, MakeOrderAccount account, Instrument instrument,boolean isOpen, BigDecimal lot, Price price)
	{
		Object[] result = new Object[]{false,""};

		boolean isAccept = false;

		TradePolicyDetail tradePolicyDetail = settingsManager.getTradePolicyDetail(account.get_Account().get_TradePolicyId(), instrument.get_Id());
		if (tradePolicyDetail == null)
		{
			result[1] = "Not Exists TradePolicyDetail!";
			return result;
		}

		if(!isOpen)
		{
			return MakeOrder.isAcceptCloseEntrance(tradePolicyDetail, account, instrument, lot);
		}

		/*BigDecimal minOpen = tradePolicyDetail.get_MinOpen();
		BigDecimal minClose = tradePolicyDetail.get_MinClose();

		BigDecimal openMultiplier = tradePolicyDetail.get_OpenMultiplier();
		BigDecimal closeMultiplier = tradePolicyDetail.get_CloseMultiplier();*/

		//Option (1. Quantity, 2. Quantity * Contract Size, 3. Quantity * Contract Size * price, 4. Quantity * Contract Size / price)
		int option = tradePolicyDetail.get_Option();
		BigDecimal contractSize = tradePolicyDetail.get_ContractSize();
		BigDecimal contractSize2 = contractSize;

		BigDecimal minValue = isOpen ? tradePolicyDetail.get_MinOpen() : tradePolicyDetail.get_MinClose();
		minValue=minValue.multiply(account.get_Account().get_RateLotMin());
		BigDecimal multiplier = isOpen ? tradePolicyDetail.get_OpenMultiplier() : tradePolicyDetail.get_CloseMultiplier();
		multiplier = multiplier.multiply(account.get_Account().get_RateLotMultiplier());

		BigDecimal defaultLot = AppToolkit.getDefaultLot(instrument, isOpen, tradePolicyDetail, account);

		BigDecimal lot2 = BigDecimal.ZERO;

		switch (option)
		{
			case 1: //Quantity
				isAccept = lot.compareTo(minValue) == 0 || lot.compareTo(defaultLot) == 0
					|| (lot.compareTo(minValue) > 0 && MakeOrder.isMultiple(lot, multiplier));
				break;
			case 2: //Quantity * Contract Size
				lot2= lot.multiply(contractSize2);
				isAccept = lot2.compareTo(minValue) == 0 || lot.compareTo(defaultLot) == 0
					|| (lot2.compareTo(minValue) > 0 && MakeOrder.isMultiple(lot, contractSize2));

				/*isAccept = ( (isOpen && (lot.multiply(contractSize2).compareTo(minOpen)) >= 0 && MakeOrder.isMultiple(lot.subtract(minOpen), (contractSize * openMultiplier.doubleValue())))
							|| (!isOpen && (lot.multiply(contractSize2).compareTo(minClose)) >= 0 && MakeOrder.isMultiple(lot.subtract(minClose), (contractSize * closeMultiplier.doubleValue()))));*/
				break;
			case 3: //Quantity * Contract Size * price
				if (price == null)
				{
					isAccept = true;
				}
				else
				{
					BigDecimal priceValue = Price.toBigDecimal(price);
					lot2= lot.multiply(contractSize2).multiply(priceValue);
					multiplier = priceValue.multiply(multiplier);
					multiplier = contractSize.multiply(multiplier);
					isAccept = lot2.compareTo(minValue) == 0 || lot.compareTo(defaultLot) == 0
						|| ( lot2.compareTo(minValue) > 0 && MakeOrder.isMultiple(lot, multiplier));

					/*isAccept = ( (isOpen && (lot.multiply(contractSize2).multiply(priceValue2).compareTo(minOpen)) >= 0 &&
								  MakeOrder.isMultiple(lot.subtract(minOpen), (contractSize * priceValue * openMultiplier.doubleValue())))
								||
								(!isOpen && (lot.multiply(contractSize2).multiply(priceValue2).compareTo(minClose)) >= 0 &&
								 MakeOrder.isMultiple(lot.subtract(minClose), (contractSize * priceValue * closeMultiplier.doubleValue()))));*/
				}
				break;
			case 4: //Quantity * Contract Size / price
				if (price == null)
				{
					isAccept = true;
				}
				else
				{
					BigDecimal priceValue = Price.toBigDecimal(price);
					if (priceValue.compareTo(BigDecimal.ZERO) == 0)
					{
						isAccept = true;
					}
					else
					{
						//BigDecimal priceValue2 = new BigDecimal(priceValue);
						lot2= lot.multiply(contractSize2).divide(priceValue, BigDecimal.ROUND_HALF_EVEN);
						multiplier = priceValue.multiply(multiplier);
						multiplier = contractSize.divide(multiplier, BigDecimal.ROUND_HALF_EVEN);
						isAccept = lot2.compareTo(minValue) == 0  || lot.compareTo(defaultLot) == 0
							|| (lot2.compareTo(minValue) > 0 && MakeOrder.isMultiple(lot, multiplier));

						/*isAccept = ( (isOpen && (lot.multiply(contractSize2).divide(priceValue2, BigDecimal.ROUND_HALF_EVEN).compareTo(minOpen)) >= 0 &&
									  MakeOrder.isMultiple(lot.subtract(minOpen), (contractSize / priceValue * openMultiplier.doubleValue())))
									||
									(!isOpen && (lot.multiply(contractSize2).divide(priceValue2, BigDecimal.ROUND_HALF_EVEN).compareTo(minClose)) >= 0 &&
									 MakeOrder.isMultiple(lot.subtract(minClose), (contractSize / priceValue * closeMultiplier.doubleValue()))));*/
					}
				}
				break;
		}
		if (isAccept == false)
		{
			result[1] = Language.EntranceForInput + "\n" + account.get_Code() + "\n" + Language.EntranceForInput2;
		}
		result[0] = isAccept;
		return result;
	}

	private static Object[] isAcceptCloseEntrance(TradePolicyDetail tradePolicyDetail, MakeOrderAccount account, Instrument instrument, BigDecimal lot)
	{
		Object[] result = new Object[]{false,""};
		boolean isAccept = false;

		BigDecimal minValue = tradePolicyDetail.get_MinClose();
		minValue=minValue.multiply(account.get_Account().get_RateLotMin());
		BigDecimal multiplier = tradePolicyDetail.get_CloseMultiplier();
		multiplier = multiplier.multiply(account.get_Account().get_RateLotMultiplier());
		BigDecimal defaultLot = AppToolkit.getDefaultLot(instrument, false, tradePolicyDetail, account);
		if(lot.compareTo(minValue) == 0 || lot.compareTo(defaultLot) == 0)
		{
			isAccept = true;
		}
		else if(lot.compareTo(minValue) > 0)
		{
			if(MakeOrder.isMultiple(lot, multiplier))
			{
				isAccept = true;
			}
			else
			{
				BigDecimal totalCloseLotOfFullClose = account.getTotalCloseLotOfFullClose();
				if(lot.compareTo(totalCloseLotOfFullClose) == 0
				   || MakeOrder.isMultiple(lot.subtract(totalCloseLotOfFullClose), multiplier))
				{
					isAccept = true;
				}
			}
		}

		if (isAccept == false)
		{
			result[1] = Language.EntranceForInput + "\n" + account.get_Code() + "\n" + Language.EntranceForInput2;
		}
		result[0] = isAccept;
		return result;
	}

	public static Object[] isAcceptEntrance(SettingsManager settingsManager, MakeOrderAccount account,Instrument instrument, OrderType orderType,
											boolean isBuy, BigDecimal lot, BigDecimal liqLots, Price setPrice,
											Price setPriceForOneCancelOther, boolean isAssignOrder)
	{
		Object[] result = new Object[]{false,""};

		if (lot.compareTo(BigDecimal.ZERO) > 0)
		{
			int operateType = 1;
			if (liqLots.compareTo(BigDecimal.ZERO) > 0)
			{
				if (lot.compareTo(liqLots) > 0)
				{
					operateType = 2;
				}
				else if (lot.compareTo(liqLots) == 0)
				{
					operateType = 0;
				}
			}
			Price entrancePrice = null;
			if ( (orderType.equals(OrderType.SpotTrade) && !isAssignOrder)
				|| orderType.equals(OrderType.Market)
				|| orderType.equals(OrderType.MarketOnOpen)
				|| orderType.equals(OrderType.MarketOnClose)
				|| orderType.equals(OrderType.MarketToLimit)
				|| orderType.equals(OrderType.FAK_Market))
			{
				entrancePrice = instrument.get_LastQuotation().getBuySell(isBuy);
			}
			else
			{
				entrancePrice = setPrice;
			}

			boolean isOpen;
			BigDecimal entranceLot = BigDecimal.ZERO;
			int startI = (operateType == 0 || operateType == 2) ? 0 : 1;
			int endI = (operateType == 0) ? 0 : 1;
			for (int i = startI; i <= endI; i++)
			{
				isOpen = (i == 0) ? false : true;
				entranceLot = ( (i == 0) ? liqLots : lot.subtract(liqLots));
				result = MakeOrder.isAcceptEntrance(settingsManager,account, instrument,isOpen, entranceLot, entrancePrice);
				if (!(Boolean)result[0])
				{
					break;
				}
			}
			if (((Boolean)result[0]) && orderType.equals(OrderType.OneCancelOther))
			{
				isOpen = false;
				entranceLot = liqLots;
				result = MakeOrder.isAcceptEntrance(settingsManager,account, instrument,isOpen,entranceLot, setPriceForOneCancelOther);
			}
		}
		return result;
	}

	public static Object[] isAllowMakeSpotTradeOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		return isAllowMakeSpotTradeOrder(tradingConsole, settingsManager, instrument, null);
	}

	//outer use
	public static Object[] isAllowMakeSpotTradeOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Account account)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,MakeSpotTradeOrder

		if(instrument.isFromBursa())
		{
			result[1] = Language.DisallowTradePrompt;
			return result;
		}

		MakeSpotTradeOrder makeSpotTradeOrder = new MakeSpotTradeOrder(tradingConsole, settingsManager, instrument, account);
		Object[] result2 = makeSpotTradeOrder.isAccept();

		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeSpotTradeOrder;
		return result;
	}

	public static Object[] isAllowMakeLimitOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument,Account relationAccount)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,MakeLimitOrder

		MakeLimitOrder makeLimitOrder = new MakeLimitOrder(tradingConsole, settingsManager, instrument);
		Object[] result2 = makeLimitOrder.isAccept();

		if (relationAccount!=null && (Boolean) result2[0])
		{
			if (makeLimitOrder.get_MakeOrderAccount(relationAccount.get_Id())==null)
			{
				result2[0] = false;
				result2[1] = Language.MakeOrderNotAllowLiqudate;
			}
		}

		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeLimitOrder;
		return result;
	}

	public static Object[] isAllowMakeStopOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument,Account relationAccount)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,MakeStopOrder

		MakeStopOrder makeStopOrder = new MakeStopOrder(tradingConsole, settingsManager, instrument);
		Object[] result2 = makeStopOrder.isAccept();

		if (relationAccount!=null && (Boolean) result2[0])
		{
			if (makeStopOrder.get_MakeOrderAccount(relationAccount.get_Id())==null)
			{
				result2[0] = false;
				result2[1] = Language.MakeOrderNotAllowLiqudate;
			}
		}

		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeStopOrder;
		return result;
	}

	public static Object[] isAllowMakeMarketOnOpenOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument,Account relationAccount)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,MakeMarketOnOpenOrder

		MakeMarketOnOpenOrder makeMarketOnOpenOrder = new MakeMarketOnOpenOrder(tradingConsole, settingsManager, instrument);
		Object[] result2 = makeMarketOnOpenOrder.isAccept();

		if (relationAccount!=null && (Boolean) result2[0])
		{
			if (makeMarketOnOpenOrder.get_MakeOrderAccount(relationAccount.get_Id())==null)
			{
				result2[0] = false;
				result2[1] = Language.MakeOrderNotAllowLiqudate;
			}
		}

		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeMarketOnOpenOrder;
		return result;
	}

	public static Object[] isAllowMakeMarketOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument,Account relationAccount)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,MakeMarketOrder

		MakeMarketOrder makeMarketOrder = new MakeMarketOrder(tradingConsole, settingsManager, instrument);
		Object[] result2 = makeMarketOrder.isAccept();

		if (relationAccount!=null && (Boolean) result2[0])
		{
			if (makeMarketOrder.get_MakeOrderAccount(relationAccount.get_Id())==null)
			{
				result2[0] = false;
				result2[1] = Language.MakeOrderNotAllowLiqudate;
			}
		}
		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeMarketOrder;
		return result;
	}

	public static Object[] isAllowMakeOneCancelOtherOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument,Account relationAccount)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,makeOneCancelOtherOrder

		MakeOneCancelOtherOrder makeOneCancelOtherOrder = new MakeOneCancelOtherOrder(tradingConsole, settingsManager, instrument);
		Object[] result2 = makeOneCancelOtherOrder.isAccept();

		if (relationAccount!=null && (Boolean) result2[0])
		{
			if (makeOneCancelOtherOrder.get_MakeOrderAccount(relationAccount.get_Id())==null)
			{
				result2[0] = false;
				result2[1] = Language.MakeOrderNotAllowLiqudate;
			}
		}
		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeOneCancelOtherOrder;
		return result;
	}

	public static Object[] isAllowMakeLimitStopOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument,Account relationAccount)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,makeOneCancelOtherOrder

		MakeLimitStopOrder makeLimitStopOrder = new MakeLimitStopOrder(tradingConsole, settingsManager, instrument);
		Object[] result2 = makeLimitStopOrder.isAccept();
		if (relationAccount!=null && (Boolean) result2[0])
		{
			if (makeLimitStopOrder.get_MakeOrderAccount(relationAccount.get_Id())==null)
			{
				result2[0] = false;
				result2[1] = Language.MakeOrderNotAllowLiqudate;
			}
		}
		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeLimitStopOrder;
		return result;
	}

	public static Object[] isAllowMakeMarketOnCloseOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument,Account relationAccount)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,MakeMarketOnCloseOrder

		MakeMarketOnCloseOrder makeMarketOnCloseOrder = new MakeMarketOnCloseOrder(tradingConsole, settingsManager, instrument);
		Object[] result2 = makeMarketOnCloseOrder.isAccept();

		if (relationAccount!=null && (Boolean) result2[0])
		{
			if (makeMarketOnCloseOrder.get_MakeOrderAccount(relationAccount.get_Id())==null)
			{
				result2[0] = false;
				result2[1] = Language.MakeOrderNotAllowLiqudate;
			}
		}
		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeMarketOnCloseOrder;
		return result;
	}

	public static Object[] isAllowMarketToLimitOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Account relationAccount)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,MakeMarketOnCloseOrder

		MakeMarketToLimitOrder makeMarketToLimitOrder = new MakeMarketToLimitOrder(tradingConsole, settingsManager, instrument);
		Object[] result2 = makeMarketToLimitOrder.isAccept();

		if (relationAccount!=null && (Boolean) result2[0])
		{
			if (makeMarketToLimitOrder.get_MakeOrderAccount(relationAccount.get_Id())==null)
			{
				result2[0] = false;
				result2[1] = Language.MakeOrderNotAllowLiqudate;
			}
		}
		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeMarketToLimitOrder;
		return result;
	}

	public static Object[] isAllowStopLimitOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Account relationAccount)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,MakeMarketOnCloseOrder

		MakeStopLimitOrder makeStopLimitOrder = new MakeStopLimitOrder(tradingConsole, settingsManager, instrument);
		Object[] result2 = makeStopLimitOrder.isAccept();

		if (relationAccount!=null && (Boolean) result2[0])
		{
			if (makeStopLimitOrder.get_MakeOrderAccount(relationAccount.get_Id())==null)
			{
				result2[0] = false;
				result2[1] = Language.MakeOrderNotAllowLiqudate;
			}
		}
		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeStopLimitOrder;
		return result;

	}

	public static Object[] isAllowFAKMarketOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Account relationAccount)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,MakeMarketOnCloseOrder

		MakeFAKMarketOrder makeFAKMarketOrder = new MakeFAKMarketOrder(tradingConsole, settingsManager, instrument);
		Object[] result2 = makeFAKMarketOrder.isAccept();

		if (relationAccount!=null && (Boolean) result2[0])
		{
			if (makeFAKMarketOrder.get_MakeOrderAccount(relationAccount.get_Id())==null)
			{
				result2[0] = false;
				result2[1] = Language.MakeOrderNotAllowLiqudate;
			}
		}
		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeFAKMarketOrder;
		return result;
	}

	public static Object[] isAllowMakeAssignOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,MakeAssignOrder

		MakeAssignOrder makeAssignOrder = new MakeAssignOrder(tradingConsole, settingsManager, instrument);
		Object[] result2 = makeAssignOrder.isAccept();

		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeAssignOrder;
		return result;
	}

	//for single order to MakeLiquidationOrder
	public static Object[] isAllowMakeLiquidationOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Order order)
	{
		Object[] result = new Object[]{false, "", null}; //isAllow,message,MakeLiquidationOrder
		MakeLiquidationOrder makeLiquidationOrder = new MakeLiquidationOrder(tradingConsole, settingsManager, order.get_Transaction().get_Instrument());
		Object[] result2 = makeLiquidationOrder.isAccept(order);

		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeLiquidationOrder;
		return result;
	}

	//for multi_orders to MakeLiquidationOrder
	public static Object[] isAllowMakeLiquidationOrder(TradingConsole tradingConsole,SettingsManager settingsManager)
	{
		Object[] result = new Object[]{false, "", null}; //isAllow,message,MakeLiquidationOrder

		for (Iterator<Order> iterator = tradingConsole.get_OpenOrders().values().iterator();iterator.hasNext();)
		{
			Order order = iterator.next();
			if (order.get_Close())
			{
				MakeLiquidationOrder makeLiquidationOrder = new MakeLiquidationOrder(tradingConsole, settingsManager, order.get_Transaction().get_Instrument());
				result[0] = true;
				result[2] = makeLiquidationOrder;
				break;
			}
		}
		return result;
	}

/*
	public static Object[] isAllowMakeLiquidationOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		Object[] result = new Object[]
			{false, "", null}; //isAllow,message,MakeLiquidationOrder
		MakeLiquidationOrder makeLiquidationOrder = new MakeLiquidationOrder(tradingConsole, settingsManager, instrument);
		Object[] result2 = makeLiquidationOrder.isAccept(order);

		result[0] = result2[0];
		result[1] = result2[1];
		result[2] = makeLiquidationOrder;
		return result;
	}
*/

   //include Limit,Stop,MarketOnOpen,Market,MarketOnClose
   public static Object[] isAllowMakeBroadLimitOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument,Account relationAccount)
   {
	   Object[] result = new Object[]{false, "", null}; //isAllow,message
	   if(MakeOrder.isOverRangeOperateOrderUI(settingsManager))
	   {
		   result[1] = Language.OperateDQOrderPrompt + "/" + Language.InstrumentSelectOverRange + " " + Parameter.operateOrderCount;
		   return result;
	   }

		for (int i = 0; i < 5; i++)
		{
			Object[] result2 = new Object[]{false, "", null}; //isAllow,message,object
			switch (i)
			{
				case 0:
					result2 = MakeOrder.isAllowMakeLimitOrder(tradingConsole, settingsManager, instrument,relationAccount);
					break;
				case 1:
					result2 = MakeOrder.isAllowMakeStopOrder(tradingConsole, settingsManager, instrument,relationAccount);
					break;
				case 2:
					result2 = MakeOrder.isAllowMakeMarketOnOpenOrder(tradingConsole, settingsManager, instrument,relationAccount);
					break;
				case 3:
					result2 = MakeOrder.isAllowMakeMarketOrder(tradingConsole, settingsManager, instrument,relationAccount);
					break;
				case 4:
					result2 = MakeOrder.isAllowMakeMarketOnCloseOrder(tradingConsole, settingsManager, instrument,relationAccount);
					break;
			}
			result[0] = result2[0];
			if (!StringHelper.isNullOrEmpty(result2[1].toString()))
			{
				result[1] = ( (StringHelper.isNullOrEmpty(result[1].toString())) ? "" : "\n") + result2[1].toString();
			}
			if ( (Boolean) result2[0])
			{
				result[2] = result2[2];
				return result;
			}
		}

		return result;
   }

   //include SpotTrade,Limit,Stop,MarketOnOpen,Market,MarketOnClose
   public static Object[] isAllowMakeOrder2(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		Object[] result = new Object[]{false,""};//isAllow,message
		Object[] result2 = MakeOrder.isAllowMakeSpotTradeOrder(tradingConsole, settingsManager, instrument);
		if ( (Boolean)result2[0])
		{
			result[0] = result2[0];
			return result;
		}
		else
		{
			Object[] result3 = MakeOrder.isAllowMakeBroadLimitOrder(tradingConsole, settingsManager, instrument, null);
			if ( (Boolean)result3[0])
			{
				result[0] = result3[0];
				return result;
			}
			else
			{
				result[0] = false;
				result[1] = result2[1].toString() + "(" + Language.SPTPrompt + ")\n" + result3[1].toString();
				return result;
			}
		}
	}

	//include Limit,Stop,MarketOnOpen,Market,MarketOnClose
	public static Object[] isAllowMakeBroadLimitOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		return MakeOrder.isAllowMakeBroadLimitOrder(tradingConsole, settingsManager, instrument, null);
	}

	public static boolean isQuote(Instrument instrument, BigDecimal quoteLot)
	{
		if(instrument.isFromBursa()) return false;
		BigDecimal quoteMinLot = instrument.get_DQQuoteMinLot();
		boolean result = quoteLot.compareTo(quoteMinLot) > 0;
		/*TradingConsole.traceSource.trace(TraceType.Error, StringHelper.format("quoteLot={0}, instrument.get_DQQuoteMinLot()={1}, result={2}", new Object[]{ quoteLot, quoteMinLot, result}));
		result = quoteLot.doubleValue() > quoteMinLot.doubleValue();
		TradingConsole.traceSource.trace(TraceType.Error, StringHelper.format("quoteLot={0}, instrument.get_DQQuoteMinLot()={1}, result={2}", new Object[]{ quoteLot.doubleValue(), quoteMinLot.doubleValue(), result}));*/
		return result;
	}

	public static boolean getLastQuotationIsUsing(Instrument instrument)
	{
		return instrument.get_LastQuotation().get_IsUsing();
	}

	public static void setLastQuotationIsUsing(Instrument instrument,boolean isUsing)
	{
		instrument.get_LastQuotation().set_IsUsing(isUsing);
	}

	public void finalize() throws Throwable
	{
		MakeOrder.setLastQuotationIsUsing(this._instrument,false);

		super.finalize();
	}

	public static boolean canMatchingOrder(SettingsManager settingsManager, Instrument instrument, Account account)
	{
		if(account != null)
		{
			if(!account.isSelectAccount()) return false;
			TradePolicyDetail tradePolicyDetail = settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), instrument.get_Id());
			return tradePolicyDetail == null ? false : tradePolicyDetail.get_CanPlaceMatchOrder();
		}

		for(Account item : settingsManager.get_Accounts().values())
		{
			if(!item.isSelectAccount()) continue;
			TradePolicyDetail tradePolicyDetail = settingsManager.getTradePolicyDetail(item.get_TradePolicyId(), instrument.get_Id());
			if(tradePolicyDetail != null && tradePolicyDetail.get_CanPlaceMatchOrder()) return true;
		}
		return false;
	}
}
