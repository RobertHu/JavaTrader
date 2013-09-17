package tradingConsole;

import java.math.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;

import com.jidesoft.docking.*;
import com.jidesoft.grid.*;
import framework.*;
import framework.DateTime;
import framework.data.*;
import framework.diagnostics.*;
import framework.lang.Enum;
import framework.threading.*;
import framework.threading.Scheduler.*;
import framework.xml.*;
import tradingConsole.enumDefine.*;
import tradingConsole.enumDefine.bursa.*;
import tradingConsole.framework.*;
import tradingConsole.settings.*;
import tradingConsole.ui.*;
import tradingConsole.ui.borderStyleHelper.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnFixedHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.fontHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.grid.TableColumnChooser;
import tradingConsole.ui.language.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import tradingConsole.common.Combinator;
import tradingConsole.enumDefine.physical.PhysicalTradeSide;
import org.apache.log4j.Logger;
public class Instrument implements Scheduler.ISchedulerCallback
{
	private Logger logger = Logger.getLogger(Instrument.class);
	public static final String tradingPanelKey = "TradingPanelKey";
	public static final String summaryPanelKey = "SummaryPanelKey";

	public static final String refreshColor = "RefreshColor";
	private SchedulerEntry _refreshColorScheduleEntry;

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;

	private Guid _id;
	private String _originCode;
	private String _code;
	private String _description;
	private String _narrative;
	private String _quoteDescription;
	private String _unit;
	private int _denominator;
	private int _numeratorUnit;
	private Short _commissionFormula;
	private Short _marginFormula;
	private Short _tradePLFormula;
	private int _orderTypeMask;
	private boolean _isNormal;
	private BigDecimal _maxDQLot;
	private BigDecimal _maxOtherLot;
	private Guid _currencyId;
	private int _priceValidTime;
	private BigDecimal _dqQuoteMinLot;
	private boolean _isSinglePrice;
	private int _acceptLmtVariation;
	private int _acceptCloseLmtVariation;
	private int _cancelLmtVariation;
	private int _acceptIfDoneVariation;
	private boolean _isActive;
	private int _sequence;
	private short _priceType;
	private int _lastAcceptTimeSpan;
	private DateTime _beginTime;
	private DateTime _endTime;
	private int _acceptDQVariation;
	//private boolean _isHasDocument;
	private DateTime _dayOpenTime;
	private DateTime _dayCloseTime;
	private boolean _canPlacePendingOrderAtAnyTime;
	private AllowedOrderSides _allowAddNewPosition = AllowedOrderSides.AllowAll;
	private int _deliveryTimeBeginDay = 0;
	private int _deliveryTimeEndDay = 0;

	//Added by Michael on 2008-04-23
	private DateTime _lastDayCloseTime;

	private DateTime _nextDayOpenTime;
	private DateTime _mocTime;
	private DateTime _lastTradeDay;
	private InstrumentCategory _category;
	private Guid _deliveryPointGroupId;

	private Quotation _quotation; //temp store price
	private boolean _select; //isDisplay order

	private DateTime _openTime;
	private DateTime _closeTime;

	private HashMap<CompositeKey2<Guid, DateTime>, TradingTime> _tradingTimes;
	private HashMap<Guid, Transaction> _transactions;
	private Currency _currency;
	private LastQuotation _lastQuotation; //operate order use

	private Quotation _last1Quotation;
	private Quotation _last2Quotation;
	private Quotation _last3Quotation;

	//For Summary
	private BigDecimal _sellLots;
	private double _totalSellPrice;

	private BigDecimal _buyLots;
	private double _totalBuyPrice;

	private String _externalExchangeCode;
	private BestLimits _bestLimits;
	private BestPendings _bestPendings;
	private TimeAndSales _timeAndSales;
	private Guid _groupId;
	private short _physicalLotDecimal;

	public static Instrument SubtotalInstrument = new Instrument(InstrumentCategory.Margin);
	private static ArrayList<Instrument> _instrumentsToSubtotal = new ArrayList<Instrument>();

	static
	{
		com.jidesoft.utils.Lm.verifyLicense("Omnicare System Limited", "iTrader", "TEzuZ3nWadgaTf8Lf6BvmJSbwyBlhFD2");
	}

	public InstrumentCategory get_Category()
	{
		//Test
		//return InstrumentCategory.Order;
		return this._category;
	}

	public String get_SellString()
	{
		if(this == Instrument.SubtotalInstrument) return "";
		return (this._sellLots.compareTo(BigDecimal.ZERO) != 0) ? AppToolkit.getFormatLot(this._sellLots, true) : "-";
	}

	public String get_BuyString()
	{
		if(this == Instrument.SubtotalInstrument) return "";
		return (this._buyLots.compareTo(BigDecimal.ZERO) != 0) ? AppToolkit.getFormatLot(this._buyLots, true) : "-";
	}

	public boolean get_CanPlacePendingOrderAtAnyTime()
	{
		return this._canPlacePendingOrderAtAnyTime;
	}

	public String get_AvgBuyPrice()
	{
		if(this == Instrument.SubtotalInstrument) return "";
		if(this._totalBuyPrice <= 0 || this._buyLots.compareTo(BigDecimal.ZERO) == 0)
		{
			return "-";
		}
		else
		{
			double avgPrice = this._totalBuyPrice / this._buyLots.doubleValue();
			avgPrice = ((double)Math.round(avgPrice * this._denominator)) / this._denominator;
			return Price.toString(Price.create(avgPrice, this.get_NumeratorUnit(), this.get_Denominator()));
		}
	}

	public String get_AvgSellPrice()
	{
		if(this == Instrument.SubtotalInstrument) return "";
		if(this._totalSellPrice <= 0 || this._sellLots.compareTo(BigDecimal.ZERO) == 0)
		{
			return "-";
		}
		else
		{
			//BigDecimal avgPrice = this._totalSellPrice.divide(this._sellLots, this.get_Decimal(), BigDecimal.ROUND_HALF_UP);
			double avgPrice = this._totalSellPrice / this._sellLots.doubleValue();
			avgPrice = ((double)Math.round(avgPrice * this._denominator)) / this._denominator;
			return Price.toString(Price.create(avgPrice, this.get_NumeratorUnit(), this.get_Denominator()));
		}
	}

	public String get_NetString()
	{
		if(this == Instrument.SubtotalInstrument) return "";

		BigDecimal net = this._buyLots.subtract(this._sellLots);
		int compare = net.compareTo(BigDecimal.ZERO);
		if (compare == 0)
		{
			return "-";
		}
		else if (compare < 0)
		{
			return "-" + AppToolkit.getFormatLot(net.abs(), true);
		}
		else
		{
			return AppToolkit.getFormatLot(net, true);
		}
	}

	private HashMap<String, Double> _plFloats = new HashMap<String, Double>();
	private HashMap<String, Currency> _plFloatCurrencies = new HashMap<String, Currency>();
	public Object get_DynamicValue(String key)
	{
		if(this._plFloats.containsKey(key))
		{
			Double value = this._plFloats.get(key);
			Currency currency = this._plFloatCurrencies.get(key);
			return AppToolkit.format(value, currency.get_Decimals());
		}
		else
		{
			return "";
		}
	}

	public void set_DynamicValue(String key, Object value)
	{
		this._plFloats.put(key, (Double)value);
	}

	private void addPLFloat(Currency currency, double value)
	{
		String currencyCode = currency.get_Code();
		if(!this._plFloatCurrencies.containsKey(currencyCode)) this._plFloatCurrencies.put(currencyCode, currency);
		Double oldValue = 0d;
		if(this._plFloats.containsKey(currencyCode))
		{
			oldValue = this._plFloats.get(currencyCode);
		}
		this._plFloats.put(currencyCode, oldValue.doubleValue() + value);
		/*if(this != Instrument.SubtotalInstrument)
		{
			Instrument.SubtotalInstrument.addPLFloat(currency, value);
		}*/
	}

	public SettingsManager get_SettingsManager()
	{
		return this._settingsManager;
	}

	public TradingTime getTradingTime(CompositeKey2<Guid, DateTime> compositeKey2)
	{
		return (this._tradingTimes.containsKey(compositeKey2)) ? this._tradingTimes.get(compositeKey2) : null;
	}

	public void setTradingTime(CompositeKey2<Guid, DateTime> compositeKey2, TradingTime tradingTime)
	{
		this._tradingTimes.put(compositeKey2, tradingTime);
	}

	public void setTradingTime(TradingTime tradingTime)
	{
		CompositeKey2<Guid, DateTime> compositeKey2 = new CompositeKey2<Guid, DateTime> (this._id, tradingTime.get_BeginTime());
		this._tradingTimes.put(compositeKey2, tradingTime);
	}

	//the method getNextMocTime, getNextTradingTime, getNextTradeDay are used to caculate trading time of future
	//these methods may cause problem, such as the order can't be executed
	public DateTime getNextMocTime()
	{
		if(this._mocTime == null)
		{
			return null;
		}
		else
		{
			return this.caculateTimeOfNextTradeDay(this._mocTime);
		}
	}

	private DateTime caculateTimeOfNextTradeDay(DateTime timeOfThisTradeDay)
	{
		TimeSpan span = this._nextDayOpenTime.substract(this._dayOpenTime);
		return timeOfThisTradeDay.add(span);
	}

	public DateTime[] getNextTradingTime()
	{
		DateTime appTime = TradingConsoleServer.appTime();
		if (this._openTime != null && appTime.before(this._openTime))
		{
			return new DateTime[] {this._openTime, this._closeTime};
		}

		TradingTime nextTradingTime = null;
		TradingTime firstTradingTime = null;
		for(TradingTime tradingTime : this._tradingTimes.values())
		{
			if(!tradingTime.get_BeginTime().before(this._closeTime))
			{
				if(nextTradingTime == null || nextTradingTime.get_BeginTime().after(tradingTime.get_BeginTime()))
				{
					nextTradingTime = tradingTime;
				}
			}

			if(firstTradingTime == null || tradingTime.get_BeginTime().before(firstTradingTime.get_BeginTime()))
			{
				firstTradingTime = tradingTime;
			}
		}

		if(nextTradingTime == null)
		{
			return new DateTime[] {this.caculateTimeOfNextTradeDay(firstTradingTime.get_BeginTime()),
				this.caculateTimeOfNextTradeDay(firstTradingTime.get_EndTime())};
		}
		else
		{
			return new DateTime[] {nextTradingTime.get_BeginTime(), nextTradingTime.get_EndTime()};
		}
	}

	public DateTime[] getNextTradeDay()
	{
		DateTime appTime = TradingConsoleServer.appTime();
		if (this.get_DayOpenTime() != null && appTime.before(this.get_DayOpenTime()))
		{
			return new DateTime[] {this.get_DayOpenTime(), this.get_DayCloseTime()};
		}
		else
		{
			return new DateTime[] {this.caculateTimeOfNextTradeDay(this.get_DayOpenTime()),
				this.caculateTimeOfNextTradeDay(this.get_DayCloseTime())};
		}
	}

	public boolean get_IsSinglePrice()
	{
		return this._isSinglePrice;
	}

	private Object[] caculateLotSummaryForGetAcceptLmtVariation(Account account, boolean isBuy, Order amendedOrder,
		boolean hasAnotherPlacingTran, BigDecimal anotherPlacingOrderLot, HashMap<Guid, RelationOrder> anotherPlacingOrderRelation)
	{
		BigDecimal buyLot = BigDecimal.ZERO, sellLot = BigDecimal.ZERO, ocoLot = BigDecimal.ZERO;
		ArrayList<String> ocoCloseTrans = null;
		HashMap<Order, ArrayList<BigDecimal>> closeOrders = null;

		if(hasAnotherPlacingTran)
		{
			if(anotherPlacingOrderRelation != null && anotherPlacingOrderRelation.size() > 0)
			{
				for (Iterator<RelationOrder> relationOrderIterator = anotherPlacingOrderRelation.values().iterator(); relationOrderIterator.hasNext(); )
				{
					RelationOrder relationOrder = relationOrderIterator.next();
					ArrayList<BigDecimal> closeLots = null;
					if(closeOrders == null) closeOrders = new HashMap<Order, ArrayList<BigDecimal>>();
					if (!closeOrders.containsKey(relationOrder.get_OpenOrder()))
					{
						closeLots = new ArrayList<BigDecimal>();
						closeOrders.put(relationOrder.get_OpenOrder(), closeLots);
					}
					else
					{
						closeLots = closeOrders.get(relationOrder.get_OpenOrder());
					}
					closeLots.add(relationOrder.get_CloseLot());
				}
			}
			/*else
			{
				if(isBuy)
					buyLot = buyLot.add(anotherPlacingOrderLot);
				else
					sellLot = sellLot.add(anotherPlacingOrderLot);
			}*/
		}

		for (Iterator<Transaction> iterator = this._transactions.values().iterator(); iterator.hasNext(); )
		{
			Transaction transaction = iterator.next();
			if(amendedOrder != null && transaction.get_Id().equals(amendedOrder.get_Transaction().get_Id())) continue;

			if (transaction.get_Account().get_Id().compareTo(account.get_Id()) == 0)
			{
				HashMap<Guid, Order> orders = transaction.get_Orders();
				for (Iterator<Order> iterator2 = orders.values().iterator(); iterator2.hasNext(); )
				{
					Order order = iterator2.next();
					BigDecimal lot = order.get_IsOpen() ? order.get_LotBalance() : (order.get_Phase() == Phase.Placed ? order.get_Lot() : BigDecimal.ZERO);//lotBalance of close order is ZERO
					if (order.get_Phase() == Phase.Executed)
					{
						if(order.get_IsBuy())
							buyLot = buyLot.add(lot);
						else
							sellLot = sellLot.add(lot);
					}
					else if(order.get_Phase() == Phase.Placed && order.get_IsBuy() == isBuy)
					{
						if(!order.get_IsOpen())
						{
							for (Iterator<RelationOrder> iterator3 = order.get_RelationOrders().values().iterator(); iterator3.hasNext(); )
							{
								RelationOrder relationOrder = iterator3.next();

								if (transaction.get_Type() == TransactionType.OneCancelOther)
								{
									String key = StringHelper.format("{0}-{1}", new Guid[]{transaction.get_Id(), relationOrder.get_OpenOrderId()});
									if (ocoCloseTrans != null && ocoCloseTrans.contains(key))
									{
										continue;
									}
									else
									{
										if (ocoCloseTrans == null) ocoCloseTrans = new ArrayList<String>();
										ocoCloseTrans.add(key);
									}
								}

								ArrayList<BigDecimal> closeLots = null;
								if (closeOrders == null) closeOrders = new HashMap<Order, ArrayList<BigDecimal>> ();
								if (!closeOrders.containsKey(relationOrder.get_OpenOrder()))
								{
									closeLots = new ArrayList<BigDecimal> ();
									closeOrders.put(relationOrder.get_OpenOrder(), closeLots);
								}
								else
								{
									closeLots = closeOrders.get(relationOrder.get_OpenOrder());
								}
								closeLots.add(relationOrder.get_CloseLot());
							}
						}
						else
						{
							if (transaction.get_Type() == TransactionType.OneCancelOther)
							{
								ocoLot = ocoLot.add(lot);
							}
							else
							{
								if (isBuy)
									buyLot = buyLot.add(lot);
								else
									sellLot = sellLot.add(lot);
							}
						}
					}
				}
			}
		}

		if(ocoLot.compareTo(BigDecimal.ZERO) > 0)
		{
			ocoLot = ocoLot.divide(new BigDecimal(2));
			if (isBuy)
				buyLot = buyLot.add(ocoLot);
			else
				sellLot = sellLot.add(ocoLot);
		}

		HashMap<Guid, BigDecimal> remainCloseLot = null;
		if(closeOrders != null)
		{
			remainCloseLot = new HashMap<Guid, BigDecimal>();

			BigDecimal totalCloseLot = BigDecimal.ZERO;
			for (Iterator<Order> iterator4 = closeOrders.keySet().iterator(); iterator4.hasNext(); )
			{
				Order openOrder = iterator4.next();
				ArrayList<BigDecimal> closeLots = closeOrders.get(openOrder);
				BigDecimal closeLot = GetMaxValidCloseLot(closeLots, openOrder.get_LotBalance());
				totalCloseLot = totalCloseLot.add(closeLot);
				remainCloseLot.put(openOrder.get_Id(), openOrder.get_LotBalance().subtract(closeLot));
			}
			if (isBuy)
				buyLot = buyLot.add(totalCloseLot);
			else
				sellLot = sellLot.add(totalCloseLot);
		}

		return new Object[]{buyLot, sellLot, remainCloseLot};
	}

