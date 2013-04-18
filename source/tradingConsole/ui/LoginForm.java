package tradingConsole.ui;

//import java.awt.event.WindowEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import javax.swing.*;
import javax.swing.event.*;

import framework.*;
import framework.data.*;
import framework.diagnostics.*;
import framework.xml.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.framework.*;
import tradingConsole.service.*;
import tradingConsole.settings.*;
import tradingConsole.ui.colorHelper.*;
import tradingConsole.ui.grid.ActionEvent;
import tradingConsole.ui.language.*;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;
import Packet.*;
import Connection.ConnectionManager;
import Connection.ConnectionManager.TcpInializeTimeoutException;
public class LoginForm extends JFrame
{
	public static int maxTryCount = 3;
	private TradingConsole _owner;
	private ServiceManager _editingServiceManager = ServiceManager.Create();
	private boolean isRecover;
	private Logger logger = Logger.getLogger(LoginForm.class);

	public LoginForm(JFrame parent)
	{
		//super(parent/*, true*/);
		//this.setAlwaysOnTop(true);
		try
		{
			UserLoginInfoStorage.load();
			jbInit();
			applyUserLoginInfo();

			this.setIconImage(TradingConsole.get_TraderImage());

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
			//this.setIconImage(TradingConsole.get_TraderImage());
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	private void applyUserLoginInfo()
	{
		this.remberUserName.setSelected(UserLoginInfoStorage.get_saveUserName());
		this.remberPassword.setSelected(UserLoginInfoStorage.get_savePassword());
		this.userIdEdit.setText(UserLoginInfoStorage.get_UserName());
		this.passwordPassword.setText(UserLoginInfoStorage.get_Password());
		this.remberPassword.setEnabled(this.remberUserName.isSelected());
	}

	@Override
	public void show()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "Before show Login form");
		super.show();
	}

	public LoginForm(TradingConsole owner,boolean isRecover)
	{
		this( (JFrame) (owner.get_MainForm()));
		this._owner = owner;
		this.isRecover = isRecover;
		Settings.setWaitTimeout(this._editingServiceManager.getWaitTimeout());
		this.serverSettingInitalize();

		//this.setTitle(Login.loginFormTitle);
		if (PublicParametersManager.version.equals("JPN"))
		{
			this.setTitle(this._owner.get_ServiceManager().getSelectedHostName() + " " + Login.loginFormTitle);
		}
		else
		{
			this.setTitle(Login.loginFormTitle + " " + this._owner.get_ServiceManager().getSelectedHostName());
		}



		this.languageStaticText.setText(Language.optionFormLanguageLabel);
		ItemListener itemListener = new ItemListener()
		{
			private String priviousLanguage = null;
			public void itemStateChanged(ItemEvent e)
			{
				String currentLanguage = languageChoice.getSelectedItem().toString();
				if(languageChoice.getSelectedIndex() == 1) currentLanguage = "CHT";
				if(languageChoice.getSelectedIndex() == 2) currentLanguage = "CHS";
				if(languageChoice.getSelectedIndex() == 3) currentLanguage = "JPN";
				if (!StringHelper.isNullOrEmpty(currentLanguage))
				{
					if(StringHelper.isNullOrEmpty(priviousLanguage) || !currentLanguage.equals(priviousLanguage))
					{
						PublicParametersManager.version = currentLanguage;
						if(!PublicParametersManager.saveVersion())
						{
							_owner.messageNotify(Language.FailToSaveSettings, false);
						}
						applyLanguage();

						priviousLanguage = currentLanguage;
					}
				}
			}
		};
		this.languageChoice.addItemListener(itemListener);
		String currentLanguage = languageChoice.getSelectedItem().toString();
		if(languageChoice.getSelectedIndex() == 1) currentLanguage = "CHT";
		if(languageChoice.getSelectedIndex() == 2) currentLanguage = "CHS";
		if(languageChoice.getSelectedIndex() == 3) currentLanguage = "JPN";
		if(currentLanguage.equals(PublicParametersManager.version))
		{
			this.applyLanguage();
		}
		else
		{
			if(PublicParametersManager.version.compareToIgnoreCase("CHT") == 0)
			{
				this.languageChoice.setSelectedItem("繁體中文");
			}
			else if(PublicParametersManager.version.compareToIgnoreCase("CHS") == 0)
			{
				this.languageChoice.setSelectedItem("简体中文");
			}
			else if(PublicParametersManager.version.compareToIgnoreCase("JPN") == 0)
			{
				this.languageChoice.setSelectedItem("日本語");
			}
			else
			{
				this.languageChoice.setSelectedItem(PublicParametersManager.version);
			}
		}

		this.headerStaticText.setText(Login.lblSystemLogon);
		this.userIdStaticText.setText(Login.lblUserName);
		this.passwordStaticText.setText(Login.lblPasswords);
		this.submitButton.setText(Login.btnOk);
		this.forgetPasswordLinkButton.setText(Login.ForgetPasswordLink);
		this.forgetPasswordLinkButton.setVisible(!StringHelper.isNullOrEmpty(this._owner.get_ServiceManager().getSelectedForgetPasswordLink()));
		//this.forgetPasswordLinkButton.setVisible(false);

		//   0 - text string is not modified.
		//   1 - text string is converted to the upper case.
		//   2 - text string is converted to the lower case.
		//   Default value is 0.
		//BUT DEFAULT is 2!!!

		this.passwordPassword.setEchoChar('*');

		this.submitButton.setFont(new Font("SansSerif", Font.BOLD, 12));
		this.forgetPasswordLinkButton.setFont(new Font("SansSerif", Font.BOLD, 10));

		this.caseSensitiveStaticText.setText(Login.lblCaseSensitive);
		this.attentionMultiTextArea.setEnabled(true);

		this.attention();

		String userId = System.getProperty("UserId");
		if (!StringHelper.isNullOrEmpty(userId))
		{
			this.userIdEdit.setText(userId);
		}
		String password = System.getProperty("Password");
		if (!StringHelper.isNullOrEmpty(password))
		{
			this.passwordPassword.setText(password);
		}
		this.userIdEdit.requestFocus();
		this.userIdEdit.select(0, this.userIdEdit.getText().length());
	}



