package tradingConsole.settings;

import framework.data.DataRow;
import framework.Guid;
import tradingConsole.TradingConsole;
import tradingConsole.ui.colorHelper.CurrentCellColor;
import tradingConsole.ui.fontHelper.GridFont;
import tradingConsole.ui.colorHelper.GridBackColor;
import tradingConsole.ui.colorHelper.GridFixedBackColor;
import java.awt.Color;
import tradingConsole.framework.PropertyDescriptor;
import java.util.Collection;
import tradingConsole.ui.borderStyleHelper.BorderStyle;
import tradingConsole.ui.colorHelper.GridFixedForeColor;
import tradingConsole.ui.fontHelper.HeaderFont;
import tradingConsole.ui.columnFixedHelper.RowLabelWidth;
import tradingConsole.ui.colorHelper.SelectionBackground;
import tradingConsole.ui.borderStyleHelper.CurrentCellBorder;
import tradingConsole.ui.language.TradingAccountLanguage;
import tradingConsole.ui.columnKey.TradingAccountColKey;
import javax.swing.SwingConstants;
import tradingConsole.ui.grid.DataGrid;
import tradingConsole.ui.ColorSettings;

public class TradingAccount
{
	private Guid _id;
	private String _code;
	private Guid _customerId;

	private boolean _isActivated;

	public boolean get_IsActivated()
	{
		return this._isActivated;
	}

	public void set_IsActivated(boolean value)
	{
		this._isActivated = value;
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public String get_Code()
	{
		return this._code;
	}

	public Guid get_CustomerId()
	{
		return this._customerId;
	}

	public TradingAccount(Guid id, DataRow dataRow)
	{
		this._id = id;
		this.setValue(dataRow);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		this._code = (String) dataRow.get_Item("Code");
		this._customerId = new Guid(dataRow.get_Item("CustomerId").toString());
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[2];
		int i = -1;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(TradingAccount.class, TradingAccountColKey.IsActivated, true, null,
			TradingAccountLanguage.IsActivated,	60, SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(TradingAccount.class, TradingAccountColKey.Code, true, null,
			TradingAccountLanguage.Code, 90, SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		return propertyDescriptors;
	}

	public static void initialize(DataGrid grid, String dataSourceKey, Collection dataSource, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		//grid.setRedraw(false);
		//grid.reset();
		grid.setShowVerticalLines(false);
		grid.setBackground(GridFixedBackColor.account);
		grid.setForeground(GridBackColor.account);
		//grid.setBorderStyle(BorderStyle.account);
		//grid.setRowLabelWidth(RowLabelWidth.account);
		//grid.setSelectionBackground(SelectionBackground.account);
		//grid.setCurrentCellColor(CurrentCellColor.account);
		//grid.setCurrentCellBorder(CurrentCellBorder.account);

		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, TradingAccount.getPropertyDescriptors());
		grid.setModel(bindingSource);
		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.account, Color.white, HeaderFont.account);

		Color foreground = ColorSettings.useBlackAsBackground ? ColorSettings.GridForeground : Color.black;
		Color background = ColorSettings.useBlackAsBackground ? ColorSettings.TradingListGridBackground : Color.white;
		TradingConsole.bindingManager.setGrid(dataSourceKey, 18, foreground, background, Color.blue, true, true, GridFont.account, false, true, false);
		//grid.setRedraw(true);
	}

	public static void unbind(String dataSourceKey, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		TradingConsole.bindingManager.unbind(dataSourceKey, bindingSource);
	}

	public void add(String dataSourceKey)
	{
		TradingConsole.bindingManager.add(dataSourceKey, this);
	}

	public void update(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
	}
}
