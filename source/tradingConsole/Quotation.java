package tradingConsole;

import java.math.*;

import java.awt.*;
import javax.swing.*;

import framework.*;
import framework.data.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import framework.diagnostics.TraceType;

public class Quotation
{
	private static Icon _picDown;
	private static Icon _picNotChange;
	private static Icon _picUp;

	protected SettingsManager _settingsManager;

	protected Guid _instrumentId; //as instrumentId, instrument 1<---->1 Quotation
	protected DateTime _timestamp;
	protected Price _bid;
	protected Price _ask;
	protected Price _high;
	protected Price _low;
	protected Price _open;
	protected String _volume;
	protected String _totalVolume;
	protected boolean _isPrivateOpen;
	protected Price _prevClose;
	protected boolean _isPrivateClose;
	protected boolean _isPrivateBid;
	protected boolean _isPrivateAsk;

	protected Price _lastAsk;
	protected Price _lastBid;
	protected double _change;

	protected QuotationStatus _bidStatus;
	protected QuotationStatus _askStatus;
	protected QuotationStatus _changeStatus;

	//when quote arrived, set _isQuote=true, then Instrument.get_Quotation and Instrument.update
	private boolean _isQuote;
	//exclude quote
	//when user is using quotation, set isUsing = true
	//when user has used quotation, set isUsing = false,
	//then if isChangeQuotation = true, call Instrument.get_Quotation and Instrument.update
	private boolean _isUsing;
	//exclude quote
	//when quotation change,set isChangeQuotation = true
	//when isUsing = true and isChangeQuotation = false, can not call Instrument.get_Quotation and Instrument.update
	//otherwise, can call them
	private boolean _isChangedQuotation;

	protected Instrument _instrument;

	private BigDecimal _answerLot;

	public synchronized static Icon get_PicDown()
	{
		if (Quotation._picDown == null)
		{
			Quotation._picDown = AppToolkit.getAsIcon("PicDown.gif");
		}
		return Quotation._picDown;
	}

	public synchronized static Icon get_PicUp()
	{
		if (Quotation._picUp == null)
		{
			Quotation._picUp = AppToolkit.getAsIcon("PicUp.gif");
		}
		return Quotation._picUp;
	}

	public synchronized static Icon get_PicNotChange()
	{
		if (Quotation._picNotChange == null)
		{
			Quotation._picNotChange = AppToolkit.getAsIcon("PicNotChange.gif");
		}
		return Quotation._picNotChange;
	}

	private Instrument get_Instrument()
	{
		return this._instrument;
	}

	public boolean get_IsQuote()
	{
		return this._isQuote;
	}

	public void set_IsQuote(boolean value)
	{
		if(this._instrument.get_Id().equals(Instrument.DebugInstrumentId) || this._instrument.get_Id().equals(Instrument.DebugInstrumentId2))
		{
			TradingConsole.traceSource.trace(TraceType.Information, "[2]IsQuote = " + value);
		}
		this._isQuote = value;
	}

	public BigDecimal get_AnswerLot()
	{
		return this._answerLot;
	}

	public void set_AnswerLot(BigDecimal value)
	{
		this._answerLot = value;
	}

	public boolean get_IsUsing()
	{
		return this._isUsing;
	}

	public void set_IsUsing(boolean value)
	{
		this._isUsing = value;
	}

	public boolean get_IsChangedQuotation()
	{
		return this._isChangedQuotation;
	}

	public void set_IsChangedQuotation(boolean value)
	{
		this._isChangedQuotation = value;
	}

	public Icon getBidImage()
	{
		if (this._bidStatus.equals(QuotationStatus.Up))
		{
			return Quotation.get_PicUp();
		}
		else if (this._bidStatus.equals(QuotationStatus.Down))
		{
			return Quotation.get_PicDown();
		}
		else
		{
			return Quotation.get_PicNotChange();
		}
	}

	public Color getBidForeColor()
	{
		return getForeColor(this._bidStatus);
	}

