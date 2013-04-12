package tradingConsole.enumDefine;

import framework.lang.Enum;

public class MoveDirection extends Enum<MoveDirection>
{
	public static final MoveDirection Up = new MoveDirection("Up", 0);
	public static final MoveDirection Down = new MoveDirection("Down", 1);

	private MoveDirection(String name, int value)
	{
		super(name, value);
	}
}
