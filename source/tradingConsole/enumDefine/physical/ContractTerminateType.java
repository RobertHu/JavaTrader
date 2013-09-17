package tradingConsole.enumDefine.physical;

import tradingConsole.ui.language.ContractTerminateTypeLanguage;
import framework.lang.Enum;

public class ContractTerminateType  extends Enum<ContractTerminateType>
{
	public static final ContractTerminateType RepaymentRatio = new ContractTerminateType("RepaymentRatio", 0);
	public static final ContractTerminateType DeductOneInstalment = new ContractTerminateType("DeductOneInstalment", 1);

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
		else
		{
			return this.name();
		}
	}
}
