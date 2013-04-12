package tradingConsole.ui;

import java.awt.*;
import javax.swing.*;

import tradingConsole.*;
import tradingConsole.settings.*;
import javax.swing.border.Border;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.colorHelper.FormBackColor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.ActionListener;
import framework.CompositeKey2;
import framework.StringHelper;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import framework.Guid;
import java.util.Iterator;
import framework.xml.XmlConvert;
import tradingConsole.ui.language.PaymentInstructionLanguage;
import tradingConsole.enumDefine.LogCode;
import java.awt.event.WindowEvent;
import com.jidesoft.swing.JideSwingUtilities;
import java.awt.event.WindowAdapter;

public class PaymentInstructionInternalForm extends JDialog implements VerifyMarginPin.IMarginPinVerifyCallback
{
	private Border border1 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
	private Border border2 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
	private String warrantyFormat = "  The owner of the source account No. [{0}] hereby declares that the acount requested"
		+ " to be transferred is the available acmount in his trading account and hereby absolves RONGLI.LLP form any"
		+ " liabilities whatsoever on the amount transferred uapon execution of the Payment instruecion, hereof."
		+ "\r\n  The owner of the recipient account No. [{1}] hereby confirms receipt and benefits of the transferred"
		+ " amount upon execution of the herein Payment instruction in his favor.";

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
	private PVStaticText2 emailCaptionStaticText2 = new PVStaticText2();
	private PVStaticText2 amountCaptionStaticText = new PVStaticText2();
	private PVStaticText2 separator2CaptionStaticText = new PVStaticText2();
	private PVButton2 submitButton = new PVButton2();
	private PVButton2 resetButton = new PVButton2();
	private PVStaticText2 messageCaptionStaticText = new PVStaticText2();
	private JTextField emailEdit = new JTextField();
	private JTextField emailEdit2 = new JTextField();
	private PVStaticText2 reportDateStaticText = new PVStaticText2();
	private PVStaticText2 organizationStaticText = new PVStaticText2();
	private PVStaticText2 customerAccountStaticText = new PVStaticText2();
	private PVStaticText2 clientStaticText = new PVStaticText2();

	private JTextField amountEdit = new JTextField();
	private JComboBox currencyChoice = new JComboBox();
	//private PVStaticText2 currencyStaticText = new PVStaticText2();
	//private PVStaticText2 clientCaptionStaticText = new PVStaticText2();
	private PVStaticText2 beneficiaryAccountCaptionStaticText = new PVStaticText2();
	private PVStaticText2 beneficiaryAccountOwnerCaptionStaticText = new PVStaticText2();
	private PVStaticText2 specialInstructionCaptionStaticText = new PVStaticText2();
	private PVStaticText2 remarksCaptionStaticText = new PVStaticText2();
	private MultiTextArea beneficiaryAccountTextArea = new MultiTextArea();
	private MultiTextArea beneficiaryAccountOwnerTextArea = new MultiTextArea();
	private MultiTextArea remarksTextArea = new MultiTextArea();
	private MultiTextArea specialInstructionEdit = new MultiTextArea();

	public PaymentInstructionInternalForm(TradingConsole owner, SettingsManager settingsManager, Account account)
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
		this.beneficiaryAccountTextArea.setEditable(true);
		this.beneficiaryAccountOwnerTextArea.setEditable(true);

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
		this.emailEdit.setText("");

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

	private void setCaption()
	{
		this.paymentInstructionCaptionStaticText.setText(PaymentInstructionLanguage.paymentInstructionCaption);
		this.reportDateCaptionStaticText.setText(PaymentInstructionLanguage.reportDateCaption);
		this.organizationCaptionStaticText.setText(PaymentInstructionLanguage.organizationCaption);
		this.customerAccountCaptionStaticText.setText(PaymentInstructionLanguage.customerAccountCaption);
		this.emailCaptionStaticText.setText(PaymentInstructionLanguage.emailCaption);
		this.emailCaptionStaticText2.setText(PaymentInstructionLanguage.emailCaption);
		this.amountCaptionStaticText.setText(PaymentInstructionLanguage.amountToBeTransferredCaption);
		this.beneficiaryAccountCaptionStaticText.setText(PaymentInstructionLanguage.beneficiaryAccountCaption);
		this.beneficiaryAccountOwnerCaptionStaticText.setText(PaymentInstructionLanguage.beneficiaryAccountOwnerCaption);
		this.specialInstructionCaptionStaticText.setText(PaymentInstructionLanguage.specialInstructionCaption);
		this.remarksCaptionStaticText.setText(PaymentInstructionLanguage.declarationAndWarrantyCaption);
		this.paymentInstructionCaptionStaticText2.setText("(" + PaymentInstructionLanguage.internalTransfer + ")");
		this.specialInstructionEdit.setText(PaymentInstructionLanguage.internalTransferInstruction);
		this.clientCaptionStaticText.setText(PaymentInstructionLanguage.clientCaption);

		this.submitButton.setText(PaymentInstructionLanguage.submitButtonCaption);
		this.resetButton.setText(PaymentInstructionLanguage.resetButtonCaption);
	}

