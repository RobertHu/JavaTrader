package tradingConsole.settings;

import java.io.File;
import java.io.*;
import java.util.Locale;

import framework.xml.XmlDocument;
import framework.xml.XmlNode;
import framework.StringHelper;
import framework.xml.XmlConvert;

import tradingConsole.AppToolkit;
import framework.io.DirectoryHelper;
import tradingConsole.ui.language.Language;
import framework.xml.XmlElement;
import tradingConsole.TradingConsole;
import framework.diagnostics.TraceType;

public class PublicParametersManager
{
	public static String version = "";
	public static boolean isEmptyAllLogFilesWhenEnterSystem = true;
	public static boolean isTrace = false;
	public static boolean isSpecialTrace = false;
	public static int gridsPerUnitRow = 6;
	public static boolean showInstrumentSpanGrid = false;
	public static String selectedChartInstrument = "";
	public static int minTimeTaken = 1000;
	public static boolean useGreenAsRiseColor = false;

	public static Locale getLocal()
	{
		Locale locale = Locale.getDefault();
		if (PublicParametersManager.version.equalsIgnoreCase("ENG"))
		{
			locale = Locale.ENGLISH;
		}
		else if (PublicParametersManager.version.equalsIgnoreCase("CHT"))
		{
			locale = Locale.TRADITIONAL_CHINESE;
		}
		else if (PublicParametersManager.version.equalsIgnoreCase("CHS"))
		{
			locale = Locale.SIMPLIFIED_CHINESE;//Locale.CHINESE;
		}
		else if (PublicParametersManager.version.equalsIgnoreCase("JPN"))
		{
			locale = Locale.JAPANESE;
		}
		else if (PublicParametersManager.version.equalsIgnoreCase("KOR"))
		{
			locale = Locale.KOREAN;
		}
		else if (PublicParametersManager.version.equalsIgnoreCase("FRE"))
		{
			locale = Locale.FRENCH;
		}
		else if (PublicParametersManager.version.equalsIgnoreCase("GER"))
		{
			locale = Locale.GERMAN;
		}
		else if (PublicParametersManager.version.equalsIgnoreCase("ITA"))
		{
			locale = Locale.ITALIAN;
		}

		return locale;
	}

	private PublicParametersManager()
	{
	}

	public static boolean initialize()
	{
		return PublicParametersManager.setValue(false);
	}

	public static String get_SelectedChartInstrument()
	{
		return PublicParametersManager.selectedChartInstrument;
	}

	public static void set_SelectedChartInstrument(String value)
	{
		PublicParametersManager.selectedChartInstrument = value;
	}

	public static boolean saveVersion()
	{
		return PublicParametersManager.setValue(true);
	}

	private static void fixUnitCell()
	{
		if (!PublicParametersManager.showInstrumentSpanGrid)
		{
			if (ServiceManager.isUseCell())
			{
				PublicParametersManager.showInstrumentSpanGrid = true;
				PublicParametersManager.gridsPerUnitRow = 6;
				//PublicParametersManager.saveVersion();
				//UnitGridRow.gridsPerUnitRow = PublicParametersManager.gridsPerUnitRow;
			}
		}
	}

