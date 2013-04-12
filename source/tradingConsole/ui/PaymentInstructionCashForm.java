package tradingConsole.ui;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.*;

import framework.*;
import framework.xml.*;
import tradingConsole.*;
import tradingConsole.Currency;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.language.*;
import tradingConsole.enumDefine.LogCode;
import com.jidesoft.swing.JideSwingUtilities;

public class PaymentInstructionCashForm extends JDialog implements VerifyMarginPin.IMarginPinVerifyCallback
{
	private Border border1 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
	private Border border2 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));

	private TradingConsole _owner;
	private SettingsManager _settingsManager;
	private Account _account;

	private PVStaticText2 paymentInstructionCaptionStaticText = new PVStaticText2();
	private PVStaticText2 paymentInstructionCaptionStaticText2 = new PVStaticText2();
	private PVStaticText2 reportDateCaptionStaticText = new PVStaticText2();
	private PVStaticText2 organizationCaptionStaticText = new PVStaticText2();
	private PVStaticText2 customerAccountCaptionStaticText = new PVStaticText2();
	private PVStaticText2 clientCaptionStaticText = new PVStaticText2();

	private PVStaticText2 emailCaptionStaticText = new PVStaticText2();
	private PVStaticText2 separator1CaptionStaticText = new PVStaticText2();
	private PVStaticText2 amountCaptionStaticText = new PVStaticText2();
	private PVStaticText2 separator2CaptionStaticText = new PVStaticText2();
	private PVButton2 submitButton = new PVButton2();
	private PVButton2 resetButton = new PVButton2();
	private PVStaticText2 messageCaptionStaticText = new PVStaticText2();
	private JTextField emailEdit = new JTextField();
	private PVStaticText2 reportDateStaticText = new PVStaticText2();
	private PVStaticText2 organizationStaticText = new PVStaticText2();
	private PVStaticText2 customerAccountStaticText = new PVStaticText2();
	private PVStaticText2 clientStaticText = new PVStaticText2();

	private JTextField amountEdit = new JTextField();
	private JComboBox currencyChoice = new JComboBox();
	//private PVStaticText2 clientCaptionStaticText = new PVStaticText2();
	private PVStaticText2 beneficiaryNameCaptionStaticText = new PVStaticText2();
	private PVStaticText2 beneficiaryAddressCaptionStaticText = new PVStaticText2();
	private PVStaticText2 specialInstructionCaptionStaticText = new PVStaticText2();
	private PVStaticText2 remarksCaptionStaticText = new PVStaticText2();
	private MultiTextArea beneficiaryNameTextArea = new MultiTextArea();
	private MultiTextArea beneficiaryAddressTextArea = new MultiTextArea();
	private MultiTextArea remarksTextArea = new MultiTextArea();
	private JTextField specialInstructionEdit = new JTextField();

	public PaymentInstructionCashForm(TradingConsole owner, SettingsManager settingsManager, Account account)
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
		this.beneficiaryNameTextArea.setEditable(true);
		this.beneficiaryAddressTextArea.setEditable(true);

		this.setCaption();
		this.setDefaultValue();
	}

	private void setDefaultValue()
	{
		this.reportDateStaticText.setText(XmlConvert.toString(this._owner.get_TradingConsoleServer().appTime().addMinutes(Parameter.timeZoneMinuteSpan),
			"yyyy/MM/dd HH:mm:ss"));
		this.organizationStaticText.setText(this._account.get_OrganizationName());
		Customer customer = this._settingsManager.get_Customer();
		this.customerAccountStaticText.setText(this._account.get_Code());
		this.clientStaticText.setText(customer.get_CustomerName());
		this.emailEdit.setText(customer.get_Email());

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

		String info = StringHelper2.format(PaymentInstructionLanguage.cashWarrant, new Object[]
										   {this._account.get_OrganizationName()});
		remarksTextArea.setText(info);
	}

	private void setCaption()
	{
		this.paymentInstructionCaptionStaticText.setText(PaymentInstructionLanguage.paymentInstructionCaption);
		this.paymentInstructionCaptionStaticText2.setText("(" + PaymentInstructionLanguage.payableInCash + ")");
		this.reportDateCaptionStaticText.setText(PaymentInstructionLanguage.reportDateCaption);
		this.organizationCaptionStaticText.setText(PaymentInstructionLanguage.organizationCaption);
		this.customerAccountCaptionStaticText.setText(PaymentInstructionLanguage.customerAccountCaption);
		this.emailCaptionStaticText.setText(PaymentInstructionLanguage.emailCaption);
		this.amountCaptionStaticText.setText(PaymentInstructionLanguage.amountCaption);
		this.beneficiaryNameCaptionStaticText.setText(PaymentInstructionLanguage.beneficiaryNameCaption);
		this.beneficiaryAddressCaptionStaticText.setText(PaymentInstructionLanguage.beneficiaryAddressCaption);
		this.specialInstructionCaptionStaticText.setText(PaymentInstructionLanguage.specialInstructionCaption);
		this.remarksCaptionStaticText.setText(PaymentInstructionLanguage.declarationAndWarrantyCaption);
		this.specialInstructionEdit.setText(PaymentInstructionLanguage.cashInstruction);
		this.clientCaptionStaticText.setText(PaymentInstructionLanguage.clientCaption);

		this.submitButton.setText(PaymentInstructionLanguage.submitButtonCaption);
		this.resetButton.setText(PaymentInstructionLanguage.resetButtonCaption);
	}

	private void reset()
	{
		this.setDefaultValue();

		this.messageCaptionStaticText.setText("");

		this.beneficiaryNameTextArea.setText("");
		this.beneficiaryAddressTextArea.setText("");

		this.emailEdit.requestFocus();
		this.updateSubmitStatus();
	}

	private static class KeyAdapter extends java.awt.event.KeyAdapter
	{
		private PaymentInstructionCashForm _owner;

		public KeyAdapter(PaymentInstructionCashForm owner)
		{
			this._owner = owner;
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			this._owner.updateSubmitStatus();
		}
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
		if(!StringHelper.isNullOrEmpty(currency)) currency = this._settingsManager.getCurrency(currency).get_Code();
		String amount = this.amountEdit.getText();
		String beneficiaryName = this.beneficiaryNameTextArea.getText();
		String beneficiaryAddress = this.beneficiaryAddressTextArea.getText();

		this.emailEdit.setForeground(EmailInputVerifier.isValidEmail(true, true, email) ? Color.BLACK : Color.RED);
		if (!StringHelper.isNullOrEmpty(amount)
			&& AppToolkit.convertStringToDouble(amount) <= 0)
		{
			this.amountEdit.setForeground(Color.RED);
		}
		else
		{
			this.amountEdit.setForeground(Color.BLACK);
		}

		boolean enable = true;
		if (!EmailInputVerifier.isValidEmail(false, true, email)
			|| StringHelper.isNullOrEmpty(organizationName)
			|| StringHelper.isNullOrEmpty(accountCode)
			|| StringHelper.isNullOrEmpty(currency)
			|| StringHelper.isNullOrEmpty(amount)
			|| (!StringHelper.isNullOrEmpty(amount) && AppToolkit.convertStringToDouble(this.amountEdit.getText()) <= 0)
			|| StringHelper.isNullOrEmpty(beneficiaryName)
			|| StringHelper.isNullOrEmpty(beneficiaryAddress))
		{
			enable = false;
		}

		this.submitButton.setEnabled(enable);
	}

	private void jbInit()
	{
		this.setSize(465, 670);
		this.setResizable(false);
		this.setLayout(null);
		this.setTitle(Language.PaymentInstructionPrompt);
		this.setBackground(FormBackColor.paymentInstructionForm);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		paymentInstructionCaptionStaticText.setBounds(new Rectangle(15, 25, 392, 20));
		paymentInstructionCaptionStaticText.setFont(new Font("SansSerif", Font.BOLD, 13));
		paymentInstructionCaptionStaticText.setText("PAYMENT INSTRUCTION");
		paymentInstructionCaptionStaticText.setAlignment(1);

		paymentInstructionCaptionStaticText2.setBounds(new Rectangle(15, 45, 392, 20));
		paymentInstructionCaptionStaticText2.setFont(new Font("SansSerif", Font.BOLD, 13));
		paymentInstructionCaptionStaticText2.setText("(Payable In Cash)");
		paymentInstructionCaptionStaticText2.setAlignment(1);

		reportDateCaptionStaticText.setBounds(new Rectangle(16, 85, 166, 20));
		reportDateCaptionStaticText.setText("Date:");
		organizationCaptionStaticText.setBounds(new Rectangle(16, 111, 166, 20));
		organizationCaptionStaticText.setText("To:");
		customerAccountCaptionStaticText.setBounds(new Rectangle(16, 140, 166, 20));
		customerAccountCaptionStaticText.setText("From:");
		clientCaptionStaticText.setBounds(new Rectangle(16, 165, 166, 20));
		clientCaptionStaticText.setText("ClientName:");

		emailCaptionStaticText.setBounds(new Rectangle(16, 190, 166, 20));
		emailCaptionStaticText.setText("Registrated E-mail:");
		this.add(PVStaticText2.createNotNullFlagFor(emailCaptionStaticText));

		separator1CaptionStaticText.setBounds(new Rectangle(17, 224, 428, 2));
		separator1CaptionStaticText.setBorder(border1);
		separator1CaptionStaticText.setPreferredSize(new Dimension(428, 2));
		separator1CaptionStaticText.setToolTipText("");
		amountCaptionStaticText.setBounds(new Rectangle(16, 245, 166, 20));
		amountCaptionStaticText.setText("Amount:");
		this.add(PVStaticText2.createNotNullFlagFor(amountCaptionStaticText));

		separator2CaptionStaticText.setBounds(new Rectangle(16, 585, 428, 2));
		separator2CaptionStaticText.setBorder(border2);
		separator2CaptionStaticText.setPreferredSize(new Dimension(428, 2));
		separator2CaptionStaticText.setToolTipText("");
		submitButton.setBounds(new Rectangle(155, 605, 70, 23));
		submitButton.setText("Submit");
		submitButton.addActionListener(new PaymentInstructionCashForm_submitButton_actionAdapter(this));
		resetButton.setBounds(new Rectangle(235, 605, 70, 23));
		resetButton.setText("Reset");
		resetButton.addActionListener(new PaymentInstructionCashForm_resetButton_actionAdapter(this));
		messageCaptionStaticText.setBounds(new Rectangle(16, 588, 423, 20));
		messageCaptionStaticText.setAlignment(1);
		reportDateStaticText.setBounds(new Rectangle(196, 84, 249, 20));
		reportDateStaticText.setText("2006/10/20");
		organizationStaticText.setBounds(new Rectangle(196, 113, 249, 20));
		customerAccountStaticText.setBounds(new Rectangle(196, 138, 249, 20));
		clientStaticText.setBounds(new Rectangle(196, 164, 249, 20));
		emailEdit.setBounds(new Rectangle(196, 190, 249, 20));
		emailEdit.setInputVerifier(new EmailInputVerifier(true, true));

		amountEdit.setBounds(new Rectangle(196, 244, 100, 20));
		amountEdit.setText("0.00");
		amountEdit.addFocusListener(new PaymentInstructionCashForm_amountEdit_focusAdapter(this));
		currencyChoice.setBounds(new Rectangle(307, 244, 100, 20));
		currencyChoice.addActionListener(new PaymentInstructionCashForm_currencyChoice_actionAdapter(this));
		beneficiaryNameCaptionStaticText.setBounds(new Rectangle(16, 283, 166, 20));
		beneficiaryNameCaptionStaticText.setText("Name of beneficiary:");
		this.add(PVStaticText2.createNotNullFlagFor(beneficiaryNameCaptionStaticText));

		beneficiaryAddressCaptionStaticText.setBounds(new Rectangle(16, 351, 166, 20));
		beneficiaryAddressCaptionStaticText.setText("Address of Beneficiary:");
		this.add(PVStaticText2.createNotNullFlagFor(beneficiaryAddressCaptionStaticText));

		specialInstructionCaptionStaticText.setBounds(new Rectangle(16, 396, 166, 20));
		specialInstructionCaptionStaticText.setText("Special Instruction:");
		remarksCaptionStaticText.setBounds(new Rectangle(16, 483, 170, 20));
		remarksCaptionStaticText.setFont(new Font("SansSerif", Font.BOLD, 11));
		remarksCaptionStaticText.setText("DECLARATION AND WARRANTY:");
		beneficiaryNameTextArea.setBounds(new Rectangle(196, 268, 249, 60));
		beneficiaryAddressTextArea.setBounds(new Rectangle(196, 333, 249, 58));

		remarksTextArea.setBounds(new Rectangle(196, 420, 249, 145));
		remarksTextArea.setEditable(false);

		specialInstructionEdit.setBounds(new Rectangle(196, 396, 249, 20));
		specialInstructionEdit.setFont(new Font("SansSerif", Font.BOLD, 11));
		specialInstructionEdit.setText("Please pay in Cash to the Beneficiary above");
		specialInstructionEdit.setEditable(false);
		this.add(paymentInstructionCaptionStaticText);
		this.add(paymentInstructionCaptionStaticText2);
		this.add(reportDateStaticText);
		this.add(organizationStaticText);
		this.add(clientStaticText);

		KeyAdapter keyAdapter = new KeyAdapter(this);
		this.add(emailEdit);
		emailEdit.addKeyListener(keyAdapter);
		this.add(customerAccountStaticText);
		this.add(clientCaptionStaticText);
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
		this.add(beneficiaryAddressCaptionStaticText);
		this.add(specialInstructionCaptionStaticText);
		this.add(specialInstructionEdit);
		specialInstructionEdit.addKeyListener(keyAdapter);
		this.add(beneficiaryAddressTextArea);
		beneficiaryAddressTextArea.addKeyAdapter(keyAdapter);
		this.add(remarksTextArea);
		remarksTextArea.addKeyAdapter(keyAdapter);
		this.add(remarksCaptionStaticText);
		this.add(beneficiaryNameCaptionStaticText);
		this.add(separator2CaptionStaticText);
		this.add(resetButton);
		this.add(messageCaptionStaticText);
		this.add(submitButton);
		this.add(separator1CaptionStaticText);
		this.updateSubmitStatus();
	}

	public void amountEdit_focusLost(FocusEvent e)
	{
		this.currencyChoice_OnChange();
	}

	public void currencyChoice_actionPerformed(ActionEvent e)
	{
		this.currencyChoice_OnChange();
	}

	public void resetButton_actionPerformed(ActionEvent e)
	{
		this.reset();
	}

	public void submitButton_actionPerformed(ActionEvent e)
	{
		this.submit();
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
		String currency = this.currencyChoice.getSelectedItem() == null ? "" : this.currencyChoice.getSelectedItem().toString();
		if(!StringHelper.isNullOrEmpty(currency)) currency = this._settingsManager.getCurrency(currency).get_RealCode();
		String amount = this.amountEdit.getText();
		String beneficiaryName = this.beneficiaryNameTextArea.getText();
		String beneficiaryAddress = this.beneficiaryAddressTextArea.getText();

		this.emailEdit.setForeground(EmailInputVerifier.isValidEmail(true, true, email) ? Color.BLACK : Color.RED);
		if (!StringHelper.isNullOrEmpty(amount)
			&& AppToolkit.convertStringToDouble(amount) <= 0)
		{
			this.amountEdit.setForeground(Color.RED);
		}
		else
		{
			this.amountEdit.setForeground(Color.BLACK);
		}

		boolean canSubmit = true;
		if (!EmailInputVerifier.isValidEmail(false, true, email)
			|| StringHelper.isNullOrEmpty(organizationName)
			|| StringHelper.isNullOrEmpty(accountCode)
			|| StringHelper.isNullOrEmpty(currency)
			|| StringHelper.isNullOrEmpty(amount)
			|| (!StringHelper.isNullOrEmpty(amount) && AppToolkit.convertStringToDouble(this.amountEdit.getText()) <= 0)
			|| StringHelper.isNullOrEmpty(beneficiaryName)
			|| StringHelper.isNullOrEmpty(beneficiaryAddress))
		{
			canSubmit = false;
		}

		if (canSubmit)
		{
			if (!this._account.hasSufficientUsableMargin(currency, amount))
			{
				this.messageCaptionStaticText.setText(Language.UsableMarginNotSufficient);
				this.amountEdit.setText("");
				this.amountEdit.requestFocus();
				return;
			}

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
		else
		{
			this.messageCaptionStaticText.setText(Language.PaymentInstructionPageSubmitAlert0);
			this.emailEdit.requestFocus();
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
		String currency = this.currencyChoice.getSelectedItem() == null ? "" : this.currencyChoice.getSelectedItem().toString();
		if(!StringHelper.isNullOrEmpty(currency)) currency = this._settingsManager.getCurrency(currency).get_RealCode();
		String amount = this.amountEdit.getText();
		String beneficiaryName = this.beneficiaryNameTextArea.getText();
		String beneficiaryAddress = this.beneficiaryAddressTextArea.getText();

		String result = this._owner.get_TradingConsoleServer().paymentInstructionCash(email, organizationName, customerName, reportDate,
			accountCode, currency, amount, beneficiaryName, beneficiaryAddress);

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
}

class PaymentInstructionCashForm_amountEdit_focusAdapter extends FocusAdapter
{
	private PaymentInstructionCashForm adaptee;
	PaymentInstructionCashForm_amountEdit_focusAdapter(PaymentInstructionCashForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.amountEdit_focusLost(e);
	}
}

class PaymentInstructionCashForm_currencyChoice_actionAdapter implements ActionListener
{
	private PaymentInstructionCashForm adaptee;
	PaymentInstructionCashForm_currencyChoice_actionAdapter(PaymentInstructionCashForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.currencyChoice_actionPerformed(e);
	}
}

class PaymentInstructionCashForm_resetButton_actionAdapter implements ActionListener
{
	private PaymentInstructionCashForm adaptee;
	PaymentInstructionCashForm_resetButton_actionAdapter(PaymentInstructionCashForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.resetButton_actionPerformed(e);
	}
}

class PaymentInstructionCashForm_submitButton_actionAdapter implements ActionListener
{
	private PaymentInstructionCashForm adaptee;
	PaymentInstructionCashForm_submitButton_actionAdapter(PaymentInstructionCashForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}

class StringHelper2
{
	public static final String empty = "";

	public static boolean isNullOrEmpty(String s)
	{
		return s == null || StringHelper.isEmpty(s);
	}

	public static boolean isEmpty(String s)
	{
		return StringHelper.isEmpty(s, false);
	}

	public static boolean isEmpty(String s, boolean trim)
	{
		return trim ? s.trim().length() == 0 : s.length() == 0;
	}

	//Find the first not Quoted position of char in s which position after startIndex
	public static int indexOf(String s, boolean hasQuotedSubString, char value)
	{
		return StringHelper.indexOf(s, hasQuotedSubString, value, 0, s.length());
	}

	public static int indexOf(String s, boolean hasQuotedSubString, char value, int startIndex)
	{
		return StringHelper.indexOf(s, hasQuotedSubString, value, startIndex, s.length() - startIndex);
	}

	public static int indexOf(String s, boolean hasQuotedSubString, char value, int startIndex, int count)
	{
		return StringHelper.indexOfAny(s, hasQuotedSubString, new char[]
									   {value}, startIndex, count);
	}

	public static int indexOfAny(String s, boolean hasQuotedSubString, char[] values)
	{
		return StringHelper.indexOfAny(s, hasQuotedSubString, values, 0, s.length());
	}

	public static int indexOfAny(String s, boolean hasQuotedSubString, char[] values, int startIndex)
	{
		return StringHelper.indexOfAny(s, hasQuotedSubString, values, startIndex, s.length() - startIndex);
	}

	public static int indexOfAny(String s, boolean hasQuotedSubString, char[] values, int startIndex, int count)
	{
		if (hasQuotedSubString)
		{
			char leftQuoteChar = (char)0;
			char preChar = (char)0;
			int lastQuoteCharIndex = -1;

			int offset = startIndex;
			int length = startIndex + count;

			for (; offset < length; offset++)
			{
				char ch = s.charAt(offset);

				if (preChar != '\\' && (ch == '\'' || ch == '\"'))
				{
					if (leftQuoteChar == (char)0)
					{
						leftQuoteChar = ch;
						lastQuoteCharIndex = offset;
					}
					else if (leftQuoteChar == ch)
					{
						leftQuoteChar = (char)0;
					}
				}
				else if (leftQuoteChar == (char)0)
				{
					if (ArrayHelper.indexOf(values, ch) != -1)
					{
						break;
					}
				}

				preChar = ch;
			}

			if (offset >= length)
			{
				if (leftQuoteChar == (char)0)
				{
					offset = -1;
				}
				else
				{
					return StringHelper.indexOfAny(s, hasQuotedSubString, values, lastQuoteCharIndex + 1, s.length() - lastQuoteCharIndex - 1);
				}
			}
			return offset;
		}
		else
		{
			return StringHelper.indexOfAny(s, values, startIndex);
		}
	}

	public static String[] split(String splitedString, boolean hasQuotedSubString, char separator)
	{
		int offset = 0, endIndex;
		Vector results = new Vector();

		while (true)
		{
			endIndex = StringHelper.indexOf(splitedString, hasQuotedSubString, separator, offset);
			if (endIndex == -1)
			{
				results.addElement(splitedString.substring(offset));
				break;
			}
			else
			{
				results.addElement(splitedString.substring(offset, endIndex));
			}
			offset = ++endIndex;
		}

		String results2[] = new String[results.size()];
		results.copyInto(results2);

		return results2;
	}

	//char group
	public static int indexOfAny(String s, char[] anyOf)
	{
		return StringHelper.indexOfAny(s, anyOf, 0);
	}

	public static int indexOfAny(String s, char[] anyOf, int startIndex)
	{
		return StringHelper.indexOfAny(s, anyOf, 0, s.length());
	}

	public static int indexOfAny(String s, char[] anyOf, int startIndex, int count)
	{
		String indexedString2 = s.substring(0, startIndex + count);

		//In order to be consistent with java, here is a low performance implemention
		int minIndex = -1;
		for (int i = 0; i < anyOf.length; i++)
		{
			int index = indexedString2.indexOf(anyOf[i], startIndex);
			if (index < minIndex)
			{
				minIndex = index;
			}
		}

		return minIndex;
	}

	public static String[] split(String splitedString, char separator)
	{
		int offset = 0, endIndex;
		Vector results = new Vector();

		while (true)
		{
			endIndex = splitedString.indexOf(separator, offset);
			if (endIndex == -1)
			{
				results.addElement(splitedString.substring(offset));
				break;
			}
			else
			{
				results.addElement(splitedString.substring(offset, endIndex));
			}
			offset = ++endIndex;
		}

		String results2[] = new String[results.size()];
		results.copyInto(results2);

		return results2;
	}

	public static String[] split(String splitedString, char[] separators)
	{
		int offset = 0, endIndex;
		Vector results = new Vector();

		for (int i = 0; i < splitedString.length(); i++)
		{
			char c = splitedString.charAt(i);

			int index = ArrayHelper.indexOf(separators, c);
			if (index != -1)
			{
				String s = splitedString.substring(offset, i);
				results.addElement(s);
				offset = i + 1;
			}
		}
		if (offset < splitedString.length())
		{
			results.addElement(splitedString.substring(offset, splitedString.length()));
		}

		String results2[] = new String[results.size()];
		results.copyInto(results2);

		return results2;
	}

	//string group
	public static int indexOfAny(String s, String[] anyOf)
	{
		return StringHelper.indexOfAny(s, anyOf, 0);
	}

	public static int indexOfAny(String s, String[] anyOf, int startIndex)
	{
		return StringHelper.indexOfAny(s, anyOf, 0, s.length());
	}

	public static int indexOfAny(String s, String[] anyOf, int startIndex, int count)
	{
		String indexedString2 = s.substring(0, startIndex + count);

		//In order to be consistent with java, here is a low performance implemention
		int minIndex = -1;
		for (int i = 0; i < anyOf.length; i++)
		{
			int index = indexedString2.indexOf(anyOf[i], startIndex);
			if (index < minIndex)
			{
				minIndex = index;
			}
		}

		return minIndex;
	}

	public static String[] split(String splitedString, String separator)
	{
		int offset = 0, endIndex;
		Vector results = new Vector();

		while (true)
		{
			endIndex = splitedString.indexOf(separator, offset);
			if (endIndex == -1)
			{
				endIndex = splitedString.length();
			}

			results.addElement(splitedString.substring(offset, endIndex));

			if (endIndex >= splitedString.length())
			{
				break;
			}

			offset = endIndex + separator.length();
		}

		String results2[] = new String[results.size()];
		results.copyInto(results2);

		return results2;
	}

	public static String replicate(String s, int times)
	{
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < times; i++)
		{
			buffer.append(s);
		}

		return buffer.toString();
	}

	public static String replace(String s, String oldValue, String newValue)
	{
		return StringHelper.replace(s, oldValue, newValue, Integer.MAX_VALUE);
	}

	public static String replace(String s, String oldValue, String newValue, int times)
	{
		if (oldValue == null)
		{
			throw new NullPointerException("oldValue can't be null");
		}
		if (oldValue.length() == 0)
		{
			throw new IllegalArgumentException("oldValue can't be empty");
		}

		StringBuffer buffer = new StringBuffer();

		int preOffset = 0;
		int offset = 0;
		int timeCount = 0;
		do
		{
			offset = s.indexOf(oldValue, offset);
			if (offset == -1 || timeCount == times)
			{
				buffer.append(s.substring(preOffset));
				break;
			}

			int endIndex = offset + oldValue.length();
			buffer.append(s.substring(preOffset, offset));
			buffer.append(StringHelper.isNullOrEmpty(newValue) ? "" : newValue);

			offset = endIndex;
			preOffset = offset;
			timeCount++;
		}
		while (true);

		return buffer.toString();
	}

	public static String replace(String s, int startIndex, int endIndex, String value)
	{
		return s.substring(0, startIndex) + value + s.substring(endIndex);
	}

	public static String toUpper(String s, int startIndex, int endIndex)
	{
		if (startIndex == 0)
		{
			return s.substring(startIndex, endIndex).toUpperCase() + s.substring(endIndex);
		}
		else
		{
			return s.substring(0, startIndex) + s.substring(startIndex, endIndex).toUpperCase() + s.substring(endIndex);
		}
	}

	public static String toLower(String s, int startIndex, int endIndex)
	{
		if (startIndex == 0)
		{
			return s.substring(startIndex, endIndex).toLowerCase() + s.substring(endIndex);
		}
		else
		{
			return s.substring(0, startIndex) + s.substring(startIndex, endIndex).toLowerCase() + s.substring(endIndex);
		}
	}

	public static String insert(String s, int startIndex, String value)
	{
		return s.substring(0, startIndex) + value + s.substring(startIndex);
	}

	public static String remove(String s, int startIndex, int count)
	{
		return s.substring(0, startIndex) + s.substring(startIndex + count);
	}

	public static boolean hasOccured(String s, String pattern, int times)
	{
		int timeIndex = 0;
		int offset = 0;
		while (timeIndex < times)
		{
			offset = s.indexOf(pattern, offset + 1);
			timeIndex++;
		}

		return (timeIndex == times && offset != -1);
	}

	public static String format(String format, Object[] args)
	{
		if (StringHelper.isNullOrEmpty(format))
		{
			throw new FrameworkException("Illegal argument: format cann't be null or empty");
		}
		if (args == null || args.length == 0)
		{
			throw new FrameworkException("Illegal argument: args cann't be null or empty");
		}

		ArrayList<FormatEntry> formatEntries = Formatter.parse(format);

		StringBuffer stringBuffer = new StringBuffer();
		for (int index = 0; index < formatEntries.size(); index++)
		{
			FormatEntry formatEntry = formatEntries.get(index);
			stringBuffer.append(formatEntry.format(args[formatEntry._sequenceNumber]));
		}
		return stringBuffer.toString();
	}

	static class FormatEntry implements Comparable<FormatEntry>
	{
		private String _prefix;
		private String _postfix;
		private int _sequenceNumber;

		FormatEntry(String prefix, String postfix, int sequenceNumber)
		{
			this._prefix = prefix;
			this._postfix = postfix;
			this._sequenceNumber = sequenceNumber;
		}

		public int compareTo(FormatEntry o)
		{
			return this._sequenceNumber - o._sequenceNumber;
		}

		String format(Object arg)
		{
			return this._prefix + (arg == null ? "null" : arg.toString()) + this._postfix;
		}
	}

	static class Formatter
	{
		private static final char _openingBrace = '{';
		private static final char _closingBrace = '}';
		private static final FormatEntry[] _formatEntryArrayTemplate = new FormatEntry[]
			{};

		private static final int _fillPrefix = 0;
		private static final int _fillSequenceNumber = 1;
		private static final int _fillPostfix = 2;
		private static final int _done = 3;

		private Formatter()
		{
		}

		static ArrayList<FormatEntry> parse(String rawFormat)
		{
			ArrayList<FormatEntry> formatEntries = new ArrayList<FormatEntry> ();
			int startIndex = 0;
			while (startIndex < rawFormat.length())
			{
				startIndex = Formatter.getFormatEntry(formatEntries, rawFormat, startIndex);
			}
			Collections.sort(formatEntries);
			return formatEntries;
		}

		private static int getFormatEntry(ArrayList<FormatEntry> formatEntries, String rawFormat, int startIndex)
		{
			String prefix = "";
			String postfix = "";
			String sequenceNumber = "";
			int step = Formatter._fillPrefix;

			int index = startIndex;
			for (; index < rawFormat.length(); index++)
			{
				char charater = rawFormat.charAt(index);

				if (step == Formatter._fillPrefix)
				{
					if (charater != Formatter._openingBrace)
					{
						prefix = prefix + charater;
					}
					else
					{
						if (index < rawFormat.length() - 1 && rawFormat.charAt(index + 1) == Formatter._openingBrace)
						{
							prefix = prefix + charater;
							index++;
						}
						else
						{
							step = Formatter._fillSequenceNumber;
						}
					}
				}
				else if (step == Formatter._fillSequenceNumber)
				{
					if (Character.isDigit(charater))
					{
						sequenceNumber = sequenceNumber + charater;
					}
					else
					{
						if (sequenceNumber.length() == 0)
						{
							throw new FrameworkException("Illegal format: " + index + " at "
								+ rawFormat + " must be digit charaterarater");
						}
						else
						{
							if (charater == Formatter._closingBrace)
							{
								step = Formatter._fillPostfix;
							}
							else
							{
								throw new FrameworkException("Illegal format: " + index + " at "
									+ rawFormat + " must be " + Formatter._closingBrace);
							}
						}
					}
				}
				else
				{
					if (charater == Formatter._openingBrace)
					{
						if (index < rawFormat.length() - 1 && rawFormat.charAt(index + 1) == Formatter._openingBrace)
						{
							postfix = postfix + charater;
							index++;
						}
						else
						{
							step = Formatter._done;
							break;
						}
					}
					else
					{
						if (charater == Formatter._closingBrace)
						{
							if (index < rawFormat.length() - 1 && rawFormat.charAt(index + 1) == Formatter._closingBrace)
							{
								index++;
							}
							else
							{
								throw new FrameworkException("Illegal format: " + index + " at "
									+ rawFormat + " must be " + Formatter._closingBrace);
							}
						}
						postfix = postfix + charater;
					}
				}
			}

			if (step >= Formatter._fillPostfix)
			{
				FormatEntry formatEntry = new FormatEntry(prefix, postfix, Integer.parseInt(sequenceNumber));
				formatEntries.add(formatEntry);
				return index;
			}
			else
			{
				throw new FrameworkException("Illegal format: " + rawFormat);
			}
		}
	}
}
