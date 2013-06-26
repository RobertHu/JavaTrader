package Connection;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;

public class SSLConnection
{
	private SSLSocket sslSocket;
	private String _ip;
	private int _port;
	private Logger logger=Logger.getLogger(SSLConnection.class);

	public SSLConnection(String ip, int port) throws KeyManagementException, NoSuchAlgorithmException, IOException
	{
		this._ip = ip;
		this._port = port;
		init();
	}

	public SSLSocket getSocket()
	{
		return this.sslSocket;
	}

	private void init() throws IOException, NoSuchAlgorithmException, KeyManagementException
	{
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, new TrustManager[]{new X509TrustManager()
		{
			public X509Certificate[] getAcceptedIssuers()
			{
				return null;
			}
			public void checkClientTrusted(X509Certificate[] chain,String authType) throws CertificateException
			{
			}

			public void checkServerTrusted(X509Certificate[] chain,	String authType) throws CertificateException
			{
			}
		}
		}, null);
		System.setProperty("java.net.preferIPv4Stack", "true");
		try{
			sslSocket = (SSLSocket)sslContext.getSocketFactory().createSocket(this._ip,this._port);
		}
		catch(Exception ex){
			this.logger.error("create ssl socket failed",ex);
		}
	}

}
