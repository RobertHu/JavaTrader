package tradingConsole;

public class TradingItem
{
	private double _interest;
	private double _storage;
	private double _trade;
	private double _valueAsMargin;
	private double _physicalTrade;

	public double get_Trade()
	{
		return this._trade;
	}

	public void set_Trade(double value)
	{
		this._trade = value;
	}

	public double get_PhysicalTrade()
	{
		return this._physicalTrade;
	}

	public void set_PhysicalTrade(double value)
	{
		this._physicalTrade = value;
	}

	public double get_Interest()
	{
		return this._interest;
	}

	public void set_Interest(double value)
	{
		this._interest = value;
	}

	public double get_Storage()
	{
		return this._storage;
	}

	public void set_Storage(double value)
	{
		this._storage = value;
	}

	public void set_ValueAsMargin(double value)
	{
		this._valueAsMargin = value;
	}

	public double get_ValueAsMargin()
	{
		return this._valueAsMargin;
	}

	private TradingItem()
	{
	}

	private TradingItem(double interest,double storage,double trade,double valueAsMargin, double physicalTrade)
	{
		this._interest = interest;
		this._storage = storage;
		this._trade = trade;
		this._valueAsMargin = valueAsMargin;
		this._physicalTrade = physicalTrade;
	}

	public static TradingItem create()
	{
		return new TradingItem(0,0,0,0,0);
	}

	public static TradingItem create(double interest,double storage,double trade,double valueAsMargin,double physicalTrade)
	{
		return new TradingItem(interest,storage,trade,valueAsMargin,physicalTrade);
	}

	public static TradingItem add(TradingItem tradingItem,TradingItem tradingItem2)
	{
		return TradingItem.create(tradingItem.get_Interest() + tradingItem2.get_Interest(),
						   tradingItem.get_Storage() + tradingItem2.get_Storage(),
						   tradingItem.get_Trade() + tradingItem2.get_Trade(),
						   tradingItem.get_ValueAsMargin() + tradingItem2.get_ValueAsMargin(),
						   tradingItem.get_PhysicalTrade() + tradingItem2.get_PhysicalTrade());
	}

	public static TradingItem subStract(TradingItem tradingItem,TradingItem tradingItem2)
	{
		return TradingItem.create(tradingItem.get_Interest() - tradingItem2.get_Interest(),
						   tradingItem.get_Storage() - tradingItem2.get_Storage(),
						   tradingItem.get_Trade() - tradingItem2.get_Trade(),
						   tradingItem.get_ValueAsMargin() - tradingItem2.get_ValueAsMargin(),
						   tradingItem.get_PhysicalTrade() - tradingItem2.get_PhysicalTrade());
	}

	public static double sum(TradingItem tradingItem)
	{
		return tradingItem.get_Interest() + tradingItem.get_Storage()
			+ tradingItem.get_Trade() + tradingItem.get_ValueAsMargin()
			+ tradingItem.get_PhysicalTrade();
	}

	public static TradingItem exchange(TradingItem tradingItem,CurrencyRate currencyRate)
	{
		return tradingItem.create(currencyRate.exchange(tradingItem.get_Interest()),
								  currencyRate.exchange(tradingItem.get_Storage()),
								  currencyRate.exchange(tradingItem.get_Trade()),
								  tradingItem.get_ValueAsMargin(),
								  currencyRate.exchange(tradingItem.get_PhysicalTrade()));
	}

	public void clear()
	{
		this._interest = 0.00;
		this._storage = 0.00;
		this._trade = 0.00;
		this._valueAsMargin = 0.00;
		this._physicalTrade = 0.00;
	}

	public TradingItem clone()
	{
		TradingItem tradingItem = TradingItem.create(this._interest,this._storage,this._trade,this._valueAsMargin,this._physicalTrade);
		return tradingItem;
	}

	public void merge(TradingItem tradingItem)
	{
		this._interest = tradingItem.get_Interest();
		this._storage = tradingItem.get_Storage();
		this._trade = tradingItem.get_Trade();
		this._valueAsMargin = tradingItem._valueAsMargin;
		this._physicalTrade = tradingItem._physicalTrade;
	}
}
