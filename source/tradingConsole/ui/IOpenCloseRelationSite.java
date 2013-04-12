package tradingConsole.ui;

import java.math.*;

import javax.swing.*;

import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.grid.*;

public interface IOpenCloseRelationSite extends IOpenCloseRelationBaseSite
{
	OperateType getOperateType();
	BigDecimal getLot();
	PVStaticText2 getLiqLotValueStaticText();
	BigDecimal getTotalQuantity();
	void updateAccount(BigDecimal accountLot, boolean openOrderIsBuy);
	void updateTotalColseLot(BigDecimal totalCloseLot);
}
