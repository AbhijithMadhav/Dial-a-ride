import java.util.Collections;
import java.util.LinkedList;

/**
 * 
 */

/**
 * @author kempa
 *
 */
public class A
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		LinkedList<B> lst = new LinkedList<>();
		lst.add(new B(1));
		for (B b : lst)
			System.out.print(b + " ");
		System.out.println();
		LinkedList<B> tmp = new LinkedList<>();
		Collections.copy(tmp, lst);
		for (B b : lst)
			tmp.add(new B(100));
		tmp.get(0).i = 2;
		for (B b : lst)
			System.out.print(b + " ");
		System.out.println();
		//for (B b : tmp)
			//System.out.print(b + " ");

	}

}

class B
{
	int i;
	
	public B(int i)
	{
		this.i = i;
	}
	
	public String toString()
	{
		String s = "" + i;
		return s;
	}
	
	protected Object clone()
	{
		return new B(i);
	}
}