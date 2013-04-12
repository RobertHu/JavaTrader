package tradingConsole.ui.grid;

import tradingConsole.framework.PropertyDescriptor;

public class PropertyChangingEvent
{
	private Object _owner;
	private PropertyDescriptor _propertyDescriptor;
	private Object _oldValue;
	private Object _newValue;
	private int _row;
	private int _column;
	private boolean _cancel = false;

	public PropertyChangingEvent(Object owner, PropertyDescriptor propertyDescriptor, Object oldValue, Object newValue, int row, int column)
	{
		this._owner = owner;
		this._propertyDescriptor = propertyDescriptor;
		this._oldValue = oldValue;
		this._newValue = newValue;
		this._row = row;
		this._column = column;
	}

	public Object get_Owner()
	{
		return this._owner;
	}

	public PropertyDescriptor get_PropertyDescriptor()
	{
		return this._propertyDescriptor;
	}

	public Object get_OldValue()
	{
		return this._oldValue;
	}

	public Object get_NewValue()
	{
		return this._newValue;
	}

	public void set_NewValue(Object value)
	{
		this._newValue = value;
	}

	public boolean get_Cancel()
	{
		return this._cancel;
	}

	public void set_Cancel(boolean cancel)
	{
		this._cancel |= cancel;
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
