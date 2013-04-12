package tradingConsole.ui.grid;

import framework.*;

public class ColumnUIInfo
{
	private static final String _seprator = ";";

	private String _name;
	private boolean _isChoosed = true;
	private int _sequence = -1;
	private int _width = -1;

	public ColumnUIInfo(String name)
	{
		this._name = name;
	}

	public String get_Name()
	{
		return this._name;
	}

	public void set_IsChoosed(boolean isChoosed)
	{
		this._isChoosed = isChoosed;
	}

	public boolean get_IsChoosed()
	{
		return this._isChoosed;
	}

	public void set_Sequence(int sequence)
	{
		this._sequence = sequence;
	}

	public int get_Sequence()
	{
		return this._sequence;
	}

	public void set_Width(int width)
	{
		this._width = width;
	}

	public int get_Width()
	{
		return this._width;
	}

	public static ColumnUIInfo parse(String value)
	{
		String values[] = StringHelper.split(value, ColumnUIInfo._seprator);
		String name = values[0];
		ColumnUIInfo columnUIInfo = new ColumnUIInfo(name);
		columnUIInfo.set_IsChoosed(Boolean.parseBoolean(values[1]));
		columnUIInfo.set_Sequence(Integer.parseInt(values[2]));
		columnUIInfo.set_Width(Integer.parseInt(values[3]));
		return columnUIInfo;
	}

	@Override
	public String toString()
	{
		return this._name + ColumnUIInfo._seprator + this._isChoosed + ColumnUIInfo._seprator
			+ this._sequence + ColumnUIInfo._seprator + this._width ;
	}
}
