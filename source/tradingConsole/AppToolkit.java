package tradingConsole;

import java.io.*;
import java.math.*;
import java.net.*;
import java.text.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;

import com.jidesoft.swing.*;
import framework.*;
import framework.DateTime;
import framework.diagnostics.*;
import framework.io.*;
import framework.xml.*;
import tradingConsole.framework.*;
import tradingConsole.ui.*;



import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;


public class AppToolkit
{
	private static Dimension _screenSize;
	//private static String _appBaseDirectory;
	//private static String _configurationDirectory;
	//private static String _languageDirectory;
	private static String _chartDataDirectory;
	private static String _settingDirectory;
	private static String _logDirectory;
	private static String _dumpDirectory;
	private static String _install4JDirectory;

	private static String _companyPath;
	private static String _companySettingDirectory;

	//Added by Michael on 2008-04-23
	private static DateTime _minDatabaseDateTime;

	static
	{
	com.jidesoft.utils.Lm.verifyLicense("Omnicare System Limited", "iTrader","TEzuZ3nWadgaTf8Lf6BvmJSbwyBlhFD2");
	}
	/*
	  static
	  {
	  //System.out.print("----------------------------------------");
	  //System.getProperties().list(System.out);
	  //System.out.print("----------------------------------------");

	 AppToolkit._screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	 //String appBaseDirectory = DirectoryHelper.getCurrentDirectory(); //TradingConsole.class);
	 //String appBaseDirectory = DirectoryHelper.getParentDirectory(AppToolkit.class);
	 String appBaseDirectory = System.getProperty("app.basePath");
	 System.out.println(appBaseDirectory);
	 if (appBaseDirectory == null)
	 {
	  appBaseDirectory = DirectoryHelper.getCurrentDirectory();
	 }
	 else
	 {
	  appBaseDirectory += File.separator;
	 }
		AppToolkit._appBaseDirectory = appBaseDirectory;
	 AppToolkit._configurationDirectory = appBaseDirectory + "Configuration" + File.separator;
	 AppToolkit._languageDirectory = appBaseDirectory + "Language" + File.separator;
	 AppToolkit._chartDataDirectory = appBaseDirectory + "ChartData" + File.separator;
	 AppToolkit._settingDirectory = appBaseDirectory + "UserSetting" + File.separator;
	 AppToolkit._logDirectory = appBaseDirectory + "Log" + File.separator;
	  }
	 */

	/*
	 static
	  {
	  //System.out.print("----------------------------------------");
	  //System.getProperties().list(System.out);
	  //System.out.print("----------------------------------------");

	   AppToolkit._screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	   String userHomeDirectory = System.getProperty("user.home");

	   System.out.println(userHomeDirectory);

	   userHomeDirectory = DirectoryHelper.combine(userHomeDirectory, "iTrader");

	   //AppToolkit._configurationDirectory = userHomeDirectory + "Configuration" + File.separator;
	   //AppToolkit._languageDirectory = userHomeDirectory + "Language" + File.separator;

	   AppToolkit._chartDataDirectory = DirectoryHelper.combine(userHomeDirectory,"ChartData");
	   AppToolkit._settingDirectory = DirectoryHelper.combine(userHomeDirectory,"UserSetting");
	   AppToolkit._logDirectory = DirectoryHelper.combine(userHomeDirectory,"Log");
	  }
	 */

	//first call
	public static void setDirectory(String companyPath)
	{
		AppToolkit._companyPath = companyPath;

		AppToolkit._screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//String userHomeDirectory = System.getProperty("user.home");
		String userHomeDirectory = System.getProperty("AppDir");
		AppToolkit._install4JDirectory = DirectoryHelper.combine(userHomeDirectory, ".install4j");

		userHomeDirectory = DirectoryHelper.combine(userHomeDirectory, "iTrader");
		userHomeDirectory = DirectoryHelper.combine(userHomeDirectory, AppToolkit._companyPath);

		AppToolkit.createDirectory(userHomeDirectory);

		AppToolkit._companySettingDirectory = DirectoryHelper.combine(userHomeDirectory, "CompanySetting");
		AppToolkit.createDirectory(AppToolkit._companySettingDirectory);

		AppToolkit._chartDataDirectory = DirectoryHelper.combine(userHomeDirectory, "ChartData");
		AppToolkit._settingDirectory = DirectoryHelper.combine(userHomeDirectory, "UserSetting");
		AppToolkit._logDirectory = DirectoryHelper.combine(userHomeDirectory, "Log");
	}

