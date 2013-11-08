/**
 * 
 */
package org.iiitb.drp;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author kempa
 *
 */
public class LogFileFormatter extends Formatter
{
	    @Override
	    public String format(LogRecord record) {
	        return record.getMessage() + "\n";
	    }
	
}
