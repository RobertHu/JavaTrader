package tradingConsole.enumDefine;

import framework.lang.Enum;

public class BSStatus extends Enum<BSStatus>
{
	public static final BSStatus None = new BSStatus("None", -1);
	public static final BSStatus HasSellOnly = new BSStatus("HasSellOnly", 0);
	public static final BSStatus HasBuyOnly = new BSStatus("HasBuyOnly", 1);
	public static final BSStatus Both = new BSStatus("Both", 2);

	private BSStatus(String name, int value)
	{
		super(name, value);
	}
}
