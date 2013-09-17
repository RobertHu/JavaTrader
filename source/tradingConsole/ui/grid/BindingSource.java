package tradingConsole.ui.grid;

import java.util.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.jidesoft.grid.*;
import framework.diagnostics.*;
import tradingConsole.*;
import tradingConsole.framework.*;
import tradingConsole.ui.*;

public class BindingSource extends DefaultStyleTableModel implements IView, HierarchicalTableModel
{
	private static final int _columnCellStylePriority = 1;
	private static final int _specificalCellStylePriority = 2;
	private static CellStyle _blackBackgroundDefaultCellStyle;
	private CellStyle _defaultCellStyle;

	private String _name;
	private Vector _indexer;
	private PropertyDescriptor[] _propertyDescriptors;
	private HashMap<String, CellStyle> _columnToCellSytles = new HashMap<String, CellStyle>();
	private HashMap<Long, Boolean> _columnToEditable = new HashMap<Long, Boolean>();
	private ArrayList<IView> _views = new ArrayList<IView>();
	private ArrayList<IPropertyChangingListener> _propertyChangingListeners = new ArrayList<IPropertyChangingListener>();
	private ArrayList<IPropertyChangedListener> _propertyChangedListeners = new ArrayList<IPropertyChangedListener>();
	private ArrayList<IFontChangedListener> _fontChangedListeners = new ArrayList<IFontChangedListener>();
	private ArrayList<IObjectAddingListener> _objectAddingListeners = new ArrayList<IObjectAddingListener>();
	private ArrayList<IObjectRemovedListener> _objectRemovedListeners = new ArrayList<IObjectRemovedListener>();
	private ArrayList<IObjectUpdatedListener> _objectUpdatedListeners = new ArrayList<IObjectUpdatedListener>();
	private Font _font;
	private HierarchicalTableModel _hierarchicalTableModel;
	private String _dataSourceKey;

	private boolean _useBlackAsBackground = false;
	private Color _backgroundColor = null;

	static
	{
		BindingSource._blackBackgroundDefaultCellStyle = new CellStyle();
		BindingSource._blackBackgroundDefaultCellStyle.setForeground(ColorSettings.GridForeground);
		BindingSource._blackBackgroundDefaultCellStyle.setSelectionForeground(ColorSettings.GridForeground);
	}

	public BindingSource()
	{
		this(null);
	}

	public BindingSource(HierarchicalTableModel hierarchicalTableModel)
	{
		this._hierarchicalTableModel = hierarchicalTableModel;

		this._defaultCellStyle = new CellStyle();
		this._defaultCellStyle.setForeground(Color.BLACK);
		this._defaultCellStyle.setSelectionForeground(Color.BLACK);
	}

	public Font get_Font()
	{
		return this._font;
	}

	public void set_Name(String name)
	{
		this._name = name;

		if(this._font == null && ColumnUIInfoManager.get_IsLoaded())
		{
			Font font = ColumnUIInfoManager.getFont(this._name);
			if(font != null) this.setFont(font);
		}
	}

	public boolean contains(Object value)
	{
		return this._indexer.contains(value);
	}

	public void addView(IView view)
	{
		if(!this._views.contains(view))
		{
			this._views.add(view);
		}
	}

	public boolean removeView(IView view)
	{
		return this._views.remove(view);
	}

	public void setFont(Font font)
	{
		Font oldFont = this._font;
		this._font = font;
		for(int row = 0; row < this.getRowCount(); row++)
		{
			for(int column = 0; column < this.getColumnCount(); column++)
			{
				this.setCellStyle(row, column, CellStyleItem.Font, font);
			}
		}
		this.fireTableDataChanged();
		this.fireFontChanged(this._font, oldFont);
	}

	private void fireFontChanged(Font newFont, Font oldFont)
	{
		for(IFontChangedListener fontChangedListener : this._fontChangedListeners)
		{
			fontChangedListener.fontChanged(newFont, oldFont);
		}
	}

	public void addPropertyChangingListener(IPropertyChangingListener propertyChangingListener)
	{
		if(!this._propertyChangingListeners.contains(propertyChangingListener))
		{
			this._propertyChangingListeners.add(propertyChangingListener);
		}
	}

