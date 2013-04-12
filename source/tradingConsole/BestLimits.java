package tradingConsole;

import java.util.*;

import framework.*;
import tradingConsole.ui.grid.*;
import javax.swing.SwingConstants;
import tradingConsole.ui.colorHelper.GridFixedForeColor;
import tradingConsole.ui.fontHelper.HeaderFont;
import java.awt.Color;

public class BestLimits
{
	private Guid _instrumentId;
	BindingSource _bestBuys = new BindingSource();
	BindingSource _bestSells = new BindingSource();

	public BestLimits(Guid instrumentId)
	{
		this._instrumentId = instrumentId;

		String dataSourceKey = this._instrumentId.toString() + "BestBuyLimits";
		TradingConsole.bindingManager.bind(dataSourceKey, new Vector(), this._bestBuys, BestLimit.getPropertyDescriptors());

		dataSourceKey = this._instrumentId.toString() + "BestSellLimits";
		TradingConsole.bindingManager.bind(dataSourceKey, new Vector(), this._bestSells, BestLimit.getPropertyDescriptors());
	}

	public Guid get_InstrumentId()
	{
		return this._instrumentId;
	}

	public BindingSource get_BetsBuys()
	{
		return this._bestBuys;
	}

	public BindingSource get_BestSells()
	{
		return this._bestSells;
	}


	public void set(BestLimit bestLimit)
	{
		String sourceKey = this._instrumentId.toString() + (bestLimit.get_IsBuy() ? "BestBuyLimits" : "BestSellLimits");
		if (bestLimit.get_IsBuy())
		{
			BestLimits.set(this._bestBuys, bestLimit, sourceKey);
		}
		else
		{
			BestLimits.set(this._bestSells, bestLimit, sourceKey);
		}
	}

	public BestLimit getBestBuy()
	{
		if(this._bestBuys.getRowCount() > 0)
		{
			return ((BestLimit)this._bestBuys.getObject(0));
		}
		else
		{
			return null;
		}
	}

	public BestLimit getBestSell()
	{
		if(this._bestSells.getRowCount() > 0)
		{
			return ((BestLimit)this._bestSells.getObject(0));
		}
		else
		{
			return null;
		}
	}

	public boolean contians(BestLimit bestLimit)
	{
		if(bestLimit.get_IsBuy())
		{
			return BestLimits.contians(this._bestBuys, bestLimit);
		}
		else
		{
			return BestLimits.contians(this._bestSells, bestLimit);
		}
	}

	private static void set(BindingSource bestLimits, BestLimit bestLimit, String sourceKey)
	{
		for(int index = 0; index < bestLimits.getRowCount(); index++)
		{
			BestLimit bestLimit2 = (BestLimit)bestLimits.getObject(index);
			if(bestLimit2.get_Sequence() == bestLimit.get_Sequence())
			{
				bestLimit2.updateWith(bestLimit);
				bestLimit2.update(sourceKey);
				return;
			}
		}
		bestLimits.add(bestLimit);
	}

	private static boolean contians(BindingSource bestLimits, BestLimit bestLimit)
	{
		for(int index = 0; index < bestLimits.getRowCount(); index++)
		{
			BestLimit bestLimit2 = (BestLimit)bestLimits.getObject(index);
			if(bestLimit2.get_Price() == bestLimit.get_Price() && bestLimit2.get_Quantity() >= bestLimit.get_Quantity())
			{
				return true;
			}
		}
		return false;
	}
}
