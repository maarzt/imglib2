/**
 * 
 */
package net.imglib2.algorithm.morphology;

import java.util.Vector;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.Shape;
import net.imglib2.img.Img;
import net.imglib2.multithreading.Chunk;
import net.imglib2.multithreading.SimpleMultiThreading;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

/**
 * A set of static utilities performing various morphological operations, such
 * as dilatation, eroding, etc...
 * 
 * @author Jean-Yves Tinevez <jeanyves.tinevez@gmail.com> 2013
 * 
 */
public class MorphologicalOperations
{

	private MorphologicalOperations()
	{}

	/*
	 * DILATION
	 */

	/**
	 * Performs the dilation morphological operation, on a {@link RealType}
	 * {@link Img} using a {@link Shape} as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Dilation_(morphology)">
	 * Dilation_(morphology)</a>.
	 * <p>
	 * This method performs what is called the 'full' dilation. That is: the
	 * result image has its dimension dilated by the structuring element, with
	 * respect to the source image. It is limited to flat structuring elements,
	 * only having <code>on/off</code> pixels, contrary to grayscale structuring
	 * elements. This allows to simply use a {@link Shape} as a type for these
	 * structuring elements.
	 * <p>
	 * <b>Warning:</b> Current implementation does not do <i>stricto sensu</i>
	 * the full dilation. Indeed, if the structuring element has more dimensions
	 * than the source {@link Img}, they are ignored, and the returned
	 * {@link Img} has the same number of dimensions that of the source (but
	 * dilated). This is due to the fact that we use a {@link Shape} for
	 * structuring elements, and that it does not return a number of dimensions.
	 * The neighborhood created have therefore at most as many dimensions as the
	 * source image. The real, full dilation results should have a number of
	 * dimensions equals to the maximum of the number of dimension of both
	 * source and structuring element.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the dilation result. Must be
	 *            a sub-type of <code>T extends {@link RealType}</code>.
	 * @return a new {@link Img}, possibly of larger dimensions than the source.
	 */
	public static < T extends RealType< T > > Img< T > dilateFull( final Img< T > source, final Shape strel, final int numThreads )
	{
		final T minVal = source.firstElement().createVariable();
		minVal.setReal( minVal.getMinValue() );
		return dilate( source, strel, minVal, numThreads );
	}

	/**
	 * Performs the dilation morphological operation, on a {@link RealType}
	 * {@link Img} using a {@link Shape} as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Dilation_(morphology)">
	 * Dilation_(morphology)</a>.
	 * <p>
	 * The result image has the same dimensions that of the source image. It is
	 * limited to flat structuring elements, only having <code>on/off</code>
	 * pixels, contrary to grayscale structuring elements. This allows to simply
	 * use a {@link Shape} as a type for these structuring elements.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the dilation result. Must be
	 *            a sub-type of <code>T extends {@link RealType}</code>.
	 * @return a new {@link Img}, of same dimensions than the source.
	 */
	public static < T extends RealType< T >> Img< T > dilate( final Img< T > source, final Shape strel, final int numThreads )
	{
		final T minVal = source.firstElement().createVariable();
		minVal.setReal( minVal.getMinValue() );
		return dilate( source, strel, minVal, numThreads );
	}

