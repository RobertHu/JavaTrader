package tradingConsole.settings;

import java.math.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;

import com.jidesoft.grid.*;
import com.jidesoft.grid.TableColumnChooser;
import framework.*;
import framework.DateTime;
import framework.diagnostics.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.framework.*;
import tradingConsole.ui.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.fontHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;

public class MakeOrderAccount
{
	public static ComparatorForAdjustingLot comparatorForAdjustingLot = new ComparatorForAdjustingLot();
	//must auto create it
	private String _outstandingKey;
	private BindingSource _bindingSourceForOutstanding;

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;

	//maybe used them when make order use agent account
	private BigDecimal _buyLot= BigDecimal.ZERO;
	private BigDecimal _sellLot= BigDecimal.ZERO;

	private Price _buySetPrice;
	private Price _sellSetPrice;
	private TradeOption _buyTradeOption;
	private TradeOption _sellTradeOption;

	private BuySellType _buySellType;

	private boolean _isBuyForCurrent;
	private String _isBuyForCombo = "";

	//For OneCancelOther
	private Price _buySetPrice2;
	private Price _sellSetPrice2;
	private TradeOption _buyTradeOption2;
	private TradeOption _sellTradeOption2;

	private DateTime _priceTimestamp = null;
	private boolean _priceIsQuote = false;

	private int _DQMaxMove = 0;

	private Account _account;
	private Instrument _instrument;
	private HashMap<Guid, RelationOrder> _outstandingOrders;
	//private IOpenCloseRelationBaseSite _site;


	public static MakeOrderAccount create(TradingConsole tradingConsole, SettingsManager settingsManager, Guid accountId, Guid instrumentId)
	{
		return new MakeOrderAccount(tradingConsole, settingsManager, accountId, instrumentId);
	}

	public static MakeOrderAccount create(TradingConsole tradingConsole, SettingsManager settingsManager, Account account, Instrument instrument)
	{
		return MakeOrderAccount.create(tradingConsole, settingsManager, account.get_Id(), instrument.get_Id());
	}

	/*public static MakeOrderAccount create(MakeOrderAccount orginalAccount)
	{
		return MakeOrderAccount.create(orginalAccount._tradingConsole, orginalAccount._settingsManager, orginalAccount._account, orginalAccount._instrument);
	}*/

	private MakeOrderAccount(TradingConsole tradingConsole, SettingsManager settingsManager, Guid accountId, Guid instrumentId)
	{
		this._outstandingOrders = new HashMap<Guid, RelationOrder> ();

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;

		this._account = this._settingsManager.getAccount(accountId);
		this._instrument = this._settingsManager.getInstrument(instrumentId);

		this._buySetPrice = null;
		this._sellSetPrice = null;
		this._buyTradeOption = TradeOption.None;
		this._sellTradeOption = TradeOption.None;
		this._buyTradeOption2 = TradeOption.None;
		this._sellTradeOption2 = TradeOption.None;
		this._buyLot = this._sellLot = this.getDefaultLot();

		/*if(this._settingsManager.get_Accounts().size() > 1
		   && !this._account.get_IsLocked() && !this._instrument.get_Code().startsWith("#"))
		{
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._account.get_TradePolicyId(),
				this._instrument.get_Id());
			BigDecimal defaultLot = tradePolicyDetail.get_DefaultLot();
			BigDecimal maxLot = this._instrument.get_MaxDQLot();
			if (this._instrument.get_MaxDQLot().compareTo(this._instrument.get_MaxOtherLot()) > 0)
				maxLot = this._instrument.get_MaxOtherLot();
			defaultLot = defaultLot.compareTo(maxLot) > 0 ? maxLot : defaultLot;
			if(defaultLot.compareTo(BigDecimal.ZERO) > 0)
			{
				if(this._account.get_IsSplitLot() || defaultLot.compareTo(new BigDecimal(defaultLot.intValue())) == 0) this._sellLot = this._buyLot = defaultLot;
			}
		}*/
		//this._site = site;
	}

	private BigDecimal getDefaultLot()
	{
		Account account = this.get_Account();
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(account.get_TradePolicyId(),
			this._instrument.get_Id());
		return AppToolkit.getDefaultLot(this._instrument, true, tradePolicyDetail, account);
	}

	//For OneCancelOther
	public void set_BuyTradeOption(TradeOption value)
	{
		this._buyTradeOption = value;
	}

	//For OneCancelOther
	public void set_SellTradeOption(TradeOption value)
	{
		this._sellTradeOption = value;
	}

	//For OneCancelOther
	public TradeOption getTradeOption(boolean isBuy)
	{
		return (isBuy) ? this._buyTradeOption : this._sellTradeOption;
	}

	//For OneCancelOther
	public void set_BuyTradeOption2(TradeOption value)
	{
		this._buyTradeOption2 = value;
	}

	//For OneCancelOther
	public void set_SellTradeOption2(TradeOption value)
	{
		this._sellTradeOption2 = value;
	}

	//For OneCancelOther
	public TradeOption getTradeOption2(boolean isBuy)
	{
		return (isBuy) ? this._buyTradeOption2 : this._sellTradeOption2;
	}

	//For OneCancelOther
	public void set_BuySetPrice2(Price value)
	{
		this._buySetPrice2 = value;
	}

	//For OneCancelOther
	public void set_SellSetPrice2(Price value)
	{
		this._sellSetPrice2 = value;
	}

	public String getSetPrice2String(boolean isBuy)
	{
		return Price.toString( (isBuy) ? this._buySetPrice2 : this._sellSetPrice2);
	}

	public Price getSetPrice(boolean isBuy)
	{
		return (isBuy) ? this._buySetPrice : this._sellSetPrice;
	}

	public Price getSetPrice2(boolean isBuy)
	{
		return (isBuy) ? this._buySetPrice2 : this._sellSetPrice2;
	}

	public BigDecimal get_BuyLot()
	{
		return this._buyLot;
	}

