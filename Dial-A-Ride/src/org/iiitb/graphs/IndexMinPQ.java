package org.iiitb.graphs;

public class IndexMinPQ<Item extends Comparable<Item>>
{
	private int N; // No. of data items
	private Item items[]; // array containing actual data items
	private int pq[]; // Priority Q containing indexes of the above items[]
	private int qp[]; // Reverse index which determines where in the priority Q
						// the i'th data item is

	@SuppressWarnings("unchecked")
	public IndexMinPQ(int maxN)
	{
		items = (Item[]) new Comparable[maxN + 1];
		pq = new int[maxN + 1];
		qp = new int[maxN + 1];
		for (int i = 0; i < maxN; i++)
			qp[i] = -1;
		N = 0;
	}

	/**
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private boolean greater(Item i, Item j)
	{
		if (i.compareTo(j) > 0)
			return true;
		return false;
	}

	/**
	 * Exchange pq[i] and pq[j]
	 * 
	 * @param i
	 * @param j
	 */
	private void exch(int i, int j)
	{
		int t = pq[i];
		pq[i] = pq[j];
		pq[j] = t;
	}

	/**
	 * Make the i'th element in the priority queue swim up. Adjust the reverse
	 * index accordingly
	 * 
	 * @param i
	 */
	private void swim(int i)
	{
		int t = pq[i]; // Save for later restoring

		while (i > 1 && greater(items[pq[i / 2]], items[t]))
		{
			pq[i] = pq[i / 2]; // half exchanges
			qp[pq[i / 2]] = i; // reverse index
			i /= 2;
		}

		pq[i] = t; // restore in the correct position
		qp[t] = i; // reverse index
	}

	/**
	 * Make the i'th in the priority queue swim up. Adjust the reverse index
	 * accordingly
	 * 
	 * @param i
	 */
	private void sink(int i)
	{
		int t = pq[i]; // save for later restoring at correct position

		while (2 * i + 1 <= N) // The node has both the children
		{
			int j = 2 * i + 1; // right child

			if (greater(items[pq[j]], items[pq[j - 1]])) // determine the
															// lesser child
				j--;

			if (greater(items[pq[j]], items[t])) // Final position for
														// items[pq[i]] reached
				break;

			pq[i] = pq[j]; // half exchanges
			qp[pq[j]] = i; // adjust the reverse index
			i = j;
		}

		// if pq[i] had only one child, the comparision for sink hasn't been
		// done yet
		if (2 * i == N)
			if (greater(items[pq[i]], items[pq[N]]))
			{
				pq[i] = pq[N];
				qp[pq[N]] = i;
				i = N;
			}

		pq[i] = t; // restore in the correct position
		qp[t] = i; // adjust the reverse index accordingly
	}

	/**
	 * Insert {@code item}; associate it with k
	 * 
	 * @param k
	 * @param item
	 */
	public void insert(int k, Item item)
	{
		// associate element with an index k by inserting it into items[k]
		items[k] = item;

		// insert item index, k, into the priority queue
		N++;
		pq[N] = k; // insert into last position
		qp[k] = N; // reverse index
		swim(N); // make it swim up to its rightful place. The reverse index is
					// also adjusted
	}

	/**
	 * Change the element associated with k to 'item'
	 * 
	 * @param k
	 * @param item
	 */
	public void change(int k, Item item)
	{
		items[k] = item;
		swim(qp[k]);
		sink(qp[k]);
	}

	/**
	 * Is k associated with some item?
	 */

	public boolean contains(int k)
	{
		return qp[k] != -1;
	}

	/**
	 * remove k and its associated item
	 */
	public void delete(int k)
	{
		int i = qp[k];

		qp[k] = -1;
		exch(i, N--);
		sink(i);
	}

	/**
	 * return the minimal item
	 */
	public Item min()
	{
		return items[pq[1]];
	}

	/**
	 * return the minimal items index
	 */
	public int minIndex()
	{
		return pq[1];
	}

	/**
	 * remove a minimal item and return its index
	 */
	public int delMin()
	{
		int t = pq[1];
		delete(pq[1]);
		return t;
	}

	/**
	 * is the priority queue empty?
	 */
	public boolean isEmpty()
	{
		return N == 0;
	}

	/**
	 * number of items in the priority queue
	 */
	public int size()
	{
		return N;
	}

}
