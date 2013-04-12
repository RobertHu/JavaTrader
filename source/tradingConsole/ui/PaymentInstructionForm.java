package tradingConsole.ui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;

import framework.xml.XmlConvert;
import framework.DateTime;
import framework.StringHelper;
import framework.CompositeKey2;
import framework.Guid;

import tradingConsole.AppToolkit;
import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Account;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.ui.language.PaymentInstructionLanguage;
import tradingConsole.Customer;
import tradingConsole.settings.Parameter;
import tradingConsole.AccountCurrency;

import tradingConsole.Currency;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import java.awt.Color;
import javax.swing.border.Border;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import java.awt.event.KeyEvent;
import java.util.Date;
import tradingConsole.enumDefine.LogCode;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import framework.IAsyncCallback;
import framework.IAsyncResult;
import framework.data.DataSet;
import tradingConsole.City;
import tradingConsole.Province;
import java.util.ArrayList;
import framework.data.DataRow;
import framework.data.DataRowCollection;
import tradingConsole.Bank;
import tradingConsole.BankAccount;
import framework.DBNull;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import com.jidesoft.swing.JideSwingUtilities;
import tradingConsole.Country;
import javax.swing.border.LineBorder;

public class PaymentInstructionForm extends JDialog implements VerifyMarginPin.IMarginPinVerifyCallback
{
	private TradingConsole _owner;
	private SettingsManager _settingsManager;
	private Account _account;
	private PVStaticText2 paymentInstructionCaptionStaticText = new PVStaticText2();
	private PVStaticText2 reportDateCaptionStaticText = new PVStaticText2();
	private PVStaticText2 organizationCaptionStaticText = new PVStaticText2();
	private PVStaticText2 customerAccountCaptionStaticText = new PVStaticText2();
	private PVStaticText2 emailCaptionStaticText = new PVStaticText2();
	private PVStaticText2 separator1CaptionStaticText = new PVStaticText2();
	private PVStaticText2 notifyCaptionStaticText = new PVStaticText2();

	private PVStaticText2 amountCaptionStaticText = new PVStaticText2();
	private PVStaticText2 separator2CaptionStaticText = new PVStaticText2();
	private MultiTextArea paymentRemarkStaticText = new MultiTextArea();
	private PVButton2 submitButton = new PVButton2();
	private PVButton2 resetButton = new PVButton2();
	private PVStaticText2 messageCaptionStaticText = new PVStaticText2();
	private JTextField emailEdit = new JTextField();
	private PVStaticText2 reportDateStaticText = new PVStaticText2();
	private PVStaticText2 organizationStaticText = new PVStaticText2();
	private PVStaticText2 customerAccountStaticText = new PVStaticText2();
	private JTextField amountEdit = new JTextField();
	private JComboBox currencyChoice = new JComboBox();
	private PVStaticText2 clientCaptionStaticText = new PVStaticText2();
	private PVStaticText2 beneficiaryNameCaptionStaticText = new PVStaticText2();
	private PVStaticText2 bankAccountNoCaptionStaticText = new PVStaticText2();
	private PVStaticText2 bankerNameCaptionStaticText = new PVStaticText2();
	private PVStaticText2 bankerAddressCaptionStaticText = new PVStaticText2();
	private PVStaticText2 swiftCodeCaptionStaticText = new PVStaticText2();
	private PVStaticText2 remarksCaptionStaticText = new PVStaticText2();
	private PVStaticText2 bankAccountCaptionStaticText = new PVStaticText2();
	//private TextArea beneficiaryNameTextArea = new TextArea();
	//private TextArea bankerAddressTextArea = new TextArea();
	//private TextArea remarksTextArea = new TextArea();
	private MultiTextArea beneficiaryNameTextArea = new MultiTextArea();
	private MultiTextArea bankerAddressTextArea = new MultiTextArea();
	private MultiTextArea remarksTextArea = new MultiTextArea();
	private JTextField swiftCodeEdit = new JTextField();
	private JTextField bankerNameEdit = new JTextField();
	private JTextField bankAccountNoEdit = new JTextField();

	private ButtonGroup clientNoCheckboxGroup = new ButtonGroup();
	private JCheckBox clientNo1CheckBox = new JCheckBox("(1)", true);
	private JCheckBox clientNo2CheckBox = new JCheckBox("(2)", false);
	private JCheckBox clientNo3CheckBox = new JCheckBox("(3)", false);
	private JCheckBox clientNo4CheckBox = new JCheckBox("(4)", false);
	private Border border1 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
	private Border border2 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
	private JAdvancedComboBox bankAccountChoice = new JAdvancedComboBox();
	private PVButton2 addButton = new PVButton2();

