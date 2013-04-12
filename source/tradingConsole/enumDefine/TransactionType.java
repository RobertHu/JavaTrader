package tradingConsole.enumDefine;

import framework.lang.Enum;

public class TransactionType extends Enum<TransactionType>
{
	public static final TransactionType Single = new TransactionType("Single", 0);
	public static final TransactionType Pair = new TransactionType("Pair", 1);
	public static final TransactionType OneCancelOther = new TransactionType("OneCancelOther", 2);
	public static final TransactionType Mapping = new TransactionType("Mapping", 3);
	public static final TransactionType MultipleClose = new TransactionType("MultipleClose", 4);
	public static final TransactionType Assign = new TransactionType("Assign", 100);

	private TransactionType(String name, int value)
	{
		super(name, value);
	}
}
