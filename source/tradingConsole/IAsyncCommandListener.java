package tradingConsole;

import framework.Guid;

public interface IAsyncCommandListener
{
	void asyncCommandCompleted(Guid asyncResultId, String methodName, boolean failed, String errorMessage);
	Guid getAsyncResultId();
}
