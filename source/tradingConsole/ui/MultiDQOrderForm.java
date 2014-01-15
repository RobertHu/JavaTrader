package tradingConsole.ui;

import java.math.*;
import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;

import com.jidesoft.grid.*;
import com.jidesoft.swing.*;
import framework.*;
import framework.diagnostics.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.framework.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;

public class MultiDQOrderForm extends FrameBase2
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private MakeSpotTradeOrder _makeSpotTradeOrder;
	private MakeOrderAccount _makeOrderAccount; //record current makeOrderAccount
	private boolean _isDblClickAsk;

	private Instrument _instrument;
	private boolean _isHasDeal;
	private OpenCloseRelationSite _relationOrderSite;

	private JideTabbedPane tabbedPane = new JideTabbedPane();
	private JPanel spotTradePanel = new JPanel();

	//MultiTextArea instrumentNarrative = new MultiTextArea();
	NoneResizeableTextField instrumentQuoteDescription = new NoneResizeableTextField();

	private int _dQMaxMove = 0;
	private BigDecimal _dealTotalLot = null;
	private SelectedAccountChangedListener _selectedAccountChangedListener;

	public MultiDQOrderForm(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		super(tradingConsole, settingsManager, instrument);
		try
		{
			jbInit();
			this._relationOrderSite = new OpenCloseRelationSite(this);

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	public MultiDQOrderForm(TradingConsole tradingConsole, SettingsManager settingsManager, MakeSpotTradeOrder makeSpotTradeOrder, Instrument instrument,
							boolean isDblClickAsk, boolean allowSpt, boolean allowLimit, boolean isSptDefault)
	{
		this(tradingConsole, settingsManager, makeSpotTradeOrder, instrument,
							isDblClickAsk, allowSpt, allowLimit, isSptDefault, null);
	}

	public MultiDQOrderForm(TradingConsole tradingConsole, SettingsManager settingsManager, MakeSpotTradeOrder makeSpotTradeOrder, Instrument instrument,
							boolean isDblClickAsk, boolean allowSpt, boolean allowLimit, boolean isSptDefault, Boolean closeAllSell)
	{
		//this(tradingConsole, settingsManager, instrument);
		super(tradingConsole, settingsManager, instrument);
		this._dQMaxMove = makeSpotTradeOrder.GetDQMaxMove();
		this._instrument = instrument;
		try
		{
			jbInit();
			this._relationOrderSite = new OpenCloseRelationSite(this);

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._makeSpotTradeOrder = makeSpotTradeOrder;
		this._isDblClickAsk = isDblClickAsk;

		this._isHasDeal = this.isHasDeal();

		this.lotStaticText.setText(Language.OrderLMTlblLot);
		this.dealButton.setText(Language.OrderMultiDQbtnDeal);
		this.clearButton.setText(Language.OrderMultiDQbtnClear);
		//null.setText(Language.OrderMultiDQlblBalanceA);
		//null.setText(Language.OrderMultiDQlblEquityA);
		//null.setText(Language.OrderAssignlblACTotalLotsA);
		//null.setText("");
		//null.setText(Language.OrderAssignlblLiqLotsA);
		//null.setText("");
		this.submitButton.setText(Language.OrderMultiDQbtnSubmit);
		this.exitButton.setText(Language.OrderMultiDQbtnExit);
		this.closeAllButton.setText(Language.CloseAll);
		this.instalmentButton.setText(InstalmentLanguage.PaymentMode);

		//this.instrumentDescriptionStaticText.setText(this._instrument.get_Description());
		this.setTitle(this._instrument.get_DescriptionForTrading());
		InstrumentPriceProvider priceProvider = new InstrumentPriceProvider(this._instrument);
		this.bidButton.set_PriceProvider(priceProvider);
		this.askButton.set_PriceProvider(priceProvider);

		//fill accountTable
		int stepSize = this._instrument.get_NumeratorUnit();
		this._makeSpotTradeOrder.initialize(this.accountTable, true, false, this._isDblClickAsk, this._dQMaxMove, stepSize, new PropertyChangingListener(this));
		this.accountTable.get_BindingSource().addPropertyChangedListener(new IPropertyChangedListener()
		{
			public void propertyChanged(Object owner, PropertyDescriptor propertyDescriptor, Object oldValue, Object newValue, int row, int column)
			{
				updateInstalmentButtonStatus();
			}
		});
		boolean isBuy = Instrument.getSelectIsBuy(this._instrument, isDblClickAsk);
		if(isBuy)
		{
			for(int index = 0; index < this.accountTable.get_BindingSource().getRowCount(); index++)
			{
				MakeOrderAccount makeOrderAccount = (MakeOrderAccount) (this.accountTable.get_BindingSource()).getObject(index);
				this.updateInstalmentInfoForCurrent(makeOrderAccount, isBuy);
			}
		}
		this.updateInstalmentButtonStatus();
		this.initializeSaveMaxMove();
		this.updateAccountTableEditable();
		//int column = this.accountTable.get_BindingSource().getColumnByName(MakeOrderAccountGridColKey.SetPriceString);
		//TableColumnChooser.hideColumn(this.accountTable, column);
		this._selectedAccountChangedListener = new SelectedAccountChangedListener(this);
		this.accountTable.addSelectedRowChangedListener(this._selectedAccountChangedListener);
		//if (isNeedDQMaxMove)
		//{
		//	this.setSize(625, 280);
		//	this.accountTable.getSize().width = 298;
		//	this.doLayout();
		//}
		this._makeOrderAccount = (MakeOrderAccount) (this.accountTable.get_BindingSource()).getObject(0);
		this.lotNumeric.setText(this.getFormatDefaultLot());

		//set default row = first row

		//this.accountTable.setSelectedRow(0);
		//int column = this._makeSpotTradeOrder.get_BindingSource().getColumn(MakeOrderAccountGridColKey.LotString);
		//this.accountTable.requestFocus();
		//this.accountTable.setCurrentCell(0, column);
		if(allowSpt && this._instrument.get_MaxDQLot().compareTo(BigDecimal.ZERO) > 0)
		{
			this.tabbedPane.addTab(Language.spotTradeOrderFormTitle, this.spotTradePanel);
		}
		if(allowLimit && this._instrument.get_MaxOtherLot().compareTo(BigDecimal.ZERO) > 0)
		{
			limitOrderForm = new LimitOrderForm(this, this._tradingConsole, this._settingsManager, this._instrument, null, null, true, true);
			this.tabbedPane.add(Language.limitOrderFormTitle, limitOrderForm);
		}
		boolean allowMatchingOrder = MakeOrder.canMatchingOrder(this._settingsManager, this._instrument, null);
		if(allowMatchingOrder && this._instrument.get_MaxOtherLot().compareTo(BigDecimal.ZERO) > 0)
		{
			Object[] result = MakeOrder.isAllowMakeLimitOrder(this._tradingConsole, this._settingsManager,
				this._instrument, null);
			if ( (Boolean)result[0])
			{
				MakeLimitOrder makeLimitOrder = (MakeLimitOrder)result[2];
				makeLimitOrder.setDefaultBuySellType(BuySellType.Buy);
				matchingOrderForm = new MatchingOrderForm(this, this._tradingConsole, this._settingsManager, this._instrument, null, null, true, makeLimitOrder, false, closeAllSell);
				this.tabbedPane.addTab(Language.matchingOrderFormTitle, matchingOrderForm);
			}
		}

		boolean showDeliveryForm = TradingConsole.DeliveryHelper.getDeliveryAccounts(this._tradingConsole, this._instrument).size() > 0;
		if(showDeliveryForm)
		{
			deliveryForm
				= new DeliveryForm(this, this._tradingConsole, this._instrument, null, null, closeAllSell);
			this.tabbedPane.addTab(Language.deliveryFormTitle, deliveryForm);
		}

		//init outstanding Order Grid
		this._makeOrderAccount.set_IsBuyForCurrent(isBuy);
		this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both,
			( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._relationOrderSite);
		//Fill first Account Infomation
		this.fillAccount(this._makeOrderAccount.get_Account());
		//this.instrumentDescriptionStaticText.setForeground(Color.blue);
		this.changeColor();

		this.refreshPrice();

		this.setBuySellEnabled(false);

		this.lotStaticText.setVisible(this._isHasDeal);
		this.dealButton.setVisible(this._isHasDeal);
		this.lotNumeric.setVisible(this._isHasDeal);
		this.lotNumeric.setEditable(false);
		if (this._isHasDeal)
		{
			this.setDealStatus(false);
			this.dealButton.setEnabled(false);
		}

		this.outTimeSchedulerStart();

		this.setMakeOrderWindow();
		this.setNotifyIsAcceptWindow();

		this.accountTable.requestFocus();

		this.setTitle(this._instrument.get_DescriptionForTrading());

		this.tabbedPane.setHideOneTab(true);

		ChangeListener changeListener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				TradingConsole.traceSource.trace(TraceType.Information, "#########JTabPane changed ");
				outTimeSchedulerStop();
				outTimeSchedulerStart();
				int selectedIndex = tabbedPane.getSelectedIndex();
				if(selectedIndex != -1)
				{
					String title = tabbedPane.getTitleAt(selectedIndex);
					if (title.equalsIgnoreCase(Language.spotTradeOrderFormTitle))
					{
						limitOrderForm.updateEditingValue();
						resetData();
						initializeOutstanding();
					}
					else
					{
						updateEditingValue();
						limitOrderForm.initializeOutstanding();
					}
				}
			}
		};
		this.tabbedPane.addChangeListener(changeListener);

		if (isSptDefault)
		{
			int index = this.tabbedPane.indexOfTab(Language.spotTradeOrderFormTitle);
			if(index != -1)
			{
				this.tabbedPane.setSelectedIndex(index);
			}
		}
		else
		{
			int index = this.tabbedPane.indexOfTab(Language.limitOrderFormTitle);
			if(index != -1)
			{
				this.tabbedPane.setSelectedIndex(index);
			}
		}
		this.resetBuySell();
	}

	JPanel saveMaxMovePanel = null;
	private void initializeSaveMaxMove()
	{
		int maxMove = 0;
		int stepSize = this._instrument.get_NumeratorUnit();

		BindingSource makeOrderAccounts = this.accountTable.get_BindingSource();
		for(int index = 0; index < makeOrderAccounts.getRowCount(); index++)
		{
			MakeOrderAccount makeOrderAccount = (MakeOrderAccount)makeOrderAccounts.getObject(index);
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(makeOrderAccount.get_Account().get_TradePolicyId(),this._instrument.get_Id());
			maxMove = Math.max(maxMove, tradePolicyDetail.get_DQMaxMove());
		}

		boolean isNeed = (maxMove > 0 && maxMove > stepSize);
		this.saveMaxMovePanel.setVisible(isNeed);
		this.dQMaxMoveNumeric.setValue(0);
		if(isNeed)
		{
			JDQMoveSpinnerHelper.applyMaxDQMove(this.dQMaxMoveNumeric, maxMove, stepSize);

			this.dQMaxMoveNumeric.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e)
				{
					applyDQMaxMove();
				}
			});

			Boolean oldValue = MaxMoveAutoFillHelper.get_SaveMaxMoveAsDefaultForMultiAccount(this._instrument.get_Id());
			this.remberMaxMoveCheckBox.setSelected(oldValue != null ? oldValue.booleanValue() : false);
			Integer defaultValue = MaxMoveAutoFillHelper.getDefaultMaxMove(Guid.empty, this._instrument.get_Id());
			if(defaultValue != null)
			{
				this.dQMaxMoveNumeric.setValue(defaultValue);
			}

			 makeOrderAccounts = this.accountTable.get_BindingSource();
			 for (int index = 0; index < makeOrderAccounts.getRowCount(); index++)
			 {
				 MakeOrderAccount makeOrderAccount = (MakeOrderAccount)makeOrderAccounts.getObject(index);
				 TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(makeOrderAccount.get_Account().get_TradePolicyId(),
					 this._instrument.get_Id());
				 maxMove = tradePolicyDetail.get_DQMaxMove();
				 isNeed = (maxMove > 0 && maxMove > stepSize);
				 if(isNeed)
				 {
					 Integer defaultMaxMove = MaxMoveAutoFillHelper.getDefaultMaxMove(makeOrderAccount.get_Account().get_Id(), this._instrument.get_Id());
					 if(defaultMaxMove != null)
					 {
						 makeOrderAccount.set_DQMaxMove(defaultMaxMove);
						 makeOrderAccount.update(this._makeSpotTradeOrder.get_DataSourceKey());
					 }
				 }
			 }
		}
	}

	private void updateInstalmentButtonStatus()
	{
		boolean canInstalment = false;
		BindingSource makeOrderAccounts = this.accountTable.get_BindingSource();
		for(int index = 0; index < makeOrderAccounts.getRowCount(); index++)
		{
			MakeOrderAccount makeOrderAccount = (MakeOrderAccount)makeOrderAccounts.getObject(index);
			if(makeOrderAccount.get_IsBuyForCurrent() &&
			   (makeOrderAccount.get_CanAdvancePayment() || makeOrderAccount.get_CanInstalment()))
			{
				canInstalment = true;
				break;
			}
		}
		this.instalmentButton.setVisible(canInstalment);
	}

	private void doSetupInstalment()
	{
		ArrayList<MakeOrderAccount> accounts = new ArrayList<MakeOrderAccount>();
		HashMap<Guid, BigDecimal> lots = new HashMap<Guid,BigDecimal>();

		BindingSource makeOrderAccounts = this.accountTable.get_BindingSource();
		for(int index = 0; index < makeOrderAccounts.getRowCount(); index++)
		{
			MakeOrderAccount makeOrderAccount = (MakeOrderAccount)makeOrderAccounts.getObject(index);
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());
			if(makeOrderAccount.get_IsBuyForCurrent() && tradePolicyDetail.get_InstalmentPolicyId() != null)
			{
				if((makeOrderAccount.get_CanAdvancePayment() || makeOrderAccount.get_CanInstalment())
				   && makeOrderAccount.get_BuyLot().compareTo(BigDecimal.ZERO) > 0)
				{
					accounts.add(makeOrderAccount);
					lots.put(makeOrderAccount.get_Account().get_Id(), makeOrderAccount.get_BuyLot());
				}
			}
		}
		MakeOrderAccount[] accountArray = new MakeOrderAccount[accounts.size()];
		accountArray = accounts.toArray(accountArray);
		InstalmentForm form
			= new InstalmentForm(this, accountArray, lots, this._instrument, this._settingsManager, this.instalmentInfoList);
		JideSwingUtilities.centerWindow(form);
		form.show();
		form.toFront();
		if(form.get_IsConfirmed())
		{
			this.instalmentInfoList = form.get_InstalmentInfoList();
		}
	}

	private void applyDQMaxMove()
	{
		int stepSize = this._instrument.get_NumeratorUnit();
		BindingSource makeOrderAccounts = this.accountTable.get_BindingSource();
		int newMaxMove = (Integer)this.dQMaxMoveNumeric.getValue();
		for(int index = 0; index < makeOrderAccounts.getRowCount(); index++)
		{
			MakeOrderAccount makeOrderAccount = (MakeOrderAccount)makeOrderAccounts.getObject(index);
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());

			int maxMove = tradePolicyDetail.get_DQMaxMove();
			boolean isNeed = (maxMove > 0 && maxMove > stepSize);
			if(isNeed)
			{
				makeOrderAccount.set_DQMaxMove(Math.min(maxMove, newMaxMove));
				makeOrderAccount.update(this._makeSpotTradeOrder.get_DataSourceKey());
			}
		}
	}

	private void updateAccountTableEditable()
	{
		BindingSource makeOrderAccounts = this.accountTable.get_BindingSource();
		int column = makeOrderAccounts.getColumnByName(MakeOrderAccountGridColKey.IsBuyForCombo);
		for(int index = 0; index < makeOrderAccounts.getRowCount(); index++)
		{
			MakeOrderAccount makeOrderAccount = (MakeOrderAccount)makeOrderAccounts.getObject(index);
			boolean isEditable = !this._makeSpotTradeOrder.isUsingMixAgent(makeOrderAccount.get_Account());
			makeOrderAccounts.setEditable(index, column, isEditable);
		}
	}

	private void initializeOutstanding()
	{
		//boolean isBuy = Instrument.getSelectIsBuy(this._instrument, this._isDblClickAsk);
		//this._makeOrderAccount.set_IsBuyForCurrent(isBuy);
		boolean isBuy = this._makeOrderAccount.get_IsBuyForCurrent();
		this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both,
			( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._relationOrderSite);
	}

	@Override
	protected void outTimeSchedulerStart()
	{
		int selectedIndex = this.tabbedPane.getSelectedIndex();
		if(selectedIndex != -1)
		{
			String title = this.tabbedPane.getTitleAt(selectedIndex);
			if (title.equalsIgnoreCase(Language.spotTradeOrderFormTitle))
			{
				super.outTimeSchedulerStart();
			}
		}
	}

	private void changeColor()
	{
		//Color color = BuySellColor.getColor(this._makeOrderAccount.get_IsBuyForCurrent());
		//this.lotStaticText.setBackground(color);
		//null.setBackground(color);
		//null.setBackground(color);
		//null.setBackground(color);
		//null.setBackground(color);
	}

	private void fillAccount(Account account)
	{
		/*
		 boolean isNoShowAccountStatus = this._settingsManager.get_Customer().get_IsNoShowAccountStatus();
		 if (isNoShowAccountStatus)
		 {
		  null.setText("-");
		  null.setText("-");
		 }
		 else
		 {
		  short accountCurrencyDecimals = account.get_Currency().get_Decimals();
		  null.setForeground(NumericColor.getColor(account.get_Balance()));
		  null.setText(Functions.format(account.get_Balance(), accountCurrencyDecimals));
		  null.setForeground(NumericColor.getColor(account.get_Equity()));
		  null.setText(Functions.format(account.get_Equity(), accountCurrencyDecimals));
		 }
		 */
	}

	@Override
	public void refreshPrice()
	{
		this.bidButton.updatePrice();
		this.askButton.updatePrice();
		/*this.setPriceBidStaticText.setText( (!this._instrument.get_IsSinglePrice()) ? this._instrument.get_Bid() : this._instrument.get_Ask());
		this.setPriceAskStaticText.setText(this._instrument.get_Ask());*/
	    if(this.limitOrderForm != null) this.limitOrderForm.refreshPrice();
		if(this.deliveryForm != null) this.deliveryForm.refreshPrice();
		this._makeSpotTradeOrder.update(this._selectedAccountChangedListener.get_RelationSnapshot(), this.isHasDeal());
	}

	public void dispose2()
	{
		this.dispose();
	}

	public void dispose()
	{
		try
		{
			this._makeOrderAccount.unbindOutstanding();
			super.dispose();
		}
		catch (Throwable exception)
		{
		}
	}

	private String getFormatDefaultLot()
	{
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
			this._instrument.get_Id());
		BigDecimal maxDQLot = tradePolicyDetail.get_DefaultLot().multiply(new BigDecimal(Convert.toString(this.accountTable.getRowCount())));
		//BigDecimal maxDQLot = new BigDecimal(this._instrument.get_MaxDQLot().doubleValue() * this.accountTable.m_rowCount);
		return AppToolkit.getFormatLot(maxDQLot, false);
	}

	private void setBuySellVisible(boolean isVisible)
	{
		this.setBuySellEnabled(isVisible);
	}

	private void setBuySellEnabled(boolean isVisible)
	{
		isVisible = (isVisible && this.isValidPrice());
		this.submitButton.setEnabled(isVisible);
	}

	private boolean isValidPrice()
	{
		boolean isValidPrice = false;
		if(this.bidButton.isQuoting() || this.askButton.isQuoting())
		/*if (this.setPriceAskStaticText.getText().equals("")
			|| this.setPriceBidStaticText.getText().equals("")
			|| this.setPriceAskStaticText.getText().equals("-")
			|| this.setPriceBidStaticText.getText().equals("-"))*/
		{
			if (!this._isHasDeal)
			{
				//BigDecimal lot = Functions.convertStringToBigDecimal(this.lotNumeric.getText());
				BigDecimal sumLots = this._makeSpotTradeOrder.getSumLots();
				if (MakeOrder.isQuote(this._instrument, sumLots))
				{
					isValidPrice = true;
				}
			}
		}
		else
		{
			isValidPrice = true;
		}
		return isValidPrice;
	}

	private boolean isHasDeal()
	{
		return (this._settingsManager.get_Customer().get_MultiAccountsOrderType() == 1);
	}

	//For deal
	public void setDealStatus(boolean isDeal)
	{
		this.dealButton.setEnabled(!isDeal);

		//this.lotNumeric.setEditable(!isDeal);
		this.accountTable.setEditable(!isDeal);
		this.outstandingOrderTable.setEditable(!isDeal);
		this.setBuySellVisible(isDeal);
		//this.doLayout();
		if(isDeal)
		{
			this._instrument.freezeRefreshQuotation();
		}
		else
		{
			this._instrument.unfreezeRefreshQuotation();
			this.refreshPrice();
		}
	}

	public void resetBuySell()
	{
		BigDecimal sumLots = this._makeSpotTradeOrder.getSumLots();
		if (!this._isHasDeal)
		{
			this.setBuySellEnabled(sumLots.compareTo(BigDecimal.ZERO) > 0);
		}
	}

	public void quoteArrived()
	{
		super.quoteArrived();
		this.setBuySellEnabled(true);
		this.priceValidTimeSchedulerStart(OperateType.MultiSpotTrade);
	}

	private void updateEditingValue()
	{
		this.accountTable.updateEditingValue();
		this.outstandingOrderTable.updateEditingValue();
	}

	private void deal()
	{
		this.fillVerificationInfoScheculerStop();

		this._dealTotalLot = null;

		this.updateEditingValue();
		this.outTimeSchedulerStop();
		this.setDealStatus(true);

		//BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.lotNumeric.getText());
		BigDecimal lot = this._makeSpotTradeOrder.getQuoteLot();
		if (MakeOrder.isQuote(this._instrument, lot))
		{
			this.bidButton.quotingPrice();
			this.askButton.quotingPrice();
			/*this.setPriceBidStaticText.setText("-");
			this.setPriceAskStaticText.setText("-");*/
			/*
			if (this._instrument.get_PriceValidTime() <= 0)
			{
				this.dispose();
				return;
			}
		    */
			this.quote(lot, BSStatus.Both);
			this.setBuySellEnabled(false);
		}
		else
		{
			if (this._instrument.get_PriceValidTime() <= 0)
			{
				this.submit();
			}
			else
			{
				MakeOrder.setLastQuotationIsUsing(this._instrument, true);
				if (Parameter.nonQuoteVerificationUiDelay <= 0)
				{
					this.priceValidTimeSchedulerStart(OperateType.MultiSpotTrade);
				}
				else
				{
					this.askButton.quotingPrice();
					this.bidButton.quotingPrice();
					/*this.setPriceBidStaticText.setText("-");
					this.setPriceAskStaticText.setText("-");*/
					this.setBuySellEnabled(false);
					this.dealButton.setVisible(false);
					this.fillVerificationInfoScheculerStart();
				}
			}
		}
	}

	public void dealDelayProcess()
	{
		//this.refreshPrice();
		this.setBuySellEnabled(true);
		this.priceValidTimeSchedulerStart(OperateType.MultiSpotTrade);
	}

	@Override
	protected void priceValidTimeSchedulerStart(OperateType operateType)
	{
		this._dealTotalLot = AppToolkit.convertStringToBigDecimal(this.lotNumeric.getText());
		this.outstandingOrderTable.setEditable(true);
		this.accountTable.setEditable(true);

		super.priceValidTimeSchedulerStart(operateType);
	}

	@Override
	public void resetData()
	{
		//this._makeOrderAccount.set_BuySellType(BuySellType.Both);
		super.resetData();
		this._dealTotalLot = null;
		//this.submitButton.setEnabled(false);
		//this.dealButton.setEnabled(true);
		if (this.isHasDeal())
		{
			MakeOrder.setLastQuotationIsUsing(this._instrument, false);
		}
	}

	private void upateLot(BigDecimal sumLots)
	{
		if (sumLots.compareTo(BigDecimal.ZERO) > 0)
		{
			this.lotNumeric.setText(AppToolkit.getFormatLot(sumLots, false));
			if(this.isHasDeal() && this._dealTotalLot != null
				&& sumLots.compareTo(this._dealTotalLot) > 0)
			{
				super.priceValidTimeSchedulerStop();
				this.resetData();
				return;
			}
		}
		else
		{
			this.lotNumeric.setText(this.getFormatDefaultLot());
		}

		if(this.isHasDeal())
		{
			this.dealButton.setEnabled(this._dealTotalLot == null && !this.bidButton.isQuoting());
		}
		else
		{
			this.submitButton.setEnabled(true);
		}
	}

	//input Lot validation
	private void lotNumeric_FocusLost()
	{
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.lotNumeric.getText());
		BigDecimal sumLots = this._makeSpotTradeOrder.getSumLots();
		if (lot.compareTo(BigDecimal.ZERO) <= 0)
		{
			this.upateLot(sumLots);
			return;
		}
		else
		{
			BigDecimal maxLot = this._instrument.get_MaxDQLot().multiply(new BigDecimal(Convert.toString(this.accountTable.getRowCount())));
			//BigDecimal maxLot = new BigDecimal(this._instrument.get_MaxDQLot().doubleValue() * this.accountTable.m_rowCount);
			if (maxLot.compareTo(BigDecimal.ZERO) != 0	&& lot.compareTo(maxLot) > 0)
			{
				AlertDialogForm.showDialog(this, null, true,
										   Language.OrderSingleDQPagetextLot_OnblurAlert0 + "(" + AppToolkit.getFormatLot(maxLot, true) + ")!");
				this.upateLot(sumLots);
				return;
			}
			else
			{
				if (lot.compareTo(sumLots) < 0)
				{
					this.upateLot(sumLots);
					return;
				}
				else
				{
					this.lotNumeric.setText(AppToolkit.getFormatLot(lot, this._makeOrderAccount.get_Account(), this._instrument));
				}
			}
		}
	}

	private boolean isValidOrder(boolean isPrompt)
	{
		if(!MakeOrder.isAllowOrderType(this._instrument, this.getOrderType()))
		{
			if(isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
			}
			return false;
		}

		if (this._isHasDeal)
		{
			BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.lotNumeric.getText());
			if (lot.compareTo(BigDecimal.ZERO) <= 0)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this, null, true, Language.OrderOperateOrderValidAlert1);
				}
				return false;
			}
		}

		BigDecimal sumLots = this._makeSpotTradeOrder.getSumLots();
		if (sumLots.compareTo(BigDecimal.ZERO) <= 0)
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderOperateOrderValidAlert1);
			}
			return false;
		}
		else
		{
			BigDecimal maxDQLot = this._instrument.get_MaxDQLot();
			boolean DQMaxLotApplyAccount = this._settingsManager.get_SystemParameter().get_DQMaxLotApplyAccount();
			boolean exceedMaxLot = DQMaxLotApplyAccount ? false : sumLots.compareTo(maxDQLot) > 0;
			if (maxDQLot.compareTo(BigDecimal.ZERO) != 0 && exceedMaxLot)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderOperateOrderOperateAccountGrid_ValidateEditAlert0
										   + AppToolkit.getFormatLot(maxDQLot, this._makeOrderAccount.get_Account(), this._instrument) + ")!");
				return false;
			}
		}

		//if (this._isAssigningOrder) return true;//???????????
		Object[] result = this._makeSpotTradeOrder.isAcceptTime();
		if (! (Boolean)result[0])
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, result[1].toString());
			}
			return false;
		}

		int validAccountNumber = this._makeSpotTradeOrder.get_MakeOrderAccounts().size();
		for(MakeOrderAccount account : this._makeSpotTradeOrder.get_MakeOrderAccounts().values())
		{
			BuySellType buySellType = account.get_BuySellType();
			BigDecimal liqLots = account.getSumLiqLots(buySellType == BuySellType.Buy);
			BigDecimal lot = (buySellType == BuySellType.Buy) ? account.get_BuyLot() : account.get_SellLot();
			if(lot.compareTo(BigDecimal.ZERO) <= 0)
			{
				validAccountNumber--;
				continue;
			}

			if (liqLots.compareTo(BigDecimal.ZERO) > 0)
			{
				if (lot.compareTo(liqLots) != 0)
				{
					if (isPrompt)
					{
						String info = account.get_Code() + ": " + Language.OrderLMTPageorderValidAlert7;
						AlertDialogForm.showDialog(this, null, true, info);
						/*TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
							this._instrument.get_Id());
						liqLots = AppToolkit.fixLot(liqLots, false, tradePolicyDetail, this._makeOrderAccount.get_Account());
						this.totalLotTextField.setText(AppToolkit.getFormatLot(liqLots, this._makeOrderAccount.get_Account()));
						this.updateBuySellLot();*/
					}
					return false;
				}
			}
			else if(!account.get_Account().get_AllowAddNewPosition()
				|| !this._instrument.getAllowAddNewPosition(account.get_IsBuyForCurrent()))
			{
				if(isPrompt) AlertDialogForm.showDialog(this, null, true, account.get_Account().get_Code() + ": " + Language.NewPositionIsNotAllowed);
				validAccountNumber--;
				account.set_BuyLot(BigDecimal.ZERO);
				account.set_SellLot(BigDecimal.ZERO);
				TradingConsole.bindingManager.update(this._makeSpotTradeOrder.get_DataSourceKey(), account);
			}
		}

		if (validAccountNumber == 0)
		{
			if (isPrompt) AlertDialogForm.showDialog(this, null, true, Language.OrderOperateOrderValidAlert1);
			return false;
		}

		return true;
	}

	private void submit()
	{
		this._inSubmit = true;

		try
		{
			this.updateEditingValue();

			this.outTimeSchedulerStop();
			this.priceValidTimeSchedulerStop();

			if (this._pauseSumit)
			{
				this._pauseSumit = false;
				this.outTimeSchedulerStart();
				return;
			}

			if (!this.isValidPrice())
			{
				AlertDialogForm.showDialog(this, null, true, Language.OperateOrderMethod1PageSubmitAlert1);
				this.resetData();
				this.outTimeSchedulerStart();
				return;
			}
			if (this.isValidOrder(true))
			{
				this.saveMaxMoves();
				this.fillInstalmentInfo();
				VerificationOrderForm verificationOrderForm = new VerificationOrderForm(this, "Verification Order", true, this._tradingConsole,
					this._settingsManager, this._instrument,
					this._makeSpotTradeOrder.get_MakeOrderAccounts(), OrderType.SpotTrade, OperateType.MultiSpotTrade, this._isHasDeal, null, null);
				//verificationOrderForm.show();
				if(verificationOrderForm.isCanceled())
				{
					this.outTimeSchedulerStart();
				}
				else
				{
					this.outTimeSchedulerStop();
				}
			}
			else
			{
				this.resetData();
				this.outTimeSchedulerStart();
				return;
			}
		}
		finally
		{
			this._inSubmit = false;
		}
		//this.outTimeSchedulerStart();
	}

	private void fillInstalmentInfo()
	{
		BindingSource makeOrderAccounts = this.accountTable.get_BindingSource();
		for (int index = 0; index < makeOrderAccounts.getRowCount(); index++)
		{
			MakeOrderAccount makeOrderAccount = (MakeOrderAccount)makeOrderAccounts.getObject(index);
			makeOrderAccount.set_InstalmentInfo(null);
		}

		if(this.instalmentInfoList == null) return;
		for(Guid accountId : this.instalmentInfoList.keySet())
		{
			InstalmentInfo instalmentInfo = this.instalmentInfoList.get(accountId);
			if(!instalmentInfo.isFullPayment())
			{
				for (int index = 0; index < makeOrderAccounts.getRowCount(); index++)
				{
					MakeOrderAccount makeOrderAccount = (MakeOrderAccount)makeOrderAccounts.getObject(index);
					if (makeOrderAccount.get_Account().get_Id().equals(accountId))
					{
						makeOrderAccount.set_InstalmentInfo(instalmentInfo);
						break;
					}
				}
			}
		}
	}

	private void saveMaxMoves()
	{
		MaxMoveAutoFillHelper.set_SaveMaxMoveAsDefaultForMultiAccount(this._instrument.get_Id(), this.remberMaxMoveCheckBox.isSelected());
		if(this.remberMaxMoveCheckBox.isSelected())
		{
			int newMaxMove = (Integer)this.dQMaxMoveNumeric.getValue();
			MaxMoveAutoFillHelper.setDefaultMaxMove(Guid.empty,
					this._instrument.get_Id(), newMaxMove);

			BindingSource makeOrderAccounts = this.accountTable.get_BindingSource();
			for (int index = 0; index < makeOrderAccounts.getRowCount(); index++)
			{
				MakeOrderAccount makeOrderAccount = (MakeOrderAccount)makeOrderAccounts.getObject(index);
				int maxMove = makeOrderAccount.get_DQMaxMove();

				MaxMoveAutoFillHelper.setDefaultMaxMove(makeOrderAccount.get_Account().get_Id(),
					this._instrument.get_Id(), maxMove);
			}
		}
	}

	static class SelectedAccountChangedListener implements ISelectedRowChangedListener
	{
		private MultiDQOrderForm _owner;
		private RelationOrder.RelationSnapshot _relationSnapshot = new RelationOrder.RelationSnapshot();

		public SelectedAccountChangedListener(MultiDQOrderForm owner)
		{
			this._owner = owner;
		}

		public RelationOrder.RelationSnapshot get_RelationSnapshot()
		{
			return this._relationSnapshot;
		}

		public void selectedRowChanged(DataGrid source)
		{
			DataGrid table = this._owner.accountTable;
			int row = table.getSelectedRow();
			if(row != -1)
			{
				row = TableModelWrapperUtils.getActualRowAt(table.getModel(), row);
				MakeOrderAccount makeOrderAccount = (MakeOrderAccount)table.get_BindingSource().getObject(row);
				boolean isBuy = makeOrderAccount.get_IsBuyForCurrent();

				if (! (this._owner._makeOrderAccount.get_Account().get_Id().equals(makeOrderAccount.get_Account().get_Id())))
				{
					this._relationSnapshot.takeSnapshot(this._owner._relationOrderSite);
					this._owner._makeOrderAccount = makeOrderAccount;
					this._owner.fillAccount(this._owner._makeOrderAccount.get_Account());
					this._owner._makeOrderAccount.initializeOutstanding(this._owner.outstandingOrderTable, isBuy, false, BuySellType.Both,
						( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._owner._relationOrderSite);
					this._relationSnapshot.applySnapshot(this._owner._relationOrderSite);
				}
			}
		}
	}

	private boolean _hasWarning = false;
	static class PropertyChangingListener implements IPropertyChangingListener
	{
		private MultiDQOrderForm _owner;
		public PropertyChangingListener(MultiDQOrderForm owner)
		{
			this._owner = owner;
		}

		public void propertyChanging(PropertyChangingEvent e)
		{
			DataGrid table = this._owner.accountTable;
			int row = e.get_Row();
			int column = e.get_Column();
			MakeOrderAccount makeOrderAccount = (MakeOrderAccount)e.get_Owner();
			boolean isBuy = makeOrderAccount.get_IsBuyForCurrent();

			PropertyDescriptor property = e.get_PropertyDescriptor();
			/*if (property.get_Name().equals(MakeOrderAccountGridColKey.IsBuyForCombo))
			{
				boolean isEditable = !this._owner._makeSpotTradeOrder.isUsingMixAgent(this._owner._makeOrderAccount.get_Account());
				this._owner._makeSpotTradeOrder.get_BindingSource().setEditable(column, isEditable);
			}*/

			if (property.get_Name().equals(MakeOrderAccountGridColKey.IsBuyForCombo))
			{
				if(!e.get_NewValue().equals(e.get_OldValue()))
				{
					if (this._owner._makeSpotTradeOrder.isUsingMixAgent(this._owner._makeOrderAccount.get_Account()))
					{
						e.set_Cancel(true);
						return;
					}

					if (isBuy)
					{
						this._owner._makeOrderAccount.set_BuyLot(BigDecimal.ZERO);
					}
					else
					{
						this._owner._makeOrderAccount.set_SellLot(BigDecimal.ZERO);
					}

					this._owner._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, this._owner.getOrderType().isSpot(), null);
					isBuy = e.get_NewValue().toString().equalsIgnoreCase(Language.Buy);
					this._owner._makeOrderAccount.set_IsBuyForCurrent(isBuy);
					this._owner.updateInstalmentInfoForCurrent(isBuy);
					this._owner._makeOrderAccount.update(this._owner._makeSpotTradeOrder.get_DataSourceKey(), this._owner._isDblClickAsk);
					this._owner._makeOrderAccount.initializeOutstanding(this._owner.outstandingOrderTable, isBuy, false, BuySellType.Both,
						( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._owner._relationOrderSite);
					BigDecimal sumLots = this._owner._makeSpotTradeOrder.getSumLots();
					if (!this._owner._isHasDeal)
					{
						this._owner.setBuySellEnabled(sumLots.compareTo(BigDecimal.ZERO) > 0);
					}
					else
					{
						this._owner.dealButton.setEnabled(sumLots.compareTo(BigDecimal.ZERO) > 0);
					}
					/*if (this._owner._makeSpotTradeOrder.isUsingMixAgent(this._owner._makeOrderAccount.get_Account()))
						 {
					 table.setSelectionForeground(BuySellColor.getColor(isBuy));
						 }*/
					   this._owner.updateInstalmentButtonStatus();
				}
			}
			else if (property.get_Name().equals(MakeOrderAccountGridColKey.LotString))
			{
				BigDecimal oldValue = AppToolkit.convertStringToBigDecimal(e.get_OldValue().toString());
				BigDecimal newValue = AppToolkit.convertStringToBigDecimal(e.get_NewValue().toString());
				if (newValue.compareTo(BigDecimal.ZERO) == 0 && !StringHelper.isNullOrEmpty(e.get_NewValue().toString()))
				{
					//e.set_Cancel(true);
					//return;
					e.set_NewValue("");
					newValue = BigDecimal.ZERO;
				}
				BigDecimal newValue2 = AppToolkit.convertStringToBigDecimal(AppToolkit.getFormatLot(newValue, makeOrderAccount.get_Account(), this._owner._instrument));
				if (newValue2.compareTo(BigDecimal.ZERO) == 0 || newValue2.compareTo(newValue) != 0)
				{
					//e.set_Cancel(true);
					//return;
					if(newValue2.compareTo(BigDecimal.ZERO) != 0 || newValue.compareTo(BigDecimal.ZERO) > 0)
					{
						String info = StringHelper.format(Language.LotIsNotValidAndWillChangeTo, new Object[]
							{newValue, newValue2});
						AlertDialogForm.showDialog(this._owner, null, true, info);
						e.set_NewValue(AppToolkit.getFormatLot(newValue2, makeOrderAccount.get_Account(), this._owner._instrument));
						newValue = newValue2;
					}
					else
					{
						e.set_NewValue("");
						newValue = BigDecimal.ZERO;
					}
				}
				BigDecimal sumLots = this._owner._makeSpotTradeOrder.getSumLots();
				sumLots = sumLots.subtract(oldValue).add(newValue);
				BigDecimal maxDQLot = this._owner._instrument.get_MaxDQLot();
				boolean DQMaxLotApplyAccount = this._owner._settingsManager.get_SystemParameter().get_DQMaxLotApplyAccount();
				boolean exceedMaxLot = DQMaxLotApplyAccount ? newValue.compareTo(maxDQLot) > 0 : sumLots.compareTo(maxDQLot) > 0;
				if (maxDQLot.compareTo(BigDecimal.ZERO) != 0 && exceedMaxLot)
				{
					if(this._owner._inSubmit)
					{
						this._owner._pauseSumit = true;
					}
					AlertDialogForm.showDialog(this._owner, null, true, Language.OrderOperateOrderOperateAccountGrid_ValidateEditAlert0
											   + AppToolkit.getFormatLot(maxDQLot, this._owner._makeOrderAccount.get_Account(), this._owner._instrument) + ")!");
					e.set_Cancel(true);
					return;
				}

				if (this._owner._makeSpotTradeOrder.isUsingMixAgent(this._owner._makeOrderAccount.get_Account()))
				{
					//Object value = table.getValueAt(row, 1);
					//this._owner._makeOrderAccount.set_IsBuyForCurrent(value.toString().equals("true"));
				}

				//input lot < sumLiqLots, clear liqLots....
				isBuy = this._owner._makeOrderAccount.get_IsBuyForCurrent();
				BigDecimal sumLiqLots = this._owner._makeOrderAccount.getSumLiqLots(isBuy);
				if (sumLiqLots.compareTo(BigDecimal.ZERO) != 0 && newValue.compareTo(sumLiqLots) != 0)
				{
					if(this._owner._inSubmit)
					{
						this._owner._pauseSumit = true;
					}
					AlertDialogForm.showDialog(this._owner, null, true, Language.OrderLMTPageorderValidAlert7);
					//this._owner._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, this._owner.getOrderType().isSpot(), null);
					e.set_Cancel(true);
					return;
				}
				//BigDecimal openLot = new BigDecimal( ( (Double) (newValue - sumLiqLots)).doubleValue());
				boolean isHasMakeNewOrder = newValue.compareTo(sumLiqLots) > 0;
				boolean isAcceptLot = this._owner._makeOrderAccount.isAcceptLot(isBuy, newValue, isHasMakeNewOrder); //openLot);
				if (!isAcceptLot)
				{
					if(this._owner._inSubmit)
					{
						this._owner._pauseSumit = true;
					}

					AlertDialogForm.showDialog(this._owner, null, true, Language.NewOrderAcceptedHedging);
					e.set_Cancel(true);
					return;
				}
				this._owner.lotNumeric.setText(AppToolkit.getFormatLot(sumLots, this._owner._makeOrderAccount.get_Account(), this._owner._instrument));
				if (!this._owner._isHasDeal)
				{
					this._owner.setBuySellEnabled(sumLots.compareTo(BigDecimal.ZERO) > 0);
				}
				else
				{
					this._owner.dealButton.setEnabled(sumLots.compareTo(BigDecimal.ZERO) > 0);
				}
			}
			else if (property.get_Name().equals(MakeOrderAccountGridColKey.DQMaxMove))
			{
				TradePolicyDetail tradePolicyDetail = this._owner._settingsManager.getTradePolicyDetail(this._owner._makeOrderAccount.get_Account().get_TradePolicyId(),
					this._owner._instrument.get_Id());
				int maxMove = tradePolicyDetail.get_DQMaxMove();
				if (maxMove <= 0)
				{
					e.set_Cancel(true);
					return;
				}
				else
				{
					int newValue = Integer.parseInt(e.get_NewValue().toString());
					if (newValue < 0)
					{
						e.set_Cancel(true);
						return;
					}
					if (newValue > maxMove)
					{
						if(this._owner._inSubmit)
						{
							this._owner._pauseSumit = true;
						}

						//restricted to the zone starting from maxMove pips
						AlertDialogForm.showDialog(this._owner, null, true, Language.OrderOperateOrderOperateAccountGrid_ValidateEditAlert5 + " "
							+ Integer.toString(maxMove) + " " + Language.OrderOperateOrderOperateAccountGrid_ValidateEditAlert6);
						e.set_Cancel(true);
						return;
					}
					this._owner._makeOrderAccount.set_DQMaxMove(newValue);
				}
			}
		}
	}

	public void updateAccount(BigDecimal accountLot, boolean openOrderIsBuy)
	{
		if(accountLot.compareTo(BigDecimal.ZERO) > 0)
		{
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());
			accountLot = AppToolkit.fixLot(accountLot, false, tradePolicyDetail, this._makeOrderAccount);
		}

		boolean isBuy = this._makeOrderAccount.get_IsBuyForCurrent();
		if (isBuy)
		{
			this._makeOrderAccount.set_BuyLot(accountLot);
		}
		else
		{
			this._makeOrderAccount.set_SellLot(accountLot);
		}

		TradingConsole.bindingManager.update(this._makeSpotTradeOrder.get_DataSourceKey(), this._makeOrderAccount);
		//this._makeOrderAccount.update(this._makeSpotTradeOrder.get_DataSourceKey(), this._isDblClickAsk);
		//this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both,( (!isBuy) ? BuySellType.Buy : BuySellType.Sell));

		BigDecimal sumLots = this._makeSpotTradeOrder.getSumLots();
		if (!this._isHasDeal)
		{
			this.setBuySellEnabled(sumLots.compareTo(BigDecimal.ZERO) > 0);
		}
		else
		{
			this.upateLot(sumLots);
		}
	}

	static class OpenCloseRelationSite implements IOpenCloseRelationSite
	{
		private MultiDQOrderForm _owner;

		public OpenCloseRelationSite(MultiDQOrderForm owner)
		{
			this._owner = owner;
		}

		public OperateType getOperateType()
		{
			return OperateType.MultiSpotTrade;
		}

		public Instrument getInstrument()
		{
			return this._owner._instrument;
		}

		public MakeOrderAccount getMakeOrderAccount()
		{
			return this._owner._makeOrderAccount;
		}

		public BigDecimal getLot()
		{
			return this._owner._makeOrderAccount.get_LotCurrent();
		}

		public PVStaticText2 getLiqLotValueStaticText()
		{
			return null;
		}

		public BigDecimal getTotalQuantity()
		{
			return AppToolkit.convertStringToBigDecimal(this._owner.lotNumeric.getText());
		}

		public DataGrid getRelationDataGrid()
		{
			return this._owner.outstandingOrderTable;
		}

		public JDialog getFrame()
		{
			return this._owner;
		}

		public void updateAccount(BigDecimal accountLot, boolean openOrderIsBuy)
		{
			this._owner.updateAccount(accountLot, openOrderIsBuy);
		}

		public void updateTotalColseLot(BigDecimal totalCloseLot)
		{
		}

		public OrderType getOrderType()
		{
			return OrderType.SpotTrade;
		}

		public Boolean isMakeLimitOrder()
		{
			return null;
		}

		public boolean isDelivery()
		{
			return false;
		}

		public void addPlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
		{
		}

		public void removePlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
		{
		}

		public JTextField getCloseLotEditor()
		{
			return null;
		}

		public JTextField getTotalLotEditor()
		{
			return null;
		}

		public boolean allowChangeCloseLot()
		{
			return true;
		}

		public DataGrid getAccountDataGrid()
		{
			return this._owner.accountTable;
		}

		public void rebind()
		{
			this._owner.rebind();
		}
	}

	/*public void paint(Graphics g)
	{
		super.paint(g);
		try
		{
			int x = this.setPriceBidStaticText.getLocation().x - 1;
			int y = this.setPriceBidStaticText.getLocation().y - 1;
			int width = this.outstandingOrderTable.getSize().width;
			int height = this.setPriceBidStaticText.getSize().height + 2;
			g.drawRect(x, y, width, height);
		}
		catch (Throwable exception)
		{}
	}*/

	//SourceCode End///////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.addWindowListener(new MultiDQOrderUi_this_windowAdapter(this));

		this.setSize(_dQMaxMove > 0 ? 572 : 552, 520);
		this.setResizable(true);

		this.getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);
		spotTradePanel.setLayout(gridBagLayout3);
		//this.setTitle(Language.multiDQOrderFormTitle);
		spotTradePanel.setBackground(FormBackColor.multiDQOrderForm);

		timerTime = new Timer(500, new MultiDQOrderForm_timerTime_actionAdapter(this));
		Font font = new Font("SansSerif", Font.BOLD, 14);
		instrumentQuoteDescription.setFont(font);
		lotStaticText.setText("Lot");
		lotNumeric.setHorizontalAlignment(SwingConstants.RIGHT);
		lotNumeric.addFocusListener(new MultiDQOrderForm_lotNumeric_focusAdapter(this));
		dealButton.setText("Deal");
		clearButton.setText("Clear");
		submitButton.setText("Submit");
		exitButton.setText("Exit");

		exitButton.addActionListener(new MultiDQOrderUi_exitButton_actionAdapter(this));
		dealButton.addActionListener(new MultiDQOrderForm_dealButton_actionAdapter(this));
		clearButton.addActionListener(new MultiDQOrderForm_clearButton_actionAdapter(this));
		submitButton.addActionListener(new MultiDQOrderForm_submitButton_actionAdapter(this));
		exitButton.addActionListener(new MultiDQOrderForm_exitButton_actionAdapter(this));
		closeAllButton.addActionListener(new MultiDQOrderForm_closeAllButton_actionAdapter(this));
		this.instalmentButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doSetupInstalment();
			}
		});
		bidButton.setEnabled(false);
		askButton.setEnabled(false);

		JScrollPane openOrderScrollPane = new JScrollPane(outstandingOrderTable);
		outstandingOrderTable.enableRowStripe();
		JScrollPane accountScrollPane = new JScrollPane(accountTable);
		accountTable.enableRowStripe();
		//accountTable.setClickCountToStart(2);
		//accountTable.setAutoStartCellEditing(false);
		FocusListener focusListener = new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
				accountTable.cancelEditing();
			}

			public void focusLost(FocusEvent e)
			{
				//outstandingOrderTable.updateEditingValue();
			}
		};
		this.outstandingOrderTable.addFocusListener(focusListener);

		FocusListener focusListener2 = new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
				outstandingOrderTable.updateEditingValue();
			}

			public void focusLost(FocusEvent e)
			{
				//accountTable.updateEditingValue();
			}
		};
		this.accountTable.addFocusListener(focusListener2);

		this.accountTable.addCellEditorListener(new JideCellEditorAdapter()
		{
			@Override
			public void editingStarted(ChangeEvent changeEvent)
			{
				super.editingStarted(changeEvent);

				boolean enable = !bidButton.isQuoting();
				if (isHasDeal())
				{
					dealButton.setEnabled(enable);
					submitButton.setEnabled(enable && !dealButton.isEnabled());
				}
				else
				{
					submitButton.setEnabled(enable);
				}
			}
		});

		spotTradePanel.add(bidButton, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 80, 55));
		spotTradePanel.add(askButton, new GridBagConstraints(2, 0, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 2, 0, 0), 80, 55));

		spotTradePanel.add(this.instrumentQuoteDescription, new GridBagConstraints2(0, 1, 4, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 0, 0), 0, 20));

		if(!StringHelper.isNullOrEmpty(this._instrument.get_QuoteDescription()))
		{
			this.instrumentQuoteDescription.setText(this._instrument.get_QuoteDescription());
		}
		else
		{
			this.instrumentQuoteDescription.setVisible(false);
		}


		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new GridBagLayout());
		panel.add(accountScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
			, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		saveMaxMovePanel = new JPanel();
		saveMaxMovePanel.setBackground(Color.WHITE);
		saveMaxMovePanel.setLayout(new GridBagLayout());
		panel.add(saveMaxMovePanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 0));

		this.dQMaxMoveStaticText.setText(Language.OrderSingleDQlblDQMaxMove);
		saveMaxMovePanel.add(dQMaxMoveStaticText, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 60, 0));
		saveMaxMovePanel.add(dQMaxMoveNumeric, new GridBagConstraints(1, 0, 1, 1, 0.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 30, 0));
		remberMaxMoveCheckBox.setText(Language.SaveDQlblDQMaxMoveAsDefault);
		remberMaxMoveCheckBox.setBackground(Color.WHITE);
		saveMaxMovePanel.add(remberMaxMoveCheckBox, new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		spotTradePanel.add(panel, new GridBagConstraints(0, 2, 6, 1, 0.0, 1.0
			, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 0, 0), 0, 0));

		spotTradePanel.add(instalmentButton, new GridBagConstraints(0, 3, 6, 1, 0.0, 0.0
			, GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));

		spotTradePanel.add(openOrderScrollPane, new GridBagConstraints(4, 0, 6, 4, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 10, 0, 5), 0, 0));

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.setBackground(null);
		spotTradePanel.add(buttonPanel, new GridBagConstraints(0, 4, 6, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		buttonPanel.add(submitButton, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(5, 5, 10, 0), 10, 0));

		buttonPanel.add(clearButton, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(5, 1, 10, 0), 10, 0));

		buttonPanel.add(dealButton, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(5, 1, 10, 0), 10, 0));

		spotTradePanel.add(lotStaticText, new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 2, 0), 0, 0));
		spotTradePanel.add(lotNumeric, new GridBagConstraints(5, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 2), 40, 0));

		spotTradePanel.add(closeAllButton, new GridBagConstraints(6, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 8, 10, 0), 0, 0));

		spotTradePanel.add(exitButton, new GridBagConstraints(9, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 1, 10, 5), 10, 0));
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	//PVStaticText2 instrumentDescriptionStaticText = new PVStaticText2();
	//PVStaticText2 setPriceBidStaticText = new PVStaticText2();
	//PVStaticText2 separatorStaticText = new PVStaticText2();
	//PVStaticText2 setPriceAskStaticText = new PVStaticText2();
	PVStaticText2 lotStaticText = new PVStaticText2();
	JFormattedTextField lotNumeric = new JFormattedTextField(new DecimalFormat());
	PVButton2 dealButton = new PVButton2();
	PVButton2 clearButton = new PVButton2();
	PVButton2 submitButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();
	PVButton2 closeAllButton = new PVButton2();
	DataGrid accountTable = new DataGrid("AccountTable");
	BidAskButton bidButton = new BidAskButton(true);
	BidAskButton askButton = new BidAskButton(false);
	private PVStaticText2 dQMaxMoveStaticText = new PVStaticText2();
	private JSpinner dQMaxMoveNumeric = new JSpinner();
	private JCheckBox remberMaxMoveCheckBox = new JCheckBox();
	private PVButton2 instalmentButton = new PVButton2();
	private HashMap<Guid, InstalmentInfo> instalmentInfoList = null;

	//Table verifyTable = new Table();
	//PVStaticText2 openOrderStaticText = new PVStaticText2();
	//java.awt.Panel accountpanel = new Panel();
	DataGrid outstandingOrderTable = new DataGrid("OutstandingOrderTable");
	//java.awt.GridBagLayout gridBagLayout1 = new GridBagLayout();
	//java.awt.Panel verifypanel = new Panel();
	//java.awt.GridBagLayout gridBagLayout2 = new GridBagLayout();

	java.awt.GridBagLayout gridBagLayout3 = new GridBagLayout();
	private Timer timerTime;
	private LimitOrderForm limitOrderForm;
	private DeliveryForm deliveryForm;
	private MatchingOrderForm matchingOrderForm;
	private boolean _pauseSumit = false;
	private boolean _inSubmit = false;

	@Override
	public void startCloseTimer()
	{
		this.timerTime.start();
	}

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	public void closeAllButton_actionPerformed(ActionEvent e)
	{
		this.closeAll();
	}

	private void closeAll()
	{
		BigDecimal lot = this._makeOrderAccount.closeAll();
		this.updateAccount(lot, false);
	}

	public void lotNumeric_focusLost(FocusEvent e)
	{
		this.lotNumeric_FocusLost();
	}

	public void dealButton_actionPerformed(ActionEvent e)
	{
		this.deal();
	}

	public void clearButton_actionPerformed(ActionEvent e)
	{
		this.accountTable.updateEditingValue();
		for(int index = 0; index < this.accountTable.get_BindingSource().getRowCount();  index++)
		{
			MakeOrderAccount makeOrderAccount
				= (MakeOrderAccount)(this.accountTable.get_BindingSource().getObject(index));
			makeOrderAccount.set_BuyLot(BigDecimal.ZERO);
			makeOrderAccount.set_SellLot(BigDecimal.ZERO);
			this.accountTable.get_BindingSource().update(makeOrderAccount);
		}
		this.submitButton.setEnabled(false);
		this.dealButton.setEnabled(false);

		this.outstandingOrderTable.updateEditingValue();
		for(int index = 0; index < this.outstandingOrderTable.get_BindingSource().getRowCount();  index++)
		{
			RelationOrder relationOrder
				= (RelationOrder)(this.outstandingOrderTable.get_BindingSource().getObject(index));
			relationOrder.resetLiqLot();
			relationOrder.set_IsSelected(false);
			this.outstandingOrderTable.get_BindingSource().update(relationOrder);
		}
		this.lotNumeric.setValue(BigDecimal.ZERO);
	}

	public void submitButton_actionPerformed(ActionEvent e)
	{
		this.submit();
	}

	public void timerTime_actionPerformed(ActionEvent e)
	{
		this.timerTime.stop();
		this.dispose();
	}

	/*public void accountTable_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == e.VK_ENTER || e.getID() == e.VK_TAB)
		{
			this.accountTable.requestFocus();
		}
	}

	public void outstandingOrderTable_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == e.VK_ENTER || e.getID() == e.VK_TAB)
		{
			this.outstandingOrderTable.requestFocus();
		}
	}*/

	class MultiDQOrderUi_this_windowAdapter extends WindowAdapter
	{
		private MultiDQOrderForm adaptee;
		MultiDQOrderUi_this_windowAdapter(MultiDQOrderForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}

	private OrderType getOrderType()
	{
		return OrderType.SpotTrade;
	}

	private void rebind()
	{
		this.initializeOutstanding();
	}

	private void updateInstalmentInfoForCurrent(boolean isBuy)
	{
		MakeOrderAccount makeOrderAccount = this._makeOrderAccount;
		this.updateInstalmentInfoForCurrent(makeOrderAccount, isBuy);
	}

	private void updateInstalmentInfoForCurrent(MakeOrderAccount makeOrderAccount, boolean isBuy)
	{
		if(makeOrderAccount.get_CanAdvancePayment()
			|| (makeOrderAccount.get_CanInstalment() && !makeOrderAccount.hasMultiPayment()))
		{
			Guid accountId = makeOrderAccount.get_Account().get_Id();
			if (isBuy && (this.instalmentInfoList == null || !this.instalmentInfoList.containsKey(accountId)))
			{
				InstalmentInfo instalmentInfo = makeOrderAccount.get_CanAdvancePayment()?
					InstalmentInfo.createDefaultAdvancePaymentInfo(makeOrderAccount) :
					InstalmentInfo.createDefaultInstalmentInfo(makeOrderAccount);

				if (this.instalmentInfoList == null)
					this.instalmentInfoList = new HashMap<Guid, InstalmentInfo> ();
				this.instalmentInfoList.put(accountId, instalmentInfo);
			}
			else
			{
				this.instalmentInfoList.remove(accountId);
			}
		}
	}
}