	private BigDecimal GetMaxValidCloseLot(ArrayList<BigDecimal> closeLots, BigDecimal lotBalance)
	{
		if(closeLots == null || closeLots.size() == 0) return BigDecimal.ZERO;
		if(closeLots.size() == 1)
		{
			return closeLots.get(0).min(lotBalance);
		}

		if(closeLots.size() == 2)
		{
			BigDecimal totalCloseLot = closeLots.get(0).add(closeLots.get(1));
			if(totalCloseLot.compareTo(lotBalance) <= 0)
			{
				return totalCloseLot;
			}
			else
			{
				BigDecimal maxCloseLot = closeLots.get(0).max(closeLots.get(1));
				return maxCloseLot.min(lotBalance);
			}
		}
		else
		{
			for(BigDecimal item : closeLots)
			{
				if(item.compareTo(lotBalance) >= 0) return lotBalance;
			}

			BigDecimal[] closeLotArray = new BigDecimal[closeLots.size()];
			closeLotArray = closeLots.toArray(closeLotArray);
			BigDecimal sumCloseLot = this.sum(closeLotArray);
			if(sumCloseLot.compareTo(lotBalance) < 0) return sumCloseLot;

			BigDecimal maxValidCloseLot = BigDecimal.ZERO;
			for (int combinationLen= 2; combinationLen <= closeLots.size() - 1; combinationLen++)
			{
				for (Iterator<BigDecimal[]> iterator = new Combinator<BigDecimal>(closeLotArray, combinationLen).iterator(); iterator.hasNext(); )
				{
					BigDecimal[] combination = iterator.next();
					BigDecimal subtotalCloseLot = this.sum(combination);
					if(subtotalCloseLot.compareTo(lotBalance) <= 0)
					{
						maxValidCloseLot = maxValidCloseLot.max(subtotalCloseLot);
						if(maxValidCloseLot.compareTo(lotBalance) == 0) return lotBalance;
					}
				}
			}

			return maxValidCloseLot;
		}
	}

	private BigDecimal sum(BigDecimal[] values)
	{
		BigDecimal sum = BigDecimal.ZERO;
		for(BigDecimal item : values)
		{
			sum = sum.add(item);
		}
		return sum;
	}

	public int get_AcceptLmtVariation(Account account, boolean isBuy, BigDecimal placeLot, Order amendedOrder,
									  HashMap<Guid, RelationOrder> placeRelations, boolean hasAnotherPlacingTran)
	{
		boolean isOpen = false;
		if(account == null || placeLot.compareTo(BigDecimal.ZERO) <= 0)
		{
			isOpen = true;
		}
		else
		{
			Object[] lotSummary = this.caculateLotSummaryForGetAcceptLmtVariation(account, isBuy, amendedOrder, hasAnotherPlacingTran, placeLot, placeRelations);
			BigDecimal buyLot = (BigDecimal)lotSummary[0];
			BigDecimal sellLot = (BigDecimal)lotSummary[1];
			HashMap<Guid, BigDecimal> remainCloseLot = (HashMap<Guid, BigDecimal>)lotSummary[2];

			BigDecimal avaiableCloseLot = BigDecimal.ZERO;
			boolean isPlaceOpenOrder = remainCloseLot == null || remainCloseLot.size() == 0;
			if(placeRelations != null && remainCloseLot != null)
			{
				for(Guid openOrderId : placeRelations.keySet())
				{
					BigDecimal closeLot = placeRelations.get(openOrderId).get_CloseLot();
					if(remainCloseLot.containsKey(openOrderId))
					{
						closeLot = closeLot.min(remainCloseLot.get(openOrderId));
					}
					avaiableCloseLot = avaiableCloseLot.add(closeLot);
				}
				placeLot = placeLot.min(avaiableCloseLot);
			}

			BigDecimal netLot = buyLot.subtract(sellLot);
			int netLotDirection = netLot.compareTo(BigDecimal.ZERO);
			if (netLotDirection == 0)
			{
				isOpen = isPlaceOpenOrder ? true : placeLot.compareTo(BigDecimal.ZERO) > 0;
			}
			else if (netLotDirection > 0)
			{
				isOpen = isBuy ? true : placeLot.compareTo(netLot) > 0;
			}
			else
			{
				isOpen = !isBuy ? true : placeLot.compareTo(netLot.abs()) > 0;
			}
		}

		DealingPolicyDetail dealingPolicyDetail = this.getDealingPolicyDetail();
		if(isOpen)
		{
			return dealingPolicyDetail == null ? this._acceptLmtVariation : dealingPolicyDetail.get_AcceptLmtVariation();
		}
		else
		{
			return dealingPolicyDetail == null ? this._acceptCloseLmtVariation : dealingPolicyDetail.get_AcceptCloseLmtVariation();
		}
	}

	private int get_AcceptDQVariation()
	{
		DealingPolicyDetail dealingPolicyDetail = this.getDealingPolicyDetail();
		return dealingPolicyDetail == null ? this._acceptDQVariation :  dealingPolicyDetail.get_AcceptDQVariation();
	}

	public int get_DeliveryTimeBeginDay()
	{
		return this._deliveryTimeBeginDay;
	}

	public int get_DeliveryTimeEndDay()
	{
		return this._deliveryTimeEndDay;
	}

	public int get_CancelLmtVariation()
	{
		DealingPolicyDetail dealingPolicyDetail = this.getDealingPolicyDetail();
		return dealingPolicyDetail == null ? this._cancelLmtVariation :  dealingPolicyDetail.get_CancelLmtVariation();
	}

	public int get_AcceptIfDoneVariation()
	{
		return this._acceptIfDoneVariation;
	}

	public DateTime get_DayOpenTime()
	{
		return this._dayOpenTime;
	}

	public DateTime get_DayCloseTime()
	{
		return this._dayCloseTime;
	}

	public DateTime get_LastDayCloseTime()
	{
		return this._lastDayCloseTime;
	}

	public DateTime get_TradingBeginTime()
	{
		if(this._tradingTimes.size() == 0) return null;

		DateTime tradingBeginTime = DateTime.maxValue;
		for (Iterator<TradingTime> iterator = this._tradingTimes.values().iterator(); iterator.hasNext(); )
		{
			TradingTime tradingTime = iterator.next();
			if(tradingTime.get_BeginTime().before(tradingBeginTime))
			{
				tradingBeginTime = tradingTime.get_BeginTime();
			}
		}
		return tradingBeginTime;
	}

	public DateTime get_TradingEndTime()
	{
		if(this._tradingTimes.size() == 0) return null;

		DateTime tradingEndTime = DateTime.minValue;
		for (Iterator<TradingTime> iterator = this._tradingTimes.values().iterator(); iterator.hasNext(); )
		{
			TradingTime tradingTime = iterator.next();
			if(tradingTime.get_EndTime().after(tradingEndTime))
			{
				tradingEndTime = tradingTime.get_EndTime();
			}
		}
		return tradingEndTime;
	}

	public DateTime get_MOCTime()
	{
		return this._mocTime;
	}

	public BigDecimal get_MaxDQLot()
	{
		DealingPolicyDetail dealingPolicyDetail = this.getDealingPolicyDetail();
		return dealingPolicyDetail == null ? this._maxDQLot :  dealingPolicyDetail.get_MaxDQLot();
	}

	public short get_PhysicalLotDecimal()
	{
		return this._physicalLotDecimal;
	}

	public BigDecimal get_MaxOtherLot()
	{
		DealingPolicyDetail dealingPolicyDetail = this.getDealingPolicyDetail();
		return dealingPolicyDetail == null ? this._maxOtherLot :  dealingPolicyDetail.get_MaxOtherLot();
	}

	public int get_PriceValidTime()
	{
		return this._priceValidTime;
	}