	public void set_BuyLot(BigDecimal value)
	{
		this._buyLot = value;
	}

	public BigDecimal get_SellLot()
	{
		return this._sellLot;
	}

	public void set_SellLot(BigDecimal value)
	{
		this._sellLot = value;
	}

	public BuySellType get_BuySellType()
	{
		return this._buySellType;
	}

	public void set_BuySellType(BuySellType value)
	{
		this._buySellType = value;
	}

	public String get_IsBuyForCombo()
	{
		return this._isBuyForCombo;
	}

	public DateTime get_PriceTimestamp()
	{
		return this._priceTimestamp;
	}

	public boolean get_PriceIsQuote()
	{
		return this._priceIsQuote;
	}

	public void set_PriceInfo(DateTime timestamp, boolean priceIsQuote)
	{
		String info = StringHelper.format("MakeOrderAccount.[set_PriceInfo] timestamp={0}, priceIsQuote={1}", new Object[]{timestamp, priceIsQuote});
		TradingConsole.traceSource.trace(TraceType.Information, info);

		this._priceTimestamp = timestamp;
		this._priceIsQuote = priceIsQuote;
	}

	public void set_IsBuyForCombo(String value)
	{
		if (StringHelper.isNullOrEmpty(value))
		{
			value = this._isBuyForCurrent ? Language.Buy : Language.Sell;
		}
		this._isBuyForCombo = value;
		this.setIsBuyForCurrent(value.equalsIgnoreCase(Language.Buy));
	}

	private void setIsBuyForCurrent(boolean value)
	{
		if (this._buySellType != BuySellType.Both)
		{
			this._buySellType = (value) ? BuySellType.Buy : BuySellType.Sell;
		}
		this._isBuyForCurrent = value;
	}

	public boolean get_IsBuyForCurrent()
	{
		return this._isBuyForCurrent;
	}

	public void set_IsBuyForCurrent(boolean value)
	{
		this.setIsBuyForCurrent(value);
		this._isBuyForCombo = this._isBuyForCurrent ? Language.Buy : Language.Sell;
	}

	public BigDecimal get_LotCurrent()
	{
		return (this._isBuyForCurrent) ? this._buyLot : this._sellLot;
	}

	public int get_DQMaxMove()
	{
		return this._DQMaxMove;
	}

	public void set_DQMaxMove(int value)
	{
		this._DQMaxMove = value;
	}

	public HashMap<Guid, RelationOrder> getPlaceRelation()
	{
		HashMap<Guid, RelationOrder> placeRelation = null;

		HashMap<Guid, RelationOrder> outstandingOrders = this.getOutstandingOrders();
		for (RelationOrder relationOrder : outstandingOrders.values())
		{
			if (relationOrder.get_IsSelected() && relationOrder.get_CloseLot().compareTo(BigDecimal.ZERO) > 0)
			{
				if (placeRelation == null)
					placeRelation = new HashMap<Guid, RelationOrder> ();
				placeRelation.put(relationOrder.get_OpenOrderId(),relationOrder);
			}
		}

		return placeRelation;
	}

	public tradingConsole.ui.grid.BindingSource get_BindingSourceForOutstanding()
	{
		return this._bindingSourceForOutstanding;
	}

	public Account get_Account()
	{
		return this._account;
	}

	public Instrument get_Instrument()
	{
		return this._instrument;
	}

	public HashMap<Guid, RelationOrder> getOutstandingOrders()
	{
		return this._outstandingOrders;
	}

	public void setOutstandingOrdersForMakeLiquidationOrder(Instrument instrument, boolean forMakeOrderForm)
	{
		if (this._outstandingOrders.size() > 0)
		{
			return;
		}
		for (Iterator<Order> iterator = this._tradingConsole.get_OpenOrders().values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();

			if (order.get_Transaction().get_Instrument() == instrument && order.get_Transaction().get_Account() == this._account)
			{
				if(!forMakeOrderForm || order.getAvailableLotBanlance(false, null).compareTo(BigDecimal.ZERO) > 0)
				{
					Guid orderId = order.get_Id();
					RelationOrder outstandingOrder = new RelationOrder(this._tradingConsole, this._settingsManager, order);
					outstandingOrder.set_IsSelected(order.get_Close());
					this._outstandingOrders.put(orderId, outstandingOrder);
				}
			}
		}
	}

	//exclude make Liquidation order.....
	private void setOutstandingOrders(BuySellType buySellType, Order mapOrder, Boolean isSpot, Boolean isMakeLimitOrder)
	{
		if (mapOrder == null)
		{
			this.setOutstandingOrders(buySellType, isSpot, isMakeLimitOrder);
		}
		else
		{
			this.setOutstandingOrders(mapOrder, isSpot, isMakeLimitOrder);
		}
	}

	//Used by Limit,MOO,MOC,MKT,OCO for mapOrder
	private void setOutstandingOrders(Order mapOrder, Boolean isSpot, Boolean isMakeLimitOrder)
	{
		/*if (this._outstandingOrders.size() > 0 || this._account.get_Type() == AccountType.Agent)
		{
			Iterator<RelationOrder> relationOrders = this._outstandingOrders.values().iterator();
			while(relationOrders.hasNext())
			{
				relationOrders.next().set_IsMakeLimitOrder(isMakeLimitOrder);
			}
			return;
		}*/
		this._outstandingOrders.clear();
		Guid orderId = mapOrder.get_Id();
		RelationOrder outstandingOrder = new RelationOrder(this._tradingConsole, this._settingsManager, mapOrder, isSpot, isMakeLimitOrder);

		BigDecimal avaiableCloseLot = outstandingOrder.get_LiqLot();
		if(avaiableCloseLot.compareTo(BigDecimal.ZERO) > 0)
		{
			TradePolicyDetail tradePolicyDetail = this.getTradePolicyDetail();
			BigDecimal lot = AppToolkit.fixLot(avaiableCloseLot, false, tradePolicyDetail, this._account);
			if(avaiableCloseLot.compareTo(lot) <= 0)
			{
				outstandingOrder.set_LiqLot(lot);

				outstandingOrder.set_IsMakeLimitOrder(isMakeLimitOrder);
				this._outstandingOrders.put(orderId, outstandingOrder);
			}
		}
	}

