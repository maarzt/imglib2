package net.imglib2.interval;

import net.imglib2.AbstractInterval;
import net.imglib2.DefaultInterval;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@State( Scope.Benchmark )
public class DefaultIntervalBenchmark
{

	long[] min = {1,2,3};
	long[] max = {4,5,6};
	Point point = new Point(1,2,3);

	DefaultInterval defaultInterval = new FinalDefaultInterval(min, max);

	Interval interval = new FinalInterval(min, max);

	long[][] positions = new long[100][3];
	double[][] realPositions = new double[100][3];

	Interval a = new IntervalA();
	Interval b = new IntervalB();
	Interval c = new IntervalC();

	DefaultInterval defaultA = new DefaultIntervalA();
	DefaultInterval defaultB = new DefaultIntervalB();
	DefaultInterval defaultC = new DefaultIntervalC();

	@Benchmark
	public void abc() {
		for ( double[] position : realPositions ) {
			a.realMin( position );
			b.realMin( position );
			c.realMin( position );
		}
	}

	@Benchmark
	public void abcDefault() {
		for ( double[] position : realPositions ) {
			defaultA.realMin( position );
			defaultB.realMin( position );
			defaultC.realMin( position );
		}
	}

	@Benchmark
	public void benchmarkMin() {
		for ( long[] position : positions )
			interval.min( position );
	}

	@Benchmark
	public void benchmarkMinDefault() {
		for ( long[] position : positions )
			defaultInterval.min( position );
	}

	@Benchmark
	public void benchmarkRealMin() {
		for ( double[] position : realPositions )
			interval.realMin( position );
	}

	@Benchmark
	public void benchmarkRealMinDefault() {
		for ( double[] position : realPositions )
			defaultInterval.realMin( position );
	}


	IntervalView<UnsignedByteType> image = Views.interval( Views.extendBorder( ArrayImgs.unsignedBytes( new byte[]{ 1, 2, 3, 4 }, 2, 2) ), Intervals.createMinMax( -100, -100, 100, 100 ) );

	@Benchmark
	public void iterateExtended( Blackhole blackhole ) {
		long sum = 0;
		for( UnsignedByteType pixel : image ) {
			sum = pixel.get();
		}
		blackhole.consume( sum );
	}


	public static void main( final String... args ) throws RunnerException
	{
		final Options opt = new OptionsBuilder()
				.include( DefaultIntervalBenchmark.class.getSimpleName() )
				.forks( 1 )
				.warmupIterations( 4 )
				.measurementIterations( 8 )
				.warmupTime( TimeValue.milliseconds( 100 ) )
				.measurementTime( TimeValue.milliseconds( 100 ) )
				.build();
		new Runner( opt ).run();
	}

	private static class DefaultIntervalA implements DefaultInterval {

		@Override
		public long min( int d )
		{
			return - d - 10;
		}

		@Override
		public long max( int d )
		{
			return + d;
		}

		@Override
		public int numDimensions()
		{
			return 3;
		}
	}

	long[] bmin = {2, 2, 2};
	long[] bmax = {24, 25, 89};

	private class DefaultIntervalB implements DefaultInterval {

		@Override
		public long min( int d )
		{
			return bmin[d];
		}

		@Override
		public long max( int d )
		{
			return bmax[d];
		}

		@Override
		public int numDimensions()
		{
			return 3;
		}
	}

	private class DefaultIntervalC implements DefaultInterval {

		@Override
		public long min( int d )
		{
			return - bmin[d];
		}

		@Override
		public long max( int d )
		{
			return bmax[d] + d;
		}

		@Override
		public int numDimensions()
		{
			return 3;
		}
	}

	private static class IntervalA extends AbstractInterval
	{

		public IntervalA()
		{
			super( 3 );
		}

		@Override
		public long min( int d )
		{
			return - d - 10;
		}

		@Override
		public long max( int d )
		{
			return + d;
		}
	}

	private class IntervalB extends AbstractInterval {

		public IntervalB()
		{
			super( 3 );
		}

		@Override
		public long min( int d )
		{
			return bmin[d];
		}

		@Override
		public long max( int d )
		{
			return bmax[d];
		}
	}

	private class IntervalC extends AbstractInterval {

		public IntervalC() {
			super( 3 );
		}

		@Override
		public long min( int d )
		{
			return - bmin[d];
		}

		@Override
		public long max( int d )
		{
			return bmax[d] + d;
		}
	}
}
