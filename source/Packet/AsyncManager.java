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
import nu.xom.Element;
import nu.xom.Attribute;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

public class AsyncManager implements Runnable
{
	private Logger logger = Logger.getLogger(AsyncManager.class);
	private SlidingWindow slidingWindow;
	private volatile boolean isStoped=false;
	private volatile boolean isStarted=false;
	private final DateTime base = new DateTime(2011, 4, 1, 0, 0, 0);
	private final int CAPACITPY = 100;
	private BlockingQueue<byte[]> queue = new ArrayBlockingQueue<byte[]> (CAPACITPY);
	public AsyncManager()
	{
	}

	public void setSlidingWindow(SlidingWindow slidingWindow)
	{
		this.slidingWindow = slidingWindow;
	}

	public  void add(byte[] result)
	{
		if(result==null){
			return;
		}
		try
		{
			this.queue.put(result);
		}
		catch (InterruptedException ex)
		{
			this.logger.error("enqueue failed",ex);
		}
	}

	public void start()
	{
		if(!this.isStarted){
			Thread thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
			this.isStarted=true;
		}
	}

	public void stop()
	{
		this.isStoped=true;
	}

	public  byte[] dequeue()
	{
		try
		{
			return this.queue.take();
		}
		catch (InterruptedException ex)
		{
			this.logger.error("dequeue failed",ex);
			return null;
		}
	}


	public void run()
	{
		try
		{
			while (!Thread.interrupted())
			{
				if (this.isStoped)
				{
					break;
				}
				while (true)
				{
					if (this.isStoped)
					{
						break;
					}
					byte[] result = dequeue();
					if (result == null)
					{
						break;
					}
					this.doWork(result);
				}
			}
			this.logger.info("closed");
		}
		catch (Exception e)
		{
			this.logger.error("async manager main body get error",e);
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
			this.logger.error("the process logic error",ex);
		}
	}


	private void proccessCommand(ComunicationObject target)
	{
		try
		{
			XmlNode command =null;
			Element command2 = null;
			if(this.slidingWindow==null){
				return;
			}
			if(target.getIsPrice()){
				String content = Quotation4BitEncoder.decode(target.getPrice());
				command = this.buildQuotationCommand(content);
				if(command==null){
					return;
				}
			}
			else{
				if(target.getContent().getLocalName().equals("News")){
					Element content = target.getContent();
					Attribute sequenceA = content.getAttribute(PacketContants.COMMAND_SEQUENCE);
					content.removeAttribute(sequenceA);
					String sequence = sequenceA.getValue();
					String xml = String.format("<Commands FirstSequence=\"%s\" LastSequence=\"%s\">%s</Commands>", sequence, sequence,
											   content.toXML());
					command2 = XmlElementHelper.parse(xml);
				}
				else{
					XmlNode cmd = XmlElementHelper.ConvertToXmlNode(target.getRawContent());
					XmlElement commandElement = (XmlElement)cmd;
					String sequence = commandElement.getAttribute(PacketContants.COMMAND_SEQUENCE);
					commandElement.removeAttribute(PacketContants.COMMAND_SEQUENCE);
					String xml = String.format("<Commands FirstSequence=\"%s\" LastSequence=\"%s\">%s</Commands>", sequence, sequence,
											   commandElement.get_OuterXml());
					command = XmlElementHelper.ConvertToXmlNode(xml);
				}

			}
			if (command == null && command2==null)
			{
				return;
			}
			SequenceCommand sequenceCommand = BusinessCommandHelper.convertRawCommandToSequenceCommand(command, false,command2);
			this.slidingWindow.addCommand(sequenceCommand, false);
		}
		catch (Exception ex)
		{
			this.logger.error("process command error",ex);
		}
	}

	private XmlNode buildQuotationCommand(String content){
		try{
			String startAndEndSeparator = "/";
			String quotationSeparator = ";";
			String fieldSeparator = ":";
			char rowSeparator = '\n';
			char colSeparator = '\t';
			String commandSeqenceSeparater = "-";
			String rowSeparatorString = new String(new char[]
				{rowSeparator});
			String colSeparatorString = new String(new char[]
				{colSeparator});
			int startIndex = content.indexOf(startAndEndSeparator);
			int endIndex = content.lastIndexOf(startAndEndSeparator);
			String commandSequence = content.substring(0, startIndex);
			String[] commandSeqs =commandSequence.split(commandSeqenceSeparater);
			String quotationContent = content.substring(startIndex + 1, endIndex);
			String[] quotations = quotationContent.split(quotationSeparator);
			XmlDocument xmlDocument = new XmlDocument();
			XmlElement commandsElement = xmlDocument.createElement("Commands");
			StringBuilder sBuilder = new StringBuilder();
			for (String quotation : quotations)
			{
				if (StringHelper.IsNullOrEmpty(quotation))
				{
					continue;
				}
				String[] quotationCols = quotation.split(fieldSeparator);
				String volumn = "";
				if (quotationCols.length == 9)
				{
					volumn = StringHelper.IsNullOrEmpty(quotationCols[7]) ? "" : colSeparator + quotationCols[7] + colSeparator + quotationCols[8];
				}
				DateTime currentDateTime = base.addSeconds(Double.parseDouble(quotationCols[6]));
				Integer instrumentMapId = Integer.parseInt(quotationCols[0]);
				Guid instrumentMapGuid = GuidMapping.Default.get(instrumentMapId);
				if(instrumentMapGuid == null)
				{
					this.logger.error("get instrumentmapGuid error "+instrumentMapId.toString());
					return null;
				}
				String quotationString = instrumentMapGuid.toString() + colSeparator +
					currentDateTime.toString("yyyy-MM-dd HH:mm:ss") + colSeparator + quotationCols[1] + colSeparator + quotationCols[2] + colSeparator +
					quotationCols[3] + colSeparator + quotationCols[4] + volumn;
				sBuilder.append(quotationString + rowSeparatorString);
			}
			if (sBuilder.length() > 0)
			{
				sBuilder.setLength(sBuilder.length() - rowSeparatorString.length());
			}
			XmlElement quotationElement2 = new XmlDocument().createElement("Quotation");
			quotationElement2.setAttribute("Overrided", sBuilder.toString());
			commandsElement.appendChild(quotationElement2);
			commandsElement.setAttribute("FirstSequence", commandSeqs[0]);
			commandsElement.setAttribute("LastSequence", commandSeqs[1]);
			return commandsElement;
		}
		catch(Exception ex)
		{
			this.logger.error("process quotation error  "+content,ex);
			return null;
		}
	}



	private void proccessMethodInvoke(ComunicationObject target, String invokeId)
	{
		SignalObject signal = SignalContainer.Default.get(invokeId);
		if (signal == null)
		{
			this.logger.error("can't find the signal by the invoke id "+invokeId);
			return;
		}
		if(target.getIsKeepAlive()){
			signal.setKeepAliveSucess(target.getIsKeepAliveSuccess());
		}
		else if(target.isInitData()){
			signal.setRowContent(target.getRawContent());
		}
		else{
			signal.setResult(target.getContent());
		}
		synchronized (signal)
		{
			signal.notify();
		}
		SignalContainer.Default.remove(invokeId);
	}
}
