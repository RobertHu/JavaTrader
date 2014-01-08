package tradingConsole.settings;

import tradingConsole.enumDefine.physical.InstalmentFrequence;
import java.util.HashMap;

public class InstalmentPeriod
{
	public static final InstalmentPeriod TillPayoffInstalmentPeriod = new InstalmentPeriod(1, InstalmentFrequence.TillPayoff);

	private static HashMap<InstalmentPeriod, InstalmentPeriod> InstalmentPeriods = null;

	private int _period;
	private InstalmentFrequence _frequence;

	public int get_Period()
	{
		return this._period;
	}

	public InstalmentFrequence get_Frequence()
	{
		return this._frequence;
	}

	@Override
	public int hashCode()
	{
		return this._period + this._frequence.value();
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj == null) return false;

		if(obj instanceof InstalmentPeriod)
		{
			InstalmentPeriod period = (InstalmentPeriod)obj;
			return this._period == period._period && this._frequence.equals(period._frequence);
		}
		else
		{
			return false;
		}
	}

	@Override
	public String toString()
	{
		if(this._frequence.equals(InstalmentFrequence.TillPayoff))
		{
			return this._frequence.toLocalString();
		}
		else
		{
			return Integer.toString(this._period) + " " + this._frequence.toLocalString();
		}
	}

	private InstalmentPeriod(int period, InstalmentFrequence frequence)
	{
		this._period = period;
		this._frequence = frequence;
	}

	public synchronized static InstalmentPeriod create(int period, InstalmentFrequence frequence)
	{
		if(InstalmentPeriod.InstalmentPeriods == null)
		{
			InstalmentPeriod.InstalmentPeriods = new HashMap<InstalmentPeriod, InstalmentPeriod>();
			InstalmentPeriod.InstalmentPeriods.put(InstalmentPeriod.TillPayoffInstalmentPeriod, InstalmentPeriod.TillPayoffInstalmentPeriod);
		}

		InstalmentPeriod instalmentPeriod = new InstalmentPeriod(period, frequence);
		if(!InstalmentPeriod.InstalmentPeriods.containsKey(instalmentPeriod))
		{
			InstalmentPeriod.InstalmentPeriods.put(instalmentPeriod, instalmentPeriod);
		}
		return InstalmentPeriod.InstalmentPeriods.get(instalmentPeriod);
	}
}