	public boolean removePropertyChangingListener(IPropertyChangingListener propertyChangingListener)
	{
		return this._propertyChangingListeners.remove(propertyChangingListener);
	}

	private void fireObjectUpdated(Object object)
	{
		for(IObjectUpdatedListener objectUpdatedListener : this._objectUpdatedListeners)
		{
			objectUpdatedListener.ObjectUpdated(object);
		}
	}

	public void addObjectUpdatedListener(IObjectUpdatedListener objectUpdatedListener)
	{
		if(!this._objectUpdatedListeners.contains(objectUpdatedListener))
		{
			this._objectUpdatedListeners.add(objectUpdatedListener);
		}
	}

	public boolean removeObjectUpdatedListener(IObjectUpdatedListener objectUpdatedListener)
	{
		return this._objectUpdatedListeners.remove(objectUpdatedListener);
	}

	public void addPropertyChangedListener(IPropertyChangedListener propertyChangedListener)
	{
		if(!this._propertyChangingListeners.contains(propertyChangedListener))
		{
			this._propertyChangedListeners.add(propertyChangedListener);
		}
	}

	public boolean removePropertyChangedListener(IPropertyChangedListener propertyChangedListener)
	{
		return this._propertyChangedListeners.remove(propertyChangedListener);
	}


	public void addAddingObjectListener(IObjectAddingListener objectAddingListener)
	{
		if(!this._objectAddingListeners.contains(objectAddingListener))
		{
			this._objectAddingListeners.add(objectAddingListener);
		}
	}

	public boolean removeAddingObjectListener(IObjectAddingListener objectAddingListener)
	{
		return this._objectAddingListeners.remove(objectAddingListener);
	}

	public void addObjectRemovedListener(IObjectRemovedListener objectRemovedListener)
	{
		if(!this._objectRemovedListeners.contains(objectRemovedListener))
		{
			this._objectRemovedListeners.add(objectRemovedListener);
		}
	}

	public boolean removeObjectRemovedListener(IObjectRemovedListener objectRemovedListener)
	{
		return this._objectRemovedListeners.remove(objectRemovedListener);
	}

	public void addFontChangedListener(IFontChangedListener fontChangedListener)
	{
		if(!this._fontChangedListeners.contains(fontChangedListener))
		{
			this._fontChangedListeners.add(fontChangedListener);
		}
	}

	public boolean removeFontChangedListener(IFontChangedListener fontChangedListener)
	{
		return this._fontChangedListeners.remove(fontChangedListener);
	}

	public void clearPropertyChangingListener()
	{
		this._propertyChangingListeners.clear();
	}

	public void clearPropertyChangedListener()
	{
		this._propertyChangedListeners.clear();
	}

	public PropertyDescriptor[] getPropertyDescriptors()
	{
		return this._propertyDescriptors;
	}

	//Begin of HierarchicalTableModel
	public boolean hasChild(int rowIndex)
	{
		return this._hierarchicalTableModel == null ?  false : this._hierarchicalTableModel.hasChild(rowIndex);
	}

	public boolean isHierarchical(int rowIndex)
	{
		return this._hierarchicalTableModel == null ?  false : this._hierarchicalTableModel.isHierarchical(rowIndex);
	}

	public Object getChildValueAt(int rowIndex)
	{
		return this._hierarchicalTableModel == null ?  null : this._hierarchicalTableModel.getChildValueAt(rowIndex);
	}

	public boolean isExpandable(int rowIndex)
	{
		return this._hierarchicalTableModel == null ?  false : this._hierarchicalTableModel.isExpandable(rowIndex);
	}
	//End of HierarchicalTableModel

	@Override
	public boolean isCellEditable(int row, int column)
	{
		long key = ((long)row) << 32;
		key += column;
		if(this._columnToEditable.containsKey(key))
		{
			return this._columnToEditable.get(key).booleanValue();
		}
		else
		{
			return !this._propertyDescriptors[column].get_IsReadonly();
		}
	}

	public Object getObject(int index)
	{
		return this._indexer.elementAt(index);
	}

	public int getRow(Object item)
	{
		return this._indexer.indexOf(item);
	}

