package tradingConsole.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.math.BigDecimal;

import framework.Guid;

import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.settings.MakeOrderAccount;
import tradingConsole.ui.language.Language;
import tradingConsole.Account;
import tradingConsole.Instrument;
import tradingConsole.Order;
import tradingConsole.enumDefine.AccountType;
import tradingConsole.settings.RelationOrder;
import tradingConsole.settings.Parameter;
import tradingConsole.ui.grid.DataGrid;
import tradingConsole.ui.grid.IPropertyChangingListener;
import tradingConsole.enumDefine.OrderType;
import javax.swing.JTextField;
import tradingConsole.ui.grid.IPropertyChangedListener;
import tradingConsole.AppToolkit;
import tradingConsole.enumDefine.OperateType;
import framework.DateTime;
import tradingConsole.Quotation;
import tradingConsole.TradePolicyDetail;
import tradingConsole.ui.columnKey.MakeOrderLiquidationGridColKey;

public class MakeLiquidationOrder extends MakeSpotTradeOrder
{
	private String _liquidationKey;
	private tradingConsole.ui.grid.BindingSource _bindingSourceForLiquidation;
	private HashMap<Guid, RelationOrder> _dataSourceForLiquidations;

	public tradingConsole.ui.grid.BindingSource get_BindingSourceForLiquidation()
	{
		return this._bindingSourceForLiquidation;
	}

