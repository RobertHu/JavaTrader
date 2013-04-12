package tradingConsole.ui.grid;

import java.io.*;
import java.util.*;

import javax.swing.table.*;

import framework.*;
import tradingConsole.framework.*;
import java.awt.Font;
import tradingConsole.ui.GridNames;

public class ColumnUIInfoManager
{
	private static final String _version = "1.0.0";
	private static final String _versionTag = "VersionOfColumnUIInfo By OmniCare System=";
	private static final String _seprator = "|";
	private static HashMap<String, ColumnUIInfo[]> _columnUIInfos = new HashMap<String, ColumnUIInfo[]>();
	private static HashMap<String, Font> _fonts = new HashMap<String, Font>();
	private static ArrayList<DataGrid> _grids = new ArrayList<DataGrid>();
	private static boolean _isLoaded = false;

	public static boolean get_IsLoaded()
	{
		return ColumnUIInfoManager._isLoaded;
	}

	public static void clear()
	{
		ColumnUIInfoManager._columnUIInfos.clear();
		ColumnUIInfoManager._fonts.clear();
		ColumnUIInfoManager._grids.clear();;
	}

	public static void load(String fullFilePath) throws FileNotFoundException, IOException
	{
		if(new File(fullFilePath).exists())
		{
			FileReader fileReader = null;
			BufferedReader reader = null;
			try
			{
				fileReader = new FileReader(fullFilePath);
				reader = new BufferedReader(fileReader);

				String versionInfo = reader.readLine();
				if(!versionInfo.startsWith(ColumnUIInfoManager._versionTag))
				{
					return;
				}
				else
				{
					String version = versionInfo.substring(ColumnUIInfoManager._versionTag.length());
					if(!version.equalsIgnoreCase(ColumnUIInfoManager._version))
					{
						return;
					}
				}

				String gridName = reader.readLine();
				if (gridName != null)
				{
					String hidedColumns = reader.readLine();
					while (true)
					{
						String[] columnInfoArray = StringHelper.split(hidedColumns, ColumnUIInfoManager._seprator);

						String fontInfo = columnInfoArray[0];
						Font font = FontHelper.parse(fontInfo);
						if (font != null)
						{
							ColumnUIInfoManager._fonts.put(gridName, font);
						}

						ColumnUIInfo[] columnUIInfos = new ColumnUIInfo[columnInfoArray.length - 1];
						for (int index = 1; index < columnInfoArray.length; index++)
						{
							columnUIInfos[index - 1] = ColumnUIInfo.parse(columnInfoArray[index]);
						}
						ColumnUIInfoManager._columnUIInfos.put(gridName, columnUIInfos);

						gridName = reader.readLine();
						if (gridName == null)
							break;
						hidedColumns = reader.readLine();
					}
				}
				ColumnUIInfoManager._isLoaded = true;
			}
			finally
			{
				reader.close();
				fileReader.close();
			}
		}
	}

	public static void save(String fullFilePath) throws FileNotFoundException, IOException
	{
		PrintWriter writer = new PrintWriter(fullFilePath);
		StringBuilder columnUIInfos = new StringBuilder();
		writer.println(ColumnUIInfoManager._versionTag + ColumnUIInfoManager._version);
		for(DataGrid grid : ColumnUIInfoManager._grids)
		{
			if(grid.get_BindingSource() == null) continue;

			Font font = grid.get_BindingSource().get_Font();
			columnUIInfos.append(font == null ? "null" : FontHelper.toString(font));

			for(PropertyDescriptor propertyDescriptor : grid.get_BindingSource().getPropertyDescriptors())
			{
				String columnName = propertyDescriptor.get_Name();
				int columnIndex = grid.get_BindingSource().getColumnByName(columnName);
				ColumnUIInfo columnUIInfo = new ColumnUIInfo(columnName);

				boolean isChoosed = TableColumnChooser.isVisibleColumn(grid.getColumnModel(), columnIndex);
				columnUIInfo.set_IsChoosed(isChoosed);
				if(isChoosed)
				{
					int viewIndex = grid.convertColumnIndexToView(columnIndex);
					TableColumn tableColumn = grid.getColumnModel().getColumn(viewIndex);
					columnUIInfo.set_Width(tableColumn.getWidth());
					columnUIInfo.set_Sequence(viewIndex);
				}
				else
				{
					ColumnUIInfo oldColumnUIInfo = ColumnUIInfoManager.getColumnUIInfo(grid.get_Name(), columnName);
					if(oldColumnUIInfo != null)
					{
						columnUIInfo.set_Sequence(oldColumnUIInfo.get_Sequence());
						columnUIInfo.set_Width(oldColumnUIInfo.get_Width());
					}
				}
				if (columnUIInfos.length() > 0)
				{
					columnUIInfos.append(ColumnUIInfoManager._seprator);
				}
				columnUIInfos.append(columnUIInfo.toString());
			}

			if(columnUIInfos.length() > 0)
			{
				writer.println(grid.get_Name());
				writer.println(columnUIInfos.toString());
				columnUIInfos.delete(0, columnUIInfos.length());
			}
		}
		writer.close();
	}

