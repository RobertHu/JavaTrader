package tradingConsole.ui.grid;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import com.jidesoft.converter.*;
import com.jidesoft.grid.*;
import com.jidesoft.grid.Filter;
import tradingConsole.framework.*;
import tradingConsole.ui.*;
import tradingConsole.ui.columnKey.*;

public class DataGrid extends HierarchicalTable implements IView
{
	public static final Color SelectionBackground = ColorSettings.useBlackAsBackground ?  new Color(96, 96, 96): new Color(228, 228, 228);
	public static Font DefaultFont;
	public static Color[] StripeColors;
	protected static tradingConsole.ui.grid.TableColumnChooser _columnChooser = new tradingConsole.ui.grid.TableColumnChooser();

	protected ArrayList<ISelectedRowChangedListener> _selectedRowChangedListeners = new ArrayList<ISelectedRowChangedListener> ();
	protected String _name;
	protected BindingSource _bindingSource;
	protected FilterableTableModel _filterableTableModel;
	protected HeaderRenderer _headerRenderer;
	protected boolean _editable = true;
	private String _dataSourceKey;
	private ArrayList<String> _enableFilterColumns = new ArrayList<String>();
	private JTableHeader _orginalTableHeader = null;

	protected ArrayList<IActionListener> _actionListeners = new ArrayList<IActionListener> ();

	static
	{
		ObjectConverterManager.initDefaultConverter();
		CellEditorManager.initDefaultEditor();
		CellRendererManager.initDefaultRenderer();
		DataGrid.StripeColors = new Color[]{Color.WHITE, new Color(240, 240, 255)};
		DataGrid.DefaultFont = ColorSettings.useBlackAsBackground ? new Font("SansSerif", Font.PLAIN, 12) :  new Font("SansSerif", Font.BOLD, 12);
	}

	public DataGrid(String name)
	{
		//TradingConsole.traceSource.trace(TraceType.Information, "DataGrid constructor begin " + DateTime.get_Now().toString(DateTime.fullFormat));
		if (name == null || name.length() == 0)
		{
			throw new IllegalArgumentException("name can't be null or empty");
		}

		this._name = name;
		//this.setSelectionForeground(Color.BLACK);
		this.setSelectionBackground(DataGrid.SelectionBackground);
		this.setAutoResort(true);
		this.setAutoscrolls(false);

		this._headerRenderer = new HeaderRenderer();
		this.getTableHeader().setDefaultRenderer(this._headerRenderer);

		this.setBorder(BorderFactory.createEmptyBorder());
		this.setIntercellSpacing(new Dimension(0, 0));
		this.setRowAutoResizes(false);
		this.setColumnAutoResizable(false);

		super.addMouseListener(new MouseListener(new AdvancedMouseListener(this)));
		this.setOpaque(true);
		//TradingConsole.traceSource.trace(TraceType.Information, "DataGrid constructor end " + DateTime.get_Now().toString(DateTime.fullFormat));
	}

	public String get_Name()
	{
		return this._name;
	}

	public BindingSource get_BindingSource()
	{
		return this._bindingSource;
	}

	public HeaderRenderer get_HeaderRenderer()
	{
		return this._headerRenderer;
	}

	public void enableChooseColumn()
	{
		this.putClientProperty(TableColumnChooser.TABLE_COLUMN_CHOOSER, DataGrid._columnChooser);
		this.putClientProperty(TableColumnChooser.SHOW_AUTO_RESIZE, false);
		TableColumnChooser.install(this);
	}

	public void disableChooseColumn()
	{
		TableColumnChooser.uninstall(this);
	}

	public void enableColumnUIPersistent()
	{
		ColumnUIInfoManager.enableColumnUIPersistent(this);
	}

	public void disableColumnUIPersistent()
	{
		ColumnUIInfoManager.disableColumnUIPersistent(this);
	}

	public void enableRowStripe()
	{
		this.enableRowStripe(DataGrid.StripeColors);
	}

