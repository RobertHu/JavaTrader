package tradingConsole.settings;

import java.awt.Color;
import java.util.Collection;

import framework.xml.XmlAttributeCollection;
import framework.xml.XmlConvert;

import tradingConsole.TradingConsole;
import tradingConsole.ui.colorHelper.GridBackgroundColor;
import tradingConsole.ui.language.Language;
import tradingConsole.ui.colorHelper.GridBackColor;
import tradingConsole.ui.columnKey.UISetting2ColKey;
import tradingConsole.ui.colorHelper.GridFixedBackColor;
import tradingConsole.framework.PropertyDescriptor;
import tradingConsole.ui.language.AccountSingleLanguage;
import tradingConsole.ui.language.InstrumentLanguage;
import tradingConsole.ui.language.OrderLanguage;
import tradingConsole.ui.language.OpenOrderLanguage;
import tradingConsole.ui.colorHelper.GridFixedForeColor;
import tradingConsole.ui.fontHelper.GridFont;
import tradingConsole.ui.fontHelper.HeaderFont;
import tradingConsole.ui.borderStyleHelper.BorderStyle;
import tradingConsole.ui.columnFixedHelper.RowLabelWidth;
import tradingConsole.ui.borderStyleHelper.CurrentCellBorder;
import tradingConsole.ui.colorHelper.CurrentCellColor;
import tradingConsole.ui.colorHelper.SelectionBackground;
import javax.swing.SwingConstants;
import tradingConsole.ui.grid.DataGrid;
import tradingConsole.ui.BindingManager;
import tradingConsole.ui.grid.BindingSource;

public class UISetting2
{
	private String _colKey;
	private String _column;
	private int _colWidth;
	private int _sequence;

	public String get_ColKey()
	{
		return this._colKey;
	}

	public String get_Column()
	{
		return this._column;
	}

	public int get_ColWidth()
	{
		return this._colWidth;
	}

	public void set_ColWidth(int value)
	{
		this._colWidth = value;
	}

	public int get_Sequence()
	{
		return this._sequence;
	}

	public void set_Sequence(int value)
	{
		this._sequence = value;
	}

	public String getValue()
	{
		String value = "<Col ";
		value += "ColKey=\"" + this._colKey + "\" ";
		value += "ColWidth=\"" + XmlConvert.toString(this._colWidth) + "\" ";
		value += "Sequence=\"" + XmlConvert.toString(this._sequence) + "\" ";
		value += "></Col>";

		return value;
	}

	public UISetting2(UISetting uiSetting, XmlAttributeCollection uiSetting2XmlAttributeCollection)
	{
		this.setValue(uiSetting, uiSetting2XmlAttributeCollection);
	}

	private static String getColumn(UISetting uiSetting, String colKey)
	{
		if (uiSetting.get_ObjectId().equals(UISetting.accountStatusUiSetting))
		{
			try
			{
				return (String) AccountSingleLanguage.class.getField(colKey).get(colKey);
			}
			catch (SecurityException ex)
			{
				return null;
			}
			catch (NoSuchFieldException ex)
			{
				return null;
			}
			catch (IllegalAccessException ex)
			{
				return null;
			}
			catch (IllegalArgumentException exception)
			{
				return null;
			}
		}
		else if (uiSetting.get_ObjectId().equals(UISetting.tradingPanelUiSetting))
		{
			try
			{
				return (String) InstrumentLanguage.class.getField(colKey).get(colKey);
			}
			catch (SecurityException exception)
			{
				return null;
			}
			catch (NoSuchFieldException exception)
			{
				return null;
			}
			catch (IllegalAccessException exception)
			{
				return null;
			}
			catch (IllegalArgumentException exception)
			{
				return null;
			}
		}
		else if (uiSetting.get_ObjectId().equals(UISetting.workingOrderListUiSetting))
		{
			try
			{
				return (String) OrderLanguage.class.getField(colKey).get(colKey);
			}
			catch (SecurityException ex2)
			{
				return null;
			}
			catch (NoSuchFieldException ex2)
			{
				return null;
			}
			catch (IllegalAccessException ex2)
			{
				return null;
			}
			catch (IllegalArgumentException ex2)
			{
				return null;
			}
		}
		else if (uiSetting.get_ObjectId().equals(UISetting.openOrderListUiSetting))
		{
			try
			{
				return (String) OpenOrderLanguage.class.getField(colKey).get(colKey);
			}
			catch (SecurityException ex3)
			{
				return null;
			}
			catch (NoSuchFieldException ex3)
			{
				return null;
			}
			catch (IllegalAccessException ex3)
			{
				return null;
			}
			catch (IllegalArgumentException ex3)
			{
				return null;
			}
		}
		return null;
	}