	private void optionTab_actionPerformed()
	{
		this.hostChoice_OnChange();
	}

	private void hostChoice_OnChange()
	{
		int selectedServiceHostSequence = this.hostChoice.getSelectedIndex();
		this.hostEdit.setEnabled(selectedServiceHostSequence == 0);
		this.hostEdit.setVisible(selectedServiceHostSequence == 0);
		String host = this._editingServiceManager.getSelectedHost(selectedServiceHostSequence);
		this.forgetPasswordLinkButton.setVisible(!StringHelper.isNullOrEmpty(this._editingServiceManager.getForgetPasswordLink(selectedServiceHostSequence)));
		this.hostEdit.setText(host);
		this.setEditingTitle();
		this.hostEdit.requestFocus();
		this.doLayout();
		this.doLayout();
		this.updateSubmitButtonStatus();
	}

	private void hostEdit_keyPressed()
	{
		this.setEditingTitle();
		this.updateSubmitButtonStatus();
		this.forgetPasswordLinkButton.setVisible(!StringHelper.isNullOrEmpty(this.hostEdit.getText())
			&& !StringHelper.isNullOrEmpty(this._editingServiceManager.get_ForgetPasswordLink()));
	}

	private void updateSubmitButtonStatus()
	{
		int selectedServiceHostSequence = this.hostChoice.getSelectedIndex();
		this.submitButton.setEnabled((selectedServiceHostSequence != 0 || !StringHelper.isNullOrEmpty(this.hostEdit.getText().trim()))
			&& !StringHelper.isNullOrEmpty(this.userIdEdit.getText()) && !StringHelper.isNullOrEmpty(this.passwordPassword.getText()));
		if(this.submitButton.isEnabled())
		{
			this.visibleOkProcess();
		}
	}

	private void setEditingTitle()
	{
		if (this.hostChoice.getSelectedIndex() == 0)
		{
			if (PublicParametersManager.version.equals("JPN"))
			{
				this.setTitle(this.hostEdit.getText() + " " + Login.loginFormTitle);
			}
			else
			{
				this.setTitle(Login.loginFormTitle + " " + this.hostEdit.getText());
			}
		}
		else
		{
			if (PublicParametersManager.version.equals("JPN"))
			{
				this.setTitle(this.hostChoice.getSelectedItem() + " " + Login.loginFormTitle);
			}
			else
			{
				this.setTitle(Login.loginFormTitle + " " + this.hostChoice.getSelectedItem());
			}
		}
	}

