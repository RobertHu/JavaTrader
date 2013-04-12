package tradingConsole.ui;

import java.awt.Rectangle;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.Panel;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import tradingConsole.Account;

import framework.diagnostics.TraceType;

import tradingConsole.service.UpdatePasswordResult;
import tradingConsole.ui.language.Language;
import tradingConsole.AppToolkit;
import tradingConsole.TradingConsole;
import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.ui.language.ActivateAccount;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JFrame;
import tradingConsole.ui.grid.DataGrid;
import javax.swing.JComboBox;
import java.util.Arrays;
import javax.swing.JTextField;
import framework.StringHelper;
import java.util.Iterator;
import java.awt.Color;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import com.jidesoft.combobox.ListComboBox;
import tradingConsole.UserLoginInfoStorage;
import tradingConsole.ui.language.Login;
import framework.Guid;

public class ChangePasswordForm2 extends JDialog
{
	private TradingConsole _tradingConsole;

	public ChangePasswordForm2(JFrame parent, TradingConsole tradingConsole)
	{
		super(parent, true);

		this._tradingConsole = tradingConsole;
		try
		{
			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}

		this.optionTab.removeAll();
		if(this._tradingConsole.get_SettingsManager().allowChangePasswordInTrader())
		{
			this.optionTab.addTab(Language.ChangePasswordForm2ChangePasswordPanel, null, this.changePasswordPanel,
								  Language.ChangePasswordForm2ChangePasswordPanel);
		}
		if (this._tradingConsole.needRecoverPasswordQuotationSetting())
		{
			this.optionTab.addTab(Language.ChangePasswordForm2RecoverPasswordPanel, null, this.recoverPasswordPanel, Language.ChangePasswordForm2RecoverPasswordPanel);
		}
		if (this._tradingConsole.get_SettingsManager().get_SystemParameter().get_EnableModifyTelephoneIdentification())
		{
			this.optionTab.addTab(Language.TelephoneIdentificationCodePanel, null, this.telephoneIdentificationCodePanel, Language.TelephoneIdentificationCodePanel);
			this.accountChoice.setEditable(false);
			this.optionTab.addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent e)
				{
					if(optionTab.getSelectedComponent() == telephoneIdentificationCodePanel)
					{
						boolean isEmployee = _tradingConsole.get_SettingsManager().get_Customer().get_IsEmployee();
						if(isEmployee) return;

						telephoneIdentificationCodePanel.remove(accountChoice);
						telephoneIdentificationCodePanel.add(accountChoice, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
							, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 50, 0));
						int count = fillAccount(accountChoice);
						if(count > 0)
						{
							accountChoice.setSelectedIndex(0);
						}
					}
				}
			});
		}
		if(this._tradingConsole.get_SettingsManager().get_SystemParameter().get_EnableMarginPin())
		{
			this.optionTab.addTab(Language.changeMarginPIN, null, this.changeMarginPinPanel, Language.changeMarginPIN);
		}

		this.setTitle(Language.ChangePasswordlblChangePasswordPromptTitle);
		this.okButton2.setText(Language.btnOKCaption);
		this.currentStaticText.setText(Language.ChangePasswordlblCurrentPassword);
		this.newStaticText.setText(Language.ChangePasswordlblNewPassword);
		this.confirmStaticText.setText(Language.ChangePasswordlblConfirmPassword);
		this.messageMultiTextArea.setMessage(Language.ChangePasswordlblPrompt);

		//   0 - text string is not modified.
		//   1 - text string is converted to the upper case.
		//   2 - text string is converted to the lower case.
		//   Default value is 0.

		//BUT DEFAULT is 2!!!

		this.currentPassword.setEchoChar('*');
		this.newPassword.setEchoChar('*');
		this.confirmPassword.setEchoChar('*');

		//this.notifyStaticText.setText(ActivateAccount.RecoverPasswordQuotationSettingNotify);
		this.okButton.setText(ActivateAccount.Ok);
		this.messageStaticText.setText("");

		this._tradingConsole.get_RecoverPasswordManager().setAnswerToEmpty();
		this._tradingConsole.get_RecoverPasswordManager().initialize(this.answerTable, true, false);

		this.currentPassword.requestFocus();
	}

	private int fillAccount(ListComboBox choice)
	{
		boolean isEmployee = this._tradingConsole.get_SettingsManager().get_Customer().get_IsEmployee();
		if(isEmployee)
		{
			this.coustomerNameText.setText(this._tradingConsole.get_SettingsManager().get_Customer().get_CustomerName());
			return 0;
		}
		else
		{
			choice.removeAllItems();
			Account[] accounts = new Account[this._tradingConsole.get_SettingsManager().get_Accounts().values().size()];
			accounts = this._tradingConsole.get_SettingsManager().get_Accounts().values().toArray(accounts);
			Arrays.sort(accounts, Account.comparatorByCode);
			int count = 0;
			for (Account account : accounts)
			{
				boolean verifiedCustomerIdentity = account.get_VerifiedCustomerIdentity();
				if (verifiedCustomerIdentity)
				{
					choice.addItem(account.get_Code());
					count++;
				}
			}
			return count;
		}
	}

	private boolean isValidPasswordInput(String inputString)
	{
		return (inputString.length() >= 8 && inputString.length() <= 16);
	}

	private void changePasswordSubmit()
	{
		if (this.isValidPasswordInput(new String(this.currentPassword.getPassword())) &&
			this.isValidPasswordInput(new String(this.newPassword.getPassword())) &&
			this.isValidPasswordInput(new String(this.confirmPassword.getPassword())))
		{
			if (new String(this.newPassword.getPassword()).equals(new String(this.confirmPassword.getPassword())))
			{
				UpdatePasswordResult updatePasswordResult = this._tradingConsole.get_TradingConsoleServer().updatePassword(this._tradingConsole.
					get_LoginInformation().get_LoginName(),
					new String(this.currentPassword.getPassword()), new String(this.newPassword.getPassword()));
				boolean isSucced = updatePasswordResult.get_IsSucceed();
				if (isSucced)
				{
					UserLoginInfoStorage.save(UserLoginInfoStorage.get_UserName(), UserLoginInfoStorage.get_saveUserName(),
						new String(this.newPassword.getPassword()), UserLoginInfoStorage.get_savePassword());
					this.messageStaticText2.setText(Language.ChangePasswordMessage1);
					this.currentPassword.setText("");
					this.newPassword.setText("");
					this.confirmPassword.setText("");
				}
				else
				{
					this.messageStaticText2.setText(Language.ChangePasswordMessage2);
				}
			}
			else
			{
				this.messageStaticText2.setText(Language.ChangePasswordMessage3);
			}
		}
		else
		{
			this.messageStaticText2.setText(Language.ChangePasswordMessage4);
		}
	}

	private void clearMessage()
	{
		this.messageStaticText2.setText("");
	}

	public void okButton2_actionPerformed(ActionEvent e)
	{
		this.changePasswordSubmit();
	}

	private void submit()
	{
		this.answerTable.updateEditingValue();
		this.messageStaticText.setText("");

		String[][] recoverPasswordDatas = this._tradingConsole.get_RecoverPasswordManager().getRecoverPasswordDatas();
		if (recoverPasswordDatas.length <= 0)
		{
			this.messageStaticText.setText(ActivateAccount.RecoverPasswordDataNotAvailable);
			return;
		}

		try
		{
			boolean isSucceed = this._tradingConsole.get_TradingConsoleServer().recoverPasswordDatas(recoverPasswordDatas);
			if (isSucceed)
			{
				this.messageStaticText.setText(Language.ChangePasswordForm2SucceedRecoverPassword);
			}
			else
			{
				this.messageStaticText.setText(Language.ChangePasswordForm2FailedRecoverPassword);
			}
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

//SourceCode End//////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		Font font = new java.awt.Font("SansSerif", Font.BOLD, 12);
		this.setSize(620, 360);
		this.setBackground(FormBackColor.changePasswordForm);
		this.setLayout(gridBagLayout1);
		this.setResizable(false);
		messageStaticText2.setBounds(new Rectangle(9, 112, 610, 21));

		messageStaticText2.setAlignment(1);
		currentPassword.addFocusListener(new ChangePasswordForm2_currentPassword_focusAdapter(this));
		currentPassword.addActionListener(new ChangePasswordForm2_currentPassword_actionAdapter(this));
		newPassword.addFocusListener(new ChangePasswordForm2_newPassword_focusAdapter(this));
		confirmPassword.addFocusListener(new ChangePasswordForm2_confirmPassword_focusAdapter(this));
		okButton2.setBounds(new Rectangle(262, 151, 85, 26));
		okButton2.setText("Ok");
		okButton2.addActionListener(new ChangePasswordForm2_okButton2_actionAdapter(this));
		currentStaticText.setBounds(new Rectangle(178, 14, 80, 31));
		currentStaticText.setFont(font);
		currentStaticText.setText("Current");
		confirmStaticText.setBounds(new Rectangle(178, 85, 77, 24));
		confirmStaticText.setText("Confirm");
		confirmStaticText.setFont(font);
		newStaticText.setBounds(new Rectangle(178, 52, 78, 28));
		newStaticText.setText("New");
		newStaticText.setFont(font);
		confirmPassword.setBounds(new Rectangle(260, 80, 130, 26));
		newPassword.setBounds(new Rectangle(260, 50, 130, 26));
		currentPassword.setBounds(new Rectangle(260, 19, 130, 27));

		changePasswordPanel.setLayout(null);
		optionTab.setFont(font);
		optionTab.setPreferredSize(new Dimension(397, 525));
		recoverPasswordPanel.setLayout(null);
		changePasswordPanel.setLayout(null);
		messageMultiTextArea.setBounds(new Rectangle(113, 182, 370, 88));
		messageMultiTextArea.setFont(font);
		okButton.setBounds(new Rectangle(262, 265, 85, 26));
		answerTable.setBounds(new Rectangle(1, 3, 610, 231));
		messageStaticText.setBounds(new Rectangle(9, 235, 610, 28));
		messageStaticText.setAlignment(1);
		//answerTable.setLabel("pVTable1");
		okButton.setText("OK");
		okButton.setFont(font);
		okButton.addActionListener(new ChangePasswordForm2_okButton_actionAdapter(this));
		messageStaticText.setText("message");
		messageStaticText.setFont(font);
		messageStaticText2.setFont(font);
		okButton2.setFont(font);

		changePasswordPanel.add(messageMultiTextArea);
		changePasswordPanel.add(confirmPassword);
		changePasswordPanel.add(currentPassword);
		changePasswordPanel.add(currentStaticText);
		changePasswordPanel.add(newPassword);
		changePasswordPanel.add(messageStaticText2);
		changePasswordPanel.add(confirmStaticText);
		changePasswordPanel.add(newStaticText);
		changePasswordPanel.add(okButton2);

		JScrollPane scrollPane = new JScrollPane(answerTable);
		scrollPane.setBounds(1, 2, 600, 231);
		recoverPasswordPanel.add(scrollPane);
		recoverPasswordPanel.add(messageStaticText);
		recoverPasswordPanel.add(okButton);

		boolean isEmployee = this._tradingConsole.get_SettingsManager().get_Customer().get_IsEmployee();
		accountStaticText.setFont(font);
		coustomerNameText.setFont(font);
		accountStaticText.setText(isEmployee ? Login.lblUserName : Language.ReportAccountCode);
		telephoneIdentificationCodePanel.setLayout(new GridBagLayout());
		telephoneIdentificationCodePanel.add(this.accountStaticText, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 30, 0));

		if(isEmployee)
		{
			telephoneIdentificationCodePanel.add(this.coustomerNameText, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 50, 0));
		}
		else
		{
			telephoneIdentificationCodePanel.add(this.accountChoice, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
				, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 50, 0));
		}
		oldTVCodeStaticText.setFont(font);
		oldTVCodeStaticText.setText(Language.OldTelephoneIdentificationCode);
		telephoneIdentificationCodePanel.add(this.oldTVCodeStaticText, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 30, 0));
		telephoneIdentificationCodePanel.add(this.oldTVCodeEdit, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 0, 0), 50, 0));

		newTVCodeStaticText.setFont(font);
		newTVCodeStaticText.setText(Language.NewTelephoneIdentificationCode);
		telephoneIdentificationCodePanel.add(this.newTVCodeStaticText, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 30, 0));
		telephoneIdentificationCodePanel.add(this.newTVCodeEdit, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 0, 0), 50, 0));

		reconfirmTVCodeStaticText.setFont(font);
		reconfirmTVCodeStaticText.setText(Language.ReconfirmTelephoneIdentificationCode);
		telephoneIdentificationCodePanel.add(this.reconfirmTVCodeStaticText, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 30, 0));
		telephoneIdentificationCodePanel.add(this.reconfirmTVCodeEdit, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 0, 0), 50, 0));

		this.messageStaticText3.setFont(font);
		telephoneIdentificationCodePanel.add(this.messageStaticText3, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 30, 0));

		submitTVCodeButton.setFont(font);
		submitTVCodeButton.setText(Language.btnOKCaption);
		telephoneIdentificationCodePanel.add(this.submitTVCodeButton, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 0), 30, 0));
		submitTVCodeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				changeTelephoneIdentificationCode();
			}
		});
		jbInitChangeMarginPinPanel(font);

		this.add(optionTab, new GridBagConstraints2(0, 6, 1, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 2, 1, 2), 0, 0));
		/*this.add(recoverPasswordPanel, new GridBagConstraints2(0, 5, 1, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(7, 0, 0, 1), 0, 0));
		this.add(changePasswordPanel, new GridBagConstraints2(0, 6, 1, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(9, 2, 2, 2), 0, 0));

		optionTab.removeAll();*/
		this.addWindowListener(new ChangePasswordForm_this_windowAdapter(this));
	}

	private void jbInitChangeMarginPinPanel(Font font)
	{
		changeMarginPinPanel.setLayout(new GridBagLayout());

		accountStaticText2.setFont(font);
		accountStaticText2.setText(Language.ReportAccountCode);
		changeMarginPinPanel.add(this.accountStaticText2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 30, 0));
		this.accountChoice2.setEditable(false);
		changeMarginPinPanel.add(this.accountChoice2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 50, 0));

		oldTVCodeStaticText2.setFont(font);
		oldTVCodeStaticText2.setText(Language.OldTelephoneIdentificationCode);
		changeMarginPinPanel.add(this.oldTVCodeStaticText2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 30, 0));
		changeMarginPinPanel.add(this.oldTVCodeEdit2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 0, 0), 50, 0));

		newTVCodeStaticText2.setFont(font);
		newTVCodeStaticText2.setText(Language.NewTelephoneIdentificationCode);
		changeMarginPinPanel.add(this.newTVCodeStaticText2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 30, 0));
		changeMarginPinPanel.add(this.newTVCodeEdit2, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 0, 0), 50, 0));

		reconfirmTVCodeStaticText2.setFont(font);
		reconfirmTVCodeStaticText2.setText(Language.ReconfirmTelephoneIdentificationCode);
		changeMarginPinPanel.add(this.reconfirmTVCodeStaticText2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 30, 0));
		changeMarginPinPanel.add(this.reconfirmTVCodeEdit2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 0, 0), 50, 0));

		this.messageStaticText32.setFont(font);
		changeMarginPinPanel.add(this.messageStaticText32, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 10, 0, 0), 30, 0));

		submitTVCodeButton2.setFont(font);
		submitTVCodeButton2.setText(Language.btnOKCaption);
		changeMarginPinPanel.add(this.submitTVCodeButton2, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 0), 30, 0));
		submitTVCodeButton2.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				changeMarginPin();
			}
		});
		int count = fillAccount(accountChoice2);
		if(count > 0)
		{
			accountChoice2.setSelectedIndex(0);
		}
	}

	JTabbedPane optionTab = new JTabbedPane();
	Panel changePasswordPanel = new Panel();
	Panel recoverPasswordPanel = new Panel();
	Panel changeMarginPinPanel = new Panel();
	Panel telephoneIdentificationCodePanel = new Panel();

	private JPasswordField currentPassword = new JPasswordField();
	private JPasswordField newPassword = new JPasswordField();
	private JPasswordField confirmPassword = new JPasswordField();
	private PVButton2 okButton2 = new PVButton2();
	private PVStaticText2 currentStaticText = new PVStaticText2();
	private PVStaticText2 newStaticText = new PVStaticText2();
	private PVStaticText2 confirmStaticText = new PVStaticText2();
	private PVStaticText2 messageStaticText2 = new PVStaticText2();
	private MultiTextArea messageMultiTextArea = new MultiTextArea();
	private DataGrid answerTable = new DataGrid("RecoverPasswordAnswerGrid");
	private PVButton2 okButton = new PVButton2();
	private PVStaticText2 messageStaticText = new PVStaticText2();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	private PVStaticText2 accountStaticText = new PVStaticText2();
	private ListComboBox accountChoice = new ListComboBox();
	private PVStaticText2 coustomerNameText = new PVStaticText2();
	private PVStaticText2 oldTVCodeStaticText = new PVStaticText2();
	private JPasswordField oldTVCodeEdit = new JPasswordField();

	private PVStaticText2 newTVCodeStaticText = new PVStaticText2();
	private JPasswordField newTVCodeEdit = new JPasswordField();

	private PVStaticText2 reconfirmTVCodeStaticText = new PVStaticText2();
	private JPasswordField reconfirmTVCodeEdit = new JPasswordField();

	private PVStaticText2 messageStaticText3 = new PVStaticText2();
	private PVButton2 submitTVCodeButton = new PVButton2();


	private PVStaticText2 accountStaticText2 = new PVStaticText2();
	private ListComboBox accountChoice2 = new ListComboBox();
	private PVStaticText2 oldTVCodeStaticText2 = new PVStaticText2();
	private JPasswordField oldTVCodeEdit2 = new JPasswordField();

	private PVStaticText2 newTVCodeStaticText2 = new PVStaticText2();
	private JPasswordField newTVCodeEdit2 = new JPasswordField();

	private PVStaticText2 reconfirmTVCodeStaticText2 = new PVStaticText2();
	private JPasswordField reconfirmTVCodeEdit2 = new JPasswordField();

	private PVStaticText2 messageStaticText32 = new PVStaticText2();
	private PVButton2 submitTVCodeButton2 = new PVButton2();


	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	private void changeTelephoneIdentificationCode()
	{
		this.messageStaticText3.setForeground(Color.RED);
		if(StringHelper.isNullOrEmpty(new String(this.oldTVCodeEdit.getPassword())))
		{
			this.messageStaticText3.setText(Language.OldTelephoneIdentificationCodeIsEmpty);
			return;
		}

		if(StringHelper.isNullOrEmpty(new String(this.newTVCodeEdit.getPassword())))
		{
			this.messageStaticText3.setText(Language.NewTelephoneIdentificationCodeIsEmpty);
			return;
		}
		if(StringHelper.isNullOrEmpty(new String(this.reconfirmTVCodeEdit.getPassword())))
		{
			this.messageStaticText3.setText(Language.ReconfirmTelephoneIdentificationCodeIsEmpty);
			return;
		}
		if(!new String(this.newTVCodeEdit.getPassword()).equals(new String(this.reconfirmTVCodeEdit.getPassword())))
		{
			this.messageStaticText3.setText(Language.TelephoneIdentificationCodeIsDifferent);
			return;
		}

		String oldCode = new String(this.oldTVCodeEdit.getPassword());
		String newCode = new String(this.newTVCodeEdit.getPassword());
		int selectedIndex = this.accountChoice.getSelectedIndex();
		//String accountCode = this.accountChoice.getItemAt(selectedIndex).toString();

		boolean isEmployee = this._tradingConsole.get_SettingsManager().get_Customer().get_IsEmployee();
		Guid id = null;
		if(isEmployee)
		{
			id = this._tradingConsole.get_SettingsManager().get_Customer().get_UserId();
		}
		else
		{
			String accountCode = this.accountChoice.getSelectedItem().toString();
			Account account = this.getAccount(accountCode);
			id =  account.get_Id();
		}
		if(this._tradingConsole.get_TradingConsoleServer().modifyTelephoneIdentificationCode(id, oldCode, newCode))
		{
			this.messageStaticText3.setForeground(Color.BLACK);
			this.messageStaticText3.setText(Language.ModifyTelephoneIdentificationCodeSussessed);
		}
		else
		{
			this.messageStaticText3.setText(Language.ModifyTelephoneIdentificationCodeFailed);
		}
	}

	private void changeMarginPin()
	{
		this.messageStaticText32.setForeground(Color.RED);
		if(this.oldTVCodeEdit2.getPassword() == null || this.oldTVCodeEdit2.getPassword().length == 0)
		{
			this.messageStaticText32.setText(Language.OldTelephoneIdentificationCodeIsEmpty);
			return;
		}
		if(this.newTVCodeEdit2.getPassword() == null || this.newTVCodeEdit2.getPassword().length == 0)
		{
			this.messageStaticText32.setText(Language.NewTelephoneIdentificationCodeIsEmpty);
			return;
		}
		if(this.reconfirmTVCodeEdit2.getPassword() == null || this.reconfirmTVCodeEdit2.getPassword().length == 0)
		{
			this.messageStaticText32.setText(Language.ReconfirmTelephoneIdentificationCodeIsEmpty);
			return;
		}
		String newCode = new String(this.newTVCodeEdit2.getPassword()).trim();
		String reconfirmNewCode = new String(this.reconfirmTVCodeEdit2.getPassword()).trim();

		if(!this.isValidPasswordInput(newCode) || !this.isValidPasswordInput(reconfirmNewCode))
		{
			this.messageStaticText32.setText(Language.ChangePasswordMessage3);
			return;
		}

		if(!newCode.equals(reconfirmNewCode))
		{
			this.messageStaticText32.setText(Language.TelephoneIdentificationCodeIsDifferent);
			return;
		}

		String oldCode = new String(this.oldTVCodeEdit2.getPassword()).trim();
		String accountCode = this.accountChoice2.getSelectedItem().toString();
		Account account = this.getAccount(accountCode);
		if(this._tradingConsole.get_TradingConsoleServer().changeMarginPin(account.get_Id(), oldCode, newCode))
		{
			this.messageStaticText32.setForeground(Color.BLACK);
			this.messageStaticText32.setText(Language.ModifyTelephoneIdentificationCodeSussessed);
		}
		else
		{
			this.messageStaticText32.setText(Language.ModifyTelephoneIdentificationCodeFailed);
		}
	}

	private Account getAccount(String accountCode)
	{
		for (Iterator<Account> iterator = this._tradingConsole.get_SettingsManager().get_Accounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			if (account.get_Code().equals(accountCode))
			{
				return account;
			}
		}
		return null;
	}


	class ChangePasswordForm2_okButton_actionAdapter implements ActionListener
	{
		private ChangePasswordForm2 adaptee;
		ChangePasswordForm2_okButton_actionAdapter(ChangePasswordForm2 adaptee)
		{
			this.adaptee = adaptee;
		}

		public void actionPerformed(ActionEvent e)
		{
			adaptee.okButton_actionPerformed(e);
		}
	}

	class ChangePasswordForm_this_windowAdapter extends WindowAdapter
	{
		private ChangePasswordForm2 adaptee;
		ChangePasswordForm_this_windowAdapter(ChangePasswordForm2 adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}

	public void currentPassword_actionPerformed(ActionEvent e)
	{

	}

	public void okButton_actionPerformed(ActionEvent e)
	{
		this.submit();
	}

	public void currentPassword_focusGained(FocusEvent focusEvent)
	{
		this.clearMessage();
	}

	public void confirmPassword_focusGained(FocusEvent focusEvent)
	{
		this.clearMessage();
	}

	public void newPassword_focusGained(FocusEvent focusEvent)
	{
		this.clearMessage();
	}
}

