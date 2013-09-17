package tradingConsole.ui;

import java.math.*;
import java.text.*;

import java.awt.*;
import javax.swing.*;

import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import java.util.Iterator;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ActionListener;
import framework.xml.XmlDocument;
import framework.xml.XmlNode;
import java.awt.event.ActionEvent;
import framework.Guid;
import tradingConsole.common.TransactionError;
import tradingConsole.ui.AlertDialogForm;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import tradingConsole.service.ApplyDeliveryResult;
import framework.StringHelper;
import framework.DateTime;
import framework.xml.XmlConvert;
import java.util.Date;
import framework.data.DataSet;
import framework.data.DataTable;
import framework.data.DataRowCollection;
import framework.data.DataRow;
public class DeliveryForm extends JPanel
{
	private JDialog _residedWindow = null;
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private Boolean _closeAllSell = null;
	private Instrument _instrument;
	private Account _account;
	private MakeOrderAccount _makeOrderAccount;
	private Order _order;

	private JAdvancedComboBox accountChoice = new JAdvancedComboBox();
	private JFormattedTextFieldEx totalLotTextField = new JFormattedTextFieldEx(new DecimalFormat(), true);
	private JFormattedTextFieldEx totalQuantityTextField = new JFormattedTextFieldEx(new DecimalFormat(), true);
	private JFormattedTextFieldEx closeLotTextField = new JFormattedTextFieldEx(new DecimalFormat(), true);
	private DataGrid outstandingOrderTable = new DataGrid("OutstandingOrderGrid");
	private BidAskButton bidButton = new BidAskButton(true);
	private BidAskButton askButton = new BidAskButton(false);
	private PVButton2 resetButton = new PVButton2();
	private PVButton2 submitButton = new PVButton2();
	private PVButton2 closeAllButton = new PVButton2();
	private PVButton2 exitButton = new PVButton2();
	private PVStaticText2 accountStaticText = new PVStaticText2();
	private PVStaticText2 totalLotStaticText = new PVStaticText2();
	private PVStaticText2 totalQuantityStaticText = new PVStaticText2();
	private PVStaticText2 quantityUnitStaticText = new PVStaticText2();
	private PVStaticText2 closeLotStaticText = new PVStaticText2();

	private PVStaticText2 deliveryTimeStaticText = new PVStaticText2();
	private DeliveryTimeEditor deliveryTimeEditor = new DeliveryTimeEditor();

	private PVStaticText2 deliveryAddressStaticText = new PVStaticText2();
	private JAdvancedComboBox deliveryAddressChoice = new JAdvancedComboBox();

	//MultiTextArea instrumentNarrative = new MultiTextArea();
	NoneResizeableTextField instrumentQuoteDescription = new NoneResizeableTextField();

	private PVStaticText2 deliveryChargeStaticText = new PVStaticText2();
	private PVStaticText2 deliveryChargeCurrencyStaticText = new PVStaticText2();
	private JFormattedTextField deliveryChargeTextField = new JFormattedTextField(new DecimalFormat());

	private BigDecimal _lastAccountLot = BigDecimal.ZERO;
	private OpenCloseRelationSite _openCloseRelationSite;

	public DeliveryForm(JDialog parent, TradingConsole tradingConsole, Instrument instrument,
						Account account, Order order, Boolean closeAllSell)
	{
		this._residedWindow = parent;
		this._closeAllSell = closeAllSell;
		this._account = account;
		this._instrument = instrument;
		this._order = order;

		this._tradingConsole = tradingConsole;
		this._settingsManager = this._tradingConsole.get_SettingsManager();

		this._openCloseRelationSite = new OpenCloseRelationSite(this);

		this.jbInit();
		this.init();
		this.updateSubmitButtonStatus();
	}

	public void refreshPrice()
	{
		this.bidButton.updatePrice();
		this.askButton.updatePrice();
	}

