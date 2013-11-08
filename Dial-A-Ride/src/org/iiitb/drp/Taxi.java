package org.iiitb.drp;

import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.iiitb.graphs.EdgeWeightedDigraph;

public class Taxi
{
	private static int counter = 0;
	private int id;
	private Stop startPoint;
	private int capacity;
	LinkedList<Stop> route;
	private Logger logger;
	int revenue;

	public Taxi(int startPoint, int capacity, EdgeWeightedDigraph cityMap)
			throws SecurityException, IOException
	{
		id = ++counter;
		this.capacity = capacity;
		this.startPoint = new Stop(startPoint, DialARide.dayStartTime,
				DialARide.dayStartTime, -1, StopType.TAXI_LOCATION, cityMap);
		route = new LinkedList<Stop>();
		logger = MyLogger.getInstance();
		revenue = 0;
	}

	/**
	 * Attempt insertion of a request into this taxi route.
	 * 
	 * @param r The request to be inserted
	 * @return Was the insertion successful?
	 */
	public boolean schedule(Request r)
	{
		Stop pickUpPoint = r.pickUp;
		Stop dropPoint = r.drop;

		logger.info("\nAttempting to service " + r + " through taxi " + id);
		if (route.isEmpty())
		{
			if (insertStop(0, pickUpPoint))
				if (insertStop(route.size(), dropPoint))
				{
					revenue += pickUpPoint.distTo(dropPoint) * DialARide.ratePerKm;
					return true;
				}
			while (!route.isEmpty())
				route.remove();
			return false;
		}

		// Attempt inserting pickUpPoint into the route
		int nPassengers = 0;
		for (int i = 0; i <= route.size(); i++)
		{// No iterator as there is a need to modify list
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
			for (Stop s : route)
				s.b_at = s.at;
			int tmpNPassengers = nPassengers;

			// Attempt insertion of pickUpPoint before nextStop
			int insertPosition = route.size();
			if (i < route.size())
				insertPosition = route.indexOf(nextStop);
			if (!insertStop(insertPosition, pickUpPoint))
			{
				for (Stop s : route)
					s.b_at = s.at;
				continue;
			}

			// Find a stop after pickUpPoint before which insertion of dropPoint
			// can be attempted
			Stop nextOfDropPoint = null;
			prevStop = pickUpPoint;
			boolean exceededCapacity = false;
			for (int k = insertPosition + 1; k < route.size(); k++, prevStop = nextOfDropPoint)
			{// not using iterator as I need to modify the list

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
					route.remove(pickUpPoint);
					for (Stop s : route)
						s.at = s.b_at;
					nPassengers = tmpNPassengers;

					logger.info("Exceeded capacity. Passengers are "
							+ nPassengers + " between stops " + prevStop
							+ " and " + nextOfDropPoint
							+ ". Need reinsert pickup point, " + pickUpPoint
							+ ", elsewhere");
					exceededCapacity = true;
					break;
				}

				// Attempt insertion of dropPoint before nextOfDropPoint
				if (!insertStop(route.indexOf(nextOfDropPoint), dropPoint))
				{
					// Try in further positions
					continue;
				}
				revenue += pickUpPoint.distTo(dropPoint) * DialARide.ratePerKm;
				return true;
			}
			if (!exceededCapacity)
			{
				// Try inserting dropPoint in last position
				if (insertStop(route.size(), dropPoint))
				{
					revenue += pickUpPoint.distTo(dropPoint) * DialARide.ratePerKm;
					return true;
				}
				else
				{
					route.remove(pickUpPoint);
					route.remove(dropPoint);
					for (Stop s : route)
						s.at = s.b_at;
					nPassengers = tmpNPassengers;
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
		Stop tmpStop = new Stop(stop.location, stop.et, stop.lt,
				stop.requestId, stop.stopType, stop.g);

		if (position > route.size() || position < 0)
			return false;

		if (position == 0)
		{
			assert stop.type() == StopType.PICKUP;
			logger.info("Attempting to add " + tmpStop + " into taxi " + id
					+ " in the first position");

			// Can taxi reach pickup point before its window closes?
			if (startPoint.at + shortestTime(startPoint, stop) > stop.lt)
			{
				logger.info("Can't reach " + stop.location + " from "
						+ startPoint + " in time(Need "
						+ shortestTime(startPoint, stop) + " min)");
				return false;
			}
			stop.at = startPoint.at + shortestTime(startPoint, stop);
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
					{
						logger.info("Can't postpone future scheduled stops");
						// To do : Remove a lesser revenue future scheduled stop
						return false;
					}
				}
			}
		}
		else if (position == route.size())
		{// To do : merge this with the else block
			Stop prevStop = route.getLast();
			logger.info("Attempting to add " + tmpStop + " into taxi " + id
					+ " in the last position, after " + prevStop);
			if (prevStop.at > stop.lt)
			{
				logger.info("Previous stop AT > LT");
				return false;
			}

			if (prevStop.at + shortestTime(prevStop, stop) > stop.lt)
			{
				logger.info("Can't reach " + stop.location + " from "
						+ prevStop.location + " in time(Need "
						+ shortestTime(prevStop, stop) + " min)");
				return false;
			}

			stop.at = prevStop.at + shortestTime(prevStop, stop);
			if (stop.at < stop.et)
				stop.at = stop.et;
		}
		else
		{
			Stop prevStop = route.get(position - 1);
			assert prevStop != null;
			Stop nextStop = route.get(position);

			logger.info("Attempting to add " + tmpStop + " into taxi " + id
					+ " between " + prevStop + " and " + nextStop);

			if (prevStop.at > stop.lt)
			{
				logger.info("Previous stop AT > LT");
				return false;
			}
			
			if (prevStop.location == nextStop.location
					&& prevStop.at == nextStop.at)
			{
				logger.info("Not scheduling " + stop
						+ " inbetween stops having the same location");
				return false; // Don't schedule a request in between
								// pickup/drops at the same location
			}
			

			// Can taxi make it from prevStop to this stop within time
			// constraints
			if (prevStop.at + shortestTime(prevStop, stop) > stop.lt)
			{
				logger.info("Can't reach " + stop.location + " from "
						+ prevStop.location + " in time(Need "
						+ shortestTime(prevStop, stop) + " min)");
				return false;
			}

			stop.at = prevStop.at + shortestTime(prevStop, stop);
			if (stop.at < stop.et)
				stop.at = stop.et;

			// Can taxi make it from this stop to the next with
			int t = stop.at + shortestTime(stop, nextStop);
			if (t > nextStop.lt)
			{
				logger.info("Can't reach " + nextStop.location + " from "
						+ stop.location + " in time(Need "
						+ shortestTime(stop, nextStop) + " min)");
				return false;
			}

			// If this stop is not on the way from previous stop to the next
			// stop will have to adjust the actual pickup time of all subsequent
			// stops
			if (nextStop.at < t)
				if (!postponeAllFromStop(nextStop, stop))
				{
					logger.info("Can postpone future scheduled stops");
					return false;
				}
		}
		route.add(position, stop);
		checkScheduleAfterInsertion(stop);

		logger.info("Added " + tmpStop + " to taxi " + id);
		logger.info(toString());
		return true;
	}

