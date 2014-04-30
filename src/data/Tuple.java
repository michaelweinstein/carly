package data;

/**
 * Solid tuple class
 * 
 * @author dgattey
 * @param <A> some type
 * @param <B> some type
 */
public class Tuple<A, B> {
	
	public final A	first;
	public final B	second;
	
	/**
	 * Just sets a and b
	 * 
	 * @param a the a item
	 * @param b the b item
	 */
	public Tuple(final A a, final B b) {
		this.first = a;
		this.second = b;
	}
}