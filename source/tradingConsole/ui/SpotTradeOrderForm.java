package tradingConsole.ui;

import java.beans.*;
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

import com.jidesoft.swing.*;
import framework.*;
import framework.diagnostics.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;

public class SpotTradeOrderForm extends FrameBase2
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private MakeSpotTradeOrder _makeSpotTradeOrder;
	private MakeOrderAccount _makeOrderAccount;

	private Instrument _instrument;

	private boolean _isDblClickAsk;
	private Boolean _closeAllSell = null;
	private BigDecimal _dealTotalLot = null;

	public SpotTradeOrderForm()
	{
		super(null, null, null);
		try
		{
			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	public SpotTradeOrderForm(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument)
	{
		super(tradingConsole, settingsManager, instrument);
		try
		{
			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	public SpotTradeOrderForm(MakeSpotTradeOrder makeSpotTradeOrder, MakeOrderAccount makeOrderAccount,
							  boolean isDblClickAsk, boolean allowSpt, boolean allowLimit, boolean isSptDefault)
	{
		this(makeSpotTradeOrder, makeOrderAccount, isDblClickAsk, allowSpt, allowLimit, isSptDefault, null);
	}

	public SpotTradeOrderForm(MakeSpotTradeOrder makeSpotTradeOrder, MakeOrderAccount makeOrderAccount,
							  boolean isDblClickAsk, boolean allowSpt, boolean allowLimit, boolean isSptDefault, Boolean closeAllSell)
	{
		this(makeSpotTradeOrder.get_TradingConsole(), makeSpotTradeOrder.get_SettingsManager(), makeOrderAccount.get_Instrument());

		this._makeSpotTradeOrder = makeSpotTradeOrder;
		this._tradingConsole = this._makeSpotTradeOrder.get_TradingConsole();
		this._settingsManager = this._makeSpotTradeOrder.get_SettingsManager();

		this._makeOrderAccount = makeOrderAccount;
		this._instrument = this._makeSpotTradeOrder.get_Instrument();
		InstrumentPriceProvider instrumentPriceProvider = new InstrumentPriceProvider(this._instrument);
		this.bidButton.set_PriceProvider(instrumentPriceProvider);
		this.askButton.set_PriceProvider(instrumentPriceProvider);

		this._isDblClickAsk = isDblClickAsk;
		this._closeAllSell = closeAllSell;

		this.accountStaticText.setText(Language.OrderSingleDQlblAccountCodeA);
		this.totalLotStaticText.setText(Language.OrderSingleDQlblLot);
		this.closeLotStaticText.setText(Language.CloseLot);
		this.dQMaxMoveStaticText.setText(Language.OrderSingleDQlblDQMaxMove);
		this.remberMaxMoveCheckBox.setText(Language.SaveDQlblDQMaxMoveAsDefault);
		this.dealButton.setText(Language.OrderMultiDQbtnDeal);
		//this.buyButton.setText(Language.OrderSingleDQbtnBuy);
		//this.sellButton.setText(Language.OrderSingleDQbtnSell);
		this.resetButton.setText(Language.OrderSingleDQbtnReset);
		this.exitButton.setText(Language.OrderSingleDQbtnExit);
		this.closeAllButton.setText(Language.CloseAll);

		if(allowSpt && this._instrument.get_MaxDQLot().compareTo(BigDecimal.ZERO) > 0)
		{
			this.tabbedPane.addTab(Language.spotTradeOrderFormTitle, this.spotTradePanel);
		}
		if(allowLimit && this._instrument.get_MaxOtherLot().compareTo(BigDecimal.ZERO) > 0)
		{
			limitOrderForm = new LimitOrderForm(this, this._tradingConsole, this._settingsManager, this._instrument, null, null, true, true, closeAllSell, makeOrderAccount);
			this.tabbedPane.addTab(Language.limitOrderFormTitle, limitOrderForm);
		}
		boolean allowMatchingOrder = MakeOrder.canMatchingOrder(this._settingsManager, this._instrument, null);
		if(allowMatchingOrder && this._instrument.get_MaxOtherLot().compareTo(BigDecimal.ZERO) > 0)
		{
			Object[] result = MakeOrder.isAllowMakeLimitOrder(this._tradingConsole, this._settingsManager,
				this._instrument, null);
			if ( (Boolean)result[0])
			{
				MakeLimitOrder makeLimitOrder = (MakeLimitOrder)result[2];

				allowMatchingOrder = false;
				for(MakeOrderAccount account : makeLimitOrder.get_MakeOrderAccounts().values())
				{
					allowMatchingOrder |= MakeOrder.canMatchingOrder(this._settingsManager, this._instrument, account.get_Account());
				}
				if(allowMatchingOrder)
				{
					makeLimitOrder.setDefaultBuySellType(BuySellType.Buy);
					matchingOrderForm = new MatchingOrderForm(this, this._tradingConsole, this._settingsManager, this._instrument, null, null, true,
						makeLimitOrder, false, closeAllSell);
					if (closeAllSell != null)
						matchingOrderForm.closeAllButton_actionPerformed(null);
					this.tabbedPane.addTab(Language.matchingOrderFormTitle, matchingOrderForm);
				}
			}
		}

		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),this._instrument.get_Id());
		int stepSize = this._instrument.get_NumeratorUnit();
		int maxMove = tradePolicyDetail.get_DQMaxMove();
		boolean isNeed = (maxMove > 0 && maxMove > stepSize);
		this.dQMaxMoveStaticText.setVisible(isNeed);
		this.dQMaxMoveNumeric.setVisible(isNeed);
		this.remberMaxMoveCheckBox.setVisible(isNeed);
		this.dQMaxMoveNumeric.setValue(0);//String.valueOf(tradePolicyDetail.get_DQMaxMove()));		\
		if(isNeed)
		{
			Boolean oldValue = MaxMoveAutoFillHelper.get_SaveMaxMoveAsDefaultForSingleAccount(this._instrument.get_Id());
			remberMaxMoveCheckBox.setSelected(oldValue != null ? oldValue.booleanValue() : false);

			JDQMoveSpinnerHelper.applyMaxDQMove(this.dQMaxMoveNumeric, maxMove, stepSize);
			Integer defaultValue = MaxMoveAutoFillHelper.getDefaultMaxMove(this._makeOrderAccount.get_Account().get_Id(), this._instrument.get_Id());
			if(defaultValue != null)
			{
				this.dQMaxMoveNumeric.setValue(defaultValue);
			}
		}
		//this.dQMaxMoveNumeric.setInputVerifier(new NumberTextFieldVerifier(0, tradePolicyDetail.get_DQMaxMove()));
		this.instrumentDescriptionStaticText.setText(this._instrument.get_Description());
		this.refreshPrice();
		this.accountEdit.setText(this._makeOrderAccount.get_Account().get_Code());
		BigDecimal defaultLot = this.getDefaultLot();
		this.totalLotTextField.setText(this.getFormatLot(defaultLot));

		boolean isDiaplayAccount = this._settingsManager.get_Accounts().size() > 1;
		this.accountStaticText.setVisible(isDiaplayAccount);
		this.accountEdit.setVisible(isDiaplayAccount);

		//init outstanding Order Grid
		boolean isBuy = Instrument.getSelectIsBuy(this._instrument, this._isDblClickAsk);
		//this._makeOrderAccount.set_BuySellType(BuySellType.Both);
		openCloseRelationSite = new OpenCloseRelationSite(this);
		this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, closeAllSell == null ? BuySellType.Both : (closeAllSell ? BuySellType.Sell : BuySellType.Buy), closeAllSell == null ? BuySellType.Both : (closeAllSell ? BuySellType.Sell : BuySellType.Buy), openCloseRelationSite);
		this.totalLotTextField.setText(this.getFormatLot(this.getDefaultLot()));
		this.updateCloseLotVisible();

		this.setBuySellEnabled(true);
		if (this.isHasDeal())
		{
			this.dealButton.setVisible(true);
			this.setDealStatus(false);
		}
		else
		{
			this.dealButton.setVisible(false);
		}

		this.outTimeSchedulerStart();

		this.setMakeOrderWindow();
		this.setNotifyIsAcceptWindow();

		this.totalLotTextField.requestFocus();
		this.totalLotTextField.select(0, this.totalLotTextField.getText().length());
		this.setTitle(this._instrument.get_Description());

		this.tabbedPane.setHideOneTab(true);

		ChangeListener changeListener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				outTimeSchedulerStop();
				outTimeSchedulerStart();
				int selectedIndex = tabbedPane.getSelectedIndex();
				if(selectedIndex != -1)
				{
					String title = tabbedPane.getTitleAt(selectedIndex);
					if (title.equalsIgnoreCase(Language.spotTradeOrderFormTitle))
					{
						resetData();
						initializeOutstanding();
						if(_closeAllSell != null)
						{
							closeAll();
						}
					}
					else
					{
						limitOrderForm.initializeOutstanding();
						if(limitOrderForm.get_CloseAllSell() != null)
						{
							limitOrderForm.closeAllButton_actionPerformed(null);
						}

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
		this.updateBuySellLot();
		BuySellType buySell = this._makeOrderAccount.GetBuySellForCloseAll();
		this.closeAllButton.setEnabled(buySell != BuySellType.Both);

		if(closeAllSell != null)
		{
			this.closeAll();
		}
	}

	private void initializeOutstanding()
	{
		boolean isBuy = Instrument.getSelectIsBuy(this._instrument, this._isDblClickAsk);
		String totalLot = "";
		String closeLot = "";
		if(this.openCloseRelationSite.getTotalLotEditor() != null)
		{
			totalLot = this.openCloseRelationSite.getTotalLotEditor().getText();
		}
		if(this.openCloseRelationSite.getCloseLotEditor() != null)
		{
			closeLot = this.openCloseRelationSite.getCloseLotEditor().getText();
		}
		//if(this.outstandingOrderTable.get_BindingSource() != null) this.outstandingOrderTable.get_BindingSource().removeAll();
		Boolean closeAllSell = this._closeAllSell;
		this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, closeAllSell == null ? BuySellType.Both : (closeAllSell ? BuySellType.Sell : BuySellType.Buy), closeAllSell == null ? BuySellType.Both : (closeAllSell ? BuySellType.Sell : BuySellType.Buy), openCloseRelationSite);
		this._lastBuyAccountLot = BigDecimal.ZERO;
		this._lastSellAccountLot = BigDecimal.ZERO;
		//this.totalLotTextField.setText(this.getFormatLot(this.getDefaultLot()));
		if(this.openCloseRelationSite.getTotalLotEditor() != null)
		{
			this.openCloseRelationSite.getTotalLotEditor().setText(totalLot);
		}
		if(this.openCloseRelationSite.getCloseLotEditor() != null)
		{
			this.openCloseRelationSite.getCloseLotEditor().setText(closeLot);
		}

		if (!this.isHasDeal() || this._dealTotalLot != null)
		{
			this.setBuySellEnabledSepcialProcess();
		}
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

	private void dQMaxMoveNumeric_focusLost()
	{
		/*Integer value = (Integer)this.dQMaxMoveNumeric.getValue();
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
			this._instrument.get_Id());
		if(value.intValue() > tradePolicyDetail.get_DQMaxMove())
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderOperateOrderOperateAccountGrid_ValidateEditAlert5 + " "
							+ Integer.toString(tradePolicyDetail.get_DQMaxMove()) + " " + Language.OrderOperateOrderOperateAccountGrid_ValidateEditAlert6);
			this.dQMaxMoveNumeric.setValue(0);
		}*/
	}

	@Override
	public void refreshPrice()
	{
		//?????????????
		//this.setPriceBidStaticText.setText( (!this._instrument.get_IsSinglePrice()) ? this._instrument.get_Bid() : this._instrument.get_Ask());
		//this.setPriceAskStaticText.setText(this._instrument.get_Ask());
		if(this._instrument.get_Id().equals(Instrument.DebugInstrumentId) || this._instrument.get_Id().equals(Instrument.DebugInstrumentId2))
		{
			String info = StringHelper.format("inQuoting = {0}, inSubmitting = {1}, LastQuotation().get_IsQuote={2}",
											  new Object[]{this._inQuoting, this._inSubmitting, this._instrument.get_LastQuotation().get_IsQuote()});
			TradingConsole.traceSource.trace(TraceType.Information, info);
		}

		if(!this._inQuoting && (!this._inSubmitting || this._instrument.get_LastQuotation().get_IsQuote()))
		{
			if(this._instrument.get_Id().equals(Instrument.DebugInstrumentId) || this._instrument.get_Id().equals(Instrument.DebugInstrumentId2))
			{
				TradingConsole.traceSource.trace(TraceType.Information, "refreshPrice 1");
			}

			this.bidButton.updatePrice();
			this.askButton.updatePrice();
			this._makeOrderAccount.set_BuySetPrice(this._instrument.get_LastQuotation().getBuy());
			this._makeOrderAccount.set_SellSetPrice(this._instrument.get_LastQuotation().getSell());
			this._makeOrderAccount.set_PriceInfo(this._instrument.get_LastQuotation().get_Timestamp(), this._instrument.get_LastQuotation().get_IsQuote());
			if(this._instrument.get_Id().equals(Instrument.DebugInstrumentId) || this._instrument.get_Id().equals(Instrument.DebugInstrumentId2))
			{
				TradingConsole.traceSource.trace(TraceType.Information, "refreshPrice 2");
			}
		}

		if(this.limitOrderForm != null) this.limitOrderForm.refreshPrice();
		if(this.isHasDeal())
		{
			BigDecimal answerLot = this._instrument.get_LastQuotation().get_AnswerLot();
			if (answerLot != null)
			{
				try
				{
					TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
						this._instrument.get_Id());
					answerLot = AppToolkit.fixLot(answerLot, false, tradePolicyDetail, this._makeOrderAccount.get_Account());

					BigDecimal dealLot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
					dealLot = AppToolkit.fixLot(dealLot, false, tradePolicyDetail, this._makeOrderAccount.get_Account());
					if (answerLot.compareTo(dealLot) < 0)
					{
						RelationOrder[] relations = new RelationOrder[this._makeOrderAccount.getOutstandingOrders().values().size()];
						relations = this._makeOrderAccount.getOutstandingOrders().values().toArray(relations);
						Arrays.sort(relations, RelationOrder.comparatorForAdjustingLot);

						BigDecimal leftLot = answerLot;
						for (RelationOrder relationOrder : relations)
						{
							if (relationOrder.get_IsSelected())
							{
								if (leftLot.compareTo(BigDecimal.ZERO) > 0)
								{
									BigDecimal closeLot = relationOrder.get_CloseLot();
									closeLot = closeLot.compareTo(leftLot) > 0 ? leftLot : closeLot;
									relationOrder.set_LiqLot(closeLot);

									leftLot = leftLot.subtract(closeLot);
								}
								else
								{
									relationOrder.set_IsSelected(false);
								}
							}
							this.outstandingOrderTable.get_BindingSource().update(relationOrder);
						}

						this._dealTotalLot = answerLot;
						this.totalLotTextField.setText(answerLot.toString());
						this.updateTotalColseLot(answerLot);
					}
				}
				finally
				{
					this._instrument.get_LastQuotation().clearAnswerLot();
				}
			}
		}
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
			exception.printStackTrace();
			super.dispose();
		}
	}

	private BigDecimal getDefaultLot()
	{
		Account account = this._makeOrderAccount.get_Account();
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(account.get_TradePolicyId(),
			this._instrument.get_Id());
		return AppToolkit.getDefaultLot(this._instrument, true, tradePolicyDetail, account);
	}

	private String getFormatLot(BigDecimal lot)
	{
		return AppToolkit.getFormatLot(lot, this._makeOrderAccount.get_Account(), this._makeOrderAccount.get_Instrument());
	}

	private BigDecimal _lastBuyAccountLot = BigDecimal.ZERO;
	private BigDecimal _lastSellAccountLot = BigDecimal.ZERO;
	public void updateAccount(BigDecimal accountLot, boolean openOrderIsBuy)
	{
		//BigDecimal otherSumLiqLots = this._makeOrderAccount.getSumLiqLots(openOrderIsBuy);
		//BigDecimal currentLot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());

		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
			this._instrument.get_Id());
		accountLot = AppToolkit.fixLot(accountLot, false, tradePolicyDetail, this._makeOrderAccount.get_Account());

		BigDecimal accountLot2 = accountLot;
		/*if(currentLot.compareTo(accountLot) >= 0)
		{
			accountLot2 = currentLot;
			BigDecimal lastAccountLot = openOrderIsBuy ? this._lastBuyAccountLot : this._lastSellAccountLot;
			if(accountLot.compareTo(lastAccountLot) < 0)
			{
				accountLot2 = accountLot2.subtract(lastAccountLot.subtract(accountLot));
			}
		}*/
		if(openOrderIsBuy)
		{
			this._lastBuyAccountLot = accountLot;
		}
		else
		{
			this._lastSellAccountLot = accountLot;
		}

		if (accountLot2.compareTo(BigDecimal.ZERO) <= 0)
		{
			accountLot2 = this.getDefaultLot();
		}
		if(this.closeLotTextField.isVisible())
		{
			this.totalLotTextField.setText(this.getFormatLot(accountLot2));
			if(this.isHasDeal() && this._dealTotalLot != null
				&& accountLot2.compareTo(this._dealTotalLot) > 0)
			{
				this.resetData();
				return;
			}
		}
		//boolean isBuy = Instrument.getSelectIsBuy(this._instrument, this._isDblClickAsk);
		//this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both, BuySellType.Both);

		if (!this.isHasDeal() || this._dealTotalLot != null)
		{
			this.setBuySellEnabledSepcialProcess();
		}

		this.updateBuySellLot();
	}

	//Input liqLot validation
	/*private void processOutstandingOrderTable(tradingConsole.ui.grid.ActionEvent e)
	{
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.lotNumeric.getText());
		super.processOutstandingOrderTable(e, OperateType.SingleSpotTrade, this._instrument, this._makeOrderAccount, lot, null, new BigDecimal("0"));
		//new
		if (!this.isHasDeal())
		{
			this.setBuySellEnabledSepcialProcess();
		}
	}*/

	static class OpenCloseRelationSite implements IOpenCloseRelationSite
	{
		private SpotTradeOrderForm _owner;

		public OpenCloseRelationSite(SpotTradeOrderForm owner)
		{
			this._owner = owner;
		}

		public OperateType getOperateType()
		{
			return OperateType.SingleSpotTrade;
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
			return AppToolkit.convertStringToBigDecimal(this._owner.totalLotTextField.getText());
		}

		public PVStaticText2 getLiqLotValueStaticText()
		{
			return null;
		}

		public Boolean isMakeLimitOrder()
		{
			return null;
		}

		public BigDecimal getTotalQuantity()
		{
			return BigDecimal.ZERO;
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
			this._owner.updateTotalColseLot(totalCloseLot);
		}

		public OrderType getOrderType()
		{
			return this._owner.getOrderType();
		}

		public void addPlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
		{
			this._owner.addPlaceOrderTypeChangedListener(placeOrderTypeChangedListener);
		}

		public void removePlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
		{
			this._owner.removePlaceOrderTypeChangedListener(placeOrderTypeChangedListener);
		}

		public JTextField getCloseLotEditor()
		{
			return this._owner.getCloseLotEditor();
		}

		public JTextField getTotalLotEditor()
		{
			return this._owner.getTotalLotEditor();
		}

		public int getLotStringColumn()
		{
			return this._owner.getLotStringColumn();
		}

		public boolean allowChangeCloseLot()
		{
			return false;
		}

		public Instrument get_Instrument()
		{
			return this._owner._instrument;
		}

		public DataGrid getAccountDataGrid()
		{
			return null;
		}

		public void rebind()
		{
			this._owner.rebind();
		}
	}

	public void lotNumeric_focusLost()
	{
		this.lotNumericValidation();
	}

	//private void lotNumeric_textValueChanged()
	//{
	//	this.lotNumericValidation();
	//}

	private void lotNumeric_focusGained()
	{
		this.totalLotTextField.select(0, this.totalLotTextField.getText().length());
	}

	private void dQMaxMoveNumeric_focusGained()
	{
		//this.dQMaxMoveNumeric.select(0, this.dQMaxMoveNumeric.getValue().toString().length());
	}

	//input Lot validation
	private boolean lotNumericValidation()
	{
		boolean isValid = true;
		boolean isOpen = this._makeOrderAccount.getSumLiqLots(true).compareTo(BigDecimal.ZERO) <= 0
			&& this._makeOrderAccount.getSumLiqLots(false).compareTo(BigDecimal.ZERO) <= 0;

		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if (lot.compareTo(BigDecimal.ZERO) <= 0)
		{
			BigDecimal defaultLot = this.getDefaultLot();
			String info = StringHelper.format(Language.LotIsNotValidAndWillChangeTo, new Object[]{lot, defaultLot});
			AlertDialogForm.showDialog(this, null, true,  info);
			lot = defaultLot;
			isValid =  false;
		}
		else
		{
			BigDecimal maxLot = this._instrument.get_MaxDQLot();
			if (maxLot.compareTo(BigDecimal.ZERO) != 0	&& lot.compareTo(maxLot) > 0)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderSingleDQPagetextLot_OnblurAlert0 + AppToolkit.getFormatLot(maxLot, this._makeOrderAccount.get_Account(), this._instrument) + ")!");
				BigDecimal defaultLot = this.getDefaultLot();
				lot = defaultLot;
				isValid =  false;
			}
		}
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
			this._instrument.get_Id());
		BigDecimal lot2 = AppToolkit.fixLot(lot, isOpen, tradePolicyDetail, this._makeOrderAccount.get_Account());
		BigDecimal defaultLot = AppToolkit.getDefaultLot(this._instrument, isOpen, tradePolicyDetail, this._makeOrderAccount.get_Account());
		String formattedLot = this.getFormatLot(lot2);
		if(lot.compareTo(lot2) != 0)
		{
			if(lot.compareTo(defaultLot) != 0)
			{
				String info = StringHelper.format(Language.LotIsNotValidAndWillChangeTo, new Object[]
												  {lot, formattedLot});
				AlertDialogForm.showDialog(this, null, true, info);
				isValid = false;
			}
		}
		this.totalLotTextField.setText(formattedLot);

		//input lot < sumLiqLots, clear liqLots....
		BuySellType buySellOfCurrentRelationOrder = this.getBuySellOfCurrentRelationOrder();
		boolean isBuy = true;
		BigDecimal closeLot = this._makeOrderAccount.getSumLiqLots(isBuy);
		if ((buySellOfCurrentRelationOrder == BuySellType.Both || buySellOfCurrentRelationOrder == BuySellType.Sell)
			&& (closeLot.compareTo(BigDecimal.ZERO) > 0 && lot.compareTo(closeLot) < 0))
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderSingleDQPagetextLot_OnblurAlert3);
			this._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, this.getOrderType().isSpot(), null);
			isValid =  false;
		}

		isBuy = false;
		closeLot = this._makeOrderAccount.getSumLiqLots(isBuy);
		if ((buySellOfCurrentRelationOrder == BuySellType.Both || buySellOfCurrentRelationOrder == BuySellType.Buy)
			&& (closeLot.compareTo(BigDecimal.ZERO) > 0 && lot.compareTo(closeLot) < 0))
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderSingleDQPagetextLot_OnblurAlert3);
			this._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, this.getOrderType().isSpot(), null);
			isValid =  false;
		}
		this.updateBuySellLot();
		return isValid;
	}

	private String _buyTotalLot = null;
	private String _sellTotalLot = null;
	private void updateBuySellLot()
	{
		String currentLot = this.totalLotTextField.getText();
		//BuySellType buySellOfCurrentRelationOrder = this.getBuySellOfCurrentRelationOrder();
		BuySellType buySellOfCurrentRelationOrder = BuySellType.Both;
		if(buySellOfCurrentRelationOrder == BuySellType.Buy)
		{
			this._sellTotalLot = currentLot;
			/*this.bidButton.updateLot(this._sellTotalLot, false);
			this.askButton.updateLot(this._sellTotalLot, false);*/
		}
		else if(buySellOfCurrentRelationOrder == BuySellType.Sell)
		{
			this._buyTotalLot = currentLot;
			/*this.bidButton.updateLot(this._buyTotalLot, true);
			this.askButton.updateLot(this._buyTotalLot, true);*/
		}
		else
		{
			this._sellTotalLot = currentLot;
			/*this.bidButton.updateLot(this._sellTotalLot, false);
			this.askButton.updateLot(this._sellTotalLot, false);*/

			this._buyTotalLot = currentLot;
			/*this.bidButton.updateLot(this._buyTotalLot, true);
			this.askButton.updateLot(this._buyTotalLot, true);*/
		}
	}

	private BuySellType getBuySellOfCurrentRelationOrder()
	{
		int selectedRow = this.outstandingOrderTable.getSelectedRow();
		if(selectedRow >= 0)
		{
			RelationOrder relationOrder = (RelationOrder)this.outstandingOrderTable.getObject(selectedRow);
			if(relationOrder.get_IsSelected())
			{
				return relationOrder.get_IsBuy() ? BuySellType.Buy : BuySellType.Sell;
			}
		}
		return BuySellType.Both;
	}

	private void setBuySellVisible(boolean isVisible)
	{
		this.setBuySellEnabled(isVisible);
	}

	private void setBuySellEnabled(boolean isVisible)
	{
		isVisible = (isVisible && this.isValidPrice());

		this.askButton.setEnabled(isVisible);
		this.bidButton.setEnabled(isVisible);

		/*this.buyButton.setVisible(isVisible);
		this.sellButton.setVisible(isVisible);
		this.buyButton.setEnabled(isVisible);
		this.sellButton.setEnabled(isVisible);*/

		this.dealButton.setEnabled(!isVisible);
		this.dealButton.setVisible(!isVisible);
	}

	private void setBuySellEnabledSepcialProcess()
	{
		if (!this.isValidPrice())
		{
			this.bidButton.setEnabled(false);
			this.askButton.setEnabled(false);
			/*this.buyButton.setEnabled(false);
			this.sellButton.setEnabled(false);
			this.buyButton.setVisible(false);
			this.sellButton.setVisible(false);*/

			this.dealButton.setEnabled(true);
			this.dealButton.setVisible(true);
		}
		else
		{
			BigDecimal sumLiqLotsForBuy = this._makeOrderAccount.getSumLiqLots(true);
			BigDecimal sumLiqLotsForSell = this._makeOrderAccount.getSumLiqLots(false);

			//this.buyButton.setVisible(true);
			boolean enableBuyButton = sumLiqLotsForBuy.compareTo(BigDecimal.ZERO) > 0 || (sumLiqLotsForBuy.compareTo(BigDecimal.ZERO) == 0 && sumLiqLotsForSell.compareTo(BigDecimal.ZERO) == 0);
			if(this._instrument.get_IsNormal())
			{
				this.askButton.setEnabled(enableBuyButton);
			}
			else
			{
				this.bidButton.setEnabled(enableBuyButton);
			}
			//this.buyButton.setEnabled(enableBuyButton);

			//this.sellButton.setVisible(true);
			boolean enableSellButton = sumLiqLotsForSell.compareTo(BigDecimal.ZERO) > 0 || (sumLiqLotsForBuy.compareTo(BigDecimal.ZERO) == 0 && sumLiqLotsForSell.compareTo(BigDecimal.ZERO) == 0);
			if(this._instrument.get_IsNormal())
			{
				this.bidButton.setEnabled(enableSellButton);
			}
			else
			{
				this.askButton.setEnabled(enableSellButton);
			}
			//this.sellButton.setEnabled(enableSellButton);

			this.dealButton.setEnabled(false);
			this.dealButton.setVisible(false);
		}
	}

	private boolean isValidPrice()
	{
		return this.isValidPrice(null);
	}

	private boolean isValidPrice(Boolean isBuy)
	{
		boolean isValidPrice = false;
		/*if (this.setPriceAskStaticText.getText().equals("")
			|| this.setPriceBidStaticText.getText().equals("")
			|| this.setPriceAskStaticText.getText().equals("-")
			|| this.setPriceBidStaticText.getText().equals("-"))*/
		if(this.askButton.isQuoting() || this.bidButton.isQuoting())
		{
			if (!this.isHasDeal())
			{
				BigDecimal lot = BigDecimal.ZERO;
				if(isBuy == null)
				{
					lot = AppToolkit.convertStringToBigDecimal(this._buyTotalLot);
					BigDecimal lot2 = AppToolkit.convertStringToBigDecimal(this._sellTotalLot);
					if(lot2.compareTo(lot) > 0)
					{
						lot = lot2;
					}
				}
				else
				{
					lot = AppToolkit.convertStringToBigDecimal((isBuy.booleanValue() ? this._buyTotalLot : this._sellTotalLot));
				}

				if (MakeOrder.isQuote(this._instrument, lot))
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
		return (this._settingsManager.get_Customer().get_SingleAccountOrderType() == 1);
	}

	//For deal
	public void setDealStatus(boolean isDeal)
	{
		this.dealButton.setVisible( (this.isHasDeal()) ? true : false);
		this.setBuySellVisible( (this.isHasDeal()) ? isDeal : true);
		this.dealButton.setEnabled(!isDeal);

		this.outstandingOrderTable.setEditable(!isDeal);
		this.totalLotTextField.setEditable(!isDeal);
		this.setBuySellVisible(isDeal);
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

	@Override
	public void resetData(boolean inDisposing)
	{
		//this._makeOrderAccount.set_BuySellType(BuySellType.Both);
		super.resetData(inDisposing);
		this._dealTotalLot = null;
		if (!this.isHasDeal())
		{
			this.setBuySellEnabledSepcialProcess();
		}
		else
		{
			MakeOrder.setLastQuotationIsUsing(this._instrument, false);
			this.setDealStatus(false);
		}
		if(!inDisposing) this.outTimeSchedulerStart();
	}

	public void quoteArrived()
	{
		//if(this.askButton.isQuoting()) return;

		super.quoteArrived();
		//this.setBuySellEnabled(true);
		this.setBuySellEnabledSepcialProcess();
		this.priceValidTimeSchedulerStart(OperateType.SingleSpotTrade);
	}

	@Override
	protected void priceValidTimeSchedulerStart(OperateType operateType)
	{
		this._dealTotalLot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		this.outstandingOrderTable.setEditable(true);
		this.totalLotTextField.setEditable(true);
		this.closeLotTextField.setEditable(true);

		super.priceValidTimeSchedulerStart(operateType);
	}

	private void closeAll()
	{
		BuySellType buySell = this._makeOrderAccount.GetBuySellForCloseAll();
		this._makeOrderAccount.set_IsBuyForCurrent(buySell == BuySellType.Buy ? true : false);
		BigDecimal lot = this._makeOrderAccount.closeAll();
		this.updateAccount(lot, buySell==BuySellType.Buy ? true : false);
	}

	private void reset()
	{
		this._inSubmitting = false;
		this._inQuoting = false;
		this.outTimeSchedulerStop();

		this._makeOrderAccount.reset(this.getOrderType().isSpot(), null);
		this.totalLotTextField.setText(this.getFormatLot(this.getDefaultLot()));
		this._buyTotalLot = this.totalLotTextField.getText();
		this._sellTotalLot = this.totalLotTextField.getText();
		this.closeLotTextField.setText("");

		if (!this.isHasDeal())
		{
			this.setBuySellEnabledSepcialProcess();
		}
		else
		{
			this._dealTotalLot = null;
			this.resetData();
			this.setDealStatus(false);
		}
		this.outTimeSchedulerStart();
	}

	private void deal()
	{
		this.fillVerificationInfoScheculerStop();
		this._dealTotalLot = null;

		BigDecimal liqLots = this._makeOrderAccount.getSumLiqLots(true);
		if(liqLots.compareTo(BigDecimal.ZERO) == 0)
		{
			liqLots = this._makeOrderAccount.getSumLiqLots(false);
		}
		if (liqLots.compareTo(BigDecimal.ZERO) > 0)
		{
			BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
			if (lot.compareTo(liqLots) != 0)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert7);

				TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
						this._instrument.get_Id());
				liqLots = AppToolkit.fixLot(liqLots, false, tradePolicyDetail, this._makeOrderAccount.get_Account());
				this.totalLotTextField.setText(AppToolkit.getFormatLot(liqLots, this._makeOrderAccount.get_Account(), this._instrument));
				this.updateBuySellLot();
				return;
			}
		}

		this.setDealStatus(true);

		this.outTimeSchedulerStop();

		this.setBuySellEnabledSepcialProcess();

		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
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
			this.dealButton.setVisible(false);
		}
		else
		{
			if (this._instrument.get_PriceValidTime() <= 0)
			{
				//this.submit();
			}
			else
			{
				MakeOrder.setLastQuotationIsUsing(this._instrument, true);
				if (Parameter.nonQuoteVerificationUiDelay <= 0)
				{
					this.priceValidTimeSchedulerStart(OperateType.SingleSpotTrade);
				}
				else
				{
					this.bidButton.quotingPrice();
					this.askButton.quotingPrice();
					this.setBuySellEnabled(false);
					this.dealButton.setVisible(false);
					this.fillVerificationInfoScheculerStart();
				}
			}
		}
	}

	private boolean _inQuoting = false;
	@Override
	protected void setIsQuoting(boolean isQuoting)
	{
		this._inQuoting = isQuoting;
		super.setIsQuoting(isQuoting);
	}

	@Override
	public void dealDelayProcess()
	{
		//this.refreshPrice();
		this.setBuySellEnabledSepcialProcess();
		//this.setBuySellEnabled(true);
		this.priceValidTimeSchedulerStart(OperateType.SingleSpotTrade);
	}

	private boolean isValidOrder(boolean isBuy, boolean isPrompt)
	{
		if(!MakeOrder.isAllowOrderType(this._instrument, this.getOrderType()))
		{
			if(isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
			}
			return false;
		}

		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if (lot.compareTo(BigDecimal.ZERO) <= 0)
		{
			AlertDialogForm.showDialog(this, null, true, Language.OperateOrderMethod1PageSubmitAlert0);
			this.resetData();
			return false;
		}
		BigDecimal liqLots = this._makeOrderAccount.getSumLiqLots(isBuy);
		if (liqLots.compareTo(BigDecimal.ZERO) > 0)
		{
			if (lot.compareTo(liqLots) != 0)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert7);
					TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
						this._instrument.get_Id());
					liqLots = AppToolkit.fixLot(liqLots, false, tradePolicyDetail, this._makeOrderAccount.get_Account());
					this.totalLotTextField.setText(AppToolkit.getFormatLot(liqLots, this._makeOrderAccount.get_Account(), this._instrument));
					this.updateBuySellLot();
				}
				return false;
			}
		}
		else if(!this._makeOrderAccount.get_Account().get_AllowAddNewPosition()
			|| !this._instrument.getAllowAddNewPosition(isBuy))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, Language.NewPositionIsNotAllowed);
			}
			return false;
		}


		//for Instrument code with "#"// & isOpen=true
		//if (lot.doubleValue() > liqLots)
		{
			//BigDecimal openLot = new BigDecimal( ( (Double) (lot.doubleValue() - liqLots)).doubleValue());
			boolean isHasMakeNewOrder = lot.compareTo(liqLots) > 0;
			boolean isAcceptLot = this._makeOrderAccount.isAcceptLot(isBuy,lot,isHasMakeNewOrder);// openLot);
			if (!isAcceptLot)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this, null, true, Language.NewOrderAcceptedHedging);
				}
				return false;
			}
		}
		Object[] result = this._makeSpotTradeOrder.isAcceptTime();
		if (! (Boolean)result[0])
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, result[1].toString());
			}
			return false;
		}
		return true;
	}

	private boolean _inSubmitting = false;
	private void submit(boolean isBuy)
	{
		this._inSubmitting = true;

		this.outTimeSchedulerStop();
		this.priceValidTimeSchedulerStop();

		BigDecimal newLot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if(isHasDeal() && _dealTotalLot != null  && newLot.compareTo(_dealTotalLot) > 0)
		{
			priceValidTimeSchedulerStop();
			resetData();
			this._inSubmitting = false;
			this.outTimeSchedulerStart();
			return;
		}

		//VisibleButton(0);
		boolean isQuotePirceForDealing = true;
		if(this.isHasDeal())
		{
			BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
			if (MakeOrder.isQuote(this._instrument, lot))
			{
				isQuotePirceForDealing = this._makeOrderAccount.get_PriceIsQuote();
			}
		}

		if (!isQuotePirceForDealing || !this.isValidPrice(isBuy))
		{
			AlertDialogForm.showDialog(this, null, true, Language.OperateOrderMethod1PageSubmitAlert1);
			priceValidTimeSchedulerStop();
			this.resetData();
			this._inSubmitting = false;
			this.outTimeSchedulerStart();
			return;
		}

		if (this.isValidOrder(isBuy, true))
		{
			MaxMoveAutoFillHelper.set_SaveMaxMoveAsDefaultForSingleAccount(this._instrument.get_Id(), this.remberMaxMoveCheckBox.isSelected());
			if(this.remberMaxMoveCheckBox.isSelected())
			{
				MaxMoveAutoFillHelper.setDefaultMaxMove(this._makeOrderAccount.get_Account().get_Id(), this._instrument.get_Id(),
					(Integer)this.dQMaxMoveNumeric.getValue());
			}
			//set value to MakeOrderAccount
			this.updateBuySellLot();

			BigDecimal buyLot = AppToolkit.convertStringToBigDecimal(this._buyTotalLot);
			BigDecimal sllLot = AppToolkit.convertStringToBigDecimal(this._sellTotalLot);
			//PalceLotNnemonic.set(this._instrument.get_Id(), buyLot.compareTo(BigDecimal.ZERO) == 0 ? sllLot : buyLot);

			/*Price askPrice
				= Price.parse(this.askButton.get_price(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
			Price bidPrice
				= Price.parse(this.bidButton.get_price(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());

			//SpotOrderPriceContainer spotOrderPriceContainer = new SpotOrderPriceContainer();
			if(this._instrument.get_IsNormal())
			{
				//spotOrderPriceContainer.BuyPrice = askPrice;
				//spotOrderPriceContainer.SellPrice = bidPrice;
				this._makeOrderAccount.set_BuySetPrice(askPrice);
				this._makeOrderAccount.set_SellSetPrice(bidPrice);
			}
			else
			{
				//spotOrderPriceContainer.BuyPrice = bidPrice;
				//spotOrderPriceContainer.SellPrice = askPrice;
				this._makeOrderAccount.set_BuySetPrice(bidPrice);
				this._makeOrderAccount.set_SellSetPrice(askPrice);
			}*/

			//spotOrderPriceContainer.LogInfo = logInfo;
			PlaceOrderInfo placeOrderInfo = new PlaceOrderInfo(this._instrument.get_LastQuotation(), this.bidButton.get_price(), this.askButton.get_price());
			this._makeOrderAccount.set_PlaceOrderInfo(placeOrderInfo);

			this._makeOrderAccount.set_BuyLot(buyLot);
			this._makeOrderAccount.set_SellLot(sllLot);
			this._makeOrderAccount.set_BuySellType( (isBuy) ? BuySellType.Buy : BuySellType.Sell);
			this._makeOrderAccount.set_IsBuyForCurrent(isBuy);
			this._makeOrderAccount.set_DQMaxMove((Integer)this.dQMaxMoveNumeric.getValue());
			VerificationOrderForm verificationOrderForm = new VerificationOrderForm(this, "Verification Order", true, this._tradingConsole,
				this._settingsManager, this._instrument,
				this._makeSpotTradeOrder.get_MakeOrderAccounts(), OrderType.SpotTrade, OperateType.SingleSpotTrade, this.isHasDeal(), null, null);
			//verificationOrderForm.show();
			this._inSubmitting = false;

			this.quoteDelayTimeSchedulerStop();
			this.priceValidTimeSchedulerStop();
			this.fillVerificationInfoScheculerStop();

			this.refreshPrice();

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
			this._inSubmitting = false;
			this.resetData();
			priceValidTimeSchedulerStop();
			this.outTimeSchedulerStart();
			return;
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

			int x2 = this.lotStaticText.getLocation().x - 1;
			int y2 = this.lotStaticText.getLocation().y - 1;
			int width2 = 164; //this.dealButton.getSize().width;
			int height2 = this.lotNumeric.getSize().height + 2;
			this.getGraphics().drawRect(x2, y2, width2, height2);

			int x3 = this.dQMaxMoveStaticText.getLocation().x - 1;
			int y3 = this.dQMaxMoveStaticText.getLocation().y - 1;
			int width3 = 164;
			int height3 = this.dQMaxMoveNumeric.getSize().height + 2;
			this.getGraphics().drawRect(x3, y3, width3, height3);

		}
		catch (Throwable exception)
		{}
	}*/

