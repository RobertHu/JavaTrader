package tradingConsole.ui;

import javax.swing.*;

import tradingConsole.*;
import tradingConsole.settings.*;
import tradingConsole.ui.language.Language;
import java.math.BigDecimal;
import tradingConsole.enumDefine.BuySellType;
import com.jidesoft.swing.JideTabbedPane;
import framework.StringHelper;

public class LimitOrderFormDialog extends JDialog
{
	private LimitOrderForm _limitOrderForm;
	private MatchingOrderForm matchingOrderForm;
	private SettingsManager _settingsManager;
	private Instrument _instrument;
	private OpenContractForm _openContractForm;

	public LimitOrderFormDialog(JDialog parent, TradingConsole tradingConsole, SettingsManager settingsManager, Instrument instrument, Order order,
					  OpenContractForm openContractForm, boolean isBuy)
	{
		super(parent, false);
		this._settingsManager = settingsManager;
		this._instrument = instrument;
		this._openContractForm = openContractForm;
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		boolean allowMatchingOrder = MakeOrder.canMatchingOrder(this._settingsManager, this._instrument, order == null ? null : order.get_Account());
		if(allowMatchingOrder && this._instrument.get_MaxOtherLot().compareTo(BigDecimal.ZERO) > 0)
		{
			JideTabbedPane tabbedPane = new JideTabbedPane();

			this.setSize(520, 470);
			this.getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

			this._limitOrderForm = new LimitOrderForm(this, tradingConsole, settingsManager, instrument, order,  openContractForm, isBuy, true);
			tabbedPane.addTab(Language.limitOrderFormTitle, this._limitOrderForm);

			Object[] result = MakeOrder.isAllowMakeLimitOrder(tradingConsole, this._settingsManager,
				this._instrument, null);
			if ( (Boolean)result[0])
			{
				MakeLimitOrder makeLimitOrder = (MakeLimitOrder)result[2];
				makeLimitOrder.setDefaultBuySellType(BuySellType.Buy);
				matchingOrderForm = new MatchingOrderForm(this, tradingConsole, this._settingsManager, this._instrument, order,  openContractForm, isBuy, makeLimitOrder);
				tabbedPane.addTab(Language.matchingOrderFormTitle, matchingOrderForm);
			}
		}
		else
		{
			this.setSize(500, 420);
			this._limitOrderForm = new LimitOrderForm(this, tradingConsole, settingsManager, instrument, order,  openContractForm, isBuy, false);
			this._limitOrderForm.setOpaque(false);
			this.getContentPane().add(this._limitOrderForm, java.awt.BorderLayout.CENTER);
		}
		this.setTitle(instrument.get_DescriptionForTrading());

		MakeOrderWindow makeOrderWindow = new MakeOrderWindow(instrument.get_Id(), this, false);
		this._settingsManager.setMakeOrderWindow(instrument, makeOrderWindow);

		if(this._openContractForm != null)	this._openContractForm.setCanClose(false);
	}

	public void dispose2()
	{
		if(this.getParent() != null && this.getParent() instanceof JDialog)
		{
			((JDialog)this.getParent()).dispose();
		}
		this.dispose();
	}

	@Override
	public void dispose()
	{
		this._settingsManager.removeMakeOrderWindow(this._instrument, this);
		if(this._openContractForm != null)
		{
			this._openContractForm.setCanClose(true);
			this._openContractForm.enableLimitStopButton();
		}
		super.dispose();
	}

	public void resetData()
	{
		this._limitOrderForm.resetData();
	}

	public void notifyMakeOrderUiByPrice()
	{
		this._limitOrderForm.refreshPrice();
	}
}
