package tradingConsole.enumDefine;

import java.awt.Color;

import framework.lang.Enum;

import tradingConsole.ui.language.Language;
import tradingConsole.ui.colorHelper.PhaseColor;

public class Phase extends Enum<Phase>
{
	public static final Phase Placing = new Phase("Placing", 255);
	public static final Phase Placed = new Phase("Placed", 0);
	public static final Phase Cancelled = new Phase("Cancelled", 1);
	public static final Phase Executed = new Phase("Executed", 2);
	public static final Phase Completed = new Phase("Completed", 3);
	public static final Phase Deleted = new Phase("Deleted", 4);

	private Phase(String name, int value)
	{
		super(name, value);
	}

	public static String getCaption(Phase phase)
	{
		if (phase.equals(Phase.Placing))
		{
			return Language.PhasePlacedPlacingPrompt;
		}
		else if (phase.equals(Phase.Placed))
		{
			return Language.PhasePlacedUnconfirmedPrompt;
		}
		else if (phase.equals(Phase.Cancelled))
		{
			return Language.PhaseCancelledPrompt;
		}
		else if (phase.equals(Phase.Executed))
		{
			return Language.PhaseExecutedPrompt;
		}
		else if (phase.equals(Phase.Completed))
		{
			return Language.PhaseCompletedPrompt;
		}
		else if (phase.equals(Phase.Deleted))
		{
			return Language.PhaseDeletedPrompt;
		}
		return "";
	}

	public static Color getColor(Phase phase)
	{
		if (phase.equals(Phase.Placing))
		{
			return PhaseColor.placing;
		}
		else if (phase.equals(Phase.Placed))
		{
			return PhaseColor.placed;
		}
		else if (phase.equals(Phase.Cancelled))
		{
			return PhaseColor.cancelled;
		}
		else if (phase.equals(Phase.Executed))
		{
			return PhaseColor.executed;
		}
		else if (phase.equals(Phase.Completed))
		{
			return PhaseColor.completed;
		}
		else if (phase.equals(Phase.Deleted))
		{
			return PhaseColor.deleted;
		}
		return Color.white;
	}

}
