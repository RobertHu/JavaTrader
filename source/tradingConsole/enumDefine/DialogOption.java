package tradingConsole.enumDefine;

import framework.lang.Enum;

public class DialogOption extends Enum<DialogOption>
{
	public static final DialogOption Yes = new DialogOption("Yes", 0);
	public static final DialogOption No = new DialogOption("No", 1);
	public static final DialogOption Ok = new DialogOption("Ok", 2);
	public static final DialogOption Cancel = new DialogOption("Cancel", 3);

	private DialogOption(String name, int value)
	{
		super(name, value);
	}
}
