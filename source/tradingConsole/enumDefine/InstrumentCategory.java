package tradingConsole.enumDefine;

import framework.lang.Enum;

public class InstrumentCategory extends Enum<InstrumentCategory>
{
	public static final InstrumentCategory Margin = new InstrumentCategory("Margin", 10);
	public static final InstrumentCategory Physical = new InstrumentCategory("Physical", 20);

	private InstrumentCategory(String name, int value)
	{
		super(name, value);
	}
}
