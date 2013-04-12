package tradingConsole.ui;

import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.Dialog;
import java.awt.*;
import java.awt.event.ActionListener;

import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.TradingConsole;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import tradingConsole.AppToolkit;
import tradingConsole.ui.language.Login;
import framework.diagnostics.TraceType;
import framework.StringHelper;
import tradingConsole.service.TradingAccountLoginResult;
import tradingConsole.settings.TradingAccount;
import tradingConsole.settings.SettingsManager;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import tradingConsole.ui.grid.DataGrid;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import tradingConsole.ui.grid.IActionListener;
import javax.swing.WindowConstants;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.KeyListener;

public class TradingAccountForm extends JDialog //Frame
{
	private TradingConsole _owner;
	private SettingsManager _settingsManager;
	private TradingAccount _activatingAccount;

/*
	 public TradingAccountForm()
		{
	   super();
	  try
	  {
	   jbInit();

	  }
	  catch (Throwable exception)
	  {
	   exception.printStackTrace();
	  }

		}
*/

	public TradingAccountForm(JFrame parent)
	{
		super(parent, true);
		try
		{
			jbInit();

			Rectangle rectangle = AppToolkit.getRectangleByDimension(this.getSize());
			this.setBounds(rectangle);
		}
		catch (Throwable exception)
		{
			exception.printStackTrace();
		}
	}