	private void serverSettingInitalize()
	{
		this.hostChoice_OnChange();

		this.hostStaticText.setText(Language.host);
		//this.autoUpdateListenPortStaticText.setText(Language.autoUpdateListenPort);
		//this.connectionMessageStaticText.setText(Language.serverChangeNotify);
		this.hostChoice.setEditable(false);
		//this.hostChoice.removeAllItems();
		//this.hostRange.setText("(1025-65534)");
		String userDefinedHost = this._editingServiceManager.getSelectedHost(0);
		this.hostChoice.addItem(Language.userDefineHost);
		/*if (StringHelper.isNullOrEmpty(userDefinedHost))
		{
			this.hostChoice.addItem(Language.userDefineHost);
		}
		else
		{
			this.hostChoice.addItem(userDefinedHost);
		}*/

		if (this._editingServiceManager.get_TotalService() >= 1)
		{
			this.hostChoice.addItem(Language.serviceHost1);
		}
		if (this._editingServiceManager.get_TotalService() >= 2)
		{
			this.hostChoice.addItem(Language.serviceHost2);
		}
		if (this._editingServiceManager.get_TotalService() >= 3)
		{
			this.hostChoice.addItem(Language.serviceHost3);
		}
		if (this._editingServiceManager.get_TotalService() >= 4)
		{
			this.hostChoice.addItem(Language.serviceHost4);
		}
		if (this._editingServiceManager.get_TotalService() >= 5)
		{
			this.hostChoice.addItem(Language.serviceHost5);
		}
		if (this._editingServiceManager.get_TotalService() >= 6)
		{
			this.hostChoice.addItem(Language.serviceHost6);
		}
		int selectedServiceHostSequence = this._editingServiceManager.get_SelectedServiceHostSequence();
		this.hostChoice.setSelectedIndex(selectedServiceHostSequence);
		this.hostEdit.setEnabled(selectedServiceHostSequence == 0);
		this.hostEdit.setVisible(selectedServiceHostSequence == 0);
		this.hostEdit.setText(this._editingServiceManager.getSelectedHost(selectedServiceHostSequence));
		//this.autoUpdateListenPortNumeric.setText(XmlConvert.toString(this._editingServiceManager.get_AutoUpdateListenPort()));
	}

	private void visibleOkProcess()
	{
		if (Login.getRiskDisclosureStatementType().equals(RiskDisclosureStatementType.Special))
		{
			this.submitButton.setEnabled(this.visibleOkCheckbox.isSelected());
		}
		else
		{
			this.submitButton.setEnabled(true);
		}
	}

	private void forgetPasswordLink()
	{
		int selectedServiceHostSequence = this.hostChoice.getSelectedIndex();
		String forgetPasswordLink = this._owner.get_ServiceManager().getForgetPasswordLink(selectedServiceHostSequence);
		String passwordHost = this.hostEdit.getText();
		if(selectedServiceHostSequence == 0)
		{
			forgetPasswordLink =  this._owner.get_ServiceManager().get_ForgetPasswordLink();
			forgetPasswordLink = AppToolkit.changeUrlHost(forgetPasswordLink, passwordHost);
		}
		if (!StringHelper.isNullOrEmpty(forgetPasswordLink))
		{
			forgetPasswordLink += ( (forgetPasswordLink.indexOf("?") > 0) ? "&" : "?") + "Language=" + PublicParametersManager.version;

			BrowserControl.displayURL(forgetPasswordLink, true);
		}
	}

	private boolean isAcceptedOrganization(String organizationName)
	{
		boolean isAcceptedOrganization = false;
		if(StringHelper.isNullOrEmpty(ServiceTimeoutSetting.acceptOrganizations))
		{
			isAcceptedOrganization = true;
		}
		else
		{
			String[] acceptOrganizations = StringHelper.split(ServiceTimeoutSetting.acceptOrganizations, ",");
			for(String item : acceptOrganizations)
			{
				if(item.compareToIgnoreCase(organizationName) == 0)
				{
					isAcceptedOrganization = true;
					break;
				}
			}
		}
		return isAcceptedOrganization;
	}



	private int loginFailedCount = 0;

