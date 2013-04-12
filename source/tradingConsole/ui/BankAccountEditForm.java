package tradingConsole.ui;

import javax.swing.*;
import tradingConsole.BankAccount;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import tradingConsole.ui.language.Language;
import tradingConsole.TradingConsole;
import tradingConsole.Account;
import framework.Guid;
import tradingConsole.Currency;
import tradingConsole.Bank;
import tradingConsole.Province;
import tradingConsole.City;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import framework.StringHelper;
import tradingConsole.BankIdType;
import tradingConsole.BankAccountProp;
import tradingConsole.BankAccountType;
import tradingConsole.settings.PublicParametersManager;
import tradingConsole.Country;

public class BankAccountEditForm extends JDialog
{
	private BankAccount _bankAccount;
	private TradingConsole _tradingConsole;

	private PVStaticText2 accountTitle = new PVStaticText2();
	private PVStaticText2 account = new PVStaticText2();

	private PVStaticText2 countryTitle = new PVStaticText2();

	private PVStaticText2 bankTitle = new PVStaticText2();
	private JAdvancedComboBox bankChoice = new JAdvancedComboBox();

	private JAdvancedComboBox countryChoice = new JAdvancedComboBox();

	private PVStaticText2 bankNameTitle = new PVStaticText2();
	private JTextField bankName = new JTextField();

	private PVStaticText2 provinceTitle = new PVStaticText2();
	private JAdvancedComboBox provinceChoice = new JAdvancedComboBox();

	private PVStaticText2 cityTitle = new PVStaticText2();
	private JAdvancedComboBox cityChoice = new JAdvancedComboBox();

	private PVStaticText2 swiftCodeTitle = new PVStaticText2();
	private JTextField swiftCode = new JTextField();

	private PVStaticText2 bankAddressTitle = new PVStaticText2();
	private JTextField bankAddress = new JTextField();

	private PVStaticText2 bankAccountPropTitle = new PVStaticText2();
	private JAdvancedComboBox bankAccountProp = new JAdvancedComboBox();

	private PVStaticText2 bankAccountOpenerTitle = new PVStaticText2();
	private JTextField bankAccountOpener = new JTextField();

	private PVStaticText2 bankAccountTypeTitle = new PVStaticText2();
	private JAdvancedComboBox bankAccountType = new JAdvancedComboBox();

	private PVStaticText2 bankAccountNoTitle = new PVStaticText2();
	private JTextField bankAccountNo = new JTextField();

	private PVStaticText2 bankAccountCurrencyTitle = new PVStaticText2();
	private JAdvancedComboBox bankAccountCurrency = new JAdvancedComboBox();

	private PVStaticText2 bankAccountIdTypeTitle = new PVStaticText2();
	private JAdvancedComboBox bankAccountIdType = new JAdvancedComboBox();

	private PVStaticText2 bankAccountIdTitle = new PVStaticText2();
	private JTextField bankAccountId = new JTextField();

	private PVButton2 OKButton = new PVButton2();
	private PVButton2 CancelButton = new PVButton2();

	private boolean _isAddNewBankAccount = false;

	public BankAccountEditForm(JDialog parent, TradingConsole tradingConsole, Account account)
	{
		this(parent, tradingConsole, new BankAccount(Guid.newGuid(), account, tradingConsole.get_BankAccountHelper().getCountry(account.get_BankAccountDefaultCountryId()), Bank.Others, null,null, null, null, null, account.get_Currency(), null,  BankIdType.ShenFenZheng.get_InnerValue(), null, null, null, null, null), true);
	}

	public BankAccountEditForm(JDialog parent, TradingConsole tradingConsole, BankAccount bankAccount)
	{
		this(parent, tradingConsole, bankAccount, false);
	}

