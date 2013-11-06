package org.iiitb.graphs;

import java.util.LinkedList;

// Finds the shortest paths from all vertices of G to s
// The union of all shortest paths forms the SPT, a spanning tree
// The SPT contains all vertices of G and is hence represented in two vertex indexed data structures, distFromSource[] and edgeFromSPT[]
// That a vertex is a part of the SPT is indicated by its distance to the source not being infinity

public class DijkstraSP implements ShortestPaths
{
	// vertex indexed data structures
	private double distFromSource[]; // shortest distance of a vertex to the
										// source
	private DirectedEdge edgeFromSPT[]; // Edge connecting the vertex to the SPT
	private IndexMinPQ<Double> pq; // Shortest distances of vertices adjacent to
									// the still-constructing SPT


	public DijkstraSP(EdgeWeightedDigraph G, int s)
	{
		distFromSource = new double[G.V()];
		for (int v = 0; v < G.V(); v++)
			distFromSource[v] = Double.POSITIVE_INFINITY;

		edgeFromSPT = new DirectedEdge[G.V()];
		pq = new IndexMinPQ<Double>(G.V());

		// SPT construction starts at s
		// That being the case, the only candidate for inclusion into SPT is
		// this start vertex itself. So insert it into the PQ
		distFromSource[s] = 0.0;
		pq.insert(s, distFromSource[0]);

		// The construction of the SPT is complete only when all vertices of G
		// are included in the SPT, i.e., the PQ is empty
		while (!pq.isEmpty())
		{
			// the vertex with the smallest distance from the source should
			// become a part of the SPT. Get that vertex.
			int v = pq.delMin();

			// Now that v is a part of the SPT,
			// 1. The smallest distance of adjacent vertices of v to the source
			// may reduce. Check and update
			// 2. Some vertices from among those adjacent to v may become
			// adjacent to the SPT. Need to insert those into the PQ
			update(G, v);
		}

	}

	// Update the shortest-distances-to-source of vertices adjacent to v
	// Insert newly adjacent-to-MST vertices into the PQ
	private void update(EdgeWeightedDigraph G, int v)
	{
		// for all adjacent vertices of v
		for (DirectedEdge e : G.adj(v))
		{
			int w = e.to(); // adjacent vertex of v
			if (distFromSource[w] > distFromSource[v] + e.weight())
			{
				// Note: distFromSource[w] will not be greater than
				// (distFromSource[v] + e.weight()) if w is already a part of
				// the
				// spanning tree. Therefore there is no danger of w being
				// inserting into the pq once again and thus saying that it is
				// not a prt of the SPT

				// Shortest path to w is through v
				distFromSource[w] = distFromSource[v] + e.weight();
				edgeFromSPT[w] = e;

				// w adjacent to the SPT vertex v is also adjacent to the SPT
				// Therefore insert it into the PQ.
				// If it is already there as a result of being an adjacent
				// vertex to another SPT-vertex, update its distance to the
				// source
				if (!pq.contains(w))
					pq.insert(w, distFromSource[w]);
				else
					pq.change(w, distFromSource[w]);
			}
		}
	}

	public double distTo(int v)
	{
		return distFromSource[v];
	}

	public boolean hasPathTo(int v)
	{
		return distFromSource[v] != Double.POSITIVE_INFINITY;
	}

	public Iterable<DirectedEdge> pathTo(int v)
	{
		if (!hasPathTo(v))
			return null;
		LinkedList<DirectedEdge> stack = new LinkedList<DirectedEdge>();
		for (DirectedEdge e = edgeFromSPT[v]; e != null; e = edgeFromSPT[e.from()])
			stack.addFirst(e);
		return stack;
	}
	
	public String toString()
	{
		String s = "";
		for (int i = 0; i < distFromSource.length; i++)
		{
			s += i + " = " + distTo(i);
			if (i < distFromSource.length - 1)
				s+= ", ";
		}
		return s;
	}
}