	private void connect() throws TcpInializeTimeoutException
	{
		if(!this.isRecover){
			try{
				this._owner.connectHelper();
			}
			catch(TcpInializeTimeoutException tcpInitializeException){
				throw tcpInitializeException;
			}
			catch(Exception ex){
				this._owner.disconnect(this.isRecover);
			}
		}

	}

	private void submit()
	{
		TradingConsole.traceSource.trace(TraceType.Information, "Login-SaveHostSetting");
		boolean isSucceed = true;
		int selectedServiceHostSequence = this.hostChoice.getSelectedIndex();
		String host
			= selectedServiceHostSequence == 0 ? this.hostEdit.getText() : this._editingServiceManager.getSelectedHost(selectedServiceHostSequence);
		String passwordHost
			= selectedServiceHostSequence == 0 ? this.hostEdit.getText() : this._editingServiceManager.getForgetPasswordLink(selectedServiceHostSequence);

		if (AppToolkit.isUrl(passwordHost))
		{
			passwordHost = AppToolkit.getHost(passwordHost);
		}
		Settings.setHostName(host);
		Settings.setPort(this._editingServiceManager.getMapPort());
		isSucceed = ServiceManager.saveServerSetting(this.hostChoice.getSelectedIndex(), host, passwordHost);
		if (!isSucceed)
		{
			this.messageStaticText.setText(Language.updateServiceFailed);
			TradingConsole.traceSource.trace(TraceType.Information, "Login-SaveHostSetting is Fail!");
			return;
		}

		this._owner.setServiceManager();
		TradingConsole.traceSource.trace(TraceType.Information, "Login-SaveHostSetting is OK!");

		TradingConsole.traceSource.trace(TraceType.Information, "Login-Submit");

		String loginName = this.userIdEdit.getText();
		if (StringHelper.isNullOrEmpty(loginName))
		{
			this.messageStaticText.setText(Login.lblLoginPrompt1);
			return;
		}
		this.saveUserLoingInfo();

		try
		{
			this.connect();
			int usingBackupSettingsIndex = 0;
			while (true)
			{

				TradingConsole.traceSource.trace(TraceType.Information, "Call Service Method: Login-Begin");

				String environmentInfo = AppToolkit.getBriefSystemInfo() + "; " + PublicParametersManager.version;
				String localIPAddress = AppToolkit.getLocalIPAddress();
				if (!StringHelper.isNullOrEmpty(localIPAddress))
					environmentInfo = localIPAddress + "; " + environmentInfo;

				String password = new String(this.passwordPassword.getPassword());

				LoginResult result = this._owner.get_TradingConsoleServer().loginForJava(loginName, password, environmentInfo, PacketContants.AppType);
				TradingConsole.traceSource.trace(TraceType.Information, "Call Service Method: Login-End");
				if (result != null)
				{
					Guid customerID = result.get_UserId();
					if (!customerID.equals(Guid.empty) && this.isAcceptedOrganization(result.get_CompanyName()))
					{
						AppToolkit.setDirectory(customerID);

						this.setVisible(false);

						if (!this._owner.get_TradingConsoleServer().start())
						{
							AlertDialogForm.showDialog(this, null, true, Login.lblLoginPrompt23);
							return;
						}

						if (this._owner.getConnectionManager().getAsyncManager() != null)
						{
							this._owner.getConnectionManager().getAsyncManager().setSlidingWindow(this._owner.get_TradingConsoleServer().get_SlidingWindow());
						}
						TradingConsole.traceSource.trace(TraceType.Information, "call loggedIn-Begin");

						//TradingConsole.traceSource.trace(TraceType.Information, "Parameter: " + result.get_Parameter().get_InnerText());

						this._owner.get_LoginInformation().loggedIn(customerID, result.get_CompanyName(), loginName, TradingConsoleServer.appTime());
						this._owner.loggedIn(result);
						LoginInfoManager.Default.setLoginId(loginName);
						LoginInfoManager.Default.setPassword(password);
						LoginInfoManager.Default.setVersion(environmentInfo);
						TradingConsole.traceSource.trace(TraceType.Warning, "call loggedIn-End");

						this.dispose();
					}
					else
					{
						this.passwordPassword.setText("");
						String message = (!result.get_DisallowLogin()) ? Login.lblLoginPrompt2 : Login.lblLoginPrompt22;
						if (result.get_Parameter() != null && result.get_Parameter().get_OuterXml().indexOf("ExceedMaxRetryLimit") > 0)
						{
							AlertDialogForm.showDialog(this, null, true, Login.lblLoginPrompt24);
						}
						else
						{
							this.messageStaticText.setText(message);
						}

						if (++loginFailedCount >= LoginForm.maxTryCount
							/*|| JOptionPane.showConfirmDialog(this, message, Login.LoginFailed, JOptionPane. YES_NO_OPTION) == 1*/
							)
						{
							Runtime.getRuntime().exit( -2);
							return;
						}
					}
					break;
				}
				else
				{
					if (usingBackupSettingsIndex < this._owner.get_ServiceManager().getBackupServerNumber()
						&& this._owner.get_ServiceManager().tryToUsingBackupSettings(usingBackupSettingsIndex))
					{
						String backupHostName = this._owner.get_ServiceManager().get_BackupHostName(usingBackupSettingsIndex);
						if (PublicParametersManager.version.equals("JPN"))
						{
							this.setTitle(backupHostName + " " + Login.loginFormTitle);
						}
						else
						{
							this.setTitle(Login.loginFormTitle + " " + backupHostName);
						}

						usingBackupSettingsIndex++;
						continue;
					}
					else
					{
						this.passwordPassword.setText("");
						this.messageStaticText.setText(Login.lblLoginPrompt23);
						this.setVisible(true);
						break;
					}
				}
			}
		}
		catch (Throwable exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
			this._owner.disconnect(false);
			this.passwordPassword.setText("");
			this.messageStaticText.setText(Login.lblLoginPrompt2);
			this.setVisible(true);
		}
	}

