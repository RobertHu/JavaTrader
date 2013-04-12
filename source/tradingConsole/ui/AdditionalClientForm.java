package tradingConsole.ui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.Font;
import javax.swing.JFrame;

import framework.xml.XmlConvert;
import framework.StringHelper;
import framework.DateTime;

import tradingConsole.AppToolkit;
import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Account;
import tradingConsole.ui.language.AdditionalClientLanguage;
import tradingConsole.ui.language.Language;
import tradingConsole.Customer;
import tradingConsole.settings.Parameter;
import tradingConsole.ui.colorHelper.FormBackColor;
import javax.swing.JTextField;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import java.awt.Color;
import javax.swing.border.Border;
import javax.swing.JDialog;
import java.awt.event.KeyEvent;
import tradingConsole.Log;
import tradingConsole.enumDefine.LogCode;
import framework.Guid;

public class AdditionalClientForm extends JDialog
{
	private static DateTime _reportDate;
	private TradingConsole _owner;
	private SettingsManager _settingsManager;
	private Account _account;

	public AdditionalClientForm(TradingConsole owner, SettingsManager settingsManager, Account account)
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
		this.additionalClientCaptionStaticText.setText(AdditionalClientLanguage.additionalClientCaption);
		this.reportDateCaptionStaticText.setText(AdditionalClientLanguage.reportDateCaption);
		this.organizationCaptionStaticText.setText(AdditionalClientLanguage.organizationCaption);
		this.customerAccountCaptionStaticText.setText(AdditionalClientLanguage.customerAccountCaption);
		this.emailCaptionStaticText.setText(AdditionalClientLanguage.emailCaption);
		this.separator1CaptionStaticText.setText(AdditionalClientLanguage.separatorCaption);
		this.originalOwnerCaptionStaticText.setText(AdditionalClientLanguage.originalOwnerCaption);
		this.correspondingAddressCaptionStaticText.setText(AdditionalClientLanguage.correspondingAddressCaption);
		this.newEmailAddressCaptionStaticText.setText(AdditionalClientLanguage.newEmailAddressCaption);
		this.telephoneCaptionStaticText.setText(AdditionalClientLanguage.telephoneCaption);
		this.mobileCaptionStaticText.setText(AdditionalClientLanguage.mobileCaption);
		this.faxCaptionStaticText.setText(AdditionalClientLanguage.faxCaption);
		this.ownerInfo1CaptionStaticText.setText(AdditionalClientLanguage.ownerInfo1Caption);
		this.ic1CaptionStaticText.setText(AdditionalClientLanguage.icCaption);
		this.fullName1CaptionStaticText.setText(AdditionalClientLanguage.fullNameCaption);
		this.ownerInfo2CaptionStaticText.setText(AdditionalClientLanguage.ownerInfo2Caption);
		this.ic3CaptionStaticText.setText(AdditionalClientLanguage.icCaption);
		this.fullName3CaptionStaticText.setText(AdditionalClientLanguage.fullNameCaption);
		this.ownerInfo3CaptionStaticText.setText(AdditionalClientLanguage.ownerInfo3Caption);
		this.fullName2CaptionStaticText.setText(AdditionalClientLanguage.fullNameCaption);
		this.ic2CaptionStaticText.setText(AdditionalClientLanguage.icCaption);
		this.submitButton.setText(AdditionalClientLanguage.submitButtonCaption);
		this.resetButton.setText(AdditionalClientLanguage.resetButtonCaption);
	}

	private void setDefaultValue()
	{
		AdditionalClientForm._reportDate = this._owner.get_TradingConsoleServer().appTime().get_Date();
		this.reportDateStaticText.setText(XmlConvert.toString(this._owner.get_TradingConsoleServer().appTime().addMinutes(Parameter.timeZoneMinuteSpan), "yyyy/MM/dd HH:mm:ss"));
		this.organizationStaticText.setText(this._account.get_OrganizationName());
		Customer customer = this._settingsManager.get_Customer();
		this.customerAccountStaticText.setText(AppToolkit.getCustomerAccountName(customer, this._account));
		this.emailEdit.setText(customer.get_Email());

		this.updateSubmitStatus();
	}

	private void submit()
	{
		this.messageCaptionStaticText.setText("");

		Customer customer = this._settingsManager.get_Customer();
		String email = this.emailEdit.getText();
		String receive = Parameter.receiveAddress;
		String organizationName = this._account.get_OrganizationName();
		String customerName = customer.get_CustomerName();
		String reportDate = AdditionalClientForm._reportDate.toString("yyyy/MM/dd");
		String accountCode = this._account.get_Code();
		String correspondingAddress = this.correspondingAddressEdit.getText();
		String registratedEmailAddress = this.newEmailAddressEdit.getText();
		String tel = this.telephoneEdit.getText();
		String mobile = this.mobileEdit.getText();
		String fax = this.faxEdit.getText();
		String fillName1 = this.fullName1Edit.getText();
		String ICNo1 = this.ic1Edit.getText();
		String fillName2 = this.fullName2Edit.getText();
		String ICNo2 = this.ic2Edit.getText();
		String fillName3 = this.fullName3Edit.getText();
		String ICNo3 = this.ic3Edit.getText();
		if (StringHelper.isNullOrEmpty(email)
			//|| email.indexOf(',', 0) != -1
			|| StringHelper.isNullOrEmpty(organizationName)
			|| StringHelper.isNullOrEmpty(accountCode)
			|| (
				StringHelper.isNullOrEmpty(correspondingAddress)
				&& StringHelper.isNullOrEmpty(registratedEmailAddress)
				&& StringHelper.isNullOrEmpty(tel)
				&& StringHelper.isNullOrEmpty(mobile)
				&& StringHelper.isNullOrEmpty(fax)
				&& StringHelper.isNullOrEmpty(fillName1)
				&& StringHelper.isNullOrEmpty(ICNo1)
				&& StringHelper.isNullOrEmpty(fillName2)
				&& StringHelper.isNullOrEmpty(ICNo2)
				&& StringHelper.isNullOrEmpty(fillName3)
				&& StringHelper.isNullOrEmpty(ICNo3)))
		{
			this.messageCaptionStaticText.setText(Language.AdditionalClientPageSubmitAlert0);
			this.emailEdit.requestFocus();
		}
		else
		{
			//Dialog2.showDialog(this, Language.notify2, true, Language.ConfirmSendEmail);
			//if (Dialog2.dialogOption == DialogOption.Yes)
			{
				String result = this._owner.get_TradingConsoleServer().additionalClient(email, receive, organizationName, customerName, reportDate,
					accountCode,
					correspondingAddress, registratedEmailAddress, tel, mobile, fax,
					fillName1, ICNo1, fillName2, ICNo2, fillName3, ICNo3);
				if(result.indexOf("Error") != 0)
				{
					String logAction = Language.WebServiceAdditionalClientMessage1 + result;
					this._owner.saveLog(LogCode.Other, logAction, Guid.empty, this._account.get_Id());
					AlertDialogForm.showDialog(this, Language.messageContentFormTitle, true, logAction);
					this.dispose();
				}
				else
				{
					this.messageCaptionStaticText.setText(Language.WebServiceAdditionalClientMessage0);
				}
			}
		}
		//this.emailEdit.requestFocus();
	}

	private void reset()
	{
		this.setDefaultValue();
		this.messageCaptionStaticText.setText("");

		this.correspondingAddressEdit.setText("");
		this.newEmailAddressEdit.setText("");
		this.telephoneEdit.setText("");
		this.mobileEdit.setText("");
		this.faxEdit.setText("");
		this.fullName1Edit.setText("");
		this.ic1Edit.setText("");
		this.fullName2Edit.setText("");
		this.ic2Edit.setText("");
		this.fullName3Edit.setText("");
		this.ic3Edit.setText("");

		this.emailEdit.requestFocus();
		this.updateSubmitStatus();
	}

	private void jbInit() throws Exception
	{
		this.addWindowListener(new AdditionalClientForm_this_windowAdapter(this));

		this.setSize(465, 710);
		this.setResizable(true);
		this.setLayout(null);
		this.setTitle(Language.AdditionalClientPrompt);
		this.setBackground(FormBackColor.additionalClientForm);

		additionalClientCaptionStaticText.setBounds(new Rectangle(12, 21, 428, 27));
		additionalClientCaptionStaticText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 13));
		additionalClientCaptionStaticText.setText("CHANGE OF OWNER REGISTRATION");
		additionalClientCaptionStaticText.setAlignment(1);
		reportDateCaptionStaticText.setBounds(new Rectangle(12, 69, 163, 20));
		reportDateCaptionStaticText.setText("Date:");
		organizationCaptionStaticText.setBounds(new Rectangle(12, 96, 163, 20));
		organizationCaptionStaticText.setText("To:");
		customerAccountCaptionStaticText.setBounds(new Rectangle(12, 124, 163, 20));
		customerAccountCaptionStaticText.setText("From:");

		emailCaptionStaticText.setBounds(new Rectangle(12, 151, 163, 20));
		emailCaptionStaticText.setText("Registrated E-mail:");
		this.add(PVStaticText2.createNotNullFlagFor(emailCaptionStaticText));
		separator1CaptionStaticText.setBounds(new Rectangle(12, 624, 428, 2));
		separator1CaptionStaticText.setBorder(border1);
		separator1CaptionStaticText.setPreferredSize(new Dimension(428, 2));
		separator1CaptionStaticText.setToolTipText("");
		reportDateStaticText.setBounds(new Rectangle(182, 69, 258, 20));
		reportDateStaticText.setText("2006/10/20");
		organizationStaticText.setBounds(new Rectangle(182, 96, 258, 20));
		organizationStaticText.setText("OR7");
		customerAccountStaticText.setBounds(new Rectangle(182, 124, 258, 20));
		customerAccountStaticText.setText("c12,21x");
		emailEdit.setBounds(new Rectangle(182, 151, 258, 20));
		//EmailInputVerifier inputVerifier = new EmailInputVerifier(true);
		//emailEdit.setInputVerifier(inputVerifier);

		originalOwnerCaptionStaticText.setBounds(new Rectangle(12, 206, 428, 20));
		originalOwnerCaptionStaticText.setText("Original owner information");
		correspondingAddressCaptionStaticText.setBounds(new Rectangle(12, 234, 163, 20));
		correspondingAddressCaptionStaticText.setText("Corresponding address:");
		newEmailAddressCaptionStaticText.setBounds(new Rectangle(12, 261, 163, 20));
		newEmailAddressCaptionStaticText.setText("New email address:");
		telephoneCaptionStaticText.setBounds(new Rectangle(12, 289, 163, 20));
		telephoneCaptionStaticText.setText("Telephone No.:");
		correspondingAddressEdit.setBounds(new Rectangle(182, 234, 258, 20));
		newEmailAddressEdit.setBounds(new Rectangle(182, 261, 258, 20));
		//newEmailAddressEdit.setInputVerifier(inputVerifier);
		telephoneEdit.setBounds(new Rectangle(182, 289, 258, 20));
		mobileCaptionStaticText.setBounds(new Rectangle(12, 316, 163, 20));
		mobileCaptionStaticText.setText("Mobile No.:");
		faxCaptionStaticText.setBounds(new Rectangle(12, 344, 163, 20));
		faxCaptionStaticText.setText("Fax No.:");
		mobileEdit.setBounds(new Rectangle(182, 316, 258, 20));
		faxEdit.setBounds(new Rectangle(182, 344, 258, 20));
		ownerInfo1CaptionStaticText.setBounds(new Rectangle(12, 371, 428, 20));
		ownerInfo1CaptionStaticText.setText("Information of additional owner(1)");
		fullName1CaptionStaticText.setBounds(new Rectangle(12, 398, 163, 20));
		fullName1CaptionStaticText.setText("Full Name:");
		ic1CaptionStaticText.setBounds(new Rectangle(12, 428, 163, 20));
		ic1CaptionStaticText.setText("IC No.:");
		fullName1Edit.setBounds(new Rectangle(182, 398, 258, 20));
		ic1Edit.setBounds(new Rectangle(182, 428, 258, 20));
		ownerInfo2CaptionStaticText.setBounds(new Rectangle(12, 455, 428, 20));
		ownerInfo2CaptionStaticText.setText("Information of additional owner(2)");
		ic3CaptionStaticText.setBounds(new Rectangle(12, 593, 163, 20));
		ic3CaptionStaticText.setText("IC No.:");
		fullName3CaptionStaticText.setBounds(new Rectangle(12, 565, 163, 20));
		fullName3CaptionStaticText.setText("Full Name:");
		fullName2Edit.setBounds(new Rectangle(182, 483, 258, 20));
		ic2Edit.setBounds(new Rectangle(182, 510, 258, 20));
		ownerInfo3CaptionStaticText.setBounds(new Rectangle(12, 538, 428, 20));
		ownerInfo3CaptionStaticText.setText("Information of additional owner(3)");
		ic2CaptionStaticText.setBounds(new Rectangle(12, 510, 163, 20));
		ic2CaptionStaticText.setText("IC No.:");
		fullName2CaptionStaticText.setBounds(new Rectangle(12, 483, 163, 20));
		fullName2CaptionStaticText.setText("Full Name:");
		fullName3Edit.setBounds(new Rectangle(182, 565, 258, 20));
		ic3Edit.setBounds(new Rectangle(182, 593, 258, 20));
		submitButton.setBounds(new Rectangle(151, 648, 78, 23));
		submitButton.setPreferredSize(new Dimension(70, 25));
		submitButton.setText("Submit");
		submitButton.addActionListener(new AdditionalClientForm_submitButton_actionAdapter(this));
		resetButton.setBounds(new Rectangle(233, 648, 70, 23));
		resetButton.setPreferredSize(new Dimension(70, 25));
		resetButton.setText("Reset");
		resetButton.addActionListener(new AdditionalClientForm_resetButton_actionAdapter(this));
		messageCaptionStaticText.setBounds(new Rectangle(14, 626, 428, 20));
		messageCaptionStaticText.setAlignment(1);
		pVStaticText21.setBorder(border1);
		pVStaticText21.setPreferredSize(new Dimension(428, 2));
		pVStaticText21.setToolTipText("");
		pVStaticText21.setText("pVStaticText21");
		pVStaticText21.setBounds(new Rectangle(12, 194, 428, 2));
		this.add(reportDateCaptionStaticText);
		this.add(organizationCaptionStaticText);
		this.add(customerAccountCaptionStaticText);
		this.add(emailCaptionStaticText);
		this.add(originalOwnerCaptionStaticText);
		this.add(correspondingAddressCaptionStaticText);
		this.add(newEmailAddressCaptionStaticText);
		this.add(telephoneCaptionStaticText);
		this.add(mobileCaptionStaticText);
		this.add(faxCaptionStaticText);
		this.add(ownerInfo1CaptionStaticText);
		this.add(ic1CaptionStaticText);
		this.add(fullName1CaptionStaticText);
		this.add(ownerInfo2CaptionStaticText);
		this.add(ic3CaptionStaticText);
		this.add(fullName3CaptionStaticText);
		this.add(ownerInfo3CaptionStaticText);
		this.add(fullName2CaptionStaticText);
		this.add(ic2CaptionStaticText);

		KeyAdapter keyAdapter = new KeyAdapter(this);
		this.add(emailEdit);
		emailEdit.addKeyListener(keyAdapter);
		this.add(reportDateStaticText);
		this.add(organizationStaticText);
		this.add(customerAccountStaticText);
		this.add(correspondingAddressEdit);
		correspondingAddressEdit.addKeyListener(keyAdapter);
		this.add(newEmailAddressEdit);
		newEmailAddressEdit.addKeyListener(keyAdapter);
		this.add(telephoneEdit);
		telephoneEdit.addKeyListener(keyAdapter);
		this.add(mobileEdit);
		mobileEdit.addKeyListener(keyAdapter);
		this.add(faxEdit);
		faxEdit.addKeyListener(keyAdapter);
		this.add(fullName1Edit);
		fullName1Edit.addKeyListener(keyAdapter);
		this.add(ic1Edit);
		ic1Edit.addKeyListener(keyAdapter);
		this.add(fullName2Edit);
		fullName2Edit.addKeyListener(keyAdapter);
		this.add(ic2Edit);
		ic2Edit.addKeyListener(keyAdapter);
		this.add(fullName3Edit);
		fullName3Edit.addKeyListener(keyAdapter);
		this.add(ic3Edit);
		ic3Edit.addKeyListener(keyAdapter);
		this.add(additionalClientCaptionStaticText);
		this.getContentPane().add(pVStaticText21);
		this.add(separator1CaptionStaticText);
		this.add(resetButton);
		this.add(submitButton);
		this.add(messageCaptionStaticText);
		this.setResizable(false);

		this.updateSubmitStatus();
	}

	private void updateSubmitStatus()
	{
		Customer customer = this._settingsManager.get_Customer();
		String email = this.emailEdit.getText();
		String organizationName = this._account.get_OrganizationName();
		String accountCode = this._account.get_Code();
		String correspondingAddress = this.correspondingAddressEdit.getText();
		String registratedEmailAddress = this.newEmailAddressEdit.getText();
		String tel = this.telephoneEdit.getText();
		String mobile = this.mobileEdit.getText();
		String fax = this.faxEdit.getText();
		String fillName1 = this.fullName1Edit.getText();
		String ICNo1 = this.ic1Edit.getText();
		String fillName2 = this.fullName2Edit.getText();
		String ICNo2 = this.ic2Edit.getText();
		String fillName3 = this.fullName3Edit.getText();
		String ICNo3 = this.ic3Edit.getText();

		this.emailEdit.setForeground(EmailInputVerifier.isValidEmail(true, email) ? Color.BLACK : Color.RED);
		this.newEmailAddressEdit.setForeground(EmailInputVerifier.isValidEmail(true, registratedEmailAddress) ? Color.BLACK : Color.RED);

		boolean enable = true;

		if (!EmailInputVerifier.isValidEmail(false, email)
		|| StringHelper.isNullOrEmpty(organizationName)
		|| StringHelper.isNullOrEmpty(accountCode)
		|| !EmailInputVerifier.isValidEmail(true, registratedEmailAddress)
		|| (
			StringHelper.isNullOrEmpty(correspondingAddress)
			&& StringHelper.isNullOrEmpty(registratedEmailAddress)
			&& StringHelper.isNullOrEmpty(tel)
			&& StringHelper.isNullOrEmpty(mobile)
			&& StringHelper.isNullOrEmpty(fax)
			&& StringHelper.isNullOrEmpty(fillName1)
			&& StringHelper.isNullOrEmpty(ICNo1)
			&& StringHelper.isNullOrEmpty(fillName2)
			&& StringHelper.isNullOrEmpty(ICNo2)
			&& StringHelper.isNullOrEmpty(fillName3)
			&& StringHelper.isNullOrEmpty(ICNo3)))
		{
			enable = false;
		}

		this.submitButton.setEnabled(enable);
	}

	private static class KeyAdapter extends java.awt.event.KeyAdapter
	{
		private AdditionalClientForm _owner;

		public KeyAdapter(AdditionalClientForm owner)
		{
			this._owner = owner;
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			this._owner.updateSubmitStatus();
		}
	}

	private PVStaticText2 additionalClientCaptionStaticText = new PVStaticText2();
	private PVStaticText2 reportDateCaptionStaticText = new PVStaticText2();
	private PVStaticText2 organizationCaptionStaticText = new PVStaticText2();
	private PVStaticText2 customerAccountCaptionStaticText = new PVStaticText2();
	private PVStaticText2 emailCaptionStaticText = new PVStaticText2();
	private PVStaticText2 separator1CaptionStaticText = new PVStaticText2();
	private PVStaticText2 reportDateStaticText = new PVStaticText2();
	private PVStaticText2 organizationStaticText = new PVStaticText2();
	private PVStaticText2 customerAccountStaticText = new PVStaticText2();
	private JTextField emailEdit = new JTextField();
	private PVStaticText2 originalOwnerCaptionStaticText = new PVStaticText2();
	private PVStaticText2 correspondingAddressCaptionStaticText = new PVStaticText2();
	private PVStaticText2 newEmailAddressCaptionStaticText = new PVStaticText2();
	private PVStaticText2 telephoneCaptionStaticText = new PVStaticText2();
	private JTextField correspondingAddressEdit = new JTextField();
	private JTextField newEmailAddressEdit = new JTextField();
	private JTextField telephoneEdit = new JTextField();
	private PVStaticText2 mobileCaptionStaticText = new PVStaticText2();
	private PVStaticText2 faxCaptionStaticText = new PVStaticText2();
	private JTextField mobileEdit = new JTextField();
	private JTextField faxEdit = new JTextField();
	private PVStaticText2 ownerInfo1CaptionStaticText = new PVStaticText2();
	private PVStaticText2 fullName1CaptionStaticText = new PVStaticText2();
	private PVStaticText2 ic1CaptionStaticText = new PVStaticText2();
	private JTextField fullName1Edit = new JTextField();
	private JTextField ic1Edit = new JTextField();
	private PVStaticText2 ownerInfo2CaptionStaticText = new PVStaticText2();
	private PVStaticText2 ic3CaptionStaticText = new PVStaticText2();
	private PVStaticText2 fullName3CaptionStaticText = new PVStaticText2();
	private JTextField fullName2Edit = new JTextField();
	private JTextField ic2Edit = new JTextField();
	private PVStaticText2 ownerInfo3CaptionStaticText = new PVStaticText2();
	private PVStaticText2 ic2CaptionStaticText = new PVStaticText2();
	private PVStaticText2 fullName2CaptionStaticText = new PVStaticText2();
	private JTextField fullName3Edit = new JTextField();
	private JTextField ic3Edit = new JTextField();
	private PVButton2 submitButton = new PVButton2();
	private PVButton2 resetButton = new PVButton2();
	private PVStaticText2 messageCaptionStaticText = new PVStaticText2();
	private Border border1 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
	private PVStaticText2 pVStaticText21 = new PVStaticText2();

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

	class AdditionalClientForm_this_windowAdapter extends WindowAdapter
	{
		private AdditionalClientForm adaptee;
		AdditionalClientForm_this_windowAdapter(AdditionalClientForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class AdditionalClientForm_resetButton_actionAdapter implements ActionListener
{
	private AdditionalClientForm adaptee;
	AdditionalClientForm_resetButton_actionAdapter(AdditionalClientForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.resetButton_actionPerformed(e);
	}
}

class AdditionalClientForm_submitButton_actionAdapter implements ActionListener
{
	private AdditionalClientForm adaptee;
	AdditionalClientForm_submitButton_actionAdapter(AdditionalClientForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}