	public Color getAskForeColor()
	{
		return getForeColor(this._askStatus);
	}

	public Color getBidBackColor()
	{
		return getBackColor(this._bidStatus);
	}

	public Color getAskBackColor()
	{
		return getBackColor(this._askStatus);
	}

	public Color getChangeColor()
	{
		return getForeColor(this._changeStatus);
	}

	private Color getForeColor(QuotationStatus quotationStatus)
	{
		return QuotationStatus.getColor(quotationStatus);
	}

	private Color getBackColor(QuotationStatus quotationStatus)
	{
		return QuotationStatus.getBackColor(quotationStatus);
	}

	public Guid get_Id()
	{
		return this._instrumentId;
	}

	public DateTime get_Timestamp()
	{
		return this._timestamp;
	}

	public Price get_Bid()
	{
		return this._bid;
	}

	public Price get_Ask()
	{
		return this._ask;
	}

	public Price get_High()
	{
		return this._high;
	}

	public Price get_Low()
	{
		return this._low;
	}

	public Price get_Open()
	{
		return this._open;
	}

	public String get_Volume()
	{
		return this._volume;
	}

	public String get_TotalVolume()
	{
		return this._totalVolume;
	}

	public boolean get_IsPrivateOpen()
	{
		return this._isPrivateOpen;
	}

	public Price get_PrevClose()
	{
		return this._prevClose;
	}

	public boolean get_IsPrivateAsk()
	{
		return this._isPrivateAsk;
	}

	public boolean get_IsPrivateBid()
	{
		return this._isPrivateBid;
	}

	public boolean get_IsPrivateClose()
	{
		return this._isPrivateClose;
	}

	public double get_Change()
	{
		return this._change;
	}

	public Price get_LastForBursa()
	{
		if(!this._instrument.isFromBursa()) throw new UnsupportedOperationException("Instrument is not from Bursa");

		return this._ask;
	}

	public String getLast()
	{
		if(this._instrument.isFromBursa())
		{
			return Price.toString(this._ask);
		}
		else
		{
			String lastString = ( (this._lastBid == null) ? "" : Price.toString(this._lastBid)) + "/" +
				( (this._lastAsk == null) ? "" : Price.getPriceRight(this._lastAsk));
			return (lastString.equals("/")) ? "" : lastString;
		}
	}

	public QuotationStatus get_QuotationStatus()
	{
		return this._bidStatus;
	}

	public Quotation(Instrument instrument, SettingsManager settingsManager)
	{
		this._settingsManager = settingsManager;

		this._instrumentId = instrument.get_Id();
		this._instrument = instrument;

		this._isQuote = false;
		if(this._instrument.get_Id().equals(Instrument.DebugInstrumentId) || this._instrument.get_Id().equals(Instrument.DebugInstrumentId2))
		{
			TradingConsole.traceSource.trace(TraceType.Information, "[1]IsQuote = false");
		}

		this._isUsing = false;
		this._isChangedQuotation = false;

		this._ask = null;
		this._bid = null;
		this._high = null;
		this._low = null;
		this._open = null;
		this._prevClose = null;
		this._lastAsk = null;
		this._lastBid = null;

		this._change = 0;

		this._bidStatus = QuotationStatus.None;
		this._askStatus = QuotationStatus.None;
		this._changeStatus = QuotationStatus.None;
	}

	public Quotation(Instrument instrument, SettingsManager settingsManager, DataRow dataRow)
	{
		this(instrument, settingsManager);

		//this._id = (Guid) (dataRow.get_Item("InstrumentID"));
		//this._instrument = this._settingsManager.getInstrument(this._id);
		this.setValue(dataRow);
	}

	public Price getBuy()
	{
		return this.getBuySell(true);
	}

	public Price getSell()
	{
		return this.getBuySell(false);
	}

