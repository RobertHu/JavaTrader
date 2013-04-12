package tradingConsole.service;

import framework.IAsyncCallback;
import framework.IAsyncResult;
import framework.net.CookieContainer;
import framework.web.services.protocols.SoapHttpClientProtocol;
import framework.xml.XmlDocument;
import framework.xml.XmlNode;
import framework.xml.serialization.XmlTypeMapping;
import framework.xml.serialization.XmlType;
import framework.data.DataSet;
import framework.Guid;

public class AuthenticationWebService extends SoapHttpClientProtocol
{
	private static String nameSpace = "http://www.omnicare.com/TradingConsole/";

	public AuthenticationWebService(String url, CookieContainer cookieContainer)
	{
		super(url);
		this.AuthenticationWebService2(cookieContainer);
	}

	public AuthenticationWebService(String url, CookieContainer cookieContainer, XmlDocument xmlDocument)
	{
		super(url, xmlDocument);
		this.AuthenticationWebService2(cookieContainer);
	}

	private void AuthenticationWebService2(CookieContainer cookieContainer)
	{
		this.set_CookieContainer(cookieContainer);

		XmlTypeMapping typeMapping = new XmlTypeMapping();
		typeMapping.addMap(DataSet.class, XmlType.getXmlType("DataSet", AuthenticationWebService.nameSpace));
		super.service.set_XmlTypeMapping(typeMapping);
	}

	public Object[] activeAccountLogin(Guid customerId,String loginID,String password)
	{
		Object[] results = this.invoke("ActiveAccountLogin", ServiceTimeoutSetting.login, new Object[]
									   {
									   customerId,
									   loginID,
									   password});
		return results;
	}

	public IAsyncResult beginActiveAccountLogin(Guid customerId,String loginID, String password, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("ActiveAccountLogin", new Object[]
								{
								customerId,
								loginID,
								password}, callback, asyncState);
	}

	/// <remarks/>
	public Object[] endActiveAccountLogin(IAsyncResult asyncResult)
	{
		Object[] results = this.endInvoke(asyncResult);
		return results;
	}

	public Object[] loginForJava(String loginID, String password, String version,
								 /*ref*/String companyName, /*out*/ boolean disallowLogin, /*out*/ boolean isActivateAccount,
								 /*out*/byte[] companyLogo, /*out*/ XmlNode colorSettings, /*out*/ XmlNode parameter, /*out*/ XmlNode settings, /*out*/
								 DataSet recoverPasswordData, /*out*/ DataSet tradingAccountData)
	{
		Object[] results = this.invoke("LoginForJava3", ServiceTimeoutSetting.login, new Object[]
									   {
									   loginID,
									   password,
									   version,
									   companyName});
		return results;
	}

	public IAsyncResult beginLoginForJava(String loginID, String password, String version,
										  String companyName, IAsyncCallback callback, Object asyncState)
	{
		return this.beginInvoke("LoginForJava3", new Object[]
								{
								loginID,
								password,
								version,
								companyName}, callback, asyncState);
	}

/// <remarks/>
	public Object[] endLoginForJava(IAsyncResult asyncResult, /*ref*/ String companyName, /*out*/ boolean disallowLogin, /*out*/ boolean isActivateAccount,
									/*out*/byte[] companyLogo, /*out*/ XmlNode colorSettings, /*out*/ XmlNode parameter, /*out*/ XmlNode settings, /*out*/
									DataSet recoverPasswordData, /*out*/ DataSet tradingAccountData)
	{
		Object[] results = this.endInvoke(asyncResult);
		return results;
	}

}
