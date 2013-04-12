package tradingConsole;

import java.nio.channels.FileLock;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import framework.threading.Mutex;
import framework.FrameworkException;
import java.io.File;

public class Mutex2  extends Mutex
{
  private static final int _sleepTime = 16;

  private String _name;
  private File _file;
  private FileChannel _fileChannel;
  private FileLock _fileLock;

  public Mutex2(int port)
  {
	super(port);
	this._name = "TradingConsole_Mutex2_" + port;
  }

  public synchronized boolean waitOne()
  {
	return this.waitOne( -1, true);
  }

  synchronized public boolean waitOne(int millisecondsTimeout,
									  boolean exitContext)
  {
	boolean holdLock = false;
	int remainTime = millisecondsTimeout == -1 ? Integer.MAX_VALUE :
		millisecondsTimeout;
	try {
	  if (this._file == null) {
		String path = System.getProperty("AppDir");//System.getProperty("user.home");
		this._file = new File(path, this._name);
		if (!this._file.exists())
		{
		  this._file.createNewFile();
		}
		this._fileChannel = new RandomAccessFile(this._file, "rw").getChannel();
	  }
	}
	catch (Exception exception)
	{
	  throw new FrameworkException(exception);
	}

	while (!holdLock && remainTime >= 0) {
	  try {
		this._fileLock = this._fileChannel.lock();
		holdLock = true;
	  }
	  catch (OverlappingFileLockException fileException) {
		Mutex2.sleep(Mutex2._sleepTime);

		if (millisecondsTimeout != -1) {
		  remainTime -= Mutex2._sleepTime;
		}
	  }
	  catch (Exception exception) {
		throw new FrameworkException(exception);
	  }
	}

	return holdLock;
  }

  synchronized public void releaseMutex() {
	try {
	  this._fileLock.release();
	}
	catch (IOException exception) {
	  System.out.println(exception);
	}
  }

  private static void sleep(int time) {
	try {
	  Thread.sleep(time);
	}
	catch (Exception exception) {
	  System.out.println(exception);
	}
  }
}
