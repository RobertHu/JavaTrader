package tradingConsole.enumDefine.physical;

import tradingConsole.ui.language.InstalmentLanguage;
import framework.lang.Enum;

public class InstalmentDetailStatus extends Enum<InstalmentDetailStatus>
{
	public static final InstalmentDetailStatus NotPaid = new InstalmentDetailStatus("NotPaid", 0);
	public static final InstalmentDetailStatus Paid = new InstalmentDetailStatus("Paid", 1);
	public static final InstalmentDetailStatus Overdue = new InstalmentDetailStatus("Overdue", 2);

	private InstalmentDetailStatus(String name, int value)
	{
		super(name, value);
	}

	public String toLocalString()
	{
		if(this.compareTo(InstalmentDetailStatus.NotPaid) == 0)
		{
			return InstalmentLanguage.NotPaid;
		}
		else if(this.compareTo(InstalmentDetailStatus.Paid) == 0)
		{
			return InstalmentLanguage.Paid;
		}
		else if(this.compareTo(InstalmentDetailStatus.Overdue) == 0)
		{
			return InstalmentLanguage.Overdue;
		}
		else
		{
			return this.name();
		}
	}
}
