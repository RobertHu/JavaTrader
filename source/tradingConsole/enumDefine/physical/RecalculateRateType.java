package tradingConsole.enumDefine.physical;

import framework.lang.Enum;
import tradingConsole.ui.language.RecalculateRateTypeLanguage;

public class RecalculateRateType extends Enum<RecalculateRateType>
{
	public static final RecalculateRateType NextMonth = new RecalculateRateType("NextMonth", 1);
	public static final RecalculateRateType NextYear = new RecalculateRateType("NextYear", 2);
	public static final RecalculateRateType All = new RecalculateRateType("All", 3);

	private RecalculateRateType(String name, int value)
	{
		super(name, value);
	}

	public String toLocalString()
	{
		if(this.equals(NextMonth))
		{
			return RecalculateRateTypeLanguage.NextMonth;
		}
		else if(this.equals(NextYear))
		{
			return RecalculateRateTypeLanguage.NextYear;
		}
		else
		{
			return this.name();
		}
	}
}
