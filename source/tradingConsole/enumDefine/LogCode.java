package tradingConsole.enumDefine;

import framework.lang.Enum;
import java.awt.Color;
import tradingConsole.ui.colorHelper.PhaseColor;
import tradingConsole.ui.ColorSettings;

public class LogCode extends Enum<LogCode>
{
	public static final LogCode Placing = new LogCode("Placing", 0);
	public static final LogCode Placed = new LogCode("Placed", 1);
	public static final LogCode Confirmed = new LogCode("Confirmed", 2);
	public static final LogCode Cancelled = new LogCode("Cancelled", 3);
	public static final LogCode Deleted = new LogCode("Deleted", 4);
	public static final LogCode Affected = new LogCode("Affected", 5);
	public static final LogCode RejectCancelLmtOrder = new LogCode("RejectCancelLmtOrder", 6);
	public static final LogCode RequestCancelLmtOrder = new LogCode("RequestCancelLmtOrder", 7);
	public static final LogCode Other = new LogCode("Other", 8);

	public static final LogCode Ledger = new LogCode("Ledger", 9);
	public static final LogCode Statement = new LogCode("Statement", 10);
	public static final LogCode AccountSummary = new LogCode("AccountSummary", 11);

	private LogCode(String name, int value)
	{
		super(name, value);
	}

	public static String getCaption(LogCode logCode)
	{
		return logCode.name();
	}

	public static Color getColor(LogCode logCode)
	{
		if (logCode.equals(LogCode.Placing))
		{
			return LogColor.placing;
		}
		else if (logCode.equals(LogCode.Placed))
		{
			return LogColor.placed;
		}
		else if (logCode.equals(LogCode.Cancelled))
		{
			return LogColor.cancelled;
		}
		else if (logCode.equals(LogCode.Confirmed))
		{
			return LogColor.confirmed;
		}
		else if (logCode.equals(LogCode.Deleted))
		{
			return LogColor.deleted;
		}
		else if (logCode.equals(LogCode.Affected))
		{
			return LogColor.affected;
		}
		else if (logCode.equals(LogCode.RejectCancelLmtOrder))
		{
			return LogColor.rejectCancelLmtOrder;
		}
		else if (logCode.equals(LogCode.RequestCancelLmtOrder))
		{
			return LogColor.requestCancelLmtOrder;
		}
		return ColorSettings.useBlackAsBackground ? Color.WHITE : Color.BLACK;
	}

}
