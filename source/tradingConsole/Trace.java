package tradingConsole;

import java.io.FileOutputStream;
import java.io.IOException;

import framework.diagnostics.TraceManager;
import framework.diagnostics.TraceSource;
import framework.diagnostics.TraceType;

import tradingConsole.settings.PublicParametersManager;
import framework.io.DirectoryHelper;
import tradingConsole.framework.ResourceHelper;
import tradingConsole.ui.AlertForm;

public class Trace
{
	private Trace()
	{
	}

	public static void setLogProperties(TradingConsole tradingConsole)
	{
		String propertiesFilePath = "";
		if (PublicParametersManager.isSpecialTrace)
		{
			//propertiesFilePath = AppToolkit.get_SettingDirectory();
			propertiesFilePath = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(),"TradingConsole.Trace.Properties");
		}
		else
		{
			//propertiesFilePath = AppToolkit.get_ConfigurationDirectory();

			//propertiesFilePath = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(),"DefaultTradingConsole.Trace.Properties");
			propertiesFilePath = DirectoryHelper.combine(AppToolkit.get_CompanySettingDirectory(),"TradingConsole.Trace.Properties");
			try
			{
				ResourceHelper.fetchToFile("Configuration","TradingConsole.Trace.Properties", propertiesFilePath, true);
			}
			catch (IOException iOException)
			{
				AlertForm alertForm = new AlertForm("ERROR", "Failed to read TradingConsole.Trace.Properties!");
				alertForm.show();
				//TradingConsole.traceSource.trace(TraceType.Error, iOException);
			}
		}
		//propertiesFilePath += "TradingConsole.Trace.Properties";

		String logDirectory = AppToolkit.get_LogDirectory();
		LoginInformation loginInformation = tradingConsole.get_LoginInformation();
		if (loginInformation != null && loginInformation.getIsConnected())
		{
			logDirectory += tradingConsole.get_LoginInformation().get_LoginName();
		}
		TraceManager.setConfigPath(propertiesFilePath, logDirectory);
	}

	public static void GetTracePropertiesForJava(TradingConsoleServer tradingConsoleServer)
	{
		try
		{
			byte[] traceProperties = tradingConsoleServer.GetTracePropertiesForJava();
			//String propertiesFilePath = AppToolkit.get_SettingDirectory() + "TradingConsole.Trace.Properties";
			String propertiesFilePath = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(),"TradingConsole.Trace.Properties");
			FileOutputStream outputStream = new FileOutputStream(propertiesFilePath);
			outputStream.write(traceProperties);
			outputStream.close();
		}
		catch (IOException exception)
		{
			AlertForm alertForm = new AlertForm("ERROR", "Failed to read TradingConsole.Trace.Properties!");
			alertForm.show();
			//TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}
}