	private void reset()
	{
		this.setDefaultValue();

		this.messageCaptionStaticText.setText("");

		this.beneficiaryAccountTextArea.setText("");
		this.beneficiaryAccountOwnerTextArea.setText("");

		this.emailEdit.requestFocus();
		this.updateSubmitStatus();
	}

	private static class KeyAdapter extends java.awt.event.KeyAdapter
	{
		private PaymentInstructionInternalForm _owner;

		public KeyAdapter(PaymentInstructionInternalForm owner)
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
		String email2 = this.emailEdit2.getText();
		String receive = Parameter.receiveAddress;
		String organizationName = this._account.get_OrganizationName();
		String customerName = customer.get_CustomerName();
		String reportDate = this.reportDateStaticText.getText();
		String accountCode = this._account.get_Code();
		String currencyValue = this.amountEdit.getText();
		String beneficiaryAccount = this.beneficiaryAccountTextArea.getText();
		String beneficiaryAccountOwner = this.beneficiaryAccountOwnerTextArea.getText();

		this.emailEdit.setForeground(EmailInputVerifier.isValidEmail(true, true, email) ? Color.BLACK : Color.RED);
		this.emailEdit2.setForeground(EmailInputVerifier.isValidEmail(true, true, email2) ? Color.BLACK : Color.RED);
		if (!StringHelper.isNullOrEmpty(currencyValue)
			&& AppToolkit.convertStringToDouble(currencyValue) <= 0)
		{
			this.amountEdit.setForeground(Color.RED);
		}
		else
		{
			this.amountEdit.setForeground(Color.BLACK);
		}

		boolean enable = true;
		if (!EmailInputVerifier.isValidEmail(false, true, email)
			|| !EmailInputVerifier.isValidEmail(false, true, email2)
			|| StringHelper.isNullOrEmpty(organizationName)
			|| StringHelper.isNullOrEmpty(accountCode)
			|| StringHelper.isNullOrEmpty(currencyValue)
			|| (!StringHelper.isNullOrEmpty(currencyValue) && AppToolkit.convertStringToDouble(this.amountEdit.getText()) <= 0)
			|| StringHelper.isNullOrEmpty(beneficiaryAccount)
			|| StringHelper.isNullOrEmpty(beneficiaryAccountOwner))
		{
			enable = false;
		}

		this.setTextOfRemarks();
		this.submitButton.setEnabled(enable);
	}

