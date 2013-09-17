package tradingConsole.enumDefine.physical;

import framework.lang.Enum;
import tradingConsole.ui.language.AdministrationFeeBaseLanguage;

public class AdministrationFeeBase extends Enum<AdministrationFeeBase>
{
	public static final AdministrationFeeBase PerLot = new AdministrationFeeBase("PerLot", 0);
	public static final AdministrationFeeBase PerValue = new AdministrationFeeBase("PerValue", 1);
	public static final AdministrationFeeBase LumpSum = new AdministrationFeeBase("LumpSum", 2);

	private AdministrationFeeBase(String name, int value)
	{
		super(name, value);
	}

	public String toLocalString()
	{
		if (this.equals(PerLot))
		{
			return AdministrationFeeBaseLanguage.PerLot;
		}
		else if (this.equals(PerValue))
		{
			return AdministrationFeeBaseLanguage.PerValue;
		}
		else if (this.equals(LumpSum))
		{
			return AdministrationFeeBaseLanguage.LumpSum;
		}
		else
		{
			return this.name();
		}
	}
}