	public int getColumnByCaption(String propertyCaption)
	{
		for(int index = 0; index < this._propertyDescriptors.length; index++)
		{
			PropertyDescriptor propertyDescriptor = this._propertyDescriptors[index];
			if(propertyDescriptor.get_Caption().compareToIgnoreCase(propertyCaption) == 0)
			{
				return index;
			}
		}
		return -1;
	}

	public int getColumnByName(String propertyName)
	{
		for(int index = 0; index < this._propertyDescriptors.length; index++)
		{
			PropertyDescriptor propertyDescriptor = this._propertyDescriptors[index];
			String name = propertyDescriptor.get_IsDynamic() ? propertyDescriptor.get_DynamicName() : propertyDescriptor.get_Name();
			if(name.compareToIgnoreCase(propertyName) == 0)
			{
				return index;
			}
		}
		return -1;
	}

	//////////////////////IView///////////////////
	public void updateProperties(PropertyDescriptor[] properties)
	{
		for(PropertyDescriptor propertyDescriptor : properties)
		{
			this.updateProperty(propertyDescriptor);
		}
	}

	public void updateProperty(PropertyDescriptor property)
	{
		for(IView view : this._views)
		{
			view.updateProperty(property);
		}

		int column = this.getColumnByCaption(property.get_Caption());
		this._propertyDescriptors[column] = property;
		CellStyle cellStyle = this._columnToCellSytles.get(property.get_Name());
		cellStyle.setFont(this._font == null ? property.get_Font() : this._font);
		cellStyle.setHorizontalAlignment(property.get_Alignment());

		this.fireTableDataChanged();
	}

	public void update(Object item, String propertyName, Object propertyValue)
	{
		int row = this.getRow(item);
		if(row == -1) return;
		int column = this.getColumnByName(propertyName);

		if(column == -1) return;//fix the bug of the old version, which may provide propertyName not in the model, ie, in the method update of Order

		Object oldValue = this.getValueAt(row, column);
		if(oldValue == null || !oldValue.equals(propertyValue))
		{
			super.setValueAt(propertyValue, row, column);
		}
	}

	public void update(Object item)
	{
		int row = this.getRow(item);
		if(row == -1) return;
		for(int column = 0; column < this._propertyDescriptors.length; column++)
		{
			PropertyDescriptor propertyDescriptor = this._propertyDescriptors[column];
			Object newValue = propertyDescriptor.getValue(item);

			Object oldValue = this.getValueAt(row, column);
			if(oldValue == null || !oldValue.equals(newValue))
			{
				super.setValueAt(newValue, row, column);
			}
		}
		this.fireObjectUpdated(item);
	}

	@Override
	public void setValueAt(Object newValue, int row, int column)
	{
		Object object =  this.getObject(row);
		PropertyDescriptor property = this._propertyDescriptors[column];
		//if (!property.get_IsReadonly())
		{
			Object oldValue = property.getValue(object);

			if(!this._suppressPropertyChaningEvent)
			{
				PropertyChangingEvent e = new PropertyChangingEvent(object, property, oldValue, newValue, row, column);
				IPropertyChangingListener[] listeners = new IPropertyChangingListener[this._propertyChangingListeners.size()];
				listeners = this._propertyChangingListeners.toArray(listeners);
				for (IPropertyChangingListener propertyChangingListener : listeners)
				{
					propertyChangingListener.propertyChanging(e);
				}

				if(e.get_Cancel())
				{
					return;
				}
				newValue = e.get_NewValue();
			}

			super.setValueAt(newValue, row, column);
			property.setValue(object, newValue);

			for (IPropertyChangedListener propertyChangedListener : this._propertyChangedListeners)
			{
				propertyChangedListener.propertyChanged(object, property, oldValue, newValue, row, column);
			}
		}
	}

	public void insert(int index, Object item)
	{
		if(!this._indexer.contains(item))
		{
			Vector propertyValues = this.getPropertyValues(item);
			this._indexer.insertElementAt(item, index);
			this.insertRow(index, propertyValues);
		}
	}

	public void add(Object item)
	{
		if(!this._indexer.contains(item))
		{
			if(this.fireObjectAddingEvent(item))
			{
				Vector propertyValues = this.getPropertyValues(item);
				this._indexer.add(item);
				this.addRow(propertyValues);
			}
		}
	}

