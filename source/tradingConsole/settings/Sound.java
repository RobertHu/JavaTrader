package tradingConsole.settings;

import framework.diagnostics.TraceType;
import tradingConsole.TradingConsole;
import tradingConsole.framework.ResourceHelper;

public class Sound
{
	public static String Login = "1.au";
	public static String Logout = "1.au";
	public static String InitError = "1.au";
	public static String Reset = "1.au";
	public static String Update = "2.au";
	public static String Execute = "2.au";
	public static String Execute2 = "2.au";
	public static String Delete = "2.au";
	public static String Cut = "2.au";
	public static String ClearOrder = "1.au";
	public static String CancelLMT = "2.au";
	public static String Cancel = "2.au";
	public static String Quote = "2.au";
	public static String Answer = "1.au";
	public static String Place = "2.au";
	public static String Assign = "2.au";
	public static String Message = "2.au";

	public static void play(String soundFile)
	{
		try
		{
			ResourceHelper.playAsSound("Configuration",soundFile);
		}
		catch (Throwable throwable)
		{
			TradingConsole.traceSource.trace(TraceType.Error, throwable);
		}
	}
}