	/**
	 * Performs the dilation morphological operation, on an {@link Img} using a
	 * {@link Shape} as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Dilation_(morphology)">
	 * Dilation_(morphology)</a>.
	 * <p>
	 * This method performs what is called the 'full' dilation. That is: the
	 * result image has its dimension dilated by the structuring element, with
	 * respect to the source image. It is limited to flat structuring elements,
	 * only having <code>on/off</code> pixels, contrary to grayscale structuring
	 * elements. This allows to simply use a {@link Shape} as a type for these
	 * structuring elements.
	 * <p>
	 * This method relies on a specified minimal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * dilation, it is sufficient that the specified min value is smaller
	 * (against {@link Comparable}) than any of the value found in the source
	 * image. This normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * <p>
	 * <b>Warning:</b> Current implementation does not do <i>stricto sensu</i>
	 * the full dilation. Indeed, if the structuring element has more dimensions
	 * than the source {@link Img}, they are ignored, and the returned
	 * {@link Img} has the same number of dimensions that of the source (but
	 * dilated). This is due to the fact that we use a {@link Shape} for
	 * structuring elements, and that it does not return a number of dimensions.
	 * The neighborhood created have therefore at most as many dimensions as the
	 * source image. The real, full dilation results should have a number of
	 * dimensions equals to the maximum of the number of dimension of both
	 * source and structuring element.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source {@link Img} (against {@link Comparable}. This is
	 *            required to perform a proper mathematical dilation. Because we
	 *            operate on a generic {@link Type}, it has to be provided
	 *            manually.
	 * @param <T>
	 *            the type of the source image and the dilation result. Must be
	 *            a sub-type of <code>T extends {@link Comparable} &
	 *            {@link Type}</code>.
	 * @return a new {@link Img}, possibly of larger dimensions than the source.
	 */
	public static < T extends Type< T > & Comparable< T > > Img< T > dilateFull( final Img< T > source, final Shape strel, final T minVal, int numThreads )
	{
		numThreads = Math.max( 1, numThreads );

		/*
		 * Compute target image size
		 */

		final long[] targetDims;

		/*
		 * Get a neighborhood to play with. Note: if we would have a dedicated
		 * interface for structuring elements, that would extend Shape and
		 * Dimensions, we would need to do what we are going to do now. On top
		 * of that, this is the part that causes the full dilation not to be a
		 * real full dilation: if the structuring element has more dimensions
		 * than the source, they are ignored. This is because we use the source
		 * as the Dimension to create the sample neighborhood we play with.
		 */
		final Neighborhood< BitType > sampleNeighborhood = MorphologyUtils.getNeighborhood( strel, source );
		int ndims = sampleNeighborhood.numDimensions();
		ndims = Math.max( ndims, source.numDimensions() );
		targetDims = new long[ ndims ];
		for ( int d = 0; d < ndims; d++ )
		{
			long d1;
			if ( d < source.numDimensions() )
			{
				d1 = source.dimension( d );
			}
			else
			{
				d1 = 1;
			}

			long d2;
			if ( d < sampleNeighborhood.numDimensions() )
			{
				d2 = sampleNeighborhood.dimension( d );
			}
			else
			{
				d2 = 1;
			}

			targetDims[ d ] = d1 + d2 - 1;
		}

		/*
		 * Instantiate target images.
		 */

		final Img< T > dilated = source.factory().create( targetDims, source.firstElement().copy() );
		// Offset coordinates so that they match the source coordinates, which
		// will not be extended.
		final long[] offset = new long[ dilated.numDimensions() ];
		for ( int d = 0; d < offset.length; d++ )
		{
			if ( d < sampleNeighborhood.numDimensions() )
			{
				offset[ d ] = -sampleNeighborhood.min( d );
			}
			else
			{
				offset[ d ] = 0;
			}
		}
		final IntervalView< T > offsetDilated = Views.offset( dilated, offset );

		/*
		 * Prepare iteration.
		 */

		final RandomAccessibleInterval< Neighborhood< T >> accessible;
		if ( numThreads > 1 )
		{
			accessible = strel.neighborhoodsRandomAccessibleSafe( source );
		}
		else
		{
			accessible = strel.neighborhoodsRandomAccessible( source );
		}
		final IterableInterval< T > iterable = Views.iterable( offsetDilated );

		/*
		 * Multithread
		 */

		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( iterable.size(), numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );
		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology dilate thread " + i )
			{
				@Override
				public void run()
				{
					final RandomAccess< Neighborhood< T >> randomAccess = accessible.randomAccess( source );
					final Cursor< T > cursorDilated = iterable.cursor();
					cursorDilated.jumpFwd( chunk.getStartPosition() );

					final T max = source.firstElement().createVariable();
					for ( long steps = 0; steps < chunk.getLoopSize(); steps++ )
					{
						cursorDilated.fwd();
						randomAccess.setPosition( cursorDilated );
						final Neighborhood< T > neighborhood = randomAccess.get();
						final Cursor< T > nc = neighborhood.cursor();

						/*
						 * Look for max in the neighborhood.
						 */

						max.set( minVal );
						while ( nc.hasNext() )
						{
							nc.fwd();
							if ( !Intervals.contains( source, nc ) )
							{
								continue;
							}
							final T val = nc.get();
							// We need only Comparable to do this:
							if ( val.compareTo( max ) > 0 )
							{
								max.set( val );
							}
						}
						cursorDilated.get().set( max );
					}

				}
			};
		}