	/*public void remove(Object item)
	{
		if(this._indexer.contains(item))
		{
			int row = this.getRow(item);

			this._indexer.remove(row);
			this.removeRow(row);
		}
	}*/

	public void remove(Object item)
	{
		if(this._indexer.contains(item))
		{
			int row = this.getRow(item);

			this.beginCacheTableChangedEvent();
			this.removeRow(row);
			this._indexer.remove(row);
			this.fireCachedTableChanged();
			this.endCacheTableChangedEvent();

			this.fireObjectRemovedEvent(item);
		}
	}

	private ArrayList<TableModelEvent> _cachedEvents = new ArrayList<TableModelEvent>();
	private boolean _cacheTableChangedEvent = false;
	private void beginCacheTableChangedEvent()
	{
		this._cacheTableChangedEvent = true;
	}

	private void endCacheTableChangedEvent()
	{
		this._cacheTableChangedEvent = false;
	}

	private void fireCachedTableChanged()
	{
		TableModelEvent[] events = this._cachedEvents.toArray(new TableModelEvent[]{});
		for(TableModelEvent e : events)
		{
			super.fireTableChanged(e);
		}
		this._cachedEvents.clear();
	}

	@Override
	 public void fireTableChanged(TableModelEvent e)
	 {
		 if(this._cacheTableChangedEvent)
		 {
			 this._cachedEvents.add(e);
		 }
		 else
		 {
			 super.fireTableChanged(e);
		 }
	}

	public void setBackground(Object item, Color background)
	{
		int row = this.getRow(item);
		for(int column = 0; column < this._propertyDescriptors.length; column++)
		{
			this.setCellStyle(row, column, CellStyleItem.Background, background);
		}
	}

	public void setForeground(Object item, Color color)
	{
		int row = this.getRow(item);
		for(int column = 0; column < this._propertyDescriptors.length; column++)
		{
			this.setCellStyle(row, column, CellStyleItem.Foreground, color);
		}
	}

	public void resetBackground(Object item)
	{
		int row = this.getRow(item);
		for(int column = 0; column < this._propertyDescriptors.length; column++)
		{
			if(super.getCellStyleAt(row, column) != null)
			{
				//CellStyle cellStyle = this.getCellStyleAt(row, column);
				//cellStyle.setBackground(null);
				//super.setCellStyle(row, column, cellStyle);
				super.removeCellStyle(row, column);
			}
		}
	}

	public void resetForeground(Object item)
	{
		int row = this.getRow(item);
		for(int column = 0; column < this._propertyDescriptors.length; column++)
		{
			if(super.getCellStyleAt(row, column) != null)
			{
				//CellStyle cellStyle = this.getCellStyleAt(row, column);
				//cellStyle.setBackground(null);
				//super.setCellStyle(row, column, cellStyle);
				super.removeCellStyle(row, column);
			}
		}
	}

	public void setBackground(Object item, String propertyName, Color background)
	{
		this.setCellStyle(item, propertyName, CellStyleItem.Background, background);
	}

	public void setForeground(Object item, String propertyName, Color foreground)
	{
		this.setCellStyle(item, propertyName, CellStyleItem.Foreground, foreground);
	}

	public void setImage(Object item, String propertyName, Image image)
	{
		this.setCellStyle(item, propertyName, CellStyleItem.Image, image);
	}

	public void setIcon(Object item, String propertyName, Icon icon)
	{
		this.setCellStyle(item, propertyName, CellStyleItem.Icon, icon);
	}

	public void bind(Collection dataSource, PropertyDescriptor[] propertyDescriptors)
	{
		this._propertyDescriptors = new PropertyDescriptor[propertyDescriptors.length];
		Vector columnIdentifiers = new Vector(propertyDescriptors.length);

		for(int column = 0; column < propertyDescriptors.length; column++)
		{
			PropertyDescriptor propertyDescriptor = propertyDescriptors[column];
			this._propertyDescriptors[column] = propertyDescriptor;
			String name = propertyDescriptor.get_IsDynamic() ? propertyDescriptor.get_DynamicName() : propertyDescriptor.get_Name();
			columnIdentifiers.add(name);

			CellStyle cellStyle = CellStyleHelper.createCellStyle(propertyDescriptor);
			cellStyle.setPriority(BindingSource._columnCellStylePriority);
			this._columnToCellSytles.put(name, cellStyle);
		}

		this._indexer = dataSource == null ? new Vector() : new Vector(dataSource.size());
		Vector dataVector = dataSource == null ? new Vector() :  new Vector(dataSource.size());

		if(dataSource != null)
		{
			Iterator iterator = dataSource.iterator();
			while (iterator.hasNext())
			{
				Object object = iterator.next();
				this._indexer.add(object);

				Vector properties = new Vector(propertyDescriptors.length);
				for (int column = 0; column < propertyDescriptors.length; column++)
				{
					PropertyDescriptor propertyDescriptor = propertyDescriptors[column];
					Object property = propertyDescriptor.getValue(object);
					properties.add(property);
				}
				dataVector.add(properties);
			}
		}
		super.setDataVector(dataVector, columnIdentifiers);
	}

