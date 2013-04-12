package tradingConsole.settings;

import java.util.Iterator;
import java.util.HashMap;

import framework.Guid;
import framework.data.DataSet;
import framework.data.DataTable;
import framework.data.DataTableCollection;
import framework.data.DataRow;
import framework.data.DataRowCollection;
import framework.diagnostics.TraceType;

import tradingConsole.TradingConsole;
import tradingConsole.ui.columnKey.RecoverPasswordQuotationSettingGridColKey;
import com.jidesoft.grid.ListComboBoxCellEditor;
import java.awt.Color;

public class RecoverPasswordManager
{
	private String _dataSourceKey;
	private tradingConsole.ui.grid.BindingSource _bindingSource;

	private HashMap<Guid, RecoverPasswordQuestion> _recoverPasswordQuestions;
	private HashMap<String, RecoverPasswordAnswer> _recoverPasswordAnswers;
	private int _recoverPasswordAnswerCount = 6;

	private RecoverPasswordManager()
	{
		this._recoverPasswordQuestions = new HashMap<Guid, RecoverPasswordQuestion>();
		this._recoverPasswordAnswers = new HashMap<String, RecoverPasswordAnswer>();
	}

	public static RecoverPasswordManager Create()
	{
		return new RecoverPasswordManager();
	}

