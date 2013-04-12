package tradingConsole.framework;

import framework.sound.SoundPlayer;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import com.vladium.utils.ResourceLoader;

public class ResourceHelper
{
	public static boolean exists(String packageName, String name) throws IOException
	{
		return ResourceHelper.exists(ResourceHelper.combineName(packageName, name));
	}

	public static boolean exists(String fullName) throws IOException
	{
		return ResourceLoader.getResource(fullName) != null;
	}
/*
	public static boolean isPackage(String name) throws IOException
	{
		if(!name.endsWith("/"))
		{
			name = name + "/";
		}
		return ResourceHelper.exists(name);
	}
*/
	public static InputStream getAsStream(String packageName, String name)
	{
		return ResourceHelper.getAsStream(ResourceHelper.combineName(packageName, name));
	}

	public static InputStream getAsStream(String fullName)
	{
		/*ClassLoader classLoader = ResourceHelper.class.getClassLoader();*/
		InputStream inputStream = ResourceLoader.getResourceAsStream(fullName);
		return inputStream;
	}

	public static byte[] getAsByteArray(String packageName, String name) throws IOException
	{
		return ResourceHelper.getAsByteArray(ResourceHelper.combineName(packageName, name));
	}

	public static byte[] getAsByteArray(String fullName) throws IOException
	{
		InputStream inputStream = ResourceHelper.getAsStream(fullName);
		if(inputStream == null)
		{
			return null;
		}
		else
		{
			try
			{
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				byte[] buffer = new byte[2048];
				int readCount = 0;
				while ( (readCount = inputStream.read(buffer, 0, buffer.length)) != -1)
				{
					byteArrayOutputStream.write(buffer, 0, readCount);
				}
				byte[] byteArray = byteArrayOutputStream.toByteArray();
				byteArrayOutputStream.close();

				return byteArray;
			}
			catch(IOException exception)
			{
				throw exception;
			}
			finally
			{
				inputStream.close();
			}
		}
	}

	public static Image getAsImage(String packageName, String name) throws IOException
	{
		return ResourceHelper.getAsImage(ResourceHelper.combineName(packageName, name));
	}

	public static Image getAsImage(String fullName) throws IOException
	{
		byte[] imageData = ResourceHelper.getAsByteArray(fullName);
		if(imageData == null)
		{
			return null;
		}
		else
		{
			return Toolkit.getDefaultToolkit().createImage(imageData);
		}
	}

	public static Icon getAsIcon(String packageName, String name) throws IOException
	{
		return ResourceHelper.getAsIcon(ResourceHelper.combineName(packageName, name));
	}

	public static Icon getAsIcon(String fullName) throws IOException
	{
		//return new ImageIcon(fullName);
		byte[] imageData = ResourceHelper.getAsByteArray(fullName);
		if(imageData == null)
		{
			return null;
		}
		else
		{
			return new ImageIcon(imageData);
		}
	}

	public static boolean fetchToFile(String fullName, String destinationFilePath) throws IOException
	{
		return ResourceHelper.fetchToFile(fullName, destinationFilePath, true);
	}

	public static boolean fetchToFile(String packageName, String name, String destinationFilePath) throws IOException
	{
		return ResourceHelper.fetchToFile(ResourceHelper.combineName(packageName, name), destinationFilePath, true);
	}

	public static boolean fetchToFile(String packageName, String name, String destinationFilePath, boolean overwriteOldFile) throws IOException
	{
		return ResourceHelper.fetchToFile(ResourceHelper.combineName(packageName, name), destinationFilePath, overwriteOldFile);
	}

	public static boolean fetchToFile(String fullName, String destinationFilePath, boolean overwriteOldFile) throws IOException
	{
		InputStream inputStream = ResourceHelper.getAsStream(fullName);
		FileOutputStream fileOutputStream = null;
		if(inputStream == null)
		{
			throw new IOException("Can't get resource: " + fullName);
		}
		else
		{
			try
			{
				File directory = new File(destinationFilePath.substring(0, destinationFilePath.lastIndexOf(File.separator)));
				if (!directory.exists())
				{
					directory.mkdirs();
				}

				File file = new File(destinationFilePath);
				if (file.exists())
				{
					if (overwriteOldFile)
					{
						file.delete();
					}
					else
					{
						throw new IOException(destinationFilePath + " already exists");
					}
				}

				file.createNewFile();
				fileOutputStream = new FileOutputStream(destinationFilePath);
				byte[] buffer = new byte[4096];
				int readCount = inputStream.read(buffer, 0, buffer.length);
				while (readCount != -1)
				{
					fileOutputStream.write(buffer, 0, readCount);
					readCount = inputStream.read(buffer, 0, buffer.length);
				}
				return true;
			}
			catch(IOException exception)
			{
				throw exception;
			}
			finally
			{
				if(inputStream != null)
				{
					inputStream.close();
				}
				if(fileOutputStream != null)
				{
					fileOutputStream.close();
				}
			}
		}
	}

	public static void playAsSound(String packageName, String name) throws Exception
	{
		ResourceHelper.playAsSound(ResourceHelper.combineName(packageName, name));
	}

	public static void playAsSound(String fullName) throws Exception
	{
		InputStream inputStream = null;
		ByteArrayInputStream inputStream2 = null;
		try
		{
			SoundPlayer.get_default().stop();
			inputStream = ResourceHelper.getAsStream(fullName);
			byte[] buffer = new byte[inputStream.available()] ;
			inputStream.read(buffer);
			inputStream2 = new ByteArrayInputStream(buffer);
			SoundPlayer.get_default().play(inputStream2);
		}
		catch(Exception exception)
		{
			throw exception;
		}
		finally
		{
			if(inputStream2 != null)
			{
				inputStream2.close();
			}

			if(inputStream != null)
			{
				inputStream.close();
			}
		}
	}

	private static String combineName(String packageName, String name)
	{
		return packageName + "/" + name;
	}
}
