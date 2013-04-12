package tradingConsole;

import java.util.Collection;
import java.util.ArrayList;

public class Province
{
	private long _id;
	private String _name;
	private Collection<City> _cities;

	public Province(long id, String name)
	{
		this._id = id;
		this._name = name;
		this._cities = new ArrayList<City> ();
	}

	public long get_Id()
	{
		return this._id;
	}

	public String get_Name()
	{
		return this._name;
	}

	public Collection<City> get_Cities()
	{
		return this._cities;
	}
}
