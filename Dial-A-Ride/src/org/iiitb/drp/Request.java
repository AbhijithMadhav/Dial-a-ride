package org.iiitb.drp;

/**
 * Models a service request
 * @author kempa
 *
 */
public class Request implements Comparable<Request>
{
	private int id;
	Stop pickUp;
	Stop drop;
	private int spCost;

	public Request(Stop pickUp, Stop drop)
	{
		id = pickUp.requestId;
		this.pickUp = pickUp;
		this.drop = drop;
		spCost = pickUp.distTo(drop) * DialARide.ratePerKm;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Request r)
	{
		Stop l = ((Request)r).pickUp;
		 return pickUp.et - l.et; // Based on earliest pickUp time
		//return (spCost - r.spCost); // Based on revenue
		// return ((pickUp.lt - pickUp.et)- (l.lt - l.et)); // Based on pickup interval
	}
	
	public int getCost()
	{
		return spCost;
	}
	
	public String toString()
	{
		return "Request " + id + " : " + pickUp.location
				+ " " + drop.location + " " + pickUp.et + " "
				+ pickUp.lt;
	}

}