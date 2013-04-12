package tradingConsole.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;
import javax.swing.JComboBox;

import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Account;
import java.awt.event.WindowEvent;
import java.awt.event.FocusEvent;
import tradingConsole.AppToolkit;
import java.text.DateFormat;
import java.awt.Rectangle;
import tradingConsole.ui.language.FundTransferLanguage;
import tradingConsole.settings.Parameter;
import framework.xml.XmlConvert;
import tradingConsole.Customer;
import java.util.HashMap;
import framework.CompositeKey2;
import framework.Guid;
import tradingConsole.AccountCurrency;
import java.util.Iterator;
import framework.DateTime;
import framework.StringHelper;
import tradingConsole.ui.language.Language;
import java.util.Date;
import tradingConsole.Currency;
import tradingConsole.ui.colorHelper.FormBackColor;
import java.awt.event.ActionEvent;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import java.awt.Color;
import javax.swing.border.Border;
import javax.swing.JDialog;

public class FundTransferForm extends JDialog
{
	private TradingConsole _owner;
	private SettingsManager _settingsManager;
	private Account _account;
	private PVStaticText2 callMarginExtensionCaptionStaticText = new PVStaticText2();
	private PVStaticText2 reportDateCaptionStaticText = new PVStaticText2();
	private PVStaticText2 organizationCaptionStaticText = new PVStaticText2();
	private PVStaticText2 customerAccountCaptionStaticText = new PVStaticText2();
	private PVStaticText2 emailCaptionStaticText = new PVStaticText2();
	private PVStaticText2 separator1CaptionStaticText = new PVStaticText2();
	private PVStaticText2 effectiveDateCaptionStaticText = new PVStaticText2();
	private PVStaticText2 transferAmountCaptionStaticText = new PVStaticText2();
	private PVStaticText2 separator2CaptionStaticText = new PVStaticText2();
	private PVButton2 submitButton = new PVButton2();
	private PVButton2 resetButton = new PVButton2();
	private PVStaticText2 messageCaptionStaticText = new PVStaticText2();
	private JTextField emailEdit = new JTextField();
	private PVStaticText2 reportDateStaticText = new PVStaticText2();
	private PVStaticText2 organizationStaticText = new PVStaticText2();
	private PVStaticText2 customerAccountStaticText = new PVStaticText2();
	private JFormattedTextField effectiveDateDate = new JFormattedTextField(DateFormat.getDateInstance());
	private JTextField transferAmountEdit = new JTextField();
	private JComboBox currencyChoice = new JComboBox();
	private PVStaticText2 accountHolderNameCaptionStaticText = new PVStaticText2();
	private PVStaticText2 transferToCaptionStaticText = new PVStaticText2();
	private PVStaticText2 tradingAccountNoCaptionStaticText = new PVStaticText2();
	private JTextField tradingAccountNoEdit = new JTextField();
	private JTextField accountHolderNameEdit = new JTextField();
	private Border border1 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
	private Border border2 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));

	public FundTransferForm(TradingConsole owner, SettingsManager settingsManager, Account account)
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
		this.setCaption();
		this.setDefaultValue();
	}

	private void setCaption()
	{
		this.callMarginExtensionCaptionStaticText.setText(FundTransferLanguage.fundTransferCaption);
		this.reportDateCaptionStaticText.setText(FundTransferLanguage.reportDateCaption);
		this.organizationCaptionStaticText.setText(FundTransferLanguage.organizationCaption);
		this.customerAccountCaptionStaticText.setText(FundTransferLanguage.customerAccountCaption);
		this.emailCaptionStaticText.setText(FundTransferLanguage.emailCaption);
		this.separator1CaptionStaticText.setText(FundTransferLanguage.separatorCaption);
		this.effectiveDateCaptionStaticText.setText(FundTransferLanguage.effectiveDateCaption);
		this.transferAmountCaptionStaticText.setText(FundTransferLanguage.transferAmountCaption);
		this.separator2CaptionStaticText.setText(FundTransferLanguage.separatorCaption);
		this.transferToCaptionStaticText.setText(FundTransferLanguage.transferToCaption);
		this.tradingAccountNoCaptionStaticText.setText(FundTransferLanguage.tradingAccountNoCaption);
		this.accountHolderNameCaptionStaticText.setText(FundTransferLanguage.accountHolderNameCaption);
		this.submitButton.setText(FundTransferLanguage.submitButtonCaption);
		this.resetButton.setText(FundTransferLanguage.resetButtonCaption);
	}

	private void setDefaultValue()
	{
		this.messageCaptionStaticText.setText("");

		//this.effectiveDateDate.setFormat(2);
		this.reportDateStaticText.setText(XmlConvert.toString(this._owner.get_TradingConsoleServer().appTime().addMinutes(Parameter.timeZoneMinuteSpan),
			"yyyy/MM/dd HH:mm:ss"));
		this.organizationStaticText.setText(this._account.get_OrganizationName());
		Customer customer = this._settingsManager.get_Customer();
		this.customerAccountStaticText.setText(customer.get_CustomerName() + "," + this._account.get_Code());
		this.emailEdit.setText(customer.get_Email());

		this.currencyChoice.removeAllItems();
		HashMap<CompositeKey2<Guid, Guid>, AccountCurrency> accountCurrencies = this._account.get_AccountCurrencies();
		for (Iterator<AccountCurrency> iterator =accountCurrencies.values().iterator(); iterator.hasNext(); )
		{
			AccountCurrency accountCurrency = iterator.next();
			String currencyCode = accountCurrency.get_Currency().get_Code();
			this.currencyChoice.addItem(currencyCode);
		}
		//this.currencyChoice.sort(true);
		this.currencyChoice.setSelectedIndex(0);
		this.currencyChoice.setEditable(false);

		DateTime effectiveDate = this._owner.get_TradingConsoleServer().appTime().addMinutes(Parameter.timeZoneMinuteSpan);
		effectiveDate = effectiveDate.addDays(Parameter.extendDay);
		this.effectiveDateDate.setValue(effectiveDate.get_Date().toDate());
		this.transferAmountEdit.setText("0.0");
		this.currencyChoice_OnChange();
	}

	private void reset()
	{
		this.setDefaultValue();
		this.messageCaptionStaticText.setText("");
		this.tradingAccountNoEdit.setText("");
		this.accountHolderNameEdit.setText("");
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
		String currencyValue = this.transferAmountEdit.getText();
		String replyDate = XmlConvert.toString((DateTime.fromDate((Date)this.effectiveDateDate.getValue()).addMinutes(0-Parameter.timeZoneMinuteSpan)), "yyyy/MM/dd");
		String bankAccount = tradingAccountNoEdit.getText();
		String beneficiaryName = accountHolderNameEdit.getText();
		if (StringHelper.isNullOrEmpty(email)
			//|| email.indexOf(',', 0) != -1
			|| StringHelper.isNullOrEmpty(organizationName)
			|| StringHelper.isNullOrEmpty(accountCode)
			|| StringHelper.isNullOrEmpty(currency)
			|| StringHelper.isNullOrEmpty(currencyValue)
			|| (!StringHelper.isNullOrEmpty(currencyValue) && AppToolkit.convertStringToDouble(this.transferAmountEdit.getText()) <= 0)
			|| StringHelper.isNullOrEmpty(bankAccount)
			|| StringHelper.isNullOrEmpty(beneficiaryName)
			|| StringHelper.isNullOrEmpty(replyDate))
		{
			this.messageCaptionStaticText.setText(Language.FundTransferPageSubmitAlert0);
			this.emailEdit.requestFocus();
		}
		else
		{
			//Dialog2.showDialog(this, Language.notify2, true, Language.ConfirmSendEmail);
			//if (Dialog2.dialogOption == DialogOption.Yes)
			{
				boolean isSucceed = this._owner.get_TradingConsoleServer().fundTransfer(email, receive, organizationName, customerName, reportDate, currency,
					currencyValue,
					accountCode, bankAccount, beneficiaryName, replyDate);

				this.messageCaptionStaticText.setText( (isSucceed) ? Language.WebServiceFundTransferMessage1 : Language.WebServiceFundTransferMessage0);
			}
		}
	}

	private void effectiveDateDateValidate()
	{
		try
		{
			DateTime effectiveDate = DateTime.fromDate((Date)this.effectiveDateDate.getValue());
			DateTime appTime = this._owner.get_TradingConsoleServer().appTime();
			DateTime effectiveDate2 = appTime.addMinutes(Parameter.timeZoneMinuteSpan);
			effectiveDate2 = effectiveDate2.addDays(Parameter.extendDay);
			if (effectiveDate.before(appTime.get_Date()) || effectiveDate.after(effectiveDate2.get_Date()))
			{
				this.effectiveDateDate.setValue(effectiveDate2.get_Date().toDate());
				this.effectiveDateDate.requestFocus();
			}
		}
		catch (Throwable exception)
		{
		}

	}

	private void currencyChoice_OnChange()
	{
		String currencyCode = currencyChoice.getSelectedItem().toString();
		Currency currency = this._settingsManager.getCurrency(currencyCode);
		if (currency==null) return;
		short decimals = currency.get_Decimals();
		this.transferAmountEdit.setText(AppToolkit.format(AppToolkit.convertStringToDouble(this.transferAmountEdit.getText()),decimals));
	}

	private void jbInit() throws Exception
	{
		this.addWindowListener(new FundTransferForm_this_windowAdapter(this));

		this.setSize(465, 460);
		this.setResizable(false);
		this.setLayout(null);
		this.setTitle(Language.FundTransferPrompt);
		this.setBackground(FormBackColor.fundTransferForm);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		callMarginExtensionCaptionStaticText.setBounds(new Rectangle(18, 24, 392, 20));
		callMarginExtensionCaptionStaticText.setText("FUND TRANSFER");
		callMarginExtensionCaptionStaticText.setAlignment(1);
		reportDateCaptionStaticText.setBounds(new Rectangle(17, 54, 166, 20));
		reportDateCaptionStaticText.setText("Date:");
		organizationCaptionStaticText.setBounds(new Rectangle(18, 84, 166, 20));
		organizationCaptionStaticText.setText("To:");
		customerAccountCaptionStaticText.setBounds(new Rectangle(19, 114, 166, 20));
		customerAccountCaptionStaticText.setText("From:");
		emailCaptionStaticText.setBounds(new Rectangle(19, 144, 166, 20));
		emailCaptionStaticText.setText("Registrated E-mail:");
		separator1CaptionStaticText.setBounds(new Rectangle(20, 245, 422, 2));
		separator1CaptionStaticText.setBorder(border1);
		separator1CaptionStaticText.setPreferredSize(new Dimension(422, 2));
		separator1CaptionStaticText.setToolTipText("");
		effectiveDateCaptionStaticText.setBounds(new Rectangle(19, 174, 166, 20));
		effectiveDateCaptionStaticText.setText("Effective Date:");
		transferAmountCaptionStaticText.setBounds(new Rectangle(18, 204, 166, 20));
		transferAmountCaptionStaticText.setText("Transfer Amount:");
		separator2CaptionStaticText.setBounds(new Rectangle(19, 361, 424, 2));
		separator2CaptionStaticText.setBorder(border2);
		separator2CaptionStaticText.setPreferredSize(new Dimension(424, 2));
		separator2CaptionStaticText.setToolTipText("");
		submitButton.setBounds(new Rectangle(151, 396, 70, 23));
		submitButton.setText("Submit");
		submitButton.addActionListener(new FundTransferForm_submitButton_actionAdapter(this));
		resetButton.setBounds(new Rectangle(236, 396, 70, 23));
		resetButton.setText("Reset");
		resetButton.addActionListener(new FundTransferForm_resetButton_actionAdapter(this));
		messageCaptionStaticText.setBounds(new Rectangle(17, 366, 423, 20));
		messageCaptionStaticText.setAlignment(1);
		emailEdit.setBounds(new Rectangle(195, 144, 249, 20));
		emailEdit.setInputVerifier(new EmailInputVerifier(false));
		reportDateStaticText.setBounds(new Rectangle(195, 54, 249, 20));
		reportDateStaticText.setText("2006/10/20");
		organizationStaticText.setBounds(new Rectangle(195, 84, 249, 20));
		customerAccountStaticText.setBounds(new Rectangle(195, 114, 249, 20));
		effectiveDateDate.setBounds(new Rectangle(195, 174, 100, 20));
		//effectiveDateDate.setText("");
		effectiveDateDate.addFocusListener(new FundTransferForm_effectiveDateDate_focusAdapter(this));
		transferAmountEdit.setBounds(new Rectangle(195, 204, 100, 20));
		transferAmountEdit.setText("0.00");
		transferAmountEdit.addFocusListener(new FundTransferForm_transferAmountEdit_focusAdapter(this));
		currencyChoice.setBounds(new Rectangle(304, 204, 100, 20));
		currencyChoice.addActionListener(new FundTransferForm_currencyChoice_actionAdapter(this));
		accountHolderNameCaptionStaticText.setBounds(new Rectangle(18, 324, 166, 20));
		accountHolderNameCaptionStaticText.setText("Account Holder Name:");
		transferToCaptionStaticText.setBounds(new Rectangle(18, 264, 426, 20));
		transferToCaptionStaticText.setText("Transfer To");
		tradingAccountNoCaptionStaticText.setBounds(new Rectangle(18, 294, 166, 20));
		tradingAccountNoCaptionStaticText.setText("Trading Account No.:");
		tradingAccountNoEdit.setBounds(new Rectangle(195, 294, 249, 20));
		accountHolderNameEdit.setBounds(new Rectangle(195, 324, 249, 20));
		this.add(callMarginExtensionCaptionStaticText);
		this.add(reportDateCaptionStaticText);
		this.add(organizationCaptionStaticText);
		this.add(customerAccountCaptionStaticText);
		this.add(emailCaptionStaticText);
		this.add(effectiveDateCaptionStaticText);
		this.add(transferAmountCaptionStaticText);
		this.add(currencyChoice);
		this.add(reportDateStaticText);
		this.add(organizationStaticText);
		this.add(emailEdit);
		this.add(effectiveDateDate);
		this.add(transferAmountEdit);
		this.add(customerAccountStaticText);
		this.add(transferToCaptionStaticText);
		this.add(tradingAccountNoCaptionStaticText);
		this.add(accountHolderNameCaptionStaticText);
		this.add(separator2CaptionStaticText);
		this.add(tradingAccountNoEdit);
		this.add(accountHolderNameEdit);
		this.add(separator1CaptionStaticText);
		this.add(messageCaptionStaticText);
		this.add(resetButton);
		this.add(submitButton);
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

	public void effectiveDateDate_focusLost(FocusEvent e)
	{
		this.effectiveDateDateValidate();
	}

	public void currencyChoice_actionPerformed(ActionEvent e)
	{
		this.currencyChoice_OnChange();
	}

	public void transferAmountEdit_focusLost(FocusEvent e)
	{
		this.currencyChoice_OnChange();
	}

	class FundTransferForm_this_windowAdapter extends WindowAdapter
	{
		private FundTransferForm adaptee;
		FundTransferForm_this_windowAdapter(FundTransferForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class FundTransferForm_transferAmountEdit_focusAdapter extends FocusAdapter
{
	private FundTransferForm adaptee;
	FundTransferForm_transferAmountEdit_focusAdapter(FundTransferForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.transferAmountEdit_focusLost(e);
	}
}

class FundTransferForm_currencyChoice_actionAdapter implements ActionListener
{
	private FundTransferForm adaptee;
	FundTransferForm_currencyChoice_actionAdapter(FundTransferForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.currencyChoice_actionPerformed(e);
	}
}

class FundTransferForm_effectiveDateDate_focusAdapter extends FocusAdapter
{
	private FundTransferForm adaptee;
	FundTransferForm_effectiveDateDate_focusAdapter(FundTransferForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.effectiveDateDate_focusLost(e);
	}
}

class FundTransferForm_resetButton_actionAdapter implements ActionListener
{
	private FundTransferForm adaptee;
	FundTransferForm_resetButton_actionAdapter(FundTransferForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.resetButton_actionPerformed(e);
	}
}

class FundTransferForm_submitButton_actionAdapter implements ActionListener
{
	private FundTransferForm adaptee;
	FundTransferForm_submitButton_actionAdapter(FundTransferForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}
