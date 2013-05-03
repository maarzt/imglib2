/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2013 Stephan Preibisch, Tobias Pietzsch, Barry DeZonia,
 * Stephan Saalfeld, Albert Cardona, Curtis Rueden, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Lee Kamentsky, Larry Lindsey, Grant Harris,
 * Mark Hiner, Aivar Grislis, Martin Horn, Nick Perry, Michael Zinsmaier,
 * Steffen Jaensch, Jan Funke, Mark Longair, and Dimiter Prodanov.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package net.imglib2.ops.sandbox;

import java.util.Iterator;

import net.imglib2.AbstractInterval;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableRealInterval;
import net.imglib2.Localizable;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RealPositionable;
import net.imglib2.Sampler;
import net.imglib2.type.logic.BitType;

// Note - the bind()'ing approach is slow for this class

/**
 * AndPointSet is a set of points composed of points that live in the
 * intersection of two other point sets.
 * 
 * @author Barry DeZonia
 */
public class BoundAndPointSet implements NewPointSet
{
	// -- instance fields --
	
	private NewPointSet p1;
	private NewPointSet p2;
	private boolean needsCalc;
	private long[] min;
	private long[] max;
	private int n;
	private long size;
	
	// -- constructors --
	
	public BoundAndPointSet(NewPointSet p1, NewPointSet p2) {
		if (p1.numDimensions() != p2.numDimensions()) {
			throw new IllegalArgumentException("point sets must have same num dimensions");
		}
		this.p1 = p1;
		this.p2 = p2;
		n = p1.numDimensions();
		min = new long[n];
		max = new long[n];
		size = 0;
		needsCalc = true;
	}

	// -- public methods --
	
	@Override
	public <T> Cursor<T> bind( RandomAccess<T> randomAccess ) {
		return new BoundCursor<T>(this, randomAccess);
	}
	
	@Override
	public boolean contains(long[] point) {
		return p1.contains(point) && p2.contains(point);
	}

	@Override
	public void fwd(int d) {
		p1.fwd(d);
		p2.fwd(d);
		needsCalc = true;
	}

	@Override
	public void bck(int d) {
		p1.fwd(d);
		p2.fwd(d);
		needsCalc = true;
	}

	@Override
	public void move(int distance, int d) {
		move((long) distance, d);
	}

	@Override
	public void move(long distance, int d) {
		p1.move(distance,d);
		p2.move(distance,d);
		needsCalc = true;
	}

	@Override
	public void move(Localizable localizable) {
		for (int i = 0; i < n; i++) {
			move(localizable.getLongPosition(i), i);
		}
	}

	@Override
	public void move(int[] distance) {
		for (int i = 0; i < n; i++) {
			move(distance[i], i);
		}
	}

	@Override
	public void move(long[] distance) {
		for (int i = 0; i < n; i++) {
			move(distance[i], i);
		}
	}

	@Override
	public void setPosition(Localizable localizable) {
		for (int i = 0; i < n; i++) {
			setPosition(localizable.getLongPosition(i), i);
		}
	}

	@Override
	public void setPosition(int[] position) {
		for (int i = 0; i < n; i++) {
			setPosition(position[i], i);
		}
	}

	@Override
	public void setPosition(long[] position) {
		for (int i = 0; i < n; i++) {
			setPosition(position[i], i);
		}
	}

	@Override
	public void setPosition(int position, int d) {
		setPosition((long)position, d);
	}

	@Override
	public void setPosition(long position, int d) {
		long delta = position - getLongPosition(d);
		p1.move(delta, d);
		p2.move(delta, d);
		needsCalc = true;
	}

	@Override
	public int numDimensions() {
		return p1.numDimensions();
	}

	@Override
	public void localize(int[] position) {
		p1.localize(position);
	}

	@Override
	public void localize(long[] position) {
		p1.localize(position);
	}

	@Override
	public void localize(float[] position) {
		p1.localize(position);
	}

	@Override
	public void localize(double[] position) {
		p1.localize(position);
	}

	@Override
	public int getIntPosition(int d) {
		return p1.getIntPosition(d);
	}

	@Override
	public long getLongPosition(int d) {
		return p1.getLongPosition(d);
	}

