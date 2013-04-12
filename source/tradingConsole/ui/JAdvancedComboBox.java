package tradingConsole.ui;

import javax.swing.JComboBox;
import java.util.HashMap;
import java.awt.AWTEvent;
import java.awt.Event;
import java.awt.event.ItemListener;


public class JAdvancedComboBox extends JComboBox
{
	private ItemListener[] _itemListeners;

	public void addItem(String text, Object value)
	{
		super.addItem(new Item(text, value));
	}

	public String getSelectedText()
	{
		Object item = this.getSelectedItem();
		return item == null ? null : ((Item)item).get_Text();
	}

	public Object getSelectedValue()
	{
		Object item = this.getSelectedItem();
		return item == null ? null : ((Item)item).get_Value();
	}

	public Object getValueAt(int index)
	{
		Object item = this.getItemAt(index);
		return item == null ? null : ((Item)item).get_Value();
	}

	public void disableItemEvent()
	{
		/*  Doesn't work ????
		super.disableEvents(Event.LIST_DESELECT);
		super.disableEvents(Event.LIST_SELECT);*/

		this._itemListeners = super.getItemListeners();
		for(ItemListener itemListerner : this._itemListeners)
		{
			super.removeItemListener(itemListerner);
		}
	}

	public void enableItemEvent()
	{
		/*  Doesn't work ????
		super.enableEvents(Event.LIST_DESELECT);
		super.enableEvents(Event.LIST_SELECT);*/

		for(ItemListener itemListerner : this._itemListeners)
		{
			super.addItemListener(itemListerner);
		}
	}

	static class Item
	{
		private String _text;
		private Object _value;

		public Item(String text, Object value)
		{
			this._text = text;
			this._value = value;
		}

		public String get_Text()
		{
			return this._text;
		}

		public Object get_Value()
		{
			return this._value;
		}

		@Override
		public String toString()
		{
			return this._text;
		}
	}
}
