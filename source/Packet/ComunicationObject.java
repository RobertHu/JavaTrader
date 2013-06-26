package Packet;

import nu.xom.Element;

public class ComunicationObject
{
	private boolean isPrice;
	private String session;
	private String invokeID;
	private Element content;
	private byte[] price;
	private String rawContent;
	private boolean isKeepAlive;
	private boolean isKeepAliveSuccess;
	private boolean isInitData;

	private ComunicationObject(){}

	public ComunicationObject(String session, byte[] price)
	{
		this(true, session, "", null, price);
	}

	public ComunicationObject(String session, Element content)
	{
		this(false, session, "", content, null);
	}

	public ComunicationObject(String invokeID,boolean isKeepAliveSuccess){
		this.invokeID = invokeID;
		this.isKeepAlive= true;
		this.isKeepAliveSuccess = isKeepAliveSuccess;
	}

	public ComunicationObject(String session, String invokeID, Element content)
	{
		this(false, session, invokeID, content, null);
	}

	public ComunicationObject(boolean isPrice, String session, String invokeID,
							  Element content, byte[] price)
	{
		this.isPrice = isPrice;
		this.session = session;
		this.invokeID = invokeID;
		this.content = content;
		this.price = price;
	}

	public static ComunicationObject CreateForInitData(String invokeId, String content)
	{
		ComunicationObject target = new ComunicationObject();
		target.rawContent=content;
		target.invokeID=invokeId;
		target.isInitData=true;
		return target;
	}

	public boolean getIsKeepAlive(){
		return this.isKeepAlive;
	}

	public void setIsKeepAlive(boolean value){
		this.isKeepAlive=value;
	}

	public boolean getIsKeepAliveSuccess(){
		return this.isKeepAliveSuccess;
	}

	public void setIsKeepAliveSuccess(boolean value){
		this.isKeepAliveSuccess = value;
	}

	public void setRawContent(String rawContent)
	{
		this.rawContent=rawContent;
	}

	public String getRawContent()
	{
		return this.rawContent;
	}


	public boolean getIsPrice()
	{
		return this.isPrice;
	}

	public String getSession()
	{
		return this.session;
	}

	public String getInvokeID()
	{
		return this.invokeID;
	}

	public Element getContent()
	{
		return this.content;
	}

	public byte[] getPrice()
	{
		return this.price;
	}

	public boolean isInitData(){
		return this.isInitData;
	}

}
