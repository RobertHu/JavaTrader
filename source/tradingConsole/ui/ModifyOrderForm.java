package tradingConsole.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.*;

import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.ui.language.*;
import framework.StringHelper;
import java.math.BigDecimal;
import tradingConsole.settings.Parameter;
import tradingConsole.settings.SettingsManager;
import java.util.Iterator;
import framework.xml.XmlConvert;
import framework.Guid;
import tradingConsole.settings.RelationOrder;
import framework.IAsyncCallback;
import framework.xml.XmlNode;
import framework.IAsyncResult;
import framework.diagnostics.TraceType;
import tradingConsole.common.TransactionError;
import tradingConsole.service.PlaceResult;
import framework.xml.XmlDocument;
import java.util.HashMap;

public class ModifyOrderForm extends JDialog
{
	private TradingConsole tradingConsole;
	private Order order;

	private PVButton2 confirmButton = new PVButton2();
	private PVButton2 exitButton = new PVButton2();
	private PVStaticText2 lotStaticText = new PVStaticText2();
	private JTextField lotTextField = new JTextField();

	private PVStaticText2 stopPirceStaticText;
	private JTextField stopPirceTextField;
	private PVStaticText2 limitPirceStaticText;
	private JTextField limitPirceTextField;

	public ModifyOrderForm(Dialog parent, TradingConsole tradingConsole, Order order)
	{
		super(parent, true);

		this.tradingConsole = tradingConsole;
		this.order = order;
		this.jbInit();
		this.setTitle(Language.ModifyInstructionPrompt);
		this.addActionListeners();
		this.lotTextField.setEditable(order.get_RelationOrders() == null || order.get_RelationOrders().size() == 0);
	}

	private void addActionListeners()
	{
		this.exitButton.addActionListener(new ActionListener()
			  {
				  public void actionPerformed(ActionEvent e)
				  {
					  dispose();
				  }
			  });

			  this.confirmButton.addActionListener(new ActionListener()
			  {
				  public void actionPerformed(ActionEvent e)
				  {
					  confirm();
				  }
			  });
	}

