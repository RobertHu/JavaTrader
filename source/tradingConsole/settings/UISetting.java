package tradingConsole.settings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.io.File;

import framework.xml.XmlDocument;
import framework.xml.XmlNode;
import framework.xml.XmlAttributeCollection;
import framework.xml.XmlConvert;
import framework.awt.FontHelper;

import tradingConsole.ui.language.Language;
//import tradingConsole.AppToolkit;
import java.io.*;
import framework.diagnostics.TraceType;
import tradingConsole.TradingConsole;
import javax.swing.JComboBox;

public class UISetting
{
	public static final String accountStatusUiSetting = "AccountStatusUiSetting";
	public static final String tradingPanelUiSetting = "TradingPanelUiSetting";
	public static final String workingOrderListUiSetting = "WorkingOrderListUiSetting";
	public static final String openOrderListUiSetting = "OpenOrderListUiSetting";
	//public static final String frameset = "Frameset";

	public static final String fontSeparator = "----------------------------------------------";

	private String _objectId;
	private String _parameter;
	private String _fontName;
	private int _fontSize;
	private int _rowHeight;
	private HashMap<String, UISetting2> _uiSetting2s;

	public String get_ObjectId()
	{
		return this._objectId;
	}

	public String get_Parameter()
	{
		this.setParameter();
		return this._parameter;
	}

	public void set_Parameter(String value)
	{
		this._parameter = value;
	}

	public HashMap<String, UISetting2> get_UiSetting2s()
	{
		return this._uiSetting2s;
	}

	public UISetting2 get_UiSetting2(String colKey)
	{
		if (this._uiSetting2s.containsKey(colKey))
		{
			return this._uiSetting2s.get(colKey);
		}
		else
		{
			return null;
		}
	}

	public String get_FontName()
	{
		return this._fontName;
	}

	public void set_FontName(String value)
	{
		this._fontName = value;
	}

	public int get_FontSize()
	{
		return this._fontSize;
	}

	public void set_FontSize(int value)
	{
		this._fontSize = value;
	}

	public int get_RowHeight()
	{
		return this._rowHeight;
	}

	public void set_RowHeight(int value)
	{
		this._rowHeight = value;
	}

	public UISetting(String objectId, String parameter)
	{
		this._uiSetting2s = new HashMap<String, UISetting2> ();
		this._objectId = objectId;
		this._parameter = parameter;
		this.setValue2();
	}

	public void setValue2()
	{
		XmlDocument uiSetting2XmlDocument = new XmlDocument();
		uiSetting2XmlDocument.loadXml(this._parameter);
		XmlNode uiSetting2XmlNode = uiSetting2XmlDocument.get_DocumentElement();
		XmlAttributeCollection uiSetting2XmlAttributeCollection = uiSetting2XmlNode.
			get_Attributes();
		for (int i = 0; i < uiSetting2XmlAttributeCollection.get_Count(); i++)
		{
			String nodeName = uiSetting2XmlAttributeCollection.get_ItemOf(i).
				get_LocalName();
			String nodeValue = uiSetting2XmlAttributeCollection.get_ItemOf(i).
				get_Value();
			if (nodeName.equals("FontName"))
			{
				this._fontName = nodeValue;
			}
			else if (nodeName.equals("FontSize"))
			{
				this._fontSize = Integer.parseInt(nodeValue);
			}
			else if (nodeName.equals("RowHeight"))
			{
				this._rowHeight = Integer.parseInt(nodeValue);
			}
		}

		uiSetting2XmlNode = uiSetting2XmlDocument.get_DocumentElement().get_Item(
			"Cols");
		for (int i = 0; i < uiSetting2XmlNode.get_ChildNodes().get_Count(); i++)
		{
			XmlNode uiSetting2XmlNode2 = uiSetting2XmlNode.get_ChildNodes().itemOf(i);
			uiSetting2XmlAttributeCollection = uiSetting2XmlNode2.get_Attributes();
			UISetting2 uiSetting2 = new UISetting2(this,
				uiSetting2XmlAttributeCollection);
			if (!this._uiSetting2s.containsKey(uiSetting2.get_ColKey()))
			{
				this._uiSetting2s.put(uiSetting2.get_ColKey(), uiSetting2);
			}
		}
	}

	private void setParameter()
	{
		String xml = "<Grid ";
		xml += "FontName=\"" + this._fontName + "\" ";
		xml += "FontSize=\"" + XmlConvert.toString(this._fontSize) + "\" ";
		xml += "RowHeight=\"" + XmlConvert.toString(this._rowHeight) + "\" ";
		xml += ">";
		xml += "<Cols>";
		for (Iterator<UISetting2> iterator = this._uiSetting2s.values().iterator();
			 iterator.hasNext(); )
		{
			UISetting2 uiSetting2 = iterator.next();
			xml += uiSetting2.getValue();
		}
		xml += "</Cols>";
		xml += "</Grid>";

		this._parameter = xml;
	}

	private static void fillSelect(JComboBox control, Object text, Object value)
	{
		control.addItem(text);
	}

	public static void fillUnitGridCountPerUnitRow(JComboBox control)
	{
		control.removeAllItems();
		for (int i = 4; i < 16; i++)
		{
			UISetting.fillSelect(control, i, i);
		}
	}

	public static void fillFontName(JComboBox control)
	{
		control.removeAllItems();

		String[] allfonts = FontHelper.get_LogicFonts();
		int i;
		for (i = 0; i < allfonts.length; i++)
		{
			UISetting.fillSelect(control, allfonts[i], allfonts[i]);
		}

		UISetting.fillSelect(control, UISetting.fontSeparator, UISetting.fontSeparator);

		allfonts = FontHelper.get_PlatformFonts();
		for (i = 0; i < allfonts.length; i++)
		{
			UISetting.fillSelect(control, allfonts[i], allfonts[i]);
		}
	}

   public static void fillMultiLanguage(JComboBox control)
   {
	   control.removeAllItems();

	   try
	   {
		   String[] availableLanguages = Language.getAvailableLanguages("Language");
		   for (int i = 0, count = availableLanguages.length; i < count; i++)
		   {
			   String filePath = availableLanguages[i];
			   UISetting.fillSelect(control, filePath, filePath);
		   }
	   }
	   catch (IOException iOException)
	   {
		   TradingConsole.traceSource.trace(TraceType.Error, iOException);
	   }

	   //control.sort(true);
   }

	public static void fillFontSize(JComboBox control)
	{
		control.removeAllItems();
		int i;
		for (i = 3; i < 25; i++)
		{
			UISetting.fillSelect(control, i, i);
		}
		i = 30;
		UISetting.fillSelect(control, i, i);
		i = 36;
		UISetting.fillSelect(control, i, i);
		i = 48;
		UISetting.fillSelect(control, i, i);
		i = 72;
		UISetting.fillSelect(control, i, i);
	}

	public static void fillHeight(JComboBox control)
	{
		control.removeAllItems();
		for (int i = 10; i <= 55; i += 5)
		{
			UISetting.fillSelect(control, i, i);
		}
	}

	public static void fillGrid(JComboBox control)
	{
		control.removeAllItems();
		//UISetting.fillSelect(control, Language.SettingAccountGrid, UISetting.accountStatusUiSetting);
		UISetting.fillSelect(control, Language.SettingInstrumentGrid,
							 UISetting.tradingPanelUiSetting);
		UISetting.fillSelect(control, Language.SettingOrderGrid,
							 UISetting.workingOrderListUiSetting);
		UISetting.fillSelect(control, Language.SettingOpenOrderGrid,
							 UISetting.openOrderListUiSetting);
	}

}