	public BigDecimal get_DQQuoteMinLot()
	{
		DealingPolicyDetail dealingPolicyDetail = this.getDealingPolicyDetail();
		return dealingPolicyDetail == null ? this._dqQuoteMinLot :  dealingPolicyDetail.get_DQQuoteMinLot();
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public Guid get_GroupId()
	{
		return this._groupId;
	}

	public boolean get_IsActive()
	{
		return this._isActive;
	}

	public boolean getAllowAddNewPosition(boolean isBuy)
	{
		DealingPolicyDetail dealingPolicyDetail = this.getDealingPolicyDetail();
		if(dealingPolicyDetail != null)
		{
			return dealingPolicyDetail.getAllowAddNewPosition(isBuy);
		}
		else
		{
			if(this._allowAddNewPosition.value() == AllowedOrderSides.AllowNoe.value())
			{
				return false;
			}
			else
			{
				return true;//Let Transaction server to check allow side
			}
		}
	}

	public DateTime get_OpenTime()
	{
		return this._openTime;
	}

	public DateTime get_CloseTime()
	{
		return this._closeTime;
	}

	public DateTime get_NextDayOpenTime()
	{
		return this._nextDayOpenTime;
	}

	public DateTime get_LastTradeDay()
	{
		return this._lastTradeDay;
	}

	public int get_OrderTypeMask()
	{
		return this._orderTypeMask;
	}

	public int get_LastAcceptTimeSpan()
	{
		return this._lastAcceptTimeSpan;
	}

	public String get_OriginCode()
	{
		return this._originCode;
	}

	public boolean get_Select()
	{
		return this._select;
	}

	public void set_Select(boolean value)
	{
		this._select = value;
		this._tradingConsole.get_MainForm().get_OpenOrderTable().filter();
		this._tradingConsole.get_MainForm().get_OrderTable().filter();
		this._tradingConsole.get_MainForm().get_NotConfirmedPendingOrderTable().filter();
		this._tradingConsole.get_MainForm().get_PhysicalInventoryTable().filter();
		this._tradingConsole.get_MainForm().get_PhysicalPendingInventoryTable().filter();
		this._tradingConsole.get_MainForm().get_PhysicalShotSellTable().filter();

		//this._settingsManager.calculateSummary();
	}

	public int get_Sequence()
	{
		return this._sequence;
	}

	public void set_Sequence(int value)
	{
		this._sequence = value;
	}

	public String get_Code()
	{
		return this._code;
	}

	public String get_Description()
	{
		return this._description;
	}

	public String get_DescriptionForTrading()
	{
		if(!StringHelper.isNullOrEmpty(this._narrative))
		{
			return this._description + "(" + this._narrative + ")";
		}
		else
		{
			return this._description;
		}
	}

	public String get_Narrative()
	{
		return this._narrative;
	}

	public String get_QuoteDescription()
	{
		return this._quoteDescription;
	}

	public String get_Unit()
	{
		return this._unit;
	}

	public Guid get_DeliveryPointGroupId()
	{
		return this._deliveryPointGroupId;
	}

	public String get_Bid()
	{
		return Price.toString(this._lastQuotation.get_Bid());
	}

	public HashMap<Guid,Double> get_Margins()
	{
		return this._margins;
	}

	public String get_Ask()
	{
		return Price.toString(this._lastQuotation.get_Ask());
	}

	public String get_Last1Bid()
	{
		return Price.toString(this._last1Quotation.get_Bid());
	}

	public String get_Last1Ask()
	{
		return Price.toString(this._last1Quotation.get_Ask());
	}

	public String get_Last2Bid()
	{
		return Price.toString(this._last2Quotation.get_Bid());
	}

	public String get_Last2Ask()
	{
		return Price.toString(this._last2Quotation.get_Ask());
	}

	public String get_Last3Bid()
	{
		return Price.toString(this._last3Quotation.get_Bid());
	}

	public String get_Last3Ask()
	{
		return Price.toString(this._last3Quotation.get_Ask());
	}

	//Modified by Michael on 2008-04-23
	public String get_Timestamp()
	{
		DateTime timestamp = this._lastQuotation.get_Timestamp();
		if (this._lastQuotation.get_Timestamp() == null || timestamp.equals(AppToolkit.get_MinDatabaseDateTime()))
		{
			return "";
		}
		return Convert.toString(this._lastQuotation.get_Timestamp(), "HH:mm:ss");
	}

	public String get_Last()
	{
		return this._lastQuotation.getLast();
	}

	public String get_High()
	{
		return Price.toString(this._quotation.get_High());
		/*QuotePolicyDetail quotePolicyDetail = this._settingsManager.getQuotePolicyDetail(this._id);
		if(quotePolicyDetail == null
		   || quotePolicyDetail.get_IsOriginHiLo()
		   || !this._settingsManager.get_SystemParameter().get_HighBid())
		{
			return Price.toString(this._lastQuotation.get_High());
		}
		else
		{
			if(this._lastQuotation.get_High() == null)
			{
				return "";
			}
			else
			{
				return Price.toString(Price.subStract(this._lastQuotation.get_High(), quotePolicyDetail.get_HiLoSpread()));
			}
		}*/
	}

	public String get_Low()
	{
		return Price.toString(this._quotation.get_Low());
		/*QuotePolicyDetail quotePolicyDetail = this._settingsManager.getQuotePolicyDetail(this._id);
		if(quotePolicyDetail == null
		   || quotePolicyDetail.get_IsOriginHiLo()
		   || this._settingsManager.get_SystemParameter().get_LowBid())
		{
			return Price.toString(this._lastQuotation.get_Low());
		}
		else
		{
			if(this._lastQuotation.get_Low() == null)
			{
				return "";
			}
			else
			{
				return Price.toString(Price.add(this._lastQuotation.get_Low(), quotePolicyDetail.get_HiLoSpread()));
			}
		}*/
	}

	public String get_Open()
	{
		return Price.toString(this._lastQuotation.get_Open());
	}

	public String get_PrevClose()
	{
		return Price.toString(this._lastQuotation.get_PrevClose());
	}

	public String get_Change()
	{
		double change = this._lastQuotation.get_Change();
		Price preClose = this._lastQuotation.get_PrevClose();

		if(change == 0 || preClose == null || Price.toDouble(preClose) == 0)
		{
			return "-";
		}
		else
		{
			if(this._settingsManager.get_SystemParameter().get_CaculateChangeWithDenominator())
			{
				if ( (double) ( (int)change) == change)
				{
					return Convert.toString( (int)change);
				}
				else
				{
					return AppToolkit.format(change, Convert.toString(this._denominator).length() - 1);
				}
			}
			else
			{
				double changeInPercent = change * 100 / Price.toDouble(preClose);
				return (changeInPercent > 0 ? "+" : "") + AppToolkit.format(changeInPercent, 2) + "%";
			}
		}
	}

	public String get_InterestRateBuy()
	{
		Account currentAccount = this._tradingConsole.getCurrentAccount();
		if (currentAccount == null)
		{
			return "";
		}
		Guid tradePolicyId = currentAccount.get_TradePolicyId();
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(tradePolicyId, this._id);
		if (tradePolicyDetail != null)
		{
			BigDecimal interestRate = tradePolicyDetail.get_InterestRateBuy();
			if (interestRate == null)
			{
				return "";
			}
			return AppToolkit.format(interestRate.doubleValue(), 2);
		}
		return "";
	}

	public String get_InterestRateSell()
	{
		Account currentAccount = this._tradingConsole.getCurrentAccount();
		if (currentAccount == null)
		{
			return "";
		}
		Guid tradePolicyId = currentAccount.get_TradePolicyId();
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(tradePolicyId, this._id);
		if (tradePolicyDetail != null)
		{
			BigDecimal interestRate = tradePolicyDetail.get_InterestRateSell();
			if (interestRate == null)
			{
				return "";
			}
			return AppToolkit.format(interestRate.doubleValue(), 2);
		}
		return "";
	}

	public String get_FilledVolume()
	{
		return this._lastQuotation.get_Volume();
	}

	public String get_TotalFilledVolume()
	{
		return this._lastQuotation.get_TotalVolume();
	}

	public boolean get_IsNormal()
	{
		return this._isNormal;
	}

	public int get_NumeratorUnit()
	{
		return this._numeratorUnit;
	}

	public int get_Denominator()
	{
		return this._denominator;
	}

	public short get_CommissionFormula()
	{
		return this._commissionFormula;
	}

	public short get_MarginFormula()
	{
		return this._marginFormula;
	}

	public short get_TradePLFormula()
	{
		return this._tradePLFormula;
	}

	public void set_PriceType(short value)
	{
		this._priceType = value;
	}

	public static Guid DebugInstrumentId = new Guid("65F4D401-626A-42C4-A99A-9B728CB49346");
	public static Guid DebugInstrumentId2 = new Guid("D14C482D-74C8-4427-A606-B679B64FBF2A");
	//use it will solve not synchronize quotation
	public void refreshLastQuotation()
	{
		//if (this._quotation == null) return;
		boolean merged = false;
		if (!this._lastQuotation.equals(this._quotation))
		{
			if(this._lastQuotation.get_Timestamp() != null && this._quotation.get_Timestamp() != null
			   && this._quotation.get_Timestamp().before(this._lastQuotation.get_Timestamp()))
			{
				String info = StringHelper.format("New quotation({0}) is before the last quotation[{1}]",
												 new Object[]{this._quotation.get_Timestamp(), this._lastQuotation.get_Timestamp()});
				TradingConsole.traceSource.trace(TraceType.Warning, info);
			}
			else
			{
				if(this._lastQuotation.get_Timestamp() == null
				   || this._quotation.get_Timestamp().after(this._lastQuotation.get_Timestamp())
					|| (this._quotation.get_QuotationStatus().value() == QuotationStatus.Down.value()
					|| this._quotation.get_QuotationStatus().value() == QuotationStatus.Up.value()))
				{
					this._lastQuotation.merge(this._quotation);
					merged = true;
				}
			}
		}

		if(this._id.equals(Instrument.DebugInstrumentId) || this._id.equals(Instrument.DebugInstrumentId2))
		{
			String info = StringHelper.format("[RefreshLastQuotation] LastQuotation = {0}; Quotation = {1}",
				new Object[]{this._lastQuotation.toLogString(), this._quotation.toLogString()});
			TradingConsole.traceSource.trace(TraceType.Information, info);
		}

		if(!merged && this._lastQuotation.get_IsQuote()) this._lastQuotation.merge(this._quotation);
		this._lastQuotation.set_IsQuote(this._quotation.get_IsQuote());

		this._quotation.set_IsQuote(false);
		this._quotation.set_IsChangedQuotation(false);
	}

	public Quotation get_LastQuotation()
	{
		return this._lastQuotation;
	}

	public Quotation get_Quotation()
	{
		return this._quotation;
	}

	/*
	  public Quotation get_Last1Quotation()
	  {
	 return this._last1Quotation;
	  }

	  public Quotation get_Last2Quotation()
	  {
	 return this._last2Quotation;
	  }

	  public Quotation get_Last3Quotation()
	  {
	 return this._last3Quotation;
	  }
	 */

	public void set_Quotation(Quotation value)
	{
		this._quotation = value;
	}

	public HashMap<Guid, Transaction> get_Transactions()
	{
		//outer fill them
		return this._transactions;
	}

	public Currency get_Currency()
	{
		if (this._currency == null)
		{
			this._currency = this._settingsManager.getCurrency(this._currencyId);
		}
		return this._currency;
	}

	public BestLimits get_BestLimits()
	{
		return this._bestLimits;
	}

	public BestPendings get_BestPendings()
	{
		return this._bestPendings;
	}

	public TimeAndSales get_TimeAndSales()
	{
		return this._timeAndSales;
	}

	private Instrument(InstrumentCategory category)//for SubtotalInstrument
	{
		this._id = Guid.empty;
		this._select = true;
		this._isActive = false;
		this._category = category;
		this._sequence = Integer.MAX_VALUE;
		this._code = "";
		this._description = "";
		this._narrative = "";
		this._quoteDescription = "";
		this._unit = "";
		this._sellLots = BigDecimal.ZERO;
		this._buyLots = BigDecimal.ZERO;
		this._totalBuyPrice = 0.0;//BigDecimal.ZERO;
		this._totalSellPrice = 0.0;//BigDecimal.ZERO;
		this._plFloats.clear();
		this._plFloatCurrencies.clear();
	}

	public Instrument(TradingConsole tradingConsole, SettingsManager settingsManager, DataRow dataRow)
	{
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;

		this._sellLots = BigDecimal.ZERO;
		this._buyLots = BigDecimal.ZERO;
		this._totalBuyPrice = 0.0;//BigDecimal.ZERO;
		this._totalSellPrice = 0.0;//BigDecimal.ZERO;
		this._plFloats.clear();
		this._plFloatCurrencies.clear();

		this._id = (Guid)dataRow.get_Item("ID");
		this.setValue(dataRow);

		this._select = true;

		this._tradingTimes = new HashMap<CompositeKey2<Guid, DateTime>, TradingTime> ();
		this._transactions = new HashMap<Guid, Transaction> ();
		this._currency = null;
		this._lastQuotation = new LastQuotation(this, this._settingsManager);
		this._quotation = new Quotation(this, this._settingsManager);

		this._last1Quotation = new LastQuotation(this, this._settingsManager);
		this._last2Quotation = new LastQuotation(this, this._settingsManager);
		this._last3Quotation = new LastQuotation(this, this._settingsManager);

		this._bindingSourceForMatchOrder = new tradingConsole.ui.grid.BindingSource();
		this._matchOrders = new HashMap<Guid, MatchOrder> ();

		this._bestLimits = new BestLimits(this._id);
		this._bestPendings = new BestPendings(this._id);
		this._timeAndSales = new TimeAndSales(this._id);

		Instrument._instrumentsToSubtotal.add(this);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
		this.clearBuySellLots();
		if(!Instrument._instrumentsToSubtotal.contains(this))
		{
			Instrument._instrumentsToSubtotal.add(this);
		}
	}

	private void setValue(DataRow dataRow)
	{
		this._originCode = (String)dataRow.get_Item("OriginCode");
		this._code = (String)dataRow.get_Item("Code");
		this._description = (String)dataRow.get_Item("Description");
		if(dataRow.get_Table().get_Columns().contains("Narrative"))
		{
			this._narrative = dataRow.get_Item("Narrative") == DBNull.value ? "" : ((String)dataRow.get_Item("Narrative")).trim();
		}
		if(dataRow.get_Table().get_Columns().contains("QuoteDescription"))
		{
			this._quoteDescription = dataRow.get_Item("QuoteDescription") == DBNull.value ? "" : ((String)dataRow.get_Item("QuoteDescription")).trim();
		}
		this._unit = dataRow.get_Item("Unit") == DBNull.value ? "" : (String)dataRow.get_Item("Unit");
		this._denominator = (Integer)dataRow.get_Item("Denominator");
		this._numeratorUnit = (Integer)dataRow.get_Item("NumeratorUnit");
		this._commissionFormula = (Short)dataRow.get_Item("CommissionFormula");
		this._marginFormula = (Short)dataRow.get_Item("MarginFormula");
		this._tradePLFormula = (Short)dataRow.get_Item("TradePLFormula");
		this._orderTypeMask = (Integer)dataRow.get_Item("OrderTypeMask");
		this._isNormal = (Boolean)dataRow.get_Item("IsNormal");
		this._maxDQLot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MaxDQLot"), 0.0);
		this._maxOtherLot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("MaxOtherLot"), 0.0);
		this._currencyId = (Guid)dataRow.get_Item("CurrencyID");
		this._deliveryPointGroupId = dataRow.get_Item("DeliveryPointGroupId") == DBNull.value ? null : (Guid)dataRow.get_Item("DeliveryPointGroupId");
		this._priceValidTime = (Integer)dataRow.get_Item("PriceValidTime");
		this._dqQuoteMinLot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("DQQuoteMinLot"), 0.0);
		this._isSinglePrice = (Boolean)dataRow.get_Item("IsSinglePrice");
		this._lastAcceptTimeSpan = (Integer)dataRow.get_Item("LastAcceptTimeSpan");
		this._beginTime = (DateTime)dataRow.get_Item("BeginTime");
		this._endTime = (DateTime)dataRow.get_Item("EndTime");
		this._acceptDQVariation = (Integer)dataRow.get_Item("AcceptDQVariation");
		if(dataRow.get_Item("DeliveryTimeBeginDay") != DBNull.value) this._deliveryTimeBeginDay = (Integer)dataRow.get_Item("DeliveryTimeBeginDay");
		if(dataRow.get_Item("DeliveryTimeEndDay") != DBNull.value) this._deliveryTimeEndDay = (Integer)dataRow.get_Item("DeliveryTimeEndDay");
		this._acceptLmtVariation = (Integer)dataRow.get_Item("AcceptLmtVariation");
		this._acceptCloseLmtVariation = (Integer)dataRow.get_Item("AcceptCloseLmtVariation");
		this._cancelLmtVariation = (Integer)dataRow.get_Item("CancelLmtVariation");
		this._acceptIfDoneVariation = (Integer)dataRow.get_Item("AcceptIfDoneVariation");
		this._priceType = (Short)dataRow.get_Item("PriceType");
		if(dataRow.get_Table().get_Columns().contains("PhysicalLotDecimal"))
		{
			this._physicalLotDecimal = (Short)dataRow.get_Item("PhysicalLotDecimal");
		}
		//this._isHasDocument = (Boolean)dataRow.get_Item("IsHasDocument");
		this._sequence = (Integer)dataRow.get_Item("Sequence");
		this._dayOpenTime = (AppToolkit.isDBNull(dataRow.get_Item("DayOpenTime"))) ? null : (DateTime)dataRow.get_Item("DayOpenTime");
		this._dayCloseTime = (AppToolkit.isDBNull(dataRow.get_Item("DayCloseTime"))) ? null : (DateTime)dataRow.get_Item("DayCloseTime");
		this._lastDayCloseTime = (AppToolkit.isDBNull(dataRow.get_Item("LastDayCloseTime"))) ? null : (DateTime)dataRow.get_Item("LastDayCloseTime");
		this._nextDayOpenTime = (AppToolkit.isDBNull(dataRow.get_Item("NextDayOpenTime"))) ? null : (DateTime)dataRow.get_Item("NextDayOpenTime");
		this._mocTime = (AppToolkit.isDBNull(dataRow.get_Item("MOCTime"))) ? null : (DateTime)dataRow.get_Item("MOCTime");
		this._lastTradeDay = (AppToolkit.isDBNull(dataRow.get_Item("LastTradeDay"))) ? null : (DateTime)dataRow.get_Item("LastTradeDay");
		this._isActive = (Boolean)dataRow.get_Item("IsActive");
		this._category = Enum.valueOf(InstrumentCategory.class, (Integer)dataRow.get_Item("Category"));
		this._canPlacePendingOrderAtAnyTime = (Boolean) dataRow.get_Item("CanPlacePendingOrderAtAnyTime");
		if(dataRow.get_Table().get_Columns().contains("ExternalExchangeCode"))
		{
			this._externalExchangeCode = (AppToolkit.isDBNull(dataRow.get_Item("ExternalExchangeCode"))) ? null :(String)dataRow.get_Item("ExternalExchangeCode");
		}
		if(dataRow.get_Table().get_Columns().contains("AllowedNewTradeSides"))
		{
			this._allowAddNewPosition = Enum.valueOf(AllowedOrderSides.class, (Short)dataRow.get_Item("AllowedNewTradeSides"));
		}

