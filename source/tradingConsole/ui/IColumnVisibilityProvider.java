package tradingConsole.ui;

import tradingConsole.ui.grid.BindingSource;

public interface IColumnVisibilityProvider
{
	public interface IColumnVisibilityChangedHandler
	{
		void handleColumnVisibilityChanged(BindingSource bindingSource, String columnName, boolean isVisible);
	}

	boolean isVisible(BindingSource bindingSource, String columnName);
	void addColumnVisibilityChangedHandler(IColumnVisibilityChangedHandler handler);
}