	public static void hideAllNotChoosedColumns(DataGrid grid)
	{
		String gridName = grid.get_Name();
		if(ColumnUIInfoManager._columnUIInfos.containsKey(gridName))
		{
			ColumnUIInfo[] columnUIInfos = ColumnUIInfoManager._columnUIInfos.get(gridName);
			for (ColumnUIInfo columnUIInfo : columnUIInfos)
			{
				int modelIndex = grid.get_BindingSource().getColumnByName(columnUIInfo.get_Name());
				if(modelIndex != -1 && !columnUIInfo.get_IsChoosed())
				{
					TableColumnChooser.hideColumn(grid, modelIndex);
				}
			}
		}
	}

	public static PropertyDescriptor[] applyUIInfo(String gridName, PropertyDescriptor[] propertyDescriptors)
	{
		if(!ColumnUIInfoManager._columnUIInfos.containsKey(gridName))
		{
			return propertyDescriptors;
		}
		else
		{
			ArrayList<PropertyDescriptor> notAddedProperties = new ArrayList<PropertyDescriptor>();
			PropertyDescriptor[] sortedPropertyDescriptors = new PropertyDescriptor[propertyDescriptors.length];
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
			{
				ColumnUIInfo columnUIInfo = ColumnUIInfoManager.getColumnUIInfo(gridName, propertyDescriptor.get_Name());
				if(columnUIInfo == null || columnUIInfo.get_Sequence() == -1)
				{
					notAddedProperties.add(propertyDescriptor);
				}
				else
				{
					int width = columnUIInfo.get_Width() == -1? propertyDescriptor.get_Width() : columnUIInfo.get_Width();
					boolean isDynamic = propertyDescriptor.get_IsDynamic();
					String name = isDynamic ? propertyDescriptor.get_DynamicName() : propertyDescriptor.get_Name();
					PropertyDescriptor newPropertyDescriptor = PropertyDescriptor.create(propertyDescriptor.get_OwnerType(),
						name, propertyDescriptor.get_IsReadonly(), propertyDescriptor.get_Editor(),
						propertyDescriptor.get_Caption(), width, propertyDescriptor.get_Alignment(), propertyDescriptor.get_Font(),
						propertyDescriptor.get_Background(), propertyDescriptor.get_CellEditor(), propertyDescriptor.get_CellRenderer(), isDynamic);
					if(columnUIInfo.get_Sequence() < propertyDescriptors.length)
					{
						sortedPropertyDescriptors[columnUIInfo.get_Sequence()] = newPropertyDescriptor;
					}
					else
					{
						return propertyDescriptors;
					}
				}
			}

			for (int index = 0; index < sortedPropertyDescriptors.length; index++)
			{
				if(sortedPropertyDescriptors[index] == null)
				{
					if(notAddedProperties.size() > 0)
					{
						sortedPropertyDescriptors[index] = notAddedProperties.get(0);
						notAddedProperties.remove(0);
					}
					else
					{
						return propertyDescriptors;
					}
				}
			}

			return sortedPropertyDescriptors;
		}
	}

	public static void enableColumnUIPersistent(DataGrid grid)
	{
		if(!ColumnUIInfoManager._grids.contains(grid))
		{
			ColumnUIInfoManager._grids.add(grid);
		}
	}

	public static void disableColumnUIPersistent(DataGrid grid)
	{
		if(ColumnUIInfoManager._grids.contains(grid))
		{
			ColumnUIInfoManager._grids.remove(grid);
		}
	}

	private static ColumnUIInfo getColumnUIInfo(String gridName, String columnName)
	{
		ColumnUIInfo[] columnUIInfos = ColumnUIInfoManager._columnUIInfos.get(gridName);
		if(columnUIInfos != null)
		{
			for (ColumnUIInfo columnUIInfo : columnUIInfos)
			{
				if (columnUIInfo.get_Name().equalsIgnoreCase(columnName))
				{
					return columnUIInfo;
				}
			}
		}
		return null;
	}

	public static Font getFont(String name)
	{
		if(ColumnUIInfoManager._fonts.containsKey(name))
		{
			return ColumnUIInfoManager._fonts.get(name);
		}
		else
		{
			return null;
		}
	}
}

class FontHelper
{
	private static final String _seprator = ":";
	public static String toString(Font font)
	{
		return font.getName() + FontHelper._seprator + font.getStyle() + FontHelper._seprator + font.getSize();
	}

	public static Font parse(String value)
	{
		if(StringHelper.isNullOrEmpty(value)) return null;
		String[] values = StringHelper.split(value, FontHelper._seprator);
		if(values.length != 3) return null;
		return new Font(values[0], Integer.parseInt(values[1]), Integer.parseInt(values[2]));
	}
}