	public void enableRowStripe(Color[] stripeColors)
	{
		this.setCellStyleProvider(new RowStripeCellStyleProvider(stripeColors));
	}

	public void disableRowStripe()
	{
		this.setCellStyleProvider(null);
	}

	public void addSelectedRowChangedListener(ISelectedRowChangedListener selectedRowChangedListener)
	{
		this._selectedRowChangedListeners.add(selectedRowChangedListener);
	}

	private boolean _suppressSelectedRowChangedEvent = false;
	public void suppressSelectedRowChangedEvent()
	{
		this._suppressSelectedRowChangedEvent = true;
	}

	public void resumeSelectedRowChangedEvent()
	{
		this._suppressSelectedRowChangedEvent = false;
	}

	public boolean removeSelectedRowChangedListener(ISelectedRowChangedListener selectedRowChangedListener)
	{
		return this._selectedRowChangedListeners.remove(selectedRowChangedListener);
	}

	public void addActionListener(IActionListener actionListener)
	{
		this._actionListeners.add(actionListener);
	}

	public boolean removeActionListener(IActionListener actionListener)
	{
		return this._actionListeners.remove(actionListener);
	}

	@Override
	public void setModel(TableModel model)
	{
		this.setModel(model, -1);
	}

	public void setModel(TableModel model, int expandableColumn)
	{
		if (model instanceof BindingSource)
		{
			this._bindingSource = (BindingSource)model;
			this._bindingSource.addFontChangedListener(new FontChangedListener(this));
			this._bindingSource.set_Name(this._name);
			this._bindingSource.addView(this);
			this._filterableTableModel = new FilterableTableModel(this._bindingSource);
			this._filterableTableModel.setFiltersApplied(true);
			super.setModel(this._filterableTableModel);

			this.updateProperties(this._bindingSource.getPropertyDescriptors());
			ColumnUIInfoManager.hideAllNotChoosedColumns(this);
			this.setHierarchicalColumn(expandableColumn);
			if(this._bindingSource.get_Font() == null)
			{
				this.setFont(DataGrid.DefaultFont);
			}
			else
			{
				this.setFont(this._bindingSource.get_Font());
			}
		}
		else
		{
			super.setModel(model);
		}
	}

	@Override
	public TableModel getModel()
	{
		return super.getModel();
	}

	@Override
	public void setFont(Font font)
	{
		super.setFont(font);
		this.setRowHeight(font.getSize());
	}

	@Override
	public void setRowHeight(int height)
	{
		if(this.getFont() != null) height =  this.getFont().getSize();
		height = height * 2;
		super.setRowHeight(height);
	}

	private void fontChanged(Font newFont, Font oldFont)
	{
		float oldSize = oldFont == null ? 12 : (float)oldFont.getSize();
		float quotiety = (float)newFont.getSize() / oldSize;

		int rowHeight = (int)Math.floor(this.getRowHeight() * quotiety);
		rowHeight = Math.max(rowHeight, newFont.getSize());
		this.setRowHeight(rowHeight);

		Enumeration<TableColumn> tableColumns = this.getColumnModel().getColumns();
		while (tableColumns.hasMoreElements())
		{
			TableColumn tableColumn = tableColumns.nextElement();
			//tableColumn.sizeWidthToFit();//doesn't work as expire???
			int oldWidth = tableColumn.getWidth();
			int columnWidth = (int)Math.ceil(oldWidth * quotiety);
			tableColumn.setWidth(columnWidth);
		}
	}

	public void bind(Collection dataSource, PropertyDescriptor[] properties)
	{
		BindingSource bindingSource = new BindingSource();
		bindingSource.bind(dataSource, properties);
		this.setModel(bindingSource);
	}

	public void updateProperties(PropertyDescriptor[] properties)
	{
		this.updateProperties(properties, true);
	}

	public void updateProperty(PropertyDescriptor property)
	{
		this.updateProperty(property, true);
	}

