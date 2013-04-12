package tradingConsole;

import java.io.*;
import java.math.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import com.jidesoft.grid.*;
import framework.*;
import framework.DateTime;
import framework.data.*;
import framework.diagnostics.*;
import framework.lang.Enum;
import framework.xml.*;
import tradingConsole.CloseOrder.*;
import tradingConsole.enumDefine.*;
import tradingConsole.framework.*;
import tradingConsole.settings.*;
import tradingConsole.ui.*;
import tradingConsole.ui.borderStyleHelper.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnFixedHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.fontHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;

public class Order
{
	public static final String workingOrdersKey = "WorkingOrdersKey";
	public static final String notConfirmedPendingOrdersKey = "NotConfirmedPendingOrdersKey";
	public static final String workingRelationOrdersKey = "WorkingRelationOrdersKey";
	public static final String openOrdersKey = "OpenOrdersKey";
	public static OrderComparatorForAdjustingLot orderComparatorForAdjustingLot = new OrderComparatorForAdjustingLot();

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;

	private Guid _id;
	private String _code;
	private BigDecimal _lot;
	private BigDecimal _lotBalance;
	private boolean _isOpen;
	private boolean _isBuy;
	private Price _setPrice;
	private Price _setPrice2;
	private Price _executePrice;
	private DateTime _executeTradeDay;
	private Price _autoLimitPrice;
	private Price _autoStopPrice;
	private TradeOption _tradeOption;
	private BigDecimal _commissionSum;
	private BigDecimal _levySum;
	private BigDecimal _interestPerLot;
	private BigDecimal _storagePerLot;
	private double _tradePL;
	private Phase _phase; //when an OCO order was executed,another OCO order's phase = cancelled, but transaction's phase = executed

	//will rewrite it use _relationOrders?????????????????
	private String _peerOrderCodes;
	private String _peerOrderIDs;
	private HashMap<Guid, RelationOrder> _relationOrders;

	private TradingItem _pLTradingItem; //If multi currency account then this.TradePL * Rate is require.
	private TradingItem _floatTradingItem;
	private TradingItem _notValuedTradingItem;

	private boolean _isRejectDQByDealer;
	private boolean _close;
	private Price _livePrice;
	private String _message;
	private short _displayDecimals;
	private BigDecimal _interestRate;
	private double _margin;

	private int _dQMaxMove = 0;

	//assign order flag, for put it to workingOrderList
	private boolean _isAssignOrder;

	private Transaction _transaction;

	//for Make Order----------------------------------
	private int _sequence;
	private String _verificationOutstandingKey;
	private tradingConsole.ui.grid.BindingSource _bindingSourceForVerificationOutstanding;
	private OrderModification _orderModification;
	private PLNotValued _PLNotValued = new PLNotValued();

	//outstanding order use
	public String get_Summary()
	{
		return this.get_ExecuteTradeDay() + " " + this.get_LotBalanceString()
			+ this.get_IsBuyString() + " " + this.get_ExecutePriceString();
	}

	public String get_CurrencyRate()
	{
		CurrencyRate currencyRate =
			this._settingsManager.getCurrencyRate(this.get_Instrument().get_Currency().get_Id(), this.get_Account().get_Currency().get_Id());
		double rate = currencyRate.getRateFor(get_TradePLFloat());
		if (rate < 1)
		{
			rate = 1 / rate;
		}
		return AppToolkit.format(rate, 4);
	}

	public String get_PeerOrderIDs()
	{
		return this._peerOrderIDs;
	}

	public void set_PeerOrderIDs(String value)
	{
		this._peerOrderIDs = value;
	}

	public void setPeerOrderIDs(String value)
	{
		this._peerOrderIDs = value;
		this.initailizeCloseOrders();
	}

	public void initailizeCloseOrders()
	{
		if (!StringHelper.isNullOrEmpty(this._peerOrderIDs))
		{
			String[] orders = StringHelper.split(this._peerOrderIDs, TradingConsole.delimiterRow);
			for (String order : orders)
			{
				String[] items = StringHelper.split(order, TradingConsole.delimiterCol);
				Guid peerOrderId = new Guid(items[0]);
				Order peerOrder = this._tradingConsole.getOrder(peerOrderId);

				BigDecimal closeLot = new BigDecimal(items[1]);

				if (this._isOpen && !peerOrder._isOpen)
				{
					peerOrder.addRelationOrder(this, closeLot);
					this.addCloseOrder(peerOrder);
				}
				else if (!this._isOpen && peerOrder._isOpen)
				{
					this.addRelationOrder(peerOrder, closeLot);
					peerOrder.addCloseOrder(this);
				}
			}
		}
	}

	public void addRelationOrder(Order openOrder, BigDecimal closeLot)
	{
		if (this._relationOrders == null)
		{
			this._relationOrders = new HashMap<Guid, RelationOrder> ();
		}

		RelationOrder relationOrder = new RelationOrder(this._tradingConsole, this._settingsManager, openOrder, closeLot);
		relationOrder.set_IsSelected(true);
		this._relationOrders.put(openOrder.get_Id(), relationOrder);
	}

	public void refrshCloseOrders()
	{
		if (this._relationOrders != null && this._relationOrders.size() > 0)
		{
			for (RelationOrder relationOrder : this._relationOrders.values())
			{
				if (relationOrder.get_IsSelected())
				{
					Order openOrder = relationOrder.get_OpenOrder();
					openOrder.addCloseOrder(this);
				}
			}
		}
	}

	public BindingSource get_CloseOrders()
	{
		return this._closeOrders;
	}

	private BindingSource _closeOrders;
	private String _limitCloseOrderSummary;
	private String _stopCloseOrderSummary;

	public String get_LimitCloseOrderSummary()
	{
		return this._limitCloseOrderSummary;
	}

	public void set_LimitCloseOrderSummary(String value)
	{
		this._limitCloseOrderSummary = value;
	}

	public String get_StopCloseOrderSummary()
	{
		return this._stopCloseOrderSummary;
	}

	static class OpenOrderAddingListener implements IObjectAddingListener
	{
		private Order _owner;

		public OpenOrderAddingListener(Order owner)
		{
			this._owner = owner;
		}

