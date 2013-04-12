package tradingConsole.ui.chart;

import java.util.*;

import org.aiotrade.core.common.*;
import org.aiotrade.core.common.Quotation;
import org.aiotrade.math.timeseries.*;
import framework.*;
import framework.DateTime;
import framework.data.*;
import framework.diagnostics.*;
import framework.lang.Enum;
import framework.threading.*;
import tradingConsole.*;
import tradingConsole.enumDefine.*;

public class QuotationProvider implements IQuotationProvider
{
	private TradingConsole _tradingConsole;

	//for aysnc call/////////////////////////////////////////////////
	private CharDataStatus _charDataStatus = CharDataStatus.Pending;
	private WaitHandle _charDataWaitHandle = new WaitHandle();
	private Guid _asyncResultId;
	private Guid _readyAsyncResultId;
	private String _methodName;
	private boolean _failed;
	private String _errorMessage;
	private Object _lock = new Object();
	////////////////////////////////////////////////////////////////

	public QuotationProvider(TradingConsole tradingConsole)
	{
		this._tradingConsole = tradingConsole;
		IAsyncCommandListener asyncCommandListener = new IAsyncCommandListener()
		{
			public void asyncCommandCompleted(Guid asyncResultId, String methodName, boolean failed, String errorMessage)
			{
				_readyAsyncResultId = asyncResultId;
				_methodName = methodName;
				_failed = failed;
				_errorMessage = errorMessage;
				TradingConsole.traceSource.trace(TraceType.Information,
					"[QuotationProvider.asyncCommandCompleted] to pulse with asyncResultId = " + asyncResultId.toString());
				_charDataStatus = CharDataStatus.Ready;
				_charDataWaitHandle.pulse();
				TradingConsole.traceSource.trace(TraceType.Information, "[QuotationProvider.asyncCommandCompleted] pulsed");
			}

			public Guid getAsyncResultId()
			{
				return _asyncResultId;
			}
		};
		this._tradingConsole.addAsyncCommandListener(asyncCommandListener);
	}

	public QuotationList getQuotations(String instrumentCode, Frequency frequency, Date from, Date to, boolean fixLastPeriod)
	{
		//if(from.after(to)) throw new IllegalArgumentException(StringHelper.format("from {0} is after to {1}", new Object[]{from, to}));

		TradingConsole.traceSource.trace(TraceType.Information,
										 StringHelper.format("[QuotationProvider.getQuotations] For: instrument = {0}; frequency = {1}; from = {2}; to={3}",
			new Object[]
			{instrumentCode, frequency.getName(), from, to}));
		Guid instrumentId = new Guid(instrumentCode);
		String dataPeriod = DataCycle.fromFrequency(frequency).toDataPeriod() + (fixLastPeriod ? "[FixLastPeriod]" : "");
		this._asyncResultId = this._tradingConsole.get_TradingConsoleServer().asyncGetChartData2(instrumentId,
			dataPeriod, DateTime.fromDate(from), DateTime.fromDate(to));

		if (this._asyncResultId.compareTo(Guid.empty) == 0)
		{
			TradingConsole.traceSource.trace(TraceType.Error,
											 StringHelper.format(
				"[QuotationProvider.getQuotations] Failed to get char data for: instrument = {0}; frequency = {1}; from = {2}; to={3}",
				new Object[]
				{instrumentCode, frequency.getName(), from, to}));
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Information,
											 "[QuotationProvider.getQuotations] Wait for reply from server. asyncResultId = " + this._asyncResultId.toString());
			this.waitWithTimeout(90);
		}

