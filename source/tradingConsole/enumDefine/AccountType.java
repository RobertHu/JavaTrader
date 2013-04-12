package tradingConsole.enumDefine;

import framework.lang.Enum;

/*use sample:
AccountType e=Enum.valueOf(AccountType.class,0);
AccountType e2=Enum.valueOf(AccountType.class,"Common");
 */
public class AccountType extends Enum<AccountType>
{
	public static final AccountType Common = new AccountType("Common", 0);
	public static final AccountType Agent = new AccountType("Agent", 1);
	public static final AccountType Company = new AccountType("Company", 2);
	public static final AccountType Transit = new AccountType("Transit", 3);
	public static final AccountType BlackList = new AccountType("BlackList", 4);

	private AccountType(String name, int value)
	{
		super(name, value);
	}
}
