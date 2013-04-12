package tradingConsole;

import java.math.BigDecimal;

import framework.Guid;
import framework.DateTime;
import tradingConsole.settings.SettingsManager;
import framework.data.DataRow;
import framework.Convert;

public class MatchedOrder
{
	private TradingConsole _tradingConsole;
	private SettingsManager _settingsManager;

	private Guid _id;
	private DateTime _timestamp;
	private BigDecimal _lot;
	private Price _bidPrice;
	private Price _askPrice;

	private Instrument _instrument;

	public String get_TimestampString()
	{
		String timestampString = Convert.toString(this._timestamp, "yyyy-MM-dd HH:mm:ss");
		return timestampString;
	}

	public String get_LotString()
	{
		return AppToolkit.getFormatLot(this._lot, true);
	}

	public BigDecimal get_Lot()
	{
		return this._lot;
	}

	public String get_BidPriceString()
	{
		String priceString = Price.toString(this._bidPrice);
		return priceString;
	}

	public String get_AskPriceString()
	{
		String priceString = Price.toString(this._askPrice);
		return priceString;
	}

	public MatchedOrder(TradingConsole tradingConsole, SettingsManager settingsManager)
	{
		this._tradingConsole = tradingConsole;
		this._settingsManager = settingsManager;
	}

	public MatchedOrder(TradingConsole tradingConsole, SettingsManager settingsManager, DataRow dataRow)
	{
		this(tradingConsole, settingsManager);
		this._id = new Guid(dataRow.get_Item("ID").toString());
		this._instrument = this._settingsManager.getInstrument( (Guid) dataRow.get_Item("InstrumentID"));
		this.setValue(dataRow);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		this._timestamp = (DateTime) dataRow.get_Item("Timestamp");
		this._lot = AppToolkit.convertDBValueToBigDecimal(dataRow.get_Item("Lot"), 0.0);
		this._bidPrice = (AppToolkit.isDBNull(dataRow.get_Item("BidPrice"))) ? null :
			Price.parse(dataRow.get_Item("BidPrice").toString(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
		this._askPrice = (AppToolkit.isDBNull(dataRow.get_Item("AskPrice"))) ? null :
			Price.parse(dataRow.get_Item("AskPrice").toString(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
	}

}
