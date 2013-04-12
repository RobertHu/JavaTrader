package tradingConsole.ui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import framework.xml.XmlConvert;
import framework.DateTime;
import framework.StringHelper;

import tradingConsole.AppToolkit;
import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.Account;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.ui.language.AgentLanguage;
import tradingConsole.Customer;
import tradingConsole.settings.Parameter;
import tradingConsole.enumDefine.DialogOption;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField;
import java.text.DateFormat;
import javax.swing.JFrame;
import java.util.Date;
import javax.swing.BorderFactory;
import java.awt.SystemColor;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.Color;
import javax.swing.JDialog;
import java.awt.event.KeyEvent;
import tradingConsole.enumDefine.LogCode;
import framework.Guid;

public class AgentForm extends JDialog
{
	private static DateTime _reportDate;
	private TradingConsole _owner;
	private SettingsManager _settingsManager;
	private Account _account;

	public AgentForm(TradingConsole owner, SettingsManager settingsManager, Account account)
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
		this.agentCaptionStaticText.setText(AgentLanguage.agentCaption);
		this.reportDateCaptionStaticText.setText(AgentLanguage.reportDateCaption);
		this.organizationCaptionStaticText.setText(AgentLanguage.organizationCaption);
		this.customerAccountCaptionStaticText.setText(AgentLanguage.customerAccountCaption);
		this.emailCaptionStaticText.setText(AgentLanguage.emailCaption);
		this.separator1CaptionStaticText.setText(AgentLanguage.separatorCaption);
		this.effectiveDateCaptionStaticText.setText(AgentLanguage.effectiveDateCaption);
		this.preAgentInfoCaptionStaticText.setText(AgentLanguage.preAgentInfoCaption);
		this.agentCode1CaptionStaticText.setText(AgentLanguage.agentCodeCaption);
		this.agentName1CaptionStaticText.setText(AgentLanguage.agentNameCaption);
		this.newAgentInfoCaptionStaticText.setText(AgentLanguage.newAgentInfoCaption);
		this.agentCode2CaptionStaticText.setText(AgentLanguage.agentCodeCaption);
		this.agentName2CaptionStaticText.setText(AgentLanguage.agentNameCaption);
		this.icCaptionStaticText.setText(AgentLanguage.icCaption);
		this.submitButton.setText(AgentLanguage.submitButtonCaption);
		this.resetButton.setText(AgentLanguage.resetButtonCaption);
	}

	private void setDefaultValue()
	{
		this.messageCaptionStaticText.setText("");

		//this.effectiveDateDate.setFormat(2);
		DateTime appTime = this._owner.get_TradingConsoleServer().appTime();
		AgentForm._reportDate = appTime.get_Date();
		DateTime effectiveDate = appTime.addMinutes(Parameter.timeZoneMinuteSpan);
		this.reportDateTextField.setValue(effectiveDate.get_Date().toDate());
		this.organizationStaticText.setText(this._account.get_OrganizationName());
		Customer customer = this._settingsManager.get_Customer();
		this.customerAccountStaticText.setText(AppToolkit.getCustomerAccountName(customer, this._account));
		this.emailEdit.setText(customer.get_Email());
		this.effectiveDateDate.setValue(effectiveDate.get_Date().toDate());
		if(this._account.get_AgentId() != null)
		{
			this.agentCode1Edit.setText(this._account.get_AgentCode());
			this.agentName1Edit.setText(this._account.get_AgentName());
		}
		this.agentName1Edit.setEditable(false);
		this.agentCode1Edit.setEditable(false);
		this.updateSubmitStatus();
	}

	private void reset()
	{
		this.setDefaultValue();
		this.messageCaptionStaticText.setText("");

		//this.agentCode1Edit.setText("");
		//this.agentName1Edit.setText("");
		this.agentCode2Edit.setText("");
		this.agentName2Edit.setText("");
		this.icEdit.setText("");

		this.emailEdit.requestFocus();
		this.submitButton.setEnabled(false);

		this.updateSubmitStatus();
	}

	private void updateSubmitStatus()
	{
		Customer customer = this._settingsManager.get_Customer();
		String email = this.emailEdit.getText();
		String receive = Parameter.receiveAddress;
		String organizationName = this._account.get_OrganizationName();
		String customerName = customer.get_CustomerName();
		String reportDate = AgentForm._reportDate.toString("yyyy/MM/dd");
		String accountCode = this._account.get_Code();

		String previousAgentCode = this.agentCode1Edit.getText();
		String previousAgentName = this.agentName1Edit.getText();
		String newAgentCode = this.agentCode2Edit.getText();
		String newAgentName = this.agentName2Edit.getText();
		String newAgentICNo = this.icEdit.getText();
		String dateReply = XmlConvert.toString(DateTime.fromDate((Date)this.effectiveDateDate.getValue()).addMinutes(0 - Parameter.timeZoneMinuteSpan),
											   "yyyy/MM/dd");

		this.emailEdit.setForeground(EmailInputVerifier.isValidEmail(true, email) ? Color.BLACK : Color.RED);

		boolean enable = true;

		if (!EmailInputVerifier.isValidEmail(false, email)
			|| StringHelper.isNullOrEmpty(organizationName)
			|| StringHelper.isNullOrEmpty(accountCode)
			//|| StringHelper.isNullOrEmpty(previousAgentCode)
			//|| StringHelper.isNullOrEmpty(previousAgentName)
			|| StringHelper.isNullOrEmpty(newAgentCode)
			|| StringHelper.isNullOrEmpty(newAgentName)
			|| StringHelper.isNullOrEmpty(newAgentICNo)
			|| StringHelper.isNullOrEmpty(dateReply))
		{
			enable = false;
		}

		this.submitButton.setEnabled(enable);
	}

	private static class KeyAdapter extends java.awt.event.KeyAdapter
	{
		private AgentForm _owner;

		public KeyAdapter(AgentForm owner)
		{
			this._owner = owner;
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			this._owner.updateSubmitStatus();
		}
	}

	private void submit()
	{
		Customer customer = this._settingsManager.get_Customer();
		String email = this.emailEdit.getText();
		String receive = Parameter.receiveAddress;
		String organizationName = this._account.get_OrganizationName();
		String customerName = customer.get_CustomerName();
		String reportDate = AgentForm._reportDate.toString("yyyy/MM/dd");
		String accountCode = this._account.get_Code();

		String previousAgentCode = this.agentCode1Edit.getText();
		String previousAgentName = this.agentName1Edit.getText();
		String newAgentCode = this.agentCode2Edit.getText();
		String newAgentName = this.agentName2Edit.getText();
		String newAgentICNo = this.icEdit.getText();
		String dateReply = XmlConvert.toString( DateTime.fromDate((Date)this.effectiveDateDate.getValue()).addMinutes(0 - Parameter.timeZoneMinuteSpan),
											   "yyyy/MM/dd");
		if (StringHelper.isNullOrEmpty(email)
			//|| email.indexOf(',', 0) != -1
			|| StringHelper.isNullOrEmpty(organizationName)
			|| StringHelper.isNullOrEmpty(accountCode)
			//|| StringHelper.isNullOrEmpty(previousAgentCode)
			//|| StringHelper.isNullOrEmpty(previousAgentName)
			|| StringHelper.isNullOrEmpty(newAgentCode)
			|| StringHelper.isNullOrEmpty(newAgentName)
			|| StringHelper.isNullOrEmpty(newAgentICNo)
			|| StringHelper.isNullOrEmpty(dateReply))
		{
			this.messageCaptionStaticText.setText(Language.AgentPageSubmitAlert0);
			this.emailEdit.requestFocus();
		}
		else
		{
			//Dialog2.showDialog(this, Language.notify2, true, Language.ConfirmSendEmail);
			//if (Dialog2.dialogOption == DialogOption.Yes)
			{
				String result = this._owner.get_TradingConsoleServer().agent(email, receive, organizationName, customerName, reportDate, accountCode,
					previousAgentCode, previousAgentName, newAgentCode, newAgentName, newAgentICNo, dateReply);

				if(result.indexOf("Error") != 0)
				{
					String logAction = Language.WebServiceAgentMessage1 + result;
					this._owner.saveLog(LogCode.Other, logAction, Guid.empty, this._account.get_Id());
					AlertDialogForm.showDialog(this, Language.messageContentFormTitle, true, logAction);
					this.dispose();
				}
				else
				{
					if(result.length() > 6)
					{
						this.messageCaptionStaticText.setText(result.substring(6));
					}
					else
					{
						this.messageCaptionStaticText.setText(Language.WebServiceAgentMessage0);
					}
				}
			}
		}
		//this.emailEdit.requestFocus();
	}

	private void jbInit() throws Exception
	{
		border1 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.white, new Color(148, 145, 140));
		this.addWindowListener(new AgentForm_this_windowAdapter(this));

		this.setSize(465, 560);
		this.setResizable(false);
		this.setLayout(null);
		this.setTitle(Language.AgentPrompt);
		this.setBackground(FormBackColor.agentForm);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		agentCaptionStaticText.setBounds(new Rectangle(20, 14, 428, 33));
		agentCaptionStaticText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 13));
		agentCaptionStaticText.setText("CHANGE OF AGENT REGISTRATION");
		agentCaptionStaticText.setAlignment(1);
		reportDateCaptionStaticText.setBounds(new Rectangle(20, 70, 164, 20));
		reportDateCaptionStaticText.setText("Date:");
		this.add(PVStaticText2.createNotNullFlagFor(reportDateCaptionStaticText));
		reportDateTextField.setBounds(new Rectangle(189, 70, 247, 20));
		//reportDateStaticText.setText("2006/10/20");
		organizationCaptionStaticText.setBounds(new Rectangle(20, 103, 164, 20));
		organizationCaptionStaticText.setText("To:");
		organizationStaticText.setBounds(new Rectangle(189, 103, 247, 20));
		customerAccountCaptionStaticText.setBounds(new Rectangle(20, 136, 164, 20));
		customerAccountCaptionStaticText.setText("From:");
		emailCaptionStaticText.setBounds(new Rectangle(20, 168, 164, 20));
		emailCaptionStaticText.setText("Registrated E-mail:");
		this.add(PVStaticText2.createNotNullFlagFor(emailCaptionStaticText));

		emailEdit.setBounds(new Rectangle(189, 168, 247, 20));
		//emailEdit.setInputVerifier(new EmailInputVerifier(true));
		separator1CaptionStaticText.setBounds(new Rectangle(19, 204, 418, 2));
		separator1CaptionStaticText.setBorder(border2);
		separator1CaptionStaticText.setMaximumSize(new Dimension(4, 2));
		separator1CaptionStaticText.setMinimumSize(new Dimension(4, 2));
		separator1CaptionStaticText.setPreferredSize(new Dimension(418, 2));
		separator1CaptionStaticText.setToolTipText("");
		effectiveDateCaptionStaticText.setBounds(new Rectangle(21, 216, 164, 20));
		effectiveDateCaptionStaticText.setText("Effective Date:\t");
		this.add(PVStaticText2.createNotNullFlagFor(effectiveDateCaptionStaticText));
		effectiveDateDate.setBounds(new Rectangle(190, 216, 247, 20));
		//effectiveDateDate.setText("2006/10/20");
		preAgentInfoCaptionStaticText.setBounds(new Rectangle(21, 249, 414, 20));
		preAgentInfoCaptionStaticText.setText("Previous Agent Information");
		agentCode1CaptionStaticText.setBounds(new Rectangle(21, 281, 164, 20));
		agentCode1CaptionStaticText.setText("Agent Code:");
		//this.add(PVStaticText2.createNotNullFlagFor(agentCode1CaptionStaticText));

		agentCode1Edit.setBounds(new Rectangle(190, 281, 247, 20));
		agentName1CaptionStaticText.setBounds(new Rectangle(21, 314, 164, 20));
		agentName1CaptionStaticText.setText("Agent Name:");
		//this.add(PVStaticText2.createNotNullFlagFor(agentName1CaptionStaticText));

		agentName1Edit.setBounds(new Rectangle(189, 325, 247, 20));
		newAgentInfoCaptionStaticText.setBounds(new Rectangle(21, 347, 415, 20));
		newAgentInfoCaptionStaticText.setText("New Agent Information");
		agentCode2CaptionStaticText.setBounds(new Rectangle(21, 380, 164, 20));
		agentCode2CaptionStaticText.setText("Agent Code:");
		this.add(PVStaticText2.createNotNullFlagFor(agentCode2CaptionStaticText));

		agentCode2Edit.setBounds(new Rectangle(190, 380, 247, 20));
		agentName2CaptionStaticText.setBounds(new Rectangle(21, 412, 164, 20));
		agentName2CaptionStaticText.setText("Agent Name:");
		this.add(PVStaticText2.createNotNullFlagFor(agentName2CaptionStaticText));

		agentName2Edit.setBounds(new Rectangle(190, 412, 247, 20));
		icCaptionStaticText.setBounds(new Rectangle(21, 445, 164, 20));
		icCaptionStaticText.setText("IC No.:");
		this.add(PVStaticText2.createNotNullFlagFor(icCaptionStaticText));

		icCaptionStaticText.setPreferredSize(new Dimension(163, 20));
		icEdit.setBounds(new Rectangle(190, 445, 247, 20));
		messageCaptionStaticText.setBounds(new Rectangle(20, 477, 428, 20));
		messageCaptionStaticText.setAlignment(1);
		submitButton.setBounds(new Rectangle(166, 498, 70, 23));
		submitButton.setText("Submit");
		submitButton.addActionListener(new AgentForm_submitButton_actionAdapter(this));
		resetButton.setBounds(new Rectangle(242, 498, 70, 23));
		resetButton.setText("Reset");
		resetButton.addActionListener(new AgentForm_resetButton_actionAdapter(this));
		customerAccountStaticText.setBounds(new Rectangle(189, 136, 247, 20));
		pVStaticText21.setBorder(border2);
		pVStaticText21.setMaximumSize(new Dimension(4, 2));
		pVStaticText21.setMinimumSize(new Dimension(4, 2));
		pVStaticText21.setPreferredSize(new Dimension(418, 2));
		pVStaticText21.setToolTipText("");
		pVStaticText21.setText("pVStaticText21");
		pVStaticText21.setBounds(new Rectangle(19, 478, 418, 2));
		this.add(agentCaptionStaticText);
		this.add(reportDateCaptionStaticText);
		this.add(organizationCaptionStaticText);
		this.add(customerAccountCaptionStaticText);
		this.add(emailCaptionStaticText);
		this.add(agentName1Edit);
		KeyAdapter keyAdapter = new KeyAdapter(this);
		agentName1Edit.addKeyListener(keyAdapter);
		this.add(customerAccountStaticText);
		this.add(emailEdit);
		emailEdit.addKeyListener(keyAdapter);
		this.add(reportDateTextField);
		reportDateTextField.addKeyListener(keyAdapter);
		this.add(organizationStaticText);
		this.add(separator1CaptionStaticText);
		this.add(agentCode2Edit);
		agentCode2Edit.addKeyListener(keyAdapter);
		this.add(effectiveDateDate);
		effectiveDateDate.addKeyListener(keyAdapter);
		this.add(effectiveDateCaptionStaticText);
		this.add(preAgentInfoCaptionStaticText);
		this.add(agentCode1Edit);
		agentCode1Edit.addKeyListener(keyAdapter);
		this.add(agentCode1CaptionStaticText);
		this.add(agentName1CaptionStaticText);
		this.add(newAgentInfoCaptionStaticText);
		this.add(agentCode2CaptionStaticText);
		this.add(agentName2CaptionStaticText);
		this.add(icCaptionStaticText);
		this.add(icEdit);
		icEdit.addKeyListener(keyAdapter);
		this.add(agentName2Edit);
		agentName2Edit.addKeyListener(keyAdapter);
		this.add(resetButton);
		this.add(messageCaptionStaticText);
		this.getContentPane().add(pVStaticText21);
		this.add(submitButton);
		submitButton.setEnabled(false);
	}

	private PVStaticText2 agentCaptionStaticText = new PVStaticText2();
	private PVStaticText2 reportDateCaptionStaticText = new PVStaticText2();
	private JFormattedTextField reportDateTextField = new JFormattedTextField(DateFormat.getDateInstance());
	private PVStaticText2 organizationCaptionStaticText = new PVStaticText2();
	private PVStaticText2 organizationStaticText = new PVStaticText2();
	private PVStaticText2 customerAccountCaptionStaticText = new PVStaticText2();
	private PVStaticText2 emailCaptionStaticText = new PVStaticText2();
	private JTextField emailEdit = new JTextField();
	private PVStaticText2 separator1CaptionStaticText = new PVStaticText2();
	private PVStaticText2 effectiveDateCaptionStaticText = new PVStaticText2();
	private JFormattedTextField effectiveDateDate = new JFormattedTextField(DateFormat.getDateInstance());
	private PVStaticText2 preAgentInfoCaptionStaticText = new PVStaticText2();
	private PVStaticText2 agentCode1CaptionStaticText = new PVStaticText2();
	private JTextField agentCode1Edit = new JTextField();
	private PVStaticText2 agentName1CaptionStaticText = new PVStaticText2();
	private JTextField agentName1Edit = new JTextField();
	private PVStaticText2 newAgentInfoCaptionStaticText = new PVStaticText2();
	private PVStaticText2 agentCode2CaptionStaticText = new PVStaticText2();
	private JTextField agentCode2Edit = new JTextField();
	private PVStaticText2 agentName2CaptionStaticText = new PVStaticText2();
	private JTextField agentName2Edit = new JTextField();
	private PVStaticText2 icCaptionStaticText = new PVStaticText2();
	private JTextField icEdit = new JTextField();
	private PVStaticText2 messageCaptionStaticText = new PVStaticText2();
	private PVButton2 submitButton = new PVButton2();
	private PVButton2 resetButton = new PVButton2();
	private PVStaticText2 customerAccountStaticText = new PVStaticText2();
	private Border border1 = BorderFactory.createLineBorder(SystemColor.controlText, 2);
	private Border border2 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
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

	class AgentForm_this_windowAdapter extends WindowAdapter
	{
		private AgentForm adaptee;
		AgentForm_this_windowAdapter(AgentForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class AgentForm_resetButton_actionAdapter implements ActionListener
{
	private AgentForm adaptee;
	AgentForm_resetButton_actionAdapter(AgentForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.resetButton_actionPerformed(e);
	}
}

class AgentForm_submitButton_actionAdapter implements ActionListener
{
	private AgentForm adaptee;
	AgentForm_submitButton_actionAdapter(AgentForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}
