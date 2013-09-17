package tradingConsole;

import java.math.BigDecimal;

import framework.FrameworkException;
import framework.Convert;
import framework.StringHelper;
import framework.text.regularExpressions.Regex;
import framework.text.regularExpressions.Match;
import framework.text.regularExpressions.RegexOptions;
import framework.text.regularExpressions.RegexCompiler;
import framework.diagnostics.TraceType;
import tradingConsole.ui.language.Language;
import java.text.DecimalFormat;
import java.util.Locale;

// priceStyle1 : 1/1		17590 17592
// priceStyle2 : 1/100		127.08 127.09
// priceStyle3 : 1/16		127.8/16  127.9/16
// priceStyle4 : 5/1		12700 12705 12710

public class Price
{
	private static Regex regex;

	//property
	private String _normalizedPrice;
	private double _normalizedValue;
	private int _numeratorUnit;
	private int _denominator;

	public int get_NumeratorUnit()
	{
		return this._numeratorUnit;
	}

	public int get_Denominator()
	{
		return this._denominator;
	}

	public String get_NormalizedPrice()
	{
		return this._normalizedPrice;
	}

	public static Price clone(Price price)
	{
		return (price == null)? null:price.clone();
	}

	public Price clone()
	{
		return new Price(this._normalizedPrice, this._normalizedValue, this._numeratorUnit, this._denominator);
	}

	public int compareTo(Price price)
	{
		//Comparable c = new Comparable<Price>;

		return Double.compare(this._normalizedValue, price._normalizedValue);

		/*double error = 0.0000001;
		if ( (this._normalizedValue - price._normalizedValue) < -error)
		{
			return -1;
		}
		else if ( (this._normalizedValue - price._normalizedValue) > error)
		{
			return 1;
		}
		else
		{
			return 0;
		}*/
	}

	static
	{
		RegexCompiler compiler = new RegexCompiler();
		String regexExpression = "(\\d+)(?:\\.(\\d+)(?:/(\\d+)|)|)";
		Price.regex = new Regex(regexExpression, RegexOptions.none);
		Price.regex.set_Program(compiler.compile(regexExpression));
	}

	private Price()
	{
	}

	private Price(String normalizedPrice, double normalizedValue, int numeratorUnit, int denominator)
	{
		this._normalizedPrice = normalizedPrice;
		this._normalizedValue = normalizedValue;
		this._numeratorUnit = numeratorUnit;
		this._denominator = denominator;
	}

	public synchronized static Price create(String price, int numeratorUnit, int denominator)
	{
		NormalizePrice normalizedPrice = Price.normalize(price, numeratorUnit, denominator);
		if (normalizedPrice == null)
		{
			return null;
		}

		return new Price(normalizedPrice.get_PriceString(), normalizedPrice.get_PriceValue(), numeratorUnit, denominator);
	}

	public static Price create(double value, Instrument instrument)
	{
		return Price.create(value, instrument.get_NumeratorUnit(), instrument.get_Denominator());
	}

	public static Price create(double value, int numeratorUnit, int denominator)
	{
		return create(Convert.toString(value), numeratorUnit, denominator);
	}

	public static Price parse(String priceString, int numeratorUnit, int denominator)
	{
		return Price.create(priceString, numeratorUnit, denominator);
	}

	public static String getPriceLeft(Price price)
	{
		return Price.getPriceLeft(Price.toString(price));
	}

	public static String getPriceLeft(String priceString)
	{
		if (StringHelper.isNullOrEmpty(priceString)) return "";

		String priceRight = Price.getPriceRight(priceString);
		if (priceString.length() <= priceRight.length())
		{
			return "";
		}
		String priceLeft = priceString.substring(0,priceString.length() - priceRight.length());
		int priceLeftLength = priceLeft.length();
		if (priceLeftLength > 0 && priceLeft.substring(priceLeft.length()-1).equals("."))
		{
			priceLeft = priceLeft.substring(0,priceLeft.length()-1);
		}
		return priceLeft;
	}

	public static String getPriceRight(Price price)
	{
		return Price.getPriceRight(Price.toString(price));
	}

	public static String getPriceRight(String priceString)
	{
		if (StringHelper.isNullOrEmpty(priceString)) return "";

		int iLength = priceString.length();
		if (iLength >= 2)
		{
			String rightChars = priceString.substring(iLength - 2, iLength);
			priceString = rightChars;
			if (rightChars.lastIndexOf(".") > 0)
			{
				if (iLength >= 3)
				{
					priceString = "0" + priceString.substring(iLength - 3, iLength);
				}
				else
				{
					priceString = "0" + rightChars;
				}
			}
		}
		return priceString;
	}