class ChangePasswordForm2_newPassword_focusAdapter extends FocusAdapter
{
	private ChangePasswordForm2 adaptee;
	ChangePasswordForm2_newPassword_focusAdapter(ChangePasswordForm2 adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusGained(FocusEvent focusEvent)
	{
		adaptee.newPassword_focusGained(focusEvent);
	}
}

class ChangePasswordForm2_confirmPassword_focusAdapter extends FocusAdapter
{
	private ChangePasswordForm2 adaptee;
	ChangePasswordForm2_confirmPassword_focusAdapter(ChangePasswordForm2 adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusGained(FocusEvent focusEvent)
	{
		adaptee.confirmPassword_focusGained(focusEvent);
	}
}

class ChangePasswordForm2_okButton2_actionAdapter implements ActionListener
{
	private ChangePasswordForm2 adaptee;
	ChangePasswordForm2_okButton2_actionAdapter(ChangePasswordForm2 adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton2_actionPerformed(e);
	}
}

class ChangePasswordForm2_currentPassword_actionAdapter implements ActionListener
{
	private ChangePasswordForm2 adaptee;
	ChangePasswordForm2_currentPassword_actionAdapter(ChangePasswordForm2 adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.currentPassword_actionPerformed(e);
	}
}

class ChangePasswordForm2_currentPassword_focusAdapter extends FocusAdapter
{
	private ChangePasswordForm2 adaptee;
	ChangePasswordForm2_currentPassword_focusAdapter(ChangePasswordForm2 adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusGained(FocusEvent focusEvent)
	{
		adaptee.currentPassword_focusGained(focusEvent);
	}
}