	private void setTextOfRemarks()
	{
		String sourceAccount = this._account.get_Code();
		if (StringHelper.isNullOrEmpty(sourceAccount))
		{
			sourceAccount = "---";
		}
		String recipientAccount = beneficiaryAccountTextArea.getText();
		if (StringHelper.isNullOrEmpty(recipientAccount))
		{
			recipientAccount = "---";
		}
		String formatString = PaymentInstructionLanguage.internalTransferWarrant1
			+ "\r\n"
			+ PaymentInstructionLanguage.internalTransferWarrant2;
		String info = StringHelper.format(formatString, new Object[]
										  {sourceAccount, this._account.get_OrganizationName(), recipientAccount});
		remarksTextArea.setText(info);
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
		paymentInstructionCaptionStaticText2.setText("(Internal Transfer)");
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
		emailEdit.setBounds(new Rectangle(196, 190, 249, 20));
		emailEdit.setInputVerifier(new EmailInputVerifier(true, true));

		specialInstructionCaptionStaticText.setBounds(new Rectangle(16, 225, 166, 20));
		specialInstructionCaptionStaticText.setText("Special Instruction:");
		specialInstructionCaptionStaticText.setFont(new Font("SansSerif", Font.BOLD, 11));
		specialInstructionEdit.setBounds(new Rectangle(196, 215, 249, 50));
		specialInstructionEdit.setFont(new Font("SansSerif", Font.BOLD, 11));
		specialInstructionEdit.setText("Please Transfer Funds From the Above Source Account to the Recipient Account Stated Below");
		specialInstructionEdit.setEditable(false);

		amountCaptionStaticText.setBounds(new Rectangle(16, 270, 166, 20));
		amountCaptionStaticText.setText("Amount to be Transferred:");
		this.add(PVStaticText2.createNotNullFlagFor(amountCaptionStaticText));
		amountEdit.setBounds(new Rectangle(196, 270, 100, 20));
		amountEdit.setText("0.00");
		amountEdit.addFocusListener(new PaymentInstructionInternalForm_amountEdit_focusAdapter(this));
		currencyChoice.setBounds(new Rectangle(307, 270, 100, 20));
		currencyChoice.addActionListener(new PaymentInstructionInternalForm_currencyChoice_actionAdapter(this));

		reportDateStaticText.setBounds(new Rectangle(196, 84, 249, 20));
		reportDateStaticText.setText("2006/10/20");
		organizationStaticText.setBounds(new Rectangle(196, 113, 249, 20));
		customerAccountStaticText.setBounds(new Rectangle(196, 138, 249, 20));
		clientStaticText.setBounds(new Rectangle(196, 164, 249, 20));

		beneficiaryAccountCaptionStaticText.setBounds(new Rectangle(16, 295, 166, 20));
		beneficiaryAccountCaptionStaticText.setText("Beneficiary Account No.:");
		this.add(PVStaticText2.createNotNullFlagFor(beneficiaryAccountCaptionStaticText));
		beneficiaryAccountTextArea.setBounds(new Rectangle(196, 295, 249, 20));

		beneficiaryAccountOwnerCaptionStaticText.setBounds(new Rectangle(16, 320, 166, 20));
		beneficiaryAccountOwnerCaptionStaticText.setText("Owner of Recipient");
		this.add(PVStaticText2.createNotNullFlagFor(beneficiaryAccountOwnerCaptionStaticText));
		beneficiaryAccountOwnerTextArea.setBounds(new Rectangle(196, 320, 249, 20));

		emailCaptionStaticText2.setBounds(new Rectangle(16, 345, 166, 20));
		emailCaptionStaticText2.setText("Registrated E-mail:");
		this.add(PVStaticText2.createNotNullFlagFor(emailCaptionStaticText2));
		emailEdit2.setBounds(new Rectangle(196, 345, 249, 20));
		emailEdit2.setInputVerifier(new EmailInputVerifier(true, true));

		remarksCaptionStaticText.setBounds(new Rectangle(16, 472, 170, 20));
		remarksCaptionStaticText.setFont(new Font("SansSerif", Font.BOLD, 11));
		remarksCaptionStaticText.setText("DECLARATION AND WARRANTY:");
		remarksTextArea.setBounds(new Rectangle(196, 370, 249, 205));
		remarksTextArea.setEditable(false);

		separator2CaptionStaticText.setBounds(new Rectangle(16, 585, 428, 2));
		separator2CaptionStaticText.setBorder(border2);
		separator2CaptionStaticText.setPreferredSize(new Dimension(428, 2));
		separator2CaptionStaticText.setToolTipText("");
		submitButton.setBounds(new Rectangle(155, 605, 70, 23));
		submitButton.setText("Submit");
		submitButton.addActionListener(new PaymentInstructionInternalForm_submitButton_actionAdapter(this));
		resetButton.setBounds(new Rectangle(235, 605, 70, 23));
		resetButton.setText("Reset");
		resetButton.addActionListener(new PaymentInstructionInternalForm_resetButton_actionAdapter(this));
		messageCaptionStaticText.setBounds(new Rectangle(16, 588, 423, 20));
		messageCaptionStaticText.setAlignment(1);

		this.add(paymentInstructionCaptionStaticText);
		this.add(paymentInstructionCaptionStaticText2);
		this.add(reportDateStaticText);
		this.add(organizationStaticText);
		this.add(clientStaticText);

		KeyAdapter keyAdapter = new KeyAdapter(this);
		this.add(emailEdit);
		emailEdit.addKeyListener(keyAdapter);
		this.add(emailEdit2);
		emailEdit2.addKeyListener(keyAdapter);
		this.add(customerAccountStaticText);
		this.add(clientCaptionStaticText);
		this.add(currencyChoice);
		this.add(amountEdit);
		amountEdit.addKeyListener(keyAdapter);
		this.add(beneficiaryAccountTextArea);
		beneficiaryAccountTextArea.addKeyAdapter(keyAdapter);
		this.add(reportDateCaptionStaticText);
		this.add(organizationCaptionStaticText);
		this.add(customerAccountCaptionStaticText);
		this.add(emailCaptionStaticText);
		this.add(emailCaptionStaticText2);
		this.add(amountCaptionStaticText);
		//this.add(clientCaptionStaticText);
		this.add(beneficiaryAccountOwnerCaptionStaticText);
		this.add(specialInstructionCaptionStaticText);
		this.add(specialInstructionEdit);
		specialInstructionEdit.addKeyListener(keyAdapter);
		this.add(beneficiaryAccountOwnerTextArea);
		beneficiaryAccountOwnerTextArea.addKeyAdapter(keyAdapter);
		this.add(remarksTextArea);
		remarksTextArea.addKeyAdapter(keyAdapter);
		this.add(remarksCaptionStaticText);
		this.add(beneficiaryAccountCaptionStaticText);
		this.add(separator2CaptionStaticText);
		this.add(resetButton);
		this.add(messageCaptionStaticText);
		this.add(submitButton);
		this.updateSubmitStatus();
	}