	private void saveUserLoingInfo()
	{
		String userName = this.userIdEdit.getText();
		boolean saveUserName = this.remberUserName.isSelected();
		String password = new String(this.passwordPassword.getPassword());
		boolean savePassword = this.remberPassword.isSelected();
		UserLoginInfoStorage.save(userName, saveUserName, password, savePassword);
	}

	private void showOptionForm()
	{
		OptionForm optionForm = new OptionForm(this, this._owner, this._owner.get_SettingsManager(), 1);
		optionForm.toFront();
		optionForm.show();
	}

	//SourceCode End//////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		this.setSize(375, 420);
		this.setLayout(gridBagLayout1);
		this.setResizable(false);
		this.setBackground(FormBackColor.loginForm);

		this.submitButton.setDefaultCapable(true);
		this.userIdEdit.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				updateSubmitButtonStatus();
			}

			public void keyReleased(KeyEvent e)
			{
				updateSubmitButtonStatus();
			}
		});

		this.passwordPassword.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				updateSubmitButtonStatus();
			}

			public void keyReleased(KeyEvent e)
			{
				updateSubmitButtonStatus();
			}
		});

		Font headerFont = new Font("SansSerif", Font.BOLD, 12);
		headerStaticText.setFont(headerFont);
		headerStaticText.setAlignment(1);
		headerStaticText.setOdometer(4);
		headerStaticText.setText("SYSTEM LOGIN");

		userIdStaticText.setText("User Name:");
		//userIdStaticText.setAlignment(0);
		userIdStaticText.setFont(headerFont);
		//userIdStaticText.setBorderColor(new Color(134, 134, 134));
		passwordStaticText.setText("Password:");
		//passwordStaticText.setHorizontalAlignment(SwingConstants.LEFT);
		passwordStaticText.setFont(headerFont);
		remberUserName.setFont(new Font("SansSerif", Font.PLAIN, 12));
		remberPassword.setFont(new Font("SansSerif", Font.PLAIN, 12));

		messageStaticText.setText("");
		messageStaticText.setForeground(Color.red);
		messageStaticText.setAlignment(1);

		passwordPassword.setText(UserLoginInfoStorage.get_Password());
		passwordPassword.addFocusListener(new LoginForm_passwordPassword_focusAdapter(this));
		passwordPassword.addKeyListener(new LoginForm_passwordPassword_keyAdapter(this));
		userIdEdit.setText(UserLoginInfoStorage.get_UserName());
		submitButton.setText("OK");
		submitButton.addActionListener(new Login_submitButton_actionAdapter(this));

		caseSensitiveStaticText.setForeground(Color.red);
		caseSensitiveStaticText.setFont(new java.awt.Font("SansSerif", Font.ITALIC, 10));
		caseSensitiveStaticText.setText("* passwords are case-sensitive");
		caseSensitiveStaticText.setAlignment(1);

		visibleOkCheckbox.setText("I Agree");
		visibleOkCheckbox.addItemListener(new LoginForm_visibleOkCheckbox_itemAdapter(this));
		//hostStaticText.setBounds(new Rectangle(8, 32, 130, 10));
		hostStaticText.setText("host");

		//hostChoice.addKeyListener(new LoginForm_hostChoice_keyAdapter(this));
		hostChoice.setFont(new java.awt.Font("SansSerif", Font.PLAIN, 12));
		//hostChoice.setBounds(new Rectangle(143, 33, 165, 20));
		hostChoice.addItemListener(new LoginForm_hostChoice_itemAdapter(this));
		forgetPasswordLinkButton.setText("forgotten Password");
		forgetPasswordLinkButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		forgetPasswordLinkButton.setBounds(new Rectangle(0, 32, 120, 10));
		forgetPasswordLinkButton.addActionListener(new Login_forgetPasswordLinkButton_actionAdapter(this));
		attentionMultiTextArea.setColumns(20);

		hostEdit.addKeyListener(new LoginForm_hostEdit_keyAdapter(this));
		JPanel panel = new JPanel();
		proxyPanel.add(languageStaticText, new GridBagConstraints2(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		proxyPanel.add(languageChoice, new GridBagConstraints2(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 0, 0), 0, 0));

		proxyPanel.add(hostStaticText, new GridBagConstraints2(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
		proxyPanel.add(hostChoice, new GridBagConstraints2(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 2, 0, 0), 0, 0));
		proxyPanel.add(hostEdit, new GridBagConstraints2(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 0), 0, 0));
		proxyPanel.add(panel, new GridBagConstraints2(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 0, 0, 0), 0, 30));

		UISetting.fillMultiLanguage(this.languageChoice);
		optionTab.setFont(new java.awt.Font("SansSerif", Font.BOLD, 12));
		//optionTab.setPreferredSize(new Dimension(397, 525));

		this.add(headerStaticText, new GridBagConstraints2(1, 0, 3, 1, 1.0, 0.0
			, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));

		userIdStaticText.setAlignment(SwingConstants.LEFT);
		this.add(userIdStaticText, new GridBagConstraints2(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(10, 20, 0, 0), 0, 8));
		this.add(userIdEdit, new GridBagConstraints2(2, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 50, 0, 0), 120, 0));

		passwordStaticText.setAlignment(SwingConstants.LEFT);
		this.add(passwordStaticText, new GridBagConstraints2(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 20, 2, 0), 0, 0));
		this.add(passwordPassword, new GridBagConstraints2(2, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 50, 0, 0), 120, 0));

		this.add(forgetPasswordLinkButton, new GridBagConstraints2(3, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 2, 5), 80, 4));

		this.add(caseSensitiveStaticText, new GridBagConstraints2(1, 3, 3, 1, 1.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));

		Panel panel2 = new Panel(new GridBagLayout());
		this.add(panel2, new GridBagConstraints2(1, 4, 3, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 8));

		panel2.add(remberUserName, new GridBagConstraints2(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 40, 0, 0), 0, 8));
		remberUserName.setText(Language.rememberMe);

		panel2.add(remberPassword, new GridBagConstraints2(2, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 30, 0, 40), 0, 8));
		remberPassword.setText(Language.rememberPassword);
		remberPassword.setEnabled(false);

		this.add(submitButton, new GridBagConstraints2(1, 8, 3, 1, 0.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(8, 10, 2, 10), 60, 0));
		this.add(messageStaticText, new GridBagConstraints2(1, 9, 3, 1, 1.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(1, 10, 2, 10), 0, 0));

		this.add(optionTab, new GridBagConstraints2(1, 10, 3, 1, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 2, 1, 2), 0, 0));

		this.add(visibleOkCheckbox, new GridBagConstraints2(1, 5, 3, 1, 1.0, 0.0
			, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 20, 0, 0), 0, 0));

		attentionMultiTextArea.setLineWrap(true);
		attentionMultiTextArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(attentionMultiTextArea);
		attentionPanel.setLayout(new BorderLayout());
		attentionPanel.add(scrollPane, BorderLayout.CENTER);
		optionTab.addTab(Login.lblAttention, null, attentionPanel, Login.lblAttention);
		optionTab.addTab(Language.optionFormTitle, null, proxyPanel, Language.optionFormTitle);

		this.addWindowListener(new Login_this_windowAdapter(this));
		remberPassword.addActionListener(new ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent e)
			{
				handleRember(remberPassword);
			}
		});

		remberUserName.addActionListener(new ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent e)
			{
				handleRember(remberUserName);
			}
		});
	}

	private void handleRember(JCheckBox sender)
	{
		if(sender == this.remberUserName)
		{
			this.remberPassword.setEnabled(sender.isSelected());
			if(!sender.isSelected()) this.remberPassword.setSelected(false);
		}
	}

	private void applyLanguage()
	{
		Language.initialize();
		//this._owner.get_MainForm().applyLanguage();

		this.languageStaticText.setText(Language.optionFormLanguageLabel);
		this.hostStaticText.setText(Language.host);

		Login.initialize();
		this.hostChoice.removeAllItems();
		this.serverSettingInitalize();
		this.caseSensitiveStaticText.setText(Login.lblCaseSensitive);
		this.headerStaticText.setText(Login.lblSystemLogon);
		this.userIdStaticText.setText(Login.lblUserName);
		this.passwordStaticText.setText(Login.lblPasswords);
		this.submitButton.setText(Login.btnOk);
		this.forgetPasswordLinkButton.setText(Login.ForgetPasswordLink);
		optionTab.setTitleAt(0, Login.lblAttention);
		optionTab.setToolTipTextAt(0, Login.lblAttention);

		optionTab.setTitleAt(1, Language.optionFormTitle);
		optionTab.setToolTipTextAt(1, Language.optionFormTitle);

		remberUserName.setText(Language.rememberMe);
		remberPassword.setText(Language.rememberPassword);

		this.attention();
	}

	private void attention()
	{
		if (Login.getRiskDisclosureStatementType().equals(RiskDisclosureStatementType.Special))
		{
			if (Login.endUser.startsWith("\\n"))
			{
				this.attentionMultiTextArea.setText(Login.endUser.substring(2));
			}
			else
			{
				this.attentionMultiTextArea.setText(Login.endUser);
			}

			this.visibleOkCheckbox.setLabel(Login.agreeCheckBoxCaption);
			this.visibleOkCheckbox.setFont(new Font("SansSerif", Font.PLAIN, 10));
			this.visibleOkCheckbox.setVisible(true);
		}
		else
		{
			if (Login.lblDescription.startsWith("\\n"))
			{
				this.attentionMultiTextArea.setText(Login.lblDescription.substring(2));
			}
			else
			{
				this.attentionMultiTextArea.setText(Login.lblDescription);
			}
			this.visibleOkCheckbox.setVisible(false);
		}
		this.attentionMultiTextArea.setCaretPosition(0);
		this.visibleOkProcess();
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	public void this_windowOpened(WindowEvent e)
	{
		this.userIdEdit.requestFocus();
		this.userIdEdit.select(0, this.userIdEdit.getText().length());
	}

	JTabbedPane optionTab = new JTabbedPane();
	JPanel proxyPanel = new JPanel(new GridBagLayout());
	JPanel attentionPanel = new JPanel();
	PVStaticText2 headerStaticText = new PVStaticText2();
	PVStaticText2 userIdStaticText = new PVStaticText2();
	PVStaticText2 passwordStaticText = new PVStaticText2();
	PVStaticText2 messageStaticText = new PVStaticText2();
	JPasswordField passwordPassword = new JPasswordField();
	JTextField userIdEdit = new JTextField();
	PVButton2 submitButton = new PVButton2();
	PVStaticText2 caseSensitiveStaticText = new PVStaticText2();
	JTextArea attentionMultiTextArea = new JTextArea("", 8, 1);
	JCheckBox visibleOkCheckbox = new JCheckBox();
	java.awt.GridBagLayout gridBagLayout1 = new GridBagLayout();
	private PVStaticText2 hostStaticText = new PVStaticText2();
	private JComboBox hostChoice = new JComboBox();
	private PVButton2 forgetPasswordLinkButton = new PVButton2();
	private JTextField hostEdit = new JTextField();
	private PVStaticText2 languageStaticText = new PVStaticText2();
	private JAdvancedComboBox languageChoice = new JAdvancedComboBox();
	private JCheckBox remberUserName = new JCheckBox();
	private JCheckBox remberPassword = new JCheckBox();

	//private PVStaticText2 hostRange = new PVStaticText2();
	//private PVStaticText2 autoUpdateListenPortStaticText = new PVStaticText2();
	//private PVNumeric autoUpdateListenPortNumeric = new PVNumeric();
	//private PVButton2 agreementButton = new PVButton2();
	public void submitButton_actionPerformed(java.awt.event.ActionEvent e)
	{
		this.submit();
	}

	public void hostChoice_keyPressed(KeyEvent e)
	{
		this.hostEdit_keyPressed();
	}

	public void forgetPasswordLinkButton_actionPerformed(java.awt.event.ActionEvent e)
	{
		this.forgetPasswordLink();
	}

	public void cancelButton_actionPerformed(java.awt.event.ActionEvent e)
	{
		this.dispose();
	}

	public void visibleOkCheckbox_itemStateChanged(ItemEvent e)
	{
		this.visibleOkProcess();
	}

	public void passwordPassword_keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == e.VK_ENTER || e.getID() == e.VK_TAB)
		{
			this.submitButton.requestFocus();
		}
	}

	public void passwordPassword_focusLost(FocusEvent e)
	{
		//this.submitButton.requestFocus();
	}

	/*
	 public void hostChoice_actionPerformed(ActionEvent e)
	 {
	  if (e.getID() == PVChoice.EDIT_UPDATED)
	  {
	   this.hostChoice_OnChange();
	  }
	 }
	 */
	public void hostChoice_itemStateChanged(ItemEvent e)
	{
		this.hostChoice_OnChange();
	}

	public void optionTab_actionPerformed(ActionEvent e)
	{
		this.optionTab_actionPerformed();
	}

	class Login_this_windowAdapter extends WindowAdapter
	{
		private LoginForm adaptee;
		Login_this_windowAdapter(LoginForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}

		public void windowOpened(WindowEvent e)
		{
			adaptee.this_windowOpened(e);
		}
	}
}

