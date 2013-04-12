package tradingConsole.ui.grid;

import tradingConsole.framework.PropertyDescriptor;

public interface IPropertyChangedListener
{
	void propertyChanged(Object owner, PropertyDescriptor propertyDescriptor, Object oldValue, Object newValue, int row, int column);
}
