package org.iiitb.graphs;


/**
 * An Adjacency-list representation of a graph
 * 
 */
public class Graph
{
	/**
	 * Number of vertices
	 */
	protected int V;
	/**
	 * Number of edges
	 */
	protected int E;
	/**
	 * Adjacency lists
	 */
	protected Bag<Integer>[] adj;

	/**
	 * Create a {@code V} vertex graph with no edges
	 * 
	 * @param V Number of edges
	 */
	@SuppressWarnings("unchecked")
	public Graph(int V)
	{
		this.V = V;
		E = 0;

		// create an array of bags(references to bags)
		adj = (Bag<Integer>[]) new Bag[V];

		// Initialise the bags(actually create the bags)
		for (int v = 0; v < V; v++)
			adj[v] = new Bag<Integer>();
	}


	/**
	 * Construct a graph from a {@code Graph} object. Note that the order of the
	 * edges in the adjacency list reverses
	 * 
	 * @param G The {@code Graph} object using which this object is constructed
	 */
	public Graph(Graph G)
	{
		this(G.V());
		for (int i = 0; i < G.V(); i++)
			for (int v : G.adj(i))
				try
				{
					addEdge(i, v);
				}
				catch (Exception e)
				{
				}
	}

	/**
	 * Determine number of vertices
	 * 
	 * @return Number of vertices
	 */
	public int V()
	{
		return V;
	}

	/**
	 * Determine number of edges
	 * 
	 * @return Number of edges
	 */
	public int E()
	{
		return E;
	}

	/**
	 * Add edge v-w to this graph
	 * 
	 * @param v One vertex of the edge
	 * @param w Other vertex of the edge
	 * @throws InvalidEdgeException
	 * @throws SelfLoopExistsException
	 * @throws ParallelEdgeExistsException
	 */
	public void addEdge(int v, int w) throws InvalidEdgeException,
			SelfLoopExistsException, ParallelEdgeExistsException
	{
		// check for invalid edge
		if (v < 0 || v >= V || w < 0 || w >= V)
			throw new InvalidEdgeException("Invalid edge : " + v + "-" + w);

		if (v == w)
			throw new SelfLoopExistsException("Self loops not allowed : " + v
					+ "-" + w);

		for (int a : adj[v])
			if (a == w)
				throw new ParallelEdgeExistsException(
						"Parallel edges not allowed : " + v + "-" + w);

		if (!adj[v].contains(w)) // this condition is needed only if adjacency
									// lists are presented in the input stream.
									// Since edges v-w and w-v are explicitly
									// specified in the lists care should be
									// taken to not to insert edges twice
		{
			adj[v].add(w);
			adj[w].add(v);
			E++;
		}

	}

	/**
	 * Determines if the edge v-w exists in the graph
	 * 
	 * @param v One vertex of the edge
	 * @param w Other vertex of the edge
	 * @return Does the edge v-w exist?
	 */
	public boolean hasEdge(int v, int w)
	{
		for (int a : adj(v))
			if (a == w)
				return true;
		return false;
	}

	/**
	 * Determine vertices adjacent to v
	 * 
	 * @param v The vertex relative to which adjacent vertices must be
	 *            determined
	 * @return Adjacent vertices
	 */
	Iterable<Integer> adj(int v)
	{
		return adj[v];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String s = V + " vertices " + E + " edges\n";
		for (int v = 0; v < V; v++)
		{
			s += v + ": ";
			for (Integer w : adj(v))
				s += w + " ";
			s += "\n";
		}
		return s;
	}

	/**
	 * Determines if {@code v} is in the graph
	 * 
	 * @param v The vertex
	 * @return if {@code v} in the graph?
	 */
	public boolean contains(int v)
	{
		return v >= 0 && v < V();
	}

	/*
	 * 4.1.7 Develop a test client for Graph that reads a graph from the input
	 * stream named as command-line argument and then prints it, relying on
	 * toString().
	 */


}

/**
 * Exception indicating an invalid edge
 */
@SuppressWarnings("serial")
class InvalidEdgeException extends Exception
{
	InvalidEdgeException(String s)
	{
		super(s);
	}
}

/**
 * Exception indicating a self loop
 */
@SuppressWarnings("serial")
class SelfLoopExistsException extends Exception
{
	SelfLoopExistsException(String s)
	{
		super(s);
	}
}

/**
 * Exception indicating a parallel edge
 */
@SuppressWarnings("serial")
class ParallelEdgeExistsException extends Exception
{
	ParallelEdgeExistsException(String s)
	{
		super(s);
	}
}

/**
 * Exception indicating a non-existent vertex
 */
@SuppressWarnings("serial")
class NonExistentVertexException extends Exception
{
	NonExistentVertexException(String s)
	{
		super(s);
	}
}
