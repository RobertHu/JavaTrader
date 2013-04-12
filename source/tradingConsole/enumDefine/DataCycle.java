package tradingConsole.enumDefine;

import framework.lang.Enum;
import org.aiotrade.math.timeseries.Frequency;
import org.aiotrade.math.timeseries.Unit;

public class DataCycle extends Enum<AccountType>
{
	public static final DataCycle cbDaily = new DataCycle("DAY1", 100001);
	public static final DataCycle cbWeekly = new DataCycle("WEEK1", 100002);
	public static final DataCycle cbMonthly = new DataCycle("MONTH1", 100003);
	public static final DataCycle cbIntraday1 = new DataCycle("MINUTE1", 60);
	public static final DataCycle cbIntraday5 = new DataCycle("MINUTE5", 300);
	public static final DataCycle cbIntraday15 = new DataCycle("MINUTE15", 900);
	public static final DataCycle cbIntraday30 = new DataCycle("MINUTE30", 1800);
	public static final DataCycle cbIntraday60 = new DataCycle("HOUR1", 3600);
	public static final DataCycle cbIntraday120 = new DataCycle("HOUR2", 2*3600);
	public static final DataCycle cbIntraday180 = new DataCycle("HOUR3", 3*3600);
	public static final DataCycle cbIntraday240 = new DataCycle("HOUR4", 4*3600);
	public static final DataCycle cbIntraday300 = new DataCycle("HOUR5", 5*3600);
	public static final DataCycle cbIntraday360 = new DataCycle("HOUR6", 6*3600);
	public static final DataCycle cbIntraday420 = new DataCycle("HOUR7", 7*3600);
	public static final DataCycle cbIntraday480 = new DataCycle("HOUR8", 8*3600);

	public static final DataCycle cbIntraday = new DataCycle("Intraday", 0);
	public static final DataCycle cbIntraday1s = new DataCycle("Intraday1s", 1);
	public static final DataCycle cbIntraday2s = new DataCycle("Intraday2s", 2);
	public static final DataCycle cbIntraday3s = new DataCycle("Intraday3s", 3);
	public static final DataCycle cbIntraday4s = new DataCycle("Intraday4s", 4);
	public static final DataCycle cbIntraday5s = new DataCycle("Intraday5s", 5);
	public static final DataCycle cbIntraday10s = new DataCycle("Intraday10s", 10);
	public static final DataCycle cbIntraday15s = new DataCycle("Intraday15s", 15);
	public static final DataCycle cbIntraday20s = new DataCycle("Intraday20s", 20);
	public static final DataCycle cbIntraday30s = new DataCycle("Intraday30s", 30);

	public static final DataCycle cbIntraday2 = new DataCycle("Intraday2", 120);
	public static final DataCycle cbIntraday3 = new DataCycle("Intraday3", 180);
	public static final DataCycle cbIntraday4 = new DataCycle("Intraday4", 240);
	public static final DataCycle cbIntraday10 = new DataCycle("Intraday10", 600);
	public static final DataCycle cbIntraday20 = new DataCycle("Intraday20", 1200);

	private DataCycle(String name, int value)
	{
		super(name, value);
	}

	public static DataCycle getDataCycle(String dataPeriod)
	{
		if (dataPeriod.equals("Daily"))
		{
			return DataCycle.cbDaily;
		}
		else if (dataPeriod.equals("Weekly"))
		{
			return DataCycle.cbWeekly;
		}
		else if (dataPeriod.equals("Monthly"))
		{
			return DataCycle.cbMonthly;
		}
		else if (dataPeriod.equals("1 sec"))
		{
			return DataCycle.cbIntraday1s;
		}
		else if (dataPeriod.equals("1 min"))
		{
			return DataCycle.cbIntraday1;
		}
		else if (dataPeriod.equals("5 min"))
		{
			return DataCycle.cbIntraday5;
		}
		else if (dataPeriod.equals("15 min"))
		{
			return DataCycle.cbIntraday15;
		}
		else if (dataPeriod.equals("30 min"))
		{
			return DataCycle.cbIntraday30;
		}
		else if (dataPeriod.equals("60 min"))
		{
			return DataCycle.cbIntraday60;
		}
		else if (dataPeriod.equals("2 hour"))
		{
			return DataCycle.cbIntraday120;
		}
		else if (dataPeriod.equals("3 hour"))
		{
			return DataCycle.cbIntraday180;
		}
		else if (dataPeriod.equals("4 hour"))
		{
			return DataCycle.cbIntraday240;
		}
		else if (dataPeriod.equals("5 hour"))
		{
			return DataCycle.cbIntraday300;
		}
		else if (dataPeriod.equals("6 hour"))
		{
			return DataCycle.cbIntraday360;
		}
		else if (dataPeriod.equals("7 hour"))
		{
			return DataCycle.cbIntraday420;
		}
		else if (dataPeriod.equals("8 hour"))
		{
			return DataCycle.cbIntraday480;
		}

		return null;
	}

