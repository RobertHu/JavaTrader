package tradingConsole.ui;

import java.util.Collection;
import java.awt.Color;

import framework.Guid;

import tradingConsole.TradingConsole;
import tradingConsole.ui.language.InstrumentSelectLanguage;
import tradingConsole.framework.PropertyDescriptor;
import tradingConsole.Instrument;
import tradingConsole.ui.columnKey.InstrumentSelectColKey;
import tradingConsole.ui.colorHelper.GridBackColor;
import tradingConsole.ui.colorHelper.GridFixedBackColor;
import tradingConsole.ui.colorHelper.GridBackgroundColor;
import tradingConsole.ui.colorHelper.GridFixedForeColor;
import tradingConsole.ui.fontHelper.HeaderFont;
import tradingConsole.ui.fontHelper.GridFont;
import tradingConsole.ui.borderStyleHelper.BorderStyle;
import tradingConsole.ui.columnFixedHelper.RowLabelWidth;
import tradingConsole.ui.borderStyleHelper.CurrentCellBorder;
import tradingConsole.ui.colorHelper.CurrentCellColor;
import tradingConsole.ui.colorHelper.SelectionBackground;
import javax.swing.SwingConstants;
import tradingConsole.ui.grid.DataGrid;
import com.jidesoft.grid.TableColumnChooser;
import com.jidesoft.grid.BooleanCheckBoxCellEditor;
import com.jidesoft.grid.BooleanCheckBoxCellRenderer;

public class InstrumentSelection implements Comparable
{
	private Guid _id;
	private boolean _isSelected;
	private String _description;
	private int _sequence;
	private String _groupName;

	public Guid get_Id()
	{
		return this._id;
	}

	public String get_Description()
	{
		return this._description;
	}

	public String get_GroupName()
	{
		return this._groupName;
	}

	public void set_GroupName(String groupName)
	{
		this._groupName = groupName;
	}

	public boolean get_IsSelected()
	{
		return this._isSelected;
	}

	public void set_IsSelected(boolean value)
	{
		this._isSelected = value;
	}

	public int get_Sequence()
	{
		return this._sequence;
	}

	public void set_Sequence(int value)
	{
		this._sequence = value;
	}

	public void setValue(Instrument instrument)
	{
		this.setValue(instrument.get_Id(),true,instrument.get_Description(),instrument.get_Sequence(),"");
	}

	public void setValue(Guid id,boolean isSelected,String description,int sequence,String groupName)
	{
		this._id = id;
		this._isSelected = isSelected;
		this._description = description;
		this._sequence = sequence;
		this._groupName = groupName;
	}

	private InstrumentSelection()
	{
	}

	public static InstrumentSelection create()
	{
		return new InstrumentSelection();
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[2];
		int i = 0;

		/*PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(InstrumentSelection.class, InstrumentSelectColKey.IsSelected, false, null, InstrumentSelectLanguage.IsSelected, 60,
			SwingConstants.CENTER, null, null, new BooleanCheckBoxCellEditor(), new BooleanCheckBoxCellRenderer());
		propertyDescriptors[i++] = propertyDescriptor;*/

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(InstrumentSelection.class, InstrumentSelectColKey.Description, true, null, InstrumentSelectLanguage.Description, 108,
			SwingConstants.LEFT, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(InstrumentSelection.class, InstrumentSelectColKey.Sequence, true, null,
			InstrumentSelectLanguage.Sequence, 0, SwingConstants.CENTER, null, null);
		propertyDescriptors[i++] = propertyDescriptor;

		return propertyDescriptors;
	}

	public static void initialize(DataGrid grid, String dataSourceKey, Collection dataSource, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		//grid.setRedraw(false);
		//grid.reset();
		grid.setBackground(GridFixedBackColor.instrumentSelection);
		grid.setForeground(GridBackColor.instrumentSelection);
		//grid.setBorderStyle(BorderStyle.instrumentSelection);
		//grid.setRowLabelWidth(RowLabelWidth.instrumentSelection);
		//grid.setSelectionBackground(SelectionBackground.instrumentSelection);
		//grid.setCurrentCellColor(CurrentCellColor.instrumentSelection);
		//grid.setCurrentCellBorder(CurrentCellBorder.instrumentSelection);

		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, InstrumentSelection.getPropertyDescriptors());
		grid.setModel(bindingSource);
		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.LEFT, 25, GridFixedForeColor.instrumentSelection, Color.white, HeaderFont.instrumentSelection);
		TradingConsole.bindingManager.setGrid(dataSourceKey, 18, Color.black, Color.lightGray, Color.blue, false, true, GridFont.instrumentSelection,false, true, true);

		int column = bindingSource.getColumnByName(InstrumentSelectColKey.Sequence);
		TableColumnChooser.hideColumn(grid, column);
	}

	public void update(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
		this.setBackground(dataSourceKey,GridBackgroundColor.instrumentSelection);
	}

	public void setBackground(String dataSourceKey, Color background)
	{
		TradingConsole.bindingManager.setBackground(dataSourceKey, this, background);
	}

	public static void unbind(String dataSourceKey, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		if (bindingSource!=null)
		{
			TradingConsole.bindingManager.unbind(dataSourceKey, bindingSource);
		}
	}

	public int compareTo(Object o)
	{
		return this._sequence - ((InstrumentSelection)o)._sequence;
	}
}
