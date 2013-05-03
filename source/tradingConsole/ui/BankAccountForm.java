package tradingConsole.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import tradingConsole.*;
import tradingConsole.settings.*;
import tradingConsole.ui.grid.*;
import tradingConsole.ui.language.*;
import framework.IAsyncResult;
import framework.data.DataSet;
import framework.IAsyncCallback;
import framework.StringHelper;
import framework.data.DataRowCollection;
import framework.data.DataRow;
import framework.Guid;
import framework.DBNull;
import java.util.ArrayList;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import com.jidesoft.swing.JideSwingUtilities;
import java.awt.event.WindowAdapter;
import Packet.SignalObject;
import Util.XmlElementHelper;

public class BankAccountForm extends JDialog implements InitializeBankAccount
{
	private DataGrid bankAccountsTable = new DataGrid("BankAccount");
	private PVButton2 addButton = new PVButton2();
	private PVButton2 modifyButton = new PVButton2();
	private PVButton2 deleteButton = new PVButton2();
	private PVButton2 exitButton = new PVButton2();

	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;
	private Account _account;

	public BankAccountForm(TradingConsole tradingConsole, SettingsManager settingsManager, Account account)
	{
		super(tradingConsole.get_MainForm(), true);

		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
		this._account = account;

		this.jbInit();
		this.getBankAccounts();
	}

	public void showWarning(String info)
	{
		AlertDialogForm.showDialog(this, Language.BankAccount, true, info);
	}

