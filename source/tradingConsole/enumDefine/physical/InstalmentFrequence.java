package tradingConsole.enumDefine.physical;

import framework.lang.Enum;
import tradingConsole.ui.language.InstalmentFrequenceLanguage;

public class InstalmentFrequence extends Enum<InstalmentFrequence>
{
	public static final InstalmentFrequence TillPayoff = new InstalmentFrequence("TillPayoff", -1);
	public static final InstalmentFrequence Month = new InstalmentFrequence("Month", 0);
	public static final InstalmentFrequence Season = new InstalmentFrequence("Season", 1);
	public static final InstalmentFrequence TwoWeeks = new InstalmentFrequence("TwoWeeks", 2);

	private InstalmentFrequence(String name, int value)
	{
		super(name, value);
	}

	public String toLocalString()
	{
		if(this.equals(InstalmentFrequence.TillPayoff))
		{
			return InstalmentFrequenceLanguage.TillPayoff;
		}
		else if(this.equals(InstalmentFrequence.Month))
		{
			return InstalmentFrequenceLanguage.Month;
		}
		else if(this.equals(InstalmentFrequence.Season))
		{
			return InstalmentFrequenceLanguage.Season;
		}
		else if(this.equals(InstalmentFrequence.TwoWeeks))
		{
			return InstalmentFrequenceLanguage.TwoWeeks;
		}
		else
		{
			return this.toString();
		}
	}
}
