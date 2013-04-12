package Packet;

public class LoginInfoManager
{
	private String loginId;
	private String password;
	private String session;
	private String version;
	private LoginInfoManager()
	{}


	public String getLoginId()
	{
		return loginId;
	}

	public void setLoginId(String loginId)
	{
		this.loginId = loginId;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getSession()
	{
		return session;
	}

	public void setSession(String session)
	{
		this.session = session;
	}

	public String getVersion(){
		return this.version;
	}

	public void setVersion(String value){
		this.version = value;
	}

	public static final LoginInfoManager Default = new LoginInfoManager();

}