		public boolean canAddObject(Object item)
		{
			Order closeOrder = item instanceof CloseOrder ? ( (CloseOrder)item).get_Order() : (Order)item;
			OrderType orderType = closeOrder.get_Transaction().get_OrderType();
			if (orderType == OrderType.Limit || orderType == OrderType.MarketOnClose
				|| orderType == OrderType.MarketOnOpen || orderType == OrderType.OneCancelOther)
			{
				HashMap<Guid, RelationOrder> relationOrders = closeOrder.get_RelationOrders();
				if (relationOrders != null && relationOrders.size() > 0)
				{
					for (RelationOrder relationOrder : relationOrders.values())
					{
						if (relationOrder.get_OpenOrder() == this._owner)
						{
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	private void createCloseOrders()
	{
		this._closeOrders = new BindingSource();
		IObjectUpdatedListener objectUpdatedListener = new IObjectUpdatedListener()
		{
			public void ObjectUpdated(Object object)
			{
				Order order = ( (CloseOrder)object).get_Order();
				if (order._phase.equals(Phase.Cancelled) || order._phase.equals(Phase.Executed))
				{
					_tradingConsole.get_MainForm().get_OpenOrderTable().collapseAllRows();
					if (CloseOrder.CloseOrderHelper.getCreatedCloseOrders(order) != null)
					{
						for (CloseOrder closeOrder : CloseOrder.CloseOrderHelper.getCreatedCloseOrders(order))
						{
							_closeOrders.remove(closeOrder);
						}
					}
				}
			}
		};
		this._closeOrders.addObjectUpdatedListener(objectUpdatedListener);

		OpenOrderAddingListener objectAddingListener = new OpenOrderAddingListener(this);
		this._closeOrders.addAddingObjectListener(objectAddingListener);

		TradingConsole.bindingManager.bind(Order.workingRelationOrdersKey, new Vector(0), this._closeOrders,
										   CloseOrder.getPropertyDescriptorsForWorkingOrderList(this._settingsManager));

		/*IObjectRemovedListener objectRemovedListener = new IObjectRemovedListener()
		   {
		 public void removed(BindingSource source, Object removedObject)
		 {
		  refreshCloseOrderSummary(TradeOption.Stop);
		  refreshCloseOrderSummary(TradeOption.Better);
		  _tradingConsole.get_MainForm().get_OpenOrderTable().collapseAllRows(); //fix bug of jide
		 }
		   };
		   this._closeOrders.addObjectRemovedListener(objectRemovedListener);*/

		TableModelListener tableModelListener = new TableModelListener()
		{
			public void tableChanged(TableModelEvent e)
			{
				if (e.getType() == TableModelEvent.DELETE || e.getType() == TableModelEvent.INSERT)
				{
					TradingConsole.traceSource.trace(TraceType.Information, "event type = " + (e.getType() == TableModelEvent.DELETE ? "delete" : "add"));
					refreshCloseOrderSummary(TradeOption.Stop);
					refreshCloseOrderSummary(TradeOption.Better);
					_tradingConsole.get_MainForm().get_OpenOrderTable().collapseAllRows(); //fix bug of jide
				}
			}
		};
		this._closeOrders.addTableModelListener(tableModelListener);
	}

	public void addCloseOrder(Order order)
	{
		if (!this.inCloseOrders(order))
		{
			BigDecimal closeLot = this.getCloseLot(order);
			CloseOrder closeOrder = CloseOrder.CloseOrderHelper.createCloseOrder(order, closeLot);
			this._closeOrders.add(closeOrder);
			order.orderStyleWorkingOrderList();
			if (order.isOffLineMakeOrder())
			{
				order.setBackground(Order.workingOrdersKey, GridBackgroundColor.workingOrderListForOfflineMakeOrder);
			}
		}
	}

	private boolean inCloseOrders(Order order)
	{
		for (int index = 0; index < this._closeOrders.getRowCount(); index++)
		{
			CloseOrder closeOrder = (CloseOrder)this._closeOrders.getObject(index);
			if (closeOrder.get_Order() == order)
			{
				return true;
			}
		}
		return false;
	}

	private BigDecimal getCloseLot(Order closeOrder)
	{
		for (RelationOrder relationOrder : closeOrder.get_RelationOrders().values())
		{
			if (relationOrder.get_OpenOrder() == this)
			{
				return relationOrder.get_LiqLot();
			}
		}
		return BigDecimal.ZERO;
	}

	private void refreshCloseOrderSummary(TradeOption tradeOption)
	{
		String summary = "";
		TradingConsole.traceSource.trace(TraceType.Information, "refreshCloseOrderSummary with tradeOption = " + tradeOption.name());

		if (this._closeOrders != null && this._closeOrders.getRowCount() > 0)
		{
			BigDecimal totalCloseLot = BigDecimal.ZERO;
			BigDecimal totalPrice = BigDecimal.ZERO;
			for (int index = 0; index < this._closeOrders.getRowCount(); index++)
			{
				Order closeOrder = ( (CloseOrder)this._closeOrders.getObject(index)).get_Order();
				TradingConsole.traceSource.trace(TraceType.Information, "closeOrder :" + closeOrder
												 + closeOrder.get_LotString() + " X " + closeOrder.get_SetPriceString()
												 + " tradeOption = " + closeOrder.get_TradeOption().name());
				if (closeOrder.get_TradeOption() == tradeOption)
				{
					BigDecimal closeLot = getCloseLot(closeOrder);
					totalCloseLot = totalCloseLot.add(closeLot);
					BigDecimal price = Price.toBigDecimal(closeOrder.get_SetPrice());
					totalPrice = totalPrice.add(price.multiply(closeLot));
				}
			}

			TradingConsole.traceSource.trace(TraceType.Information, "totalCloseLot = " + totalCloseLot.toString());

			if (!totalCloseLot.equals(BigDecimal.ZERO))
			{
				double avaragePrice = 0;
				try
				{
					avaragePrice = totalPrice.divide(totalCloseLot).doubleValue();
				}
				catch (ArithmeticException exception)
				{
					avaragePrice = totalPrice.doubleValue() / totalCloseLot.doubleValue();
				}
				Price avaragePrice2 = Price.create(avaragePrice,
					this._transaction.get_Instrument().get_NumeratorUnit(),
					this._transaction.get_Instrument().get_Denominator());
				summary = AppToolkit.getFormatLot(totalCloseLot, this.get_Account(), this.get_Instrument()) + " x " + Price.toString(avaragePrice2);
			}
		}

		if (tradeOption == TradeOption.Better)
		{
			this._limitCloseOrderSummary = summary;
			TradingConsole.bindingManager.update(Order.openOrdersKey, this, OpenOrderColKey.LimitCloseOrderSummary, summary);
		}
		else if (tradeOption == TradeOption.Stop)
		{
			this._stopCloseOrderSummary = summary;
			TradingConsole.bindingManager.update(Order.openOrdersKey, this, OpenOrderColKey.StopCloseOrderSummary, summary);
		}
	}

	public String get_PeerOrderCodes()
	{
		return this._peerOrderCodes;
	}

	public void set_PeerOrderCodes(String value)
	{
		this._peerOrderCodes = value;
	}

	//temp????????????????
	public String[] getPeerOrders()
	{
		String[] peerOrders = new String[]
			{"", ""};
		for (Iterator<RelationOrder> iterator = this._relationOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			String[] peerOrders2 = relationOrder.getPeerOrders();
			peerOrders[0] = (!StringHelper.isNullOrEmpty(peerOrders[0])) ? peerOrders[0] + TradingConsole.delimiterRow : "";
			peerOrders[0] += peerOrders2[0].toString();
			peerOrders[1] = (!StringHelper.isNullOrEmpty(peerOrders[1])) ? peerOrders[1] + TradingConsole.enterLine : "";
			peerOrders[1] += peerOrders2[1].toString();
		}

		return peerOrders;
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public String get_Code()
	{
		return this._code;
	}

	public void set_Code(String code)
	{
		this._code = code;
	}


	public double get_Margin()
	{
		return this._margin;
	}

	public DateTime get_EndTime()
	{
		return this._transaction.get_EndTime();
	}

	public String get_ShortCode()
	{
		if (StringHelper.isNullOrEmpty(this._code))
		{
			return "";
		}
		return this._code.substring(3);
	}

	public String get_RefCode()
	{
		return (StringHelper.isNullOrEmpty(this._code)) ? this._transaction.get_Code() : this._code;
	}

	public BigDecimal get_InterestRate()
	{
		return this._interestRate;
	}

	public void set_InterestRate(BigDecimal value)
	{
		this._interestRate = value;
	}

	public String get_InterestRateString()
	{
		if (this._interestRate == null)
		{
			return "";
		}
		return AppToolkit.format(this._interestRate.doubleValue(), 2);
	}

	public Phase get_Phase()
	{
		return this._phase;
	}

	public String get_PhaseString()
	{
		if (this._transaction.isInModification())
		{
			return Language.CancellingForModification;
		}
		else if (this._phase.equals(Phase.Cancelled) && this._isRejectDQByDealer)
		{
			return Language.PhaseCancelledPromptForRejectDQByDealer;
		}
		else
		{
			if (this._phase.value() == Phase.Placing.value() && !this._isOpen
				&& this._transaction.get_SubType().value() == TransactionSubType.IfDone.value())
			{
				return Language.PhasePlacedPlacingPromptForDoneOrder;
			}
			if (this._phase.value() == Phase.Placed.value())
			{
				if (this._transaction.get_OrderType().value() == OrderType.Market.value()
					|| this._transaction.get_OrderType().value() == OrderType.SpotTrade.value())
				{
					return Language.PhasePlacedUnconfirmedPrompt2;
				}
				else
				{
					return Language.PhasePlacedUnconfirmedPrompt;
				}
			}
			return Phase.getCaption(this._phase);
		}
	}

	public String get_SubmitTime()
	{
		return XmlConvert.toString(this._transaction.get_SubmitTime().addMinutes(Parameter.timeZoneMinuteSpan), "yyyy-MM-dd HH:mm:ss");
	}

	public String get_SubmitDate()
	{
		return XmlConvert.toString(this._transaction.get_SubmitTime().addMinutes(Parameter.timeZoneMinuteSpan), "yyyy-MM-dd");
	}

	public Account get_Account()
	{
		return this._transaction.get_Account();
	}

	public Instrument get_Instrument()
	{
		return this._transaction.get_Instrument();
	}

	public String get_AccountCode()
	{
		if (this._transaction.get_Account().get_TradingConsole().get_SettingsManager().get_SystemParameter().get_ShowAccountName())
		{
			return this._transaction.get_Account().get_Code() + " (" + this._transaction.get_Account().get_Name() + ")";
		}
		else
		{
			return this._transaction.get_Account().get_Code();
		}
	}

	public String get_InstrumentCode()
	{
		return this._transaction.get_Instrument().get_Description();
	}

	public String get_OrderTypeString()
	{
		OrderType orderType = this._transaction.get_OrderType();
		//special process
		if (orderType == OrderType.Limit)
		{
			if (this._transaction.get_Type() == TransactionType.OneCancelOther)
			{
				if (this._tradeOption == TradeOption.Better)
				{
					return Language.OCOPrompt + "(" + Language.LMTPrompt + ")";
				}
				else if (this._tradeOption == TradeOption.Stop)
				{
					return Language.OCOPrompt + "(" + Language.STPPrompt + ")";
				}
				else
				{
					return Language.OCOPrompt;
				}
			}
			else if (this._transaction.get_SubType() == TransactionSubType.Match)
			{
				return Language.MatchPrompt;
			}
			else
			{
				if (this._tradeOption == TradeOption.Better)
				{
					return Language.LMTPrompt;
				}
				else
				{
					return Language.STPPrompt;
				}
			}
		}
		else if (orderType == OrderType.OneCancelOther)
		{
			if (this._tradeOption == TradeOption.Better)
			{
				return Language.OCOPrompt + "(" + Language.LMTPrompt + ")";
			}
			else if (this._tradeOption == TradeOption.Stop)
			{
				return Language.OCOPrompt + "(" + Language.STPPrompt + ")";
			}
			else
			{
				return Language.OCOPrompt;
			}
		}
		else
		{
			return OrderType.getCaption(orderType);
		}
	}

	public String get_LotString()
	{
		return AppToolkit.getFormatLot(this._lot, this.get_Account(), this.get_Instrument());
	}

	public BigDecimal get_Lot()
	{
		return this._lot;
	}

	public boolean get_IsOpen()
	{
		return this._isOpen;
	}

	public String get_IsOpenString()
	{
		return (this._isOpen) ? Language.Open : Language.Close;
	}

	public boolean get_IsBuy()
	{
		return this._isBuy;
	}

	public String get_IsBuyString()
	{
		return (this._isBuy) ? Language.Buy : Language.Sell;
	}

	public String get_IsBuyLongString()
	{
		return (this._isBuy) ? Language.LongBuy : Language.LongSell;
	}

	public String get_SetPriceString()
	{
		String priceString = Price.toString(this._executePrice);
		return (StringHelper.isNullOrEmpty(priceString)) ? Price.toString(this._setPrice) : priceString;
	}

	public String get_SetPrice2String()
	{
		return Price.toString(this._setPrice2);
	}

	public String get_AutoLimitPriceString()
	{
		return Price.toString(this._autoLimitPrice);
	}

	public void set_AutoLimitPriceString(String value)
	{
		Instrument instrument = this._transaction.get_Instrument();
		this._autoLimitPrice = Price.create(value, instrument.get_NumeratorUnit(), instrument.get_Denominator());
	}

	public String get_AutoStopPriceString()
	{
		return Price.toString(this._autoStopPrice);
	}

	public void set_AutoStopPriceString(String value)
	{
		Instrument instrument = this._transaction.get_Instrument();
		this._autoStopPrice = Price.create(value, instrument.get_NumeratorUnit(), instrument.get_Denominator());
	}

	public BigDecimal get_CommissionSum()
	{
		return this._commissionSum;
	}

	public String get_TradePLString()
	{
		return AppToolkit.format(this._pLTradingItem.get_Trade(), this._displayDecimals);
	}

	public String get_CommissionSumString()
	{
		return AppToolkit.format(this._commissionSum.doubleValue(), this._displayDecimals);
	}

	public BigDecimal get_LevySum()
	{
		return this._levySum;
	}

	public String get_LevySumString()
	{
		return AppToolkit.format(this._levySum.doubleValue(), this._displayDecimals);
	}

	public Transaction get_Transaction()
	{
		return this._transaction;
	}

	//OpenOrderList use
	public boolean get_Close()
	{
		return this._close;
	}

	public void set_Close(boolean value)
	{
		this._close = value;
	}

	public String get_LotBalanceString()
	{
		return AppToolkit.getFormatLot(this._lotBalance, this.get_Account(), this.get_Instrument());
	}

	public BigDecimal get_LotBalance()
	{
		return this._lotBalance;
	}

	public BigDecimal getAvailableLotBanlance(boolean isSpotTrade, Boolean isMakeLimitOrder)
	{
		if (isSpotTrade)
		{
			return this._lotBalance;
		}
		else
		{
			BigDecimal pendingCloseLot = BigDecimal.ZERO;
			BigDecimal pendingLimitCloseLot = BigDecimal.ZERO;
			BigDecimal pendingStopCloseLot = BigDecimal.ZERO;

			for (int index = 0; index < this._closeOrders.getRowCount(); index++)
			{
				Order closeOrder = ( (CloseOrder)this._closeOrders.getObject(index)).get_Order();
				if (!closeOrder.get_Transaction().get_OrderType().isSpot()
					&& (closeOrder.get_Phase() == Phase.Placed || closeOrder.get_Phase() == Phase.Placing))
				{
					BigDecimal closeLot = this.getCloseLot(closeOrder);
					if (isMakeLimitOrder != null)
					{
						if((isMakeLimitOrder && isTheSpecialLimitOrder(closeOrder, TradeOption.Better))
							|| (!isMakeLimitOrder && isTheSpecialLimitOrder(closeOrder, TradeOption.Stop)))
						{
							pendingCloseLot = pendingCloseLot.add(closeLot);
						}
					}
					else
					{
						if(isTheSpecialLimitOrder(closeOrder, TradeOption.Better))
						{
							pendingLimitCloseLot = pendingLimitCloseLot.add(closeLot);
						}

						if(isTheSpecialLimitOrder(closeOrder, TradeOption.Stop))
						{
							pendingStopCloseLot = pendingStopCloseLot.add(closeLot);
						}
					}
				}
			}

			if (isMakeLimitOrder == null) pendingCloseLot
				= pendingLimitCloseLot.compareTo(pendingStopCloseLot) > 0 ? pendingLimitCloseLot : pendingStopCloseLot;

			return this._lotBalance.subtract(pendingCloseLot);
		}
	}

	private static boolean isTheSpecialLimitOrder(Order order, TradeOption option)
	{
		if(order._transaction.get_OrderType() == OrderType.Limit)
		{
			return order.get_TradeOption().value() == option.value();
		}

		return false;
	}

	public double get_Commission()
	{
		return this.calcCommission();
	}

	public String get_CommissionString()
	{
		return AppToolkit.format(this.calcCommission(), this._displayDecimals);
	}

	public String get_LivePriceString()
	{
		return Price.toString(this._livePrice);
	}

	/*public static String getExecuteTradeDay(String code)
	{
		if (StringHelper.isNullOrEmpty(code))
		{
			return "";
		}
		return (code.substring(3, 7) + "-" + code.substring(7, 9) + "-" + code.substring(9, 11));
	}*/

	public String get_ExecuteTradeDay()
	{
		return this._executeTradeDay == null ? "" : this._executeTradeDay.toString("yyyy-MM-dd");
	}

	public Price get_ExecutePrice()
	{
		return this._executePrice;
	}

	public String get_ExecutePriceString()
	{
		return Price.toString(this._executePrice);
	}

	public TradingItem get_FloatTradingItem()
	{
		return this._floatTradingItem;
	}

	public TradingItem get_NotValuedTradingItem()
	{
		return this._notValuedTradingItem;
	}

	public double get_TradePLFloat()
	{
		return this._floatTradingItem.get_Trade();
	}

	public String get_TradePLFloatString()
	{
		return AppToolkit.format(this._floatTradingItem.get_Trade(), this._displayDecimals);
	}

	public double get_InterestPLFloat()
	{
		return this._floatTradingItem.get_Interest();
	}

	public short get_DisplayDecimals()
	{
		return this._displayDecimals;
	}

	public String get_InterestPLFloatString()
	{
		return AppToolkit.format(this._floatTradingItem.get_Interest(), this._displayDecimals);
	}

	public double get_StoragePLFloat()
	{
		return this._floatTradingItem.get_Storage();
	}

	public String get_StoragePLFloatString()
	{
		return AppToolkit.format(this._floatTradingItem.get_Storage(), this._displayDecimals);
	}

	public String get_UnrealisedSwapString()
	{
		return AppToolkit.format(this._PLNotValued.get_Storage() + this._PLNotValued.get_Interest(), this._displayDecimals);
	}

	public TradingItem get_PLTradingItem()
	{
		return this._pLTradingItem;
	}

	public void set_PLTradingItem(TradingItem value)
	{
		this._pLTradingItem = value;
	}

	public HashMap<Guid, RelationOrder> get_RelationOrders()
	{
		return this._relationOrders;
	}

	public String getRelationString()
	{
		String relationString = "";
		for (Iterator<RelationOrder> iterator = this._relationOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			relationString += ( (!StringHelper.isNullOrEmpty(relationString)) ? TradingConsole.enterLine : "") + relationOrder.toString();
		}
		return (StringHelper.isNullOrEmpty(relationString)) ? this._peerOrderCodes : relationString;
	}

	public void set_LotBalance(BigDecimal value)
	{
		if (this._lotBalance.compareTo(value) != 0)
		{
			this._lotBalance = value;
			RelationOrder.get_OrderStateReceiver().lotBanlanceChanged(this);

			Guid instrumentId = this.get_Transaction().get_Instrument().get_Id();
			if (this._settingsManager.containsOpenContractFormOf(instrumentId))
			{
				OpenContractForm openContractForm = this._settingsManager.getOpenContractFormOf(instrumentId);
				if (openContractForm.get_Order().get_Id().equals(this._id))
				{
					openContractForm.rebind();
				}
			}
			this._tradingConsole.refreshSummary();
		}
	}

	public void set_IsAssignOrder(boolean value)
	{
		this._isAssignOrder = value;
	}

	public boolean get_IsAssignOrder()
	{
		return this._isAssignOrder;
	}

	public int get_Sequence()
	{
		return this._sequence;
	}

	public void set_Sequence(int value)
	{
		this._sequence = value;
	}

	public int get_DQMaxMove()
	{
		return this._dQMaxMove;
	}

	public void set_DQMaxMove(int value)
	{
		this._dQMaxMove = value;
	}

	public Order(TradingConsole tradingConsole, SettingsManager settingsManager)
	{
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;

		//this._verificationOutstandingKey = Guid.newGuid().toString();
		this._bindingSourceForVerificationOutstanding = new tradingConsole.ui.grid.BindingSource();
		this._relationOrders = new HashMap<Guid, RelationOrder> ();
		this._phase = Phase.Placing;
		this._isRejectDQByDealer = false;
		this._tradeOption = TradeOption.None;
		this._sequence = 0;

		this._displayDecimals = Short.MIN_VALUE;
		this._pLTradingItem = TradingItem.create(0.00, 0.00, 0.00);
		this._floatTradingItem = TradingItem.create(0.00, 0.00, 0.00);
		this._notValuedTradingItem = TradingItem.create(0.00, 0.00, 0.00);
		this._isAssignOrder = false;

		this._code = "";
		this._executePrice = null;
		this._interestRate = null;
		this._commissionSum = BigDecimal.ZERO;
		this._levySum = BigDecimal.ZERO;
		this._peerOrderCodes = "";
		this._peerOrderIDs = "";
		this._interestPerLot = BigDecimal.ZERO;
		this._storagePerLot = BigDecimal.ZERO;

		this.createCloseOrders();
	}

	public Order(TradingConsole tradingConsole, SettingsManager settingsManager, DataRow dataRow, Transaction transaction)
	{
		this(tradingConsole, settingsManager, dataRow, transaction, false);
	}

	public Order(TradingConsole tradingConsole, SettingsManager settingsManager, DataRow dataRow, Transaction transaction, boolean calculatePLFloat)
	{
		this(tradingConsole, settingsManager);

		this._id = (Guid)dataRow.get_Item("ID");
		this._transaction = transaction;
		this._displayDecimals = this.getDisplayDecimals();
		this.setValue(dataRow);

		//Modified by Michael on 2008-04-22
		//this._livePrice = this._transaction.get_Instrument().get_LastQuotation().getBuySell(!this._isBuy);
		Price price = this._transaction.get_Instrument().get_LastQuotation().getBuySell(!this._isBuy);
		if (price != null)
		{
			this._livePrice = price;
			if (calculatePLFloat && this.get_Phase().equals(Phase.Executed) && this.get_IsOpen()
				&& this.get_LotBalance().compareTo(BigDecimal.ZERO) > 0)
			{
				this.calculatePLFloat(price, price);
			}
		}

		//maybe has error...........
		this._phase = this._transaction.get_Phase();
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	public Order getFirstOpenOrder()
	{
		if (this._relationOrders.size() > 0)
		{
			Guid openOrderId = this._relationOrders.keySet().iterator().next();
			return this._tradingConsole.getOrder(openOrderId);
		}
		else
		{
			return null;
		}
	}

	private void setValue(DataRow dataRow)
	{
		this._code = (AppToolkit.isDBNull(dataRow.get_Item("Code"))) ? null : (String)dataRow.get_Item("Code");
		this._lot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("Lot"), 0.0);
		this._lotBalance = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("LotBalance"), 0.0);
		this._isOpen = (Boolean)dataRow.get_Item("IsOpen");
		this._isBuy = (Boolean)dataRow.get_Item("IsBuy");
		//before call you should init transaction & Instrument
		int numeratorUnit = this._transaction.get_Instrument().get_NumeratorUnit();
		int denominator = this._transaction.get_Instrument().get_Denominator();
		this._setPrice = (AppToolkit.isDBNull(dataRow.get_Item("SetPrice"))) ? null :
			Price.parse(dataRow.get_Item("SetPrice").toString(), numeratorUnit, denominator);

		if (dataRow.get_Table().get_Columns().contains("SetPrice2"))
		{
			this._setPrice2 = (AppToolkit.isDBNull(dataRow.get_Item("SetPrice2"))) ? null :
				Price.parse(dataRow.get_Item("SetPrice2").toString(), numeratorUnit, denominator);
		}

		if (dataRow.get_Table().get_Columns().contains("AutoLimitPriceString"))
		{
			this._autoLimitPrice = (AppToolkit.isDBNull(dataRow.get_Item("AutoLimitPriceString"))) ? null :
				Price.parse(dataRow.get_Item("AutoLimitPriceString").toString(), numeratorUnit, denominator);
		}

		if (dataRow.get_Table().get_Columns().contains("AutoStopPriceString"))
		{
			this._autoStopPrice = (AppToolkit.isDBNull(dataRow.get_Item("AutoStopPriceString"))) ? null :
				Price.parse(dataRow.get_Item("AutoStopPriceString").toString(), numeratorUnit, denominator);
		}

		this._executePrice = (AppToolkit.isDBNull(dataRow.get_Item("ExecutePrice"))) ? null : Price.parse(dataRow.get_Item("ExecutePrice").toString(),
			numeratorUnit, denominator);
		if (dataRow.get_Table().get_Columns().contains("ExecuteTradeDay"))
		{
			this._executeTradeDay = (AppToolkit.isDBNull(dataRow.get_Item("ExecuteTradeDay"))) ? null : (DateTime)dataRow.get_Item("ExecuteTradeDay");
		}
		this._tradeOption = Enum.valueOf(TradeOption.class, ( (Short)dataRow.get_Item("TradeOption")).intValue());
		this._commissionSum = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("CommissionSum"), 0.0);
		this._levySum = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("LevySum"), 0.0);
		this._interestPerLot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("InterestPerLot"), 0.0);
		this._storagePerLot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("StoragePerLot"), 0.0);
		this._interestRate = (AppToolkit.isDBNull(dataRow.get_Item("InterestRate")) ? null : (BigDecimal) (dataRow.get_Item("InterestRate")));

		this._peerOrderCodes = (String)dataRow.get_Item("PeerOrderCodes");
		if (dataRow.get_Table().get_Columns().contains("TradePL"))
		{
			this._pLTradingItem.set_Trade(AppToolkit.convertDBValueToDouble(dataRow.get_Item("TradePL"), 0.0));
		}
		if (dataRow.get_Table().get_Columns().contains("Necessary"))
		{
			this._margin = AppToolkit.round(AppToolkit.convertDBValueToDouble(dataRow.get_Item("Necessary"), 0.0), this._displayDecimals);
		}
		this._floatTradingItem.set_Trade(AppToolkit.round(AppToolkit.convertDBValueToDouble(dataRow.get_Item("TradePLFloat"), 0.0), this._displayDecimals));
		this._floatTradingItem.set_Interest(AppToolkit.round(AppToolkit.convertDBValueToDouble(dataRow.get_Item("InterestPLFloat"), 0.0), this._displayDecimals));
		this._floatTradingItem.set_Storage(AppToolkit.round(AppToolkit.convertDBValueToDouble(dataRow.get_Item("StoragePLFloat"), 0.0), this._displayDecimals));

		if (dataRow.get_Table().get_Columns().contains("InterestPLNotValued"))
		{
			this._notValuedTradingItem.set_Interest(AppToolkit.round(AppToolkit.convertDBValueToDouble(dataRow.get_Item("InterestPLNotValued"), 0.0),
				this._displayDecimals));
		}

		if (dataRow.get_Table().get_Columns().contains("StoragePLNotValued"))
		{
			this._notValuedTradingItem.set_Storage(AppToolkit.round(AppToolkit.convertDBValueToDouble(dataRow.get_Item("StoragePLNotValued"), 0.0),
				this._displayDecimals));
		}

		this._dQMaxMove = (AppToolkit.isDBNull(dataRow.get_Item("DQMaxMove")) ? 0 : (Integer) (dataRow.get_Item("DQMaxMove")));

		//Added by Michael on 2008-04-22
		if (!AppToolkit.isDBNull(dataRow.get_Item("LivePrice")))
		{
			this._livePrice = Price.parse(dataRow.get_Item("LivePrice").toString(), numeratorUnit, denominator);
		}
	}

	public void setValue(XmlAttributeCollection orderCollection, Transaction transaction)
	{
		this._transaction = transaction;
		int numeratorUnit = this._transaction.get_Instrument().get_NumeratorUnit();
		int denominator = this._transaction.get_Instrument().get_Denominator();
		this._displayDecimals = this.getDisplayDecimals();

		for (int i = 0; i < orderCollection.get_Count(); i++)
		{
			String nodeName = orderCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = orderCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("ID"))
			{
				this._id = new Guid(nodeValue);
			}
			else if (nodeName.equals("Code"))
			{
				this._code = nodeValue;
			}
			else if (nodeName.equals("Lot"))
			{
				this._lot = AppToolkit.convertStringToBigDecimal(nodeValue);
			}
			else if (nodeName.equals("LotBalance"))
			{
				this._lotBalance = AppToolkit.convertStringToBigDecimal(nodeValue);
			}
			//else if (nodeName.equals("Phase"))
			//{
			//	this._phase = Phase.number(Integer.parseInt(nodeValue));
			//}
			else if (nodeName.equals("IsOpen"))
			{
				this._isOpen = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("IsBuy"))
			{
				this._isBuy = Boolean.valueOf(nodeValue);
			}
			else if (nodeName.equals("SetPrice"))
			{
				this._setPrice = Price.parse(nodeValue, numeratorUnit, denominator);
			}
			else if (nodeName.equals("SetPrice2"))
			{
				this._setPrice2 = Price.parse(nodeValue, numeratorUnit, denominator);
			}
			else if (nodeName.equals("AutoLimitPrice"))
			{
				this._autoLimitPrice = Price.parse(nodeValue, numeratorUnit, denominator);
			}
			else if (nodeName.equals("AutoStopPrice"))
			{
				this._autoStopPrice = Price.parse(nodeValue, numeratorUnit, denominator);
			}
			else if (nodeName.equals("ExecutePrice"))
			{
				this._executePrice = Price.parse(nodeValue, numeratorUnit, denominator);
			}
			else if (nodeName.equals("ExecuteTradeDay"))
			{
				this._executeTradeDay = DateTime.valueOf(nodeValue);
			}
			else if (nodeName.equals("LivePrice"))
			{
				//maybe has error ????????????????????????
				this._livePrice = Price.parse(nodeValue, numeratorUnit, denominator);
				//this._livePrice = this._transaction.get_Instrument().get_LastQuotation().getBuySell(this._isBuy);
			}
			else if (nodeName.equals("TradeOption"))
			{
				this._tradeOption = Enum.valueOf(TradeOption.class, Integer.parseInt(nodeValue));
			}
			else if (nodeName.equals("CommissionSum"))
			{
				this._commissionSum = new BigDecimal(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("LevySum"))
			{
				this._levySum = new BigDecimal(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("InterestPerLot"))
			{
				this._interestPerLot = new BigDecimal(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("StoragePerLot"))
			{
				this._storagePerLot = new BigDecimal(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("InterestRate"))
			{
				this._interestRate = new BigDecimal(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("PeerOrderIDs"))
			{
				this._peerOrderIDs = nodeValue;
			}
			else if (nodeName.equals("PeerOrderCodes"))
			{
				this._peerOrderCodes = nodeValue;
			}
			else if (nodeName.equals("TradePLFloat"))
			{
				this._floatTradingItem.set_Trade(AppToolkit.round(Double.valueOf(nodeValue).doubleValue(), this._displayDecimals));
			}
			else if (nodeName.equals("InterestPLFloat"))
			{
				this._floatTradingItem.set_Interest(AppToolkit.round(Double.valueOf(nodeValue).doubleValue(), this._displayDecimals));
			}
			else if (nodeName.equals("StoragePLFloat"))
			{
				this._floatTradingItem.set_Storage(AppToolkit.round(Double.valueOf(nodeValue).doubleValue(), this._displayDecimals));
			}
			else if (nodeName.equals("TradePL"))
			{
				this._pLTradingItem.set_Trade(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("InterestPL"))
			{
				this._pLTradingItem.set_Interest(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("StoragePL"))
			{
				this._pLTradingItem.set_Storage(Double.valueOf(nodeValue).doubleValue());
			}
			else if (nodeName.equals("DQMaxMove"))
			{
				this._dQMaxMove = (Integer.valueOf(nodeValue).intValue());
			}

			this._tradingConsole.refreshSummary();
		}

		//maybe has error...........
		//Modified by Michael on 2008-04-22
		//this._livePrice = this._transaction.get_Instrument().get_LastQuotation().getBuySell(!this._isBuy);
		Price price = this._transaction.get_Instrument().get_LastQuotation().getBuySell(!this._isBuy);
		if (price != null)
		{
			this._livePrice = price;
		}

		//maybe has error...........
		this._phase = this._transaction.get_Phase();
		if (this._phase == Phase.Executed)
		{
			this._message = "";
		}
	}

	public static void rebindWorkingOrderList(SettingsManager settingsManager, tradingConsole.ui.grid.DockableTable grid, String dataSourceKey,
											  Collection dataSource,
											  tradingConsole.ui.grid.BindingSource bindingSource)
	{
		Order.unbind(dataSourceKey, bindingSource);
		Order.initializeWorkingOrderList(settingsManager, grid, dataSourceKey, dataSource, bindingSource);
		for (int i = 0, count = bindingSource.getRowCount(); i < count; i++)
		{
			Order order = (Order)bindingSource.getObject(i);
			order.orderStyleWorkingOrderList();
			order.setBackground(Order.workingOrdersKey, GridBackgroundColor.workingOrderList);
		}
	}

	public static void initializeWorkingOrderList(SettingsManager settingsManager, tradingConsole.ui.grid.DataGrid grid, String dataSourceKey,
												  Collection dataSource,
												  tradingConsole.ui.grid.BindingSource bindingSource)
	{
		initializeWorkingOrderList(settingsManager, grid, dataSourceKey, dataSource, bindingSource, false);
	}

	//WorkingOrderList useinitializeOpenOrderList
	public static void initializeWorkingOrderList(SettingsManager settingsManager, tradingConsole.ui.grid.DataGrid grid, String dataSourceKey,
												  Collection dataSource,
												  tradingConsole.ui.grid.BindingSource bindingSource, boolean filterExecuted)
	{
		if(grid instanceof tradingConsole.ui.grid.DockableTable)
		{
			tradingConsole.ui.grid.DockableTable table = (tradingConsole.ui.grid.DockableTable)grid;
			table.reset();
			table.setLabel("");
			table.setVertGridLines(false);
			table.setHorzGridLines(false);
			table.setBackColor(GridFixedBackColor.workingOrderList);
			table.setTableColor(GridBackColor.workingOrderList);
			table.setBorderStyle(BorderStyle.workingOrderList);
			table.setRowLabelWidth(RowLabelWidth.workingOrderList);
			//table.setSelectionBackground(SelectionBackground.workingOrderList);
			table.setCurrentCellColor(CurrentCellColor.workingOrderList);
			table.setCurrentCellBorder(CurrentCellBorder.workingOrderList);
		}
		else
		{
			grid.setShowVerticalLines(false);
			grid.setShowHorizontalLines(false);
			grid.setBackground(GridFixedBackColor.workingOrderList);
			grid.setForeground(GridBackColor.workingOrderList);
		}
		grid.setShowGrid(false);

		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, Order.getPropertyDescriptorsForWorkingOrderList(settingsManager));
		if(grid instanceof tradingConsole.ui.grid.DockableTable)
		{
			tradingConsole.ui.grid.DockableTable table = (tradingConsole.ui.grid.DockableTable)grid;
			table.setDataModel(bindingSource);
		}
		else
		{
			grid.setModel(bindingSource);
		}
		UISetting uiSetting = settingsManager.getUISetting(UISetting.workingOrderListUiSetting);

		TradingConsole.bindingManager.setGrid(dataSourceKey, uiSetting.get_RowHeight(),
											  ColorSettings.useBlackAsBackground ? ColorSettings.GridForeground : Color.black,
											  ColorSettings.useBlackAsBackground ? ColorSettings.TradingListGridBackground : Color.white, Color.blue, true, true,
											  new Font(uiSetting.get_FontName(), Font.BOLD, uiSetting.get_FontSize()), false, true, true);
		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.workingOrderList, Color.white,
												HeaderFont.workingOrderList);

		grid.addFilter(new AccountInstrumentFilter(grid.get_BindingSource(), filterExecuted));
		grid.sortColumn(OrderColKey.SubmitTime, true, false);

		grid.clearFilterColumns();
		grid.enableFilter(OrderColKey.OrderTypeString);
		grid.enableFilter(OrderColKey.AccountCode);
		grid.enableFilter(OrderColKey.InstrumentCode);
		grid.enableFilter(OrderColKey.IsBuyString);
		grid.enableFilter(OrderColKey.IsOpenString);
		grid.enableFilter(OrderColKey.PhaseString);
		grid.enableFilter(OrderColKey.LotString);
	}

	//WorkingOrderList use
	public static PropertyDescriptor[] getPropertyDescriptorsForWorkingOrderList(SettingsManager settingsManager)
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[13];

		UISetting uiSetting = settingsManager.getUISetting(UISetting.workingOrderListUiSetting);
		HashMap<String, UISetting2> uiSetting2s = uiSetting.get_UiSetting2s();
		UISetting2 uiSetting2;

		uiSetting2 = uiSetting2s.get(OrderColKey.PhaseString);
		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.PhaseString, true, null, OrderLanguage.PhaseString,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.SubmitTime);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.SubmitTime, true, null, OrderLanguage.SubmitTime, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.EndTime);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.EndTime, true, null, OrderLanguage.EndTime, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.AccountCode);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.AccountCode, true, null, OrderLanguage.AccountCode, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.InstrumentCode);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.InstrumentCode, true, null, OrderLanguage.InstrumentCode,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.LotString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.LotString, true, null, OrderLanguage.LotString, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.IsOpenString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.IsOpenString, true, null, OrderLanguage.IsOpenString, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.IsBuyString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.IsBuyString, true, null, OrderLanguage.IsBuyString, uiSetting2.get_ColWidth(),
			SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.SetPriceString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.SetPriceString, true, null, OrderLanguage.SetPriceString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.OrderTypeString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.OrderTypeString, true, null, OrderLanguage.OrderTypeString,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.TradePLString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.TradePLString, true, null, OrderLanguage.TradePLString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.CommissionSumString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.CommissionSumString, true, null, OrderLanguage.CommissionSumString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OrderColKey.LevySumString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OrderColKey.LevySumString, true, null, OrderLanguage.LevySumString, uiSetting2.get_ColWidth(),
			SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		ArrayList<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor> ();
		for (PropertyDescriptor item : propertyDescriptors)
		{
			if (Parameter.isShouldShowColumn("WorkingOrderList", item.get_Name()))
			{
				propertyDescriptorList.add(item);
			}
		}
		if (propertyDescriptorList.size() != propertyDescriptors.length)
		{
			propertyDescriptors = new PropertyDescriptor[propertyDescriptorList.size()];
			propertyDescriptors = propertyDescriptorList.toArray(propertyDescriptors);
		}

		return ColumnUIInfoManager.applyUIInfo(GridNames.OrderGrid, propertyDescriptors);
	}

