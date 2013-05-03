package tradingConsole.enumDefine;

import framework.lang.Enum;

public class VolumeNecessaryOption extends Enum<VolumeNecessaryOption>
{
	public static final VolumeNecessaryOption Progessive = new VolumeNecessaryOption("Progessive", 0);
	public static final VolumeNecessaryOption Flat = new VolumeNecessaryOption("Flat", 1);

	private VolumeNecessaryOption(String name, int value)
	{
		super(name, value);
	}
}