		if(dataRow.get_Table().get_Columns().contains("GroupID"))
		{
			this._groupId = (AppToolkit.isDBNull(dataRow.get_Item("GroupID"))) ? null :(Guid)dataRow.get_Item("GroupID");
		}
	}

	public void setValue(XmlAttributeCollection instrumentCollection)
	{
		for (int i = 0; i < instrumentCollection.get_Count(); i++)
		{
			String nodeName = instrumentCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = instrumentCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("ID"))
			{
				this._id = new Guid(nodeValue);
			}
			else if (nodeName.equals("OriginCode"))
			{
				this._originCode = nodeValue;
			}
			else if (nodeName.equals("Code"))
			{
				this._code = nodeValue;
			}
			else if (nodeName.equals("Description"))
			{
				//this._description = nodeValue;
			}
			else if (nodeName.equals("Denominator"))
			{
				this._denominator = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("NumeratorUnit"))
			{
				this._numeratorUnit = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("CommissionFormula"))
			{
				this._commissionFormula = Short.parseShort(nodeValue);
			}
			else if (nodeName.equals("MarginFormula"))
			{
				this._marginFormula = Short.parseShort(nodeValue);
			}
			else if (nodeName.equals("TradePLFormula"))
			{
				this._tradePLFormula = Short.parseShort(nodeValue);
			}
			else if (nodeName.equals("CanPlacePendingOrderAtAnyTime"))
			{
				this._canPlacePendingOrderAtAnyTime = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("OrderTypeMask"))
			{
				this._orderTypeMask = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("IsNormal"))
			{
				this._isNormal = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("MaxDQLot"))
			{
				this._maxDQLot = new BigDecimal(nodeValue); //new BigDecimal(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("MaxOtherLot"))
			{
				this._maxOtherLot = new BigDecimal(nodeValue); //new BigDecimal(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("CurrencyID"))
			{
				this._currencyId = new Guid(nodeValue);
			}
			else if (nodeName.equals("DeliveryPointGroupId"))
			{
				if(StringHelper.isNullOrEmpty(nodeValue))
				{
					this._deliveryPointGroupId = null;
				}
				else
				{
					this._deliveryPointGroupId = new Guid(nodeValue);
				}
			}
			else if (nodeName.equals("PriceValidTime"))
			{
				this._priceValidTime = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("DQQuoteMinLot"))
			{
				this._dqQuoteMinLot = new BigDecimal(nodeValue); //new BigDecimal(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("IsSinglePrice"))
			{
				this._isSinglePrice = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("BeginTime"))
			{
				this._beginTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("EndTime"))
			{
				this._endTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("LastTradeDay"))
			{
				this._lastTradeDay = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("AcceptLmtVariation"))
			{
				this._acceptLmtVariation = Integer.parseInt(nodeValue);
			}
			else if(nodeName.equals("AcceptCloseLmtVariation"))
			{
				this._acceptCloseLmtVariation = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("CancelLmtVariation"))
			{
				this._cancelLmtVariation = Integer.parseInt(nodeValue);
			}
			else if(nodeName.equals("AcceptIfDoneVariation"))
			{
				this._acceptIfDoneVariation = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("PriceType"))
			{
				this._priceType = Short.parseShort(nodeValue);
			}
			else if (nodeName.equals("PhysicalLotDecimal"))
			{
				this._physicalLotDecimal = Short.parseShort(nodeValue);
			}
			else if (nodeName.equals("LastAcceptTimeSpan"))
			{
				this._lastAcceptTimeSpan = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("Sequence"))
			{
				this._sequence = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("DayOpenTime"))
			{
				this._dayOpenTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("DayCloseTime"))
			{
				this._dayCloseTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("LastDayCloseTime"))
			{
				this._lastDayCloseTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("NextDayOpenTime"))
			{
				this._nextDayOpenTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("MOCTime"))
			{
				if (StringHelper.isNullOrEmpty(nodeValue))
				{
					this._mocTime = null;
				}
				else
				{
					this._mocTime = AppToolkit.getDateTime(nodeValue);
				}
			}
			else if (nodeName.equals("IsActive"))
			{
				this._isActive = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("AllowedNewTradeSides"))
			{
				this._allowAddNewPosition = Enum.valueOf(AllowedOrderSides.class, Integer.valueOf(nodeValue).intValue());
			}
			else if (nodeName.equals("AcceptDQVariation"))
			{
				this._acceptDQVariation = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("DeliveryTimeBeginDay"))
			{
				this._deliveryTimeBeginDay = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("DeliveryTimeEndDay"))
			{
				this._deliveryTimeEndDay = Integer.parseInt(nodeValue);
			}
			//else if (nodeName.equals("IsHasDocument"))
			//{
			//	this._isHasDocument = Boolean.valueOf(nodeValue);
			//}
			else if (nodeName.equals("Category"))
			{
				this._category = Enum.valueOf(InstrumentCategory.class, Integer.parseInt(nodeValue));
			}
		}
	}

	public static void rebindSummaryPanel(DockableFrame dockableFrame, tradingConsole.ui.grid.DockableTable grid, String dataSourceKey, Collection dataSource,
										  tradingConsole.ui.grid.BindingSource bindingSource)
	{
		for (int i = 0, count = bindingSource.getRowCount(); i < count; i++)
		{
			Instrument instrument = (Instrument)bindingSource.getObject(i);
			instrument.update(dataSourceKey);
			instrument.changeSummaryPanelStyle(dataSourceKey);
		}
		//grid.tableChanged(new TableModelEvent(bindingSource));
		//bindingSource.fireTableChanged(new TableModelEvent(bindingSource));

		/*Instrument.unbind(dataSourceKey, bindingSource);
		Instrument.initializeSummaryPanel(dockableFrame, grid, dataSourceKey, dataSource, bindingSource);*/
	}

	public static void initializeSummaryPanel(DockableFrame dockableFrame, tradingConsole.ui.grid.DockableTable grid, String dataSourceKey,
											  Collection dataSource, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		grid.reset();
		grid.setVertGridLines(false);
		grid.setHorzGridLines(false);
		grid.setBackColor(GridFixedBackColor.summaryPanel);
		grid.setTableColor(GridBackColor.summaryPanel);
		grid.setBorderStyle(BorderStyle.summaryPanel);
		grid.setRowLabelWidth(RowLabelWidth.summaryPanel);
		//grid.setSelectionBackground(SelectionBackground.summaryPanel);
		grid.setCurrentCellColor(CurrentCellColor.summaryPanel);
		grid.setCurrentCellBorder(CurrentCellBorder.summaryPanel);
		grid.setOpaque(false);

		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, Instrument.getPropertyDescriptorsForSummary(dockableFrame));
		grid.setModel(bindingSource, 0);

		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.summaryPanel, Color.white, HeaderFont.summaryPanel);
		Color foreground = ColorSettings.useBlackAsBackground ? ColorSettings.GridForeground : Color.black;
		Color background = ColorSettings.useBlackAsBackground ? ColorSettings.TradingListGridBackground : Color.white;
		TradingConsole.bindingManager.setGrid(dataSourceKey, 18, foreground, background, Color.blue, true, true,
											  GridFont.summaryPanel, false, true, true);

		Instrument.updateSubtotalSummary();

		for (int i = 0, count = bindingSource.getRowCount(); i < count; i++)
		{
			Instrument instrument = (Instrument)bindingSource.getObject(i);
			instrument.changeSummaryPanelStyle(dataSourceKey);
		}

		int sequenceColumn = bindingSource.getColumnByName(SummaryColKey.Sequence);
		grid.sortColumn(sequenceColumn, true, true);
		TableColumnChooser.hideColumn(grid, sequenceColumn);
		grid.setSortingEnabled(false);
		grid.setAutoResort(true);
	}

	private static Collection<Instrument> sortBySequence(Collection source)
	{
		Instrument[] instruments = new Instrument[source.size()];
		for(Object item : source.toArray())
		{
			Instrument instrument = (Instrument)item;
			instruments[instrument.get_Sequence() - 1] = instrument;
		}

		ArrayList<Instrument> sortedCollection = new ArrayList<Instrument>();
		for(Instrument instrument : instruments)
		{
			sortedCollection.add(instrument);
		}
		return sortedCollection;
	}

	public static void initializeTradingPanel(SettingsManager settingsManager, tradingConsole.ui.grid.DockableTable grid, String dataSourceKey,
											  Collection dataSource, tradingConsole.ui.grid.BindingSource bindingSource, int priceAlign)
	{
		grid.reset();
		grid.setIntercellSpacing(new Dimension(0, 0));
		grid.setVertGridLines(false);
		grid.setHorzGridLines(false);
		grid.setBackColor(GridFixedBackColor.tradingPanel);
		grid.setTableColor(GridBackColor.tradingPanel);
		grid.setBorderStyle(BorderStyle.tradingPanel);
		grid.setRowLabelWidth(RowLabelWidth.tradingPanel);
		grid.setCurrentCellColor(CurrentCellColor.tradingPanel);
		grid.setCurrentCellBorder(CurrentCellBorder.tradingPanel);

		TradingConsole.bindingManager.bind(dataSourceKey, sortBySequence(dataSource), bindingSource, Instrument.getPropertyDescriptors(settingsManager, priceAlign));
		grid.setModel(bindingSource);

		UISetting uiSetting = settingsManager.getUISetting(UISetting.tradingPanelUiSetting);
		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.tradingPanel, Color.white, HeaderFont.tradingPanel);
		TradingConsole.bindingManager.setGrid(dataSourceKey, uiSetting.get_RowHeight(), ColorSettings.useBlackAsBackground ? ColorSettings.GridForeground : Color.black,
											  ColorSettings.useBlackAsBackground ? ColorSettings.TradingListGridBackground : Color.white, Color.blue, true, true,
											  new Font(uiSetting.get_FontName(), Font.BOLD, uiSetting.get_FontSize()), false, true, true);

		int sequenceColumn = bindingSource.getColumnByName(InstrumentColKey.Sequence);
		grid.sortColumn(sequenceColumn, true, true);
		grid.setAutoResort(true);

		ArrayList<String> unchoosableColumns = new ArrayList<String>();
		unchoosableColumns.add(InstrumentColKey.Sequence);
		grid.putClientProperty(TableColumnChooser.UnchoosableColumns, unchoosableColumns);

		for (int i = 0, count = bindingSource.getRowCount(); i < count; i++)
		{
			Instrument instrument = (Instrument)bindingSource.getObject(i);
			instrument.changeLastQuotationStyle(dataSourceKey);
			instrument.setValidateColor();
		}
	}

	private static void setType(String dataSourceKey, int cursor)
	{
		TradingConsole.bindingManager.setType(dataSourceKey, InstrumentColKey.Description, cursor);
		TradingConsole.bindingManager.setType(dataSourceKey, InstrumentColKey.Bid, cursor);
		TradingConsole.bindingManager.setType(dataSourceKey, InstrumentColKey.Ask, cursor);
	}

	public static void unbind(String dataSourceKey, BindingSource bindingSource)
	{
		TradingConsole.bindingManager.unbind(dataSourceKey, bindingSource);
	}

	//SummaryPanel use
	public static void updatePropertiesForSummary(String dataSourceKey, DockableFrame dockableFrame)
	{
		TradingConsole.bindingManager.updateProperties(dataSourceKey, Instrument.getPropertyDescriptorsForSummary(dockableFrame));
	}

	//SummaryPanel use
	public static PropertyDescriptor[] getPropertyDescriptorsForSummary(DockableFrame dockableFrame)
	{
		java.util.ArrayList<String> currencies = InstrumentPLFloatHelper.get_Currencies();

		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[7 + currencies.size()];
		int index = 0;
		int width = (dockableFrame == null) ? 260 : dockableFrame.getWidth() - 3;

		propertyDescriptors[index++] = PropertyDescriptor.create(Instrument.class, SummaryColKey.Description, true, null, SummaryLanguage.Description,
			UISettingsManager.getWidth(width, 0.25, 80), SwingConstants.CENTER, null, null);

		String sequence = SummaryLanguage.Sequence == null || SummaryLanguage.Sequence.trim().length() == 0 ? "#" : SummaryLanguage.Sequence;
		propertyDescriptors[index++] = PropertyDescriptor.create(Instrument.class, SummaryColKey.Sequence, true, null, sequence,
			0, SwingConstants.LEFT, null, null);

		propertyDescriptors[index++] = PropertyDescriptor.create(Instrument.class, SummaryColKey.BuyString, true, null, SummaryLanguage.Buy,
			UISettingsManager.getWidth(width, 0.25, 60), SwingConstants.RIGHT, null, null);

		propertyDescriptors[index++] = PropertyDescriptor.create(Instrument.class, SummaryColKey.SellString, true, null, SummaryLanguage.Sell,
			UISettingsManager.getWidth(width, 0.25, 60), SwingConstants.RIGHT, null, null);

		propertyDescriptors[index++] = PropertyDescriptor.create(Instrument.class, SummaryColKey.AvgBuyPrice, true, null, SummaryLanguage.AvgBuyPrice,
			UISettingsManager.getWidth(width, 0.25, 60), SwingConstants.RIGHT, null, null);

		propertyDescriptors[index++] = PropertyDescriptor.create(Instrument.class, SummaryColKey.AvgSellPrice, true, null, SummaryLanguage.AvgSellPrice,
			UISettingsManager.getWidth(width, 0.25, 60), SwingConstants.RIGHT, null, null);

		propertyDescriptors[index++] = PropertyDescriptor.create(Instrument.class, SummaryColKey.NetString, true, null, SummaryLanguage.Net,
			UISettingsManager.getWidth(width, 0.25, 60), SwingConstants.RIGHT, null, null);

		for(String currency : currencies)
		{
			String caption = Language.OpenContractlblTradePLFloatA + "(" + currency + ")";
			propertyDescriptors[index++] = PropertyDescriptor.create(Instrument.class, currency, true, null, caption,
				150, SwingConstants.RIGHT, null, null, true);
		}

		//return propertyDescriptors;
		return ColumnUIInfoManager.applyUIInfo(GridNames.SummaryGrid, propertyDescriptors);
	}

	//TradingPanel use
	public static PropertyDescriptor[] getPropertyDescriptors(SettingsManager settingsManager, int priceAlign)
	{
		ArrayList<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor> ();

		propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.Select, false, null,
			InstrumentLanguage.Select, 10, SwingConstants.CENTER, null, null, new BooleanCheckBoxCellEditor(), new BooleanCheckBoxCellRenderer()));

		String sequence = InstrumentLanguage.Sequence == null || InstrumentLanguage.Sequence.trim().length() == 0 ? InstrumentColKey.Sequence :
			InstrumentLanguage.Sequence;
		propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.Sequence, true, null, sequence,
			30, SwingConstants.LEFT, null, null));

		propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.Description, true,
			null, InstrumentLanguage.Description, 100, SwingConstants.LEFT, null, null));

		propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.Bid, true,
			null, InstrumentLanguage.Bid, 85, priceAlign, null, null));

		propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.Ask, true, null, InstrumentLanguage.Ask,
			85, priceAlign, null, null));

		propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.Timestamp, true, null, InstrumentLanguage.Timestamp,
			85, priceAlign, null, null));

		propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.Last, true, null, InstrumentLanguage.Last,
			85, priceAlign, null, null));

		if (Parameter.isShouldShowColumn("Instrument", InstrumentColKey.High))
		{
			propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.High, true, null, InstrumentLanguage.High,
				85, priceAlign, null, null));
		}

		if (Parameter.isShouldShowColumn("Instrument", InstrumentColKey.Low))
		{
			propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.Low, true, null, InstrumentLanguage.Low,
				85, priceAlign, null, null));
		}

		if (Parameter.isShouldShowColumn("Instrument", InstrumentColKey.Open))
		{
			propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.Open, true, null, InstrumentLanguage.Open,
				85, priceAlign, null, null));
		}

		if(Parameter.isShouldShowColumn("Instrument", InstrumentColKey.PrevClose))
		{
			propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.PrevClose, true, null, InstrumentLanguage.PrevClose,
				85, priceAlign, null, null));
		}

		if(Parameter.isShouldShowColumn("Instrument", InstrumentColKey.Change))
		{
			propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.Change, true, null, InstrumentLanguage.Change,
				85, priceAlign, null, null));
		}

		if(Parameter.isShouldShowColumn("Instrument", InstrumentColKey.InterestRateBuy))
		{
			propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.InterestRateBuy, true, null,
				InstrumentLanguage.InterestRateBuy,
				85, priceAlign, null, null));
		}

		if(Parameter.isShouldShowColumn("Instrument", InstrumentColKey.InterestRateSell))
		{
			propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.InterestRateSell, true, null,
				InstrumentLanguage.InterestRateSell,
				85, priceAlign, null, null));
		}

		if(Parameter.isShouldShowColumn("Instrument", InstrumentColKey.FilledVolume))
		{
			propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.FilledVolume, true, null,
				InstrumentLanguage.FilledVolume,
				85, priceAlign, null, null));
		}

		if(Parameter.isShouldShowColumn("Instrument", InstrumentColKey.TotalFilledVolume))
		{
			propertyDescriptorList.add(PropertyDescriptor.create(Instrument.class, InstrumentColKey.TotalFilledVolume, true, null,
				InstrumentLanguage.TotalFilledVolume,
				85, priceAlign, null, null));
		}

		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[propertyDescriptorList.size()];
		propertyDescriptors = propertyDescriptorList.toArray(propertyDescriptors);
		return ColumnUIInfoManager.applyUIInfo(GridNames.InstrumentGrid, propertyDescriptors);
	}

	public void setForeground(String dataSourceKey, String propertyName, Color foreground)
	{
		TradingConsole.bindingManager.setForeground(dataSourceKey, this, propertyName, foreground);
	}

	public void setIcon(String dataSourceKey, String propertyName, Icon image)
	{
		TradingConsole.bindingManager.setIcon(dataSourceKey, this, propertyName, image);
		this._tradingConsole.get_BindingSourceForTradingPanel().setIcon(this, propertyName, image);
		this._tradingConsole.get_InstrumentSpanBindingSource().setIcon(this, propertyName, image);
	}

	public void setImage(String dataSourceKey, String propertyName, Image image)
	{
		TradingConsole.bindingManager.setImage(dataSourceKey, this, propertyName, image);
		this._tradingConsole.get_BindingSourceForTradingPanel().setImage(this, propertyName, image);
		this._tradingConsole.get_InstrumentSpanBindingSource().setImage(this, propertyName, image);
	}

	public void setBackground(String dataSourceKey, Color background)
	{
		TradingConsole.bindingManager.setBackground(dataSourceKey, this, background);
		this._tradingConsole.get_InstrumentSpanBindingSource().setBackground(this, background);
	}

	public void resetBackground(String dataSourceKey)
	{
		TradingConsole.bindingManager.resetBackground(dataSourceKey, this);
		this._tradingConsole.get_InstrumentSpanBindingSource().resetBackground(this);
	}

	public void setForeground(String dataSourceKey, Color color)
	{
		TradingConsole.bindingManager.setForeground(dataSourceKey, this, color);
		this._tradingConsole.get_InstrumentSpanBindingSource().setForeground(this, color);
	}

	public void resetForeground(String dataSourceKey)
	{
		TradingConsole.bindingManager.resetForeground(dataSourceKey, this);
		this._tradingConsole.get_InstrumentSpanBindingSource().resetForeground(this);
	}

	private void changeSummaryPanelStyle(String dataSourceKey)
	{
		//this.setForeground(dataSourceKey, SummaryColKey.SellString, NumericColor.getColor(this._sellLots, true));
		this.setForeground(dataSourceKey, SummaryColKey.SellString, (this._sellLots != null && this._sellLots.compareTo(BigDecimal.ZERO) > 0) ? Color.red : NumericColor.zero);
		this.setForeground(dataSourceKey, SummaryColKey.BuyString, NumericColor.getColor(this._buyLots, true));
		this.setForeground(dataSourceKey, SummaryColKey.NetString, NumericColor.getColor(this._buyLots.subtract(this._sellLots), true));
		java.util.ArrayList<String> currencies = InstrumentPLFloatHelper.get_Currencies();
		if(currencies != null && currencies.size() > 0)
		{
			for(String currency : currencies)
			{
				if(this._plFloats.containsKey(currency))
				{
					double plFlaot = this._plFloats.get(currency);
					this.setForeground(dataSourceKey, currency, NumericColor.getColor(plFlaot, true));
				}
			}
		}

		if(this._accountTradingSummary != null)
		{
			for (int index = 0; index < this._accountTradingSummary.getRowCount(); index++)
			{
				AccountTradingSummary item
					= (AccountTradingSummary)this._accountTradingSummary.getObject(index);
				item.changeSummaryPanelStyle(AccountTradingSummary.AccountTradingSummaryBindingKey+this._code);
			}
		}
	}

	private void changeLastQuotationStyle(String dataSourceKey)
	{
		this.setIcon(dataSourceKey, InstrumentColKey.Bid, this._lastQuotation.getBidImage());
		this.setForeground(dataSourceKey, InstrumentColKey.Bid, this._lastQuotation.getBidForeColor());
		this.setForeground(dataSourceKey, InstrumentColKey.Ask, this._lastQuotation.getAskForeColor());
		this.setForeground(dataSourceKey, InstrumentColKey.Change, this._lastQuotation.getChangeColor());
	}

	public Color getBidForeColor()
	{
		return this._lastQuotation.getBidForeColor();
	}

	public Color getAskForeColor()
	{
		return this._lastQuotation.getAskForeColor();
	}

	public Color getBidBackColor()
	{
		return this._lastQuotation.getBidBackColor();
	}

	public Color getAskBackColor()
	{
		return this._lastQuotation.getAskBackColor();
	}

	public void setValidateColor()
	{
		if (!this.getIsValidate())
		{
			TradingConsole.traceSource.trace(TraceType.Information, "Set insturment background to InstrumentValidationBackColor.unvalidate");
			//this.setBackground(Instrument.tradingPanelKey, InstrumentValidationBackColor.unvalidate);
			this.setForeground(Instrument.tradingPanelKey, InstrumentValidationBackColor.unvalidate);
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Information, "Reset insturment background");
			//this.resetBackground(Instrument.tradingPanelKey);
			this.resetForeground(Instrument.tradingPanelKey);
		}
	}

	public void add(String dataSourceKey)
	{
		//this.refreshLastQuotation();
		TradingConsole.bindingManager.add(dataSourceKey, this);
		this.changeLastQuotationStyle(dataSourceKey);
		this.setValidateColor();
	}

	public void addSummaryPanel(String dataSourceKey)
	{
		if(this._category.equals(InstrumentCategory.Margin))
		{
			TradingConsole.bindingManager.add(dataSourceKey, this);
			this.changeSummaryPanelStyle(dataSourceKey);
		}
		//this.setValidateColor();
	}

	public String getBidLeft()
	{
		return Price.getPriceLeft(this.get_Bid());
	}

	public String getBidRight()
	{
		return Price.getPriceRight(this.get_Bid());
	}

	public String getAskLeft()
	{
		return Price.getPriceLeft(this.get_Ask());
	}

	public String getAskRight()
	{
		return Price.getPriceRight(this.get_Ask());
	}

	public void updateLastQuotationForUnitGrid()
	{
		if(!this._refreshQuotation) return;

		if (this._tradingConsole.get_InstrumentSpanBindingSource() != null)
		{
			this._tradingConsole.get_InstrumentSpanBindingSource().update(this);
		}
	}

	//public void set_UnitGrid(UnitGrid unitGrid)
	//{
	//    this.unitGrid = unitGrid;
	//}

	public void remove(String dataSourceKey)
	{
		TradingConsole.bindingManager.remove(dataSourceKey, this);
	}

	public static void removeAll(String dataSourceKey)
	{
		TradingConsole.bindingManager.removeAll(dataSourceKey);
	}

	public void update(String dataSourceKey, String propertyName, Object value)
	{
		this.refreshLastQuotation();
		TradingConsole.bindingManager.update(dataSourceKey, this, propertyName, value);
		this.changeLastQuotationStyle(dataSourceKey);
	}

	private void update2(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
		this.changeLastQuotationStyle(dataSourceKey);
	}

	public void update(String dataSourceKey)
	{
		if(!this._refreshQuotation) return;

		this.refreshLastQuotation();
		//if (this._tradingConsole.get_MainForm().get_InstrumentViewTab().getCurrentIndex()==0)
		//if (MainForm.instrumentViewTabCurrentIndex == 0)
		{
			this.update2(dataSourceKey);
		}
	}

	public void updateForSelection(String dataSourceKey)
	{
		this.refreshLastQuotation();
		this.update2(dataSourceKey);
	}

	public void updateSummaryPanel(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
		this.changeSummaryPanelStyle(dataSourceKey);
	}

	public boolean isAllowUpdateQuotationUI()
	{
		/*return (this._quotation.get_IsQuote()
				|| (this._lastQuotation.get_IsUsing() == false && this._quotation.get_IsChangedQuotation()));*/
		return (this._quotation.get_IsQuote()
				|| (this._quotation.get_IsChangedQuotation()));
	}

	public boolean isFromBursa()
	{
		return !StringHelper.isNullOrEmpty(this._externalExchangeCode) && this._externalExchangeCode.equals("Bursa");
	}

	/*
	  //Format: <DTYYYYMMDD>,<OPEN>,<HIGH>,<LOW>,<CLOSE>,<VOL>
	  //Format: 20030430,010100,1.3620,1.3638,1.3525,1.3550,0.0,0.0
	  public void saveRealTimeChartData()
	  {
	 String data = "";
	 String dateTime = XmlConvert.toString(this._quotation.get_Timestamp(), "yyyyMMdd") + CacheAwareDataProvider.separator; ;
	 dateTime += XmlConvert.toString(this._quotation.get_Timestamp(), "HHmmss") + CacheAwareDataProvider.separator; ;

	 String bid = Price.toString(this._quotation.get_Bid());
	 data = dateTime;
	 data += bid + CacheAwareDataProvider.separator;
	 data += bid + CacheAwareDataProvider.separator;
	 data += bid + CacheAwareDataProvider.separator;
	 data += bid + CacheAwareDataProvider.separator;
	 data += "0.0" + CacheAwareDataProvider.separator;
	 data += "0.0" + TradingConsole.enterLine;
	 CacheAwareDataProvider.saveChartData(DataCyle.cbIntraday1s, this, data, true);
	  }
	 */

	public void saveRealTimeChartData()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "Begin [Instrument.saveRealTimeChartData]");
		double ask = Price.toDouble(this._quotation.get_Ask());
		double bid = Price.toDouble(this._quotation.get_Bid());
		if(ask == 0.0 || bid == 0.0) return;
		org.aiotrade.core.common.Quotation quotation = new org.aiotrade.core.common.Quotation();
		quotation.setTime(this._quotation.get_Timestamp().toDate().getTime());
		quotation.setAsk( (float)ask);
		quotation.setBid( (float)bid);
		this._tradingConsole.get_ChartManager().setRealTimeData(this._id.toString(), quotation);
		TradingConsole.traceSource.trace(TraceType.Information, "End [Instrument.saveRealTimeChartData]");
	}

	//Added by Michael on 2008-04-23
	private void quotationForLastDayCloseTimeProcess(String[] quotationString)
	{
		TradingConsole.traceSource.trace(TraceType.Information, "Begin [Instrument.quotationForLastDayCloseTimeProcess]");
		Quotation quotation = this._quotation.clone();
		quotation.parse(quotationString, false);
		this.calculateTradePLFloat(quotation);
		TradingConsole.traceSource.trace(TraceType.Information, "End [Instrument.quotationForLastDayCloseTimeProcess]");
	}

	public void setBestLimit(BestLimit bestLimit)
	{
		this._bestLimits.set(bestLimit);
		if(bestLimit.get_Sequence() == 0) this.quotationChanged(false, true);
	}

	public void addTimeAndSale(TimeAndSale timeAndSale)
	{
		this._timeAndSales.add(timeAndSale);
	}

	public boolean setQuotation(String[] quotationString, boolean isQuote)
	{
		if(isQuote)
		{
			if(!this._inQuoting)
			{
				return true;
			}
			else
			{
				this._inQuoting = false;
			}
		}
		//Added by Michael on 2008-04-23
		if (!isQuote)
		{
			//if (this._lastDayCloseTime == null) ERROR
			if (this._lastDayCloseTime != null)
			{
				DateTime timestamp = AppToolkit.getDateTime(quotationString[1]);
				if (timestamp.equals(this._lastDayCloseTime))
				{
					/*if (this._quotation.get_Ask() == null || this._quotation.get_Bid() == null)
					{
						this.quotationForLastDayCloseTimeProcess(quotationString);
					}
					return;*/
				}
				else if (!timestamp.equals(AppToolkit.get_MinDatabaseDateTime()) && timestamp.before(this._lastDayCloseTime))
				{
					return false;
				}
			}
		}

		TradingConsole.traceSource.trace(TraceType.Information, "Begin of [Instrument.setQuotation]");

		this._last3Quotation = this._last2Quotation.clone();
		this._last2Quotation = this._last1Quotation.clone();
		this._last1Quotation = this._lastQuotation.clone();

		this._quotation.parse(quotationString, isQuote);

		return this.quotationChanged(isQuote);
	}

	private boolean quotationChanged(boolean isQuote)
	{
		return this.quotationChanged(isQuote, false);
	}

	private boolean quotationChanged(boolean isQuote, boolean isBestLimit)
	{
		boolean caculatePLFloat = false;
		//this.logger.debug("quotation changed");
		if(!isQuote && !isBestLimit)
		{
			TradingConsole.traceSource.trace(TraceType.Information, "[Instrument.quotationChanged]Instrument.saveRealTimeChartData");
			this.saveRealTimeChartData();
		}

		//Refresh UI
		if (!this.isAllowUpdateQuotationUI() && !isBestLimit)
		{
			//TradingConsole.traceSource.trace(TraceType.Information, "Don't update price " + quotationString + " for " + this._description);
		}
		else
		{
			//TradingConsole.traceSource.trace(TraceType.Information, "[Instrument.setQuotation]Instrument.update: update price " + quotationString + " for " + this._description);
			this.update(Instrument.tradingPanelKey);
			this._lastQuotation.set_IsQuote(isQuote);

			TradingConsole.traceSource.trace(TraceType.Information, "[Instrument.quotationChanged]Instrument.refreshColorSchedulerStart");
			this.refreshColorSchedulerStart();

			TradingConsole.traceSource.trace(TraceType.Information, "[Instrument.quotationChanged]Instrument.updateLastQuotationForUnitGrid");
			this.updateLastQuotationForUnitGrid();

			if(!this._lastQuotation.get_IsUsing() || isQuote)
			{
				//Notify Make order Ui
				TradingConsole.traceSource.trace(TraceType.Information, "[Instrument.quotationChanged]Instrument._settingsManager.notifyMakeOrderUiByPrice");
				this._settingsManager.notifyMakeOrderUiByPrice(this);
			}

			this._quotation.set_IsQuote(false);
			this._quotation.set_IsChangedQuotation(false);

			//calculation
			if (!isQuote && this._settingsManager.get_Customer().get_IsCalculateFloat() && !isBestLimit)
			{
				TradingConsole.traceSource.trace(TraceType.Information, "[Instrument.quotationChanged]Instrument.calculateTradePLFloat");
				this.calculateTradePLFloat(this._quotation);
				caculatePLFloat = true;
			}
		}

		if (Parameter.isCheckAcceptDQVariation)
		{
			TradingConsole.traceSource.trace(TraceType.Information,
											 "[Instrument.quotationChanged]Instrument._settingsManager.notifyIsAcceptMakeSpotTradeOrderByPrice");
			this._settingsManager.notifyIsAcceptMakeSpotTradeOrderByPrice(this);
		}
		TradingConsole.traceSource.trace(TraceType.Information, "End [Instrument.quotationChanged]");
		return caculatePLFloat;
	}

	public void answer(String[] quotationString, BigDecimal answerLot)
	{
		this._lastQuotation.set_AnswerLot(answerLot);
		this.setQuotation(quotationString, true);
	}

	private void refreshColorSchedulerStart()
	{
		DateTime appTime = TradingConsoleServer.appTime();
		TimeSpan timeSpan = TimeSpan.fromSeconds(3);
		String queue = this._id.toString();
		try
		{
			this.refreshColorSchedulerStop();

			this._refreshColorScheduleEntry = TradingConsoleServer.scheduler.add(queue, this, Instrument.refreshColor, appTime.add(timeSpan));
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	private synchronized void refreshColorSchedulerStop()
	{
		if (this._refreshColorScheduleEntry != null)
		{
			TradingConsoleServer.scheduler.remove(this._refreshColorScheduleEntry);
			this._refreshColorScheduleEntry = null;
		}
	}

	public void setFirstOpenCloseTime()
	{
		if (this._openTime != null && this._closeTime != null)
		{
			return;
		}

		DateTime appTime = TradingConsoleServer.appTime();
		DateTime maxEndTime = null;
		DateTime beginTimeTmp = null;
		DateTime firstBeginTime = null;
		DateTime firstEndTime = null;

		for (Iterator<TradingTime> iterator = this._tradingTimes.values().iterator(); iterator.hasNext(); )
		{
			TradingTime tradingTime = iterator.next();

			DateTime beginTime = tradingTime.get_BeginTime();
			DateTime endTime = tradingTime.get_EndTime();
			if (!appTime.before(beginTime) && !appTime.after(endTime))
			{
				this._openTime = beginTime;
				this._closeTime = endTime;
				break;
			}
			else
			{
				if (this._openTime == null || this._closeTime == null)
				{
					if (appTime.after(endTime))
					{
						if (maxEndTime == null)
						{
							maxEndTime = endTime;
							beginTimeTmp = beginTime;
						}
						else
						{
							if (!maxEndTime.after(endTime))
							{
								maxEndTime = endTime;
								beginTimeTmp = beginTime;
							}
						}
					}
					if (firstBeginTime == null)
					{
						firstBeginTime = beginTime;
						firstEndTime = endTime;
					}
				}
			}
		}
		if (this._openTime == null || this._closeTime == null)
		{
			if (maxEndTime != null)
			{
				this._openTime = beginTimeTmp;
				this._closeTime = maxEndTime;
			}
			else
			{
				if (firstBeginTime != null)
				{
					this._openTime = firstBeginTime;
					this._closeTime = firstEndTime;
				}
			}
		}
	}

	private void clearPrice(String dataSourceKey)
	{
		this._lastQuotation.clear();
		this._quotation.clear();
		this.update(dataSourceKey);
		this.updateLastQuotationForUnitGrid();
	}

	private void refreshTradingTime(TradingTime tradingTime)
	{
		this._openTime = tradingTime.get_BeginTime();
		this._closeTime = tradingTime.get_EndTime();

		this.setValidateColor();
	}

	private void refreshColor(String dataSourceKey)
	{
		this.setForeground(dataSourceKey, InstrumentColKey.Bid, QuotationStatusColor.notChange);
		this.setForeground(dataSourceKey, InstrumentColKey.Ask, QuotationStatusColor.notChange);
	}

	//Scheduler.SchedulerCallback.ActionsetDealStatus
	public synchronized void action(Scheduler.SchedulerEntry schedulerEntry)
	{
		if (schedulerEntry.get_IsRemoved())
		{
			return;
		}
		SwingUtilities.invokeLater(new InstrumentRefresher(this, schedulerEntry));
	}

	private static class InstrumentRefresher implements Runnable
	{
		private Scheduler.SchedulerEntry _schedulerEntry;
		private Instrument _owner;

		public InstrumentRefresher(Instrument owner, Scheduler.SchedulerEntry schedulerEntry)
		{
			this._owner = owner;
			this._schedulerEntry = schedulerEntry;
		}

		public void run()
		{
			if (_schedulerEntry.get_ActionArgs().getClass().equals(TradingTime.class))
			{
				TradingConsole.traceSource.trace(TraceType.Information,
												 "Instrument.action, TradeTime = " + _schedulerEntry.get_ActionArgs());

				this._owner.refreshTradingTime( (TradingTime)_schedulerEntry.get_ActionArgs());

				DateTime appTime = TradingConsoleServer.appTime();
				if (!this._owner._openTime.after(appTime) && !appTime.after(this._owner._closeTime))
				{
					LoginInformation loginInformation = this._owner._tradingConsole.get_LoginInformation();
					if (loginInformation.get_LoginTime().before(this._owner._openTime))
					{
						this._owner.clearPrice(Instrument.tradingPanelKey);
					}
				}
			}
			else if (_schedulerEntry.get_ActionArgs().equals(Instrument.refreshColor))
			{
				this._owner.refreshColor(Instrument.tradingPanelKey);
				//if (MainForm.instrumentViewTabCurrentIndex == 1 && this.unitGrid != null)
				if (this._owner._tradingConsole.get_InstrumentSpanBindingSource() != null)
				{
					this._owner._tradingConsole.get_InstrumentSpanBindingSource().resetForeground(this._owner);
					//this.unitGrid.refreshColor(this._tradingConsole.get_MainForm().get_Instrument2Table());
				}
			}
		}
	}

	public void dispose()
	{
		String queue = this._id.toString();
		TradingConsoleServer.scheduler.remove(queue);

		this._tradingTimes.clear();
		this._lastQuotation = null;
		this._quotation = null;
		this._transactions.clear();
		this._currency = null;

		this._last1Quotation = null;
		this._last2Quotation = null;
		this._last3Quotation = null;
	}

	public boolean getIsValidate()
	{
		if(this.isFromBursa() && this._tradingConsole.get_InstrumentStateManager() != null)
		{
			InstrumentTimeState state = this._tradingConsole.get_InstrumentStateManager().getInstrumentTimeState(this._id);
			if(state == InstrumentTimeState.Closed || state == InstrumentTimeState.End || !this._isActive)
			{
				return false;
			}
			else
			{
				return true;
			}
		}

		if ( (!this._isActive) || (this._openTime == null) || (this._closeTime == null))
		{
			return false;
		}
		DateTime appTime = TradingConsoleServer.appTime();
		if (!this._openTime.after(appTime)	&& !appTime.after(this._closeTime))
		{
			return true;
		}

		return false;
	}

	private HashMap<Guid, TradingItem> _floatingTradingItems = new HashMap<Guid, TradingItem> ();
	public HashMap<Guid, TradingItem> get_FloatingTradingItems()
	{
		return this._floatingTradingItems;
	}

	private HashMap<Guid, TradingItem> _notValuedTradingItems = new HashMap<Guid, TradingItem> ();
	public HashMap<Guid, TradingItem> get_NotValuedTradingItems()
	{
		return this._notValuedTradingItems;
	}

	public void calculateTradePLFloat()
	{
		this._floatingTradingItems.clear();
		this._notValuedTradingItems.clear();

		if (this._transactions.size() <= 0)
		{
			return;
		}

		for (Iterator iterator = this._transactions.values().iterator(); iterator.hasNext(); )
		{
			Transaction transaction = (Transaction)iterator.next();
			Guid accountId = transaction.get_Account().get_Id();
			for (Iterator<Order> iterator2 = transaction.get_Orders().values().iterator(); iterator2.hasNext(); )
			{
				Order order = iterator2.next();
				if(this._tradingConsole.getOpenOrder(order.get_Id()) == null) continue;
				if (order.get_Phase().equals(Phase.Executed)
					&& order.get_IsOpen()
					&& order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0)
				{
					if (!this._floatingTradingItems.containsKey(accountId))
					{
						this._floatingTradingItems.put(accountId, order.get_FloatTradingItem().clone());
						this._notValuedTradingItems.put(accountId, order.get_NotValuedTradingItem().clone());
					}
					else
					{
						TradingItem floatinTradingItem = TradingItem.add(this._floatingTradingItems.get(accountId), order.get_FloatTradingItem());
						this._floatingTradingItems.put(accountId, floatinTradingItem);

						TradingItem notValuedTradingItem = TradingItem.add(this._notValuedTradingItems.get(accountId), order.get_NotValuedTradingItem());
						this._notValuedTradingItems.put(accountId, notValuedTradingItem);
					}
				}
			}
		}
		Instrument.updateSubtotalSummary();
		this.updateSummaryPanel(Instrument.summaryPanelKey);
	}

	public void calculateTradePLFloat(Quotation newQuotation)
	{
		TradingConsole.traceSource.trace(TraceType.Information, "Begin [Instrument.calculateTradePLFloat]");
		if (this._transactions.size() <= 0)
		{
			TradingConsole.traceSource.trace(TraceType.Information, "End [Instrument.calculateTradePLFloat]");
			return;
		}
		Price newBuy = newQuotation.getBuySell(false);
		Price newSell = newQuotation.getBuySell(true);
		this.clearBuySellLots(false);
		HashMap<Guid, NecssaryCalculateTemporary> necssaryCalculateTemporarys = new HashMap<Guid,NecssaryCalculateTemporary>();
		for (Iterator iterator = this._transactions.values().iterator(); iterator.hasNext(); )
		{
			Transaction transaction = (Transaction)iterator.next();
			for (Iterator<Order> iterator2 = transaction.get_Orders().values().iterator(); iterator2.hasNext(); )
			{
				Order order = iterator2.next();
				if(this._tradingConsole.getOpenOrder(order.get_Id()) == null) continue;

				if (order.get_Phase().equals(Phase.Executed)
					&& order.get_IsOpen()
					&& order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0)
				{
					boolean isBuy = order.get_IsBuy();
					order.calculatePLFloat(newBuy, newSell);
					double quantity = order.get_LotBalance().doubleValue() * order.get_Transaction().get_ContractSize().doubleValue();
					NecssaryCalculateTemporary necssaryCalculateTemporary = null;
					Guid accountId = order.get_Account().get_Id();
					if(necssaryCalculateTemporarys.containsKey(accountId))
					{
						necssaryCalculateTemporary = necssaryCalculateTemporarys.get(accountId);
					}
					else
					{
						necssaryCalculateTemporary = new NecssaryCalculateTemporary();
						necssaryCalculateTemporarys.put(accountId, necssaryCalculateTemporary);
					}

					necssaryCalculateTemporary.BuySum += isBuy ? quantity : 0;
					necssaryCalculateTemporary.SellSum += isBuy ? 0 : quantity;
					necssaryCalculateTemporary.BuyMarginSum += isBuy ? order.get_Margin() : 0;
					necssaryCalculateTemporary.SellMarginSum += isBuy ? 0 : order.get_Margin();
					order.update(false);
					if(transaction.needCalculateSummary()) this.addToSummary(order);
				}
			}
		}
		this.calculateMargin(necssaryCalculateTemporarys);
		if(this != Instrument.SubtotalInstrument)
		{
			Instrument.updateSubtotalSummary();
		}
		this.updateSummaryPanel(Instrument.summaryPanelKey);
	}

	private HashMap<Guid, Double> _margins = new HashMap<Guid,Double>();
	private void calculateMargin(HashMap<Guid, NecssaryCalculateTemporary> necssaryCalculateTemporarys)
	{
		this._margins.clear();

		boolean shouldUseDayNecessary = this.shouldUseDayNecessary();
		for (Iterator<Guid> iterator2 = necssaryCalculateTemporarys.keySet().iterator(); iterator2.hasNext(); )
		{
			Guid accountId = iterator2.next();
			Account account = this._settingsManager.getAccount(accountId);
			NecssaryCalculateTemporary necssaryCalculateTemporary = necssaryCalculateTemporarys.get(accountId);
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), this._id);
			int decimals = Math.min(account.get_Currency().get_Decimals(), tradePolicyDetail.get_NecessaryRound());
			double margin = 0;
			double netNecessary = 0;
			double hedgeNecessary = 0;
			if (this._marginFormula == 0 || this._marginFormula == 1)
			{
				if(shouldUseDayNecessary)
				{
					if(tradePolicyDetail.get_VolumeNecessaryId() != null && this._marginFormula == 0)
					{
						VolumeNecessary volumeNecessary = this._settingsManager.getVolumeNecessary(tradePolicyDetail.get_VolumeNecessaryId());
						if(volumeNecessary == null) this._tradingConsole.get_MainForm().refreshSystem();
						netNecessary = volumeNecessary.calculateMargin(account.get_RateMarginD().doubleValue(), tradePolicyDetail.get_MarginD().doubleValue(), Math.abs(necssaryCalculateTemporary.BuyMarginSum - necssaryCalculateTemporary.SellMarginSum), true);
					}
					else
					{
						netNecessary = account.get_RateMarginD().doubleValue() * tradePolicyDetail.get_MarginD().doubleValue() *
							Math.abs(necssaryCalculateTemporary.BuyMarginSum - necssaryCalculateTemporary.SellMarginSum);
					}
					hedgeNecessary = account.get_RateMarginLockD().doubleValue() * tradePolicyDetail.get_MarginLockedD().doubleValue() * Math.min(necssaryCalculateTemporary.BuyMarginSum, necssaryCalculateTemporary.SellMarginSum);
				}
				else
				{
					if(tradePolicyDetail.get_VolumeNecessaryId() != null && this._marginFormula == 0)
					{
						VolumeNecessary volumeNecessary = this._settingsManager.getVolumeNecessary(tradePolicyDetail.get_VolumeNecessaryId());
						if(volumeNecessary == null) this._tradingConsole.get_MainForm().refreshSystem();
						netNecessary = volumeNecessary.calculateMargin(account.get_RateMarginO().doubleValue(), tradePolicyDetail.get_MarginO().doubleValue(), Math.abs(necssaryCalculateTemporary.BuyMarginSum - necssaryCalculateTemporary.SellMarginSum), false);
					}
					else
					{
						netNecessary = account.get_RateMarginO().doubleValue() * tradePolicyDetail.get_MarginO().doubleValue() *
							Math.abs(necssaryCalculateTemporary.BuyMarginSum - necssaryCalculateTemporary.SellMarginSum);
					}
					hedgeNecessary = account.get_RateMarginLockO().doubleValue() * tradePolicyDetail.get_MarginLockedO().doubleValue() * Math.min(necssaryCalculateTemporary.BuyMarginSum, necssaryCalculateTemporary.SellMarginSum);
				}
			}
			else if(this._marginFormula == 2 || this._marginFormula == 3
				|| this._marginFormula == 6 || this._marginFormula == 7)
			{
				double buyAvarage = necssaryCalculateTemporary.BuySum == 0 ? 0 : necssaryCalculateTemporary.BuyMarginSum / necssaryCalculateTemporary.BuySum;
				double sellAvarage = necssaryCalculateTemporary.SellSum == 0 ? 0 : necssaryCalculateTemporary.SellMarginSum / necssaryCalculateTemporary.SellSum;
				double hedge = Math.min(necssaryCalculateTemporary.BuySum, necssaryCalculateTemporary.SellSum);
				double netAvarage = necssaryCalculateTemporary.BuySum > necssaryCalculateTemporary.SellSum ? buyAvarage : sellAvarage;
				double net = Math.abs(necssaryCalculateTemporary.BuySum - necssaryCalculateTemporary.SellSum);

				if(shouldUseDayNecessary)
				{
					netNecessary = account.get_RateMarginD().doubleValue() * tradePolicyDetail.get_MarginD().doubleValue() * net * netAvarage;
					hedgeNecessary = account.get_RateMarginLockD().doubleValue() * tradePolicyDetail.get_MarginLockedD().doubleValue() * hedge * (buyAvarage + sellAvarage);
				}
				else
				{
					netNecessary = account.get_RateMarginO().doubleValue() * tradePolicyDetail.get_MarginO().doubleValue() * net * netAvarage;
					hedgeNecessary = account.get_RateMarginLockO().doubleValue() * tradePolicyDetail.get_MarginLockedO().doubleValue() * hedge * (buyAvarage + sellAvarage);
				}
			}

			margin = AppToolkit.round((netNecessary + hedgeNecessary), decimals);
			TradingConsole.traceSource.trace(TraceType.Verbose, this._code + ": " + margin);
			this._margins.put(accountId, margin);
		}
	}

	private boolean shouldUseDayNecessary()
	{
		boolean useNightNecessaryWhenBreak = this._tradingConsole.get_SettingsManager().get_SystemParameter().get_UseNightNecessaryWhenBreak();
		if (this.getIsValidate())
		{
			return true;
		}
		else if(this._dayOpenTime == null && this._dayCloseTime == null)
		{
			return false;
		}
		else
		{
			DateTime appTime = TradingConsoleServer.appTime();
			return (this._dayOpenTime.before(appTime) && appTime.before(this._dayCloseTime)) && (!useNightNecessaryWhenBreak);
		}
	}

	public static void updateSubtotalSummary()
	{
		Instrument.SubtotalInstrument._plFloats.clear();
		Instrument.SubtotalInstrument._plFloatCurrencies.clear();
		for(Instrument instrument : Instrument._instrumentsToSubtotal)
		{
			if(instrument.get_Category().equals(InstrumentCategory.Margin))
			{
				for (String currencyCode : instrument._plFloats.keySet())
				{
					double value = instrument._plFloats.get(currencyCode);
					Currency currency = instrument._plFloatCurrencies.get(currencyCode);
					Instrument.SubtotalInstrument.addPLFloat(currency, value);
				}
			}
		}
		Instrument.SubtotalInstrument.updateSummaryPanel(Instrument.summaryPanelKey);
	}

	/*
	  public void calculateTradePLFloat(Quotation newQuotation)
	  {
	 if (this._transactions.size() <= 0)
	 {
	  return;
	 }
	 HashMap<Guid, TradingItem> updatedAccountTradingItems = this._updatedAccountTradingItems;
	 updatedAccountTradingItems.clear();
	 Price newBuy = newQuotation.getBuySell(false);
	 Price newSell = newQuotation.getBuySell(true);
	 if (newSell == null && newBuy == null)
	 {
	  return;
	 }
	 this._tradingConsole.get_MainForm().get_AccountTree().setRedraw(false);
	 this._tradingConsole.get_MainForm().get_OrderTable().setRedraw(false);
	 this._tradingConsole.get_MainForm().get_OpenOrderTable().setRedraw(false);
	 for (Iterator iterator = this._transactions.values().iterator(); iterator.hasNext(); )
	 {
	  Transaction transaction = (Transaction)iterator.next();
	  Guid accountId = transaction.get_Account().get_Id();
	  for (Iterator<Order> iterator2 = transaction.get_Orders().values().iterator(); iterator2.hasNext(); )
	  {
	   Order order = iterator2.next();
	   if (order.get_Phase().equals(Phase.Executed)
	 && order.get_IsOpen()
	 && order.get_LotBalance().compareTo(new BigDecimal("0")) > 0)
	   {
	 boolean isBuy = order.get_IsBuy();
	 if ( (isBuy && newSell == null) || (!isBuy && newBuy == null))
	 {
	  continue;
	 }
	 TradingItem deltaTradingItem = order.calculatePLFloat(newBuy, newSell);
	 if (!updatedAccountTradingItems.containsKey(accountId))
	 {
	  updatedAccountTradingItems.put(accountId, deltaTradingItem);
	 }
	 else
	 {
	  TradingItem tradingItem = TradingItem.add(updatedAccountTradingItems.get(accountId), deltaTradingItem);
	  updatedAccountTradingItems.put(accountId, tradingItem);
	 }
	 //Update OpenOrderGrid
	 order.update();
	   }
	  }
	 }

	 for (Iterator<Guid> iterator = updatedAccountTradingItems.keySet().iterator(); iterator.hasNext(); )
	 {
	  Guid accountId = iterator.next();
	  Account account = this._settingsManager.getAccount(accountId);

	  Currency currency = (account.get_IsMultiCurrency()) ? this._currency : account.get_Currency();
	  AccountCurrency accountCurrency = this._settingsManager.getAccountCurrency(accountId, currency.get_Id());
	  TradingItem deltaAccountTradingItem = updatedAccountTradingItems.get(accountId);
	  accountCurrency.calculate(deltaAccountTradingItem);

	  //Update Account Currency
	  accountCurrency.updateNode();
	 }
	 this._tradingConsole.get_MainForm().get_AccountTree().setRedraw(true);
	 this._tradingConsole.get_MainForm().get_OrderTable().setRedraw(true);
	 this._tradingConsole.get_MainForm().get_OpenOrderTable().setRedraw(true);

	 this._tradingConsole.get_MainForm().get_AccountTree().doLayout();
	 this._tradingConsole.get_MainForm().get_OrderTable().doLayout();
	 this._tradingConsole.get_MainForm().get_OpenOrderTable().doLayout();
	  }
	 */

	//such as new order...
	public void reCalculateTradePLFloat()
	{
		this.calculateTradePLFloat(this._lastQuotation);
	}

	public static void updateInstruments(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode instrumentsNode, String updateType)
	{
		for (int i = 0; i < instrumentsNode.get_ChildNodes().get_Count(); i++)
		{
			XmlNode instrumentNode = instrumentsNode.get_ChildNodes().itemOf(i);
			Instrument.updateInstrument(tradingConsole, settingsManager, instrumentNode, updateType);
		}
	}

	public static void updateInstrument(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode instrumentNode, String updateType)
	{
		Guid instrumentId = new Guid(instrumentNode.get_Attributes().get_ItemOf("ID").get_Value());
		Instrument instrument = settingsManager.getInstrument(instrumentId);
		if (updateType.equals("Modify")) // || updateType.equals("Add"))
		{
			if (instrument == null)
			{
				//Not add

				//instrument = new Instrument(instrumentID,this);
				//instrument.initializeXml(instrumentNode,true);
				//this.Instruments.add(instrumentID,instrument);
				//instrument.refreshInstrumentWithTradingTime();
				//instrument.fillGrid();
				//instrument.tradingTimeAddSchedule();
			}
			else
			{
				instrument.setValue(instrumentNode.get_Attributes());
				//Update Instrument UI
				instrument.update2(Instrument.tradingPanelKey);
				Instrument.updateSubtotalSummary();
				instrument.updateSummaryPanel(Instrument.summaryPanelKey);

				if (instrument._tradingConsole.get_InstrumentSpanBindingSource() != null)
				{
					instrument._tradingConsole.get_InstrumentSpanBindingSource().update(instrument);
				}

				for (Iterator iterator = instrument.get_Transactions().values().iterator(); iterator.hasNext(); )
				{
					Transaction transaction = (Transaction)iterator.next();
					for (Iterator<Order> iterator2 = transaction.get_Orders().values().iterator(); iterator2.hasNext(); )
					{
						Order order = iterator2.next();
						order.update("InstrumentCode", instrument.get_Description());
					}
				}
			}
		}
		else if (updateType.equals("Delete"))
		{
			if (instrument != null)
			{
				settingsManager.removeInstrument(instrument);
				//Update Instrument UI
				instrument.remove(Instrument.tradingPanelKey);
				instrument.remove(Instrument.summaryPanelKey);

				instrument._tradingConsole.get_InstrumentSpanBindingSource().remove(instrument);
			}
		}
	}

	public static boolean updatePrivateDailyQuotation(SettingsManager settingsManager, XmlNode privateDailyQuotationNode, String updateType)
	{
		if (privateDailyQuotationNode == null)
		{
			return false;
		}
		XmlAttributeCollection privateDailyQuotationCollection = privateDailyQuotationNode.get_Attributes();
		int count = privateDailyQuotationCollection.get_Count();
		if (count <= 0)
		{
			return false;
		}
		Guid instrumentId = new Guid(privateDailyQuotationCollection.get_ItemOf("InstrumentID").get_Value());
		Instrument instrument = settingsManager.getInstrument(instrumentId);
		if (instrument == null)
		{
			return false;
		}

		DateTime tradeDay = AppToolkit.getDateTime(privateDailyQuotationCollection.get_ItemOf("TradeDay").get_Value());
		if (tradeDay == null)
		{
			return false;
		}
		DateTime dayCloseTime = AppToolkit.getDateTime(privateDailyQuotationCollection.get_ItemOf("DayCloseTime").get_Value());
		if (dayCloseTime != null) instrument._tradingConsole.get_ChartManager().refreshChartBar(instrument.get_Id().toString(), dayCloseTime.toDate());

		boolean isNeedCalculatePLFloat = false;
		boolean isDelete = updateType.equals("Delete");
		Quotation quotation = instrument.get_Quotation();

		Guid privateQuotePolicyId = settingsManager.get_Customer().get_PrivateQuotePolicyId();
		Guid publicQuotePolicyId = settingsManager.get_Customer().get_PublicQuotePolicyId();
		String privateAsk = null, privateBid = null, publicAsk = null, publicBid = null;
		XmlNodeList quotePolicyNodes = privateDailyQuotationNode.get_ChildNodes();
		for (int i = 0; i < quotePolicyNodes.get_Count(); i++)
		{
			XmlNode quotePolicyNode = quotePolicyNodes.item(i);
			if(quotePolicyNode.get_Name().equalsIgnoreCase("QuotePolicy"))
			{
				Guid quotePolicyId = new Guid(quotePolicyNode.get_Attributes().get_ItemOf("ID").get_Value());
				if(quotePolicyId.equals(privateQuotePolicyId))
				{
					privateAsk = quotePolicyNode.get_Attributes().get_ItemOf("Ask").get_Value();
					privateBid = quotePolicyNode.get_Attributes().get_ItemOf("Bid").get_Value();
				}
				else if(quotePolicyId.equals(publicQuotePolicyId))
				{
					publicAsk = quotePolicyNode.get_Attributes().get_ItemOf("Ask").get_Value();
					publicBid = quotePolicyNode.get_Attributes().get_ItemOf("Bid").get_Value();
				}
			}
		}

		for (int i = 0; i < count; i++)
		{
			String nodeName = privateDailyQuotationCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = privateDailyQuotationCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("Open") && tradeDay.getTime() == settingsManager.get_TradeDay().get_TradeDay().getTime())
			{
				Price open = (StringHelper.isNullOrEmpty(nodeValue)) ? null :
					Price.create(Double.valueOf(nodeValue), instrument.get_NumeratorUnit(), instrument.get_Denominator());

				quotation.setOpen(true, isDelete ? null : open);
				instrument.update(Instrument.tradingPanelKey);
			}
			else if (nodeName.equals("Close") && tradeDay.getTime() == settingsManager.get_TradeDay().get_LastTradeDay().getTime())
			{
				Price close = (StringHelper.isNullOrEmpty(nodeValue)) ? null :
					Price.create(Double.valueOf(nodeValue), instrument.get_NumeratorUnit(), instrument.get_Denominator());

				quotation.setPrevClose(true, (isDelete ? null : close));
				instrument.update(Instrument.tradingPanelKey);
			}
			else if (nodeName.equals("Bid") && tradeDay.getTime() == settingsManager.get_TradeDay().get_TradeDay().getTime())
			{
				boolean closingUseCustomerQuotePolicy = instrument._tradingConsole.get_SettingsManager().get_SystemParameter().get_ClosingUseCustomerQuotePolicy();
				closingUseCustomerQuotePolicy = true;
				if (!closingUseCustomerQuotePolicy)
				{
					if (publicBid != null)
						nodeValue = publicBid;
					if (privateBid != null)
						nodeValue = privateBid;

					Price bid = (StringHelper.isNullOrEmpty(nodeValue)) ? null :
						Price.create(Double.valueOf(nodeValue), instrument.get_NumeratorUnit(), instrument.get_Denominator());

					if (!isDelete)
					{
						quotation.set_IsChangedQuotation(true);
						quotation.setBid(true, bid);
						isNeedCalculatePLFloat |= instrument.quotationChanged(false);
					}
				}
			}
			else if (nodeName.equals("Ask") && tradeDay.getTime() == settingsManager.get_TradeDay().get_TradeDay().getTime())
			{
				boolean closingUseCustomerQuotePolicy = instrument._tradingConsole.get_SettingsManager().get_SystemParameter().get_ClosingUseCustomerQuotePolicy();
				closingUseCustomerQuotePolicy = true;
				if (!closingUseCustomerQuotePolicy)
				{

					if (publicAsk != null)
						nodeValue = publicAsk;
					if (privateAsk != null)
						nodeValue = privateAsk;

					Price ask = (StringHelper.isNullOrEmpty(nodeValue)) ? null :
						Price.create(Double.valueOf(nodeValue), instrument.get_NumeratorUnit(), instrument.get_Denominator());

					if (!isDelete)
					{
						quotation.set_IsChangedQuotation(true);
						quotation.setAsk(true, ask);
						isNeedCalculatePLFloat |= instrument.quotationChanged(false);
					}
				}
			}
		}
		if(isNeedCalculatePLFloat)
		{
			instrument.calculateTradePLFloat(quotation);
		}
		return isNeedCalculatePLFloat;
	}

	public static boolean updatePublicDailyQuotation(SettingsManager settingsManager, XmlNode publicDailyQuotationNode, String updateType)
	{
		if (publicDailyQuotationNode == null)
		{
			return false;
		}
		XmlAttributeCollection publicDailyQuotationCollection = publicDailyQuotationNode.get_Attributes();
		int count = publicDailyQuotationCollection.get_Count();
		if (count <= 0)
		{
			return false;
		}
		String originCode = publicDailyQuotationCollection.get_ItemOf("InstrumentOriginCode").get_Value();
		if (StringHelper.isNullOrEmpty(originCode))
		{
			return false;
		}
		DateTime tradeDay = AppToolkit.getDateTime(publicDailyQuotationCollection.get_ItemOf("TradeDay").get_Value());
		if (tradeDay == null)
		{
			return false;
		}

		XmlNodeList childList = publicDailyQuotationNode.get_ChildNodes();
		for(int count2 = 0; count2 < childList.get_Count(); count2++)
		{
			XmlNode instrumentNode = childList.item(count2);
			if(instrumentNode.get_Name().equalsIgnoreCase("Instrument"))
			{
				XmlAttributeCollection instrumentNodeCollection = instrumentNode.get_Attributes();
				Guid instrumentId = new Guid(instrumentNodeCollection.get_ItemOf("ID").get_Value());
				if(settingsManager.getInstruments().containsKey(instrumentId))
				{
					Instrument instrument = settingsManager.getInstruments().get(instrumentId);
					DateTime dayCloseTime = AppToolkit.getDateTime(instrumentNodeCollection.get_ItemOf("DayCloseTime").get_Value());
					instrument._tradingConsole.get_ChartManager().refreshChartBar(instrument.get_Id().toString(), dayCloseTime.toDate());
				}
			}
		}

		boolean isNeedCalculatePLFloat = false;
		boolean isDelete = updateType.equals("Delete");
		for (Iterator<Instrument> iterator = settingsManager.getInstruments().values().iterator(); iterator.hasNext(); )
		{
			boolean isNeedCalculatePLFloat2 = false;
			Instrument instrument = iterator.next();
			if (instrument.get_OriginCode().equals(originCode))
			{
				Guid privateQuotePolicyId = settingsManager.get_Customer().get_PrivateQuotePolicyId();
				Guid publicQuotePolicyId = settingsManager.get_Customer().get_PublicQuotePolicyId();
				String privateAsk = null, privateBid = null, publicAsk = null, publicBid = null;
				XmlNodeList quotePolicyNodes = publicDailyQuotationNode.get_ChildNodes();
				for (int i = 0; i < quotePolicyNodes.get_Count(); i++)
				{
					XmlNode quotePolicyNode = quotePolicyNodes.item(i);
					if (quotePolicyNode.get_Name().equalsIgnoreCase("QuotePolicy"))
					{
						Guid quotePolicyId = new Guid(quotePolicyNode.get_Attributes().get_ItemOf("ID").get_Value());
						Guid instrumentId = new Guid(quotePolicyNode.get_Attributes().get_ItemOf("InstrumentID").get_Value());
						if(instrumentId.equals(instrument.get_Id()))
						{
							if (quotePolicyId.equals(privateQuotePolicyId))
							{
								privateAsk = quotePolicyNode.get_Attributes().get_ItemOf("Ask").get_Value();
								privateBid = quotePolicyNode.get_Attributes().get_ItemOf("Bid").get_Value();
							}
							else if (quotePolicyId.equals(publicQuotePolicyId))
							{
								publicAsk = quotePolicyNode.get_Attributes().get_ItemOf("Ask").get_Value();
								publicBid = quotePolicyNode.get_Attributes().get_ItemOf("Bid").get_Value();
							}
						}
					}
				}

				Quotation quotation = instrument.get_Quotation();
				for (int i = 0; i < count; i++)
				{
					String nodeName = publicDailyQuotationCollection.get_ItemOf(i).get_LocalName();
					String nodeValue = publicDailyQuotationCollection.get_ItemOf(i).get_Value();
					if (nodeName.equals("Open") && tradeDay.getTime() == settingsManager.get_TradeDay().get_TradeDay().getTime())
					{
						if (!quotation.get_IsPrivateOpen())
						{
							Price open = (StringHelper.isNullOrEmpty(nodeValue)) ? null :
								Price.create(Double.valueOf(nodeValue), instrument.get_NumeratorUnit(), instrument.get_Denominator());

							quotation.setOpen(false, ( isDelete? null : open));
							instrument.update(Instrument.tradingPanelKey);
						}
					}
					else if (nodeName.equals("Close") && tradeDay.getTime() == settingsManager.get_TradeDay().get_LastTradeDay().getTime())
					{
						if (!quotation.get_IsPrivateClose())
						{
							Price close = (StringHelper.isNullOrEmpty(nodeValue)) ? null :
								Price.create(Double.valueOf(nodeValue), instrument.get_NumeratorUnit(), instrument.get_Denominator());
							quotation.setPrevClose(false, ( isDelete ? null : close));
							instrument.update(Instrument.tradingPanelKey);
						}
					}
					else if (nodeName.equals("Bid") && tradeDay.getTime() == settingsManager.get_TradeDay().get_TradeDay().getTime())
					{
						if(publicBid != null) nodeValue = publicBid;
						if(privateBid != null) nodeValue = privateBid;

						boolean closingUseCustomerQuotePolicy = instrument._tradingConsole.get_SettingsManager().get_SystemParameter().get_ClosingUseCustomerQuotePolicy();
						closingUseCustomerQuotePolicy = true;
						if (!closingUseCustomerQuotePolicy && !quotation.get_IsPrivateBid())
						{
							Price bid = (StringHelper.isNullOrEmpty(nodeValue)) ? null :
								Price.create(Double.valueOf(nodeValue), instrument.get_NumeratorUnit(), instrument.get_Denominator());

							if(!isDelete)
							{
								quotation.set_IsChangedQuotation(true);
								quotation.setBid(false, bid);
								isNeedCalculatePLFloat2 |= instrument.quotationChanged(false);
							}
						}
					}
					else if (nodeName.equals("Ask") && tradeDay.getTime() == settingsManager.get_TradeDay().get_TradeDay().getTime())
					{
						if(publicAsk != null) nodeValue = publicAsk;
						if(privateAsk != null) nodeValue = privateAsk;

						boolean closingUseCustomerQuotePolicy = instrument._tradingConsole.get_SettingsManager().get_SystemParameter().get_ClosingUseCustomerQuotePolicy();
						closingUseCustomerQuotePolicy = true;
						if (!closingUseCustomerQuotePolicy && !quotation.get_IsPrivateAsk())
						{
							Price ask = (StringHelper.isNullOrEmpty(nodeValue)) ? null :
								Price.create(Double.valueOf(nodeValue), instrument.get_NumeratorUnit(), instrument.get_Denominator());

							if(!isDelete)
							{
								quotation.set_IsChangedQuotation(true);
								quotation.setAsk(false, ask);
								isNeedCalculatePLFloat2 |= instrument.quotationChanged(false);
							}
						}
					}
				}
				if (isNeedCalculatePLFloat2)
				{
					instrument.calculateTradePLFloat(quotation);
				}
				isNeedCalculatePLFloat |= isNeedCalculatePLFloat2;
			}
		}
		return isNeedCalculatePLFloat;
	}

	public static void updateQuotePolicyDetail(TradingConsole tradingConsole, SettingsManager settingsManager, XmlNode quotePolicyDetailNode,
											   String updateType)
	{
		if (quotePolicyDetailNode == null)
		{
			return;
		}
		XmlAttributeCollection quotePolicyDetailCollection = quotePolicyDetailNode.get_Attributes();
		int count = quotePolicyDetailCollection.get_Count();
		if (count <= 0)
		{
			return;
		}
		Guid instrumentId = new Guid(quotePolicyDetailCollection.get_ItemOf("InstrumentID").get_Value());
		Instrument instrument = settingsManager.getInstrument(instrumentId);
		if (instrument == null)
		{
			return;
		}
		Guid quotePolicyId = new Guid(quotePolicyDetailCollection.get_ItemOf("QuotePolicyID").get_Value());
		if (settingsManager.get_Customer().get_PrivateQuotePolicyId().equals(quotePolicyId)
			&&	updateType.equals("Modify")) // || updateType.equals("Add"))
		{
			if (quotePolicyDetailCollection.get_ItemOf("PriceType") != null)
			{
				short priceType = Short.parseShort(quotePolicyDetailCollection.get_ItemOf("PriceType").get_Value());
				instrument.set_PriceType(priceType);
				//call webservice,.....Update service State.Instruments
				tradingConsole.get_TradingConsoleServer().updateQuotePolicyDetail(instrumentId, quotePolicyId);
			}
		}
	}

	public static boolean getSelectIsBuy(Instrument instrument, boolean isDblClickAsk)
	{
		return ( (instrument.get_IsNormal() ^ isDblClickAsk) ? false : true);
	}

