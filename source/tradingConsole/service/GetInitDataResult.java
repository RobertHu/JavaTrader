package tradingConsole.service;

import framework.data.*;
import nu.xom.Element;
import Util.*;

public class GetInitDataResult
{
	private int _commandSequence;
	private DataSet _dataSet;

	public int get_CommandSequence()
	{
		return this._commandSequence;
	}

	public DataSet get_DataSet()
	{
		return this._dataSet;
	}

	public GetInitDataResult(Object[] results)
	{
		this._commandSequence = ((Integer) (results[1])).intValue();
		this._dataSet=(DataSet)(results[0]);
	}

	public GetInitDataResult(String result)
	{
		this._dataSet=XmlElementHelper.convertToDataset(result);
		if(this._dataSet!=null){
			DataTableCollection tables = this._dataSet.get_Tables();
			DataTable commadSequenceTable = tables.get_Item("CommandSequence");
			DataRowCollection commandSequenceCollection = commadSequenceTable.get_Rows();
			DataRow dRow = commandSequenceCollection.get_Item(0);
			int commandSequence = (Integer)dRow.get_Item("CommandSequenceCol");
			this._commandSequence = commandSequence;
		}
	}
}
