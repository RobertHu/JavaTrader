package tradingConsole;

import java.util.Collection;
import java.util.ArrayList;
import framework.Guid;
import tradingConsole.ui.language.Language;

public class Country
{
	public static Country Others = new Country(-1, Language.Others, true);

	private long _id;
	private boolean _isReady;
	private String _name;
	private Collection<Province> _provinces;
	private Collection<Bank> _banks;

	public Country(long id, String name)
	{
		this(id, name, false);
	}

	public Country(long id, String name, boolean isReady)
	{
		this._id = id;
		this._isReady = isReady;
		this._name = name;
		this._provinces = new ArrayList<Province> ();
		this._banks = new ArrayList<Bank> ();
		this._banks.add(Bank.Others);
	}

	public long get_Id()
	{
		return this._id;
	}

	public boolean get_IsReady()
	{
		return this._isReady;
	}

	public void set_IsReady(boolean value)
	{
		this._isReady = value;
	}

	public String get_Name()
	{
		return this._name;
	}

	public Collection<Province> get_Provinces()
	{
		return this._provinces;
	}

	public Collection<Bank> get_Banks()
	{
		return this._banks;
	}
}
