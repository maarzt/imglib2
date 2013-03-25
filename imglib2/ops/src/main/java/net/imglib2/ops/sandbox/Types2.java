package net.imglib2.ops.sandbox;


import java.math.BigInteger;


// Mostly from:
// http://www.haskell.org/ghc/docs/latest/html/libraries/base/Prelude.html

public class Types2 {

	// **************************************************************
	// TYPES
	// **************************************************************

	private interface Factory<T> {

		T create();

		T copy();
	}

	/**
	 * Things that can be tested for equality: isEqual, isNotEqual
	 */
	private interface Equable<T> {}

	/**
	 * Things that are ordered and can test less than, greater than, etc.
	 */
	private interface Ordered<T> extends Equable<T> {}

	/**
	 * Things that can be incremented and decremented
	 */
	private interface Enumerable<T> {}

	/**
	 * Things that have bounds that can be queried
	 */
	private interface Bounded<T> {}

	/**
	 * Numbers that can be added, subtracted, raised to powers, etc.
	 */
	private interface Number<T> {}

	/**
	 * Real numbers that can be converted to/from (sometimes approximate)
	 * fractions.
	 */
	private interface Real<T> extends Number<T>, Ordered<T> {}

	/**
	 * Whole numbers with which we can do div, mod, gcd, isOdd, etc.
	 */
	private interface Integral<T> extends Real<T>, Enumerable<T> {}

	/**
	 * A ratio based on Integers
	 */
	private interface Rational extends Ratio<Integer> {
		// nothing to declare
	}

	/**
	 * A fraction
	 */
	private interface Fractional<T> extends Number<T> {}

	/**
	 * A floating point type that supports operations like sin, cos, log, etc.
	 */
	private interface Floating<T> extends Fractional<T> {}

	/**
	 * A real that can support fractional methods like trunc, round, ceil, floor.
	 */
	private interface RealFrac<T> extends Real<T>, Fractional<T> {}

	/**
	 * A real that can support encoding, decoding, isNaN, isInfinite, etc.
	 */
	private interface RealFloat<T> extends RealFrac<T>, Floating<T> {}

	// **************************************************************
	// DATA IMPLEMENTATIONS
	// **************************************************************
	
	// primitive boolean type
	// TODO - make it a class

	private interface Bool extends Factory<Bool> {

		boolean getValue();

		void getValue(Bool result);

		void setValue(boolean b);

		void setValue(Bool b);
	}

	// primitive integer type
	// TODO - make it a class

	private interface Int extends Bounded<Int>, Integral<Int>, Factory<Int> {

		int getValue();

		void getValue(Int result);

		void setValue(int i);

		void setValue(Int i);
	}

	// unbounded integer type
	// TODO - make it a class

	private interface Integer extends Integral<Integer>, Factory<Integer> {

		BigInteger getValue();

		void getValue(Integer result);

		void setValue(BigInteger i);

		void setValue(Integer i);
	}

	// TODO - some subtyping not enforced here. See definition at:
	// http://www.haskell.org/ghc/docs/latest/html/libraries/base/Data-Complex.html

	/**
	 * A complex number type
	 */
	private interface Complex<T> extends Equable<Complex<T>>,
		Floating<Complex<T>>, Factory<Complex<T>>
	{
		void real(T result);

		void imag(T result);

		void cartesian(T realResult, T imagResult);

		void magnitude(T result);

		void phase(T result);

		void polar(T magResult, T phaseResult);
	}

	// **************************************************************
	// OPS
	// **************************************************************

	// Equable OPS

	private interface IsEqualOp<T extends Equable<T>> {

		void compute(T a, T b, Bool result);
	}

	private interface IsNotEqualOp<T extends Equable<T>> {

		void compute(T a, T b, Bool result);
	}

	// Ordered OPS

	private interface CompareOp<T extends Ordered<T>> {

		int compute(T a, T b);
	}

	private interface IsLessOp<T extends Ordered<T>> {

		void compute(T a, T b, Bool result);
	}

	private interface IsLessEqualOp<T extends Ordered<T>> {

		void compute(T a, T b, Bool result);
	}

