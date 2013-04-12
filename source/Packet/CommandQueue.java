package Packet;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import nu.xom.Element;

public class CommandQueue implements Runnable
{
	private CommandQueue()
	{}
	public static final CommandQueue Default = new CommandQueue();
	private Logger logger = Logger.getLogger(CommandQueue.class);
	private final int capacity = 5000;
	private ArrayList<Element> queue = new ArrayList<Element> (capacity);
	private Object waitEvent = new Object();
	public void start()
	{
		try
		{
			Thread thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
		}
		catch (Exception e)
		{
			this.logger.error(e);
		}
	}

	public synchronized void add(Element command)
	{
		this.queue.add(command);
		this.waitEvent.notify();
	}

	private Element dequeue()
	{
		if (this.queue.size() == 0)
			return null;
		int lastIndex = this.queue.size() - 1;
		Element target = this.queue.get(lastIndex);
		this.queue.remove(lastIndex);
		return target;
	}

	public void run()
	{
		while (true)
		{
			try
			{
				this.waitEvent.wait();
				while (true)
				{
					synchronized (this)
					{
						if (this.queue.size() == 0)
							break;
						Element result = this.dequeue();
						logger.debug(result.toXML());
					}
				}
			}
			catch (Exception e)
			{
				logger.error(e);
			}

		}
	}

}