	public Price getBuySell(boolean isBuy)
	{
		return (this.get_Instrument().get_IsNormal() ^ isBuy) ? Price.clone(this._bid) : Price.clone(this._ask);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private String castPriceString(Object item)
	{
		return (AppToolkit.isDBNull(item)) ? null : (String)item;
	}

	private void setValue(DataRow dataRow)
	{
		int numeratorUnit = this.get_Instrument().get_NumeratorUnit();
		int denominator = this.get_Instrument().get_Denominator();
		this._timestamp = AppToolkit.getDateTime(((DateTime)dataRow.get_Item("Timestamp")).toString(DateTime.defaultFormat));
		//Modified by Michael on 2008-04-22
		this._bid = Price.parse(this.castPriceString(dataRow.get_Item("Bid")), numeratorUnit, denominator);
		this._ask = Price.parse(this.castPriceString(dataRow.get_Item("Ask")), numeratorUnit, denominator);
		this._high = Price.parse(this.castPriceString(dataRow.get_Item("High")), numeratorUnit, denominator);
		this._low = Price.parse(this.castPriceString(dataRow.get_Item("Low")), numeratorUnit, denominator);
		this._open = Price.parse(this.castPriceString(dataRow.get_Item("Open")), numeratorUnit, denominator);
		if(dataRow.get_Table().get_Columns().contains("Volume")) this._volume = dataRow.get_Item("Volume").toString();
		if(dataRow.get_Table().get_Columns().contains("TotalVolume")) this._totalVolume = dataRow.get_Item("TotalVolume").toString();
		if(dataRow.get_Table().get_Columns().contains("IsPrivateDailyAsk")) this._isPrivateAsk = (Boolean)dataRow.get_Item("IsPrivateDailyAsk");
		if(dataRow.get_Table().get_Columns().contains("IsPrivateDailyBid")) this._isPrivateBid = (Boolean)dataRow.get_Item("IsPrivateDailyBid");
		//this._bid = Price.parse(dataRow.get_Item("Bid").toString(), numeratorUnit, denominator);
		//this._ask = Price.parse(dataRow.get_Item("Ask").toString(), numeratorUnit, denominator);
		//this._high = Price.parse(dataRow.get_Item("High").toString(), numeratorUnit, denominator);
		//this._low = Price.parse(dataRow.get_Item("Low").toString(), numeratorUnit, denominator);
		//this._open = Price.parse(dataRow.get_Item("Open").toString(), numeratorUnit, denominator);
		this._isPrivateOpen = (Boolean)dataRow.get_Item("IsPrivateOpen");
		this._prevClose = Price.parse(this.castPriceString(dataRow.get_Item("PrevClose")), numeratorUnit, denominator);
		//this._prevClose = Price.parse(dataRow.get_Item("PrevClose").toString(), numeratorUnit, denominator);
		this._isPrivateClose = (Boolean)dataRow.get_Item("IsPrivateClose");

		this._change = this.getChange();
		this._changeStatus = this.getQuotationStatus(this._change);
	}

	//by update call
	public void setOpen(boolean isPrivateOpen, Price open)
	{
		this._isPrivateOpen = isPrivateOpen;
		this._open = open;
	}

	public void setAsk(boolean isPrivateAsk, Price ask)
	{
		this._isPrivateAsk = isPrivateAsk;
		this._ask = ask;
	}

	public void setBid(boolean isPrivateBid, Price bid)
	{
		this._isPrivateBid = isPrivateBid;
		this._bid = bid;
	}

	//by update call
	public void setPrevClose(boolean isPrivateClose, Price prevClose)
	{
		this._isPrivateClose = isPrivateClose;
		this._prevClose = prevClose;

		this._change = this.getChange();
		this._changeStatus = this.getQuotationStatus(this._change);
	}

	private double getChange()
	{
		double change;
		if (this._bid == null || this._prevClose == null)
		{
			change = 0;
		}
		else
		{
			/*if(this._settingsManager.get_SystemParameter().get_CaculateChangeWithDenominator())
			{
				change = Price.subStract(this._bid, this._prevClose);
			}
			else*/
			{
				change = Price.valueSubStract(this._bid, this._prevClose);
			}
		}
		return change;
	}

	private QuotationStatus getQuotationStatus(double value)
	{
		if (value > 0)
		{
			return QuotationStatus.Up;
		}
		else if (value < 0)
		{
			return QuotationStatus.Down;
		}
		else
		{
			return QuotationStatus.NotChange;
		}
	}

	private QuotationStatus getQuotationStatus(Price price, Price lastPrice)
	{
		if (Price.more(price, lastPrice))
		{
			return QuotationStatus.Up;
		}
		else if (Price.less(price, lastPrice))
		{
			return QuotationStatus.Down;
		}
		else
		{
			return QuotationStatus.NotChange;
		}
	}

	public void parse(String[] quotationString, boolean isQuote)
	{
		this._isQuote = isQuote;
		if(this._instrument.get_Id().equals(Instrument.DebugInstrumentId) || this._instrument.get_Id().equals(Instrument.DebugInstrumentId2))
		{
			TradingConsole.traceSource.trace(TraceType.Information, "[3]IsQuote = " + isQuote);
		}

		this._isChangedQuotation = true;

		boolean isRequireModifyChange = false;
		int numeratorUnit = this.get_Instrument().get_NumeratorUnit();
		int denominator = this.get_Instrument().get_Denominator();
		for (int i = 0, iCount = quotationString.length; i < iCount; i++)
		{
			String quotationItem = quotationString[i];
			switch (i)
			{
				case 0:
					this._instrumentId = new Guid(quotationItem);
					continue;
				case 1:
					this._timestamp = AppToolkit.getDateTime(quotationItem);
					continue;
				case 2:
					if (!StringHelper.isNullOrEmpty(quotationItem))
					{
						if (this._ask != null)
						{
							this._lastAsk = Price.clone(this._ask);
						}
						this._ask = Price.parse(quotationItem, numeratorUnit, denominator);
						this._askStatus = this.getQuotationStatus(this._ask, this._lastAsk);
					}
					continue;
				case 3:
					if (!StringHelper.isNullOrEmpty(quotationItem))
					{
						isRequireModifyChange = true;
						if (this._bid != null)
						{
							this._lastBid = Price.clone(this._bid);
						}
						this._bid = Price.parse(quotationItem, numeratorUnit, denominator);
						this._bidStatus = this.getQuotationStatus(this._bid, this._lastBid);
					}
					continue;
				case 4:
					if (!StringHelper.isNullOrEmpty(quotationItem))
					{
						this._high = Price.parse(quotationItem, numeratorUnit, denominator);
					}
					continue;
				case 5:
					if (!StringHelper.isNullOrEmpty(quotationItem))
					{
						this._low = Price.parse(quotationItem, numeratorUnit, denominator);
					}
					continue;
				case 6:
					if (!StringHelper.isNullOrEmpty(quotationItem))
					{
						if(this._instrument.isFromBursa())
						{
							this._volume = quotationItem;
						}
						else
						{
							this._open = Price.parse(quotationItem, numeratorUnit, denominator);
						}
					}
					continue;
				case 7:
					if (!StringHelper.isNullOrEmpty(quotationItem))
					{
						if(this._instrument.isFromBursa())
						{
							this._totalVolume = quotationItem;
						}
						else
						{
							this._prevClose = Price.parse(quotationItem, numeratorUnit, denominator);
							isRequireModifyChange = true;
						}
					}
					continue;
			}
		}
		if (isRequireModifyChange)
		{
			this._change = this.getChange();
			this._changeStatus = this.getQuotationStatus(this._change);
		}
	}

	public boolean equals(Quotation quotation)
	{
		return (this._timestamp == quotation.get_Timestamp()
				&& Price.equals(this._ask, quotation._ask)
				&& Price.equals(this._bid, quotation._bid)
				&& Price.equals(this._high, quotation._high)
				&& Price.equals(this._low, quotation._low)
				&& Price.equals(this._open, quotation._open)
				&& Price.equals(this._prevClose, quotation._prevClose));
	}

	//set lastQuotation to quotation
	//should modify...........
	public void merge(Quotation quotation)
	{
		this._instrumentId = quotation._instrumentId;
		this._timestamp = quotation._timestamp;
		this._ask = Price.clone(quotation._ask);
		this._bid = Price.clone(quotation._bid);
		this._high = Price.clone(quotation._high);
		this._low = Price.clone(quotation._low);
		this._open = Price.clone(quotation._open);
		this._prevClose = Price.clone(quotation._prevClose);
		this._lastAsk = Price.clone(quotation._lastAsk);
		this._lastBid = Price.clone(quotation._lastBid);

		this._change = quotation._change;

		this._bidStatus = quotation._bidStatus;
		this._askStatus = quotation._askStatus;
		this._changeStatus = quotation._changeStatus;

		this._isPrivateOpen = quotation._isPrivateOpen;
		this._isPrivateClose = quotation._isPrivateClose;
		this._volume = quotation._volume;
		this._totalVolume = quotation._totalVolume;

		this._instrument = quotation._instrument;
	}

	public Quotation clone()
	{
		Quotation quotation = new Quotation(this._instrument, this._settingsManager);

		quotation._instrumentId = this._instrumentId;
		quotation._timestamp = this._timestamp;
		quotation._ask = Price.clone(this._ask);
		quotation._bid = Price.clone(this._bid);
		quotation._high = Price.clone(this._high);
		quotation._low = Price.clone(this._low);
		quotation._open = Price.clone(this._open);
		quotation._prevClose = Price.clone(this._prevClose);
		quotation._lastAsk = Price.clone(this._lastAsk);
		quotation._lastBid = Price.clone(this._lastBid);

		quotation._change = this._change;

		quotation._bidStatus = this._bidStatus;
		quotation._askStatus = this._askStatus;
		quotation._changeStatus = this._changeStatus;

		quotation._instrument = this._instrument;

		quotation._isPrivateOpen = this._isPrivateOpen;
		quotation._isPrivateClose = this._isPrivateClose;
		quotation._volume = this._volume;
		quotation._totalVolume = this._totalVolume;
		quotation._isQuote = this._isQuote;
		if(this._instrument.get_Id().equals(Instrument.DebugInstrumentId) || this._instrument.get_Id().equals(Instrument.DebugInstrumentId2))
		{
			TradingConsole.traceSource.trace(TraceType.Information, "[4]IsQuote = " + this._isQuote);
		}


		return quotation;
	}

	public void clear()
	{
		//this._isQuote = false;
		//this._isUsing = false;
		this._isChangedQuotation = true;//false;

		this._timestamp = null;
		this._lastAsk = Price.clone(this._ask);
		this._lastBid = Price.clone(this._bid);
		this._ask = null;
		this._bid = null;
		this._high = null;
		this._low = null;

		this._change = 0;

		this._bidStatus = QuotationStatus.None;
		this._askStatus = QuotationStatus.None;
		this._changeStatus = QuotationStatus.None;
	}

	public static String priceAssembly(Price ask, Price bid)
	{
		String prices = "";
		String askString = Price.toString(ask);
		String bidString = Price.toString(bid);

		if (!askString.equals("") && !bidString.equals(""))
		{
			prices = askString + "/" + bidString;
		}
		else if (!askString.equals(""))
		{
			prices = askString;
		}
		else if (!bidString.equals(""))
		{
			prices = bidString;
		}
		return prices;
	}

	public void clearAnswerLot()
	{
		this._answerLot = null;
	}

	public String toLogString()
	{
		return StringHelper.format("Ask = {0}, Bid= {1}, TimeStamp = {2}, IsQuote = {3}",
								   new Object[]{ Price.toString(this._ask), Price.toString(this._bid), this._timestamp, this._isQuote});
	}
}
