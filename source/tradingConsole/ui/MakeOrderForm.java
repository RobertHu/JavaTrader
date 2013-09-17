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
import javax.swing.Timer;

import com.jidesoft.grid.TableColumnChooser;
import framework.*;
import framework.DateTime;
import framework.diagnostics.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.framework.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.columnKey.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import com.jidesoft.swing.JideTabbedPane;

public class MakeOrderForm extends FrameBase2
{
	private static Color _enabledBackground = Color.white;
	private static Color _enabledForeground = Color.BLACK;

	private static Color _disnabledBackground = Color.lightGray;
	private static Color _disnabledForeground = Color.gray;

	private OpenContractForm _openContractForm;

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private MakeOrderAccount _makeOrderAccount;
	private Instrument _instrument;
	private Order _order;
	private MakeLiquidationOrder _makeLiquidationOrder;
	private OperateSource _operateSource = OperateSource.Common;

	private Quote quote = Quote.Create();

	private MakeOrderOperatingObject _makeOrderOperatingObject;

	private DateTime _expireTime = null;
	private OpenCloseRelationSite _openCloseRelationSite;

	private OrderType _oldOrderType;
	private ArrayList<IPlaceOrderTypeChangedListener> _placeOrderTypeChangedListeners = new ArrayList<IPlaceOrderTypeChangedListener> ();

	//used for design ui
	private void uiInit()
	{
		this.orderTypeStaticText.setText("Type");
		this.accountStaticText.setText("Account");
		this.isBuyStaticText.setText("Buy/Sell");
		this.totalLotStaticText.setText("Lot");

		this.setPriceStaticText.setText("Limit");
		this.stopSetPriceStaticText.setText("Stop");
		this.tradeOption1StaticText.setText("BETTER");
		this.tradeOption2StaticText.setText("STOP");

		this.buyButton.setText("Buy");
		this.sellButton.setText("Sell");

		this.expireTimeStaticText.setText("Expire Time");
	}

	public MakeOrderForm()
	{
		super(null, null, null);
		try
		{
			jbInit();
			uiInit();
			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	private void initCaption()
	{
		this.setTitle(this._instrument.get_Description() + " " + this.getTitle());
		this.orderTypeStaticText.setText(Language.OrderLMTlblOrderTypeA);
		this.accountStaticText.setText(Language.OrderLMTlblAccountCodeA);
		this.isBuyStaticText.setText(Language.OrderLMTlblIsBuyA);
		this.setPriceStaticText.setText(Language.OrderLMTlblSetPriceA);
		this.stopSetPriceStaticText.setText(Language.OrderLMTlblSetPriceA2);
		this.tradeOption1StaticText.setText(TradeOption.getCaption(TradeOption.None));
		this.tradeOption1StaticText.setValue(TradeOption.None);
		this.tradeOption2StaticText.setText(TradeOption.getCaption(TradeOption.None));
		this.tradeOption2StaticText.setValue(TradeOption.None);
		this.totalLotStaticText.setText(Language.OrderLMTlblLot);
		this.closeLotStaticText.setText(Language.CloseLot);
		this.expireTimeStaticText.setText(Language.ExpireOrderPrompt);
		this.submitButton.setText(Language.OrderLMTbtnSubmit);
		this.multipleCloseButton.setText(Language.MultipleClose);
	}

	public MakeOrderForm(TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Order order,
						 MakeLiquidationOrder makeLiquidationOrder, OpenContractForm openContractForm,
						 OperateSource operateSource)
	{
		super(tradingConsole, settingsManager, instrument);

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._instrument = instrument;

		this._openContractForm = openContractForm;
		this._order = order;
		this._makeLiquidationOrder = makeLiquidationOrder;
		this._operateSource = operateSource;
		try
		{
			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}

		this.initCaption();

		//init structure

		if (this._openContractForm != null)
		{
			this._openContractForm.setCanClose(false);
		}

		this._makeOrderOperatingObject = (this._operateSource == OperateSource.Common) ? MakeOrderOperatingObject.None : MakeOrderOperatingObject.Other;

		this.setPriceEdit.setText("");
		this.stopSetPriceEdit.setText("");
		this.fillIsBuyChoice();
		this.fillOrderType();

		InstrumentPriceProvider instrumentPriceProvider = new InstrumentPriceProvider(this._instrument);
		this.sellButton.set_PriceProvider(instrumentPriceProvider);
		this.buyButton.set_PriceProvider(instrumentPriceProvider);

		if (this.orderTypeChoice.getItemCount() <= 1)
		{
			this.dispose();
			return;
		}

		if (this._operateSource == OperateSource.Common)
		{
			this.isBuyChoice.setSelectedIndex(0);
			this.orderTypeChoice.setSelectedIndex(0);
			this.fillAccount();
			if (this.accountChoice.getItemCount() > 0)
			{
				this.accountChoice.setSelectedIndex(0);
				this.accountChoice_OnChange();
			}
			MakeOrderAccount.initialize(this.outstandingOrderTable);

			this.fillExpireTime();
		}
		else
		{
			if (this._operateSource == OperateSource.LiquidationLMTSTP || this._operateSource == OperateSource.LiquidationSingleSPT)
			{
				if (this.orderTypeChoice.getItemCount() > 0)
				{
					this.orderTypeChoice.setSelectedIndex(1);
				}
				this.fillAccount();
				if (this.accountChoice.getItemCount() > 0)
				{
					this.accountChoice.setSelectedIndex(1);
					this.accountChoice_OnChange();
				}

				//this.outstandingOrderTable.suppressSelectedRowChangedEvent();
				this.isBuyChoice.setSelectedIndex( (!this._order.get_IsBuy()) ? 1 : 2);
				//this.outstandingOrderTable.resumeSelectedRowChangedEvent();

				this.changeColor(!this._order.get_IsBuy());

				this.orderTypeChoice_OnChange();

				if (this._operateSource == OperateSource.LiquidationLMTSTP)
				{
					this.setPriceStaticText.setText(Language.OrderLMTlblSetPriceA3);
					this.fillExpireTime();
				}

				if (this._operateSource == OperateSource.LiquidationSingleSPT)
				{
					this.quoteNotify(true);
				}
			}
			else if (this._operateSource == OperateSource.LiquidationMultiSPT)
			{
				if (this.orderTypeChoice.getItemCount() > 0)
				{
					this.orderTypeChoice.setSelectedIndex(1);
				}
				this.fillAccount();

				if (this.accountChoice.getItemCount() > 0)
				{
					this.accountChoice.setSelectedIndex(0);
				}
				this.isBuyChoice.setSelectedIndex(0);

				this._makeLiquidationOrder.initialize(this.outstandingOrderTable, true, false, new PropertyChangingListener(this), this._openCloseRelationSite,
					new PropertyChangedListener(this), false);
				int column = this.outstandingOrderTable.get_BindingSource().getColumnByName(OutstandingOrderColKey.IsSelected);
				this.outstandingOrderTable.sortColumn(column, false, false);
				this.outstandingOrderTable.setAutoResort(false);
				this.quoteNotify(true);
			}
		}

		this.setMessageArea();
		this.refreshPrice();
		this.setControlVisible();
		this.setControlEnable();
		this.updateButtonStatus();

		if (!isUnselectIsBuy())
		{
			boolean isBuy = this.getIsBuy();
			this.changeColor(isBuy);
		}

		this.setMakeOrderWindow();
		this.setNotifyIsAcceptWindow();
		this.refreshBuySellSubmitButtonStatus();
		this.expireTimeDate.initialize(this._settingsManager, this._instrument);
	}

	private void refreshBuySellSubmitButtonStatus()
	{
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(totalLotTextField.getText());

		if (this._operateSource == OperateSource.Common && this.isMakeSpotTradeOrder()) // && !this.isUnselectIsBuy())
		{
			this.submitButton.setVisible(false);
			this.multipleCloseButton.setVisible(false);
			if (lot.compareTo(BigDecimal.ZERO) == 0 || this.isBuyChoice.getSelectedValue() == null)
			{
				this.buyButton.setEnabled(false);
				this.sellButton.setEnabled(false);
				return;
			}

			boolean enable = lot.compareTo(BigDecimal.ZERO) > 0 && this.isBuyChoice.getSelectedValue() != null;
			enable = this._instrument.get_IsNormal() ? enable : !enable;

			if (this.getIsBuy())
			{
				this.buyButton.setEnabled(!enable);
				this.sellButton.setEnabled(enable);
			}
			else
			{
				this.sellButton.setEnabled(!enable);
				this.buyButton.setEnabled(enable);
			}
		}
		else
		{
			this.submitButton.setVisible(true);
			//this.multipleCloseButton.setVisible(true);
			this.buyButton.setEnabled(false);
			this.sellButton.setEnabled(false);

			if (this._operateSource == OperateSource.LiquidationMultiSPT)
			{
				boolean canSubmit = this._makeLiquidationOrder.getSumLiqLots(true).compareTo(BigDecimal.ZERO) > 0
					|| this._makeLiquidationOrder.getSumLiqLots(false).compareTo(BigDecimal.ZERO) > 0;
				this.submitButton.setEnabled(canSubmit);
			}
			else
			{
				Object o = this.getOrderTypeValue();
				if (o == null)
				{
					this.submitButton.setEnabled(false);
				}
				else if (o.getClass() == MakeLimitStopOrder.class)
				{
					submitButton.setEnabled(this.isBuyChoice.getSelectedValue() != null
											&& lot.compareTo(BigDecimal.ZERO) > 0 && (this.limitCheckBox.isSelected() || this.stopCheckBox.isSelected()));
				}
				else
				{
					submitButton.setEnabled(this.isBuyChoice.getSelectedValue() != null
											&& lot.compareTo(BigDecimal.ZERO) > 0);
				}
			}
		}
	}

	private void setControlEnable()
	{
		boolean enable = true;

		if (this._makeOrderOperatingObject == MakeOrderOperatingObject.None)
		{
			enable = true;

			this.orderTypeChoice.setEnabled(enable && this._order == null);

			enable = false;
			this.accountChoice.setEnabled(enable);

			this.isBuyChoice.setEnabled(enable);

			this.updateLotTextFieldsStatus(enable);
			this.setPriceEdit.setEnabled(enable);
			this.stopSetPriceEdit.setEnabled(enable);
			this.refreshBuySellSubmitButtonStatus();
		}
		else if (this._makeOrderOperatingObject == MakeOrderOperatingObject.OrderType)
		{
			enable = ! (this.orderTypeChoice.getSelectedIndex() == -1 || this.orderTypeChoice.getSelectedIndex() == 0);

			this.accountChoice.setEnabled(enable);

			OrderType orderType = this.getOrderType();

			enable = orderType == null ? false : !this.getOrderType().isSpot();

			this.isBuyChoice.setEnabled(enable);

			this.updateLotTextFieldsStatus(!enable);

			enable = orderType == null ? false : orderType != OrderType.Market;

			this.setPriceEdit.setEnabled(enable);

			enable = false;
			this.stopSetPriceEdit.setEnabled(enable);

			this.refreshBuySellSubmitButtonStatus();
		}
		else if (this._makeOrderOperatingObject == MakeOrderOperatingObject.Account)
		{
			enable = ! (this.accountChoice.getSelectedIndex() == -1 || this.accountChoice.getSelectedIndex() == 0);

			this.isBuyChoice.setEnabled(enable);

			enable = (enable) ? !this.isUnselectIsBuy() : enable;

			this.updateLotTextFieldsStatus(enable);

			enable = false;
			this.setPriceEdit.setEnabled(enable);

			this.stopSetPriceEdit.setEnabled(enable);
			this.refreshBuySellSubmitButtonStatus();
		}
		else if (this._makeOrderOperatingObject == MakeOrderOperatingObject.IsBuy)
		{
			enable = ! (this.isBuyChoice.getSelectedIndex() == -1 || this.isBuyChoice.getSelectedIndex() == 0);

			this.updateLotTextFieldsStatus(enable);

			enable = false;
			this.setPriceEdit.setEnabled(enable);

			this.stopSetPriceEdit.setEnabled(enable);

			this.refreshBuySellSubmitButtonStatus();
		}
		else if (this._makeOrderOperatingObject == MakeOrderOperatingObject.Lot)
		{
			BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
			enable = (lot.compareTo(BigDecimal.ZERO) > 0);

			this.refreshBuySellSubmitButtonStatus();

			if (this._operateSource == OperateSource.Common && this.isMakeSpotTradeOrder() && !this.isUnselectIsBuy())
			{
				boolean isBuy = Instrument.getSelectIsBuy(this._instrument, false);
				this.buyButton.setEnabled(enable && isBuy == this.getIsBuy());
				isBuy = Instrument.getSelectIsBuy(this._instrument, true);
				this.sellButton.setEnabled(enable && isBuy == this.getIsBuy());
			}

			if (this.isMakeSpotTradeOrder())
			{
				enable = false;
			}

			Object orderType = this.getOrderTypeValue();
			if (enable)
			{
				boolean enable2 = false;
				if (orderType == null)
				{
					this.setPriceEdit.setEnabled(enable);
					this.stopSetPriceEdit.setEnabled(enable);
				}
			}
			else
			{
				this.setPriceEdit.setEnabled(enable);
				this.stopSetPriceEdit.setEnabled(enable);
			}
		}
		else if (this._makeOrderOperatingObject == MakeOrderOperatingObject.Other)
		{
			enable = false;

			this.orderTypeChoice.setEnabled(enable && this._order == null);

			this.accountChoice.setEnabled(enable);

			this.isBuyChoice.setEnabled(enable);

			this.updateLotTextFieldsStatus(enable);

			enable = true;
			this.setPriceEdit.setEnabled(enable);
			this.stopSetPriceEdit.setEnabled(enable);

			this.refreshBuySellSubmitButtonStatus();
		}

		if (this._operateSource == OperateSource.LiquidationLMTSTP || this._operateSource == OperateSource.LiquidationSingleSPT)
		{
			enable = this._operateSource == OperateSource.LiquidationLMTSTP;

			this.orderTypeChoice.setEnabled(enable && this._order == null);
			enable = false;

			this.accountChoice.setEnabled(enable);
			this.isBuyChoice.setEnabled(enable);
			enable = true;
			this.updateLotTextFieldsStatus(enable);

			enable = this._operateSource == OperateSource.LiquidationLMTSTP;
			this.setPriceEdit.setEnabled(enable);
			this.stopSetPriceEdit.setEnabled(enable);

			this.refreshBuySellSubmitButtonStatus();
		}
		else if (this._operateSource == OperateSource.LiquidationMultiSPT)
		{
			enable = false;

			this.orderTypeChoice.setEnabled(enable && this._order == null);
			this.accountChoice.setEnabled(enable);
			this.isBuyChoice.setEnabled(enable);
			this.updateLotTextFieldsStatus(enable);
			this.setPriceEdit.setEnabled(enable);
			this.stopSetPriceEdit.setEnabled(enable);

			this.refreshBuySellSubmitButtonStatus();
		}

		if (this.accountChoice.getItemCount() == 2)
		{
			this.accountChoice.setEnabled(false);
		}
		this.updatePriceEditStatus();
	}

	private void updatePriceEditStatus()
	{
		Object o = this.getOrderTypeValue();
		if (o != null)
		{
			if (o.getClass() == MakeLimitStopOrder.class)
			{
				this.setPriceEdit.setEnabled(this.limitCheckBox.isSelected());
				this.stopSetPriceEdit.setEnabled(this.stopCheckBox.isSelected());
			}
		}
		else
		{
			this.setPriceEdit.setEnabled(false);
			this.stopSetPriceEdit.setEnabled(false);
		}
	}

	private void updateLotTextFieldsStatus(boolean enable)
	{
		if (this._operateSource == OperateSource.LiquidationSingleSPT
			|| this._operateSource == OperateSource.LiquidationLMTSTP)
		{
			this.totalLotTextField.setEnabled(false);
		}
		else
		{
			this.totalLotTextField.setEnabled(enable);
		}
		this.totalLotTextField.setBackground(this.totalLotTextField.isEnabled() ? MakeOrderForm._enabledBackground : MakeOrderForm._disnabledBackground);
		this.totalLotTextField.setForeground(this.totalLotTextField.isEnabled() ? MakeOrderForm._enabledForeground : MakeOrderForm._disnabledForeground);

		/*this.closeLotTextField.setEnabled(enable);
		   this.closeLotTextField.setBackground(enable ? MakeOrderForm._enabledBackground : MakeOrderForm._unenabledBackground);
		   this.closeLotTextField.setForeground(enable ? MakeOrderForm._enabledForeground : MakeOrderForm._unenabledForeground);*/
	}

	private void setMessageArea()
	{
		Object orderType = this.getOrderTypeValue();
		if (orderType == null)
		{
			this.messageTextArea.setMessage(Language.MakeOrderMessage);
		}
		else if (orderType.getClass() == MakeSpotTradeOrder.class
				 || orderType.getClass() == MakeLiquidationOrder.class)
		{
			this.messageTextArea.setMessage(Language.MakeOrderMessageSPT);
		}
		else if (orderType.getClass() == MakeLimitOrder.class || orderType.getClass() == MakeLimitStopOrder.class)
		{
			this.messageTextArea.setMessage(Language.MakeOrderMessageLMT);
		}
		else if (orderType.getClass() == MakeStopOrder.class)
		{
			this.messageTextArea.setMessage(Language.MakeOrderMessageSTP);
		}
		else if (orderType.getClass() == MakeMarketOrder.class
				 || orderType.getClass() == MakeMarketOnOpenOrder.class
				 || orderType.getClass() == MakeMarketOnCloseOrder.class)
		{
			this.messageTextArea.setMessage(Language.MakeOrderMessage);
		}
		else if (orderType.getClass() == MakeOneCancelOtherOrder.class)
		{
			this.messageTextArea.setMessage(Language.MakeOrderMessage);
		}
		this.messageTextArea.scrollToFirstLine();
	}

	private void setControlVisible()
	{
		Object orderType = this.getOrderTypeValue();

		this.orderTypeStaticText.setVisible(true);
		this.orderTypeChoice.setVisible(true);

		this.accountStaticText.setVisible(true);
		this.accountChoice.setVisible(true);

		this.isBuyStaticText.setVisible(true);
		this.isBuyChoice.setVisible(true);

		this.totalLotStaticText.setVisible(true);
		this.totalLotTextField.setVisible(true);

		this.outstandingOrderTable.setVisible(true);

		this.tradeOption1StaticText.setVisible(false);
		this.tradeOption2StaticText.setVisible(false);

		if (orderType == null
			|| orderType.getClass() == MakeSpotTradeOrder.class
			|| orderType.getClass() == MakeLiquidationOrder.class)
		{
			this.setPriceEdit.setEnabled(false);
			this.setPriceEdit.setText("");

			this.stopSetPriceEdit.setEnabled(false);
			this.stopSetPriceEdit.setText("");
			this.expireTimeVisible(false);
		}
		else if (orderType.getClass() == MakeLimitOrder.class)
		{
			boolean isUnselectIsBuy = this.isUnselectIsBuy();
			this.setPriceEdit.setEnabled(!isUnselectIsBuy);
			this.stopSetPriceEdit.setEnabled(false);
			this.stopSetPriceEdit.setText("");
			this.expireTimeVisible(true);
		}
		else if (orderType.getClass() == MakeStopOrder.class)
		{
			this.setPriceEdit.setEnabled(false);
			this.setPriceEdit.setText("");

			boolean isUnselectIsBuy = this.isUnselectIsBuy();
			this.stopSetPriceEdit.setEnabled(!this.isUnselectIsBuy());
			this.expireTimeVisible(true);
		}
		else if (orderType.getClass() == MakeMarketOrder.class
				 || orderType.getClass() == MakeMarketOnOpenOrder.class
				 || orderType.getClass() == MakeMarketOnCloseOrder.class)
		{
			this.setPriceEdit.setEnabled(false);
			this.setPriceEdit.setText("");
			this.stopSetPriceEdit.setEnabled(false);
			this.stopSetPriceEdit.setText("");
			this.expireTimeVisible(false);
		}
		else if (orderType.getClass() == MakeOneCancelOtherOrder.class
				 || orderType.getClass() == MakeLimitStopOrder.class)
		{
			this.setPriceEdit.setEnabled(this.limitCheckBox.isSelected());
			this.stopSetPriceEdit.setEnabled(this.stopCheckBox.isSelected());
			this.expireTimeVisible(true);
		}
		boolean showLimitStopCheckBox = orderType == null ? false : orderType.getClass() == MakeLimitStopOrder.class;
		this.limitCheckBox.setVisible(showLimitStopCheckBox);
		this.stopCheckBox.setVisible(showLimitStopCheckBox);
		if (!showLimitStopCheckBox)
		{
			this.limitCheckBox.setSelected(false);
			this.stopCheckBox.setSelected(false);
		}
		this.setPriceStaticText.setText(showLimitStopCheckBox ? Language.OrderLMTlblSetPriceA3 : Language.OrderLMTlblSetPriceA);

		this.updatePriceEditStatus();
	}

	private boolean isMakeSpotTradeOrder()
	{
		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			return false;
		}
		return (o.getClass() == MakeSpotTradeOrder.class || o.getClass() == MakeLiquidationOrder.class);
	}

	public void notifyMakeOrderUiByPrice()
	{
		this.refreshPrice();
	}

	@Override
	public void refreshPrice()
	{
		this.sellButton.updatePrice();
		this.buyButton.updatePrice();

		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			return;
		}

		if (o.getClass() == MakeSpotTradeOrder.class)
		{
			if (this._makeOrderAccount != null)
			{
				this._makeOrderAccount.set_BuySetPrice(this._instrument.get_LastQuotation().getBuy());
				this._makeOrderAccount.set_SellSetPrice(this._instrument.get_LastQuotation().getSell());
				this._makeOrderAccount.set_PriceInfo(this._instrument.get_LastQuotation().get_Timestamp(), this._instrument.get_LastQuotation().get_IsQuote());
			}
		}
		if (o.getClass() == MakeLiquidationOrder.class)
		{
			if (this._makeLiquidationOrder != null)
			{
				this._makeLiquidationOrder.update();
			}
		}
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
		this.isBuyChoice.addItem(Language.MakeOrderSelectBuySell, null);
		this.isBuyChoice.addItem(Language.LongBuy, true);
		this.isBuyChoice.addItem(Language.LongSell, false);
	}

