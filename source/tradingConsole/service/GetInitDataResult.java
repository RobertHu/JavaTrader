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

	public GetInitDataResult(Element result)
	{
		Element commandSeqElement = result.getFirstChildElement("commandSequence");
		Element dataElement = result.getFirstChildElement("data");
		this._commandSequence=Integer.parseInt(commandSeqElement.getValue());
		this._dataSet=XmlElementHelper.convertToDataset(dataElement);
	}
}
