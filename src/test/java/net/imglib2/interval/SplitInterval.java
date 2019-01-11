package net.imglib2.interval;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SplitInterval
{
	public static void split( Interval in, Consumer< Interval > out ) {
		int longestDimensions = getLongestDimensions( in );
		long[] min = Intervals.minAsLongArray( in );
		long[] max = Intervals.maxAsLongArray( in );
		long mid = ( min[ longestDimensions ] + max[ longestDimensions ] ) / 2;
		max[longestDimensions] = mid;
		out.accept( new FinalInterval( min, max ) );
		max[longestDimensions] = in.min( longestDimensions );
		min[longestDimensions] = mid;
		out.accept( new FinalInterval( min, max ) );
	}

	public static List<Interval> split( List<Interval> in ) {
		List<Interval> result = new ArrayList<>( in.size() * 2 );
		for(Interval interval : in)
			split( interval, result::add );
		return result;
	}

	private static int getLongestDimensions( Interval in )
	{
		int longestDimensions = 0;
		long longestLength = -1;
		for ( int d = 0; d < in.numDimensions(); d++ )
		{
			final long l = in.dimension( d );
			if ( longestLength < l )
			{
				longestDimensions = d;
				longestLength = l;
			}
		}
		return longestDimensions;
	}
}
