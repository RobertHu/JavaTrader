package tradingConsole.common;

public class AppType
{
	private int value;
	public int toValue()
	{
		return value;
	}

	private AppType(int value)
	{
		this.value = value;
	}

	public static final AppType
		StateServer = new AppType(0),
		QuotationServer = new AppType(1),
		TransactionServer = new AppType(2),
		DealingConsoleServer = new AppType(3),
		QuotationCollector = new AppType(4),
		BackOffice = new AppType(5),
		DealingConsole = new AppType(6),
		TradingConsole = new AppType(7),
		RiskMonitor = new AppType(8),
		TradingMonitor = new AppType(9),
		Mobile = new AppType(10);

	public static final AppType[] appType =
		{
		StateServer,
		QuotationServer,
		TransactionServer,
		DealingConsoleServer,
		QuotationCollector,
		BackOffice,
		DealingConsole,
		TradingConsole,
		RiskMonitor,
		TradingMonitor,
		Mobile
	};

	public static final AppType number(int ord)
	{
		return appType[ord];
	}

	public int compareTo(AppType appType)
	{
		if (this.value != appType.toValue())
		{
			return (this.value < appType.toValue()) ? -1 : 1;
		}
		return 0;
	}
}