		DataSet dataSet = null;
		if (this._charDataStatus.value() == CharDataStatus.Ready.value() && !this._failed)
		{
			TradingConsole.traceSource.trace(TraceType.Information, "[QuotationProvider.getQuotations] To get data from server: " + this._readyAsyncResultId.toString());
			dataSet = this._tradingConsole.get_TradingConsoleServer().getChartData(this._readyAsyncResultId);
		}
		return this.getQuotationList(dataSet, false);
	}

	private void waitWithTimeout(int timeoutInSeconds)
	{
		this._charDataStatus = CharDataStatus.Pending;
		this._charDataWaitHandle.waitOne(timeoutInSeconds * 1000, true);
		if(this._charDataStatus.value() != CharDataStatus.Ready.value())
		{
			TradingConsole.traceSource.trace(TraceType.Warning,
											 "[QuotationProvider.waitWithTimeout] Get chart data with async id = " + this._asyncResultId + " is timeout");
		}
		/*int count = 0;
		while (this._charDataStatus.value() != CharDataStatus.Ready.value())
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException ex1)
			{
			}

			if (count++ > timeoutInSeconds)
			{
				TradingConsole.traceSource.trace(TraceType.Warning,
												 "[QuotationProvider.waitWithTimeout] Get chart data with async id = " + this._asyncResultId + " is timeout");
				this._charDataStatus = CharDataStatus.Timeout;
				this._asyncResultId = Guid.empty;
				break;
			}
		}*/
	}

	public QuotationList getTickByTickQuotations(String instrumentCode, Date from, Date to)
	{
		//if(from.after(to)) throw new IllegalArgumentException(StringHelper.format("from {0} is after to {1}", new Object[]{from, to}));

		TradingConsole.traceSource.trace(TraceType.Information,
										 StringHelper.format("[QuotationProvider.getTickByTickQuotations] For: instrument = {0}; from = {1}; to={2}",
			new Object[]
			{instrumentCode, from, to}));

		Guid instrumentId = new Guid(instrumentCode);
		/*this._asyncResultId = this._tradingConsole.get_TradingConsoleServer().asyncGetTickByTickHistoryDatas2(instrumentId,
			DateTime.fromDate(from), DateTime.fromDate(to));

		if (this._asyncResultId.compareTo(Guid.empty) == 0)
		{
			TradingConsole.traceSource.trace(TraceType.Error,
											 StringHelper.format(
				"[QuotationProvider.getTickByTickQuotations] Failed to get TickByTick char data for: instrument = {0}; from = {1}; to={2}",
				new Object[]
				{instrumentCode, from, to}));
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Information, "[QuotationProvider.getTickByTickQuotations] Wait for reply from server");
			this.waitWithTimeout(90);
		}*/
		DataSet dataSet = this._tradingConsole.get_TradingConsoleServer().GetTickByTickHistoryData(instrumentId,DateTime.fromDate(from), DateTime.fromDate(to));
		/*if (this._charDataStatus.value() == CharDataStatus.Ready.value() && !this._failed)
		{
			TradingConsole.traceSource.trace(TraceType.Information, "[QuotationProvider.getTickByTickQuotations] To get data from server: " + this._readyAsyncResultId.toString());
			dataSet = this._tradingConsole.get_TradingConsoleServer().getChartData(this._readyAsyncResultId);
		}*/
		return this.getQuotationList(dataSet, true);
	}

	private QuotationList getQuotationList(DataSet dataSet, boolean isTickByTickQuotation)
	{
		ArrayList<Quotation> quotations = null;
		boolean isSuccessed = false;
		if (dataSet != null && dataSet.get_Tables().get_Count() > 0)
		{
			DataTable dataTable = dataSet.get_Tables().get_Item("ChartDataTable");

			isSuccessed = true;
			quotations = new ArrayList<Quotation> (dataTable.get_Rows().get_Count());
			for (int index = 0; index < dataTable.get_Rows().get_Count(); index++)
			{
				DataRow dataRow = dataTable.get_Rows().get_Item(index);
				DateTime date = (DateTime)dataRow.get_Item("Date");

				float open, close, high, low, volume;
				if (isTickByTickQuotation)
				{
					open = ( (java.math.BigDecimal)dataRow.get_Item("Open")).floatValue();
					close = high = low = open;
					volume = 0;
				}
				else
				{
					open = ( (java.math.BigDecimal)dataRow.get_Item("Open")).floatValue();
					high = ( (java.math.BigDecimal)dataRow.get_Item("High")).floatValue();
					low = ( (java.math.BigDecimal)dataRow.get_Item("Low")).floatValue();
					close = ( (java.math.BigDecimal)dataRow.get_Item("Close")).floatValue();
					volume = dataRow.get_Item("Volume") == DBNull.value ? 0 : ( (Double)dataRow.get_Item("Volume")).floatValue();
				}

				Quotation quotation = new Quotation();
				quotation.setTime(date.toDate().getTime());
				quotation.setOpen(open);
				quotation.setClose(close);
				quotation.setHigh(high);
				quotation.setLow(low);
				quotation.setVolume(volume);

				quotations.add(quotation);

//				TradingConsole.traceSource.trace(TraceType.Information, "[QuotationProvider.getQuotationList]: " + quotation.toString());
			}
		}
		TradingConsole.traceSource.trace(TraceType.Information, "[QuotationProvider.getQuotationList] isSuccessed = " + isSuccessed +
										 "; count of quotations = " + (quotations == null ? "0" : quotations.size()));
		return new QuotationList(quotations, isSuccessed);
	}

	public void reset()
	{
		this._charDataStatus = CharDataStatus.Pending;
		this._failed = true;
		this._asyncResultId = null;
		this._readyAsyncResultId = null;
		this._charDataWaitHandle.pulse();
	}
}

class CharDataStatus extends Enum<CharDataStatus>
{
	public static final CharDataStatus Pending = new CharDataStatus("Pending", 0);
	public static final CharDataStatus Ready = new CharDataStatus("Ready", 1);
	public static final CharDataStatus Timeout = new CharDataStatus("Timeout", 2);

	private CharDataStatus(String name, int value)
	{
		super(name, value);
	}
}
