package tradingConsole.service;

import java.math.BigDecimal;
import nu.xom.Element;
import Packet.StringConstants;

public class ChangeLeverageResult
{
	private boolean _successed;
	private BigDecimal _necessary;

	public ChangeLeverageResult(Element data)
	{
		this._necessary = new BigDecimal(data.getFirstChildElement("necessary").getValue());
		this._successed = data.getFirstChildElement("successed").getValue().equals(StringConstants.BoolTrueSymbol) ? true : false;
	}

	public ChangeLeverageResult(Object[] results)
	{
		this._successed = (Boolean) (results[0]);
		this._necessary = (BigDecimal) (results[1]);
	}

	public boolean get_IsSuceessed()
	{
		return this._successed;
	}

	public BigDecimal get_Necessary()
	{
		return this._necessary;
	}
}
