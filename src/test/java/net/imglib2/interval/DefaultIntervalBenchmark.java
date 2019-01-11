package net.imglib2.interval;

import net.imglib2.DefaultInterval;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
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
				.forks( 0 )
				.warmupIterations( 4 )
				.measurementIterations( 8 )
				.warmupTime( TimeValue.milliseconds( 100 ) )
				.measurementTime( TimeValue.milliseconds( 100 ) )
				.build();
		new Runner( opt ).run();
	}
}
