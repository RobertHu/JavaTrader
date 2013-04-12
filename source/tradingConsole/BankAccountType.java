package tradingConsole;

import tradingConsole.ui.language.Language;
import framework.lang.Enum;
import framework.StringHelper;

public class BankAccountType extends Enum<BankAccountType>
{
	public static BankAccountType BankCard = new BankAccountType("BankCard", 0, "00");
	public static BankAccountType BankBook = new BankAccountType("BankBook", 1, "01");
	public static BankAccountType NotSet = new BankAccountType("NotSet", 2, "");

	private String _innerValue;

	private BankAccountType(String name, int value, String innerValue)
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
				return Language.BankCard;
			case 1:
				return Language.BankBook;
			case 2:
				return Language.NotSet;
			default:
				return Language.NotSet;
		}
	}

	public static BankAccountType fromInnerValue(String value)
	{
		if(StringHelper.isNullOrEmpty(value)) return BankAccountType.NotSet;

		if(value.compareToIgnoreCase("00") == 0)
		{
			return BankAccountType.BankCard;
		}
		else if(value.compareToIgnoreCase("01") == 0)
		{
			return BankAccountType.BankBook;
		}
		else
		{
			return BankAccountType.NotSet;
		}
	}
}