	//exclude make Liquidation order and make Limit,MOO,MOC,MKT,OCO for mapOrder.....
	private void setOutstandingOrders(BuySellType buySellType, Boolean isSpot, Boolean isMakeLimitOrder)
	{
		/*if (this._outstandingOrders.size() > 0 || this._account.get_Type() == AccountType.Agent)
		{
			Iterator<RelationOrder> relationOrders = this._outstandingOrders.values().iterator();
			while(relationOrders.hasNext())
			{
				relationOrders.next().set_IsMakeLimitOrder(isMakeLimitOrder);
			}
			return;
		}*/

		this._outstandingOrders.clear();
		HashMap<Guid, Transaction> accountInstrumentTransactions = this.getAccountInstrumentTransactions();
		for (Iterator<Transaction> iterator = accountInstrumentTransactions.values().iterator(); iterator.hasNext(); )
		{
			Transaction transaction = iterator.next();
			if (transaction.get_Phase().equals(Phase.Executed))
			{
				for (Iterator<Order> iterator2 = transaction.get_Orders().values().iterator(); iterator2.hasNext(); )
				{
					Order order = iterator2.next();
					if(order.get_Phase() != Phase.Executed) continue;
					if(order.get_Transaction().get_Account().get_Type() == AccountType.Agent
						|| order.get_Transaction().get_Account().get_Type() == AccountType.Transit) continue;
					BigDecimal availableLotBanlance = order.get_LotBalance();
					if(isSpot != null) availableLotBanlance = order.getAvailableLotBanlance(isSpot, isMakeLimitOrder);
					if (availableLotBanlance.compareTo(BigDecimal.ZERO) > 0)
					{
						Guid orderId = order.get_Id();
						RelationOrder outstandingOrder = new RelationOrder(this._tradingConsole, this._settingsManager, order);
						outstandingOrder.set_IsMakeLimitOrder(isMakeLimitOrder);
						if (buySellType.equals(BuySellType.Both))
						{
							this._outstandingOrders.put(orderId, outstandingOrder);
						}
						else if (order.get_IsBuy() && (buySellType.equals(BuySellType.Buy)))
						{
							this._outstandingOrders.put(orderId, outstandingOrder);
						}
						else if (!order.get_IsBuy() && (buySellType.equals(BuySellType.Sell)))
						{
							this._outstandingOrders.put(orderId, outstandingOrder);
						}
					}
				}
			}
		}
	}

	private HashMap<Guid, Transaction> getAccountInstrumentTransactions()
	{
		HashMap<Guid, Transaction> transactions = null;
		HashMap<Guid, Transaction> accountTransactions = this._account.get_Transactions();
		HashMap<Guid, Transaction> instrumentTransactions = this._instrument.get_Transactions();
		if (accountTransactions == null || instrumentTransactions == null)
		{
			return transactions;
		}
		transactions = new HashMap<Guid, Transaction> ();
		for (Iterator<Transaction> iterator = accountTransactions.values().iterator(); iterator.hasNext(); )
		{
			Transaction accountTransaction = iterator.next();
			Guid accountTransactionId = accountTransaction.get_Id();
			if (instrumentTransactions.containsKey(accountTransactionId))
			{
				transactions.put(accountTransactionId, accountTransaction);
			}
		}
		return transactions;
	}

	private HashMap<Guid, RelationOrder> getEditOutstandingOrders(BuySellType buySellType, Order mapOrder)
	{
		HashMap<Guid, RelationOrder> editOutstandingOrders;
		if (buySellType == BuySellType.Both)
		{
			editOutstandingOrders = this._outstandingOrders;
			for (Iterator<RelationOrder> iterator = this._outstandingOrders.values().iterator(); iterator.hasNext(); )
			{
				RelationOrder relationOrder = iterator.next();
				if (mapOrder != null)
				{
					relationOrder.set_IsSelected(true);
				}
			}
		}
		else
		{
			editOutstandingOrders = new HashMap<Guid, RelationOrder> ();
			for (Iterator<RelationOrder> iterator = this._outstandingOrders.values().iterator(); iterator.hasNext(); )
			{
				RelationOrder relationOrder = iterator.next();
				if (mapOrder != null)
				{
					relationOrder.set_IsSelected(true);
				}
				if (buySellType == BuySellType.Buy && relationOrder.get_IsBuy())
				{
					editOutstandingOrders.put(relationOrder.get_OpenOrderId(), relationOrder);
				}
				else if (buySellType == BuySellType.Sell && !relationOrder.get_IsBuy())
				{
					editOutstandingOrders.put(relationOrder.get_OpenOrderId(), relationOrder);
				}
			}
		}
		return editOutstandingOrders;
	}

	public boolean initializeOutstanding(DataGrid grid, boolean ascend, boolean caseOn, BuySellType buySellType, BuySellType editOutstandingOrderBuySellType, IOpenCloseRelationSite openCloseRelationSite)
	{
		return this.initializeOutstanding2(grid, ascend, caseOn, buySellType, editOutstandingOrderBuySellType, openCloseRelationSite, null);
	}

	public boolean initializeOutstanding(DataGrid grid, boolean ascend, boolean caseOn, BuySellType buySellType,
									  BuySellType editOutstandingOrderBuySellType, IOpenCloseRelationSite openCloseRelationSite, Order mapOrder)
	{
		return this.initializeOutstanding2(grid, ascend, caseOn, buySellType, editOutstandingOrderBuySellType, openCloseRelationSite, mapOrder);
	}

