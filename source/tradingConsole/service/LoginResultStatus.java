package tradingConsole.service;
import framework.lang.Enum;
public class LoginResultStatus extends Enum<LoginResultStatus>
{
	public static final LoginResultStatus None = new LoginResultStatus("None",0);
	public static final LoginResultStatus ExceedMaxRetryCount = new LoginResultStatus("ExceedMaxRetryCount",1);
	public static final LoginResultStatus LoginIdIsEmpty = new LoginResultStatus("LoginIdIsEmpty",2);
	public static final LoginResultStatus ParticipantServiceLoginFailed = new LoginResultStatus("ParticipantServiceLoginFailed",3);
	public static final LoginResultStatus UserIdIsEmpty = new LoginResultStatus("UserIdIsEmpty",4);
	public static final LoginResultStatus CheckPermissionFailed = new LoginResultStatus("CheckPermissionFailed",5);
	public static final LoginResultStatus NotAuthrized = new LoginResultStatus("NotAuthrized",6);
	public static final LoginResultStatus StateServerLoginFailed = new LoginResultStatus("StateServerLoginFailed",7);
	public static final LoginResultStatus StateServerNotLogined = new LoginResultStatus("StateServerNotLogined",8);
	public static final LoginResultStatus OrganizationDirNotExist = new LoginResultStatus("OrganizationDirNotExist",9);
	public static final LoginResultStatus Successpervasiveness = new LoginResultStatus("Successpervasiveness",10);

	private LoginResultStatus(String name, int value){
		super(name,value);
	}
}
