package tradingConsole;

import framework.data.DataRow;
import framework.Guid;
import tradingConsole.settings.SettingsManager;
import framework.DateTime;

public class LastQuotation extends Quotation
{
	public LastQuotation(Instrument instrument, SettingsManager settingsManager)
	{
		super(instrument, settingsManager);
	}

	public Price get_Bid()
	{
		if(!this._instrument.isFromBursa()) return super.get_Bid();
		if(this._instrument.get_BestLimits() == null) return null;

		BestLimit bestLimit = this._instrument.get_IsNormal() ? this._instrument.get_BestLimits().getBestBuy() : this._instrument.get_BestLimits().getBestSell();
		return bestLimit == null ? super.get_Bid() : Price.create(bestLimit.get_Price(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
	}

	public Price get_Ask()
	{
		if(!this._instrument.isFromBursa()) return super.get_Ask();
		if(this._instrument.get_BestLimits() == null) return null;

		BestLimit bestLimit = this._instrument.get_IsNormal() ? this._instrument.get_BestLimits().getBestSell() : this._instrument.get_BestLimits().getBestBuy();
		return bestLimit == null ? super.get_Ask() : Price.create(bestLimit.get_Price(), this._instrument.get_NumeratorUnit(), this._instrument.get_Denominator());
	}

	public Price getBuySell(boolean isBuy)
	{
		if(!this._instrument.isFromBursa()) return super.getBuySell(isBuy);
		return (this._instrument.get_IsNormal() ^ isBuy) ? Price.clone(this.get_Bid()) : Price.clone(this.get_Ask());
	}

	public DateTime get_Timestamp()
	{
		if(!this._instrument.isFromBursa()) return super.get_Timestamp();
		if(this._instrument.get_BestLimits() == null) return null;
		BestLimit bestLimit = this._instrument.get_IsNormal() ? this._instrument.get_BestLimits().getBestSell() : this._instrument.get_BestLimits().getBestBuy();
		return bestLimit == null ? super.get_Timestamp() : bestLimit.get_Timestamp();
	}
}
