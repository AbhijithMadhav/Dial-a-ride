package org.iiitb.graphs;

public class EdgeWeightedDigraph
{
	private int V;
	private int E;
	private Bag<DirectedEdge>[] adj;

	@SuppressWarnings("unchecked")
	public EdgeWeightedDigraph(int V)
	{
		this.V = V;
		adj = (Bag<DirectedEdge>[]) new Bag[V];
		for (int v = 0; v < V; v++)
			adj[v] = new Bag<DirectedEdge>();
	}

	public int V()
	{
		return V;
	}

	public int E()
	{
		return E;
	}

	public void addEdge(DirectedEdge e)
	{
		adj[e.from()].add(e);
		E++;
	}

	public Iterable<DirectedEdge> adj(int v)
	{
		return adj[v];
	}

	public Iterable<DirectedEdge> edges()
	{
		Bag<DirectedEdge> b = new Bag<DirectedEdge>();
		for (int v = 0; v < V; v++)
			for (DirectedEdge e : adj[v])
				b.add(e);
		return b;
	}

	public Digraph digraph(EdgeWeightedDigraph G)
	{
		Digraph g = new Digraph(G.V());

		try
		{
			for (DirectedEdge e : G.edges())
				g.addEdge(e.from(), e.to());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		return g;
	}

	public String toString()
	{
		String s = new String();
		s += V() + " vertices " + E() + " edges\n";
		for (int v = 0; v < V(); v++)
		{
			s += v + ": ";
			for (DirectedEdge e : adj[v])
				s += e + ", ";
			s += "\n";
		}
		return s;
	}
}