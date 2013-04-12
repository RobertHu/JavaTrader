package tradingConsole;

import java.util.HashMap;
import framework.Guid;
import java.io.File;
import java.io.BufferedReader;
import framework.io.DirectoryHelper;
import java.io.FileReader;
import framework.diagnostics.TraceType;
import framework.FrameworkException;
import framework.StringHelper;
import java.io.PrintWriter;

public class MaxMoveAutoFillHelper
{
	private static HashMap<Guid, HashMap<Guid, Integer>> _maxMoves;
	private static HashMap<Guid, Boolean> _saveMaxMoveAsDefaultForSingleAccount;
	private static HashMap<Guid, Boolean> _saveMaxMoveAsDefaultForMultiAccount;

	static
	{
		MaxMoveAutoFillHelper._maxMoves = new HashMap<Guid, HashMap<Guid, Integer>> ();
		MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForSingleAccount = new HashMap<Guid,Boolean>();
		MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForMultiAccount = new HashMap<Guid,Boolean>();
	}

	public static void initialize()
	{
		MaxMoveAutoFillHelper._maxMoves.clear();
		MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForSingleAccount.clear();
		MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForMultiAccount.clear();

		MaxMoveAutoFillHelper.load();
	}

	public static Boolean get_SaveMaxMoveAsDefaultForSingleAccount(Guid instrumentId)
	{
		return MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForSingleAccount.containsKey(instrumentId) ?
			MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForSingleAccount.get(instrumentId) : null;
	}

	public static Boolean get_SaveMaxMoveAsDefaultForMultiAccount(Guid instrumentId)
	{
		return MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForMultiAccount.containsKey(instrumentId) ?
			MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForMultiAccount.get(instrumentId) : null;
	}

	public static void set_SaveMaxMoveAsDefaultForSingleAccount(Guid instrumentId, boolean value)
	{
		MaxMoveAutoFillHelper.set_SaveMaxMoveAsDefaultForSingleAccount(instrumentId, value, false);
	}

	public static void set_SaveMaxMoveAsDefaultForSingleAccount(Guid instrumentId, boolean value, boolean forLoading)
	{
		Boolean oldValue = MaxMoveAutoFillHelper.get_SaveMaxMoveAsDefaultForSingleAccount(instrumentId);
		if(oldValue == null || oldValue != value)
		{
			MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForSingleAccount.put(instrumentId, value);
			if(!forLoading) MaxMoveAutoFillHelper.save();
		}
	}

	public static void set_SaveMaxMoveAsDefaultForMultiAccount(Guid instrumentId, boolean value)
	{
		MaxMoveAutoFillHelper.set_SaveMaxMoveAsDefaultForMultiAccount(instrumentId, value, false);
	}

	private static void set_SaveMaxMoveAsDefaultForMultiAccount(Guid instrumentId, boolean value, boolean forLoading)
	{
		Boolean oldValue = MaxMoveAutoFillHelper.get_SaveMaxMoveAsDefaultForMultiAccount(instrumentId);
		if(oldValue == null || oldValue != value)
		{
			MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForMultiAccount.put(instrumentId, value);
			if(!forLoading) MaxMoveAutoFillHelper.save();
		}
	}

	public static void setDefaultMaxMove(Guid accountId, Guid instrumentId, int maxMove)
	{
		MaxMoveAutoFillHelper.setDefaultMaxMove(accountId, instrumentId, maxMove, false);
	}

	private static void setDefaultMaxMove(Guid accountId, Guid instrumentId, int maxMove, boolean forLoading)
	{
		Integer oldDefaultValue = MaxMoveAutoFillHelper.getDefaultMaxMove(accountId, instrumentId);
		if (oldDefaultValue == null || oldDefaultValue.intValue() != maxMove)
		{
			HashMap<Guid, Integer> maxMoves = null;
			if (MaxMoveAutoFillHelper._maxMoves.containsKey(accountId))
			{
				maxMoves = MaxMoveAutoFillHelper._maxMoves.get(accountId);
			}
			else
			{
				maxMoves = new HashMap<Guid, Integer> ();
				MaxMoveAutoFillHelper._maxMoves.put(accountId, maxMoves);
			}
			maxMoves.put(instrumentId, maxMove);

			if (!forLoading)
			{
				MaxMoveAutoFillHelper.save();
			}
		}
	}