	private boolean initializeOutstanding2(DataGrid grid, boolean ascend, boolean caseOn, BuySellType buySellType,
										BuySellType editOutstandingOrderBuySellType, IOpenCloseRelationSite openCloseRelationSite, Order mapOrder)
	{
		Boolean isSpot = openCloseRelationSite.getOrderType() == null ?  null : openCloseRelationSite.getOrderType().isSpot();
		this.setOutstandingOrders(buySellType, mapOrder, isSpot, openCloseRelationSite.isMakeLimitOrder());
		HashMap<Guid, RelationOrder> editOutstandingOrders = this.getEditOutstandingOrders(editOutstandingOrderBuySellType, mapOrder);
		boolean hasChange = this.hasChange(grid, editOutstandingOrders);

		if(hasChange)
		{
			this.unbindOutstanding();
			this._outstandingKey = Guid.newGuid().toString();
			this._bindingSourceForOutstanding = new tradingConsole.ui.grid.BindingSource();

			boolean isMakeOrder2 = this._settingsManager.get_Customer().get_SingleAccountOrderType() == 2
				|| this._settingsManager.get_Customer().get_MultiAccountsOrderType() == 2;

			RelationOrder.initialize(isMakeOrder2, grid, this._outstandingKey, editOutstandingOrders.values(), this._bindingSourceForOutstanding,
									 openCloseRelationSite, editOutstandingOrderBuySellType);

			int column = this._bindingSourceForOutstanding.getColumnByName(OutstandingOrderColKey.IsBuy);
			if (column != -1)
			{
				grid.sortColumn(column, false, ascend);
				TableColumnChooser.hideColumn(grid, column);
			}
		}
		else
		{
			this._outstandingKey = grid.get_BindingSource().get_DataSourceKey();
			this._outstandingOrders.clear();
			for(int row = 0; row < grid.getRowCount(); row++)
			{
				RelationOrder relationOrder = (RelationOrder)(grid.getObject(row));
				this._outstandingOrders.put(relationOrder.get_OpenOrderId(), relationOrder);
			}
		}

		return hasChange;
	}

	private boolean hasChange(DataGrid grid, HashMap<Guid, RelationOrder> relationOrders)
	{
		if(grid.get_BindingSource() == null || grid.getRowCount() != relationOrders.size())
		{
			return true;
		}
		else
		{
			int openOrderSummaryColumn = -1;
			for(int column = 0; column < grid.getColumnModel().getColumnCount(); column++)
			{
				if(grid.getColumnModel().getColumn(column).getIdentifier().equals(OutstandingOrderColKey.OpenOrderSummary))
				{
					openOrderSummaryColumn = column;
					break;
				}
			}
			for(int row = 0; row < grid.getRowCount(); row++)
			{
				RelationOrder realtionOrder = (RelationOrder)grid.getObject(row);
				Guid openOrderId = realtionOrder.get_OpenOrderId();
				RelationOrder realtionOrder2 = relationOrders.get(openOrderId);
				if(realtionOrder2 == null
				   || realtionOrder2.getAvailableCloseLot().compareTo(realtionOrder.getAvailableCloseLot()) != 0
				   || (openOrderSummaryColumn > -1 && !realtionOrder2.get_OpenOrderSummary().equals(grid.getValueAt(row, openOrderSummaryColumn).toString())))
				{
					return true;
				}
			}
		}
		return false;
	}

	public void finalize() throws Throwable
	{
		this.unbindOutstanding();

		super.finalize();
	}

	public void unbindOutstanding()
	{
		if (!StringHelper.isNullOrEmpty(this._outstandingKey) && this._bindingSourceForOutstanding != null)
		{
			RelationOrder.unbind(this._outstandingKey, this._bindingSourceForOutstanding);
		}
	}

	//for Instrument code with "#"
	//for IsBuy = true:
	//SumBuy = all confirmed order.Buy.LotBalance + all unconfirmed order.Buy.Lot
	//SumSell = all confirmed order.Sell.LotBalance
	//for IsBuy = false:
	//SumSell = all confirmed order.Sell.LotBalance + all unconfirmed order.Sell.Lot
	//SumBuy = all confirmed order.Buy.LotBalance
	public GetSumLotBSForOpenOrderWithFlagResult getSumLotBSForOpenOrderWithFlag(boolean isBuy)
	{
		BigDecimal[] sumLot = new BigDecimal[]{BigDecimal.ZERO,BigDecimal.ZERO};
		boolean isExistsUnconfirmedOrder = false;
		HashMap<Guid, Transaction> accountInstrumentTransactions = this.getAccountInstrumentTransactions();
		for (Iterator<Transaction> iterator = accountInstrumentTransactions.values().iterator(); iterator.hasNext(); )
		{
			Transaction transaction = iterator.next();
			if (transaction.get_Phase() == Phase.Executed
				|| transaction.get_Phase() == Phase.Placing
				|| transaction.get_Phase() == Phase.Placed)
			{
				for (Iterator<Order> iterator2 = transaction.get_Orders().values().iterator(); iterator2.hasNext(); )
				{
					Order order = iterator2.next();
					if (order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0)
					{
						if (isBuy)
						{
							if (order.get_IsBuy())
							{
								if (transaction.get_Phase() == Phase.Executed)
								{
									sumLot[0] = sumLot[0].add(order.get_LotBalance());
								}
								else
								{
									sumLot[0] = sumLot[0].add(order.get_Lot());
									isExistsUnconfirmedOrder = true;
								}
							}
							else
							{
								if (transaction.get_Phase() == Phase.Executed)
								{
									sumLot[1] = sumLot[1].add(order.get_LotBalance());
								}
							}
						}
						else
						{
							if (order.get_IsBuy())
							{
								if (transaction.get_Phase() == Phase.Executed)
								{
									sumLot[0] = sumLot[0].add(order.get_LotBalance());
								}
							}
							else
							{
								if (transaction.get_Phase() == Phase.Executed)
								{
									sumLot[1] = sumLot[1].add(order.get_LotBalance());
								}
								else
								{
									sumLot[1] = sumLot[1].add(order.get_Lot());
									isExistsUnconfirmedOrder = true;
								}
							}
						}
					}
				}
			}
		}

		return new GetSumLotBSForOpenOrderWithFlagResult(sumLot[0],sumLot[1],isExistsUnconfirmedOrder);
	}