	public static String getLocalIPAddress()
	{
		try
		{
			InetAddress localAddress = InetAddress.getLocalHost();

			TradingConsole.traceSource.trace(TraceType.Information, localAddress.getHostAddress() + (localAddress.isAnyLocalAddress() ? " is AnyLocalAddress" : " is not AnyLocalAddress"));
			TradingConsole.traceSource.trace(TraceType.Information, localAddress.getHostAddress() + (localAddress.isLinkLocalAddress() ? " is LinkLocalAddress" : " is not LinkLocalAddress"));
			TradingConsole.traceSource.trace(TraceType.Information, localAddress.getHostAddress() + (localAddress.isLoopbackAddress() ? " is LoopbackAddress" : " is not LoopbackAddress"));
			TradingConsole.traceSource.trace(TraceType.Information, localAddress.getHostAddress() + (localAddress.isSiteLocalAddress() ? " is SiteLocalAddress" : " is not SiteLocalAddress"));

			if(!localAddress.isAnyLocalAddress() && !localAddress.isLinkLocalAddress()
			   && !localAddress.isLoopbackAddress() && !localAddress.isSiteLocalAddress())
			{
				return localAddress.getHostAddress();
			}
		}
		catch (UnknownHostException ex)
		{
		}
		return null;
	}

	//call it after loggedin
	public static void setDirectory(Guid userId)
	{
		AppToolkit._screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		//String userHomeDirectory = System.getProperty("user.home");
		String userHomeDirectory = System.getProperty("AppDir");

		userHomeDirectory = DirectoryHelper.combine(userHomeDirectory, "iTrader");
		userHomeDirectory = DirectoryHelper.combine(userHomeDirectory, AppToolkit._companyPath);
		userHomeDirectory = DirectoryHelper.combine(userHomeDirectory, userId.toString());

		AppToolkit.createDirectory(userHomeDirectory);

		AppToolkit._chartDataDirectory = DirectoryHelper.combine(userHomeDirectory, "ChartData");
		AppToolkit.createDirectory(AppToolkit._chartDataDirectory);
		AppToolkit._settingDirectory = DirectoryHelper.combine(userHomeDirectory, "UserSetting");
		AppToolkit.createDirectory(AppToolkit._settingDirectory);
		AppToolkit._logDirectory = DirectoryHelper.combine(userHomeDirectory, "Log");

		AppToolkit._dumpDirectory = DirectoryHelper.combine(userHomeDirectory, "Dump");
		AppToolkit.createDirectory(AppToolkit._dumpDirectory);
	}

	//Added by Michael on 2008-04-23
	public static DateTime get_MinDatabaseDateTime()
	{
		if (AppToolkit._minDatabaseDateTime == null)
		{
			AppToolkit._minDatabaseDateTime = AppToolkit.getDateTime("1970-01-01 23:59:59");
		}
		return AppToolkit._minDatabaseDateTime;
	}

	public static Dimension get_ScreenSize()
	{
		return AppToolkit._screenSize;
	}

	public static String getCustomerAccountName(Customer customer, Account account)
	{
		String customerName = customer.get_CustomerName();
		String accountName = account.get_Code();
		if(customerName.equals(accountName))
		{
			return customerName;
		}
		else
		{
			return customerName + "," + accountName;
		}
	}


	//public static String get_AppBaseDirectory()
	//{
	//	return AppToolkit._appBaseDirectory;
	//}

	//public static String get_ConfigurationDirectory()
	//{
	//	return AppToolkit._configurationDirectory;
	//}

	//public static String get_LanguageDirectory()
	//{
	//	return AppToolkit._languageDirectory;
	//}

	public static String get_ChartDataDirectory()
	{
		return AppToolkit._chartDataDirectory;
	}

	public static String get_SettingDirectory()
	{
		return AppToolkit._settingDirectory;
	}

	public static String get_CompanySettingDirectory()
	{
		return AppToolkit._companySettingDirectory;
	}

	public static String get_LogDirectory()
	{
		return AppToolkit._logDirectory;
	}

	public static String get_DumpDirectory()
	{
		return AppToolkit._dumpDirectory;
	}