	private BankAccountEditForm(JDialog parent, TradingConsole tradingConsole, BankAccount bankAccount, boolean isAddNewBankAccount)
	{
		super(parent, true);

		this._isAddNewBankAccount = isAddNewBankAccount;

		this._bankAccount = bankAccount;
		this._tradingConsole = tradingConsole;

		this.jbInit();
		if(this._tradingConsole.get_SettingsManager().get_SystemParameter().get_BankAccountNameMustSameWithAccountName())
		{
			this.bankAccountOpener.setEditable(false);
			this.bankAccountOpener.setText(this._bankAccount.get_Account().get_Name());
		}
	}

	private void jbInit()
	{
		this.setSize(640, 370);
		this.setLayout(new GridBagLayout());
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setTitle(Language.BankAccount);

		JPanel panel = new JPanel(new GridBagLayout());
		this.add(panel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
											   , GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		int maxCustomerBankNo = this._tradingConsole.get_SettingsManager().get_SystemParameter().get_MaxCustomerBankNo();

		accountTitle.setText(Language.Account);
		panel.add(accountTitle, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(15, 5, 5, 0), 20, 0));

		boolean allowEdit = this._tradingConsole.get_SettingsManager().get_SystemParameter().get_AllowEditBankAccountInTrader()
			&& maxCustomerBankNo > 0;


		account.setText(_bankAccount.get_AccountCode());
		panel.add(account, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
												  , GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(15, 5, 5, 0), 0, 0));