	private void jbInit()
	{
		InstrumentPriceProvider instrumentPriceProvider = new InstrumentPriceProvider(this._instrument);
		this.bidButton.set_PriceProvider(instrumentPriceProvider);
		this.askButton.set_PriceProvider(instrumentPriceProvider);
		this.bidButton.setEnabled(false);
		this.askButton.setEnabled(false);
		this.deliveryTimeEditor.initialize(this._instrument);

		this.setSize(700, 300);

		this.setLayout(new GridBagLayout());

		Font font = new Font("SansSerif", Font.BOLD, 13);
		totalLotTextField.setFont(font);
		this.deliveryChargeCurrencyStaticText.setFont(font);
		this.deliveryChargeStaticText.setFont(font);
		instrumentQuoteDescription.setFont(font);

		accountStaticText.setFont(font);
		totalLotStaticText.setFont(font);
		closeLotStaticText.setFont(font);
		deliveryTimeStaticText.setFont(font);
		totalQuantityStaticText.setFont(font);
		quantityUnitStaticText.setFont(font);
		deliveryAddressStaticText.setFont(font);
		quantityUnitStaticText.setText(this._instrument.get_Unit());

		this.totalLotTextField.getDocument().addDocumentListener(new DocumentListener()
			{
			public void insertUpdate(DocumentEvent e)
			{
				updateDeliveryCharge();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateDeliveryCharge();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateDeliveryCharge();
			}
		});

		totalLotTextField.addFocusListener(new DeliveryForm_lotNumeric_focusAdapter(this));
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

		this.accountStaticText.setText(Language.OrderLMTlblAccountCodeA);
		this.totalLotStaticText.setText(Language.OrderLMTlblLot);
		this.closeLotStaticText.setText(Language.DeliveryLot);
		this.deliveryTimeStaticText.setText(Language.DeliveryTime);
		this.deliveryAddressStaticText.setText(Language.DeliveryAddress);

		this.resetButton.setText(Language.OrderLMTbtnReset);
		this.submitButton.setText(Language.OrderLMTbtnSubmit);
		this.closeAllButton.setText(Language.CloseAll);
		this.deliveryChargeStaticText.setText(Language.DeliveryCharge);
		this.exitButton.setText(Language.OrderLMTbtnExit);
		this.totalQuantityStaticText.setText(Language.DeliveryWeight);

		this.add(bidButton, new GridBagConstraints2(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 80, 55));
		this.add(askButton, new GridBagConstraints2(1, 0, 2, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 0, 0), 80, 55));

		this.add(instrumentQuoteDescription, new GridBagConstraints2(0, 1, 3, 1, 0.0, 0.0,
				GridBagConstraints.SOUTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 20));

		if(!StringHelper.isNullOrEmpty(this._instrument.get_QuoteDescription()))
		{
			this.instrumentQuoteDescription.setText(this._instrument.get_QuoteDescription());
		}
		else
		{
			this.instrumentQuoteDescription.setVisible(false);
		}

