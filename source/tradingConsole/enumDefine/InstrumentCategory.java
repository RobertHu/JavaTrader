package tradingConsole.enumDefine;

import framework.lang.Enum;

public class InstrumentCategory extends Enum<InstrumentCategory>
{
	public static final InstrumentCategory Forex = new InstrumentCategory("Forex", 10);
	public static final InstrumentCategory Futures = new InstrumentCategory("Futures", 20);
	public static final InstrumentCategory Index = new InstrumentCategory("Index", 21);
	public static final InstrumentCategory Order = new InstrumentCategory("Order", 22);

	private InstrumentCategory(String name, int value)
	{
		super(name, value);
	}
}
