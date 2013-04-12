package tradingConsole.ui;

import java.util.*;

import java.awt.*;

import tradingConsole.framework.*;
import javax.swing.Icon;

public interface IView
{
	void updateProperties(PropertyDescriptor[] properties);

	void updateProperty(PropertyDescriptor property);

	void bind(Collection dataSource, PropertyDescriptor[] properties);

	void update(Object item, String propertyName, Object propertyValue);

	void update(Object item);

	void add(Object item);
	void insert(int index, Object item);

	void remove(Object item);

	void removeAll();

	void setValueAt(Object value, int row, int column);

	void setHeader(int alignment, int height, Color foreground, Color background, Font font);

	void setGrid(int rowsHeight, Color foreground, Color background, Color borderColor, boolean rowResizing,
				 boolean columnResizing, Font font, boolean cellRangeSelection, boolean largeCurrentCell, boolean sortOnColumnClick);

	void setHeight(Object item,int height);

	void setType(String propertyName,int typeValue);

	void setBackground(Object item, Color background);
	void setForeground(Object item, Color background);

	void setBackground(Object item, String propertyName, Color background);

	void setForeground(Object item, String propertyName, Color foreground);

	void setImage(Object item, String propertyName, Image image);
	void setIcon(Object item, String propertyName, Icon image);

	void resetBackground(Object item);
	void resetForeground(Object item);

	void setDataSourceKey(String dataSourceKey);
}