//SourceCode End////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		PropertyChangeListener propertyChangeListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent evt)
			{
				if(evt.getPropertyName().compareToIgnoreCase("value") == 0)
				{
					BigDecimal newLot = AppToolkit.convertStringToBigDecimal(evt.getNewValue().toString());
					if(isHasDeal() && _dealTotalLot != null  && newLot.compareTo(_dealTotalLot) > 0)
					{
						priceValidTimeSchedulerStop();
						resetData();
					}
				}
			}
		};
		this.totalLotTextField.addPropertyChangeListener(propertyChangeListener);
		this.addWindowListener(new SpotTradeOrderForm_this_windowAdapter(this));

		this.setSize(490, 460);
		this.setResizable(true);
		this.setTitle(Language.spotTradeOrderFormTitle);

		this.getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

		tabbedPane.setBackground(FormBackColor.spotTradeOrderForm);

		spotTradePanel.setLayout(gridBagLayout1);

		spotTradePanel.setBackground(FormBackColor.spotTradeOrderForm);
		this.closeLotTextField.setEnabled(false);
		this.closeLotTextField.setBackground(Color.LIGHT_GRAY);

		//timerTime.setVisible(false);
		Font font = new Font("SansSerif", Font.BOLD, 13);
		accountStaticText.setFont(font);
		accountStaticText.setText("Account");
		accountStaticText.setVisible(false);
		instrumentDescriptionStaticText.setText("GBP");
		totalLotStaticText.setText("Lot");
		totalLotStaticText.setFont(font);
		closeLotStaticText.setFont(font);
		font = new Font("SansSerif", Font.BOLD, 14);
		instrumentDescriptionStaticText.setFont(font);
		instrumentDescriptionStaticText.setVisible(false);
		dealButton.setText("Deal");
		dealButton.addActionListener(new SpotTradeOrderForm_dealButton_actionAdapter(this));
		bidButton.addActionListener(new SpotTradeOrderForm_sellButton_actionAdapter(this));
		askButton.addActionListener(new SpotTradeOrderForm_buyButton_actionAdapter(this));
		/*buyButton.setText("Buy");
		buyButton.setForeground(Color.blue);
		buyButton.addActionListener(new SpotTradeOrderForm_buyButton_actionAdapter(this));
		sellButton.setText("Sell");
		sellButton.setForeground(Color.red);
		sellButton.addActionListener(new SpotTradeOrderForm_sellButton_actionAdapter(this));*/
		resetButton.setText("Reset");
		resetButton.addActionListener(new SpotTradeOrderForm_resetButton_actionAdapter(this));
		exitButton.setText("Exit");
		exitButton.addActionListener(new SpotTradeOrderForm_exitButton_actionAdapter(this));
		closeAllButton.addActionListener(new SpotTradeOrderForm_closeAllButton_actionAdapter(this));
		//accountEdit.setEnabled(false);
		accountEdit.setVisible(false);
		/*setPriceBidStaticText.setText("1.2250");
		setPriceBidStaticText.setForeground(Color.red);
		setPriceBidStaticText.setAlignment(2);
		setPriceBidStaticText.setFont(font);
		setPriceAskStaticText.setForeground(Color.blue);
		setPriceAskStaticText.setAlignment(0);
		setPriceAskStaticText.setText("1.2550");
		setPriceAskStaticText.setFont(font);
		separatorStaticText.setText("/");
		separatorStaticText.setFont(font);*/
		//outstandingOrderTable.addActionListener(new SpotTradeOrderForm_outstandingOrderTable_actionAdapter(this));
		//lotNumeric.addTextListener(new SpotTradeOrderForm_lotNumeric_textAdapter(this));
		//lotNumeric.setBorderStyle(0);
		totalLotTextField.addFocusListener(new SpotTradeOrderForm_lotNumeric_focusAdapter(this));
		totalLotTextField.setFont(font);
		closeLotTextField.setFont(font);
		timerTime = new Timer(500, new SpotTradeOrderForm_timer_actionAdapter(this));
		//outstandingOrderTable.addKeyListener(new SpotTradeOrderForm_outstandingOrderTable_keyAdapter(this));
		dQMaxMoveStaticText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 13));
		dQMaxMoveStaticText.setText("Slide Pips");
		//dQMaxMoveNumeric.set1000Multiplier("");
		//dQMaxMoveNumeric.setMaxValue(9.999999999E9);
		//dQMaxMoveNumeric.setMinValue(0.0);
		//dQMaxMoveNumeric.setMaxDecimals(0);
		dQMaxMoveNumeric.setValue(0);
		dQMaxMoveNumeric.addFocusListener(new SpotTradeOrderForm_dQMaxMoveNumeric_focusAdapter(this));
		//dQMaxMoveNumeric.setBorderStyle(0);
		dQMaxMoveNumeric.setFont(font);
		remberMaxMoveCheckBox.setFont(font);
		spotTradePanel.add(accountEdit, new GridBagConstraints2(3, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 0, 2, 1), 0, 0));
		spotTradePanel.add(accountStaticText, new GridBagConstraints2(0, 3, 3, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 0, 5), 0, 0));
		spotTradePanel.add(dQMaxMoveStaticText, new GridBagConstraints(0, 7, 3, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 0, 5), 0, 0));
		remberMaxMoveCheckBox.setBackground(Color.WHITE);
		spotTradePanel.add(totalLotStaticText, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 0, 5), 0, 0));
		spotTradePanel.add(closeLotStaticText, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 0, 5), 0, 0));

		spotTradePanel.add(bidButton, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 80, 55));

		spotTradePanel.add(askButton, new GridBagConstraints(2, 0, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 2, 0, 0), 80, 55));

		/*spotTradePanel.add(setPriceAskStaticText, new GridBagConstraints(7, 0, 1, 3, 1.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(15, 0, 5, 10), 0, 0));
		spotTradePanel.add(setPriceBidStaticText, new GridBagConstraints(4, 0, 2, 3, 1.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(15, 5, 5, 0), 0, 0));
		spotTradePanel.add(separatorStaticText, new GridBagConstraints(6, 0, 1, 3, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(15, 0, 5, 2), 0, 0));		*/

	    JScrollPane scrollPane = new JScrollPane(outstandingOrderTable);
		/*ISelectedRowChangedListener selectedRowChangedListener = new ISelectedRowChangedListener()
		{
			private Boolean _isBuyOfLastSelectedRow = null;
			public void selectedRowChanged(DataGrid source)
			{
				int selectedRow = source.getSelectedRow();
				if(selectedRow >= 0)
				{
					RelationOrder relationOrder = (RelationOrder)source.getObject(selectedRow);
					//closeLotTextField.setText(relationOrder.get_LiqLotString());
					if (this._isBuyOfLastSelectedRow == null
						|| _isBuyOfLastSelectedRow.booleanValue() != relationOrder.get_IsBuy())
					{
						if (relationOrder.get_IsBuy())
						{
							if (!StringHelper.isNullOrEmpty(_sellTotalLot))
							{
								totalLotTextField.setText(_sellTotalLot);
							}
						}
						else
						{
							if (!StringHelper.isNullOrEmpty(_buyTotalLot))
							{
								totalLotTextField.setText(_buyTotalLot);
							}
						}
					}
				}
			}
		};
		outstandingOrderTable.addSelectedRowChangedListener(selectedRowChangedListener);*/
		outstandingOrderTable.enableRowStripe();
		spotTradePanel.add(instrumentDescriptionStaticText, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(15, 5, 5, 10), 0, 0));
		spotTradePanel.add(scrollPane, new GridBagConstraints(4, 0, 4, 9, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 10, 5, 10), 0, 0));
		spotTradePanel.add(exitButton, new GridBagConstraints(6, 10, 2, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 1, 10, 10), 10, 0));
		/*spotTradePanel.add(sellButton, new GridBagConstraints(0, 9, 3, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 10, 1), 10, 0));
		spotTradePanel.add(buyButton, new GridBagConstraints(3, 9, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 1, 10, 2), 10, 0));*/
		spotTradePanel.add(resetButton, new GridBagConstraints(4, 10, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 10, 0), 0, 0));
		spotTradePanel.add(closeAllButton, new GridBagConstraints(5, 10, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 1, 10, 0), 0, 0));
		spotTradePanel.add(dealButton, new GridBagConstraints(0, 10, 4, 1, 0.0, 0.0
			, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 10, 2), 10, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		panel.add(dQMaxMoveNumeric, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 20, 0));
		panel.add(remberMaxMoveCheckBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 2, 0), 0, 0));

		spotTradePanel.add(panel, new GridBagConstraints(3, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 2, 1), 0, 0));

		spotTradePanel.add(totalLotTextField, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 2, 1), 60, 0));
		spotTradePanel.add(closeLotTextField, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(6, 0, 2, 1), 60, 0));
	}

	@Override
	public void startCloseTimer()
	{
		this.timerTime.start();
	}

	public void timer_actionPerformed(ActionEvent e)
	{
		this.timerTime.stop();
		this.dispose();
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.timerTime.stop();
		this.dispose();
	}

	PVStaticText2 instrumentDescriptionStaticText = new PVStaticText2();
	PVStaticText2 accountStaticText = new PVStaticText2();
	PVStaticText2 totalLotStaticText = new PVStaticText2();
	PVStaticText2 closeLotStaticText = new PVStaticText2();
	PVButton2 dealButton = new PVButton2();
	//PVButton2 buyButton = new PVButton2();
	//PVButton2 sellButton = new PVButton2();
	PVButton2 resetButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();
	PVButton2 closeAllButton = new PVButton2();
	DataGrid outstandingOrderTable = new DataGrid("OutstandingOrderTable");
	JFormattedTextField totalLotTextField = new JFormattedTextField(new DecimalFormat());
	JFormattedTextField closeLotTextField = new JFormattedTextField(new DecimalFormat());
	PVStaticText2 accountEdit = new PVStaticText2();
	//PVStaticText2 setPriceBidStaticText = new PVStaticText2();
	//PVStaticText2 setPriceAskStaticText = new PVStaticText2();
	BidAskButton bidButton = new BidAskButton(true);
	BidAskButton askButton = new BidAskButton(false);
	//PVStaticText2 separatorStaticText = new PVStaticText2();
	JideTabbedPane tabbedPane = new JideTabbedPane();
	JPanel spotTradePanel = new JPanel();
	private Timer timerTime;

	java.awt.GridBagLayout gridBagLayout1 = new GridBagLayout();
	private PVStaticText2 dQMaxMoveStaticText = new PVStaticText2();
	private JSpinner dQMaxMoveNumeric = new JSpinner();
	private JCheckBox remberMaxMoveCheckBox = new JCheckBox();
	private LimitOrderForm limitOrderForm;
	private MatchingOrderForm matchingOrderForm;
	private IOpenCloseRelationSite openCloseRelationSite;

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	public void closeAll_actionPerformed(ActionEvent e)
	{
		this.closeAll();
	}

	/*public void outstandingOrderTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processOutstandingOrderTable(e);
	}*/

	//public void lotNumeric_textValueChanged(TextEvent e)
	//{
	//	this.lotNumeric_textValueChanged();
	//}

	public void resetButton_actionPerformed(ActionEvent e)
	{
		this.reset();
	}

	public void dealButton_actionPerformed(ActionEvent e)
	{
		this.deal();
	}

	public void buyButton_actionPerformed(ActionEvent e)
	{
		if(this.lotNumericValidation())
		{
			this.submit(this._instrument.get_IsNormal() ? true : false);
		}
	}

	public void sellButton_actionPerformed(ActionEvent e)
	{
		if(this.lotNumericValidation())
		{
			this.submit(this._instrument.get_IsNormal() ? false : true);
		}
	}

	public void lotNumeric_focusLost(FocusEvent e)
	{
		if(e.getOppositeComponent() != this.resetButton
		   && e.getOppositeComponent() != this.bidButton
		   && e.getOppositeComponent() != this.askButton)
		{
			this.lotNumeric_focusLost();
		}
	}

	public void lotNumeric_focusGained(FocusEvent e)
	{
		this.lotNumeric_focusGained();
	}
	public void dQMaxMoveNumeric_focusLost(FocusEvent e)
	{
		this.dQMaxMoveNumeric_focusLost();
	}
	public void dQMaxMoveNumeric_focusGained(FocusEvent e)
	{
		this.dQMaxMoveNumeric_focusGained();
	}

	class SpotTradeOrderForm_this_windowAdapter extends WindowAdapter
	{
		private SpotTradeOrderForm adaptee;
		SpotTradeOrderForm_this_windowAdapter(SpotTradeOrderForm adaptee)
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

	private void addPlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
	{
	}

	private JTextField getCloseLotEditor()
	{
		if(this.outstandingOrderTable.getRowCount() > 0)
		{
			return this.closeLotTextField;
		}
		else
		{
			return null;
		}
	}

	private void updateCloseLotVisible()
	{
		this.closeLotTextField.setVisible(this.outstandingOrderTable.getRowCount() > 0);
		this.closeLotStaticText.setVisible(this.outstandingOrderTable.getRowCount() > 0);
	}

	private JTextField getTotalLotEditor()
	{
		return this.totalLotTextField;
	}

	private void removePlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
	{
	}

	private int getLotStringColumn()
	{
		return 0;
	}

	private void updateTotalColseLot(BigDecimal totalCloseLot)
	{
		if(totalCloseLot.compareTo(BigDecimal.ZERO) > 0)
		{
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());
			totalCloseLot = AppToolkit.fixLot(totalCloseLot, false, tradePolicyDetail, this._makeOrderAccount.get_Account());
		}

		/*BigDecimal currentLot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());

		BigDecimal accountLot2 = totalCloseLot;
		if(currentLot.compareTo(totalCloseLot) >= 0)
		{
			accountLot2 = currentLot;
		}

		if (accountLot2.compareTo(BigDecimal.ZERO) <= 0)
		{
			accountLot2 = this.getDefaultLot();
		}

		if(currentLot.compareTo(accountLot2) != 0)
		{
			this.totalLotTextField.setText(this.getFormatLot(accountLot2));
			if(this.isHasDeal() && this._dealTotalLot != null
				&& accountLot2.compareTo(this._dealTotalLot) > 0)
			{
				this.resetData();
				return;
			}
		}*/

		if (totalCloseLot.compareTo(BigDecimal.ZERO) <= 0)
		{
			totalCloseLot = this.getDefaultLot();
		}
		this.totalLotTextField.setText(this.getFormatLot(totalCloseLot));

		if (!this.isHasDeal())
		{
			this.setBuySellEnabledSepcialProcess();
		}

		this.updateBuySellLot();
	}

	private void rebind()
	{
		boolean isBuy = Instrument.getSelectIsBuy(this._instrument, this._isDblClickAsk);
		this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both, BuySellType.Both, openCloseRelationSite);
	}
}

