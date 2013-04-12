package tradingConsole.ui;

import framework.DateTime;
import tradingConsole.Price;
import tradingConsole.Quotation;

public class PlaceOrderInfo
{
	private DateTime _timestampOfLastQuotation;
	private String _bidOfLastQuotation;
	private String _askOfLastQuotation;
	private String _bidOnUI;
	private String _askOnUI;

	public DateTime get_TimestampOfLastQuotation()
	{
		return this._timestampOfLastQuotation;
	}

	public String get_BidOfLastQuotation()
	{
		return this._bidOfLastQuotation;
	}

	public String get_AskOfLastQuotation()
	{
		return this._askOfLastQuotation;
	}

	public String get_BidOnUI()
	{
		return this._bidOnUI;
	}

	public String get_AskOnUI()
	{
		return this._askOnUI;
	}

	public PlaceOrderInfo(Quotation lastQuotation, String bidOnUI, String askOnUI)
	{
		this._timestampOfLastQuotation = lastQuotation.get_Timestamp();
		this._bidOfLastQuotation = Price.toString(lastQuotation.get_Bid());
		this._askOfLastQuotation = Price.toString(lastQuotation.get_Ask());
		this._bidOnUI = bidOnUI;
		this._askOnUI = askOnUI;
	}
}