	public static String toString(Price price)
	{
		//Special process
		if (price == null)
		{
			return "";
		}

		return (price._normalizedPrice == null) ? "" : price._normalizedPrice;
	}

	public static double toDouble(Price price)
	{
		//Special process
		if (price == null)
		{
			return 0.0;
		}

		return price._normalizedValue;
	}

	public static float toFloat(Price price)
	{
		//Special process
		if (price == null)
		{
			return 0.0f;
		}

		return new Double(price._normalizedValue).floatValue();
	}

	public static BigDecimal toBigDecimal(Price price)
	{
		return new BigDecimal(price._normalizedValue);
	}

	public static Price add(Price price1, int points)
	{
		double value = price1._normalizedValue + points / (new BigDecimal(Convert.toString(price1._denominator))).doubleValue();
		return Price.create(value, price1._numeratorUnit, price1._denominator);
	}

	public static Price subStract(Price price1, int points)
	{
		return Price.add(price1, -points);
	}

	public static int subStract(Price price1, Price price2)
	{
		if (!Price.isSameType(price1, price2))
		{
			return 0;
		}
		long result = Math.round( (price1._normalizedValue - price2._normalizedValue) * price1._denominator);
		return new Long(result).intValue();
	}

	public static double valueSubStract(Price price1, Price price2)
	{
		if (!Price.isSameType(price1, price2))
		{
			return 0;
		}

		return price1._normalizedValue - price2._normalizedValue;
	}

	public static Price avg(Price price1, Price price2)
	{
		if (!Price.isSameType(price1, price2))
		{
			return null;
		}

		return Price.create( (price1._normalizedValue + price2._normalizedValue) / 2, price1._numeratorUnit, price1._denominator);
	}

	public int getHashCode()
	{
		return this._normalizedPrice.hashCode();
	}

	public boolean equals(Object obj)
	{
		if (obj == null || this.getClass() != obj.getClass())
		{
			return false;
		}

		return this.equals(this, (Price) obj);
	}

	public static boolean equals(Price price1, Price price2)
	{
		if (!Price.isSameType(price1, price2))
		{
			return false;
		}

		if ( (Object) price1 == null && (Object) price2 == null)
		{
			return true;
		}
		if ( (Object) price1 == null ^ (Object) price2 == null)
		{
			return false;
		}

		return price1._normalizedValue == price2._normalizedValue;
	}

	public static boolean more(Price price1, Price price2)
	{
		if (!Price.isSameType(price1, price2))
		{
			return false;
		}

		return price1._normalizedValue > price2._normalizedValue;
	}

	public static boolean less(Price price1, Price price2)
	{
		if (!Price.isSameType(price1, price2))
		{
			return false;
		}

		return price1._normalizedValue < price2._normalizedValue;
	}

	public static int compare(Price price1, Price price2, boolean isReciprocal) //Reciprocal:
	{
		if (price1 == null && price2 == null)
		{
			return 0;
		}
		else if (price1 != null)
		{
			return price1.compareTo(price2) * (isReciprocal ? -1 : 1);
		}
		else
		{
			return price2.compareTo(price1) * (isReciprocal ? 1 : -1);
		}
	}

	private static boolean isSameType(Price price1, Price price2)
	{
		boolean isSameType = true;

		if (price1 == null || price2 == null)
		{
			return false;
		}

		if (price1._numeratorUnit != price2._numeratorUnit || price1._denominator != price2._denominator)
		{
			isSameType = false;
			throw new FrameworkException("Prices are not belong to the same instrument");
		}

		return isSameType;
	}

