package tradingConsole.common;

public class UserType
{
	private int value;
	public int toValue()
	{
		return value;
	}

	private UserType(int value)
	{
		this.value = value;
	}

	public static final UserType
		System = new UserType(0),
		Dealer = new UserType(1),
		Customer = new UserType(2);

	public static final UserType[] userType =
		{
		System,
		Dealer,
		Customer
	};

	public static final UserType number(int ord)
	{
		return userType[ord];
	}

	public int compareTo(UserType userType)
	{
		if (this.value != userType.toValue())
		{
			return (this.value < userType.toValue()) ? -1 : 1;
		}
		return 0;
	}
}
