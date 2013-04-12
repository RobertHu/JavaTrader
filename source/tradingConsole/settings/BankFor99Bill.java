package tradingConsole.settings;

public class BankFor99Bill
{
	private String _code;
	private String _name;

	public BankFor99Bill(String code, String name)
	{
		this._code = code;
		this._name = name;
	}

	public String get_Code()
	{
		return this._code;
	}

	public String get_Name()
	{
		return this._name;
	}

	@Override
	public String toString()
	{
		return this._name;
	}
}
