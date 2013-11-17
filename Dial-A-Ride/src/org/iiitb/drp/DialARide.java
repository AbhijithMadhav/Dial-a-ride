package org.iiitb.drp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

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
	private Logger logger;
	static final int dayStartTime = 0;
	static final int dayEndTime = 24 * 60;
	final static int timePerKm = 2; // minutes
	final static int ratePerKm = 1; // Rupees

	final static double deviationFactor = 2;

	public DialARide(ArrayList<Request> requests, ArrayList<Taxi> taxis,
			EdgeWeightedDigraph cityMap) throws SecurityException, IOException
	{
		// sort requests by their earliest time
		this.requests = requests;
		Collections.sort(this.requests);
		unservicedRequests = new LinkedList<Request>(requests);
		revenue = 0;
		this.taxis = taxis;
		this.cityMap = cityMap;
		logger = MyLogger.getInstance();

		schedule1();
		for (Taxi t : taxis)
			t.check();

		logger.info("\nUnserviced Requests");
		for (Request r : unservicedRequests)
			logger.info(r.toString());
		logger.info("");
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
					revenue += request.getCost();
					break;
				}
	}

	public void schedule2()
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
					revenue += request.getCost();
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
					revenue += request.getCost();
					break;
				}
		}
	}

	public void logReport()
	{
		int nTaxi = 0;
		int distance = 0;
		int idleTime = 0;
		logger.info("Taxi Schedules");
		for (Taxi t : taxis)
		{
			int dist = t.distanceTravelled();
			if (dist != 0)
				nTaxi++;
			distance += dist;
			idleTime += t.idleTime();
			logger.info(t.toString());
		}
		logger.info("");

		logger.info(requests.size() - unservicedRequests.size()
				+ " requests out of " + requests.size() + " serviced.");
		logger.info(nTaxi + " taxis out of " + taxis.size() + " used");
		logger.info("");

		logger.info("Total distance travelled = " + distance + " kms");
		logger.info("");

		logger.info("Idle time = " + idleTime + " min");
		logger.info("");

		int sum = 0;
		for (Request r : requests)
			sum += r.getCost();
		logger.info("Maximum revenue possible = " + sum);
		logger.info("Total revenue = " + revenue);
	}

	public String toString()
	{
		String str = "";
		int revenue = 0;
		for (Taxi t : taxis)
		{
			for (Stop s : t.route)
				str += s.location + " " + s.at + " ";
			str += t.revenue + "\n";
			revenue += t.revenue;
		}
		str += revenue;
		return str;
	}

	public static void main(String[] args) throws IOException
	{
		if (args.length < 1)
			System.exit(1);
		
		Logger logger = MyLogger.getInstance();
		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		StringTokenizer st = new StringTokenizer(br.readLine());

		int nLocations = Integer.parseInt(st.nextToken());
		int nVehicles = Integer.parseInt(st.nextToken());
		int vehicleCapacity = Integer.parseInt(st.nextToken());
		int nRequests = Integer.parseInt(st.nextToken());

		// create city map
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
		logger.info("City Map");
		logger.info(cityMap.toString());

		// create taxis
		logger.info("Taxi's");
		st = new StringTokenizer(br.readLine());
		ArrayList<Taxi> taxis = new ArrayList<Taxi>();
		for (int i = 0; i < nVehicles; i++)
		{
			int location = Integer.parseInt(st.nextToken());
			Taxi t = new Taxi(location, vehicleCapacity, cityMap);
			taxis.add(t);
			logger.info(t.toString());
		}
		logger.info("");

		// Requests
		logger.info("Requests list");
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
			DijkstraSP sp = new DijkstraSP(cityMap, src.location);
			int destLt = Math
					.min(et
							+ (int) (sp.distTo(destLocation) * deviationFactor * timePerKm),
							dayEndTime);
			Stop dest = new Stop(destLocation, et
					+ (int) (sp.distTo(destLocation) * timePerKm), destLt, i,
					StopType.DROP, cityMap);
			Request r = new Request(src, dest);
			requests.add(r);
			logger.info(r.toString());
		}
		logger.info("");

		final long startTime = System.currentTimeMillis();
		DialARide darp = new DialARide(requests, taxis, cityMap);
		final long endTime = System.currentTimeMillis();
		//System.out.println("Program run time = " + (endTime - startTime)
			//	/ 1000.0 + "secs\n");
		//darp.logReport();
		System.out.println(darp);
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