	public void updateProperties(PropertyDescriptor[] properties, boolean includeColumnWidth)
	{
		for (PropertyDescriptor propertyDescriptor : properties)
		{
			this.updateProperty(propertyDescriptor, includeColumnWidth);
		}

		if(GridNames.InstrumentGrid.equalsIgnoreCase(this._name))
		{
			try
			{
				int sequenceColumn = this._bindingSource.getColumnByName(InstrumentColKey.Sequence);
				TableColumnChooser.hideColumn(this, sequenceColumn);
			}
			catch(Throwable throwable)
			{
			}
		}
	}

	private void updateProperty(PropertyDescriptor property, boolean includeColumnWidth)
	{
		String columnName = property.get_Name();
		TableColumn tableColumn = this.getColumnByName(columnName);

		if (tableColumn != null)
		{
			if (includeColumnWidth)
			{
				tableColumn.setWidth(property.get_Width());
				tableColumn.setPreferredWidth(property.get_Width());
			}
			tableColumn.setHeaderValue(property.get_Caption());
			tableColumn.setIdentifier(columnName);
			this._headerRenderer.setAlignment(property.get_HeadingAlignment());
			if (property.get_CellEditor() != null)
			{
				tableColumn.setCellEditor(property.get_CellEditor());
			}
			if (property.get_CellRenderer() != null)
			{
				tableColumn.setCellRenderer(property.get_CellRenderer());
			}
		}
	}

	//column hiden using TableColumnChooser will throw exception, can't find way to avoid....???
	private TableColumn getColumnByName(String columnName)
	{
		try
		{
			return this.getColumn(columnName);
		}
		catch(Throwable t)
		{
			return null;
		}
	}

	public void clearFilters()
	{
		if (this._filterableTableModel != null)
		{
			this._filterableTableModel.clearFilters();
		}
	}

	public void addFilter(Filter filter)
	{
		if (this._filterableTableModel != null)
		{
			this._filterableTableModel.addFilter(0, filter);
		}
	}

	public void filter()
	{
		if (this._filterableTableModel != null)
		{
			this._filterableTableModel.refresh();
		}
	}

	public void update(Object item, String propertyName, Object propertyValue) //for implement interface IView
	{
	}

	public void update(Object item) //for implement interface IView
	{
	}

	public void add(Object item) //for implement interface IView
	{
	}

	public void insert(int index, Object item) //for implement interface IView
	{
	}

	public void remove(Object item) //for implement interface IView
	{
	}

	public void setHeader(int alignment, int height, Color foreground, Color background, Font font)
	{
		this._headerRenderer.setAlignment(alignment);
		this._headerRenderer.setHeight(height);
		this._headerRenderer.setForeground(foreground);
		this._headerRenderer.setAlignment(alignment);
		if (background == null || !background.equals(foreground))
		{
			this._headerRenderer.setBackground(background);
		}
		else
		{
			this._headerRenderer.setBackground(new Color(144, 144, 225));
		}
		this._headerRenderer.setFont(font);
	}

	public void setGrid(int rowsHeight, Color foreground, Color background, Color borderColor, boolean rowResizing,
						boolean columnResizing, Font font, boolean cellRangeSelection, boolean largeCurrentCell, boolean sortOnColumnClick)
	{
		this.setRowHeight(rowsHeight);

		this.setForeground(foreground);
		this.setDisabledForeground(foreground);
		this.setSelectionForeground(foreground);

		this.setBackground(background);
		this.setDisabledBackground(background);
		this.setSelectionBackground(DataGrid.SelectionBackground);

		this.setColumnResizable(columnResizing);
		//this.setRowResizable(rowResizing);
		this.setRowResizable(false);
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.setSortingEnabled(sortOnColumnClick);
	}

	public void setHeight(Object item, int height)
	{
		int row = this.getRow(item);
		this.setRowHeight(row, height);
	}

