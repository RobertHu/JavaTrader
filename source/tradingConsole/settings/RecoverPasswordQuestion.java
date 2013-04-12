package tradingConsole.settings;

import framework.Guid;
import framework.data.DataRow;

public class RecoverPasswordQuestion
{
	private Guid _questionId;
	private String _content;

	private RecoverPasswordQuestion(Guid questionId)
	{
		this._questionId=questionId;
	}

	public RecoverPasswordQuestion(Guid questionId, DataRow dataRow)
	{
		this(questionId);
		this.setValue(dataRow);
	}

	public void replace(DataRow dataRow)
	{
		this.setValue(dataRow);
	}

	private void setValue(DataRow dataRow)
	{
		if (dataRow.get_Item(PublicParametersManager.version) != null)
		{
			this._content = (String)dataRow.get_Item(PublicParametersManager.version);
		}
		else
		{
			this._content = (String)dataRow.get_Item(1);
		}
	}

	public Guid get_Id()
	{
		return this._questionId;
	}

	public String get_Content()
	{
		return this._content;
	}
}
