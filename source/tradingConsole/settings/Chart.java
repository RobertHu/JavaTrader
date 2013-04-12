package tradingConsole.settings;

import tradingConsole.enumDefine.DataCycle;

public class Chart
{
	private String _symbol;
	private DataCycle _DataCycle;

	private Chart(String symbol,DataCycle DataCycle)
	{
		this._symbol = symbol;
		this._DataCycle = DataCycle;
	}

	public static Chart create(String symbol,DataCycle DataCycle)
	{
		return new Chart(symbol,DataCycle);
	}

	public boolean equals(Chart chart)
	{
		return (this._symbol.equals(chart._symbol)
			&& this._DataCycle.equals(chart._DataCycle));
	}

}