	public String toDataPeriod()
	{
		if (this.equals(DataCycle.cbDaily))
		{
			return "Daily";
		}
		else if (this.equals(DataCycle.cbWeekly))
		{
			return "Weekly";
		}
		else if (this.equals(DataCycle.cbMonthly))
		{
			return "Monthly";
		}
		else if (this.equals(DataCycle.cbIntraday1s))
		{
			return "1 sec";
		}
		else if (this.equals(DataCycle.cbIntraday1))
		{
			return "1 min";
		}
		else if (this.equals(DataCycle.cbIntraday5))
		{
			return "5 min";
		}
		else if (this.equals(DataCycle.cbIntraday15))
		{
			return "15 min";
		}
		else if (this.equals(DataCycle.cbIntraday30))
		{
			return "30 min";
		}
		else if (this.equals(DataCycle.cbIntraday60))
		{
			return "60 min";
		}
		else if (this.equals(DataCycle.cbIntraday120))
		{
			return "2 hours";
		}
		else if (this.equals(DataCycle.cbIntraday180))
		{
			return "3 hours";
		}
		else if (this.equals(DataCycle.cbIntraday240))
		{
			return "4 hours";
		}
		else if (this.equals(DataCycle.cbIntraday300))
		{
			return "5 hours";
		}
		else if (this.equals(DataCycle.cbIntraday360))
		{
			return "6 hours";
		}
		else if (this.equals(DataCycle.cbIntraday420))
		{
			return "7 hours";
		}
		else if (this.equals(DataCycle.cbIntraday480))
		{
			return "8 hours";
		}

		return "";
	}

	public boolean sameType(DataCycle dataCycle)
	{
		boolean sameType = true;
		if (this.equals(DataCycle.cbDaily)
			|| this.equals(DataCycle.cbMonthly)
			|| this.equals(DataCycle.cbWeekly))
		{
			if (! (dataCycle.equals(DataCycle.cbDaily)
				   || dataCycle.equals(DataCycle.cbMonthly)
				   || dataCycle.equals(DataCycle.cbWeekly)))
			{
				sameType = false;
			}
		}
		else
		{
			if (dataCycle.equals(DataCycle.cbDaily)
				|| dataCycle.equals(DataCycle.cbMonthly)
				|| dataCycle.equals(DataCycle.cbWeekly))
			{
				sameType = false;
			}

		}
		return sameType;
	}

	public static DataCycle fromFrequency(Frequency frequency)
	{
		if(frequency.equals(Frequency.DAILY))
		{
			return DataCycle.cbDaily;
		}
		else if(frequency.equals(Frequency.FIFTEEN_MINS))
		{
			return DataCycle.cbIntraday15;
		}
		else if(frequency.equals(Frequency.FIFTEEN_SECS))
		{
			return DataCycle.cbIntraday15s;
		}
		else if(frequency.equals(Frequency.FIVE_MINS))
		{
			return DataCycle.cbIntraday5;
		}
		else if(frequency.equals(Frequency.FIVE_SECS))
		{
			return DataCycle.cbIntraday5s;
		}
		else if(frequency.equals(Frequency.MONTHLY))
		{
			return DataCycle.cbMonthly;
		}
		else if(frequency.equals(Frequency.ONE_HOUR))
		{
			return DataCycle.cbIntraday60;
		}
		else if(frequency.getUnit() == Unit.Hour && frequency.getNUnits() == 2)
		{
			return DataCycle.cbIntraday120;
		}
		else if(frequency.getUnit() == Unit.Hour && frequency.getNUnits() == 3)
		{
			return DataCycle.cbIntraday180;
		}
		else if(frequency.getUnit() == Unit.Hour && frequency.getNUnits() == 4)
		{
			return DataCycle.cbIntraday240;
		}
		else if(frequency.getUnit() == Unit.Hour && frequency.getNUnits() == 5)
		{
			return DataCycle.cbIntraday300;
		}
		else if(frequency.getUnit() == Unit.Hour && frequency.getNUnits() == 6)
		{
			return DataCycle.cbIntraday360;
		}
		else if(frequency.getUnit() == Unit.Hour && frequency.getNUnits() == 7)
		{
			return DataCycle.cbIntraday420;
		}
		else if(frequency.getUnit() == Unit.Hour && frequency.getNUnits() == 8)
		{
			return DataCycle.cbIntraday480;
		}
		else if(frequency.equals(Frequency.ONE_MIN))
		{
			return DataCycle.cbIntraday1;
		}
		else if(frequency.equals(Frequency.ONE_SEC))
		{
			return DataCycle.cbIntraday1s;
		}
		else if(frequency.equals(Frequency.TWO_MINS))
		{
			return DataCycle.cbIntraday2;
		}
		else if(frequency.equals(Frequency.TWO_SECS))
		{
			return DataCycle.cbIntraday2s;
		}
		else if(frequency.equals(Frequency.THREE_MINS))
		{
			return DataCycle.cbIntraday3;
		}
		else if(frequency.equals(Frequency.THREE_SECS))
		{
			return DataCycle.cbIntraday3s;
		}
		else if(frequency.equals(Frequency.THREE_MINS))
		{
			return DataCycle.cbIntraday3;
		}
		else if(frequency.equals(Frequency.THREE_SECS))
		{
			return DataCycle.cbIntraday3s;
		}
		else if(frequency.equals(Frequency.FOUR_MINS))
		{
			return DataCycle.cbIntraday4;
		}
		else if(frequency.equals(Frequency.FOUR_SECS))
		{
			return DataCycle.cbIntraday4s;
		}
		else if(frequency.equals(Frequency.THIRTY_MINS))
		{
			return DataCycle.cbIntraday30;
		}
		else if(frequency.equals(Frequency.THIRTY_SECS))
		{
			return DataCycle.cbIntraday30s;
		}
		else if(frequency.equals(Frequency.WEEKLY))
		{
			return DataCycle.cbWeekly;
		}
		else if(frequency.getUnit() == Unit.Second && frequency.getNUnits() == 10)
		{
			return DataCycle.cbIntraday10s;
		}
		else if(frequency.getUnit() == Unit.Minute && frequency.getNUnits() == 10)
		{
			return DataCycle.cbIntraday10;
		}
		else if(frequency.getUnit() == Unit.Second && frequency.getNUnits() == 20)
		{
			return DataCycle.cbIntraday20s;
		}
		else if(frequency.getUnit() == Unit.Minute && frequency.getNUnits() == 20)
		{
			return DataCycle.cbIntraday20;
		}
		throw new IllegalArgumentException(frequency.getName());
	}

