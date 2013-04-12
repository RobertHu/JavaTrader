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
import tradingConsole.ui.columnKey.NewsColKey;
import tradingConsole.ui.language.NewsLanguage;
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

public class News
{
	public static final String newsPanelKey = "NewsPanelKey";

	private Guid _id;
	private String _title;
	private String _content;
	private DateTime _publishTime;
	private String _author;

	static
	{
		com.jidesoft.utils.Lm.verifyLicense("Omnicare System Limited", "iTrader","TEzuZ3nWadgaTf8Lf6BvmJSbwyBlhFD2");
	}

	//For News
	public News(Guid id,String title,String content,DateTime publishTime,String author)
	{
		this._id = id;
		this._title = title;
		this._content = content;
		this._publishTime = publishTime;
		this._author = author;
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
		if (framework.StringHelper.isNullOrEmpty(this._author))
		{
			return "[" + this.get_PublishTimeString() + "] " + this._title;
		}
		else
		{
			return "[" + this.get_PublishTimeString() + "][" + this._author + "] " + this._title;
		}
	}

	public String get_Content()
	{
		return this._content;
	}
	public void set_Content(String value)
	{
		this._content = value;
	}

	public DateTime get_PublishTime()
	{
		return this._publishTime;
	}

	public String get_PublishTimeString()
	{
		return Convert.toString(this._publishTime, "yyyy-MM-dd HH:mm:ss");
	}

	public String get_Author()
	{
		return this._author;
	}

	public News()
	{
	}

	public News(DataRow dataRow)
	{
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
		this._author = AppToolkit.isDBNull(dataRow.get_Item("Author")) ? "":(String) dataRow.get_Item("Author");
		//this._content = AppToolkit.isDBNull(dataRow.get_Item("Contents")) ? null : (String) dataRow.get_Item("Contents");
		this._publishTime = (DateTime) dataRow.get_Item("PublishTime");
	}

	public void setValue(XmlAttributeCollection accountCollection)
	{
		for (int i = 0; i < accountCollection.get_Count(); i++)
		{
			String nodeName = accountCollection.get_ItemOf(i).get_LocalName();
			String nodeValue = accountCollection.get_ItemOf(i).get_Value();
			if (nodeName.equals("Id"))
			{
				this._id = new Guid(nodeValue);
			}
			else if (nodeName.equals("Title"))
			{
				this._title = nodeValue;
			}
			else if (nodeName.equals("Contents"))
			{
				this._content = nodeValue;
			}
			else if (nodeName.equals("PublishTime"))
			{
				this._publishTime = AppToolkit.getDateTime(nodeValue);
			}
			else if (nodeName.equals("Author"))
			{
				this._author = nodeValue;
			}
		}
	}

	public static void updateProperties(String dataSourceKey,DockableFrame dockableFrame)
	{
		TradingConsole.bindingManager.updateProperties(dataSourceKey,News.getPropertyDescriptors(dockableFrame));
	}

	private static PropertyDescriptor[] getPropertyDescriptors(DockableFrame dockableFrame)
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[3];
		int i = -1;

		int width = (dockableFrame==null)?865:dockableFrame.getWidth() - 3;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(News.class, NewsColKey.Title,
			true, null, NewsLanguage.Title, UISettingsManager.getWidth(width,0.7,400), SwingConstants.LEFT, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(News.class, NewsColKey.Author,
								true, null, NewsLanguage.Author, UISettingsManager.getWidth(width,0.15,120), SwingConstants.CENTER, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(News.class, NewsColKey.PublishTimeString,
					true, null, NewsLanguage.PublishTimeString, UISettingsManager.getWidth(width,0.15,145),	SwingConstants.CENTER, null, null);
		propertyDescriptors[++i] = propertyDescriptor;

		return propertyDescriptors;
	}

	public static void initialize(DockableFrame dockableFrame, tradingConsole.ui.grid.DockableTable grid, String dataSourceKey, Collection dataSource, tradingConsole.ui.grid.BindingSource bindingSource)
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

		TradingConsole.bindingManager.bind(dataSourceKey, dataSource, bindingSource, News.getPropertyDescriptors(dockableFrame));
		grid.setDataModel(bindingSource);

		TradingConsole.bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.message, Color.white, HeaderFont.message);
		TradingConsole.bindingManager.setGrid(dataSourceKey, 18, ColorSettings.useBlackAsBackground ? ColorSettings.GridForeground : Color.black,
											  ColorSettings.useBlackAsBackground ? ColorSettings.TradingListGridBackground : Color.white, Color.blue, true, true, GridFont.message,false, true, true);
	}

	public static void rebind(DockableFrame dockableFrame,tradingConsole.ui.grid.DockableTable grid, String dataSourceKey, Collection dataSource,
										  tradingConsole.ui.grid.BindingSource bindingSource)
	{
		News.initialize(dockableFrame,grid, dataSourceKey, dataSource, bindingSource);
		for (int i = 0, count = bindingSource.getRowCount(); i < count; i++)
		{
			News news = (News)bindingSource.getObject(i);
			news.update(dataSourceKey);
		}
	}

	public void add(String dataSourceKey)
	{
		TradingConsole.bindingManager.add(dataSourceKey, this);
		this.setBackground(dataSourceKey, GridBackgroundColor.message);
	}

	public void update(String dataSourceKey)
	{
		TradingConsole.bindingManager.update(dataSourceKey, this);
		//this.setBackground(dataSourceKey, GridBackgroundColor.message);
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
