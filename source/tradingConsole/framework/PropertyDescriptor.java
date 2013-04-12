package tradingConsole.framework;

import java.lang.reflect.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

import framework.*;
import tradingConsole.ui.fontHelper.*;

public class PropertyDescriptor
{
	private static final Font _defaultFont = GridFont.defaultFont;
	private static final int _defaultAlignment = SwingConstants.CENTER; //Table.CENTER_ALIGNMENT

	private Class _ownerType;
	private String _name;
	private boolean _isReadonly;
	private Component _editor;
	private String _caption;
	private int _width;
	private int _alignment;
	private Font _font;
	private Color _background;
	private boolean _isDynamic;
	private String _dynamicName;
	private TableCellEditor _tableCellEditor;
	private TableCellRenderer _tableCellRenderer;

	private Method _getMethod;
	private Method _setMethod;

	private static PropertyDescriptor getProperty(Class objectType, String name, boolean isDynamic)
	{
		HashMap<String, PropertyDescriptor> propertyDescriptors = TypeHelper.getProperties(objectType);
		if(isDynamic)
		{
			name = "DynamicValue";
		}
		for (Iterator<PropertyDescriptor> iterator = propertyDescriptors.values().iterator(); iterator.hasNext(); )
		{
			PropertyDescriptor property = iterator.next();
			if (property.get_Name().equals(name))
			{
				return property;
			}
		}
		return null;
	}

	public static PropertyDescriptor create(Class objectType, String name, boolean isReadonly,
											Component editor, String caption, int width, int alignment, Font font, Color background)
	{
		return PropertyDescriptor.create(objectType, name, isReadonly, editor, caption, width, alignment, font, background, null, null);
	}

	public static PropertyDescriptor create(Class objectType, String name, boolean isReadonly,
											Component editor, String caption, int width, int alignment, Font font, Color background, boolean isDynamic)
	{
		return PropertyDescriptor.create(objectType, name, isReadonly, editor, caption, width, alignment, font, background, null, null, isDynamic);
	}


	public static PropertyDescriptor create(Class objectType, String name, boolean isReadonly,
											Component editor, String caption, int width, int alignment, Font font, Color background,
											TableCellEditor tableCellEditor, TableCellRenderer tableCellRenderer)
	{
		return PropertyDescriptor.create(objectType, name, isReadonly, editor, caption, width, alignment, font, background, tableCellEditor, tableCellRenderer, false);
	}

	public static PropertyDescriptor create(Class objectType, String name, boolean isReadonly,
											Component editor, String caption, int width, int alignment, Font font, Color background,
											TableCellEditor tableCellEditor, TableCellRenderer tableCellRenderer, boolean isDynamic)
	{
		PropertyDescriptor property2 = PropertyDescriptor.getProperty(objectType, name, isDynamic);
		if (property2 == null)
		{
			throw new FrameworkException(new IllegalArgumentException());
		}

		PropertyDescriptor property = new PropertyDescriptor(property2);

		property._ownerType = objectType;
		property._isReadonly = isReadonly;
		property._editor = editor;
		property._caption = (caption == null) ? name : caption;
		property._width = width;
		property._alignment = (alignment == -1) ? _defaultAlignment : alignment;
		property._font = (font == null) ? PropertyDescriptor._defaultFont : font;
		property._tableCellEditor = tableCellEditor;
		property._tableCellRenderer = tableCellRenderer;
		property._background = background;
		property._isDynamic = isDynamic;
		property._dynamicName = name;

		return property;
	}

	PropertyDescriptor(PropertyDescriptor property)
	{
		this.update(property);

		this._getMethod = property._getMethod;
		this._setMethod = property._setMethod;
	}

	PropertyDescriptor(Class objectType, String name, Method getMethod, Method setMethod)
	{
		this._ownerType = objectType;
		this._name = name;
		this._getMethod = getMethod;
		this._setMethod = setMethod;
	}

