package tradingConsole.ui;

import javax.swing.*;

import tradingConsole.*;
import tradingConsole.enumDefine.*;
import tradingConsole.settings.*;
import tradingConsole.ui.grid.*;

public interface IOpenCloseRelationBaseSite
{
	OrderType getOrderType();
	Boolean isMakeLimitOrder();
	boolean allowChangeCloseLot();
	void addPlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener);
	void removePlaceOrderTypeChangedListener(IPlaceOrderTypeChangedListener placeOrderTypeChangedListener);
	JTextField getCloseLotEditor();
	JTextField getTotalLotEditor();
	JDialog getFrame();
	MakeOrderAccount getMakeOrderAccount();
	DataGrid getRelationDataGrid();
	DataGrid getAccountDataGrid();
	Instrument getInstrument();
	void rebind();
}