	public static void rebindOpenOrderList(SettingsManager settingsManager, tradingConsole.ui.grid.DockableTable grid, String dataSourceKey,
										   Collection dataSource,
										   tradingConsole.ui.grid.BindingSource bindingSource)
	{
		Order.unbind(dataSourceKey, bindingSource);
		Order.initializeOpenOrderList(settingsManager, grid, dataSourceKey, dataSource, bindingSource);
		for (int i = 0, count = bindingSource.getRowCount(); i < count; i++)
		{
			Order order = (Order)bindingSource.getObject(i);
			order.orderStyleOpenOrderList();
			order.setBackground(Order.openOrdersKey, GridBackgroundColor.openOrderList);
		}
	}

	private static class PropertyChangingListener implements IPropertyChangingListener
	{
		private SettingsManager _settingsManager;

		public PropertyChangingListener(SettingsManager settingsManager)
		{
			this._settingsManager = settingsManager;
		}

		public void propertyChanging(PropertyChangingEvent e)
		{
			Order order = (Order)e.get_Owner();
			if (order.get_Close())
			{
				return;
			}
			Order firstOpenOrderHasClosed = MakeLiquidationOrder.getFirstOpenOrderHasClosed(order._tradingConsole, order);
			if (firstOpenOrderHasClosed != null)
			{
				if (!firstOpenOrderHasClosed.get_Transaction().get_Instrument().equals(order.get_Transaction().get_Instrument()))
				{
					e.set_Cancel(true);
				}
			}
			else
			{
				Object[] result = MakeLiquidationOrder.isAccept(order._settingsManager, order);
				if (! (Boolean)result[0])
				{
					e.set_Cancel(true);
				}
			}
			if (_settingsManager.get_Customer().get_SingleAccountOrderType() == 2
				|| _settingsManager.get_Customer().get_MultiAccountsOrderType() == 2)
			{
				if (order.getAvailableLotBanlance(false, null).compareTo(BigDecimal.ZERO) <= 0)
				{
					e.set_Cancel(true);
				}
			}
		}
	}

	//OpenOrderList use
	public static void initializeOpenOrderList(SettingsManager settingsManager, tradingConsole.ui.grid.DataGrid grid, String dataSourceKey,
											   Collection dataSource,
											   tradingConsole.ui.grid.BindingSource bindingSource)
	{
		grid.setShowVerticalLines(false);
		grid.setShowHorizontalLines(false);
		grid.setShowGrid(false);
		grid.setBackground(GridFixedBackColor.openOrderList);
		grid.setForeground(GridBackColor.openOrderList);

		UISetting uiSetting = settingsManager.getUISetting(UISetting.openOrderListUiSetting);
		PropertyDescriptor[] propertyDescriptors = Order.getPropertyDescriptorsForOpenOrderList(settingsManager);
		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, propertyDescriptors);
		grid.setModel(bindingSource, 0);

