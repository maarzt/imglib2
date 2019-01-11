package net.imglib2.interval;

import net.imglib2.DefaultInterval;

public class FinalDefaultInterval implements DefaultInterval
{
	private final long[] min;

	private final long[] max;

	public FinalDefaultInterval( long[] min, long[] max )
	{
		assert min.length == max.length;
		this.min = min.clone();
		this.max = max.clone();
	}

	@Override
	public long min( int d )
	{
		return min[ d ];
	}

	@Override
	public long max( int d )
	{
		return max[ d ];
	}

	@Override
	public int numDimensions()
	{
		return min.length;
	}
}
