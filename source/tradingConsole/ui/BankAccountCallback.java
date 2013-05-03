package tradingConsole.ui;

import framework.IAsyncCallback;
import framework.StringHelper;
import Util.XmlElementHelper;
import tradingConsole.ui.language.Language;
import javax.swing.SwingUtilities;
import framework.IAsyncResult;
import Packet.SignalObject;
import framework.data.DataSet;
import Util.RequestCommandHelper;

public  class BankAccountCallback implements IAsyncCallback
	{
		private String code;
		private InitializeBankAccount initializeBankAccount;
		public BankAccountCallback(String code,InitializeBankAccount initializeBankAccount){
			this.code=code;
			this.initializeBankAccount = initializeBankAccount;
		}
		public void asyncCallback(IAsyncResult asyncResult){}

		public void asyncCallback(SignalObject signal)
		{
			DataSet bankAccountsDataSet =null;
			if(!signal.getIsError()){
				System.out.println(signal.getResult().toXML());
				bankAccountsDataSet = RequestCommandHelper.getDataFromResponse(signal.getResult());
			}
			if (bankAccountsDataSet == null)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						String info = StringHelper.format(Language.CannotGetBankAccounts, new Object[]
							{code});
						initializeBankAccount.showWarning(info);
					}
				});
			}
			else
			{
				initializeBankAccount.initialize(bankAccountsDataSet);
			}
		}

	}