	private void setValue(UISetting uiSetting, XmlAttributeCollection uiSetting2XmlAttributeCollection)
	{
		for (int i = 0; i < uiSetting2XmlAttributeCollection.get_Count(); i++)
		{
			String nodeName = uiSetting2XmlAttributeCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = uiSetting2XmlAttributeCollection.get_ItemOf(i).get_Value();

			if (nodeName.equals("ColKey"))
			{
				this._colKey = nodeValue;
				String column = UISetting2.getColumn(uiSetting, nodeValue);
				this._column = (column == null) ? this._colKey : column;
			}
			else if (nodeName.equals("ColWidth"))
			{
				this._colWidth = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("Sequence"))
			{
				this._sequence = Integer.parseInt(nodeValue);
			}
		}
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[4];
		int i = -1;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(UISetting2.class, UISetting2ColKey.ColKey,
			false, null, "ColKey", 0, SwingConstants.CENTER, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(UISetting2.class, UISetting2ColKey.Sequence,
			false, null, "Sequence", 0,	SwingConstants.CENTER, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(UISetting2.class, UISetting2ColKey.Column,
			true, null, Language.lblFomtColKeyCaption, 145,	SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(UISetting2.class, UISetting2ColKey.ColWidth,
			false, null, Language.lblFontColWidthCaption, 82, SwingConstants.RIGHT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		return propertyDescriptors;
	}

	public static void initialize(DataGrid grid, String dataSourceKey, Collection dataSource, BindingSource bindingSource)
	{
		//grid.setRedraw(false);
		//grid.reset();
		//grid.setBackColor(GridFixedBackColor.uiSetting2);
		//grid.setTableColor(GridBackColor.uiSetting2);
		//grid.setBorderStyle(BorderStyle.uiSetting2);
		//grid.setRowLabelWidth(RowLabelWidth.uiSetting2);
		//grid.setSelectionBackground(SelectionBackground.uiSetting2);
		//grid.setCurrentCellColor(CurrentCellColor.uiSetting2);
		//grid.setCurrentCellBorder(CurrentCellBorder.uiSetting2);
		//grid.setVertGridLines(false);

		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, UISetting2.getPropertyDescriptors());
		grid.setModel(bindingSource);
		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.uiSetting2, Color.white, HeaderFont.uiSetting2);
		TradingConsole.bindingManager.setGrid(dataSourceKey, 18, Color.black, Color.lightGray, Color.blue, true, true, GridFont.uiSetting2,false, true, true);

		//grid.setRedraw(true);
	}

	public void update(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
		this.setBackground(dataSourceKey, GridBackgroundColor.uiSetting2);
	}

	public void setHide(String dataSourceKey)
	{
		TradingConsole.bindingManager.setHeight(dataSourceKey, this, 0);
	}

	private void setBackground(String dataSourceKey, Color background)
	{
		TradingConsole.bindingManager.setBackground(dataSourceKey, this, background);
	}

	public static void unbind(String dataSourceKey, BindingSource bindingSource)
	{
		if (bindingSource != null)
		{
			TradingConsole.bindingManager.unbind(dataSourceKey, bindingSource);
		}
	}

}
