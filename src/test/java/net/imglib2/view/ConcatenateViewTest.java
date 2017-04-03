/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2016 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
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
 * #L%
 */

package net.imglib2.view;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;

/**
 *
 * @author Philipp Hanslovsky
 *
 */
public class ConcatenateViewTest
{

	private final long[] dim = new long[] { 3, 4, 5, 6 };

	private final long divider = 3;

	private final int axis = 3;

	@Test
	public void testConcatenate()
	{
		final long numElements = Intervals.numElements( dim );
		final Random rng = new Random();
		final byte[] data = new byte[ ( int ) numElements ];
		rng.nextBytes( data );
		final ArrayImg< ByteType, ByteArray > img = ArrayImgs.bytes( data, dim );

		final long[] min = Intervals.minAsLongArray( img );
		final long[] max = Intervals.maxAsLongArray( img );
		final long[] min1 = min.clone();
		final long[] min2 = min.clone();
		final long[] max1 = max.clone();
		final long[] max2 = max.clone();

		max1[ axis ] = divider;
		min2[ axis ] = divider + 1;

		final IntervalView< ByteType > interval1 = Views.interval( img, min1, max1 );
		final IntervalView< ByteType > interval2 = Views.interval( img, min2, max2 );

		final RandomAccessibleInterval< ByteType > concatenated = Views.concatenate( axis, interval1, interval2 );

		for ( final Pair< ByteType, ByteType > p : Views.flatIterable( Views.interval( Views.pair( img, concatenated ), img ) ) )
			Assert.assertEquals( p.getA().getInteger(), p.getB().getInteger() );
	}

}