		IPropertyChangingListener propertyChangingListener = new PropertyChangingListener(settingsManager);
		bindingSource.addPropertyChangingListener(propertyChangingListener);

		TradingConsole.bindingManager.setGrid(dataSourceKey, uiSetting.get_RowHeight(),
											  ColorSettings.useBlackAsBackground ? ColorSettings.GridForeground : Color.black,
											  ColorSettings.useBlackAsBackground ? ColorSettings.TradingListGridBackground : Color.white, Color.blue, true, true,
											  new Font(uiSetting.get_FontName(), Font.BOLD, uiSetting.get_FontSize()), false, true, true);
		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.openOrderList, Color.white,
												HeaderFont.openOrderList);

		grid.addFilter(new AccountInstrumentFilter(grid.get_BindingSource(), false));
		grid.sortColumn(OpenOrderColKey.ShortCode, true, false);

		OpenOrderTableModelListener tableModelListener = new OpenOrderTableModelListener(grid);
		bindingSource.addTableModelListener(tableModelListener);

		ArrayList<String> unchoosableColumns = new ArrayList<String> ();
		unchoosableColumns.add(OpenOrderColKey.AutoLimitPriceString);
		unchoosableColumns.add(OpenOrderColKey.AutoStopPriceString);
		grid.putClientProperty(tradingConsole.ui.grid.TableColumnChooser.UnchoosableColumns, unchoosableColumns);
		grid.putClientProperty(com.jidesoft.grid.TableColumnChooser.SHOW_AUTO_RESIZE, false);
		grid.putClientProperty(com.jidesoft.grid.TableColumnChooser.TABLE_COLUMN_CHOOSER, DataGrid.get_ColumnChooser());
		com.jidesoft.grid.TableColumnChooser.install(grid);
		grid.sortColumn(OrderColKey.SubmitTime, true, false);