		/*
		 * Launch calculation
		 */

		SimpleMultiThreading.startAndJoin( threads );

		/*
		 * Return
		 */

		return dilated;
	}

	/**
	 * Performs the dilation morphological operation, on an {@link Img} using a
	 * {@link Shape} as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Dilation_(morphology)">
	 * Dilation_(morphology)</a>.
	 * <p>
	 * The result image has the same dimensions that of the source image. It is
	 * limited to flat structuring elements, only having <code>on/off</code>
	 * pixels, contrary to grayscale structuring elements. This allows to simply
	 * use a {@link Shape} as a type for these structuring elements.
	 * <p>
	 * This method relies on a specified minimal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * dilation, it is sufficient that the specified min value is smaller
	 * (against {@link Comparable}) than any of the value found in the source
	 * image. This normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param minVal
	 *            a T containing set to a value smaller than any of the values
	 *            in the source {@link Img} (against {@link Comparable}. This is
	 *            required to perform a proper mathematical dilation. Because we
	 *            operate on a generic {@link Type}, it has to be provided
	 *            manually.
	 * @param <T>
	 *            the type of the source image and the dilation result. Must be
	 *            a sub-type of <code>T extends {@link Comparable} &
	 *            {@link Type}</code>.
	 * @return a new {@link Img}, of same dimensions than the source.
	 */
	public static < T extends Type< T > & Comparable< T > > Img< T > dilate( final Img< T > source, final Shape strel, final T minVal, int numThreads )
	{
		numThreads = Math.max( 1, numThreads );

		/*
		 * Instantiate target images.
		 */

		final Img< T > dilated = source.factory().create( source, source.firstElement().copy() );

		/*
		 * Prepare iteration.
		 */

		final RandomAccessibleInterval< Neighborhood< T >> accessible;
		if ( numThreads > 1 )
		{
			accessible = strel.neighborhoodsRandomAccessibleSafe( source );
		}
		else
		{
			accessible = strel.neighborhoodsRandomAccessible( source );
		}

		/*
		 * Multithread
		 */

		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( dilated.size(), numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );
		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology dilate thread " + i )
			{
				@Override
				public void run()
				{
					final RandomAccess< Neighborhood< T >> randomAccess = accessible.randomAccess( source );
					final Cursor< T > cursorDilated = dilated.cursor();
					cursorDilated.jumpFwd( chunk.getStartPosition() );

					final T max = source.firstElement().createVariable();
					for ( long steps = 0; steps < chunk.getLoopSize(); steps++ )
					{
						cursorDilated.fwd();
						randomAccess.setPosition( cursorDilated );
						final Neighborhood< T > neighborhood = randomAccess.get();
						final Cursor< T > nc = neighborhood.cursor();

						/*
						 * Look for max in the neighborhood.
						 */

						max.set( minVal );
						while ( nc.hasNext() )
						{
							nc.fwd();
							if ( !Intervals.contains( source, nc ) )
							{
								continue;
							}
							final T val = nc.get();
							// We need only Comparable to do this:
							if ( val.compareTo( max ) > 0 )
							{
								max.set( val );
							}
						}
						cursorDilated.get().set( max );
					}

				}
			};
		}

		/*
		 * Launch calculation
		 */

		SimpleMultiThreading.startAndJoin( threads );

		/*
		 * Return
		 */

		return dilated;
	}

	/*
	 * EROSION
	 */

	/**
	 * Performs the erosion morphological operation, on an {@link Img} using a
	 * {@link Shape} as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * The result image has the same dimensions that of the source image. It is
	 * limited to flat structuring elements, only having <code>on/off</code>
	 * pixels, contrary to grayscale structuring elements. This allows to simply
	 * use a {@link Shape} as a type for these structuring elements.
	 * <p>
	 * This method relies on a specified maximal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * erosion, it is sufficient that the specified max value is larger (against
	 * {@link Comparable}) than any of the value found in the source image. This
	 * normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source {@link Img} (against {@link Comparable}. This is
	 *            required to perform a proper mathematical erosion. Because we
	 *            operate on a generic {@link Type}, it has to be provided
	 *            manually.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of
	 * 
	 *            <code>T extends {@link Comparable} & {@link Type}</code>.
	 * @return a new {@link Img}, of same dimensions than the source.
	 */
	public static < T extends Type< T > & Comparable< T > > Img< T > erode( final Img< T > source, final Shape strel, final T maxVal, int numThreads )
	{
		numThreads = Math.max( 1, numThreads );

		/*
		 * Instantiate target images.
		 */

		final Img< T > eroded = source.factory().create( source, source.firstElement().copy() );

		/*
		 * Prepare iteration.
		 */

		final RandomAccessibleInterval< Neighborhood< T >> accessible;
		if ( numThreads > 1 )
		{
			accessible = strel.neighborhoodsRandomAccessibleSafe( source );
		}
		else
		{
			accessible = strel.neighborhoodsRandomAccessible( source );
		}

		/*
		 * Multithread
		 */

		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( eroded.size(), numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );
		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology erode thread " + i )
			{
				@Override
				public void run()
				{
					final RandomAccess< Neighborhood< T >> randomAccess = accessible.randomAccess( source );
					final Cursor< T > cursorEroded = eroded.cursor();
					cursorEroded.jumpFwd( chunk.getStartPosition() );

					final T min = source.firstElement().createVariable();
					for ( long steps = 0; steps < chunk.getLoopSize(); steps++ )
					{
						cursorEroded.fwd();
						randomAccess.setPosition( cursorEroded );
						final Neighborhood< T > neighborhood = randomAccess.get();
						final Cursor< T > nc = neighborhood.cursor();

						/*
						 * Look for max in the neighborhood.
						 */

						min.set( maxVal );
						while ( nc.hasNext() )
						{
							nc.fwd();
							if ( !Intervals.contains( source, nc ) )
							{
								continue;
							}
							final T val = nc.get();
							// We need only Comparable to do this:
							if ( val.compareTo( min ) < 0 )
							{
								min.set( val );
							}
						}
						cursorEroded.get().set( min );
					}

				}
			};
		}

		/*
		 * Launch calculation
		 */

		SimpleMultiThreading.startAndJoin( threads );

		/*
		 * Return
		 */

		return eroded;
	}

	/**
	 * Performs the erosion morphological operation, on a {@link RealType}
	 * {@link Img} using a {@link Shape} as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * The result image has the same dimensions that of the source image. It is
	 * limited to flat structuring elements, only having <code>on/off</code>
	 * pixels, contrary to grayscale structuring elements. This allows to simply
	 * use a {@link Shape} as a type for these structuring elements.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link RealType}</code>.
	 * @return a new {@link Img}, of same dimensions than the source.
	 */
	public static < T extends RealType< T >> Img< T > erode( final Img< T > source, final Shape strel, final int numThreads )
	{
		final T maxVal = source.firstElement().createVariable();
		maxVal.setReal( maxVal.getMaxValue() );
		return erode( source, strel, maxVal, numThreads );
	}

	/**
	 * Performs the full erosion morphological operation, on an {@link Img}
	 * using a {@link Shape} as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * This method performs what is called the 'full' erosion. That is: the
	 * result image has its dimension shrunk by the structuring element, with
	 * respect to the source image. It is limited to flat structuring elements,
	 * only having <code>on/off</code> pixels, contrary to grayscale structuring
	 * elements. This allows to simply use a {@link Shape} as a type for these
	 * structuring elements.
	 * <p>
	 * This method relies on a specified maximal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * erosion, it is sufficient that the specified max value is greater
	 * (against {@link Comparable}) than any of the value found in the source
	 * image. This normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * <p>
	 * <b>Warning:</b> Current implementation does not do <i>stricto sensu</i>
	 * the full erosion. Indeed, if the structuring element has less dimensions
	 * than the source {@link Img}, they are not pruned, and the returned
	 * {@link Img} has the same number of dimensions that of the source (but
	 * shrunk). This is due to the fact that we use a {@link Shape} for
	 * structuring elements, and that it does not return a number of dimensions.
	 * The neighborhood created have therefore at most as many dimensions as the
	 * source image. The real, full erosion results should have a number of
	 * dimensions equals to the minimum of the number of dimension of both
	 * source and structuring element.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source {@link Img} (against {@link Comparable}. This is
	 *            required to perform a proper mathematical erosion. Because we
	 *            operate on a generic {@link Type}, it has to be provided
	 *            manually.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link Comparable} & {@link Type}
	 *            </code>.
	 * @return a new {@link Img}, possibly of smaller dimensions than the
	 *         source.
	 */
	public static < T extends Type< T > & Comparable< T > > Img< T > erodeFull( final Img< T > source, final Shape strel, final T maxVal, int numThreads )
	{
		numThreads = Math.max( 1, numThreads );

		/*
		 * Compute target image size
		 */

		final long[] targetDims;

		/*
		 * Get a neighborhood to play with. Note: if we would have a dedicated
		 * interface for structuring elements, that would extend Shape and
		 * Dimensions, we would need to do what we are going to do now. On top
		 * of that, this is the part that causes the full dilation not to be a
		 * real full dilation: if the structuring element has more dimensions
		 * than the source, they are ignored. This is because we use the source
		 * as the Dimension to create the sample neighborhood we play with.
		 */
		final Neighborhood< BitType > sampleNeighborhood = MorphologyUtils.getNeighborhood( strel, source );
		int ndims = sampleNeighborhood.numDimensions();
		ndims = Math.max( ndims, source.numDimensions() );
		targetDims = new long[ ndims ];
		for ( int d = 0; d < ndims; d++ )
		{
			long d1;
			if ( d < source.numDimensions() )
			{
				d1 = source.dimension( d );
			}
			else
			{
				d1 = 1;
			}

			long d2;
			if ( d < sampleNeighborhood.numDimensions() )
			{
				d2 = sampleNeighborhood.dimension( d );
			}
			else
			{
				d2 = 1;
			}

			targetDims[ d ] = Math.max( 1, d1 - ( d2 - 1 ) );
			// At least of size 1 in all dimensions. We do not prune dimensions.
		}

		/*
		 * Instantiate target images.
		 */

		final Img< T > eroded = source.factory().create( targetDims, source.firstElement().copy() );
		// Offset coordinates so that they match the source coordinates, which
		// will not be extended.
		final long[] offset = new long[ eroded.numDimensions() ];
		for ( int d = 0; d < offset.length; d++ )
		{
			if ( d < sampleNeighborhood.numDimensions() )
			{
				offset[ d ] = Math.min( sampleNeighborhood.min( d ), eroded.dimension( d ) - 1 );
			}
			else
			{
				offset[ d ] = 0;
			}
		}
		final IntervalView< T > offsetEroded = Views.offset( eroded, offset );

		/*
		 * Prepare iteration.
		 */

		final RandomAccessibleInterval< Neighborhood< T >> accessible;
		if ( numThreads > 1 )
		{
			accessible = strel.neighborhoodsRandomAccessibleSafe( source );
		}
		else
		{
			accessible = strel.neighborhoodsRandomAccessible( source );
		}
		final IterableInterval< T > iterable = Views.iterable( offsetEroded );

		/*
		 * Multithread
		 */

		final Vector< Chunk > chunks = SimpleMultiThreading.divideIntoChunks( iterable.size(), numThreads );
		final Thread[] threads = SimpleMultiThreading.newThreads( numThreads );
		for ( int i = 0; i < threads.length; i++ )
		{
			final Chunk chunk = chunks.get( i );
			threads[ i ] = new Thread( "Morphology erode thread " + i )
			{
				@Override
				public void run()
				{
					final RandomAccess< Neighborhood< T >> randomAccess = accessible.randomAccess( source );
					final Cursor< T > cursorEroded = iterable.cursor();
					cursorEroded.jumpFwd( chunk.getStartPosition() );

					final T min = source.firstElement().createVariable();
					for ( long steps = 0; steps < chunk.getLoopSize(); steps++ )
					{
						cursorEroded.fwd();
						randomAccess.setPosition( cursorEroded );
						final Neighborhood< T > neighborhood = randomAccess.get();
						final Cursor< T > nc = neighborhood.cursor();

						/*
						 * Look for max in the neighborhood.
						 */

						min.set( maxVal );
						while ( nc.hasNext() )
						{
							nc.fwd();
							if ( !Intervals.contains( source, nc ) )
							{
								continue;
							}
							final T val = nc.get();
							// We need only Comparable to do this:
							if ( val.compareTo( min ) < 0 )
							{
								min.set( val );
							}
						}
						cursorEroded.get().set( min );
					}

				}
			};
		}

		/*
		 * Launch calculation
		 */

		SimpleMultiThreading.startAndJoin( threads );

		/*
		 * Return
		 */

		return eroded;
	}

	/**
	 * Performs the full erosion morphological operation, on a {@link RealType}
	 * {@link Img} using a {@link Shape} as a flat structuring element.
	 * 
	 * See <a href="http://en.wikipedia.org/wiki/Erosion_(morphology)">
	 * Erosion_(morphology)</a>.
	 * <p>
	 * This method performs what is called the 'full' erosion. That is: the
	 * result image has its dimension shrunk by the structuring element, with
	 * respect to the source image. It is limited to flat structuring elements,
	 * only having <code>on/off</code> pixels, contrary to grayscale structuring
	 * elements. This allows to simply use a {@link Shape} as a type for these
	 * structuring elements.
	 * <p>
	 * This method relies on a specified maximal value to start comparing to
	 * other pixels in the neighborhood. For this code to properly perform
	 * erosion, it is sufficient that the specified max value is greater
	 * (against {@link Comparable}) than any of the value found in the source
	 * image. This normally unseen parameter is required to operate on
	 * <code>T extends {@link Comparable} & {@link Type}</code>.
	 * <p>
	 * <b>Warning:</b> Current implementation does not do <i>stricto sensu</i>
	 * the full erosion. Indeed, if the structuring element has less dimensions
	 * than the source {@link Img}, they are not pruned, and the returned
	 * {@link Img} has the same number of dimensions that of the source (but
	 * shrunk). This is due to the fact that we use a {@link Shape} for
	 * structuring elements, and that it does not return a number of dimensions.
	 * The neighborhood created have therefore at most as many dimensions as the
	 * source image. The real, full erosion results should have a number of
	 * dimensions equals to the minimum of the number of dimension of both
	 * source and structuring element.
	 * 
	 * @param source
	 *            the source image.
	 * @param strel
	 *            the structuring element as a {@link Shape}.
	 * @param numThreads
	 *            the number of threads to use for the calculation.
	 * @param maxVal
	 *            a T containing set to a value larger than any of the values in
	 *            the source {@link Img} (against {@link Comparable}. This is
	 *            required to perform a proper mathematical erosion. Because we
	 *            operate on a generic {@link Type}, it has to be provided
	 *            manually.
	 * @param <T>
	 *            the type of the source image and the erosion result. Must be a
	 *            sub-type of <code>T extends {@link Comparable} & {@link Type}
	 *            </code>.
	 * @return a new {@link Img}, possibly of smaller dimensions than the
	 *         source.
	 */
	public static < T extends RealType< T > > Img< T > erodeFull( final Img< T > source, final Shape strel, final int numThreads )
	{
		final T maxVal = source.firstElement().createVariable();
		maxVal.setReal( maxVal.getMaxValue() );
		return erodeFull( source, strel, maxVal, numThreads );
	}
}