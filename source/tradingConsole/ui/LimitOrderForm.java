package tradingConsole.ui;

import java.math.*;
import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.jidesoft.grid.TableColumnChooser;
import framework.*;
import framework.DateTime;
import framework.diagnostics.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.fontHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import com.jidesoft.swing.JideSwingUtilities;
import tradingConsole.enumDefine.physical.InstalmentFrequence;
import tradingConsole.enumDefine.physical.PaymentMode;

public class LimitOrderForm extends JPanel implements IPriceSpinnerSite
{
	//Remarks:
	//orderTypeChoice: MOO/MakeMarketOnOpenOrder
	//accountChoice: AccountCode/MakeOrderAccount

	private OpenContractForm _openContractForm;

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private MakeOrderAccount _makeOrderAccount;
	private Instrument _instrument;
	private Order _order;

	private DateTime _expireTime = null;
	private ExpireType _expireType = null;
	private OpenCloseRelationSite _openCloseRelationSite;
	private Boolean _closeAllSell = null;
	private MakeOrderAccount _makeOrderAccountToCloseAll = null;

	private OrderType _oldOrderType;
	private ArrayList<IPlaceOrderTypeChangedListener> _placeOrderTypeChangedListeners = new ArrayList<IPlaceOrderTypeChangedListener> ();

	/*public LimitOrderForm(JFrame parent)
	  {
	 super(parent, true);
	 try
	 {
	  jbInit();
	  this._openCloseRelationSite = new OpenCloseRelationSite(this);

	  Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
	  this.setBounds(rectangle);
	  //this.setIconImage(TradingConsole.get_TraderImage());
	 }
	 catch (Throwable exception)
	 {
	  TradingConsole.traceSource.trace(TraceType.Error, exception);
	  //exception.printStackTrace();
	 }
	  }*/

