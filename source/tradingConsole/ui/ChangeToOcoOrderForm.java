package tradingConsole.ui;

import java.text.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import javax.swing.*;

import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import framework.StringHelper;
import framework.DateTime;
import framework.data.DataRow;
import java.math.BigDecimal;
import framework.data.DataTable;
import framework.Guid;
import framework.diagnostics.TraceType;
import java.util.Locale;

public class ChangeToOcoOrderForm  extends JDialog
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private Order _originalOrder;

	private GridBagLayout gridBagLayout = new GridBagLayout();

	private JLabel _instrumentLabel = new JLabel();

	private JLabel _orderTypeLabel = new JLabel("Order type");
	private JTextField _orderTypeField = new JTextField();

	private JLabel _accountLabel = new JLabel("Account");
	private JTextField _accountField = new JTextField();

	private JLabel _buySellLabel = new JLabel("Buy/Sell");
	private JTextField _buySellField = new JTextField();

	private JLabel _lotLabel = new JLabel("Lot");
	private JTextField _lotField = new JTextField();

	private JLabel _stopPriceLabel = new JLabel("Stop price");
	JTextField _stopPriceField = new JTextField();

	private JLabel _limitPriceLabel = new JLabel("Limit price");
	JTextField _limitPriceField = new JTextField();

	private JLabel _expireTimeLabel = new JLabel("Expire time");
	private JTextField _expireTimeField = new JTextField();

	private JList _closeOrdersGrid = new JList();

	private JButton _submitButton = new JButton();
	private JButton _exitButton = new JButton();
	private boolean _isCanceled = false;

	public ChangeToOcoOrderForm(JDialog parent, TradingConsole tradingConsole, SettingsManager settingsManager, Order originalOrder)
	{
		super(parent, true);
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._originalOrder = originalOrder;

		jbInit();

		this.initializeOpenOrders();

		if(this._originalOrder.get_TradeOption() == TradeOption.Stop)
		{
			this._limitPriceField.requestFocus();
		}
		else
		{
			this._stopPriceField.requestFocus();
		}
		this.fillDefaultSetPrice(true);
	}

	public boolean get_IsCanceled()
	{
		return this._isCanceled;
	}

	private void jbInit()
	{
		this.setSize(430, 285);
		//this.setResizable(false);
		this.setTitle(Language.limitOrderFormTitle);
		this.setBackground(FormBackColor.limitOrderForm);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setModal(true);

		this.getContentPane().setLayout(gridBagLayout);

		_instrumentLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
		_instrumentLabel.setText(this._originalOrder.get_Transaction().get_Instrument().get_Description());

		Font font = new Font("SansSerif", Font.BOLD, 12);
		_orderTypeLabel.setFont(font);
		_orderTypeLabel.setText(Language.OrderLMTlblOrderTypeA);
		if(_originalOrder.get_TradeOption() == TradeOption.Stop)
		{
			_orderTypeField.setText(Language.PlaceLimitOrder);
		}
		else
		{
			_orderTypeField.setText(Language.PlaceStopOrder);
		}
		_orderTypeField.setEditable(false);

		_accountLabel.setFont(font);
		_accountLabel.setText(Language.OrderLMTlblAccountCodeA);
		_accountField.setText(_originalOrder.get_AccountCode());
		_accountField.setEditable(false);

		_buySellLabel.setFont(font);
		_buySellLabel.setText(Language.OrderLMTlblIsBuyA);
		_buySellField.setText(_originalOrder.get_IsBuy() ? Language.LongBuy : Language.LongSell);
		_buySellField.setEditable(false);

		_lotLabel.setFont(font);
		_lotLabel.setText(Language.OrderLMTlblLot);
		_lotField.setText(_originalOrder.get_LotString());
		_lotField.setEditable(false);

		_stopPriceLabel.setFont(font);
		_stopPriceLabel.setText(Language.TradeOptionPromptStop);
		_limitPriceLabel.setFont(font);
		_limitPriceLabel.setText(Language.TradeOptionPromptBetter);
		if(_originalOrder.get_TradeOption() == TradeOption.Stop)
		{
			_stopPriceField.setText(_originalOrder.get_SetPriceString());
			_stopPriceField.setEditable(false);
			FocusListener focusListener = new FocusListener()
			{
				public void focusGained(FocusEvent e)
				{
					String text = _limitPriceField.getText();
					_limitPriceField.select( ( (text.indexOf(".") != -1) ? text.indexOf(".") + 1 : 0), text.length());
				}

				public void focusLost(FocusEvent e)
				{
				}
			};
			_limitPriceField.addFocusListener(focusListener);
			_limitPriceField.requestFocus();
		}
		else
		{
			_limitPriceField.setText(_originalOrder.get_SetPriceString());
			_limitPriceField.setEditable(false);
			FocusListener focusListener = new FocusListener()
			{
				public void focusGained(FocusEvent e)
				{
					String text = _stopPriceField.getText();
					_stopPriceField.select( ( (text.indexOf(".") != -1) ? text.indexOf(".") + 1 : 0), text.length());
				}

				public void focusLost(FocusEvent e)
				{
				}
			};
			_stopPriceField.addFocusListener(focusListener);
			_stopPriceField.requestFocus();
		}
		Color color = BuySellColor.getColor(_originalOrder.get_IsBuy(), false);
		_stopPriceField.setForeground(color);
		_limitPriceField.setForeground(color);
		_lotField.setForeground(color);
		_stopPriceField.setFont(font);
		_limitPriceField.setFont(font);
		_lotField.setFont(font);

		_expireTimeLabel.setFont(font);
		_expireTimeLabel.setText(Language.ExpireOrderPrompt);
		_expireTimeField.setText(_originalOrder.get_EndTime().toString());
		_expireTimeField.setEditable(false);

		_submitButton.setText(Language.OrderLMTbtnSubmit);
		ActionListener submitButtonActionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				submit();
			}
		};
		_submitButton.addActionListener(submitButtonActionListener);

		_exitButton.setText(Language.OrderLMTbtnExit);
		ActionListener exitButtonActionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				_isCanceled = true;
				dispose();
			}
		};
		_exitButton.addActionListener(exitButtonActionListener);

		this.getContentPane().add(_instrumentLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 0, 0), 80, 0));

		this.getContentPane().add(_orderTypeLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 0, 0), 10, 0));
		this.getContentPane().add(_orderTypeField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 2, 0, 0), 20, 0));

		this.getContentPane().add(_accountLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 0, 0), 10, 0));
		this.getContentPane().add(_accountField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 20, 0));

		this.getContentPane().add(_buySellLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 0, 0), 10, 0));
		this.getContentPane().add(_buySellField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 20, 0));

		this.getContentPane().add(_lotLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 0, 0), 10, 0));
		this.getContentPane().add(_lotField, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 20, 0));

		this.getContentPane().add(_stopPriceLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 0, 0), 10, 0));
		this.getContentPane().add(_stopPriceField, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 20, 0));

		this.getContentPane().add(_limitPriceLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 0, 0), 10, 0));
		this.getContentPane().add(_limitPriceField, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 20, 0));

		this.getContentPane().add(_expireTimeLabel, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 0, 0), 10, 0));
		this.getContentPane().add(_expireTimeField, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 135, 5));

		_closeOrdersGrid.setFont(font);
		JScrollPane scrollPane = new JScrollPane(_closeOrdersGrid);
		this.getContentPane().add(scrollPane, new GridBagConstraints(2, 1, 2, 7, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 0, 10), 0, 0));

		this.getContentPane().add(_submitButton, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 10, 0), 20, 0));
		this.getContentPane().add(_exitButton, new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 0, 10, 10), 20, 0));
	}

	private void fillDefaultSetPrice(boolean showAlterDialog)
	{
		boolean isBuy = this._originalOrder.get_IsBuy();
		boolean isOpen = this._originalOrder.get_IsOpen();
		Instrument instrument = this._originalOrder.get_Transaction().get_Instrument();
		Price bid = instrument.get_LastQuotation().get_Bid();
		Price ask = instrument.get_LastQuotation().get_Ask();
		if (bid == null || ask == null)
		{
			if(showAlterDialog)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderSingleDQPageorderValidAlert0);
			}
			return;
		}
		int acceptLmtVariation = instrument.get_AcceptLmtVariation(isOpen);

		if(this._originalOrder.get_TradeOption() == TradeOption.Stop)
		{
			if(this._originalOrder.getFirstOpenOrder().get_Phase() == Phase.Executed)
			{
				//Fill Limit Price
				Price price = (instrument.get_IsNormal() == isBuy) ? Price.subStract(ask, acceptLmtVariation) : Price.add(bid, acceptLmtVariation);
				if (this.isBetweenBidToAsk(price, instrument))
				{
					price = (instrument.get_IsNormal() == isBuy) ? Price.subStract(bid, instrument.get_NumeratorUnit()) :
						Price.add(ask, instrument.get_NumeratorUnit());
				}
				this._limitPriceField.setText(Price.toString(price));
			}
			else
			{
				Price setPrice = this._originalOrder.getFirstOpenOrder().get_SetPrice();
				int acceptIfDoneVariation = instrument.get_AcceptIfDoneVariation();
				Price price = (instrument.get_IsNormal() == isBuy) ? Price.subStract(setPrice, acceptIfDoneVariation) : Price.add(setPrice, acceptIfDoneVariation);
				this._limitPriceField.setText(Price.toString(price));
			}
		}
		else
		{
			if(this._originalOrder.getFirstOpenOrder() == null) this._originalOrder.initailizeCloseOrders();
			if(this._originalOrder.getFirstOpenOrder().get_Phase() == Phase.Executed)
			{
				//Fill Stop Price
				Price price = (instrument.get_IsNormal() == isBuy) ? Price.add(ask, acceptLmtVariation) : Price.subStract(bid, acceptLmtVariation);
				if (this.isBetweenBidToAsk(price, instrument))
				{
					price = (instrument.get_IsNormal() == isBuy) ? Price.add(ask, instrument.get_NumeratorUnit()) :
						Price.subStract(bid, instrument.get_NumeratorUnit());
				}
				this._stopPriceField.setText(Price.toString(price));
			}
			else
			{
				int spread = this._settingsManager.getQuotePolicyDetail(instrument.get_Id()).get_Spread() + instrument.get_NumeratorUnit();
				int acceptIfDoneVariation = instrument.get_AcceptIfDoneVariation();
				spread = Math.max(spread, acceptIfDoneVariation);
				Price setPrice = this._originalOrder.getFirstOpenOrder().get_SetPrice();
				Price price = (instrument.get_IsNormal() == isBuy) ? Price.add(setPrice, spread) : Price.subStract(setPrice, spread);
				this._stopPriceField.setText(Price.toString(price));
			}
		}
	}

	private void initializeOpenOrders()
	{
		String[] openOrders = StringHelper.split(this._originalOrder.get_PeerOrderCodes(), TradingConsole.delimiterRow);
		_closeOrdersGrid.setListData(openOrders);
	}

	private void submit()
	{
		if(!this.validatePrice())
		{
			this.fillDefaultSetPrice(false);
		}
		else
		{
			Instrument instrument = this._originalOrder.get_Transaction().get_Instrument();
			Account account = this._originalOrder.get_Transaction().get_Account();
			Object[] result2 = MakeLimitOrder.isAllowTime(this._settingsManager, instrument, account);
			if (! (Boolean) result2[0])
			{
				String errorMessage = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
				AlertDialogForm.showDialog(this, null, true, errorMessage);
				return;
			}

			Transaction transaction = new Transaction(this._tradingConsole, this._settingsManager, this._originalOrder);
			DateTime appTime = TradingConsoleServer.appTime();
			DateTime beginTime = appTime.addMinutes(0 - Parameter.orderBeginTimeDiff);

			for(Order order : this.createOrders(transaction))
			{
				transaction.get_Orders().put(order.get_Id(), order);
			}
			transaction.changeToOcoOrder(beginTime, this._originalOrder.get_EndTime(), appTime);
			this.dispose();
		}
	}

	private Order[] createOrders(Transaction transaction)
	{
		DataTable orderDataTable = Order.createStructure();
		DataRow orderDataRow = orderDataTable.newRow();
		orderDataRow.set_Item("ID", Guid.newGuid());
		TradeOption tradeOption = this._originalOrder.get_TradeOption() == TradeOption.Better ? TradeOption.Stop : TradeOption.Better;
		orderDataRow.set_Item("TradeOption", (short)tradeOption.value());
		orderDataRow.set_Item("IsOpen", false);
		orderDataRow.set_Item("IsBuy", this._originalOrder.get_IsBuy());

		String setPrice = this._originalOrder.get_TradeOption() == TradeOption.Stop ? this._limitPriceField.getText() : this._stopPriceField.getText();
		orderDataRow.set_Item("SetPrice", setPrice);
		orderDataRow.set_Item("Lot", this._originalOrder.get_Lot());
		orderDataRow.set_Item("LotBalance", this._originalOrder.get_Lot());
		orderDataRow.set_Item("Code", "");
		orderDataRow.set_Item("ExecutePrice", "");
		orderDataRow.set_Item("CommissionSum", BigDecimal.ZERO);
		orderDataRow.set_Item("LevySum", BigDecimal.ZERO);
		orderDataRow.set_Item("PeerOrderCodes", "");
		orderDataRow.set_Item("InterestPerLot", BigDecimal.ZERO);
		orderDataRow.set_Item("StoragePerLot", BigDecimal.ZERO);
		orderDataRow.set_Item("TradePLFloat", BigDecimal.ZERO);
		orderDataRow.set_Item("InterestPLFloat", BigDecimal.ZERO);
		orderDataRow.set_Item("StoragePLFloat", BigDecimal.ZERO);
		orderDataRow.set_Item("DQMaxMove", 0);

		Order newOrder = new Order(this._tradingConsole, this._settingsManager, orderDataRow, transaction);
		Order oldOrder = this._originalOrder.clone(transaction);
		oldOrder.set_IsAssignOrder(true);
		for(RelationOrder relationOrder : this._originalOrder.get_RelationOrders().values())
		{
			oldOrder.set_PeerOrderIDs(this._originalOrder.get_PeerOrderIDs());
			newOrder.set_PeerOrderIDs(this._originalOrder.get_PeerOrderIDs());
			oldOrder.addRelationOrder(relationOrder.get_OpenOrder(), relationOrder.get_CloseLot());
			newOrder.addRelationOrder(relationOrder.get_OpenOrder(), relationOrder.get_CloseLot());
		}
		TradingConsole.traceSource.trace(TraceType.Information, "[ChangeToOcoOrder.createOrders]oldOrder = " + oldOrder + oldOrder.get_TradeOption().name());
		TradingConsole.traceSource.trace(TraceType.Information, "[ChangeToOcoOrder.createOrders]newOrder = " + newOrder + newOrder.get_TradeOption().name());
		return new Order[]{oldOrder, newOrder};
	}

	private boolean validatePrice()
	{
		boolean isValidOrder = true;

		Instrument instrument = this._originalOrder.get_Transaction().get_Instrument();
		String setPrice = null;

		if(!MakeOrder.isAllowOrderType(instrument, OrderType.OneCancelOther))
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
			return false;
		}

		if(this._originalOrder.get_TradeOption() == TradeOption.Stop)
		{
			setPrice = this._limitPriceField.getText();
		}
		else
		{
			setPrice = this._stopPriceField.getText();
		}
		Price price = Price.parse(setPrice, instrument.get_NumeratorUnit(), instrument.get_Denominator());
		if (price == null)
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert1);
			isValidOrder = false;
		}
		else if (this._originalOrder.getFirstOpenOrder().get_Phase() == Phase.Executed && this.isBetweenBidToAsk(price, instrument))
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert8);
			isValidOrder = false;
		}
		else if (this._originalOrder.getFirstOpenOrder().get_Phase() != Phase.Executed)
		{
			int spread = this._settingsManager.getQuotePolicyDetail(instrument.get_Id()).get_Spread() + instrument.get_NumeratorUnit();
			int acceptIfDoneVariation = instrument.get_AcceptIfDoneVariation();
			Price originalSetPrice = this._originalOrder.getFirstOpenOrder().get_SetPrice();
			boolean isBuy = this._originalOrder.get_IsBuy();
			if(this._originalOrder.get_TradeOption() == TradeOption.Stop)
			{
				if((instrument.get_IsNormal() != isBuy) ? price.compareTo(Price.add(originalSetPrice, acceptIfDoneVariation)) < 0 : price.compareTo(Price.subStract(originalSetPrice, acceptIfDoneVariation)) > 0)
				{
					AlertDialogForm.showDialog(this, null, true, Language.InvalidSetPrice);
					isValidOrder = false;
				}
			}
			else
			{
				spread += instrument.get_NumeratorUnit();
				spread = Math.max(spread, acceptIfDoneVariation);
				Price comparePrice = isBuy ? Price.add(originalSetPrice, spread) : Price.subStract(originalSetPrice, spread);
				if ( (instrument.get_IsNormal() != isBuy) ? price.compareTo(comparePrice) > 0 :
						price.compareTo(comparePrice) < 0)
				{
					AlertDialogForm.showDialog(this, null, true, Language.InvalidSetPrice);
					isValidOrder = false;
				}
			}
		}
		else
		{
			Price marketPrice = this.getMarketPrice(instrument);
			TradeOption previousTradeOption = this._originalOrder.get_TradeOption() == TradeOption.Stop ? TradeOption.Better : TradeOption.Stop;
			SetPriceError setPriceError = Order.checkLMTOrderSetPrice(true, instrument, this._originalOrder.get_IsBuy(), previousTradeOption, price,
				marketPrice, this._originalOrder.get_IsOpen());
			if(setPriceError != SetPriceError.Ok) isValidOrder = false;

			double dblMarketPrice = Price.toDouble(marketPrice);
			if (Math.abs(Price.toDouble(price) - dblMarketPrice) > dblMarketPrice * 0.2)
			{
				setPriceError = SetPriceError.SetPriceTooFarAwayMarket;
			}

			if (setPriceError == SetPriceError.SetPriceTooCloseMarket)
			{
				if (this._settingsManager.get_SystemParameter().get_DisplayLmtStopPoints())
				{
					boolean isOpen = this._originalOrder.get_IsOpen();
					AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert2 + " "
											   + instrument.get_AcceptLmtVariation(isOpen) + " " + Language.OrderLMTPageorderValidAlert22);
				}
				else
				{
					AlertDialogForm.showDialog(this, null, true, Language.SetPriceTooCloseToMarket);
				}
				isValidOrder = false;
			}
			if (setPriceError == SetPriceError.SetPriceTooFarAwayMarket)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert3);
				isValidOrder = false;
			}
			if (setPriceError == SetPriceError.InvalidSetPrice)
			{
				AlertDialogForm.showDialog(this, null, true, Language.InvalidSetPrice);
				isValidOrder = false;
			}
		}
		return isValidOrder;
	}

	private Price getMarketPrice(Instrument instrument)
	{
		return instrument.get_LastQuotation().getBuySell(this._originalOrder.get_IsBuy());
	}

	private boolean isBetweenBidToAsk(Price price, Instrument instrument)
	{
		if(Parameter.isAllowLimitInSpread)
		{
			return false;
		}
		else
		{
			return (!Price.less(price, instrument.get_LastQuotation().get_Bid())
					&& !Price.more(price, instrument.get_LastQuotation().get_Ask()));
		}
	}
}
