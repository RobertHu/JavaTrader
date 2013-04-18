package tradingConsole.service;

import framework.Guid;
import framework.xml.XmlNode;
import framework.data.DataSet;
import nu.xom.Element;
import Util.*;
import org.apache.log4j.Logger;
public class LoginResult
{
	private Guid _userId;
	private String _companyName;
	private boolean _disallowLogin;
	private boolean _needActiveAccount;
	private byte[] _companyLogo;
	private XmlNode _colorSettings;
	private XmlNode _parameter;
	private XmlNode _settings;
	private DataSet _recoverPasswordData;
	private DataSet _tradingAccountData;
	private Logger logger = Logger.getLogger(LoginResult.class);

	public Guid get_UserId()
	{
		return this._userId;
	}

	public String get_CompanyName()
	{
		return this._companyName;
	}

	public boolean get_DisallowLogin()
	{
		return this._disallowLogin;
	}

	public boolean get_NeedActiveAccount()
	{
		return this._needActiveAccount;
	}

	public byte[] get_CompanyLogo()
	{
		return this._companyLogo;
	}

	public XmlNode get_ColorSettings()
	{
		return this._colorSettings;
	}

	public XmlNode get_Parameter()
	{
		return this._parameter;
	}

	public XmlNode get_Settings()
	{
		return this._settings;
	}

	public DataSet get_RecoverPasswordData()
	{
		return this._recoverPasswordData;
	}

	public DataSet get_TradingAccountData()
	{
		return this._tradingAccountData;
	}

	public LoginResult(Element result)
	{
		this._tradingAccountData = XmlElementHelper.convertToDataset(result.getFirstChildElement("tradingAccountData"));
		this._recoverPasswordData = XmlElementHelper.convertToDataset(result.getFirstChildElement("recoverPasswordData"));
		this._companyName = result.getFirstChildElement("companyName").getValue();
		this._disallowLogin = Boolean.parseBoolean(result.getFirstChildElement("disallowLogin").getValue());
		this._needActiveAccount = Boolean.parseBoolean(result.getFirstChildElement("isActivateAccount").getValue());
		this._companyLogo = Base64Helper.decode(result.getFirstChildElement("companyLogo").getValue());
		this._colorSettings = XmlElementHelper.ConvertToXmlNode(result.getFirstChildElement("colorSettings"));
		this._parameter = XmlElementHelper.ConvertToXmlNode(result.getFirstChildElement("parameter"));
		this._settings = XmlElementHelper.ConvertToXmlNode(result.getFirstChildElement("settings"));
		this._userId = new Guid(result.getFirstChildElement("userId").getValue());
	}


	public LoginResult(Object[] results)
	{
		this._companyName = (String) (results[1]);
		this._disallowLogin = ((Boolean) (results[2])).booleanValue();
		this._needActiveAccount = ((Boolean) (results[3])).booleanValue();
		this._companyLogo = (byte[]) (results[4]);
		this._colorSettings = (XmlNode) (results[5]);
		this._parameter = (XmlNode) (results[6]);
		this._settings = (XmlNode) (results[7]);
		this._recoverPasswordData = (DataSet) (results[8]);
		this._tradingAccountData = (DataSet) (results[9]);
		this._userId = (Guid) results[0];
	}
}