	private LimitOrderForm(TradingConsole tradingConsole, Instrument instrument)
	{
		//super(tradingConsole.get_MainForm(), true);
		try
		{
			this._instrument = instrument;
			this.priceEdit = new PriceSpinner(this);
			this.stopPriceEdit = new PriceSpinner(this);
			this.limitPriceEditForIfLimitDone = new PriceSpinner(this);
			this.limitPriceEditForIfStopDone = new PriceSpinner(this);
			this.stopPriceEditForIfLimitDone = new PriceSpinner(this);
			this.stopPriceEditForIfStopDone = new PriceSpinner(this);

			if (instrument.isFromBursa())
			{
				this.bestBuyTable = new DataGrid("BestBuy");
				this.bestSellTable = new DataGrid("BestSell");

				BestLimitTableActionListener actionListener = new BestLimitTableActionListener(this);
				this.bestBuyTable.addActionListener(actionListener);
				this.bestSellTable.addActionListener(actionListener);
			}
			jbInit();
			this._openCloseRelationSite = new OpenCloseRelationSite(this);

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());

			this.instalmentButton.setEnabled(false);
			this.advancePaymentButton.setEnabled(false);
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
			//exception.printStackTrace();
		}
	}

	private boolean fullAmountCheckBoxChangeListenerAdded = false;
	private void updataInstalmentStatus(boolean isBuy)
	{
		if(isBuy)
		{
			if(!this._makeOrderAccount.get_CanInstalment() && !this._makeOrderAccount.get_CanAdvancePayment())
			{
				this.hideInstalmentControls();
			}
			else
			{
				this.paymentModeStaticText.setVisible(this._makeOrderAccount.hasMultiPayment());
				this.fullAmountCheckBox.setVisible(this._makeOrderAccount.hasMultiPayment() && this._makeOrderAccount.get_CanFullPayment());
				if(!fullAmountCheckBoxChangeListenerAdded)
				{
					fullAmountCheckBoxChangeListenerAdded = true;
					fullAmountCheckBox.addChangeListener(new ChangeListener()
					{
						public void stateChanged(ChangeEvent e)
						{
							if (fullAmountCheckBox.isSelected() && instalmentInfo != null)
							{
								instalmentInfo.set_PaymentMode(PaymentMode.FullAmount);
							}
						}
					});
				}
				this.instalmentCheckBox.setVisible(this._makeOrderAccount.get_CanInstalment());
				this.instalmentButton.setVisible(this._makeOrderAccount.get_CanInstalment());

				this.advancePaymentCheckBox.setVisible(this._makeOrderAccount.get_CanAdvancePayment());
				this.advancePaymentButton.setVisible(this._makeOrderAccount.get_CanAdvancePayment());
				if(this.advancePaymentCheckBox.isVisible())
				{
					this.advancePaymentCheckBox.setSelected(true);
				}
				else if(this.instalmentCheckBox.isVisible())
				{
					this.instalmentCheckBox.setSelected(true);
					this.instalmentButton.setEnabled(true);
				}
				else
				{
					this.fullAmountCheckBox.setSelected(true);
					this.instalmentButton.setEnabled(false);
				}
			}
		}
		else
		{
			this.hideInstalmentControls();
		}
	}

	private void hideInstalmentControls()
	{
		this.paymentModeStaticText.setVisible(false);
		this.fullAmountCheckBox.setVisible(false);

		this.advancePaymentCheckBox.setVisible(false);
		this.advancePaymentButton.setVisible(false);

		this.instalmentCheckBox.setVisible(false);
		this.instalmentButton.setVisible(false);
	}

	private void setupInstalment(PaymentMode paymentMode)
	{
		if(this.instalmentForm != null) return;

		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if(lot.compareTo(BigDecimal.ZERO) <= 0) return;

		Price limitPrice = null, stopPrice = null;
		OrderType orderType = this.getOrderType();
		if(orderType.compareTo(OrderType.Limit) == 0 || orderType.compareTo(OrderType.OneCancelOther) == 0
		   || orderType.compareTo(OrderType.Stop) == 0)
		{
			if(this.limitCheckBox.isSelected()) limitPrice = Price.parse(this.priceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
			if(this.stopCheckBox.isSelected()) stopPrice = Price.parse(this.stopPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		}

		this.instalmentForm
			= new InstalmentForm(this.getFrame(), this._makeOrderAccount.get_Account(), lot, this._instrument, this._settingsManager, limitPrice, stopPrice, this.instalmentInfo, paymentMode, false);
		JideSwingUtilities.centerWindow(instalmentForm);
		this.instalmentForm.show();
		this.instalmentForm.toFront();

		if(this.instalmentForm.get_IsConfirmed())
		{
			this.instalmentInfo = this.instalmentForm.get_InstalmentInfoList().get(this._makeOrderAccount.get_Account().get_Id());
		}

		if(this.instalmentInfo == null)
		{
			this.fullAmountCheckBox.setSelected(true);
		}
		else
		{
			if(this.instalmentInfo.isAdvancePayment())
			{
				this.advancePaymentCheckBox.setSelected(true);
			}
			else
			{
				this.instalmentCheckBox.setSelected(true);
			}
		}

		this.instalmentForm = null;
	}

	private void autoCompleteBy(BestLimit bestLimit)
	{
		int limitOrderTypeIndex = -1;
		for (int index = 0; index < this.orderTypeChoice.getItemCount(); index++)
		{
			Object o = this.orderTypeChoice.getValueAt(index);
			if (o instanceof MakeLimitStopOrder)
			{
				limitOrderTypeIndex = index;
				break;
			}
		}

		if (limitOrderTypeIndex != -1)
		{
			this.reset();

			this.orderTypeChoice.setSelectedIndex(limitOrderTypeIndex);
			int selectedIndex = bestLimit.get_IsBuy() ? 1 : 0;
			this.isBuyChoice.setSelectedIndex(selectedIndex);
			if (this.limitCheckBox.isEnabled())
			{
				this.limitCheckBox.setSelected(true);
			}
			this.priceEdit.setText(Double.toString(bestLimit.get_Price()));
			this.priceEdit.setEnabled(true);
			this.priceEdit.setEditable(true);
			this.totalLotTextField.setText(Double.toString(bestLimit.get_Quantity()));

			this.submitButton.setEnabled(true);
		}
	}

	private static class BestLimitTableActionListener implements IActionListener
	{
		private LimitOrderForm _owner;

		public BestLimitTableActionListener(LimitOrderForm limitOrderForm)
		{
			this._owner = limitOrderForm;
		}

		public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
		{
			if (e.get_GridAction() == tradingConsole.ui.grid.Action.DoubleClicked)
			{
				BestLimit bestLimit = (BestLimit)e.get_Object();
				this._owner.autoCompleteBy(bestLimit);
			}
		}
	}

	public void resetData()
	{
	}

	private JDialog _residedWindow = null;

	public LimitOrderForm(JDialog parent, TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Order order,
						  OpenContractForm openContractForm, boolean isBuy, boolean inTab)
	{
		this(parent, tradingConsole, settingsManager, instrument, order,
			 openContractForm, isBuy, inTab, null);
	}

	public LimitOrderForm(JDialog parent, TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Order order,
						  OpenContractForm openContractForm, boolean isBuy, boolean inTab, Boolean closeAllSell)
	{
		this(parent, tradingConsole, settingsManager, instrument, order,
			 openContractForm, isBuy, inTab, null, null);
	}

	public LimitOrderForm(JDialog parent, TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Order order,
						  OpenContractForm openContractForm, boolean isBuy, boolean inTab, Boolean closeAllSell, MakeOrderAccount makeOrderAccountToCloseAll)
	{
		this(tradingConsole, instrument);
		this._residedWindow = parent;

		if (inTab)
		{
			limitCheckBox.setBackground(null);
			stopCheckBox.setBackground(null);
			ocoCheckBox.setBackground(null);
			ifDoneCheckBox.setBackground(null);
			limitCheckBoxForIfLimitDone.setBackground(null);
			stopCheckBoxForIfLimitDone.setBackground(null);
			limitCheckBoxForIfStopDone.setBackground(null);
			stopCheckBoxForIfStopDone.setBackground(null);
		}

		this._openContractForm = openContractForm;

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._order = order;
		this.outstandingOrderTable.setEditable(this._order == null);

		InstrumentPriceProvider instrumentPriceProvider = new InstrumentPriceProvider(this._instrument);
		this.bidButton.set_PriceProvider(instrumentPriceProvider);
		this.askButton.set_PriceProvider(instrumentPriceProvider);
		this.bidButton.setEnabled(false);
		this.askButton.setEnabled(false);

		this.orderTypeStaticText.setText(Language.OrderLMTlblOrderTypeA);
		this.accountStaticText.setText(Language.OrderLMTlblAccountCodeA);
		this.isBuyStaticText.setText(Language.OrderLMTlblIsBuyA);
		this.setPriceStaticText.setText( (this._order != null) ? Language.OrderLMTlblSetPriceA3 : Language.OrderLMTlblSetPriceA);
		this.stopSetPriceStaticText.setText(Language.OrderLMTlblSetPriceA2);
		this.tradeOption1StaticText.setText(TradeOption.getCaption(TradeOption.None));
		this.tradeOption1StaticText.setValue(TradeOption.None);
		this.tradeOption2StaticText.setText(TradeOption.getCaption(TradeOption.None));
		this.tradeOption2StaticText.setValue(TradeOption.None);
		this.totalLotStaticText.setText(Language.OrderLMTlblLot);
		this.closeLotStaticText.setText(Language.CloseLot);
		this.expireTimeStaticText.setText(Language.ExpireOrderPrompt);
		this.resetButton.setText(Language.OrderLMTbtnReset);
		this.submitButton.setText(Language.OrderLMTbtnSubmit);
		this.closeAllButton.setText(Language.CloseAll);
		this.exitButton.setText(Language.OrderLMTbtnExit);
		this.limitPriceStaticTextForIfLimitDone.setText(Language.OrderLMTlblSetPriceA3);
		this.limitPriceStaticTextForIfStopDone.setText(Language.OrderLMTlblSetPriceA3);
		this.stopPriceStaticTextForIfLimitDone.setText(Language.OrderLMTlblSetPriceA2);
		this.stopPriceStaticTextForIfStopDone.setText(Language.OrderLMTlblSetPriceA2);

		this.orderTypeChoice.setEditable(false);
		this.accountChoice.setEditable(false);
		this.isBuyChoice.setEditable(false);
		this.expireTimeChoice.setEditable(false);

		//init structure
		//this.imstrumentDesciptionStaticText.setText(this._instrument.get_Description());
		//this.setTitle(Language.limitOrderFormTitle + "-" + this._instrument.get_Description());
		this.priceEdit.setText("");
		this.stopPriceEdit.setText("");

		//this.fillExpireTime();
		this.fillIsBuyChoice();
		this.fillOrderType();

		this._closeAllSell = closeAllSell;
		if (closeAllSell != null)
		{
			this._makeOrderAccountToCloseAll = makeOrderAccountToCloseAll;
			isBuy = closeAllSell ? true : false;
		}
		boolean isBuy2 = (this._order != null) ? !this._order.get_IsBuy() : isBuy;
		this.changeColor(isBuy2);
		this.isBuyChoice.disableItemEvent();
		this.isBuyChoice.setSelectedIndex( (isBuy2) ? 0 : 1);
		this.isBuyChoice.enableItemEvent();
		//if (Parameter.goodTillMonthType == 1)
		//{
		//	this.expireTimeChoice.setSelectedIndex(0);
		//	//this.expireTimeChoice_OnChange();
		//}
		if (this.orderTypeChoice.getItemCount() > 0)
		{
			this.orderTypeChoice.setSelectedIndex(0);
			this.orderTypeChoice_OnChange();
		}

		this.fillExpireTime();
		if (this._order == null)
		{
			this._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, this.isMakeSpotOrder(), this.isMakeLimitOrder());
			this.totalLotTextField.setText("");
		}
		else
		{
			TradePolicyDetail tradePolicyDetail =
				this._settingsManager.getTradePolicyDetail(this._order.get_Account().get_TradePolicyId(), this._instrument.get_Id());

			BigDecimal availableLot = this._order.getAvailableLotBanlance(false, true);
			BigDecimal lot = AppToolkit.fixLot(availableLot, false, tradePolicyDetail, this._makeOrderAccount);

			this.limitCheckBox.setEnabled(availableLot.compareTo(BigDecimal.ZERO) > 0 && availableLot.compareTo(lot) >= 0);

			availableLot = this._order.getAvailableLotBanlance(false, false);
			lot = AppToolkit.fixLot(availableLot, false, tradePolicyDetail, this._makeOrderAccount);
			this.stopCheckBox.setEnabled(availableLot.compareTo(BigDecimal.ZERO) > 0 && availableLot.compareTo(lot) >= 0);
		}
		this.lotNumericValidation(false);
		this.updateSubmitButtonStatus();
		this.expireTimeDate.initialize(this._settingsManager, this._instrument);
		if (this._instrument.isFromBursa())
		{
			this.fillBestLimits();
		}

		this.updataInstalmentStatus(this.getIsBuy());

		//if (this._makeOrderAccount.get_CanInstalment())
		{
			this.instalmentCheckBox.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					instalmentButton.setEnabled(instalmentCheckBox.isSelected());
					if(instalmentCheckBox.isSelected() &&
					   (instalmentInfo == null || instalmentInfo.get_Period().get_Frequence().equals(InstalmentFrequence.TillPayoff)))
					{
						setupInstalment(PaymentMode.Instalment);
					}
				}
			});

			this.instalmentButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setupInstalment(PaymentMode.Instalment);
				}
			});
		}

		if (this._makeOrderAccount.get_CanAdvancePayment())
		{
			this.advancePaymentCheckBox.setSelected(true);
			this.advancePaymentButton.setEnabled(true);
			this.instalmentInfo = InstalmentInfo.createDefaultAdvancePaymentInfo(this._makeOrderAccount);
		}
		else if(this._makeOrderAccount.get_CanInstalment() && !this._makeOrderAccount.get_CanFullPayment())
		{
			this.instalmentCheckBox.setSelected(true);
			this.instalmentButton.setEnabled(true);
			this.instalmentInfo = InstalmentInfo.createDefaultInstalmentInfo(this._makeOrderAccount);
		}
		else if(this._makeOrderAccount.get_CanFullPayment())
		{
			this.fullAmountCheckBox.setSelected(true);
		}

		this.advancePaymentCheckBox.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				advancePaymentButton.setEnabled(advancePaymentCheckBox.isSelected());
				if(advancePaymentCheckBox.isSelected() &&
				   (instalmentInfo == null || !instalmentInfo.isAdvancePayment()))
				{
					setupInstalment(PaymentMode.AdvancePayment);
				}
			}
		});

		this.advancePaymentButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setupInstalment(PaymentMode.AdvancePayment);
			}
		});

	}

	public Boolean get_CloseAllSell()
	{
		return this._closeAllSell;
	}

	public Instrument getInstrument()
	{
		return this._instrument;
	}

	private boolean isMakeSpotOrder()
	{
		Object o = this.getOrderTypeValue();
		return o.getClass() == MakeSpotTradeOrder.class
			|| o.getClass() == MakeMarketOrder.class || o.getClass() == MakeLiquidationOrder.class;
	}

	private void fillBestLimits()
	{
		this.bestBuyTable.setModel(this._instrument.get_BestLimits().get_BetsBuys());
		this.bestBuyTable.sortColumn(0);
		//TableColumnChooser.hideColumn(this.bestBuyTable, 0);
		TradingConsole.bindingManager.setHeader(this._instrument.get_BestLimits().get_BetsBuys().get_DataSourceKey(), SwingConstants.CENTER, 25,
												GridFixedForeColor.summaryPanel, Color.white, HeaderFont.summaryPanel);

		this.bestSellTable.setModel(this._instrument.get_BestLimits().get_BestSells());
		this.bestSellTable.sortColumn(0);
		//TableColumnChooser.hideColumn(this.bestSellTable, 0);
		TradingConsole.bindingManager.setHeader(this._instrument.get_BestLimits().get_BestSells().get_DataSourceKey(), SwingConstants.CENTER, 25,
												GridFixedForeColor.summaryPanel, Color.white, HeaderFont.summaryPanel);

		this.bestBuyTable.setShowVerticalLines(false);
		this.bestBuyTable.setShowHorizontalLines(false);

		this.bestSellTable.setShowVerticalLines(false);
		this.bestSellTable.setShowHorizontalLines(false);
	}

	public void refreshPrice()
	{
		this.bidButton.updatePrice();
		this.askButton.updatePrice();
		if(this.instalmentForm != null) this.instalmentForm.updatePrice();
	}

	private void fillIsBuyChoice()
	{
		this.isBuyChoice.disableItemEvent();
		this.reallyFillIsBuyChoice();
		this.isBuyChoice.enableItemEvent();
	}

	private void reallyFillIsBuyChoice()
	{
		this.isBuyChoice.removeAllItems();
		this.isBuyChoice.addItem(Language.LongBuy, true);
		this.isBuyChoice.addItem(Language.LongSell, false);
	}

	private void changeColor(boolean isBuy)
	{
		Color color = BuySellColor.getColor(isBuy, false);
		this.priceEdit.setForeground(color);
		this.stopPriceEdit.setForeground(color);
		this.totalLotTextField.setForeground(color);
		//special process,otherwise color will ineffective
		this.totalLotTextField.setText(this.totalLotTextField.getText());

		color = BuySellColor.getColor(!isBuy, false);
		this.limitPriceEditForIfLimitDone.setForeground(color);
		this.stopPriceEditForIfLimitDone.setForeground(color);
		this.limitPriceEditForIfStopDone.setForeground(color);
		this.stopPriceEditForIfStopDone.setForeground(color);
	}

	private boolean isBetweenBidToAsk(Price setPrice)
	{
		if (Parameter.isAllowLimitInSpread)
		{
			return false;
		}
		else
		{
			return (!Price.less(setPrice, this._instrument.get_LastQuotation().get_Bid())
					&& !Price.more(setPrice, this._instrument.get_LastQuotation().get_Ask()));
		}
	}

	private boolean getIsOpen()
	{
		return this._makeOrderAccount == null ? true : this._makeOrderAccount.getSumLiqLots().compareTo(BigDecimal.ZERO) == 0;
	}

	private void setPrice_FocusLost(boolean isPrompt, boolean isLimitPrice)
	{
		boolean isBuy = this.getIsBuy();
		Object o = this.getOrderTypeValue();
		if (o.getClass() == MakeLimitStopOrder.class) //SetOneCancelOtherOrder
		{
			if (isLimitPrice && this.limitCheckBox.isSelected())
			{
				Price setPrice = Price.parse(this.priceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
				this.priceEdit.setText(Price.toString(setPrice));
				if (this.isBetweenBidToAsk(setPrice))
				{
					if (isPrompt)
					{
						AlertDialogForm.showDialog(this.getFrame(), null, true,
							"[" + Language.OrderLMTlblSetPriceA3 + "]" + Language.OrderLMTPageorderValidAlert8);
					}
					this.fillDefaultSetPrice(false);
					return;
				}
			}

			if (!isLimitPrice && this.stopCheckBox.isSelected())
			{
				Price setPrice2 = Price.parse(this.stopPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
				this.stopPriceEdit.setText(Price.toString(setPrice2));
				if (this.isBetweenBidToAsk(setPrice2))
				{
					if (isPrompt)
					{
						AlertDialogForm.showDialog(this.getFrame(), null, true,
							"[" + Language.OrderLMTlblSetPriceA2 + "]" + Language.OrderLMTPageorderValidAlert8);
					}
					this.fillDefaultStopSetPrice(false);
					return;
				}
			}

			SetPriceError[] setPriceErrors = this.changeTradeOptionValue(false);
			if (isLimitPrice && setPriceErrors[0] != SetPriceError.Ok)
			{
				this.handleSetPrieceError(setPriceErrors[0], isPrompt, Language.OrderLMTlblSetPriceA3, isBuy);
				this.fillDefaultSetPrice(false);
			}
			else if (!isLimitPrice && setPriceErrors[1] != SetPriceError.Ok)
			{
				this.handleSetPrieceError(setPriceErrors[1], isPrompt, Language.OrderLMTlblSetPriceA2, isBuy);
				this.fillDefaultStopSetPrice(false);
			}
		}
	}

	private BigDecimal getLotForGetAcceptVariation()
	{
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if(this.limitCheckBox.isSelected() && this.stopCheckBox.isSelected()
		   && !this.ocoCheckBox.isSelected() && this.getIsOpen())
		{
			lot = lot.add(lot);
		}
		return lot;
	}

	private boolean hasAnotherPlacingTran()
	{
		return !this.ocoCheckBox.isSelected() && this.limitCheckBox.isSelected() && this.stopCheckBox.isSelected();
	}

	private HashMap<Guid, RelationOrder> getPlaceRelation()
	{
		return this._makeOrderAccount == null ? null : this._makeOrderAccount.getPlaceRelation();
	}

	private void handleSetPrieceError(SetPriceError setPriceError, boolean isPrompt, String priceName, boolean isBuy)
	{
		Account account = this._makeOrderAccount == null ? null : this._makeOrderAccount.get_Account();
		if (setPriceError == SetPriceError.SetPriceTooCloseMarket)
		{
			if (isPrompt)
			{
				if (this._settingsManager.get_SystemParameter().get_DisplayLmtStopPoints())
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true,
											   "[" + priceName + "] " + Language.OrderLMTPageorderValidAlert2 + " " +
											   this._instrument.get_AcceptLmtVariation(account, isBuy, getLotForGetAcceptVariation(), null, getPlaceRelation(), hasAnotherPlacingTran()) +
											   " " +
											   Language.OrderLMTPageorderValidAlert22);
				}
				else
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, priceName + " " + Language.SetPriceTooCloseToMarket);
				}
			}
		}
		else if (setPriceError == SetPriceError.SetPriceTooFarAwayMarket)
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, "[" + priceName + "] " + Language.OrderLMTPageorderValidAlert3);
			}
		}
		else if (setPriceError == SetPriceError.InvalidSetPrice)
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, "[" + priceName + "] " + Language.InvalidSetPrice);
			}
		}
	}

	private boolean getIsBuy()
	{
		if (this._closeAllSell != null)
		{
			return this._closeAllSell ? true : false;
		}
		if (this.isBuyChoice.getSelectedIndex() == -1)
		{
			this.isBuyChoice.setSelectedIndex(0);
		}
		return (Boolean)this.isBuyChoice.getSelectedValue();
	}

	//checkOpenClose
	private void isBuyChoice_OnChange()
	{
		this._closeAllSell = null;
		this.updateOcoCheckBoxStatus();
		Object o = this.getOrderTypeValue();
		boolean isBuy = this.getIsBuy();
		this.changeColor(isBuy);
		//this.checkOpenClose( (isBuyPrevious && isBuy == isBuyPrevious) ? false : true);
		this.updataInstalmentStatus(isBuy);

		if (o.getClass() == MakeOneCancelOtherOrder.class
			|| o.getClass() == MakeLimitStopOrder.class) //SetLimitStop
		{
			boolean isOpen = this.getIsOpen();
			if (this.limitCheckBox.isSelected())
			{
				this.fillDefaultSetPrice(false);
			}
			if (this.stopCheckBox.isSelected())
			{
				this.fillDefaultStopSetPrice(false);
			}
			/*Price marketPrice = this.getMarketPrice();
			 this.changeTradeOptionValue(false, marketPrice);*/
		}

		boolean oldIsBuy = this._makeOrderAccount.get_IsBuyForCurrent();
		BuySellType buySellType = (isBuy) ? BuySellType.Buy : BuySellType.Sell;
		if (o.getClass() == MakeLimitOrder.class)
		{
			MakeLimitOrder makeLimitOrder = ( (MakeLimitOrder)o);
			makeLimitOrder.setDefaultBuySellType(buySellType);
		}
		if (o.getClass() == MakeLimitStopOrder.class)
		{
			MakeLimitStopOrder makeLimitStopOrder = ( (MakeLimitStopOrder)o);
			makeLimitStopOrder.setDefaultBuySellType(buySellType);
		}
		if (o.getClass() == MakeStopOrder.class)
		{
			MakeStopOrder makeStopOrder = ( (MakeStopOrder)o);
			makeStopOrder.setDefaultBuySellType(buySellType);
		}
		if (o.getClass() == MakeMarketOnOpenOrder.class)
		{
			MakeMarketOnOpenOrder makeMarketOnOpenOrder = ( (MakeMarketOnOpenOrder)o);
			makeMarketOnOpenOrder.setDefaultBuySellType(buySellType);
		}
		if (o.getClass() == MakeMarketOnCloseOrder.class)
		{
			MakeMarketOnCloseOrder makeMarketOnCloseOrder = ( (MakeMarketOnCloseOrder)o);
			makeMarketOnCloseOrder.setDefaultBuySellType(buySellType);
		}
		if (o.getClass() == MakeMarketOrder.class)
		{
			MakeMarketOrder makeMarketOrder = ( (MakeMarketOrder)o);
			makeMarketOrder.setDefaultBuySellType(buySellType);
		}
		if (o.getClass() == MakeOneCancelOtherOrder.class)
		{
			MakeOneCancelOtherOrder makeOneCancelOtherOrder = ( (MakeOneCancelOtherOrder)o);
			makeOneCancelOtherOrder.setDefaultBuySellType(buySellType);
		}

		if (oldIsBuy != isBuy)
		{
			this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both,
				( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._openCloseRelationSite, this._order);
			this._lastAccountLot = BigDecimal.ZERO;
			this.updateCloseLotVisible(true);
			this.updateOcoCheckBoxStatus();
		}
		if (isBuy)
		{
			this._makeOrderAccount.set_BuyLot(BigDecimal.ZERO);
		}
		else
		{
			this._makeOrderAccount.set_SellLot(BigDecimal.ZERO);
		}
		this.totalLotTextField.setText(this.getFormatDefaultLot());
		this._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, this.isMakeSpotOrder(), this.isMakeLimitOrder());
		//isBuy = !isBuy;
		this._makeOrderAccount.set_IsBuyForCurrent(isBuy);
		this.closeLotTextField.setText("");
		this.updateIfDoneCheckBoxStatus();
	}

	private void fillOrderType()
	{
		this.orderTypeChoice.disableItemEvent();
		this.reallyFillOrderType();
		this.orderTypeChoice.enableItemEvent();
	}

	private void reallyFillOrderType()
	{
		this.orderTypeChoice.removeAllItems();

		boolean isBuy = this.getIsBuy();
		BuySellType buySellType = (isBuy) ? BuySellType.Buy : BuySellType.Sell;
		boolean allowPlaceSpotTrade = this._order == null ? true : this._order.getAvailableLotBanlance(true, null).compareTo(BigDecimal.ZERO) > 0;
		boolean allowPlaceNonSpotTrade = this._order == null ? true : this._order.getAvailableLotBanlance(false, true).compareTo(BigDecimal.ZERO) > 0;
		if (!allowPlaceNonSpotTrade)
		{
			allowPlaceNonSpotTrade = this._order.getAvailableLotBanlance(false, false).compareTo(BigDecimal.ZERO) > 0;
		}

		if (allowPlaceNonSpotTrade)
		{
			Object[] result = MakeOrder.isAllowMakeMarketOnOpenOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				MakeMarketOnOpenOrder makeMarketOnOpenOrder = (MakeMarketOnOpenOrder)result[2];
				makeMarketOnOpenOrder.setDefaultBuySellType(buySellType);
				this.orderTypeChoice.addItem(Language.MOOPrompt, makeMarketOnOpenOrder);
			}

			boolean allowLimitStop = false;

			result = MakeOrder.isAllowMakeLimitOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				allowLimitStop = true;
				MakeLimitOrder makeLimitOrder = (MakeLimitOrder)result[2];
				makeLimitOrder.setDefaultBuySellType(buySellType);
				//this.orderTypeChoice.addItem(Language.LMTPrompt, makeLimitOrder);
			}
			else
			{
				allowLimitStop = false;
			}

			result = MakeOrder.isAllowMakeStopOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				allowLimitStop = true;
				MakeStopOrder makeStopOrder = (MakeStopOrder)result[2];
				makeStopOrder.setDefaultBuySellType(buySellType);
				//this.orderTypeChoice.addItem(Language.STPPrompt, makeStopOrder);
			}
			else
			{
				allowLimitStop = false;
			}

			if (allowLimitStop)
			{
				result = MakeOrder.isAllowMakeLimitStopOrder(this._tradingConsole, this._settingsManager, this._instrument,
					(this._order == null) ? null : this._order.get_Transaction().get_Account());
				if ( (Boolean)result[0])
				{
					MakeLimitStopOrder makeLimitStopOrder = (MakeLimitStopOrder)result[2];
					makeLimitStopOrder.setDefaultBuySellType(buySellType);
					this.orderTypeChoice.addItem(Language.LMTPrompt + "/" + Language.STPPrompt, makeLimitStopOrder);
				}
			}

			result = MakeOrder.isAllowMakeMarketOnCloseOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				MakeMarketOnCloseOrder makeMarketOnCloseOrder = (MakeMarketOnCloseOrder)result[2];
				makeMarketOnCloseOrder.setDefaultBuySellType(buySellType);
				this.orderTypeChoice.addItem(Language.MOCPrompt, makeMarketOnCloseOrder);
			}
			if (this._order != null)
			{
				result = MakeOrder.isAllowMakeOneCancelOtherOrder(this._tradingConsole, this._settingsManager, this._instrument,
					(this._order == null) ? null : this._order.get_Transaction().get_Account());
				if ( (Boolean)result[0])
				{
					MakeOneCancelOtherOrder makeOneCancelOtherOrder = (MakeOneCancelOtherOrder)result[2];
					makeOneCancelOtherOrder.setDefaultBuySellType(buySellType);
					//this.orderTypeChoice.addItem(Language.OCOPrompt, makeOneCancelOtherOrder);
				}
			}

			result = MakeOrder.isAllowMarketToLimitOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				MakeMarketToLimitOrder makeMarketToLimitOrder = (MakeMarketToLimitOrder)result[2];
				makeMarketToLimitOrder.setDefaultBuySellType(buySellType);
				this.orderTypeChoice.addItem(Language.MarketToLimit, makeMarketToLimitOrder);
			}

			result = MakeOrder.isAllowStopLimitOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				MakeStopLimitOrder makeStopLimitOrder = (MakeStopLimitOrder)result[2];
				makeStopLimitOrder.setDefaultBuySellType(buySellType);
				this.orderTypeChoice.addItem(Language.StopLimit, makeStopLimitOrder);
			}

			result = MakeOrder.isAllowFAKMarketOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				MakeFAKMarketOrder makeFAKMarketOrder = (MakeFAKMarketOrder)result[2];
				makeFAKMarketOrder.setDefaultBuySellType(buySellType);
				this.orderTypeChoice.addItem(Language.FAK_Market, makeFAKMarketOrder);
			}
		}

		if (allowPlaceSpotTrade)
		{
			Object[] result = MakeOrder.isAllowMakeMarketOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				MakeMarketOrder makeMarketOrder = (MakeMarketOrder)result[2];
				makeMarketOrder.setDefaultBuySellType(buySellType);
				this.orderTypeChoice.addItem(Language.MKTPrompt, makeMarketOrder);
			}
		}
	}

	private Price getMarketPrice()
	{
		return this._instrument.get_LastQuotation().getBuySell(this.getIsBuy());
	}

	private SetPriceError[] changeTradeOptionValue(boolean isSubmit)
	{
		Price marketPrice = this.getMarketPrice();

		SetPriceError[] setPriceErrors = new SetPriceError[]
			{SetPriceError.Ok, SetPriceError.Ok};

		SetPriceError setPriceError = SetPriceError.Ok;
		Object o = this.getOrderTypeValue();
		if (o.getClass() == MakeLimitOrder.class
			|| o.getClass() == MakeOneCancelOtherOrder.class
			|| (o.getClass() == MakeLimitStopOrder.class && this.limitCheckBox.isSelected()))
		{
			setPriceErrors[0] = this.changeTradeOptionValue2(isSubmit, marketPrice, this.priceEdit, this.tradeOption1StaticText);
		}

		if (o.getClass() == MakeStopOrder.class
			|| o.getClass() == MakeOneCancelOtherOrder.class
			|| (o.getClass() == MakeLimitStopOrder.class && this.stopCheckBox.isSelected()))
		{
			setPriceErrors[1] = this.changeTradeOptionValue2(isSubmit, marketPrice, this.stopPriceEdit, this.tradeOption2StaticText);
		}
		return setPriceErrors;
	}

	private SetPriceError changeTradeOptionValue2(boolean isSubmit, Price marketPrice, PriceSpinner setpriceControl, PVStaticText2 tradeOptionControl)
	{
		Account account = this._makeOrderAccount == null ? null : this._makeOrderAccount.get_Account();

		SetPriceError setPriceError = SetPriceError.Ok;
		TradeOption currentTradeOption = TradeOption.None;
		boolean isBuy = this.getIsBuy();

		Price setPrice = Price.parse(setpriceControl.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		if (setPrice == null || marketPrice == null)
		{
			return setPriceError;
		}
		TradeOption tradeOption1 = (!isBuy && (Price.more(setPrice, marketPrice))
									|| (isBuy && (!Price.more(setPrice, marketPrice)))) ? TradeOption.Better : TradeOption.Stop;
		TradeOption tradeOption2 = (isBuy && (Price.more(setPrice, marketPrice))
									|| (!isBuy && (!Price.more(setPrice, marketPrice)))) ? TradeOption.Better : TradeOption.Stop;
		//if (isSubmit)
		{
			TradeOption previousTradeOption = (TradeOption)tradeOptionControl.getValue();
			setPriceError = Order.checkLMTOrderSetPrice(account, true, this._instrument, isBuy, previousTradeOption, setPrice, marketPrice, getLotForGetAcceptVariation(), null, this.getPlaceRelation(), hasAnotherPlacingTran());

			double dblMarketPrice = Price.toDouble(marketPrice);
			if (Math.abs(Price.toDouble(setPrice) - dblMarketPrice) > dblMarketPrice * 0.2)
			{
				setPriceError = SetPriceError.SetPriceTooFarAwayMarket;
			}

			return setPriceError;
		}
		/*else
		   {
		 TradeOption previousTradeOption = (TradeOption)tradeOptionControl.getValue();
		 currentTradeOption = (this._instrument.get_IsNormal()) ? tradeOption1 : tradeOption2;
		 if (previousTradeOption != currentTradeOption)
		 {
		  setPriceError = SetPriceError.InvalidSetPrice;
		 }
		 else
		 {
		  tradeOptionControl.setForeground(TradeOption.getColor(currentTradeOption));
		  tradeOptionControl.setText(TradeOption.getCaption(currentTradeOption));
		  tradeOptionControl.setValue(currentTradeOption);
		 }
		   }
		   return setPriceError;*/
	}

	private void fillDefaultSetPrice(boolean showAlterDialog)
	{
		Account account = this._makeOrderAccount == null ? null : this._makeOrderAccount.get_Account();

		boolean isBuy = this.getIsBuy();
		Price bid = this._instrument.get_LastQuotation().get_Bid();
		Price ask = this._instrument.get_LastQuotation().get_Ask();
		if (bid == null || ask == null)
		{
			if (showAlterDialog)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderSingleDQPageorderValidAlert0);
			}
			return;
		}
		int acceptLmtVariation = this._instrument.get_AcceptLmtVariation(account, isBuy, getLotForGetAcceptVariation(), null, getPlaceRelation(), hasAnotherPlacingTran());
		//Fill Limit Price
		Price price = (this._instrument.get_IsNormal() == isBuy) ? Price.subStract(ask, acceptLmtVariation) : Price.add(bid, acceptLmtVariation);
		if (this.isBetweenBidToAsk(price))
		{
			price = (this._instrument.get_IsNormal() == isBuy) ? Price.subStract(bid, this._instrument.get_NumeratorUnit()) :
				Price.add(ask, this._instrument.get_NumeratorUnit());
		}
		if (this.limitPriceEditForIfLimitDone.isEnabled())
		{
			this.limitPriceEditForIfLimitDone.setText(Price.toString(this.getLimitPriceForDone(price)));
		}
		if (this.stopPriceEditForIfLimitDone.isEnabled())
		{
			this.stopPriceEditForIfLimitDone.setText(Price.toString(this.getStopPriceForDone(price)));
		}

		this.priceEdit.setText(Price.toString(price));
		this.tradeOption1StaticText.setText(TradeOption.getCaption(TradeOption.Better));
		this.tradeOption1StaticText.setValue(TradeOption.Better);
		this.tradeOption1StaticText.setForeground(TradeOption.getColor(TradeOption.Better));
	}

	private void fillDefaultStopSetPrice(boolean showAlterDialog)
	{
		Account account = this._makeOrderAccount == null ? null : this._makeOrderAccount.get_Account();

		boolean isBuy = this.getIsBuy();
		Price bid = this._instrument.get_LastQuotation().get_Bid();
		Price ask = this._instrument.get_LastQuotation().get_Ask();
		if (bid == null || ask == null)
		{
			if (showAlterDialog)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderSingleDQPageorderValidAlert0);
			}
			return;
		}
		int acceptLmtVariation = this._instrument.get_AcceptLmtVariation(account, isBuy, getLotForGetAcceptVariation(), null, getPlaceRelation(), hasAnotherPlacingTran());
		//Fill Stop Price
		Price price2 = (this._instrument.get_IsNormal() == isBuy) ? Price.add(ask, acceptLmtVariation) : Price.subStract(bid, acceptLmtVariation);
		if (this.isBetweenBidToAsk(price2))
		{
			price2 = (this._instrument.get_IsNormal() == isBuy) ? Price.add(ask, this._instrument.get_NumeratorUnit()) :
				Price.subStract(bid, this._instrument.get_NumeratorUnit());
		}
		this.limitPriceEditForIfStopDone.setText(Price.toString(this.getLimitPriceForDone(price2)));
		this.stopPriceEditForIfStopDone.setText(Price.toString(this.getStopPriceForDone(price2)));

		this.stopPriceEdit.setText(Price.toString(price2));
		this.tradeOption2StaticText.setText(TradeOption.getCaption(TradeOption.Stop));
		this.tradeOption2StaticText.setValue(TradeOption.Stop);
		this.tradeOption2StaticText.setForeground(TradeOption.getColor(TradeOption.Stop));
	}

	private BigDecimal getDefaultLot()
	{
		BigDecimal defaultLot;
		Account account = this._makeOrderAccount.get_Account();
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), this._instrument.get_Id());
		boolean isOpen = true;
		if (this.getOrderTypeValue() instanceof MakeOneCancelOtherOrder)
		{
			defaultLot = this._order.get_LotBalance();
			isOpen = false;
			return AppToolkit.fixDefaultLot(defaultLot, isOpen, tradePolicyDetail, account);
		}
		else
		{
			return AppToolkit.getDefaultLot(this._instrument, isOpen, tradePolicyDetail, this._makeOrderAccount);
		}
	}

	private String getFormatDefaultLot()
	{
		BigDecimal defaultValue = this.getDefaultLot();
		return AppToolkit.getFormatLot(defaultValue, this._makeOrderAccount.get_Account(), this._instrument);
		//if (this.getOrderTypeValue() instanceof MakeOneCancelOtherOrder)
		/*if (this._order != null)
		   {
		 return this._order.get_LotBalanceString();
		   }
		   else
		   {
		 TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
		  this._instrument.get_Id());

		 BigDecimal minOpen = tradePolicyDetail.get_MinOpen();
		 BigDecimal defaultLot = tradePolicyDetail.get_DefaultLot();
		 BigDecimal defaultValue = defaultLot.equals(BigDecimal.ZERO) ? minOpen : defaultLot;

		 return AppToolkit.getFormatLot(defaultValue, this._makeOrderAccount.get_Account());
		   }*/
	}

	private void fillDefaultValueForOutstandingOrder(boolean isSelectedRelationOrder)
	{
		if (this.getOrderTypeValue() instanceof MakeOneCancelOtherOrder
			|| (this.getOrderTypeValue() instanceof MakeLimitStopOrder && this._order != null))
		{
			boolean isBuy = this.getIsBuy();
			BuySellType buySellType = (isBuy) ? BuySellType.Buy : BuySellType.Sell;
			//????????//
			//this._makeOrderAccount.setDefaultLiqLotForOutStanding(buySellType, this._order.get_LotBalance());
			this._makeOrderAccount.setDefaultLiqLotForOutStanding(buySellType, isSelectedRelationOrder, this.getOrderType().isSpot(), this.isMakeLimitOrder());
		}
		else
		{
			this._makeOrderAccount.reset(isSelectedRelationOrder, this.getOrderType().isSpot(), this.isMakeLimitOrder());
		}
	}

	private Object getOrderTypeValue()
	{
		return this.orderTypeChoice.getSelectedValue();
	}

	private void fillExpireTime()
	{
		this.fillExpireTimeChoice();
	}

	private void expireTimeVisible(boolean isVisible)
	{
		if (!isVisible)
		{
			this.expireTimeStaticText.setVisible(isVisible);
			this.expireTimeChoice.setVisible(isVisible);
			this.expireTimeDate.setVisible(isVisible);
		}
		else
		{
			this.expireTimeStaticText.setVisible(isVisible);

			this.expireTimeDate.setVisible(false);
			this.expireTimeDate.setEnabled(false);
			this.expireTimeChoice.setVisible(true);
			if (this.expireTimeChoice.getSelectedIndex() != -1)
			{
				String caption = this.expireTimeChoice.getSelectedText();
				if (caption.equalsIgnoreCase(Language.GoodTillDate))
				{
					this.expireTimeDate.setVisible(true);
					this.expireTimeDate.setEnabled(true);
				}
			}
		}
		this.doLayout();
	}

	private DateTime caculateExpireTimeOfGTD()
	{
		DateTime inputTime = this.expireTimeDate.getExpireTime();
		DateTime tradeDayBeginTime
			= this._tradingConsole.get_SettingsManager().get_SystemParameter().get_TradeDayBeginTime();
		return inputTime.get_Date().addDays(1).add(tradeDayBeginTime.get_TimeOfDay());
	}

	private void convertInputGoodTillMonth()
	{
		if (this.expireTimeChoice.getSelectedIndex() != -1)
		{
			DateTime date = (DateTime) (this.expireTimeChoice.getSelectedValue());
			String caption = this.expireTimeChoice.getSelectedText();
			if (caption.equalsIgnoreCase(Language.GoodTillDate))
			{
				this._expireTime = this.caculateExpireTimeOfGTD();
			}
			else
			{
				this._expireTime = date;
			}

			this._expireType = ExpireType.GTD;

			//if(this._instrument.isFromBursa())
			{
				if (caption.equalsIgnoreCase(Language.GoodTillDate))
				{
					this._expireType = ExpireType.GTD;
				}
				else if (caption.equalsIgnoreCase(Language.GoodTillMonthDayOrder))
				{
					this._expireType = ExpireType.Day;
				}
				if (caption.equalsIgnoreCase(Language.GoodTillCancel))
				{
					this._expireType = ExpireType.GTC;
				}
				if (caption.equalsIgnoreCase(Language.ImmediateOrCancel))
				{
					this._expireType = ExpireType.IOC;
				}
				if (caption.equalsIgnoreCase(Language.GoodTillMonthSession))
				{
					this._expireType = ExpireType.Session;
				}
				if (caption.equalsIgnoreCase(Language.FillOrKill))
				{
					this._expireType = ExpireType.FillOrKill;
				}
				if (caption.equalsIgnoreCase(Language.FillAndKill))
				{
					this._expireType = ExpireType.FillAndKill;
				}
			}
			//else
			//{

			//}
		}
	}

	private void orderTypeChoice_OnChange()
	{
		this.fillAccount();
		this.totalLotTextField.setEditable(true);
		if (this.accountChoice.getItemCount() > 0)
		{
			this.accountChoice.setSelectedIndex(0);
		}
		else
		{
			return;
		}

		this.limitCheckBox.setSelected(false);
		this.stopCheckBox.setSelected(false);

		this.updateIfDoneCheckBoxStatus();
		this.ifDoneCheckBox.setSelected(false);
		this.setIfDoneVisible(this.ifDoneCheckBox.isSelected());

		this.rebind();
	}

	private void ifDoneCheckBoxChanged()
	{
		this.setIfDoneVisible(ifDoneCheckBox.isSelected());
		this.updateOcoCheckBoxStatus();
	}

	private void setIfDoneVisible(boolean value)
	{
		this.ifLimitDonePanel.setVisible(value);
		this.ifStopDonePanel.setVisible(value);
		/*this.isBuyChoiceForIfDone.setVisible(value);
		   this.limitCheckBoxForIfDone.setVisible(value);
		   this.LimitPriceEditForIfDone.setVisible(value);
		   this.stopCheckBoxForIfDone.setVisible(value);
		   this.stopPriceEditForIfDone.setVisible(value);
		   this.ocoCheckBoxForIfDone.setVisible(value);
		   this.totalLotForIfDone.setVisible(value);*/
		if (this.scrollPane != null)
		{
			this.scrollPane.setVisible(!value);
		}
		this.closeAllButton.setVisible(!value);
		this.updateIfDoneStatus();
	}

	private void updateIfDoneStatus()
	{
		this.ifLimitDonePanel.setEnabled(this.limitCheckBox.isSelected());
		this.limitCheckBoxForIfLimitDone.setEnabled(this.limitCheckBox.isSelected());
		if (!this.limitCheckBox.isSelected())
		{
			this.limitCheckBoxForIfLimitDone.setSelected(false);
		}
		this.stopCheckBoxForIfLimitDone.setEnabled(this.limitCheckBox.isSelected());
		if (!this.limitCheckBox.isSelected())
		{
			this.stopCheckBoxForIfLimitDone.setSelected(false);
		}

		this.limitPriceEditForIfLimitDone.setEnabled(this.limitCheckBox.isSelected() && this.limitCheckBoxForIfLimitDone.isSelected());
		this.stopPriceEditForIfLimitDone.setEnabled(this.limitCheckBox.isSelected() && this.stopCheckBoxForIfLimitDone.isSelected());

		this.ifStopDonePanel.setEnabled(this.stopCheckBox.isSelected());
		this.limitCheckBoxForIfStopDone.setEnabled(this.stopCheckBox.isSelected());
		if (!this.stopCheckBox.isSelected())
		{
			this.limitCheckBoxForIfStopDone.setSelected(false);
		}
		this.stopCheckBoxForIfStopDone.setEnabled(this.stopCheckBox.isSelected());
		if (!this.stopCheckBox.isSelected())
		{
			this.stopCheckBoxForIfStopDone.setSelected(false);
		}

		this.limitPriceEditForIfStopDone.setEnabled(this.stopCheckBox.isSelected() && this.limitCheckBoxForIfStopDone.isSelected());
		this.stopPriceEditForIfStopDone.setEnabled(this.stopCheckBox.isSelected() && this.stopCheckBoxForIfStopDone.isSelected());
	}

	private void updatePriceEditStatus()
	{
		Object o = this.getOrderTypeValue();
		this.priceEdit.setEnabled(this.limitCheckBox.isSelected() || o.getClass() == MakeStopLimitOrder.class);
		this.stopPriceEdit.setEnabled(this.stopCheckBox.isSelected() || o.getClass() == MakeStopLimitOrder.class);
	}

	private void rebind()
	{
		this._makeOrderAccount = (MakeOrderAccount)this.accountChoice.getSelectedValue();
		//init outstanding Order Grid
		boolean isBuy = this.getIsBuy();
		this._makeOrderAccount.set_IsBuyForCurrent(isBuy);

		boolean relationsHasChange = this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both,
			( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._openCloseRelationSite, this._order);

		if (relationsHasChange)
		{
			this.totalLotTextField.setText(this.getFormatDefaultLot());
			if (this._order != null)
			{
				this.totalLotTextField.setText(AppToolkit.getFormatLot(this._makeOrderAccount.getSumLiqLots(isBuy), this._makeOrderAccount.get_Account(),
					this._instrument));
			}
			this._lastAccountLot = BigDecimal.ZERO;
		}

		if (this.outstandingOrderTable.getSelectedRow() == -1 && this.outstandingOrderTable.getRowCount() > 0)
		{
			this.outstandingOrderTable.changeSelection(0, 0, false, false);
		}

		this.updateCloseLotVisible(false);
		this.accountChoice.setBackground( (this._order == null) ? Color.white : Color.gray);
		this.isBuyChoice.setBackground( (this._order == null) ? Color.white : Color.gray);
		this.accountChoice.setEnabled(this._order == null);
		this.isBuyChoice.setEnabled(this._order == null);

		Object o = this.getOrderTypeValue();
		if (o.getClass() == MakeMarketOrder.class
			|| o.getClass() == MakeMarketOnOpenOrder.class
			|| o.getClass() == MakeMarketOnCloseOrder.class
			|| o.getClass() == MakeMarketToLimitOrder.class
			|| o.getClass() == MakeFAKMarketOrder.class)
		{
			if (this._instrument.isFromBursa())
			{
				this.expireTimeVisible(true);
			}
			else
			{
				this.expireTimeVisible(false);
			}
			this.setPriceStaticText.setVisible(false);
			this.priceEdit.setVisible(false);
			this.stopSetPriceStaticText.setVisible(false);
			this.stopPriceEdit.setVisible(false);
		}
		else if (o.getClass() == MakeLimitStopOrder.class || o.getClass() == MakeStopLimitOrder.class)
		{
			this.expireTimeVisible(true);
			this.setPriceStaticText.setVisible(true);
			this.priceEdit.setVisible(true);
			this.stopSetPriceStaticText.setVisible(true);
			this.stopPriceEdit.setVisible(true);
			//this.outstandingOrderTable.setEditable(true);

			if (relationsHasChange)
			{
				this.fillDefaultValueForOutstandingOrder(true);
			}
			if (relationsHasChange)
			{
				BigDecimal closeLot = this._makeOrderAccount.getSumLiqLots(isBuy);
				if (closeLot.compareTo(BigDecimal.ZERO) > 0)
				{
					this.totalLotTextField.setText(AppToolkit.getFormatLot(closeLot, this._makeOrderAccount.get_Account(), this._instrument));
				}
				else
				{
					this.totalLotTextField.setText(AppToolkit.getFormatLot(this.getDefaultLot(), this._makeOrderAccount.get_Account(), this._instrument));
				}
			}
			this.updatePriceEditStatus();
			this.totalLotTextField.setEditable(true);
		}

		this.limitCheckBox.setVisible(o.getClass() == MakeLimitStopOrder.class);
		this.stopCheckBox.setVisible(o.getClass() == MakeLimitStopOrder.class);

		this.closeLotTextField.setVisible(this.outstandingOrderTable.getRowCount() > 0);
		this.closeLotStaticText.setVisible(this.outstandingOrderTable.getRowCount() > 0);

		BigDecimal currentLot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if (currentLot.compareTo(BigDecimal.ZERO) <= 0)
		{
			this.totalLotTextField.setText(this.getFormatDefaultLot());
		}
		this.updateOcoCheckBoxStatus();
		this.updateSubmitButtonStatus();

		OrderType newOrderType = this.getOrderType();
		for (IPlaceOrderTypeChangedListener placeOrderTypeChangedListener : this._placeOrderTypeChangedListeners)
		{
			placeOrderTypeChangedListener.OrderTypeChanged(newOrderType, this._oldOrderType);
		}
		this._oldOrderType = newOrderType;
		this.setPriceStaticText.setText(newOrderType.isSpot() ? Language.OrderLMTlblSetPriceA : Language.OrderLMTlblSetPriceA3);
		if (this._openContractForm != null)
		{
			this._openContractForm.rebind();
		}
	}

	private void updateCloseLotVisible(boolean resetText)
	{
		this.totalLotTextField.setEnabled(true);
		if (resetText)
		{
			this.totalLotTextField.setText("");
		}
		this.closeLotTextField.setVisible(this.outstandingOrderTable.getRowCount() > 0);
		this.closeLotStaticText.setVisible(this.outstandingOrderTable.getRowCount() > 0);
	}

	private MakeOrderAccount getMakeOrderAccount(Guid accountId)
	{
		Object o = this.getOrderTypeValue();
		if (o.getClass() == MakeLimitOrder.class)
		{
			return ( (MakeLimitOrder)o).get_MakeOrderAccount(accountId);
		}
		else if (o.getClass() == MakeStopOrder.class)
		{
			return ( (MakeStopOrder)o).get_MakeOrderAccount(accountId);
		}
		else if (o.getClass() == MakeMarketOnOpenOrder.class)
		{
			return ( (MakeMarketOnOpenOrder)o).get_MakeOrderAccount(accountId);
		}
		else if (o.getClass() == MakeMarketOnCloseOrder.class)
		{
			return ( (MakeMarketOnCloseOrder)o).get_MakeOrderAccount(accountId);
		}
		else if (o.getClass() == MakeMarketOrder.class)
		{
			return ( (MakeMarketOrder)o).get_MakeOrderAccount(accountId);
		}
		else if (o.getClass() == MakeOneCancelOtherOrder.class)
		{
			return ( (MakeOneCancelOtherOrder)o).get_MakeOrderAccount(accountId);
		}
		else if (o.getClass() == MakeLimitStopOrder.class)
		{
			return ( (MakeLimitStopOrder)o).get_MakeOrderAccount(accountId);
		}
		else if (o.getClass() == MakeMarketToLimitOrder.class)
		{
			return ( (MakeMarketToLimitOrder)o).get_MakeOrderAccount(accountId);
		}
		else if (o.getClass() == MakeStopLimitOrder.class)
		{
			return ( (MakeStopLimitOrder)o).get_MakeOrderAccount(accountId);
		}

		return null;
	}

	private HashMap<Guid, MakeOrderAccount> getMakeOrderAccounts()
	{
		Object o = this.getOrderTypeValue();
		if (o.getClass() == MakeLimitOrder.class)
		{
			return ( (MakeLimitOrder)o).get_MakeOrderAccounts();
		}
		else if (o.getClass() == MakeStopOrder.class)
		{
			return ( (MakeStopOrder)o).get_MakeOrderAccounts();
		}
		else if (o.getClass() == MakeMarketOnOpenOrder.class)
		{
			return ( (MakeMarketOnOpenOrder)o).get_MakeOrderAccounts();
		}
		else if (o.getClass() == MakeMarketOnCloseOrder.class)
		{
			return ( (MakeMarketOnCloseOrder)o).get_MakeOrderAccounts();
		}
		else if (o.getClass() == MakeMarketOrder.class)
		{
			return ( (MakeMarketOrder)o).get_MakeOrderAccounts();
		}
		else if (o.getClass() == MakeOneCancelOtherOrder.class)
		{
			return ( (MakeOneCancelOtherOrder)o).get_MakeOrderAccounts();
		}
		else if (o.getClass() == MakeSpotTradeOrder.class)
		{
			return ( (MakeSpotTradeOrder)o).get_MakeOrderAccounts();
		}
		else if (o.getClass() == MakeLimitStopOrder.class)
		{
			return ( (MakeLimitStopOrder)o).get_MakeOrderAccounts();
		}

		else if (o.getClass() == MakeMarketToLimitOrder.class)
		{
			return ( (MakeMarketToLimitOrder)o).get_MakeOrderAccounts();
		}
		else if (o.getClass() == MakeStopLimitOrder.class)
		{
			return ( (MakeStopLimitOrder)o).get_MakeOrderAccounts();
		}
		else if (o.getClass() == MakeFAKMarketOrder.class)
		{
			return ( (MakeFAKMarketOrder)o).get_MakeOrderAccounts();
		}

		return null;
	}

	private void fillAccount()
	{
		this.accountChoice.disableItemEvent();
		this.reallyFillAccount();
		this.accountChoice.enableItemEvent();
	}

	private void reallyFillAccount()
	{
		this.accountChoice.removeAllItems();
		if (this._order != null)
		{
			this.accountChoice.addItem(this._order.get_AccountCode(), this.getMakeOrderAccount(this._order.get_Transaction().get_Account().get_Id()));
		}
		else
		{
			for (Iterator<MakeOrderAccount> iterator = this.getMakeOrderAccounts().values().iterator(); iterator.hasNext(); )
			{
				MakeOrderAccount makeOrderAccount = iterator.next();
				if (this._makeOrderAccountToCloseAll == null
					|| this._makeOrderAccountToCloseAll.get_Account().get_Id().equals(makeOrderAccount.get_Account().get_Id()))
				{
					this.accountChoice.addItem(makeOrderAccount.get_Account().get_Code(), makeOrderAccount);
				}
			}
		}
		//this.accountChoice.sort(true);
	}

	private void accountChoice_OnChange()
	{
		MakeOrderAccount makeOrderAccount = (MakeOrderAccount)this.accountChoice.getSelectedValue();
		boolean isBuy = makeOrderAccount.get_IsBuyForCurrent();
		if (! (this._makeOrderAccount.get_Account().get_Id().equals(makeOrderAccount.get_Account().get_Id())
			   && this._makeOrderAccount.get_IsBuyForCurrent() == isBuy))
		{
			this._makeOrderAccount = makeOrderAccount;
			this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both,
				( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._openCloseRelationSite, this._order);
			this._lastAccountLot = BigDecimal.ZERO;
			this.updateOcoCheckBoxStatus();
			this.updateIfDoneCheckBoxStatus();
			this.updateCloseLotVisible(true);
			if(this._makeOrderAccount.get_CanAdvancePayment())
			{
				this.instalmentInfo = InstalmentInfo.createDefaultAdvancePaymentInfo(this._makeOrderAccount);
			}
			else
			{
				if(this._makeOrderAccount.get_CanInstalment() && !this._makeOrderAccount.get_CanFullPayment())
				{
					this.instalmentInfo = InstalmentInfo.createDefaultInstalmentInfo(this._makeOrderAccount);
				}
				else
				{
					this.instalmentInfo = null;
				}
			}
			this.isBuyChoice_OnChange();
			this.closeLotTextField.setText("");
		}
		this.fillExpireTime();
	}

	private void expireTimeChoice_OnChange()
	{
		DateTime date = null;
		if (this.expireTimeChoice.getSelectedIndex() != -1)
		{
			date = (DateTime)this.expireTimeChoice.getSelectedValue();
			String caption = this.expireTimeChoice.getSelectedText();
			if (caption.equalsIgnoreCase(Language.GoodTillDate))
			{
				this.expireTimeDate.setVisible(true);
				//this.expireTimeDate.setValue(date);
			}
			this.expireTimeVisible(true);
		}
		else
		{
			//this.expireTimeDate.setDate(null);
			this.expireTimeDate.setVisible(false);
		}
		this._expireTime = date;
	}

	private void fillExpireTimeChoice()
	{
		this.expireTimeChoice.disableItemEvent();
		this.reallyFillExpireTimeChoice();
		this.expireTimeChoice.enableItemEvent();
	}

	private void reallyFillExpireTimeChoice()
	{
		//this.expireTimeDate.setDate(null);
		this._expireTime = null;
		this.expireTimeChoice.removeAllItems();
		if (this._makeOrderAccount == null)
		{
			return;
		}
		if (this._instrument.isFromBursa())
		{
			this.expireTimeChoice.addItem(Language.GoodTillMonthDayOrder, DateTime.maxValue);
			this.expireTimeChoice.addItem(Language.GoodTillCancel, DateTime.maxValue);
			this.expireTimeChoice.addItem(Language.GoodTillDate, DateTime.maxValue);
			this.expireTimeChoice.addItem(Language.FillOrKill, DateTime.maxValue);
			this.expireTimeChoice.addItem(Language.FillAndKill, DateTime.maxValue);
			return;
		}

		DateTime tradeDay = this._settingsManager.get_TradeDay().get_TradeDay();
		//this._settingsManager.get_TradeDay()
		TimeSpan timePart = this._settingsManager.get_TradeDay().get_BeginTime().get_TimeOfDay();

		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
			this._instrument.get_Id());
		//if (Parameter.isHasGoodTillMonthDayOrder)
		if (tradePolicyDetail.get_GoodTillMonthDayOrder())
		{
			DateTime dayOrderDateTime = this._instrument.get_DayCloseTime();
			if (dayOrderDateTime == null || dayOrderDateTime == DateTime.maxValue)
			{
				dayOrderDateTime = tradeDay.addDays(1).get_Date().add(timePart);
			}
			this.expireTimeChoice.addItem(Language.GoodTillMonthDayOrder, dayOrderDateTime);
		}
		//if (Parameter.isHasGoodTillMonthSession)
		if (tradePolicyDetail.get_GoodTillMonthSession())
		{
			DateTime sessionDateTime = this._instrument.get_CloseTime();
			if (sessionDateTime == null || sessionDateTime == DateTime.maxValue)
			{
				sessionDateTime = tradeDay.addDays(1).get_Date().add(timePart);
			}
			this.expireTimeChoice.addItem(Language.GoodTillMonthSession, sessionDateTime);
		}
		//if (Parameter.isHasGoodTillMonthGTM)
		if (tradePolicyDetail.get_GoodTillMonthGTM())
		{
			DateTime addOneMonthTradeDay = tradeDay.addMonths(1);
			DateTime gtmDateTime = addOneMonthTradeDay.addDays( -1).add(timePart);
			this.expireTimeChoice.addItem(Language.GoodTillMonthGTM, gtmDateTime);
		}
		//if (Parameter.isHasGoodTillMonthGTF)
		if (tradePolicyDetail.get_GoodTillMonthGTF())
		{
			this.expireTimeChoice.addItem(Language.GoodTillMonthGTF, this._instrument.get_LastTradeDay());
		}
		if (tradePolicyDetail.get_GoodTillDate())
		{
			this.expireTimeChoice.addItem(Language.GoodTillDate, tradeDay);
		}

		if (this.expireTimeChoice.getItemCount() > 0)
		{
			this.expireTimeChoice.setSelectedIndex(0);

			DateTime date = (DateTime)this.expireTimeChoice.getSelectedValue();
			this._expireTime = date;
		}
	}

