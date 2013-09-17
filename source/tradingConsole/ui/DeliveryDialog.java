package tradingConsole.ui;

import javax.swing.*;
import tradingConsole.Account;
import tradingConsole.Instrument;
import tradingConsole.TradingConsole;
import tradingConsole.Order;
import tradingConsole.ui.language.Language;
import java.awt.Frame;

public class DeliveryDialog extends JDialog
{
	private DeliveryForm _deliveryForm;
	private OpenContractForm _openContractForm;

	public DeliveryDialog(JFrame parent, TradingConsole tradingConsole, Instrument instrument,
						Account account, Order order, Boolean closeAllSell)
	{
		super(parent, false);

		this.setSize(480, 380);

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setTitle(Language.deliveryFormTitle);

		this._deliveryForm = new DeliveryForm(this, tradingConsole, instrument,
						account, order, closeAllSell);

		this.add(this._deliveryForm);
	}

	public DeliveryDialog(OpenContractForm parent, TradingConsole tradingConsole, Instrument instrument,
						Account account, Order order, Boolean closeAllSell)
	{
		super(parent, false);
		this._openContractForm = parent;

		this.setSize(480, 380);

		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setTitle(Language.deliveryFormTitle + "  " + instrument.get_DescriptionForTrading());

		this._deliveryForm = new DeliveryForm(this, tradingConsole, instrument,
						account, order, closeAllSell);

		this.add(this._deliveryForm);
	}

	@Override
	public void dispose()
	{
		super.dispose();
		if(this._openContractForm != null)
		{
			OpenContractForm openContractForm = this._openContractForm;
			this._openContractForm = null;
			openContractForm.dispose();
		}
	}
}
