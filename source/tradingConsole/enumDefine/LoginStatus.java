package tradingConsole.enumDefine;

import framework.lang.Enum;

import tradingConsole.ui.language.Login;

public class LoginStatus extends Enum<LoginStatus>
{
	public static LoginStatus Unlogin = new LoginStatus("Unlogin", 0);
	public static LoginStatus LoginFailed = new LoginStatus("LoginFailed", 1);
	public static LoginStatus Loging = new LoginStatus("Loging", 2);
	public static LoginStatus LoginSucceed = new LoginStatus("LoginSucceed", 3);
	public static LoginStatus Logouting = new LoginStatus("Logouting", 4);
	public static LoginStatus Logouted = new LoginStatus("Logouted", 5);
	public static LoginStatus Ready = new LoginStatus("Ready", 6);
	public static LoginStatus Connected = new LoginStatus("Connected", 7);
	public static LoginStatus Disconnected = new LoginStatus("Disconnected", 8);
	public static LoginStatus Kickouted = new LoginStatus("Kickouted", 9);
	public static LoginStatus Connecting = new LoginStatus("Connecting",10);

	private LoginStatus(String name, int value)
	{
		super(name, value);
	}

	public static String getCaption(LoginStatus loginStatus)
{
	if (loginStatus.equals(LoginStatus.Unlogin))
	{
		return Login.Unlogin;
	}
	else if (loginStatus.equals(LoginStatus.LoginFailed))
	{
		return Login.LoginFailed;
	}
	else if (loginStatus.equals(LoginStatus.Loging))
	{
		return Login.Loging;
	}
	else if (loginStatus.equals(LoginStatus.LoginSucceed))
	{
		return Login.LoginSucceed;
	}
	else if (loginStatus.equals(LoginStatus.Logouting))
	{
		return Login.Logouting;
	}
	else if (loginStatus.equals(LoginStatus.Logouted))
	{
		return Login.Logouted;
	}
	else if (loginStatus.equals(LoginStatus.Ready))
	{
		return Login.Ready;
	}
	else if (loginStatus.equals(LoginStatus.Connected))
	{
		return Login.Connected;
	}
	else if (loginStatus.equals(LoginStatus.Disconnected))
	{
		return Login.Disconnected;
	}
	else if (loginStatus.equals(LoginStatus.Kickouted))
	{
		return Login.Kickouted;
	}
	else if (loginStatus.equals(LoginStatus.Connecting)){
		return Login.Connecting;
	}
	return "";
}

}