	public void amountEdit_focusLost(FocusEvent e)
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
		String email2 = this.emailEdit2.getText();
		String organizationName = this._account.get_OrganizationName();
		String customerName = customer.get_CustomerName();
		String reportDate = this.reportDateStaticText.getText();
		String accountCode = this._account.get_Code();
		String amount = this.amountEdit.getText();
		String beneficiaryAccount = this.beneficiaryAccountTextArea.getText();
		String beneficiaryAccountOwner = this.beneficiaryAccountOwnerTextArea.getText();
		String currency = this.currencyChoice.getSelectedItem() == null ? "" : this.currencyChoice.getSelectedItem().toString();
		if(!StringHelper.isNullOrEmpty(currency)) currency = this._settingsManager.getCurrency(currency).get_RealCode();

		boolean canSubmit = true;
		if (!EmailInputVerifier.isValidEmail(false, true, email)
			|| !EmailInputVerifier.isValidEmail(false, true, email2)
			|| StringHelper.isNullOrEmpty(organizationName)
			|| StringHelper.isNullOrEmpty(accountCode)
			|| StringHelper.isNullOrEmpty(currency)
			|| StringHelper.isNullOrEmpty(amount)
			|| (!StringHelper.isNullOrEmpty(amount) && AppToolkit.convertStringToDouble(this.amountEdit.getText()) <= 0)
			|| StringHelper.isNullOrEmpty(beneficiaryAccount)
			|| StringHelper.isNullOrEmpty(beneficiaryAccountOwner))
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
		String email2 = this.emailEdit2.getText();
		String organizationName = this._account.get_OrganizationName();
		String customerName = customer.get_CustomerName();
		String reportDate = this.reportDateStaticText.getText();
		String accountCode = this._account.get_Code();
		String amount = this.amountEdit.getText();
		String beneficiaryAccount = this.beneficiaryAccountTextArea.getText();
		String beneficiaryAccountOwner = this.beneficiaryAccountOwnerTextArea.getText();
		String currency = this.currencyChoice.getSelectedItem() == null ? "" : this.currencyChoice.getSelectedItem().toString();
		if(!StringHelper.isNullOrEmpty(currency)) currency = this._settingsManager.getCurrency(currency).get_RealCode();

		String result = this._owner.get_TradingConsoleServer().paymentInstructionInternal(email, organizationName, customerName, reportDate,
			accountCode, currency, amount, beneficiaryAccount, beneficiaryAccountOwner, email2);

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

	public void currencyChoice_actionPerformed(ActionEvent e)
	{
		this.currencyChoice_OnChange();
	}
}

class PaymentInstructionInternalForm_amountEdit_focusAdapter extends FocusAdapter
{
	private PaymentInstructionInternalForm adaptee;
	PaymentInstructionInternalForm_amountEdit_focusAdapter(PaymentInstructionInternalForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.amountEdit_focusLost(e);
	}
}

class PaymentInstructionInternalForm_resetButton_actionAdapter implements ActionListener
{
	private PaymentInstructionInternalForm adaptee;
	PaymentInstructionInternalForm_resetButton_actionAdapter(PaymentInstructionInternalForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.resetButton_actionPerformed(e);
	}
}

class PaymentInstructionInternalForm_currencyChoice_actionAdapter implements ActionListener
{
	private PaymentInstructionInternalForm adaptee;
	PaymentInstructionInternalForm_currencyChoice_actionAdapter(PaymentInstructionInternalForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.currencyChoice_actionPerformed(e);
	}
}

class PaymentInstructionInternalForm_submitButton_actionAdapter implements ActionListener
{
	private PaymentInstructionInternalForm adaptee;
	PaymentInstructionInternalForm_submitButton_actionAdapter(PaymentInstructionInternalForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}
