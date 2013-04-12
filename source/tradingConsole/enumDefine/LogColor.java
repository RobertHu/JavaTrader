package tradingConsole.enumDefine;

import java.awt.Color;
import tradingConsole.ui.ColorSettings;

public class LogColor
{
	public static Color placing;
	public static Color placed;
	public static Color confirmed;
	public static Color cancelled;
	public static Color deleted;
	public static Color affected;
	public static Color rejectCancelLmtOrder;
	public static Color requestCancelLmtOrder;

	static
	{
	if (ColorSettings.useBlackAsBackground)
	{
		placing = Color.WHITE; // new Color(65480);
		placed = Color.WHITE; //new Color(640266);
		confirmed = Color.WHITE; //Color.blue;
		cancelled = Color.WHITE; //Color.lightGray;
		deleted = Color.WHITE; //new Color(255);
		affected = Color.WHITE; //Color.blue;
		rejectCancelLmtOrder = Color.WHITE;
		requestCancelLmtOrder = Color.WHITE; //new Color(65480);
	}
	else
	{
		placing = Color.BLACK; // new Color(65480);
		placed = Color.BLACK; //new Color(640266);
		confirmed = Color.BLACK; //Color.blue;
		cancelled = Color.BLACK; //Color.lightGray;
		deleted = Color.BLACK; //new Color(255);
		affected = Color.BLACK; //Color.blue;
		rejectCancelLmtOrder = Color.BLACK;
		requestCancelLmtOrder = Color.BLACK; //new Color(65480);
	}
}
}
