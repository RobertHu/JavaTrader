package tradingConsole.ui;

import javax.swing.JFrame;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import framework.Guid;

import tradingConsole.TradingConsole;
import tradingConsole.settings.SettingsManager;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.columnKey.AccountLockColKey;
import tradingConsole.Account;
import tradingConsole.enumDefine.AccountType;
import tradingConsole.enumDefine.LockedStatus;
import tradingConsole.AppToolkit;
import tradingConsole.ui.colorHelper.FormBackColor;
import tradingConsole.ui.grid.DataGrid;
import tradingConsole.ui.grid.IPropertyChangingListener;
import tradingConsole.ui.grid.PropertyChangingEvent;
import javax.swing.JScrollPane;
import javax.swing.JDialog;
import javax.swing.Icon;

public class LockAccountForm extends JDialog
{
	private static String _dataSourceKey = "LockAccount";
	private tradingConsole.ui.grid.BindingSource _bindingSource;
	private HashMap<Guid, LockAccount> _lockAccounts;

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;

	public LockAccountForm(JFrame parent)
	{
		super(parent, true);

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
	}

	public LockAccountForm(JFrame parent,TradingConsole tradingConsole, SettingsManager settingsManager)
	{
		this(parent);

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;

		this.headerStaticText.setText(Language.LockAccountlblLockAccountPrompt);
		this.selectAllButton.setText(Language.LockAccountbtnSelectAll);
		this.clearAllButton.setText(Language.LockAccountbtnClearAll);
		this.submitButton.setText(Language.LockAccountbtnSubmit);
		//this.messageStaticText.setText("");

		this.setLockAccounts();
		this.initialize(this.lockAccountTable, true, false);
	}

	private void setLockAccounts()
	{
		this._lockAccounts = new HashMap<Guid, LockAccount> ();
		this._bindingSource = new tradingConsole.ui.grid.BindingSource();
		this._bindingSource.addPropertyChangingListener(new PropertyChangingListener(this));
		for (Iterator<Account> iterator = this._settingsManager.get_Accounts().values().iterator(); iterator.hasNext(); )
		{
			Account account = iterator.next();
			if (account.get_Type() == AccountType.Agent)
			{
				continue;
			}
			LockAccount lockAccount = LockAccount.create();
			lockAccount.setValue(account);
			this._lockAccounts.put(account.get_Id(), lockAccount);
		}
	}

	private void initialize(DataGrid grid, boolean ascend, boolean caseOn)
	{
		LockAccount.unbind(this._dataSourceKey, this._bindingSource);

		//grid.setRedraw(false);
		LockAccount.initialize(grid, this._dataSourceKey, this._lockAccounts.values(), this._bindingSource);
		for (Iterator<LockAccount> iterator = this._lockAccounts.values().iterator(); iterator.hasNext(); )
		{
			LockAccount lockAccount = iterator.next();

			lockAccount.update(this._dataSourceKey);
			Icon statusIcon = lockAccount.get_Account().getStatusIcon();
			this._bindingSource.setIcon(lockAccount, AccountLockColKey.Code, statusIcon);
		}
		int column = this._bindingSource.getColumnByName(AccountLockColKey.Code);
		grid.sortColumn(column, true, ascend);
		//grid.setRedraw(true);
		//this.doLayout();
	}

	public void dispose()
	{
		LockAccount.unbind(this._dataSourceKey, this._bindingSource);
		super.dispose();
	}

	private void selectAll()
	{
		Guid userId = this._settingsManager.get_Customer().get_UserId();
		for (Iterator<LockAccount> iterator = this._lockAccounts.values().iterator(); iterator.hasNext(); )
		{
			LockAccount lockAccount = iterator.next();
			Account account = lockAccount.get_Account();
			if (account.get_AgentId()!=null && account.get_AgentId().equals(userId))
			{
				lockAccount.set_IsLocked(true);
			}
			else
			{
				lockAccount.set_IsLocked(false);
			}
		}
		this.initialize(this.lockAccountTable,true,false);
	}

	private void clearAll()
	{
		boolean isHasAgentOrder = this._settingsManager.isHasAgentOrder();
		for (Iterator<LockAccount> iterator = this._lockAccounts.values().iterator(); iterator.hasNext(); )
		{
			LockAccount lockAccount = iterator.next();
			if (isHasAgentOrder
				&& lockAccount.get_PrevLockedStatus()==LockedStatus.BySelf)
			{
				lockAccount.set_IsLocked(true);
			}
			else
			{
				lockAccount.set_IsLocked(false);
			}
		}
		this.initialize(this.lockAccountTable,true,false);
	}

