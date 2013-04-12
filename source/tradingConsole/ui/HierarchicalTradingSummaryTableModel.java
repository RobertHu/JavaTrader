package tradingConsole.ui;

import com.jidesoft.grid.HierarchicalTableModel;
import tradingConsole.ui.grid.BindingSource;
import tradingConsole.Instrument;

public class HierarchicalTradingSummaryTableModel implements HierarchicalTableModel
{
	private BindingSource _owner;

	public void set_Owner(BindingSource owner)
	{
		this._owner = owner;
	}

	public BindingSource get_Owner()
	{
		return this._owner;
	}

	public boolean hasChild(int row)
	{
		Instrument instrument = (Instrument)this._owner.getObject(row);
		BindingSource accountTradingSummary = instrument.get_AccountTradingSummary();
		return accountTradingSummary != null && accountTradingSummary.getRowCount() > 0;
	}

	public boolean isHierarchical(int row)
	{
		Instrument instrument = (Instrument)this._owner.getObject(row);
		BindingSource accountTradingSummary = instrument.get_AccountTradingSummary();
		return accountTradingSummary != null && accountTradingSummary.getRowCount() > 0;
	}

	public Object getChildValueAt(int row)
	{
		Instrument instrument = (Instrument)this._owner.getObject(row);
		return instrument.get_AccountTradingSummary();
	}

	public boolean isExpandable(int row)
	{
		Instrument instrument = (Instrument)this._owner.getObject(row);
		BindingSource accountTradingSummary = instrument.get_AccountTradingSummary();
		return accountTradingSummary != null && accountTradingSummary.getRowCount() > 0;
	}
}