	public BigDecimal[] getSumLotBSForOpenOrder()
	{
		BigDecimal[] sumLot = new BigDecimal[]{BigDecimal.ZERO,BigDecimal.ZERO};
		HashMap<Guid, Transaction> accountInstrumentTransactions = this.getAccountInstrumentTransactions();
		for (Iterator<Transaction> iterator = accountInstrumentTransactions.values().iterator(); iterator.hasNext(); )
		{
			Transaction transaction = iterator.next();
			if (transaction.get_Phase() == Phase.Executed)
			{
				for (Iterator<Order> iterator2 = transaction.get_Orders().values().iterator(); iterator2.hasNext(); )
				{
					Order order = iterator2.next();
					if (order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0)
					{
						if (order.get_IsBuy())
						{
							sumLot[0] = sumLot[0].add(order.get_LotBalance());
						}
						else
						{
							sumLot[1] = sumLot[1].add(order.get_LotBalance());
						}
					}
				}
			}
		}
		return sumLot;
	}

	/*
	 Remarks
	 sum (SellLotBalance) = all confirmed order.Sell.LotBalance
	   + unconfirmed order (stop).Sell.Lot
	   + iif("MOC/MOO allow New" = False,
	  unconfirmed order (MOO/MOC).Sell.Lot,
	  0)

	 sum (BuyLotBalance) = all confirmed order.Buy.LotBalance
	   + unconfirmed order (stop).Buy.Lot
	   + iif("MOC/MOO allow New" = False,
	  unconfirmed order (MOO/MOC).Buy.Lot,
	  0)
	 */
	public BigDecimal[] getSumLotBSForMakeStopOrder()
	{
		BigDecimal[] sumLot = new BigDecimal[]{BigDecimal.ZERO,BigDecimal.ZERO};
		TradePolicyDetail tradePolicyDetail = this.getTradePolicyDetail();
		HashMap<Guid, Transaction> accountInstrumentTransactions = this.getAccountInstrumentTransactions();
		for (Iterator<Transaction> iterator = accountInstrumentTransactions.values().iterator(); iterator.hasNext(); )
		{
			Transaction transaction = iterator.next();
			if (transaction.get_Phase() == Phase.Executed)
			{
				for (Iterator<Order> iterator2 = transaction.get_Orders().values().iterator(); iterator2.hasNext(); )
				{
					Order order = iterator2.next();
					if (order.get_LotBalance().compareTo(BigDecimal.ZERO) > 0)
					{
						if (order.get_IsBuy())
						{
							sumLot[0] = sumLot[0].add(order.get_LotBalance());
						}
						else
						{
							sumLot[1] = sumLot[1].add(order.get_LotBalance());
						}
					}
				}
			}
			else if (transaction.get_Phase() == Phase.Placing || transaction.get_Phase() == Phase.Placed)
			{
				for (Iterator<Order> iterator2 = transaction.get_Orders().values().iterator(); iterator2.hasNext(); )
				{
					Order order = iterator2.next();
					if (order.get_TradeOption() == TradeOption.Stop
						|| (!tradePolicyDetail.get_IsAcceptNewMOOMOC()
							&& (transaction.get_OrderType() == OrderType.MarketOnOpen
								|| transaction.get_OrderType() == OrderType.MarketOnClose)))
					{
						if (order.get_IsBuy())
						{
							sumLot[0] = sumLot[0].add(order.get_Lot());
						}
						else
						{
							sumLot[1] = sumLot[1].add(order.get_Lot());
						}
					}
				}
			}

		}
		return sumLot;
	}

	public BigDecimal getQuoteLotForDirectLiq(boolean isSpotTrade, Boolean isMakeLimitOrder)
	{
		BigDecimal quoteLot = BigDecimal.ZERO;
		for (Iterator<RelationOrder> iterator = this._outstandingOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			quoteLot = quoteLot.add(relationOrder.get_OpenOrder().getAvailableLotBanlance(isSpotTrade, isMakeLimitOrder));
		}
		return quoteLot;
	}

	public BigDecimal getSumLiqLots()
	{
		BigDecimal sumLiqLots = BigDecimal.ZERO;
		for (Iterator<RelationOrder> iterator = this._outstandingOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			if (relationOrder.get_IsSelected() && relationOrder.get_LiqLot() != null)
			{
				sumLiqLots = sumLiqLots.add(relationOrder.get_LiqLot());
			}
		}
		return sumLiqLots;
	}

	public BigDecimal getSumLiqLots(boolean isBuy)
	{
		BigDecimal sumLiqLots = BigDecimal.ZERO;
		for (Iterator<RelationOrder> iterator = this._outstandingOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			if (relationOrder.get_IsBuy() == !isBuy && relationOrder.get_IsSelected())
			{
				if (relationOrder.get_LiqLot() != null)
				{
					sumLiqLots = sumLiqLots.add(relationOrder.get_LiqLot());
				}
			}
		}
		return sumLiqLots;
	}

	public boolean isAcceptLot(boolean isBuy, BigDecimal lot,boolean isHasMakeNewOrder)
	{
		//if (!Parameter.isNewOrderAcceptedHedging)
		//{
		//	return true;
		//}

		if (this._instrument.get_Code().substring(0, 1).equals("#"))
		{
			GetSumLotBSForOpenOrderWithFlagResult getSumLotBSForOpenOrderWithFlagResult = this.getSumLotBSForOpenOrderWithFlag(isBuy);
			BigDecimal sumBuyLots = getSumLotBSForOpenOrderWithFlagResult.sumBuyLots;
			BigDecimal sumSellLots = getSumLotBSForOpenOrderWithFlagResult.sumSellLots;
			if (isHasMakeNewOrder || getSumLotBSForOpenOrderWithFlagResult.isExistsUnconfirmedOrder)
			{
				return (isBuy && (sumSellLots.subtract(sumBuyLots).compareTo(lot) >= 0)
						|| (!isBuy && (sumBuyLots.subtract(sumSellLots).compareTo(lot) >= 0)));
			}
		}
		return true;
	}