		grid.clearFilterColumns();
		grid.enableFilter(OpenOrderColKey.ShortCode);
		grid.enableFilter(OpenOrderColKey.AccountCode);
		grid.enableFilter(OpenOrderColKey.InstrumentCode);
		grid.enableFilter(OpenOrderColKey.IsBuyString);
		grid.enableFilter(OpenOrderColKey.IsOpenString);
		grid.enableFilter(OpenOrderColKey.ExecuteTradeDay);
		grid.enableFilter(OpenOrderColKey.LotBalanceString);
	}

	private static class OpenOrderTableModelListener implements TableModelListener
	{
		private DataGrid _owner;
		OpenOrderTableModelListener(DataGrid owner)
		{
			this._owner = owner;
		}

		public void tableChanged(TableModelEvent e)
		{
			BindingSource bindingSource = (BindingSource)e.getSource();
			boolean showAutoLimitPriceString = false;
			boolean showStopLimitPriceString = false;
			for (int row = 0; row < bindingSource.getRowCount(); row++)
			{
				Order order = (Order)bindingSource.getObject(row);
				if (!StringHelper.isNullOrEmpty(order.get_AutoLimitPriceString()))
				{
					showAutoLimitPriceString = true;
				}
				if (!StringHelper.isNullOrEmpty(order.get_AutoStopPriceString()))
				{
					showStopLimitPriceString = true;
				}
				if (showAutoLimitPriceString && showStopLimitPriceString)
				{
					break;
				}
			}

			int offset = -1;
			if (showAutoLimitPriceString || showStopLimitPriceString)
			{
				TableColumnModel columns = this._owner.getColumnModel();
				for (int column = 0; column < columns.getColumnCount(); column++)
				{
					TableColumn tableColumn = columns.getColumn(column);
					if (tableColumn.getIdentifier().equals(OpenOrderColKey.ExecutePriceString))
					{
						offset = column;
						break;
					}
				}
			}

			int autoLimitColumn = bindingSource.getColumnByName(OpenOrderColKey.AutoLimitPriceString);
			if (showAutoLimitPriceString)
			{
				if (!com.jidesoft.grid.TableColumnChooser.isVisibleColumn(this._owner.getColumnModel(), autoLimitColumn))
				{
					offset = offset == -1 ? -1 : offset + 1;
					com.jidesoft.grid.TableColumnChooser.showColumn(this._owner, autoLimitColumn, offset);
				}
			}
			else
			{
				com.jidesoft.grid.TableColumnChooser.hideColumn(this._owner, autoLimitColumn);
			}

			int autoStopColumn = bindingSource.getColumnByName(OpenOrderColKey.AutoStopPriceString);
			if (showStopLimitPriceString)
			{
				if (!com.jidesoft.grid.TableColumnChooser.isVisibleColumn(this._owner.getColumnModel(), autoStopColumn))
				{
					offset = offset == -1 ? -1 : offset + 1;
					com.jidesoft.grid.TableColumnChooser.showColumn(this._owner, autoStopColumn, offset);
				}
			}
			else
			{
				com.jidesoft.grid.TableColumnChooser.hideColumn(this._owner, autoStopColumn);
			}
		}
	}

	//OpenOrderList use
	public static PropertyDescriptor[] getPropertyDescriptorsForOpenOrderList(SettingsManager settingsManager)
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[20];

		UISetting uiSetting = settingsManager.getUISetting(UISetting.openOrderListUiSetting);
		HashMap<String, UISetting2> uiSetting2s = uiSetting.get_UiSetting2s();
		UISetting2 uiSetting2;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.Close);
		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.Close, false, null, OpenOrderLanguage.Close,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null, new BooleanCheckBoxCellEditor(), new BooleanCheckBoxCellRenderer());
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.ShortCode);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.ShortCode, false, null, OpenOrderLanguage.ShortCode,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.LimitCloseOrderSummary);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.LimitCloseOrderSummary, true, null,
			OpenOrderLanguage.LimitCloseOrderSummary, uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.StopCloseOrderSummary);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.StopCloseOrderSummary, true, null, OpenOrderLanguage.StopCloseOrderSummary,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.AccountCode);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.AccountCode, true, null, OpenOrderLanguage.AccountCode,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.InstrumentCode);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.InstrumentCode, true, null, OpenOrderLanguage.InstrumentCode,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.ExecuteTradeDay);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.ExecuteTradeDay, true, null, OpenOrderLanguage.ExecuteTradeDay,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.LotBalanceString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.LotBalanceString, true, null, OpenOrderLanguage.LotBalanceString,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.IsBuyString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.IsBuyString, true, null, OpenOrderLanguage.IsBuyString,
			uiSetting2.get_ColWidth(), SwingConstants.CENTER, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.ExecutePriceString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.ExecutePriceString, true, null, OpenOrderLanguage.ExecutePriceString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.LivePriceString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.LivePriceString, true, null, OpenOrderLanguage.LivePriceString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.AutoLimitPriceString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.AutoLimitPriceString, true, null, OpenOrderLanguage.AutoLimitPriceString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.AutoStopPriceString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.AutoStopPriceString, true, null, OpenOrderLanguage.AutoStopPriceString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.TradePLFloatString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.TradePLFloatString, true, null, OpenOrderLanguage.TradePLFloatString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.UnrealisedSwapString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.UnrealisedSwapString, true, null, OpenOrderLanguage.UnrealisedSwapString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.InterestPLFloatString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.InterestPLFloatString, true, null, OpenOrderLanguage.InterestPLFloatString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.StoragePLFloatString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.StoragePLFloatString, true, null, OpenOrderLanguage.StoragePLFloatString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.InterestRateString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.InterestRateString, true, null, OpenOrderLanguage.InterestRateString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.CommissionString);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.CommissionString, true, null, OpenOrderLanguage.CommissionString,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		uiSetting2 = uiSetting2s.get(OpenOrderColKey.CurrencyRate);
		propertyDescriptor = PropertyDescriptor.create(Order.class, OpenOrderColKey.CurrencyRate, true, null, OpenOrderLanguage.CurrencyRate,
			uiSetting2.get_ColWidth(), SwingConstants.RIGHT, null, null);
		propertyDescriptors[uiSetting2.get_Sequence()] = propertyDescriptor;

		ArrayList<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor> ();
		for (PropertyDescriptor item : propertyDescriptors)
		{
			if (Parameter.isShouldShowColumn("OpenOrderList", item.get_Name()))
			{
				propertyDescriptorList.add(item);
			}
		}
		if (propertyDescriptorList.size() != propertyDescriptors.length)
		{
			propertyDescriptors = new PropertyDescriptor[propertyDescriptorList.size()];
			propertyDescriptors = propertyDescriptorList.toArray(propertyDescriptors);
		}
		return ColumnUIInfoManager.applyUIInfo(GridNames.OpenOrderGrid, propertyDescriptors);
	}

	public static void unbind(String dataSourceKey, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		TradingConsole.bindingManager.unbind(dataSourceKey, bindingSource);
	}

	public void setForeground(String dataSourceKey, String propertyName, Color foreground)
	{
		TradingConsole.bindingManager.setForeground(dataSourceKey, this, propertyName, foreground);
		if (CloseOrder.CloseOrderHelper.getCreatedCloseOrders(this) != null)
		{
			for (CloseOrder closeOrder : CloseOrder.CloseOrderHelper.getCreatedCloseOrders(this))
			{
				TradingConsole.bindingManager.setForeground(Order.workingRelationOrdersKey, closeOrder, propertyName, foreground);
			}
		}
	}

	private void orderStyleWorkingOrderList()
	{
		this.setForeground(Order.workingOrdersKey, OrderColKey.PhaseString, Phase.getColor(this._phase));
		this.setForeground(Order.workingOrdersKey, OrderColKey.IsBuyString, BuySellColor.getColor(this._isBuy, true));
		this.setForeground(Order.workingOrdersKey, OrderColKey.IsOpenString, OpenCloseColor.getColor(this._isOpen));
		this.setForeground(Order.workingOrdersKey, OrderColKey.CommissionSumString, NumericColor.getColor(this._commissionSum, true));
		this.setForeground(Order.workingOrdersKey, OrderColKey.LevySumString, NumericColor.getColor(this._levySum, true));
		//this.setForeground(Order.workingOrdersKey, OrderColKey.InterestPLString, NumericColor.getColor(this._pLTradingItem.get_Interest()));
		this.setForeground(Order.workingOrdersKey, OrderColKey.TradePLString, NumericColor.getColor(this._pLTradingItem.get_Trade(), true));

		this.setForeground(Order.notConfirmedPendingOrdersKey, OrderColKey.PhaseString, Phase.getColor(this._phase));
		this.setForeground(Order.notConfirmedPendingOrdersKey, OrderColKey.IsBuyString, BuySellColor.getColor(this._isBuy, true));
		this.setForeground(Order.notConfirmedPendingOrdersKey, OrderColKey.IsOpenString, OpenCloseColor.getColor(this._isOpen));
		this.setForeground(Order.notConfirmedPendingOrdersKey, OrderColKey.CommissionSumString, NumericColor.getColor(this._commissionSum, true));
		this.setForeground(Order.notConfirmedPendingOrdersKey, OrderColKey.LevySumString, NumericColor.getColor(this._levySum, true));
		//this.setForeground(Order.notConfirmedPendingOrdersKey, OrderColKey.InterestPLString, NumericColor.getColor(this._pLTradingItem.get_Interest()));
		this.setForeground(Order.notConfirmedPendingOrdersKey, OrderColKey.TradePLString, NumericColor.getColor(this._pLTradingItem.get_Trade(), true));

	}

	private void orderStyleOpenOrderList()
	{
		this.setForeground(Order.openOrdersKey, OpenOrderColKey.IsBuyString, BuySellColor.getColor(this._isBuy, true));
		this.setForeground(Order.openOrdersKey, OpenOrderColKey.IsOpenString, OpenCloseColor.getColor(this._isOpen));
		this.setForeground(Order.openOrdersKey, OpenOrderColKey.LivePriceString, NumericColor.getColor(Price.toDouble(this._livePrice), true));
		this.setForeground(Order.openOrdersKey, OpenOrderColKey.TradePLFloatString, NumericColor.getColor(this._floatTradingItem.get_Trade(), true));
		this.setForeground(Order.openOrdersKey, OpenOrderColKey.InterestPLFloatString, NumericColor.getColor(this._floatTradingItem.get_Interest(), true));
		this.setForeground(Order.openOrdersKey, OpenOrderColKey.StoragePLFloatString, NumericColor.getColor(this._floatTradingItem.get_Storage(), true));
		if (this._interestRate != null)
		{
			this.setForeground(Order.openOrdersKey, OpenOrderColKey.InterestRateString, NumericColor.getColor(this._interestRate, true));
		}
		this.setForeground(Order.openOrdersKey, OpenOrderColKey.CommissionString, NumericColor.getColor(this._commissionSum, true));
	}

	public void addWorkingOrder(OperateWhichOrderUI operateWhichOrderUI)
	{
		this.addWorkingOrder(operateWhichOrderUI, false);
	}

	public void addWorkingOrder(OperateWhichOrderUI operateWhichOrderUI, boolean isFromExecute2)
	{
		if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
			|| operateWhichOrderUI.equals(OperateWhichOrderUI.WorkingOrderList))
		{
			if (this.get_Transaction().get_Phase() == Phase.Executed && !isFromExecute2)
			{
				//don't add to working list, but maybe should add to some other list in future
			}
			else
			{
				TradingConsole.bindingManager.add(Order.workingOrdersKey, this);
				if (this._transaction.get_OrderType().equals(OrderType.Limit)
					|| this._transaction.get_OrderType().equals(OrderType.Stop)
					|| this._transaction.get_OrderType().equals(OrderType.OneCancelOther)
					|| this._transaction.get_OrderType().equals(OrderType.MarketOnOpen)
					|| this._transaction.get_OrderType().equals(OrderType.MarketOnClose))
				{
					TradingConsole.bindingManager.add(Order.notConfirmedPendingOrdersKey, this);
				}

				this.orderStyleWorkingOrderList();
				if (this.isOffLineMakeOrder())
				{
					this.setBackground(Order.workingOrdersKey, GridBackgroundColor.workingOrderListForOfflineMakeOrder);
				}
			}
		}
	}

	public void addOpenOrderList(OperateWhichOrderUI operateWhichOrderUI)
	{
		if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
			|| operateWhichOrderUI.equals(OperateWhichOrderUI.OpenOrderList))
		{
			TradingConsole.bindingManager.add(Order.openOrdersKey, this);
			this.orderStyleOpenOrderList();
			RelationOrder.get_OrderStateReceiver().add(this);
		}
	}

	private void setBackground(String dataSourceKey, Color background)
	{
		TradingConsole.bindingManager.setBackground(dataSourceKey, this, background);
		if (CloseOrder.CloseOrderHelper.getCreatedCloseOrders(this) != null)
		{
			for (CloseOrder closeOrder : CloseOrder.CloseOrderHelper.getCreatedCloseOrders(this))
			{
				TradingConsole.bindingManager.setBackground(Order.workingRelationOrdersKey, closeOrder, background);
			}
		}
	}

	public void add()
	{
		this.add(false, false);
	}

	public void add(boolean isFromExecute2, boolean isInIntializing)
	{
		OperateWhichOrderUI operateWhichOrderUI = this.getOperateWhichOrderUI();
		TradingConsole.traceSource.trace(TraceType.Information, "add order with operateWhichOrderUI = " + operateWhichOrderUI.name());
		this.addWorkingOrder(operateWhichOrderUI, isFromExecute2);
		this.addOpenOrderList(operateWhichOrderUI);
		if(!isInIntializing) this._tradingConsole.refreshSummary();
	}

	public static void removeFromWorkingOrderList(Order order)
	{
		TradingConsole.bindingManager.remove(Order.workingOrdersKey, order);
		TradingConsole.bindingManager.remove(Order.notConfirmedPendingOrdersKey, order);
		if (CloseOrder.CloseOrderHelper.getCreatedCloseOrders(order) != null)
		{
			for (CloseOrder closeOrder : CloseOrder.CloseOrderHelper.getCreatedCloseOrders(order))
			{
				TradingConsole.bindingManager.remove(Order.workingRelationOrdersKey, closeOrder);
			}
		}
	}

	public static void removeFromOpenOrderList(Order order)
	{
		TradingConsole.bindingManager.remove(Order.openOrdersKey, order);
		RelationOrder.get_OrderStateReceiver().remove(order);
		order._tradingConsole.refreshSummary();
	}

	public void remove()
	{
		OperateWhichOrderUI operateWhichOrderUI = this.getOperateWhichOrderUI();
		if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
			|| operateWhichOrderUI.equals(OperateWhichOrderUI.WorkingOrderList))
		{
			Order.removeFromWorkingOrderList(this);
		}
		if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
			|| operateWhichOrderUI.equals(OperateWhichOrderUI.OpenOrderList))
		{
			Order.removeFromOpenOrderList(this);
		}
		this._tradingConsole.refreshSummary();
	}

	public void removeAll(String dataSourceKey)
	{
		TradingConsole.bindingManager.removeAll(dataSourceKey);
		this._tradingConsole.refreshSummary();
	}

	public void update(String propertyName, Object value)
	{
		OperateWhichOrderUI operateWhichOrderUI = this.getOperateWhichOrderUI();
		if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
			|| operateWhichOrderUI.equals(OperateWhichOrderUI.WorkingOrderList))
		{
			if (this._tradingConsole.getWorkingOrder(this._id) != null)
			{
				TradingConsole.bindingManager.update(Order.workingOrdersKey, this, propertyName, value);
				if (this._transaction.get_OrderType().equals(OrderType.Limit)
					|| this._transaction.get_OrderType().equals(OrderType.Stop)
					|| this._transaction.get_OrderType().equals(OrderType.OneCancelOther))
				{
					TradingConsole.bindingManager.update(Order.notConfirmedPendingOrdersKey, this, propertyName, value);
				}
				if (CloseOrder.CloseOrderHelper.getCreatedCloseOrders(this) != null)
				{
					for (CloseOrder closeOrder : CloseOrder.CloseOrderHelper.getCreatedCloseOrders(this))
					{
						TradingConsole.bindingManager.update(Order.workingRelationOrdersKey, closeOrder, propertyName, value);
					}
				}
				this.orderStyleWorkingOrderList();
			}
		}
		if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
			|| operateWhichOrderUI.equals(OperateWhichOrderUI.OpenOrderList))
		{
			TradingConsole.bindingManager.update(Order.openOrdersKey, this, propertyName, value);
			this.orderStyleOpenOrderList();
		}
		this._tradingConsole.refreshSummary();
		if(!this._phase.equals(Phase.Placed) && !this._phase.equals(Phase.Placing))
		{
			TradingConsole.bindingManager.remove(Order.notConfirmedPendingOrdersKey, this);
		}

	}

	public void waitingForAcceptance()
	{
		this._message = Language.WaitingForAcceptance;
		this.update();
	}

	public void update()
	{
		this.update(true);
	}

	public void update(boolean refreshSummary)
	{
		OperateWhichOrderUI operateWhichOrderUI = this.getOperateWhichOrderUI();

		if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
			|| operateWhichOrderUI.equals(OperateWhichOrderUI.WorkingOrderList))
		{
			TradingConsole.bindingManager.update(Order.workingOrdersKey, this);
			this.orderStyleWorkingOrderList();
			if (this._tradingConsole.getWorkingOrder(this._id) != null)
			{
				if (this.get_Phase() != Phase.Placed && this.get_Phase() != Phase.Placing)
				{
					if (CloseOrder.CloseOrderHelper.getCreatedCloseOrders(this) != null)
					{
						for (CloseOrder colseOrder : CloseOrder.CloseOrderHelper.getCreatedCloseOrders(this))
						{
							TradingConsole.bindingManager.update(Order.workingRelationOrdersKey, colseOrder);
						}
					}
				}
			}
		}
		if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
			|| operateWhichOrderUI.equals(OperateWhichOrderUI.OpenOrderList))
		{
			TradingConsole.bindingManager.update(Order.openOrdersKey, this);
			this.orderStyleOpenOrderList();
		}
		if(refreshSummary) this._tradingConsole.refreshSummary();
		if(!this._phase.equals(Phase.Placed) && !this._phase.equals(Phase.Placing))
		{
			TradingConsole.bindingManager.remove(Order.notConfirmedPendingOrdersKey, this);
		}

	}

	private boolean isOffLineMakeOrder()
	{
		DateTime executeTime = this._transaction.get_ExecuteTime();
		DateTime logonTime = this._tradingConsole.get_LoginInformation().get_LoginTime();
		if (executeTime != null && !executeTime.before(this._settingsManager.get_Customer().get_LastLogTime()) && !executeTime.after(logonTime))
		{
			return true;
		}
		return false;
	}

	public OperateWhichOrderUI getOperateWhichOrderUI()
	{
		DateTime executeTime = this._transaction.get_ExecuteTime();

		boolean isEffectWorkingOrderList = false;
		DateTime logonTime = this._tradingConsole.get_LoginInformation().get_LoginTime();
		DateTime endTime = this._transaction.get_EndTime();
		if (this._transaction.get_Account().get_Type().equals(AccountType.Agent))
		{
			isEffectWorkingOrderList = ( (executeTime == null || executeTime.equals(DateTime.maxValue)) && endTime.getTime() >= logonTime.getTime())
				|| ( (this._phase.equals(Phase.Placing) || this._phase.equals(Phase.Placed)) && !endTime.before(logonTime))
				|| ( (this._phase.equals(Phase.Executed) || this._phase.equals(Phase.Completed))
					&& !executeTime.before(logonTime)
					&& this._lotBalance.compareTo(BigDecimal.ZERO) > 0);
		}
		else
		{
			isEffectWorkingOrderList = (this._phase.equals(Phase.Cancelled))
				|| ( (executeTime == null || executeTime.equals(DateTime.maxValue)) && !endTime.before(logonTime))
				|| ( (this._phase.equals(Phase.Placing) || this._phase.equals(Phase.Placed)) && !endTime.before(logonTime))
				|| ( (this._phase.equals(Phase.Executed) || this._phase.equals(Phase.Completed))
					&& !executeTime.before(logonTime));
		}
		//??????????????????????????
		if (this._isAssignOrder)
		{
			isEffectWorkingOrderList = true;
		}

		if (this.isOffLineMakeOrder())
		{
			isEffectWorkingOrderList = true;
		}

		boolean isEffectOpenOrderList = (executeTime != null && this._phase.equals(Phase.Executed) && this._lotBalance.doubleValue() > 0);

		OperateWhichOrderUI operateWhichOrderUI;
		if (isEffectWorkingOrderList && isEffectOpenOrderList)
		{
			operateWhichOrderUI = OperateWhichOrderUI.Both;
		}
		else if (isEffectWorkingOrderList)
		{
			operateWhichOrderUI = OperateWhichOrderUI.WorkingOrderList;
		}
		else if (isEffectOpenOrderList)
		{
			operateWhichOrderUI = OperateWhichOrderUI.OpenOrderList;
		}
		else
		{
			operateWhichOrderUI = OperateWhichOrderUI.None;
		}

		return operateWhichOrderUI;
	}

	public void calculateForDeleteOrder()
	{
		//????????????????????
	}

	public void rejectCancelLmtOrder(String message)
	{
		this._message = message;
		//Update UI
		this.update();
	}

	public String get_Message()
	{
		return StringHelper.isNullOrEmpty(this._message) ? "" : this._message;
	}

	public void setMessage(String message)
	{
		this._message = message;
		//Update UI
		this.update();
	}

	public void updateTranCode()
	{
		//this._phase = Phase.Placed;
		//Update UI
		this.update();
	}

	public void acceptPlace(String message)
	{
		this._message = message;
		this._phase = this._transaction.get_Phase();
		//Update UI
		this.update();
	}

	public void cancel(String message, boolean isRejectDQByDealer)
	{
		this._isRejectDQByDealer = isRejectDQByDealer;
		this._message = message;
		this._phase = Phase.Cancelled;

		//Update UI
		this.update();
	}

	public void cancelOtherOCOOrder()
	{
		this.cancel(Language.OneCancelOtherPrompt, false);
	}

	public short getDisplayDecimals()
	{
		short decimals = 2;
		Account account = this._transaction.get_Account();
		if (!account.get_IsMultiCurrency())
		{
			decimals = account.get_Currency().get_Decimals();
		}
		else
		{
			decimals = this._transaction.get_Instrument().get_Currency().get_Decimals();
		}

		return decimals;
	}

	private double calcCommission()
	{
		double commission = 0.00;

		Instrument instrument = this._transaction.get_Instrument();
		Guid instrumentId = instrument.get_Id();
		short commissionFormula = instrument.get_CommissionFormula();
		short tradePLFormula = instrument.get_TradePLFormula();

		double rateCommission = this._transaction.get_Account().get_RateCommission();
		Guid tradePolicyId = this._transaction.get_Account().get_TradePolicyId();
		BigDecimal contractSize = this._transaction.get_ContractSize();

		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(tradePolicyId, instrumentId);
		double commissionCloseD = tradePolicyDetail.get_CommissionCloseD().doubleValue();
		double commissionCloseO = tradePolicyDetail.get_CommissionCloseO().doubleValue();
		double commissionClose = 0.00;
		BigDecimal pairRelationFactor = BigDecimal.ONE;
		if (this._transaction.get_Type() == TransactionType.Pair)
		{
			pairRelationFactor = tradePolicyDetail.get_PairRelationFactor();
		}
		/*if (!this._transaction.get_ExecuteTime().before(this._transaction.get_BeginTime())
		 && this._transaction.get_ExecuteTime().after(this._transaction.get_EndTime()))*/
		DateTime tradeDayBegin = this._settingsManager.get_TradeDay().get_BeginTime();
		if (this._transaction.get_ExecuteTime().before(tradeDayBegin))
		{
			commissionClose = commissionCloseO * pairRelationFactor.doubleValue();
		}
		else
		{
			commissionClose = commissionCloseD * pairRelationFactor.doubleValue();
		}
		double lotBalance = this._lotBalance.doubleValue();
		double commission2 = commissionClose * rateCommission;
		switch (commissionFormula)
		{
			case 0:
				commission = commission2 * lotBalance;
				break;
			case 1:
				commission = commission2 * lotBalance * contractSize.doubleValue();
				break;
			case 2:
				commission = commission2 * lotBalance * contractSize.doubleValue() / Price.toDouble(this._executePrice);
				break;
			case 3:
				commission = commission2 * lotBalance * contractSize.doubleValue() * Price.toDouble(this._executePrice);
				break;
			case 4:
				Price buy = null;
				Price sell = null;
				Price close = null;
				if (tradePLFormula == 2)
				{
					buy = Price.add(this._executePrice, (int)commissionClose);
					sell = this._executePrice.clone();
					close = this._executePrice.clone();
				}
				else
				{
					buy = this._executePrice.clone();
					sell = Price.add(this._executePrice, (int)commissionClose);
					close = this._executePrice.clone();
				}
				short decimals = this._transaction.get_Instrument().get_Currency().get_Decimals();
				commission = this.calculateTradePL2(tradePLFormula, this._lotBalance, contractSize, buy, sell, close, decimals);
				break;
			case 5:
				commission = 0;
				break;
		}
		if (commissionFormula != 5) //5--->Price pips
		{
			double minCommission = tradePolicyDetail.get_MinCommissionClose().doubleValue();
			commission = Math.max(minCommission, commission);
		}
		return commission;
	}

	public void calculatePLNotValued()
	{
		Guid sourceCurrencyId = this._transaction.get_Instrument().get_Currency().get_Id();
		Guid targetCurrencyId = this._transaction.get_Account().get_Currency().get_Id();
		if (this._transaction.get_Account().get_IsMultiCurrency())
		{
			targetCurrencyId = sourceCurrencyId;
		}
		CurrencyRate currencyRate = this._tradingConsole.get_SettingsManager().getCurrencyRate(sourceCurrencyId, targetCurrencyId);
		this._PLNotValued.caculatePLNotValued(currencyRate);
	}

	public void calculatePLFloat(Price buy, Price sell)
	{
		if (this._isOpen)
		{
			TradingItem tradingItem = TradingItem.create(0.00, 0.00, 0.00);

			Price close = null;
			if (this._isBuy)
			{
				close = buy == null ? null : buy.clone();

				buy = this._executePrice.clone();
				sell = close == null ? null :close.clone();
			}
			else
			{
				close = sell == null ? null :sell.clone();

				buy = close == null ? null :close.clone();
				sell = this._executePrice.clone();
			}

			this._margin = this.calculateMargin(close);

			//Modified by Michael on 2008-04-22
			//this._livePrice = Price.clone(close);
			if (close != null)
			{
				this._livePrice = Price.clone(close);
			}
			if ( (this._isBuy && sell == null) || (!this._isBuy && buy == null))
			{
				return;
			}

			short decimals = this._transaction.get_Instrument().get_Currency().get_Decimals();
			tradingItem.set_Interest(AppToolkit.round(this._lotBalance.doubleValue() * this._interestPerLot.doubleValue(), decimals));
			tradingItem.set_Storage(AppToolkit.round(this._lotBalance.doubleValue() * this._storagePerLot.doubleValue(), decimals));
			tradingItem.set_Trade(this.calculateTradePL2(this.get_Transaction().get_Instrument().get_TradePLFormula(), this._lotBalance,
				this.get_Transaction().get_ContractSize(), buy, sell, close, decimals));

			if (this._transaction.get_Account().get_IsMultiCurrency() == false)
			{
				Guid instrumentCurrencyId = this._transaction.get_Instrument().get_Currency().get_Id();
				Guid accountCurrencyId = this._transaction.get_Account().get_Currency().get_Id();
				CurrencyRate currencyRate = this._settingsManager.getCurrencyRate(instrumentCurrencyId, accountCurrencyId);
				tradingItem = TradingItem.exchange(tradingItem, currencyRate);

				tradingItem.set_Interest(AppToolkit.round(tradingItem.get_Interest(), this._displayDecimals));
				tradingItem.set_Storage(AppToolkit.round(tradingItem.get_Storage(), this._displayDecimals));
				tradingItem.set_Trade(AppToolkit.round(tradingItem.get_Trade(), this._displayDecimals));
			}

			this._floatTradingItem.merge(tradingItem);
		}
	}

	private double calculateMargin(Price livePirce)
	{
		Price price = this._executePrice;
		int marginFormula = this._transaction.get_Instrument().get_MarginFormula();
		if(marginFormula == 6 || marginFormula == 7)
		{
			price = livePirce;
			if(price == null) return this._margin;
		}
		double margin = 0;
		if(marginFormula == 0)
		{
			int decimals = this._transaction.get_Account().get_IsMultiCurrency() ? this._transaction.get_Instrument().get_Currency().get_Decimals() : this._transaction.get_Account().get_Currency().get_Decimals();
			margin = AppToolkit.round(this._lotBalance.doubleValue(), decimals);
		}
		else
		{
			if (marginFormula == 1)
			{
				margin = this._lotBalance.doubleValue() * this._transaction.get_ContractSize().doubleValue();
			}
			else if (marginFormula == 2 || marginFormula == 6)
			{
				if(price != null)
				{
					margin = (this._lotBalance.doubleValue() * this._transaction.get_ContractSize().doubleValue()) / Price.toDouble(price);
				}
			}
			else if (marginFormula == 3 || marginFormula == 7)
			{
				if(price != null)
					margin = this._lotBalance.doubleValue() * this._transaction.get_ContractSize().doubleValue() * Price.toDouble(price);
			}

			margin = AppToolkit.round(margin, this._transaction.get_Instrument().get_Currency().get_Decimals());
			if (!this._transaction.get_Account().get_IsMultiCurrency())
			{
				Guid instrumentCurrencyId = this._transaction.get_Instrument().get_Currency().get_Id();
				Guid accountCurrencyId = this._transaction.get_Account().get_Currency().get_Id();
				CurrencyRate currencyRate = this._settingsManager.getCurrencyRate(instrumentCurrencyId, accountCurrencyId);
				margin = -currencyRate.exchange(-margin);
			}
		}
		Currency targetCurrency = this._transaction.get_Instrument().get_Currency();
		if (!this._transaction.get_Account().get_IsMultiCurrency())
		{
			targetCurrency = this._transaction.get_Account().get_Currency();
		}
		margin = AppToolkit.round(margin, targetCurrency.get_Decimals());
		return margin;
	}

	/*
	  public TradingItem calculatePLFloat(Price buy, Price sell)
	  {
	 TradingItem deltaTradingItem = TradingItem.create(0.00, 0.00, 0.00);

	 if (this._isOpen)
	 {
	  TradingItem tradingItem = TradingItem.create(0.00, 0.00, 0.00);

	  Price close = null;
	  if (this._isBuy)
	  {
	   close = buy.clone();

	   buy = this._executePrice.clone();
	   sell = close.clone();
	  }
	  else
	  {
	   close = sell.clone();

	   buy = close.clone();
	   sell = this._executePrice.clone();
	  }
	  this._livePrice = (close == null) ? close : close.clone();

	  short decimals = this._transaction.get_Instrument().get_Currency().get_Decimals();
	  tradingItem.set_Interest(AppToolkit.round(this._lotBalance.doubleValue() * this._interestPerLot.doubleValue(), decimals));
	  tradingItem.set_Storage(AppToolkit.round(this._lotBalance.doubleValue() * this._storagePerLot.doubleValue(), decimals));
	  tradingItem.set_Trade(this.calculateTradePL2(this.get_Transaction().get_Instrument().get_TradePLFormula(), this._lotBalance,
	   this.get_Transaction().get_ContractSize(),
	   buy, sell, close, decimals));

	  if (this._transaction.get_Account().get_IsMultiCurrency() == false)
	  {
	   Guid instrumentCurrencyId = this._transaction.get_Instrument().get_Currency().get_Id();
	   Guid accountCurrencyId = this._transaction.get_Account().get_Currency().get_Id();
	   CurrencyRate currencyRate = this._settingsManager.getCurrencyRate(instrumentCurrencyId, accountCurrencyId);
	   tradingItem = TradingItem.exchange(tradingItem, currencyRate);
	  }

	  deltaTradingItem = TradingItem.subStract(tradingItem, this._floatTradingItem);
	  this._floatTradingItem.merge(tradingItem);
	 }

	 return deltaTradingItem;
	  }
	 */

	private double calculateTradePL2(short tradePLFormula, BigDecimal lot, BigDecimal contractSize, Price buyPrice, Price sellPrice, Price closePrice,
									 short decimals)
	{
		double tradePL = 0.00;
		double lotValue = lot.doubleValue();
		switch (tradePLFormula)
		{
			case 0: //(S-B)*CS
				tradePL = lotValue * (Price.toDouble(sellPrice) - Price.toDouble(buyPrice)) * contractSize.doubleValue();
				break;
			case 1: //(S-B)*CS/L
				tradePL = lotValue * (Price.toDouble(sellPrice) - Price.toDouble(buyPrice)) * contractSize.doubleValue() / Price.toDouble(closePrice);
				break;
			case 2: //(1/S-1/B)*CS
				tradePL = lotValue * (1 / Price.toDouble(sellPrice) - 1 / Price.toDouble(buyPrice)) * contractSize.doubleValue();
				break;
			case 3: //(S-B)*CS/O
				tradePL = lotValue * (Price.toDouble(sellPrice) - Price.toDouble(buyPrice)) * contractSize.doubleValue() / Price.toDouble(this._executePrice);
				break;
		}
		tradePL = AppToolkit.round(tradePL, decimals);

		return tradePL;
	}

	//not use
	public Object[] calculatePriceByTradePL(double tradePL)
	{
		double priceValue = 0.00;
		Instrument instrument = this._transaction.get_Instrument();
		short tradePLFormula = instrument.get_TradePLFormula();
		BigDecimal contractSize = this._transaction.get_ContractSize();
		double executePriceValue = Price.toDouble(this._executePrice);
		switch (tradePLFormula)
		{
			case 0: //(S-B)*CS

				if (this._isBuy)
				{
					priceValue = executePriceValue + tradePL / (contractSize.doubleValue() * this._lotBalance.doubleValue());
				}
				else
				{
					priceValue = executePriceValue - tradePL / (contractSize.doubleValue() * this._lotBalance.doubleValue());
				}
				break;
			case 1: //(S-B)*CS/L
				if (this._isBuy)
				{
					priceValue = (executePriceValue * contractSize.doubleValue()) / (contractSize.doubleValue() - tradePL);
				}
				else
				{
					priceValue = (executePriceValue * contractSize.doubleValue()) / (contractSize.doubleValue() + tradePL);
				}
				break;
			case 2: //(1/S-1/B)*CS
				if (this._isBuy)
				{
					priceValue = (executePriceValue * contractSize.doubleValue()) / (contractSize.doubleValue() + (executePriceValue * tradePL));
				}
				else
				{
					priceValue = (executePriceValue * contractSize.doubleValue()) / (contractSize.doubleValue() - (executePriceValue * tradePL));
				}
				break;
			case 3: //(S-B)*CS/O
				if (this._isBuy)
				{
					priceValue = executePriceValue + tradePL * executePriceValue / contractSize.doubleValue();
				}
				else
				{
					priceValue = executePriceValue - tradePL * executePriceValue / contractSize.doubleValue();
				}
				break;
		}

		Price marketPrice = instrument.get_LastQuotation().getBuySell(this._isBuy);
		double marketPriceValue = Price.toDouble(marketPrice);
		if (priceValue <= 0)
		{
			priceValue = marketPriceValue * 0.7;
		}

		boolean isLimitPrice = (instrument.get_IsNormal() == ( (!this._isBuy) ^ priceValue >= marketPriceValue));
		Price calculatePrice = Price.create(Math.abs(priceValue), instrument.get_NumeratorUnit(), instrument.get_Denominator());
		Object[] result = new Object[]
			{isLimitPrice, calculatePrice};
		return result;
	}

	public static BigDecimal getLotWithPeerOrderIDs(String peerOrderIDs)
	{
		BigDecimal sumLot = BigDecimal.ZERO;
		if (StringHelper.isNullOrEmpty(peerOrderIDs))
		{
			return sumLot;
		}
		String[] str1 = StringHelper.split(peerOrderIDs, TradingConsole.delimiterRow);
		for (int i = 0; i < str1.length; i++)
		{
			String[] str2 = StringHelper.split(str1[i], TradingConsole.delimiterCol);
			sumLot = sumLot.add(AppToolkit.convertStringToBigDecimal(str2[1]));
		}
		return sumLot;
	}

	//for make order???????????????????????
	public void relationOrdersProcess(Order newOrder)
	{
		if (this._relationOrders.size() <= 0)
		{
			return;
		}
		for (Iterator<RelationOrder> iterator = this._relationOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			relationOrder.process(newOrder);
		}
	}

	//if rewrite it...................
	public static void disassemblePeerOrderIDs(TradingConsole tradingConsole, Order closedOrder)
	{
		String newPeerOrderIDs = closedOrder.get_PeerOrderIDs();
		if (StringHelper.isNullOrEmpty(newPeerOrderIDs))
		{
			return;
		}
		String newOrderCode = closedOrder.get_Code();
		Price newOrderExecutePrice = closedOrder.get_ExecutePrice();

		String[] str1 = StringHelper.split(newPeerOrderIDs, TradingConsole.delimiterRow);
		for (int i = 0; i < str1.length; i++)
		{
			String[] str2 = StringHelper.split(str1[i], TradingConsole.delimiterCol);
			Guid relationId = new Guid(str2[0]);
			BigDecimal relationLiqLot = AppToolkit.convertStringToBigDecimal(str2[1]);

			Order openOrder = tradingConsole.getOrder(relationId);
			if (openOrder == null)
			{
				continue;
			}

			String previousRelationPeerOrderIDs = openOrder.get_PeerOrderIDs();
			if (previousRelationPeerOrderIDs.indexOf(closedOrder._id.toString()) >= 0)
			{
				tradingConsole.traceSource.trace(TraceType.Warning,
												 "Relation Order Disassemble 2 times is not allow!" + " ClosedOrder: " + closedOrder.get_Id().toString() +
												 " OpenOrder: " + str2[0]);
				continue;
			}
			else
			{
				String previousRelationPeerOrderCodes = openOrder.get_PeerOrderCodes();
				BigDecimal relationLotBalance = openOrder.get_LotBalance();

				String currenctRelationPeerOrderIDs = "";

				currenctRelationPeerOrderIDs = (!StringHelper.isNullOrEmpty(previousRelationPeerOrderIDs))
					? previousRelationPeerOrderIDs + TradingConsole.delimiterRow : "";
				currenctRelationPeerOrderIDs += closedOrder._id.toString() + TradingConsole.delimiterCol +
					AppToolkit.getFormatLot(relationLiqLot, openOrder._transaction.get_Account(), openOrder._transaction.get_Instrument()) +
					TradingConsole.delimiterCol +
					Price.toString(newOrderExecutePrice);
				openOrder.set_PeerOrderIDs(currenctRelationPeerOrderIDs);

				String currenctRelationPeerOrderCodes = "";
				currenctRelationPeerOrderCodes = (!StringHelper.isNullOrEmpty(previousRelationPeerOrderCodes))
					? previousRelationPeerOrderCodes + TradingConsole.delimiterRow : "";
				currenctRelationPeerOrderCodes += closedOrder.get_ExecuteTradeDay() + TradingConsole.delimiterCol2 +
					AppToolkit.getFormatLot(relationLiqLot, openOrder._transaction.get_Account(), openOrder._transaction.get_Instrument())
					+ TradingConsole.delimiterCol2 + Price.toString(newOrderExecutePrice);
				openOrder.set_PeerOrderCodes(currenctRelationPeerOrderCodes);

				Instrument instrument = openOrder.get_Transaction().get_Instrument();
				BigDecimal lotBalance = relationLotBalance.subtract(relationLiqLot);
				if (lotBalance.compareTo(BigDecimal.ZERO) <= 0)
				{
					//Michael......
					//tradingConsole.removeOrder(order);
					tradingConsole.removeOpenOrder(openOrder);
					openOrder.calculateForDeleteOrder();
					Order.removeFromOpenOrderList(openOrder);

					openOrder.set_LotBalance(lotBalance);
					instrument.reCalculateTradePLFloat();
					openOrder.get_FloatTradingItem().clear();
					openOrder.update();
				}
				else
				{
					openOrder.set_LotBalance(lotBalance);
					instrument.reCalculateTradePLFloat();
					/*if (openOrder.get_Transaction().needCalculateSummary())
						  {
					 if (openOrder.get_IsBuy())
					 {
					  instrument.subtractBuyLots(relationLiqLot);
					 }
					 else
					 {
					  instrument.subtractSellLots(relationLiqLot);
					 }
					 tradingConsole.rebindSummary();
						  }*/
					openOrder.update();
				}
			}
		}
	}

	//relate transaction to check
	public boolean getIsMustAssignOrder()
	{
		return (this._lotBalance.compareTo(BigDecimal.ZERO) > 0);
	}

	//for Make Order----------------------------------------------------------------------
	public TradeOption get_TradeOption()
	{
		return this._tradeOption;
	}

	public Price get_SetPrice()
	{
		return this._setPrice;
		//return this._makeOrderAccount.get_Instrument().get_LastQuotation().getBuySell(this._isBuy);
	}

	private boolean _freezePriceForPlacing = false;
	public void freezePriceForPlacing()
	{
		this._freezePriceForPlacing = true;
	}

	public void unfreezePriceForPlacing()
	{
		this._freezePriceForPlacing = true;
	}

	public void set_SetPrice(Price value)
	{
		if (!this._freezePriceForPlacing)
		{
			this._setPrice = value;
		}
	}

	public Price get_SetPrice2()
	{
		return this._setPrice2;
	}

	public void set_SetPrice2(Price value)
	{
		if (!this._freezePriceForPlacing)
		{
			this._setPrice2 = value;
		}
	}

	public String get_TradeOptionString()
	{
		return TradeOption.getCaption(this._tradeOption);
	}

	public static PropertyDescriptor[] getPropertyDescriptorsForVerification(boolean isHiddenSetPrice, boolean isHiddenTradeOption)
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[7];
		int i = -1;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(Order.class, MakeOrderVerificationColKey.Sequence, true, null,
			MakeOrderVerificationLanguage.Sequence, 0, SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Order.class, MakeOrderVerificationColKey.AccountCode, true, null,
			MakeOrderVerificationLanguage.AccountCode, 70, SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Order.class, MakeOrderVerificationColKey.Lot, true, null, MakeOrderVerificationLanguage.Lot, 40,
			SwingConstants.RIGHT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Order.class, MakeOrderVerificationColKey.IsBuyString, true, null,
			MakeOrderVerificationLanguage.IsBuyString, 40, SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Order.class, MakeOrderVerificationColKey.IsOpenString, true, null,
			MakeOrderVerificationLanguage.IsOpenString, 40, SwingConstants.CENTER, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Order.class, MakeOrderVerificationColKey.SetPriceString, true, null,
			MakeOrderVerificationLanguage.SetPriceString, ( (isHiddenSetPrice) ? 0 : 70), SwingConstants.RIGHT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Order.class, MakeOrderVerificationColKey.TradeOptionString, true, null,
			MakeOrderVerificationLanguage.TradeOptionString, ( (isHiddenTradeOption) ? 0 : 40), SwingConstants.CENTER, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		return propertyDescriptors;
	}

	private void setStyleForVerification(String dataSourceKey)
	{
		Color buySellColor = BuySellColor.getColor(this._isBuy, false);
		this.setForeground(dataSourceKey, MakeOrderVerificationColKey.Lot, buySellColor);
		this.setForeground(dataSourceKey, MakeOrderVerificationColKey.IsBuyString, buySellColor);
		this.setForeground(dataSourceKey, MakeOrderVerificationColKey.SetPriceString, buySellColor);
		this.setForeground(dataSourceKey, MakeOrderVerificationColKey.IsOpenString, OpenCloseColor.getColor(this._isOpen));
		this.setForeground(dataSourceKey, MakeOrderVerificationColKey.TradeOptionString, TradeOption.getColor(this._tradeOption));
	}

	public String getVerificationInfo()
	{
		String space = " ";
		String verificationInfo = this.get_AccountCode() + space
			+ this.get_IsBuyString() + space
			+ this.get_OrderTypeString() + space
			+ (this._isOpen ? this.get_LotBalanceString() : this.get_LotString()) + space
			+ Language.Lots + space
			+ this.get_InstrumentCode() + space
			+ Language.At + space
			+ this.get_SetPriceString()
			+ (this._dQMaxMove == 0 ? "" : space + Language.OrderSingleDQlblDQMaxMove + space + this._dQMaxMove);

		if (this._transaction.get_OrderType() == OrderType.StopLimit && this._executePrice == null)
		{
			verificationInfo = this.get_AccountCode() + space
				+ this.get_IsBuyString() + space
				+ (this._isOpen ? this.get_LotBalanceString() : this.get_LotString()) + space
				+ Language.Lots + space
				+ this.get_InstrumentCode() + space
				+ Language.TrigerAt + space
				+ this.get_SetPrice2String() + space
				+ Language.LimitWith + space
				+ this.get_SetPriceString()
				+ (this._dQMaxMove == 0 ? "" : space + Language.OrderSingleDQlblDQMaxMove + space + this._dQMaxMove);
		}

		if (this._isOpen)
		{
			String ifDoneVerificationInfo = "";
			IfDoneInfo ifDoneInfo = this._transaction.get_IfDoneInfo();
			if (ifDoneInfo != null)
			{
				if (this._tradeOption.value() == TradeOption.Better.value() &&
					(ifDoneInfo.get_LimitPriceForIfLimitDone() != null || ifDoneInfo.get_StopPirceForIfLimitDone() != null))
				{
					Price limitPrice = ifDoneInfo.get_LimitPriceForIfLimitDone();
					Price stopPrice = ifDoneInfo.get_StopPirceForIfLimitDone();
					ifDoneVerificationInfo += limitPrice == null ? "" : (Language.LMTPrompt + "=" + Price.toString(limitPrice) + TradingConsole.enterLine);
					ifDoneVerificationInfo += stopPrice == null ? "" : (Language.STPPrompt + "=" + Price.toString(stopPrice) + TradingConsole.enterLine);
				}
				if (this._tradeOption.value() == TradeOption.Stop.value() &&
					(ifDoneInfo.get_LimitPriceForIfStopDone() != null || ifDoneInfo.get_StopPirceForIfStopDone() != null))
				{
					Price limitPrice = ifDoneInfo.get_LimitPriceForIfStopDone();
					Price stopPrice = ifDoneInfo.get_StopPirceForIfStopDone();
					ifDoneVerificationInfo += limitPrice == null ? "" : (Language.LMTPrompt + "=" + Price.toString(limitPrice) + TradingConsole.enterLine);
					ifDoneVerificationInfo += stopPrice == null ? "" : (Language.STPPrompt + "=" + Price.toString(stopPrice) + TradingConsole.enterLine);
				}

				if (!StringHelper.isNullOrEmpty(ifDoneVerificationInfo))
				{
					ifDoneVerificationInfo = TradingConsole.enterLine + Language.IfDonePrompt + TradingConsole.enterLine + ifDoneVerificationInfo;
				}
			}
			return verificationInfo + ifDoneVerificationInfo;
		}

		verificationInfo += TradingConsole.enterLine
			+ Language.ForLiquidation + TradingConsole.enterLine;
		String relationOrderVerificationInfo = "";
		String relationOrderVerificationInfo2 = "";
		this.setVerificationOutstandingOrders();
		for (Iterator<RelationOrder> iterator = this._relationOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			relationOrderVerificationInfo2 = relationOrder.getVerificationInfo();
			if (StringHelper.isNullOrEmpty(relationOrderVerificationInfo))
			{
				relationOrderVerificationInfo = relationOrderVerificationInfo2;
			}
			else
			{
				relationOrderVerificationInfo += TradingConsole.enterLine + relationOrderVerificationInfo2;
			}
		}

		return verificationInfo + relationOrderVerificationInfo;
	}

	public static void unbindVerification(String dataSourceKey, BindingSource bindingSource)
	{
		TradingConsole.bindingManager.unbind(dataSourceKey, bindingSource);
	}

	public void addVerification(String dataSourceKey)
	{
		TradingConsole.bindingManager.add(dataSourceKey, this);
		this.setStyleForVerification(dataSourceKey);
		this.setBackground(dataSourceKey, GridBackgroundColor.verification);
	}

	public void updateForVerification(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
		this.setStyleForVerification(dataSourceKey);
		this.setBackground(dataSourceKey, GridBackgroundColor.verification);
	}

	//for DirectLiq
	public void set_RelationOrders(HashMap<Guid, RelationOrder> value)
	{
		this._relationOrders = value;
		for (RelationOrder relationOrder : value.values())
		{
			relationOrder.set_Owner(this);
		}
	}

	public void setVerificationOutstandingOrders()
	{
		if (this._relationOrders.size() > 0)
		{
			return;
		}
		for (Iterator<RelationOrder> iterator = this._transaction.get_MakeOrderAccount().getOutstandingOrders().values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			if (relationOrder.get_IsBuy() == !this._isBuy && relationOrder.get_IsSelected())
			{
				if (relationOrder.get_LiqLot().compareTo(BigDecimal.ZERO) > 0)
				{
					this._relationOrders.put(relationOrder.get_OpenOrderId(), relationOrder);
				}
			}
		}
	}

	public void setRelationOrders(ArrayList<RelationOrder> relationOrders)
	{
		for (RelationOrder relationOrder : relationOrders)
		{
			this._relationOrders.put(relationOrder.get_OpenOrderId(), relationOrder);
		}
	}

	public void initializeVerificationOutstanding(DataGrid grid, boolean ascend, boolean caseOn, IOpenCloseRelationSite openCloseRelationSite)
	{
		//unbind last outstanding orders
		this.unbindVerificationOutstanding();

		this._verificationOutstandingKey = Guid.newGuid().toString();

		this.setVerificationOutstandingOrders();
		boolean isMakeOrder2 = this._settingsManager.get_Customer().get_SingleAccountOrderType() == 2
			|| this._settingsManager.get_Customer().get_MultiAccountsOrderType() == 2;
		RelationOrder.initialize(isMakeOrder2, grid, this._verificationOutstandingKey, this._relationOrders.values(),
								 this._bindingSourceForVerificationOutstanding, openCloseRelationSite, BuySellType.Both);
		for (Iterator<RelationOrder> iterator = this._relationOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();

			//relationOrder.add(this._verificationOutstandingKey);
			relationOrder.update(this._verificationOutstandingKey);
		}
		int column = this._bindingSourceForVerificationOutstanding.getColumnByName(OutstandingOrderColKey.IsBuy);
		grid.sortColumn(column, ascend);
	}

	public void unbindVerificationOutstanding()
	{
		if (!StringHelper.isNullOrEmpty(this._verificationOutstandingKey) && this._bindingSourceForVerificationOutstanding != null)
		{
			RelationOrder.unbind(this._verificationOutstandingKey, this._bindingSourceForVerificationOutstanding);
		}
	}

	public void finalize() throws Throwable
	{
		this.unbindVerificationOutstanding();

		super.finalize();
	}

	//Make order
	public String getMakeOrderConfirmXml(OperateType operateType)
	{
		String xml = "";
		if (operateType.equals(OperateType.Assign))
		{
			xml = "<Order " +
				"ID=\'" + this._id.toString() + "\' " +
				"IsOpen=\'" + XmlConvert.toString(this._isOpen) + "\' " +
				"Lot=\'" + this.get_LotString() + "\'>";
		}
		else if (operateType.equals(OperateType.MultiSpotTrade) ||
				 operateType.equals(OperateType.Method2SpotTrade) ||
				 operateType.equals(OperateType.SingleSpotTrade) ||
				 operateType.equals(OperateType.Limit) ||
				 operateType.equals(OperateType.DirectLiq) ||
				 operateType.equals(OperateType.OneCancelOther))
		{
			xml = "<Order ";
			xml += "ID=\'" + this._id.toString() + "\'";
			xml += "TradeOption=\'" + XmlConvert.toString(this._tradeOption.value()) + "\' ";
			xml += "IsOpen=\'" + XmlConvert.toString(this._isOpen) + "\' ";
			xml += "IsBuy=\'" + XmlConvert.toString(this._isBuy) + "\' ";
			xml += "SetPrice=\'" + Price.toString(this._setPrice) + "\' ";
			xml += "SetPrice2=\'" + Price.toString(this._setPrice2) + "\' ";
			xml += "DQMaxMove=\'" + XmlConvert.toString(this._dQMaxMove) + "\' ";
			if (this._makeOrderAccount != null && this._makeOrderAccount.get_PriceTimestamp() != null)
			{
				xml += "PriceTimestamp=\'" + XmlConvert.toString(this._makeOrderAccount.get_PriceTimestamp()) + "\' ";
			}
			if (this._makeOrderAccount != null)
			{
				xml += "PriceIsQuote=\'" + XmlConvert.toString(this._makeOrderAccount.get_PriceIsQuote()) + "\' ";
			}
			xml += "Lot=\'" + this.get_LotString() + "\' ";
			xml += "OriginalLot=\'" + this.get_LotString() + "\' ";

			IfDoneInfo ifDoneInfo = this._transaction.get_IfDoneInfo();
			if (ifDoneInfo != null && this._transaction.get_OrderType().value() == OrderType.Limit.value())
			{
				if (this._tradeOption.value() == TradeOption.Better.value() &&
					(ifDoneInfo.get_LimitPriceForIfLimitDone() != null || ifDoneInfo.get_StopPirceForIfLimitDone() != null))
				{
					Price limitPrice = ifDoneInfo.get_LimitPriceForIfLimitDone();
					Price stopPrice = ifDoneInfo.get_StopPirceForIfLimitDone();
					String ifDone = "&lt;IfDone " +
						(limitPrice == null ? "" : " LimitPrice=&quot;" + Price.toString(limitPrice) + "&quot;") +
						(stopPrice == null ? "" : " StopPrice=&quot;" + Price.toString(stopPrice) + "&quot;") +
						" /&gt;";

					xml += " Extension=\"" + ifDone + "\"";
				}
				if (this._tradeOption.value() == TradeOption.Stop.value() &&
					(ifDoneInfo.get_LimitPriceForIfStopDone() != null || ifDoneInfo.get_StopPirceForIfStopDone() != null))
				{
					Price limitPrice = ifDoneInfo.get_LimitPriceForIfStopDone();
					Price stopPrice = ifDoneInfo.get_StopPirceForIfStopDone();
					String ifDone = "&lt;IfDone " +
						(limitPrice == null ? "" : " LimitPrice=&quot;" + Price.toString(limitPrice) + "&quot;") +
						(stopPrice == null ? "" : " StopPrice=&quot;" + Price.toString(stopPrice) + "&quot;") +
						" /&gt;";

					xml += " Extension=\"" + ifDone + "\"";
				}
			}

			xml += ">";
		}
		for (Iterator<RelationOrder> iterator = this._relationOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			xml += relationOrder.getMakeOrderConfirmXml();
		}

		xml += "</Order>";

		return xml;
	}

	public void makeOrderConfirm()
	{
		this._tradingConsole.setOrder(this);
		//this._transaction.get_Orders().put(this._id, this);
		this._settingsManager.verifyAccountCurrency(this);

		OperateWhichOrderUI operateWhichOrderUI = this.getOperateWhichOrderUI();
		if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
			|| operateWhichOrderUI.equals(OperateWhichOrderUI.WorkingOrderList))
		{
			this._tradingConsole.setWorkingOrder(this);
		}
		if (operateWhichOrderUI.equals(OperateWhichOrderUI.Both)
			|| operateWhichOrderUI.equals(OperateWhichOrderUI.OpenOrderList))
		{
			this._tradingConsole.setOpenOrder(this);
		}
		//Refresh Order UI
		this.add();
	}

	public String getLogAction()
	{
		String submitor = this._transaction.get_Submitor();
		String action = "";
		if (PublicParametersManager.version.equalsIgnoreCase("CHS")
			|| PublicParametersManager.version.equalsIgnoreCase("CHT"))
		{
			String accountCode = this.get_AccountCode();
			if (this._phase == Phase.Placing)
			{
				String message = this.get_Message();
				message = (StringHelper.isNullOrEmpty(message)) ? "" : ", " + message;
				action = submitor + LogLanguage.Placing + accountCode + LogLanguage.At + this.getLogActionOrderInfo() + LogLanguage.Send + message;
				action += (StringHelper.isNullOrEmpty(this.get_RefCode())) ? "" : LogLanguage.RefrenceCode + " " + this.get_RefCode();
			}
			else if (this._phase == Phase.Placed)
			{
				action = submitor + LogLanguage.Set + accountCode + LogLanguage.At + this.getLogActionOrderInfo() + LogLanguage.Message +
					LogLanguage.RefrenceCode + " " + this.get_RefCode();
			}
			else if (this._phase == Phase.Executed || this._phase == Phase.Completed)
			{
				if (this._transaction.get_OrderType() == OrderType.Risk)
				{
					String setPrice = this.get_SetPriceString();
					action = LogLanguage.Risk + accountCode + LogLanguage.Set + LogLanguage.At + setPrice + this.get_IsBuyLongString() + this.get_LotString() +
						LogLanguage.Lot + this.get_InstrumentCode() + LogLanguage.RefrenceCode2 + this._code;
				}
				else
				{
					action = submitor + LogLanguage.Set + accountCode + LogLanguage.At + this.getLogActionOrderInfo() + LogLanguage.Executed +
						LogLanguage.RefrenceCode2 + " " + this.get_RefCode();
				}
			}
			else if (this._phase == Phase.Cancelled)
			{
				action = submitor + LogLanguage.Set + accountCode + LogLanguage.At + this.getLogActionOrderInfo() + LogLanguage.Cancelled; // + " " +
				//this.get_Message();
			}
			else if (this._phase == Phase.Deleted)
			{
				action = accountCode + LogLanguage.ExecutedAt + LogLanguage.At + this.getLogActionOrderInfo() + LogLanguage.Deleted + this.get_Message();
			}
		}
		else
		{
			if (this._phase == Phase.Placing)
			{
				String message = this.get_Message();
				message = (StringHelper.isNullOrEmpty(message)) ? "" : ", " + message;
				action = submitor + " " + LogLanguage.Placing + " " +
					this.getLogActionOrderInfo() + LogLanguage.Send + message;
			}
			else if (this._phase == Phase.Placed)
			{
				action = LogLanguage.OrderTo + " " + this.getLogActionOrderInfo() + " " + LogLanguage.Placed + LogLanguage.RefrenceCode + " " +
					this.get_RefCode();
			}
			else if (this._phase == Phase.Executed || this._phase == Phase.Completed)
			{
				if (this._transaction.get_OrderType() == OrderType.Risk)
				{
					action = LogLanguage.Risk + " " + this.getLogActionOrderInfo() + LogLanguage.RefrenceCode2 + this._code;
				}
				else
				{
					action = this.getLogActionOrderInfo() + " " + LogLanguage.Executed + LogLanguage.RefrenceCode2 + " " +
						this.get_RefCode();
				}
			}
			else if (this._phase == Phase.Cancelled)
			{
				action = this.getLogActionOrderInfo() + " " + LogLanguage.Cancelled + " " + this.get_Message();
			}
			else if (this._phase == Phase.Deleted)
			{
				action = LogLanguage.OrderTo + " " + this.getLogActionOrderInfo() + " " + LogLanguage.Deleted + this.get_Message();
			}
		}

		if (this._isOpen)
		{
			String ifDoneVerificationInfo = "";
			IfDoneInfo ifDoneInfo = this._transaction.get_IfDoneInfo();
			if (ifDoneInfo != null)
			{
				if (this._tradeOption.value() == TradeOption.Better.value() &&
					(ifDoneInfo.get_LimitPriceForIfLimitDone() != null || ifDoneInfo.get_StopPirceForIfLimitDone() != null))
				{
					Price limitPrice = ifDoneInfo.get_LimitPriceForIfLimitDone();
					Price stopPrice = ifDoneInfo.get_StopPirceForIfLimitDone();
					ifDoneVerificationInfo += limitPrice == null ? "" : (Language.LMTPrompt + "=" + Price.toString(limitPrice) + ";");
					ifDoneVerificationInfo += stopPrice == null ? "" : (Language.STPPrompt + "=" + Price.toString(stopPrice) + ";");
				}
				if (this._tradeOption.value() == TradeOption.Stop.value() &&
					(ifDoneInfo.get_LimitPriceForIfStopDone() != null || ifDoneInfo.get_StopPirceForIfStopDone() != null))
				{
					Price limitPrice = ifDoneInfo.get_LimitPriceForIfStopDone();
					Price stopPrice = ifDoneInfo.get_StopPirceForIfStopDone();
					ifDoneVerificationInfo += limitPrice == null ? "" : (Language.LMTPrompt + "=" + Price.toString(limitPrice) + ";");
					ifDoneVerificationInfo += stopPrice == null ? "" : (Language.STPPrompt + "=" + Price.toString(stopPrice) + ";");
				}

				if (!StringHelper.isNullOrEmpty(ifDoneVerificationInfo))
				{
					ifDoneVerificationInfo = "; " + Language.IfDonePrompt + ":" + ifDoneVerificationInfo;
				}
			}
			return action + ifDoneVerificationInfo;
		}
		else
		{
			String openOrderInfo = "";
			String openOrderInfo2 = "";
			for (Iterator<RelationOrder> iterator = this._relationOrders.values().iterator(); iterator.hasNext(); )
			{
				RelationOrder relationOrder = iterator.next();
				openOrderInfo2 = relationOrder.getVerificationInfo();
				if (StringHelper.isNullOrEmpty(openOrderInfo))
				{
					openOrderInfo = openOrderInfo2;
				}
				else
				{
					openOrderInfo += TradingConsole.enterLine + openOrderInfo2;
				}
			}
			if (!StringHelper.isNullOrEmpty(openOrderInfo))
			{
				openOrderInfo = "; " + Language.ForLiquidation + openOrderInfo;
			}

			return action + openOrderInfo;
		}
	}

	public String getLogActionForRemoveByRisk()
	{
		String action = "";
		if (PublicParametersManager.version.equalsIgnoreCase("CHS")
			|| PublicParametersManager.version.equalsIgnoreCase("CHT"))
		{
			action = this.get_AccountCode() + LogLanguage.ExecutedAt + LogLanguage.At + this.getLogActionOrderInfo() + LogLanguage.RemoveByRisk;
		}
		else
		{
			action = this.getLogActionOrderInfo() + " " + LogLanguage.RemoveByRisk;
		}
		return action;
	}

	public String getLogActionForRequestCancelLMTOrder()
	{
		String submitor = this._transaction.get_Submitor();
		String action = "";
		if (PublicParametersManager.version.equalsIgnoreCase("CHS")
			|| PublicParametersManager.version.equalsIgnoreCase("CHT"))
		{
			action = submitor + LogLanguage.RequestCancelLMTOrder + LogLanguage.At +
				this.getLogActionOrderInfo() + LogLanguage.Message2;
		}
		else
		{
			action = submitor + " " + LogLanguage.RequestCancelLMTOrder + " " +
				this.getLogActionOrderInfo();
		}
		return action;
	}

	public String getLogActionOrderInfo()
	{
		String setPrice = this.get_SetPriceString();
		String action = "";
		OrderType orderType = this._transaction.get_OrderType();
		String orderTypeString = "";
		if (PublicParametersManager.version.equalsIgnoreCase("CHS")
			|| PublicParametersManager.version.equalsIgnoreCase("CHT"))
		{
			if (orderType != OrderType.SpotTrade && orderType != OrderType.Risk)
			{
				orderTypeString = this.get_OrderTypeString() + " ";
			}
			action = setPrice + orderTypeString + this.get_IsBuyLongString() + this.get_LotString() + LogLanguage.Lot + this.get_InstrumentCode();
		}
		else
		{
			if (orderType != OrderType.SpotTrade && orderType != OrderType.Risk)
			{
				orderTypeString = this.get_OrderTypeString() + " ";
			}
			action = orderTypeString + this.get_IsBuyLongString() + " " + this.get_LotString() + " " + this.get_InstrumentCode() +
				( (StringHelper.isNullOrEmpty(setPrice)) ? "" : " " + LogLanguage.At + " ") + setPrice + " " + LogLanguage.For + " " + this.get_AccountCode();
		}
		return action;
	}

	public Order clone(Transaction transaction)
	{
		Order order = new Order(this._tradingConsole, this._settingsManager);
		order._transaction = transaction;

		order._id = Guid.newGuid();
		order._tradeOption = this._tradeOption;
		order._isOpen = this._isOpen;
		order._isBuy = this._isBuy;
		order._setPrice = this._setPrice;
		order._setPrice2 = this._setPrice2;
		order._lot = this._lot;
		order._lotBalance = this._lotBalance;
		order._code = "";
		order._executePrice = this._executePrice;
		order._commissionSum = this._commissionSum;
		order._levySum = this._levySum;
		order._peerOrderCodes = this._peerOrderCodes;
		order._interestPerLot = this._interestPerLot;
		order._storagePerLot = this._storagePerLot;
		order._floatTradingItem = this._floatTradingItem.clone();
		order._pLTradingItem = this._pLTradingItem.clone();
		order._dQMaxMove = this._dQMaxMove;
		order._displayDecimals = this._displayDecimals;

		return order;
	}

	public void dump(PrintWriter writer)
	{
		DateTime submitTime = this.get_Transaction().get_SubmitTime();
		DateTime executeTime = this.get_Transaction().get_ExecuteTime();

		String info = "SubmitTime = " + submitTime == null ? "-" : submitTime.toString(DateTime.fullFormat) + "; "
			+ "ExecuteTime = " + executeTime == null ? "-" : executeTime.toString(DateTime.fullFormat) + "; "
			+ "Account = " + this.get_AccountCode() + "; "
			+ "Instrument = " + this.get_InstrumentCode() + "; "
			+ "Lot = " + this.get_LotString() + "; "
			+ "LotBanlance = " + this.get_LotBalanceString() + "; "
			+ this.get_IsBuyString() + "; " + this.get_IsOpenString() + "; " + this.get_PhaseString();

		writer.println(info);
	}

	public static SetPriceError checkLMTOrderSetPrice(boolean isCheckAccept, Instrument instrument, boolean isBuy, TradeOption previousTradeOption,
		Price setPrice, Price marketPrice, boolean isOpen)
	{
		int variation = (isCheckAccept) ? instrument.get_AcceptLmtVariation(isOpen) : instrument.get_CancelLmtVariation();

		SetPriceError setPriceError = SetPriceError.Ok;
		TradeOption currentTradeOption;
		if (setPrice == null || marketPrice == null)
		{
			return setPriceError;
		}

		//double diffValue = Price.toDouble(setPrice) - Price.toDouble(marketPrice);
		int diffValue = Math.abs(Price.subStract(setPrice, marketPrice));
		if (instrument.get_IsNormal())
		{
			//if (Math.abs(diffValue) < (cancelLmtVariation / denominator))
			if (diffValue < variation)
			{
				setPriceError = SetPriceError.SetPriceTooCloseMarket;
			}
			if ( (!isBuy && (Price.more(setPrice, marketPrice))) ||
				(isBuy && (!Price.more(setPrice, marketPrice))))
			{
				currentTradeOption = TradeOption.Better;
			}
			else
			{
				currentTradeOption = TradeOption.Stop;
			}
		}
		else
		{
			//if (Math.abs(diffValue) < (cancelLmtVariation / denominator))
			if (diffValue < variation)
			{
				setPriceError = SetPriceError.SetPriceTooCloseMarket;
			}
			if ( (isBuy && (Price.more(setPrice, marketPrice))) ||
				(!isBuy && (!Price.more(setPrice, marketPrice))))
			{
				currentTradeOption = TradeOption.Better;
			}
			else
			{
				currentTradeOption = TradeOption.Stop;
			}
		}
		if (previousTradeOption != currentTradeOption)
		{
			setPriceError = SetPriceError.InvalidSetPrice;
		}

		return setPriceError;
	}

	public static SetPriceError checkLMTOrderSetPrice(Order order)
	{
		Instrument instrument = order.get_Transaction().get_Instrument();
		Price marketPrice = instrument.get_LastQuotation().getBuySell(order.get_IsBuy());
		return Order.checkLMTOrderSetPrice(false, instrument, order.get_IsBuy(), order.get_TradeOption(), order.get_SetPrice(), marketPrice, order.get_IsOpen());
	}

	public static String checkSpotSetPrice(Order order)
	{
		if (order.get_Transaction().get_OrderType().value() == OrderType.SpotTrade.value())
		{
			Instrument instrument = order.get_Transaction().get_Instrument();
			boolean isBuy = order.get_IsBuy();
			Price marketPrice = instrument.get_LastQuotation().getBuySell(isBuy);
			Price setPrice = order.get_SetPrice();
			int diffValue = Price.subStract(setPrice, marketPrice);
			if ( (isBuy && diffValue > 10) || (!isBuy && diffValue < -10))
			{
				return StringHelper.format(Language.setPriceFarFromMaketPrice,
										   new Object[]
										   {setPrice.get_NormalizedPrice(), marketPrice.get_NormalizedPrice()});
			}
		}
		return StringHelper.empty;
	}

	private static DataTable dataTable = null;
	public static DataTable createStructure()
	{
		if (Order.dataTable == null)
		{
			Order.dataTable = new DataTable("Order");
			Order.dataTable.get_Columns().add("ID", Guid.class);
			Order.dataTable.get_Columns().add("TradeOption", Short.class);
			Order.dataTable.get_Columns().add("IsOpen", Boolean.class);
			Order.dataTable.get_Columns().add("IsBuy", Boolean.class);
			Order.dataTable.get_Columns().add("SetPrice", String.class);
			Order.dataTable.get_Columns().add("SetPrice2", String.class);
			Order.dataTable.get_Columns().add("Lot", BigDecimal.class);
			Order.dataTable.get_Columns().add("LotBalance", BigDecimal.class);
			Order.dataTable.get_Columns().add("Code", String.class);
			Order.dataTable.get_Columns().add("ExecutePrice", String.class);
			Order.dataTable.get_Columns().add("CommissionSum", BigDecimal.class);
			Order.dataTable.get_Columns().add("LevySum", BigDecimal.class);
			Order.dataTable.get_Columns().add("PeerOrderCodes", String.class);
			Order.dataTable.get_Columns().add("InterestPerLot", BigDecimal.class);
			Order.dataTable.get_Columns().add("StoragePerLot", BigDecimal.class);
			Order.dataTable.get_Columns().add("TradePLFloat", BigDecimal.class);
			Order.dataTable.get_Columns().add("InterestPLFloat", BigDecimal.class);
			Order.dataTable.get_Columns().add("StoragePLFloat", BigDecimal.class);
			Order.dataTable.get_Columns().add("InterestRate", BigDecimal.class);
			//dataTable.get_Columns().add("Sequence",Integer.class);
			Order.dataTable.get_Columns().add("DQMaxMove", Integer.class);
			Order.dataTable.get_Columns().add("LivePrice", String.class);
		}
		return Order.dataTable;
	}

	public String getRelationsInfoForEmail()
	{
		String info = "";
		for (Iterator<RelationOrder> iterator = this._relationOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			info += relationOrder.getInfoForEmail();
		}
		return info;
	}

	public BigDecimal adjustLot(BigDecimal value)
	{
		if (value.compareTo(this._lot) < 0)
		{
			MakeOrderAccount makeOrderAccount = this._transaction.get_MakeOrderAccount();
			if (makeOrderAccount != null)
			{
				makeOrderAccount.adjustLot(value.subtract(this._lot), this._isBuy);
			}

			this._lot = value;
			if (this._isOpen)
			{
				this.set_LotBalance(value);
			}
		}
		return this._lot;
	}

	public void set_OrderModification(OrderModification orderModification)
	{
		this._orderModification = orderModification;
	}

	public OrderModification get_OrderModification()
	{
		return this._orderModification;
	}

	public PLNotValued get_PLNotValued()
	{
		return this._PLNotValued;
	}

	public void applyModification(OrderModifyResult result)
	{
		if (result == OrderModifyResult.Confirmed)
		{
			String newLot = this.get_OrderModification().get_NewLot();
			if (newLot != null)
			{
				this._lot = this._lotBalance = new BigDecimal(newLot);
			}

			Instrument instrument = this._transaction.get_Instrument();
			String newSetPrice = this.get_OrderModification().get_NewSetPrice();
			if (newSetPrice != null)
			{
				this._setPrice = Price.create(newSetPrice, instrument.get_NumeratorUnit(), instrument.get_Denominator());
			}
			String newSetPrice2 = this.get_OrderModification().get_NewSetPrice2();
			if (newSetPrice2 != null)
			{
				this._setPrice2 = Price.create(newSetPrice2, instrument.get_NumeratorUnit(), instrument.get_Denominator());
			}
		}
		String action = Language.ModifyInstructionPrompt + " " + this.getModificationInfo() + " " +
			(result == OrderModifyResult.Confirmed ? Language.IsConfirmed : Language.IsRejected);
		this._tradingConsole.saveLog(result == OrderModifyResult.Confirmed ? LogCode.Confirmed : LogCode.Cancelled, action, this._transaction.get_Id(),this._transaction.get_Account().get_Id());
		this._orderModification = null;
		this.update();
	}

	private String getModificationInfo()
	{
		if (this._orderModification == null)
		{
			return "";
		}
		String lotInfo = this._orderModification.get_NewLot() == null ? "" : Language.OrderLMTlblLot + " = " + this._orderModification.get_NewLot();
		String setPriceInfo = this._orderModification.get_NewSetPrice() == null ? "" :
			( (this._tradeOption == TradeOption.Stop ? Language.OrderLMTlblSetPriceA2 : Language.OrderLMTlblSetPriceA3) + " = " +
			 this._orderModification.get_NewSetPrice());
		String setPrice2Info = this._orderModification.get_NewSetPrice2() == null ? "" :
			Language.OrderLMTlblSetPriceA2 + " = " + this._orderModification.get_NewSetPrice2();

		return lotInfo + (lotInfo.length() == 0 ? "" : "; ") + setPriceInfo + (setPriceInfo.length() == 0 ? "" : "; ") + setPrice2Info;
	}

	private MakeOrderAccount _makeOrderAccount = null;
	public void set_PriceInfo(MakeOrderAccount makeOrderAccount)
	{
		this._makeOrderAccount = makeOrderAccount;
	}

	public void changeLotForMatching(BigDecimal lot)
	{
		if (this._isOpen)
		{
			this._lot = this._lotBalance = lot;
		}
		else
		{
			this._lot = lot;
		}
	}

	public void setPhase(Phase phase)
	{
		this._phase = phase;
		if(!this._phase.equals(Phase.Placed) && !this._phase.equals(Phase.Placing))
		{
			TradingConsole.bindingManager.remove(Order.notConfirmedPendingOrdersKey, this);
		}
	}

	private static class OrderComparatorForAdjustingLot implements Comparator<Order>
	{
		public int compare(Order left, Order right)
		{
			int leftIsOpen = left._isOpen ? 1 : 0;
			int righIsOpen = right._isOpen ? 1 : 0;
			int result = leftIsOpen - righIsOpen;
			if (result != 0)
			{
				return result;
			}

			result = left._lot.compareTo(right._lot);
			if (result != 0)
			{
				return -result;
			}

			return left.get_Account().get_Code().compareTo(right.get_Account().get_Code());
		}

		public boolean equals(Object obj)
		{
			return false;
		}
	}

	public static class PLNotValued
	{
		private ArrayList<DayPLNotValuedItem> _dayPLNotValuedList = new ArrayList<DayPLNotValuedItem> ();

		private double _interest = 0;
		private double _storage = 0;

		public double get_Interest()
		{
			return this._interest;
		}

		public double get_Storage()
		{
			return this._storage;
		}

		public void set(String dayInterestNotValued, String dayStorageNotValued)
		{
			this._dayPLNotValuedList.clear();

			String[] dayInterestNotValuedArray = StringHelper.split(dayInterestNotValued, "|");
			String[] dayStorageNotValuedArray = StringHelper.split(dayStorageNotValued, "|");
			int length = Math.max(dayInterestNotValuedArray.length, dayStorageNotValuedArray.length);
			for (int index = 0; index < length; index++)
			{
				double interest = dayInterestNotValuedArray.length > index ? Double.parseDouble(dayInterestNotValuedArray[index]) : 0.0d;
				double storage = dayStorageNotValuedArray.length > index ? Double.parseDouble(dayStorageNotValuedArray[index]) : 0.0d;

				this._dayPLNotValuedList.add(new DayPLNotValuedItem(interest, storage));
			}
		}

		public void addDayPLNotValuedItem(DayPLNotValuedItem dayPLNotValued)
		{
			this._dayPLNotValuedList.add(dayPLNotValued);
		}

		public void caculatePLNotValued(CurrencyRate currencyRate)
		{
			this._interest = this._storage = 0.0d;
			for (DayPLNotValuedItem dayPLNotValuedItem : this._dayPLNotValuedList)
			{
				this._interest += currencyRate.exchange(dayPLNotValuedItem.get_Interest());
				this._storage += currencyRate.exchange(dayPLNotValuedItem.get_Storage());
			}
		}
	}

	public static class DayPLNotValuedItem
	{
		private double _interest;
		private double _storage;

		public DayPLNotValuedItem(double interest, double storage)
		{
			this._interest = interest;
			this._storage = storage;
		}

		public double get_Interest()
		{
			return this._interest;
		}

		public double get_Storage()
		{
			return this._storage;
		}
	}
}
