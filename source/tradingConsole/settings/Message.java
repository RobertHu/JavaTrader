package tradingConsole.settings;

import java.awt.Color;
import java.util.Collection;

import framework.DateTime;
import framework.Guid;
import framework.Convert;
import framework.data.DataRow;
import framework.xml.XmlAttributeCollection;

import tradingConsole.TradingConsole;
import tradingConsole.AppToolkit;
import tradingConsole.framework.PropertyDescriptor;
import tradingConsole.ui.columnKey.MessageColKey;
import tradingConsole.ui.language.MessageLanguage;
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
import com.jidesoft.docking.DockableFrame;
import javax.swing.SwingConstants;
import tradingConsole.ui.ColorSettings;
import javax.swing.event.TableModelEvent;

public class Message
{
	public static final String messagePanelKey = "MessagePanelKey";

	private Guid _id;
	private String _title;
	private String _content;
	private DateTime _publishTime;
	private boolean _isSelected;

	static
	{
	com.jidesoft.utils.Lm.verifyLicense("Omnicare System Limited", "iTrader","TEzuZ3nWadgaTf8Lf6BvmJSbwyBlhFD2");
	}

	public Guid get_Id()
	{
		return this._id;
	}

	public String get_Title()
	{
		return this._title;
	}

	public String get_Title2()
	{
		return "[" + this.get_PublishTimeString() + "] " + this._title;
	}

	public String get_Content()
	{
		return this._content;
	}

	public DateTime get_PublishTime()
	{
		return this._publishTime;
	}

	public String get_PublishTimeString()
	{
		return Convert.toString(this._publishTime, "yyyy-MM-dd HH:mm:ss");
	}

	public boolean get_IsSelected()
	{
		return this._isSelected;
	}

	public void set_IsSelected(boolean value)
	{
		this._isSelected = value;
	}

	public Message()
	{
		this._isSelected = false;
	}

	public Message(DataRow dataRow)
	{
		this();

		this._id = (Guid) dataRow.get_Item("ID");
		this.setValue(dataRow);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		this._title = (String) dataRow.get_Item("Title");
		this._content = AppToolkit.isDBNull(dataRow.get_Item("Content")) ? null : (String) dataRow.get_Item("Content");
		this._publishTime = (DateTime) dataRow.get_Item("PublishTime");
	}

	public void setValue(XmlAttributeCollection accountCollection)
	{
		for (int i = 0; i < accountCollection.get_Count(); i++)
		{
			String nodeName = accountCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = accountCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("ID"))
			{
				this._id = new Guid(nodeValue);
			}
			else if (nodeName.equals("Title"))
			{
				this._title = nodeValue;
			}
			else if (nodeName.equals("Content"))
			{
				this._content = nodeValue;
			}
			else if (nodeName.equals("PublishTime"))
			{
				this._publishTime = AppToolkit.getDateTime(nodeValue);
			}
		}
	}

	public static void updateProperties(String dataSourceKey,DockableFrame dockableFrame)
	{
		TradingConsole.bindingManager.updateProperties(dataSourceKey,Message.getPropertyDescriptors(dockableFrame));
	}

	public static PropertyDescriptor[] getPropertyDescriptors(DockableFrame dockableFrame)
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[2];
		int i = -1;

		int width = (dockableFrame==null)?865:dockableFrame.getWidth() - 3;

		/*PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(Message.class, MessageColKey.IsSelected,
			false, null, MessageLanguage.IsSelected, 0,
			PVColumn.COLUMN_CENTER, null);
		propertyDescriptors[++i] = propertyDescriptor;*/

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(Message.class, MessageColKey.PublishTimeString,
			true, null, MessageLanguage.PublishTimeString, UISettingsManager.getWidth(width, 0.20, 163), SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(Message.class, MessageColKey.Title,
			true, null, MessageLanguage.Title2, UISettingsManager.getWidth(width,0.80,702),	SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		return propertyDescriptors;
	}

	public static void initialize(DockableFrame dockableFrame,tradingConsole.ui.grid.DockableTable grid, String dataSourceKey, Collection dataSource, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		grid.reset();
		grid.setVertGridLines(false);
		grid.setHorzGridLines(false);
		grid.setBackColor(GridFixedBackColor.message);
		grid.setTableColor(GridBackColor.message);
		grid.setBorderStyle(BorderStyle.message);
		grid.setRowLabelWidth(RowLabelWidth.message);
		//grid.setSelectionBackground(SelectionBackground.message);
		grid.setCurrentCellColor(CurrentCellColor.message);
		grid.setCurrentCellBorder(CurrentCellBorder.message);

		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, Message.getPropertyDescriptors(dockableFrame));
		grid.setDataModel(bindingSource);

		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.message, Color.white, HeaderFont.message);
		TradingConsole.bindingManager.setGrid(dataSourceKey, 18, ColorSettings.useBlackAsBackground ? ColorSettings.GridForeground : Color.black,
											  ColorSettings.useBlackAsBackground ? ColorSettings.TradingListGridBackground : Color.white, Color.blue, true, true, GridFont.message, false, true, true);
	}

	public static void rebind(DockableFrame dockableFrame, tradingConsole.ui.grid.DockableTable grid, String dataSourceKey, Collection dataSource,
										  tradingConsole.ui.grid.BindingSource bindingSource)
	{
		Message.initialize(dockableFrame, grid, dataSourceKey, dataSource, bindingSource);
		for (int i = 0, count = bindingSource.getRowCount(); i < count; i++)
		{
			Message message = (Message)bindingSource.getObject(i);
			message.update(dataSourceKey);
		}
	}

	public void update(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
		/*if(ColorSettings.Enabled)
		{
			this.setBackground(dataSourceKey, ColorSettings.MessageGridBackground);
		}
		else
		{
			this.setBackground(dataSourceKey, Color.WHITE);
		}*/
	}

	private void setBackground(String dataSourceKey, Color background)
	{
		TradingConsole.bindingManager.setBackground(dataSourceKey, this, background);
	}

	public static void unbind(String dataSourceKey, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		if (bindingSource != null)
		{
			TradingConsole.bindingManager.unbind(dataSourceKey, bindingSource);
		}
	}
}