	public void enableFilter(String propertyName)
	{
		if(this._enableFilterColumns.size() == 0)
		{
			AutoFilterTableHeader header = new AutoFilterTableHeader(this)
			{
				@Override
				protected void customizeAutoFilterBox(AutoFilterBox autoFilterBox)
				{
					int column = autoFilterBox.getTableColumnIndex();

					if (_bindingSource != null)
					{
						PropertyDescriptor propertyDescriptor = _bindingSource.getPropertyDescriptors()[column];
						String caption = propertyDescriptor.get_Caption();
						autoFilterBox.setText(caption);
						autoFilterBox.setFilterButtonVisible(_enableFilterColumns.contains(propertyDescriptor.get_Name()));
					}

					autoFilterBox.setBorder(_headerRenderer.getBorder());
					autoFilterBox.setBackground(_headerRenderer.getBackground());
					autoFilterBox.setOpaque(true);
					for(Component child : autoFilterBox.getComponents())
					{
						if(child instanceof JLabel)
						{
							((JLabel)child).setOpaque(true);
							((JLabel)child).setHorizontalAlignment(SwingConstants.CENTER);
						}
						if(child instanceof JButton)
						{
							((JButton)child).setOpaque(true);
						}
						child.setFont(_headerRenderer.getFont());
						child.setForeground(_headerRenderer.getForeground());
						child.setBackground(_headerRenderer.getBackground());
					}
				}
			};
			header.setAutoFilterEnabled(true);
			header.setShowSortArrow(true);

			this._orginalTableHeader = this.getTableHeader();
			this.setTableHeader(header);
			header.setDefaultRenderer(this._headerRenderer);
		}
		if(!this._enableFilterColumns.contains(propertyName)) this._enableFilterColumns.add(propertyName);
	}

	public void disableAutoFilter(String propertyName)
	{
		this._enableFilterColumns.remove(propertyName);
		if(this._enableFilterColumns.size() == 0)
		{
			this.setTableHeader(this._orginalTableHeader);
		}
	}

	public void setType(String propertyName, int typeValue) //for implement interface IView
	{
	}

	public void setBackground(Object item, Color background) //for implement interface IView
	{
	}

	public void resetBackground(Object item) //for implement interface IView
	{
	}

	public void setForeground(Object item, Color background) //for implement interface IView
	{
	}

	public void resetForeground(Object item) //for implement interface IView
	{
	}

	public void setBackground(Object item, String propertyName, Color background) //for implement interface IView
	{
	}

	public void setForeground(Object item, String propertyName, Color foreground) //for implement interface IView
	{
	}

	public void setImage(Object item, String propertyName, Image image) //for implement interface IView
	{
	}

	public void setIcon(Object item, String propertyName, Icon image) //for implement interface IView
	{
	}

	public void setEditable(boolean value)
	{
		this._editable = value;
	}

	@Override
	public void tableChanged(TableModelEvent e)
	{
		int editingRow = this.getSelectedRow();
		int editingColumn = this.getSelectedColumn();

		super.tableChanged(e);

		if (this._bindingSource != null)
		{
			this.updateProperties(this._bindingSource.getPropertyDescriptors(), false);
		}

		if (this.getRowCount() > 0 && e.getFirstRow() >= 0 && e.getLastRow() > e.getFirstRow()
			&& e.getType() == TableModelEvent.UPDATE && editingRow != -1 && editingColumn != -1
			&& this.getSelectedRow() != editingRow && this.getSelectedColumn() != editingColumn)
		{
			this.changeSelection(editingRow, editingColumn, false, false);
		}
	}