class MultiDQOrderForm_timerTime_actionAdapter implements ActionListener
{
	private MultiDQOrderForm adaptee;
	MultiDQOrderForm_timerTime_actionAdapter(MultiDQOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.timerTime_actionPerformed(e);
	}
}

/*
 class MultiDQOrderForm_verifyTable_actionAdapter implements ActionListener
 {
 private MultiDQOrderForm adaptee;
 MultiDQOrderForm_verifyTable_actionAdapter(MultiDQOrderForm adaptee)
 {
  this.adaptee = adaptee;
 }

 public void actionPerformed(ActionEvent e)
 {
  adaptee.verifyTable_actionPerformed(e);
 }
 }
 */


/*class MultiDQOrderForm_accountTable_keyAdapter extends KeyAdapter
{
	private MultiDQOrderForm adaptee;
	MultiDQOrderForm_accountTable_keyAdapter(MultiDQOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e)
	{
		adaptee.accountTable_keyPressed(e);
	}
}

class MultiDQOrderForm_outstandingOrderTable_keyAdapter extends KeyAdapter
{
	private MultiDQOrderForm adaptee;
	MultiDQOrderForm_outstandingOrderTable_keyAdapter(MultiDQOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e)
	{
		adaptee.outstandingOrderTable_keyPressed(e);
	}
}*/