	private String[][] cast(HashMap<Guid, Boolean> accountLocks)
	{
		int i = 0;
		int size = accountLocks.size();
		String[][] arrayAccountLock = new String[size][2];
		for (Iterator<Guid> iterator = accountLocks.keySet().iterator();iterator.hasNext();)
		{
			Guid accountId = iterator.next();
			boolean isLocked = accountLocks.get(accountId).booleanValue();
			arrayAccountLock[i][0] = accountId.toString();
			arrayAccountLock[i][1] = (isLocked)?"true":"false";
			i++;
		}
		return arrayAccountLock;
	}

	//"<UpdateAccountLock>" +
	//		"<Account " +
	//			"ID="1" +
	//			"IsLocked="true" +
	//		">" +
	//		"<Account " +
	//			"ID="2" +
	//			"IsLocked="false" +
	//		">" +
	//		"</Account>" +
	//	"</UpdateAccountLock>"
	private void submit()
	{
		this.lockAccountTable.updateEditingValue();
		//this.messageStaticText.setText("");
		int lockedCount = 0;
		int size = this._lockAccounts.values().size();
		for (int row = 0; row < size; row++)
		{
			LockAccount lockAccount = (LockAccount)this._bindingSource.getObject(row);
			lockedCount += (lockAccount.get_IsLocked()) ? 1 : 0;
			if (lockedCount >= 2) break;
		}

		HashMap<Guid, Boolean> accountLocks = new HashMap<Guid,Boolean>();
		for (int row = 0; row < size; row++)
		{
			LockAccount lockAccount = (LockAccount)this._bindingSource.getObject(row);
			LockedStatus prevLockedStatus = lockAccount.get_PrevLockedStatus();
			boolean isLocked = (lockedCount < 2)?false:lockAccount.get_IsLocked();
			if (prevLockedStatus != LockedStatus.ByOther
				&& ( (isLocked && prevLockedStatus == LockedStatus.NotLocked)
					|| !isLocked && prevLockedStatus == LockedStatus.BySelf))
			{
				Guid accountId = lockAccount.get_Account().get_Id();
				accountLocks.put(accountId,isLocked);
			}
		}

		size = accountLocks.size();
		if (size > 0)
		{
			if(lockedCount == 1)
			{
				AlertDialogForm.showDialog(this, null, true, Language.AccountLockButtonSubmit_OnclickAlert1);
			}

			Guid agentAccountId = this._settingsManager.getAgentAccountIDForLockAccount();
			String[][] arrayAccountLock = this.cast(accountLocks);
			boolean isSucceed = this._tradingConsole.get_TradingConsoleServer().updateAccountLock(agentAccountId, arrayAccountLock);

			if (isSucceed)
			{
				this._settingsManager.getUpdateAccountLockResult(agentAccountId,accountLocks);
				//Update Ui
				LockAccount.unbind(this._dataSourceKey, this._bindingSource);
				this._lockAccounts.clear();
				this.setLockAccounts();
				this.initialize(this.lockAccountTable, true, false);

				//this.messageStaticText.setText(Language.TradeConsoleGetUpdateAccountLockResultAlert0);
				AlertDialogForm.showDialog(this, Language.notify, true, Language.TradeConsoleGetUpdateAccountLockResultAlert0);
			}
			else
			{
				//this.messageStaticText.setText(Language.TradeConsoleGetUpdateAccountLockResultAlert1);
				AlertDialogForm.showDialog(this, null, true, Language.TradeConsoleGetUpdateAccountLockResultAlert1);
			}
		}
		else
		{
			//this.messageStaticText.setText(Language.AccountLockButtonSubmit_OnclickAlert0);
			AlertDialogForm.showDialog(this, null, true, Language.AccountLockButtonSubmit_OnclickAlert0);
		}
	}

	static class PropertyChangingListener implements IPropertyChangingListener
	{
		private LockAccountForm _owner;
		public PropertyChangingListener(LockAccountForm owner)
		{
			this._owner = owner;
		}

