package org.iiitb.drp;

import java.util.LinkedList;

/**
 * Models the schedule of a taxi
 * 
 * @author kempa
 * 
 */
public class TaxiSchedule
{
	Stop startPoint;
	LinkedList<Stop> route;
	int capacity;
	int taxiId;

	public TaxiSchedule(Stop startPoint, int capacity, int taxiId)
	{
		this.startPoint = startPoint; // not inserting starting point into
										// route
		this.capacity = capacity;
		route = new LinkedList<Stop>();
		this.taxiId = taxiId;
	}

	/**
	 * Attempt insertion of a request into this taxi route.
	 * 
	 * @param r The request to be inserted
	 * @return Was the insertion successful?
	 */
	public boolean insertRequest(Request r)
	{
		Stop pickUpPoint = r.pickUp;
		Stop dropPoint = r.drop;

		if (route.isEmpty())
		{
			if (insertStop(0, pickUpPoint))
				if (insertStop(route.size(), dropPoint))
					return true;
			while (!route.isEmpty())
				route.remove();
			return false;
		}

		// Attempt inserting pickUpPoint into the route
		int nPassengers = 0;
		for (int i = 0; i <= route.size(); i++)// No iterator. Need to modify
												// list
		{
			Stop prevStop = null;
			if (i != 0)
			{
				prevStop = route.get(i - 1);
				// Count passengers still in the car before nextStop
				if (prevStop.type() == StopType.PICKUP)
					nPassengers++;
				else
					nPassengers--;
			}
			if (nPassengers >= capacity)
				continue; // try inserting after a later stop. Maybe somebody
							// would have been dropped

			Stop nextStop = null;
			if (i < route.size())
			{
				nextStop = route.get(i);
				if (pickUpPoint.et > nextStop.lt)
					continue;
			}

			// Backup 'taxi state' as might have to revert back if insertion of
			// pickUpPoint is unsuccessful(Due to taxi overflow discovered while
			// trying to insert dropPoint)
			LinkedList<Stop> tmpRoute = new LinkedList<Stop>(route); // To do : WRONG
			for (Stop s : tmpRoute)
			{ // backup
				s.b_at = s.at;
				s.b_et = s.et;
				s.b_lt = s.lt;
			}
				
			// Very inefficient
			//LinkedList<Stop> tmpRoute = new LinkedList<Stop>();
			//for (Stop s : route)
			//{
				//tmpRoute.add((Stop) s.clone()); // Deep copy of list
				//count++;
			//}
			int tmpNPassengers = nPassengers;

			// Attempt insertion of pickUpPoint before nextStop
			int insertPosition = route.size();
			if (i < route.size())
				insertPosition = route.indexOf(nextStop);
			if (!insertStop(insertPosition, pickUpPoint))
			{
				// To do : restore??
				continue;
			}

			// Find a stop after pickUpPoint before which insertion of dropPoint
			// can be attempted
			Stop nextOfDropPoint = null;
			prevStop = pickUpPoint;
			boolean exceededCapacity = false;
			for (int k = insertPosition + 1; k < route.size(); k++, prevStop = nextOfDropPoint)
			// not using iterator as I need to modify the list
			{
				nextOfDropPoint = route.get(k);

				// Continue counting passengers
				if (prevStop.type() == StopType.PICKUP)
					nPassengers++;
				else
					nPassengers--;

				if (nPassengers > capacity)
				{
					// The insertion of pickupPoint is resulting in an
					// earlier scheduled passenger getting elbowed out. Try
					// inserting the pickUp point at a later stop
					// Restore taxi-state
					route = tmpRoute;
					for (Stop s : route)
					{// restore
						s.at = s.b_at;
						s.et = s.b_et;
						s.lt = s.b_lt;
					}
					nPassengers = tmpNPassengers;
					exceededCapacity = true;
					break;
				}

				// Attempt insertion of dropPoint before nextOfDropPoint
				if (!insertStop(route.indexOf(nextOfDropPoint), dropPoint))
				{
					// To do : Insert using log4j
			//		System.out.println("Could not insert drop : " + dropPoint + "before " + nextOfDropPoint + ". Trying in the next position");
				//	System.out.println();
					// To do : restore
					continue;
				}
				return true;
			}
			if (!exceededCapacity)
			{

				if (insertStop(route.size(), dropPoint))
					return true;
				else
				{
					route = tmpRoute;
					for (Stop s : route)
					{// restore
						s.at = s.b_at;
						s.et = s.b_et;
						s.lt = s.b_lt;
					}
					nPassengers = tmpNPassengers;
					// To do : Insert using log4j
					//System.out.println("Cannot insert drop : " + dropPoint);
					//System.out.println("Trying to restore original schedule by removing " + pickUpPoint);
					//System.out.println(this);
					//System.out.println();
				}

			}
		}
		return false;
	}

