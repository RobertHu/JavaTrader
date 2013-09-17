package tradingConsole.enumDefine.physical;

import framework.lang.Enum;

public class InstalmentCloseOption  extends Enum<InstalmentCloseOption>
{
	public static final InstalmentCloseOption Disallow = new InstalmentCloseOption("Disallow", 0);
	public static final InstalmentCloseOption AllowAll = new InstalmentCloseOption("AllowAll", 1);
	public static final InstalmentCloseOption AllowWhenNotOverdue = new InstalmentCloseOption("AllowWhenNotOverdue", 2);
	public static final InstalmentCloseOption AllowPrepayment = new InstalmentCloseOption("AllowPrepayment", 4);

	private InstalmentCloseOption(String name, int value)
	{
		super(name, value);
	}
}
