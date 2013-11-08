package org.iiitb.drp;

import org.iiitb.graphs.DijkstraSP;
import org.iiitb.graphs.EdgeWeightedDigraph;

/**
 * Models a pickup or drop in the schedule of a taxi.
 * 
 * @author kempa
 * 
 */
public class Stop
{
	int location;
	int requestId; // requester of the stop?
	StopType stopType;
	private DijkstraSP sp;
	EdgeWeightedDigraph g; // Needed only for clone()

	final int et; // earliest pickup/delivery time
	final int lt; // latest pickup/delivery time
	int at; // actual pickup/delivery time
	
	int b_at;

	public Stop(int location, int et, int lt, int requestId, StopType stopType,
			EdgeWeightedDigraph g)
	{
		this.location = location;
		this.stopType = stopType;
		this.et = et;
		this.lt = lt;
		this.requestId = requestId;
		this.at = et;
		this.sp = new DijkstraSP(g, location);
		this.g = g;
	}

	public StopType type()
	{
		return stopType;
	}

	public int distTo(Stop w)
	{
		return (int) sp.distTo(w.location);
	}

	public String toString()
	{
		String str = "";
		if (stopType == StopType.PICKUP)
			str += "+";
		else
			str += "-";
		
		return str + requestId + "("
				+ location + ") : [" + et + "-" + at + "-" + lt + "]" ;
	}
	
	protected Object clone()
	{
		Stop s = new Stop(location, et, lt, requestId, stopType, g);
		s.at = at;
		return s;
	}

}

enum StopType {
	PICKUP, DROP, TAXI_LOCATION
}