		countryTitle.setText(Language.Country);
		panel.add(countryTitle, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 20, 0));
		countryChoice.setEnabled(allowEdit);
		panel.add(countryChoice, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
												  , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));


		bankTitle.setText(Language.Bank);
		panel.add(bankTitle, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
		bankChoice.setEnabled(allowEdit);
		panel.add(bankChoice, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));

		bankNameTitle.setText(Language.BankName);
		panel.add(bankNameTitle, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 35, 5, 0), 0, 0));

		panel.add(bankName, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 180, 0));
		bankName.setEnabled(allowEdit);
		bankName.setText(this._bankAccount.get_BankName());

		provinceTitle.setText(Language.Province);
		panel.add(provinceTitle, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
		provinceChoice.setEnabled(allowEdit);
		panel.add(provinceChoice, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 40, 0));

		cityTitle.setText(Language.City);
		panel.add(cityTitle, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 35, 5, 0), 0, 0));
		cityChoice.setEnabled(allowEdit);
		panel.add(cityChoice, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		swiftCodeTitle.setText(Language.SwiftCode);
		panel.add(swiftCodeTitle, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
		swiftCode.setEnabled(allowEdit);
		panel.add(swiftCode, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
		swiftCode.setText(this._bankAccount.get_SwiftCode());

		bankAddressTitle.setText(Language.BankAddress);
		panel.add(bankAddressTitle, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 35, 5, 0), 0, 0));
		bankAddress.setEnabled(allowEdit);
		panel.add(bankAddress, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 180, 0));
		bankAddress.setText(this._bankAccount.get_BankAddress());

		bankAccountPropTitle.setText(Language.BankAccountProp);
		panel.add(bankAccountPropTitle, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
		bankAccountProp.setEnabled(allowEdit);
		panel.add(bankAccountProp, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));

		bankAccountOpenerTitle.setText(Language.BankAccountOpener);
		panel.add(bankAccountOpenerTitle, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 35, 5, 0), 0, 0));
		bankAccountOpener.setEnabled(allowEdit);
		panel.add(bankAccountOpener, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 180, 0));
		bankAccountOpener.setText(this._bankAccount.get_AccountOpener());

		bankAccountTypeTitle.setText(Language.BankAccountType);
		panel.add(bankAccountTypeTitle, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
		bankAccountType.setEnabled(allowEdit);
		panel.add(bankAccountType, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));

		bankAccountNoTitle.setText(Language.BankAccountNo);
		panel.add(bankAccountNoTitle, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 35, 5, 0), 0, 0));
		bankAccountNo.setEnabled(allowEdit);
		panel.add(bankAccountNo, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 180, 0));
		bankAccountNo.setText(this._bankAccount.get_BankAccountNo());

		bankAccountCurrencyTitle.setText(Language.BankAccountCurrency);
		panel.add(bankAccountCurrencyTitle, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
		bankAccountCurrency.setEnabled(allowEdit);
		panel.add(bankAccountCurrency, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));

		bankAccountIdTypeTitle.setText(Language.BankAccountIdType);
		panel.add(bankAccountIdTypeTitle, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
		bankAccountIdType.setEnabled(allowEdit);
		panel.add(bankAccountIdType, new GridBagConstraints(1, 8, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));

		bankAccountIdTitle.setText(Language.BankAccountId);
		panel.add(bankAccountIdTitle, new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 35, 5, 0), 0, 0));
		bankAccountId.setEnabled(allowEdit);
		panel.add(bankAccountId, new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		bankAccountId.setText(this._bankAccount.get_IdNo());

		OKButton.setText(Language.Confirm);
		OKButton.setEnabled(allowEdit);
		CancelButton.setText(Language.ExitCaption);
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		this.add(buttonPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
			, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 5, 5, 0), 0, 25));
		buttonPanel.add(OKButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 5, 0), 10, 0));
		buttonPanel.add(CancelButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
			, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 20, 0));

		countryChoice.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				handleCountryChoiceItemStateChangedEvent();
			}
		});

		int selectedIndex = 0;
		int index = 0;
		for(Country country : this._tradingConsole.get_BankAccountHelper().get_Countries())
		{
			countryChoice.addItem(country.get_Name(), country);
			if (this._bankAccount.get_Country() == country)
			{
				selectedIndex = index;
			}
			index++;
		}
		countryChoice.setSelectedIndex(selectedIndex);

		selectedIndex = 0;
		index = 0;
		bankChoice.removeAllItems();
		for (Bank bank : this._bankAccount.get_Country().get_Banks())
		{
			bankChoice.addItem(bank.get_Name(), bank);
			if (this._bankAccount.get_Bank() == bank)
			{
				selectedIndex = index;
			}
			index++;
		}
		//if (selectedIndex > -1)
		{
			bankChoice.setSelectedIndex(selectedIndex);
		}

		this.bankName.setEnabled(allowEdit && selectedIndex == -1);

		provinceChoice.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				handleProvinceChoiceItemStateChangedEvent();
			}
		});

		selectedIndex = -1;
		index = 0;
		provinceChoice.removeAllItems();
		for (Province province : this._bankAccount.get_Country().get_Provinces())
		{
			provinceChoice.addItem(province.get_Name(), province);
			if (this._bankAccount.get_BankProvince() == province)
			{
				selectedIndex = index;
			}
			index++;
		}

		//if (selectedIndex > -1)
		{
			if(selectedIndex != - 1 || provinceChoice.getItemCount() > selectedIndex) provinceChoice.setSelectedIndex(selectedIndex);
		}

		selectedIndex = 0;
		index = 0;
		for (Currency currency : this._tradingConsole.get_SettingsManager().get_Currencies())
		{
			bankAccountCurrency.addItem(currency.get_Code(), currency);
			if (this._bankAccount.get_AccountBankBaseCurrency() == currency)
			{
				selectedIndex = index;
			}
			index++;
		}

		//if (selectedIndex > 0)
		{
			bankAccountCurrency.setSelectedIndex(selectedIndex);
		}

		//this.bankAccountProp.addItem(BankAccountProp.NotSet.getCaption(),  BankAccountProp.NotSet);
		this.bankAccountProp.addItem(BankAccountProp.PrivateBankProp.getCaption(),  BankAccountProp.PrivateBankProp);
		this.bankAccountProp.addItem(BankAccountProp.CompanyBankProp.getCaption(),  BankAccountProp.CompanyBankProp);
		BankAccountProp prop = BankAccountProp.fromInnerValue(this._bankAccount.get_AccountBankProp());
		//if(prop == BankAccountProp.NotSet) bankAccountProp.setSelectedIndex(0);
		/*else*/ if(prop == BankAccountProp.PrivateBankProp) bankAccountProp.setSelectedIndex(0);
		else if(prop == BankAccountProp.CompanyBankProp) bankAccountProp.setSelectedIndex(1);

		//this.bankAccountType.addItem(BankAccountType.NotSet.getCaption(), BankAccountType.NotSet);
		this.bankAccountType.addItem(BankAccountType.BankCard.getCaption(), BankAccountType.BankCard);
		this.bankAccountType.addItem(BankAccountType.BankBook.getCaption(), BankAccountType.BankBook);
		BankAccountType bankAccountType = BankAccountType.fromInnerValue(this._bankAccount.get_BankAccountType());
		//if(bankAccountType == BankAccountType.NotSet) this.bankAccountType.setSelectedIndex(0);
		/*else*/ if(bankAccountType == BankAccountType.BankCard) this.bankAccountType.setSelectedIndex(0);
		else if(bankAccountType == BankAccountType.BankBook) this.bankAccountType.setSelectedIndex(1);

		this.bankAccountIdType.addItem(BankIdType.QiTaZhengJian.getCaption(), BankIdType.QiTaZhengJian);
		this.bankAccountIdType.addItem(BankIdType.ShenFenZheng.getCaption(), BankIdType.ShenFenZheng);
		this.bankAccountIdType.addItem(BankIdType.HuZhao.getCaption(), BankIdType.HuZhao);

		if(PublicParametersManager.version.equalsIgnoreCase("CHS")
			|| PublicParametersManager.version.equalsIgnoreCase("CHT"))
		{
			this.bankAccountIdType.addItem(BankIdType.GangAoTongXingZheng.getCaption(), BankIdType.GangAoTongXingZheng);
			this.bankAccountIdType.addItem(BankIdType.HuKouBo.getCaption(), BankIdType.HuKouBo);
			this.bankAccountIdType.addItem(BankIdType.JingGuanZheng.getCaption(), BankIdType.JingGuanZheng);
			this.bankAccountIdType.addItem(BankIdType.JunGuanZheng.getCaption(), BankIdType.JunGuanZheng);
			this.bankAccountIdType.addItem(BankIdType.LinShiShenFenZheng.getCaption(), BankIdType.LinShiShenFenZheng);
			this.bankAccountIdType.addItem(BankIdType.ShiBingZheng.getCaption(), BankIdType.ShiBingZheng);
			this.bankAccountIdType.addItem(BankIdType.TaiBaoZheng.getCaption(), BankIdType.TaiBaoZheng);
		}
		BankIdType bankIdType = BankIdType.fromInnerValue(this._bankAccount.get_IdType());

		if(bankIdType == BankIdType.QiTaZhengJian) this.bankAccountIdType.setSelectedIndex(0);
		else if(bankIdType == BankIdType.ShenFenZheng) this.bankAccountIdType.setSelectedIndex(1);
		else if(bankIdType == BankIdType.HuZhao) this.bankAccountIdType.setSelectedIndex(2);
		else if(bankIdType == BankIdType.GangAoTongXingZheng) this.bankAccountIdType.setSelectedIndex(3);
		else if(bankIdType == BankIdType.HuKouBo) this.bankAccountIdType.setSelectedIndex(4);
		else if(bankIdType == BankIdType.JingGuanZheng) this.bankAccountIdType.setSelectedIndex(5);
		else if(bankIdType == BankIdType.JunGuanZheng) this.bankAccountIdType.setSelectedIndex(6);
		else if(bankIdType == BankIdType.LinShiShenFenZheng) this.bankAccountIdType.setSelectedIndex(7);
		else if(bankIdType == BankIdType.ShiBingZheng) this.bankAccountIdType.setSelectedIndex(8);
		else if(bankIdType == BankIdType.TaiBaoZheng) this.bankAccountIdType.setSelectedIndex(9);
		else this.bankAccountIdType.setSelectedIndex(0);

		OKButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				submit();
			}
		});

		CancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancel();
			}
		});

		provinceChoice.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				updateOKButtonStatus();
			}
		});
		cityChoice.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				updateOKButtonStatus();
			}
		});

		bankChoice.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				updateOKButtonStatus();
				updateBankNameStatus();
			}
		});

		bankAccountCurrency.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				updateOKButtonStatus();
			}
		});

		bankAccountProp.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				updateOKButtonStatus();
			}
		});

		this.bankAccountType.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				updateOKButtonStatus();
			}
		});

		bankName.getDocument().addDocumentListener(new DocumentListener()
			{
			public void insertUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}
		});
		swiftCode.getDocument().addDocumentListener(new DocumentListener()
			{
			public void insertUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}
		});
		bankAddress.getDocument().addDocumentListener(new DocumentListener()
			{
			public void insertUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}
		});
		this.bankAccountOpener.getDocument().addDocumentListener(new DocumentListener()
			{
			public void insertUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}
		});
		this.bankAccountId.getDocument().addDocumentListener(new DocumentListener()
			{
			public void insertUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}
		});
		this.bankAccountNo.getDocument().addDocumentListener(new DocumentListener()
			{
			public void insertUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}

			public void removeUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}

			public void changedUpdate(DocumentEvent e)
			{
				updateOKButtonStatus();
			}
		});

		updateOKButtonStatus();
		if(!this._isAddNewBankAccount) this.OKButton.setEnabled(false);
		updateBankNameStatus();
	}

	private boolean _hasChange;
	public boolean hasChange()
	{
		return this._hasChange;
	}

	private void updateBankNameStatus()
	{
		if(this.bankChoice.getSelectedIndex() > -1)
		{
			Bank bank = (Bank)this.bankChoice.getSelectedValue();
			int maxCustomerBankNo = this._tradingConsole.get_SettingsManager().get_SystemParameter().get_MaxCustomerBankNo();
			boolean allowEdit = this._tradingConsole.get_SettingsManager().get_SystemParameter().get_AllowEditBankAccountInTrader()
			&& maxCustomerBankNo > 0;
			this.bankName.setEnabled(allowEdit && bank == Bank.Others);
		}
		else
		{
			this.bankName.setEnabled(false);
		}
	}

	private void updateOKButtonStatus()
	{
		Bank bank = (Bank)this.bankChoice.getSelectedValue();
			boolean enable = (bank != Bank.Others || !StringHelper.isNullOrEmpty(this.bankName.getText()))
				&& (this.provinceChoice.getSelectedValue() != null || !StringHelper.isNullOrEmpty(this.bankAddress.getText()))
				&& this.bankAccountType.getSelectedValue() != BankAccountType.NotSet && !StringHelper.isNullOrEmpty(this.bankAccountNo.getText())
				&& this.bankAccountProp.getSelectedValue() != BankAccountProp.NotSet && !StringHelper.isNullOrEmpty(this.bankAccountOpener.getText())
				&& this.bankAccountCurrency.getSelectedValue() != null;
			int maxCustomerBankNo = this._tradingConsole.get_SettingsManager().get_SystemParameter().get_MaxCustomerBankNo();
			boolean allowEdit = this._tradingConsole.get_SettingsManager().get_SystemParameter().get_AllowEditBankAccountInTrader()
			&& maxCustomerBankNo > 0;
			this.OKButton.setEnabled(allowEdit && enable);
	}

	private void showWarning(String info)
	{
		AlertDialogForm.showDialog(this, Language.BankAccount, true, info);
	}

	private void submit()
	{
		this._hasChange = false;

		String countryId = null;
		if(this.countryChoice.getSelectedIndex() > -1)
		{
			Country country = (Country)this.countryChoice.getSelectedValue();
			if(country != Country.Others) countryId = Long.toString(country.get_Id());
		}

		String bankId = null;
		if(this.bankChoice.getSelectedIndex() > -1)
		{
			Bank bank = (Bank)this.bankChoice.getSelectedValue();
			if(bank != Bank.Others) bankId = bank.get_Id().toString();
		}

		String bankName = null;
		if(StringHelper.isNullOrEmpty(bankId))
		{
			bankName = this.bankName.getText();
		}

		String provinceId = null;
		if(this.provinceChoice.getSelectedIndex() > -1)
		{
			Province province = (Province)this.provinceChoice.getSelectedValue();
			provinceId = Long.toString(province.get_Id());
		}

		String cityId = null;
		if(this.cityChoice.getSelectedIndex() > -1)
		{
			City city = (City)this.cityChoice.getSelectedValue();
			cityId = Long.toString(city.get_Id());
		}

		boolean[] result = this._tradingConsole.get_TradingConsoleServer().apply(this._isAddNewBankAccount ? this._bankAccount.get_Id() : Guid.newGuid(),
			this._isAddNewBankAccount ? null : this._bankAccount.get_Id().toString(), this._bankAccount.get_Account().get_Id().toString(),countryId,
			bankId, bankName, this.bankAccountNo.getText(), ((BankAccountType)this.bankAccountType.getSelectedValue()).get_InnerValue(),
			this.bankAccountOpener.getText(), ((BankAccountProp)this.bankAccountProp.getSelectedValue()).get_InnerValue(),
			((Currency)this.bankAccountCurrency.getSelectedValue()).get_Id(), null,
			((BankIdType)this.bankAccountIdType.getSelectedValue()).get_InnerValue(), this.bankAccountId.getText(),
			provinceId, cityId, this.bankAddress.getText(), this.swiftCode.getText(), this._isAddNewBankAccount ? 0 : 1);

		if(result == null || !result[0])
		{
			this.showWarning(Language.FailedToSaveBankAccount);
		}
		else
		{
			this._hasChange = true;
			if(result[0] && result[1])
			{
				this.showWarning(Language.SaveBankAccountSuccessedAndApproved);
			}
			else
			{
				this.showWarning(Language.SaveBankAccountSuccessed);
			}
			this.dispose();
		}
	}

	private void cancel()
	{
		this._hasChange = false;
		this.dispose();
	}

	private void handleCountryChoiceItemStateChangedEvent()
	{
		if (this.countryChoice.getSelectedIndex() != -1)
		{
			Country country = (Country)countryChoice.getSelectedValue();
			if(!country.get_IsReady())
			{
				this._tradingConsole.get_BankAccountHelper().getBankAccountReferenceData(Long.toString(country.get_Id()));
			}
			int index = 0;
			int selectedIndex = -1;
			bankChoice.setSelectedIndex(-1);
			bankChoice.removeAllItems();
			for (Bank bank : country.get_Banks())
			{
				bankChoice.addItem(bank.get_Name(), bank);
				if(this._bankAccount.get_Bank() == bank)
				{
					selectedIndex = index;
				}
				index++;
			}
			//if (selectedIndex > 0)
			{
				bankChoice.setSelectedIndex(selectedIndex);
			}

			index = 0;
			selectedIndex = -1;
			provinceChoice.removeAllItems();
			for (Province province : country.get_Provinces())
			{
				provinceChoice.addItem(province.get_Name(), province);
				if(this._bankAccount.get_BankProvince() == province)
				{
					selectedIndex = index;
				}
				index++;
			}
			//if (selectedIndex > 0)
			{
				if(selectedIndex != - 1 || provinceChoice.getItemCount() > selectedIndex) provinceChoice.setSelectedIndex(selectedIndex);
			}

			if(provinceChoice.getItemCount() == 0) this.cityChoice.removeAllItems();
		}
	}

	private void handleProvinceChoiceItemStateChangedEvent()
	{
		if (this.provinceChoice.getSelectedIndex() != -1)
		{
			cityChoice.removeAllItems();
			Province province = (Province)provinceChoice.getSelectedValue();
			int index = 0;
			int selectedIndex = -1;
			for (City city : province.get_Cities())
			{
				cityChoice.addItem(city.get_Name(), city);
				if(this._bankAccount.get_BankCity() == city)
				{
					selectedIndex = index;
				}
				index++;
			}
			//if (selectedIndex > 0)
			{
				cityChoice.setSelectedIndex(selectedIndex);
			}
		}
	}
}