class LoginForm_hostChoice_itemAdapter implements ItemListener
{
	private LoginForm adaptee;
	LoginForm_hostChoice_itemAdapter(LoginForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.hostChoice_itemStateChanged(e);
	}
}

/*
 class LoginForm_hostChoice_actionAdapter implements ItemListener// implements ActionListener
 {
 private LoginForm adaptee;
 LoginForm_hostChoice_actionAdapter(LoginForm adaptee)
 {
  this.adaptee = adaptee;
 }

 public void actionPerformed(ActionEvent e)
 {
  adaptee.hostChoice_actionPerformed(e);
 }
 }
 */
class LoginForm_passwordPassword_focusAdapter extends FocusAdapter
{
	private LoginForm adaptee;
	LoginForm_passwordPassword_focusAdapter(LoginForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void focusLost(FocusEvent e)
	{
		adaptee.passwordPassword_focusLost(e);
	}
}

class LoginForm_passwordPassword_keyAdapter extends KeyAdapter
{
	private LoginForm adaptee;
	LoginForm_passwordPassword_keyAdapter(LoginForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e)
	{
		adaptee.passwordPassword_keyPressed(e);
	}
}

class LoginForm_visibleOkCheckbox_itemAdapter implements ItemListener
{
	private LoginForm adaptee;
	LoginForm_visibleOkCheckbox_itemAdapter(LoginForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void itemStateChanged(ItemEvent e)
	{
		adaptee.visibleOkCheckbox_itemStateChanged(e);
	}
}

class Login_submitButton_actionAdapter implements ActionListener
{
	private LoginForm adaptee;
	Login_submitButton_actionAdapter(LoginForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}

class Login_forgetPasswordLinkButton_actionAdapter implements ActionListener
{
	private LoginForm adaptee;
	Login_forgetPasswordLinkButton_actionAdapter(LoginForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		adaptee.forgetPasswordLinkButton_actionPerformed(e);
	}
}

class LoginForm_hostEdit_keyAdapter extends KeyAdapter
{
	private LoginForm adaptee;
	LoginForm_hostEdit_keyAdapter(LoginForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent e)
	{
		adaptee.hostChoice_keyPressed(e);
	}

	public void keyReleased(KeyEvent e)
	{
		adaptee.hostChoice_keyPressed(e);
	}
}