	public BuySellType GetBuySellForCloseAll()
	{
		boolean hasIsBuy = false;
		for (Iterator<RelationOrder> iterator = this._outstandingOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			if (relationOrder.get_IsBuy())
			{
				hasIsBuy = true;
			}
			else if (hasIsBuy)
			{
				return BuySellType.Both;
			}
		}
		return hasIsBuy ? BuySellType.Sell : BuySellType.Buy;
	}

	public BigDecimal closeAll()
	{
		BigDecimal totalLiqLot = BigDecimal.ZERO;
		for (Iterator<RelationOrder> iterator = this._outstandingOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			if(this._isBuyForCurrent != relationOrder.get_IsBuy())
			{
				totalLiqLot = totalLiqLot.add(relationOrder.get_LiqLot());
			}
		}
		TradePolicyDetail tradePolicyDetail
					= this._settingsManager.getTradePolicyDetail(this._account.get_TradePolicyId(), this._instrument.get_Id());
		totalLiqLot = AppToolkit.fixLot(totalLiqLot, false, tradePolicyDetail, this._account);
		BigDecimal totalLiqLot2 = totalLiqLot;

		for (Iterator<RelationOrder> iterator = this._outstandingOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			if(this._isBuyForCurrent != relationOrder.get_IsBuy())
			{
				BigDecimal liqLot = relationOrder.get_LiqLot();
				liqLot = totalLiqLot.compareTo(liqLot) > 0 ? liqLot : totalLiqLot;
				relationOrder.set_LiqLot(liqLot);
				totalLiqLot = totalLiqLot.subtract(liqLot);
				relationOrder.set_IsSelected(true);
				relationOrder.update(this._outstandingKey);

				if (totalLiqLot.compareTo(BigDecimal.ZERO) <= 0)
					break;
			}
		}

		if (this._isBuyForCurrent)
		{
			this._buyLot = totalLiqLot2;
		}
		else
		{
			this._sellLot = totalLiqLot2;
		}
		return totalLiqLot2;
	}

	public void cleanOutStandingTable(boolean closeAllSell)
	{
		ArrayList<Guid> toBeRemoved = new ArrayList<Guid>();
		for (Iterator<RelationOrder> iterator = this._outstandingOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			if(relationOrder.get_OpenOrder().get_IsBuy() == closeAllSell)
			{
				toBeRemoved.add(relationOrder.get_OpenOrder().get_Id());
				this._bindingSourceForOutstanding.remove(relationOrder);
			}
		}

		for (Guid id : toBeRemoved)
		{
			this._outstandingOrders.remove(id);
		}
	}

	public void setDefaultLiqLotForOutStanding(BuySellType buySellType, boolean isSelectedRelationOrder, boolean isSpotTrade, Boolean isMakeLimitOrder)
	{
		for (Iterator<RelationOrder> iterator = this._outstandingOrders.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();

			relationOrder.set_IsSelected(isSelectedRelationOrder);
			BigDecimal liqLot = relationOrder.get_OpenOrder().getAvailableLotBanlance(isSpotTrade, isMakeLimitOrder);
			if(this._outstandingOrders.values().size() == 1)
			{
				TradePolicyDetail tradePolicyDetail = this.getTradePolicyDetail();
				liqLot = AppToolkit.fixLot(liqLot, false, tradePolicyDetail, this._account);
			}

			boolean isBuy = (buySellType == BuySellType.Buy);
			if (buySellType == BuySellType.Both)
			{
				relationOrder.set_LiqLot(liqLot);
				//Update Outstanding Order UI
				relationOrder.update(this._outstandingKey);
			}
			else if (relationOrder.get_IsBuy() != isBuy)
			{
				relationOrder.set_LiqLot(liqLot);
				//Update Outstanding Order UI
				//maybe not binding
				//if (this._isBuyForCurrent != isBuy)
				//{
				relationOrder.update(this._outstandingKey);
				//}
			}
		}
	}

	public void reset(boolean isSpotTrade, Boolean isMakeLimitOrder)
	{
		this.reset(false, isSpotTrade, isMakeLimitOrder);
	}

	public void reset(boolean isSelectedRelationOrder, boolean isSpotTrade, Boolean isMakeLimitOrder)
	{
		if (this._account.get_Type() == AccountType.Agent)
		{
			this.setDefaultLiqLotForOutStanding(this._buySellType, isSelectedRelationOrder, isSpotTrade, isMakeLimitOrder);
		}
		else
		{
			this.setDefaultLiqLotForOutStanding(BuySellType.Buy, isSelectedRelationOrder, isSpotTrade, isMakeLimitOrder);
			this.setDefaultLiqLotForOutStanding(BuySellType.Sell, isSelectedRelationOrder, isSpotTrade, isMakeLimitOrder);
		}
	}

	public void clearOutStandingTable(BuySellType buySellType, boolean isSpotTrade, Boolean isMakeLimitOrder)
	{
		this.setDefaultLiqLotForOutStanding(buySellType, false, isSpotTrade, isMakeLimitOrder);
	}

	public String get_Code()
	{
		return this._account.get_Code();
	}

	public String getSetPriceString(boolean isBuy)
	{
		return Price.toString( (isBuy) ? this._buySetPrice : this._sellSetPrice);
	}

	public String get_SetPriceString()
	{
		return this.getSetPriceString(this._isBuyForCurrent);
	}

	public Price get_BuySetPrice()
	{
		return this._buySetPrice;
	}

	public void set_BuySetPrice(Price value)
	{
		this._buySetPrice = value;
	}

	public Price get_SellSetPrice()
	{
		return this._sellSetPrice;
	}

	public void set_SellSetPrice(Price value)
	{
		this._sellSetPrice = value;
	}

