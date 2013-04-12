package tradingConsole.setup;

import java.io.IOException;

public class WebStartLauncher
{
	private static final String _uninstallOption = "-uninstall";

	//args:
	//1: the jnlp url
	//2: none or "u", none means install the application, "u" to uninstall
	public static void main(String[] args) throws IOException, InterruptedException
	{
		String jnlpUrl = args[0];
		boolean isInstall = true;
		if(args.length == 2 && args[1].compareToIgnoreCase("u") == 0)
		{
			isInstall = false;
		}

		ProcessBuilder processBuilder = null;
		String command = WebStartLauncher.getWebStartProgramFullPath();
		if(isInstall)
		{
			processBuilder = new ProcessBuilder(command, jnlpUrl);
		}
		else
		{
			processBuilder = new ProcessBuilder(command, WebStartLauncher._uninstallOption, jnlpUrl);
		}
		processBuilder.start();
	}

	private static String getWebStartProgramFullPath()
	{
		/*String javaHome = (String) System.getProperties().get("java.home");
		String osName = (String) System.getProperties().get("os.name");
		String separator = (String) System.getProperties().get("file.separator");*/

		return "javaws";
	}
}
