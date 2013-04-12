package tradingConsole;

import tradingConsole.ui.language.Language;
import framework.lang.Enum;

//#0;身份证|#1;户口簿|#2;护照|#3;军官证|#4;士兵证|#5;港澳居民来往内地通行证|#6;台湾同胞来往内地通行证|#7;临时身份证|#8;外国人居留证|#9;警官证|#x;其他证件
public class BankIdType extends Enum<BankIdType>
{
	public static BankIdType QiTaZhengJian = new BankIdType("QiTaZhengJian", 0, "x");
	public static BankIdType ShenFenZheng = new BankIdType("ShenFenZheng", 1, "0");
	public static BankIdType HuKouBo = new BankIdType("HuKouBo", 2, "1");
	public static BankIdType HuZhao = new BankIdType("HuZhao", 3, "2");
	public static BankIdType JunGuanZheng = new BankIdType("JunGuanZheng", 4, "3");
	public static BankIdType ShiBingZheng = new BankIdType("ShiBingZheng", 5, "4");
	public static BankIdType GangAoTongXingZheng = new BankIdType("GangAoTongXingZheng", 6, "5");
	public static BankIdType TaiBaoZheng = new BankIdType("TaiBaoZheng", 7, "6");
	public static BankIdType LinShiShenFenZheng = new BankIdType("LinShiShenFenZheng", 8, "7");
	public static BankIdType JunLiuZheng = new BankIdType("JunLiuZheng", 9, "8");
	public static BankIdType JingGuanZheng = new BankIdType("JingGuanZheng", 10, "9");

	private String _innerValue;

	private BankIdType(String name, int value, String innerValue)
	{
		super(name, value);
		this._innerValue = innerValue;
	}

	public String get_InnerValue()
	{
		return this._innerValue;
	}

	public String getCaption()
	{
		switch (this.value())
		{
			case 0:
				return Language.QiTaZhengJian;
			case 1:
				return Language.ShenFenZheng;
			case 2:
				return Language.HuKouBo;
			case 3:
				return Language.HuZhao;
			case 4:
				return Language.ShiBingZheng;
			case 5:
				return Language.GangAoTongXingZheng;
			case 6:
				return Language.TaiBaoZheng;
			case 7:
				return Language.LinShiShenFenZheng;
			case 8:
				return Language.JunLiuZheng;
			case 9:
				return Language.JingGuanZheng;
			default:
				return Language.QiTaZhengJian;
		}
	}

	public static BankIdType fromInnerValue(String value)
	{
		try
		{
			if (value.compareToIgnoreCase("x") == 0)
			{
				return BankIdType.QiTaZhengJian;
			}
			else
			{
				int value2 = Integer.parseInt(value) + 1;
				return BankIdType.valueOf(BankIdType.class, value2);
			}
		}
		catch (Throwable t)
		{
			return BankIdType.QiTaZhengJian;
		}
	}
}
