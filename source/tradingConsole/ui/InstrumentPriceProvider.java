package tradingConsole.ui;

import tradingConsole.Instrument;

public class InstrumentPriceProvider implements IPriceProvider
{
	private Instrument _instrument;

	public InstrumentPriceProvider(Instrument instrument)
	{
		this._instrument = instrument;
	}

	/*public String getBidLeft()
	{
		return this._instrument.get_IsSinglePrice() ? this._instrument.getAskLeft() : this._instrument.getBidLeft();
	}

	public String getBidRight()
	{
		return this._instrument.get_IsSinglePrice() ? this._instrument.getAskRight() : this._instrument.getBidRight();
	}

	public String getAskLeft()
	{
		return this._instrument.getAskLeft();
	}

	public String getAskRight()
	{
		return this._instrument.getAskRight();
	}*/

	public String get_Bid()
	{
		return this._instrument.get_IsSinglePrice() ? this._instrument.get_Ask() : this._instrument.get_Bid();
	}

	public String get_Ask()
	{
		return this._instrument.get_Ask();
	}

	public boolean isNormal()
	{
		return this._instrument.get_IsNormal();
	}
}