	public void checkScheduleAfterInsertion(Stop stop)
	{
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
	}

	public void check()
	{
		Stop p = startPoint;
		int nPassengers = 0;
		for (Stop s : route)
		{
			// Distance between two stops is atleast the shortest path time
			if (s.at - p.at < shortestTime(p, s))
			{
				System.err.println(this);
				System.err
						.println("Travel time from " + p.location + " to "
								+ s.location + " must be atleast "
								+ shortestTime(p, s));
				System.err.println("Listed time is " + (s.at - p.at)
						+ " though");
				System.exit(2);
			}

			// No. of passengers does not exceed capacity
			if (s.stopType == StopType.PICKUP)
				nPassengers++;
			else
				nPassengers--;
			if (nPassengers > capacity)
			{
				System.err.println();
				System.err
						.println("Schedule of this taxi exceeds capacity after accomadation of last request : "
								+ s.requestId + " " + nPassengers);
				System.exit(1);
			}

			// Time constraints are obeyed
			if (s.at < s.et || s.at > s.lt)
			{
				System.err.println("Timing constraint of request "
						+ s.requestId + ": " + s.et + "---" + s.lt
						+ " not met. Actual time assigned : " + s.at);
				System.exit(1);
			}
			p = s;
		}
	}

	int shortestTime(Stop v, Stop w)
	{
		return (int) (v.distTo(w)) * DialARide.timePerKm;
	}

	private boolean canPostpone(Stop stop, Stop prevStop)
	{
		Stop p = prevStop;
		int sAt, pAt = prevStop.at;
		for (int i = route.indexOf(stop); i < route.size(); i++)
		{
			Stop s = route.get(i);
			sAt = pAt + shortestTime(p, s);
			if (sAt > s.lt)
				return false;
			if (sAt < s.et)
				sAt = s.et;
			p = s;
			pAt = sAt;
		}
		return true;
	}

	private boolean postponeAllFromStop(Stop stop, Stop prevStop)
	{
		if (!canPostpone(stop, prevStop))
			return false;

		for (int i = route.indexOf(stop); i < route.size(); i++)
		{
			Stop s = route.get(i);
			s.at = prevStop.at + shortestTime(prevStop, s);
			if (s.at < s.et)
				s.at = s.et;
			assert s.at <= s.lt; // did
			prevStop = s;
		}
		return true;
	}

	public String toString()
	{
		String str = "Taxi " + id + " : " + startPoint;
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

	public int dist(int time, Stop dest)
	{
		if (route.isEmpty())
			return startPoint.distTo(dest);

		if (time < route.getFirst().et)
			return route.getFirst().distTo(dest);

		for (Stop s : route)
			if (time > s.et)
				return s.distTo(dest);
		return route.getLast().distTo(dest);

	}

	public int distanceTravelled()
	{
		int distance = 0;
		Stop p = startPoint;
		for (Stop s : route)
		{
			distance += p.distTo(s);
			p = s;
		}
		return distance;
	}

	public int idleTime()
	{
		int idleTime = 0;
		Stop p = startPoint;
		for (Stop s : route)
		{
			idleTime += (s.at - p.at) - DialARide.timePerKm * p.distTo(s);
			p = s;
		}
		idleTime += DialARide.dayEndTime - route.getLast().at;
		return idleTime;
	}
}
