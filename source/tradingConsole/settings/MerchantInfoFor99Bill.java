package tradingConsole.settings;

import framework.Guid;

public class MerchantInfoFor99Bill
{
	private String _id;
	private String _key;
	private Guid _organizationId;

	public MerchantInfoFor99Bill(String id, String key, Guid organizationId)
	{
		this._id = id;
		this._key = key;
		this._organizationId = organizationId;
	}

	public String get_Id()
	{
		return this._id;
	}

	public String get_Key()
	{
		return this._key;
	}

	public Guid get_OrganizationId()
	{
		return this._organizationId;
	}
}
