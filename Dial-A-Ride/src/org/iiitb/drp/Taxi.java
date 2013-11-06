package org.iiitb.drp;

import org.iiitb.graphs.EdgeWeightedDigraph;

public class Taxi
{
	static int counter = 0;
	int id;
	TaxiSchedule schedule;
	Stop startPoint;

	public Taxi(int startPoint, int capacity, EdgeWeightedDigraph cityMap)
	{
		id = ++counter;
		this.startPoint = new Stop(startPoint, DialARide.dayStartTime,
				DialARide.dayStartTime, -1, StopType.TAXI_LOCATION, cityMap);
		schedule = new TaxiSchedule(this.startPoint, capacity, id);
	}

	public boolean schedule(Request r)
	{
		return schedule.insertRequest(r);
	}
	
	public String toString()
	{
		return "Taxi " + id + " : " + startPoint;
	}

	public int dist(int time, Stop dest)
	{
		if (schedule.route.isEmpty())
			return startPoint.distTo(dest);
		
		if (time < schedule.route.getFirst().et)
			return schedule.route.getFirst().distTo(dest);
		
		for (Stop s: schedule.route)
			if (time > s.et)
				return s.distTo(dest);
		return schedule.route.getLast().distTo(dest);	
			
	}
}