	public MakeLiquidationOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		super(tradingConsole, settingsManager, instrument);
		this._liquidationKey = Guid.newGuid().toString();
		this._bindingSourceForLiquidation = new tradingConsole.ui.grid.BindingSource();
	}

	public Object[] isAccept(Order order)
	{
		return MakeLiquidationOrder.isAccept(this._settingsManager, order);
	}

	public static Object[] isAccept(SettingsManager settingsManager, Order order)
	{
		Object[] result = new Object[]{false, ""}; //isAccept,message

		if (MakeOrder.isDisallowTradeSetting(settingsManager))
		{
			result[1] = Language.DisallowTradePrompt;
			return result;
		}

		Instrument instrument = order.get_Transaction().get_Instrument();
		if (/*MakeOrder.isOperateSameInstrument(settingsManager, instrument)
			||*/ MakeOrder.isOverRangeOperateOrderUI(settingsManager))
		{
			result[1] = Language.OperateDQOrderPrompt + "/" + Language.InstrumentSelectOverRange + Parameter.operateOrderCount;
			return result;
		}
		Account account = order.get_Transaction().get_Account();
		if (!account.getIsAllowTrade())
		{
			return result;
		}

		if (account.get_Type() == AccountType.Agent
			|| (account.get_Type() != AccountType.Agent && account.get_IsLocked())
			|| !account.isValidAccountForTradePolicy(instrument, true)
			|| account.get_Type() == AccountType.Transit)
		{
			return result;
		}
		if (instrument.get_LastQuotation().get_Ask() == null
			|| instrument.get_LastQuotation().get_Bid() == null)
		{
			result[1] = Language.OrderSingleDQPageorderValidAlert0;
			return result;
		}

		Object[] result2 = MakeSpotTradeOrder.isAllowTime(instrument);
		result[0] = (Boolean) result2[0];
		result[1] = result2[1].toString();

		return result;
	}

	private void setMakeOrderAccounts()
	{
		if (this._makeOrderAccounts.size() > 0)
		{
			return;
		}
		for (Iterator<Order> iterator = this._tradingConsole.get_OpenOrders().values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();

			if (order.get_Close())
			{
				Account account = order.get_Transaction().get_Account();
				if (!this._makeOrderAccounts.containsKey(account.get_Id()))
				{
					if (!account.getIsAllowTrade())
					{
						continue;
					}
					MakeOrderAccount makeOrderAccount = MakeOrderAccount.create(this._tradingConsole, this._settingsManager, account, this._instrument);
					makeOrderAccount.set_BuySetPrice(this._instrument.get_LastQuotation().getBuy());
					makeOrderAccount.set_SellSetPrice(this._instrument.get_LastQuotation().getSell());
					makeOrderAccount.set_PriceInfo(this._instrument.get_LastQuotation().get_Timestamp(), this._instrument.get_LastQuotation().get_IsQuote());
					this._makeOrderAccounts.put(account.get_Id(), makeOrderAccount);
				}
			}
		}
	}

	public BigDecimal getSumLiqLots()
	{
		BigDecimal sumLiqLots = BigDecimal.ZERO;
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			sumLiqLots = sumLiqLots.add(makeOrderAccount.getSumLiqLots());
		}
		return sumLiqLots;
	}

	private BigDecimal getMaxLotGroupByAccount()
	{
		BigDecimal maxLot = BigDecimal.ZERO;
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			BigDecimal lot = makeOrderAccount.getSumLiqLots();
			maxLot = maxLot.compareTo(lot) > 0 ? maxLot : lot;
		}
		return maxLot;
	}

	public BigDecimal getSumLiqLots(boolean isBuy)
	{
		BigDecimal sumLiqLots = BigDecimal.ZERO;
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			sumLiqLots = sumLiqLots.add(makeOrderAccount.getSumLiqLots(isBuy));
		}
		return sumLiqLots;
	}

	public BigDecimal getQuoteLot(boolean isSpotTrade)
	{
		BigDecimal quoteLot = BigDecimal.ZERO;
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			quoteLot = quoteLot.add(makeOrderAccount.getQuoteLotForDirectLiq(isSpotTrade, null));
		}
		return quoteLot;
	}

	public static Order getFirstOpenOrderHasClosed(TradingConsole tradingConsole, Order currentOrder)
	{
		Order order = null;
		for (Iterator<Order> iterator = tradingConsole.get_OpenOrders().values().iterator(); iterator.hasNext(); )
		{
			order = iterator.next();

			if (order.get_Close())
			{
				if (currentOrder == null)
				{
					return order;
				}
				else if (!order.equals(currentOrder))
				{
					return order;
				}
			}
		}
		return null;
	}

	private void clearOpenOrderCloseFlag()
	{
		for (Iterator<Order> iterator = this._tradingConsole.get_OpenOrders().values().iterator(); iterator.hasNext(); )
		{
			Order order = iterator.next();
			if (order.get_Close())
			{
				order.set_Close(false);
				order.update();
			}
		}
	}

	private void setOutstandingOrder(boolean forMakeOrderForm)
	{
		this._dataSourceForLiquidations = new HashMap<Guid, RelationOrder> ();
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			makeOrderAccount.setOutstandingOrdersForMakeLiquidationOrder(this._instrument, forMakeOrderForm);

			//Create a dataSource
			for (Iterator<RelationOrder> iterator2 = makeOrderAccount.getOutstandingOrders().values().iterator(); iterator2.hasNext(); )
			{
				RelationOrder relationOrder = iterator2.next();
				this._dataSourceForLiquidations.put(relationOrder.get_OpenOrderId(), relationOrder);
			}
		}
	}

	public void showUi(OpenContractForm openContractForm)
	{
		LiquidationOrderForm liquidationOrderUi = new LiquidationOrderForm(this._tradingConsole, this._settingsManager, this, openContractForm);
		liquidationOrderUi.show();
	}

	public void initialize(DataGrid grid, boolean ascend, boolean caseOn, IPropertyChangingListener propertyChangingListener, IOpenCloseRelationBaseSite site, boolean enableEditLot)
	{
		this.initialize(grid, ascend, caseOn, propertyChangingListener, site, null, enableEditLot);
	}

	public void initialize(DataGrid grid, boolean ascend, boolean caseOn, IPropertyChangingListener propertyChangingListener, IOpenCloseRelationBaseSite site, IPropertyChangedListener propertyChangedListener, boolean enableEditLot)
	{
		this.setMakeOrderAccounts();
		this.setOutstandingOrder(site.getFrame() instanceof MakeOrderForm);
		this.clearOpenOrderCloseFlag();

		this._bindingSourceForLiquidation.clearPropertyChangingListener();
		this._bindingSourceForLiquidation.addPropertyChangingListener(propertyChangingListener);
		if(propertyChangedListener != null)
		{
			this._bindingSourceForLiquidation.clearPropertyChangedListener();
			this._bindingSourceForLiquidation.addPropertyChangedListener(propertyChangedListener);
		}
		RelationOrder.initializeLiquidation(grid, this._liquidationKey, this._dataSourceForLiquidations.values(), this._bindingSourceForLiquidation, site, enableEditLot);
		for (Iterator<RelationOrder> iterator = this._dataSourceForLiquidations.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			if(this._dataSourceForLiquidations.values().size() == 1)
			{
				Account account = relationOrder.get_OpenOrder().get_Account();
				TradePolicyDetail tradePolicyDetail
					= this._settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), relationOrder.get_OpenOrder().get_Transaction().get_Instrument().get_Id());
				BigDecimal lot = relationOrder.get_LiqLot();
				lot = AppToolkit.fixLot(lot, false, tradePolicyDetail, account);
				relationOrder.set_LiqLot(lot);
			}
			relationOrder.updateLiquidation(this._liquidationKey);
		}
		//According to the return of method RelationOrder.getPropertyDescriptorsForLiquidation, no column OutstandingOrderColKey.IsBuy
		/*int column = this._bindingSourceForLiquidation.getColumnByName(OutstandingOrderColKey.IsBuy);
		grid.sortColumn(column, ascend);
		TableColumnChooser.hideColumn(grid, column);*/
	}

	public void closeAll()
	{
		Account account = null;
		Instrument instrument = null;

		BigDecimal totalLiqLotOfBuyOrder = BigDecimal.ZERO;
		BigDecimal totalLiqLotOfSellOrder = BigDecimal.ZERO;
		for (Iterator<RelationOrder> iterator = this._dataSourceForLiquidations.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			if(relationOrder.get_OpenOrder().get_IsBuy())
			{
				totalLiqLotOfBuyOrder = totalLiqLotOfBuyOrder.add(relationOrder.get_LiqLot());
			}
			else
			{
				totalLiqLotOfSellOrder = totalLiqLotOfSellOrder.add(relationOrder.get_LiqLot());
			}
			account = relationOrder.get_OpenOrder().get_Account();
			instrument = relationOrder.get_OpenOrder().get_Transaction().get_Instrument();
		}
		TradePolicyDetail tradePolicyDetail
					= this._settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), instrument.get_Id());
		totalLiqLotOfBuyOrder = AppToolkit.fixLot(totalLiqLotOfBuyOrder, false, tradePolicyDetail, account);
		totalLiqLotOfSellOrder = AppToolkit.fixLot(totalLiqLotOfSellOrder, false, tradePolicyDetail, account);
		for (Iterator<RelationOrder> iterator = this._dataSourceForLiquidations.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			BigDecimal lot = relationOrder.get_LiqLot();
			if(relationOrder.get_OpenOrder().get_IsBuy())
			{
				if (lot.compareTo(totalLiqLotOfBuyOrder) > 0) lot = totalLiqLotOfBuyOrder;
				if (lot.compareTo(BigDecimal.ZERO) > 0)
				{
					totalLiqLotOfBuyOrder = totalLiqLotOfBuyOrder.subtract(lot);
					relationOrder.set_LiqLot(lot);
					relationOrder.set_IsSelected(true);
					relationOrder.updateLiquidation(this._liquidationKey);
				}
			}
			else
			{
				if (lot.compareTo(totalLiqLotOfSellOrder) > 0) lot = totalLiqLotOfSellOrder;
				if (lot.compareTo(BigDecimal.ZERO) > 0)
				{
					totalLiqLotOfSellOrder = totalLiqLotOfSellOrder.subtract(lot);
					relationOrder.set_LiqLot(lot);
					relationOrder.set_IsSelected(true);
					relationOrder.updateLiquidation(this._liquidationKey);
				}
			}
		}
	}

	private DateTime _lastQuotationTimeStamp = null;

	public DateTime get_LastQuotationTimeStamp()
	{
		return this._lastQuotationTimeStamp;
	}

	public void update()
	{
		Quotation lastQuotation = this._instrument.get_LastQuotation();
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			makeOrderAccount.set_BuySetPrice(lastQuotation.getBuy());
			makeOrderAccount.set_SellSetPrice(lastQuotation.getSell());
			makeOrderAccount.set_PriceInfo(this._instrument.get_LastQuotation().get_Timestamp(), this._instrument.get_LastQuotation().get_IsQuote());
		}
		this._lastQuotationTimeStamp = lastQuotation.get_Timestamp();

		for (Iterator<RelationOrder> iterator = this._dataSourceForLiquidations.values().iterator(); iterator.hasNext(); )
		{
			RelationOrder relationOrder = iterator.next();
			relationOrder.updateLiquidation(this._liquidationKey);
		}
	}

	public void unbindLiquidation()
	{
		RelationOrder.unbind(this._liquidationKey, this._bindingSourceForLiquidation);
	}

	private boolean isHasLiq()
	{
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			if (makeOrderAccount.getSumLiqLots(true).compareTo(BigDecimal.ZERO) > 0)
			{
				return true;
			}
			if (makeOrderAccount.getSumLiqLots(false).compareTo(BigDecimal.ZERO) > 0)
			{
				return true;
			}
		}
		return false;
	}

	public Object[] isValidOrder(boolean isSpotTrade, OperateType orderType)
	{
		Object[] result = new Object[]{false,""};//isValid,message

		if (!this.isHasLiq())
		{
			result[1] = Language.OrderOperateOrderValidAlert1;
			return result;
		}

		if (this._instrument.get_LastQuotation().get_Ask() == null
			|| this._instrument.get_LastQuotation().get_Bid() == null)
		{
			BigDecimal quoteLot = this.getQuoteLot(isSpotTrade);
			if (!MakeOrder.isQuote(this._instrument, quoteLot))
			{
				result[1] = Language.OrderOperateOrderValidAlert0;
				return result;
			}
		}
		Object[] result2 = this.isAcceptTime();
		if (! (Boolean) result2[0])
		{
			result[1] = result2[1].toString();
			return result;
		}

		if(orderType != OperateType.MultipleClose)
		{
			BigDecimal maxLotGroupByAccount = this.getMaxLotGroupByAccount();
			BigDecimal maxLot = isSpotTrade ? this._instrument.get_MaxDQLot() : this._instrument.get_MaxOtherLot();
			if (maxLotGroupByAccount.compareTo(maxLot) > 0)
			{
				result[1] = Language.OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert1 + maxLot;
				return result;
			}
		}
		result[0] = true;
		return result;
	}
}