	public PaymentInstructionForm(TradingConsole owner, SettingsManager settingsManager, Account account)
	{
		super(owner.get_MainForm(), true);
		try
		{
			this._owner = owner;
			this._settingsManager = settingsManager;
			this._account = account;

			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());

			this.initialize();
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	private void initialize()
	{
		/*this.beneficiaryNameTextArea.setEditable(true);
		   this.bankerAddressTextArea.setEditable(true);*/
		this.remarksTextArea.setEditable(true);

		this.setCaption();
		this.setDefaultValue();
		this.getBankAccounts();
	}

	private void showWarning(String info)
	{
		AlertDialogForm.showDialog(this, Language.BankAccount, true, info);
	}

	private void getBankAccounts()
	{
		if(!this._owner.get_TradingConsoleServer().isReady()) return;

		this._owner.get_TradingConsoleServer().beginGetAccountBanksApproved(this._account.get_Id(), new IAsyncCallback()
		{
			public void asyncCallback(IAsyncResult asyncResult)
			{
				DataSet bankAccountsDataSet = _owner.get_TradingConsoleServer().endGetAccountBanksApproved(asyncResult);
				if (bankAccountsDataSet == null)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							String info = StringHelper.format(Language.CannotGetBankAccounts, new Object[]
								{_account.get_Code()});
							showWarning(info);
						}
					});
				}
				else
				{
					initialize(bankAccountsDataSet);
				}
			}
		}, null);
	}

	private void initialize(DataSet dataSet)
	{
		this._owner.get_BankAccountHelper().initializeReferenceData(dataSet);

		this.bankAccountChoice.removeAllItems();

		DataRowCollection rows = dataSet.get_Tables().get_Item(3).get_Rows();
		for (int index = 0; index < rows.get_Count(); index++)
		{
			DataRow row = rows.get_Item(index);

			Guid id = (Guid)row.get_Item("Id");

			Object value = row.get_Item("CountryID");
			Country country = value == DBNull.value ? Country.Others : this._owner.get_BankAccountHelper().getCountry( (Long)value);

			value = row.get_Item("BankId");
			Bank bank = value == DBNull.value ? null : this._owner.get_BankAccountHelper().getBank( (Guid)value);

			value = row.get_Item("BankName");
			String bankName = value == DBNull.value ? null : (String)value;

			String accountBankNo = (String)row.get_Item("AccountBankNo");
			String accountBankType = (String)row.get_Item("AccountBankType");
			String accountOpener = (String)row.get_Item("AccountOpener");

			value = row.get_Item("AccountBankProp");
			String accountBankProp = value == DBNull.value ? null : (String)value;

			value = row.get_Item("AccountBankBCId");
			Currency accountBankBaseCurrency = value == DBNull.value ? null : this._settingsManager.getCurrency( (Guid)value);

			value = row.get_Item("AccountBankBCName");
			String accountBankBCName = value == DBNull.value ? null : (String)value;

			value = row.get_Item("IdType");
			String idType = value == DBNull.value ? null : (String)value;

			value = row.get_Item("IdNo");
			String idNo = value == DBNull.value ? null : (String)value;

			value = row.get_Item("BankProvinceId");
			Province province = value == DBNull.value ? null : this._owner.get_BankAccountHelper().getProvince( (Long)value);

			value = row.get_Item("BankCityId");
			City city = value == DBNull.value ? null : this._owner.get_BankAccountHelper().getCity( (Long)value);

			value = row.get_Item("BankAddress");
			String bankAddress = value == DBNull.value ? null : (String)value;

			value = row.get_Item("SwiftCode");
			String swiftCode = value == DBNull.value ? null : (String)value;

			BankAccount bankAccount = new BankAccount(id, this._account, country, bank, bankName, accountBankNo, accountBankType,
				accountOpener, accountBankProp, accountBankBaseCurrency, accountBankBCName, idType, idNo, province, city,
				bankAddress, swiftCode);

			this.bankAccountChoice.addItem(bankAccount.get_Summary(), bankAccount);
		}
	}

	private void setCaption()
	{
		this.paymentInstructionCaptionStaticText.setText(PaymentInstructionLanguage.paymentInstructionCaption);
		this.reportDateCaptionStaticText.setText(PaymentInstructionLanguage.reportDateCaption);
		this.organizationCaptionStaticText.setText(PaymentInstructionLanguage.organizationCaption);
		this.customerAccountCaptionStaticText.setText(PaymentInstructionLanguage.customerAccountCaption);
		this.emailCaptionStaticText.setText(PaymentInstructionLanguage.emailCaption);
		this.clientCaptionStaticText.setText(PaymentInstructionLanguage.clientCaption);
		this.clientNo1CheckBox.setLabel(PaymentInstructionLanguage.clientNo1Caption);
		this.clientNo2CheckBox.setLabel(PaymentInstructionLanguage.clientNo2Caption);
		this.clientNo3CheckBox.setLabel(PaymentInstructionLanguage.clientNo3Caption);
		this.clientNo4CheckBox.setLabel(PaymentInstructionLanguage.clientNo4Caption);
		this.separator1CaptionStaticText.setText(PaymentInstructionLanguage.separatorCaption);
		this.amountCaptionStaticText.setText(PaymentInstructionLanguage.amountCaption);
		this.beneficiaryNameCaptionStaticText.setText(PaymentInstructionLanguage.beneficiaryNameCaption);
		this.bankAccountNoCaptionStaticText.setText(PaymentInstructionLanguage.bankAccountNoCaption);
		this.bankerNameCaptionStaticText.setText(PaymentInstructionLanguage.bankerNameCaption);
		this.bankerAddressCaptionStaticText.setText(PaymentInstructionLanguage.bankerAddressCaption);
		this.swiftCodeCaptionStaticText.setText(PaymentInstructionLanguage.swiftCodeCaption);
		this.remarksCaptionStaticText.setText(PaymentInstructionLanguage.remarksCaption);
		this.separator2CaptionStaticText.setText(PaymentInstructionLanguage.separatorCaption);
		this.submitButton.setText(PaymentInstructionLanguage.submitButtonCaption);
		this.resetButton.setText(PaymentInstructionLanguage.resetButtonCaption);
	}

	private void setDefaultValue()
	{
		this.messageCaptionStaticText.setText("");

		this.reportDateStaticText.setText(XmlConvert.toString(this._owner.get_TradingConsoleServer().appTime().addMinutes(Parameter.timeZoneMinuteSpan),
			"yyyy/MM/dd HH:mm:ss"));
		this.organizationStaticText.setText(this._account.get_OrganizationName());
		Customer customer = this._settingsManager.get_Customer();
		this.customerAccountStaticText.setText(AppToolkit.getCustomerAccountName(customer, this._account));
		this.emailEdit.setText(customer.get_Email());
		this.clientNo1CheckBox.setSelected(true);

		this.currencyChoice.removeAllItems();
		HashMap<CompositeKey2<Guid, Guid>, AccountCurrency> accountCurrencies = this._account.get_AccountCurrencies();
		for (Iterator<AccountCurrency> iterator = accountCurrencies.values().iterator(); iterator.hasNext(); )
		{
			AccountCurrency accountCurrency = iterator.next();
			String currencyCode = accountCurrency.get_Currency().get_Code();
			this.currencyChoice.addItem(currencyCode);
		}
		//this.currencyChoice.sort(true);
		this.currencyChoice.setSelectedIndex(0);
		this.currencyChoice.setEditable(false);

		this.amountEdit.setText("0.0");
		this.amountEdit.setForeground(Color.RED);
		this.currencyChoice_OnChange();

		this.updateSubmitStatus();
	}

	private void reset()
	{
		this.setDefaultValue();

		this.messageCaptionStaticText.setText("");

		this.beneficiaryNameTextArea.setText("");
		this.bankAccountNoEdit.setText("");
		this.bankerNameEdit.setText("");
		this.bankerAddressTextArea.setText("");
		this.swiftCodeEdit.setText("");
		this.remarksTextArea.setText("");

		this.emailEdit.requestFocus();
		this.updateSubmitStatus();
		this.bankAccountChoice.setSelectedItem(null);
	}

	private void submit()
	{
		Customer customer = this._settingsManager.get_Customer();
		String email = this.emailEdit.getText();
		String receive = Parameter.receiveAddress;
		String organizationName = this._account.get_OrganizationName();
		String customerName = customer.get_CustomerName();
		String reportDate = this.reportDateStaticText.getText();
		String accountCode = this._account.get_Code();
		String currency = this.currencyChoice.getSelectedItem().toString();
		currency = this._settingsManager.getCurrency(currency).get_RealCode();
		String currencyValue = this.amountEdit.getText();
		String beneficiaryName = this.beneficiaryNameTextArea.getText();
		String bankAccount = this.bankAccountNoEdit.getText();
		String bankerName = this.bankerNameEdit.getText();
		String bankerAddress = this.bankerAddressTextArea.getText();
		String swiftCode = this.swiftCodeEdit.getText();
		String remarks = this.remarksTextArea.getText();
		String thisisClient = "(1)";
		/*if (this.clientNo1CheckBox.isSelected())
		   {
		 thisisClient = "(1)";
		   }
		   if (this.clientNo2CheckBox.isSelected())
		   {
		 thisisClient = "(2)";
		   }
		   if (this.clientNo3CheckBox.isSelected())
		   {
		 thisisClient = "(3)";
		   }
		   if (this.clientNo4CheckBox.isSelected())
		   {
		 thisisClient = "(4)";
		   }*/
		if (StringHelper.isNullOrEmpty(email)
			//|| email.indexOf(',', 0) != -1
			|| StringHelper.isNullOrEmpty(organizationName)
			|| StringHelper.isNullOrEmpty(accountCode)
			|| StringHelper.isNullOrEmpty(currency)
			|| StringHelper.isNullOrEmpty(currencyValue)
			|| (!StringHelper.isNullOrEmpty(currencyValue) && AppToolkit.convertStringToDouble(this.amountEdit.getText()) <= 0)
			|| StringHelper.isNullOrEmpty(beneficiaryName)
			|| StringHelper.isNullOrEmpty(bankAccount))
		{
			this.messageCaptionStaticText.setText(Language.PaymentInstructionPageSubmitAlert0);
			this.emailEdit.requestFocus();
		}
		else
		{
			if (!this._account.hasSufficientUsableMargin(currency, currencyValue))
			{
				this.messageCaptionStaticText.setText(Language.UsableMarginNotSufficient);
				this.amountEdit.setText("");
				this.amountEdit.requestFocus();
				return;
			}
			//Dialog2.showDialog(this, Language.notify2, true, Language.ConfirmSendEmail);
			//if (Dialog2.dialogOption == DialogOption.Yes)
			{
				if (this._settingsManager.get_SystemParameter().get_EnableMarginPin())
				{
					VerifyMarginPin verifyMarginPin = new VerifyMarginPin(this, this._owner.get_TradingConsoleServer(), this._account, this);
					JideSwingUtilities.centerWindow(verifyMarginPin);
					verifyMarginPin.show();
					verifyMarginPin.toFront();
					verifyMarginPin.requestFocus();
				}
				else
				{
					pay();
				}
			}
		}
	}

	public void OnMarginPinVerified(boolean isValidMarginPin)
	{
		if(isValidMarginPin)
		{
			pay();
		}
		else
		{
			this.dispose();
		}
	}


	private void pay()
	{
		Customer customer = this._settingsManager.get_Customer();
		String email = this.emailEdit.getText();
		String receive = Parameter.receiveAddress;
		String organizationName = this._account.get_OrganizationName();
		String customerName = customer.get_CustomerName();
		String reportDate = this.reportDateStaticText.getText();
		String accountCode = this._account.get_Code();
		String currency = this.currencyChoice.getSelectedItem().toString();
		currency = this._settingsManager.getCurrency(currency).get_RealCode();
		String currencyValue = this.amountEdit.getText();
		String beneficiaryName = this.beneficiaryNameTextArea.getText();
		String bankAccount = this.bankAccountNoEdit.getText();
		String bankerName = this.bankerNameEdit.getText();
		String bankerAddress = this.bankerAddressTextArea.getText();
		String swiftCode = this.swiftCodeEdit.getText();
		String remarks = this.remarksTextArea.getText();
		String thisisClient = "(1)";

		String result = this._owner.get_TradingConsoleServer().paymentInstruction(email, receive, organizationName, customerName, reportDate,
			accountCode,
			currency, currencyValue, beneficiaryName, bankAccount, bankerName, bankerAddress, swiftCode, remarks, thisisClient);

		if (result.indexOf("Error") != 0)
		{
			String logAction = Language.WebServicePaymentInstructionMessage1 + result;
			this._owner.saveLog(LogCode.Other, logAction, Guid.empty, this._account.get_Id());
			AlertDialogForm.showDialog(this, Language.messageContentFormTitle, true, logAction);
			this.dispose();
		}
		else
		{
			this.messageCaptionStaticText.setText(Language.WebServicePaymentInstructionMessage0);
		}
	}

	private void currencyChoice_OnChange()
	{
		if (currencyChoice.getSelectedIndex() < 0)
		{
			return;
		}
		String currencyCode = currencyChoice.getSelectedItem().toString();
		Currency currency = this._settingsManager.getCurrency(currencyCode);
		if (currency == null)
		{
			return;
		}
		short decimals = currency.get_Decimals();
		this.amountEdit.setText(AppToolkit.format(AppToolkit.convertStringToDouble(this.amountEdit.getText()), decimals));
	}

	private void updateSubmitStatus()
	{
		Customer customer = this._settingsManager.get_Customer();
		String email = this.emailEdit.getText();
		String receive = Parameter.receiveAddress;
		String organizationName = this._account.get_OrganizationName();
		String customerName = customer.get_CustomerName();
		String reportDate = this.reportDateStaticText.getText();
		String accountCode = this._account.get_Code();
		String currency = this.currencyChoice.getSelectedItem() == null ? "" : this.currencyChoice.getSelectedItem().toString();
		String currencyValue = this.amountEdit.getText();
		String beneficiaryName = this.beneficiaryNameTextArea.getText();
		String bankAccount = this.bankAccountNoEdit.getText();
		String bankerName = this.bankerNameEdit.getText();
		String bankerAddress = this.bankerAddressTextArea.getText();
		String swiftCode = this.swiftCodeEdit.getText();
		String remarks = this.remarksTextArea.getText();
		String thisisClient = "(1)";

		this.emailEdit.setForeground(EmailInputVerifier.isValidEmail(true, true, email) ? Color.BLACK : Color.RED);
		if (!StringHelper.isNullOrEmpty(currencyValue)
			&& AppToolkit.convertStringToDouble(this.amountEdit.getText()) <= 0)
		{
			this.amountEdit.setForeground(Color.RED);
		}
		else
		{
			this.amountEdit.setForeground(Color.BLACK);
		}

		boolean enable = true;
		boolean allowEditBankAccountInTrader = this._settingsManager.get_SystemParameter().get_AllowEditBankAccountInTrader();
		if (!EmailInputVerifier.isValidEmail(false, true, email)
			|| StringHelper.isNullOrEmpty(organizationName)
			|| StringHelper.isNullOrEmpty(accountCode)
			|| StringHelper.isNullOrEmpty(currency)
			|| StringHelper.isNullOrEmpty(currencyValue)
			|| (!StringHelper.isNullOrEmpty(currencyValue) && AppToolkit.convertStringToDouble(this.amountEdit.getText()) <= 0)
			|| StringHelper.isNullOrEmpty(beneficiaryName)
			|| StringHelper.isNullOrEmpty(bankAccount)
			|| (!Parameter.isAllowNull("PaymentInstruction", "BankName") && StringHelper.isNullOrEmpty(bankerName))
			|| (allowEditBankAccountInTrader && !Parameter.isAllowNull("PaymentInstruction", "BankAddress") && StringHelper.isNullOrEmpty(bankerAddress)))
		{
			enable = false;
		}

		this.submitButton.setEnabled(enable);
	}

	private static class KeyAdapter extends java.awt.event.KeyAdapter
	{
		private PaymentInstructionForm _owner;

		public KeyAdapter(PaymentInstructionForm owner)
		{
			this._owner = owner;
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			this._owner.updateSubmitStatus();
		}
	}

	private void addBankAccount()
	{
		BankAccountEditForm form = new BankAccountEditForm(this, this._owner, this._account);
		this.showEditForm(form);
	}

	private void showEditForm(BankAccountEditForm form)
	{
		JideSwingUtilities.centerWindow(form);
		form.show();
		form.addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent e)
			{
				BankAccountEditForm form = (BankAccountEditForm)e.getWindow();
				if (form.hasChange())
				{
					getBankAccounts();
				}
			}
		});
	}

	private void jbInit() throws Exception
	{
		this.addWindowListener(new PaymentInstructionForm_this_windowAdapter(this));
		String paymentRemark = this._settingsManager.getPaymentInstructionRemark(this._account.get_OrganizationId());

		this.setSize(465, paymentRemark == null ? 640 : 650);
		this.setResizable(false);
		this.setLayout(null);
		this.setTitle(Language.PaymentInstructionPrompt);
		this.setBackground(FormBackColor.paymentInstructionForm);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		/*this.clientNoCheckboxGroup.add(this.clientNo1CheckBox);
		   this.clientNoCheckboxGroup.add(this.clientNo2CheckBox);
		   this.clientNoCheckboxGroup.add(this.clientNo3CheckBox);
		   this.clientNoCheckboxGroup.add(this.clientNo4CheckBox);*/

		paymentInstructionCaptionStaticText.setBounds(new Rectangle(15, 25, 392, 20));
		paymentInstructionCaptionStaticText.setFont(new Font("SansSerif", Font.BOLD, 13));
		paymentInstructionCaptionStaticText.setText("PAYMENT INSTRUCTION");
		paymentInstructionCaptionStaticText.setAlignment(1);
		reportDateCaptionStaticText.setBounds(new Rectangle(16, 85, 166, 20));
		reportDateCaptionStaticText.setText("Date:");
		organizationCaptionStaticText.setBounds(new Rectangle(16, 111, 166, 20));
		organizationCaptionStaticText.setText("To:");
		customerAccountCaptionStaticText.setBounds(new Rectangle(16, 140, 166, 20));
		customerAccountCaptionStaticText.setText("From:");
		emailCaptionStaticText.setBounds(new Rectangle(16, 165, 166, 20));
		emailCaptionStaticText.setText("Registrated E-mail:");
		this.add(PVStaticText2.createNotNullFlagFor(emailCaptionStaticText));

		separator1CaptionStaticText.setBounds(new Rectangle(17, 218, 428, 2));
		separator1CaptionStaticText.setBorder(border1);
		separator1CaptionStaticText.setPreferredSize(new Dimension(428, 2));
		separator1CaptionStaticText.setToolTipText("");
		amountCaptionStaticText.setBounds(new Rectangle(16, 235, 166, 20));
		amountCaptionStaticText.setText("Amount:");
		this.add(PVStaticText2.createNotNullFlagFor(amountCaptionStaticText));


		separator2CaptionStaticText.setBounds(new Rectangle(16, paymentRemark == null ? 541 : 573, 428, 2));
		if(paymentRemark != null)
		{
			paymentRemarkStaticText.setBorder(new LineBorder(Color.BLACK, 1));
			paymentRemarkStaticText.setEditable(false);
			paymentRemarkStaticText.setBounds(16, 531, 430, 40);
			paymentRemarkStaticText.setTextColor(Color.RED);
			paymentRemarkStaticText.setText(paymentRemark);
			this.add(paymentRemarkStaticText);
		}
		separator2CaptionStaticText.setBorder(border2);
		separator2CaptionStaticText.setPreferredSize(new Dimension(428, 2));
		separator2CaptionStaticText.setToolTipText("");
		notifyCaptionStaticText.setText(Language.PaymentInstructionPageSubmitAlert1);
		notifyCaptionStaticText.setHorizontalAlignment(SwingConstants.CENTER);
		notifyCaptionStaticText.setBounds(new Rectangle(17, 543, 428, 23));
		submitButton.setBounds(new Rectangle(155, paymentRemark == null ? 578 : 598, 70, 23));
		submitButton.setText("Submit");
		submitButton.addActionListener(new PaymentInstructionForm_submitButton_actionAdapter(this));
		resetButton.setBounds(new Rectangle(235, paymentRemark == null ? 578 : 598, 70, 23));
		resetButton.setText("Reset");
		resetButton.addActionListener(new PaymentInstructionForm_resetButton_actionAdapter(this));
		messageCaptionStaticText.setBounds(new Rectangle(16, paymentRemark == null ? 558 : 578, 423, 20));
		messageCaptionStaticText.setAlignment(1);
		emailEdit.setBounds(new Rectangle(196, 164, 249, 20));
		emailEdit.setInputVerifier(new EmailInputVerifier(true, true));
		reportDateStaticText.setBounds(new Rectangle(196, 84, 249, 20));
		reportDateStaticText.setText("2006/10/20");
		organizationStaticText.setBounds(new Rectangle(196, 113, 249, 20));
		customerAccountStaticText.setBounds(new Rectangle(196, 138, 249, 20));
		amountEdit.setBounds(new Rectangle(196, 234, 100, 20));
		amountEdit.setText("0.00");
		amountEdit.addFocusListener(new PaymentInstructionForm_amountEdit_focusAdapter(this));
		currencyChoice.setBounds(new Rectangle(307, 234, 100, 20));
		currencyChoice.addActionListener(new PaymentInstructionForm_currencyChoice_actionAdapter(this));
		/*clientCaptionStaticText.setBounds(new Rectangle(16, 187, 100, 20));
		   clientCaptionStaticText.setText("This is Client:");*/

		beneficiaryNameCaptionStaticText.setBounds(new Rectangle(16, 283, 166, 20));
		beneficiaryNameCaptionStaticText.setText("Name of beneficiary:");
		this.add(PVStaticText2.createNotNullFlagFor(beneficiaryNameCaptionStaticText));
		bankAccountNoCaptionStaticText.setBounds(new Rectangle(16, 332, 166, 20));
		this.add(PVStaticText2.createNotNullFlagFor(bankAccountNoCaptionStaticText));
		bankAccountNoCaptionStaticText.setText("Bank Account No.:");

		bankerNameCaptionStaticText.setBounds(new Rectangle(16, 358, 166, 20));
		bankerNameCaptionStaticText.setText("Name of Banker:");
		this.add(Parameter.isAllowNull("PaymentInstruction", "BankName") ? bankerNameCaptionStaticText :
				 PVStaticText2.createNotNullFlagFor(bankerNameCaptionStaticText));

		bankerAddressCaptionStaticText.setBounds(new Rectangle(16, 401, 166, 20));
		bankerAddressCaptionStaticText.setText("Banker\'s Address:");
		boolean allowEditBankAccountInTrader = this._settingsManager.get_SystemParameter().get_AllowEditBankAccountInTrader();
		this.add(Parameter.isAllowNull("PaymentInstruction", "BankAddress") || !allowEditBankAccountInTrader ? bankerAddressCaptionStaticText :
				 PVStaticText2.createNotNullFlagFor(bankerAddressCaptionStaticText));

		swiftCodeCaptionStaticText.setBounds(new Rectangle(16, 445, 166, 20));
		swiftCodeCaptionStaticText.setText("Swift Code:");
		remarksCaptionStaticText.setBounds(new Rectangle(16, 490, 166, 20));
		remarksCaptionStaticText.setText("Remarks:");

		bankAccountCaptionStaticText.setBounds(new Rectangle(24, 264, 166, 20));
		bankAccountCaptionStaticText.setText(Language.BankAccount);
		this.add(bankAccountCaptionStaticText);
		if(this._settingsManager.get_SystemParameter().get_AllowEditBankAccountInTrader())
		{
			bankAccountChoice.setBounds(new Rectangle(196, 264, 193, 20));
		}
		else
		{
			bankAccountChoice.setBounds(new Rectangle(196, 264, 249, 20));
		}

		this.add(bankAccountChoice);
		this.bankAccountChoice.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if (bankAccountChoice.getSelectedIndex() > -1)
				{
					BankAccount accountBank = (BankAccount)bankAccountChoice.getSelectedValue();
					beneficiaryNameTextArea.setText(accountBank.get_AccountOpener());
					bankAccountNoEdit.setText(accountBank.get_BankAccountNo());
					bankerAddressTextArea.setText(accountBank.getAddress());
					String bankName = StringHelper.isNullOrEmpty(accountBank.get_BankName()) ? accountBank.get_Bank().get_Name() : accountBank.get_BankName();
					bankerNameEdit.setText(bankName);
					swiftCodeEdit.setText(accountBank.get_SwiftCode());
				}
			}
		});

		if(this._settingsManager.get_SystemParameter().get_AllowEditBankAccountInTrader())
		{
			addButton.setText(Language.AddCaption);
			addButton.setBounds(new Rectangle(391, 264, 55, 20));
			this.add(addButton);
			boolean canAddBankAccount = this._settingsManager.get_SystemParameter().get_MaxCustomerBankNo() != 0;
			addButton.setEnabled(canAddBankAccount && this._account.get_CustomerId().compareTo(this._settingsManager.get_Customer().get_UserId()) == 0);
			addButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					addBankAccount();
				}
		});
		}

		beneficiaryNameTextArea.setBounds(new Rectangle(196, 288, 249, 40));
		bankerAddressTextArea.setBounds(new Rectangle(196, 382, 249, 58));
		remarksTextArea.setBounds(new Rectangle(196, 469, 249, 55));
		swiftCodeEdit.setBounds(new Rectangle(196, 445, 249, 20));
		bankerNameEdit.setBounds(new Rectangle(196, 358, 249, 20));
		bankAccountNoEdit.setBounds(new Rectangle(196, 332, 249, 20));
		/*clientNo1CheckBox.setFont(new java.awt.Font("SansSerif", Font.BOLD, 12));
		   clientNo1CheckBox.setBounds(new Rectangle(196, 187, 49, 20));
		   clientNo2CheckBox.setFont(new java.awt.Font("SansSerif", Font.BOLD, 12));
		   clientNo2CheckBox.setBounds(new Rectangle(255, 187, 47, 20));
		   clientNo3CheckBox.setFont(new java.awt.Font("SansSerif", Font.BOLD, 12));
		   clientNo3CheckBox.setBounds(new Rectangle(313, 187, 45, 20));
		   clientNo4CheckBox.setFont(new java.awt.Font("SansSerif", Font.BOLD, 12));
		   clientNo4CheckBox.setBounds(new Rectangle(373, 187, 45, 20));*/
		this.add(paymentInstructionCaptionStaticText);
		this.add(reportDateStaticText);
		this.add(organizationStaticText);
		KeyAdapter keyAdapter = new KeyAdapter(this);
		this.add(emailEdit);
		emailEdit.addKeyListener(keyAdapter);
		this.add(customerAccountStaticText);
		this.add(currencyChoice);
		this.add(amountEdit);
		amountEdit.addKeyListener(keyAdapter);
		this.add(beneficiaryNameTextArea);
		beneficiaryNameTextArea.addKeyAdapter(keyAdapter);
		this.add(reportDateCaptionStaticText);
		this.add(organizationCaptionStaticText);
		this.add(customerAccountCaptionStaticText);
		this.add(emailCaptionStaticText);
		this.add(amountCaptionStaticText);
		//this.add(clientCaptionStaticText);
		this.add(bankAccountNoCaptionStaticText);
		this.add(bankAccountNoEdit);
		bankAccountNoEdit.addKeyListener(keyAdapter);
		this.add(bankerNameCaptionStaticText);
		this.add(bankerAddressCaptionStaticText);
		this.add(swiftCodeCaptionStaticText);
		this.add(swiftCodeEdit);
		swiftCodeEdit.addKeyListener(keyAdapter);
		this.add(bankerAddressTextArea);
		bankerAddressTextArea.addKeyAdapter(keyAdapter);
		this.add(remarksTextArea);
		remarksTextArea.addKeyAdapter(keyAdapter);
		this.add(remarksCaptionStaticText);
		this.add(bankerNameEdit);
		bankerNameEdit.addKeyListener(keyAdapter);
		this.add(beneficiaryNameCaptionStaticText);
		this.add(separator2CaptionStaticText);
		/*this.add(clientNo1CheckBox);
		   this.add(clientNo2CheckBox);
		   this.add(clientNo3CheckBox);
		   this.add(clientNo4CheckBox);*/
		//this.add(this.notifyCaptionStaticText);
		this.add(resetButton);
		this.add(messageCaptionStaticText);
		this.add(submitButton);
		this.add(separator1CaptionStaticText);
		this.updateSubmitStatus();

		beneficiaryNameTextArea.setEnabled(!this._settingsManager.get_SystemParameter().get_BankAccountOnly());
		beneficiaryNameTextArea.setEditable(!this._settingsManager.get_SystemParameter().get_BankAccountOnly());
		bankAccountNoEdit.setEnabled(!this._settingsManager.get_SystemParameter().get_BankAccountOnly());
		bankerAddressTextArea.setEnabled(!this._settingsManager.get_SystemParameter().get_BankAccountOnly());
		bankerAddressTextArea.setEditable(!this._settingsManager.get_SystemParameter().get_BankAccountOnly());
		bankerNameEdit.setEnabled(!this._settingsManager.get_SystemParameter().get_BankAccountOnly());
		swiftCodeEdit.setEnabled(!this._settingsManager.get_SystemParameter().get_BankAccountOnly());
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	public void submitButton_actionPerformed(ActionEvent e)
	{
		this.submit();
	}

	public void resetButton_actionPerformed(ActionEvent e)
	{
		this.reset();
	}

	public void currencyChoice_actionPerformed(ActionEvent e)
	{
		this.currencyChoice_OnChange();
	}

	public void amountEdit_focusLost(FocusEvent e)
	{
		this.currencyChoice_OnChange();
	}

	class PaymentInstructionForm_this_windowAdapter extends WindowAdapter
	{
		private PaymentInstructionForm adaptee;
		PaymentInstructionForm_this_windowAdapter(PaymentInstructionForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class PaymentInstructionForm_amountEdit_focusAdapter extends FocusAdapter
{
	private PaymentInstructionForm adaptee;
	PaymentInstructionForm_amountEdit_focusAdapter(PaymentInstructionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.amountEdit_focusLost(e);
	}
}

class PaymentInstructionForm_currencyChoice_actionAdapter implements ActionListener
{
	private PaymentInstructionForm adaptee;
	PaymentInstructionForm_currencyChoice_actionAdapter(PaymentInstructionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.currencyChoice_actionPerformed(e);
	}
}

class PaymentInstructionForm_resetButton_actionAdapter implements ActionListener
{
	private PaymentInstructionForm adaptee;
	PaymentInstructionForm_resetButton_actionAdapter(PaymentInstructionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.resetButton_actionPerformed(e);
	}
}

class PaymentInstructionForm_submitButton_actionAdapter implements ActionListener
{
	private PaymentInstructionForm adaptee;
	PaymentInstructionForm_submitButton_actionAdapter(PaymentInstructionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}