	@Override
	public float getFloatPosition(int d) {
		return p1.getFloatPosition(d);
	}

	@Override
	public double getDoublePosition(int d) {
		return p1.getDoublePosition(d);
	}

	@Override
	public Cursor<BitType> cursor() {
		return new PositionCursor();
	}

	@Override
	public Cursor<BitType> localizingCursor() {
		return cursor();
	}

	@Override
	public long size() {
		if (needsCalc) calcStuff();
		return size;
	}

	@Override
	public BitType firstElement() {
		return cursor().next();
	}

	@Override
	public Object iterationOrder() {
		return this;
	}

	@Override
	public boolean equalIterationOrder(IterableRealInterval<?> f) {
		return (f == this);
	}

	@Override
	public double realMin(int d) {
		return min(d);
	}

	@Override
	public void realMin(double[] min) {
		for (int i = 0; i < n; i++) {
			min[i] = realMin(i);
		}
	}

	@Override
	public void realMin(RealPositionable min) {
		for (int i = 0; i < n; i++) {
			min.setPosition(realMin(i), i);
		}
	}

	@Override
	public double realMax(int d) {
		return max(d);
	}

	@Override
	public void realMax(double[] max) {
		for (int i = 0; i < n; i++) {
			max[i] = realMax(i);
		}
	}

	@Override
	public void realMax(RealPositionable max) {
		for (int i = 0; i < n; i++) {
			max.setPosition(realMax(i), i);
		}
	}

	@Override
	public Iterator<BitType> iterator() {
		return cursor();
	}

	@Override
	public long min(int d) {
		if (needsCalc) calcStuff();
		return min[d];
	}

	@Override
	public void min(long[] min) {
		for (int i = 0; i < n; i++) {
			min[i] = min(i);
		}
	}

	@Override
	public void min(Positionable min) {
		for (int i = 0; i < n; i++) {
			min.setPosition(min(i), i);
		}
	}

	@Override
	public long max(int d) {
		if (needsCalc) calcStuff();
		return max[d];
	}

	@Override
	public void max(long[] max) {
		for (int i = 0; i < n; i++) {
			max[i] = max(i);
		}
	}

	@Override
	public void max(Positionable max) {
		for (int i = 0; i < n; i++) {
			max.setPosition(max(i), i);
		}
	}

	@Override
	public void dimensions(long[] dimensions) {
		for (int i = 0; i < n; i++) {
			dimensions[i] = dimension(i);
		}
	}

	@Override
	public long dimension(int d) {
		return p1.dimension(d);
	}

	// -- private helpers --
	
	private void calcStuff() {
		size = 0;
		long[] position = new long[n];
		Cursor<BitType> cursor = localizingCursor();
		while (cursor.hasNext()) {
			cursor.next();
			cursor.localize(position);
			for (int i = 0; i < n; i++) {
				if (position[i] < min[i]) min[i] = position[i];
				if (position[i] > max[i]) max[i] = position[i];
			}
			size++;
		}
		needsCalc = false;
	}

	private class PositionCursor extends AbstractInterval implements Cursor<BitType> {

		private Cursor<BitType> cursor;
		private long[] tmpPos;
		
		public PositionCursor() {
			super(BoundAndPointSet.this);
			cursor = p1.cursor();
			tmpPos = new long[n];
		}

		public PositionCursor(PositionCursor other) {
			this();
			long pos = other.cursor.getLongPosition(0);
			cursor.jumpFwd(pos+1);
		}
		
		@Override
		public void localize(float[] position) {
			cursor.localize(tmpPos);
			for (int i = 0; i < n; i++) {
				position[i] = tmpPos[i];
			}
		}

		@Override
		public void localize(double[] position) {
			cursor.localize(tmpPos);
			for (int i = 0; i < n; i++) {
				position[i] = tmpPos[i];
			}
		}

		@Override
		public float getFloatPosition(int d) {
			return cursor.getFloatPosition(d);
		}

		@Override
		public double getDoublePosition(int d) {
			return cursor.getDoublePosition(d);
		}

		@Override
		public BitType get() {
			return cursor.get();
		}