	private ArrayList<BankAccount> _BankAccounts;
	public  void initialize(DataSet dataSet)
	{
		this._tradingConsole.get_BankAccountHelper().initializeReferenceData(dataSet);

		DataRowCollection rows = dataSet.get_Tables().get_Item(3).get_Rows();
		ArrayList<BankAccount> bankAccounts = new ArrayList<BankAccount> (rows.get_Count());
		for (int index = 0; index < rows.get_Count(); index++)
		{
			DataRow row = rows.get_Item(index);

			Guid id = (Guid)row.get_Item("Id");

			Object value = row.get_Item("CountryID");
			Country country = value == DBNull.value ? Country.Others : this._tradingConsole.get_BankAccountHelper().getCountry( (Long)value);

			value = row.get_Item("BankId");
			Bank bank = value == DBNull.value ? Bank.Others : this._tradingConsole.get_BankAccountHelper().getBank( (Guid)value);

			value = row.get_Item("BankName");
			String bankName = value == DBNull.value ? null : (String)value;

			String accountBankNo = (String)row.get_Item("AccountBankNo");
			String accountBankType = (String)row.get_Item("AccountBankType");
			String accountOpener = (String)row.get_Item("AccountOpener");

			value = row.get_Item("AccountBankProp");
			String accountBankProp = value == DBNull.value ? null : (String)value;

			value = row.get_Item("AccountBankBCId");
			Currency accountBankBaseCurrency = value == DBNull.value ? null : this._settingsManager.getCurrency( (Guid)value);

			value = row.get_Item("AccountBankBCName");
			String accountBankBCName = value == DBNull.value ? null : (String)value;

			value = row.get_Item("IdType");
			String idType = value == DBNull.value ? null : (String)value;

			value = row.get_Item("IdNo");
			String idNo = value == DBNull.value ? null : (String)value;

			value = row.get_Item("BankProvinceId");
			Province province = value == DBNull.value ? null : this._tradingConsole.get_BankAccountHelper().getProvince( (Long)value);

			value = row.get_Item("BankCityId");
			City city = value == DBNull.value ? null : this._tradingConsole.get_BankAccountHelper().getCity( (Long)value);

			value = row.get_Item("BankAddress");
			String bankAddress = value == DBNull.value ? null : (String)value;

			value = row.get_Item("SwiftCode");
			String swiftCode = value == DBNull.value ? null : (String)value;

			BankAccount bankAccount = new BankAccount(id, this._account, country, bank, bankName, accountBankNo, accountBankType,
				accountOpener, accountBankProp, accountBankBaseCurrency, accountBankBCName, idType, idNo, province, city,
				bankAddress, swiftCode);

			bankAccounts.add(bankAccount);
		}
		this._BankAccounts = bankAccounts;
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				tradingConsole.ui.grid.BindingSource bindingSource = new tradingConsole.ui.grid.BindingSource();
				_tradingConsole.bindingManager.bind("BankAccount", _BankAccounts, bindingSource, BankAccount.getPropertyDescriptors());

				bindingSource.setBackground(Color.WHITE);
				bankAccountsTable.setModel(bindingSource);
				int maxCustomerBankNo = _tradingConsole.get_SettingsManager().get_SystemParameter().get_MaxCustomerBankNo();
				addButton.setVisible(_tradingConsole.get_SettingsManager().get_SystemParameter().get_AllowEditBankAccountInTrader() && maxCustomerBankNo != 0);
				modifyButton.setVisible(_tradingConsole.get_SettingsManager().get_SystemParameter().get_AllowEditBankAccountInTrader() && maxCustomerBankNo != 0);
				deleteButton.setVisible(_tradingConsole.get_SettingsManager().get_SystemParameter().get_AllowEditBankAccountInTrader() && maxCustomerBankNo != 0);
				addButton.setEnabled(_BankAccounts.size() < maxCustomerBankNo);
			}
		});
	}

	private void getBankAccounts()
	{
	try
		{
			this._tradingConsole.get_TradingConsoleServer().beginGetAccountBanksApproved(this._account.get_Id(), new BankAccountCallback(this._account.get_Code(),this), null);
		}
		catch(Exception e)
		{
			if(this._tradingConsole.get_LoginInformation().getIsConnected())
			{
				String info = StringHelper.format(Language.CannotGetBankAccounts, new Object[]
												  {_account.get_Code()});
				showWarning(info);
			}
		}
	}




	private void modify(BankAccount bankAccount)
	{
		BankAccountEditForm form = new BankAccountEditForm(this, this._tradingConsole, bankAccount);
		this.showEditForm(form);
	}

	private void showEditForm(BankAccountEditForm form)
	{
		JideSwingUtilities.centerWindow(form);
		form.show();
		form.addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent e)
			{
				BankAccountEditForm form = (BankAccountEditForm)e.getWindow();
				if (form.hasChange())
				{
					getBankAccounts();
				}
			}
		});
	}

	private void addBankAccount()
	{
		BankAccountEditForm form = new BankAccountEditForm(this, this._tradingConsole, this._account);
		this.showEditForm(form);
	}

	private void delete(BankAccount bankAccount)
	{
		boolean[] result = this._tradingConsole.get_TradingConsoleServer().apply(Guid.newGuid(), bankAccount.get_Id().toString(), null, null, null,
			null, null, null, null, null, Guid.empty, null, null, null, null, null, null, null, 2);

		if(result == null || !result[0])
		{
			this.showWarning(Language.FailedToDeleteBankAccount);
		}
		else
		{
			this.getBankAccounts();
			if(result[0] && result[1])
			{
				this.showWarning(Language.DeleteBankAccountSuccessedAndApproved);
			}
			else
			{
				this.showWarning(Language.DeleteBankAccountSuccessed);
			}
		}
	}

	private void jbInit()
	{
		this.setSize(420, 300);
		this.setTitle(Language.BankAccount);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		java.awt.GridBagLayout gridBagLayout = new GridBagLayout();
		this.setLayout(gridBagLayout);

		JScrollPane scrollPane = new JScrollPane(bankAccountsTable);
		this.add(scrollPane, new GridBagConstraints(0, 0, 5, 1, 1.0, 1.0
													, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 400, 250));

		this.add(addButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
												   , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 20, 0));

		this.add(deleteButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
													  , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 1, 5, 0), 20, 0));

		this.add(modifyButton, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0
													  , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 1, 5, 0), 20, 0));

		this.add(exitButton, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0
													, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 1, 5, 5), 20, 0));

		addButton.setText(Language.AddCaption);
		deleteButton.setText(Language.DeleteCaption);
		modifyButton.setText(Language.ModifyCaption);
		exitButton.setText(Language.ExitCaption);

		exitButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

		this.bankAccountsTable.addActionListener(new IActionListener()
		{
			public void actionPerformed(tradingConsole.ui.grid.ActionEvent e)
			{
				if (e.get_GridAction().compareTo(tradingConsole.ui.grid.Action.DoubleClicked) == 0)
				{
					Object bankAccount = e.get_Object();
					if (bankAccount != null)
					{
						modify( (BankAccount)bankAccount);
					}
				}
			}
	});

		this.bankAccountsTable.addSelectedRowChangedListener(new ISelectedRowChangedListener()
		{
			public void selectedRowChanged(DataGrid source)
			{
				deleteButton.setEnabled(bankAccountsTable.getSelectedRowCount() == 1);
				modifyButton.setEnabled(bankAccountsTable.getSelectedRowCount() == 1);
			}
		});

		if(this._tradingConsole.get_SettingsManager().get_SystemParameter().get_AllowEditBankAccountInTrader())
		{
			deleteButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (bankAccountsTable.getSelectedRowCount() == 1)
					{
						int row = bankAccountsTable.getSelectedRow();
						BankAccount bankAccount = (BankAccount)bankAccountsTable.getObject(row);
						delete(bankAccount);
					}
				}
		});

			modifyButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (bankAccountsTable.getSelectedRowCount() == 1)
					{
						int row = bankAccountsTable.getSelectedRow();
						BankAccount bankAccount = (BankAccount)bankAccountsTable.getObject(row);
						modify(bankAccount);
					}
				}
		});

			addButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					addBankAccount();
				}
		});
		}
		else
		{
			this.addButton.setVisible(false);
			this.deleteButton.setVisible(false);
			this.modifyButton.setVisible(false);
		}
	}
}