	public void update(PropertyDescriptor property)
	{
		this._ownerType = property._ownerType;
		this._name = property._name;
		this._isReadonly = property._isReadonly;
		this._editor = property._editor;
		this._caption = property._caption;
		this._width = property._width;
		this._alignment = property._alignment;
		this._font = property._font;

		this._tableCellEditor = property._tableCellEditor;
		this._tableCellRenderer = property._tableCellRenderer;
	}

	public Class get_OwnerType()
	{
		return this._ownerType;
	}

	public String get_DynamicName()
	{
		return this._dynamicName;
	}

	public String get_Name()
	{
		return this._name;
	}

	public String get_Caption()
	{
		return this._caption;
	}

	public Component get_Editor()
	{
		return this._editor;
	}

	public int get_Width()
	{
		return this._width;
	}

	public int get_HeadingAlignment()
	{
		return PropertyDescriptor._defaultAlignment;
	}

	public int get_Alignment()
	{
		return this._alignment;
	}

	public Font get_Font()
	{
		return this._font;
	}

	public Class get_ValueType()
	{
		return this._getMethod.getReturnType();
	}

	public boolean get_IsDynamic()
	{
		return this._isDynamic;
	}

	public boolean get_IsReadonly()
	{
		return this._isReadonly || this._setMethod == null;
	}

	public TableCellEditor get_CellEditor()
	{
		return this._tableCellEditor;
	}

	public TableCellRenderer get_CellRenderer()
	{
		return this._tableCellRenderer;
	}

	public Object getValue(Object object)
	{
		try
		{
			return this._isDynamic ? this._getMethod.invoke(object, new Object[]{this._dynamicName}) : this._getMethod.invoke(object, null);
		}
		catch (Exception exception)
		{
			throw new FrameworkException(exception);
		}
	}

	public void setValue(Object object, Object propertyValue)
	{
		try
		{
			if(this._isDynamic)
			{
				this._setMethod.invoke(object, new Object[]
									   {this._dynamicName, propertyValue});
			}
			else
			{
				this._setMethod.invoke(object, new Object[]
									   {propertyValue});
			}
		}
		catch (Exception exception)
		{
			throw new FrameworkException(exception);
		}
	}

	Method get_GetMethod()
	{
		return this._getMethod;
	}

	void set_GetMethod(Method value)
	{
		this._getMethod = value;
	}

	Method get_setMethod()
	{
		return this._setMethod;
	}

	void set_SetMethod(Method value)
	{
		this._setMethod = value;
	}

	public Color get_Background()
	{
		return this._background;
	}
}

class TypeHelper
{
	private static HashMap<Class, HashMap<String, PropertyDescriptor>> _cache = new HashMap<Class, HashMap<String, PropertyDescriptor>> ();
	public static synchronized HashMap<String, PropertyDescriptor> getProperties(Class type)
	{
		if (!TypeHelper._cache.containsKey(type))
		{
			HashMap<String, PropertyDescriptor> hashtable = new HashMap<String,
				PropertyDescriptor> ();

			Method[] methods = type.getMethods();
			for (int i = 0; i < methods.length; i++)
			{
				Method method = methods[i];
				String methodName = method.getName();
				if ((methodName.startsWith("get_") && method.getParameterTypes().length == 0)
					|| methodName.equalsIgnoreCase("get_DynamicValue"))
				{
					String propertyName = methodName.substring(4);
					PropertyDescriptor property = new PropertyDescriptor(type, propertyName,
						method, null);
					hashtable.put(propertyName, property);
				}
			}

			for (int i = 0; i < methods.length; i++)
			{
				Method method = methods[i];
				String methodName = method.getName();
				Class[] parameterTypes = method.getParameterTypes();
				if ((methodName.startsWith("set_") && parameterTypes.length == 1)
					|| methodName.equalsIgnoreCase("set_DynamicValue"))
				{
					String propertyName = methodName.substring(4);
					if (hashtable.containsKey(propertyName))
					{
						PropertyDescriptor property = hashtable.get(propertyName);
						if (property.get_GetMethod().getReturnType() == parameterTypes[0])
						{
							property.set_SetMethod(method);
						}
					}
				}
			}
			TypeHelper._cache.put(type, hashtable);
		}
		return TypeHelper._cache.get(type);
	}

}
