package tradingConsole.ui;

import java.util.*;

import java.awt.*;

import framework.*;
import tradingConsole.*;
import tradingConsole.framework.*;
import tradingConsole.ui.grid.ColumnUIInfoManager;
import framework.diagnostics.TraceType;
import javax.swing.Icon;

public class BindingManager
{
	private HashMap<String, ArrayList<IView>> _dataView;

	public BindingManager()
	{
		this._dataView = new HashMap<String, ArrayList<IView>> ();
	}

	public void updateProperties(String dataSourceKey, PropertyDescriptor[] properties)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);
			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.updateProperties(properties);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void updateProperty(String dataSourceKey, PropertyDescriptor property)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);
			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.updateProperty(property);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void bind(String dataSourceKey, Collection dataSource, IView view, PropertyDescriptor[] properties)
	{
		if (!this._dataView.containsKey(dataSourceKey))
		{
			this._dataView.put(dataSourceKey, new ArrayList<IView> ());
		}
		this._dataView.get(dataSourceKey).add(view);

		view.bind(dataSource, properties);
		view.setDataSourceKey(dataSourceKey);
	}

	public void unbind(String dataSourceKey, IView view)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			this._dataView.get(dataSourceKey).remove(view);
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void update(String dataSourceKey, Object item, String propertyName, Object value)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);
			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.update(item, propertyName, value);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void update(String dataSourceKey, Object item)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.update(item);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void setBackground(String dataSourceKey, Object item, Color background)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.setBackground(item, background);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void resetBackground(String dataSourceKey, Object item)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.resetBackground(item);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void setForeground(String dataSourceKey, Object item, Color color)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.setForeground(item, color);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void resetForeground(String dataSourceKey, Object item)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.resetForeground(item);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}


	public void setBackground(String dataSourceKey, Object item, String propertyName, Color background)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.setBackground(item, propertyName, background);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void setForeground(String dataSourceKey, Object item, String propertyName, Color foreground)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.setForeground(item, propertyName, foreground);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}

	}

	public void setIcon(String dataSourceKey, Object item, String propertyName, Icon image)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.setIcon(item, propertyName, image);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void setImage(String dataSourceKey, Object item, String propertyName, Image image)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.setImage(item, propertyName, image);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void setHeight(String dataSourceKey, Object item, int height)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.setHeight(item, height);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void setType(String dataSourceKey, String propertyName, int typeValue)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.setType(propertyName, typeValue);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void insert(String dataSourceKey, int index, Object item)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.insert(index, item);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void add(String dataSourceKey, Object item)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.add(item);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void remove(String dataSourceKey, Object item)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = views.size() - 1; i >= 0; i--)
			{
				IView view = views.get(i);
				view.remove(item);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void removeAll(String dataSourceKey)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = views.size() - 1; i >= 0; i--)
			{
				IView view = views.get(i);
				view.removeAll();
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void setValueAt(String dataSourceKey, Object value, int row, int column)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.setValueAt(value, row, column);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void setHeader(String dataSourceKey, int alignment, int height, Color foreground, Color background, Font font)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.setHeader(alignment, height, foreground, background, font);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}

	public void setGrid(String dataSourceKey, int rowsHeight, Color foreground, Color background, Color borderColor, boolean rowResizing, boolean columnResizing, Font font,
						boolean cellRangeSelection, boolean largeCurrentCell, boolean sortOnColumnClick)
	{
		if (this._dataView.containsKey(dataSourceKey))
		{
			ArrayList<IView> views = this._dataView.get(dataSourceKey);

			for (int i = 0; i < views.size(); i++)
			{
				IView view = views.get(i);
				view.setGrid(rowsHeight, foreground, background, borderColor, rowResizing, columnResizing, font,
							 cellRangeSelection, largeCurrentCell, sortOnColumnClick);
			}
		}
		else
		{
			TradingConsole.traceSource.trace(TraceType.Warning, "Illegal data source key: " + dataSourceKey
				+ Environment.newLine + new FrameworkException("").getStackTrace());
		}
	}
}
