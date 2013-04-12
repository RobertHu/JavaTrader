package tradingConsole.enumDefine;

import framework.lang.Enum;

public class TransactionSubType extends Enum<TransactionSubType>
{
	public static final TransactionSubType None = new TransactionSubType("None", 0);
	public static final TransactionSubType Amend = new TransactionSubType("Amend", 1);
	public static final TransactionSubType IfDone = new TransactionSubType("IfDone", 2);
	public static final TransactionSubType Match = new TransactionSubType("Match", 3);
	public static final TransactionSubType Assign = new TransactionSubType("Assign", 4); //AssigningOrderID == AssigningOrderID (id of the order been assigned from) //NotImplemented
    public static final TransactionSubType Mapping = new TransactionSubType("Mapping", 5);

	private TransactionSubType(String name, int value)
	{
		super(name, value);
	}
}