class SpotTradeOrderForm_dQMaxMoveNumeric_focusAdapter extends FocusAdapter
{
	private SpotTradeOrderForm adaptee;
	SpotTradeOrderForm_dQMaxMoveNumeric_focusAdapter(SpotTradeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.dQMaxMoveNumeric_focusLost(e);
	}

	public void focusGained(FocusEvent e)
	{
		adaptee.dQMaxMoveNumeric_focusGained(e);
	}
}

class SpotTradeOrderForm_timer_actionAdapter implements ActionListener
{
	private SpotTradeOrderForm adaptee;
	SpotTradeOrderForm_timer_actionAdapter(SpotTradeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.timer_actionPerformed(e);
	}
}

class SpotTradeOrderForm_sellButton_actionAdapter implements ActionListener
{
	private SpotTradeOrderForm adaptee;
	SpotTradeOrderForm_sellButton_actionAdapter(SpotTradeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.sellButton_actionPerformed(e);
	}
}

class SpotTradeOrderForm_buyButton_actionAdapter implements ActionListener
{
	private SpotTradeOrderForm adaptee;
	SpotTradeOrderForm_buyButton_actionAdapter(SpotTradeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.buyButton_actionPerformed(e);
	}
}

class SpotTradeOrderForm_dealButton_actionAdapter implements ActionListener
{
	private SpotTradeOrderForm adaptee;
	SpotTradeOrderForm_dealButton_actionAdapter(SpotTradeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.dealButton_actionPerformed(e);
	}
}

