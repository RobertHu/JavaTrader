package tradingConsole.settings;

import framework.xml.XmlDocument;
import framework.xml.XmlNodeList;
import framework.xml.XmlElement;
import tradingConsole.AppToolkit;
import framework.io.DirectoryHelper;
import java.util.ArrayList;

public class ServiceManager2
{
	private UserSettings _userSettings;
	private ArrayList<ServiceSettings> _serviceSettingses = new ArrayList<ServiceSettings>();

	public ServiceManager2()
	{
		String userSettingsFileName = DirectoryHelper.combine(AppToolkit.get_CompanySettingDirectory(), "UserSettings.config");;
		this._userSettings = UserSettingsHelper.fromXmlFile(userSettingsFileName);

	}

	public void set_SelectedServiceIndex(int value)
	{
		this._userSettings.set_SelectedServiceIndex(value);
	}

	public void set_UserDefineHost(String value)
	{
		this._userSettings.set_UserDefineHost(value);
	}

	public ArrayList<String> getServerList()
	{
		return null;
	}

	public ServiceSettings get_ServiceSettings()
	{
		int selectedServiceIndex = this._userSettings.get_SelectedServiceIndex();
		if(selectedServiceIndex == -1) selectedServiceIndex = 1;
		return this._serviceSettingses.get(selectedServiceIndex);
	}
}

class UserSettings
{
	private String _userDefineHost;
	private int _selectedServiceIndex;

	UserSettings(String userDefineHost, int selectedServiceIndex)
	{
		this._userDefineHost = userDefineHost;
		this._selectedServiceIndex = selectedServiceIndex;
	}

	public int get_SelectedServiceIndex()
	{
		return this._selectedServiceIndex;
	}

	public void set_SelectedServiceIndex(int value)
	{
		this._selectedServiceIndex = value;
	}

	public String get_UserDefineHost()
	{
		return this._userDefineHost;
	}

	public void set_UserDefineHost(String value)
	{
		this._userDefineHost = value;
	}
}

class UserSettingsHelper
{
	static UserSettings fromXmlFile(String fileName)
	{
		String userDefineHost = null;
		int selectedServiceIndex = -1;

		try
		{
			XmlDocument xmlDocument = new XmlDocument();
			xmlDocument.load(fileName);
			XmlNodeList children = xmlDocument.get_ChildNodes();
			for (int index = 0; index < children.get_Count(); index++)
			{
				XmlElement xmlElement = (XmlElement)children.item(index);
				String name = xmlElement.get_Name();
				String value = xmlElement.get_InnerText();

				if (name.equals("UserDefineHost"))
				{
					userDefineHost = value;
				}
				else if (name.equals("SelectedServiceIndex"))
				{
					selectedServiceIndex = Integer.parseInt(value);
				}
			}
		}
		catch(Exception ex){}

		return new UserSettings(userDefineHost, selectedServiceIndex);
	}

	static boolean toXmlFile(UserSettings userSettings, String fileName)
	{
		try
		{
			XmlDocument xmlDocument = new XmlDocument();
			XmlElement xmlElement = xmlDocument.createElement("UserDefineHost");
			xmlElement.set_InnerText(userSettings.get_UserDefineHost());

			xmlElement = xmlDocument.createElement("SelectedServiceIndex");
			xmlElement.set_InnerText(Integer.toString(userSettings.get_SelectedServiceIndex()));

			xmlDocument.save(fileName);
			return true;
		}
		catch(Exception ex)
		{
			return false;
		}
	}
}

class ServiceSettings
{
	private String _authenticationUrl;
	private String _serviceUrl;
	private String _forgetPasswordUrl;

	private String _placardUrl;
	private String _onlineHelpUrl;
	private String _downloadDocumentUrl;

	private ArrayList<ServiceSettings> _backups =  new ArrayList<ServiceSettings>();

}