class MultiDQOrderForm_exitButton_actionAdapter implements ActionListener
{
	private MultiDQOrderForm adaptee;
	MultiDQOrderForm_exitButton_actionAdapter(MultiDQOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}

class MultiDQOrderForm_closeAllButton_actionAdapter implements ActionListener
{
	private MultiDQOrderForm adaptee;
	MultiDQOrderForm_closeAllButton_actionAdapter(MultiDQOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.closeAllButton_actionPerformed(e);
	}
}

class MultiDQOrderForm_submitButton_actionAdapter implements ActionListener
{
	private MultiDQOrderForm adaptee;
	MultiDQOrderForm_submitButton_actionAdapter(MultiDQOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}

class MultiDQOrderForm_dealButton_actionAdapter implements ActionListener
{
	private MultiDQOrderForm adaptee;
	MultiDQOrderForm_dealButton_actionAdapter(MultiDQOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.dealButton_actionPerformed(e);
	}
}

class MultiDQOrderForm_clearButton_actionAdapter implements ActionListener
{
	private MultiDQOrderForm adaptee;
	MultiDQOrderForm_clearButton_actionAdapter(MultiDQOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.clearButton_actionPerformed(e);
	}
}

class MultiDQOrderForm_lotNumeric_focusAdapter extends FocusAdapter
{
	private MultiDQOrderForm adaptee;
	MultiDQOrderForm_lotNumeric_focusAdapter(MultiDQOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.lotNumeric_focusLost(e);
	}
}

class MultiDQOrderUi_exitButton_actionAdapter implements ActionListener
{
	private MultiDQOrderForm adaptee;
	MultiDQOrderUi_exitButton_actionAdapter(MultiDQOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}
/*
 class MultiDQOrderUi_multiDQOrderTab_componentAdapter extends ComponentAdapter
 {
 private MultiDQOrderForm adaptee;
 MultiDQOrderUi_multiDQOrderTab_componentAdapter(MultiDQOrderForm adaptee)
 {
  this.adaptee = adaptee;
 }

 public void componentResized(ComponentEvent e)
 {
  adaptee.multiDQOrderTab_componentResized(e);
 }
 }
 */
