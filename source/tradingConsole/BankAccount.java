package tradingConsole;

import framework.Guid;
import framework.StringHelper;
import tradingConsole.framework.PropertyDescriptor;
import com.jidesoft.grid.BooleanCheckBoxCellEditor;
import com.jidesoft.grid.BooleanCheckBoxCellRenderer;
import javax.swing.SwingConstants;
import tradingConsole.ui.columnKey.OutstandingOrderColKey;
import tradingConsole.settings.RelationOrder;
import tradingConsole.ui.language.OutstandingOrderLanguage;
import tradingConsole.enumDefine.BuySellType;
import tradingConsole.ui.language.Language;

public class BankAccount
{
	private Guid _id;
	private Account _account;
	private Bank _bank;
	private Country _country;
	private String _bankName;
	private String _accountBankNo;
	private String _accountBankType;
	private String _accountOpener;
	private String _accountBankProp;
	private Currency _accountBankBaseCurrency;
	private String _accountBankBaseCurrencyName;
	private String _idType;
	private String _idNo;
	private Province _bankProvince;
	private City _bankCity;
	private String _bankAddress;
	private String _swiftCode;

	public BankAccount(Guid id, Account account, Country country, Bank bank, String bankName, String accountBankNo, String accountBankType,
					   String accountOpener, String accountBankProp, Currency accountBankBaseCurrency, String accountBankBaseCurrencyName,
					   String idType, String idNo, Province bankProvince, City bankCity, String bankAddress, String swiftCode)
	{
		this._id = id;
		this._account = account;
		this._bank = bank;
		this._country = country;
		this._bankName = bankName;
		this._accountBankNo = accountBankNo;
		this._accountBankType = accountBankType;
		this._accountOpener = accountOpener;
		this._accountBankProp = accountBankProp;
		this._accountBankBaseCurrency = accountBankBaseCurrency;
		this._accountBankBaseCurrencyName = accountBankBaseCurrencyName;
		this._idType = idType;
		this._idNo = idNo;
		this._bankProvince = bankProvince;
		this._bankCity = bankCity;
		this._bankAddress = bankAddress;
		this._swiftCode = swiftCode;
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public Account get_Account()
	{
		return this._account;
	}

	public Country get_Country()
	{
		return this._country;
	}

	public Bank get_Bank()
	{
		return this._bank;
	}

	public void set_Bank(Bank bank)
	{
		this._bank = bank;
	}

	public String get_BankName()
	{
		return this._bankName;
	}

	public void set_BankName(String value)
	{
		this._bankName = value;
	}

	public String get_BankAccountNo()
	{
		return this._accountBankNo;
	}

	public void set_BankAccountNo(String value)
	{
		this._accountBankNo = value;
	}

	public String get_BankAccountType()
	{
		return this._accountBankType;
	}

	public void set_BankAccountType(String value)
	{
		this._accountBankType = value;
	}

	public String get_AccountOpener()
	{
		return this._accountOpener;
	}

	public void set_AccountOpener(String value)
	{
		this._accountOpener = value;
	}

	public String get_AccountBankProp()
	{
		return this._accountBankProp;
	}

	public void set_AccountBankProp(String value)
	{
		this._accountBankProp = value;
	}

	public Currency get_AccountBankBaseCurrency()
	{
		return this._accountBankBaseCurrency;
	}

	public void set_AccountBankBaseCurrency(Currency value)
	{
		this._accountBankBaseCurrency = value;
	}

	public String get_AccountBankBCName()
	{
		return this._accountBankBaseCurrencyName;
	}

	public void set_AccountBankBCName(String value)
	{
		this._accountBankBaseCurrencyName = value;
	}

	public String get_IdType()
	{
		return this._idType;
	}

	public void set_IdType(String value)
	{
		this._idType = value;
	}

	public String get_IdNo()
	{
		return this._idNo;
	}

	public void set_IdNo(String value)
	{
		this._idNo = value;
	}

	public Province get_BankProvince()
	{
		return this._bankProvince;
	}

	public void set_BankProvince(Province value)
	{
		this._bankProvince = value;
	}

	public City get_BankCity()
	{
		return this._bankCity;
	}

	public void set_BankCity(City value)
	{
		this._bankCity = value;
	}

	public String get_BankAddress()
	{
		return this._bankAddress;
	}

	public void set_BankAddress(String value)
	{
		this._bankAddress = value;
	}

	public String get_SwiftCode()
	{
		return this._swiftCode;
	}

	public void set_SwiftCode(String value)
	{
		this._swiftCode = value;
	}

	public String get_Summary()
	{
		return StringHelper.format("{0}-{1}-{2}", new Object[]
								   {this._accountOpener, StringHelper.isNullOrEmpty(this._bankName) ? this._bank.get_Name() : this._bankName,
								   this._accountBankNo});
	}

	public String get_AccountCode()
	{
		return this._account.get_Code();
	}

	public String getAddress()
	{
		return StringHelper.format("{0} {1} {2}", new Object[]
								   {this._bankProvince == null ? "" : this._bankProvince.get_Name(),
								   this._bankCity == null ? "" : this._bankCity.get_Name(), this._bankAddress});
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[2];

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(BankAccount.class, "AccountCode", true, null,
			Language.Account, 50, SwingConstants.LEFT, null, null, null, null);
		propertyDescriptors[0] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(BankAccount.class, "Summary", true, null,
			Language.BankAccount, 0, SwingConstants.LEFT, null, null, null, null);
		propertyDescriptors[1] = propertyDescriptor;

		return propertyDescriptors;
	}
}
