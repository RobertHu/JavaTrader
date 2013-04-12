package tradingConsole.enumDefine;

import framework.lang.Enum;

public class UserRelation extends Enum<UserRelation>
{
	public static final UserRelation Customer = new UserRelation("Customer", 0);
	public static final UserRelation Sales = new UserRelation("Sales", 1);
	public static final UserRelation Manager = new UserRelation("Manager", 2);

	private UserRelation(String name, int value)
	{
		super(name, value);
	}
}
