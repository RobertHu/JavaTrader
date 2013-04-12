package tradingConsole.settings;

import java.awt.Color;
import java.util.Collection;

import framework.Guid;
import framework.data.DataRow;

import tradingConsole.ui.colorHelper.CurrentCellColor;
import tradingConsole.ui.borderStyleHelper.BorderStyle;
import tradingConsole.ui.colorHelper.GridBackColor;
import tradingConsole.ui.columnFixedHelper.RowLabelWidth;
import tradingConsole.ui.colorHelper.GridFixedBackColor;
import tradingConsole.ui.colorHelper.SelectionBackground;
import tradingConsole.ui.borderStyleHelper.CurrentCellBorder;
import tradingConsole.TradingConsole;
import tradingConsole.ui.fontHelper.GridFont;
import tradingConsole.ui.colorHelper.GridFixedForeColor;
import tradingConsole.ui.fontHelper.HeaderFont;
import tradingConsole.framework.PropertyDescriptor;
import tradingConsole.ui.columnKey.RecoverPasswordQuotationSettingGridColKey;
import tradingConsole.ui.language.RecoverPasswordQuotationSettingGridLanguage;
import java.awt.Dimension;
import javax.swing.SwingConstants;
import framework.diagnostics.TraceType;
import tradingConsole.ui.BindingManager;

public class RecoverPasswordAnswer
{
	private static BindingManager _bindingManager = new BindingManager();
	private RecoverPasswordManager _recoverPasswordManager;

	private int _sequence;
	private String _key;
	private Guid _questionId;
	private String _answer;
	private String _questionContent;

	public RecoverPasswordAnswer(RecoverPasswordManager recoverPasswordManager, int sequence)
	{
		this._recoverPasswordManager = recoverPasswordManager;

		this._sequence = sequence;
		this._key = framework.xml.XmlConvert.toString(this._sequence);
		this._questionId = Guid.empty;
		this._answer="";
		this._questionContent="";
	}

	public RecoverPasswordAnswer(RecoverPasswordManager recoverPasswordManager, int sequence, DataRow dataRow)
	{
		this(recoverPasswordManager, sequence);
		this.setValue(dataRow);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}


	private void setValue(DataRow dataRow)
	{
		this._questionId = (Guid)dataRow.get_Item("QuestionId");
		this._answer = (String)dataRow.get_Item("Answer");

		this._questionContent = this.get_QuestionContent();
	}

	public String get_Key()
	{
		return this._key;
	}

	public int get_Sequence()
	{
		return this._sequence;
	}

	public Guid get_QuestionId()
	{
		return this._questionId;
	}

	public RecoverPasswordQuestion getRecoverPasswordQuestion()
	{
		return this._recoverPasswordManager.getRecoverPasswordQuestion(this._questionId);
	}

	public String get_QuestionContent()
	{
		RecoverPasswordQuestion recoverPasswordQuestion = this._recoverPasswordManager.getRecoverPasswordQuestion(this._questionId);
		if (recoverPasswordQuestion!=null)
		{
			return recoverPasswordQuestion.get_Content();
		}
		return "";
	}

	public void set_QuestionContent(String value)
	{
		if (framework.StringHelper.isNullOrEmpty(value))
		{
			this._questionId = Guid.empty;
			this._questionContent = value;
			return;
		}

		RecoverPasswordQuestion recoverPasswordQuestion = this._recoverPasswordManager.getRecoverPasswordQuestion(value);
		if (recoverPasswordQuestion!=null)
		{
			this._questionId = recoverPasswordQuestion.get_Id();
			this._questionContent = value;
		}
	}

	public String get_Answer()
	{
		return this._answer;
	}

	public void set_Answer(String value)
	{
		this._answer = value;
	}

	public boolean isFullData()
	{
		return !(framework.StringHelper.isNullOrEmpty(this._key)
			|| this._questionId.compareTo(Guid.empty) == 0
			|| framework.StringHelper.isNullOrEmpty(this._answer));
	}

	public static PropertyDescriptor[] getPropertyDescriptors()
	{
		PropertyDescriptor[] propertyDescriptors = new PropertyDescriptor[2];
		int index = 0;

		/*PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(RecoverPasswordAnswer.class, RecoverPasswordQuotationSettingGridColKey.Sequence, true, null,
			RecoverPasswordQuotationSettingGridLanguage.Sequence,
			0, PVColumn.COLUMN_LEFT, null);
		propertyDescriptors[index++] = propertyDescriptor;*/

		//propertyDescriptor = PropertyDescriptor.create(RecoverPasswordAnswer.class, RecoverPasswordQuotationSettingGridColKey.QuestionId, true, null,
		//	RecoverPasswordQuotationSettingGridLanguage.QuestionId,
		//	0, PVColumn.COLUMN_LEFT, null);
		//propertyDescriptors[++i] = propertyDescriptor;

		PropertyDescriptor propertyDescriptor = PropertyDescriptor.create(RecoverPasswordAnswer.class, RecoverPasswordQuotationSettingGridColKey.QuestionContent, false, null,
			RecoverPasswordQuotationSettingGridLanguage.QuestionContent, 300, SwingConstants.LEFT, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		propertyDescriptor = PropertyDescriptor.create(RecoverPasswordAnswer.class, RecoverPasswordQuotationSettingGridColKey.Answer, false, null,
			RecoverPasswordQuotationSettingGridLanguage.Answer,	300,SwingConstants.LEFT, null, null);
		propertyDescriptors[index++] = propertyDescriptor;

		TradingConsole.traceSource.trace(TraceType.Information, "propertyDescriptors[0] = " + (propertyDescriptors[0] == null ? "null" : propertyDescriptors[0].get_Name())
			+ "propertyDescriptors[1] = " + (propertyDescriptors[1] == null ? "null" : propertyDescriptors[1].get_Name()));
		return propertyDescriptors;
	}

	public static void initialize(tradingConsole.ui.grid.DataGrid grid, String dataSourceKey, Collection dataSource, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		grid.setIntercellSpacing(new Dimension(1,1));
		//grid.setSelectionBackground(SelectionBackground.account);

		TradingConsole.traceSource.trace(TraceType.Information, "dataSourceKey = " + (dataSourceKey == null ? "null" : dataSourceKey)
			+ "; dataSource = " + (dataSource == null ? "null" : dataSource)
			+ "; bindingSource = " + (bindingSource == null ? "null" : bindingSource));
		RecoverPasswordAnswer._bindingManager.bind(dataSourceKey, dataSource, bindingSource, RecoverPasswordAnswer.getPropertyDescriptors());
		grid.setModel(bindingSource);
		for(int row = 0; row < bindingSource.getRowCount(); row++)
		{
			bindingSource.setBackground(bindingSource.getObject(row), Color.WHITE);
		}
		RecoverPasswordAnswer._bindingManager.setHeader(dataSourceKey, SwingConstants.CENTER, 25, GridFixedForeColor.account, Color.white, HeaderFont.account);
		RecoverPasswordAnswer._bindingManager.setGrid(dataSourceKey, 31, Color.black, Color.white, Color.blue, true, true, GridFont.account,false, true, false);
	}

	public static void unbind(String dataSourceKey, tradingConsole.ui.grid.BindingSource bindingSource)
	{
		RecoverPasswordAnswer._bindingManager.unbind(dataSourceKey, bindingSource);
	}

	public void add(String dataSourceKey)
	{
		RecoverPasswordAnswer._bindingManager.add(dataSourceKey, this);
	}

	public void update(String dataSourceKey)
	{
		RecoverPasswordAnswer._bindingManager.update(dataSourceKey, this);
	}
}