		this.add(accountStaticText, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 2, 0, 0), 40, 0));
		this.add(accountChoice, new GridBagConstraints(1, 4, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 2, 0, 0), 0, 5));

		this.add(this.totalLotStaticText, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 40, 0));
		this.add(this.totalLotTextField, new GridBagConstraints(1, 5, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 0, 5));

		this.totalQuantityTextField.setEditable(false);
		this.add(this.totalQuantityStaticText, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 40, 0));
		this.add(this.totalQuantityTextField, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 80, 0));
		this.add(this.quantityUnitStaticText, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 0, 5));

		this.add(this.closeLotStaticText, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 40, 0));
		this.add(this.closeLotTextField, new GridBagConstraints(1, 7, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 0, 5));

		this.add(this.deliveryTimeStaticText, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 40, 0));
		this.add(this.deliveryTimeEditor, new GridBagConstraints(1, 8, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 0, 5));

		this.add(this.deliveryAddressStaticText, new GridBagConstraints(0, 9, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 40, 0));
		this.deliveryAddressChoice.setEditable(false);
		JScrollPane pane = new JScrollPane(this.deliveryAddressChoice, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		pane.setBorder(null);
		pane.setBackground(null);
		this.add(pane, new GridBagConstraints(1, 9, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 0, 22));

		/*JPanel panel = new JPanel(new GridBagLayout());
		this.add(panel, new GridBagConstraints(0, 4, 2, 1, 1.0, 1.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 0, 0));*/

		this.add(this.deliveryChargeStaticText, new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 2, 0, 0), 40, 0));
		JPanel panel = new JPanel(new GridBagLayout());
		this.add(panel, new GridBagConstraints(1, 10, 2, 1, 0.0, 0.0
			, GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 40, 0));

		panel.add(this.deliveryChargeCurrencyStaticText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(this.deliveryChargeTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0));
		deliveryChargeTextField.setEditable(false);
		this.updateDeliveryChargeVisibility();

		JScrollPane scrollPane = new JScrollPane(outstandingOrderTable);
		outstandingOrderTable.enableRowStripe();
		this.add(scrollPane, new GridBagConstraints(3, 0, 1, 11, 1.0, 1.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 2), 0, 5));

		this.add(this.submitButton, new GridBagConstraints(0, 11, 3, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 0, 5));

		this.add(this.exitButton, new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 0, 2), 80, 5));

		submitButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					submit();
				}
			});

		exitButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					dispose2();
				}
			});
	}

	private void fillDeliveryAddress()
	{
		this.deliveryAddressChoice.removeAllItems();
		if(this._instrument.get_DeliveryPointGroupId() == null)
		{
			this.deliveryAddressStaticText.setVisible(false);
			this.deliveryAddressChoice.setVisible(false);
		}
		else
		{
			String[] deliveryAddresses = this._tradingConsole.get_TradingConsoleServer().getDeliveryAddress(this._instrument.get_DeliveryPointGroupId());
			if(deliveryAddresses == null)
			{
				AlertDialogForm.showDialog(this.getFrame(), Language.deliveryFormTitle, true, Language.getDeliveryAddressFailed);
				this.dispose2();
				return;
			}

			for(String deliveryAddress : deliveryAddresses)
			{
				if(StringHelper.isNullOrEmpty(deliveryAddress)) continue;
				String[] items = StringHelper.split(deliveryAddress, '|');
				Guid id = new Guid(items[0]);
				String address = items[1];
				this.deliveryAddressChoice.addItem(address, id);
			}

			if(this.deliveryAddressChoice.getItemCount() > 0)
			{
				this.deliveryAddressChoice.setSelectedIndex(0);
			}
			else
			{
				this.deliveryAddressStaticText.setVisible(false);
				this.deliveryAddressChoice.setVisible(false);
			}
		}
	}

	private void updateDeliveryChargeVisibility()
	{
		boolean visible = this._deliverCharge != 0;
		this.deliveryChargeStaticText.setVisible(visible);
		this.deliveryChargeTextField.setVisible(visible);
		this.deliveryChargeCurrencyStaticText.setVisible(visible);
	}

	private double _deliverCharge;
	private void updateDeliveryCharge()
	{
		TradePolicyDetail tradePolicyDetail
			= this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(), this._instrument.get_Id());
		Guid deliveryChargeId = tradePolicyDetail.get_DeliveryChargeId();
		if(deliveryChargeId != null)
		{
			DeliveryCharge deliveryCharge = this._settingsManager.getDeliveryCharge(deliveryChargeId);
			Price price = null;
			if (deliveryCharge.get_PriceType().equals(MarketValuePriceType.DayOpenPrice))
			{
				price = this._instrument.get_LastQuotation().get_Open();
			}
			else
			{
				price = this._instrument.get_Quotation().getSell();
			}
			String closeLot = this.totalLotTextField.getPlainText();
			BigDecimal totalLot = StringHelper.isNullOrEmpty(closeLot) ? BigDecimal.ZERO : new BigDecimal(closeLot);
			if(totalLot.compareTo(BigDecimal.ZERO) <= 0 || this._makeOrderAccount.getSumLiqLots(false).compareTo(BigDecimal.ZERO) <= 0)
			{
				this.deliveryChargeTextField.setText("");
				this._deliverCharge = 0;
				this.updateDeliveryChargeVisibility();
				return;
			}

			double oddDiscount = tradePolicyDetail.get_DiscountOfOdd().doubleValue();
			short tradePLFormula = this._instrument.get_TradePLFormula();
			double marketValue = Order.caculateMarketValue(price, tradePLFormula, totalLot, tradePolicyDetail.get_ContractSize().doubleValue(), oddDiscount);
			double charge = marketValue * deliveryCharge.get_ChargeRate().doubleValue();
			charge = Math.max(charge, deliveryCharge.get_MinCharge().doubleValue());

			if (this._makeOrderAccount.get_Account().get_IsMultiCurrency() == false)
			{
				Guid instrumentCurrencyId = this._instrument.get_Currency().get_Id();
				Guid accountCurrencyId = this._makeOrderAccount.get_Account().get_Currency().get_Id();
				CurrencyRate currencyRate = this._settingsManager.getCurrencyRate(instrumentCurrencyId, accountCurrencyId);
				charge = currencyRate.exchange(charge);
				this.deliveryChargeTextField.setText(AppToolkit.format(charge, this._makeOrderAccount.get_Account().get_Currency().get_Decimals()));
			}
			else
			{
				this.deliveryChargeTextField.setText(AppToolkit.format(charge, this._instrument.get_Currency().get_Decimals()));
			}
			this._deliverCharge = charge;
			this.updateDeliveryChargeVisibility();
		}
		else
		{
			this.deliveryChargeTextField.setText("0");
			this._deliverCharge = 0;
			this.updateDeliveryChargeVisibility();
			//this.deliveryChargeCurrencyStaticText.setText("");
		}
	}

	void closeAll()
	{
		BigDecimal lot = this._makeOrderAccount.closeAll();
		this.updateAccount(lot, false);
	}

	private void init()
	{
		this.fillAccount();
		this.fillDeliveryAddress();
		this.accountChoice.addItemListener(new DeliveryForm_accountChoice_actionAdapter(this));

		this.accountChoice.setSelectedIndex(0);
		accountChoice.setEnabled(this.accountChoice.getItemCount() > 1);
		MakeOrderAccount makeOrderAccount = (MakeOrderAccount)this.accountChoice.getSelectedValue();
		this.setCurrentMakeOrderAccount(makeOrderAccount);
		if(this.accountChoice.getItemCount() == 1)
		{
			if(this._order != null)
			{
				for (RelationOrder relation : makeOrderAccount.getOutstandingOrders().values())
				{
					if (this._order.get_Id().equals(relation.get_OpenOrderId()))
					{
						relation.set_IsSelected(true);
						relation.updateLiquidation(makeOrderAccount.get_OutstandingKey());
						break;
					}
				}
			}

			if(this._closeAllSell != null)
			{
				this.closeAll();
			}

		}
	}

	private void submit()
	{
		Guid id = Guid.newGuid();
		Guid accountId = this._makeOrderAccount.get_Account().get_Id();
		Guid instrumentId = this._instrument.get_Id();
		Guid deliveryAddressId = null;
		if(this.deliveryAddressChoice.isVisible())
		{
			deliveryAddressId = (Guid)this.deliveryAddressChoice.getSelectedValue();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<ApplyDelivery");
		sb.append(" Id=\'");
		sb.append(id.toString());
		sb.append("\'");

		sb.append(" AccountId=\'");
		sb.append(accountId.toString());
		sb.append("\'");

		sb.append(" InstrumentId=\'");
		sb.append(instrumentId.toString());
		sb.append("\'");

		sb.append(" RequireLot=\'");
		sb.append(this.totalLotTextField.getText());
		sb.append("\'");

		sb.append(" Charge=\'");
		sb.append(this._deliverCharge);
		sb.append("\'");

		if(deliveryAddressId != null)
		{
			sb.append(" DeliveryAddressId=\'");
			sb.append(deliveryAddressId);
			sb.append("\'");
		}

		sb.append(" DeliveryTime=\'");
		DateTime deliveryTime = DateTime.fromDate((Date)this.deliveryTimeEditor.getValue());
		sb.append(XmlConvert.toString(deliveryTime, "yyyy-MM-dd"));
		sb.append("\'");

		Guid currencyId = this._makeOrderAccount.get_Account().get_IsMultiCurrency() ?
			this._instrument.get_Currency().get_Id() : this._makeOrderAccount.get_Account().get_Currency().get_Id();
		sb.append(" ChargeCurrencyId=\'");
		sb.append(currencyId.toString());
		sb.append("\'");

		sb.append(">");

		BigDecimal totalWeight = BigDecimal.ZERO;
		for(int index =0 ; index < this._makeOrderAccount.get_BindingSourceForOutstanding().getRowCount(); index++)
		{
			RelationOrder relation = (RelationOrder)this._makeOrderAccount.get_BindingSourceForOutstanding().getObject(index);
			if(relation.get_IsSelected() && relation.get_LiqLot() != null)
			{
				totalWeight = totalWeight.add(relation.get_Weight());

				sb.append("<DeliveryRequestOrderRelation");

				sb.append(" OpenOrderId=\'");
				sb.append(relation.get_OpenOrderId());
				sb.append("\'");

				sb.append(" DeliveryQuantity=\'");
				sb.append(relation.get_Weight().doubleValue());
				sb.append("\'");

				sb.append(" DeliveryLot=\'");
				sb.append(relation.get_LiqLotString());
				sb.append("\'");

				sb.append(">");

				sb.append("</DeliveryRequestOrderRelation>");
			}
		}

		sb.append("</ApplyDelivery>");
		XmlDocument xmlDocument = new XmlDocument();
		xmlDocument.loadXml(sb.toString());
		xmlDocument.get_DocumentElement().setAttribute("RequireQuantity", Double.toString(totalWeight.doubleValue()));
		XmlNode deliveryNode = xmlDocument.get_DocumentElement();

		ApplyDeliveryResult result = this._tradingConsole.get_TradingConsoleServer().applyDelivery(deliveryNode);
		if(result == null)
		{
			AlertDialogForm.showDialog(this.getFrame(), null, true, Language.FailedToDelivery + ": " + TransactionError.getCaption(TransactionError.RuntimeError));
		}
		else
		{
			if (result.get_TransactionError().equals(TransactionError.OK))
			{
				this.dispose2();
			}
			else if (result.get_TransactionError().equals(TransactionError.BalanceOrEquityIsShort))
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true,
										   StringHelper.format(Language.BalanceOrEquityIsShortForApplyDelivery, new Object[]{result.get_Balance(), result.get_UsableMargin()}));
				this.dispose2();
			}
			else
			{
				AlertDialogForm.showDialog(this.getFrame(), null, true, Language.FailedToDelivery + ": " + TransactionError.getCaption(result.get_TransactionError()));
			}
		}
	}

	void accountChoice_actionPerformed(ItemEvent e)
	{
		MakeOrderAccount makeOrderAccount = (MakeOrderAccount)this.accountChoice.getSelectedValue();
		if (this._makeOrderAccount == null || ! (this._makeOrderAccount.get_Account().get_Id().equals(makeOrderAccount.get_Account().get_Id())))
		{
			setCurrentMakeOrderAccount(makeOrderAccount);
		}
	}

	private void setCurrentMakeOrderAccount(MakeOrderAccount makeOrderAccount)
	{
		this._makeOrderAccount = makeOrderAccount;
		this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, false, false, BuySellType.Buy,
													 BuySellType.Buy, this._openCloseRelationSite, this._order);
		this._lastAccountLot = BigDecimal.ZERO;
		this.closeLotTextField.setText("");
		if (this._makeOrderAccount.get_Account().get_IsMultiCurrency() == false)
		{
			this.deliveryChargeCurrencyStaticText.setText(this._makeOrderAccount.get_Account().get_Currency().get_Code());
		}
		else
		{
			this.deliveryChargeCurrencyStaticText.setText(this._instrument.get_Currency().get_Code());
		}

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

		Account account = this._account;
		if(account == null && this._order != null) account = this._order.get_Account();
		if(account != null)
		{
			MakeOrderAccount makeOrderAccount = MakeOrderAccount.create(this._tradingConsole, this._settingsManager,
				this._account, this._instrument, true);
			this.accountChoice.addItem(makeOrderAccount.get_Account().get_Code(), makeOrderAccount);
		}
		else
		{
			for(MakeOrderAccount makeOrderAccount  : TradingConsole.DeliveryHelper.getDeliveryAccounts(this._tradingConsole, this._instrument))
			{
				this.accountChoice.addItem(makeOrderAccount.get_Account().get_Code(), makeOrderAccount);
			}
		}
	}

	private JDialog getFrame()
	{
		return this._residedWindow;
	}

	private String getFormatDefaultLot()
	{
		BigDecimal defaultValue = this.getDefaultLot();
		return AppToolkit.getFormatLot(defaultValue, this._makeOrderAccount.get_Account(), this._instrument);
	}

	private BigDecimal getDefaultLot()
	{
		Account account = this._makeOrderAccount.get_Account();
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(account.get_TradePolicyId(), this._instrument.get_Id());
		boolean isOpen = true;

		return AppToolkit.getDefaultLot(this._instrument, isOpen, tradePolicyDetail, this._makeOrderAccount);
	}

	private void updateTotalDeliveryWeight(BigDecimal totalCloseLot)
	{
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());
		BigDecimal weight = totalCloseLot.multiply(tradePolicyDetail.get_ContractSize());
		this.totalQuantityTextField.setText(AppToolkit.format(weight.doubleValue(), this._instrument.get_Decimal()));
	}

	private void updateTotalColseLot(BigDecimal totalCloseLot)
	{
		if (totalCloseLot.compareTo(BigDecimal.ZERO) > 0)
		{
			TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
				this._instrument.get_Id());
			totalCloseLot = AppToolkit.fixDeliveryLot(totalCloseLot, tradePolicyDetail);
		}

		/*if (totalCloseLot.compareTo(BigDecimal.ZERO) <= 0)
		{
			totalCloseLot = this.getDefaultLot();
		}*/

		this.totalLotTextField.setText(AppToolkit.getFormatLot(totalCloseLot, this._makeOrderAccount.get_Account(), this._instrument));
		this.updateTotalDeliveryWeight(totalCloseLot);
		this.updateSubmitButtonStatus();
	}

	public void updateAccount(BigDecimal accountLot, boolean openOrderIsBuy)
	{
		TradePolicyDetail tradePolicyDetail = this._settingsManager.getTradePolicyDetail(this._makeOrderAccount.get_Account().get_TradePolicyId(),
			this._instrument.get_Id());
		accountLot = AppToolkit.fixDeliveryLot(accountLot, tradePolicyDetail);

		BigDecimal currentLot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getPlainText());
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
				this.updateTotalDeliveryWeight(accountLot2);
			}
			catch (IllegalStateException exception)
			{
			}
		}
		this.updateSubmitButtonStatus();
	}

	private void rebind()
	{
		this._makeOrderAccount = (MakeOrderAccount)this.accountChoice.getSelectedValue();
		//init outstanding Order Grid
		boolean isBuy = false;
		this._makeOrderAccount.set_IsBuyForCurrent(isBuy);

		BuySellType buySellType = this._closeAllSell == null ? BuySellType.Both : (this._closeAllSell ? BuySellType.Sell : BuySellType.Buy);
		boolean relationsHasChange = this._makeOrderAccount.initializeOutstanding(this.outstandingOrderTable, isBuy, false, buySellType,
			( (!isBuy) ? BuySellType.Buy : BuySellType.Sell), this._openCloseRelationSite, this._order);

		if (relationsHasChange)
		{
			this.totalLotTextField.setText(this.getFormatDefaultLot());
			this.updateTotalDeliveryWeight(getDefaultLot());
			if (this._order != null)
			{
				BigDecimal sumLiqLot = this._makeOrderAccount.getSumLiqLots(isBuy);
				this.totalLotTextField.setText(AppToolkit.getFormatLot(sumLiqLot, this._makeOrderAccount.get_Account(),
					this._instrument));
				this.updateTotalDeliveryWeight(sumLiqLot);
			}
			this._lastAccountLot = BigDecimal.ZERO;
		}

		if (this.outstandingOrderTable.getSelectedRow() == -1 && this.outstandingOrderTable.getRowCount() > 0)
		{
			this.outstandingOrderTable.changeSelection(0, 0, false, false);
		}

		this.accountChoice.setBackground( (this._order == null) ? Color.white : Color.gray);
		this.accountChoice.setEnabled(this._order == null);

		BigDecimal currentLot = AppToolkit.convertStringToBigDecimal(this.totalLotTextField.getPlainText());
		if (currentLot.compareTo(BigDecimal.ZERO) <= 0)
		{
			this.totalLotTextField.setText(this.getFormatDefaultLot());
			this.updateTotalDeliveryWeight(getDefaultLot());
		}
		this.updateSubmitButtonStatus();
	}

	private void updateSubmitButtonStatus()
	{
		if(this._makeOrderAccount == null)
		{
			this.submitButton.setEnabled(false);
		}
		else
		{
			BigDecimal deliveryLot = this._makeOrderAccount.getSumLiqLots(false);
			BigDecimal validDeliveryLot = StringHelper.isNullOrEmpty(this.totalLotTextField.getText()) ? BigDecimal.ZERO :
				new BigDecimal(this.totalLotTextField.getPlainText());
			this.submitButton.setEnabled(deliveryLot.compareTo(BigDecimal.ZERO) > 0
										 && validDeliveryLot.compareTo(deliveryLot) <= 0);

			if(this.deliveryAddressChoice.isVisible() && this.deliveryAddressChoice.getSelectedIndex() < 0)
			{
				this.submitButton.setEnabled(false);
			}
		}
	}

	private void dispose2()
	{
		this.getFrame().dispose();
	}

	static class OpenCloseRelationSite implements IOpenCloseRelationSite
	{
		private DeliveryForm _owner;

		public OpenCloseRelationSite(DeliveryForm owner)
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
			return null;
		}

		public Boolean isMakeLimitOrder()
		{
			return null;
		}

		public boolean isDelivery()
		{
			return true;
		}

		public void addPlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
		{
			//Order type doesn't change
		}

		public void removePlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener)
		{
			//Order type doesn't change
		}

		public JTextField getCloseLotEditor()
		{
			return this._owner.closeLotTextField;
		}

		public JTextField getTotalLotEditor()
		{
			return this._owner.totalLotTextField;
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

	class DeliveryForm_lotNumeric_focusAdapter extends FocusAdapter
	{
		private DeliveryForm adaptee;
		DeliveryForm_lotNumeric_focusAdapter(DeliveryForm adaptee)
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

	class DeliveryForm_accountChoice_actionAdapter implements ItemListener
	{
		private DeliveryForm adaptee;
		DeliveryForm_accountChoice_actionAdapter(DeliveryForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void itemStateChanged(ItemEvent e)
		{
			adaptee.accountChoice_actionPerformed(e);
		}
	}

	private void lotNumeric_focusGained(FocusEvent e)
	{
		this.totalLotTextField.select(0, this.totalLotTextField.getText().length());
	}

	private void lotNumeric_focusLost(FocusEvent e)
	{
	}
}