	public void initialize(DataSet recoverPasswordData, int recoverPasswordAnswerCount)
	{
		this._recoverPasswordAnswerCount = recoverPasswordAnswerCount;

		if (recoverPasswordData == null)
		{
			TradingConsole.traceSource.trace(TraceType.Error, "RecoverPasswordManager.initialize" + " RecoverPassword data is not available!");
			return;
		}

		DataTableCollection tables = recoverPasswordData.get_Tables();
		DataTable dataTable;
		DataRowCollection dataRowCollection;
		DataRow dataRow;

		dataTable = tables.get_Item("RecoverPasswordQuestion");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				Guid id = (Guid)dataRow.get_Item("QuestionId");
				if (this._recoverPasswordQuestions.containsKey(id))
				{
					this.getRecoverPasswordQuestion(id).replace(dataRow);
				}
				else
				{
					RecoverPasswordQuestion recoverPasswordQuestion = new RecoverPasswordQuestion(id, dataRow);
					this._recoverPasswordQuestions.put(id, recoverPasswordQuestion);
				}
			}
		}

		this.setDefaultAnswers();

		dataTable = tables.get_Item("RecoverPasswordAnswer");
		if (dataTable != null)
		{
			dataRowCollection = dataTable.get_Rows();
			for (int rowIndex = 0; rowIndex < dataRowCollection.get_Count(); rowIndex++)
			{
				dataRow = dataRowCollection.get_Item(rowIndex);

				int sequence = (Integer)dataRow.get_Item("Sequence");
				String key = framework.xml.XmlConvert.toString(sequence);
				if (this._recoverPasswordAnswers.containsKey(key))
				{
					this.getRecoverPasswordAnswer(key).replace(dataRow);
				}
				else
				{
					//not allow add new record
					//RecoverPasswordAnswer recoverPasswordAnswer = new RecoverPasswordAnswer(this, sequence, dataRow);
					//this._recoverPasswordAnswers.put(key, recoverPasswordAnswer);
				}
			}
		}
	}

	private void setDefaultAnswers()
	{
		int recoverPasswordQuestionsSize = this._recoverPasswordQuestions.values().size();
		Object[] recoverPasswordQuestions = null;
		if (recoverPasswordQuestionsSize > 0)
		{
			recoverPasswordQuestions = (Object[])this._recoverPasswordQuestions.values().toArray();
		}
		for(int sequence = 0,count = this._recoverPasswordAnswerCount;sequence < count;sequence++)
		{
			String key = framework.xml.XmlConvert.toString(sequence);
			RecoverPasswordAnswer recoverPasswordAnswer = new RecoverPasswordAnswer(this, sequence);
			this._recoverPasswordAnswers.put(key, recoverPasswordAnswer);

			if (recoverPasswordQuestionsSize > 0)
			{
				RecoverPasswordQuestion recoverPasswordQuestion = (recoverPasswordQuestionsSize > sequence)?((RecoverPasswordQuestion)recoverPasswordQuestions[sequence]):((RecoverPasswordQuestion)recoverPasswordQuestions[0]);
				recoverPasswordAnswer.set_QuestionContent(recoverPasswordQuestion.get_Content());
			}
		}
	}

	public HashMap<Guid, RecoverPasswordQuestion> get_RecoverPasswordQuestions()
	{
		return this._recoverPasswordQuestions;
	}

	public RecoverPasswordQuestion getRecoverPasswordQuestion(String content)
	{
		for (Iterator<RecoverPasswordQuestion> iterator = this._recoverPasswordQuestions.values().iterator(); iterator.hasNext(); )
		{
			RecoverPasswordQuestion recoverPasswordQuestion = iterator.next();
			if (recoverPasswordQuestion.get_Content().equalsIgnoreCase(content))
			{
				return recoverPasswordQuestion;
			}
		}
		return null;
	}

	public RecoverPasswordQuestion getRecoverPasswordQuestion(Guid id)
	{
		return (this._recoverPasswordQuestions.containsKey(id)) ? this._recoverPasswordQuestions.get(id) : null;
	}

	public void setRecoverPasswordQuestion(RecoverPasswordQuestion recoverPasswordQuestion)
	{
		if (!this._recoverPasswordQuestions.containsKey(recoverPasswordQuestion.get_Id()))
		{
			this._recoverPasswordQuestions.put(recoverPasswordQuestion.get_Id(), recoverPasswordQuestion);
		}
	}

	public void removeRecoverPasswordQuestion(RecoverPasswordQuestion recoverPasswordQuestion)
	{
		if (this._recoverPasswordQuestions.containsKey(recoverPasswordQuestion.get_Id()))
		{
			this._recoverPasswordQuestions.remove(recoverPasswordQuestion.get_Id());
		}
	}

	public HashMap<String, RecoverPasswordAnswer> get_RecoverPasswordAnswers()
	{
		return this._recoverPasswordAnswers;
	}

	public RecoverPasswordAnswer getRecoverPasswordAnswer(String key)
	{
		return (this._recoverPasswordAnswers.containsKey(key)) ? this._recoverPasswordAnswers.get(key) : null;
	}

	public void setRecoverPasswordAnswer(RecoverPasswordAnswer recoverPasswordAnswer)
	{
		if (!this._recoverPasswordAnswers.containsKey(recoverPasswordAnswer.get_Key()))
		{
			this._recoverPasswordAnswers.put(recoverPasswordAnswer.get_Key(), recoverPasswordAnswer);
		}
	}

	public void removeRecoverPasswordAnswer(RecoverPasswordAnswer recoverPasswordAnswer)
	{
		if (this._recoverPasswordAnswers.containsKey(recoverPasswordAnswer.get_Key()))
		{
			this._recoverPasswordAnswers.remove(recoverPasswordAnswer.get_Key());
		}
	}

	public String get_DataSourceKey()
	{
		return this._dataSourceKey;
	}

	public tradingConsole.ui.grid.BindingSource get_BindingSource()
	{
		return this._bindingSource;
	}

	private void setQuestionContentChoice(tradingConsole.ui.grid.DataGrid grid)
	{
		int column = this._bindingSource.getColumnByName(RecoverPasswordQuotationSettingGridColKey.QuestionContent);
		String[] questions = new String[this._recoverPasswordQuestions.values().size()];
		int index = 0;
		for(RecoverPasswordQuestion recoverPasswordQuestion : this._recoverPasswordQuestions.values())
		{
			questions[index++] = recoverPasswordQuestion.get_Content();
		}
		ListComboBoxCellEditor listComboBoxCellEditor = new ListComboBoxCellEditor(questions);
		grid.getColumnModel().getColumn(column).setCellEditor(listComboBoxCellEditor);
	}

	public String[][] getRecoverPasswordDatas()
	{
		int i = 0;
		int size = this._recoverPasswordAnswers.size();
		String[][] recoverPasswordDatas = new String[size][3];
		for (Iterator<RecoverPasswordAnswer> iterator = this._recoverPasswordAnswers.values().iterator(); iterator.hasNext(); )
		{
			RecoverPasswordAnswer recoverPasswordAnswer = iterator.next();
			if (!recoverPasswordAnswer.isFullData())
			{
				recoverPasswordDatas = new String[][]{};
				return recoverPasswordDatas;
			}

			recoverPasswordDatas[i][0] = recoverPasswordAnswer.get_Key();
			recoverPasswordDatas[i][1] = recoverPasswordAnswer.get_QuestionId().toString();
			recoverPasswordDatas[i][2] = recoverPasswordAnswer.get_Answer();
			i++;
		}
		return recoverPasswordDatas;
	}

	public void setAnswerToEmpty()
	{
		for (Iterator<RecoverPasswordAnswer> iterator = this._recoverPasswordAnswers.values().iterator(); iterator.hasNext(); )
		{
			RecoverPasswordAnswer recoverPasswordAnswer = iterator.next();
			recoverPasswordAnswer.set_Answer("");
		}
	}

	public void initialize(tradingConsole.ui.grid.DataGrid grid, boolean ascend, boolean caseOn)
	{
		this.unbind();

		this._dataSourceKey = Guid.newGuid().toString();
		this._bindingSource = new tradingConsole.ui.grid.BindingSource();

		RecoverPasswordAnswer.initialize(grid, this._dataSourceKey, this._recoverPasswordAnswers.values(), this._bindingSource);

		this.setQuestionContentChoice(grid);
		grid.setBackground(Color.WHITE);

		//for (Iterator<RecoverPasswordAnswer> iterator = this._recoverPasswordAnswers.values().iterator(); iterator.hasNext(); )
		//{
		//	RecoverPasswordAnswer recoverPasswordAnswer = iterator.next();
		//	recoverPasswordAnswer.update(this._dataSourceKey);
		//}
		//int column = this._bindingSource.getColumnByName(RecoverPasswordQuotationSettingGridColKey.Sequence);
		//grid.sortColumn(column, ascend);
	}

	public void unbind()
	{
		if (this._dataSourceKey == null || this._bindingSource == null) return;

		for (Iterator<RecoverPasswordAnswer> iterator = this._recoverPasswordAnswers.values().iterator(); iterator.hasNext(); )
		{
			RecoverPasswordAnswer recoverPasswordAnswer = iterator.next();
			recoverPasswordAnswer.unbind(this._dataSourceKey, this._bindingSource);
		}
	}

	public void update()
	{
		for (Iterator<RecoverPasswordAnswer> iterator = this._recoverPasswordAnswers.values().iterator(); iterator.hasNext(); )
		{
			RecoverPasswordAnswer recoverPasswordAnswer = iterator.next();
			recoverPasswordAnswer.update(this._dataSourceKey);
		}
	}

	public void finalize() throws Throwable
	{
		this.unbind();

		super.finalize();
	}
}
