package tradingConsole.enumDefine;

import java.awt.Color;

import framework.lang.Enum;

import tradingConsole.ui.language.Language;
import tradingConsole.ui.colorHelper.TradeOptionColor;

public class TradeOption extends Enum<TradeOption>
{
	public static final TradeOption None = new TradeOption("NONE", 0);
	public static final TradeOption Stop = new TradeOption("STOP", 1);
	public static final TradeOption Better = new TradeOption("BETTER", 2);

	private TradeOption(String name, int value)
	{
		super(name, value);
	}

	public static String getCaption(TradeOption tradeOption)
	{
		if (tradeOption.equals(TradeOption.Better))
		{
			return Language.TradeOptionPromptBetter;
		}
		else if (tradeOption.equals(TradeOption.Stop))
		{
			return Language.TradeOptionPromptStop;
		}
		return "";
	}

	public static Color getColor(TradeOption tradeOption)
	{
		if (tradeOption.equals(TradeOption.Better))
		{
			return TradeOptionColor.better;
		}
		else if (tradeOption.equals(TradeOption.Stop))
		{
			return TradeOptionColor.stop;
		}
		return Color.black;
	}
}
