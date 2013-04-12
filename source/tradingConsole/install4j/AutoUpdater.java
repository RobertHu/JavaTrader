package tradingConsole.install4j;

import java.io.*;
import java.net.*;

import com.install4j.api.context.*;
import com.install4j.api.update.*;

public class AutoUpdater
{
	private boolean _hasNewVersion;

	public boolean get_HasNewVersion()
	{
		return this._hasNewVersion;
	}

	public void checkAndUpdate(String updateXmlUrl, String installFileFullPath) throws IOException, UserCanceledException
	{
		UpdateDescriptor updateDescriptor = UpdateChecker.getUpdateDescriptor(updateXmlUrl, ApplicationDisplayMode.GUI);
		UpdateDescriptorEntry updateDescriptorEntry = updateDescriptor.getPossibleUpdateEntry();
		this._hasNewVersion = updateDescriptorEntry != null;

		if (updateDescriptorEntry != null)
		{
			URL url = updateDescriptorEntry.getURL();
			InputStream inputStream = url.openStream();
			FileOutputStream fos = new FileOutputStream(installFileFullPath, false);

			byte[] buffer = new byte[4096];
			int len = inputStream.read(buffer);
			while (len != -1)
			{
				fos.write(buffer, 0, len);
				len = inputStream.read(buffer);
			}

			fos.close();
			inputStream.close();

			Runtime.getRuntime().exec(installFileFullPath);
		}
	}
}
