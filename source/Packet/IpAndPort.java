package Packet;

public class IpAndPort
{
	private String ip;
	private int port;
	public IpAndPort(String ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}

	public String getIP()
	{
		return this.ip;
	}

	public int getPort()
	{
		return this.port;
	}
}