	public Frequency toFrequency()
	{
		if(this.equals(DataCycle.cbDaily))
		{
			return Frequency.DAILY;
		}
		else if(this.equals(DataCycle.cbIntraday1))
		{
			return Frequency.ONE_MIN;
		}
		else if(this.equals(DataCycle.cbIntraday10))
		{
			return new Frequency(Unit.Minute, 10);
		}
		else if(this.equals(DataCycle.cbIntraday10s))
		{
			return new Frequency(Unit.Second, 10);
		}
		else if(this.equals(DataCycle.cbIntraday15))
		{
			return Frequency.FIFTEEN_MINS;
		}
		else if(this.equals(DataCycle.cbIntraday15s))
		{
			return Frequency.FIFTEEN_SECS;
		}
		else if(this.equals(DataCycle.cbIntraday1))
		{
			return Frequency.ONE_MIN;
		}
		else if(this.equals(DataCycle.cbIntraday1s))
		{
			return Frequency.ONE_SEC;
		}
		else if(this.equals(DataCycle.cbIntraday2s))
		{
			return Frequency.TWO_SECS;
		}
		else if(this.equals(DataCycle.cbIntraday2))
		{
			return Frequency.TWO_MINS;
		}
		else if(this.equals(DataCycle.cbIntraday20))
		{
			return new Frequency(Unit.Minute, 20);
		}
		else if(this.equals(DataCycle.cbIntraday20s))
		{
			return new Frequency(Unit.Second, 20);
		}
		else if(this.equals(DataCycle.cbIntraday3))
		{
			return Frequency.THREE_MINS;
		}
		else if(this.equals(DataCycle.cbIntraday3s))
		{
			return Frequency.THREE_SECS;
		}
		else if(this.equals(DataCycle.cbIntraday30))
		{
			return Frequency.THIRTY_MINS;
		}
		else if(this.equals(DataCycle.cbIntraday30s))
		{
			return Frequency.THIRTY_SECS;
		}
		else if(this.equals(DataCycle.cbIntraday4))
		{
			return Frequency.FOUR_MINS;
		}
		else if(this.equals(DataCycle.cbIntraday4s))
		{
			return Frequency.FOUR_SECS;
		}
		else if(this.equals(DataCycle.cbIntraday5))
		{
			return Frequency.FIVE_MINS;
		}
		else if(this.equals(DataCycle.cbIntraday5s))
		{
			return Frequency.FIVE_SECS;
		}
		else if(this.equals(DataCycle.cbIntraday60))
		{
			return Frequency.ONE_HOUR;
		}
		else if(this.equals(DataCycle.cbWeekly))
		{
			return Frequency.WEEKLY;
		}
		else if(this.equals(DataCycle.cbMonthly))
		{
			return Frequency.MONTHLY;
		}
		throw new IllegalStateException("Can't convert " + this.name() + " to Frequency");
	}
}
