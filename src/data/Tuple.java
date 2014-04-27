package data;

/**
 * Solid tuple class
 * 
 * @author dgattey
 * @param <A> some type
 * @param <B> some type
 */
public class Tuple<A, B> {
	
	public final A	a;
	public final B	b;
	
	/**
	 * Just sets a and b
	 * 
	 * @param a the a item
	 * @param b the b item
	 */
	public Tuple(final A a, final B b) {
		this.a = a;
		this.b = b;
	}
}