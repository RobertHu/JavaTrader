package tradingConsole;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

import framework.*;
import framework.DateTime;
import framework.diagnostics.*;
import framework.threading.*;
import framework.threading.Scheduler.*;
import framework.util.*;
import framework.xml.*;
import org.apache.log4j.Logger;
public class SlidingWindow implements ISchedulerCallback, Runnable, WaitCallback
{
	private static final String _readCommandSchedulerQueueName = "SlidingWindow_ReadCommand_SchedulerQueue";
	private static final String _readLostCommandSchedulerQueueName = "SlidingWindow_ReadLostCommand_SchedulerQueue";
	private static final String _proxyReadCommandSchedulerQueueName = "SlidingWindow_ReadProxyCommand_SchedulerQueue";
	private static final String _proxyReadLostCommandSchedulerQueueName = "SlidingWindow_ReadProxyLostCommand_SchedulerQueue";
	private static final TimeSpan _readLostCommandInterval = TimeSpan.fromMilliseconds(500);
	private static final TimeSpan _proxyReadLostCommandInterval = TimeSpan.fromMilliseconds(200);
	private static final XmlNode _notNeedHandleCommand = new XmlAttribute();

	private TraceSource _traceSource;

	private ICommandStream _commandStream;
	private ICommandProcessor _commandProcessor;
	private Scheduler _scheduler;
	private TimeSpan _readInterval;
	private Thread _commandProcessThread;
	private WaitHandle _commandArrivedNotifier = new WaitHandle();

	private boolean _started = false;
	private int _nextSequence;
	private int _readPendingCount = 0;
	private int _readProxyPendingCount = 0;
	private SortedList<SequenceCommand> _sequenceObjects = new SortedList<SequenceCommand>(new SequenceObjectComparaotr());
	private ArrayList<SequenceCommand> _processedCommands = new ArrayList<SequenceCommand>(); //to optimize performance

	private ICommandStream _proxyCommandStream = null;
	private SchedulerEntry _proxyReadCommandEntry = null;
	private SequenceRange _lastSequenceRange = null;

	private Timer _healthMonitorTimer = null;

	private Logger logger = Logger.getLogger(SlidingWindow.class);

	//private int _continuousReadNullCommandCount = 0;

	public SlidingWindow(ICommandStream commandStream, ICommandProcessor commandProcessor, Scheduler scheduler, TimeSpan readInterval)
	{
		this(commandStream, commandProcessor, scheduler, readInterval, "TradingConsole.SlidingWindow");
	}

