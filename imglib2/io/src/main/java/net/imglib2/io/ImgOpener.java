/*
ImgLib I/O logic using Bio-Formats.

Copyright (c) 2009, Stephan Preibisch & Stephan Saalfeld.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
  * Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
  * Neither the name of the Fiji project developers nor the
    names of its contributors may be used to endorse or promote products
    derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package net.imglib2.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import loci.common.DataTools;
import loci.common.StatusEvent;
import loci.common.StatusListener;
import loci.common.StatusReporter;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelFiller;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MinMaxCalculator;
import loci.formats.ReaderWrapper;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.CharArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.sampler.special.OrthoSliceCursor;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import ome.xml.model.primitives.PositiveFloat;

/**
 * Reads in an {@link ImgPlus} using Bio-Formats.
 * 
 * @author Curtis Rueden
 * @author Stephan Preibisch
 */
public class ImgOpener implements StatusReporter {

	// -- Fields --

	private final List<StatusListener> listeners =
		new ArrayList<StatusListener>();

	// -- ImgOpener methods --

	/**
	 * Reads in an {@link ImgPlus} from the given source. It will read it into a
	 * {@link PlanarImg}, where the {@link Type} T is defined by the file format
	 * and implements {@link RealType} and {@link NativeType}.
	 * 
	 * @param id The source of the image (e.g., a file on disk).
	 * @throws ImgIOException if there is a problem reading the image data.
	 * @throws IncompatibleTypeException if the {@link Type} of the file is
	 *           incompatible with the {@link PlanarImg}.
	 */
	public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final String id) throws ImgIOException, IncompatibleTypeException
	{
		return openImg(id, new PlanarImgFactory<T>());
	}

	/**
	 * Reads in an {@link ImgPlus} from the given source. It will read it into a
	 * {@link PlanarImg}, where the {@link Type} T is defined by the file format
	 * and implements {@link RealType} and {@link NativeType}.
	 * 
	 * @param id The source of the image (e.g., a file on disk).
	 * @param computeMinMax If set, the {@link ImgPlus}'s channel minimum and
	 *          maximum metadata is computed and populated based on the data's
	 *          actual pixel values.
	 * @throws ImgIOException if there is a problem reading the image data.
	 * @throws IncompatibleTypeException if the {@link Type} of the file is
	 *           incompatible with the {@link PlanarImg}.
	 */
	public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final String id, final boolean computeMinMax) throws ImgIOException,
		IncompatibleTypeException
	{
		return openImg(id, new PlanarImgFactory<T>(), computeMinMax);
	}

	/**
	 * Reads in an {@link ImgPlus} from the given source, using the specified
	 * {@link ImgFactory} to construct the resultant {@link Img}. The {@link Type}
	 * T is defined by the file format and implements {@link RealType} and
	 * {@link NativeType}. The {@link Type} of the {@link ImgFactory} will be
	 * ignored.
	 * 
	 * @param id The source of the image (e.g., a file on disk).
	 * @param imgFactory The {@link ImgFactory} to use for creating the resultant
	 *          {@link ImgPlus}.
	 * @throws ImgIOException if there is a problem reading the image data.
	 * @throws IncompatibleTypeException if the Type of the {@link Img} is
	 *           incompatible with the {@link ImgFactory}
	 */
	public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final String id, final ImgFactory<T> imgFactory) throws ImgIOException,
		IncompatibleTypeException
	{
		return openImg(id, imgFactory, true);
	}

	/**
	 * Reads in an {@link ImgPlus} from the given source, using the specified
	 * {@link ImgFactory} to construct the {@link Img}. The {@link Type} T is
	 * defined by the file format and implements {@link RealType} and
	 * {@link NativeType}. The {@link Type} of the {@link ImgFactory} will be
	 * ignored.
	 * 
	 * @param id The source of the image (e.g., a file on disk).
	 * @param imgFactory The {@link ImgFactory} to use for creating the resultant
	 *          {@link ImgPlus}.
	 * @param computeMinMax If set, the {@link ImgPlus}'s channel minimum and
	 *          maximum metadata is computed and populated based on the data's
	 *          actual pixel values.
	 * @throws ImgIOException if there is a problem reading the image data.
	 * @throws IncompatibleTypeException if the {@link Type} of the {@link Img} is
	 *           incompatible with the {@link ImgFactory}
	 */
	public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final String id, final ImgFactory<T> imgFactory,
		final boolean computeMinMax) throws ImgIOException,
		IncompatibleTypeException
	{
		try {
			final IFormatReader r = initializeReader(id);
			final T type = makeType(r.getPixelType());
			final ImgFactory<T> imgFactoryT = imgFactory.imgFactory(type);
			return openImg(r, imgFactoryT, type, computeMinMax);
		}
		catch (final FormatException e) {
			throw new ImgIOException(e);
		}
		catch (final IOException e) {
			throw new ImgIOException(e);
		}
	}

	/**
	 * Reads in an {@link ImgPlus} from the given source, using the given
	 * {@link ImgFactory} to construct the {@link Img}. The {@link Type} T to read
	 * is defined by the third parameter.
	 * 
	 * @param imgFactory The {@link ImgFactory} to use for creating the resultant
	 *          {@link ImgPlus}.
	 * @param type The {@link Type} T of the output {@link ImgPlus}, which must
	 *          match the typing of the {@link ImgFactory}.
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final String id, final ImgFactory<T> imgFactory, final T type)
		throws ImgIOException
	{
		try {
			final IFormatReader r = initializeReader(id);
			return openImg(r, imgFactory, type);
		}
		catch (final FormatException e) {
			throw new ImgIOException(e);
		}
		catch (final IOException e) {
			throw new ImgIOException(e);
		}
	}

	/**
	 * Reads in an {@link ImgPlus} from the given initialized
	 * {@link IFormatReader}, using the given {@link ImgFactory} to construct the
	 * {@link Img}. The {@link Type} T to read is defined by the third parameter.
	 * 
	 * @param r An initialized {@link IFormatReader} to use for reading image
	 *          data.
	 * @param imgFactory The {@link ImgFactory} to use for creating the resultant
	 *          {@link ImgPlus}.
	 * @param type The {@link Type} T of the output {@link ImgPlus}, which must
	 *          match the typing of the {@link ImgFactory}.
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final IFormatReader r, final ImgFactory<T> imgFactory, final T type)
		throws ImgIOException
	{
		return openImg(r, imgFactory, type, true);
	}

	/**
	 * Reads in an {@link ImgPlus} from the given initialized
	 * {@link IFormatReader}, using the given {@link ImgFactory} to construct the
	 * {@link Img}. The {@link Type} T to read is defined by the third parameter.
	 * 
	 * @param r An initialized {@link IFormatReader} to use for reading image
	 *          data.
	 * @param imgFactory The {@link ImgFactory} to use for creating the resultant
	 *          {@link ImgPlus}.
	 * @param type The {@link Type} T of the output {@link ImgPlus}, which must
	 *          match the typing of the {@link ImgFactory}.
	 * @param computeMinMax If set, the {@link ImgPlus}'s channel minimum and
	 *          maximum metadata is computed and populated based on the data's
	 *          actual pixel values.
	 * @throws ImgIOException if there is a problem reading the image data.
	 */
	public <T extends RealType<T> & NativeType<T>> ImgPlus<T> openImg(
		final IFormatReader r, final ImgFactory<T> imgFactory, final T type,
		final boolean computeMinMax) throws ImgIOException
	{
		// create image and read metadata
		final long[] dimLengths = getDimLengths(r);
		final Img<T> img = imgFactory.create(dimLengths, type);
		final ImgPlus<T> imgPlus = makeImgPlus(img, r);

		// read pixels
		final long startTime = System.currentTimeMillis();
		final String id = r.getCurrentFile();
		final int planeCount = r.getImageCount();
		try {
			readPlanes(r, type, imgPlus, computeMinMax);
		}
		catch (final FormatException e) {
			throw new ImgIOException(e);
		}
		catch (final IOException e) {
			throw new ImgIOException(e);
		}
		final long endTime = System.currentTimeMillis();
		final float time = (endTime - startTime) / 1000f;
		notifyListeners(new StatusEvent(planeCount, planeCount, id + ": read " +
			planeCount + " planes in " + time + "s"));

		return imgPlus;
	}

	// TODO: eliminate getPlanarAccess in favor of utility method elsewhere.

	/** Obtains planar access instance backing the given img, if any. */
	@SuppressWarnings("unchecked")
	public static PlanarAccess<ArrayDataAccess<?>> getPlanarAccess(
		final ImgPlus<?> img)
	{
		if (img.getImg() instanceof PlanarAccess) {
			return (PlanarAccess<ArrayDataAccess<?>>) img.getImg();
		}
		return null;
	}

	/** Converts Bio-Formats pixel type to imglib Type object. */
	@SuppressWarnings("unchecked")
	public static <T extends RealType<T>> T makeType(final int pixelType) {
		final RealType<?> type;
		switch (pixelType) {
			case FormatTools.UINT8:
				type = new UnsignedByteType();
				break;
			case FormatTools.INT8:
				type = new ByteType();
				break;
			case FormatTools.UINT16:
				type = new UnsignedShortType();
				break;
			case FormatTools.INT16:
				type = new ShortType();
				break;
			case FormatTools.UINT32:
				type = new UnsignedIntType();
				break;
			case FormatTools.INT32:
				type = new IntType();
				break;
			case FormatTools.FLOAT:
				type = new FloatType();
				break;
			case FormatTools.DOUBLE:
				type = new DoubleType();
				break;
			default:
				type = null;
		}
		return (T) type;
	}

	/** Wraps raw primitive array in imglib Array object. */
	public static ArrayDataAccess<?> makeArray(final Object array) {
		final ArrayDataAccess<?> access;
		if (array instanceof byte[]) {
			access = new ByteArray((byte[]) array);
		}
		else if (array instanceof char[]) {
			access = new CharArray((char[]) array);
		}
		else if (array instanceof double[]) {
			access = new DoubleArray((double[]) array);
		}
		else if (array instanceof int[]) {
			access = new IntArray((int[]) array);
		}
		else if (array instanceof float[]) {
			access = new FloatArray((float[]) array);
		}
		else if (array instanceof short[]) {
			access = new ShortArray((short[]) array);
		}
		else if (array instanceof long[]) {
			access = new LongArray((long[]) array);
		}
		else access = null;
		return access;
	}

	// -- StatusReporter methods --

	/** Adds a listener to those informed when progress occurs. */
	@Override
	public void addStatusListener(final StatusListener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
	}

	/** Removes a listener from those informed when progress occurs. */
	@Override
	public void removeStatusListener(final StatusListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}

	/** Notifies registered listeners of progress. */
	@Override
	public void notifyListeners(final StatusEvent e) {
		synchronized (listeners) {
			for (final StatusListener l : listeners)
				l.statusUpdated(e);
		}
	}

	// -- Helper methods --

	/** Constructs and initializes a Bio-Formats reader for the given file. */
	private IFormatReader initializeReader(final String id)
		throws FormatException, IOException
	{
		notifyListeners(new StatusEvent("Initializing " + id));

		IFormatReader r = null;
		r = new ImageReader();
		r = new ChannelFiller(r);
		r = new ChannelSeparator(r);
		r = new MinMaxCalculator(r);

		// attach OME-XML metadata object to reader
		try {
			final ServiceFactory factory = new ServiceFactory();
			final OMEXMLService service = factory.getInstance(OMEXMLService.class);
			final IMetadata meta = service.createOMEXMLMetadata();
			r.setMetadataStore(meta);
		}
		catch (final ServiceException e) {
			throw new FormatException(e);
		}
		catch (final DependencyException e) {
			throw new FormatException(e);
		}

		r.setId(id);

		return r;
	}

	/** Compiles an N-dimensional list of axis types from the given reader. */
	private Axis[] getDimTypes(final IFormatReader r) {
		final int sizeX = r.getSizeX();
		final int sizeY = r.getSizeY();
		final int sizeZ = r.getSizeZ();
		final int sizeT = r.getSizeT();
		final String[] cDimTypes = r.getChannelDimTypes();
		final int[] cDimLengths = r.getChannelDimLengths();
		final String dimOrder = r.getDimensionOrder();
		final List<Axis> dimTypes = new ArrayList<Axis>();

		// add core dimensions
		for (final char dim : dimOrder.toCharArray()) {
			switch (dim) {
				case 'X':
					if (sizeX > 1) dimTypes.add(Axes.X);
					break;
				case 'Y':
					if (sizeY > 1) dimTypes.add(Axes.Y);
					break;
				case 'Z':
					if (sizeZ > 1) dimTypes.add(Axes.Z);
					break;
				case 'T':
					if (sizeT > 1) dimTypes.add(Axes.TIME);
					break;
				case 'C':
					for (int c = 0; c < cDimTypes.length; c++) {
						final int len = cDimLengths[c];
						if (len > 1) dimTypes.add(Axes.get(cDimTypes[c]));
					}
					break;
			}
		}

		return dimTypes.toArray(new Axis[0]);
	}

	/** Compiles an N-dimensional list of axis lengths from the given reader. */
	private long[] getDimLengths(final IFormatReader r) {
		final long sizeX = r.getSizeX();
		final long sizeY = r.getSizeY();
		final long sizeZ = r.getSizeZ();
		final long sizeT = r.getSizeT();
		// final String[] cDimTypes = r.getChannelDimTypes();
		final int[] cDimLengths = r.getChannelDimLengths();
		final String dimOrder = r.getDimensionOrder();

		final List<Long> dimLengthsList = new ArrayList<Long>();

		// add core dimensions
		for (int i = 0; i < dimOrder.length(); i++) {
			final char dim = dimOrder.charAt(i);
			switch (dim) {
				case 'X':
					if (sizeX > 1) dimLengthsList.add(sizeX);
					break;
				case 'Y':
					if (sizeY > 1) dimLengthsList.add(sizeY);
					break;
				case 'Z':
					if (sizeZ > 1) dimLengthsList.add(sizeZ);
					break;
				case 'T':
					if (sizeT > 1) dimLengthsList.add(sizeT);
					break;
				case 'C':
					for (int c = 0; c < cDimLengths.length; c++) {
						final long len = cDimLengths[c];
						if (len > 1) dimLengthsList.add(len);
					}
					break;
			}
		}

		// convert result to primitive array
		final long[] dimLengths = new long[dimLengthsList.size()];
		for (int i = 0; i < dimLengths.length; i++) {
			dimLengths[i] = dimLengthsList.get(i);
		}
		return dimLengths;
	}

	/** Compiles an N-dimensional list of calibration values. */
	private double[] getCalibration(final IFormatReader r) {
		final long sizeX = r.getSizeX();
		final long sizeY = r.getSizeY();
		final long sizeZ = r.getSizeZ();
		final long sizeT = r.getSizeT();
		final int[] cDimLengths = r.getChannelDimLengths();
		final String dimOrder = r.getDimensionOrder();

		final IMetadata meta = (IMetadata) r.getMetadataStore();
		final PositiveFloat xCalin = meta.getPixelsPhysicalSizeX(0);
		final PositiveFloat yCalin = meta.getPixelsPhysicalSizeY(0);
		final PositiveFloat zCalin = meta.getPixelsPhysicalSizeZ(0);
		Double tCal = meta.getPixelsTimeIncrement(0);

		final Double xCal, yCal, zCal;

		if (xCalin == null) xCal = Double.NaN;
		else xCal = xCalin.getValue();

		if (yCalin == null) yCal = Double.NaN;
		else yCal = yCalin.getValue();

		if (zCalin == null) zCal = Double.NaN;
		else zCal = zCalin.getValue();

		if (tCal == null) tCal = Double.NaN;

		final List<Double> calibrationList = new ArrayList<Double>();

		// add core dimensions
		for (int i = 0; i < dimOrder.length(); i++) {
			final char dim = dimOrder.charAt(i);
			switch (dim) {
				case 'X':
					if (sizeX > 1) calibrationList.add(xCal);
					break;
				case 'Y':
					if (sizeY > 1) calibrationList.add(yCal);
					break;
				case 'Z':
					if (sizeZ > 1) calibrationList.add(zCal);
					break;
				case 'T':
					if (sizeT > 1) calibrationList.add(tCal);
					break;
				case 'C':
					for (int c = 0; c < cDimLengths.length; c++) {
						final long len = cDimLengths[c];
						if (len > 1) calibrationList.add(Double.NaN);
					}
					break;
			}
		}

		// convert result to primitive array
		final double[] calibration = new double[calibrationList.size()];
		for (int i = 0; i < calibration.length; i++) {
			calibration[i] = calibrationList.get(i);
		}
		return calibration;
	}

	/**
	 * Wraps the given {@link Img} in an {@link ImgPlus} with metadata
	 * corresponding to the specified initialized {@link IFormatReader}.
	 */
	private <T extends RealType<T>> ImgPlus<T> makeImgPlus(final Img<T> img,
		final IFormatReader r) throws ImgIOException
	{
		final String id = r.getCurrentFile();
		final File idFile = new File(id);
		final String name = idFile.exists() ? idFile.getName() : id;

		final Axis[] dimTypes = getDimTypes(r);
		final double[] cal = getCalibration(r);

		final IFormatReader base;
		try {
			base = unwrap(r);
		}
		catch (final FormatException exc) {
			throw new ImgIOException(exc);
		}
		catch (final IOException exc) {
			throw new ImgIOException(exc);
		}
		final int compositeChannelCount = base.getRGBChannelCount();
		final int validBits = r.getBitsPerPixel();

		final ImgPlus<T> imgPlus = new ImgPlus<T>(img, name, dimTypes, cal);
		imgPlus.setValidBits(validBits);
		imgPlus.setCompositeChannelCount(compositeChannelCount);

		return imgPlus;
	}

	/**
	 * Finds the lowest level wrapped reader, preferably a {@link ChannelFiller},
	 * but otherwise the base reader. This is useful for determining whether the
	 * input data is intended to be viewed with multiple channels composited
	 * together.
	 */
	private IFormatReader unwrap(final IFormatReader r) throws FormatException,
		IOException
	{
		if (!(r instanceof ReaderWrapper)) return r;
		final ReaderWrapper rw = (ReaderWrapper) r;
		final IFormatReader channelFiller = rw.unwrap(ChannelFiller.class, null);
		if (channelFiller != null) return channelFiller;
		return rw.unwrap();
	}

	/**
	 * Reads planes from the given initialized {@link IFormatReader} into the
	 * specified {@link Img}.
	 */
	private <T extends RealType<T>> void readPlanes(final IFormatReader r,
		final T type, final ImgPlus<T> imgPlus, final boolean computeMinMax)
		throws FormatException, IOException
	{
		// TODO - create better container types; either:
		// 1) an array container type using one byte array per plane
		// 2) as #1, but with an IFormatReader reference reading planes on demand
		// 3) as PlanarRandomAccess, but with an IFormatReader reference
		// reading planes on demand

		// PlanarRandomAccess is useful for efficient access to pixels in ImageJ
		// (e.g., getPixels)
		// #1 is useful for efficient Bio-Formats import, and useful for tools
		// needing byte arrays (e.g., BufferedImage Java3D texturing by reference)
		// #2 is useful for efficient memory use for tools wanting matching
		// primitive arrays (e.g., virtual stacks in ImageJ)
		// #3 is useful for efficient memory use

		// get container
		final PlanarAccess<?> planarAccess = getPlanarAccess(imgPlus);
		final T inputType = makeType(r.getPixelType());
		final T outputType = type;
		final boolean compatibleTypes =
			outputType.getClass().isAssignableFrom(inputType.getClass());

		// populate planes
		final int planeCount = r.getImageCount();
		final boolean isPlanar = planarAccess != null && compatibleTypes;
		imgPlus.initializeColorTables(planeCount);

		byte[] plane = null;
		for (int no = 0; no < planeCount; no++) {
			notifyListeners(new StatusEvent(no, planeCount, "Reading plane " +
				(no + 1) + "/" + planeCount));
			if (plane == null) plane = r.openBytes(no);
			else r.openBytes(no, plane);
			if (isPlanar) populatePlane(r, no, plane, planarAccess);
			else populatePlane(r, no, plane, imgPlus);

			// store color table
			final byte[][] lut8 = r.get8BitLookupTable();
			if (lut8 != null) imgPlus.setColorTable(new ColorTable8(lut8), no);
			final short[][] lut16 = r.get16BitLookupTable();
			if (lut16 != null) imgPlus.setColorTable(new ColorTable16(lut16), no);
		}
		if (computeMinMax) populateMinMax(r, imgPlus);
		r.close();
	}

	/** Populates plane by reference using {@link PlanarAccess} interface. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populatePlane(final IFormatReader r, final int no,
		final byte[] plane, final PlanarAccess planarAccess)
	{
		final int pixelType = r.getPixelType();
		final int bpp = FormatTools.getBytesPerPixel(pixelType);
		final boolean fp = FormatTools.isFloatingPoint(pixelType);
		final boolean little = r.isLittleEndian();
		Object planeArray = DataTools.makeDataArray(plane, bpp, fp, little);
		if (planeArray == plane) {
			// array was returned by reference; make a copy
			final byte[] planeCopy = new byte[plane.length];
			System.arraycopy(plane, 0, planeCopy, 0, plane.length);
			planeArray = planeCopy;
		}
		planarAccess.setPlane(no, makeArray(planeArray));
	}

	/**
	 * Uses a cursor to populate the plane. This solution is general and works
	 * regardless of container, but at the expense of performance both now and
	 * later.
	 */
	private <T extends RealType<T>> void populatePlane(final IFormatReader r,
		final int no, final byte[] plane, final ImgPlus<T> img)
	{
		final int sizeX = r.getSizeX();
		final int pixelType = r.getPixelType();
		final boolean little = r.isLittleEndian();

		final long[] dimLengths = getDimLengths(r);
		final long[] pos = new long[dimLengths.length];

		final int planeX = 0;
		final int planeY = 1;

		getPosition(r, no, pos);

		final OrthoSliceCursor<T> cursor =
			new OrthoSliceCursor<T>(img, planeX, planeY, pos);

		while (cursor.hasNext()) {
			cursor.fwd();
			final int index =
				cursor.getIntPosition(planeX) + cursor.getIntPosition(planeY) * sizeX;
			final double value = decodeWord(plane, index, pixelType, little);
			cursor.get().setReal(value);
		}
	}

	private void populateMinMax(final IFormatReader r, final ImgPlus<?> imgPlus)
		throws FormatException, IOException
	{
		final int sizeC = r.getSizeC();
		final ReaderWrapper rw = (ReaderWrapper) r;
		final MinMaxCalculator minMaxCalc =
			(MinMaxCalculator) rw.unwrap(MinMaxCalculator.class, null);
		for (int c = 0; c < sizeC; c++) {
			final Double min = minMaxCalc.getChannelKnownMinimum(c);
			final Double max = minMaxCalc.getChannelKnownMaximum(c);
			imgPlus.setChannelMinimum(c, min == null ? Double.NaN : min);
			imgPlus.setChannelMaximum(c, max == null ? Double.NaN : max);
		}
	}

	/** Copies the current dimensional position into the given array. */
	private void getPosition(final IFormatReader r, final int no,
		final long[] pos)
	{
		final int sizeX = r.getSizeX();
		final int sizeY = r.getSizeY();
		final int sizeZ = r.getSizeZ();
		final int sizeT = r.getSizeT();
		final int[] cDimLengths = r.getChannelDimLengths();
		final String dimOrder = r.getDimensionOrder();

		final int[] zct = r.getZCTCoords(no);

		int index = 0;
		for (int i = 0; i < dimOrder.length(); i++) {
			final char dim = dimOrder.charAt(i);
			switch (dim) {
				case 'X':
					if (sizeX > 1) index++; // NB: Leave X axis position alone.
					break;
				case 'Y':
					if (sizeY > 1) index++; // NB: Leave Y axis position alone.
					break;
				case 'Z':
					if (sizeZ > 1) pos[index++] = zct[0];
					break;
				case 'T':
					if (sizeT > 1) pos[index++] = zct[2];
					break;
				case 'C':
					final int[] cPos = FormatTools.rasterToPosition(cDimLengths, zct[1]);
					for (int c = 0; c < cDimLengths.length; c++) {
						if (cDimLengths[c] > 1) pos[index++] = cPos[c];
					}
					break;
			}
		}
	}

	private static double decodeWord(final byte[] plane, final int index,
		final int pixelType, final boolean little)
	{
		final double value;
		switch (pixelType) {
			case FormatTools.UINT8:
				value = plane[index] & 0xff;
				break;
			case FormatTools.INT8:
				value = plane[index];
				break;
			case FormatTools.UINT16:
				value = DataTools.bytesToShort(plane, 2 * index, 2, little) & 0xffff;
				break;
			case FormatTools.INT16:
				value = DataTools.bytesToShort(plane, 2 * index, 2, little);
				break;
			case FormatTools.UINT32:
				value = DataTools.bytesToInt(plane, 4 * index, 4, little) & 0xffffffff;
				break;
			case FormatTools.INT32:
				value = DataTools.bytesToInt(plane, 4 * index, 4, little);
				break;
			case FormatTools.FLOAT:
				value = DataTools.bytesToFloat(plane, 4 * index, 4, little);
				break;
			case FormatTools.DOUBLE:
				value = DataTools.bytesToDouble(plane, 4 * index, 4, little);
				break;
			default:
				value = Double.NaN;
		}
		return value;
	}

}
