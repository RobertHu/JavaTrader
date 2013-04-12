package tradingConsole;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.net.*;
import javax.net.ssl.*;

public class SSLLog
{
	private static FileOutputStream _fos;
	private static SSLLog.MyPrintStream _ps;

	public static void begin(String logFileFullName) throws Exception
	{
		SSLLog._fos = new FileOutputStream(logFileFullName);
		SSLLog._ps = new SSLLog.MyPrintStream(SSLLog._fos, false);
		System.setOut(SSLLog._ps);

		System.setProperty("javax.net.debug", "ssl");
	}

	public static void end() throws Exception
	{
		System.setOut(System.out);
		SSLLog._ps.close();
		SSLLog._fos.close();
	}

	public static class MyPrintStream extends PrintStream
	{
		public MyPrintStream (OutputStream out, boolean b)
		{
			super(out, b);
		}

		public void write(byte[] buffer, int off, int len)
		{
			if(len > 2)
			{
				String now = "[" + new Date().toString() + "]\t";
				byte[] b = now.getBytes();
				super.write(b, 0, b.length);
			}
			super.write(buffer, off, len);
		}
	}
}
