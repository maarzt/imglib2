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

package net.imglib2.algorithm.gauss;

import net.imglib2.function.Converter;
import net.imglib2.image.Image;
import net.imglib2.image.ImageFactory;
import net.imglib2.outofbounds.OutOfBoundsStrategyFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;

/**
 * TODO
 *
 * @author Stephan Preibisch
 */
public class GaussianConvolution2< A extends Type<A>, B extends NumericType<B> > extends GaussianConvolution3<A, B, B>
{
	public GaussianConvolution2( final Image<A> image, final ImageFactory<B> factoryProcess, final OutOfBoundsStrategyFactory<B> outOfBoundsFactory, final Converter<A, B> converterIn, final double[] sigma )
	{
		super( image, factoryProcess, null, outOfBoundsFactory, converterIn, null, sigma );
	}
	
	public GaussianConvolution2( final Image<A> image, final ImageFactory<B> factoryProcess, final OutOfBoundsStrategyFactory<B> outOfBoundsFactory, final Converter<A, B> converterIn, final double sigma )
	{
		this( image, factoryProcess, outOfBoundsFactory, converterIn, createArray( image, sigma ) );
	}
	
	protected Image<B> getConvolvedImage()
	{
        final Image<B> output;
        
        if ( numDimensions % 2 == 0 )
        {
        	output = temp1;
            
        	// close other temporary datastructure
            temp2.close();
        }
        else
        {
        	output = temp2;

        	// close other temporary datastructure
            temp1.close();
        }
		
		return output;		
	}
	
}