	private void changeColor(boolean isBuy)
	{
		Color color = BuySellColor.getColor(isBuy, false);
		this.setPriceEdit.setForeground(color);
		this.stopSetPriceEdit.setForeground(color);
		this.totalLotTextField.setForeground(color);
		//special process,otherwise color will ineffective
		this.totalLotTextField.setText(this.totalLotTextField.getText());
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

	private void enableButton(boolean isPriceValidate)
	{
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		boolean enable = (lot.compareTo(BigDecimal.ZERO) > 0);

		this.refreshBuySellSubmitButtonStatus();

		enable = (isPriceValidate && enable);

		this.submitButton.setEnabled(enable);
		this.buyButton.setEnabled(false);
		this.sellButton.setEnabled(false);
	}

	private void setPrice_FocusLost(boolean isPrompt, boolean isLimitPrice)
	{
		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			this.enableButton(false);
			return;
		}

		if (o.getClass() == MakeLimitOrder.class
			|| o.getClass() == MakeStopOrder.class
			|| o.getClass() == MakeOneCancelOtherOrder.class || o.getClass() == MakeLimitStopOrder.class) //SetOneCancelOtherOrder
		{
			if (isLimitPrice && this.limitCheckBox.isSelected())
			{
				Price setPrice = Price.parse(this.setPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
				this.setPriceEdit.setText(Price.toString(setPrice));
				if (this.isBetweenBidToAsk(setPrice))
				{
					if (isPrompt)
					{
						AlertDialogForm.showDialog(this, null, true,
							"[" + Language.OrderLMTlblSetPriceA3 + "]" + Language.OrderLMTPageorderValidAlert8);
					}
					this.fillDefaultSetPrice();
					return;
				}
			}

			if (!isLimitPrice && this.stopCheckBox.isSelected())
			{
				Price setPrice2 = Price.parse(this.stopSetPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
				this.stopSetPriceEdit.setText(Price.toString(setPrice2));
				if (this.isBetweenBidToAsk(setPrice2))
				{
					if (isPrompt)
					{
						AlertDialogForm.showDialog(this, null, true,
							"[" + Language.OrderLMTlblSetPriceA2 + "]" + Language.OrderLMTPageorderValidAlert8);
					}
					this.fillDefaultStopSetPrice();
					return;
				}
			}

			SetPriceError[] setPriceErrors = this.changeTradeOptionValue(false);

			if (isLimitPrice && setPriceErrors[0] != SetPriceError.Ok)
			{
				this.handleSetPrieceError(setPriceErrors[0], isPrompt, Language.OrderLMTlblSetPriceA3);
				this.fillDefaultSetPrice();
			}
			else if (!isLimitPrice && setPriceErrors[1] != SetPriceError.Ok)
			{
				this.handleSetPrieceError(setPriceErrors[1], isPrompt, Language.OrderLMTlblSetPriceA2);
				this.fillDefaultStopSetPrice();
			}
		}
		this.enableButton(true);
	}

	private void handleSetPrieceError(SetPriceError setPriceError, boolean isPrompt, String priceName)
	{
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		Account account = this._makeOrderAccount == null ? null : this._makeOrderAccount.get_Account();

		if (setPriceError == SetPriceError.SetPriceTooCloseMarket)
		{
			if (isPrompt)
			{
				if (this._settingsManager.get_SystemParameter().get_DisplayLmtStopPoints())
				{
					boolean isBuy = (Boolean)this.isBuyChoice.getSelectedValue();
					AlertDialogForm.showDialog(this, null, true,
											   "[" + priceName + "] " + Language.OrderLMTPageorderValidAlert2 + " " + this._instrument.get_AcceptLmtVariation(account, isBuy, lot, null, getPlaceRelation(), false) +
											   " " +
											   Language.OrderLMTPageorderValidAlert22);
				}
				else
				{
					AlertDialogForm.showDialog(this, null, true, priceName + " " + Language.SetPriceTooCloseToMarket);
				}
			}
		}
		else if (setPriceError == SetPriceError.SetPriceTooFarAwayMarket)
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, "[" + priceName + "] " + Language.OrderLMTPageorderValidAlert3);
			}
		}
		else if (setPriceError == SetPriceError.InvalidSetPrice)
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, "[" + priceName + "] " + Language.InvalidSetPrice);
			}
		}
	}

	private HashMap<Guid, RelationOrder> getPlaceRelation()
	{
		return this._makeOrderAccount == null ? null : this._makeOrderAccount.getPlaceRelation();
	}

	private boolean isUnselectIsBuy()
	{
		return (this.isBuyChoice.getSelectedIndex() == -1
				|| this.isBuyChoice.getSelectedIndex() == 0);
	}

	private boolean getIsBuy()
	{
		return (Boolean)this.isBuyChoice.getSelectedValue();
	}

	//checkOpenClose
	private void isBuyChoice_OnChange()
	{
		if (this._makeOrderOperatingObject != MakeOrderOperatingObject.None && this._makeOrderOperatingObject != MakeOrderOperatingObject.Other)
		{
			this._makeOrderOperatingObject = MakeOrderOperatingObject.IsBuy;
			this.totalLotTextField.setText("");
			this.setControlEnable();
		}

		if (this.isUnselectIsBuy())
		{
			MakeOrderAccount.initialize(this.outstandingOrderTable);
			this.closeLotTextField.setText("");
			this.closeLotTextField.setEnabled(false);
			this.closeLotTextField.setBackground(MakeOrderForm._disnabledBackground);

			if (this.quote.get_IsQuoting())
			{
				this.cancelQuote();
			}
			return;
		}

		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			return;
		}

		this.setStatus();
		this.accountChoice_OnChange2();

		boolean isBuy = this.getIsBuy();
		this.changeColor(isBuy);
		BuySellType buySellType = (isBuy) ? BuySellType.Buy : BuySellType.Sell;
		if (o.getClass() == MakeSpotTradeOrder.class)
		{
			if (this._operateSource != OperateSource.LiquidationMultiSPT)
			{
				MakeSpotTradeOrder makeSpotTradeOrder = ( (MakeSpotTradeOrder)o);
				makeSpotTradeOrder.setDefaultBuySellType(buySellType);
			}
		}
		else if (o.getClass() == MakeOneCancelOtherOrder.class || o.getClass() == MakeLimitStopOrder.class) //SetLimitStop
		{
			if (o.getClass() == MakeOneCancelOtherOrder.class)
			{
				MakeOneCancelOtherOrder makeOneCancelOtherOrder = ( (MakeOneCancelOtherOrder)o);
				makeOneCancelOtherOrder.setDefaultBuySellType(buySellType);
			}
			else
			{
				( (MakeLimitStopOrder)o).setDefaultBuySellType(buySellType);
			}

			this.fillDefaultSetPrice();
			this.fillDefaultStopSetPrice();
		}
		else if (o.getClass() == MakeLimitOrder.class)
		{
			MakeLimitOrder makeLimitOrder = ( (MakeLimitOrder)o);
			makeLimitOrder.setDefaultBuySellType(buySellType);
		}
		else if (o.getClass() == MakeStopOrder.class)
		{
			MakeStopOrder makeStopOrder = ( (MakeStopOrder)o);
			makeStopOrder.setDefaultBuySellType(buySellType);
		}
		else if (o.getClass() == MakeMarketOnOpenOrder.class)
		{
			MakeMarketOnOpenOrder makeMarketOnOpenOrder = ( (MakeMarketOnOpenOrder)o);
			makeMarketOnOpenOrder.setDefaultBuySellType(buySellType);
		}
		else if (o.getClass() == MakeMarketOnCloseOrder.class)
		{
			MakeMarketOnCloseOrder makeMarketOnCloseOrder = ( (MakeMarketOnCloseOrder)o);
			makeMarketOnCloseOrder.setDefaultBuySellType(buySellType);
		}
		else if (o.getClass() == MakeMarketOrder.class)
		{
			MakeMarketOrder makeMarketOrder = ( (MakeMarketOrder)o);
			makeMarketOrder.setDefaultBuySellType(buySellType);
		}
		else if (o.getClass() == MakeOneCancelOtherOrder.class || o.getClass() == MakeLimitStopOrder.class)
		{
			MakeOneCancelOtherOrder makeOneCancelOtherOrder = ( (MakeOneCancelOtherOrder)o);
			makeOneCancelOtherOrder.setDefaultBuySellType(buySellType);
		}

		if (this._makeOrderAccount.get_IsBuyForCurrent() != isBuy)
		{
			this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both,
				( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._openCloseRelationSite, this._order);
			this._lastAccountLot = BigDecimal.ZERO;
			this.updateOcoCheckBoxStatus();
		}
		if (this._makeOrderAccount != null)
		{
			if (isBuy)
			{
				this._makeOrderAccount.set_BuyLot(BigDecimal.ZERO);
			}
			else
			{
				this._makeOrderAccount.set_SellLot(BigDecimal.ZERO);
			}
			this.totalLotTextField.setText("");
			boolean isSpot = this.isMakeSpotOrder();
			this._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, isSpot, this.isMakeLimitOrder());
			this._makeOrderAccount.set_IsBuyForCurrent(isBuy);

			this.quoteNotify(true);
		}
		this.fillDefaultLot();
	}

	private boolean isMakeSpotOrder()
	{
		Object o = this.getOrderTypeValue();
		return o.getClass() == MakeSpotTradeOrder.class
			|| o.getClass() == MakeMarketOrder.class || o.getClass() == MakeLiquidationOrder.class;
	}

	private Boolean isMakeLimitOrder()
	{
		return null;
	}

	private void fillOrderType()
	{
		this.orderTypeChoice.disableItemEvent();
		this.reallyFillOrderType();
		this.orderTypeChoice.enableItemEvent();
	}

	private void reallyFillOrderType()
	{
		boolean allowLimitStop = false;

		this.orderTypeChoice.removeAllItems();

		this.orderTypeChoice.addItem(Language.MakeOrderSelectOrderType, null);
		Object[] result = null;

		if (this._operateSource != OperateSource.LiquidationSingleSPT
			&& this._operateSource != OperateSource.LiquidationMultiSPT)
		{
			result = MakeOrder.isAllowMakeLimitOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				allowLimitStop = true;
			}
			else
			{
				allowLimitStop = false;
			}
		}

		if (this._operateSource != OperateSource.LiquidationSingleSPT
			&& this._operateSource != OperateSource.LiquidationMultiSPT
			&& this._operateSource != OperateSource.LiquidationLMTSTP)
		{
			result = MakeOrder.isAllowMakeMarketOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				MakeMarketOrder makeMarketOrder = (MakeMarketOrder)result[2];
				this.orderTypeChoice.addItem(Language.MKTPrompt, makeMarketOrder);
			}

			result = MakeOrder.isAllowMakeMarketOnCloseOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				MakeMarketOnCloseOrder makeMarketOnCloseOrder = (MakeMarketOnCloseOrder)result[2];
				this.orderTypeChoice.addItem(Language.MOCPrompt, makeMarketOnCloseOrder);
			}

			result = MakeOrder.isAllowMakeMarketOnOpenOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				MakeMarketOnOpenOrder makeMarketOnOpenOrder = (MakeMarketOnOpenOrder)result[2];
				this.orderTypeChoice.addItem(Language.MOOPrompt, makeMarketOnOpenOrder);
			}

			if (this._order != null)
			{
				result = MakeOrder.isAllowMakeOneCancelOtherOrder(this._tradingConsole, this._settingsManager, this._instrument,
					(this._order == null) ? null : this._order.get_Transaction().get_Account());
				if ( (Boolean)result[0])
				{
					MakeOneCancelOtherOrder makeOneCancelOtherOrder = (MakeOneCancelOtherOrder)result[2];
					this.orderTypeChoice.addItem(Language.OCOPrompt, makeOneCancelOtherOrder);
				}
			}
		}

		if (this._operateSource != OperateSource.LiquidationLMTSTP)
		{
			if (this._operateSource == OperateSource.LiquidationMultiSPT)
			{
				this.orderTypeChoice.addItem(Language.SPTPrompt, this._makeLiquidationOrder);
			}
			else
			{
				result = MakeOrder.isAllowMakeSpotTradeOrder(this._tradingConsole, this._settingsManager, this._instrument);
				if ( (Boolean)result[0])
				{
					MakeSpotTradeOrder makeSpotTradeOrder = (MakeSpotTradeOrder)result[2];
					this.orderTypeChoice.addItem(Language.SPTPrompt, makeSpotTradeOrder);
				}
			}
		}

		if (this._operateSource != OperateSource.LiquidationSingleSPT
			&& this._operateSource != OperateSource.LiquidationMultiSPT)
		{
			result = MakeOrder.isAllowMakeStopOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				allowLimitStop = true;
			}
			else
			{
				allowLimitStop = false;
			}
		}

		if (allowLimitStop)
		{
			result = MakeOrder.isAllowMakeLimitStopOrder(this._tradingConsole, this._settingsManager, this._instrument,
				(this._order == null) ? null : this._order.get_Transaction().get_Account());
			if ( (Boolean)result[0])
			{
				MakeLimitStopOrder makeLimitStopOrder = (MakeLimitStopOrder)result[2];
				this.orderTypeChoice.addItem(Language.LMTPrompt + "/" + Language.STPPrompt, makeLimitStopOrder);
			}
		}
	}

	private Price getMarketPrice()
	{
		if (this.isUnselectIsBuy())
		{
			return null;
		}
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
			setPriceErrors[0] = this.changeTradeOptionValue2(isSubmit, marketPrice, this.setPriceEdit, this.tradeOption1StaticText);
		}

		if (o.getClass() == MakeStopOrder.class
			|| o.getClass() == MakeOneCancelOtherOrder.class
			|| (o.getClass() == MakeLimitStopOrder.class && this.stopCheckBox.isSelected()))
		{
			setPriceErrors[1] = this.changeTradeOptionValue2(isSubmit, marketPrice, this.stopSetPriceEdit, this.tradeOption2StaticText);
		}
		return setPriceErrors;
	}

	private SetPriceError changeTradeOptionValue2(boolean isSubmit, Price marketPrice, JTextField setpriceControl, PVStaticText2 tradeOptionControl)
	{
		SetPriceError setPriceError = SetPriceError.Ok;
		if (this.isBuyChoice.getSelectedValue() == null)
		{
			return SetPriceError.InvalidSetPrice;
		}
		boolean isBuy = (Boolean)this.isBuyChoice.getSelectedValue();

		Price setPrice = Price.parse(setpriceControl.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		if (setPrice == null || marketPrice == null)
		{
			return setPriceError;
		}

		TradeOption previousTradeOption = (TradeOption)tradeOptionControl.getValue();
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		Account account = this._makeOrderAccount == null ? null : this._makeOrderAccount.get_Account();
		setPriceError = Order.checkLMTOrderSetPrice(account,true, this._instrument, isBuy, previousTradeOption, setPrice, marketPrice, lot, null, this.getPlaceRelation(), false);
		if (setPriceError != SetPriceError.Ok)
		{
			return setPriceError;
		}

		double dblMarketPrice = Price.toDouble(marketPrice);
		if (Math.abs(Price.toDouble(setPrice) - dblMarketPrice) > dblMarketPrice * 0.2)
		{
			setPriceError = SetPriceError.SetPriceTooFarAwayMarket;
		}

		return setPriceError;
	}

	private boolean getIsOpen()
	{
		return this._makeOrderAccount == null ? true : this._makeOrderAccount.getSumLiqLots().compareTo(BigDecimal.ZERO) == 0;
	}

	private void fillDefaultSetPrice()
	{
		if (this.isUnselectIsBuy())
		{
			return;
		}
		boolean isBuy = this.getIsBuy();
		Price bid = this._instrument.get_LastQuotation().get_Bid();
		Price ask = this._instrument.get_LastQuotation().get_Ask();
		if (bid == null || ask == null)
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderSingleDQPageorderValidAlert0);
			return;
		}
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		Account account = this._makeOrderAccount == null ? null : this._makeOrderAccount.get_Account();
		int acceptLmtVariation = this._instrument.get_AcceptLmtVariation(account, isBuy, lot, null, getPlaceRelation(), false);
		//Fill Limit Pric(
		Price price = (this._instrument.get_IsNormal() == isBuy) ? Price.subStract(ask, acceptLmtVariation) : Price.add(bid, acceptLmtVariation);
		if (this.isBetweenBidToAsk(price))
		{
			price = (this._instrument.get_IsNormal() == isBuy) ? Price.subStract(bid, this._instrument.get_NumeratorUnit()) :
				Price.add(ask, this._instrument.get_NumeratorUnit());
		}
		this.setPriceEdit.setText(Price.toString(price));
		this.tradeOption1StaticText.setText(TradeOption.getCaption(TradeOption.Better));
		this.tradeOption1StaticText.setValue(TradeOption.Better);
		this.tradeOption1StaticText.setForeground(TradeOption.getColor(TradeOption.Better));
	}

	private void fillDefaultStopSetPrice()
	{
		if (this.isUnselectIsBuy())
		{
			return;
		}
		boolean isBuy = this.getIsBuy();
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		Account account = this._makeOrderAccount == null ? null : this._makeOrderAccount.get_Account();
		Price bid = this._instrument.get_LastQuotation().get_Bid();
		Price ask = this._instrument.get_LastQuotation().get_Ask();
		if (bid == null || ask == null)
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderSingleDQPageorderValidAlert0);
			return;
		}
		int acceptLmtVariation = this._instrument.get_AcceptLmtVariation(account, isBuy, lot, null, getPlaceRelation(), false);
		//Fill Stop Price
		Price price2 = (this._instrument.get_IsNormal() == isBuy) ? Price.add(ask, acceptLmtVariation) : Price.subStract(bid, acceptLmtVariation);
		if (this.isBetweenBidToAsk(price2))
		{
			price2 = (this._instrument.get_IsNormal() == isBuy) ? Price.add(ask, this._instrument.get_NumeratorUnit()) :
				Price.subStract(bid, this._instrument.get_NumeratorUnit());
		}
		this.stopSetPriceEdit.setText(Price.toString(price2));
		this.tradeOption2StaticText.setText(TradeOption.getCaption(TradeOption.Stop));
		this.tradeOption2StaticText.setValue(TradeOption.Stop);
		this.tradeOption2StaticText.setForeground(TradeOption.getColor(TradeOption.Stop));
	}

	private BigDecimal getDefaultLot()
	{
		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			BigDecimal lastPlaceLot = PalceLotNnemonic.getLastPlaceLot(this._instrument.get_Id(), this._makeOrderAccount.get_Account().get_Id());
			return lastPlaceLot != null ? lastPlaceLot : BigDecimal.ZERO;
		}

		if (o instanceof MakeOneCancelOtherOrder)
		{
			return this._order.get_LotBalance();
		}
		else
		{
			if (this._order != null && this.outstandingOrderTable.get_BindingSource() != null)
			{
				Object relationOrder = this.outstandingOrderTable.get_BindingSource().getObject(0);
				if (relationOrder != null)
				{
					return ( (RelationOrder)relationOrder).get_LiqLot();
				}
			}

			Account account = this._makeOrderAccount.get_Account();
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());
			boolean isOpen = this._makeOrderAccount.getSumLiqLots(true).compareTo(BigDecimal.ZERO) <= 0
				&& this._makeOrderAccount.getSumLiqLots(false).compareTo(BigDecimal.ZERO) <= 0;

			return AppToolkit.getDefaultLot(this._instrument, isOpen, tradePolicyDetail, this._makeOrderAccount);
		}
	}

	private void fillDefaultLot()
	{
		if (this._makeOrderAccount != null)
		{
			BigDecimal defaultLot = this.getDefaultLot();
			this.totalLotTextField.setText(AppToolkit.getFormatLot(defaultLot, this._makeOrderAccount.get_Account(), this._instrument));
		}
		else
		{
			this.totalLotTextField.setText("");
		}
		this.refreshBuySellSubmitButtonStatus();
	}

	private void fillDefaultValueForOutstandingOrder(boolean isSelectedRelationOrder)
	{
		if (isSelectedRelationOrder)
		{
			boolean isBuy = this.getIsBuy();
			BuySellType buySellType = (isBuy) ? BuySellType.Buy : BuySellType.Sell;
			this._makeOrderAccount.setDefaultLiqLotForOutStanding(buySellType, isSelectedRelationOrder, this.getOrderType().isSpot(), this.isMakeLimitOrder());
			int column = this.outstandingOrderTable.get_BindingSource().getColumnByName(OutstandingOrderColKey.IsSelected);
			TableColumnChooser.hideColumn(this.outstandingOrderTable, column);
		}
		else
		{
			this._makeOrderAccount.reset(isSelectedRelationOrder, this.isMakeLimitOrder());
		}
		int column = this.outstandingOrderTable.get_BindingSource().getColumnByName(OutstandingOrderColKey.IsBuy);
		TableColumnChooser.hideColumn(this.outstandingOrderTable, column);
	}

	private Object getOrderTypeValue()
	{
		if (this.orderTypeChoice.getSelectedIndex() == -1 || this.orderTypeChoice.getSelectedIndex() == 0)
		{
			return null;
		}
		return this.orderTypeChoice.getSelectedValue();
	}

	private void fillExpireTime()
	{
		this.fillExpireTimeChoice();
	}

	private void expireTimeVisible(boolean isVisible)
	{
		this.expireTimeStaticText.setVisible(isVisible);
		this.expireTimeChoice.setVisible(isVisible);
		this.expireTimeDate.setVisible(isVisible);
		if (isVisible)
		{
			if (this.expireTimeChoice.getSelectedIndex() != -1)
			{
				String caption = (String)this.expireTimeChoice.getSelectedText();
				if (caption.equalsIgnoreCase(Language.GoodTillDate))
				{
					this.expireTimeDate.setVisible(true);
				}
				else
				{
					this.expireTimeDate.setVisible(false);
				}
			}
			else
			{
				this.expireTimeDate.setVisible(false);
			}
		}
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

	private void orderTypeChoice_OnChange()
	{
		this.updateOcoCheckBoxStatus();

		if (this._makeOrderOperatingObject != MakeOrderOperatingObject.Other)
		{
			this._makeOrderOperatingObject = MakeOrderOperatingObject.OrderType;
			this.totalLotTextField.setText("");
			this.setControlEnable();
		}

		if (this._operateSource == OperateSource.LiquidationLMTSTP)
		{
			if (this.orderTypeChoice.getSelectedIndex() == -1 || this.orderTypeChoice.getSelectedIndex() == 0)
			{
				this.orderTypeChoice.setSelectedIndex(1);
			}
		}
		this.setMessageArea();

		boolean isMakeSpotTradeOrder = this.isMakeSpotTradeOrder();

		if (isMakeSpotTradeOrder)
		{
			super.outTimeSchedulerStart();
		}
		else
		{
			this.echoSchedulerStop();
			super.outTimeSchedulerStop();
		}

		if (this.quote.get_IsQuoting())
		{
			if (!isMakeSpotTradeOrder)
			{
				this.cancelQuote();
			}
			else
			{
				this.quoteNotify(true);
			}
		}

		this.fillAccount();
		if (this.orderTypeChoice.getSelectedIndex() != 0 && this.accountChoice.getItemCount() > 0)
		{
			this.accountChoice.setSelectedIndex( (this._order != null || this.accountChoice.getItemCount() == 2) ? 1 : 0);
			this.accountChoice_OnChange();
		}
		else
		{
			this.isBuyChoice.setSelectedIndex(0);
			this.isBuyChoice_OnChange();
			this.setStatus();
			return;
		}

		this.setControlVisible();
		this.setStatus();

		OrderType newOrderType = this.getOrderType();
		for (IPlaceOrderTypeChangedListener placeOrderTypeChangedListener : this._placeOrderTypeChangedListeners)
		{
			placeOrderTypeChangedListener.OrderTypeChanged(newOrderType, this._oldOrderType);
		}
		this._oldOrderType = newOrderType;
		this.fillDefaultLot();
	}

	private void setStatus()
	{
		/*boolean disableOutstandingTable = this._operateSource == OperateSource.LiquidationLMTSTP
		 || this._operateSource == OperateSource.LiquidationMultiSPT || this._operateSource == OperateSource.LiquidationSingleSPT;
		   this.outstandingOrderTable.setEditable(!disableOutstandingTable);*/
		if (this.accountChoice.getSelectedIndex() == -1 || this.accountChoice.getSelectedIndex() == 0)
		{
			if (this.outstandingOrderTable.get_BindingSource() != null)
			{
				this.outstandingOrderTable.get_BindingSource().removeAll();
			}
			this.submitButton.setEnabled(false);
			this.buyButton.setEnabled(false);
			this.sellButton.setEnabled(false);
			return;
		}

		this._makeOrderAccount = (MakeOrderAccount)this.accountChoice.getSelectedValue();
		if (this._makeOrderAccount == null)
		{
			return;
		}

		//init outstanding Order Grid
		if (this.isUnselectIsBuy())
		{
			return;
		}

		boolean isBuy = this.getIsBuy();
		this.changeColor(isBuy);
		this._makeOrderAccount.set_IsBuyForCurrent(isBuy);
		this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both,
			( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._openCloseRelationSite, this._order);
		this.updateCloseLotVisible();
		this._lastAccountLot = BigDecimal.ZERO;
		this.updateOcoCheckBoxStatus();

		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			return;
		}

		if (this._order != null)
		{
			this.fillDefaultValueForOutstandingOrder(true);

			if (o.getClass() == MakeLimitStopOrder.class)
			{
				this.totalLotTextField.setText(AppToolkit.getFormatLot(this._makeOrderAccount.getSumLiqLots(isBuy), this._makeOrderAccount.get_Account(),
					this._instrument));
				this.setPriceEdit.setEnabled(false);
				this.stopSetPriceEdit.setEnabled(false);
				this.submitButton.setEnabled(false);
			}
		}
		if (this._order != null)
		{
			this.totalLotTextField.setText(AppToolkit.getFormatLot(this._makeOrderAccount.getSumLiqLots(isBuy), this._makeOrderAccount.get_Account(),
				this._instrument));
			this.outstandingOrderTable.changeSelection(0, 0, false, false);
		}

		this.limitCheckBox.setVisible(o.getClass() == MakeLimitStopOrder.class);
		this.stopCheckBox.setVisible(o.getClass() == MakeLimitStopOrder.class);
	}

	private void updateCloseLotVisible()
	{
		this.closeLotTextField.setVisible(this.outstandingOrderTable.getRowCount() > 0);
		this.closeLotStaticText.setVisible(this.outstandingOrderTable.getRowCount() > 0);
	}

	private MakeOrderAccount getMakeOrderAccount(Guid accountId)
	{
		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			return null;
		}

		if (o.getClass() == MakeSpotTradeOrder.class)
		{
			if (this._operateSource == OperateSource.LiquidationMultiSPT)
			{ //???
				return ( (MakeLiquidationOrder)o).get_MakeOrderAccount(accountId);
			}
			else
			{
				return ( (MakeSpotTradeOrder)o).get_MakeOrderAccount(accountId);
			}
		}
		else if (o.getClass() == MakeLimitOrder.class)
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
		return null;
	}

	private HashMap<Guid, MakeOrderAccount> getMakeOrderAccounts()
	{
		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			return null;
		}

		if (o.getClass() == MakeSpotTradeOrder.class)
		{
			if (this._operateSource == OperateSource.LiquidationMultiSPT)
			{
				return ( (MakeLiquidationOrder)o).get_MakeOrderAccounts();
			}
			else
			{
				return ( (MakeSpotTradeOrder)o).get_MakeOrderAccounts();
			}
		}
		else if (o.getClass() == MakeLimitOrder.class)
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
		else if (o.getClass() == MakeLimitStopOrder.class)
		{
			return ( (MakeLimitStopOrder)o).get_MakeOrderAccounts();
		}
		else if (o.getClass() == MakeLiquidationOrder.class)
		{
			return ( (MakeLiquidationOrder)o).get_MakeOrderAccounts();
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
			if (this.getMakeOrderAccounts() == null)
			{
				this.accountChoice.insertItemAt(Language.MakeOrderSelectAccount, 0);
				return;
			}
			else
			{
				for (Iterator<MakeOrderAccount> iterator = this.getMakeOrderAccounts().values().iterator(); iterator.hasNext(); )
				{
					MakeOrderAccount makeOrderAccount = iterator.next();
					this.accountChoice.addItem(makeOrderAccount.get_Account().get_Code(), makeOrderAccount);
				}
			}
		}
		this.accountChoice.insertItemAt(Language.MakeOrderSelectAccount, 0);
	}

	private void accountChoice_OnChange()
	{
		MakeOrderAccount.initialize(this.outstandingOrderTable);

		if (this._operateSource != OperateSource.LiquidationLMTSTP
			&& this._operateSource != OperateSource.LiquidationSingleSPT)
		{
			this.isBuyChoice.setSelectedIndex(0);
		}

		if (this.isUnselectIsBuy())
		{
			if (this.quote.get_IsQuoting())
			{
				this.cancelQuote();
			}
		}
		this.accountChoice_OnChange2();

		if (this._order != null)
		{
			if (this._makeOrderAccount != null)
			{
				this.totalLotTextField.setText(AppToolkit.getFormatLot(this._makeOrderAccount.getSumLiqLots(!this._order.get_IsBuy()),
					this._makeOrderAccount.get_Account(), this._instrument));
			}
		}
		this.lotNumericValidation();
		this.fillExpireTime();
		this.fillDefaultLot();
	}

	private void clearForAccountChanged()
	{
		this.closeLotTextField.setText("");
		this.setPriceEdit.setText("");
		this.stopSetPriceEdit.setText("");
		if (this._makeOrderAccount != null)
		{
			this._makeOrderAccount.unbindOutstanding();
		}
		if (this.outstandingOrderTable.get_BindingSource() != null)
		{
			this.outstandingOrderTable.get_BindingSource().removeAll();
		}
		this.limitCheckBox.setSelected(false);
		this.stopCheckBox.setSelected(false);
	}

	private void accountChoice_OnChange2()
	{
		if (this._makeOrderOperatingObject != MakeOrderOperatingObject.None && this._makeOrderOperatingObject != MakeOrderOperatingObject.Other)
		{
			this._makeOrderOperatingObject = MakeOrderOperatingObject.Account;

			this.totalLotTextField.setText("");
			this.setControlEnable();
		}

		if (this.accountChoice.getSelectedIndex() == -1 || this.accountChoice.getSelectedIndex() == 0)
		{
			this.clearForAccountChanged();
			return;
		}

		if (this.isUnselectIsBuy())
		{
			this.clearForAccountChanged();
			this._makeOrderAccount = (MakeOrderAccount)this.accountChoice.getSelectedValue();
		}
		else
		{
			MakeOrderAccount makeOrderAccount = (MakeOrderAccount)this.accountChoice.getSelectedValue();
			boolean isBuy = makeOrderAccount.get_IsBuyForCurrent();
			if (! (this._makeOrderAccount.get_Account().get_Id().equals(makeOrderAccount.get_Account().get_Id())
				   && this._makeOrderAccount.get_IsBuyForCurrent() == isBuy))
			{
				this._makeOrderAccount = makeOrderAccount;
				this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both,
					( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._openCloseRelationSite, this._order);
				this.updateCloseLotVisible();
				this._lastAccountLot = BigDecimal.ZERO;
				this.updateOcoCheckBoxStatus();
			}
		}
	}

	private void expireTimeChoice_OnChange()
	{
		DateTime date = null;
		if (this.expireTimeChoice.getSelectedIndex() != -1)
		{
			date = (DateTime)this.expireTimeChoice.getSelectedValue();
			String caption = (String)this.expireTimeChoice.getSelectedText();
			if (caption.equalsIgnoreCase(Language.GoodTillDate))
			{
				this.expireTimeDate.setVisible(true);
			}
			this.expireTimeVisible(true);
		}
		else
		{
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
		this._expireTime = null;
		this.expireTimeChoice.removeAllItems();
		if (this._makeOrderAccount == null)
		{
			return;
		}

		DateTime tradeDay = this._settingsManager.get_TradeDay().get_TradeDay();
		TimeSpan timePart = this._settingsManager.get_TradeDay().get_BeginTime().get_TimeOfDay();
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
			this._instrument.get_Id());
		if (tradePolicyDetail.get_GoodTillMonthDayOrder())
		{
			DateTime dayOrderDateTime = this._instrument.get_DayCloseTime();
			if (dayOrderDateTime == null || dayOrderDateTime == DateTime.maxValue)
			{
				dayOrderDateTime = tradeDay.addDays(1).get_Date().add(timePart);
			}
			this.expireTimeChoice.addItem(Language.GoodTillMonthDayOrder, dayOrderDateTime);
		}

		if (tradePolicyDetail.get_GoodTillMonthSession())
		{
			DateTime sessionDateTime = this._instrument.get_CloseTime();
			if (sessionDateTime == null || sessionDateTime == DateTime.maxValue)
			{
				sessionDateTime = tradeDay.addDays(1).get_Date().add(timePart);
			}
			this.expireTimeChoice.addItem(Language.GoodTillMonthSession, sessionDateTime);
		}

		if (tradePolicyDetail.get_GoodTillMonthGTM())
		{
			DateTime addOneMonthTradeDay = tradeDay.addMonths(1);
			DateTime gtmDateTime = addOneMonthTradeDay.addDays( -1).add(timePart);
			this.expireTimeChoice.addItem(Language.GoodTillMonthGTM, gtmDateTime);
		}

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
			String caption = (String)this.expireTimeChoice.getSelectedText();
			if (caption.equalsIgnoreCase(Language.GoodTillDate))
			{
				this.expireTimeDate.setVisible(true);
			}
			this._expireTime = date;

			this.doLayout();
		}
	}

	private void quoteNotify(boolean hasCondition)
	{
		if (!this.isMakeSpotTradeOrder())
		{
			return;
		}

		BigDecimal buyLot = BigDecimal.ZERO;
		BigDecimal sellLot = BigDecimal.ZERO;
		if (this._operateSource == OperateSource.LiquidationMultiSPT)
		{
			buyLot = this._makeLiquidationOrder.getSumLiqLots(true);
			sellLot = this._makeLiquidationOrder.getSumLiqLots(false);
		}
		else
		{
			if (this.isUnselectIsBuy())
			{
				return;
			}
			boolean isBuy = this.getIsBuy();
			BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
			if (lot.compareTo(BigDecimal.ZERO) == 0)
			{
				lot = this.getDefaultLot();
			}
			if (isBuy)
			{
				buyLot = lot;
			}
			else
			{
				sellLot = lot;
			}
		}

		boolean canQuote = false;
		if (!hasCondition)
		{
			canQuote = true;
		}
		else
		{
			if (this.quote.get_IsQuoting())
			{
				if (this.quote.get_BuyLot().compareTo(buyLot) != 0 || this.quote.get_SellLot().compareTo(sellLot) != 0)
				{
					canQuote = true;
				}
			}
			else
			{
				canQuote = true;
			}
		}
		if (canQuote)
		{
			int tick = this._settingsManager.get_Customer().get_DQOrderOutTime();
			if (tick <= 0)
			{
				tick = 60;
			}
			tick += 2;

			super.echoSchedulerStart();

			this.quote.set_IsQuoting(true);
			this.quote.set_BuyLot(buyLot);
			this.quote.set_SellLot(sellLot);

			this.quote2(this.quote.get_BuyLot(), this.quote.get_SellLot(), tick);
		}
	}

	protected void echo()
	{
		this.quoteNotify(false);
	}

	public void cancelQuoteArrived()
	{
		super.cancelQuoteArrived();
		this.quote.set_IsQuoting(false);
	}

	public void quoteArrived2()
	{
		super.quoteArrived2();
	}

	public void cancelQuote()
	{
		this.echoSchedulerStop();
		super.cancelQuote(this.quote.get_BuyLot(), this.quote.get_SellLot());
	}

	private void lotNumeric_keyReleased()
	{
		//this.lotNumericValidation2();
		this._makeOrderOperatingObject = MakeOrderOperatingObject.Lot;
		this.setControlEnable();
		this.quoteNotify(true);
	}

	private void lotNumeric_keyPressed()
	{
	}

	public void lotNumeric_focusLost()
	{
		this.lotNumericValidation2();
		/*
		   this._makeOrderOperatingObject = MakeOrderOperatingObject.Lot;
		   this.setControlEnable();
		   this.quoteNotify(true);*/
	}

	private void lotNumericValidation2()
	{
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if (this._makeOrderAccount != null)
		{
			boolean isOpen = this._makeOrderAccount.getSumLiqLots(true).compareTo(BigDecimal.ZERO) <= 0
				&& this._makeOrderAccount.getSumLiqLots(false).compareTo(BigDecimal.ZERO) <= 0;
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());
			BigDecimal lot2 = AppToolkit.fixLot(lot, isOpen, tradePolicyDetail, this._makeOrderAccount);
			if (lot.compareTo(lot2) != 0)
			{
				String formattedLot = AppToolkit.getFormatLot(lot2, this._makeOrderAccount.get_Account(), this._instrument);
				String info = StringHelper.format(Language.LotIsNotValidAndWillChangeTo, new Object[]
												  {lot, formattedLot});
				AlertDialogForm.showDialog(this, null, true, info);
				lot = lot2;
				this.totalLotTextField.setText(AppToolkit.getFormatLot(lot, this._makeOrderAccount.get_Account(), this._instrument));
			}
		}

		if (lot.compareTo(BigDecimal.ZERO) <= 0)
		{
			return;
		}
		else
		{
			BigDecimal maxLot;
			if (this.isMakeSpotTradeOrder())
			{
				maxLot = this._instrument.get_MaxDQLot();
			}
			else
			{
				maxLot = this._instrument.get_MaxOtherLot();
			}
			if (maxLot.compareTo(BigDecimal.ZERO) != 0
				&& lot.compareTo(maxLot) > 0)
			{
				this.totalLotTextField.setText("");
				return;
			}
		}
		//input lot < sumLiqLots, clear liqLots....
		if (this.isUnselectIsBuy())
		{
			return;
		}
		boolean isBuy = this.getIsBuy();
		BigDecimal closeLot = this._makeOrderAccount.getSumLiqLots(isBuy);
		if (lot.compareTo(closeLot) < 0)
		{
			if (this._operateSource == OperateSource.LiquidationLMTSTP
				|| this._operateSource == OperateSource.LiquidationMultiSPT
				|| this._operateSource == OperateSource.LiquidationSingleSPT)
			{
				this.totalLotTextField.setText(AppToolkit.getFormatLot(closeLot, this._makeOrderAccount.get_Account(), this._instrument));
			}
			else
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderSingleDQPagetextLot_OnblurAlert3);
				this._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, this.isMakeSpotOrder(), this.isMakeLimitOrder());
			}
		}
	}

	//input Lot validation
	private void lotNumericValidation()
	{
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());

		if (this._makeOrderAccount != null)
		{
			boolean isOpen = this._makeOrderAccount.getSumLiqLots(true).compareTo(BigDecimal.ZERO) <= 0
				&& this._makeOrderAccount.getSumLiqLots(false).compareTo(BigDecimal.ZERO) <= 0;
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());
			lot = AppToolkit.fixLot(lot, isOpen, tradePolicyDetail, this._makeOrderAccount);
			this.totalLotTextField.setText(AppToolkit.getFormatLot(lot, this._makeOrderAccount.get_Account(), this._instrument));
		}

		if (lot.compareTo(BigDecimal.ZERO) <= 0)
		{
			this.totalLotTextField.setText("");
			return;
		}
		else
		{
			BigDecimal maxLot;
			if (this.isMakeSpotTradeOrder())
			{
				maxLot = this._instrument.get_MaxDQLot();
			}
			else
			{
				maxLot = this._instrument.get_MaxOtherLot();
			}
			if (maxLot.compareTo(BigDecimal.ZERO) != 0
				&& lot.compareTo(maxLot) > 0)
			{
				TradingConsole.traceSource.trace(TraceType.Information, "lot= " + lot.toString() + "; maxLot = " + maxLot.toString()
												 + new FrameworkException("").getStackTrace());

				AlertDialogForm.showDialog(this, null, true,
										   Language.OrderLMTPagetextLot_OnblurAlert0 + "(" +
										   AppToolkit.getFormatLot(maxLot, this._makeOrderAccount.get_Account(), this._instrument) +
										   ")!");
				this.totalLotTextField.setText("");
				return;
			}
			else
			{
				try
				{
					this.totalLotTextField.setText(AppToolkit.getFormatLot(lot, this._makeOrderAccount.get_Account(), this._instrument));
				}
				catch (IllegalStateException exception)
				{
				}
			}
		}
		//input lot < sumLiqLots, clear liqLots....
		if (this.isUnselectIsBuy())
		{
			return;
		}
		boolean isBuy = this.getIsBuy();
		if (lot.compareTo(this._makeOrderAccount.getSumLiqLots(isBuy)) < 0)
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderSingleDQPagetextLot_OnblurAlert3);
			this._makeOrderAccount.clearOutStandingTable(isBuy ? BuySellType.Buy : BuySellType.Sell, this.isMakeSpotOrder(), this.isMakeLimitOrder());
		}
	}

	private void removePlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
	{
		this._placeOrderTypeChangedListeners.remove(placeOrderTypeChangedListener);
	}

	private BigDecimal _lastAccountLot = BigDecimal.ZERO;
	public void updateAccount(BigDecimal accountLot, boolean openOrderIsBuy)
	{
		this.updateOcoCheckBoxStatus();

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
		if (o.getClass() == MakeLimitStopOrder.class && this.ocoCheckBox.isSelected())
		{
			accountLot2 = accountLot;
		}

		if (accountLot2.compareTo(BigDecimal.ZERO) <= 0)
		{
			accountLot2 = this.getDefaultLot();
		}
		if (this.closeLotTextField.isVisible())
		{
			this.totalLotTextField.setEnabled(accountLot.compareTo(BigDecimal.ZERO) <= 0);
			this.totalLotTextField.setText(AppToolkit.getFormatLot(accountLot, this._makeOrderAccount.get_Account(), this._instrument));
		}
		this.lotNumeric_keyReleased();
		this.refreshBuySellSubmitButtonStatus();
	}

	private void updateOcoCheckBoxStatus()
	{
		boolean canMakeOcoOrder = MakeOrder.isAllowOrderType(this._instrument, OrderType.OneCancelOther)
			&& limitCheckBox.isSelected() && stopCheckBox.isSelected() && !this.isUnselectIsBuy()
			&& _makeOrderAccount.getSumLiqLots(getIsBuy()).compareTo(BigDecimal.ZERO) > 0;

		this.ocoCheckBox.setSelected(canMakeOcoOrder);
		this.ocoCheckBox.setEnabled(canMakeOcoOrder);

		Object o = this.getOrderTypeValue();
		boolean showOcoCheckBox = o == null ? false : o.getClass() == MakeLimitStopOrder.class;
		this.ocoCheckBox.setVisible(showOcoCheckBox);
	}

	static class OpenCloseRelationSite implements IOpenCloseRelationSite
	{
		private MakeOrderForm _owner;

		public OpenCloseRelationSite(MakeOrderForm owner)
		{
			this._owner = owner;
		}

		public OperateType getOperateType()
		{
			return this._owner.getOperateType();
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
			return this._owner.getLot();
		}

		public PVStaticText2 getLiqLotValueStaticText()
		{
			return null;
		}

		public BigDecimal getTotalQuantity()
		{
			return AppToolkit.convertStringToBigDecimal(this._owner.totalLotTextField.getText());
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
			this._owner.rebind();
		}
	}

	static class PropertyChangedListener implements IPropertyChangedListener
	{
		private MakeOrderForm _owner;

		public PropertyChangedListener(MakeOrderForm owner)
		{
			this._owner = owner;
		}

		public void propertyChanged(Object owner, PropertyDescriptor propertyDescriptor, Object oldValue, Object newValue, int row, int column)
		{
			if (propertyDescriptor.get_Name().equals(MakeOrderLiquidationGridColKey.IsSelected))
			{
				this._owner.updateButtonStatus();

				RelationOrder relationOrder = (RelationOrder)owner;
				this._owner.closeLotTextField.setEditable(relationOrder.get_IsSelected());
				this._owner.closeLotTextField.setBackground(relationOrder.get_IsSelected() ? Color.WHITE : Color.LIGHT_GRAY);
				if (relationOrder.get_IsSelected())
				{
					this._owner.closeLotTextField.setText(relationOrder.get_LiqLotString());
				}
				else
				{
					this._owner.closeLotTextField.setText("");
				}
			}
		}
	}

	private void updateButtonStatus()
	{
		BindingSource bindingSource = this.outstandingOrderTable.get_BindingSource();
		boolean hasSelectedOrder = false;
		boolean hasOppositeSelectedOrder = false;
		boolean isContractSizeDifferent = false;

		TradePolicyDetail tradePolicyDetail = null;
		Boolean lastBuy = null;
		BigDecimal lastContractSize = null;
		ArrayList<Account> involvedAccounts = new ArrayList<Account> ();
		if (bindingSource != null)
		{
			for (int index = 0; index < bindingSource.getRowCount(); index++)
			{
				RelationOrder relationOrder = (RelationOrder)bindingSource.getObject(index);
				if (relationOrder.get_IsSelected())
				{
					if (tradePolicyDetail == null)
					{
						Guid tradePolictyId = relationOrder.get_OpenOrder().get_Account().get_TradePolicyId();
						tradePolicyDetail = this._settingsManager.getTradePolicyDetail(tradePolictyId, this._instrument.get_Id());
					}
					if (!involvedAccounts.contains(relationOrder.get_OpenOrder().get_Account()))
					{
						involvedAccounts.add(relationOrder.get_OpenOrder().get_Account());
					}

					hasSelectedOrder = true;
					if (lastBuy == null)
					{
						lastBuy = relationOrder.get_IsBuy();
					}
					else if (lastBuy.booleanValue() != relationOrder.get_IsBuy())
					{
						hasOppositeSelectedOrder = true;
					}

					if (lastContractSize == null)
					{
						lastContractSize = relationOrder.get_OpenOrder().get_Transaction().get_ContractSize();
					}
					else if (lastContractSize.compareTo(relationOrder.get_OpenOrder().get_Transaction().get_ContractSize()) != 0)
					{
						isContractSizeDifferent = true;
					}
				}
			}
		}
		boolean canSubmit = this.submitButton.isEnabled();
		this.submitButton.setEnabled(canSubmit && hasSelectedOrder);

		this.multipleCloseButton.setVisible(tradePolicyDetail != null && tradePolicyDetail.get_MultipleCloseAllowed());
		this.multipleCloseButton.setEnabled(canSubmit && !isContractSizeDifferent && hasOppositeSelectedOrder && involvedAccounts.size() == 1);
	}

	static class PropertyChangingListener implements IPropertyChangingListener
	{
		private MakeOrderForm _owner;

		public PropertyChangingListener(MakeOrderForm owner)
		{
			this._owner = owner;
		}

		public void propertyChanging(PropertyChangingEvent e)
		{
			DataGrid table = this._owner.outstandingOrderTable;
			RelationOrder relationOrder = (RelationOrder)e.get_Owner();
			Order order = relationOrder.get_OpenOrder();
			table.setSelectionForeground(BuySellColor.getColor(!order.get_IsBuy(), false));

			if (e.get_PropertyDescriptor().get_Name().equals(MakeOrderLiquidationGridColKey.IsSelected))
			{
				if ( ( (Boolean)e.get_NewValue()).booleanValue() == false)
				{
					BindingSource bindingSource = this._owner.outstandingOrderTable.get_BindingSource();
					int selectedCount = 0;
					if (bindingSource != null)
					{
						for (int index = 0; index < bindingSource.getRowCount(); index++)
						{
							RelationOrder relationOrder2 = (RelationOrder)bindingSource.getObject(index);
							if (relationOrder2.get_IsSelected())
							{
								selectedCount++;
							}
						}
					}
					e.set_Cancel(selectedCount == 1);
				}
			}
			else if (e.get_PropertyDescriptor().get_Name().equals(MakeOrderLiquidationGridColKey.LiqLotString))
			{
				BigDecimal newValue = AppToolkit.convertStringToBigDecimal(e.get_NewValue().toString());
				if (newValue.compareTo(BigDecimal.ZERO) == 0 && !StringHelper.isNullOrEmpty(e.get_NewValue().toString()))
				{
					e.set_Cancel(true);
					return;
				}

				BigDecimal newValue2 = AppToolkit.convertStringToBigDecimal(AppToolkit.getFormatLot(newValue, order.get_Account(), order.get_Instrument()));
				if (newValue2.compareTo(BigDecimal.ZERO) == 0 || newValue2.compareTo(newValue) != 0)
				{
					e.set_Cancel(true);
					return;
				}

				if (newValue.compareTo(order.getAvailableLotBanlance(this._owner.getOrderType().isSpot(), null)) > 0)
				{
					AlertDialogForm.showDialog(this._owner, null, true,
											   Language.OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert0);
					e.set_Cancel(true);
					return;
				}

				//new value can not > maxDQLot
				BigDecimal maxDQLot = this._owner._instrument.get_MaxDQLot();
				if (maxDQLot.compareTo(BigDecimal.ZERO) != 0 && newValue.compareTo(maxDQLot) > 0)
				{
					AlertDialogForm.showDialog(this._owner, null, true,
											   Language.OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert1);
					e.set_Cancel(true);
					return;
				}

				//refresh Open Order
				relationOrder.set_LiqLotString(AppToolkit.getFormatLot(newValue, order.get_Account(), order.get_Instrument()));
				this._owner._makeLiquidationOrder.update();
				this._owner.quoteNotify(true);
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
		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			return false;
		}

		if (o.getClass() == MakeLimitOrder.class
			|| o.getClass() == MakeOneCancelOtherOrder.class || o.getClass() == MakeLimitStopOrder.class)
		{
			TradeOption tradeOption1 = (TradeOption)this.tradeOption1StaticText.getValue();
			if (!tradeOption1.equals(TradeOption.Better))
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this, null, true, this.getLMTSTOPSetPriceValidationPrompt(true));
				}
				return isValidOrder;
			}
		}

		if (o.getClass() == MakeStopOrder.class
			|| o.getClass() == MakeOneCancelOtherOrder.class || o.getClass() == MakeLimitStopOrder.class)
		{
			TradeOption tradeOption2 = (TradeOption)this.tradeOption2StaticText.getValue();
			if (!tradeOption2.equals(TradeOption.Stop))
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this, null, true, this.getLMTSTOPSetPriceValidationPrompt(false));
				}
				return isValidOrder;
			}
		}

		if (o.getClass() == MakeOneCancelOtherOrder.class || o.getClass() == MakeLimitStopOrder.class)
		{
			TradeOption tradeOption1 = (TradeOption)this.tradeOption1StaticText.getValue();
			TradeOption tradeOption2 = (TradeOption)this.tradeOption2StaticText.getValue();
			if (tradeOption1 == tradeOption2)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this, null, true, Language.OrderOCOPageorderValidAlert3);
				}
				return isValidOrder;
			}
		}
		return true;
	}

	private boolean isValidOrderSpotTrade(boolean isPrompt)
	{
		if (!MakeOrder.isAllowOrderType(this._instrument, this.getOrderType()))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
			}
			return false;
		}

		boolean isBuy = this.getIsBuy();
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if (lot.compareTo(BigDecimal.ZERO) <= 0)
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OperateOrderMethod1PageSubmitAlert0);
			}
			this.resetData();
			return false;
		}
		BigDecimal liqLots = this._makeOrderAccount.getSumLiqLots(isBuy);

		//for Instrument code with "#"// & isOpen=true
		boolean isHasMakeNewOrder = lot.compareTo(liqLots) > 0;
		boolean isAcceptLot = this._makeOrderAccount.isAcceptLot(isBuy, lot, isHasMakeNewOrder); // openLot);
		if (!isAcceptLot)
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, Language.NewOrderAcceptedHedging);
			}
			return false;
		}

		Object o = this.getOrderTypeValue();
		Object[] result = null;
		if (this._operateSource == OperateSource.LiquidationMultiSPT)
		{
			MakeLiquidationOrder makeLiquidationOrder = (MakeLiquidationOrder)o;
			makeLiquidationOrder.isAcceptTime();
		}
		else
		{
			MakeSpotTradeOrder makeSpotTradeOrder = (MakeSpotTradeOrder)o;
			result = makeSpotTradeOrder.isAcceptTime();
		}
		if (! (Boolean)result[0])
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, result[1].toString());
			}
			return false;
		}
		if (liqLots.compareTo(BigDecimal.ZERO) > 0)
		{
			if (lot.compareTo(liqLots) != 0)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert7);

					TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
						this._instrument.get_Id());
					liqLots = AppToolkit.fixLot(liqLots, false, tradePolicyDetail, this._makeOrderAccount);
					this.totalLotTextField.setText(AppToolkit.getFormatLot(liqLots, this._makeOrderAccount.get_Account(), this._instrument));
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

		return true;
	}

	private boolean isValidOrderOther(boolean isPrompt)
	{
		boolean isValidOrder = false;

		if (!MakeOrder.isAllowOrderType(this._instrument, this.getOrderType()))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
			}
			return isValidOrder;
		}

		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if (lot.compareTo(BigDecimal.ZERO) <= 0)
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert6);
			}
			return isValidOrder;
		}

		boolean isBuy = this.getIsBuy();
		BigDecimal liqLots = this._makeOrderAccount.getSumLiqLots(isBuy);
		Object o = this.getOrderTypeValue();
		if (o.getClass() == MakeOneCancelOtherOrder.class
			|| (o.getClass() == MakeLimitStopOrder.class && this.ocoCheckBox.isSelected()))
		{
			if (liqLots.compareTo(BigDecimal.ZERO) <= 0 || lot.compareTo(liqLots) != 0)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert7);

					TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
						this._instrument.get_Id());
					liqLots = AppToolkit.fixLot(liqLots, false, tradePolicyDetail, this._makeOrderAccount);
					this.totalLotTextField.setText(AppToolkit.getFormatLot(liqLots, this._makeOrderAccount.get_Account(), this._instrument));
				}
				return isValidOrder;
			}
		}
		BigDecimal[] sumLot = this._makeOrderAccount.getSumLotBSForOpenOrder();
		BigDecimal sumBuyLots = sumLot[0];
		BigDecimal sumSellLots = sumLot[1];

		boolean isHasMakeNewOrder = lot.subtract(liqLots).compareTo(BigDecimal.ZERO) > 0;
		if (!this._makeOrderAccount.isAcceptLot(isBuy, lot, isHasMakeNewOrder)) //new BigDecimal( ( (Double) (lot - liqLots)).doubleValue())))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, Language.NewOrderAcceptedHedging);
			}
			return isValidOrder;
		}

		Price setPrice = Price.parse(this.setPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		Price setPrice2 = Price.parse(this.stopSetPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		TradePolicyDetail tradePolicyDetail = this._makeOrderAccount.getTradePolicyDetail();
		if (o.getClass() == MakeMarketOnOpenOrder.class
			|| o.getClass() == MakeMarketOnCloseOrder.class)
		{
			if (liqLots.compareTo(BigDecimal.ZERO) <= 0 && !tradePolicyDetail.get_IsAcceptNewMOOMOC())
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this, null, true, Language.NewOrderAcceptedHedging2);
				}
				return isValidOrder;
			}
		}
		else if (o.getClass() == MakeLimitOrder.class
				 || o.getClass() == MakeStopOrder.class
				 || o.getClass() == MakeOneCancelOtherOrder.class || o.getClass() == MakeLimitStopOrder.class) //SetLimitStop
		{
			if (o.getClass() == MakeLimitOrder.class
				|| o.getClass() == MakeOneCancelOtherOrder.class || o.getClass() == MakeLimitStopOrder.class)
			{
				if (setPrice == null)
				{
					if (isPrompt)
					{
						AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert1);
					}
					return isValidOrder;
				}
				this.setPriceEdit.setText(Price.toString(setPrice));
				if (this.isBetweenBidToAsk(setPrice))
				{
					if (isPrompt)
					{
						AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert8);
					}
					this.fillDefaultSetPrice();
					return isValidOrder;
				}
			}
			if (o.getClass() == MakeStopOrder.class
				|| o.getClass() == MakeOneCancelOtherOrder.class || o.getClass() == MakeLimitStopOrder.class)
			{
				if (setPrice2 == null)
				{
					if (isPrompt)
					{
						AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert1);
					}
					return isValidOrder;
				}
				this.stopSetPriceEdit.setText(Price.toString(setPrice2));
				if (this.isBetweenBidToAsk(setPrice2))
				{
					if (isPrompt == true)
					{
						AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert8);
					}
					this.fillDefaultStopSetPrice();
					return isValidOrder;
				}
			}

			SetPriceError[] setPriceErrors = this.changeTradeOptionValue(false);

			if (setPriceErrors[0] != SetPriceError.Ok)
			{
				this.handleSetPrieceError(setPriceErrors[0], isPrompt, Language.OrderLMTlblSetPriceA3);
				this.fillDefaultSetPrice();
			}
			else if (setPriceErrors[1] != SetPriceError.Ok)
			{
				this.handleSetPrieceError(setPriceErrors[1], isPrompt, Language.OrderLMTlblSetPriceA2);
				this.fillDefaultStopSetPrice();
			}
			if (setPriceErrors[0] != SetPriceError.Ok || setPriceErrors[1] != SetPriceError.Ok)
			{
				return false;
			}

			//for make better order
			if (o.getClass() == MakeLimitOrder.class
				|| o.getClass() == MakeOneCancelOtherOrder.class || o.getClass() == MakeLimitStopOrder.class)
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
								AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlertBetter);
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
								AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlertBetter);
							}
							return isValidOrder;
						}
					}
				}
			}
			//for make stop order
			if (o.getClass() == MakeStopOrder.class
				|| o.getClass() == MakeOneCancelOtherOrder.class || o.getClass() == MakeLimitStopOrder.class)
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
								AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert4);
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
								AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert5);
							}
							return isValidOrder;
						}
					}
				}
			}
		}
		if (liqLots.compareTo(BigDecimal.ZERO) > 0)
		{
			if (lot.compareTo(liqLots) != 0)
			{
				if (isPrompt)
				{
					AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert7);

					liqLots = AppToolkit.fixLot(liqLots, false, tradePolicyDetail, this._makeOrderAccount);
					this.totalLotTextField.setText(AppToolkit.getFormatLot(liqLots, this._makeOrderAccount.get_Account(), this._instrument));
				}
				return isValidOrder;
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

		Object[] result = MakeOrder.isAcceptTime(this.getOrderType(), this._settingsManager, this._instrument, false);
		if (! (Boolean)result[0])
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, (String)result[1]);
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
											lot2,
											liqLots2, setPrice, setPrice2, false);
		if (! ( (Boolean)result[0]))
		{
			if (isPrompt)
			{
				AlertDialogForm.showDialog(this, null, true, (String)result[1]);
			}
			return isValidOrder;
		}

		return true;
	}

	private OrderType getOrderType()
	{
		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			return null;
		}

		OrderType orderType = OrderType.Limit;
		if (o.getClass() == MakeSpotTradeOrder.class)
		{
			orderType = OrderType.SpotTrade;
		}
		else if (o.getClass() == MakeOneCancelOtherOrder.class)
		{
			orderType = OrderType.Limit;
		}
		else if (o.getClass() == MakeLimitOrder.class)
		{
			orderType = OrderType.Limit;
		}
		if (o.getClass() == MakeLimitStopOrder.class)
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
		else if (o.getClass() == MakeLiquidationOrder.class)
		{
			orderType = OrderType.SpotTrade;
		}
		return orderType;
	}

	//not use
	private void reset()
	{
		if (this.isMakeSpotTradeOrder())
		{
			if (this._makeOrderAccount != null)
			{
				this._makeOrderAccount.reset(this.getOrderType().isSpot(), this.isMakeLimitOrder());
			}
		}
		else
		{
			//this.lotNumeric.setText(this.getFormatDefaultLot());
			this.totalLotTextField.setText("");
			this.fillDefaultValueForOutstandingOrder(this._order != null);

			if (this._order != null)
			{
				if (this.isUnselectIsBuy())
				{
					return;
				}
				boolean isBuy = this.getIsBuy();
				this.totalLotTextField.setText(AppToolkit.getFormatLot(this._makeOrderAccount.getSumLiqLots(isBuy), this._makeOrderAccount.get_Account(),
					this._instrument));
			}

			this.quoteNotify(true);
		}
	}

	private boolean isValidPrice()
	{
		boolean isValidPrice = false;
		if (this.sellButton.isQuoting() || this.buyButton.isQuoting())
		{
			isValidPrice = false;
		}
		else
		{
			isValidPrice = true;
		}
		return isValidPrice;
	}

	public void quoteArrived()
	{
		super.quoteArrived();
	}

	private void submitSpotTrade()
	{
		this.echoSchedulerStop();
		this.outTimeSchedulerStop();
		this.priceValidTimeSchedulerStop();

		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			return;
		}

		if (this.isUnselectIsBuy())
		{
			return;
		}
		boolean isBuy = this.getIsBuy();

		//VisibleButton(0);
		if (!this.isValidPrice())
		{
			AlertDialogForm.showDialog(this, null, true, Language.OperateOrderMethod1PageSubmitAlert1);
			this.resetData();
			return;
		}
		if (this.isValidOrderSpotTrade(true))
		{
			//set value to MakeOrderAccount
			BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
			this._makeOrderAccount.set_BuyLot(lot);
			this._makeOrderAccount.set_SellLot(lot);
			this._makeOrderAccount.set_BuySellType( (isBuy) ? BuySellType.Buy : BuySellType.Sell);
			this._makeOrderAccount.set_IsBuyForCurrent(isBuy);
			MakeSpotTradeOrder makeSpotTradeOrder = (MakeSpotTradeOrder)o;

			if (this.quote.get_IsQuoting())
			{
				this.cancelQuote();
			}
			//PalceLotNnemonic.set(this._instrument.get_Id(), lot);

			HashMap<Guid, MakeOrderAccount> makeOrderAccounts = new HashMap<Guid, MakeOrderAccount> ();
			makeOrderAccounts.put(this._makeOrderAccount.get_Account().get_Id(), this._makeOrderAccount);
			VerificationOrderForm verificationOrderForm = new VerificationOrderForm(this, "Verification Order", true, this._tradingConsole,
				this._settingsManager, this._instrument,
				makeOrderAccounts, OrderType.SpotTrade, OperateType.SingleSpotTrade, true, null, null);
			//verificationOrderForm.show();
		}
		else
		{
			this.resetData();
			return;
		}
	}

	private void submit()
	{
		this.submit(null);
	}

	private void submit(OperateType operateType)
	{
		this.outstandingOrderTable.updateEditingValue();

		if (this._operateSource == OperateSource.LiquidationMultiSPT)
		{
			this.submitLiquidationMultiSPT(operateType);
		}
		else
		{
			if (this.isMakeSpotTradeOrder())
			{
				this.submitSpotTrade();
			}
			else
			{
				this.submitOther();
			}
		}
	}

	private void submitLiquidationMultiSPT(OperateType operateType)
	{
		Object[] result = this._makeLiquidationOrder.isValidOrder(this.getOrderType().isSpot(), operateType);
		if ( (Boolean)result[0])
		{
			VerificationOrderForm verificationOrderForm = new VerificationOrderForm(this, "Verification Order", true, this._tradingConsole,
				this._settingsManager, this._instrument,
				this.getMakeOrderAccounts(), OrderType.SpotTrade, operateType, true, null, null);
			//verificationOrderForm.show();
		}
		else
		{
			AlertDialogForm.showDialog(this, null, true, result[1].toString());

			this.resetData();
		}
	}

	private void convertInputGoodTillMonth()
	{
		if (this.expireTimeChoice.getSelectedIndex() != -1)
		{
			DateTime date = (DateTime)this.expireTimeChoice.getSelectedValue();
			String caption = (String)this.expireTimeChoice.getSelectedText();
			if (caption.equalsIgnoreCase(Language.GoodTillDate))
			{
				this._expireTime = this.expireTimeDate.getExpireTime();
			}
			else
			{
				this._expireTime = date;
			}
		}
	}

	private void submitOther()
	{
		if (!this.isValidOrderOther(true))
		{
			return;
		}
		//set value to MakeOrderAccount
		Object o = this.getOrderTypeValue();
		if (o == null)
		{
			return;
		}
		this.convertInputGoodTillMonth();
		if (this._expireTime == null)
		{
			return;
		}

		Price setPrice = Price.parse(this.setPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		Price setPrice2 = Price.parse(this.stopSetPriceEdit.getText(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
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
				 || o.getClass() == MakeMarketOnCloseOrder.class)
		{
			setPrice = null;
			setPrice2 = null;
		}

		boolean isBuy = this.getIsBuy();

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
		}

		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		this._makeOrderAccount.set_BuyLot(lot);
		this._makeOrderAccount.set_BuySetPrice( (isBuy) ? setPrice : null);
		this._makeOrderAccount.set_BuySetPrice2( (isBuy) ? setPrice2 : null);
		this._makeOrderAccount.set_SellLot(lot);
		this._makeOrderAccount.set_SellSetPrice( (isBuy) ? null : setPrice);
		this._makeOrderAccount.set_SellSetPrice2( (isBuy) ? null : setPrice2);
		this._makeOrderAccount.set_BuySellType( (isBuy) ? BuySellType.Buy : BuySellType.Sell);
		this._makeOrderAccount.set_IsBuyForCurrent(isBuy);

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

		if (this.quote.get_IsQuoting())
		{
			this.cancelQuote();
		}
		//PalceLotNnemonic.set(this._instrument.get_Id(), lot);

		HashMap<Guid, MakeOrderAccount> makeOrderAccounts = new HashMap<Guid, MakeOrderAccount> ();
		makeOrderAccounts.put(this._makeOrderAccount.get_Account().get_Id(), this._makeOrderAccount);
		VerificationOrderForm verificationOrderForm = new VerificationOrderForm(this, "Verification Order", true, this._tradingConsole,
			this._settingsManager, this._instrument, makeOrderAccounts, this.getOrderType(),
			operateType, false, null, this._expireTime, this.ocoCheckBox.isSelected()); //date);
	}

	public void resetData()
	{
		if (this.isMakeSpotTradeOrder())
		{
			super.resetData();
		}
	}

	public void dispose2()
	{
		if (this._openContractForm != null)
		{
			this._openContractForm.dispose();
		}
		this.dispose();
	}

	public void dispose()
	{
		try
		{
			if (this.quote.get_IsQuoting())
			{
				this.cancelQuoteForDispose(this.quote.get_BuyLot(), this.quote.get_SellLot());
			}

			this.echoSchedulerStop();
			this.outTimeSchedulerStop();
			this.priceValidTimeSchedulerStop();

			if (this.isMakeSpotTradeOrder())
			{
				try
				{
					if (this._makeOrderAccount != null)
					{
						this._makeOrderAccount.unbindOutstanding();
					}
				}
				catch (Throwable exception)
				{
					exception.printStackTrace();
				}
			}
		}
		catch (Exception ex)
		{
		}
		finally
		{
			if (this._openContractForm != null)
			{
				this._openContractForm.setCanClose(true);
				this._openContractForm.enableLimitStopButton();
				this._openContractForm.enableLiquidationButton();
				this._openContractForm.toFront();
			}
			super.dispose();
		}
	}

	private void lotNumeric_focusGained()
	{
		this.totalLotTextField.select(0, this.totalLotTextField.getText().length());
	}

	private void setPriceEdit_focusGained()
	{
		this.priceEdit_focusGained(this.setPriceEdit);
	}

	private void stopSetPriceEdit_focusGained()
	{
		this.priceEdit_focusGained(this.stopSetPriceEdit);
	}

	private void priceEdit_focusGained(JTextField priceEdit)
	{
		String text = priceEdit.getText();
		priceEdit.select( ( (text.indexOf(".") != -1) ? text.indexOf(".") + 1 : 0), text.length());
	}

	private void jbInit() throws Exception
	{
		this._openCloseRelationSite = new OpenCloseRelationSite(this);
		this.addWindowListener(new MakeOrderForm_this_windowAdapter(this));

		this.setSize(670, 500);
		this.setResizable(true);
		this.addKeyListener(new MakeOrderForm_this_keyAdapter(this));
		this.setTitle(Language.makeOrderFormTitle);
		this.setBackground(FormBackColor.makeOrderForm);

		ActionListener actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setPriceEdit.setEnabled(limitCheckBox.isSelected());
				stopSetPriceEdit.setEnabled(stopCheckBox.isSelected());
				fillDefaultSetPrice();
				refreshBuySellSubmitButtonStatus();
				updateOcoCheckBoxStatus();
			}
		};
		limitCheckBox.addActionListener(actionListener);

		limitCheckBox.setVisible(false);
		ocoCheckBox.setEnabled(false);
		ocoCheckBox.setVisible(false);

		this.closeLotTextField.setEnabled(false);
		this.closeLotTextField.setBackground(MakeOrderForm._disnabledBackground);

		actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				stopSetPriceEdit.setEnabled(stopCheckBox.isSelected());
				setPriceEdit.setEnabled(limitCheckBox.isSelected());
				fillDefaultStopSetPrice();
				refreshBuySellSubmitButtonStatus();
				updateOcoCheckBoxStatus();
			}
		};
		stopCheckBox.addActionListener(actionListener);

		stopCheckBox.setVisible(false);
		ocoCheckBox.setText("OCO");

		Font font = new Font("SansSerif", Font.BOLD, 14);
		totalLotTextField.setFont(font);
		closeLotTextField.setFont(font);
		setPriceEdit.setFont(font);
		font = new Font("SansSerif", Font.BOLD, 12);
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
		closeLotStaticText.setFont(font);
		expireTimeStaticText.setText("Expire Time");
		expireTimeStaticText.setFont(font);
		totalLotTextField.setText("Numeric1");
		timerTime = new Timer(500, new MakeOrderForm_timer_actionAdapter(this));
		totalLotTextField.addKeyListener(new MakeOrderForm_lotNumeric_keyAdapter(this));
		totalLotTextField.addFocusListener(new MakeOrderForm_lotNumeric_focusAdapter(this));
		submitButton.setText("Submit");
		submitButton.addActionListener(new MakeOrderForm_submitButton_actionAdapter(this));
		multipleCloseButton.addActionListener(new MakeOrderForm_multipulCloseButton_actionAdapter(this));
		stopSetPriceEdit.setText("");
		stopSetPriceEdit.setFont(new java.awt.Font("SansSerif", Font.BOLD, 14));
		stopSetPriceEdit.addFocusListener(new MakeOrderForm_stopSetPriceEdit_focusAdapter(this));
		orderTypeChoice.addItemListener(new MakeOrderForm_orderTypeChoice_actionAdapter(this));
		accountChoice.addItemListener(new MakeOrderForm_accountChoice_actionAdapter(this));
		isBuyChoice.addItemListener(new MakeOrderForm_isBuyChoice_actionAdapter(this));
		expireTimeChoice.addItemListener(new MakeOrderForm_expireTimeChoice_actionAdapter(this));
		setPriceEdit.addActionListener(new MakeOrderForm_setPriceEdit_actionAdapter(this));
		setPriceEdit.addFocusListener(new MakeOrderForm_setPriceEdit_focusAdapter(this));
		stopSetPriceStaticText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 12));
		stopSetPriceStaticText.setText("Stop");
		tradeOption1StaticText.setText("BETTER");
		tradeOption2StaticText.setText("STOP");
		messageTextArea.setText("");
		sellButton.setForeground(Color.blue);
		sellButton.setFont(new java.awt.Font("SansSerif", Font.BOLD, 16));
		sellButton.setText("Sell");
		sellButton.addActionListener(new MakeOrderForm_sellButton_actionAdapter(this));
		buyButton.setFont(new java.awt.Font("SansSerif", Font.BOLD, 16));
		buyButton.setForeground(Color.red);
		buyButton.setText("Buy");
		buyButton.addActionListener(new MakeOrderForm_buyButton_actionAdapter(this));

		JPanel mainPanel = new JPanel(gridBagLayout1);
		JPanel panel = new JPanel();
		mainPanel.add(buyButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 2), 95, 55));
		mainPanel.add(sellButton, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 5, 5), 95, 55));

		mainPanel.add(orderTypeStaticText, new GridBagConstraints2(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 1, 0), 0, 0));
		mainPanel.add(orderTypeChoice, new GridBagConstraints2(1, 1, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 1, 5), 0, 0));

		mainPanel.add(accountStaticText, new GridBagConstraints2(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 1, 0), 0, 0));
		mainPanel.add(accountChoice, new GridBagConstraints2(1, 2, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 1, 5), 0, 0));

		mainPanel.add(isBuyStaticText, new GridBagConstraints2(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 0), 0, 0));
		mainPanel.add(isBuyChoice, new GridBagConstraints2(1, 3, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 5), 0, 0));

		mainPanel.add(totalLotStaticText, new GridBagConstraints2(0, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 0), 0, 0));
		mainPanel.add(totalLotTextField, new GridBagConstraints2(1, 4, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 5), 0, 0));

		mainPanel.add(closeLotStaticText, new GridBagConstraints2(0, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 0), 0, 0));
		mainPanel.add(closeLotTextField, new GridBagConstraints2(1, 5, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 5), 0, 0));

		mainPanel.add(setPriceStaticText, new GridBagConstraints2(0, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 0), 0, 0));
		mainPanel.add(limitCheckBox, new GridBagConstraints2(1, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, -2, 1, 0), 0, 0));
		mainPanel.add(setPriceEdit, new GridBagConstraints2(2, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 5), 80, 0));

		mainPanel.add(stopSetPriceStaticText, new GridBagConstraints2(0, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 1, 0), 0, 0));
		mainPanel.add(stopCheckBox, new GridBagConstraints2(1, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, -2, 1, 0), 0, 0));
		mainPanel.add(stopSetPriceEdit, new GridBagConstraints2(2, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 5), 80, 0));

		mainPanel.add(ocoCheckBox, new GridBagConstraints(1, 8, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, -2, 1, 0), 0, 0));

		mainPanel.add(expireTimeStaticText, new GridBagConstraints2(0, 9, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1, 5, 5, 0), 0, 0));
		mainPanel.add(expireTimeChoice, new GridBagConstraints(1, 9, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 1, 5), 0, 0));
		mainPanel.add(expireTimeDate, new GridBagConstraints2(1, 10, 2, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(1, 2, 2, 5), 5, 0));
		expireTimeDate.setVisible(false);

		JScrollPane scrollPane = new JScrollPane(outstandingOrderTable);
		mainPanel.add(scrollPane, new GridBagConstraints(0, 11, 3, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 50));

		panel.add(submitButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 20, 0));
		panel.add(multipleCloseButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 20, 0));

		mainPanel.add(panel, new GridBagConstraints(0, 12, 7, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 20, 0));

		mainPanel.add(messageTextArea, new GridBagConstraints(3, 0, 1, 12, 1.0, 1.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

		boolean allowMatchingOrder = MakeOrder.canMatchingOrder(this._settingsManager, this._instrument, this._order == null ? null : this._order.get_Account());
		if (this._operateSource.value() != OperateSource.LiquidationMultiSPT.value()
			&& this._operateSource.value() != OperateSource.LiquidationSingleSPT.value()
			&& allowMatchingOrder && this._instrument.get_MaxOtherLot().compareTo(BigDecimal.ZERO) > 0)
		{
			JideTabbedPane tabbedPane = new JideTabbedPane();
			this.getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

			tabbedPane.addTab(Language.makeOrderFormTitle, mainPanel);

			Object[] result = MakeOrder.isAllowMakeLimitOrder(this._tradingConsole, this._settingsManager,
				this._instrument, null);
			if ( (Boolean)result[0])
			{
				MakeLimitOrder makeLimitOrder = (MakeLimitOrder)result[2];
				makeLimitOrder.setDefaultBuySellType(BuySellType.Buy);
				MatchingOrderForm matchingOrderForm = new MatchingOrderForm(this, this._tradingConsole, this._settingsManager, this._instrument, this._order,
					this._openContractForm, true, makeLimitOrder, true);
				tabbedPane.addTab(Language.matchingOrderFormTitle, matchingOrderForm);
			}
		}
		else
		{
			this.getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);
		}
	}

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
	JTextField setPriceEdit = new JTextField();
	JFormattedTextFieldEx totalLotTextField = new JFormattedTextFieldEx(new DecimalFormat(), true);
	JFormattedTextFieldEx closeLotTextField = new JFormattedTextFieldEx(new DecimalFormat(), true);
	JAdvancedComboBox expireTimeChoice = new JAdvancedComboBox();
	DataGrid outstandingOrderTable = new DataGrid("OutstandingOrderTable");
	PVButton2 submitButton = new PVButton2();
	PVButton2 multipleCloseButton = new PVButton2();
	java.awt.GridBagLayout gridBagLayout1 = new GridBagLayout();
	ExpireTimeEditor expireTimeDate = new ExpireTimeEditor();
	JTextField stopSetPriceEdit = new JTextField();
	PVStaticText2 stopSetPriceStaticText = new PVStaticText2();
	PVStaticText2 tradeOption1StaticText = new PVStaticText2();
	PVStaticText2 tradeOption2StaticText = new PVStaticText2();
	private MultiTextArea messageTextArea = new MultiTextArea();
	private Timer timerTime;

	private BidAskButton sellButton = new BidAskButton(false, 3);
	private BidAskButton buyButton = new BidAskButton(true, 3);

	JCheckBox limitCheckBox = new JCheckBox();
	JCheckBox stopCheckBox = new JCheckBox();
	JCheckBox ocoCheckBox = new JCheckBox();

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	public void exitButton_actionPerformed(ActionEvent e)
	{
		this.dispose();
	}

	public void resetButton_actionPerformed(ActionEvent e)
	{
		this.reset();
	}

	public void submitButton_actionPerformed(ActionEvent e)
	{
		BigDecimal lot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
		if (lot.compareTo(BigDecimal.ZERO) > 0)
		{
			BigDecimal maxLot;
			if (this.isMakeSpotTradeOrder())
			{
				maxLot = this._instrument.get_MaxDQLot();
			}
			else
			{
				maxLot = this._instrument.get_MaxOtherLot();
			}
			if (maxLot.compareTo(BigDecimal.ZERO) != 0
				&& lot.compareTo(maxLot) > 0)
			{
				TradingConsole.traceSource.trace(TraceType.Information, "lot= " + lot.toString() + "; maxLot = " + maxLot.toString()
												 + new FrameworkException("").getStackTrace());

				AlertDialogForm.showDialog(this, null, true,
										   Language.OrderLMTPagetextLot_OnblurAlert0 + "(" +
										   AppToolkit.getFormatLot(maxLot, this._makeOrderAccount.get_Account(), this._instrument) +
										   ")!");
				return;
			}
		}
		this.submit(OperateType.DirectLiq);
	}

	public void multipleCloseButton_actionPerformed(ActionEvent e)
	{
		this.submit(OperateType.MultipleClose);
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
		this.setPrice_FocusLost(true, true);
	}

	public void stopSetPriceEdit_focusLost(FocusEvent e)
	{
		this.setPrice_FocusLost(true, false);
	}

	public void lotNumeric_focusLost(FocusEvent e)
	{
		this.lotNumeric_focusLost();
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

	public void lotNumeric_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == e.VK_ENTER || e.getID() == e.VK_TAB)
		{
			this.lotNumeric_keyPressed();
		}
	}

	public void this_keyPressed(KeyEvent e)
	{
	}

	public void lotNumeric_keyReleased(KeyEvent e)
	{
		this.lotNumeric_keyReleased();
	}

	public void sellButton_actionPerformed(ActionEvent e)
	{
		this.submit();
	}

	public void buyButton_actionPerformed(ActionEvent e)
	{
		this.submit();
	}

	class MakeOrderForm_this_windowAdapter extends WindowAdapter
	{
		private MakeOrderForm adaptee;
		MakeOrderForm_this_windowAdapter(MakeOrderForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}

	private void addPlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
	{
		this._placeOrderTypeChangedListeners.add(placeOrderTypeChangedListener);
	}

	private JTextField getCloseLotEditor()
	{
		if (this.outstandingOrderTable.getRowCount() > 0)
		{
			return this.closeLotTextField;
		}
		else
		{
			return null;
		}
	}

	private JTextField getTotalLotEditor()
	{
		return this.totalLotTextField;
	}

	private BigDecimal getLot()
	{
		return AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getText());
	}

	private void updateTotalColseLot(BigDecimal accountLot)
	{
		if (accountLot.compareTo(BigDecimal.ZERO) > 0)
		{
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());
			accountLot = AppToolkit.fixLot(accountLot, false, tradePolicyDetail, this._makeOrderAccount);
		}

		this.updateOcoCheckBoxStatus();

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
		if (o.getClass() == MakeLimitStopOrder.class && this.ocoCheckBox.isSelected())
		{
			accountLot2 = accountLot;
		}

		if (accountLot2.compareTo(BigDecimal.ZERO) <= 0)
		{
			accountLot2 = this.getDefaultLot();
		}

		try
		{
			this.totalLotTextField.setText(AppToolkit.getFormatLot(accountLot, this._makeOrderAccount.get_Account(), this._instrument));
		}
		catch (IllegalStateException exception)
		{
		}

		this.updatePriceEditStatus();
		if (StringHelper.isNullOrEmpty(this.totalLotTextField.getText()))
		{
			this.fillDefaultLot();
		}
		else
		{
			this.refreshBuySellSubmitButtonStatus();
		}
	}

	private void rebind()
	{
		if (this._openContractForm != null)
		{
			this._openContractForm.rebind();
		}
		if (this._makeOrderAccount != null)
		{
			boolean isBuy = this._makeOrderAccount.get_IsBuyForCurrent();
			this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, BuySellType.Both,
				( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._openCloseRelationSite, this._order);
		}
		else if (this._makeLiquidationOrder != null)
		{
			this._makeLiquidationOrder.initialize(this.outstandingOrderTable, true, false, new PropertyChangingListener(this), this._openCloseRelationSite,
												  new PropertyChangedListener(this), false);
			int column = this.outstandingOrderTable.get_BindingSource().getColumnByName(OutstandingOrderColKey.IsSelected);
			this.outstandingOrderTable.sortColumn(column, false, false);
		}

		if ( (this._operateSource == OperateSource.LiquidationLMTSTP
			  || this._operateSource == OperateSource.LiquidationMultiSPT
			  || this._operateSource == OperateSource.LiquidationSingleSPT)
			&& this.outstandingOrderTable.getRowCount() == 0)
		{
			AlertDialogForm.showDialog(this, null, true, Language.DisposedForOpenOrderClosed);
			this.dispose2();
		}
	}

	private OperateType getOperateType()
	{
		Object orderType = this.getOrderTypeValue();
		if (orderType == null)
		{
			return null;
		}
		else if (orderType.getClass() == MakeSpotTradeOrder.class
				 || orderType.getClass() == MakeLiquidationOrder.class)
		{
			return OperateType.DirectLiq;
		}
		else
		{
			return OperateType.Limit;
		}
	}
}