	public void removeAll()
	{
		try
			{
				if (this._indexer == null || this._indexer.size() == 0)
					return;
				int rowCount = this._indexer.size();
				for (int index = rowCount - 1; index >= 0; index--)
				{
					this.remove(this.getObject(index));
				}
			}
			catch(Throwable t)
				{

			}
	}

	public void setHeader(int alignment, int height, Color foreground, Color background, Font font)
	{
		for(IView view : this._views)
		{
			view.setHeader(alignment, height, foreground, background, font);
		}
	}

	public void setGrid(int rowsHeight, Color foreground, Color background, Color borderColor, boolean rowResizing, boolean columnResizing, Font font,
						boolean cellRangeSelection, boolean largeCurrentCell, boolean sortOnColumnClick)
	{
		for(IView view : this._views)
		{
			view.setGrid(rowsHeight, foreground, background, borderColor, rowResizing, columnResizing, font,
						cellRangeSelection, largeCurrentCell, sortOnColumnClick);
		}
	}

	/*@Override
	public CellStyle getCellStyleAt(int row, int column)
	{
		if(this._font == null && ColumnUIInfoManager.get_IsLoaded())
		{
			Font font = ColumnUIInfoManager.getFont(this._name);
			if(font != null)
			{
				this.setFont(font);
			}
		}

		CellStyle cellStyle1 = super.getCellStyleAt(row, column);
		CellStyle cellStyle2 = this._columnToCellSytles.get(this._propertyDescriptors[column].get_Name());
		if(this._font != null)
		{
			if(cellStyle1 != null) cellStyle1.setFont(this._font);
			if(cellStyle2 != null) cellStyle2.setFont(this._font);
			if(cellStyle1 == null && cellStyle2 == null)
			{
				cellStyle1 = new CellStyle();
				cellStyle1.setFont(this._font);
			}
		}
		return CellStyleHelper.merge(cellStyle1, cellStyle2);
	}*/

	@Override
	public CellStyle getCellStyleAt(int row, int column)
	{
		if(this._font != null) this.setCellStyle(row, column, CellStyleItem.Font, this._font);

		CellStyle cellStyle = super.getCellStyleAt(row, column);
		if(cellStyle != null)
		{
			if (cellStyle.getForeground() == null)
				cellStyle.setForeground(this._useBlackAsBackground ? ColorSettings.GridForeground : Color.BLACK);
			if (cellStyle.getSelectionForeground() == null)
				cellStyle.setSelectionForeground(this._useBlackAsBackground ? ColorSettings.GridForeground : Color.BLACK);
			if(this._backgroundColor != null && cellStyle.getBackground() == null)
			{
				cellStyle.setBackground(this._backgroundColor);
			}
		}
		return cellStyle == null ? (this._useBlackAsBackground ? BindingSource._blackBackgroundDefaultCellStyle : this._defaultCellStyle): cellStyle;
	}

	public void setHeight(Object item, int height)
	{
		for(IView view : this._views)
		{
			view.setHeight(item, height);
		}
	}

	public void setType(String propertyName, int typeValue)
	{
		String info = "[BindingSource].setType is not supported, propertyName=" + propertyName + "; typeValue=" + typeValue;
		TradingConsole.traceSource.trace(TraceType.Warning, info);
	}
	//////////////////////IView///////////////////

	public void setEditable(int row, int column, boolean isEditable)
	{
		long key = ((long)row) << 32;
		key += column;
		this._columnToEditable.put(key, isEditable);
	}

