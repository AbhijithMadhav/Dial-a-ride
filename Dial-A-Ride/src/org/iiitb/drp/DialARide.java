package org.iiitb.drp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.iiitb.graphs.DijkstraSP;
import org.iiitb.graphs.DirectedEdge;
import org.iiitb.graphs.EdgeWeightedDigraph;

public class DialARide
{
	EdgeWeightedDigraph cityMap;
	ArrayList<Request> requests;
	LinkedList<Request> unservicedRequests;
	ArrayList<Taxi> taxis;
	int revenue;
	static final int dayStartTime = 0;
	static final int dayEndTime = 24 * 60;
	final static int timePerKm = 2; // minutes
	final static int ratePerKm = 1; // Rupees

	final static double deviationFactor = 2;

	public DialARide(ArrayList<Request> requests, ArrayList<Taxi> taxis,
			EdgeWeightedDigraph cityMap) throws PickUpTimingConstraintViolation
	{
		// sort requests by their earliest time
		this.requests = requests;
		Collections.sort(this.requests);
		unservicedRequests = new LinkedList<Request>(requests);
		revenue = 0;
		this.taxis = taxis;
		this.cityMap = cityMap;
		
		schedule1();
	/*	int size;
		do
		{
			size = unservicedRequests.size();
			System.out.println("Unserviced Requests : " + size);
			for (Taxi taxi : taxis)
			{
				for (int i = 0; i < unservicedRequests.size();)
				{
					Request request = unservicedRequests.get(i);
					if (taxi.schedule(request))
					{
						revenue += request.spCost;
						unservicedRequests.remove(i);
					}
					else
						i++;
				}
			}
		} while (unservicedRequests.size() < size);*/


	}

	// Assumes that requests are ordered by their earliest pickup time
	public void schedule1()
	{
		// Method 1
		for (Request request : requests)
			for (Taxi taxi : taxis)
				if (taxi.schedule(request))
				{
					unservicedRequests.remove(request);
					revenue += request.spCost;
					break;
				}
	}

	public void schedule2() throws PickUpTimingConstraintViolation
	{

		// Method 2
		int j = 0;
		int increment = taxis.size();
		for (Taxi taxi : taxis)
		{
			int t = j;
			for (; j < requests.size(); j += increment)
			{
				Request request = requests.get(j);
				if (taxi.schedule(request))
				{
					revenue += request.spCost;
					unservicedRequests.remove(request);
				}
			}
			j = ++t;
		}
	}

	public void schedule3()
	{
		for (Request request : requests)
		{
			LinkedList<NearestTaxi> nearestTaxis = new LinkedList<NearestTaxi>();
			for (Taxi taxi : taxis)
				nearestTaxis.add(new NearestTaxi(request.pickUp.et,
						request.pickUp, taxi));
			Collections.sort(nearestTaxis);

			for (NearestTaxi taxi : nearestTaxis)
				if (taxi.taxi.schedule(request))
				{
					unservicedRequests.remove(request);
					revenue += request.spCost;
					break;
				}
		}
	}

	// Assumes requests are ordered by their pickup time intervals
	public void schedule4()
	{
		//Collections.reverse(requests);
		for (Request request : requests)
			System.out.println(request.id + " "
					+ (request.pickUp.lt - request.pickUp.et));
		// Method 1
		for (Request request : requests)
			for (Taxi taxi : taxis)
				if (taxi.schedule(request))
				{
					unservicedRequests.remove(request);
					revenue += request.spCost;
					break;
				}
	}

