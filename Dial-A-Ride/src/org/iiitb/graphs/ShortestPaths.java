package org.iiitb.graphs;

public interface ShortestPaths
{
	double distTo(int v);
	boolean hasPathTo(int v);
	Iterable<DirectedEdge> pathTo(int v);
}
