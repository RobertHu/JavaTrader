package tradingConsole.enumDefine;

import java.awt.Color;

import framework.lang.Enum;

import tradingConsole.ui.colorHelper.LockedStatusColor;

public class LockedStatus extends Enum<LockedStatus>
{
	public static final LockedStatus NotLocked = new LockedStatus("NotLocked", 0);
	public static final LockedStatus BySelf = new LockedStatus("BySelf", 1);
	public static final LockedStatus ByOther = new LockedStatus("ByOther", 2);

	private LockedStatus(String name, int value)
	{
		super(name, value);
	}

	public static Color getColor(LockedStatus lockedStatus)
	{
		if (lockedStatus.equals(LockedStatus.NotLocked))
		{
			return LockedStatusColor.notLocked;
		}
		else if (lockedStatus.equals(LockedStatus.ByOther))
		{
			return LockedStatusColor.byOther;
		}
		else if (lockedStatus.equals(LockedStatus.BySelf))
		{
			return LockedStatusColor.bySelf;
		}
		return Color.black;
	}
}
