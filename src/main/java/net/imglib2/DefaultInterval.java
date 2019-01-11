package net.imglib2;

import net.imglib2.Dimensions;
import net.imglib2.Positionable;
import net.imglib2.RealInterval;
import net.imglib2.RealPositionable;

/**
 * @author Tobias Pietzsch
 */
// FIXME: move all the default implementations to the Interval class
public interface DefaultInterval extends RealInterval, Dimensions
{
	long min( final int d );

	long max( final int d );

	@Override
	default double realMin( final int d )
	{
		return min( d );
	}

	@Override
	default void realMin( final double[] min )
	{
		final int n = numDimensions();
		for ( int d = 0; d < n; d++ )
			min[ d ] = realMin( d );
	}

	@Override
	default void realMin( final RealPositionable min )
	{
		final int n = numDimensions();
		for ( int d = 0; d < n; d++ )
			min.setPosition( realMin( d ), d );
	}

	@Override
	default double realMax( final int d )
	{
		return max( d );
	}

	@Override
	default void realMax( final double[] max )
	{
		final int n = numDimensions();
		for ( int d = 0; d < n; d++ )
			max[ d ] = realMax( d );
	}

	@Override
	default void realMax( final RealPositionable max )
	{
		final int n = numDimensions();
		for ( int d = 0; d < n; d++ )
			max.setPosition( realMax( d ), d );
	}

	@Override
	default void dimensions( final long[] dimensions )
	{
		final int n = numDimensions();
		for ( int d = 0; d < n; d++ )
			dimensions[ d ] = dimension( d );
	}

	default void min( final long[] min )
	{
		final int n = numDimensions();
		for ( int d = 0; d < n; d++ )
			min[ d ] = min( d );
	}

	default void min( final Positionable min )
	{
		final int n = numDimensions();
		for ( int d = 0; d < n; d++ )
			min.setPosition( min( d ), d );
	}

	default void max( final long[] max )
	{
		final int n = numDimensions();
		for ( int d = 0; d < n; d++ )
			max[ d ] = max( d );
	}

	default void max( final Positionable max )
	{
		final int n = numDimensions();
		for ( int d = 0; d < n; d++ )
			max.setPosition( max( d ), d );
	}

	@Override
	default long dimension( int d )
	{
		return max( d ) - min( d ) + 1;
	}
}