	private boolean _suppressPropertyChaningEvent = false;
	public void suppressPropertyChaningEvent()
	{
		this._suppressPropertyChaningEvent = true;
	}

	public void resumePropertyChaningEvent()
	{
		this._suppressPropertyChaningEvent = false;
	}

	private boolean fireObjectAddingEvent(Object item)
	{
		boolean canAddObject = true;
		for(IObjectAddingListener objectAddingListener : this._objectAddingListeners)
		{
			canAddObject &= objectAddingListener.canAddObject(item);
		}
		return canAddObject;
	}

	private void fireObjectRemovedEvent(Object item)
	{
		for(IObjectRemovedListener objectRemovedListener : this._objectRemovedListeners)
		{
			objectRemovedListener.removed(this, item);
		}
	}

	private void setCellStyle(Object item, String propertyName, CellStyleItem cellStyleItem, Object value)
	{
		int row = this.getRow(item);
		int column = this.getColumnByName(propertyName);
		this.setCellStyle(row, column, cellStyleItem, value);
	}

	private void setCellStyle(int row, int column, CellStyleItem cellStyleItem, Object value)
	{
		CellStyle cellStyle = super.getCellStyleAt(row, column);
		if(cellStyle == null) cellStyle = new CellStyle();
		cellStyle.setPriority(BindingSource._specificalCellStylePriority);
		if(cellStyleItem == CellStyleItem.Background)
		{
			if(cellStyle.getBackground() == null || !cellStyle.getBackground().equals(value))
			{
				cellStyle.setBackground( (Color)value);
			}
			else
			{
				return;
			}
		}
		else if(cellStyleItem == CellStyleItem.Foreground)
		{
			if(cellStyle.getForeground() == null || !cellStyle.getForeground().equals(value))
			{
				cellStyle.setForeground((Color)value);
				cellStyle.setSelectionForeground((Color)value);
			}
			else
			{
				return;
			}

		}
		else if(cellStyleItem == CellStyleItem.Font)
		{
			if(cellStyle.getFont() == null || !cellStyle.getFont().equals(value))
			{
				cellStyle.setFont( (Font)value);
			}
			else
			{
				return;
			}
		}
		else if(cellStyleItem == CellStyleItem.Image)
		{
			cellStyle.setIcon(new ImageIcon( (Image)value));
		}
		else if(cellStyleItem == CellStyleItem.Icon)
		{
			if(cellStyle.getIcon() == null || !cellStyle.getIcon().equals(value))
			{
				cellStyle.setIcon( (Icon)value);
			}
			else
			{
				return;
			}
		}
		else if(cellStyleItem == CellStyleItem.HorizontalAlignment)
		{
			cellStyle.setHorizontalAlignment(((Integer)value).intValue());
		}
		else if(cellStyleItem == CellStyleItem.Border)
		{
			if(cellStyle.getBorder() == null || !cellStyle.getBorder().equals(value))
			{
				cellStyle.setBorder( (Border)value);
			}
			else
			{
				return;
			}
		}

		this.setCellStyle(row, column, cellStyle);
	}

	private Vector getPropertyValues(Object item)
	{
		Vector propertyValues = new Vector(this._propertyDescriptors.length);
		for (int column = 0; column < this._propertyDescriptors.length; column++)
		{
			PropertyDescriptor propertyDescriptor = this._propertyDescriptors[column];
			propertyValues.add(propertyDescriptor.getValue(item));
		}
		return propertyValues;
	}

	public String get_DataSourceKey()
	{
		return this._dataSourceKey;
	}

	public void setDataSourceKey(String dataSourceKey)
	{
		this._dataSourceKey = dataSourceKey;
	}

	public void useBlackAsBackground()
	{
		this._useBlackAsBackground = true;
	}

	public void setBackground(Color color)
	{
		this._backgroundColor = color;
		if(!this._useBlackAsBackground)
		{
			this._defaultCellStyle.setBackground(this._backgroundColor);
		}
	}

	public void setHorizontalAlignment(int horizontalAlignment)
	{
		this._defaultCellStyle.setHorizontalAlignment(horizontalAlignment);
	}

	public enum CellStyleItem{Background, Foreground, Font, Image, Icon, HorizontalAlignment, Border}
}
