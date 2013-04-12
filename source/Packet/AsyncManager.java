package Packet;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import Util.StringHelper;
import tradingConsole.SlidingWindow;
import Util.XmlElementHelper;
import framework.xml.XmlNode;
import Util.BusinessCommandHelper;
import tradingConsole.SlidingWindow.SequenceCommand;
import framework.xml.XmlDocument;
import framework.xml.XmlElement;
import framework.DateTime;
import framework.Guid;

public class AsyncManager implements Runnable
{
	private Logger logger = Logger.getLogger(AsyncManager.class);
	private SlidingWindow slidingWindow;
	private volatile boolean isStoped=false;
	private volatile boolean isStarted=false;
	private final DateTime base = new DateTime(2011, 4, 1, 0, 0, 0);
	private ArrayList<byte[]> resultList = new ArrayList<byte[]> ();
	public AsyncManager()
	{
	}

	public void setSlidingWindow(SlidingWindow slidingWindow)
	{
		this.slidingWindow = slidingWindow;
	}

	public synchronized void add(byte[] result)
	{
		this.resultList.add(result);
		this.notify();
	}

	public void start()
	{
		if(!this.isStarted){
			Thread thread = new Thread(this);
			thread.start();
			this.isStarted=true;
		}
	}

	public void stop()
	{
		this.isStoped=true;
	}

	public synchronized byte[] dequeue()
	{
		return dequeueHelper();
	}

	private byte[] dequeueHelper()
	{
		int size = this.resultList.size();
		byte[] result = this.resultList.get(size - 1);
		this.resultList.remove(size - 1);
		return result;
	}

	public void run()
	{
		try
		{
			while (!Thread.interrupted())
			{
				if(this.isStoped)
				{
					break;
				}
				while (true)
				{
					if(this.isStoped)
					{
						break;
					}
					byte[] result = null;
					synchronized (this)
					{
						while(this.resultList.size() == 0)
						{
							this.wait();
						}
						result = dequeueHelper();
					}
					if (result == null)
					{
						break;
					}
					this.doWork(result);
				}
			}
		}
		catch (Exception e)
		{
			this.logger.error(e);
		}

	}

	private void doWork(byte[] result)
	{
		try
		{
			ComunicationObject target = PacketParser.parse(result);
			String invokeId = target.getInvokeID();
			if (StringHelper.IsNullOrEmpty(invokeId))
			{
				this.proccessCommand(target);
			}
			else
			{
				this.proccessMethodInvoke(target, invokeId);
			}
		}
		catch (Exception ex)
		{
			this.logger.error(ex.getStackTrace());
		}
	}


	private void proccessCommand(ComunicationObject target)
	{
		try
		{
			XmlNode command =null;
			if(target.getIsPrice()){
				String content = Quotation4BitEncoder.decode(target.getPrice());
				command = this.buildQuotationCommand(content);
			}
			else{
				command = XmlElementHelper.ConvertToXmlNode(target.getRawContent());
			}
			if (command == null)
			{
				return;
			}
			SequenceCommand sequenceCommand = BusinessCommandHelper.convertRawCommandToSequenceCommand(command, false);
			this.slidingWindow.addCommand(sequenceCommand, false);
		}
		catch (Exception ex)
		{
			this.logger.error(ex.getStackTrace());
		}
	}

	private XmlNode buildQuotationCommand(String content){
		String  startAndEndSeparator = "/";
		String quotationSeparator = ";";
		String fieldSeparator = ":";
		char rowSeparator ='\n';
		char colSeparator = '\t';
		String rowSeparatorString = new String(new char[]{rowSeparator});
		String colSeparatorString = new String(new char[]{colSeparator});
		int startIndex = content.indexOf(startAndEndSeparator);
		int endIndex = content.lastIndexOf(startAndEndSeparator);
		String commandSequence=content.substring(0, startIndex);
		String quotationContent = content.substring(startIndex + 1 , endIndex);
		String[] quotations =quotationContent.split(quotationSeparator);
		XmlDocument xmlDocument = new XmlDocument();
		XmlElement commandsElement = xmlDocument.createElement("Commands");
		StringBuilder sBuilder = new StringBuilder();
		for (String quotation : quotations) {
			if(StringHelper.IsNullOrEmpty(quotation)){
				continue;
			}
			String[] quotationCols = quotation.split(fieldSeparator);
			String volumn ="";
			if(quotationCols.length==9){
				volumn= StringHelper.IsNullOrEmpty(quotationCols[7])?"":colSeparator+quotationCols[7]+colSeparator+quotationCols[8];
			}
			DateTime currentDateTime=base.addSeconds(Double.parseDouble(quotationCols[6]));
			Integer instrumentMapId= Integer.parseInt(quotationCols[0]);
			Guid instrumentMapGuid = GuidMapping.Default.get(instrumentMapId);
			String quotationString =instrumentMapGuid.toString() + colSeparator+
					currentDateTime.toString("yyyy-MM-dd HH:mm:ss")+colSeparator+quotationCols[1]+colSeparator+quotationCols[2]+colSeparator+quotationCols[3]+colSeparator+quotationCols[4]+volumn;
			sBuilder.append(quotationString+rowSeparatorString);
		}
		if(sBuilder.length()>0){
			sBuilder.setLength(sBuilder.length() - rowSeparatorString.length());
		}
		XmlElement quotationElement2 = new XmlDocument().createElement("Quotation");
		quotationElement2.setAttribute("Overrided", sBuilder.toString());
		commandsElement.appendChild(quotationElement2);
		commandsElement.setAttribute("FirstSequence", commandSequence);
		commandsElement.setAttribute("LastSequence", commandSequence);
		return commandsElement;
	}



	private void proccessMethodInvoke(ComunicationObject target, String invokeId)
	{
		SignalObject signal = SignalContainer.Default.get(invokeId);
		if (signal == null)
		{
			return;
		}
		this.logger.debug("is a method call");
		signal.setResult(target.getContent());
		synchronized (signal)
		{
			signal.notify();
		}
		SignalContainer.Default.remove(invokeId);
	}
}