	public SlidingWindow(ICommandStream commandStream, ICommandProcessor commandProcessor, Scheduler scheduler, TimeSpan readInterval, String traceSourceName)
	{
		if(commandStream == null) throw new IllegalArgumentException("Argument commandStream can't be null");
		if(commandProcessor == null) throw new IllegalArgumentException("Argument commandProcessor can't be null");
		if(scheduler == null) throw new IllegalArgumentException("Argument scheduler can't be null");

		this._traceSource = new TraceSource(traceSourceName);
		this._commandStream = commandStream;
		this._commandProcessor = commandProcessor;
		this._scheduler = scheduler;
		this._readInterval = readInterval;
		this._healthMonitorTimer = new Timer(1000, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				healthMonitor();
			}
		});
		this._healthMonitorTimer.setRepeats(true);
	}

	public synchronized void addProxyStream(ICommandStream commandStream, TimeSpan readInterval) throws Exception
	{
		this._proxyCommandStream = commandStream;
		if(this._proxyReadCommandEntry != null)
		{
			this._scheduler.remove(this._proxyReadCommandEntry);
		}
		this._proxyReadCommandEntry = this._scheduler.add(SlidingWindow._proxyReadCommandSchedulerQueueName, this, null, DateTime.minValue,
			DateTime.maxValue, readInterval, true);
	}

	public synchronized boolean isStarted()
	{
		return this._started;
	}

	public synchronized void start(int nextSequence) throws Exception
	{
		if(this._started)
		{
			throw new IllegalStateException("SlidingWindow is already started, call stop first");
		}
		else
		{
			this._traceSource.trace(TraceType.Critical, "Started with nextSequence = " + nextSequence);
			this._started = true;
			this._healthMonitorTimer.start();
			this._nextSequence = Math.max(this._nextSequence, nextSequence);//maybe invoked by refresh
			if(this._commandProcessThread == null)
			{
				this._commandProcessThread = new Thread(this);
				this._commandProcessThread.setDaemon(true);
				this._commandProcessThread.start();
			}
		}
	}

	public synchronized void stop()
	{
		if(!this._started)
		{
			throw new IllegalStateException("SlidingWindow is not started, call start first");
		}
		else
		{
			this._traceSource.trace(TraceType.Critical, "Stopped");

			this._healthMonitorTimer.stop();

			this._started = false;
			this._commandArrivedNotifier.pulse();
			this._sequenceObjects.clear();
			if(this._proxyReadCommandEntry != null) this._scheduler.remove(this._proxyReadCommandEntry);
			this._proxyCommandStream = null;
			this._readPendingCount = 0;
			//this._continuousReadNullCommandCount = 0;
		}
	}

	public synchronized void reset(int nextSequence)
	{
		this._traceSource.trace(TraceType.Critical, "Reset with nextSequence = " + nextSequence);
		this._nextSequence = Math.max(this._nextSequence, nextSequence);
		this._sequenceObjects.clear();
		this._lastSequenceRange = null;
		this._processedCommands.clear();
	}


	public synchronized void action(SchedulerEntry schedulerEntry) //ISchedulerCallback.action
	{
		if(this._started)
		{
			boolean readingProxy = !(schedulerEntry.get_QueueCode().equalsIgnoreCase(this._readCommandSchedulerQueueName)
				|| schedulerEntry.get_QueueCode().equalsIgnoreCase(this._readLostCommandSchedulerQueueName));
			boolean readLost = schedulerEntry.get_ActionArgs() != null;
			boolean queueWorkItem = (readingProxy ? this._readProxyPendingCount < 2 : this._readPendingCount < 2) || readLost;

			if (queueWorkItem)
			{
				this.increaseReadPendingCount(readingProxy);
				ThreadPool.queueUserWorkItem(this, schedulerEntry, ThreadPriority.normal);
			}
			else
			{
				if (schedulerEntry.get_ActionArgs() != null && this._lastSequenceRange != null)
				{
					SequenceRange sequenceRange = (SequenceRange) (schedulerEntry.get_ActionArgs());
					if(this._lastSequenceRange.hasSameRange(sequenceRange))
					{
						this._lastSequenceRange = null;
					}
				}
				String msg =  "ReadCommands/ReadCommands2 is ignored for the reading count("
										+ (readingProxy ? this._readProxyPendingCount : this._readPendingCount) + ") is greate than or equal to 2";
				this._traceSource.trace(TraceType.Warning,msg);
				this.logger.debug(msg);
			}
		}
	}

	public void action(Object state) throws Throwable//WaitCallback.action
	{
		SchedulerEntry schedulerEntry = (SchedulerEntry)state;
		boolean readingProxy = !(schedulerEntry.get_QueueCode().equalsIgnoreCase(this._readCommandSchedulerQueueName)
				|| schedulerEntry.get_QueueCode().equalsIgnoreCase(this._readLostCommandSchedulerQueueName));

		XmlNode command = null;
		boolean readLostCommand = false;
		SequenceRange sequenceRange = null;
		try
		{
			if (!this._started)	return;

			DateTime appTime = this._scheduler.get_TimeSource().get_UtcNow();
			DateTime now = DateTime.get_Now();
			if (schedulerEntry.get_ActionArgs() == null)
			{
				lastReadCommandTime = now;
				lastReadCommandTime2 = appTime;
				command = this.readCommand(0, 0, false, readingProxy);
			}
			else
			{
				lastReadLostCommandTime = DateTime.get_Now();
				readLostCommand = true;
				sequenceRange = (SequenceRange) (schedulerEntry.get_ActionArgs());
				command = this.readCommand(sequenceRange.get_BeginSequence(), sequenceRange.get_EndSequence(), true, readingProxy);
			}
		}
		finally
		{
			if(readLostCommand && command != null) this._lastSequenceRange = null;
			this.decreaseReadPendingCount(readingProxy);
		}

		if(!this._started){
			return;
		}
		if(command != SlidingWindow._notNeedHandleCommand)
		{
			if (command == null)
			{
				this._traceSource.trace(TraceType.Warning, sequenceRange == null ? "Readcommand return null" : "Readcommand2 with "
									+ (readingProxy? "(from proxy)" : "") + sequenceRange.toString() + " return null");

				if(this._proxyCommandStream == null/* || this._continuousReadNullCommandCount > 3*/)
				{
					String info = "Fire communicationBroken event for null command read" + (readingProxy? "(from proxy)" : "");
					this._traceSource.trace(TraceType.Error, info);
					this.logger.debug(info);
					this.communicationBroken();
				}
				//this._continuousReadNullCommandCount++;
			}
			else
			{
				//this._continuousReadNullCommandCount = 0;
				XmlAttribute firstSequenceAttribute = command.get_Attributes().get_ItemOf("FirstSequence");
				XmlAttribute lastSequenceAttribute = command.get_Attributes().get_ItemOf("LastSequence");
				int beginSequence = XmlConvert.toInt32(firstSequenceAttribute.get_Value());
				int endSequence = XmlConvert.toInt32(lastSequenceAttribute.get_Value());
				SequenceCommand sequenceCommand = new SequenceCommand(beginSequence, endSequence, command, readLostCommand);
				this.addCommand(sequenceCommand, readingProxy);
			}
		}
	}


	private SequenceRange lastSequenceRange = null;
	private synchronized void healthMonitor()
	{
		if (!this._started)	return;
		DateTime now = DateTime.get_Now();

		if(this.lastReadCommandTime != null && now.substract(this.lastReadCommandTime).get_TotalMilliseconds() > 30000)
		{
			this.logger.debug("health monitor sliding window reset");
			//let try restart slidingwindow
			this._traceSource.trace(TraceType.Warning, "SlidingWindow.healthMonitor report communication broken for read command stopped for 10 seconds");
			this.stop();
			try
			{
				this.start(this._nextSequence);
			}
			catch (Exception ex)
			{
				this._traceSource.trace(TraceType.Error, ex);
				this.logger.debug(ex.getStackTrace());
				this.communicationBroken();
			}
			return;
		}

		for(int index = 0; index < this._sequenceObjects.size(); index++)
		{
			SequenceCommand sequenceCommand = this._sequenceObjects.get(index);
			int beginSequence = sequenceCommand.get_BeginSequence();
			int compareResult = SequenceHelper.compare(beginSequence, this._nextSequence);

			if(compareResult > 0)
			{
				int lostCommandEndSequence = SequenceHelper.decrease(beginSequence);
				SequenceRange sequenceRange = new SequenceRange(this._nextSequence, lostCommandEndSequence);
				if(this.lastSequenceRange == null)
				{
					this.lastSequenceRange = sequenceRange;
				}
				else if(this.lastSequenceRange.hasSameRange(sequenceRange) && this.lastSequenceRange.incrementAge() > 300)
				{
					String info="SlidingWindow.healthMonitor report communication broken for the same gap found in commands queue for more than 3 times";

					String detail = String.format("beginSequence=%d,endSequence=%d,info: %s",this.lastSequenceRange.get_BeginSequence(),this.lastSequenceRange.get_EndSequence(),info);
					this._traceSource.trace(TraceType.Warning, info);
					this.logger.debug(detail);
					this.communicationBroken();
					return;
				}
				break;
			}
		}
	}

	private DateTime lastReadCommandTime = null;
	private DateTime lastReadCommandTime2 = null;
	private DateTime lastReadLostCommandTime = null;

	private synchronized void increaseReadPendingCount(boolean readingProxy)
	{
		if(readingProxy)
		{
			this._readProxyPendingCount++;
		}
		else
		{
			this._readPendingCount++;
		}
	}

	private synchronized void decreaseReadPendingCount(boolean readingProxy)
	{
		if(readingProxy)
		{
			this._readProxyPendingCount--;
		}
		else
		{
			this._readPendingCount--;
		}
	}

	//the four methods below are synchronized for they will be called by readCommand in scheduler
	public synchronized void addCommand(SequenceCommand sequenceCommand, boolean readingProxy)
	{
		this._traceSource.trace(TraceType.Information, "[SlidingWindow.addCommand]" + (readingProxy? "(from proxy):" : ":") + sequenceCommand.toString());

		if(this._started && SequenceHelper.compare(sequenceCommand.get_BeginSequence(), this._nextSequence) >= 0)
		{
			int index = this._sequenceObjects.binarySearch(sequenceCommand);//maybe the command is in cache
			if(index >= 0)//if the same sequence command is in cache, just remove it
			{
				this._sequenceObjects.remove(index);
			}
			this._sequenceObjects.add(sequenceCommand);
			this._commandArrivedNotifier.pulse();
		}
		else
		{
			this._traceSource.trace(TraceType.Warning, "Abnormal command received, nextSequence = " + this._nextSequence
									+ Environment.newLine + sequenceCommand);
		}
	}

	public void run() //Runnable.run
	{
		while(true)
		{
			this._commandArrivedNotifier.waitOne();
			this.process();
		}
	}

	private synchronized void process()
	{
		if(!this._started) return;
		this._processedCommands.clear();
		//this.logger.debug("process command");
		for(int index = 0; index < this._sequenceObjects.size(); index++)
		{
			SequenceCommand sequenceCommand = this._sequenceObjects.get(index);
			int beginSequence = sequenceCommand.get_BeginSequence();
			int endSequence = sequenceCommand.get_EndSequence();
			int compareResult = SequenceHelper.compare(beginSequence, this._nextSequence);
			if(compareResult == 0)
			{
				if(SequenceHelper.compare(beginSequence, endSequence) > 0)
				{
					this._traceSource.trace(TraceType.Information, "[SlidingWindow.process] No content command, not need to process:"
											+ "[" + beginSequence + "---" + endSequence + "]");
				}
				else
				{
					this._nextSequence = SequenceHelper.increase(endSequence);
					this._processedCommands.add(sequenceCommand);

					AwtSaflyCommandProcessor awtSaflyCommandProcessor = new AwtSaflyCommandProcessor(this._commandProcessor, sequenceCommand);
					this._traceSource.trace(TraceType.Information, "[SlidingWindow.process] Begin process command :"
											+ "[" + beginSequence + "---" + endSequence + "]");
					SwingUtilities.invokeLater(awtSaflyCommandProcessor);
					this._traceSource.trace(TraceType.Information, "[SlidingWindow.process] End process command :"
											+ "[" + beginSequence + "---" + endSequence + "]");
				}
			}
			else if(compareResult > 0)
			{
				TimeSpan interval = sequenceCommand.get_IsLostCommand() ? TimeSpan.zero : this._readLostCommandInterval;
				TimeSpan proxyInterval = sequenceCommand.get_IsLostCommand() ? TimeSpan.zero : this._proxyReadLostCommandInterval;
				int lostCommandEndSequence = SequenceHelper.decrease(beginSequence);
				this.scheduleReadLostCommand(this._nextSequence, lostCommandEndSequence, interval, proxyInterval);
				String msg = StringHelper.format("Read lost command[{0}---{1}]", new Object[]{this._nextSequence, lostCommandEndSequence});
				this._traceSource.trace(TraceType.Critical,msg);
				this.logger.debug(msg);
				break;
			}
			else
			{
				this._processedCommands.add(sequenceCommand);
				continue;
			}
		}

		for(SequenceCommand sequenceCommand : this._processedCommands)
		{
			this._sequenceObjects.remove(sequenceCommand);
		}
	}



	private synchronized void process(Throwable throwable)
	{
		if(this._started)
		{
			this._traceSource.trace(TraceType.Error, "Process exceptoin catched while reading command: " + throwable);
			AwtSaflyThrowableProcessor awtSaflyThrowableProcessor = new AwtSaflyThrowableProcessor(this._commandProcessor, throwable);
			SwingUtilities.invokeLater(awtSaflyThrowableProcessor);
		}
	}

	private synchronized void communicationBroken()
	{
		if(this._started)
		{
			this.logger.debug("communicationBroken");
			this._commandProcessor.communicationBroken();
		}
	}

	private synchronized boolean isStillLost(int beginSequenceOfLostCommand)
	{
		return SequenceHelper.compare(this._nextSequence, beginSequenceOfLostCommand) == 0;
	}

	private XmlNode readCommand(int beginSequenceOfLostCommand, int endSequenceOfLostCommand, boolean readLostCommand, boolean readingProxy)
	{
		if(!this._started) return null;
		int rereadCount = 0;
		XmlNode command = null;
		while(true)
		{
			if(!this._started) break;
			try
			{
				String info = StringHelper.format("Read command: beginSequence = {0}; endSequence = {1}; readLostCommand = {2}; readingProxy = {3}",
					new Object[]{beginSequenceOfLostCommand, endSequenceOfLostCommand, readLostCommand, readingProxy});
				this._traceSource.trace(TraceType.Information, info);
				this.logger.debug(info);

				if(!readLostCommand)
				{
					command =  readingProxy ?  this._proxyCommandStream.read() : this._commandStream.read();
				}
				else if(this.isStillLost(beginSequenceOfLostCommand))
				{

					command =  readingProxy ?  this._proxyCommandStream.read(beginSequenceOfLostCommand, endSequenceOfLostCommand) : this._commandStream.read(beginSequenceOfLostCommand, endSequenceOfLostCommand);
				}
				else
				{
					command =  SlidingWindow._notNeedHandleCommand;
				}
				break;
			}
			catch (Throwable throwable)
			{
				this.process(throwable);

				try
				{
					for(int index = 0; index < rereadCount + 1; index++)
					{
						if(!this._started) break;
						Thread.sleep(500);
					}
				}
				catch(InterruptedException interruptedException)
				{
				}

				if(++rereadCount > 3) break;
			}
		}
		return command;
	}



	private void scheduleReadLostCommand(int beginSequence, int endSequence, TimeSpan interval, TimeSpan proxyInterval)
	{
		DateTime appTime = null;
		try
		{
			appTime = this._scheduler.get_TimeSource().get_UtcNow();
		}
		catch(framework.FrameworkException exception)
		{
			if(exception.get_Message().indexOf("AppTime is stopped") > 0)
			{
				return;
			}
			else
			{
				throw exception;
			}
		}

		DateTime callbackTime = appTime.add(interval);
		String info = StringHelper.format("[SlidingWindow.scheduleReadLostCommand] Add readcommand in scheduler(beginSequnece = {0}; endSequnece = {1}; AppTime ={2}; callbackTime = {3})",
										  new Object[]{beginSequence, endSequence, appTime, callbackTime});
		this._traceSource.trace(TraceType.Information, info);
		try
		{
			SequenceRange sequenceRange = new SequenceRange(beginSequence, endSequence);

			if (this._lastSequenceRange != null && this._lastSequenceRange.hasSameRange(sequenceRange))
			{
				if (this._lastSequenceRange.incrementAge() > 30)
				{
					String msg="Get commands in range " + this._lastSequenceRange.toString()+ " return null, fire communicationBroken event";
					this._traceSource.trace(TraceType.Error,msg) ;
					this.logger.debug(msg);
					this.communicationBroken();
				}

			}
			else
			{
				this._lastSequenceRange = sequenceRange;
			}

			this._scheduler.add(SlidingWindow._readLostCommandSchedulerQueueName, this,
								sequenceRange, callbackTime);

			if(this._proxyCommandStream != null)
			{
				callbackTime = appTime.add(proxyInterval);
				this.logger.debug(String.format("begin get lost command %d to %d",this._lastSequenceRange.get_BeginSequence(),this._lastSequenceRange.get_EndSequence()));
				this._scheduler.add(SlidingWindow._proxyReadLostCommandSchedulerQueueName, this,
									sequenceRange, callbackTime);
			}
		}
		catch (Exception exception)
		{
			String msg = "[SlidingWindow.scheduleReadLostCommand] Fire communicationBroken event for exception below while scheduleReadCommand"
											 + Environment.newLine + exception.toString();
			this._traceSource.trace(TraceType.Error, msg);
			this.logger.debug(msg);
			this.communicationBroken();
		}
	}

	public void getHealthMessage(StringBuilder debugInfo)
	{
		String commandQueueInfo = "is continuous";
		for(int index = 0; index < this._sequenceObjects.size(); index++)
		{
			SequenceCommand sequenceCommand = this._sequenceObjects.get(index);
			int beginSequence = sequenceCommand.get_BeginSequence();
			int compareResult = SequenceHelper.compare(beginSequence, this._nextSequence);

			if (compareResult > 0)
			{
				int lostCommandEndSequence = SequenceHelper.decrease(beginSequence);
				commandQueueInfo = StringHelper.format("has gap({0}-{1})", new Object[]{this._nextSequence, lostCommandEndSequence});
			}
		}
		DateTime appTime = this._scheduler.get_TimeSource().get_UtcNow();
		DateTime now = DateTime.get_Now();

		debugInfo.append("SlidingWindowStatus: ");
		debugInfo.append("SystemTime=");
		debugInfo.append(XmlConvert.toString(now));
		debugInfo.append(";Apptime=");
		debugInfo.append(XmlConvert.toString(appTime));
		debugInfo.append(";IsStarted=");
		debugInfo.append(XmlConvert.toString(this._started));
		debugInfo.append(";NextSequence=");
		debugInfo.append(XmlConvert.toString(this._nextSequence));
		debugInfo.append(";CommandQueue=");
		debugInfo.append(commandQueueInfo);
		debugInfo.append(";LastTimeOfGetCommands=");
		debugInfo.append(this.lastReadCommandTime2 == null ? "null" : XmlConvert.toString(this.lastReadCommandTime2));
		debugInfo.append(";LastTimeOfGetCommands2=");
		debugInfo.append(this.lastReadLostCommandTime == null ? "null" : XmlConvert.toString(this.lastReadLostCommandTime));
	}

	public XmlNode getHealthMessage(XmlDocument document)
	{
		String commandQueueInfo = "is continuous";
		for(int index = 0; index < this._sequenceObjects.size(); index++)
		{
			SequenceCommand sequenceCommand = this._sequenceObjects.get(index);
			int beginSequence = sequenceCommand.get_BeginSequence();
			int compareResult = SequenceHelper.compare(beginSequence, this._nextSequence);

			if (compareResult > 0)
			{
				int lostCommandEndSequence = SequenceHelper.decrease(beginSequence);
				commandQueueInfo = StringHelper.format("has gap({0}-{1})", new Object[]{this._nextSequence, lostCommandEndSequence});
			}
		}
		DateTime appTime = this._scheduler.get_TimeSource().get_UtcNow();
		DateTime now = DateTime.get_Now();
		XmlElement slidingWindowStatus = document.createElement("SlidingWindowStatus");
		slidingWindowStatus.setAttribute("SystemTime", XmlConvert.toString(now));
		slidingWindowStatus.setAttribute("Apptime", XmlConvert.toString(appTime));
		slidingWindowStatus.setAttribute("IsStarted", XmlConvert.toString(this._started));
		slidingWindowStatus.setAttribute("NextSequence", XmlConvert.toString(this._nextSequence));
		slidingWindowStatus.setAttribute("CommandQueue", commandQueueInfo);
		slidingWindowStatus.setAttribute("LastTimeOfGetCommands", this.lastReadCommandTime2 == null ? "null" : XmlConvert.toString(this.lastReadCommandTime2));
		slidingWindowStatus.setAttribute("LastTimeOfGetCommands2", this.lastReadLostCommandTime == null ? "null" : XmlConvert.toString(this.lastReadLostCommandTime));

		return slidingWindowStatus;
	}

	private static class AwtSaflyCommandProcessor implements Runnable
	{
		private ICommandProcessor _commandProcessor;
		private SequenceCommand _sequenceCommand;

		AwtSaflyCommandProcessor(ICommandProcessor commandProcessor, SequenceCommand sequenceCommand)
		{
			this._commandProcessor = commandProcessor;
			this._sequenceCommand = sequenceCommand;
		}

		public void run()
		{
			this._commandProcessor.process(this._sequenceCommand.get_Command());
		}
	}

	private static class AwtSaflyThrowableProcessor implements Runnable
	{
		private ICommandProcessor _commandProcessor;
		private Throwable _throwable;

		AwtSaflyThrowableProcessor(ICommandProcessor commandProcessor, Throwable throwable)
		{
			this._commandProcessor = commandProcessor;
			this._throwable = throwable;
		}

		public void run()
		{
			this._commandProcessor.process(this._throwable);
		}
	}

	public static interface ICommandStream
	{
		XmlNode read();
		XmlNode read(int begingSequence, int endSequence);
	}

	public static interface ICommandProcessor
	{
		void process(XmlNode commands);
		void process(Throwable throwable);
		void communicationBroken();
	}

	private static class SequenceHelper
	{
		private static final int _maxSequence = Integer.MAX_VALUE;
		private static final int _minSequence = Integer.MIN_VALUE;

		static int increase(int sequence)
		{
			if (sequence == SequenceHelper._maxSequence)
			{
				return SequenceHelper._minSequence;
			}
			else
			{
				return (sequence + 1);
			}
		}

		static int decrease(int sequence)
		{
			if (sequence == SequenceHelper._minSequence)
			{
				return SequenceHelper._maxSequence;
			}
			else
			{
				return (sequence - 1);
			}
		}

		static int compare(int sequence1, int sequence2)
		{
			if(sequence1 == sequence2)
			{
				return 0;
			}
			else if ((sequence1 - sequence2) > 0 && (sequence1 - sequence2) <= SequenceHelper._maxSequence)
			{
				return 1;
			}
			else
			{
				return -1;
			}
		}
	}

	private static class SequenceRange
	{
		private int _age = 0;
		private int _beginSequence;
		private int _endSequence;

		SequenceRange(int beginSequence, int endSequence)
		{
			this._beginSequence = beginSequence;
			this._endSequence = endSequence;
		}

		int get_BeginSequence()
		{
			return this._beginSequence;
		}

		int get_EndSequence()
		{
			return this._endSequence;
		}

		public String toString()
		{
			return "[" + this._beginSequence + "---" + this._endSequence + ", " + this._age + " old" + "] ";
		}

		private int incrementAge()
		{
			return this._age++;
		}

		private boolean hasSameRange(SequenceRange sequenceRange)
		{
			return this._beginSequence == sequenceRange._beginSequence && this._endSequence == sequenceRange._endSequence;
		}
	}

	public static class SequenceCommand
	{
		private int _beginSequence;
		private int _endSequence;
		private XmlNode _command;
		private boolean _isLostCommand;

		public SequenceCommand(int beginSequence, int endSequence, XmlNode object, boolean isLostCommand)
		{
			this._beginSequence = beginSequence;
			this._endSequence = endSequence;
			this._command = object;
			this._isLostCommand = isLostCommand;
		}

		int get_BeginSequence()
		{
			return this._beginSequence;
		}

		int get_EndSequence()
		{
			return this._endSequence;
		}

		XmlNode get_Command()
		{
			return this._command;
		}

		boolean get_IsLostCommand()
		{
			return this._isLostCommand;
		}

		public String toString()
		{
			return StringHelper.format("Begin sequence = {0}; End sequence = {1};{2}{3}",
								 new Object[]{this._beginSequence, this._endSequence, Environment.newLine,  this._command.get_OuterXml()});
		}
	}

	private static class SequenceObjectComparaotr implements Comparator<SequenceCommand>
	{
		public int compare(SequenceCommand o1, SequenceCommand o2)
		{
			return SequenceHelper.compare(o1.get_BeginSequence(), o2.get_BeginSequence());
		}

		public boolean equals(Object obj)
		{
			return false;
		}
	}
}
