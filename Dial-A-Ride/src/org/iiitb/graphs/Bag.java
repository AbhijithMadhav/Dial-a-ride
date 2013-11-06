package org.iiitb.graphs;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A bag implementation. A bag is like a list except that an order is not
 * implied
 */
public class Bag<Item> implements Iterable<Item>
{
	private Node first;
	private int N;

	private class Node
	{
		private Item item;
		private Node next;

		private Node(Item item, Node next)
		{
			this.item = item;
			this.next = next;
		}
	}

	public Bag()
	{
		first = null;
	}

	// add item at front
	public void add(Item item)
	{
		Node t = first;
		first = new Node(item, t);
		N++;
	}

	public boolean isEmpty()
	{
		return N == 0;
	}

	public int size()
	{
		return N;
	}

	/**
	 * Determines if the bag contains the specified element
	 * @param item The specified element to find
	 * @return Does the bag contain the specified element?
	 */
	public boolean contains(Item item)
	{
		for (Node t = first; t != null; t = t.next)
			if (item.equals(t.item))
				return true;
		return false;
	}

	public Iterator<Item> iterator()
	{
		return new BagIterator();

	}

	private class BagIterator implements Iterator<Item>
	{
		private Node current = first;

		@Override
		public boolean hasNext()
		{
			return current != null;
		}

		@Override
		public Item next()
		{
			if (!hasNext())
				throw new NoSuchElementException();

			Item item = current.item;
			current = current.next;
			return item;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	public static void main(String s[])
	{
		Bag<Integer> bag = new Bag<Integer>();
		for (int i = 0; i < 10; i++)
			bag.add(i);
		if (!bag.isEmpty())
		{
			System.out.println("Size : " + bag.size());
			for (Integer i : bag)
				System.out.println(i);
			bag.iterator().remove();
		}
	}
}