//please process call?????????????????
	/*
	  private void checkOneCancelOtherLot(double inputLot)
	  {
	 Object o = this.getOrderTypeValue();
	 if (o.getClass() == MakeOneCancelOtherOrder)
	 {
	  if (this._order != null)
	  {
	   double lot = inputLot;
	   if (inputLot > this._order.get_LotBalance().compareTo(BigDecimal.ZERO) || inputLot <= 0)
	   {
	 lot = this._order.get_LotBalance().doubleValue();
	   }
	   this.lotNumeric.setText(Functions.getFormatLot(lot, this._order.get_Transaction().get_Account()));
	   this.fillDefaultValueForOutstandingOrder(this._order!=null);
	  }
	 }
	  }
	 */
	public void lotNumeric_focusLost()
	{
		this.lotNumericValidation();
	}

	private void lotNumericValidation()
	{
		this.lotNumericValidation(true);
	}

	//input Lot validation
	private void lotNumericValidation(boolean popupAlter)
	{
		boolean isOpen = this._makeOrderAccount.getSumLiqLots(this.getIsBuy()).compareTo(BigDecimal.ZERO) <= 0;

		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if (lot.compareTo(BigDecimal.ZERO) <= 0)
		{
			lot = this.getDefaultLot();
		}
		else
		{
			BigDecimal maxLot = this._instrument.get_MaxOtherLot();
			if (maxLot.compareTo(BigDecimal.ZERO) != 0 && lot.compareTo(maxLot) > 0)
			{
				if (popupAlter)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true,
											   Language.OrderLMTPagetextLot_OnblurAlert0 + "(" + AppToolkit.getFormatLot(maxLot, true) + ")!");
				}
				lot = this.getDefaultLot();
			}
			else
			{
				this.totalLotTextField.setText(AppToolkit.getFormatLot(lot, this._makeOrderAccount.get_Account(), this._instrument));
			}
		}
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
			this._instrument.get_Id());
		BigDecimal lot2 = AppToolkit.fixLot(lot, isOpen, tradePolicyDetail, this._makeOrderAccount);
		String formattedLot = AppToolkit.getFormatLot(lot2, this._makeOrderAccount.get_Account(), this._instrument);
		if (StringHelper.isNullOrEmpty(formattedLot))
		{
			formattedLot = "0";
		}
		if (lot.compareTo(new BigDecimal(formattedLot)) != 0 && popupAlter)
		{
			String info = StringHelper.format(Language.LotIsNotValidAndWillChangeTo, new Object[]
											  {lot, formattedLot});
			AlertDialogForm.showDialog(this.getFrame(), null, true, info);
		}
		this.totalLotTextField.setText(formattedLot);

		//input lot < sumLiqLots, clear liqLots....
		boolean isBuy = this.getIsBuy();
		if (lot.compareTo(this._makeOrderAccount.getSumLiqLots(isBuy)) < 0)
		{
			if (this._order != null)
			{
				this._lastAccountLot = lot;
				this.closeLotTextField.setText(this.totalLotTextField.getText());
			}
			else
			{
				if (popupAlter)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderSingleDQPagetextLot_OnblurAlert3);
				}
				this._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, this.isMakeSpotOrder(), this.isMakeLimitOrder());
			}
		}
	}

	private BigDecimal _lastAccountLot = BigDecimal.ZERO;
	public void updateAccount(BigDecimal accountLot, boolean openOrderIsBuy)
	{
		this.updateOcoCheckBoxStatus();

		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
			this._instrument.get_Id());
		accountLot = AppToolkit.fixLot(accountLot, false, tradePolicyDetail, this._makeOrderAccount);

		BigDecimal currentLot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		BigDecimal accountLot2 = accountLot;
		if (currentLot.compareTo(accountLot) >= 0)
		{
			accountLot2 = currentLot;
			if (accountLot.compareTo(this._lastAccountLot) < 0)
			{
				accountLot2 = accountLot2.subtract(this._lastAccountLot.subtract(accountLot));
			}
		}
		this._lastAccountLot = accountLot;

		Object o = this.getOrderTypeValue();
		if (o.getClass() == MakeLimitStopOrder.class)
		{
			if (this.ocoCheckBox.isSelected())
			{
				accountLot2 = accountLot;
			}
			this.updatePriceEditStatus();
		}
		if (accountLot2.compareTo(BigDecimal.ZERO) <= 0)
		{
			accountLot2 = this.getDefaultLot();
		}

		if (currentLot.compareTo(accountLot2) != 0 && this.totalLotTextField.isVisible())
		{
			try
			{
				this.totalLotTextField.setText(AppToolkit.getFormatLot(accountLot2, this._makeOrderAccount.get_Account(), this._instrument));
				//this.totalLotTextField.setEnabled(accountLot.compareTo(BigDecimal.ZERO) <= 0);
			}
			catch (IllegalStateException exception)
			{

			}
		}
		this.updateSubmitButtonStatus();
	}

	private void updateTotalColseLot(BigDecimal totalCloseLot)
	{
		if (totalCloseLot.compareTo(BigDecimal.ZERO) > 0)
		{
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());
			totalCloseLot = AppToolkit.fixLot(totalCloseLot, false, tradePolicyDetail, this._makeOrderAccount);
		}

		if (totalCloseLot.compareTo(BigDecimal.ZERO) <= 0)
		{
			totalCloseLot = this.getDefaultLot();
		}

		this.totalLotTextField.setText(AppToolkit.getFormatLot(totalCloseLot, this._makeOrderAccount.get_Account(), this._instrument));
		this.updateOcoCheckBoxStatus();

		this.updateDefaultPrice();
	}

	private void updateDefaultPrice()
	{
		boolean isOpen = this.getIsOpen();
		if (this.getOrderType() == OrderType.Limit)
		{
			if (this.limitCheckBox.isSelected())
			{
				this.fillDefaultSetPrice(false);
			}
			if (this.stopCheckBox.isSelected())
			{
				this.fillDefaultStopSetPrice(false);
			}
		}
	}

	static class OpenCloseRelationSite implements IOpenCloseRelationSite
	{
		private LimitOrderForm _owner;

		public OpenCloseRelationSite(LimitOrderForm owner)
		{
			this._owner = owner;
		}

		public OperateType getOperateType()
		{
			return OperateType.Limit;
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
			return this._owner.getFrame();
		}

		public void updateAccount(BigDecimal accountLot, boolean openOrderIsBuy)
		{
			this._owner.updateAccount(accountLot, openOrderIsBuy);
		}

		public OrderType getOrderType()
		{
			return this._owner.getOrderType();
		}

		public Boolean isMakeLimitOrder()
		{
			return this._owner.isMakeLimitOrder();
		}

		public boolean isDelivery()
		{
			return false;
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

		public void updateTotalColseLot(BigDecimal totalCloseLot)
		{
			this._owner.updateTotalColseLot(totalCloseLot);
		}

		public boolean allowChangeCloseLot()
		{
			return false;
		}

		public DataGrid getAccountDataGrid()
		{
			return null;
		}

		public void rebind()
		{
			if (this._owner._order != null
				&& this._owner._order.getAvailableLotBanlance(false, null).compareTo(BigDecimal.ZERO) == 0)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.DisposedForOpenOrderClosed);
				this._owner.dispose2();
			}
			else
			{
				this._owner.rebind();
			}
		}
	}

	private String getLMTSTOPSetPriceValidationPrompt(boolean isCheckBetter)
	{
		boolean isAbove = false;
		boolean isBuy = this.getIsBuy();
		boolean isNormal = this._instrument.get_IsNormal();

		if (isCheckBetter)
		{
			isAbove = ( (!isBuy && isNormal) || (isBuy && !isNormal));
			return (isAbove) ? Language.LMTSTOPSetPriceValidation1 : Language.LMTSTOPSetPriceValidation2;
		}
		else
		{
			isAbove = ( (!isBuy && !isNormal) || (isBuy && isNormal));
			return (isAbove) ? Language.LMTSTOPSetPriceValidation3 : Language.LMTSTOPSetPriceValidation4;
		}
	}

	private boolean isValidTradeOption(boolean isPrompt)
	{
		boolean isValidOrder = false;

		if (!MakeOrder.isAllowOrderType(this._instrument, this.getOrderType()))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert01);
			}
			return isValidOrder;
		}

		Object o = this.getOrderTypeValue();
		if (o.getClass() == MakeLimitOrder.class
			|| o.getClass() == MakeOneCancelOtherOrder.class)
		{
			TradeOption tradeOption1 = (TradeOption)this.tradeOption1StaticText.getValue();
			if (!tradeOption1.equals(TradeOption.Better))
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, this.getLMTSTOPSetPriceValidationPrompt(true));
				}
				return isValidOrder;
			}
		}

		if (o.getClass() == MakeStopOrder.class
			|| o.getClass() == MakeOneCancelOtherOrder.class)
		{
			TradeOption tradeOption2 = (TradeOption)this.tradeOption2StaticText.getValue();
			if (!tradeOption2.equals(TradeOption.Stop))
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, this.getLMTSTOPSetPriceValidationPrompt(false));
				}
				return isValidOrder;
			}
		}

		if (o.getClass() == MakeOneCancelOtherOrder.class)
		{
			TradeOption tradeOption1 = (TradeOption)this.tradeOption1StaticText.getValue();
			TradeOption tradeOption2 = (TradeOption)this.tradeOption2StaticText.getValue();
			if (tradeOption1 == tradeOption2)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderOCOPageorderValidAlert3);
				}
				return isValidOrder;
			}
		}
		return true;
	}

	private boolean isValidOrder(boolean isPrompt)
	{
		boolean isValidOrder = false;

		OrderType orderType = this.getOrderType();
		if (orderType.value() == OrderType.Limit.value() && this.ocoCheckBox.isSelected())
		{
			orderType = OrderType.OneCancelOther;
		}

		boolean isOpen = this._makeOrderAccount.getSumLiqLots(this.getIsBuy()).compareTo(BigDecimal.ZERO) == 0;
		if (orderType == OrderType.OneCancelOther && isOpen)
		{
			Account account = this._makeOrderAccount.get_Account();
			TradePolicyDetail detail = this._settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), this._instrument.get_Id());
			if (!detail.get_AllowNewOCO())
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert01);
				}
				return isValidOrder;
			}
		}
		else if (!MakeOrder.isAllowOrderType(this._instrument, orderType))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert01);
			}
			return isValidOrder;
		}

		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert0);
			}
			return isValidOrder;
		}

		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if (lot.compareTo(BigDecimal.ZERO) <= 0)
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert6);
			}
			return isValidOrder;
		}
		else if (lot.compareTo(this._instrument.get_MaxOtherLot()) > 0)
		{
			if (isPrompt)
			{
				String info = Language.OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert1 + this._instrument.get_MaxOtherLot() + ")";
				AlertDialogForm.showDialog(this.getFrame(), null, true, info);
			}
			return isValidOrder;
		}

		boolean isBuy = this.getIsBuy();
		BigDecimal liqLots = this._makeOrderAccount.getSumLiqLots(isBuy);
		if (liqLots.compareTo(BigDecimal.ZERO) > 0)
		{
			if (lot.compareTo(liqLots) != 0)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert7);

					TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
						this._instrument.get_Id());
					liqLots = AppToolkit.fixLot(liqLots, false, tradePolicyDetail, this._makeOrderAccount);
					this.totalLotTextField.setText(AppToolkit.getFormatLot(liqLots, this._makeOrderAccount.get_Account(), this._instrument));
				}
				return false;
			}
		}
		else if (!this._makeOrderAccount.get_Account().get_AllowAddNewPosition() || !this._instrument.getAllowAddNewPosition(isBuy))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.NewPositionIsNotAllowed);
			}
			return false;
		}

		if (o.getClass() == MakeOneCancelOtherOrder.class)
		{
			if (liqLots.compareTo(BigDecimal.ZERO) <= 0 || lot.compareTo(liqLots) != 0)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert7);
				}
				return isValidOrder;
			}
		}
		BigDecimal[] sumLot = this._makeOrderAccount.getSumLotBSForOpenOrder();
		BigDecimal sumBuyLots = sumLot[0];
		BigDecimal sumSellLots = sumLot[1];
		//if (lot - liqLots > 0)
		{
			//when instrumentCode.substr(0,1)=="#"
			boolean isHasMakeNewOrder = lot.subtract(liqLots).compareTo(BigDecimal.ZERO) > 0;
			if (!this._makeOrderAccount.isAcceptLot(isBuy, lot, isHasMakeNewOrder)) //new BigDecimal( ( (Double) (lot - liqLots)).doubleValue())))
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, Language.NewOrderAcceptedHedging);
				}
				return isValidOrder;
			}
		}

		Price setPrice = Price.parse(this.priceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		Price setPrice2 = Price.parse(this.stopPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		TradePolicyDetail tradePolicyDetail = this._makeOrderAccount.getTradePolicyDetail();
		if (o.getClass() == MakeMarketOnOpenOrder.class || o.getClass() == MakeMarketOnCloseOrder.class)
		{
			if (liqLots.compareTo(BigDecimal.ZERO) <= 0 && !tradePolicyDetail.get_IsAcceptNewMOOMOC())
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, Language.NewOrderAcceptedHedging2);
				}
				return isValidOrder;
			}
		}
		else if (o.getClass() == MakeStopLimitOrder.class)
		{
			if (setPrice == null)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true,
											   "[" + Language.OrderLMTlblSetPriceA3 + "]" + Language.OrderLMTPageorderValidAlert1);
				}
				return isValidOrder;
			}
			this.priceEdit.setText(Price.toString(setPrice));

			if (setPrice2 == null)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true,
											   "[" + Language.OrderLMTlblSetPriceA2 + "]" + Language.OrderLMTPageorderValidAlert1);
				}
				return isValidOrder;
			}
			this.stopPriceEdit.setText(Price.toString(setPrice2));

			Price last = this._instrument.get_LastQuotation().get_LastForBursa();
			if (last == null)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert10);
				}
				return isValidOrder;
			}

			if (isBuy)
			{
				if (!Price.more(setPrice2, last) || !Price.more(setPrice, setPrice2))
				{
					if (isPrompt)
					{
						AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert11);
					}
					return isValidOrder;
				}
			}
			else
			{
				if (!Price.less(setPrice2, last) || !Price.less(setPrice, setPrice2))
				{
					if (isPrompt)
					{
						AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert12);
					}
					return isValidOrder;
				}
			}
		}
		else if (o.getClass() == MakeLimitOrder.class
				 || o.getClass() == MakeStopOrder.class
				 || o.getClass() == MakeOneCancelOtherOrder.class
				 || o.getClass() == MakeLimitStopOrder.class) //SetLimitStop
		{
			if (o.getClass() == MakeLimitOrder.class
				|| o.getClass() == MakeOneCancelOtherOrder.class
				|| (o.getClass() == MakeLimitStopOrder.class && this.limitCheckBox.isSelected()))
			{
				if (setPrice == null)
				{
					if (isPrompt)
					{
						AlertDialogForm.showDialog(this.getFrame(), null, true,
							"[" + Language.OrderLMTlblSetPriceA3 + "]" + Language.OrderLMTPageorderValidAlert1);
					}
					this.fillDefaultSetPrice(false);
					return isValidOrder;
				}
				this.priceEdit.setText(Price.toString(setPrice));
				if (this.isBetweenBidToAsk(setPrice))
				{
					if (isPrompt)
					{
						AlertDialogForm.showDialog(this.getFrame(), null, true,
							"[" + Language.OrderLMTlblSetPriceA3 + "]" + Language.OrderLMTPageorderValidAlert8);
					}
					this.fillDefaultSetPrice(false);
					return isValidOrder;
				}
			}
			if (o.getClass() == MakeStopOrder.class
				|| o.getClass() == MakeOneCancelOtherOrder.class
				|| (o.getClass() == MakeLimitStopOrder.class && this.stopCheckBox.isSelected()))
			{
				if (setPrice2 == null)
				{
					if (isPrompt)
					{
						AlertDialogForm.showDialog(this.getFrame(), null, true,
							"[" + Language.OrderLMTlblSetPriceA2 + "]" + Language.OrderLMTPageorderValidAlert1);
					}
					this.fillDefaultStopSetPrice(false);
					return isValidOrder;
				}
				this.stopPriceEdit.setText(Price.toString(setPrice2));
				if (this.isBetweenBidToAsk(setPrice2))
				{
					if (isPrompt == true)
					{
						AlertDialogForm.showDialog(this.getFrame(), null, true,
							"[" + Language.OrderLMTlblSetPriceA2 + "]" + Language.OrderLMTPageorderValidAlert8);
					}
					this.fillDefaultStopSetPrice(false);
					return isValidOrder;
				}
			}

			SetPriceError[] setPriceErrors = this.changeTradeOptionValue(true);
			if (setPriceErrors[0] != SetPriceError.Ok)
			{
				this.handleSetPrieceError(setPriceErrors[0], isPrompt, Language.OrderLMTlblSetPriceA3, isBuy);
				this.fillDefaultSetPrice(false);
			}

			if (setPriceErrors[1] != SetPriceError.Ok)
			{
				this.handleSetPrieceError(setPriceErrors[1], isPrompt, Language.OrderLMTlblSetPriceA2, isBuy);
				this.fillDefaultStopSetPrice(false);
			}

			if (setPriceErrors[0] != SetPriceError.Ok || setPriceErrors[1] != SetPriceError.Ok)
			{
				return false;
			}

			//for make better order
			if (o.getClass() == MakeLimitOrder.class
				|| o.getClass() == MakeOneCancelOtherOrder.class
				|| (o.getClass() == MakeLimitStopOrder.class && this.limitCheckBox.isSelected()))
			{
				TradeOption tradeOption1 = (TradeOption)this.tradeOption1StaticText.getValue();
				if (tradeOption1.equals(TradeOption.Better)
					&& !tradePolicyDetail.get_IsAcceptNewLimit()
					&& liqLots.compareTo(BigDecimal.ZERO) <= 0)
				{
					if (isBuy)
					{
						if (lot.compareTo(sumSellLots.subtract(sumBuyLots)) > 0)
						{
							if (isPrompt)
							{
								AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlertBetter);
							}
							return isValidOrder;
						}
					}
					else
					{
						if (lot.compareTo(sumBuyLots.subtract(sumSellLots)) > 0)
						{
							if (isPrompt)
							{
								AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlertBetter);
							}
							return isValidOrder;
						}
					}
				}
			}
			//for make stop order
			if (o.getClass() == MakeStopOrder.class
				|| o.getClass() == MakeOneCancelOtherOrder.class
				|| (o.getClass() == MakeLimitStopOrder.class && this.stopCheckBox.isSelected()))
			{
				TradeOption tradeOption2 = (TradeOption)this.tradeOption2StaticText.getValue();
				if (tradeOption2.equals(TradeOption.Stop)
					&& !tradePolicyDetail.get_IsAcceptNewStop())
				{
					BigDecimal[] sumLot2 = this._makeOrderAccount.getSumLotBSForMakeStopOrder();
					BigDecimal sumBuyLotBalances = sumLot2[0];
					BigDecimal sumSellLotBalances = sumLot2[1];
					if (isBuy)
					{
						if (lot.compareTo(sumSellLotBalances.subtract(sumBuyLotBalances)) > 0)
						{
							if (isPrompt)
							{
								AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert4);
							}
							return isValidOrder;
						}
					}
					else
					{
						if (lot.compareTo(sumBuyLotBalances.subtract(sumSellLotBalances)) > 0)
						{
							if (isPrompt)
							{
								AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert5);
							}
							return isValidOrder;
						}
					}
				}
			}
		}
		if (liqLots.compareTo(BigDecimal.ZERO) <= 0)
		{
			//????????????
			//this.checkOpenClose(true);
		}
		else
		{
			/*if (lot.compareTo(liqLots) != 0)
			 {
			 if (isPrompt)
			 {
			  AlertDialogForm.showDialog(this.getFrame(), null, true, Language.OrderLMTPageorderValidAlert7);
			 }
			 return isValidOrder;
			 }*/
		}
		Object[] result = MakeOrder.isAcceptTime(this.getOrderType(), this._settingsManager, this._instrument, false);
		if (! (Boolean)result[0])
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, (String)result[1]);
			}
			return isValidOrder;
		}

		//IsAcceptEntrance
		if (o.getClass() == MakeLimitOrder.class)
		{
			setPrice2 = null;
		}
		else if (o.getClass() == MakeStopOrder.class)
		{
			setPrice = null;
		}
		else if (o.getClass() == MakeMarketOrder.class
				 || o.getClass() == MakeMarketOnOpenOrder.class
				 || o.getClass() == MakeMarketOnCloseOrder.class)
		{
			setPrice = null;
			setPrice2 = null;
		}
		BigDecimal lot2 = lot;
		BigDecimal liqLots2 = liqLots;
		result = MakeOrder.isAcceptEntrance(this._settingsManager, this._makeOrderAccount, this._instrument, this.getOrderType(), isBuy,
											lot2, liqLots2, setPrice, setPrice2, false);
		if (! ( (Boolean)result[0]))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, (String)result[1]);
			}
			return isValidOrder;
		}

		return true;
	}

	private OrderType getOrderType()
	{
		Object o = this.getOrderTypeValue();
		OrderType orderType = OrderType.Limit;
		if (o.getClass() == MakeOneCancelOtherOrder.class)
		{
			orderType = OrderType.Limit;
		}
		if (o.getClass() == MakeLimitStopOrder.class)
		{
			orderType = OrderType.Limit;
		}
		else if (o.getClass() == MakeLimitOrder.class)
		{
			orderType = OrderType.Limit;
		}
		else if (o.getClass() == MakeStopOrder.class)
		{
			orderType = OrderType.Limit;
		}
		else if (o.getClass() == MakeMarketOrder.class)
		{
			orderType = OrderType.Market;
		}
		else if (o.getClass() == MakeMarketOnOpenOrder.class)
		{
			orderType = OrderType.MarketOnOpen;
		}
		else if (o.getClass() == MakeMarketOnCloseOrder.class)
		{
			orderType = OrderType.MarketOnClose;
		}
		else if (o.getClass() == MakeMarketToLimitOrder.class)
		{
			orderType = OrderType.MarketToLimit;
		}
		else if (o.getClass() == MakeStopLimitOrder.class)
		{
			orderType = OrderType.StopLimit;
		}
		else if (o.getClass() == MakeFAKMarketOrder.class)
		{
			orderType = OrderType.FAK_Market;
		}
		return orderType;
	}

	private void reset()
	{
		this.totalLotTextField.setText(this.getFormatDefaultLot());
		this.closeLotTextField.setText("");
		this.limitCheckBox.setSelected(false);
		this.stopCheckBox.setSelected(false);

		this.fillDefaultValueForOutstandingOrder(this._order != null);

		if (this._order != null)
		{
			boolean isBuy = this.getIsBuy();
			this.totalLotTextField.setText(AppToolkit.getFormatLot(this._makeOrderAccount.getSumLiqLots(isBuy), this._makeOrderAccount.get_Account(),
				this._instrument));
		}
		int column = this.outstandingOrderTable.get_BindingSource().getColumnByName(OutstandingOrderColKey.IsBuy);
		if (column != -1)
		{
			TableColumnChooser.hideColumn(this.outstandingOrderTable, column);
		}

		this.updateOcoCheckBoxStatus();
		this.updateSubmitButtonStatus();
		this.updateIfDoneCheckBoxStatus();

		boolean isOpen = this.getIsOpen();
		this.fillDefaultSetPrice(false);
		this.fillDefaultStopSetPrice(false);
	}

	private void closeAll()
	{
		BigDecimal lot = this._makeOrderAccount.closeAll();
		BuySellType buySell = this._makeOrderAccount.GetBuySellForCloseAll();
		this.updateAccount(lot, buySell == BuySellType.Buy ? true : false);
		this.updateDefaultPrice();
	}

	private void submit()
	{
		if (!this.isValidOrder(true))
		{
			return;
		}

		this.convertInputGoodTillMonth();
		if (this._expireTime == null)
		{
			return;
		}
		//set value to MakeOrderAccount
		Object o = this.getOrderTypeValue();
		Price setPrice = Price.parse(this.priceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		Price setPrice2 = Price.parse(this.stopPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		if (o.getClass() == MakeLimitOrder.class)
		{
			setPrice2 = null;
		}
		else if (o.getClass() == MakeStopOrder.class)
		{
			setPrice = setPrice2.clone();
			setPrice2 = null;
		}
		else if (o.getClass() == MakeMarketOrder.class
				 || o.getClass() == MakeMarketOnOpenOrder.class
				 || o.getClass() == MakeMarketOnCloseOrder.class
				 || o.getClass() == MakeMarketToLimitOrder.class
				 || o.getClass() == MakeFAKMarketOrder.class)
		{
			setPrice = null;
			setPrice2 = null;
		}
		boolean isBuy = this.getIsBuy();
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		Price limitPriceForIfLimitDone = null, stopPirceForIfLimitDone = null, limitPriceForIfStopDone = null, stopPirceForIfStopDone = null;
		OperateType operateType = (o.getClass() == MakeOneCancelOtherOrder.class) ? OperateType.OneCancelOther : OperateType.Limit;
		if (o.getClass() == MakeLimitStopOrder.class)
		{
			if (this.limitCheckBox.isSelected() && this.stopCheckBox.isSelected())
			{
				operateType = OperateType.LimitStop;
			}
			else if (this.limitCheckBox.isSelected() || this.stopCheckBox.isSelected())
			{
				operateType = OperateType.Limit;
			}

			int numeratorUnit = this._instrument.get_NumeratorUnit();
			int denominator = this._instrument.get_Denominator();
			if (!this.ifDoneCheckBox.isSelected())
			{
				this._makeOrderAccount.set_IfDoneInfo(null);
			}
			else
			{
				boolean isBuyForIfDone = !isBuy;
				int acceptIfDoneVariation = this._instrument.get_AcceptIfDoneVariation();
				if (this.limitCheckBoxForIfLimitDone.isSelected())
				{
					limitPriceForIfLimitDone = Price.parse(this.limitPriceEditForIfLimitDone.getText(), numeratorUnit, denominator);
				}
				if (this.stopCheckBoxForIfLimitDone.isSelected())
				{
					stopPirceForIfLimitDone = Price.parse(this.stopPriceEditForIfLimitDone.getText(), numeratorUnit, denominator);
				}
				if (limitPriceForIfLimitDone != null)
				{
					Price comparePrice = (this._instrument.get_IsNormal() != isBuyForIfDone) ?
						Price.add(setPrice, acceptIfDoneVariation) : Price.subStract(setPrice, acceptIfDoneVariation);
					double comparePriceValue = Price.toDouble(comparePrice);
					if ( ( (this._instrument.get_IsNormal() != isBuyForIfDone) ? limitPriceForIfLimitDone.compareTo(comparePrice) < 0 :
						  limitPriceForIfLimitDone.compareTo(comparePrice) > 0)
						|| Math.abs(Price.toDouble(limitPriceForIfLimitDone) - comparePriceValue) > comparePriceValue * 0.2)
					{
						Object[] params = null;
						if (PublicParametersManager.getLocal() == Locale.SIMPLIFIED_CHINESE || PublicParametersManager.getLocal() == Locale.TRADITIONAL_CHINESE)
						{
							params = new Object[]
								{this.ifLimitDonePanelTitledBorder.getTitle(), Language.OrderLMTlblSetPriceA3};
						}
						else
						{
							params = new Object[]
								{Language.OrderLMTlblSetPriceA3, this.ifLimitDonePanelTitledBorder.getTitle()};
						}
						String info = StringHelper.format(Language.PriceForIsNotAcceptable, params);
						AlertDialogForm.showDialog(this.getFrame(), null, true, info);
						return;
					}
				}

				int spread = this._settingsManager.getQuotePolicyDetail(this._instrument.get_Id()).get_Spread() + this._instrument.get_NumeratorUnit();
				spread = Math.max(spread, acceptIfDoneVariation);
				if (stopPirceForIfLimitDone != null)
				{
					Price comparePrice = isBuyForIfDone ? Price.add(setPrice, spread) : Price.subStract(setPrice, spread);
					double comparePriceValue = Price.toDouble(comparePrice);
					if ( ( (this._instrument.get_IsNormal() != isBuyForIfDone) ? stopPirceForIfLimitDone.compareTo(comparePrice) > 0 :
						  stopPirceForIfLimitDone.compareTo(comparePrice) < 0)
						|| Math.abs(Price.toDouble(stopPirceForIfLimitDone) - comparePriceValue) > comparePriceValue * 0.2)
					{
						Object[] params = null;
						if (PublicParametersManager.getLocal() == Locale.SIMPLIFIED_CHINESE || PublicParametersManager.getLocal() == Locale.TRADITIONAL_CHINESE)
						{
							params = new Object[]
								{this.ifLimitDonePanelTitledBorder.getTitle(), Language.OrderLMTlblSetPriceA2};
						}
						else
						{
							params = new Object[]
								{Language.OrderLMTlblSetPriceA2, this.ifLimitDonePanelTitledBorder.getTitle()};
						}
						String info = StringHelper.format(Language.PriceForIsNotAcceptable, params);
						AlertDialogForm.showDialog(this.getFrame(), null, true, info);
						return;
					}
				}

				if (this.limitCheckBoxForIfStopDone.isSelected())
				{
					limitPriceForIfStopDone = Price.parse(this.limitPriceEditForIfStopDone.getText(), numeratorUnit, denominator);
				}
				if (this.stopCheckBoxForIfStopDone.isSelected())
				{
					stopPirceForIfStopDone = Price.parse(this.stopPriceEditForIfStopDone.getText(), numeratorUnit, denominator);
				}
				if (limitPriceForIfStopDone != null)
				{
					Price comparePrice = (this._instrument.get_IsNormal() != isBuyForIfDone) ?
						Price.add(setPrice2, acceptIfDoneVariation) : Price.subStract(setPrice2, acceptIfDoneVariation);
					double comparePriceValue = Price.toDouble(comparePrice);
					if ( ( (this._instrument.get_IsNormal() != isBuyForIfDone) ? limitPriceForIfStopDone.compareTo(comparePrice) < 0 :
						  limitPriceForIfStopDone.compareTo(comparePrice) > 0)
						|| Math.abs(Price.toDouble(limitPriceForIfStopDone) - comparePriceValue) > comparePriceValue * 0.2)
					{
						Object[] params = null;
						if (PublicParametersManager.getLocal() == Locale.SIMPLIFIED_CHINESE || PublicParametersManager.getLocal() == Locale.TRADITIONAL_CHINESE)
						{
							params = new Object[]
								{this.ifStopDonePanelTitledBorder.getTitle(), Language.OrderLMTlblSetPriceA3};
						}
						else
						{
							params = new Object[]
								{Language.OrderLMTlblSetPriceA3, this.ifStopDonePanelTitledBorder.getTitle()};
						}
						String info = StringHelper.format(Language.PriceForIsNotAcceptable, params);
						AlertDialogForm.showDialog(this.getFrame(), null, true, info);
						return;
					}
				}

				if (stopPirceForIfStopDone != null)
				{
					Price comparePrice = isBuyForIfDone ? Price.add(setPrice2, spread) : Price.subStract(setPrice2, spread);
					double comparePriceValue = Price.toDouble(comparePrice);
					if ( ( (this._instrument.get_IsNormal() != isBuyForIfDone) ? stopPirceForIfStopDone.compareTo(comparePrice) > 0 :
						  stopPirceForIfStopDone.compareTo(comparePrice) < 0)
						|| Math.abs(Price.toDouble(stopPirceForIfStopDone) - comparePriceValue) > comparePriceValue * 0.2)
					{
						Object[] params = null;
						if (PublicParametersManager.getLocal() == Locale.SIMPLIFIED_CHINESE || PublicParametersManager.getLocal() == Locale.TRADITIONAL_CHINESE)
						{
							params = new Object[]
								{this.ifStopDonePanelTitledBorder.getTitle(), Language.OrderLMTlblSetPriceA2};
						}
						else
						{
							params = new Object[]
								{Language.OrderLMTlblSetPriceA2, this.ifStopDonePanelTitledBorder.getTitle()};
						}
						String info = StringHelper.format(Language.PriceForIsNotAcceptable, params);
						AlertDialogForm.showDialog(this.getFrame(), null, true, info);
						return;
					}
				}
				IfDoneInfo ifDoneInfo
					= new IfDoneInfo(limitPriceForIfLimitDone, stopPirceForIfLimitDone, limitPriceForIfStopDone, stopPirceForIfStopDone, isBuyForIfDone, lot);
				this._makeOrderAccount.set_IfDoneInfo(ifDoneInfo);
			}
		}

		this._makeOrderAccount.set_BuyLot(lot);
		this._makeOrderAccount.set_BuySetPrice( (isBuy) ? setPrice : null);
		this._makeOrderAccount.set_BuySetPrice2( (isBuy) ? setPrice2 : null);
		this._makeOrderAccount.set_SellLot(lot);
		this._makeOrderAccount.set_SellSetPrice( (isBuy) ? null : setPrice);
		this._makeOrderAccount.set_SellSetPrice2( (isBuy) ? null : setPrice2);
		this._makeOrderAccount.set_BuySellType( (isBuy) ? BuySellType.Buy : BuySellType.Sell);
		this._makeOrderAccount.set_IsBuyForCurrent(isBuy);
		if(isBuy && (this.instalmentCheckBox.isSelected() || this.advancePaymentCheckBox.isSelected()))
		{
			this._makeOrderAccount.set_InstalmentInfo(this.instalmentInfo);
		}
		else
		{
			this._makeOrderAccount.set_InstalmentInfo(null);
		}

		if (o.getClass() == MakeLimitStopOrder.class && !this.limitCheckBox.isSelected() && this.stopCheckBox.isSelected())
		{
			this._makeOrderAccount.set_BuySetPrice( (isBuy) ? setPrice2 : null);
			this._makeOrderAccount.set_SellSetPrice( (isBuy) ? null : setPrice2);
		}

		if (o.getClass() == MakeLimitOrder.class
			|| (o.getClass() == MakeLimitStopOrder.class && this.limitCheckBox.isSelected() && !this.stopCheckBox.isSelected()))
		{
			TradeOption tradeOption = (TradeOption)this.tradeOption1StaticText.getValue();
			this._makeOrderAccount.set_BuyTradeOption( (isBuy) ? tradeOption : TradeOption.None);
			this._makeOrderAccount.set_SellTradeOption( (isBuy) ? TradeOption.None : tradeOption);
		}
		if (o.getClass() == MakeStopOrder.class
			|| (o.getClass() == MakeLimitStopOrder.class && this.stopCheckBox.isSelected() && !this.limitCheckBox.isSelected()))
		{
			TradeOption tradeOption2 = (TradeOption)this.tradeOption2StaticText.getValue();
			this._makeOrderAccount.set_BuyTradeOption( (isBuy) ? tradeOption2 : TradeOption.None);
			this._makeOrderAccount.set_SellTradeOption( (isBuy) ? TradeOption.None : tradeOption2);
		}
		else if (o.getClass() == MakeOneCancelOtherOrder.class
				 || (o.getClass() == MakeLimitStopOrder.class && this.limitCheckBox.isSelected() && this.stopCheckBox.isSelected()))
		{
			TradeOption tradeOption = (TradeOption)this.tradeOption1StaticText.getValue();
			TradeOption tradeOption2 = (TradeOption)this.tradeOption2StaticText.getValue();
			this._makeOrderAccount.set_BuyTradeOption( (isBuy) ? tradeOption : TradeOption.None);
			this._makeOrderAccount.set_SellTradeOption( (isBuy) ? TradeOption.None : tradeOption);
			this._makeOrderAccount.set_BuyTradeOption2( (isBuy) ? tradeOption2 : TradeOption.None);
			this._makeOrderAccount.set_SellTradeOption2( (isBuy) ? TradeOption.None : tradeOption2);
		}

		//this.ConvertInputGoodTillMonth();

		/*
		   DateTime date = this._expireTime;
		   if (Parameter.goodTillMonthType == 1)
		   {
		 date = (DateTime) ( (Vector)this.expireTimeChoice.getItemAt(this.expireTimeChoice.getSelectedIndex())).elementAt(1);
		   }
		   else
		   {
		 if (this.expireTimeDate.getDate() == null)
		 {
		  date = null;
		 }
		 else
		 {
		  date = DateTime.fromDate(this.expireTimeDate.getDate());
		 }
		   }
		 */

		HashMap<Guid, MakeOrderAccount> makeOrderAccounts = new HashMap<Guid, MakeOrderAccount> ();
		makeOrderAccounts.put(this._makeOrderAccount.get_Account().get_Id(), this._makeOrderAccount);

		//PalceLotNnemonic.set(this._instrument.get_Id(), lot);

		VerificationOrderForm verificationOrderForm = new VerificationOrderForm(this.getFrame(), "Verification Order", true, this._tradingConsole,
			this._settingsManager, this._instrument, makeOrderAccounts /*this.getMakeOrderAccounts()*/, this.getOrderType(),
			operateType, false, null, this._expireTime, this._expireType, this.ocoCheckBox.isSelected()); //date);
		//verificationOrderForm.show();
	}

	public void dispose2()
	{
		if (this._openContractForm != null)
		{
			this._openContractForm.dispose();
		}
		this.getFrame().dispose();
	}

	private void lotNumeric_focusGained()
	{
		this.totalLotTextField.select(0, this.totalLotTextField.getText().length());
	}

	private void setPriceEdit_focusGained()
	{
		this.priceEdit_focusGained(this.priceEdit);
	}

	private void stopSetPriceEdit_focusGained()
	{
		this.priceEdit_focusGained(this.stopPriceEdit);
	}

	private void priceEdit_focusGained(PriceSpinner priceEdit)
	{
		String text = priceEdit.getText();
		priceEdit.select( ( (text.indexOf(".") != -1) ? text.indexOf(".") + 1 : 0), text.length());
	}

	private void limitCheckBoxForIfLimitDoneChanged()
	{
		this.limitPriceEditForIfLimitDone.setEnabled(this.limitCheckBox.isSelected() && this.limitCheckBoxForIfLimitDone.isSelected());
		if (this.limitPriceEditForIfLimitDone.isEnabled())
		{
			Price setPrice = Price.parse(this.priceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
			Price price = this.getLimitPriceForDone(setPrice);
			this.limitPriceEditForIfLimitDone.setText(Price.toString(price));
		}
	}

	private Price getLimitPriceForDone(Price basePrice)
	{
		boolean isBuyForDone = !this.getIsBuy();
		int acceptIfDoneVariation = this._instrument.get_AcceptIfDoneVariation();
		if (isBuyForDone == this._instrument.get_IsNormal())
		{
			return Price.subStract(basePrice, acceptIfDoneVariation);
		}
		else
		{
			return Price.add(basePrice, acceptIfDoneVariation);
		}
	}

	private Price getStopPriceForDone(Price basePrice)
	{
		boolean isBuyForDone = !this.getIsBuy();
		int spread = this._settingsManager.getQuotePolicyDetail(this._instrument.get_Id()).get_Spread() + this._instrument.get_NumeratorUnit();
		int acceptIfDoneVariation = this._instrument.get_AcceptIfDoneVariation();
		spread = Math.max(spread, acceptIfDoneVariation);
		if (isBuyForDone == this._instrument.get_IsNormal())
		{
			return Price.add(basePrice, spread);
		}
		else
		{
			return Price.subStract(basePrice, spread);
		}
	}

	private void stopCheckBoxForIfLimitDoneChanged()
	{
		this.stopPriceEditForIfLimitDone.setEnabled(this.limitCheckBox.isSelected() && this.stopCheckBoxForIfLimitDone.isSelected());
		if (this.stopPriceEditForIfLimitDone.isEnabled())
		{
			Price setPrice = Price.parse(this.priceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
			Price price = this.getStopPriceForDone(setPrice);
			this.stopPriceEditForIfLimitDone.setText(Price.toString(price));
		}
	}

	private void limitCheckBoxForIfStopDoneChanged()
	{
		this.limitPriceEditForIfStopDone.setEnabled(this.stopCheckBox.isSelected() && this.limitCheckBoxForIfStopDone.isSelected());
		if (this.limitPriceEditForIfStopDone.isEnabled())
		{
			Price setPrice = Price.parse(this.stopPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
			Price price = this.getLimitPriceForDone(setPrice);
			this.limitPriceEditForIfStopDone.setText(Price.toString(price));
		}
	}

	private void stopCheckBoxForIfStopDoneChanged()
	{
		this.stopPriceEditForIfStopDone.setEnabled(this.stopCheckBox.isSelected() && this.stopCheckBoxForIfStopDone.isSelected());
		if (this.stopPriceEditForIfStopDone.isEnabled())
		{
			Price setPrice = Price.parse(this.stopPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
			Price price = this.getStopPriceForDone(setPrice);
			this.stopPriceEditForIfStopDone.setText(Price.toString(price));
		}
	}

//SourceCode End///////////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		//this.addWindowListener(new LimitOrderUi_this_windowAdapter(this));
		totalCloseLotTitle.setText("Total close lot:");

		this.setSize(700, 300);
		//this.setResizable(true);
		this.setLayout(gridBagLayout1);
		//this.setTitle(Language.limitOrderFormTitle);
		this.setBackground(FormBackColor.limitOrderForm);

		//ocoIfDoneGroup.add(this.ifDoneCheckBox);
		//ocoIfDoneGroup.add(this.ocoCheckBox);

		Font font = new Font("SansSerif", Font.BOLD, 14);
		totalLotTextField.setFont(font);
		priceEdit.setFont(font);
		stopPriceEdit.setFont(font);
		limitPriceEditForIfLimitDone.setFont(font);
		stopPriceEditForIfLimitDone.setFont(font);

		limitPriceEditForIfStopDone.setFont(font);
		stopPriceEditForIfStopDone.setFont(font);

		ifLimitDonePanel = new JPanel();
		ifLimitDonePanel.setBackground(null);
		ifLimitDonePanelTitledBorder.setTitle(Language.IfDonePrompt);
		ifLimitDonePanelTitledBorder.setTitleFont(font);
		ifLimitDonePanel.setBorder(ifLimitDonePanelTitledBorder);

		ifStopDonePanel = new JPanel();
		ifStopDonePanel.setBackground(null);
		ifStopDonePanelTitledBorder.setTitle(Language.IfDonePrompt);
		ifStopDonePanelTitledBorder.setTitleFont(font);
		ifStopDonePanel.setBorder(ifStopDonePanelTitledBorder);

		//imstrumentDesciptionStaticText.setText("GBP");

		//this.imstrumentDesciptionStaticText.setFont(font);
		font = new Font("SansSerif", Font.BOLD, 13);

		limitPriceStaticTextForIfStopDone.setFont(font);
		stopPriceStaticTextForIfStopDone.setFont(font);
		limitPriceStaticTextForIfLimitDone.setFont(font);
		stopPriceStaticTextForIfLimitDone.setFont(font);

		orderTypeStaticText.setText("Type");
		orderTypeStaticText.setFont(font);
		accountStaticText.setText("Account");
		accountStaticText.setFont(font);
		isBuyStaticText.setText("B/S");
		isBuyStaticText.setFont(font);
		setPriceStaticText.setText("Limit");
		setPriceStaticText.setFont(font);
		totalLotStaticText.setText("Lot");
		totalLotStaticText.setFont(font);
		instrumentQuoteDescription.setFont(font);
		closeLotStaticText.setFont(font);
		expireTimeStaticText.setText("Expire Time");
		expireTimeStaticText.setFont(font);
		totalLotTextField.setText("Numeric1");
		this.instalmentCheckBox.setFont(font);
		this.paymentModeStaticText.setFont(font);
		this.fullAmountCheckBox.setFont(font);
		this.advancePaymentCheckBox.setFont(font);
		totalLotTextField.addFocusListener(new LimitOrderForm_lotNumeric_focusAdapter(this));
		KeyListener keyListener = new KeyListener()
		{
			public void keyTyped(KeyEvent e)
			{
			}

			public void keyPressed(KeyEvent e)
			{
			}

			public void keyReleased(KeyEvent e)
			{
				updateSubmitButtonStatus();
			}
		};
		totalLotTextField.addKeyListener(keyListener);
		resetButton.setText("Reset");
		resetButton.addActionListener(new LimitOrderForm_resetButton_actionAdapter(this));
		submitButton.setText("Submit");
		submitButton.addActionListener(new LimitOrderForm_submitButton_actionAdapter(this));
		closeAllButton.addActionListener(new LimitOrderForm_closeAllButton_actionAdapter(this));
		exitButton.setText("Exit");
		exitButton.addActionListener(new LimitOrderUi_exitButton_actionAdapter(this));
		stopPriceEdit.setText("");
		stopPriceEdit.addFocusListener(new LimitOrderForm_stopSetPriceEdit_focusAdapter(this));
		orderTypeChoice.addItemListener(new LimitOrderForm_orderTypeChoice_actionAdapter(this));
		accountChoice.addItemListener(new LimitOrderForm_accountChoice_actionAdapter(this));
		isBuyChoice.addItemListener(new LimitOrderForm_isBuyChoice_actionAdapter(this));
		expireTimeChoice.addItemListener(new LimitOrderForm_expireTimeChoice_actionAdapter(this));
		priceEdit.addActionListener(new LimitOrderForm_setPriceEdit_actionAdapter(this));
		priceEdit.addFocusListener(new LimitOrderForm_setPriceEdit_focusAdapter(this));
		outstandingOrderTable.addKeyListener(new LimitOrderForm_outstandingOrderTable_keyAdapter(this));
		tradeOption2StaticText.setToolTipText("");
		tradeOption2StaticText.setText("Trade Option");
		tradeOption1StaticText.setText("Trade Option");
		stopSetPriceStaticText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 12));
		stopSetPriceStaticText.setToolTipText("");
		stopSetPriceStaticText.setText("Stop");

		ActionListener actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				rebind();
				if (limitCheckBox.isSelected())
				{
					boolean isOpen = getIsOpen();
					fillDefaultSetPrice(false);
				}

				updatePriceEditStatus();
				updateSubmitButtonStatus();
				updateOcoCheckBoxStatus();
				if(ocoCheckBox.isSelected())
				{
					fillDefaultSetPrice(false);
					fillDefaultStopSetPrice(false);
				}
				updateIfDoneCheckBoxStatus();
				updateIfDoneStatus();
			}
		};
		limitCheckBox.addActionListener(actionListener);
		limitCheckBox.setVisible(false);
		ocoCheckBox.setEnabled(false);
		ocoCheckBox.setVisible(false);

		this.closeLotTextField.setEnabled(false);
		this.closeLotTextField.setBackground(Color.LIGHT_GRAY);

		actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				rebind();
				if (stopCheckBox.isSelected())
				{
					boolean isOpen = getIsOpen();
					fillDefaultStopSetPrice(false);
				}

				updatePriceEditStatus();
				updateSubmitButtonStatus();
				updateOcoCheckBoxStatus();
				if(ocoCheckBox.isSelected())
				{
					fillDefaultSetPrice(false);
					fillDefaultStopSetPrice(false);
				}
				updateIfDoneCheckBoxStatus();
				updateIfDoneStatus();
			}
		};
		stopCheckBox.addActionListener(actionListener);
		stopCheckBox.setVisible(false);
		ocoCheckBox.setText(Language.OCOPrompt);
		ifDoneCheckBox.setText(Language.IfDonePrompt);
		instalmentCheckBox.setText(InstalmentLanguage.Instalment);
		instalmentButton.setText(InstalmentLanguage.Setup);
		paymentModeStaticText.setText(InstalmentLanguage.PaymentMode);
		advancePaymentCheckBox.setText(InstalmentLanguage.AdvancePayment);
		advancePaymentButton.setText(InstalmentLanguage.Setup);
		fullAmountCheckBox.setText(InstalmentLanguage.FullAmount);

		actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				_isOcoCheckBoxChangedManully = true;
			}
		};
		ocoCheckBox.addActionListener(actionListener);
		ocoCheckBox.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				if (ocoCheckBox.isSelected())
				{
					boolean isOpen = _makeOrderAccount.getSumLiqLots(getIsBuy()).compareTo(BigDecimal.ZERO) == 0;
					if (!isOpen)
					{
						ifDoneCheckBox.setSelected(false);
						setIfDoneVisible(false);
					}
				}
			}
		});

		actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ifDoneCheckBoxChanged();
			}
		};
		ifDoneCheckBox.addActionListener(actionListener);
		ifDoneCheckBox.setEnabled(false);
		ifDoneCheckBox.setVisible(false);

		if (this._instrument.isFromBursa())
		{
			JScrollPane bestBuyScrollPane = new JScrollPane(this.bestBuyTable);
			this.bestBuyTable.enableRowStripe();
			this.add(bestBuyScrollPane, new GridBagConstraints(0, 0, 8, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 0, 0), 200, 98));

			JScrollPane bestSellScrollPane = new JScrollPane(this.bestSellTable);
			this.bestSellTable.enableRowStripe();
			this.add(bestSellScrollPane, new GridBagConstraints(8, 0, 4, 1, 0.0, 0.0
				, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(5, 10, 0, 5), 0, 0));
		}
		else
		{
			this.add(bidButton, new GridBagConstraints2(0, 0, 5, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 80, 55));
			this.add(askButton, new GridBagConstraints2(5, 0, 3, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 0, 0), 80, 55));
		}
		this.add(this.instrumentQuoteDescription, new GridBagConstraints2(0, 3, 8, 1, 0.0, 0.0,
				GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 20));

		if(!StringHelper.isNullOrEmpty(this._instrument.get_QuoteDescription()))
		{
			this.instrumentQuoteDescription.setText(this._instrument.get_QuoteDescription());
		}
		else
		{
			this.instrumentQuoteDescription.setVisible(false);
		}

		/*this.add(plStaticText, new GridBagConstraints2(0, 0, 8, 1, 0.0, 0.0,
		 GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 5, 5, 5), 0, 0));*/
		this.add(totalLotStaticText, new GridBagConstraints(0, 7, 5, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 5), 0, 0));
		this.add(closeLotStaticText, new GridBagConstraints(0, 11, 5, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 1, 5), 0, 0));
		this.add(setPriceStaticText, new GridBagConstraints(0, 8, 5, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 10), 0, 0));
		this.add(isBuyStaticText, new GridBagConstraints(0, 6, 5, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 5), 0, 0));
		this.add(accountStaticText, new GridBagConstraints(0, 5, 5, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 5), 40, 0));
		this.add(orderTypeStaticText, new GridBagConstraints(0, 4, 5, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 1, 5), 0, 0));
		/*this.add(imstrumentDesciptionStaticText, new GridBagConstraints(0, 1, 4, 1, 0.0, 0.0
		 , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 5, 5, 5), 0, 0));*/

		scrollPane = new JScrollPane(outstandingOrderTable);
		outstandingOrderTable.enableRowStripe();
		this.add(expireTimeStaticText, new GridBagConstraints(0, 12, 5, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		ifLimitDonePanel.setLayout(new GridBagLayout());
		ifLimitDonePanel.add(limitPriceStaticTextForIfLimitDone, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 1, 2), 0, 0));
		ifLimitDonePanel.add(this.limitCheckBoxForIfLimitDone, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 1, 2), 0, 0));
		ifLimitDonePanel.add(limitPriceEditForIfLimitDone, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 1, 2), 50, 0));
		limitCheckBoxForIfLimitDone.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				limitCheckBoxForIfLimitDoneChanged();
			}
		});

		ifLimitDonePanel.add(stopPriceStaticTextForIfLimitDone, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 1, 2), 0, 0));
		ifLimitDonePanel.add(this.stopCheckBoxForIfLimitDone, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 1, 2), 0, 0));
		ifLimitDonePanel.add(stopPriceEditForIfLimitDone, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 1, 2), 50, 0));
		stopCheckBoxForIfLimitDone.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				stopCheckBoxForIfLimitDoneChanged();
			}
		});

		ifStopDonePanel.setLayout(new GridBagLayout());
		ifStopDonePanel.add(limitPriceStaticTextForIfStopDone, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 1, 2), 0, 0));
		ifStopDonePanel.add(this.limitCheckBoxForIfStopDone, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 1, 2), 0, 0));
		ifStopDonePanel.add(limitPriceEditForIfStopDone, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 1, 2), 50, 0));
		limitCheckBoxForIfStopDone.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				limitCheckBoxForIfStopDoneChanged();
			}
		});

		ifStopDonePanel.add(stopPriceStaticTextForIfStopDone, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 1, 2), 0, 0));
		ifStopDonePanel.add(this.stopCheckBoxForIfStopDone, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 1, 2), 0, 0));
		ifStopDonePanel.add(stopPriceEditForIfStopDone, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 1, 2), 50, 0));
		stopCheckBoxForIfStopDone.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				stopCheckBoxForIfStopDoneChanged();
			}
		});

		if (this._instrument.isFromBursa())
		{
			this.add(scrollPane, new GridBagConstraints(8, 2, 4, 18, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 10, 0, 5), 0, 0));

			JPanel panel = new JPanel();
			panel.setBackground(null);
			this.add(panel, new GridBagConstraints(8, 2, 4, 18, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 10, 0, 5), 0, 0));

			panel.setLayout(new GridBagLayout());
			panel.add(ifLimitDonePanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.5
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
			panel.add(ifStopDonePanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.5
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		}
		else
		{
			this.add(scrollPane, new GridBagConstraints(8, 0, 4, 18, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 10, 0, 5), 250, 0));

			JPanel panel = new JPanel();
			panel.setBackground(null);
			this.add(panel, new GridBagConstraints(8, 0, 4, 18, 1.0, 1.0
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 10, 0, 5), 40, 0));

			panel.setLayout(new GridBagLayout());
			panel.add(ifLimitDonePanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.5
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
			panel.add(ifStopDonePanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.5
				, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		}
		ifLimitDonePanel.setVisible(false);
		ifStopDonePanel.setVisible(false);

		instalmentCheckBox.setBackground(null);
		this.fullAmountCheckBox.setBackground(null);
		this.advancePaymentCheckBox.setBackground(null);
		/*this.add(this.instalmentCheckBox, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 6, 10, 0), 0, 0));*/
		ButtonGroup group = new ButtonGroup();
		group.add(this.fullAmountCheckBox);
		group.add(this.advancePaymentCheckBox);
		group.add(this.instalmentCheckBox);
		this.add(this.paymentModeStaticText, new GridBagConstraints(0, 14, 5, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 0), 0, 0));
		this.add(this.fullAmountCheckBox, new GridBagConstraints(0, 15, 5, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 0), 0, 0));
		this.add(this.advancePaymentCheckBox, new GridBagConstraints(0, 16, 5, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 0), 0, 0));
		this.add(this.advancePaymentButton, new GridBagConstraints(5, 16, 3, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		this.add(this.instalmentCheckBox, new GridBagConstraints(0, 17, 5, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 0), 0, 0));
		this.add(this.instalmentButton, new GridBagConstraints(5, 17, 3, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		this.add(submitButton, new GridBagConstraints(0, 18, 8, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 6, 10, 0), 0, 0));
		this.add(resetButton, new GridBagConstraints(8, 18, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 8, 10, 0), 0, 0));
		this.add(closeAllButton, new GridBagConstraints(9, 18, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 1, 10, 0), 0, 0));
		this.add(exitButton, new GridBagConstraints(11, 18, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 1, 10, 5), 0, 0));

		stopPriceEdit.getDocument().addDocumentListener(new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e)
			{
				updateIfDoneTitle();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateIfDoneTitle();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateIfDoneTitle();
			}
		});

		this.add(ocoCheckBox, new GridBagConstraints(5, 10, 2, 1, 0.5, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, -4, 1, 0), 0, 0));

		this.add(ifDoneCheckBox, new GridBagConstraints(7, 10, 1, 1, 0.5, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, -4, 1, 0), 0, 0));

		this.add(orderTypeChoice, new GridBagConstraints(5, 4, 3, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 1, 0), 0, 5));
		this.add(accountChoice, new GridBagConstraints(5, 5, 3, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 1, 0), 0, 5));
		this.add(isBuyChoice, new GridBagConstraints(5, 6, 3, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 1, 0), 0, 5));

		this.add(expireTimeChoice, new GridBagConstraints(5, 12, 3, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 1, 0), 0, 5));
		this.add(expireTimeDate, new GridBagConstraints(5, 13, 3, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 0, 0), 0, 5));

		this.add(totalLotTextField, new GridBagConstraints(5, 7, 3, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 1, 0), 0, 0));

		this.add(closeLotTextField, new GridBagConstraints(5, 11, 3, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 1, 0), 0, 0));
		closeLotTextField.getDocument().addDocumentListener(new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e)
			{
				updateIfDoneCheckBoxStatus();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateIfDoneCheckBoxStatus();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateIfDoneCheckBoxStatus();
			}
		});

		this.add(limitCheckBox, new GridBagConstraints(5, 8, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, -4, 1, 0), 0, 0));

		this.add(priceEdit, new GridBagConstraints(6, 8, 2, 1, 1.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 1, 2), 20, 0));

		this.add(stopCheckBox, new GridBagConstraints(5, 9, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, -4, 1, 0), 0, 0));
		this.add(stopPriceEdit, new GridBagConstraints(6, 9, 2, 1, 1.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 0, 1, 2), 20, 0));

		priceEdit.getDocument().addDocumentListener(new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e)
			{
				updateIfDoneTitle();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateIfDoneTitle();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateIfDoneTitle();
			}
		});
		/*this.add(tradeOption1StaticText, new GridBagConstraints(7, 5, 1, 1, 0.0, 0.0
		 , GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(1, 0, 1, 0), 0, 0));*/
		this.add(stopSetPriceStaticText, new GridBagConstraints(0, 9, 5, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 10), 0, 0));

		this.tradeOption1StaticText.setVisible(false);
		this.tradeOption2StaticText.setVisible(false);
	}

	//PVStaticText2 imstrumentDesciptionStaticText = new PVStaticText2();

	PVStaticText2 orderTypeStaticText = new PVStaticText2();
	PVStaticText2 accountStaticText = new PVStaticText2();
	PVStaticText2 isBuyStaticText = new PVStaticText2();
	PVStaticText2 setPriceStaticText = new PVStaticText2();
	PVStaticText2 totalLotStaticText = new PVStaticText2();
	PVStaticText2 closeLotStaticText = new PVStaticText2();
	PVStaticText2 expireTimeStaticText = new PVStaticText2();
	JAdvancedComboBox orderTypeChoice = new JAdvancedComboBox();
	JAdvancedComboBox accountChoice = new JAdvancedComboBox();
	JAdvancedComboBox isBuyChoice = new JAdvancedComboBox();
	PriceSpinner priceEdit;
	JScrollPane scrollPane;
	JFormattedTextFieldEx totalLotTextField = new JFormattedTextFieldEx(new DecimalFormat(), true);
	JFormattedTextFieldEx closeLotTextField = new JFormattedTextFieldEx(new DecimalFormat(), true);
	PVStaticText2 totalCloseLot = new PVStaticText2();
	PVStaticText2 totalCloseLotTitle = new PVStaticText2();
	JAdvancedComboBox expireTimeChoice = new JAdvancedComboBox();
	DataGrid outstandingOrderTable = new DataGrid("OutstandingOrderGrid");

	//MultiTextArea instrumentNarrative = new MultiTextArea();
	NoneResizeableTextField instrumentQuoteDescription = new NoneResizeableTextField();

	DataGrid bestBuyTable;
	DataGrid bestSellTable;

	PVButton2 resetButton = new PVButton2();
	PVButton2 submitButton = new PVButton2();
	PVButton2 closeAllButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();
	java.awt.GridBagLayout gridBagLayout1 = new GridBagLayout();
	ExpireTimeEditor expireTimeDate = new ExpireTimeEditor();
	PriceSpinner stopPriceEdit;
	PVStaticText2 stopSetPriceStaticText = new PVStaticText2();
	PVStaticText2 tradeOption1StaticText = new PVStaticText2();
	PVStaticText2 tradeOption2StaticText = new PVStaticText2();

	BidAskButton bidButton = new BidAskButton(true);
	BidAskButton askButton = new BidAskButton(false);

	JCheckBox limitCheckBox = new JCheckBox();
	JCheckBox stopCheckBox = new JCheckBox();

	ButtonGroup ocoIfDoneGroup = new ButtonGroup();
	JCheckBox ocoCheckBox = new JCheckBox();
	JCheckBox ifDoneCheckBox = new JCheckBox();

	JPanel ifLimitDonePanel;
	TitledBorder ifLimitDonePanelTitledBorder = BorderFactory.createTitledBorder("");
	PVStaticText2 limitPriceStaticTextForIfLimitDone = new PVStaticText2();
	PriceSpinner limitPriceEditForIfLimitDone;
	PVStaticText2 stopPriceStaticTextForIfLimitDone = new PVStaticText2();
	PriceSpinner stopPriceEditForIfLimitDone;
	JCheckBox limitCheckBoxForIfLimitDone = new JCheckBox();
	JCheckBox stopCheckBoxForIfLimitDone = new JCheckBox();

	JPanel ifStopDonePanel;
	TitledBorder ifStopDonePanelTitledBorder = BorderFactory.createTitledBorder("");
	PVStaticText2 limitPriceStaticTextForIfStopDone = new PVStaticText2();
	PriceSpinner limitPriceEditForIfStopDone;
	PVStaticText2 stopPriceStaticTextForIfStopDone = new PVStaticText2();
	PriceSpinner stopPriceEditForIfStopDone;
	JCheckBox limitCheckBoxForIfStopDone = new JCheckBox();
	JCheckBox stopCheckBoxForIfStopDone = new JCheckBox();

	PVStaticText2 paymentModeStaticText = new PVStaticText2();
	JCheckBox fullAmountCheckBox = new JCheckBox();
	JCheckBox advancePaymentCheckBox = new JCheckBox();
	PVButton2 advancePaymentButton = new PVButton2();
	JCheckBox instalmentCheckBox = new JCheckBox();
	PVButton2 instalmentButton = new PVButton2();
	InstalmentForm instalmentForm = null;
	InstalmentInfo instalmentInfo = null;

	private boolean _isOcoCheckBoxChangedManully = false;

	/*public void this_windowClosing(WindowEvent e)
	  {
	 this.dispose();
	  }*/

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.getFrame().dispose();
	}

	/*public void outstandingOrderTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	  {
	 this.processOutstandingOrderTable(e);
	  }*/

	public void resetButton_actionPerformed(ActionEvent e)
	{
		this.reset();
	}

	public void submitButton_actionPerformed(ActionEvent e)
	{
		this.submit();
	}

	public void closeAllButton_actionPerformed(ActionEvent e)
	{
		this.closeAll();
	}

	public void orderTypeChoice_actionPerformed(ItemEvent e)
	{
		this.orderTypeChoice_OnChange();
	}

	public void accountChoice_actionPerformed(ItemEvent e)
	{
		this.accountChoice_OnChange();
	}

	public void isBuyChoice_actionPerformed(ItemEvent e)
	{
		this.isBuyChoice_OnChange();
	}

	public void expireTimeChoice_actionPerformed(ItemEvent e)
	{
		this.expireTimeChoice_OnChange();
	}

	public void setPriceEdit_actionPerformed(ActionEvent e)
	{
	}

	public void setPriceEdit_focusLost(FocusEvent e)
	{
		boolean isPrompt = e.getOppositeComponent() != this.resetButton;
		this.setPrice_FocusLost(isPrompt, true);
	}

	public void stopSetPriceEdit_focusLost(FocusEvent e)
	{
		boolean isPrompt = e.getOppositeComponent() != this.resetButton;
		this.setPrice_FocusLost(isPrompt, false);
	}

	public void lotNumeric_focusLost(FocusEvent e)
	{
		if (e.getOppositeComponent() != this.resetButton)
		{
			this.lotNumeric_focusLost();
		}
	}

	public void lotNumeric_focusGained(FocusEvent e)
	{
		this.lotNumeric_focusGained();
	}

	public void setPriceEdit_focusGained(FocusEvent e)
	{
		this.setPriceEdit_focusGained();
	}

	public void stopSetPriceEdit_focusGained(FocusEvent e)
	{
		this.stopSetPriceEdit_focusGained();
	}

	public void outstandingOrderTable_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == e.VK_ENTER || e.getID() == e.VK_TAB)
		{
			this.outstandingOrderTable.requestFocus();
		}
	}

	private void updateIfDoneTitle()
	{
		String limitPrice = this.priceEdit.getText() == null ? "-" : this.priceEdit.getText();
		this.ifLimitDonePanelTitledBorder.setTitle(Language.IfDonePrompt + "(" + limitPrice + ")");
		String stopPrice = this.stopPriceEdit.getText() == null ? "-" : this.stopPriceEdit.getText();
		this.ifStopDonePanelTitledBorder.setTitle(Language.IfDonePrompt + "(" + stopPrice + ")");

		this.ifLimitDonePanel.updateUI();
		this.ifStopDonePanel.updateUI();
	}

	public void initializeOutstanding()
	{
		boolean isBuy = this.getIsBuy();
		this._makeOrderAccount.set_IsBuyForCurrent(isBuy);
		String totalLot = "";
		String closeLot = "";
		if (this._openCloseRelationSite.getTotalLotEditor() != null)
		{
			totalLot = this._openCloseRelationSite.getTotalLotEditor().getText();
		}
		if (this._openCloseRelationSite.getCloseLotEditor() != null)
		{
			closeLot = this._openCloseRelationSite.getCloseLotEditor().getText();
		}
		if (this.outstandingOrderTable.get_BindingSource() != null)
		{
			this.outstandingOrderTable.get_BindingSource().removeAll();
		}
		BuySellType buySellType = this._closeAllSell == null ? BuySellType.Both : (this._closeAllSell ? BuySellType.Sell : BuySellType.Buy);
		this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, buySellType,
			( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._openCloseRelationSite, this._order);
		this._lastAccountLot = BigDecimal.ZERO;
		this.updateOcoCheckBoxStatus();
		if (this._openCloseRelationSite.getTotalLotEditor() != null)
		{
			this._openCloseRelationSite.getTotalLotEditor().setText(totalLot);
		}
		if (this._openCloseRelationSite.getCloseLotEditor() != null)
		{
			this._openCloseRelationSite.getCloseLotEditor().setText(closeLot);
		}
		this.totalLotTextField.setText(this.getFormatDefaultLot());
		this.closeLotTextField.setText("");
		this.limitCheckBox.setSelected(false);
		this.stopCheckBox.setSelected(false);
		this.limitCheckBoxForIfLimitDone.setSelected(false);
		this.stopCheckBoxForIfLimitDone.setSelected(false);
		this.limitCheckBoxForIfStopDone.setSelected(false);
		this.stopCheckBoxForIfStopDone.setSelected(false);
		//this.submitButton.setEnabled(false);
		this.updateSubmitButtonStatus();
		this.priceEdit.setEnabled(false);
		this.stopPriceEdit.setEnabled(false);
		this.limitPriceEditForIfLimitDone.setEnabled(false);
		this.stopPriceEditForIfLimitDone.setEnabled(false);
		this.limitPriceEditForIfStopDone.setEnabled(false);
		this.stopPriceEditForIfStopDone.setEnabled(false);
	}

	/*class LimitOrderUi_this_windowAdapter extends WindowAdapter
	  {
	 private LimitOrderForm adaptee;
	 LimitOrderUi_this_windowAdapter(LimitOrderForm adaptee)
	 {
	  this.adaptee = adaptee;
	 }

	 public void windowClosing(WindowEvent e)
	 {
	  adaptee.this_windowClosing(e);
	 }
	  }*/

	private void addPlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
	{
		this._placeOrderTypeChangedListeners.add(placeOrderTypeChangedListener);
	}

	private void updateIfDoneCheckBoxStatus()
	{
		String closeLot = this.closeLotTextField.getText();
		if (!this.closeLotTextField.isVisible())
		{
			closeLot = "";
		}
		boolean isOpen = StringHelper.isNullOrEmpty(closeLot);
		if (this._makeOrderAccount != null)
		{
			isOpen = this._makeOrderAccount.getSumLiqLots(this.getIsBuy()).compareTo(BigDecimal.ZERO) <= 0;
		}

		boolean enabled = false;
		if (this.accountChoice.getSelectedValue() != null && isOpen)
		{
			MakeOrderAccount makeOrderAccount = (MakeOrderAccount)this.accountChoice.getSelectedValue();
			Guid tradePolicyId = makeOrderAccount.get_Account().get_TradePolicyId();
			TradePolicyDetail tradePolicyDetail =
				this._settingsManager.getTradePolicyDetail(tradePolicyId, this._instrument.get_Id());
			enabled = (this.limitCheckBox.isSelected() || this.stopCheckBox.isSelected())
				&& tradePolicyDetail.get_AllowIfDone();
		}
		this.ifDoneCheckBox.setVisible(enabled);
		this.ifDoneCheckBox.setEnabled(enabled);
		if (!enabled)
		{
			this.ifDoneCheckBox.setSelected(false);
			this.setIfDoneVisible(false);
		}
	}

	private void updateOcoCheckBoxStatus()
	{
		if (this._instrument.isFromBursa())
		{
			this.ocoCheckBox.setVisible(false);
			return;
		}

		Account account = this._makeOrderAccount.get_Account();
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), this._instrument.get_Id());

		boolean isOpen = this._makeOrderAccount.getSumLiqLots(this.getIsBuy()).compareTo(BigDecimal.ZERO) == 0;

		boolean canMakeOcoOrder = (isOpen ? tradePolicyDetail.get_AllowNewOCO() : MakeOrder.isAllowOrderType(this._instrument, OrderType.OneCancelOther))
			&& this.limitCheckBox.isSelected() && this.stopCheckBox.isSelected();

		if (!this._isOcoCheckBoxChangedManully || !canMakeOcoOrder)
		{
			this.ocoCheckBox.setSelected(canMakeOcoOrder);
		}
		if (this.ifDoneCheckBox.isSelected() && !isOpen)
		{
			this.ocoCheckBox.setSelected(false);
		}
		this.ocoCheckBox.setEnabled(canMakeOcoOrder);

		Object o = this.getOrderTypeValue();
		boolean showOcoCheckBox = o == null ? false : o.getClass() == MakeLimitStopOrder.class;
		boolean isVisible = this.ocoCheckBox.isVisible();
		this.ocoCheckBox.setVisible(showOcoCheckBox);
		if (isVisible != showOcoCheckBox)
		{
			this._isOcoCheckBoxChangedManully = false;
		}
	}

	private void updateSubmitButtonStatus()
	{
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(totalLotTextField.getText());
		Object o = this.getOrderTypeValue();
		if (o.getClass() == MakeLimitStopOrder.class)
		{
			submitButton.setEnabled(lot.compareTo(BigDecimal.ZERO) > 0 &&
									( (this.limitCheckBox.isSelected() && priceEdit.isEnabled())
									 || (this.stopCheckBox.isSelected() && stopPriceEdit.isEnabled())));
		}
		else
		{
			submitButton.setEnabled(lot.compareTo(BigDecimal.ZERO) > 0);
		}
	}

	private void removePlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
	{
		this._placeOrderTypeChangedListeners.remove(placeOrderTypeChangedListener);
	}

	private JTextField getCloseLotEditor()
	{
		if (this.outstandingOrderTable.getRowCount() > 0)
		{
			return this.closeLotTextField;
		}
		else
		{
			return this.totalLotTextField;
		}
	}

	private JTextField getTotalLotEditor()
	{
		return this.totalLotTextField;
	}

	private JDialog getFrame()
	{
		return this._residedWindow;
	}

	private Boolean isMakeLimitOrder()
	{
		if (this.limitCheckBox.isSelected() && this.stopCheckBox.isSelected())
		{
			return null;
		}
		else if (this.limitCheckBox.isSelected())
		{
			return true;
		}
		else if (this.stopCheckBox.isSelected())
		{
			return false;
		}
		else
		{
			return null;
		}
	}

	public void updateEditingValue()
	{
		this.outstandingOrderTable.updateEditingValue();
	}
}

