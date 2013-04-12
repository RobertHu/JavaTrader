package tradingConsole;

import framework.Guid;
import tradingConsole.ui.language.Language;

public class Bank
{
	public static Bank Others = new Bank(Guid.empty, Language.Others);

	private Guid _id;
	private String _name;
	public Bank(Guid id, String name)
	{
		this._id = id;
		this._name = name;
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public String get_Name()
	{
		return this._name;
	}
}