	public String get_LotString()
	{
		return AppToolkit.getFormatLot( ( (this._isBuyForCurrent) ? this._buyLot : this._sellLot), this._account, this.get_Instrument());
	}

	public void set_LotString(String value)
	{
		if (StringHelper.isNullOrEmpty(value))
		{
			if (this._isBuyForCurrent)
			{
				this._buyLot = BigDecimal.ZERO;
			}
			else
			{
				this._sellLot = BigDecimal.ZERO;
			}
		}
		else
		{
			if (this._isBuyForCurrent)
			{
				this._buyLot = AppToolkit.convertStringToBigDecimal(value);
			}
			else
			{
				this._sellLot = AppToolkit.convertStringToBigDecimal(value);
			}
		}
	}

	//For Make Multi_SpotTrade Order/Assign Order
	public static PropertyDescriptor[] getPropertyDescriptors(boolean isAllowEditIsBuy, int dQMaxMove, int stepSize)
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[dQMaxMove > 0 && dQMaxMove > stepSize ? 4 : 3];
		int i = -1;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(MakeOrderAccount.class, MakeOrderAccountGridColKey.Code, true, null,
			MakeOrderAccountGridLanguage.Code, 40, SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(MakeOrderAccount.class, MakeOrderAccountGridColKey.IsBuyForCombo, !isAllowEditIsBuy, null,
			MakeOrderAccountGridLanguage.IsBuyForCombo,	15,	SwingConstants.CENTER, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		/*propertyDescriptor = PropertyDescriptor.create(MakeOrderAccount.class, MakeOrderAccountGridColKey.SetPriceString, true, null,
			MakeOrderAccountGridLanguage.SetPriceString, 40, SwingConstants.RIGHT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;*/

		propertyDescriptor = PropertyDescriptor.create(MakeOrderAccount.class, MakeOrderAccountGridColKey.LotString, false, null,
			MakeOrderAccountGridLanguage.LotString, 25,	SwingConstants.CENTER, null, null, new StringCellEditor(), null);
		propertyDescriptors[++i] = propertyDescriptor;

		if(dQMaxMove > 0 && dQMaxMove > stepSize)
		{
			SpinnerCellEditor spinnerCellEditor = new SpinnerCellEditor();
			JDQMoveSpinnerHelper.applyMaxDQMove(spinnerCellEditor.getSpinner(), dQMaxMove, stepSize);

			propertyDescriptor = PropertyDescriptor.create(MakeOrderAccount.class, MakeOrderAccountGridColKey.DQMaxMove, false, null,
				MakeOrderAccountGridLanguage.DQMaxMove, (dQMaxMove > 0) ? 50 : 0, SwingConstants.CENTER, null, null, spinnerCellEditor, null);
			propertyDescriptors[++i] = propertyDescriptor;
		}
		return propertyDescriptors;
	}

	public static void initialize(DataGrid grid)
	{
		//grid.setRedraw(false);
		//grid.reset();
		grid.setShowVerticalLines(false);
		grid.setBackground(GridFixedBackColor.account);
		grid.setForeground(GridBackColor.account);
		//grid.setBorderStyle(BorderStyle.account);
		//grid.setRowLabelWidth(RowLabelWidth.account);
		//grid.setSelectionBackground(SelectionBackground.account);
		//grid.setCurrentCellColor(CurrentCellColor.account);
		//grid.setCurrentCellBorder(CurrentCellBorder.account);

		//grid.setRedraw(true);
		//grid.doLayout();
	}

	public static void initialize(DataGrid grid, String dataSourceKey, Collection dataSource, tradingConsole.ui.grid.BindingSource bindingSource, boolean isAllowEditIsBuy, int dQMaxMove, int stepSize)
	{
		grid.setShowVerticalLines(false);
		grid.setBackground(GridFixedBackColor.account);
		grid.setForeground(GridBackColor.account);
		//grid.setSelectionBackground(SelectionBackground.account);

		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, MakeOrderAccount.getPropertyDescriptors(isAllowEditIsBuy, dQMaxMove, stepSize));
		grid.setModel(bindingSource);
		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.account, Color.white, HeaderFont.account);
		TradingConsole.bindingManager.setGrid(dataSourceKey, 18, Color.black, Color.lightGray, Color.blue, true, true, GridFont.account, false, true, true);