	/**
	 * Insert a stop(pickup/drop) into this taxi route.
	 * 
	 * @param stop The pickup or drop request.
	 * @param position The position of the stop in the route.
	 * @param route This taxi route.
	 * @return Was the route able to accommodate the requested stop?
	 */
	boolean insertStop(int position, Stop stop)
	{
		if (position > route.size())
			return false;

		if (position == 0)
		{
			assert stop.type() == StopType.PICKUP;

			stop.at = startPoint.at + shortestTime(startPoint, stop);

			// Can taxi reach pickup point before its window closes?
			if (stop.at > stop.lt)
				return false;

			// Set the actual time of the first pickup to the earliest time
			// possible. Because of this it is not possible to prepone any
			// pickups in the route. They can only be postponed.
			if (stop.at < stop.et)
				stop.at = stop.et;

			// Adjust the actual pickup/drop times of all subsequent stops to
			// reflect their postponed pickup/drop time due to the introduction
			// of this stop into the schedule
			if (!route.isEmpty())
			{
				Stop nextStop = route.get(position);
				if (stop.at + shortestTime(stop, nextStop) > nextStop.at)
				{
					if (!postponeAllFromStop(nextStop, stop))
						return false;
				}
				else if (stop.at + shortestTime(stop, nextStop) < nextStop.at)
					// To do : prepone nestStop
					return false;
			}
		}
		else if (position == route.size())
		{
			stop.at = route.getLast().at + shortestTime(route.getLast(), stop);

			if (stop.at < stop.et)
				stop.at = stop.et; // To do : Taxi idle's here
			else if (stop.at > stop.lt)
				return false;
		}
		else
		{
			Stop prevStop = route.get(position - 1);
			assert prevStop != null;
			Stop nextStop = route.get(position);

			if (prevStop.location == nextStop.location
					&& prevStop.at == nextStop.at)
				return false; // Don't schedule a request in between
								// pickup/drops at the same location
			// To do : What if there is an idle period inbetween??

			stop.at = prevStop.at + shortestTime(prevStop, stop);
			// Can taxi make it from prevStop to this stop within time
			// constraints
			if (stop.at < stop.et)
				stop.at = stop.et; // To do : Taxi idles here
			else if (stop.at > stop.lt)
				return false;

			// Can taxi make it from this stop to the next with
			int t = stop.at + shortestTime(stop, nextStop);
//			if (t < nextStop.at)
//				nextStop.at = t;
			if (t > nextStop.lt) // To do : Taxi idles here
				return false;

			// If this stop is not on the way from previous stop to the next
			// stop will have to adjust the actual pickup time of all subsequent
			// stops
			if (nextStop.at < t)
				if (!postponeAllFromStop(nextStop, stop))
					return false;
		}
		route.add(position, stop);
		
		// to do : insert using log4j
		//System.out.println("Added " + tmpStop + "to taxi " + taxiId);
		//System.out.println("Schedule for taxi " + taxiId + " : " + toString());
		// System.out.println();
		Stop p = startPoint;
		for (Stop s : route)
		{
			if (s.at - p.at < shortestTime(p, s))
			{
				System.err.println("Inserted : " + stop);
				System.err.println(this);
				System.err
						.println("Travel time from " + p.location + " to "
								+ s.location + " must be atleast "
								+ shortestTime(p, s));
				System.err.println("Listed time is " + (s.at - p.at)
						+ " though");
				System.exit(2);
			}
			p = s;
		}
		return true;
	}

	int shortestTime(Stop v, Stop w)
	{
		return (int) (v.distTo(w)) * DialARide.timePerKm;
	}

	private boolean postponeAllFromStop(Stop stop, Stop prevStop)
	{
		// To do : Clean code
		//LinkedList<Stop> tRoute = new LinkedList<>(route);
		Stop p = prevStop;
		int sAt, pAt = prevStop.at;
		for (int i = route.indexOf(stop); i < route.size(); i++)
		{
			Stop s = route.get(i);
			sAt = pAt  + shortestTime(p, s);
			if (sAt > s.lt)
				return false;
			if (sAt < s.et)
				sAt = s.et;
			p = s;
			pAt = sAt;
		}

		for (int i = route.indexOf(stop); i < route.size(); i++)
		{
			Stop s = route.get(i);
			s.at = prevStop.at + shortestTime(prevStop, s);
			if (s.at < s.et)
				s.at = s.et;
			assert s.at <= s.lt;
			prevStop = s;
		}
		return true;
	}

	public String toString()
	{
		String str = "";
		if (route.isEmpty())
			return str;

		str += "(" + startPoint.location + ")";
		Stop p = startPoint;
		for (Stop s : route)
		{
			str += " --(" + shortestTime(p, s);

			if (s.at - p.at > shortestTime(p, s))
				str += " + " + (s.at - p.at - shortestTime(p, s))
						+ "(IDLE) min)--> ";
			else
				str += " min)--> ";

			if (s.type() == StopType.PICKUP)
				str += "+";
			else
				str += "-";
			str += s.requestId + "(" + s.location + " : " + "[" + s.et + "-"
					+ s.at + "-" + s.lt + "])";

			p = s;
			if (route.getLast().equals(s))
				str += " ---IDLE for "
						+ (DialARide.dayEndTime - route.getLast().at)
						+ " min--- ";
		}
		return str;
	}
}