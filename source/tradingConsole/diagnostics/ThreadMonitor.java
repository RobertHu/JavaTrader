package tradingConsole.diagnostics;

import java.io.*;
import java.util.*;
import tradingConsole.AppToolkit;
import framework.io.DirectoryHelper;
import tradingConsole.TradingConsole;
import framework.diagnostics.TraceType;
import framework.DateTime;

public class ThreadMonitor
{
	private static boolean _run = true;
	private static Thread _thread;
	private static Map<Thread, StackTraceElement[]> _lastThreadsSnapshot;
	private static PrintWriter _traceWriter;

	public static void start()
	{
		if(ThreadMonitor._traceWriter == null) ThreadMonitor.openTraceWriter();
		if(ThreadMonitor._thread != null) return;

		ThreadMonitor._thread = new Thread(new Runnable()
		{
			public void run()
			{
				while(_run)
				{
					Map<Thread, StackTraceElement[]> threadsSnapshot = Thread.getAllStackTraces();
					if(_lastThreadsSnapshot == null)
					{
						ThreadMonitor.traceLine("----------------------------------------------------------------------------");
						ThreadMonitor.traceLine(DateTime.get_Now().toString(DateTime.fullFormat));
						ThreadMonitor.traceLine("Totaol number of thread = " + threadsSnapshot.size());

						for(Thread thread : threadsSnapshot.keySet())
						{
							ThreadMonitor.traceLine();
							trace(thread, threadsSnapshot.get(thread));
						}

						ThreadMonitor.traceLine("----------------------------------------------------------------------------");
						ThreadMonitor._traceWriter.flush();
					}
					else
					{
						boolean hasNewThread = false;
						for(Thread thread : threadsSnapshot.keySet())
						{
							if(!_lastThreadsSnapshot.containsKey(thread))
							{
								hasNewThread = true;
								break;
							}
						}

						if(hasNewThread)
						{
							ThreadMonitor.traceLine();
							ThreadMonitor.traceLine("----------------------------------------------------------------------------");
							ThreadMonitor.traceLine(DateTime.get_Now().toString(DateTime.fullFormat));
							ThreadMonitor.traceLine("Totaol number of thread = " + threadsSnapshot.size());

							for (Thread thread : threadsSnapshot.keySet())
							{
								if (!_lastThreadsSnapshot.containsKey(thread))
								{
									ThreadMonitor.traceLine();
									trace(thread, threadsSnapshot.get(thread));
								}
							}

							ThreadMonitor.traceLine("----------------------------------------------------------------------------");
							ThreadMonitor._traceWriter.flush();
						}
					}
					_lastThreadsSnapshot = threadsSnapshot;

					try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException ex)
					{
					}
				}
			}
		});
		ThreadMonitor._thread.setPriority(Thread.MIN_PRIORITY);
		ThreadMonitor._thread.setDaemon(true);
		ThreadMonitor._thread.start();
	}

	private static void openTraceWriter()
	{
		String fullFilePath = DirectoryHelper.combine(AppToolkit.get_LogDirectory(), "threads.txt");
		File file = new File(fullFilePath);
		if (!file.exists())
		{
			try
			{
				String path = file.getParent();
				AppToolkit.createDirectory(path);
				file.createNewFile();
			}
			catch (IOException ex1)
			{
				TradingConsole.traceSource.trace(TraceType.Error, ex1);
			}
		}
		try
		{
			ThreadMonitor._traceWriter = new PrintWriter(fullFilePath);
		}
		catch (FileNotFoundException ex)
		{
			TradingConsole.traceSource.trace(TraceType.Error, ex);
		}
	}

	public static void stop()
	{
		ThreadMonitor._run = false;

		if(ThreadMonitor._thread != null)
		{
			try
			{
				ThreadMonitor._thread.join();
				ThreadMonitor._thread = null;
			}
			catch (InterruptedException ex)
			{
				TradingConsole.traceSource.trace(TraceType.Error, ex);
			}
		}

		if(ThreadMonitor._traceWriter != null)
		{
			ThreadMonitor._traceWriter.close();
		}
	}

	private static void trace(Thread thread, StackTraceElement[] stackTraceElements)
	{
		ThreadMonitor.traceLine(thread.toString());
		for(StackTraceElement element : stackTraceElements)
		{
			ThreadMonitor.traceLine(element.toString());
		}
	}

	private static void traceLine(String value)
	{
		ThreadMonitor._traceWriter.write(value);
		ThreadMonitor._traceWriter.write(TradingConsole.enterLine);
	}

	private static void traceLine()
	{
		ThreadMonitor._traceWriter.write(TradingConsole.enterLine);
	}
}
