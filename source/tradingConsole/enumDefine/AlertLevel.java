package tradingConsole.enumDefine;

import java.awt.Color;

import framework.lang.Enum;

import tradingConsole.ui.language.Language;
import tradingConsole.ui.colorHelper.AlertLevelColor;

public class AlertLevel extends Enum<AlertLevel>
{
	public static final AlertLevel AlertLevel0 = new AlertLevel("AlertLevel0", 0);
	public static final AlertLevel AlertLevel1 = new AlertLevel("AlertLevel1", 1);
	public static final AlertLevel AlertLevel2 = new AlertLevel("AlertLevel2", 2);
	public static final AlertLevel AlertLevel3 = new AlertLevel("AlertLevel3", 3);

	private AlertLevel(String name, int value)
	{
		super(name, value);
	}

	public static Color getColor(AlertLevel alertLevel)
	{
		if (alertLevel.equals(AlertLevel.AlertLevel0))
		{
			return AlertLevelColor.alertLevel0;
		}
		else if (alertLevel.equals(AlertLevel.AlertLevel1))
		{
			return AlertLevelColor.alertLevel1;
		}
		else if (alertLevel.equals(AlertLevel.AlertLevel2))
		{
			return AlertLevelColor.alertLevel2;
		}
		else if (alertLevel.equals(AlertLevel.AlertLevel3))
		{
			return AlertLevelColor.alertLevel3;
		}
		return Color.black;
	}

	public static String getCaption(AlertLevel alertLevel)
	{
		if (alertLevel.equals(AlertLevel.AlertLevel0))
		{
			return Language.AlertLevelPrompt0;
		}
		else if (alertLevel.equals(AlertLevel.AlertLevel1))
		{
			return Language.AlertLevelPrompt1;
		}
		else if (alertLevel.equals(AlertLevel.AlertLevel2))
		{
			return Language.AlertLevelPrompt2;
		}
		else if (alertLevel.equals(AlertLevel.AlertLevel3))
		{
			return Language.AlertLevelPrompt3;
		}
		throw new IllegalArgumentException(alertLevel + " is not a valid AlertLevel");
	}

	public static AlertLevel parse(String value)
	{
		if (value.equals(Language.AlertLevelPrompt0))
		{
			return AlertLevel.AlertLevel0;
		}
		else if (value.equals(Language.AlertLevelPrompt1))
		{
			return AlertLevel.AlertLevel1;
		}
		else if (value.equals(Language.AlertLevelPrompt2))
		{
			return AlertLevel.AlertLevel2;
		}
		else if (value.equals(Language.AlertLevelPrompt3))
		{
			return AlertLevel.AlertLevel3;
		}
		throw new IllegalArgumentException(value + " is not a valid AlertLevel");
	}
}