	protected static NormalizePrice normalize(String price, int numeratorUnit, int denominator)
	{
		double normalizedValue = 0.0;
		String normalizedPrice = null;
		if (StringHelper.isNullOrEmpty(price))// || price.trim().equals("0"))
		{
			return null; //normalizedPrice;
		}

		Match match = Price.regex.match(price);
		if (!match.get_Success())
		{
			return null; //normalizedPrice;
		}

		//get wholePart
		int wholePart = 0;
		if (Price.regex.get_ParenCount() > 0)
		{
			try
			{
				wholePart = Convert.toInt32(Price.regex.get_Paren(1));
			}
			catch (Exception ex)
			{
				System.out.println(price);
			}
		}
		if (numeratorUnit >= denominator) // priceStyle1,priceStyle4
		{
			//normalize wholePart to multiple of this.numeratorUnit
			 normalizedValue = (Math.round ((float)wholePart / numeratorUnit)) * numeratorUnit;
			//normalizedPrice = Convert.toString(normalizedValue); //( (int) normalizedValue).toString(CultureInfo.InvariantCulture);
			String normalizedPriceTemp = Convert.toString(normalizedValue);
			int index = normalizedPriceTemp.indexOf(".");
			if (index > -1)
			{
				normalizedPrice = normalizedPriceTemp.substring(0,index);
			}
			else
			{
				normalizedPrice = normalizedPriceTemp;
			}
		}
		else
		{
			//process decimal part
			String numeratorString = "";
			String denominatorString = "";
			if (Price.regex.get_ParenCount() >= 3)
			{
				numeratorString = Price.regex.get_Paren(2);
				denominatorString = Price.regex.get_Paren(3);
			}
			int numerator = 0;
			if (!StringHelper.isNullOrEmpty(numeratorString))
			{
				//Specimal process
				if (Double.valueOf(numeratorString) >= Integer.MAX_VALUE || Double.valueOf(numeratorString) <= Integer.MIN_VALUE)
				{
					numeratorString = numeratorString.substring(0, 8);
				}
				numerator = Integer.parseInt(numeratorString, 10);
			}
			double decimalPart;
			if (!StringHelper.isNullOrEmpty(denominatorString))
			{
				if (Integer.parseInt(denominatorString, 10) == 0)
				{
					return null;
				}

				//"127.015/16" is invalid but 127.000/16 is valid
				if (numerator != 0 && numeratorString.substring(0, 1).equals("0"))
				{
					return null; //normalizedPrice;
				}
				decimalPart = numerator / (double) Integer.parseInt(denominatorString, 10);
			}
			else
			{
				decimalPart = Convert.toDouble("0." + numeratorString);
			}

			//handle convertion and calculation error
			//numerator = (int) (decimalPart * denominator + 0.15);
			String decimalPartString = Convert.toString(decimalPart * denominator + 0.15);
			numerator = Integer.parseInt(decimalPartString.substring(0, decimalPartString.indexOf(".")));
			if (numerator < denominator)
			{
				//normalize numerator to multiple of this.numeratorUnit
				numerator = (Math.round((float)numerator / numeratorUnit)) * numeratorUnit;
				//????????????
				//TradingConsole.traceSource.trace(TraceType.Information, Convert.toString(denominator));

				normalizedValue = (double) (wholePart + numerator / (new java.math.BigDecimal(Convert.toString(denominator))).doubleValue());

				//Denominator is 10,100....
				/*
				  double power10 = Math.log10(denominator);
				  if (power10 == (int) power10)
				  {
				 normalizedPrice = normalizedValue.toString("F" + power10.toString(CultureInfo.InvariantCulture), CultureInfo.InvariantCulture);
				 normalizedValue = Convert.toDouble(normalizedPrice);// CultureInfo.InvariantCulture);
				 */
				double result = Math.log10(denominator);
				//if (denominator == 10 || denominator == 100 || denominator == 1000 || denominator == 10000)
				//var power10=Math.log(denominator)/Math.log(10);
				//if(Math.pow(10,Math.round(power10)) == denominator)
				if(((int)result) == result)
				{
					int iCount = (Convert.toString(denominator)).length() - 1;
					normalizedPrice = AppToolkit.format2(normalizedValue, iCount);
					normalizedValue = Convert.toDouble(normalizedPrice);
				}
				else
				{
					if (numerator == 0)
					{
						//??????????
						normalizedPrice = Convert.toString(wholePart); //.toString(CultureInfo.InvariantCulture);
						normalizedValue = Convert.toDouble(normalizedPrice); //, CultureInfo.InvariantCulture);
					}
					else
					{
						normalizedPrice = Convert.toString(wholePart) + "." + Convert.toString(numerator) + "/" + Convert.toString(denominator);
					}
				}
			}
		}
		NormalizePrice normalizePrice2 = new NormalizePrice();
		normalizePrice2.set_PriceString(normalizedPrice);
		normalizePrice2.set_PriceValue(normalizedValue);
		return normalizePrice2;
		//return (normalizedValue == 0 ? null : normalizedPrice);
	}
}

class NormalizePrice
{
	private String _priceString;
	private double _priceValue;

	public String get_PriceString()
	{
		return this._priceString;
	}

	public void set_PriceString(String value)
	{
		this._priceString = value;
	}

	public double get_PriceValue()
	{
		return this._priceValue;
	}

	public void set_PriceValue(double value)
	{
		this._priceValue = value;
	}

	public NormalizePrice()
	{
		this.clear();
	}

	public void clear()
	{
		this._priceString = null;
		this._priceValue = 0.00;
	}
}