		if(isAllowEditIsBuy)
		{
			String[] listStrings = new String[]{Language.Buy, Language.Sell};
			ListComboBoxCellEditor listComboBoxCellEditor = new ListComboBoxCellEditor(listStrings);
			int column = bindingSource.getColumnByName(MakeOrderAccountGridColKey.IsBuyForCombo);
			grid.getColumnModel().getColumn(column).setCellEditor(listComboBoxCellEditor);
		}
	}

	public static void unbind(String dataSourceKey, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		TradingConsole.bindingManager.unbind(dataSourceKey, bindingSource);
	}

	public void add(String dataSourceKey)
	{
		if (this._buySellType == BuySellType.Both)
		{
			this._isBuyForCurrent = true;
			this._isBuyForCombo = Language.Buy;
			this.add2(dataSourceKey);

			this._isBuyForCurrent = false;
			this._isBuyForCombo = Language.Sell;
			this.add2(dataSourceKey);
		}
		else
		{
			this.add2(dataSourceKey);
		}
	}

	public void add2(String dataSourceKey)
	{
		TradingConsole.bindingManager.add(dataSourceKey, this);
		this.setStyle(dataSourceKey);
	}

	private void setBackground(String dataSourceKey, Color background)
	{
		TradingConsole.bindingManager.setBackground(dataSourceKey, this, background);
	}

	public void update(String dataSourceKey)
	{
		if (this._buySellType == BuySellType.Both)
		{
			/*
				this._isBuyForCurrent = true;
				TradingConsole.bindingManager.update(dataSourceKey, this);
				this.setStyle(dataSourceKey);

				this._isBuyForCurrent = false;
				TradingConsole.bindingManager.update(dataSourceKey, this);
				this.setStyle(dataSourceKey);
			 */
		}
		else
		{
			TradingConsole.bindingManager.update(dataSourceKey, this);
			this.setStyle(dataSourceKey);

		}
	}

	public void update(String dataSourceKey, boolean isDblClickAsk)
	{
		if (this._buySellType == BuySellType.Both)
		{
			boolean isBuy = Instrument.getSelectIsBuy(this._instrument, isDblClickAsk);

			this._isBuyForCurrent = isBuy;
			this._isBuyForCombo = isBuy ? Language.Buy : Language.Sell;
			TradingConsole.bindingManager.update(dataSourceKey, this);
			this.setStyle(dataSourceKey);

			this._isBuyForCurrent = !isBuy;
			this._isBuyForCombo = (!isBuy) ? Language.Buy : Language.Sell;
			TradingConsole.bindingManager.update(dataSourceKey, this);
			this.setStyle(dataSourceKey);
		}
		else
		{
			TradingConsole.bindingManager.update(dataSourceKey, this);
			this.setStyle(dataSourceKey);
		}
	}

	private void setStyle(String dataSourceKey)
	{
		Color color = BuySellColor.getColor(this._isBuyForCurrent, false);
		this.setForeground(dataSourceKey, MakeOrderAccountGridColKey.Code, color);
		this.setForeground(dataSourceKey, MakeOrderAccountGridColKey.IsBuyForCurrent, color);
		this.setForeground(dataSourceKey, MakeOrderAccountGridColKey.IsBuyForCombo, color);
		this.setForeground(dataSourceKey, MakeOrderAccountGridColKey.SetPriceString, color);
		this.setForeground(dataSourceKey, MakeOrderAccountGridColKey.LotString, color);

		this.setBackground(dataSourceKey, GridBackgroundColor.account);
	}

	public void setForeground(String dataSourceKey, String propertyName, Color foreground)
	{
		//should change to array......
		TradingConsole.bindingManager.setForeground(dataSourceKey, this, propertyName, foreground);
	}

	public TradePolicyDetail getTradePolicyDetail()
	{
		return this._settingsManager.getTradePolicyDetail(this._account.get_TradePolicyId(), this._instrument.get_Id());
	}

	private class GetSumLotBSForOpenOrderWithFlagResult
	{
		private BigDecimal sumBuyLots = BigDecimal.ZERO;
		private BigDecimal sumSellLots = BigDecimal.ZERO;
		private boolean isExistsUnconfirmedOrder = false;

		public GetSumLotBSForOpenOrderWithFlagResult(BigDecimal sumBuyLots,BigDecimal sumSellLos,boolean isExistsUnconfirmedOrder)
		{
			this.sumBuyLots = sumBuyLots;
			this.sumSellLots = sumSellLots;
			this.isExistsUnconfirmedOrder = isExistsUnconfirmedOrder;
		}
	}

	private boolean isOpen()
	{
		for(RelationOrder relation : this._outstandingOrders.values())
		{
			if(relation.get_IsSelected()) return false;
		}
		return true;
	}

	public BigDecimal adjustLot(BigDecimal lot)
	{
		if(this._isBuyForCurrent)
		{
			if (this._buyLot.compareTo(BigDecimal.ZERO) > 0)
			{
				if (this._buyLot.compareTo(lot) > 0)
				{
					this._buyLot = lot;
				}
				return this._buyLot;
			}
		}
		else if(this._sellLot.compareTo(BigDecimal.ZERO) > 0)
		{
			if(this._sellLot.compareTo(lot) > 0)
			{
				this._sellLot = lot;
			}
			return this._sellLot;
		}

		return BigDecimal.ZERO;
	}

	public void adjustLot(BigDecimal adjustLot, boolean isBuy)
	{
		if(isBuy)
		{
			this._buyLot = this._buyLot.add(adjustLot);
		}
		else
		{
			this._sellLot = this._sellLot.add(adjustLot);
		}
	}

	SpotOrderPriceContainer _spotOrderPriceContainer = null;
	public void set_SpotOrderPriceContainer(SpotOrderPriceContainer spotOrderPriceContainer)
	{
		this._spotOrderPriceContainer = spotOrderPriceContainer;
	}

	public SpotOrderPriceContainer get_SpotOrderPriceContainer()
	{
		return this._spotOrderPriceContainer;
	}

	private PlaceOrderInfo _placeOrderInfo;
	public void set_PlaceOrderInfo(PlaceOrderInfo placeOrderInfo)
	{
		this._placeOrderInfo = placeOrderInfo;
	}

	public PlaceOrderInfo get_PlaceOrderInfo()
	{
		return this._placeOrderInfo;
	}

	private IfDoneInfo _ifDoneInfo;
	public void set_IfDoneInfo(IfDoneInfo _ifDoneInfo)
	{
		this._ifDoneInfo = _ifDoneInfo;
	}

	public IfDoneInfo get_IfDoneInfo()
	{
		return this._ifDoneInfo;
	}

	boolean _isMatchOrder = false;
	public boolean get_IsPlacMatchOrder()
	{
		return this._isMatchOrder;
	}

	public void set_IsPlacMatchOrder(boolean isMatchOrder)
	{
		this._isMatchOrder = isMatchOrder;
	}

	private static class ComparatorForAdjustingLot implements Comparator<MakeOrderAccount>
	{
		public int compare(MakeOrderAccount left, MakeOrderAccount right)
		{
			int leftHasClose = left.isOpen() ? 1 : 0;
			int rightHasClose = right.isOpen() ? 1 : 0;
			int result = leftHasClose - rightHasClose;
			if(result != 0) return result;

			BigDecimal leftLot = left._isBuyForCurrent ? left._buyLot : left._sellLot;
			BigDecimal rightLot = right._isBuyForCurrent ? right._buyLot : right._sellLot;

			result = leftLot.compareTo(rightLot);
			if(result != 0) return -result;

			return left._account.get_Code().compareTo(right._account.get_Code());
		}

		public boolean equals(Object obj)
		{
			return false;
		}
	}
}