class SpotTradeOrderForm_resetButton_actionAdapter implements ActionListener
{
	private SpotTradeOrderForm adaptee;
	SpotTradeOrderForm_resetButton_actionAdapter(SpotTradeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.resetButton_actionPerformed(e);
	}
}

/*
class SpotTradeOrderForm_lotNumeric_textAdapter implements TextListener //PVTextAdaptor
{
	private SpotTradeOrderForm adaptee;
	SpotTradeOrderForm_lotNumeric_textAdapter(SpotTradeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void textValueChanged(TextEvent e)
	{
		adaptee.lotNumeric_textValueChanged(e);
	}
}
*/

class SpotTradeOrderForm_lotNumeric_focusAdapter extends FocusAdapter
{
	private SpotTradeOrderForm adaptee;
	SpotTradeOrderForm_lotNumeric_focusAdapter(SpotTradeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.lotNumeric_focusLost(e);
	}

	public void focusGained(FocusEvent e)
	{
		adaptee.lotNumeric_focusGained(e);
	}
}

/*class SpotTradeOrderForm_outstandingOrderTable_actionAdapter implements IActionListener
{
	private SpotTradeOrderForm adaptee;
	SpotTradeOrderForm_outstandingOrderTable_actionAdapter(SpotTradeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.outstandingOrderTable_actionPerformed(e);
	}
}*/

/*class SpotTradeOrderForm_outstandingOrderTable_keyAdapter extends KeyAdapter
{
	private SpotTradeOrderForm adaptee;
	SpotTradeOrderForm_outstandingOrderTable_keyAdapter(SpotTradeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e)
	{
		adaptee.outstandingOrderTable_keyPressed(e);
	}
}*/

class SpotTradeOrderForm_exitButton_actionAdapter implements ActionListener
{
	private SpotTradeOrderForm adaptee;
	SpotTradeOrderForm_exitButton_actionAdapter(SpotTradeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}

class SpotTradeOrderForm_closeAllButton_actionAdapter implements ActionListener
{
	private SpotTradeOrderForm adaptee;
	SpotTradeOrderForm_closeAllButton_actionAdapter(SpotTradeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.closeAll_actionPerformed(e);
	}
}
