package org.iiitb.graphs;


public class DirectedEdge implements Comparable<DirectedEdge>
{
	private int from;
	private int to;
	private double weight;
	
	public DirectedEdge(int v, int w, double weight)
	{
		from = v;
		to = w;
		this.weight = weight;
	}
	
	public int from()
	{
		return from;
	}
	
	public int to()
	{
		return to;
	}
	
	public double weight()
	{
		return weight;
	}
	public String toString()
	{
		return String.format("%d->%d %.2f", from, to, weight);
	}
	
	public int compareTo(DirectedEdge e)
	{
		if (weight < e.weight)
			return -1;
		else if (weight > e.weight)
			return +1;
		else
			return 0;
	}
}