	public void display() throws PickUpTimingConstraintViolation
	{
		int taxiNum = 0;
		int distance = 0;
		int idleTime = 0;
		System.out.println("Taxi Schedules");
		for (Taxi t : taxis)
		{
			if (t.schedule.route.isEmpty())
				continue;

			System.out.print(++taxiNum + " : " + "(" + t.startPoint.location
					+ ")");
			int nPassengers = 0;
			Stop p = t.startPoint;
			for (Stop s : t.schedule.route)
			{
				if (nPassengers > t.schedule.capacity)
				{
					System.err.println();
					System.err
							.println("Schedule of this taxi exceeds capacity after accomadation of last request : "
									+ s.requestId);
					System.exit(1);
				}

				System.out.print(" --(" + t.schedule.shortestTime(p, s));
				distance += p.distTo(s);

				if (s.at - p.at > t.schedule.shortestTime(p, s))
				{
					idleTime += s.at - p.at - t.schedule.shortestTime(p, s);
					System.out.print(" + "
							+ (s.at - p.at - t.schedule.shortestTime(p, s))
							+ "(IDLE) min)--> ");
				}
				else
				{
					System.out.print(" min)--> ");
				}

				if (s.at < s.et || s.at > s.lt)
				{
					System.err.println("Timing constraint of request "
							+ s.requestId + ": " + s.et + "---" + s.lt
							+ " not met. Actual time assigned : " + s.at);
					throw new PickUpTimingConstraintViolation(s);
				}
				if (s.type() == StopType.PICKUP)
				{
					System.out.print("+");
					nPassengers++;
				}
				else
				{
					System.out.print("-");
					nPassengers--;
				}
				System.out.print(s.requestId + "(" + s.location + " : " + "["
						+ s.et + "-" + s.at + "-" + s.lt + "])");

				p = s;
				if (t.schedule.route.getLast().equals(s))
				{
					idleTime += dayEndTime - t.schedule.route.getLast().at;
					System.out.print(" ---IDLE for "
							+ (dayEndTime - t.schedule.route.getLast().at)
							+ " min--- ");
				}

			}
			System.out.println();
		}
		System.out.println(requests.size() - unservicedRequests.size()
				+ " requests out of " + requests.size() + " serviced.");
		System.out.println(taxiNum + " taxis used");
		System.out.println("Total distance travelled = " + distance + " kms");
		System.out.println("Total revenue = " + revenue);
		System.out.println("Idle time = " + idleTime + " min");
		int sum = 0;
		for (Request r : requests)
			sum += r.spCost;
		System.out.println("Maximum revenue possible = " + sum);

		int extraDistance = 0;
		for (Taxi t : taxis)
		{
			LinkedList<Stop> route = t.schedule.route;
			for (int i = 0; i < route.size(); i++)
			{
				Stop pickUp = route.get(i);
				if (pickUp.type() == StopType.PICKUP)
				{
					int actualRideDistance = 0;

					for (int k = i; k < route.size(); k++)
					{
						Stop s = route.get(k);
						Stop n = route.get(k + 1);
						actualRideDistance += s.distTo(n);
						if (n.requestId == pickUp.requestId
								&& n.type() == StopType.DROP)
						{
							extraDistance += actualRideDistance
									- pickUp.distTo(n);
							break;
						}
					}
				}
			}
		}
		System.out.println("Total extra distance travelled = " + extraDistance);
	}

	public static void main(String[] args) throws IOException,
			PickUpTimingConstraintViolation
	{
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		StringTokenizer st = new StringTokenizer(br.readLine());

		int nLocations = Integer.parseInt(st.nextToken());
		int nVehicles = Integer.parseInt(st.nextToken());
		int vehicleCapacity = Integer.parseInt(st.nextToken());
		int nRequests = Integer.parseInt(st.nextToken());

		// create map
		EdgeWeightedDigraph cityMap = new EdgeWeightedDigraph(nLocations + 1);
		for (int v = 1; v <= nLocations; v++)
		{
			st = new StringTokenizer(br.readLine());
			for (int w = 1; st.hasMoreElements(); w++)
			{
				int distance = Integer.parseInt(st.nextToken());
				if (distance != -1)
					cityMap.addEdge(new DirectedEdge(v, w, distance));
			}
		}
		// System.out.println("City Map");
		// System.out.println(cityMap);

		// create taxis
		// System.out.println("\nInitial taxi location");
		st = new StringTokenizer(br.readLine());
		ArrayList<Taxi> taxis = new ArrayList<Taxi>();
		for (int i = 0; i < nVehicles; i++)
		{
			int location = Integer.parseInt(st.nextToken());
			Taxi t = new Taxi(location, vehicleCapacity, cityMap);
			taxis.add(t);
			// System.out.println(t);
		}

		// Requests
		// System.out.println("\nRequests list");
		ArrayList<Request> requests = new ArrayList<>();
		for (int i = 0; i < nRequests; i++)
		{
			st = new StringTokenizer(br.readLine());
			int srcLocation = Integer.parseInt(st.nextToken());
			int destLocation = Integer.parseInt(st.nextToken());
			int et = Integer.parseInt(st.nextToken());
			int lt = Integer.parseInt(st.nextToken());

			Stop src = new Stop(srcLocation, et, lt, i, StopType.PICKUP,
					cityMap);
			// System.out.print(src.location + " : ");
			// System.out.println(src.sp);
			// System.out.println(src.lt - src.at);
			DijkstraSP sp = new DijkstraSP(cityMap, src.location);
			int destLt = Math.min(et + (int) (sp.distTo(destLocation) * deviationFactor * timePerKm), dayEndTime);
			Stop dest = new Stop(
					destLocation,
					et + (int)(sp.distTo(destLocation) * timePerKm),
					destLt,
					i, StopType.DROP, cityMap);
			// System.out.print(dest.location + " : ");
			// System.out.println(dest.sp);
			Request r = new Request(src, dest);
			requests.add(r);

			// System.out.println(r);
		}
		// System.out.println();

		final long startTime = System.currentTimeMillis();
		DialARide darp = new DialARide(requests, taxis, cityMap);
		final long endTime = System.currentTimeMillis();
		System.out.println("TIME NEEDED = " + (endTime - startTime)/1000.0 + "secs");
		darp.display();
		br.close();
	}
}

/**
 * How far is 'taxi' from 'src' at 'time'
 * 
 * @author kempa
 * 
 */
class NearestTaxi implements Comparable<NearestTaxi>
{
	Taxi taxi;
	int dist;

	public NearestTaxi(int time, Stop src, Taxi taxi)
	{
		this.taxi = taxi;
		dist = taxi.dist(time, src);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(NearestTaxi o)
	{
		return dist - o.dist;
	}
}