class MakeOrderForm_buyButton_actionAdapter implements ActionListener
{
	private MakeOrderForm adaptee;
	MakeOrderForm_buyButton_actionAdapter(MakeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.buyButton_actionPerformed(e);
	}
}

class MakeOrderForm_this_keyAdapter extends KeyAdapter
{
	private MakeOrderForm adaptee;
	MakeOrderForm_this_keyAdapter(MakeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e)
	{
		adaptee.this_keyPressed(e);
	}
}

class MakeOrderForm_lotNumeric_focusAdapter extends FocusAdapter
{
	private MakeOrderForm adaptee;
	MakeOrderForm_lotNumeric_focusAdapter(MakeOrderForm adaptee)
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

class MakeOrderForm_lotNumeric_keyAdapter extends KeyAdapter
{
	private MakeOrderForm adaptee;
	MakeOrderForm_lotNumeric_keyAdapter(MakeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e)
	{
		adaptee.lotNumeric_keyPressed(e);
	}

	public void keyReleased(KeyEvent e)
	{
		adaptee.lotNumeric_keyReleased(e);
	}
}

class MakeOrderForm_stopSetPriceEdit_focusAdapter extends FocusAdapter
{
	private MakeOrderForm adaptee;
	MakeOrderForm_stopSetPriceEdit_focusAdapter(MakeOrderForm adaptee)
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

class MakeOrderForm_setPriceEdit_focusAdapter extends FocusAdapter
{
	private MakeOrderForm adaptee;
	MakeOrderForm_setPriceEdit_focusAdapter(MakeOrderForm adaptee)
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

class MakeOrderForm_setPriceEdit_actionAdapter implements ActionListener
{
	private MakeOrderForm adaptee;
	MakeOrderForm_setPriceEdit_actionAdapter(MakeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.setPriceEdit_actionPerformed(e);
	}
}

class MakeOrderForm_expireTimeChoice_actionAdapter implements ItemListener
{
	private MakeOrderForm adaptee;
	MakeOrderForm_expireTimeChoice_actionAdapter(MakeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.expireTimeChoice_actionPerformed(e);
	}
}

class MakeOrderForm_isBuyChoice_actionAdapter implements ItemListener
{
	private MakeOrderForm adaptee;
	MakeOrderForm_isBuyChoice_actionAdapter(MakeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.isBuyChoice_actionPerformed(e);
	}
}

class MakeOrderForm_orderTypeChoice_actionAdapter implements ItemListener
{
	private MakeOrderForm adaptee;
	MakeOrderForm_orderTypeChoice_actionAdapter(MakeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.orderTypeChoice_actionPerformed(e);
	}
}

class MakeOrderForm_accountChoice_actionAdapter implements ItemListener
{
	private MakeOrderForm adaptee;
	MakeOrderForm_accountChoice_actionAdapter(MakeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{

		adaptee.accountChoice_actionPerformed(e);
	}
}

class MakeOrderForm_submitButton_actionAdapter implements ActionListener
{
	private MakeOrderForm adaptee;
	MakeOrderForm_submitButton_actionAdapter(MakeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}

class MakeOrderForm_multipulCloseButton_actionAdapter implements ActionListener
{
	private MakeOrderForm adaptee;
	MakeOrderForm_multipulCloseButton_actionAdapter(MakeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.multipleCloseButton_actionPerformed(e);
	}
}

class MakeOrderForm_sellButton_actionAdapter implements ActionListener
{
	private MakeOrderForm adaptee;
	MakeOrderForm_sellButton_actionAdapter(MakeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.sellButton_actionPerformed(e);
	}
}

class Quote
{
	private boolean isquoting = false;
	//private BSStatus _bsStatus = BSStatus.None;
	private BigDecimal _buyLot = BigDecimal.ZERO;
	private BigDecimal _sellLot = BigDecimal.ZERO;

	private Quote()
	{
	}

	public static Quote Create()
	{
		return new Quote();
	}

	public boolean get_IsQuoting()
	{
		return this.isquoting;
	}

	public void set_IsQuoting(boolean value)
	{
		this.isquoting = value;
		if (!value)
		{
			this._buyLot = BigDecimal.ZERO;
			this._sellLot = BigDecimal.ZERO;
		}
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
}

class MakeOrderForm_timer_actionAdapter implements ActionListener
{
	private MakeOrderForm adaptee;
	MakeOrderForm_timer_actionAdapter(MakeOrderForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.timer_actionPerformed(e);
	}
}
