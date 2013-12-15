/**
 * 
 */
package org.iiitb.drp;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author kempa
 * 
 */
public final class MyLogger
{
	private static Logger loggerInstance = null;

	private MyLogger() {}
	
	public static Logger getInstance() throws SecurityException, IOException
	{
		if (loggerInstance == null)
		{
			loggerInstance = Logger.getLogger(DialARide.class.getName());
			Handler fileHandler = new FileHandler("log.txt");
			loggerInstance.setUseParentHandlers(false);
			fileHandler.setFormatter(new LogFileFormatter());
			loggerInstance.addHandler(fileHandler);
			loggerInstance.setLevel(Level.INFO);
		}
		return loggerInstance;
	}

}

class LogFileFormatter extends Formatter
{
	    @Override
	    public String format(LogRecord record) {
	        return record.getMessage() + "\n";
	    }
	
}