	@Override
	public void editingStopped(ChangeEvent e)
	{
		int editingRow = this.getSelectedRow();
		int editingColumn = this.getSelectedColumn();

		super.editingStopped(e);

		if (editingRow != -1 && editingColumn != -1)
		{
			this.changeSelection(editingRow, editingColumn, false, false);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) //of ListSelectionEvent, means selection changed but named valudChanged
	{
		super.valueChanged(e);
		if (!this._suppressSelectedRowChangedEvent)
		{
			for (ISelectedRowChangedListener selectedRowChangedListener : this._selectedRowChangedListeners)
			{
				selectedRowChangedListener.selectedRowChanged(this);
			}
		}
	}

	public Object getObject(int row)
	{
		if(this.getRowCount() == 1)//seems to have bug when only 1 row
		{
			row = 0;
		}
		else
		{
			row = TableModelWrapperUtils.getActualRowAt(this.getModel(), row);
		}
		return this._bindingSource.getObject(row);
	}

	@Override
	protected boolean startCellEditing(CellEditor editor, int row, int column)
	{
		if(this._bindingSource == null) return false;
		column = TableModelWrapperUtils.getActualColumnAt(this.getModel(), column);
		PropertyDescriptor property = this._bindingSource.getPropertyDescriptors()[column];
		return (this._editable && !property.get_IsReadonly()) ? super.startCellEditing(editor, row, column) : false;
	}

	public void updateEditingValue()
	{
		if (this.getEditorComponent() != null)
		{
			this.getCellEditor(this.getEditingRow(), this.getEditingColumn()).stopCellEditing();
		}
	}

	public void cancelEditing()
	{
		if (this.getEditorComponent() != null)
		{
			this.getCellEditor(this.getEditingRow(), this.getEditingColumn()).cancelCellEditing();
		}
	}

	private void clicked(MouseEvent e)
	{
		this.fireMouseAction(e, Action.Clicked);
	}

	private void doubleClicked(MouseEvent e)
	{
		this.fireMouseAction(e, Action.DoubleClicked);
	}

	private void fireMouseAction(MouseEvent e, Action action)
	{
		if (this._actionListeners.size() == 0 || this._bindingSource == null)
		{
			return;
		}

		int row = super.rowAtPoint(e.getPoint());
		int column = super.columnAtPoint(e.getPoint());
		if (row != -1 && column != -1)
		{
			int actualRow = TableModelWrapperUtils.getActualRowAt(this.getModel(), row);
			String columnName = (String)this.getColumnModel().getColumn(column).getIdentifier();
			int actualColumn = this._bindingSource.getColumnByName(columnName);
			if(actualColumn < 0) return;
			Object object = this._bindingSource.getObject(actualRow);
			PropertyDescriptor propertyDescriptor = this._bindingSource.getPropertyDescriptors()[actualColumn];
			for (IActionListener actionListener : this._actionListeners)
			{
				actionListener.actionPerformed(new ActionEvent(this, action, object, propertyDescriptor.get_Name(), row, column));
			}
		}
	}

	public int getRow(Object item)
	{
		int indexInModel = this._bindingSource.getRow(item);
		return TableModelWrapperUtils.getRowAt(this.getModel(), indexInModel);
	}

	public static tradingConsole.ui.grid.TableColumnChooser get_ColumnChooser()
	{
		return DataGrid._columnChooser;
	}

	public String get_DataSourceKey()
	{
		return this._dataSourceKey;
	}

	public void setDataSourceKey(String dataSourceKey)
	{
		this._dataSourceKey = dataSourceKey;
	}

	public void clearFilterColumns()
	{
		this._enableFilterColumns.clear();
	}

	private static class FontChangedListener implements IFontChangedListener
	{
		private DataGrid _owner;
		public FontChangedListener(DataGrid owner)
		{
			this._owner = owner;
		}

		public void fontChanged(Font newFont, Font oldFont)
		{
			this._owner.setFont(newFont);
		}
	}

	private static class AdvancedMouseListener implements IAdvancedMouseListener
	{
		private DataGrid _owner;
		AdvancedMouseListener(DataGrid owner)
		{
			this._owner = owner;
		}

		public void clicked(MouseEvent e)
		{
			this._owner.clicked(e);
		}

		public void doubleClicked(MouseEvent e)
		{
			this._owner.doubleClicked(e);
		}
	}
}
