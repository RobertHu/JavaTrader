package tradingConsole;

public class TradingItem
{
	private double _interest;
	private double _storage;
	private double _trade;

	public double get_Trade()
	{
		return this._trade;
	}

	public void set_Trade(double value)
	{
		this._trade = value;
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

	private TradingItem()
	{
	}

	private TradingItem(double interest,double storage,double trade)
	{
		this._interest = interest;
		this._storage = storage;
		this._trade = trade;
	}

	public static TradingItem create(double interest,double storage,double trade)
	{
		return new TradingItem(interest,storage,trade);
	}

	public static TradingItem add(TradingItem tradingItem,TradingItem tradingItem2)
	{
		return TradingItem.create(tradingItem.get_Interest() + tradingItem2.get_Interest(),
						   tradingItem.get_Storage() + tradingItem2.get_Storage(),
						   tradingItem.get_Trade() + tradingItem2.get_Trade());
	}

	public static TradingItem subStract(TradingItem tradingItem,TradingItem tradingItem2)
	{
		return TradingItem.create(tradingItem.get_Interest() - tradingItem2.get_Interest(),
						   tradingItem.get_Storage() - tradingItem2.get_Storage(),
						   tradingItem.get_Trade() - tradingItem2.get_Trade());
	}

	public static double sum(TradingItem tradingItem)
	{
		return tradingItem.get_Interest() + tradingItem.get_Storage() + tradingItem.get_Trade();
	}

	public static TradingItem exchange(TradingItem tradingItem,CurrencyRate currencyRate)
	{
		return tradingItem.create(currencyRate.exchange(tradingItem.get_Interest()),
								  currencyRate.exchange(tradingItem.get_Storage()),
								  currencyRate.exchange(tradingItem.get_Trade()));
	}

	public void clear()
	{
		this._interest = 0.00;
		this._storage = 0.00;
		this._trade = 0.00;
	}

	public TradingItem clone()
	{
		return TradingItem.create(this._interest,this._storage,this._trade);
	}

	public void merge(TradingItem tradingItem)
	{
		this._interest = tradingItem.get_Interest();
		this._storage = tradingItem.get_Storage();
		this._trade = tradingItem.get_Trade();
	}

}
