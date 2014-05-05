package data;

import java.util.Objects;

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
	
	@Override
	public String toString() {
		return "Tuple<" + first + ", " + second + ">";
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Tuple<?, ?>) {
			final Tuple<?, ?> o = (Tuple<?, ?>) obj;
			return first.equals(o.first) && second.equals(o.second);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}
}