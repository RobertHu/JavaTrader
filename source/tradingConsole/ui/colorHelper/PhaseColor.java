package tradingConsole.ui.colorHelper;

import java.awt.Color;
import tradingConsole.ui.ColorSettings;

public class PhaseColor
{
	public static Color placing = new Color(65480);
	public static Color placed = new Color(640266);
	public static Color cancelled = Color.lightGray;//new Color(12632256);
	public static Color executed;// = new Color(0);
	public static Color completed;//  = new Color(0);
	public static Color deleted = new Color(255);

	static
	{
	if (ColorSettings.useBlackAsBackground)
	{
		PhaseColor.executed = Color.WHITE;
		PhaseColor.completed = Color.WHITE;
	}
	else
	{
		PhaseColor.executed = Color.BLACK;
		PhaseColor.completed = Color.BLACK;
	}
}
}
