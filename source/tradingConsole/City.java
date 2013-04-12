package tradingConsole;

public class City
{
	private long _id;
	private String _name;

	public City(long id, String name)
	{
		this._id = id;
		this._name = name;
	}

	public long get_Id()
	{
		return this._id;
	}

	public String get_Name()
	{
		return this._name;
	}
}