class LimitOrderForm_lotNumeric_focusAdapter extends FocusAdapter
{
	private LimitOrderForm adaptee;
	LimitOrderForm_lotNumeric_focusAdapter(LimitOrderForm adaptee)
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

class LimitOrderForm_stopSetPriceEdit_focusAdapter extends FocusAdapter
{
	private LimitOrderForm adaptee;
	LimitOrderForm_stopSetPriceEdit_focusAdapter(LimitOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.stopSetPriceEdit_focusLost(e);
	}

	public void focusGained(FocusEvent e)
	{
		adaptee.stopSetPriceEdit_focusGained(e);
	}
}

class LimitOrderForm_setPriceEdit_focusAdapter extends FocusAdapter
{
	private LimitOrderForm adaptee;
	LimitOrderForm_setPriceEdit_focusAdapter(LimitOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.setPriceEdit_focusLost(e);
	}

	public void focusGained(FocusEvent e)
	{
		adaptee.setPriceEdit_focusGained(e);
	}
}

class LimitOrderForm_setPriceEdit_actionAdapter implements ActionListener
{
	private LimitOrderForm adaptee;
	LimitOrderForm_setPriceEdit_actionAdapter(LimitOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.setPriceEdit_actionPerformed(e);
	}
}

class LimitOrderForm_expireTimeChoice_actionAdapter implements ItemListener
{
	private LimitOrderForm adaptee;
	LimitOrderForm_expireTimeChoice_actionAdapter(LimitOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.expireTimeChoice_actionPerformed(e);
	}
}

class LimitOrderForm_isBuyChoice_actionAdapter implements ItemListener
{
	private LimitOrderForm adaptee;
	LimitOrderForm_isBuyChoice_actionAdapter(LimitOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.isBuyChoice_actionPerformed(e);
	}
}

class LimitOrderForm_orderTypeChoice_actionAdapter implements ItemListener
{
	private LimitOrderForm adaptee;
	LimitOrderForm_orderTypeChoice_actionAdapter(LimitOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.orderTypeChoice_actionPerformed(e);
	}
}

class LimitOrderForm_accountChoice_actionAdapter implements ItemListener
{
	private LimitOrderForm adaptee;
	LimitOrderForm_accountChoice_actionAdapter(LimitOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.accountChoice_actionPerformed(e);
	}
}

class LimitOrderForm_closeAllButton_actionAdapter implements ActionListener
{
	private LimitOrderForm adaptee;
	LimitOrderForm_closeAllButton_actionAdapter(LimitOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.closeAllButton_actionPerformed(e);
	}
}

class LimitOrderForm_submitButton_actionAdapter implements ActionListener
{
	private LimitOrderForm adaptee;
	LimitOrderForm_submitButton_actionAdapter(LimitOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}

class LimitOrderForm_resetButton_actionAdapter implements ActionListener
{
	private LimitOrderForm adaptee;
	LimitOrderForm_resetButton_actionAdapter(LimitOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.resetButton_actionPerformed(e);
	}
}

class LimitOrderUi_exitButton_actionAdapter implements ActionListener
{
	private LimitOrderForm adaptee;
	LimitOrderUi_exitButton_actionAdapter(LimitOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.exitButton_actionPerformed(e);
	}
}

/*class LimitOrderUi_outstandingOrderTable_actionAdapter implements IActionListener
 {
 private LimitOrderForm adaptee;
 LimitOrderUi_outstandingOrderTable_actionAdapter(LimitOrderForm adaptee)
 {
  this.adaptee = adaptee;
 }

 public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
 {
  adaptee.outstandingOrderTable_actionPerformed(e);
 }
 }*/

class LimitOrderForm_outstandingOrderTable_keyAdapter extends KeyAdapter
{
	private LimitOrderForm adaptee;
	LimitOrderForm_outstandingOrderTable_keyAdapter(LimitOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e)
	{
		adaptee.outstandingOrderTable_keyPressed(e);
	}
}
