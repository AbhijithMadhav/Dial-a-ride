package org.iiitb.drp;

/**
 * 
 */

/**
 * @author kempa
 * 
 */
public class PickUpTimingConstraintViolation extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6668960144054714571L;

	public PickUpTimingConstraintViolation(Stop s)
	{
		super("Timing constraint of request " + s.requestId + ": " + s.et
				+ "---" + s.lt + " not met. Actual time assigned : " + s.at);
	}

}
