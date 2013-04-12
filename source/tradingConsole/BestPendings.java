package tradingConsole;

import java.util.*;

import framework.*;
import tradingConsole.ui.grid.*;
import javax.swing.SwingConstants;

public class BestPendings
{
	private Guid _instrumentId;

	private HashMap<Guid, BindingSource> _bestBuys = new HashMap<Guid, BindingSource>();
	private HashMap<Guid, BindingSource> _bestSells = new HashMap<Guid, BindingSource>();

	public BestPendings(Guid instrumentId)
	{
		this._instrumentId = instrumentId;
	}

	public BindingSource getBestBuys(Guid accountId)
	{
		BindingSource bestBuys = this._bestBuys.get(accountId);
		if(bestBuys == null)
		{
			bestBuys = new BindingSource();
			bestBuys.setHorizontalAlignment(SwingConstants.CENTER);
			String dataSourceKey = this._instrumentId.toString() + accountId.toString() + "BestBuys";
			TradingConsole.bindingManager.bind(dataSourceKey, new Vector(), bestBuys, BestPending.getPropertyDescriptors());
			this._bestBuys.put(accountId, bestBuys);
		}
		return bestBuys;
	}

	public BindingSource getBestSells(Guid accountId)
	{
		BindingSource bestSells = this._bestSells.get(accountId);
		if(bestSells == null)
		{
			bestSells = new BindingSource();
			bestSells.setHorizontalAlignment(SwingConstants.CENTER);
			String dataSourceKey = this._instrumentId.toString() + accountId.toString() + "BestSells";
			TradingConsole.bindingManager.bind(dataSourceKey, new Vector(), bestSells, BestPending.getPropertyDescriptors());
			this._bestSells.put(accountId, bestSells);
		}
		return bestSells;
	}

	public void set(boolean isBuy, Guid[] accountIds, BestPending[] bestPendings)
	{
		for(Guid accountId : accountIds)
		{
			BindingSource bestPendingBindingSource = isBuy ? this.getBestBuys(accountId) : this.getBestSells(accountId);
			bestPendingBindingSource.removeAll();
			for(BestPending bestPending : bestPendings)
			{
				bestPendingBindingSource.add(bestPending);
			}
		}
	}

	public void add(boolean isBuy, Guid[] accounts, BestPending bestPending)
	{
		for(Guid accountId : accounts)
		{
			BindingSource bestPendingBindingSource = isBuy ? this.getBestBuys(accountId) : this.getBestSells(accountId);

			boolean added = false;
			for(int index = 0; index < bestPendingBindingSource.getRowCount(); index++)
			{
				BestPending item = (BestPending)bestPendingBindingSource.getObject(index);
				if(item.get_Sequence() > bestPending.get_Sequence())
				{
					bestPendingBindingSource.insert(index, bestPending);
					added = true;
					break;
				}
			}
			if(!added) bestPendingBindingSource.add(bestPending);
		}
	}

	public void clear()
	{
		for(BindingSource bestBuys : this._bestBuys.values())
		{
			bestBuys.removeAll();
		}
		for(BindingSource bestSells : this._bestSells.values())
		{
			bestSells.removeAll();
		}
	}
}
