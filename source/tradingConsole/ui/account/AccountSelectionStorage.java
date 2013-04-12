package tradingConsole.ui.account;

import java.util.ArrayList;
import framework.Guid;
import tradingConsole.AppToolkit;
import framework.io.DirectoryHelper;
import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import framework.diagnostics.TraceType;
import framework.diagnostics.TraceSource;
import tradingConsole.TradingConsole;

public class AccountSelectionStorage
{
	private ArrayList<Guid> _unselectedAccounts = new ArrayList<Guid>();

	public static AccountSelectionStorage Instance = new AccountSelectionStorage();

	private AccountSelectionStorage()
	{
		this.load();
	}

	public void add(Account account)
	{
		if(!this._unselectedAccounts.contains(account.get_Id()))
		{
			this._unselectedAccounts.add(account.get_Id());
			this.save();
		}
	}

	public void remove(Account account)
	{
		this._unselectedAccounts.remove(account.get_Id());
		this.save();
	}

	public boolean isUnselected(Account account)
	{
		return this._unselectedAccounts.contains(account.get_Id());
	}

	private void load()
	{
		try
		{
			String fullFilePath = DirectoryHelper.combine(AppToolkit.get_SettingDirectory() , "unselectedAccount.dat");
			File file = new File(fullFilePath);
			if (file.exists())
			{
				BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
				String buffer = bufferedReader.readLine();
				while (buffer != null)
				{
					Guid id = new Guid(buffer);
					if(!this._unselectedAccounts.contains(id))
					{
						this._unselectedAccounts.add(id);
					}
					buffer = bufferedReader.readLine();
				}
				bufferedReader.close();
			}
		}
		catch (Exception exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}

	private void save()
	{
		try
		{
			String fullFilePath = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(), "unselectedAccount.dat");
			File file = new File(fullFilePath);
			if (file.exists())
				file.delete();
			file.createNewFile();
			PrintWriter printWriter = new PrintWriter(file);
			for (Guid id : this._unselectedAccounts)
			{
				printWriter.println(id.toString());
			}
			printWriter.close();
		}
		catch(Exception exception)
		{
			TradingConsole.traceSource.trace(TraceType.Error, exception);
		}
	}
}