		public void propertyChanging(PropertyChangingEvent e)
		{
			if(e.get_PropertyDescriptor().get_Name().equals(AccountLockColKey.IsLocked))
			{
				LockAccount lockAccount = (LockAccount)e.get_Owner();
				Account account = lockAccount.get_Account();
				if (!lockAccount.get_IsLocked()) //check operate
				{
					if (account.get_AgentId() == null || !account.get_AgentId().equals(this._owner._settingsManager.get_Customer().get_UserId()))
					{
						e.set_Cancel(true);
						return;
					}
				}
				else //uncheck operate
				{
					if (this._owner._settingsManager.isHasAgentOrder()
						&& lockAccount.get_PrevLockedStatus() == LockedStatus.BySelf)
					{
						e.set_Cancel(true);
						return;
					}
				}
			}
		}
	}

//SourceCode End////////////////////////////////////////////////////////////////////////////////


	private void jbInit() throws Exception
	{
		this.addWindowListener(new LockAccountTable_this_windowAdapter(this));

		this.setSize(330, 340);
		this.setResizable(true);
		this.setLayout(gridBagLayout1);
		this.setTitle(Language.lockAccountFormTitle);
		this.setBackground(FormBackColor.lockAccountForm);

		exitButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

		//lockAccountTable.addActionListener(new LockAccountForm_lockAccountTable_actionAdapter(this));

		selectAllButton.setText("Select All");
		selectAllButton.addActionListener(new LockAccountForm_selectAllButton_actionAdapter(this));
		clearAllButton.setText("Clear All");
		clearAllButton.addActionListener(new LockAccountForm_clearAllButton_actionAdapter(this));
		submitButton.setText("Submit");
		submitButton.addActionListener(new LockAccountForm_submitButton_actionAdapter(this));
		headerStaticText.setText("select the acount for agent operation:");
		exitButton.setText(Language.ExitCaption);
		JScrollPane scrollPane = new JScrollPane(lockAccountTable);
		this.getContentPane().add(selectAllButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 10), 10, 0));
		this.getContentPane().add(clearAllButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 10), 10, 0));
		this.getContentPane().add(headerStaticText, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
			, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(20, 10, 10, 20), 0, 0));
		this.getContentPane().add(scrollPane, new GridBagConstraints(0, 1, 1, 4, 1.0, 1.0
			, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 10, 10, 5), 0, 0));
		this.getContentPane().add(submitButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(2, 5, 10, 10), 34, 0));
		this.getContentPane().add(exitButton, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(2, 5, 10, 10), 34, 0));
	}

	public void this_windowClosing(WindowEvent e)
	{
		this.dispose();
	}

	PVStaticText2 headerStaticText = new PVStaticText2();
	DataGrid lockAccountTable = new DataGrid("LockAccountTable");
	PVButton2 selectAllButton = new PVButton2();
	PVButton2 clearAllButton = new PVButton2();
	PVButton2 submitButton = new PVButton2();
	PVButton2 exitButton = new PVButton2();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	/*public void lockAccountTable_actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		this.processLockAccountTable( e);
	}*/

	public void selectAllButton_actionPerformed(ActionEvent e)
	{
		this.selectAll();
	}

	public void clearAllButton_actionPerformed(ActionEvent e)
	{
		this.clearAll();
	}

	public void submitButton_actionPerformed(ActionEvent e)
	{
		this.submit();
	}

	class LockAccountTable_this_windowAdapter extends WindowAdapter
	{
		private LockAccountForm adaptee;
		LockAccountTable_this_windowAdapter(LockAccountForm adaptee)
		{
			this.adaptee = adaptee;
		}

		public void windowClosing(WindowEvent e)
		{
			adaptee.this_windowClosing(e);
		}
	}
}

class LockAccountForm_submitButton_actionAdapter implements ActionListener
{
	private LockAccountForm adaptee;
	LockAccountForm_submitButton_actionAdapter(LockAccountForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.submitButton_actionPerformed(e);
	}
}

class LockAccountForm_clearAllButton_actionAdapter implements ActionListener
{
	private LockAccountForm adaptee;
	LockAccountForm_clearAllButton_actionAdapter(LockAccountForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.clearAllButton_actionPerformed(e);
	}
}

class LockAccountForm_selectAllButton_actionAdapter implements ActionListener
{
	private LockAccountForm adaptee;
	LockAccountForm_selectAllButton_actionAdapter(LockAccountForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(ActionEvent e)
	{
		adaptee.selectAllButton_actionPerformed(e);
	}
}

/*class LockAccountForm_lockAccountTable_actionAdapter implements IActionListener
{
	private LockAccountForm adaptee;
	LockAccountForm_lockAccountTable_actionAdapter(LockAccountForm adaptee)
	{
		this.adaptee = adaptee;
	}

	public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
	{
		adaptee.lockAccountTable_actionPerformed(e);
	}
}*/