	public TradingAccountForm(JFrame parent, TradingConsole tradingConsole,SettingsManager settingsManager)
	{
		this(parent);

		this._owner = tradingConsole;
		this._settingsManager = settingsManager;

		this.notifyStaticText.setText(Login.TradingAccountNotify);
		this.loginNameStaticText.setText(Login.lblUserName);
		this.passwordStaticText.setText(Login.lblPasswords);
		this.messageStaticText.setText("");
		this.okButton.setText(Login.TradingAccountActive);

		this._owner.get_TradingAccountManager().initialize(this.tradingAccountTable, true, false);
		//this.tradingAccountTable.setSelectedRow(0);
		//this.tradingAccountTable.requestFocus();

		this.tradingAccountTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.tradingAccountTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				int row = tradingAccountTable.getSelectedRow();
				TradingAccount tradingAccount = (TradingAccount)tradingAccountTable.getObject(row);
				setActivingAccount(tradingAccount);
			}
		}
		);

		this.tradingAccountTable.getSelectionModel().setSelectionInterval(0,0);

		/*this._activatingAccount = (TradingAccount)this.tradingAccountTable.get_BindingSource().getObject(0);
		boolean isActivated = this._activatingAccount.get_IsActivated();
		this.setEnabledLogin(!isActivated);
		this.setTradingAccountActiveLoginTitle();*/
	}

	private void setTradingAccountActiveLoginTitle()
	{
		this.loginStaticText.setText(Login.TradingAccountActiveLogin + " " + this._activatingAccount.get_Code());
	}

	private void submit()
	{
		this.messageStaticText.setText("");
		if (this._activatingAccount == null)
		{
			this.setEnabledLogin(false);
			this.messageStaticText.setText(Login.TradingAccountNotSelect);
			return;
		}

		if (this._activatingAccount.get_IsActivated())
		{
			this.setEnabledLogin(false);
			this.messageStaticText.setText(Login.TradingAccountHasActivated);
			if(!this.selectNextUnactiveAccount()) this.dispose();
			return;
		}

		TradingConsole.traceSource.trace(TraceType.Information, "TradingAccountForm-Submit");
		String loginName = this.userIdEdit.getText();
		if (StringHelper.isNullOrEmpty(loginName))
		{
			this.messageStaticText.setText(Login.lblLoginPrompt1);
			return;
		}

		try
		{
			TradingConsole.traceSource.trace(TraceType.Information, "Call Service Method: TradingAccountForm-Begin, loginName: " + loginName);
			TradingAccountLoginResult result = this._owner.get_TradingConsoleServer().activeAccountLogin(this._activatingAccount.get_CustomerId(), loginName,
				new String(this.passwordPassword.getPassword()));
			TradingConsole.traceSource.trace(TraceType.Information, "Call Service Method: TradingAccountForm-End, loginName: " + loginName);
			if (result != null && result.get_UserId().compareTo(framework.Guid.empty) != 0)
			{
				this._owner.get_TradingAccountManager().activeAccount(result.get_UserId());
				this.messageStaticText.setText(Login.LoginSucceed);

				String key = this.tradingAccountTable.get_BindingSource().get_DataSourceKey();
				try
				{
					this._activatingAccount.update(key);
				}
				catch(Exception exception)
				{
					//Can't find why excepton throwed, just ignore temporarily
					TradingConsole.traceSource.trace(TraceType.Warning, exception);
				}

				this._settingsManager.activeAgentedAccount(result.get_UserId());

				if(!this.selectNextUnactiveAccount()) this.dispose();
			}
			else
			{
				this.passwordPassword.setText("");
				this.messageStaticText.setText(Login.lblLoginPrompt2);
			}
		}
		catch (Throwable exception)
		{
			this.passwordPassword.setText("");
			this.messageStaticText.setText(Login.lblLoginPrompt2);
		}
		//this.doLayout();
	}

	private boolean selectNextUnactiveAccount()
	{
		for(int row = 0; row < this.tradingAccountTable.getRowCount(); row++)
		{
			TradingAccount tradingAccount = (TradingAccount)this.tradingAccountTable.getObject(row);
			if(!tradingAccount.get_IsActivated())
			{
				this.tradingAccountTable.getSelectionModel().setSelectionInterval(row,row);
				return true;
			}
		}
		return false;
	}

	private void processTradingAccountTable(tradingConsole.ui.grid.ActionEvent e)
	{
		TradingAccount tradingAccount = (TradingAccount)e.get_Object();
		this.setActivingAccount(tradingAccount);
	}

	private void setActivingAccount(TradingAccount tradingAccount)
	{
		if (this._activatingAccount == null || this._activatingAccount.get_Id().compareTo(tradingAccount.get_Id()) != 0)
		{
			this._activatingAccount = tradingAccount;

			this.userIdEdit.setText(this._activatingAccount.get_Code());
			this.passwordPassword.setText("");
			this.passwordPassword.requestFocus();
			this.messageStaticText.setText("");
		}
		this.setTradingAccountActiveLoginTitle();
		boolean isActivated = this._activatingAccount.get_IsActivated();
		this.setEnabledLogin(!isActivated);
		//this.doLayout();
	}

	private void setEnabledLogin(boolean enabled)
	{
		this.userIdEdit.setEnabled(enabled);
		this.passwordPassword.setEnabled(enabled);
		this.okButton.setEnabled(enabled);
	}
	//SourceCode End////////////////////////////////////////////////////////////////////////////////////////

	private void jbInit() throws Exception
	{
		//this.addWindowListener(new TradingAccountForm_this_windowAdapter(this));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		this.setSize(490, 280);
		this.setResizable(false);
		this.setLayout(gridBagLayout1);
		this.setBackground(FormBackColor.activateAccountForm);

		notifyStaticText.setText("Please activate your trading right by insert the account's user ID and passwords.");
		//tradingAccountTable.setLabel("pVTable1");
		tradingAccountTable.addActionListener(new TradingAccountForm_tradingAccountTable_actionAdapter(this));
		okButton.setText("Submit");
		okButton.addActionListener(new TradingAccountForm_okButton_actionAdapter(this));
		messageStaticText.setText("message");
		loginNameStaticText.setText("User Name:");
		loginNameStaticText.setAlignment(2);
		passwordStaticText.setText("Password:");
		passwordStaticText.setAlignment(2);
		loginStaticText.setFont(new java.awt.Font("SansSerif", Font.BOLD, 14));
		loginStaticText.setText("Login");
		loginStaticText.setOdometer(10);
		//passwordPassword.addKeyListener(new TradingAccountForm_passwordPassword_keyAdapter(this));
		JScrollPane scrollPane = new JScrollPane(tradingAccountTable);
		this.getContentPane().add(notifyStaticText, new GridBagConstraints(0, 0, 5, 1, 1.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(15, 10, 5, 2), 0, 0));
		this.getContentPane().add(messageStaticText, new GridBagConstraints(0, 8, 5, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 10, 5), 0, 0));
		this.getContentPane().add(loginStaticText, new GridBagConstraints(4, 1, 1, 1, 0.2, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 20, 2, 2), 0, 0));
		this.getContentPane().add(okButton, new GridBagConstraints(4, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 20, 2, 10), 5, 0));
		this.getContentPane().add(passwordPassword, new GridBagConstraints(4, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 20, 2, 0), 70, 0));
		this.getContentPane().add(passwordStaticText, new GridBagConstraints(4, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 20, 2, 2), 0, 0));
		this.getContentPane().add(userIdEdit, new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 2, 0), 70, 0));
		this.getContentPane().add(loginNameStaticText, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(20, 20, 2, 2), 0, 0));
		this.getContentPane().add(scrollPane, new GridBagConstraints(0, 1, 4, 7, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 10, 10, 10), 0, 0));
		this.passwordPassword.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == e.VK_ENTER || e.getID() == e.VK_TAB)
				{
					okButton.requestFocus();
				}
			}
		});

	}

	private PVStaticText2 notifyStaticText = new PVStaticText2();
	private DataGrid tradingAccountTable = new DataGrid("TradingAccountTable");
	private PVButton2 okButton = new PVButton2();
	private PVStaticText2 messageStaticText = new PVStaticText2();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private PVStaticText2 loginNameStaticText = new PVStaticText2();
	private JTextField userIdEdit = new JTextField();
	private PVStaticText2 passwordStaticText = new PVStaticText2();
	private PVStaticText2 loginStaticText = new PVStaticText2();
	private JPasswordField  passwordPassword = new JPasswordField();

	public void this_windowClosing(WindowEvent e)
	{
		//this._owner.enterMainForm2();
		//this._owner.get_MainForm().setMenuVisible();

		//this.dispose();
	}

	public void okButton_actionPerformed(ActionEvent e)
	{
		this.submit();
	}

	public void tradingAccountTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processTradingAccountTable(e);
	}

	/*public void passwordPassword_keyPressed(KeyEvent keyEvent)
	{
		if (keyEvent.getKeyCode() == keyEvent.VK_ENTER || keyEvent.getID() == keyEvent.VK_TAB)
		{
			this.okButton.requestFocus();
		}
	}*/

	class TradingAccountForm_this_windowAdapter extends WindowAdapter
	{
		private TradingAccountForm adaptee;
		TradingAccountForm_this_windowAdapter(TradingAccountForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

/*class TradingAccountForm_passwordPassword_keyAdapter extends KeyAdapter
{
	private TradingAccountForm adaptee;
	TradingAccountForm_passwordPassword_keyAdapter(TradingAccountForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void keyPressed(KeyEvent keyEvent)
	{
		adaptee.passwordPassword_keyPressed(keyEvent);
	}
}*/

class TradingAccountForm_okButton_actionAdapter implements ActionListener
{
	private TradingAccountForm adaptee;
	TradingAccountForm_okButton_actionAdapter(TradingAccountForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.okButton_actionPerformed(e);
	}
}

class TradingAccountForm_tradingAccountTable_actionAdapter implements IActionListener
{
	private TradingAccountForm adaptee;
	TradingAccountForm_tradingAccountTable_actionAdapter(TradingAccountForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.tradingAccountTable_actionPerformed(e);
	}
}
