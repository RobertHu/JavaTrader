package tradingConsole;

import tradingConsole.ui.language.Language;
import framework.lang.Enum;
import framework.StringHelper;

public class BankAccountProp extends Enum<BankAccountProp>
{
	public static BankAccountProp PrivateBankProp = new BankAccountProp("PrivateBankProp", 0, "0");
	public static BankAccountProp CompanyBankProp = new BankAccountProp("CompanyBankProp", 1, "1");
	public static BankAccountProp NotSet = new BankAccountProp("NotSet", 2, "");

	private String _innerValue;

	private BankAccountProp(String name, int value, String innerValue)
	{
		super(name, value);
		this._innerValue = innerValue;
	}

	public String get_InnerValue()
	{
		return this._innerValue;
	}

	public String getCaption()
	{
		switch (this.value())
		{
			case 0:
				return Language.Private;
			case 1:
				return Language.Company;
			case 2:
				return Language.NotSet;
			default:
				return Language.NotSet;
		}
	}

	public static BankAccountProp fromInnerValue(String value)
	{
		if(StringHelper.isNullOrEmpty(value)) return BankAccountProp.NotSet;
		if(value.compareToIgnoreCase("0") == 0)
		{
			return BankAccountProp.PrivateBankProp;
		}
		else if(value.compareToIgnoreCase("1") == 0)
		{
			return BankAccountProp.CompanyBankProp;
		}
		else
		{
			return BankAccountProp.NotSet;
		}
	}
}