		@Override
		public Sampler<BitType> copy() {
			return cursor();
		}

		@Override
		public void jumpFwd(long steps) {
			for (long l = 0; l < steps; l++)
				cursor.fwd();
		}

		@Override
		public void fwd() {
			cursor.fwd();
			cursor.localize(tmpPos);
		}

		@Override
		public void reset() {
			cursor.reset();
		}

		// TODO - this contract a little broken. Repeated hasNext() calls will
		// move internal cursor.
		
		@Override
		public boolean hasNext() {
			while (cursor.hasNext()) {
				cursor.next();
				cursor.localize(tmpPos);
				if (p2.contains(tmpPos)) return true;
			}
			return false;
		}

		@Override
		public BitType next() {
			return cursor.get();
		}

		@Override
		public void remove() { }

		@Override
		public void localize(int[] position) {
			cursor.localize(tmpPos);
			for (int i = 0; i < n; i++) {
				position[i] = (int) tmpPos[i];
			}
		}

		@Override
		public void localize(long[] position) {
			cursor.localize(tmpPos);
			for (int i = 0; i < n; i++) {
				position[i] = tmpPos[i];
			}
		}

		@Override
		public int getIntPosition(int d) {
			return cursor.getIntPosition(d);
		}

		@Override
		public long getLongPosition(int d) {
			return cursor.getLongPosition(d);
		}

		@Override
		public Cursor<BitType> copyCursor() {
			return new PositionCursor(this);
		}
		
	}
	
	/**
	 * TODO: This was modified from BoundGeneralPointSet. There might be code reuse possible ...
	 */
	private final class BoundCursor<T> extends AbstractInterval implements Cursor< T >
	{
		private final RandomAccess< T > randomAccess;

		private Cursor<BitType> cursor;
		
		private long[] tmpPos;
		
		public BoundCursor( final Interval interval, final RandomAccess< T > randomAccess )
		{
			super( interval );
			this.randomAccess = randomAccess;
			cursor = cursor();
			tmpPos = new long[n];
			rst();
		}

		protected BoundCursor( final BoundCursor<T> cursor )
		{
			super( cursor );
			this.randomAccess = cursor.randomAccess.copyRandomAccess();
			this.cursor = cursor.cursor.copyCursor();
		}

		@Override
		public T get()
		{
			return randomAccess.get();
		}

		@Override
		public void jumpFwd( final long steps )
		{
			cursor.jumpFwd(steps);
			cursor.localize(tmpPos);
			randomAccess.setPosition(tmpPos);
		}

		@Override
		public void fwd()
		{
			cursor.fwd();
			cursor.localize(tmpPos);
			randomAccess.setPosition(tmpPos);
		}

		@Override
		public void reset()
		{
			rst();
		}

		@Override
		public boolean hasNext()
		{
			return cursor.hasNext();
		}

		@Override
		public T next()
		{
			fwd();
			return get();
		}

		@Override
		public void remove() {}

		@Override
		public BoundCursor<T> copy()
		{
			return new BoundCursor<T>( this );
		}

		@Override
		public BoundCursor<T> copyCursor()
		{
			return copy();
		}

		@Override
		public void localize( final float[] position )
		{
			randomAccess.localize( position );
		}

		@Override
		public void localize( final double[] position )
		{
			randomAccess.localize( position );
		}

		@Override
		public float getFloatPosition( final int d )
		{
			return randomAccess.getFloatPosition( d );
		}

		@Override
		public double getDoublePosition( final int d )
		{
			return randomAccess.getDoublePosition( d );
		}

		@Override
		public void localize( final int[] position )
		{
			randomAccess.localize( position );
		}

		@Override
		public void localize( final long[] position )
		{
			randomAccess.localize( position );
		}

		@Override
		public int getIntPosition( final int d )
		{
			return randomAccess.getIntPosition( d );
		}

		@Override
		public long getLongPosition( final int d )
		{
			return randomAccess.getLongPosition( d );
		}
		
		private void rst() {
			cursor.reset();
			cursor.next();
			cursor.localize(tmpPos);
			cursor.reset();
			randomAccess.setPosition(tmpPos);
			randomAccess.bck( 0 );
		}
	}
}