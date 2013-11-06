package org.iiitb.graphs;
/**
 * @author kempa
 * 
 * @param <Key>
 *            is the type of the elements in the PQ. It extends the Comparable
 *            class
 */
@SuppressWarnings("unchecked")
public class MinPQ<Key extends Comparable<Key>>
{

	private Key[] pq; // heap-ordered complete binary tree in pq[1..N] with
	private int N; // pq[0] unused

	/**
	 * Constructs an empty MIn-PQ with the specified intial capacity
	 * 
	 * @param capacity
	 *            Initial capacity of the Max-PQ
	 */
	public MinPQ(int capacity)
	{
		pq = (Key[]) new Comparable[capacity + 1];
		N = 0;
	}

	/**
	 * Constructs a Min-PQ initialised with the specified array
	 * 
	 * @param a
	 *            array whose elements are to be used to initialise Min-PQ
	 */
	public MinPQ(Key a[])
	{
		pq = (Key[]) new Comparable[a.length + 1];
		System.arraycopy(a, 0, pq, 1, a.length);

		for (int i = pq.length / 2; i >= 1; i--)
			sink(i);

		N = pq.length - 1;
	}

	/**
	 * Constructs a Min-PQ initialised with the elements of the iterable object
	 * 
	 * @param a
	 *            Iterable object whose elements are to be used to initialise
	 *            Min-PQ
	 */
	public MinPQ(Iterable<Key> a)
	{
		pq = (Key[]) new Comparable[4];

		int j = 1;
		for (Key k : a)
		{
			if (j == pq.length)
				pq = resize(pq, pq.length * 2);
			pq[j++] = k;
		}
		N = j - 1;

		for (int i = j / 2; i >= 1; i--)
			sink(i);
	}

	private boolean greater(Key i, Key j)
	{
		if (i.compareTo(j) > 0)
			return true;
		return false;
	}

	/**
	 * Tests if Min-PQ is empty
	 * 
	 * @return Test result
	 */
	public boolean isEmpty()
	{
		return N == 0;
	}

	/**
	 * Returns size of Min-PQ
	 * 
	 * @return Size of Min-PQ
	 */
	public int size()
	{
		return N;
	}

	/**
	 * Insert a key into Min-PQ in logarithmic time
	 * 
	 * @param v
	 *            Key to be inserted
	 */
	public void insert(Key v)
	{
		// Insert
		pq[++N] = v;
		swim(N);

		// resizing
		if (N >= pq.length - 1)
			pq = resize(pq, 2 * pq.length);
	}

	/**
	 * Resizes the specified array with the specified size
	 * 
	 * @param a
	 *            Array to be resized
	 * @param n
	 *            New size
	 * @return Resized array
	 */
	Key[] resize(Key[] a, int n)
	{
		Key[] b = (Key[]) new Comparable[n];
		System.arraycopy(a, 0, b, 0, (a.length < b.length) ? a.length
				: b.length);
		return b;
	}

	/**
	 * pq[i] swims to its rightful place.
	 * 
	 * @param i
	 *            Index of element which has to swim
	 * @return Index of key after the swim
	 */
	private int swim(int i)
	{
		Key t = pq[i]; // Save key for later restoring
		while (i > 1 && greater(pq[i / 2], pq[i]))
		{
			// exch(pq, i / 2, i);
			pq[i] = pq[i / 2]; // half exchanges
			i = i / 2;
		}
		pq[i] = t; // restore key
		return i;
	}

	/**
	 * Delete the min key in logarithmic time
	 * 
	 * @return The min key which was deleted
	 */
	public Key delMin()
	{
		if (N == 0)
			throw new RuntimeException("Priority Queue Underflow");

		Key min = pq[1];

		pq[1] = pq[N]; // half exchange
		pq[N--] = null;
		sink(1);

		// resize
		if (N == pq.length / 4)
			pq = resize(pq, pq.length / 2);

		return min;
	}

	/**
	 * Returns the min key in constant time
	 * 
	 * @return The max key
	 */
	public Key max()
	{
		if (N == 0)
			throw new RuntimeException("Priority Queue Underflow");
		return pq[1];

	}

	/**
	 * pq[i] sinks to its rightful place
	 * 
	 * @param i
	 *            Index of the element which should sink
	 * @return New Index of the element which has sunk
	 */
	private int sink(int i)
	{
		Key t = pq[i]; // Save key for later restoring
		while (2 * i + 1 <= N) // while pq[i] has both the children
		{
			int j = 2 * i + 1; // rightmost child

			// 1st comparision: Making j point to the least of the two
			// children as pq[i] should sink to j
			if (greater(pq[j], pq[j - 1]))
				j--;

			// 2nd comparsion. Stop sinking if pq[i] is less than its
			// least child
			if (greater(pq[j], t))
				break;

			//exch(pq, i, j);
			pq[i] = pq[j]; // half exchanges
			i = j; // Prepare 'i' for the next iteration
		}

		// if pq[i] had only one child, the comparision for sink hasn't been
		// done yet
		if (2 * i == N)
			if (greater(pq[i], pq[N]))
			{
				pq[i] = pq[N];
				i = N;
			}

		pq[i] = t; // restore
		return i;
	}
}