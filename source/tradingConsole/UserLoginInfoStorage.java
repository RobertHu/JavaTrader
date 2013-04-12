package tradingConsole;

import java.io.*;

import framework.*;
import framework.io.*;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.SecureRandom;
import java.security.Key;
import sun.misc.BASE64Encoder;
import java.security.*;
import sun.misc.BASE64Decoder;

public class UserLoginInfoStorage
{
	private static boolean saveUserName = false;
	private static boolean savePassword = false;
	private static String userName = "";
	private static String password = "";

	public static void save(String userName, boolean saveUserName, String password, boolean savePassword)
	{
		if(!saveUserName && savePassword) return;

		try
		{
			String fileName = DirectoryHelper.combine(AppToolkit.get_CompanySettingDirectory(), "LoginInfo.dat");
			File file = new File(fileName);
			if (file.exists()) file.delete();

			FileOutputStream stream = new FileOutputStream(fileName);
			PrintWriter writer = new PrintWriter(stream);
			writer.println("saveUserName\t" + Boolean.toString(saveUserName));
			writer.println("savePassword\t" + Boolean.toString(savePassword));
			if(saveUserName) writer.println("userName\t" + userName);
			if(savePassword)
			{
				password = encryptPassword(userName, password);
				writer.println("password\t" + password);
			}
			writer.close();

			UserLoginInfoStorage.saveUserName = saveUserName;
			UserLoginInfoStorage.savePassword = savePassword;
			UserLoginInfoStorage.userName = userName;
			UserLoginInfoStorage.password = password;
		}
		catch(Exception ex)
		{
		}
	}

	public static void load()
	{
		saveUserName = savePassword = false;
		userName = password = "";

		try
		{
			String fileName = DirectoryHelper.combine(AppToolkit.get_CompanySettingDirectory(), "LoginInfo.dat");
			File file = new File(fileName);
			if (file.exists())
			{
				BufferedReader reader = new BufferedReader(new FileReader(fileName));
				String line = reader.readLine();
				while(line != null)
				{
					parse(line);
					line = reader.readLine();
				}
				decryptPassword();
				reader.close();
			}
		}
		catch(Exception ex)
		{
		}
	}

	private static void decryptPassword()
	{
		try
		{
			Key key = getKey(userName);

			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			BASE64Decoder base64en = new BASE64Decoder();
			byte[] encryptPassword = base64en.decodeBuffer(password);
			byte[] passwordDecrypted = cipher.doFinal(encryptPassword);
			password = new String(passwordDecrypted, "UTF8");
		}
		catch(Exception exception)
		{
		}
	}

	private static String encryptPassword(String userName, String password)
	{
		try
		{
			Key key = getKey(userName);

			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encryptPassword = cipher.doFinal(password.getBytes("UTF8"));
			BASE64Encoder base64en = new BASE64Encoder();
			return base64en.encode(encryptPassword);
		}
		catch(Exception exception)
		{
			return password;
		}
	}

	private static Key getKey(String userName) throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("MD5");
		 userName = confuse(userName);
		 byte[] digest = md.digest(userName.getBytes("UTF8"));

		 KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
		 keyGenerator.init(new SecureRandom(digest));
		 Key key = keyGenerator.generateKey();
		return key;
	}

	private static String confuseStr = "!@#$%^&*()_+|}{><?~";
	private static String confuse(String value)
	{
		StringBuilder sb = new StringBuilder();
		int confuseStrIndex = 0;
		for(int index = 0; index < value.length(); index++)
		{
			sb.append(value.charAt(index));
			confuseStrIndex = index >= confuseStr.length() ? index % confuseStr.length() : index;
			sb.append(confuseStr.charAt(confuseStrIndex));
		}
		return sb.toString();
	}

	private static void parse(String line)
	{
		if(StringHelper.isNullOrEmpty(line)) return;
		String[] items = StringHelper.split(line, "\t");
		if(items.length !=2) return;
		if(items[0].compareToIgnoreCase("saveUserName") == 0)
		{
			saveUserName = Boolean.parseBoolean(items[1]);
		}
		else if(items[0].compareToIgnoreCase("savePassword") == 0)
		{
			savePassword = Boolean.parseBoolean(items[1]);
		}
		else if(items[0].compareToIgnoreCase("userName") == 0)
		{
			userName = items[1];
		}
		else if(items[0].compareToIgnoreCase("password") == 0)
		{
			password = items[1];
		}
	}

	public static String get_UserName()
	{
		return userName;
	}

	public static String get_Password()
	{
		return password;
	}

	public static boolean get_saveUserName()
	{
		return saveUserName;
	}

	public static boolean get_savePassword()
	{
		return savePassword;
	}

}