	private static boolean setValue(boolean isNeedUpdate)
	{
		PublicParametersManager.fixUnitCell();

		boolean isExistsFile = false;
		//String settingDirectory = AppToolkit.get_SettingDirectory();
		String settingDirectory = AppToolkit.get_CompanySettingDirectory();
		//String parameterFilePath = settingDirectory + "PublicParameter.xml";
		String parameterFilePath = DirectoryHelper.combine(settingDirectory,"PublicParameter.xml");
		File directory = new File(settingDirectory);
		if (!directory.exists())
		{
			if(!directory.mkdirs()) return false;
		}
		else
		{
			File file = new File(parameterFilePath);
			if (file.exists() && file.isFile())
			{
				if (!isNeedUpdate)
				{
					XmlNode xmlNode = AppToolkit.getCompanySettingXml("PublicParameter.xml");

					XmlNode xmlNode2 = xmlNode.get_Item("PublicParameter");
					XmlElement xmlElement = xmlNode2.get_Item("Version");
					PublicParametersManager.version = xmlElement.get_InnerText();
					xmlElement = xmlNode2.get_Item("IsEmptyAllLogFilesWhenEnterSystem");
					if (xmlElement != null)
					{
						PublicParametersManager.isEmptyAllLogFilesWhenEnterSystem = Boolean.valueOf(xmlElement.get_InnerText());
					}
					xmlElement = xmlNode2.get_Item("IsTrace");
					if (xmlElement != null)
					{
						PublicParametersManager.isTrace = Boolean.valueOf(xmlElement.get_InnerText());
					}
					xmlElement = xmlNode2.get_Item("IsSpecialTrace");
					if (xmlElement != null)
					{
						PublicParametersManager.isSpecialTrace = Boolean.valueOf(xmlElement.get_InnerText());
					}
					xmlElement = xmlNode2.get_Item("MinTimeTaken");
					if (xmlElement != null)
					{
						PublicParametersManager.minTimeTaken = Integer.valueOf(xmlElement.get_InnerText());
					}

					xmlElement = xmlNode2.get_Item("IsUseUnitGrid");
					if (xmlElement != null)
					{
						PublicParametersManager.showInstrumentSpanGrid = Boolean.valueOf(xmlElement.get_InnerText());
					}
					xmlElement = xmlNode2.get_Item("GridsPerUnitRow");
					if (xmlElement != null)
					{
						PublicParametersManager.gridsPerUnitRow = Integer.valueOf(xmlElement.get_InnerText());
					}
					xmlElement = xmlNode2.get_Item("SelectedChartInstrument");
					if (xmlElement != null)
					{
						PublicParametersManager.selectedChartInstrument = xmlElement.get_InnerText();
					}
				}
				isExistsFile = true;
			}
		}

		String isEmptyAllLogFilesWhenEnterSystemString = XmlConvert.toString(PublicParametersManager.isEmptyAllLogFilesWhenEnterSystem);
		String isTraceString = XmlConvert.toString(PublicParametersManager.isTrace);
		String isSpecialTraceString = XmlConvert.toString(PublicParametersManager.isSpecialTrace);
		String minTimeTakenString = XmlConvert.toString(PublicParametersManager.minTimeTaken);
		String isUseUnitGridString = XmlConvert.toString(PublicParametersManager.showInstrumentSpanGrid);
		String gridsPerUnitRowString = XmlConvert.toString(PublicParametersManager.gridsPerUnitRow);

		if (!isExistsFile)
		{
			Locale locale = Locale.getDefault();
			if (locale.equals(Locale.ENGLISH))
			{
				PublicParametersManager.version = "ENG";
			}
			else if (locale.equals(Locale.TRADITIONAL_CHINESE))
			{
				PublicParametersManager.version = "CHT";
			}
			else if (locale.equals(Locale.SIMPLIFIED_CHINESE) || locale.equals(Locale.CHINESE))
			{
				PublicParametersManager.version = "CHS";
			}
			else if (locale.equals(Locale.JAPANESE))
			{
				PublicParametersManager.version = "JPN";
			}
			else if (locale.equals(Locale.KOREAN))
			{
				PublicParametersManager.version = "KOR";
			}
			else if (locale.equals(Locale.FRENCH))
			{
				PublicParametersManager.version = "FRE";
			}
			else if (locale.equals(Locale.GERMAN))
			{
				PublicParametersManager.version = "GER";
			}
			else if (locale.equals(Locale.ITALIAN))
			{
				PublicParametersManager.version = "ITA";
			}
			else
			{
				PublicParametersManager.version = "ENG";
			}

			//String languageDirectory = AppToolkit.get_LanguageDirectory();
			//directory = new File(languageDirectory + PublicParametersManager.version);
			//if (!directory.exists())
			boolean isPackage = Language.isLanguagePackage("Language",PublicParametersManager.version);
			if (!isPackage)
			{
			//if (StringHelper.isNullOrEmpty(PublicParametersManager.version))
			//{
			    isPackage = Language.isLanguagePackage("Language","ENG");

				//directory = new File(languageDirectory + "ENG");
				//if (directory.exists())
				if (isPackage)
				{
					PublicParametersManager.version = "ENG";
				}
				else
				{
					//get first version
					/*
					directory = new File(languageDirectory);
					String[] directorys = directory.list();
					for (int i = 0, count = directorys.length; i < count; i++)
					{
						String filePath = directorys[i];
						File directory2 = new File(languageDirectory + filePath);
						if (directory2.isDirectory())
						{
							PublicParametersManager.version = filePath;
							break;
						}
					}
					*/

				   try
				   {
					   String[] availableLanguages = Language.getAvailableLanguages("Language");
					   if (availableLanguages.length > 0)
					   {
						   PublicParametersManager.version = availableLanguages[0];
					   }
				   }
				   catch (IOException iOException)
				   {
					   TradingConsole.traceSource.trace(TraceType.Error, iOException);
					   return false;
				   }
				}
			}
			//sava data
			String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>";
			xml += "<PublicParameters>";
			xml += "<PublicParameter>";
			xml += "<Version></Version>";
			xml += "<IsEmptyAllLogFilesWhenEnterSystem></IsEmptyAllLogFilesWhenEnterSystem>";
			xml += "<IsTrace></IsTrace>";
			xml += "<IsSpecialTrace></IsSpecialTrace>";
			xml += "<MinTimeTaken></MinTimeTaken>";
			xml += "<IsUseUnitGrid></IsUseUnitGrid>";
			xml += "<GridsPerUnitRow></GridsPerUnitRow>";
			xml += "<SelectedChartInstrument></SelectedChartInstrument>";
			xml += "</PublicParameter>";
			xml += "</PublicParameters>";
			XmlDocument xmlDocument = new XmlDocument();
			xmlDocument.loadXml(xml);

			XmlNode xmlNode = xmlDocument.get_DocumentElement().get_Item("PublicParameter");
			if (StringHelper.isNullOrEmpty(PublicParametersManager.version))
			{
				PublicParametersManager.version = "ENG";
			}
			xmlNode.get_Item("Version").set_InnerText(PublicParametersManager.version);

			xmlNode.get_Item("IsEmptyAllLogFilesWhenEnterSystem").set_InnerText(isEmptyAllLogFilesWhenEnterSystemString);
			xmlNode.get_Item("IsTrace").set_InnerText(isTraceString);
			xmlNode.get_Item("IsSpecialTrace").set_InnerText(isSpecialTraceString);
			xmlNode.get_Item("MinTimeTaken").set_InnerText(minTimeTakenString);
			xmlNode.get_Item("IsUseUnitGrid").set_InnerText(isUseUnitGridString);
			xmlNode.get_Item("GridsPerUnitRow").set_InnerText(gridsPerUnitRowString);
			xmlNode.get_Item("SelectedChartInstrument").set_InnerText(PublicParametersManager.selectedChartInstrument);

			try
			{
				xmlDocument.save(parameterFilePath);
			}
			catch (Exception exceptionception)
			{
				TradingConsole.traceSource.trace(TraceType.Error, exceptionception);
				return false;
			}
		}

		if (isNeedUpdate)
		{
			XmlNode xmlNode = AppToolkit.getCompanySettingXml("PublicParameter.xml", false);
			if(xmlNode == null) return false;

			XmlNode xmlNode2 = xmlNode.get_Item("PublicParameter");
			xmlNode2.get_Item("Version").set_InnerText(PublicParametersManager.version);
			XmlNode xmlNodeAppend = null;
			if (xmlNode2.get_Item("IsEmptyAllLogFilesWhenEnterSystem")==null)
			{
				xmlNodeAppend = xmlNode.get_OwnerDocument().createElement("IsEmptyAllLogFilesWhenEnterSystem");
				xmlNodeAppend.set_InnerText(isEmptyAllLogFilesWhenEnterSystemString);
				xmlNode2.appendChild(xmlNodeAppend);
			}
			else
			{
				xmlNode2.get_Item("IsEmptyAllLogFilesWhenEnterSystem").set_InnerText(isEmptyAllLogFilesWhenEnterSystemString);
			}

			if (xmlNode2.get_Item("IsTrace")==null)
			{
				xmlNodeAppend = xmlNode.get_OwnerDocument().createElement("IsTrace");
				xmlNodeAppend.set_InnerText(isTraceString);
				xmlNode2.appendChild(xmlNodeAppend);
			}
			else
			{
				xmlNode2.get_Item("IsTrace").set_InnerText(isTraceString);
			}

			if (xmlNode2.get_Item("IsSpecialTrace")==null)
			{
				xmlNodeAppend = xmlNode.get_OwnerDocument().createElement("IsSpecialTrace");
				xmlNodeAppend.set_InnerText(isSpecialTraceString);
				xmlNode2.appendChild(xmlNodeAppend);
			}
			else
			{
				xmlNode2.get_Item("IsSpecialTrace").set_InnerText(isSpecialTraceString);
			}

			if (xmlNode2.get_Item("MinTimeTaken")==null)
			{
				xmlNodeAppend = xmlNode.get_OwnerDocument().createElement("MinTimeTaken");
				xmlNodeAppend.set_InnerText(minTimeTakenString);
				xmlNode2.appendChild(xmlNodeAppend);
			}
			else
			{
				xmlNode2.get_Item("MinTimeTaken").set_InnerText(minTimeTakenString);
			}

			if (xmlNode2.get_Item("IsUseUnitGrid")==null)
			{
				xmlNodeAppend = xmlNode.get_OwnerDocument().createElement("IsUseUnitGrid");
				xmlNodeAppend.set_InnerText(isUseUnitGridString);
				xmlNode2.appendChild(xmlNodeAppend);
			}
			else
			{
				xmlNode2.get_Item("IsUseUnitGrid").set_InnerText(isUseUnitGridString);
			}

			if (xmlNode2.get_Item("GridsPerUnitRow")==null)
			{
				xmlNodeAppend = xmlNode.get_OwnerDocument().createElement("GridsPerUnitRow");
				xmlNodeAppend.set_InnerText(gridsPerUnitRowString);
				xmlNode2.appendChild(xmlNodeAppend);
			}
			else
			{
				xmlNode2.get_Item("GridsPerUnitRow").set_InnerText(gridsPerUnitRowString);
			}

			if (xmlNode2.get_Item("SelectedChartInstrument")==null)
			{
				xmlNodeAppend = xmlNode.get_OwnerDocument().createElement("SelectedChartInstrument");
				xmlNodeAppend.set_InnerText(PublicParametersManager.selectedChartInstrument);
				xmlNode2.appendChild(xmlNodeAppend);
			}
			else
			{
				xmlNode2.get_Item("SelectedChartInstrument").set_InnerText(PublicParametersManager.selectedChartInstrument);
			}

			try
			{
				xmlNode.get_OwnerDocument().save(parameterFilePath);
			}
			catch (Exception exception)
			{
				TradingConsole.traceSource.trace(TraceType.Error, exception);
				return false;
			}
		}
		return true;
	}
}
