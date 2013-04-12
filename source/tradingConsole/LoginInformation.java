package tradingConsole;

import framework.DateTime;

import tradingConsole.enumDefine.LoginStatus;

import framework.Guid;
import framework.xml.XmlConvert;
import tradingConsole.ui.language.Language;
import framework.StringHelper;

public class LoginInformation
{
	private Guid _customerId;
	private String _companyName;
	private String _loginName;
	private DateTime _loginTime;
	private DateTime _logoutTime;
	private String _customerName;
	private LoginStatus _loginStatus;

	private Customer _customer;

	public boolean getIsConnected()
	{
		return this._loginStatus.equals(LoginStatus.Connected)
			|| this._loginStatus.equals(LoginStatus.LoginSucceed)
			|| this._loginStatus.equals(LoginStatus.Ready);
	}

	private LoginInformation()
	{
		this._loginStatus = LoginStatus.Unlogin;
	}

	public static LoginInformation create()
	{
		return new LoginInformation();
	}

	public void loggedIn(Guid customerId, String companyName, String loginName, DateTime loginTime)
	{
		this._customerId = customerId;
		this._companyName = companyName;
		this._loginName = loginName;
		this._loginTime = loginTime;

		this._customerName = "";

		this._loginStatus = LoginStatus.LoginSucceed;
	}

	public LoginStatus get_LoginStatus()
	{
		return this._loginStatus;
	}

	public void set_LoginStatus(LoginStatus value)
	{
		this._loginStatus = value;
	}

	public Guid get_CustomerId()
	{
		return this._customerId;
	}

	public String get_CompanyName()
	{
		return this._companyName;
	}

	public void set_Customer(Customer value)
	{
		this._customer = value;
	}

	public String get_LoginName()
	{
		return this._loginName;
	}

	public DateTime get_LoginTime()
	{
		return this._loginTime;
	}

	public DateTime get_LogoutTime()
	{
		return this._logoutTime;
	}

	public void set_LogoutTime(DateTime value)
	{
		this._logoutTime = value;
	}

	public String getLoginInformation(String hostName)
	{
		String customerName = (this._customer==null)?"":this._customer.get_CustomerName();
		if (this._loginStatus.equals(LoginStatus.LoginSucceed) || this._loginStatus.equals(LoginStatus.Ready))
		{
			if (StringHelper.isNullOrEmpty(customerName)) return "";

			return customerName + " " + Language.LoggedInAt /*+ " " + hostName*/ + "  " + XmlConvert.toString(this._loginTime, "yyyy-MM-dd HH:mm:ss");
		}
		else if (this._loginStatus.equals(LoginStatus.Logouted))
		{
			if (StringHelper.isNullOrEmpty(this._loginName) || this._logoutTime == null)
			{
				return "";
			}
			else
			{
				if (StringHelper.isNullOrEmpty(customerName)) return "";
				return customerName + " " + Language.LoggedOutAt + " " + XmlConvert.toString(this._logoutTime, "yyyy-MM-dd HH:mm:ss");
			}
		}
		else
		{
			return "";
		}
	}
}
