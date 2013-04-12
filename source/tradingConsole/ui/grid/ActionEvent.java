package tradingConsole.ui.grid;

public class ActionEvent
{
	private Object _source;
	private Action _action;
	private Object _object;
	private String _columnName;
	private int _row;
	private int _column;

	public ActionEvent(Object source, Action action, Object object, String columnName, int row, int column)
	{
		this._source = source;
		this._action = action;
		this._object = object;
		this._columnName = columnName;
		this._row = row;
		this._column = column;
	}

	public Object get_Source()
	{
		return this._source;
	}

	public Action get_GridAction()
	{
		return this._action;
	}

	public Object get_Object()
	{
		return this._object;
	}

	public String get_ColumnName()
	{
		return this._columnName;
	}

	public int get_Row()
	{
		return this._row;
	}

	public int get_Column()
	{
		return this._column;
	}
}