	private interface IsGreaterOp<T extends Ordered<T>> {

		void compute(T a, T b, Bool result);
	}

	private interface IsGreaterEqualOp<T extends Ordered<T>> {

		void compute(T a, T b, Bool result);
	}

	private interface MaxOp<T extends Ordered<T>> {

		void compute(T a, T b, Bool result);
	}

	private interface MinOp<T extends Ordered<T>> {

		void compute(T a, T b, Bool result);
	}

	// Enumerable OPS

	private interface SuccOp<T extends Enumerable<T>> {

		void compute(T a, T result);
	}

	private interface PredOp<T extends Enumerable<T>> {

		void compute(T a, T result);
	}

	// Bounded OPS

	private interface MinBoundOp<T extends Bounded<T>> {

		void compute(T a, T result);
	}
	
	private interface MaxBoundOp<T extends Bounded<T>> {

		void compute(T a, T result);
	}
	
	// Number OPS

	private interface AddOp<T extends Number<T>> {

		void compute(T a, T b, T result);
	}
	
	private interface SubtractOp<T extends Number<T>> {

		void compute(T a, T b, T result);
	}
	
	private interface MultiplyOp<T extends Number<T>> {

		void compute(T a, T b, T result);
	}
	
	private interface DivideOp1<T extends Number<T>> {

		void compute(T a, T b, T result);
	}

	// this is less restrictive than original
	private interface PowerOp1<T extends Number<T>> {

		void compute(T a, T b, T result);
	}

	private interface NegateOp<T extends Number<T>> {

		void compute(T a, T result);
	}
	
	private interface AbsOp<T extends Number<T>> {

		void compute(T a, T result);
	}
	
	private interface SignumOp<T extends Number<T>> {

		void compute(T a, T result);
	}
	
	private interface FromIntegerOp<T extends Number<T>> {

		void compute(Integer a, T result);
	}
	
	// Integral OPS

	private interface QuotientOp<T extends Integral<T>> {

		void compute(T a, T b, T result);
	}
	
	private interface RemainderOp<T extends Integral<T>> {

		void compute(T a, T b, T result);
	}

	private interface QuotientRemainderOp<T extends Integral<T>> {

		void compute(T a, T b, T quotResult, T remResult);
	}

	private interface DivOp<T extends Integral<T>> {

		void compute(T a, T b, T result);
	}

	private interface ModOp<T extends Integral<T>> {

		void compute(T a, T b, T result);
	}

	private interface DivModOp<T extends Integral<T>> {

		void compute(T a, T b, T divResult, T modResult);
	}

	private interface ToIntegerOp<T extends Integral<T>> {

		void compute(T a, Integer result);
	}

	private interface IsEvenOp<T extends Integral<T>> {

		void compute(T a, Bool result);
	}

	private interface IsOddOp<T extends Integral<T>> {

		void compute(T a, Bool result);
	}

	private interface GcdOp<T extends Integral<T>> {

		void compute(T a, T b, T result);
	}

	private interface LcmOp<T extends Integral<T>> {

		void compute(T a, T b, T result);
	}

	private interface FromIntegralOp<T extends Integral<T>> {

		<Z> void compute(T a, Number<Z> result);
	}

	// Real OPS
	
	private interface ToRationalOp<T extends Real<T>> {

		void compute(T a, Rational result);
	}

	private interface RealToFracOp<T extends Real<T>> {

		<Z> void compute(T a, Fractional<Z> result);
	}

	// Ratio OPS

	private interface Ratio<T> {

		Ratio<T> create(Integral<T> numer, Integral<T> denom);

		void numerator(T result);

		void denominator(T result);
	}

	// TODO: not an OP?
	private interface CreateRatioOp<T> {

		void compute(Integral<T> numer, Integral<T> denom, Ratio<T> result);
	}
	
	private interface ApproxRationalOp<T> {

		void compute(RealFrac<T> a, RealFrac<T> b, Rational result);
	}

	// Rational OPS

	// Fractional OPS

	private interface DivideOp2<T extends Fractional<T>> {

		void compute(T a, T b, T result);
	}

	private interface RecipOp<T extends Fractional<T>> {