	public static String get_Install4JDirectory()
	{
		return AppToolkit._install4JDirectory;
	}

	public static String getColumnVisibilityPersistentFileName()
	{
		return DirectoryHelper.combine(AppToolkit._settingDirectory, "columnvisibility");
	}

	public static void fixJavaLanguage()
	{
		Version version = Version.get_CurrentVersion();
		if (version.get_major() == 1 && version.get_minor() == 2)
		{
			String osVersion = System.getProperty("os.version");
			if (framework.Convert.toDouble(osVersion) > 4.0)
			{
				String javaLib = System.getProperty("java.home") + File.separator + "lib";
				String sourceFilePath = javaLib + File.separator + "font.properties.zh.NT4.0";
				File sourceFile = new File(sourceFilePath);
				if (!sourceFile.exists())
				{
					return;
				}
				String destinationFilePath = javaLib + File.separator + "font.properties.zh." + osVersion;

				File destinationFile = new File(destinationFilePath);
				if (destinationFile.exists())
				{
					return;
				}
				else
				{
					AppToolkit.copyFile(sourceFilePath, destinationFilePath);
					AlertForm alertForm = new AlertForm("Notify",
						"Incorrect data may result from the public nature of Java. Please exit and restart the application.");
					alertForm.show();
				}
			}
		}
	}

	public static void copyFile(String sourceFilePath, String destinationFilePath)
	{
		try
		{
			InputStream inputStream = new FileInputStream(sourceFilePath);
			FileOutputStream outputStream = new FileOutputStream(destinationFilePath);
			int byteRead;
			byte[] data = new byte[1024];
			while ( -1 != (byteRead = inputStream.read(data)))
			{
				outputStream.write(data, 0, byteRead);
			}
			outputStream.close();
			inputStream.close();
		}
		catch (FileNotFoundException exception)
		{
			exception.printStackTrace();
		}
		catch (IOException exception)
		{
			exception.printStackTrace();
		}
	}

