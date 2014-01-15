package tradingConsole.enumDefine.physical;

import tradingConsole.ui.language.ContractTerminateTypeLanguage;
import framework.lang.Enum;
import tradingConsole.ui.language.AdministrationFeeBaseLanguage;

public class ContractTerminateType  extends Enum<ContractTerminateType>
{
	public static final ContractTerminateType RepaymentRatio = new ContractTerminateType("RepaymentRatio", 0);
	public static final ContractTerminateType DeductOneInstalment = new ContractTerminateType("DeductOneInstalment", 1);
	public static final ContractTerminateType PerLot = new ContractTerminateType("PerLot", 2);
	public static final ContractTerminateType LumpSum = new ContractTerminateType("LumpSum", 3);

	private ContractTerminateType(String name, int value)
	{
		super(name, value);
	}

	public String toLocalString()
	{
		if (this.equals(RepaymentRatio))
		{
			return ContractTerminateTypeLanguage.RepaymentRatio;
		}
		else if (this.equals(DeductOneInstalment))
		{
			return ContractTerminateTypeLanguage.DeductOneInstalment;
		}
		else if(this.equals(PerLot))
		{
			return AdministrationFeeBaseLanguage.PerLot;
		}
		else if(this.equals(LumpSum))
		{
			return AdministrationFeeBaseLanguage.LumpSum;
		}
		else
		{
			return this.name();
		}
	}
}