	public static Integer getDefaultMaxMove(Guid accountId, Guid instrumentId)
	{
		if (MaxMoveAutoFillHelper._maxMoves.containsKey(accountId))
		{
			HashMap<Guid, Integer> maxMoves = MaxMoveAutoFillHelper._maxMoves.get(accountId);
			if (maxMoves.containsKey(instrumentId))
			{
				return maxMoves.get(instrumentId);
			}
		}

		return null;
	}

	private static void load()
	{
		try
		{
			String fileName = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(), "DefaultMaxMove.dat");
			File file = new File(fileName);
			if (!file.exists())
			{
				return;
			}
			FileReader fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			String record = reader.readLine();
			while (record != null)
			{
				String[] items = StringHelper.split(record, ';');
				if(items.length == 4)
				{
					boolean isForMultiAccounts = Boolean.parseBoolean(items[0]);
					Guid instrumentId = new Guid(items[1]);
					boolean value = Boolean.parseBoolean(items[2]);
					if(isForMultiAccounts)
					{
						MaxMoveAutoFillHelper.set_SaveMaxMoveAsDefaultForMultiAccount(instrumentId, value, true);
					}
					else
					{
						MaxMoveAutoFillHelper.set_SaveMaxMoveAsDefaultForSingleAccount(instrumentId, value, true);
					}
				}
				else
				{
					Guid accountId = new Guid(items[0]);
					Guid instrumentId = new Guid(items[1]);
					Integer maxMove = Integer.parseInt(items[2]);

					MaxMoveAutoFillHelper.setDefaultMaxMove(accountId, instrumentId, maxMove, true);
				}
				record = reader.readLine();
			}

			fileReader.close();
		}
		catch (Exception ex)
		{
			TradingConsole.traceSource.trace(TraceType.Error, "[MaxMoveAutoFillHelper.load] " + FrameworkException.getStackTrace(ex));
		}
	}

	private static void save()
	{
		try
		{
			String fileName = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(), "DefaultMaxMove.dat");

			File file = new File(fileName);
			if (file.exists())
			{
				file.delete();
			}
			file.createNewFile();
			PrintWriter writer = new PrintWriter(file);

			for(Guid instrumentId : MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForMultiAccount.keySet())
			{
				writer.print(Boolean.TRUE.toString());
				writer.print(';');
				writer.print(instrumentId.toString());
				writer.print(';');
				writer.print(MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForMultiAccount.get(instrumentId).toString());
				writer.println(';');
			}

			for(Guid instrumentId : MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForSingleAccount.keySet())
			{
				writer.print(Boolean.FALSE.toString());
				writer.print(';');
				writer.print(instrumentId.toString());
				writer.print(';');
				writer.print(MaxMoveAutoFillHelper._saveMaxMoveAsDefaultForSingleAccount.get(instrumentId).toString());
				writer.println(';');
			}



			for(Guid accountId : MaxMoveAutoFillHelper._maxMoves.keySet())
			{
				HashMap<Guid, Integer> instruments = MaxMoveAutoFillHelper._maxMoves.get(accountId);
				for (Guid instrumentId : instruments.keySet())
				{
					writer.print(accountId.toString());
					writer.print(';');
					writer.print(instrumentId.toString());
					writer.print(';');
					writer.println(instruments.get(instrumentId).toString());
				}
			}
			writer.close();
		}
		catch (Exception ex)
		{
			TradingConsole.traceSource.trace(TraceType.Error, "[MaxMoveAutoFillHelper.save] " + FrameworkException.getStackTrace(ex));
		}
	}
}
