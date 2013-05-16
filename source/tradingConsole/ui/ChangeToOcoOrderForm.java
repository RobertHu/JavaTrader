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
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Iterator;

public class ChangeToOcoOrderForm  extends JDialog implements IPriceSpinnerSite
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
	PriceSpinner _stopPriceField;

	private JLabel _limitPriceLabel = new JLabel("Limit price");
	PriceSpinner _limitPriceField;

	private JLabel _expireTimeLabel = new JLabel("Expire time");
	private JTextField _expireTimeField = new JTextField();

	private JList _closeOrdersGrid = new JList();

	private JButton _submitButton = new JButton();
	private JButton _exitButton = new JButton();
	private boolean _isCanceled = false;
	private JScrollPane openOrdersPanel;
	private JPanel ifDonePanel;

	private JCheckBox ocoCheckBox = new JCheckBox();
	private JCheckBox ifDoneCheckBox = new JCheckBox();

	private JCheckBox modifyLimitPriceBox = new JCheckBox();
	private JCheckBox modifyStopPriceBox = new JCheckBox();
	private ActionListener stopLimitPriceBoxActionListener;
	private ActionListener modifyLimitPriceBoxActionListener;

	private JPanel ifLimitDonePanel;
	private TitledBorder ifLimitDonePanelTitledBorder = BorderFactory.createTitledBorder("");
	private PVStaticText2 limitPriceStaticTextForIfLimitDone = new PVStaticText2();
	private PriceSpinner limitPriceEditForIfLimitDone;
	private PVStaticText2 stopPriceStaticTextForIfLimitDone = new PVStaticText2();
	private PriceSpinner stopPriceEditForIfLimitDone;
	private JCheckBox limitCheckBoxForIfLimitDone = new JCheckBox();
	private JCheckBox stopCheckBoxForIfLimitDone = new JCheckBox();

	private JPanel ifStopDonePanel;
	private TitledBorder ifStopDonePanelTitledBorder = BorderFactory.createTitledBorder("");
	private PVStaticText2 limitPriceStaticTextForIfStopDone = new PVStaticText2();
	private PriceSpinner limitPriceEditForIfStopDone;
	private PVStaticText2 stopPriceStaticTextForIfStopDone = new PVStaticText2();
	private PriceSpinner stopPriceEditForIfStopDone;
	private JCheckBox limitCheckBoxForIfStopDone = new JCheckBox();
	private JCheckBox stopCheckBoxForIfStopDone = new JCheckBox();

	private IfDoneInfo ifDoneInfo;

	public ChangeToOcoOrderForm(JDialog parent, TradingConsole tradingConsole, SettingsManager settingsManager, Order originalOrder)
	{
		super(parent, true);
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._originalOrder = originalOrder;

		this._stopPriceField = new PriceSpinner(this);
		this._limitPriceField = new PriceSpinner(this);
		this.limitPriceEditForIfLimitDone = new PriceSpinner(this);
		this.limitPriceEditForIfStopDone = new PriceSpinner(this);
		this.stopPriceEditForIfLimitDone = new PriceSpinner(this);
		this.stopPriceEditForIfStopDone = new PriceSpinner(this);

		this.addPriceChangedListener(this._stopPriceField);
		this.addPriceChangedListener(this._limitPriceField);

		this.addDonePriceChangedListener(this.limitPriceEditForIfLimitDone);
		this.addDonePriceChangedListener(this.limitPriceEditForIfStopDone);
		this.addDonePriceChangedListener(this.stopPriceEditForIfLimitDone);
		this.addDonePriceChangedListener(this.stopPriceEditForIfStopDone);

		jbInit();
		this.fillOriginalInfo();
		this.updateSubmitButtonStatus();

		this.initializeOpenOrders();

		if(this._originalOrder.get_TradeOption() == TradeOption.Stop)
		{
			this._limitPriceField.requestFocus();
		}
		else
		{
			this._stopPriceField.requestFocus();
		}
	}

	private void fillOriginalInfo()
	{
		this.ocoCheckBox.setEnabled(false);
		this.modifyLimitPriceBox.setEnabled(true);
		this._limitPriceField.setEditable(false);
		this._limitPriceField.setEnabled(false);

		this.modifyStopPriceBox.setEnabled(true);
		this._stopPriceField.setEditable(false);
		this._stopPriceField.setEnabled(false);

		this.limitCheckBoxForIfLimitDone.setEnabled(true);
		this.limitPriceEditForIfLimitDone.setEditable(false);
		this.limitPriceEditForIfLimitDone.setEnabled(false);

		this.stopCheckBoxForIfLimitDone.setEnabled(true);
		this.stopPriceEditForIfLimitDone.setEditable(false);
		this.stopPriceEditForIfLimitDone.setEnabled(false);

		this.limitCheckBoxForIfStopDone.setEnabled(true);
		this.limitPriceEditForIfStopDone.setEditable(false);
		this.limitPriceEditForIfStopDone.setEnabled(false);

		this.stopCheckBoxForIfStopDone.setEnabled(true);
		this.stopPriceEditForIfStopDone.setEditable(false);
		this.stopPriceEditForIfStopDone.setEnabled(false);

		boolean canChangeToOCO = this.canChangeToOCO();
		boolean canChangeToIfDone = this.canChangeToIfDone();
		if(!canChangeToOCO)
		{
			this.ocoCheckBox.setSelected(false);
			this.ocoCheckBox.setVisible(false);
		}
		if(!canChangeToIfDone)
		{
			this.ifDoneCheckBox.setSelected(false);
			this.ifDoneCheckBox.setVisible(false);
			this.ifDonePanel.setVisible(false);
			this.openOrdersPanel.setVisible(true);
		}

		Order theOtherOrder = null;
		boolean isOCO = this._originalOrder.get_Transaction().get_Type() == TransactionType.OneCancelOther;
		if(isOCO)
		{
			this.ocoCheckBox.setVisible(true);
			this.ocoCheckBox.setSelected(true);
			this.ocoCheckBox.setEnabled(false);

			theOtherOrder = getTheOtherOrder();
		}

		if(this._originalOrder.get_TradeOption() == TradeOption.Better)
		{
			this._limitPriceField.setText(Price.toString(this._originalOrder.get_SetPrice()), true);
			this._stopPriceField.setText(theOtherOrder == null ? "" : Price.toString(theOtherOrder.get_SetPrice()), true);
			if(!canChangeToOCO) this.modifyStopPriceBox.setEnabled(false);
		}
		else
		{
			this._stopPriceField.setText(Price.toString(this._originalOrder.get_SetPrice()), true);
			this._limitPriceField.setText(theOtherOrder == null ? "" : Price.toString(theOtherOrder.get_SetPrice()), true);
			if(!canChangeToOCO) this.modifyLimitPriceBox.setEnabled(false);
		}

		if(this._originalOrder.get_Transaction().get_SubType() == TransactionSubType.IfDone
			&& this._originalOrder.get_IsOpen())
		{
			this.limitPriceEditForIfLimitDone.setText("", true);
			this.stopPriceEditForIfLimitDone.setText("", true);
			this.limitPriceEditForIfStopDone.setText("", true);
			this.stopPriceEditForIfStopDone.setText("", true);

			this.fillOriginalDoneOrderInfo(this._originalOrder);
			if(theOtherOrder != null && theOtherOrder.get_IsOpen()
			   && theOtherOrder.get_Transaction().get_SubType() == TransactionSubType.IfDone)
			{
				this.fillOriginalDoneOrderInfo(theOtherOrder);
			}
		}
	}

	private Order getTheOtherOrder()
	{
		Order theOtherOrder = null;
		 for (Iterator<Order> iterator = this._originalOrder.get_Transaction().get_Orders().values().iterator(); iterator.hasNext(); )
		 {
			 Order order = iterator.next();
			 if (!order.get_Id().equals(this._originalOrder.get_Id()))
			 {
				 theOtherOrder = order;
				 break;
			 }
		 }
		return theOtherOrder;
	}

	private void fillOriginalDoneOrderInfo(Order order)
	{
		BindingSource closeOrders = order.get_CloseOrders();
		if (closeOrders == null || closeOrders.getRowCount() == 0)
			return;

		this.ifDoneCheckBox.setSelected(true);

		for (int index = 0; index < closeOrders.getRowCount(); index++)
		{
			CloseOrder item = (CloseOrder)closeOrders.getObject(index);
			Order closeOrder = item.get_Order();
			if (closeOrder.get_Transaction().get_SubType() == TransactionSubType.IfDone)
			{
				String price = Price.toString(closeOrder.get_SetPrice());
				if (closeOrder.get_TradeOption() == TradeOption.Better)
				{
					if (order.get_TradeOption() == TradeOption.Better)
					{
						this.limitPriceEditForIfLimitDone.setText(price, true);
					}
					else if (order.get_TradeOption() == TradeOption.Stop)
					{
						this.limitPriceEditForIfStopDone.setText(price, true);
					}
				}
				else if (closeOrder.get_TradeOption() == TradeOption.Stop)
				{
					if (order.get_TradeOption() == TradeOption.Better)
					{
						this.stopPriceEditForIfLimitDone.setText(price, true);
					}
					else if (order.get_TradeOption() == TradeOption.Stop)
					{
						this.stopPriceEditForIfStopDone.setText(price, true);
					}
				}
			}
		}
	}

	private void addDonePriceChangedListener(PriceSpinner priceField)
	{
		priceField.getDocument().addDocumentListener(new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e)
			{
				updateSubmitButtonStatus();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateSubmitButtonStatus();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateSubmitButtonStatus();
			}
	});
	}


	private void addPriceChangedListener(PriceSpinner priceField)
	{
		priceField.getDocument().addDocumentListener(new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e)
			{
				updateIfDoneTitle();
				updateSubmitButtonStatus();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateIfDoneTitle();
				updateSubmitButtonStatus();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateIfDoneTitle();
				updateSubmitButtonStatus();
			}
	});
	}

	public Instrument getInstrument()
	{
		return this._originalOrder.get_Instrument();
	}

	public boolean get_IsCanceled()
	{
		return this._isCanceled;
	}

	private void jbInit()
	{
		this.setSize(430, 305);
		//this.setResizable(false);
		this.setTitle(Language.limitOrderFormTitle);
		this.setBackground(FormBackColor.limitOrderForm);
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setModal(true);

		this.getContentPane().setLayout(gridBagLayout);

		Font font = new Font("SansSerif", Font.BOLD, 13);
		_instrumentLabel.setFont(font);
		_instrumentLabel.setText(this._originalOrder.get_Transaction().get_Instrument().get_Description());
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

		font = new Font("SansSerif", Font.BOLD, 12);
		limitPriceStaticTextForIfStopDone.setFont(font);
		stopPriceStaticTextForIfStopDone.setFont(font);
		limitPriceStaticTextForIfLimitDone.setFont(font);
		stopPriceStaticTextForIfLimitDone.setFont(font);

		ifLimitDonePanel.setLayout(new GridBagLayout());
		ifLimitDonePanel.add(limitPriceStaticTextForIfLimitDone, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 2), 0, 0));
		ifLimitDonePanel.add(this.limitCheckBoxForIfLimitDone, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 2), 0, 0));
		ifLimitDonePanel.add(limitPriceEditForIfLimitDone, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 2), 30, 0));

		limitCheckBoxForIfLimitDone.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				limitCheckBoxForIfLimitDoneChanged();
			}
		});

		stopCheckBoxForIfLimitDone.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				stopCheckBoxForIfLimitDoneChanged();
			}
		});

		limitCheckBoxForIfStopDone.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				limitCheckBoxForIfStopDoneChanged();
			}
		});

		stopCheckBoxForIfStopDone.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				stopCheckBoxForIfStopDoneChanged();
			}
		});

		ifLimitDonePanel.add(stopPriceStaticTextForIfLimitDone, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 2), 0, 0));
		ifLimitDonePanel.add(this.stopCheckBoxForIfLimitDone, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 2), 0, 0));
		ifLimitDonePanel.add(stopPriceEditForIfLimitDone, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 0, 2), 30, 0));

		ifStopDonePanel.setLayout(new GridBagLayout());
		ifStopDonePanel.add(limitPriceStaticTextForIfStopDone, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 2), 0, 0));
		ifStopDonePanel.add(this.limitCheckBoxForIfStopDone, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 2), 0, 0));
		ifStopDonePanel.add(limitPriceEditForIfStopDone, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 2), 30, 0));

		ifStopDonePanel.add(stopPriceStaticTextForIfStopDone, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 0, 2), 0, 0));
		ifStopDonePanel.add(this.stopCheckBoxForIfStopDone, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 2), 0, 0));
		ifStopDonePanel.add(stopPriceEditForIfStopDone, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 0, 2), 30, 0));

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
			/*_stopPriceField.setText(_originalOrder.get_SetPriceString());
			_stopPriceField.setEditable(false);
			_stopPriceField.setEnabled(false);

			_limitPriceField.setEditable(true);
			_limitPriceField.setEnabled(true);*/

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
			/*_limitPriceField.setText(_originalOrder.get_SetPriceString());
			_limitPriceField.setEditable(false);
			_limitPriceField.setEnabled(false);
			_stopPriceField.setEditable(true);
			_stopPriceField.setEnabled(true);*/

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

		color = BuySellColor.getColor(!_originalOrder.get_IsBuy(), false);
		limitPriceEditForIfLimitDone.setForeground(color);
		stopPriceEditForIfLimitDone.setForeground(color);
		limitPriceEditForIfStopDone.setForeground(color);
		stopPriceEditForIfStopDone.setForeground(color);

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

		/*this.getContentPane().add(_orderTypeLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 0, 0), 10, 0));
		this.getContentPane().add(_orderTypeField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 2, 0, 0), 20, 0));*/

		this.getContentPane().add(_accountLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(15, 10, 0, 0), 10, 0));
		this.getContentPane().add(_accountField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(15, 2, 0, 0), 20, 0));

		this.getContentPane().add(_buySellLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 0, 0), 10, 0));
		this.getContentPane().add(_buySellField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 20, 0));

		this.getContentPane().add(_lotLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 0, 0), 10, 0));
		this.getContentPane().add(_lotField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 20, 0));

		this.getContentPane().add(_stopPriceLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 0, 0), 10, 0));
		JPanel stopPricePanel = new JPanel();
		stopPricePanel.setLayout(new GridBagLayout());
		modifyStopPriceBox.setText(Language.ModifyCaption);
		stopPricePanel.add(modifyStopPriceBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		stopPricePanel.add(_stopPriceField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0));

		this.getContentPane().add(stopPricePanel, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, -2, 0, 0), 0, 0));


		this.getContentPane().add(_limitPriceLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 0, 0), 10, 0));

		JPanel limitPricePanel = new JPanel();
		limitPricePanel.setLayout(new GridBagLayout());
		modifyLimitPriceBox.setText(Language.ModifyCaption);
		limitPricePanel.add(modifyLimitPriceBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		limitPricePanel.add(_limitPriceField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0));

		this.limitCheckBoxForIfLimitDone.setText(Language.ModifyCaption);
		this.stopCheckBoxForIfLimitDone.setText(Language.ModifyCaption);
		this.limitCheckBoxForIfStopDone.setText(Language.ModifyCaption);
		this.stopCheckBoxForIfStopDone.setText(Language.ModifyCaption);

		this.getContentPane().add(limitPricePanel, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, -2, 0, 0), 0, 0));

		JPanel panel = new JPanel();
		panel.setBackground(null);
		this.add(panel, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, -2, 0, 0), 0, 0));

		panel.setLayout(new GridBagLayout());
		panel.add(ocoCheckBox, new GridBagConstraints(0, 0, 1, 1, 0.5, 1.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(ifDoneCheckBox, new GridBagConstraints(1, 0, 1, 1, 0.5, 1.0
			, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		ocoCheckBox.setText(Language.OCOPrompt);
		ifDoneCheckBox.setText(Language.IfDonePrompt);

		ActionListener actionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				handleIfDoneCheckBoxSelectedChanged();
			}
		};
		ifDoneCheckBox.addActionListener(actionListener);

		modifyLimitPriceBoxActionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				handleModifyLimitPriceBoxSelectedChanged();
			}
		};
		modifyLimitPriceBox.addActionListener(modifyLimitPriceBoxActionListener);

		stopLimitPriceBoxActionListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				handleModifyStopPriceBoxSelectedChanged();
			}
		};
		modifyStopPriceBox.addActionListener(stopLimitPriceBoxActionListener);

		this.getContentPane().add(_expireTimeLabel, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 0, 0), 10, 0));
		this.getContentPane().add(_expireTimeField, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 135, 5));

		_closeOrdersGrid.setFont(font);
		openOrdersPanel = new JScrollPane(_closeOrdersGrid);
		this.getContentPane().add(openOrdersPanel, new GridBagConstraints(2, 1, 2, 7, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 0, 10), 0, 0));

		ifDonePanel = new JPanel();
		ifDonePanel.setBackground(null);
		this.add(ifDonePanel, new GridBagConstraints(2, 1, 2, 7, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 10, 0, 10), 0, 0));
		ifDonePanel.setVisible(this._originalOrder.get_IsOpen());
		openOrdersPanel.setVisible(!this._originalOrder.get_IsOpen());

		ifDonePanel.setLayout(new GridBagLayout());
		ifDonePanel.add(ifLimitDonePanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.5
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		ifDonePanel.add(ifStopDonePanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.5
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		this.getContentPane().add(_submitButton, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 10, 0), 20, 0));
		this.getContentPane().add(_exitButton, new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 0, 10, 10), 20, 0));
	}

	private void limitCheckBoxForIfLimitDoneChanged()
	{
		if (this.limitCheckBoxForIfLimitDone.isSelected())
		{
			Instrument instrument = this.getInstrument();
			Price setPrice = Price.parse(this._limitPriceField.getText(), instrument.get_NumeratorUnit(), instrument.get_Denominator());
			Price price = this.getLimitPriceForDone(setPrice);
			this.limitPriceEditForIfLimitDone.enableEdit();
			this.limitPriceEditForIfLimitDone.setText(Price.toString(price));
		}
		else
		{
			this.limitPriceEditForIfLimitDone.disableEdit();
			this.limitPriceEditForIfLimitDone.resetValue();
		}
		this.updateSubmitButtonStatus();
	}

	private void stopCheckBoxForIfLimitDoneChanged()
	{
		if (this.stopCheckBoxForIfLimitDone.isSelected())
		{
			Instrument instrument = this.getInstrument();
			Price setPrice = Price.parse(this._limitPriceField.getText(), instrument.get_NumeratorUnit(), instrument.get_Denominator());
			Price price = this.getStopPriceForDone(setPrice);
			this.stopPriceEditForIfLimitDone.enableEdit();
			this.stopPriceEditForIfLimitDone.setText(Price.toString(price));
		}
		else
		{
			this.stopPriceEditForIfLimitDone.disableEdit();
			this.stopPriceEditForIfLimitDone.resetValue();
		}
		this.updateSubmitButtonStatus();
	}

	private void stopCheckBoxForIfStopDoneChanged()
	{
		if (this.stopCheckBoxForIfStopDone.isSelected())
		{
			Instrument instrument = this.getInstrument();
			Price setPrice = Price.parse(this._stopPriceField.getText(), instrument.get_NumeratorUnit(), instrument.get_Denominator());
			Price price = this.getStopPriceForDone(setPrice);
			this.stopPriceEditForIfStopDone.enableEdit();
			this.stopPriceEditForIfStopDone.setText(Price.toString(price));
		}
		else
		{
			this.stopPriceEditForIfStopDone.disableEdit();
			this.stopPriceEditForIfStopDone.resetValue();
		}
		this.updateSubmitButtonStatus();
	}

	private void limitCheckBoxForIfStopDoneChanged()
	{
		if (this.limitCheckBoxForIfStopDone.isSelected())
		{
			Instrument instrument = this.getInstrument();
			Price setPrice = Price.parse(this._stopPriceField.getText(), instrument.get_NumeratorUnit(), instrument.get_Denominator());
			Price price = this.getLimitPriceForDone(setPrice);
			this.limitPriceEditForIfStopDone.enableEdit();
			this.limitPriceEditForIfStopDone.setText(Price.toString(price));
		}
		else
		{
			this.limitPriceEditForIfStopDone.disableEdit();
			this.limitPriceEditForIfStopDone.resetValue();
		}
		this.updateSubmitButtonStatus();
	}

	private void updateIfDoneTitle()
	{
		String limitPrice = this._limitPriceField.getText() == null ? "-" : this._limitPriceField.getText();
		this.ifLimitDonePanelTitledBorder.setTitle(Language.IfDonePrompt + "(" + limitPrice + ")");
		String stopPrice = this._stopPriceField.getText() == null ? "-" : this._stopPriceField.getText();
		this.ifStopDonePanelTitledBorder.setTitle(Language.IfDonePrompt + "(" + stopPrice + ")");
		this.ifLimitDonePanel.updateUI();
		this.ifStopDonePanel.updateUI();
	}

	private boolean hasAnotherPlacingTran()
	{
		return this.hasAnotherPlacingTran(null);
	}

	private boolean hasAnotherPlacingTran(JCheckBox eventSender)
	{
		if(eventSender == this.modifyLimitPriceBox)
		{
			return!this.ocoCheckBox.isSelected() && this._stopPriceField.hasValue();
		}
		else if(eventSender == this.modifyStopPriceBox)
		{
			return!this.ocoCheckBox.isSelected() && this._limitPriceField.hasValue();
		}
		else
		{
			return!this.ocoCheckBox.isSelected() && this._limitPriceField.hasValue() && this._stopPriceField.hasValue();
		}
	}

	private void handleModifyLimitPriceBoxSelectedChanged()
	{
		if(!this.modifyLimitPriceBox.isSelected())
		{
			this._limitPriceField.disableEdit();
			this._limitPriceField.resetValue();
		}
		else
		{
			boolean isBuy = this._originalOrder.get_IsBuy();
			BigDecimal lot = this._originalOrder.get_Lot();
			Instrument instrument = this._originalOrder.get_Transaction().get_Instrument();
			Price bid = instrument.get_LastQuotation().get_Bid();
			Price ask = instrument.get_LastQuotation().get_Ask();
			if (bid == null || ask == null)	return;

			this._limitPriceField.enableEdit();
			int acceptLmtVariation = instrument.get_AcceptLmtVariation(this._originalOrder.get_Account(), isBuy, lot, this._originalOrder, this._originalOrder.get_IsOpen() ? null : this._originalOrder.get_RelationOrders(), hasAnotherPlacingTran(this.modifyLimitPriceBox));
			Price price = (instrument.get_IsNormal() == isBuy) ? Price.subStract(ask, acceptLmtVariation) : Price.add(bid, acceptLmtVariation);
			if (this.isBetweenBidToAsk(price, instrument))
			{
				price = (instrument.get_IsNormal() == isBuy) ? Price.subStract(bid, instrument.get_NumeratorUnit()) :
					Price.add(ask, instrument.get_NumeratorUnit());
			}
			this._limitPriceField.setText(Price.toString(price));
		}

		this.ocoCheckBox.setSelected(this.canChangeToOCO() && this._limitPriceField.hasValue() && this._stopPriceField.hasValue());
		this.updateSubmitButtonStatus();
		this.updatIfDoneStatus();
	}

	private void handleModifyStopPriceBoxSelectedChanged()
	{
		if(!this.modifyStopPriceBox.isSelected())
		{
			this._stopPriceField.disableEdit();
			this._stopPriceField.resetValue();
		}
		else
		{
			boolean isBuy = this._originalOrder.get_IsBuy();
			BigDecimal lot = this._originalOrder.get_Lot();
			Instrument instrument = this._originalOrder.get_Transaction().get_Instrument();
			Price bid = instrument.get_LastQuotation().get_Bid();
			Price ask = instrument.get_LastQuotation().get_Ask();
			if (bid == null || ask == null) return;

			this._stopPriceField.enableEdit();
			int acceptLmtVariation = instrument.get_AcceptLmtVariation(this._originalOrder.get_Account(), isBuy, lot, this._originalOrder, this._originalOrder.get_IsOpen() ? null : this._originalOrder.get_RelationOrders(), hasAnotherPlacingTran(this.modifyStopPriceBox));
			Price price = (instrument.get_IsNormal() == isBuy) ? Price.add(ask, acceptLmtVariation) : Price.subStract(bid, acceptLmtVariation);
			if (this.isBetweenBidToAsk(price, instrument))
			{
				price = (instrument.get_IsNormal() == isBuy) ? Price.add(ask, instrument.get_NumeratorUnit()) :
					Price.subStract(bid, instrument.get_NumeratorUnit());
			}
			this._stopPriceField.setText(Price.toString(price));
		}
		this.ocoCheckBox.setSelected(this.canChangeToOCO() && this._limitPriceField.hasValue() && this._stopPriceField.hasValue());
		this.updateSubmitButtonStatus();
		this.updatIfDoneStatus();
	}

	private void updatIfDoneStatus()
	{
		boolean value = this.ifDoneCheckBox.isVisible() && this.ifDoneCheckBox.isSelected()&& this._limitPriceField.hasValue();
		this.limitCheckBoxForIfLimitDone.setEnabled(value);
		this.stopCheckBoxForIfLimitDone.setEnabled(value);
		if(!value)
		{
			this.limitCheckBoxForIfLimitDone.setSelected(false);
			this.limitCheckBoxForIfLimitDoneChanged();
			this.stopCheckBoxForIfLimitDone.setSelected(false);
			this.stopCheckBoxForIfLimitDoneChanged();
		}

		value = this.ifDoneCheckBox.isVisible() && this.ifDoneCheckBox.isSelected() && this._stopPriceField.hasValue();
		this.limitCheckBoxForIfStopDone.setEnabled(value);
		this.stopCheckBoxForIfStopDone.setEnabled(value);
		if(!value)
		{
			this.limitCheckBoxForIfStopDone.setSelected(false);
			this.limitCheckBoxForIfStopDoneChanged();
			this.stopCheckBoxForIfStopDone.setSelected(false);
			this.stopCheckBoxForIfStopDoneChanged();
		}
	}

	private void handleIfDoneCheckBoxSelectedChanged()
	{
		this.updatIfDoneStatus();
		this.updateSubmitButtonStatus();
	}

	private void updateSubmitButtonStatus()
	{
		this._submitButton.setEnabled(this._limitPriceField.isValueDifferentFromOrigin()
									  || this._stopPriceField.isValueDifferentFromOrigin()
									  || this.limitPriceEditForIfLimitDone.isValueDifferentFromOrigin()
									  || this.stopPriceEditForIfLimitDone.isValueDifferentFromOrigin()
									  || this.limitPriceEditForIfStopDone.isValueDifferentFromOrigin()
									  || this.stopPriceEditForIfStopDone.isValueDifferentFromOrigin()
									  ||(this._originalOrder.get_Transaction().get_SubType() == TransactionSubType.IfDone
										 && this._originalOrder.get_IsOpen() && !this.ifDoneCheckBox.isSelected()));
	}

	private void fillDefaultSetPrice(boolean showAlterDialog)
	{
		boolean isBuy = this._originalOrder.get_IsBuy();
		BigDecimal lot = this._originalOrder.get_Lot();
		Instrument instrument = this._originalOrder.get_Transaction().get_Instrument();
		Price bid = instrument.get_LastQuotation().get_Bid();
		Price ask = instrument.get_LastQuotation().get_Ask();
		if (bid == null || ask == null)
		{
			if(showAlterDialog)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderPlacementbuttonConfirm_OnclickAlert2);
			}
			return;
		}

		int acceptLmtVariation = instrument.get_AcceptLmtVariation(this._originalOrder.get_Account(), isBuy, lot, this._originalOrder, this._originalOrder.get_IsOpen() ? null : this._originalOrder.get_RelationOrders(), hasAnotherPlacingTran());
		Price price = (instrument.get_IsNormal() == isBuy) ? Price.subStract(ask, acceptLmtVariation) : Price.add(bid, acceptLmtVariation);
		if (this.isBetweenBidToAsk(price, instrument))
		{
			price = (instrument.get_IsNormal() == isBuy) ? Price.subStract(bid, instrument.get_NumeratorUnit()) :
				Price.add(ask, instrument.get_NumeratorUnit());
		}
		if(this.modifyLimitPriceBox.isSelected()) this._limitPriceField.setText(Price.toString(price));

		//Fill Stop Price
		price = (instrument.get_IsNormal() == isBuy) ? Price.add(ask, acceptLmtVariation) : Price.subStract(bid, acceptLmtVariation);
		if (this.isBetweenBidToAsk(price, instrument))
		{
			price = (instrument.get_IsNormal() == isBuy) ? Price.add(ask, instrument.get_NumeratorUnit()) :
				Price.subStract(bid, instrument.get_NumeratorUnit());
		}
		if(this.modifyStopPriceBox.isSelected()) this._stopPriceField.setText(Price.toString(price));

		String priceVale = this._limitPriceField.getText();
		price = Price.parse(priceVale, instrument.get_NumeratorUnit(), instrument.get_Denominator());
		if (price != null && this.limitPriceEditForIfLimitDone.isEnabled())
		{
			this.limitPriceEditForIfLimitDone.setText(Price.toString(this.getLimitPriceForDone(price)));
		}
		if (price != null && this.stopPriceEditForIfLimitDone.isEnabled())
		{
			this.stopPriceEditForIfLimitDone.setText(Price.toString(this.getStopPriceForDone(price)));
		}

		priceVale = this._stopPriceField.getText();
		price = Price.parse(priceVale, instrument.get_NumeratorUnit(), instrument.get_Denominator());
		if (price != null && this.limitPriceEditForIfStopDone.isEnabled())
		{
			this.limitPriceEditForIfStopDone.setText(Price.toString(this.getLimitPriceForDone(price)));
		}
		if (price != null && this.stopPriceEditForIfStopDone.isEnabled())
		{
			this.stopPriceEditForIfStopDone.setText(Price.toString(this.getStopPriceForDone(price)));
		}
	}

	private Price getLimitPriceForDone(Price basePrice)
	{
		if(basePrice == null) return null;

		boolean isBuyForDone = !this._originalOrder.get_IsBuy();
		int acceptIfDoneVariation = this._originalOrder.get_Instrument().get_AcceptIfDoneVariation();
		if (isBuyForDone == this._originalOrder.get_Instrument().get_IsNormal())
		{
			return Price.subStract(basePrice, acceptIfDoneVariation);
		}
		else
		{
			return Price.add(basePrice, acceptIfDoneVariation);
		}
	}

	private boolean shouldSubtractPlaceLot()
	{
		if(!this._originalOrder.get_IsOpen()
		   || this._originalOrder.get_Transaction().get_Type() == TransactionType.OneCancelOther
		   || this.ocoCheckBox.isSelected())
		{
			return true;
		}

		if(((this._originalOrder.get_TradeOption() == TradeOption.Better && this._limitPriceField.hasValue()) && !this._stopPriceField.hasValue())
		   || (this._originalOrder.get_TradeOption() == TradeOption.Stop && !this._limitPriceField.hasValue() && this._stopPriceField.hasValue()))
		{
			return true;
		}

		return false;
	}

	private Price getStopPriceForDone(Price basePrice)
	{
		if(basePrice == null) return null;

		boolean isBuyForDone = !this._originalOrder.get_IsBuy();
		int spread = this._settingsManager.getQuotePolicyDetail(this._originalOrder.get_Instrument().get_Id()).get_Spread() + this._originalOrder.get_Instrument().get_NumeratorUnit();
		int acceptIfDoneVariation = this._originalOrder.get_Instrument().get_AcceptIfDoneVariation();
		spread = Math.max(spread, acceptIfDoneVariation);
		if (isBuyForDone == this._originalOrder.get_Instrument().get_IsNormal())
		{
			return Price.add(basePrice, spread);
		}
		else
		{
			return Price.subStract(basePrice, spread);
		}
	}

	private void initializeOpenOrders()
	{
		String[] openOrders = StringHelper.split(this._originalOrder.get_PeerOrderCodes(), TradingConsole.delimiterRow);
		_closeOrdersGrid.setListData(openOrders);
	}

	private void submit()
	{
		Object[] result = this.validatePrice();
		boolean isValid = (Boolean)result[0];
		Price setLimitPrice = (Price)result[1];
		Price setStopPrice = (Price)result[2];

		if(!isValid)
		{
			this.fillDefaultSetPrice(false);
		}
		else
		{
			Instrument instrument = this._originalOrder.get_Transaction().get_Instrument();
			Account account = this._originalOrder.get_Transaction().get_Account();

			Guid tradePolicyId = this._originalOrder.get_Account().get_TradePolicyId();
			TradePolicyDetail tradePolicyDetail
				= this._tradingConsole.get_SettingsManager().getTradePolicyDetail(tradePolicyId, instrument.get_Id());
			boolean isBuy = this._originalOrder.get_IsBuy();
			MakeOrderAccount makeOrderAccount = MakeOrderAccount.create(this._tradingConsole, this._settingsManager, account, instrument);
			BigDecimal lot = this._originalOrder.get_IsOpen() ?  this._originalOrder.get_LotBalance() : this._originalOrder.get_Lot();

			if (setStopPrice != null
				&& this._originalOrder.get_TradeOption() != TradeOption.Stop
				&& this._originalOrder.get_Transaction().get_Type() != TransactionType.OneCancelOther
				&& !tradePolicyDetail.get_IsAcceptNewStop())
			{
				BigDecimal[] sumLot2 = makeOrderAccount.getSumLotBSForMakeStopOrder();
				BigDecimal sumBuyLotBalances = sumLot2[0];
				BigDecimal sumSellLotBalances = sumLot2[1];
				if (isBuy)
				{
					if (lot.compareTo(sumSellLotBalances.subtract(sumBuyLotBalances)) > 0)
					{
						AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert4);
						return;
					}
				}
				else
				{
					if (lot.compareTo(sumBuyLotBalances.subtract(sumSellLotBalances)) > 0)
					{
						AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert5);
						return;
					}
				}
			}

			BigDecimal[] sumLot = makeOrderAccount.getSumLotBSForOpenOrder();
			BigDecimal sumBuyLots = sumLot[0];
			BigDecimal sumSellLots = sumLot[1];
			if (setLimitPrice != null && this._originalOrder.get_IsOpen()
				&& this._originalOrder.get_TradeOption() != TradeOption.Better
				&& this._originalOrder.get_Transaction().get_Type() != TransactionType.OneCancelOther
				&& !tradePolicyDetail.get_IsAcceptNewLimit())
			{
				if (isBuy)
				{
					if (lot.compareTo(sumSellLots.subtract(sumBuyLots)) > 0)
					{
						AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlertBetter);
						return;
					}
				}
				else
				{
					if (lot.compareTo(sumBuyLots.subtract(sumSellLots)) > 0)
					{
						AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlertBetter);
						return;
					}
				}
			}

			Object[] result2 = MakeLimitOrder.isAllowTime(this._settingsManager, instrument, account);
			if (! (Boolean) result2[0])
			{
				String errorMessage = (!StringHelper.isNullOrEmpty(result2[1].toString())) ? result2[1].toString() : Language.OrderPlacementbuttonConfirm_OnclickAlert2;
				AlertDialogForm.showDialog(this, null, true, errorMessage);
				return;
			}

			Transaction transaction = new Transaction(this._tradingConsole, this._settingsManager, this._originalOrder, this.ocoCheckBox.isSelected());
			transaction.set_IfDoneInfo(this.ifDoneInfo);
			DateTime appTime = TradingConsoleServer.appTime();
			DateTime beginTime = appTime.addMinutes(0 - Parameter.orderBeginTimeDiff);

			for(Order order : this.createOrders(transaction, setLimitPrice, setStopPrice))
			{
				if(order != null)
				{
					transaction.get_Orders().put(order.get_Id(), order);
				}
			}
			transaction.changeToOcoOrder(beginTime, this._originalOrder.get_EndTime(), appTime);
			this.dispose();
		}
	}

	private Order[] createOrders(Transaction transaction, Price setLimitPrice, Price setStopPrice)
	{
		Order newOrder = null;
		if((this._originalOrder.get_TradeOption() == TradeOption.Stop && setLimitPrice != null)
		   || (this._originalOrder.get_TradeOption() == TradeOption.Better && setStopPrice != null))
		{
			DataTable orderDataTable = Order.createStructure();
			DataRow orderDataRow = orderDataTable.newRow();
			orderDataRow.set_Item("ID", Guid.newGuid());
			TradeOption tradeOption = this._originalOrder.get_TradeOption() == TradeOption.Better ? TradeOption.Stop : TradeOption.Better;
			orderDataRow.set_Item("TradeOption", (short)tradeOption.value());
			orderDataRow.set_Item("IsOpen", this._originalOrder.get_IsOpen());
			orderDataRow.set_Item("IsBuy", this._originalOrder.get_IsBuy());

			String setPrice = this._originalOrder.get_TradeOption() == TradeOption.Stop ? Price.toString(setLimitPrice) : Price.toString(setStopPrice);
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
			newOrder = new Order(this._tradingConsole, this._settingsManager, orderDataRow, transaction);
		}

		Order oldOrder = this._originalOrder.clone(transaction);
		oldOrder.set_SetPrice(this._originalOrder.get_TradeOption() == TradeOption.Better ? setLimitPrice : setStopPrice);
		oldOrder.set_IsAssignOrder(true);
		for(RelationOrder relationOrder : this._originalOrder.get_RelationOrders().values())
		{
			oldOrder.set_PeerOrderIDs(this._originalOrder.get_PeerOrderIDs());
			oldOrder.addRelationOrder(relationOrder.get_OpenOrder(), relationOrder.get_CloseLot());

			if(newOrder != null)
			{
				newOrder.set_PeerOrderIDs(this._originalOrder.get_PeerOrderIDs());
				newOrder.set_PeerOrderCodes(this._originalOrder.get_PeerOrderCodes());
				newOrder.addRelationOrder(relationOrder.get_OpenOrder(), relationOrder.get_CloseLot());
			}
		}
		if(oldOrder != null) TradingConsole.traceSource.trace(TraceType.Information, "[ChangeToOcoOrder.createOrders]oldOrder = " + oldOrder + oldOrder.get_TradeOption().name());
		if(newOrder != null) TradingConsole.traceSource.trace(TraceType.Information, "[ChangeToOcoOrder.createOrders]newOrder = " + newOrder + newOrder.get_TradeOption().name());
		return new Order[]{oldOrder, newOrder};
	}

	private boolean canChangeToOCO()
	{
		Instrument instrument = this._originalOrder.get_Transaction().get_Instrument();
		Guid tradePolicyId = this._originalOrder.get_Account().get_TradePolicyId();
		TradePolicyDetail tradePolicyDetail
			= this._tradingConsole.get_SettingsManager().getTradePolicyDetail(tradePolicyId, instrument.get_Id());
		return this._originalOrder.get_IsOpen() ? tradePolicyDetail.get_AllowNewOCO() :
				MakeOrder.isAllowOrderType(instrument, OrderType.OneCancelOther);
	}

	private boolean canChangeToIfDone()
	{
		Guid instrumentId = this._originalOrder.get_Transaction().get_Instrument().get_Id();
		Guid tradePolicyId = this._originalOrder.get_Account().get_TradePolicyId();
		TradePolicyDetail tradePolicyDetail
			= this._tradingConsole.get_SettingsManager().getTradePolicyDetail(tradePolicyId, instrumentId);
		return this._originalOrder.get_IsOpen() && tradePolicyDetail.get_AllowIfDone();
	}

	private Object[] validatePrice()
	{
		this.ifDoneInfo = null;

		boolean isValidOrder = true;
		Object[] result = new Object[3];
		result[0] = false;

		Instrument instrument = this._originalOrder.get_Transaction().get_Instrument();
		Guid tradePolicyId = this._originalOrder.get_Account().get_TradePolicyId();
		TradePolicyDetail tradePolicyDetail
			= this._tradingConsole.get_SettingsManager().getTradePolicyDetail(tradePolicyId, instrument.get_Id());

		if(this.ocoCheckBox.isSelected())
		{
			boolean canChangeToOCO = this._originalOrder.get_IsOpen() ? tradePolicyDetail.get_AllowNewOCO() :
				MakeOrder.isAllowOrderType(instrument, OrderType.OneCancelOther);

			if (!canChangeToOCO)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
				return result;
			}
		}

		if(this.ifDoneCheckBox.isSelected())
		{
			boolean canChangeToIfDone = this._originalOrder.get_IsOpen() && tradePolicyDetail.get_AllowIfDone();
			if (!canChangeToIfDone)
			{
				AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert01);
				return result;
			}
		}

		boolean limitPriceChanged = this._limitPriceField.isValueDifferentFromOrigin();
		boolean stopPriceChanged = this._stopPriceField.isValueDifferentFromOrigin();
		Price newLimitPrice = Price.parse(this._limitPriceField.getText(), instrument.get_NumeratorUnit(), instrument.get_Denominator());
		Price newStopPrice = Price.parse(this._stopPriceField.getText(), instrument.get_NumeratorUnit(), instrument.get_Denominator());
		if (newLimitPrice == null && newStopPrice == null)
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert1);
			isValidOrder = false;
		}
		else if (this._originalOrder.get_IsOpen()
				  && ((limitPriceChanged && newLimitPrice != null && this.isBetweenBidToAsk(newLimitPrice, instrument))
					  || (stopPriceChanged && newStopPrice != null && this.isBetweenBidToAsk(newStopPrice, instrument))))
		{
			AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert8);
			isValidOrder = false;
		}
		else
		{
			Price marketPrice = this.getMarketPrice(instrument);
			Account account = this._originalOrder.get_Account();
			SetPriceError setPriceError = SetPriceError.Ok;
			if(newLimitPrice != null && limitPriceChanged)
			{
				setPriceError = Order.checkLMTOrderSetPrice(account, true, instrument, this._originalOrder.get_IsBuy(), TradeOption.Better,
					newLimitPrice,
					marketPrice, this._originalOrder.get_Lot(), this._originalOrder, this._originalOrder.get_RelationOrders(), hasAnotherPlacingTran());
				if (setPriceError != SetPriceError.Ok)
					isValidOrder = false;
			}

			if(newStopPrice != null && stopPriceChanged)
			{
				setPriceError = Order.checkLMTOrderSetPrice(account, true, instrument, this._originalOrder.get_IsBuy(), TradeOption.Stop,
					newStopPrice,
					marketPrice, this._originalOrder.get_Lot(), this._originalOrder, this._originalOrder.get_RelationOrders(), hasAnotherPlacingTran());
				if (setPriceError != SetPriceError.Ok)
					isValidOrder = false;
			}

			double dblMarketPrice = Price.toDouble(marketPrice);
			if ((newLimitPrice != null && limitPriceChanged && Math.abs(Price.toDouble(newLimitPrice) - dblMarketPrice) > dblMarketPrice * 0.2)
				|| (newStopPrice != null && stopPriceChanged && Math.abs(Price.toDouble(newStopPrice) - dblMarketPrice) > dblMarketPrice * 0.2))
			{
				setPriceError = SetPriceError.SetPriceTooFarAwayMarket;
			}

			if (setPriceError == SetPriceError.SetPriceTooCloseMarket)
			{
				if (this._settingsManager.get_SystemParameter().get_DisplayLmtStopPoints())
				{
					boolean isBuy = this._originalOrder.get_IsBuy();
					BigDecimal lot = this._originalOrder.get_Lot();
					AlertDialogForm.showDialog(this, null, true, Language.OrderLMTPageorderValidAlert2 + " "
						+ instrument.get_AcceptLmtVariation(this._originalOrder.get_Account(), isBuy, lot, this._originalOrder, this._originalOrder.get_IsOpen() ? null : this._originalOrder.get_RelationOrders(), hasAnotherPlacingTran()) + " " +
						Language.OrderLMTPageorderValidAlert22);
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

		Price setLimitPrice = newLimitPrice;
		Price setStopPrice = newStopPrice;
		result[1] = setLimitPrice;
		result[2] = setStopPrice;

		Price limitPriceForIfLimitDone = null, stopPirceForIfLimitDone = null, limitPriceForIfStopDone = null, stopPirceForIfStopDone = null;
		int numeratorUnit = instrument.get_NumeratorUnit();
		int denominator = instrument.get_Denominator();
		if (this.ifDoneCheckBox.isSelected())
		{
			boolean isBuyForIfDone = !this._originalOrder.get_IsBuy();
			int acceptIfDoneVariation = instrument.get_AcceptIfDoneVariation();
			limitPriceForIfLimitDone = Price.parse(this.limitPriceEditForIfLimitDone.getText(), numeratorUnit, denominator);
			stopPirceForIfLimitDone = Price.parse(this.stopPriceEditForIfLimitDone.getText(), numeratorUnit, denominator);

			if (limitPriceForIfLimitDone != null && this.limitPriceEditForIfLimitDone.isValueDifferentFromOrigin())
			{
				Price comparePrice = (instrument.get_IsNormal() != isBuyForIfDone) ?
					Price.add(setLimitPrice, acceptIfDoneVariation) : Price.subStract(setLimitPrice, acceptIfDoneVariation);
				double comparePriceValue = Price.toDouble(comparePrice);
				if ( ( (instrument.get_IsNormal() != isBuyForIfDone) ? limitPriceForIfLimitDone.compareTo(comparePrice) < 0 :
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
					AlertDialogForm.showDialog(this, null, true, info);
					isValidOrder = false;
				}
			}

			int spread = this._settingsManager.getQuotePolicyDetail(instrument.get_Id()).get_Spread() + instrument.get_NumeratorUnit();
			spread = Math.max(spread, acceptIfDoneVariation);
			if (stopPirceForIfLimitDone != null && this.stopPriceEditForIfLimitDone.isValueDifferentFromOrigin())
			{
				Price comparePrice = isBuyForIfDone ? Price.add(setLimitPrice, spread) : Price.subStract(setLimitPrice, spread);
				double comparePriceValue = Price.toDouble(comparePrice);
				if ( ( (instrument.get_IsNormal() != isBuyForIfDone) ? stopPirceForIfLimitDone.compareTo(comparePrice) > 0 :
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
					AlertDialogForm.showDialog(this, null, true, info);
					isValidOrder = false;
				}
			}

			limitPriceForIfStopDone = Price.parse(this.limitPriceEditForIfStopDone.getText(), numeratorUnit, denominator);
			stopPirceForIfStopDone = Price.parse(this.stopPriceEditForIfStopDone.getText(), numeratorUnit, denominator);

			if (limitPriceForIfStopDone != null && this.limitPriceEditForIfStopDone.isValueDifferentFromOrigin())
			{
				Price comparePrice = (instrument.get_IsNormal() != isBuyForIfDone) ?
					Price.add(setStopPrice, acceptIfDoneVariation) : Price.subStract(setStopPrice, acceptIfDoneVariation);
				double comparePriceValue = Price.toDouble(comparePrice);
				if ( ( (instrument.get_IsNormal() != isBuyForIfDone) ? limitPriceForIfStopDone.compareTo(comparePrice) < 0 :
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
					AlertDialogForm.showDialog(this, null, true, info);
					isValidOrder = false;
				}
			}

			if (stopPirceForIfStopDone != null && this.stopPriceEditForIfStopDone.isValueDifferentFromOrigin())
			{
				Price comparePrice = isBuyForIfDone ? Price.add(setStopPrice, spread) : Price.subStract(setStopPrice, spread);
				double comparePriceValue = Price.toDouble(comparePrice);
				if ( ( (instrument.get_IsNormal() != isBuyForIfDone) ? stopPirceForIfStopDone.compareTo(comparePrice) > 0 :
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
					AlertDialogForm.showDialog(this, null, true, info);
					isValidOrder = false;
				}
			}

			this.ifDoneInfo
				= new IfDoneInfo(limitPriceForIfLimitDone, stopPirceForIfLimitDone, limitPriceForIfStopDone, stopPirceForIfStopDone, isBuyForIfDone, this._originalOrder.get_Lot());
		}
		result[0] = isValidOrder;
		return result;
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