//when confirm DQ Order, check if accept price again..............
	public boolean isAcceptMakeSpotTradeOrderByPrice(Boolean isBuy)
	{
		boolean isAccept = true;
		Price setPrice = this.get_LastQuotation().getBuySell(isBuy);
		if (setPrice == null) return isAccept;

		if (this._isNormal == isBuy)
		{
			Price setPrice2 = Price.add(setPrice, this.get_AcceptDQVariation());
			//if (!Price.less(this.get_LastQuotation().get_Ask(), setPrice2))
			if (Price.more(this.get_Quotation().get_Ask(), setPrice2))
			{
				isAccept = false;
			}
		}
		else
		{
			Price setPrice2 = Price.subStract(setPrice, this.get_AcceptDQVariation());
			//if (!Price.less(setPrice2, this.get_LastQuotation().get_Bid()))
			if (Price.more(setPrice2, this.get_Quotation().get_Bid()))
			{
				isAccept = false;
			}
		}
		return isAccept;
	}

	public void addSellLots(BigDecimal lot)
	{
		this._sellLots = this._sellLots.add(lot);
	}

	public BigDecimal get_SellLots()
	{
		return this._sellLots;
	}

	public BindingSource get_AccountTradingSummary()
	{
		return this._accountTradingSummary;
	}

	private BindingSource _accountTradingSummary = null;
	public void clearBuySellLots()
	{
		this.clearBuySellLots(true);
	}

	public void clearBuySellLots(boolean clearCurrencies)
	{
		this._buyLots = BigDecimal.ZERO;
		this._sellLots = BigDecimal.ZERO;
		this._totalBuyPrice = 0.0;//BigDecimal.ZERO;
		this._totalSellPrice = 0.0;//BigDecimal.ZERO;
		this._plFloats.clear();
		if(clearCurrencies) this._plFloatCurrencies.clear();

		if(this._accountTradingSummary != null)
		{
			if(clearCurrencies)
			{
				this._accountTradingSummary.removeAll();
			}
			else
			{
				for (int index = 0; index < this._accountTradingSummary.getRowCount(); index++)
				{
					AccountTradingSummary item
						= (AccountTradingSummary)this._accountTradingSummary.getObject(index);
					item.clearBuySellLots(clearCurrencies);
				}
			}
		}
	}

	public void addToSummary(Order order)
	{
		if(!order.get_PhysicalTradeSide().equals(PhysicalTradeSide.None)) return;

		BigDecimal lotBalance = order.get_LotBalance();
		//BigDecimal executePrice = new BigDecimal(Price.toString(order.get_ExecutePrice()), this.get_Decimal());
		double executePrice = Price.toDouble(order.get_ExecutePrice());

		if(lotBalance.compareTo(BigDecimal.ZERO) > 0)
		{
			if (order.get_IsBuy())
			{
				this._buyLots = this._buyLots.add(lotBalance);
				this._totalBuyPrice += executePrice * lotBalance.doubleValue();
			}
			else
			{
				this._sellLots = this._sellLots.add(lotBalance);
				this._totalSellPrice += executePrice * lotBalance.doubleValue();
			}

			double plFloat = order.get_TradePLFloat();
			Currency currency = order.get_Transaction().get_Account().get_IsMultiCurrency() ?
					order.get_Transaction().get_Instrument().get_Currency() : order.get_Transaction().get_Account().get_Currency();
			this.addPLFloat(currency, plFloat);
			InstrumentPLFloatHelper.addFloatCurrency(currency.get_Code());

			boolean hasMultiAccount = this._settingsManager.get_Accounts().size() > 1;
			if(hasMultiAccount)
			{
				if (this._accountTradingSummary == null)
				{
					this._accountTradingSummary = new BindingSource();
					TradingConsole.bindingManager.bind(AccountTradingSummary.AccountTradingSummaryBindingKey+this._code, new Vector(0), this._accountTradingSummary,
						AccountTradingSummary.getPropertyDescriptorsForSummary(this._tradingConsole.get_MainForm().get_PositionSummaryFrame()));

					TableModelListener tableModelListener = new TableModelListener()
					{
						public void tableChanged(TableModelEvent e)
						{
							if (e.getType() == TableModelEvent.DELETE) // || e.getType() == TableModelEvent.INSERT)
							{
								if (_accountTradingSummary.getRowCount() == 0)
								{
									_tradingConsole.rebindSummary(true);
								}
								//_tradingConsole.get_MainForm().get_SummaryTable().collapseAllRows(); //fix bug of jide
							}
						}
					};
					this._accountTradingSummary.addTableModelListener(tableModelListener);
				}

				AccountTradingSummary accountTradingSummary = null;
				for (int index = 0; index < this._accountTradingSummary.getRowCount(); index++)
				{
					AccountTradingSummary item
						= (AccountTradingSummary)this._accountTradingSummary.getObject(index);
					if (item.get_Account().get_Id().compareTo(order.get_Transaction().get_Account().get_Id()) == 0)
					{
						accountTradingSummary = item;
					}
				}

				if (accountTradingSummary == null)
				{
					accountTradingSummary = new AccountTradingSummary(this, order.get_Transaction().get_Account());
					this._accountTradingSummary.add(accountTradingSummary);
				}
				accountTradingSummary.addToSummary(order);
			}
		}
	}

	public BigDecimal get_BuyLots()
	{
		return this._buyLots;
	}

	public void calculateSummary()
	{
		if(this._category.equals(InstrumentCategory.Margin))
		{
			this._tradingConsole.get_MainForm().get_SummaryTable().collapseAllRows();
			if (this._accountTradingSummary != null)
			{
				this._accountTradingSummary.removeAll();
			}
			this.clearBuySellLots(false);

			for (Iterator<Transaction> iterator = this._transactions.values().iterator(); iterator.hasNext(); )
			{
				Transaction transaction = iterator.next();
				if (transaction.get_Phase() == Phase.Executed && transaction.needCalculateSummary())
				{
					HashMap<Guid, Order> orders = transaction.get_Orders();
					for (Iterator<Order> iterator2 = orders.values().iterator(); iterator2.hasNext(); )
					{
						Order order = iterator2.next();
						if (order.get_Phase() == Phase.Executed)
						{
							this.addToSummary(order);
							/*if (order.get_IsBuy())
								   {
							 this._buyLots = this._buyLots.add(order.get_LotBalance());
								   }
								   else
								   {
							 this._sellLots = this._sellLots.add(order.get_LotBalance());
								   }*/
						}
					}
				}
			}
		}
	}

	//Matching Order--Begin
	private String _matchOrderKey;
	private tradingConsole.ui.grid.BindingSource _bindingSourceForMatchOrder;
	private HashMap<Guid, MatchOrder> _matchOrders;

	public void setMatchOrder(MatchOrder matchOrder)
	{
		if (!this._matchOrders.containsKey(matchOrder.get_Id()))
		{
			this._matchOrders.put(matchOrder.get_Id(), matchOrder);
		}
	}

	public void removeMatchOrder(MatchOrder matchOrder)
	{
		if (!this._matchOrders.containsKey(matchOrder.get_Id()))
		{
			this._matchOrders.remove(matchOrder);
		}
	}

	//?????????
	public void setMatchOrders()
	{
		if (this._matchOrders.size() > 0)
		{
			return;
		}

		//test
		String xml = "<MatchingOrders><MatchingOrder ID='73F10163-A268-4AA5-A813-3688A2EAB5BB' BidLot='2' BidPrice='1.2620' AskLot='1' AskPrice='1.2528' /><MatchingOrder ID='73F10163-A268-4AA5-A813-3688A2EAB5B1' BidLot='1' BidPrice='1.2510' AskLot='1' AskPrice='1.2518' /><MatchingOrder ID='73F10163-A268-4AA5-A813-3688A2EAB5BC' BidLot='2' BidPrice='1.2650' AskLot='2' AskPrice='1.2428' /></MatchingOrders>";
		XmlDocument xmlDocument = new XmlDocument();
		xmlDocument.loadXml(xml);
		XmlNode xmlNode = xmlDocument.get_DocumentElement();

		XmlNodeList xmlNodeList = xmlNode.get_ChildNodes();
		for (int i = 0; i < xmlNodeList.get_Count(); i++)
		{
			XmlAttributeCollection collection = xmlNodeList.item(i).get_Attributes();
			MatchOrder matchOrder = new MatchOrder(this._tradingConsole, this._settingsManager, this, collection);
			this._matchOrders.put(matchOrder.get_Id(), matchOrder);
		}

		//?????????
		//DataRow dataRow = null;
		//MatchOrder matchOrder = new MatchOrder(this._tradingConsole, this._settingsManager, dataRow);
		//MatchOrder matchOrder = new MatchOrder(this._tradingConsole, this._settingsManager);
		//this._matchOrders.put(matchOrder.get_Id(), matchOrder);
	}

	public void initializeMatchOrder(DataGrid grid, boolean ascend, boolean caseOn)
	{
		//unbind last Match Order
		this.unbindMatchOrder();

		this._matchOrderKey = Guid.newGuid().toString();
		this._bindingSourceForMatchOrder = new tradingConsole.ui.grid.BindingSource();

		this.setMatchOrders();
		MatchOrder.initialize(this._settingsManager, grid, this._matchOrderKey, this._matchOrders.values(), this._bindingSourceForMatchOrder);
		for (Iterator<MatchOrder> iterator = this._matchOrders.values().iterator(); iterator.hasNext(); )
		{
			MatchOrder matchOrder = iterator.next();

			matchOrder.update(this._matchOrderKey);
		}
		//??????
		int column = this._bindingSourceForMatchOrder.getColumnByName(MatchOrderColKey.Id);
		grid.sortColumn(column, ascend);
	}

	public void unbindMatchOrder()
	{
		if (!StringHelper.isNullOrEmpty(this._matchOrderKey) && this._bindingSourceForMatchOrder != null)
		{
			MatchOrder.unbind(this._matchOrderKey, this._bindingSourceForMatchOrder);
		}
	}

	public void finalize() throws Throwable
	{
		this.unbindMatchOrder();

		super.finalize();
	}

	public int get_Decimal()
	{
		double result = Math.log10(this._denominator);
		if(((int)result) == result)
		{
			return (int)result;
		}
		else
		{
			return 2;
		}
	}
	//Matching Order--End

	private DealingPolicyDetail getDealingPolicyDetail()
	{
		Guid dealingPolicyId = this._settingsManager.get_Customer().get_DealingPolicyId();
		if(dealingPolicyId == null)
		{
			return null;
		}
		else
		{
			return this._settingsManager.getDealingPolicyDetail(dealingPolicyId, this._id);
		}
	}

	private boolean _inQuoting = false;
	public void set_InQuoting(boolean value)
	{
		this._inQuoting = value;
	}

	/**
	 * FreezeRefreshQuotation
	 */
	private boolean _refreshQuotation = true;
	public void freezeRefreshQuotation()
	{
		this._refreshQuotation = false;
		this._tradingConsole.get_ChartManager().freeze(this._id.toString());
	}

	public void unfreezeRefreshQuotation()
	{
		boolean oldValue = this._refreshQuotation;
		this._refreshQuotation = true;
		if(!oldValue) this.update(Instrument.tradingPanelKey);
		this._tradingConsole.get_ChartManager().unfreeze(this._id.toString());
	}

	public static void clearSummary()
	{
		Instrument.SubtotalInstrument.clearBuySellLots();
		Instrument._instrumentsToSubtotal.clear();
	}

	public static void fillInstrumentsToSubtotal(Collection<Instrument> instruments)
	{
		for(Instrument instrument : instruments)
		{
			if(!Instrument._instrumentsToSubtotal.contains(instrument))
			{
				Instrument._instrumentsToSubtotal.add(instrument);
			}
		}
	}

}

class InstrumentPLFloatHelper
{
	private static TradingConsole _tradingConsole = null;
	private static java.util.ArrayList<String> _currencies = new java.util.ArrayList<String>();

	static void initialize(TradingConsole tradingConsole)
	{
		_tradingConsole = tradingConsole;
		_currencies.clear();
	}

	static java.util.ArrayList<String> get_Currencies()
	{
		return _currencies;
	}

	static void addFloatCurrency(String key)
	{
		if(!_currencies.contains(key))
		{
			_currencies.add(key);
			if(_tradingConsole != null) _tradingConsole.rebindSummary();
		}
	}
}

class NecssaryCalculateTemporary
{
	public double BuySum = 0;
	public double SellSum = 0;
	public double BuyMarginSum = 0;
	public double SellMarginSum = 0;
}
