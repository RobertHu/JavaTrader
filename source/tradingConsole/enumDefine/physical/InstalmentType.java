package tradingConsole.enumDefine.physical;

import framework.lang.Enum;
import tradingConsole.ui.language.InstalmentTypeLanguage;
public class InstalmentType extends Enum<InstalmentType>
{
	public static final InstalmentType FullAmount = new InstalmentType("FullAmount", 0);
	public static final InstalmentType EqualPrincipal = new InstalmentType("EqualPrincipal", 1);
	public static final InstalmentType EqualInstallment = new InstalmentType("EqualInstallment", 2);
	public static final InstalmentType All = new InstalmentType("All", 3);

	private InstalmentType(String name, int value)
	{
		super(name, value);
	}

	public String toLocalString()
	{
		if(this.equals(FullAmount))
		{
			return InstalmentTypeLanguage.FullAmount;
		}
		else if(this.equals(EqualPrincipal))
		{
			return InstalmentTypeLanguage.EqualPrincipal;
		}
		else if(this.equals(EqualInstallment))
		{
			return InstalmentTypeLanguage.EqualInstallment;
		}
		else
		{
			return this.name();
		}
	}
}