		void compute(T a, T result);
	}

	private interface FromRationalOp<T extends Fractional<T>> {

		void compute(Rational a, T result);
	}

	private interface PowerOp2<T extends Fractional<T>> {

		<Z> void compute(T a, Integral<Z> b, T result);
	}

	// Floating OPS

	private interface PiOp<T> extends Fractional<T> {

		void compute(T result);
	}

	private interface EOp<T> extends Fractional<T> {

		void compute(T result);
	}
	
	private interface ExpOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}

	private interface SqrtOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}

	private interface LogOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}

	private interface PowOp<T> extends Fractional<T> {

		void compute(T a, T b, T result);
	}
	
	private interface SinOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}

	private interface CosOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}
	
	private interface TanOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}
	
	private interface ArcSinOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}
	
	private interface ArcCosOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}
	
	private interface ArcTanOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}
	
	private interface SinhOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}
	
	private interface CoshOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}
	
	private interface TanhOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}
	
	private interface ArcSinhOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}
	
	private interface ArcCoshOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}
	
	private interface ArcTanhOp<T> extends Fractional<T> {

		void compute(T a, T result);
	}

	// RealFrac OPS

	private interface ProperFractionOp<T> extends RealFrac<T> {

		<Z> void compute(T a, Integral<Z> intResult, T fracResult);
	}

	private interface TruncateOp<T> extends RealFrac<T> {

		<Z> void compute(T a, Integral<Z> result);
	}

	private interface RoundOp<T> extends RealFrac<T> {

		<Z> void compute(T a, Integral<Z> result);
	}

	private interface CeilingOp<T> extends RealFrac<T> {

		<Z> void compute(T a, Integral<Z> result);
	}

	private interface FloorOp<T> extends RealFrac<T> {

		<Z> void floor(T a, Integral<Z> result);
	}

	// RealFloat OPS

	private interface FloatRadixOp<T> extends RealFloat<T> {

		void compute(T a, Integer result);
	}

	private interface FloatDigitsOp<T> extends RealFloat<T> {

		void compute(T a, Int result);
	}

	private interface FloatRangeOp<T> extends RealFloat<T> {

		void compute(T a, Int minResult, Int maxResult);
	}

	private interface DecodeFloatOp<T> extends RealFloat<T> {

		void compute(T a, Integer significandResult, Int exponentResult);
	}

	private interface EncodeFloatOp<T> extends RealFloat<T> {

		void compute(Integer significand, Int exponent, T result);
	}

	private interface ExponentOp<T> extends RealFloat<T> {

		void compute(T a, Int result);
	}

	private interface SignificandOp<T> extends RealFloat<T> {

		void compute(T a, T result);
	}

	private interface ScaleFloatOp<T> extends RealFloat<T> {

		void compute(T a, Int power, T result);
	}

	private interface isNanOp<T> extends RealFloat<T> {

		boolean compute(T a, Bool result);
	}

	private interface isInfiniteOp<T> extends RealFloat<T> {

		boolean compute(T a, Bool result);
	}

	private interface isDenormalizedOp<T> extends RealFloat<T> {

		boolean compute(T a, Bool result);
	}

	private interface isNegativeZeroOp<T> extends RealFloat<T> {

		boolean compute(T a, Bool result);
	}

	private interface isIEEEOp<T> extends RealFloat<T> {

		boolean compute(T a, Bool result);
	}

	private interface Atan2Op<T> extends RealFloat<T> {

		void compute(T x, T y, T result);
	}

	// Complex OPS

	private interface MakePolarOp<T> {

		Complex<T> compute(RealFloat<T> mag, RealFloat<T> phase);
	}

	private interface MakeCartesianOp<T> {

		Complex<T> compute(RealFloat<T> mag, RealFloat<T> phase);
	}
	
	// TODO - I might have done this one wrong: maybe no 1st param and maybe a
	// constructor
	private interface CisOp<T> {

		void compute(Complex<T> a, RealFloat<T> b, Complex<T> result);
	}
	
	private interface ConjugateOp<T> {

		void compute(Complex<T> a, Complex<T> result);
	}

}
