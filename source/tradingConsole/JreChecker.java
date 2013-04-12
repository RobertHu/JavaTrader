package tradingConsole;

import java.applet.Applet;
import java.net.URL;

public class JreChecker extends Applet
{
	private static final String _versionSeparator = "\\.";
	private static final String _unavailableJreSeparator = ";";

	private static final String _jreVersionKey = "java.version";

	private static final String _appDownloadUrlKey = "AppDownloadUrl";
	private static final String _jreDownloadUrlKey = "JreDownloadUrl";
	private static final String _isAbsoluteJreDownloadUrlKey = "IsAbsoluteJreDownloadUrl";
	private static final String _unavailableJresKey = "UnavailableJres";

	private String[] _unavailableJres;
	private int _major;
	private int _minor;

	private static final long serialVersionUID = 1;

	public JreChecker()
	{
	}

	public void start()
	{
		try
		{
			URL codeBase = this.getCodeBase();

			String appDownloadUrl = this.getParameter(JreChecker._appDownloadUrlKey);
			System.out.println(JreChecker._appDownloadUrlKey + " = " + appDownloadUrl);

			String jreDownloadUrl = this.getParameter(JreChecker._jreDownloadUrlKey);
			System.out.println(JreChecker._jreDownloadUrlKey + " = " + jreDownloadUrl);

			boolean isAbsoluteJreDownloadUrl = Boolean.parseBoolean(this.getParameter(JreChecker._isAbsoluteJreDownloadUrlKey));
			System.out.println(JreChecker._isAbsoluteJreDownloadUrlKey + " = " + isAbsoluteJreDownloadUrl);

			String unavailableJres = this.getParameter(JreChecker._unavailableJresKey);
			System.out.println(JreChecker._unavailableJresKey + " = " + unavailableJres);
			if(unavailableJres != null && unavailableJres.length() > 0)
			{
				this._unavailableJres = unavailableJres.split(JreChecker._unavailableJreSeparator);
				for(String value : this._unavailableJres)
				{
					System.out.println(value);
				}
			}

			String jreVersion = System.getProperty(JreChecker._jreVersionKey);
			System.out.println(JreChecker._jreVersionKey + " = " + jreVersion);
			this.fillVersionInfo(jreVersion);

			if (this._major >= 1 && this._minor >= 5 && this.isAvailableJre(jreVersion))
			{
				this.getAppletContext().showDocument(new URL(codeBase.toString() + appDownloadUrl));
			}
			else
			{
				URL url = isAbsoluteJreDownloadUrl ? new URL(jreDownloadUrl) : new URL(codeBase.toString() + jreDownloadUrl);
				this.getAppletContext().showDocument(url);
			}
		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}

	private boolean isAvailableJre(String jreVersion)
	{
		if(this._unavailableJres == null) return true;

		for(String value : this._unavailableJres)
		{
			if(value.startsWith(jreVersion)) return false;
		}
		return true;
	}

	private void fillVersionInfo(String jreVersion)
	{
		String[] values = jreVersion.split(JreChecker._versionSeparator);
		this._major = values.length > 0 ? Integer.parseInt(values[0]) : 0;
		this._minor = values.length > 1 ? Integer.parseInt(values[1]) : 0;

		System.out.println("major = " + this._major);
		System.out.println("minor = " + this._minor);
	}
}