	private void addDocumentListener(JTextField textField)
	{
		textField.getDocument().addDocumentListener(new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e)
			{
				enableConfirmButton();
			}

			public void removeUpdate(DocumentEvent e)
			{
				enableConfirmButton();
			}

			public void changedUpdate(DocumentEvent e)
			{
				enableConfirmButton();
			}
		});
	}
	private void confirm()
	{
		BigDecimal newLot = null;
		Price newLimitPrice = null;
		Price newStopPrice = null;

		Instrument instrument = this.order.get_Transaction().get_Instrument();

		if(StringHelper.isNullOrEmpty(this.lotTextField.getText()))
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
			return;
		}
		else
		{
			BigDecimal inputLot = new BigDecimal(lotTextField.getText());
			String validLot = AppToolkit.getFormatLot(inputLot, order.get_Account(), order.get_Instrument());
			newLot = new BigDecimal(validLot);
			if(inputLot.compareTo(newLot) != 0)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderOperateOutstandingOrderGridValidateEditPrompt0);
				this.lotTextField.setText(validLot);
				return;
			}

			if(newLot.compareTo(BigDecimal.ZERO) <= 0)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert6);
				return;
			}

			if(newLot.compareTo(instrument.get_MaxOtherLot()) > 0)
			{
				String info = Language.OrderOperateOrderOperateLiquidationGrid_ValidateEditAlert1 + instrument.get_MaxOtherLot();
				AlertDialogForm.showDialog(this, null, true, info);
				return;
			}
		}

		boolean setPriceChanged = false;
		boolean setPrice2Changed = false;

		OrderType orderType = this.order.get_Transaction().get_OrderType();
		if(orderType == OrderType.Limit)
		{
			if (this.order.get_TradeOption() == TradeOption.Better)
			{
				if (StringHelper.isNullOrEmpty(this.limitPirceTextField.getText()))
				{
					AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
					return;
				}
				newLimitPrice = Price.create(this.limitPirceTextField.getText(), instrument.get_NumeratorUnit(), instrument.get_Denominator());
			}
			else
			{
				if (StringHelper.isNullOrEmpty(this.stopPirceTextField.getText()))
				{
					AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
					return;
				}
				newStopPrice = Price.create(this.stopPirceTextField.getText(), instrument.get_NumeratorUnit(), instrument.get_Denominator());
			}

			Price newSetPrice = this.order.get_TradeOption() == TradeOption.Better ? newLimitPrice : newStopPrice;
			if (newSetPrice.compareTo(this.order.get_SetPrice()) != 0)
			{
				setPriceChanged = true;
				SetPriceError setPriceError = this.isValidPrice(this.order.get_TradeOption(), newSetPrice);
				if (setPriceError != SetPriceError.Ok)
				{
					this.alert(this.order.get_TradeOption() == TradeOption.Better ? Language.OrderLMTlblSetPriceA3 : Language.OrderLMTlblSetPriceA2,
							   setPriceError);
					if (this.order.get_TradeOption() == TradeOption.Better)
					{
						this.fillDefaultLimitPrice();
					}
					else
					{
						this.fillDefaultStopPrice();
					}
					return;
				}
			}
		}
		else if(orderType == OrderType.StopLimit)
		{
			if(StringHelper.isNullOrEmpty(this.limitPirceTextField.getText()))
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
				return;
			}
			newLimitPrice = Price.create(this.limitPirceTextField.getText(), instrument.get_NumeratorUnit(), instrument.get_Denominator());
			if(newLimitPrice.compareTo(this.order.get_SetPrice()) != 0)
			{
				setPriceChanged = true;
			}

			if(StringHelper.isNullOrEmpty(this.stopPirceTextField.getText()))
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
				return;
			}
			newStopPrice = Price.create(this.stopPirceTextField.getText(), instrument.get_NumeratorUnit(), instrument.get_Denominator());
			if(newStopPrice.compareTo(this.order.get_SetPrice()) != 0)
			{
				setPrice2Changed = true;
			}

			Price last = instrument.get_LastQuotation().get_LastForBursa();
			if(last == null)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert10);
				return;
			}

			Price setPrice = setPriceChanged ? newLimitPrice : this.order.get_SetPrice();
			Price setPrice2 = setPrice2Changed ? newStopPrice : this.order.get_SetPrice2();

			boolean isBuy = this.order.get_IsBuy();
			if(isBuy)
			{
				if(!Price.more(setPrice2, last) || !Price.more(setPrice, setPrice2))
				{
					AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert11);
					return;
				}
			}
			else
			{
				if(!Price.less(setPrice2, last) || !Price.less(setPrice, setPrice2))
				{
					AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert12);
					return;
				}
			}
		}

		newLot = newLot.compareTo(this.order.get_Lot()) == 0 ? null: newLot;
		String newSetPrice = setPriceChanged ? Price.toString(newLimitPrice) : null;
		String newSetPrice2 = setPrice2Changed ? Price.toString(newStopPrice) : null;

		if(newLot == null && newSetPrice == null && newSetPrice2 == null)
		{
			AlertDialogForm.showDialog(this, null, true, Language.TheOrderNotModified);
			return;
		}

		String xml = this.getModifyXml(newLot == null ? null : newLot.toString(), newSetPrice, newSetPrice2);
		XmlDocument xmlDocument = new XmlDocument();
		xmlDocument.loadXml(xml);
		XmlNode xmlTransaction = xmlDocument.get_DocumentElement();

		PlaceResult result = this.tradingConsole.get_TradingConsoleServer().place(xmlTransaction);
		if(result.get_TransactionError() == TransactionError.OK)
		{
			this.dispose();
			((Dialog)this.getParent()).dispose();
		}
		else
		{
			AlertDialogForm.showDialog(this, null, true, Language.FailedToModifyOrder + ": " + TransactionError.getCaption(result.get_TransactionError()));
		}
	}

	private String getModifyXml(String newLot, String setPrice, String setPrice2)
	{
		Transaction assigningTransaction = this.order.get_Transaction();
		Guid transactionId = Guid.newGuid();

		String xml = "<Transaction ";
		xml += "ID=\'" + transactionId.toString() + "\' " +
			"AccountID=\'" + assigningTransaction.get_Account().get_Id().toString() + "\' " +
			"InstrumentID=\'" + assigningTransaction.get_Instrument().get_Id().toString() + "\' " +
			"Type=\'" + XmlConvert.toString(assigningTransaction.get_Type().value()) + "\' " +
			"SubType=\'" + XmlConvert.toString(TransactionSubType.Amend.value()) + "\' " +
			"OrderType=\'" + XmlConvert.toString(assigningTransaction.get_OrderType().value()) + "\' " +
			"BeginTime=\'" + XmlConvert.toString(assigningTransaction.get_BeginTime(), "yyyy-MM-dd HH:mm:ss") + "\' " +
			"EndTime=\'" + XmlConvert.toString(assigningTransaction.get_EndTime(), "yyyy-MM-dd HH:mm:ss") + "\' " +
			"ExpireType=\'" + XmlConvert.toString(assigningTransaction.get_ExpireType().value()) + "\' " +
			"SubmitTime=\'" + XmlConvert.toString(assigningTransaction.get_SubmitTime(), "yyyy-MM-dd HH:mm:ss") + "\' " +
			"SubmitorID=\'" + assigningTransaction.get_SubmitorID().toString() + "\' " +
			"AssigningOrderID=\'" + this.order.get_Id().toString() + "\' >";

		Guid orderId = Guid.newGuid();
		String lot = StringHelper.isNullOrEmpty(newLot) ? this.order.get_LotString() : newLot;
		xml += "<Order ";
			xml += "ID=\'" + orderId.toString() + "\'";
			xml += "TradeOption=\'" + XmlConvert.toString(this.order.get_TradeOption().value()) + "\' ";
			xml += "IsOpen=\'" + XmlConvert.toString(this.order.get_IsOpen()) + "\' ";
			xml += "IsBuy=\'" + XmlConvert.toString(this.order.get_IsBuy()) + "\' ";
			xml += "SetPrice=\'" + (StringHelper.isNullOrEmpty(setPrice) ? Price.toString(this.order.get_SetPrice()) : setPrice) + "\' ";
			xml += "SetPrice2=\'" + (StringHelper.isNullOrEmpty(setPrice2) ? Price.toString(this.order.get_SetPrice2()) : setPrice2) + "\' ";
			xml += "DQMaxMove=\'" + XmlConvert.toString(this.order.get_DQMaxMove()) + "\' ";
			xml += "Lot=\'" + lot + "\' ";
			xml += "OriginalLot=\'" + lot + "\' ";
			xml += ">";
		xml += "</Order>";

		xml += "</Transaction>";
		return xml;
	}

	private SetPriceError isValidPrice(TradeOption tradeOption, Price setPrice)
	{
		if(this.isBetweenBidToAsk(setPrice)) return SetPriceError.SetPriceBetweenAskAndBid;

		Instrument instrument = this.order.get_Transaction().get_Instrument();
		boolean isBuy = this.order.get_IsBuy();
		Account account = this.order.get_Account();
		BigDecimal lot = this.order.get_LotBalance();
		Price marketPrice = instrument.get_LastQuotation().getBuySell(isBuy);
		SetPriceError setPriceError = Order.checkLMTOrderSetPrice(account, true, instrument, isBuy, tradeOption, setPrice, marketPrice, lot, this.order, this.getPlaceRelation(), false);

		double dblMarketPrice = Price.toDouble(marketPrice);
		if (Math.abs(Price.toDouble(setPrice) - dblMarketPrice) > dblMarketPrice * 0.2)
		{
			setPriceError = SetPriceError.SetPriceTooFarAwayMarket;
		}

		return setPriceError;
	}

	private void alert(String priceName, SetPriceError setPriceError)
	{
		String errorInfo = "";
		boolean isBuy = this.order.get_IsBuy();
		Account account = this.order.get_Account();
		BigDecimal lot = this.order.get_LotBalance();
		if (setPriceError == SetPriceError.SetPriceTooCloseMarket)
		{
			Instrument instrument = this.order.get_Transaction().get_Instrument();
			SettingsManager settingsManager = instrument.get_SettingsManager();

			if (settingsManager.get_SystemParameter().get_DisplayLmtStopPoints())
			{
				errorInfo = "[" + priceName + "] " + Language.OrderLMTPageorderValidAlert2 + " " + instrument.get_AcceptLmtVariation(account, isBuy, lot, this.order, getPlaceRelation(), false) + " " +
					Language.OrderLMTPageorderValidAlert22;
			}
			else
			{
				errorInfo = priceName + " " + Language.SetPriceTooCloseToMarket;
			}
		}
		else if (setPriceError == SetPriceError.SetPriceTooFarAwayMarket)
		{
			errorInfo = "[" + priceName +  "] " + Language.OrderLMTPageorderValidAlert3;
		}
		else if (setPriceError == SetPriceError.InvalidSetPrice)
		{
			errorInfo = "[" + priceName +  "] " + Language.InvalidSetPrice;
		}
		else if (setPriceError == SetPriceError.SetPriceBetweenAskAndBid)
		{
			errorInfo = "[" + priceName + "]" + Language.OrderLMTPageorderValidAlert8;
		}

		AlertDialogForm.showDialog(this, null, true, errorInfo);
	}

	private void fillDefaultLimitPrice()
	{
		boolean isBuy = this.order.get_IsBuy();

		Instrument instrument = this.order.get_Transaction().get_Instrument();
		Price bid = instrument.get_LastQuotation().get_Bid();
		Price ask = instrument.get_LastQuotation().get_Ask();
		if (bid == null || ask == null)
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderSingleDQPageorderValidAlert0);
			return;
		}

		Account account = this.order.get_Account();
		BigDecimal lot = this.order.get_LotBalance();

		int acceptLmtVariation = instrument.get_AcceptLmtVariation(account, isBuy, lot, this.order, getPlaceRelation(), false);
		//Fill Limit Price
		Price price = (instrument.get_IsNormal() == isBuy) ? Price.subStract(ask, acceptLmtVariation) : Price.add(bid, acceptLmtVariation);
		if (this.isBetweenBidToAsk(price))
		{
			price = (instrument.get_IsNormal() == isBuy) ? Price.subStract(bid, instrument.get_NumeratorUnit()) :
				Price.add(ask, instrument.get_NumeratorUnit());
		}
		this.limitPirceTextField.setText(Price.toString(price));
	}

	private void fillDefaultStopPrice()
	{
		boolean isBuy = this.order.get_IsBuy();
		Instrument instrument = this.order.get_Transaction().get_Instrument();
		Price bid = instrument.get_LastQuotation().get_Bid();
		Price ask = instrument.get_LastQuotation().get_Ask();
		if (bid == null || ask == null)
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderSingleDQPageorderValidAlert0);
			return;
		}

		Account account = this.order.get_Account();
		BigDecimal lot = this.order.get_LotBalance();

		int acceptLmtVariation = instrument.get_AcceptLmtVariation(account, isBuy, lot, this.order, getPlaceRelation(), false);
		//Fill Stop Price
		Price price2 = (instrument.get_IsNormal() == isBuy) ? Price.add(ask, acceptLmtVariation) : Price.subStract(bid, acceptLmtVariation);
		if (this.isBetweenBidToAsk(price2))
		{
			price2 = (instrument.get_IsNormal() == isBuy) ? Price.add(ask, instrument.get_NumeratorUnit()) :
				Price.subStract(bid, instrument.get_NumeratorUnit());
		}
		this.stopPirceTextField.setText(Price.toString(price2));
	}

	private HashMap<Guid, RelationOrder> getPlaceRelation()
	{
		return this.order.get_RelationOrders();
	}


	private boolean isBetweenBidToAsk(Price setPrice)
	{
		if(Parameter.isAllowLimitInSpread)
		{
			return false;
		}
		else
		{
			Instrument instrument = this.order.get_Transaction().get_Instrument();
			return (!Price.less(setPrice, instrument.get_LastQuotation().get_Bid())
					&& !Price.more(setPrice, instrument.get_LastQuotation().get_Ask()));
		}
	}

	private void enableConfirmButton()
	{
		this.confirmButton.setEnabled(true);
	}

	private void jbInit()
	{
		this.setLayout(new GridBagLayout());

		lotStaticText.setText(Language.OrderLMTlblLot);
		this.add(lotStaticText, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
			, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 3, 0, 1), 0, 0));
		this.add(lotTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
			, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 3), 0, 0));
		this.lotTextField.setText(this.order.get_LotString());
		this.addDocumentListener(this.lotTextField);

		int y = 1;

		OrderType orderType = this.order.get_Transaction().get_OrderType();
		if(orderType == OrderType.Limit)
		{
			y = 2;
			if(this.order.get_TradeOption() == TradeOption.Better)
			{
				limitPirceStaticText = new PVStaticText2();
				limitPirceStaticText.setText(Language.OrderLMTlblSetPriceA3);
				this.add(limitPirceStaticText, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
					, GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 3, 0, 1), 0, 0));

				limitPirceTextField = new JFormattedTextField();
				this.add(limitPirceTextField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
					, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 3), 0, 0));
				this.limitPirceTextField.setText(this.order.get_SetPriceString());
				this.addDocumentListener(this.limitPirceTextField);
			}
			else
			{
				stopPirceStaticText = new PVStaticText2();
				stopPirceStaticText.setText(Language.OrderLMTlblSetPriceA2);
				this.add(stopPirceStaticText, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
					, GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 3, 0, 1), 0, 0));

				stopPirceTextField = new JFormattedTextField();
				this.add(stopPirceTextField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
					, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 3), 0, 0));
				this.stopPirceTextField.setText(this.order.get_SetPriceString());
				this.addDocumentListener(this.stopPirceTextField);
			}
		}
		else if(orderType == OrderType.StopLimit)
		{
			stopPirceStaticText = new PVStaticText2();
			stopPirceStaticText.setText(Language.OrderLMTlblSetPriceA2);
			this.add(stopPirceStaticText, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 3, 0, 1), 0, 0));

			stopPirceTextField = new JFormattedTextField();
			this.add(stopPirceTextField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 3), 0, 0));
			this.stopPirceTextField.setText(this.order.get_SetPrice2String());
			this.addDocumentListener(this.limitPirceTextField);

			limitPirceStaticText = new PVStaticText2();
			limitPirceStaticText.setText(Language.OrderLMTlblSetPriceA3);
			this.add(limitPirceStaticText, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 3, 0, 1), 0, 0));

			limitPirceTextField = new JFormattedTextField();
			this.add(limitPirceTextField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
				, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 3), 0, 0));
			this.limitPirceTextField.setText(this.order.get_SetPriceString());
			this.addDocumentListener(this.stopPirceTextField);

			y = 3;
		}

		this.setSize(180,  y == 1 ? 150 : (y == 2 ? 170 : 190));
		this.setResizable(true);

		this.confirmButton.setText(Language.Confirm);
		this.add(confirmButton, new GridBagConstraints(0, y, 1, 1, 1.0, 0.0
			, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(45, 3, 0, 1), 0, 0));
		exitButton.setText(Language.UnconfirmedInstructionbtnExit);
		this.add(exitButton, new GridBagConstraints(1, y, 1, 1, 1.0, 0.0
			, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(45, 10, 0, 3), 0, 0));
		this.confirmButton.setEnabled(false);
	}
}
