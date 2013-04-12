package tradingConsole.ui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Rectangle;
import java.awt.Frame;

import tradingConsole.AppToolkit;
import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Account;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.colorHelper.FormBackColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import tradingConsole.ui.language.CallMarginExtensionLanguage;
import tradingConsole.Customer;
import tradingConsole.settings.Parameter;
import framework.xml.XmlConvert;
import framework.DateTime;
import framework.StringHelper;
import framework.CompositeKey2;
import framework.Guid;
import tradingConsole.AccountCurrency;
import java.util.HashMap;
import java.util.Iterator;
import tradingConsole.Currency;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;
import java.text.DateFormat;
import javax.swing.JComboBox;
import java.util.Date;
import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import java.awt.Color;
import javax.swing.border.Border;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JDialog;
import java.awt.Font;
import java.awt.event.KeyEvent;
import tradingConsole.enumDefine.LogCode;

public class CallMarginExtensionForm extends JDialog
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
	private PVStaticText2 requestExtendDateCaptionStaticText = new PVStaticText2();
	private PVStaticText2 marginCallAmountCaptionStaticText = new PVStaticText2();
	private PVStaticText2 separator2CaptionStaticText = new PVStaticText2();
	private PVButton2 submitButton = new PVButton2();
	private PVButton2 resetButton = new PVButton2();
	private PVStaticText2 messageCaptionStaticText = new PVStaticText2();
	private JTextField emailEdit = new JTextField();
	private PVStaticText2 reportDateStaticText = new PVStaticText2();
	private PVStaticText2 organizationStaticText = new PVStaticText2();
	private PVStaticText2 customerAccountStaticText = new PVStaticText2();
	private JFormattedTextField requestExtendDateDate = new JFormattedTextField(DateFormat.getDateInstance());
	private JTextField marginCallAmountEdit = new JTextField();
	private JAdvancedComboBox currencyChoice = new JAdvancedComboBox();
	private Border border1 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
	private Border border2 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
	public CallMarginExtensionForm(TradingConsole owner, SettingsManager settingsManager, Account account)
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
		this.callMarginExtensionCaptionStaticText.setText(CallMarginExtensionLanguage.callMarginExtensionCaption);
		this.reportDateCaptionStaticText.setText(CallMarginExtensionLanguage.reportDateCaption);
		this.organizationCaptionStaticText.setText(CallMarginExtensionLanguage.organizationCaption);
		this.customerAccountCaptionStaticText.setText(CallMarginExtensionLanguage.customerAccountCaption);
		this.emailCaptionStaticText.setText(CallMarginExtensionLanguage.emailCaption);
		this.separator1CaptionStaticText.setText(CallMarginExtensionLanguage.separatorCaption);
		this.requestExtendDateCaptionStaticText.setText(CallMarginExtensionLanguage.requestExtendDateCaption);
		this.marginCallAmountCaptionStaticText.setText(CallMarginExtensionLanguage.marginCallAmountCaption);
		this.separator2CaptionStaticText.setText(CallMarginExtensionLanguage.separatorCaption);
		this.submitButton.setText(CallMarginExtensionLanguage.submitButtonCaption);
		this.resetButton.setText(CallMarginExtensionLanguage.resetButtonCaption);
	}

	private void setDefaultValue()
	{
		this.messageCaptionStaticText.setText("");

		//this.requestExtendDateDate.setFormat(2);
		this.reportDateStaticText.setText(XmlConvert.toString(this._owner.get_TradingConsoleServer().appTime().addMinutes(Parameter.timeZoneMinuteSpan),
			"yyyy/MM/dd HH:mm:ss"));
		this.organizationStaticText.setText(this._account.get_OrganizationName());
		Customer customer = this._settingsManager.get_Customer();
		this.customerAccountStaticText.setText(AppToolkit.getCustomerAccountName(customer, this._account));
		this.emailEdit.setText(customer.get_Email());

		this.currencyChoice.disableItemEvent();
		this.currencyChoice.removeAllItems();
		HashMap<CompositeKey2<Guid, Guid>, AccountCurrency> accountCurrencies = this._account.get_AccountCurrencies();
		for (Iterator<AccountCurrency> iterator =accountCurrencies.values().iterator(); iterator.hasNext(); )
		{
			AccountCurrency accountCurrency = iterator.next();
			String currencyCode = accountCurrency.get_Currency().get_Code();
			this.currencyChoice.addItem(currencyCode);
		}
		this.currencyChoice.enableItemEvent();
		//this.currencyChoice.sort(true);
		this.currencyChoice.setSelectedIndex(0);
		this.currencyChoice.setEditable(false);

		DateTime requestExtendDate = this._owner.get_TradingConsoleServer().appTime().addMinutes(Parameter.timeZoneMinuteSpan);
		requestExtendDate = requestExtendDate.addDays(Parameter.extendDay);
		this.requestExtendDateDate.setValue(requestExtendDate.get_Date().toDate());
		this.marginCallAmountEdit.setForeground(Color.RED);
		this.marginCallAmountEdit.setText("0.0");

		this.updateSubmitStatus();
	}

	private void reset()
	{
		this.setDefaultValue();
		this.messageCaptionStaticText.setText("");

		this.emailEdit.requestFocus();
		this.updateSubmitStatus();
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
		String currencyValue = this.marginCallAmountEdit.getText();
		String dueDate = XmlConvert.toString((DateTime.fromDate((Date)this.requestExtendDateDate.getValue()).addMinutes(0-Parameter.timeZoneMinuteSpan)), "yyyy/MM/dd");
		if (StringHelper.isNullOrEmpty(email)
			//|| email.indexOf(',', 0) != -1
			|| StringHelper.isNullOrEmpty(organizationName)
			|| StringHelper.isNullOrEmpty(accountCode)
			|| StringHelper.isNullOrEmpty(currency)
			|| StringHelper.isNullOrEmpty(currencyValue)
			|| (!StringHelper.isNullOrEmpty(currencyValue) && AppToolkit.convertStringToDouble(this.marginCallAmountEdit.getText()) <= 0)
			|| StringHelper.isNullOrEmpty(dueDate))
		{
			this.messageCaptionStaticText.setText(Language.CallMarginExtensionPageSubmitAlert0);
			this.emailEdit.requestFocus();
		}
		else
		{
			//Dialog2.showDialog(this, Language.notify2, true, Language.ConfirmSendEmail);
			//if (Dialog2.dialogOption == DialogOption.Yes)
			{
				String result = this._owner.get_TradingConsoleServer().callMarginExtension(email, receive, organizationName, customerName, reportDate,
					accountCode, currency, currencyValue, dueDate);

				if(result.indexOf("Error") != 0)
				{
					String logAction = Language.WebServiceCallMarginExtensionMessage1 + result;
					this._owner.saveLog(LogCode.Other, logAction, Guid.empty, this._account.get_Id());
					AlertDialogForm.showDialog(this, Language.messageContentFormTitle, true, logAction);
					this.dispose();
				}
				else
				{
					this.messageCaptionStaticText.setText(Language.WebServiceCallMarginExtensionMessage0);
				}
			}
		}
	}

	private void requestExtendDateDateValidate()
	{
		try
		{
			//DateTime requestExtendDate = DateTime.valueOf(this.requestExtendDateDate.getText(), "yyyy/MM/dd");
			DateTime requestExtendDate = DateTime.fromDate((Date)this.requestExtendDateDate.getValue());
			DateTime appTime = this._owner.get_TradingConsoleServer().appTime();
			DateTime requestExtendDate2 = appTime.addMinutes(Parameter.timeZoneMinuteSpan);
			requestExtendDate2 = requestExtendDate2.addDays(Parameter.extendDay);
			if (requestExtendDate.before(appTime.get_Date()) || requestExtendDate.after(requestExtendDate2.get_Date()))
			{
				this.requestExtendDateDate.setValue(requestExtendDate2.get_Date().toDate());
				this.requestExtendDateDate.requestFocus();
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
		this.marginCallAmountEdit.setText(AppToolkit.format(AppToolkit.convertStringToDouble(this.marginCallAmountEdit.getText()),decimals));
	}

	private void jbInit() throws Exception
	{
		this.addWindowListener(new CallMarginExtensionForm_this_windowAdapter(this));

		this.setSize(465, 360);
		this.setResizable(false);
		this.setLayout(null);
		this.setTitle(Language.CallMarginExtensionPrompt);
		this.setBackground(FormBackColor.callMarginExtensionForm);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		callMarginExtensionCaptionStaticText.setBounds(new Rectangle(18, 24, 392, 20));
		callMarginExtensionCaptionStaticText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 13));
		callMarginExtensionCaptionStaticText.setText("EXTENSION OF MARGIN CALL");
		callMarginExtensionCaptionStaticText.setAlignment(1);
		reportDateCaptionStaticText.setBounds(new Rectangle(17, 66, 166, 20));
		reportDateCaptionStaticText.setText("Date:");
		organizationCaptionStaticText.setBounds(new Rectangle(18, 92, 166, 20));
		organizationCaptionStaticText.setText("To:");
		customerAccountCaptionStaticText.setBounds(new Rectangle(19, 121, 166, 20));
		customerAccountCaptionStaticText.setText("From:");
		emailCaptionStaticText.setBounds(new Rectangle(19, 151, 166, 20));
		emailCaptionStaticText.setText("Registrated E-mail:");
		this.add(PVStaticText2.createNotNullFlagFor(emailCaptionStaticText));

		separator1CaptionStaticText.setBounds(new Rectangle(18, 181, 424, 2));
		separator1CaptionStaticText.setBorder(border1);
		separator1CaptionStaticText.setPreferredSize(new Dimension(426, 2));
		separator1CaptionStaticText.setToolTipText("");
		requestExtendDateCaptionStaticText.setBounds(new Rectangle(19, 208, 166, 20));
		requestExtendDateCaptionStaticText.setText("Request extend date:");
		this.add(PVStaticText2.createNotNullFlagFor(requestExtendDateCaptionStaticText));

		marginCallAmountCaptionStaticText.setBounds(new Rectangle(18, 241, 166, 20));
		marginCallAmountCaptionStaticText.setText("Margin call amount:");
		this.add(PVStaticText2.createNotNullFlagFor(marginCallAmountCaptionStaticText));

		separator2CaptionStaticText.setBounds(new Rectangle(19, 274, 428, 2));
		separator2CaptionStaticText.setBorder(border2);
		separator2CaptionStaticText.setPreferredSize(new Dimension(428, 2));
		separator2CaptionStaticText.setToolTipText("");
		submitButton.setBounds(new Rectangle(155, 297, 70, 23));
		submitButton.setText("Submit");
		submitButton.addActionListener(new CallMarginExtensionForm_submitButton_actionAdapter(this));
		resetButton.setBounds(new Rectangle(235, 297, 70, 23));
		resetButton.setText("Reset");
		resetButton.addActionListener(new CallMarginExtensionForm_resetButton_actionAdapter(this));
		messageCaptionStaticText.setBounds(new Rectangle(19, 275, 423, 20));
		messageCaptionStaticText.setAlignment(1);
		emailEdit.setBounds(new Rectangle(195, 146, 249, 20));
		//emailEdit.setInputVerifier(new EmailInputVerifier(true));
		reportDateStaticText.setBounds(new Rectangle(195, 66, 249, 20));
		reportDateStaticText.setText("2006/10/20");
		organizationStaticText.setBounds(new Rectangle(195, 95, 249, 20));
		customerAccountStaticText.setBounds(new Rectangle(195, 120, 249, 20));
		requestExtendDateDate.setBounds(new Rectangle(195, 209, 119, 20));
		//requestExtendDateDate.setText("");
		requestExtendDateDate.addFocusListener(new CallMarginExtensionForm_requestExtendDateDate_focusAdapter(this));
		marginCallAmountEdit.setBounds(new Rectangle(195, 241, 119, 20));
		marginCallAmountEdit.setText("0.00");
		marginCallAmountEdit.addFocusListener(new CallMarginExtensionForm_marginCallAmountEdit_focusAdapter(this));
		currencyChoice.setBounds(new Rectangle(324, 241, 119, 20));
		currencyChoice.addItemListener(new CallMarginExtensionForm_currencyChoice_actionAdapter(this));
		this.add(callMarginExtensionCaptionStaticText);
		this.add(reportDateCaptionStaticText);
		this.add(organizationCaptionStaticText);
		this.add(customerAccountCaptionStaticText);
		this.add(emailCaptionStaticText);
		this.add(requestExtendDateCaptionStaticText);
		this.add(marginCallAmountCaptionStaticText);
		this.add(reportDateStaticText);
		this.add(organizationStaticText);
		KeyAdapter keyAdapter = new KeyAdapter(this);
		this.add(emailEdit);
		emailEdit.addKeyListener(keyAdapter);
		this.add(requestExtendDateDate);
		this.add(marginCallAmountEdit);
		marginCallAmountEdit.addKeyListener(keyAdapter);
		this.add(customerAccountStaticText);
		this.add(currencyChoice);
		this.add(resetButton);
		this.add(messageCaptionStaticText);
		this.add(submitButton);
		this.add(separator1CaptionStaticText);
		this.add(separator2CaptionStaticText);
		this.updateSubmitStatus();
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
		String currency = this.currencyChoice.getSelectedItem() == null ?  "" : this.currencyChoice.getSelectedItem().toString();
		String currencyValue = this.marginCallAmountEdit.getText();
		String dueDate = this.requestExtendDateDate.getValue() == null ? "" : XmlConvert.toString( (DateTime.fromDate( (Date)this.requestExtendDateDate.getValue()).addMinutes(0 - Parameter.timeZoneMinuteSpan)),"yyyy/MM/dd");

		this.emailEdit.setForeground(EmailInputVerifier.isValidEmail(true, email) ? Color.BLACK : Color.RED);
		if(!StringHelper.isNullOrEmpty(currencyValue)
		   && AppToolkit.convertStringToDouble(this.marginCallAmountEdit.getText()) <= 0)
		{
			this.marginCallAmountEdit.setForeground(Color.RED);
		}
		else
		{
			this.marginCallAmountEdit.setForeground(Color.BLACK);
		}

		boolean enable = true;
		if (!EmailInputVerifier.isValidEmail(false, email)
			|| StringHelper.isNullOrEmpty(organizationName)
			|| StringHelper.isNullOrEmpty(accountCode)
			|| StringHelper.isNullOrEmpty(currency)
			|| StringHelper.isNullOrEmpty(currencyValue)
			|| (!StringHelper.isNullOrEmpty(currencyValue) && AppToolkit.convertStringToDouble(this.marginCallAmountEdit.getText()) <= 0)
			|| StringHelper.isNullOrEmpty(dueDate))
		{
			enable = false;
		}

		this.submitButton.setEnabled(enable);
	}

	private static class KeyAdapter extends java.awt.event.KeyAdapter
	{
		private CallMarginExtensionForm _owner;

		public KeyAdapter(CallMarginExtensionForm owner)
		{
			this._owner = owner;
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			this._owner.updateSubmitStatus();
		}
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

	public void requestExtendDateDate_focusLost(FocusEvent e)
	{
		this.requestExtendDateDateValidate();
	}

	public void currencyChoice_actionPerformed(ItemEvent e)
	{
		this.currencyChoice_OnChange();
	}

	public void marginCallAmountEdit_focusLost(FocusEvent e)
	{
		this.currencyChoice_OnChange();
	}

	class CallMarginExtensionForm_this_windowAdapter extends WindowAdapter
	{
		private CallMarginExtensionForm adaptee;
		CallMarginExtensionForm_this_windowAdapter(CallMarginExtensionForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class CallMarginExtensionForm_marginCallAmountEdit_focusAdapter extends FocusAdapter
{
	private CallMarginExtensionForm adaptee;
	CallMarginExtensionForm_marginCallAmountEdit_focusAdapter(CallMarginExtensionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.marginCallAmountEdit_focusLost(e);
	}
}

class CallMarginExtensionForm_currencyChoice_actionAdapter implements ItemListener
{
	private CallMarginExtensionForm adaptee;
	CallMarginExtensionForm_currencyChoice_actionAdapter(CallMarginExtensionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.currencyChoice_actionPerformed(e);
	}
}

class CallMarginExtensionForm_requestExtendDateDate_focusAdapter extends FocusAdapter
{
	private CallMarginExtensionForm adaptee;
	CallMarginExtensionForm_requestExtendDateDate_focusAdapter(CallMarginExtensionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.requestExtendDateDate_focusLost(e);
	}
}

class CallMarginExtensionForm_resetButton_actionAdapter implements ActionListener
{
	private CallMarginExtensionForm adaptee;
	CallMarginExtensionForm_resetButton_actionAdapter(CallMarginExtensionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.resetButton_actionPerformed(e);
	}
}

class CallMarginExtensionForm_submitButton_actionAdapter implements ActionListener
{
	private CallMarginExtensionForm adaptee;
	CallMarginExtensionForm_submitButton_actionAdapter(CallMarginExtensionForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}