	public static boolean writeFile(String filePath, String data, boolean append)
	{
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(new FileWriter(filePath, append));
			writer.print(data);
			return true;
		}
		catch (IOException exception)
		{
			//TradingConsole.traceSource.trace(TraceType.Error,exception);
			return false;
		}
		finally
		{
			if (writer != null)
			{
				writer.close();
			}
		}
	}

	public static BigDecimal convertDBValueToBigDecimal(Object item, double defaultValue)
	{
		return AppToolkit.isDBNull(item) ? (new BigDecimal(defaultValue)) : ( item instanceof BigDecimal ? (BigDecimal)item : new BigDecimal(item.toString()));
	}

	public static double convertDBValueToDouble(Object item, double defaultValue)
	{
		return AppToolkit.isDBNull(item) ? defaultValue : ( (BigDecimal)item).doubleValue();
	}

	public static boolean isDBNull(Object object)
	{
		return object.equals(DBNull.value);
	}

	public static double round(double value, double decimalDigits)
	{
		double multiple = Math.pow(10, decimalDigits);

		return Math.round(value * multiple) / multiple;
	}

	public static String duplicateZero(int count)
	{
		if (count < 0)
		{
			return "";
		}
		String zeroString = "00000000";
		return zeroString.substring(0, count);
	}

	public static String format2(double value, int decimalDigits)
	{
		value = AppToolkit.round(value, decimalDigits);
		//String valueString = NumberFormat.getInstance().format(value);
		//valueString = StringHelper.replace(valueString,",","");
		DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.SIMPLIFIED_CHINESE);
		decimalFormat.applyPattern("##########0.########");
		String valueString = decimalFormat.format(value);

		int index = valueString.indexOf(".");
		int padZero = 0;
		if (index > -1)
		{
			padZero = decimalDigits - (valueString.length() - index - 1);
		}
		else
		{
			padZero = decimalDigits;
			valueString += ".";
		}

		return valueString.concat(AppToolkit.duplicateZero(padZero));
	}

	//will rewrite...............
	/*public static String format2(double value, int decimalDigits)
	{
		value = AppToolkit.round(value, decimalDigits);
		//String valueString = NumberFormat.getInstance().format(value);
		//valueString = StringHelper.replace(valueString,",","");

		String valueString = new DecimalFormat("##########0.########").format(value);

		int index = valueString.indexOf(".");
		int padZero = 0;
		if (index > -1)
		{
			padZero = decimalDigits - (valueString.length() - index - 1);
		}
		else
		{
			padZero = decimalDigits;
			valueString += ".";
		}

		return valueString.concat(AppToolkit.duplicateZero(padZero));
	}*/

	//will rewrite...............
	public static String format(double value, int decimalDigits) //has ","
	{
		value = AppToolkit.round(value, decimalDigits);
		//String valueString = NumberFormat.getInstance().format(value);
		//valueString = StringHelper.replace(valueString,",","");

		String valueString = new DecimalFormat("##########0.########").format(value);

		String newStr = valueString;
		String symbolPN = "";
		String firstChar = newStr.substring(0, 1);
		if (firstChar.equals("+") || firstChar.equals("-"))
		{
			symbolPN = firstChar;
			newStr = newStr.substring(1, newStr.length());
		}
		String rightPart = "";
		int iLength = newStr.length();
		int index = newStr.indexOf(".");
		int padZero = 0;
		if (index > -1)
		{
			padZero = decimalDigits - (iLength - index - 1);
			rightPart = newStr.substring(index); //, iLength - index);
			newStr = newStr.substring(0, index);
		}
		else
		{
			padZero = decimalDigits;
		}

		String leftPart = "";
		iLength = newStr.length();
		if (iLength <= 3)
		{
			leftPart = newStr;
		}
		while (iLength > 3)
		{
			iLength = newStr.length();
			leftPart = "," + newStr.substring(iLength - 3) + leftPart;
			newStr = newStr.substring(0, iLength - 3);
			iLength = newStr.length();
			if (iLength <= 3)
			{
				leftPart = newStr + leftPart;
			}
		}
		valueString = symbolPN + leftPart + rightPart;
		if (index <= -1 && padZero != 0)
		{
			valueString += ".";
		}

		return valueString.concat(AppToolkit.duplicateZero(padZero));
	}

	public static int convertStringToInteger(Object item, int defaultValue)
	{
		if(AppToolkit.isDBNull(item)) return defaultValue;
		try
		{
			String str = item.toString();
			return StringHelper.isNullOrEmpty(str) ? 0 : Integer.valueOf(str).intValue();
		}
		catch (Throwable exception)
		{
			return defaultValue;
		}
	}

	private static String trimFormat(String str)//see format below
	{
		StringBuilder trimedVlaue = new StringBuilder();
		for(int index = 0; index < str.length(); index++)
		{
			char c = str.charAt(index);
			if(c == ',' || c == '+')
			{
				continue;
			}
			else
			{
				trimedVlaue.append(c);
			}
		}
		return trimedVlaue.toString();
	}

	public static double convertStringToDouble(String str)
	{
		str = AppToolkit.trimFormat(str);
		if(StringHelper.isNullOrEmpty(str)) return 0.0;
		try
		{
			return Double.parseDouble(str);
		}
		catch (Throwable exception)
		{
			return 0.0;
		}
	}

	public static BigDecimal convertStringToBigDecimal(String str)
	{
		if (StringHelper.isNullOrEmpty(str))
		{
			return BigDecimal.ZERO;
		}
		try
		{
			return new BigDecimal(str);
		}
		catch (Throwable exception)
		{
			return BigDecimal.ZERO;
		}
	}

	public static BigDecimal fixLot(BigDecimal lot, boolean isOpen, TradePolicyDetail tradePolicyDetail, Account account)
	{
		BigDecimal minLot = isOpen ? tradePolicyDetail.get_MinOpen() : tradePolicyDetail.get_MinClose();
		minLot = minLot.multiply(account.get_RateLotMin());
		if(lot.compareTo(minLot) <= 0) return minLot;

		BigDecimal lotMultiple = isOpen ? tradePolicyDetail.get_OpenMultiplier() : tradePolicyDetail.get_CloseMultiplier();
		BigDecimal step = account.get_RateLotMultiplier().multiply(lotMultiple);
		BigDecimal fixedLot = lot;
		if(step.compareTo(BigDecimal.ZERO) > 0)
		{
			BigDecimal multiple = lot.divide(step, BigDecimal.ROUND_FLOOR);
			fixedLot = step.multiply(BigDecimal.valueOf(multiple.longValue()));
		}
		return fixedLot.compareTo(minLot) < 0 ? minLot : fixedLot;
	}

	private static boolean isValidLot(BigDecimal lot, boolean isOpen, TradePolicyDetail tradePolicyDetail, Account account)
	{
		BigDecimal lot2 = AppToolkit.fixLot(lot, isOpen, tradePolicyDetail, account);
		return lot2.compareTo(lot) == 0;
	}

	public static BigDecimal getDefaultLot(Instrument instrument, boolean isOpen, TradePolicyDetail tradePolicyDetail, Account account)
	{
		BigDecimal defaultLot = tradePolicyDetail.get_DefaultLot().multiply(account.get_RateDefaultLot());
		BigDecimal lastPlaceLot =null;
		String fileName = String.format("%s.txt",account. get_Id().toString());
		File file = new File(fileName);
		boolean isSaved=false;
		String instrumentID=instrument.get_Id().toString();
		if(file.exists())
		{
			BufferedReader inputStream = null;
			try{
				inputStream = new BufferedReader(new FileReader(fileName));
				String l;
				while((l=inputStream.readLine())!=null){
					if(l.indexOf(instrumentID)!=-1){
						String[] target= l.split(":");
						lastPlaceLot = new BigDecimal(target[1]);
						isSaved=true;
						break;
					}
				}
			}
			catch (FileNotFoundException ex)
			{
				/** @todo Handle this exception */
			}
			catch (IOException ex)
			{
				/** @todo Handle this exception */
			}

			finally{
				if(inputStream!=null){
					try
					{
						inputStream.close();
					}
					catch (IOException ex1)
					{
					}
				}
			}
		}
		if(!isSaved){
		    lastPlaceLot=PalceLotNnemonic.getLastPlaceLot(instrument.get_Id(), account.get_Id());
		}


		if (lastPlaceLot != null && AppToolkit.isValidLot(lastPlaceLot, isOpen, tradePolicyDetail, account))
		{
			defaultLot = lastPlaceLot;
		}
		BigDecimal maxLot = AppToolkit.fixLot(instrument.get_MaxDQLot(), isOpen, tradePolicyDetail, account);
		defaultLot = defaultLot.compareTo(maxLot) > 0 ? maxLot : defaultLot;

		BigDecimal minLot = isOpen ? tradePolicyDetail.get_MinOpen() : tradePolicyDetail.get_MinClose();
		minLot = minLot.multiply(account.get_RateLotMin());
		return defaultLot.compareTo(minLot) > 0 ? defaultLot : minLot;
	}

	public static BigDecimal fixDefaultLot(BigDecimal defaultLot, boolean isOpen, TradePolicyDetail tradePolicyDetail, Account account)
	{
		BigDecimal minLot = isOpen ? tradePolicyDetail.get_MinOpen() : tradePolicyDetail.get_MinClose();
		minLot = minLot.multiply(account.get_RateLotMin());
		return defaultLot.compareTo(minLot) > 0 ? defaultLot : minLot;
	}

	public static String getFormatLot(BigDecimal lot, Account account, Instrument instrument)
	{
		return AppToolkit.getFormatLot(lot, account.getIsSplitLot(instrument));
	}

	public static String getFormatLot(BigDecimal lot, boolean isSplitLot)
	{
		String lotString = (lot == null || lot.compareTo(BigDecimal.ZERO) == 0) ? "" : Double.toString(lot.doubleValue());
		if (StringHelper.isNullOrEmpty(lotString) || lot.doubleValue() <= 0)
		{
			return "";
		}
		if (lotString.length() > 0 && lotString.startsWith("."))
		{
			lotString = "0" + lotString;
		}
		String returnResult = lotString;
		int pointIndex = lotString.indexOf(".");
		if (isSplitLot)
		{
			if (pointIndex > 0)
			{
				/*
					 if (lotString.length() > pointIndex+2)
					 {
				 returnResult = lotString.substring(0, pointIndex) + "." + lotString.substring(pointIndex + 1, pointIndex + 3);
				 double lot2 = AppToolkit.round(Double.parseDouble(returnResult),1);//Math.round(Double.parseDouble(returnResult)*1000)/1000;
				 String lot2String = Double.toString(lot2);
				 int pointIndex2 = lot2String.indexOf(".");
				 returnResult = lot2String.substring(0, pointIndex2) + "." + lot2String.substring(pointIndex2 + 1, pointIndex2 + 2);
					 }
					 else
					 {
				 returnResult = lotString.substring(0, pointIndex) + "." + lotString.substring(pointIndex + 1, pointIndex + 2);
					 }
				 */
				//double lot2 = AppToolkit.round(lot.doubleValue(), 4);
				//String lot2String = Double.toString(lot2);
				//int pointIndex2 = lot2String.indexOf(".");
				//returnResult = lot2String.substring(0, pointIndex2) + "." + lot2String.substring(pointIndex2 + 1, pointIndex2 + 2);
			}
			else
			{
				returnResult = lotString + ".0";
			}
		}
		else
		{
			if (pointIndex > 0)
			{
				returnResult = lotString.substring(0, pointIndex);
			}
		}

		return returnResult;
	}

	public static Rectangle getRectangleByDimension(Dimension dimension)
	{
		Dimension screenSize = AppToolkit._screenSize;
		int xLocation = screenSize.width / 2 - dimension.width / 2;
		int yLocation = screenSize.height / 2 - dimension.height / 2;
		return new Rectangle(xLocation, yLocation, dimension.width, dimension.height);
	}

	public static Rectangle getRectangleByDimension2()
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int xLocation = 0;
		int yLocation = 0;
		return new Rectangle(xLocation, yLocation, screenSize.width, screenSize.height - 30);
	}

	public static XmlNode getUserSettingXml(String name)
	{
		String filePath = DirectoryHelper.combine(AppToolkit._settingDirectory, name);
		return AppToolkit.getXml(filePath);
	}

	public static XmlNode getCompanySettingXml(String name)
	{
		return AppToolkit.getCompanySettingXml(name, true);
	}

	public static XmlNode getCompanySettingXml(String name, boolean showAlterDialog)
	{
		String filePath = DirectoryHelper.combine(AppToolkit._companySettingDirectory, name);
		return AppToolkit.getXml(filePath, showAlterDialog);
	}

	private static XmlNode getXml(String filePath)
	{
		return AppToolkit.getXml(filePath, true);
	}

	private static XmlNode getXml(String filePath, boolean showAlterDialog)
	{
		try
		{
			System.out.println(filePath);

			XmlDocument xmlDocument = new XmlDocument();
			xmlDocument.load(filePath);
			XmlNode xmlNode = xmlDocument.get_DocumentElement();
			return xmlNode;
		}
		catch (Throwable exception)
		{
			if(showAlterDialog)
			{
				AlertForm alertForm = new AlertForm("ERROR", "Incorrect filePath: " + filePath);
				alertForm.show();
				alertForm.toFront();
			}
			exception.printStackTrace();
		}
		return null;
	}

	public static XmlNode getResourceXml(String packageName, String name)
	{
		InputStream inputStream = null;
		try
		{
			XmlDocument xmlDocument = new XmlDocument();
			inputStream = ResourceHelper.getAsStream(packageName, name);
			if (inputStream != null)
			{
				xmlDocument.load(inputStream);
			}
			else
			{
				AlertForm alertForm = new AlertForm("ERROR", "Incorrect name: packageName = " + packageName + "; name= " + name);
				alertForm.show();
				return null;
			}
			XmlNode xmlNode = xmlDocument.get_DocumentElement();
			return xmlNode;
		}
		catch (Throwable exception)
		{
			AlertForm alertForm = new AlertForm("ERROR", "Incorrect name: packageName = " + packageName + "; name= " + name);
			alertForm.show();

			exception.printStackTrace();
		}
		finally
		{
			if(inputStream != null)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException ex)
				{
					TradingConsole.traceSource.trace(TraceType.Error, ex);
				}
			}
		}
		return null;
	}

	public static boolean checkConfigurateFile()
	{
		boolean isExists = false;
		String message = "";
		String[] files = new String[]
			{"TradingConsole.Trace.Properties", "TradingConsole.config", "Settings.xml", "user.preferences"};
		//String configurationDirectory = AppToolkit.get_ConfigurationDirectory();
		for (int i = 0; i < files.length; i++)
		{
			//String filePath = configurationDirectory + files[i];
			//isExists = AppToolkit.isExistsFilePath(filePath);
			try
			{
				isExists = ResourceHelper.exists("Configuration", files[i]);
			}
			catch (IOException iOException)
			{
				isExists = false;
			}
			if (!isExists)
			{
				//message += filePath + TradingConsole.enterLine + TradingConsole.enterLine;
				message += files[i] + TradingConsole.enterLine + TradingConsole.enterLine;
			}
		}
		if (!framework.StringHelper.isNullOrEmpty(message))
		{
			AlertForm alertForm = new AlertForm("ERROR", "Not exists filePath(s): " + message);
			alertForm.show();
			return false;
		}
		return true;
	}

	public static boolean isExistsFilePath(String filePath)
	{
		File filePath2 = new File(filePath);
		if (!filePath2.exists())
		{
			return false;
		}
		else
		{
			if (!filePath2.isFile())
			{
				return false;
			}
		}
		return true;
	}

	public static synchronized boolean saveData(String fillPath, String data, boolean append)
	{
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(new FileWriter(fillPath, append));
			writer.print(data);
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			if (writer != null)
			{
				writer.close();
			}
		}
	}

	public static void createDirectory(String directory)
	{
		File path = new File(directory);
		if (!path.exists())
		{
			path.mkdirs();
		}
	}

	public static void deleteFile(String filePath)
	{
		File filePath2 = new File(filePath);
		if (filePath2.exists())
		{
			filePath2.delete();
		}
	}

	//get a random to reset system
	public static int getRandom()
	{
		int randomValue = (int) ( (Math.random() * 100 + 30) / 3);
		if (randomValue > 30)
		{
			randomValue = AppToolkit.getRandom();
			return randomValue;
		}
		return randomValue;
	}

	public static DateTime getTimestamp(String dateTimeString)
	{
		dateTimeString = StringHelper.replace(dateTimeString, "/", "-");
		return XmlConvert.toDateTime(dateTimeString, "yyyy-MM-dd'T'HH:mm:ss.fffffff");
	}

	public static DateTime getDateTime(String dateTimeString)
	{
		dateTimeString = StringHelper.replace(dateTimeString, "/", "-");
		return XmlConvert.toDateTime(dateTimeString, "yyyy-MM-dd HH:mm:ss");
	}

	public static DateTime getDate(String dateString)
	{
		dateString = StringHelper.replace(dateString, "/", "-");
		return XmlConvert.toDateTime(dateString, "yyyy-MM-dd");
	}

	public static boolean isUrl(String urlString)
	{
		boolean isUrl = false;
		try
		{
			if (!StringHelper.isNullOrEmpty(urlString))
			{
				URL url = new URL(urlString);
				isUrl = true;
			}
		}
		catch (MalformedURLException malformedURLException)
		{
		}
		return isUrl;
	}

	public static String getHost(String urlString)
	{
		try
		{
			URL url = new URL(urlString);
			return url.getHost();
		}
		catch (MalformedURLException malformedURLException)
		{
			AlertForm alertForm = new AlertForm("Notify", "Incorrect url: " + urlString);
			alertForm.show();
		}
		return "";
	}

	public static int getPort(String urlString)
	{
		int port = 80;
		try
		{
			URL url = new URL(urlString);
			port = url.getPort();
			port = (port == -1) ? 80 : port;
		}
		catch (MalformedURLException malformedURLException)
		{
		}
		return port;
	}

	public static String getProtocol(String urlString)
	{
		try
		{
			URL url = new URL(urlString);
			return url.getProtocol();
		}
		catch (MalformedURLException malformedURLException)
		{
		}
		return "http";
	}

	public static URL getURL(String urlString)
	{
		URL url = null;
		try
		{
			url = new URL(urlString);
		}
		catch (MalformedURLException malformedURLException)
		{
			AlertForm alertForm = new AlertForm("ERROR", "Incorrect url: " + urlString);
			alertForm.show();
		}
		return url;
	}

	public static String changeUrlHost(String urlString, String newHost)
	{
		urlString = urlString.trim();
		URL url = AppToolkit.getURL(urlString);
		if (url != null)
		{
			String protocol = url.getProtocol();
			protocol += "://";
			if (urlString.substring(0, protocol.length()).equalsIgnoreCase(protocol))
			{
				urlString = urlString.substring(protocol.length(), urlString.length());
			}
			String oldHost = url.getHost();
			if(StringHelper.isNullOrEmpty(oldHost))
			{
				urlString = newHost + urlString;
			}
			else
			{
				urlString = StringHelper.replace(urlString, oldHost, newHost, 1);
			}
			return protocol + urlString;
		}
		return urlString;
	}

	public static String changeToMapPort(String urlString, int mapPort)
	{
		urlString = urlString.trim();
		URL url = AppToolkit.getURL(urlString);
		if (url != null)
		{
			String protocol = url.getProtocol();
			protocol += "://";
			if (urlString.substring(0, protocol.length()).equalsIgnoreCase(protocol))
			{
				urlString = urlString.substring(protocol.length(), urlString.length());
			}
			String host = url.getHost();
			if (urlString.substring(0, host.length()).equalsIgnoreCase(host))
			{
				urlString = urlString.substring(host.length(), urlString.length());
			}
			if (urlString.substring(0, ":".length()).equalsIgnoreCase(":"))
			{
				int port = url.getPort();
				if (port == -1)
				{
					urlString = ":" + XmlConvert.toString(mapPort) + urlString;
				}
				else
				{
					urlString = StringHelper.replace(urlString, XmlConvert.toString(port), XmlConvert.toString(mapPort), 1);
				}
			}
			else
			{
				urlString = ":" + XmlConvert.toString(mapPort) + urlString;
			}
			return protocol + host + urlString;
		}
		return urlString;
	}

	public static String changeUrlProtocol(String urlString, String newProtocol)
	{
		urlString = urlString.trim();
		URL url = AppToolkit.getURL(urlString);
		if (url != null)
		{
			String protocol = url.getProtocol();
			protocol += "://";
			if (urlString.substring(0, protocol.length()).equalsIgnoreCase(protocol))
			{
				urlString = urlString.substring(protocol.length(), urlString.length());
			}
			return newProtocol + "://" + urlString;
		}
		return urlString;
	}

	public static Image getImage(String imageFile)
	{
		Image image = null;
		try
		{
			image = ResourceHelper.getAsImage("Images", imageFile);
		}
		catch (IOException iOException)
		{
			TradingConsole.traceSource.trace(TraceType.Error, iOException);
		}
		return image;
	}

	public static void menuItemInit(JMenuItem menuItem, String name, String text, String ToolTipText, String imageIcoFileName)
	{
		menuItem.setName(name);
		menuItem.setText(text);
		menuItem.setToolTipText(ToolTipText);
		Icon icon = AppToolkit.getAsIcon(imageIcoFileName);
		menuItem.setIcon(icon);
	}

	public static void toolBarItemInit(JideToggleButton button, String name, String text, String ToolTipText, String imageIcoFileName)
	{
		button.setName(name);
		//button.setText(text);
		button.setText("");
		button.setToolTipText(ToolTipText);
		Icon icon = AppToolkit.getAsIcon(imageIcoFileName);
		button.setIcon(icon);
		button.setButtonStyle(ButtonStyle.TOOLBAR_STYLE);
	}

	public static void toolBarItemInit(JideSplitButton button, String name, String text, String ToolTipText, String imageIcoFileName)
	{
		button.setName(name);
		//button.setText(text);
		button.setText("");
		button.setToolTipText(ToolTipText);
		Icon icon = AppToolkit.getAsIcon(imageIcoFileName);
		button.setIcon(icon);
	}

	public static Icon getAsIcon(String name)
	{
		Icon icon = null;
		try
		{
			icon = ResourceHelper.getAsIcon("Images", name);
		}
		catch (IOException iOException)
		{
			icon = null;
			TradingConsole.traceSource.trace(TraceType.Error, iOException);
		}

		return icon;
	}

	public static String getBriefSystemInfo()
	{
		return System.getProperty("java.version")
			+ "; " + System.getProperty("java.vendor")
			+"; " + System.getProperty("os.name")
			+ "; " + Runtime.getRuntime().availableProcessors()
			+ "; " + Runtime.getRuntime().maxMemory() /(1024 * 1024) + "m";
	}

	public static String getSystemInfo()
	{
		return "Java version=" +  System.getProperty("java.version")
			+ "; Java vendor=" + System.getProperty("java.vendor")
			+"; OS name=" +  System.getProperty("os.name")
			+ "; OS version=" + System.getProperty("os.version")
			+ "; Available processors=" + Runtime.getRuntime().availableProcessors()
			+ "; MaxMemory=" + Runtime.getRuntime().maxMemory() / (1024 * 1024) + "m";
	}
}
