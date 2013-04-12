package tradingConsole.ui;

import java.math.*;
import java.util.*;

import framework.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.settings.RelationOrder.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;

public class MakeSpotTradeOrder extends MakeOrder
{
	//for multi_SpotTrade Order
	private String _dataSourceKey;
	private tradingConsole.ui.grid.BindingSource _bindingSource;

	protected HashMap<Guid, MakeOrderAccount> _makeOrderAccounts;

	public MakeSpotTradeOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		this(tradingConsole, settingsManager, instrument, null);
	}

	public MakeSpotTradeOrder(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Account account)
	{
		super(tradingConsole, settingsManager, instrument, account);

		this._dataSourceKey = Guid.newGuid().toString();
		this._bindingSource = new tradingConsole.ui.grid.BindingSource();

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;

		this._makeOrderAccounts = new HashMap<Guid, MakeOrderAccount> ();
		this._instrument = instrument;
	}

	public int GetDQMaxMove()
	{
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());
			return tradePolicyDetail.get_DQMaxMove();
		}
		return 0;
	}

	public String get_DataSourceKey()
	{
		return this._dataSourceKey;
	}

	public tradingConsole.ui.grid.BindingSource get_BindingSource()
	{
		return this._bindingSource;
	}

	public HashMap<Guid, MakeOrderAccount> get_MakeOrderAccounts()
	{
		return this._makeOrderAccounts;
	}

	//from Make AssignOrder
	public void set_makeOrderAccounts(HashMap<Guid, MakeOrderAccount> value)
	{
		this._makeOrderAccounts = value;
	}

	public MakeOrderAccount get_MakeOrderAccount(Guid accountId)
	{
		return this._makeOrderAccounts.get(accountId);
	}

	public boolean isUsingMixAgent(Account account)
	{
		return (Parameter.isAllowMixAgent
				&& this._settingsManager.get_Customer().get_AllowFreeAgent() == 0
				&& account.get_Type().equals(AccountType.Agent));
	}

	private void setMakeOrderAccounts()
	{
		if (this._makeOrderAccounts.size() > 0)
		{
			return;
		}
		for (Iterator<Account> iterator = this._settingsManager.get_Accounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			if(this._account != null && this._account != account) continue;

			if (!account.isSelectAccount())
			{
				continue;
			}
			if (!account.getIsAllowTrade())
			{
				continue;
			}
			if (!account.isValidAccountForTradePolicy(this._instrument, true))
			{
				continue;
			}
			if (this._settingsManager.get_Customer().get_AllowFreeAgent() == 0)
			{
				if ( (!account.get_Type().equals(AccountType.Agent) && !account.get_IsLocked())
					|| (account.get_Type().equals(AccountType.Agent) && !account.getIsMustAssignOrder()))
				{
					if (account.get_Type().equals(AccountType.Agent))
					{
						if (account.isExistsLockedAccount(this._settingsManager))
						{
							MakeOrderAccount makeOrderAccount = MakeOrderAccount.create(this._tradingConsole, this._settingsManager, account, this._instrument);
							//has error??????????
							if (Parameter.isAllowMixAgent)
							{
								makeOrderAccount.set_BuySellType(BuySellType.Both);
							}
							makeOrderAccount.set_BuySetPrice(this._instrument.get_LastQuotation().getBuy());
							makeOrderAccount.set_SellSetPrice(this._instrument.get_LastQuotation().getSell());
							makeOrderAccount.set_PriceInfo(this._instrument.get_LastQuotation().get_Timestamp(), this._instrument.get_LastQuotation().get_IsQuote());
							this._makeOrderAccounts.put(account.get_Id(), makeOrderAccount);
						}
					}
					else
					{
						MakeOrderAccount makeOrderAccount = MakeOrderAccount.create(this._tradingConsole, this._settingsManager, account, this._instrument);
						makeOrderAccount.set_BuySetPrice(this._instrument.get_LastQuotation().getBuy());
						makeOrderAccount.set_SellSetPrice(this._instrument.get_LastQuotation().getSell());
						makeOrderAccount.set_PriceInfo(this._instrument.get_LastQuotation().get_Timestamp(), this._instrument.get_LastQuotation().get_IsQuote());
						this._makeOrderAccounts.put(account.get_Id(), makeOrderAccount);
					}
				}
			}
			else
			{
				if (!account.get_IsLocked())
				{
					MakeOrderAccount makeOrderAccount = MakeOrderAccount.create(this._tradingConsole, this._settingsManager, account, this._instrument);
					makeOrderAccount.set_BuySetPrice(this._instrument.get_LastQuotation().getBuy());
					makeOrderAccount.set_SellSetPrice(this._instrument.get_LastQuotation().getSell());
					makeOrderAccount.set_PriceInfo(this._instrument.get_LastQuotation().get_Timestamp(), this._instrument.get_LastQuotation().get_IsQuote());
					this._makeOrderAccounts.put(account.get_Id(), makeOrderAccount);
				}
			}
		}
	}

	public Object[] isAccept()
	{
		return this.isAccept(OrderTypeMask.SpotTrade);
	}

	private Object[] isAccept(OrderTypeMask orderTypeMask)
	{
		Object[] result = new Object[2];
		result[0] = false;
		result[1] = "";

		if (MakeOrder.isDisallowTradeSetting(this._settingsManager) || this._instrument.get_MaxDQLot().compareTo(BigDecimal.ZERO) <= 0)
		{
			result[1] = Language.DisallowTradePrompt;
			return result;
		}

		if (!MakeOrder.isAllowOrderTypeMask(this._instrument, orderTypeMask))
		{
			result[1] = Language.TradeConsoleGetOperateHtmlPageAlert3;
			return result;
		}
		if (MakeOrder.isOperateSameInstrument(this._settingsManager, this._instrument)
			|| MakeOrder.isOverRangeOperateOrderUI(this._settingsManager))
		{
			result[1] = Language.OperateDQOrderPrompt + "/" + Language.InstrumentSelectOverRange + " " + Parameter.operateOrderCount;
			return result;
		}
		Object[] result2 = this.isAcceptTime();
		if (! (Boolean)result2[0])
		{
			result[1] = result2[1].toString();
			return result;
		}
		if (this._instrument.get_LastQuotation().get_Ask() == null
			|| this._instrument.get_LastQuotation().get_Bid() == null)
		{
			result[1] = Language.OrderSingleDQPageorderValidAlert0;
			return result;
		}
		this.setMakeOrderAccounts();
		if (this._makeOrderAccounts.values().size() <= 0)
		{
			result[1] = Language.TradeConsoleGetOperateHtmlPageAlert2;
		}
		result[0] = (this._makeOrderAccounts.values().size() > 0);

		return result;
	}

	protected void setDefaultBuySellType(BuySellType buySellType)
	{
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			//exclude BuySellType.Both
			//when initialize set Both
			if (makeOrderAccount.get_BuySellType() != BuySellType.Both)
			{
				makeOrderAccount.set_BuySellType(buySellType);
				makeOrderAccount.set_IsBuyForCurrent(buySellType == BuySellType.Buy);
			}
		}
	}

	public BigDecimal getQuoteLot()
	{
		BigDecimal buyLot = BigDecimal.ZERO;
		BigDecimal sellLot = BigDecimal.ZERO;
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			if (makeOrderAccount.get_BuyLot() != null)
			{
				buyLot = buyLot.add(makeOrderAccount.get_BuyLot());
			}
			if (makeOrderAccount.get_SellLot() != null)
			{
				sellLot = sellLot.add(makeOrderAccount.get_SellLot());
			}
		}
		return buyLot.compareTo(sellLot) > 0 ? buyLot : sellLot;
	}

	public BigDecimal getSumLots()
	{
		BigDecimal sumLots = BigDecimal.ZERO;
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			if (makeOrderAccount.get_IsBuyForCurrent())
			{
				sumLots = sumLots.add(makeOrderAccount.get_BuyLot());
			}
			else
			{
				sumLots = sumLots.add(makeOrderAccount.get_SellLot());
			}
		}
		return sumLots;
	}

	public BigDecimal getSumLotsByCurrentBuySell()
	{
		BigDecimal sumLots = BigDecimal.ZERO;
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			if (makeOrderAccount.get_IsBuyForCurrent() && makeOrderAccount.get_BuyLot() != null)
			{
				sumLots = sumLots.add(makeOrderAccount.get_BuyLot());
			}
			if (!makeOrderAccount.get_IsBuyForCurrent() && makeOrderAccount.get_SellLot() != null)
			{
				sumLots = sumLots.add(makeOrderAccount.get_SellLot());
			}
		}
		return sumLots;
	}

	public void showUi(boolean isDblClickAsk, boolean allowSpt, boolean allowLimit, boolean isSptDefault)
	{
		this.showUi(isDblClickAsk, allowSpt, allowLimit, isSptDefault, null);
	}

	public void showUi(boolean isDblClickAsk, boolean allowSpt, boolean allowLimit, boolean isSptDefault, Boolean closeAllSell)
	{
		if (this._settingsManager.containsOpenContractFormOf(this._instrument.get_Id()))
		{
			this._settingsManager.getOpenContractFormOf(this._instrument.get_Id()).toFront();
			return;
		}

		MakeOrderWindow makeOrderWindow = this._settingsManager.getMakeOrderWindow(this._instrument);
		if (makeOrderWindow != null)
		{
			makeOrderWindow.get_MainWindow().toFront();
			return;
		}

		OperateType operateType = OperateType.MultiSpotTrade;
		MakeOrderAccount makeOrderAccount = null;
		if (this._makeOrderAccounts.values().size() == 1)
		{
			for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
			{
				makeOrderAccount = iterator.next();
				Account account = makeOrderAccount.get_Account();
				if (account.get_Type().equals(AccountType.Agent))
				{
					operateType = OperateType.MultiSpotTrade;
				}
				else
				{
					operateType = OperateType.SingleSpotTrade;
				}
			}
		}
		boolean isBuy = Instrument.getSelectIsBuy(this._instrument, isDblClickAsk);
		if(closeAllSell == null)
		{
			this.setDefaultBuySellType( (isBuy) ? BuySellType.Buy : BuySellType.Sell);
		}
		else
		{
			this.setDefaultBuySellType( (closeAllSell) ? BuySellType.Buy : BuySellType.Sell);
		}
		if (operateType == OperateType.SingleSpotTrade)
		{
			SpotTradeOrderForm spotTradeOrderForm = new SpotTradeOrderForm(this, makeOrderAccount, isDblClickAsk, allowSpt, allowLimit, isSptDefault, closeAllSell);
			spotTradeOrderForm.show();
		}
		else if (operateType == OperateType.MultiSpotTrade)
		{
			MultiDQOrderForm multiDQOrderForm = new MultiDQOrderForm(this._tradingConsole, this._settingsManager, this,
				this._instrument, isDblClickAsk, allowSpt, allowLimit, isSptDefault);
			multiDQOrderForm.show();
			if(closeAllSell != null) multiDQOrderForm.closeAllButton_actionPerformed(null);
		}
	}

	protected void initialize2(DataGrid grid, boolean ascend, boolean caseOn, boolean isAllowEditIsBuy, IPropertyChangingListener propertyChangingListener)
	{
		this._bindingSource.clearPropertyChangingListener();
		this._bindingSource.addPropertyChangingListener(propertyChangingListener);
		MakeOrderAccount.initialize(grid, this._dataSourceKey, this._makeOrderAccounts.values(), this._bindingSource, isAllowEditIsBuy, 0, 0);
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			makeOrderAccount.update(this._dataSourceKey);
		}
		int column = this._bindingSource.getColumnByName(MakeOrderAccountGridColKey.Code);
		grid.sortColumn(column, ascend);
	}

	protected void initialize2(DataGrid grid, boolean ascend, boolean caseOn, boolean isAllowEditIsBuy, boolean isDblClickAsk, int dQMaxMove, int stepSize,
							   IPropertyChangingListener propertyChangingListener)
	{
		this._bindingSource.clearPropertyChangingListener();
		this._bindingSource.addPropertyChangingListener(propertyChangingListener);
		MakeOrderAccount.initialize(grid, this._dataSourceKey, this._makeOrderAccounts.values(), this._bindingSource, isAllowEditIsBuy, dQMaxMove, stepSize);
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			if (makeOrderAccount.get_BuySellType() == BuySellType.Both)
			{
				boolean isBuy = Instrument.getSelectIsBuy(makeOrderAccount.get_Instrument(), isDblClickAsk);
				makeOrderAccount.set_BuySellType( (isBuy) ? BuySellType.Buy : BuySellType.Sell);
				makeOrderAccount.set_IsBuyForCurrent( (isBuy) ? true : false);
				makeOrderAccount.update(this._dataSourceKey);

				/*boolean isBuy2 = !isBuy;
					 makeOrderAccount.set_BuySellType((isBuy2)?BuySellType.Buy:BuySellType.Sell);
					 makeOrderAccount.set_IsBuyForCurrent((isBuy2)?true:false);
					 makeOrderAccount.add2(this._dataSourceKey);

//Michael?????????????
					 makeOrderAccount.set_BuySellType(BuySellType.Both);*/
			}
			else
			{
				makeOrderAccount.update(this._dataSourceKey);
			}
		}
		int column = this._bindingSource.getColumnByName(MakeOrderAccountGridColKey.Code);
		grid.sortColumn(column, ascend);
	}

	public void initialize(DataGrid grid, boolean ascend, boolean caseOn, IPropertyChangingListener propertyChangingListener)
	{
		this.initialize2(grid, ascend, caseOn, true, propertyChangingListener);
	}

	public void initialize(DataGrid grid, boolean ascend, boolean caseOn, boolean isDblClickAsk, int dQMaxMove,int stepSize,
						   IPropertyChangingListener propertyChangingListener)
	{
		this.initialize2(grid, ascend, caseOn, true, isDblClickAsk, dQMaxMove, stepSize, propertyChangingListener);
	}

	public void unbind()
	{
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			try
			{
				makeOrderAccount.unbind(this._dataSourceKey, this._bindingSource);
			}
			catch(Exception e)
			{

			}
		}
	}

	public void update()
	{
		this.update(null, false);
	}

	public void update(RelationSnapshot relationSnapshot, boolean isHasDeal)
	{
		MakeOrderAccount[] makeOrderAccounts = new MakeOrderAccount[this._makeOrderAccounts.values().size()];
		makeOrderAccounts = this._makeOrderAccounts.values().toArray(makeOrderAccounts);

		if(isHasDeal)
		{
			BigDecimal answerLot = this._instrument.get_LastQuotation().get_AnswerLot();
			if (answerLot != null)
			{
				BigDecimal leftBuyLot = answerLot;
				BigDecimal leftSellLot = answerLot;
				try
				{
					Arrays.sort(makeOrderAccounts, MakeOrderAccount.comparatorForAdjustingLot);

					ArrayList<RelationOrder> relationOrderList = new ArrayList<RelationOrder> ();
					for (MakeOrderAccount makeOrderAccount : makeOrderAccounts)
					{
						relationOrderList.addAll(makeOrderAccount.getOutstandingOrders().values());
					}
					RelationOrder[] relationOrders = new RelationOrder[relationOrderList.size()];
					relationOrders = relationOrderList.toArray(relationOrders);
					Arrays.sort(relationOrders, RelationOrder.comparatorForAdjustingLot);

					for (RelationOrder relationOrder : relationOrders)
					{
						if (relationOrder.get_IsSelected())
						{
							boolean isBuy = !relationOrder.get_IsBuy();
							BigDecimal leftLot = isBuy ? leftBuyLot : leftSellLot;
							if (leftLot.compareTo(BigDecimal.ZERO) > 0)
							{
								BigDecimal closeLot = relationOrder.get_CloseLot();
								closeLot = closeLot.compareTo(leftLot) > 0 ? leftLot : closeLot;
								relationOrder.set_LiqLot(closeLot);

								if(isBuy)
								{
									leftBuyLot = leftBuyLot.subtract(closeLot);
								}
								else
								{
									leftSellLot = leftSellLot.subtract(closeLot);
								}
							}
							else
							{
								relationOrder.set_IsSelected(false);
							}
						}
					}

					for (MakeOrderAccount makeOrderAccount : makeOrderAccounts)
					{
						BigDecimal closeLot = BigDecimal.ZERO;
						for (RelationOrder relationOrder : makeOrderAccount.getOutstandingOrders().values())
						{
							if (relationOrder.get_IsSelected())
							{
								closeLot = closeLot.add(relationOrder.get_CloseLot());
							}
							makeOrderAccount.get_BindingSourceForOutstanding().update(relationOrder);
						}
						if (closeLot.compareTo(BigDecimal.ZERO) > 0)
						{
							if (relationSnapshot != null) relationSnapshot.updateSnapshot(makeOrderAccount);
							makeOrderAccount.adjustLot(closeLot);
						}
						else
						{
							if (relationSnapshot != null) relationSnapshot.clearSnapshot(makeOrderAccount);

							BigDecimal leftLot = makeOrderAccount.get_IsBuyForCurrent() ? leftBuyLot : leftSellLot;
							BigDecimal usedLot = makeOrderAccount.adjustLot(leftLot.compareTo(BigDecimal.ZERO) > 0 ? leftLot : BigDecimal.ZERO);
							if(makeOrderAccount.get_IsBuyForCurrent())
							{
								leftBuyLot = leftBuyLot.subtract(usedLot);
							}
							else
							{
								leftSellLot = leftSellLot.subtract(usedLot);
							}
						}
					}
				}
				finally
				{
					this._instrument.get_LastQuotation().clearAnswerLot();
				}
			}
		}

		for (MakeOrderAccount makeOrderAccount : makeOrderAccounts)
		{
			makeOrderAccount.set_BuySetPrice(this._instrument.get_LastQuotation().getBuy());
			makeOrderAccount.set_SellSetPrice(this._instrument.get_LastQuotation().getSell());
			makeOrderAccount.set_PriceInfo(this._instrument.get_LastQuotation().get_Timestamp(), this._instrument.get_LastQuotation().get_IsQuote());
			makeOrderAccount.update(this._dataSourceKey);
		}
	}

	public void finalize() throws Throwable
	{
		MakeOrder.setLastQuotationIsUsing(this._instrument, false);
		this.unbind();

		super.finalize();
	}

	public void closeAll(boolean closeAllSell)
	{
		for (Iterator<MakeOrderAccount> iterator = this._makeOrderAccounts.values().iterator(); iterator.hasNext(); )
		{
			MakeOrderAccount makeOrderAccount = iterator.next();
			makeOrderAccount.cleanOutStandingTable(closeAllSell);
			makeOrderAccount.closeAll();
		}
	}
}
