package tradingConsole.ui;

import java.io.*;
import java.math.*;
import java.util.*;

import framework.*;
import framework.io.*;
import tradingConsole.*;

public class PalceLotNnemonic
{
	private static HashMap<Guid, HashMap<Guid, BigDecimal>> _instrumentToLot = new HashMap<Guid, HashMap<Guid, BigDecimal>>();
	private static boolean _enabled = true;

	public static void set(Guid instrumentId, Guid accountId, BigDecimal lastPlaceLot)
	{
		if(!PalceLotNnemonic._enabled) return;
		if(!PalceLotNnemonic._instrumentToLot.containsKey(instrumentId))
		{
			PalceLotNnemonic._instrumentToLot.put(instrumentId, new HashMap<Guid, BigDecimal>());
		}
		PalceLotNnemonic._instrumentToLot.get(instrumentId).put(accountId, lastPlaceLot);
	}

	public static BigDecimal getLastPlaceLot(Guid instrumentId, Guid accountId)
	{
		if(!PalceLotNnemonic._enabled) return null;

		if(PalceLotNnemonic._instrumentToLot.containsKey(instrumentId))
		{
			if(PalceLotNnemonic._instrumentToLot.get(instrumentId).containsKey(accountId))
			{
				return PalceLotNnemonic._instrumentToLot.get(instrumentId).get(accountId);
			}
		}
		return null;
	}

	public static void load() throws FileNotFoundException, IOException
	{
		if(!PalceLotNnemonic._enabled) return;

		String fileName = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(), "PalceLotNnemonic");
		File file = new File(fileName);
		if(!file.exists()) return;
		FileReader fileReader = new FileReader(file);
		BufferedReader reader = new BufferedReader(fileReader);
		String record = reader.readLine();
		while (record != null)
		{
			String[] items = StringHelper.split(record, '=');
			Guid instrumentId = new Guid(items[0]);
			PalceLotNnemonic._instrumentToLot.put(instrumentId, new HashMap<Guid, BigDecimal>());

			String[] accounts = StringHelper.split(items[1], ';');
			for(String account : accounts)
			{
				if(account.length() < 1) continue;
				items = StringHelper.split(account, '+');
				Guid accountId = new Guid(items[0]);
				BigDecimal lastPlaceLot = new BigDecimal(items[1]);
				PalceLotNnemonic._instrumentToLot.get(instrumentId).put(accountId, lastPlaceLot);
			}
			record = reader.readLine();
		}
	}

	public static void save() throws IOException
	{
		if(!PalceLotNnemonic._enabled) return;

		String fileName = DirectoryHelper.combine(AppToolkit.get_SettingDirectory(), "PalceLotNnemonic");

		File file = new File(fileName);
		if(file.exists()) file.delete();
		file.createNewFile();
		PrintWriter writer = new PrintWriter(file);

		for(Guid instrumentId : PalceLotNnemonic._instrumentToLot.keySet())
		{
			writer.print(instrumentId + "=");
			HashMap<Guid, BigDecimal> acounts = PalceLotNnemonic._instrumentToLot.get(instrumentId);
			for(Guid accountId : acounts.keySet())
			{
				writer.print(accountId + "+" + acounts.get(accountId)+";");
			}
			writer.println();
		}

		writer.flush();
		writer.close();
	}

	public static void set_Enable(Boolean enable)
	{
		PalceLotNnemonic._enabled = enable;
	}
}
