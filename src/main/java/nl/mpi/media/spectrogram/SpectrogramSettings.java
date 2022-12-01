package nl.mpi.media.spectrogram;

import java.awt.Color;
import java.awt.image.DataBuffer;

/**
 * A collection of settings related to creation and visualization of audio
 * spectrograms.
 * <p>
 * In the current implementation a reference to an instance of this class is
 * shared between several classes that need read and/or write access to these
 * settings in order to determine if and how audio data need to be processed
 * and how the results should be visualized. A listener or observer mechanism
 * might be more appropriate.
 *  
 * @author Han Sloetjes
 */
public class SpectrogramSettings {
	/**
	 * The constants of this enumerated type describe various amplitude or 
	 * power units as the output of a Fourier transform and input to the 
	 * image generator.
	 */
	public enum AMPL_UNIT {
		AMPLITUDE,
		POWER,
		ROOT_POWER
	}
	
	/**
	 * The constants of this enumerated type specify the color schemes that
	 * can be set for the image generator.
	 */
	public enum COLOR_SCHEME {
		GRAY,
		REVERSED_GRAY,
		BI_COLOR
	}
	
	/**
	 * Constants for a performance trade off during image creation, 
	 * speed versus quality. 
	 */
	public enum PERFORMANCE {
		SPEED,
		QUALITY
	}
	
	/**
	 * Enumerated type for specifying the source for the spectrogram data, 
	 * the first or second channel, or all (i.e. both, more than two channels
	 * are not supported at the moment).  
	 */
	public enum FREQ_CHANNEL {
		CHANNEL_1,
		CHANNEL_2,
		CHANNEL_ALL
	}
	
	/**
	 * The view range, the minimum and maximum frequency to show.
	 */
	private double minDisplayFrequency;
	private double maxDisplayFrequency;
	/**
	 * The possible maximum frequency after a transform, as determined by the
	 * sample frequency of the audio.
	 */
	private double possibleMaxFrequency;
	/**
	 * The encoded sample frequency of the audio.
	 */
	private double sampleFrequency;
	/** The calculated number of samples per window and stride */
    private int numSamplesPerWindow;
    private int numSamplesPerStride;
	
	/** The duration per frequency window as set by the user. */
	private double windowDurationSec;
	/** 
	 * The duration per window as calculated after finding the closest power of
	 * two of the number of samples corresponding to the set window duration.
	 */
	private double actualWindowDurationSec;
	/** The duration per window stride as set by the user. */
	private double strideDurationSec;
	/**
	 * The duration per stride as calculated based on the actual window duration
	 * and the ratio between the set window and stride duration.
	 */
	private double actualStrideDurationSec;
	/** The duration per pixel, depending on the zoom level of the viewer. */
	private double pixelDurationSec;
	/** In bi-color mode the foreground and background color. */
	private Color color1;
	private Color color2;
	
	private AMPL_UNIT amplUnit;
	private FREQ_CHANNEL channelMode;
	private COLOR_SCHEME colorScheme;
	private int dataBufferType;// int or double
	/** Whether or not the contrast is adapted based on actual samples in the 
	 * current interval */
	private boolean adaptiveContrast;
	/** the minimum value resulting from the FFT's machine epsilon, if applied */
	private double adaptiveMinimum;
	/** Any value above this value will be decreased to this maximum, these 
	 * maximum and minimum values determine the total extent and thus the 
	 * conversion to pixel colors */
	private double upperValueLimit;
	private double lowerValueLimit;
	/** A modification of the upper and lower value limit, as a percentage of
	 * the default range, used to correct contrast / brightness */
	private double upperValueAdjustment;
	private double lowerValueAdjustment;
	private boolean normalizedInputData;
	/** The current window function to apply to the samples */
	private String windowFunction;
	
	/**
	 * Some flags to indicate that something changed (e.g. settings) and new
	 * data or new processing is required.
	 */
	private volatile boolean newDataRequired;
	private volatile boolean newWindowDataRequired;
	// a flag to indicate whether a new Fourier transform is required after changes in settings
	private volatile boolean newTransformRequired;
	private volatile boolean newImageRequired;
	
	/**
	 * Constructor, creates a new instance with default settings.
	 */
	public SpectrogramSettings() {
		setDefaults();
	}
	
	/**
	 * Initializes fields to default values.
	 */
	private void setDefaults() {
		// initialize default settings
		sampleFrequency = 48000d;
		possibleMaxFrequency = 24000d;//48k Hz / 2
		minDisplayFrequency = 0d;
		maxDisplayFrequency = 5000d;
		windowDurationSec = 0.02d;
		strideDurationSec = 0.01d;
		pixelDurationSec = 0.01d;
		dataBufferType = DataBuffer.TYPE_INT;
		colorScheme = COLOR_SCHEME.GRAY;
		adaptiveContrast = false;
		upperValueLimit = 120d;
		lowerValueLimit = 0d;
		amplUnit = AMPL_UNIT.ROOT_POWER;
		channelMode = FREQ_CHANNEL.CHANNEL_1;
		windowFunction = "Hann";
		normalizedInputData = true;
		recalculateWindowAndStride();
		newTransformRequired = true;
	}
	
	/**
	 * Restores settings to their default values.
	 */
	public void restoreDefaultSettings() {
		// restore default settings that can/could be set with a global scope
		//sampleFrequency = 48000d;// depends on actual, loaded data
		//possibleMaxFrequency = 24000d;//48k Hz / 2
		minDisplayFrequency = 0d;
		maxDisplayFrequency = 5000d;
		windowDurationSec = 0.02d;
		strideDurationSec = 0.01d;
		//pixelDurationSec = 0.01d;//depends on the viewer's setting
		//dataBufferType = DataBuffer.TYPE_INT; 
		colorScheme = COLOR_SCHEME.GRAY;
		adaptiveContrast = false;
		upperValueLimit = 120d;
		lowerValueLimit = 0d;
		//amplUnit = AMPL_UNIT.ROOT_POWER;// internal decision which type is used
		//channelMode = FREQ_CHANNEL.CHANNEL_1;// now a strictly local choice
		windowFunction = "Hann";
		//normalizedInputData = true;// internal decision if data is normalized
		recalculateWindowAndStride();
		newTransformRequired = true;
		//newImageRequired = true;// implied by previous statement
	}
	
	/**
	 * Calculates the number of samples per window (the nearest power of 2) and
	 * per stride and then the actual duration per window, all based on the window
	 * and stride duration set by the user.
	 */
	private void recalculateWindowAndStride() {
		int samplesPerWinTemp = (int) (sampleFrequency * windowDurationSec);
		int power2 = 2;
		while (power2 < samplesPerWinTemp) {	
			power2 <<= 1;//power2 *= 2;
		}

		// check which is closer, this pow2 or the previous
		if (power2 - samplesPerWinTemp < samplesPerWinTemp - (power2 >> 1)) {
			numSamplesPerWindow = power2;
		} else {
			numSamplesPerWindow = power2 >> 1; //power2 / 2
		}
		
		numSamplesPerStride = (int) (numSamplesPerWindow * (strideDurationSec / 
				windowDurationSec));
		actualWindowDurationSec = numSamplesPerWindow / sampleFrequency;
		actualStrideDurationSec = numSamplesPerStride / sampleFrequency;
		newWindowDataRequired = true;
		newTransformRequired = true;
	}

	public double getMinDisplayFrequency() {
		return minDisplayFrequency;
	}

	/**
	 * Sets the minimum of the frequency view range, the minimum of the 
	 * displayed frequencies, 0 Hz by default.
	 * 
	 * @param minDisplayFrequency the lower limit of displayed values
	 */
	public void setMinDisplayFrequency(double minDisplayFrequency) {
		if (this.minDisplayFrequency != minDisplayFrequency) {
			this.minDisplayFrequency = minDisplayFrequency;
			newImageRequired = true;
		}
		// currently this does not require a new transform because all frequency bins
		// are returned by the transform, regardless of display settings
		// this needs to change if the transform process is changed to only return
		// the requested, displayed frequencies
	}

	public double getMaxDisplayFrequency() {
		return maxDisplayFrequency;
	}

	/**
	 * Sets the maximum of the frequency view range, the maximum of the 
	 * displayed frequencies, 5000 Hz by default.
	 * 
	 * @param maxDisplayFrequency the upper limit of displayed values
	 */
	public void setMaxDisplayFrequency(double maxDisplayFrequency) {
		if (this.maxDisplayFrequency != maxDisplayFrequency) {
			this.maxDisplayFrequency = maxDisplayFrequency;
			newImageRequired = true;
		}
	}
	
	/**
	 * Sets the view range, both minimum and maximum.
	 * 
	 * @param minDisplayFrequency the lower limit of displayed values
	 * @param maxDisplayFrequency the upper limit of displayed values
	 */
	public void setMinMaxDisplayFrequency(double minDisplayFrequency, double maxDisplayFrequency) {
		if (this.minDisplayFrequency != minDisplayFrequency || this.maxDisplayFrequency != maxDisplayFrequency) {
			if (minDisplayFrequency < 0) {
				this.minDisplayFrequency = 0d;
			} 
			if (minDisplayFrequency >= maxDisplayFrequency) {
				// adjust one
				if ((maxDisplayFrequency > 1000 && maxDisplayFrequency < getPossibleMaxFrequency())) {
					this.maxDisplayFrequency = maxDisplayFrequency;
					this.minDisplayFrequency = 0d; // or max - 1000?
				} else if (minDisplayFrequency < getPossibleMaxFrequency() / 2){// arbitrary
					this.minDisplayFrequency = minDisplayFrequency;
					this.maxDisplayFrequency = getPossibleMaxFrequency();
				} else {
					this.minDisplayFrequency = 0d;
					this.maxDisplayFrequency = getPossibleMaxFrequency();
				}
			} else {
				this.minDisplayFrequency = minDisplayFrequency;
				this.maxDisplayFrequency = maxDisplayFrequency;
			}
			newImageRequired = true;
		}
	}

	/**
	 * 
	 * @return the possible maximum frequency (in Hz), the sample frequency / 2
	 */
	public double getPossibleMaxFrequency() {
		return possibleMaxFrequency;
	}


	public void setPossibleMaxFrequency(double possibleMaxFrequency) {
		this.possibleMaxFrequency = possibleMaxFrequency;
	}

	/**
	 * Returns the sample frequency of the audio source file or stream.
	 *  
	 * @return the sample frequency of the audio (in Hz)
	 */
	public double getSampleFrequency() {
		return sampleFrequency;
	}

	/**
	 * Provides the settings and its users with sample frequency information
	 * extracted from the source file.
	 *  
	 * @param sampleFrequency the encoded sample frequency
	 */
	public void setSampleFrequency(double sampleFrequency) {
		if (this.sampleFrequency != sampleFrequency) {
			this.sampleFrequency = sampleFrequency;
			possibleMaxFrequency = sampleFrequency / 2d;
			recalculateWindowAndStride();
		}
	}

	public int getNumSamplesPerWindow() {
		return numSamplesPerWindow;
	}

	public int getNumSamplesPerStride() {
		return numSamplesPerStride;
	}

	public double getWindowDurationSec() {
		return windowDurationSec;
	}

	/**
	 * Sets the new window duration provided by the user.
	 * 
	 * @param windowDurationSec the new duration in seconds
	 */
	public void setWindowDurationSec(double windowDurationSec) {
		if (this.windowDurationSec != windowDurationSec) {
			this.windowDurationSec = windowDurationSec;
			recalculateWindowAndStride();
		}
	}

	/**
	 * Returns the actual, calculated duration of a window in seconds.
	 * @return the calculated window duration
	 */
	public double getActualWindowDurationSec() {
		return actualWindowDurationSec;
	}

	public double getStrideDurationSec() {
		return strideDurationSec;
	}

	/**
	 * Sets the user provided duration of the stride (in seconds), the step
	 * size for sliding windows.
	 * 
	 * @param strideDurationSec the new stride duration
	 */
	public void setStrideDurationSec(double strideDurationSec) {
		if (this.strideDurationSec != strideDurationSec) {
			this.strideDurationSec = strideDurationSec;
			recalculateWindowAndStride();
		}
	}

	/**
	 * Returns the actual calculated duration in seconds of a stride.
	 * @return the calculated stride duration
	 */
	public double getActualStrideDurationSec() {
		return actualStrideDurationSec;
	}
	
	/**
	 * Sets both the window and the stride duration in seconds.
	 * 
	 * @param windowDurationSec the new window duration
	 * @param strideDurationSec the new stride duration
	 */
	public void setWindowAndStride(double windowDurationSec, double strideDurationSec) {
		if (this.windowDurationSec != windowDurationSec || this.strideDurationSec != strideDurationSec) {
			if (windowDurationSec < 0.001) {
				this.windowDurationSec = 0.001;
			} else {
				this.windowDurationSec = windowDurationSec;
			}
			if (strideDurationSec <= 0) {
				this.strideDurationSec = this.windowDurationSec / 2;
			} else if (strideDurationSec > this.windowDurationSec) {
				this.strideDurationSec = this.windowDurationSec; // no overlap
			} else {
				this.strideDurationSec = strideDurationSec;
			}
			recalculateWindowAndStride();
		}
	}

	public double getPixelDurationSec() {
		return pixelDurationSec;
	}

	/**
	 * Sets the current duration of a pixel, based on the zoom level of the 
	 * viewer.
	 * 
	 * @param pixelDurationSec the duration of a pixel in seconds
	 */
	public void setPixelDurationSec(double pixelDurationSec) {
		if (this.pixelDurationSec != pixelDurationSec) {
			this.pixelDurationSec = pixelDurationSec;
			newDataRequired = true;
		}
	}

	public Color getColor1() {
		return color1;
	}

	/**
	 * Sets the foreground color (black in grayscale mode).
	 * 
	 * @param color1 the new foreground color
	 */
	public void setColor1(Color color1) {
		this.color1 = color1;
		newImageRequired = true;
	}

	public Color getColor2() {
		return color2;
	}

	/**
	 * Sets the background color (white in grayscale mode).
	 * 
	 * @param color2 the new background color
	 */
	public void setColor2(Color color2) {
		// could  check if this really involves a change
		this.color2 = color2;
		newImageRequired = true;
	}

	public AMPL_UNIT getAmplUnit() {
		return amplUnit;
	}

	/**
	 * Sets the unit of amplitude/power the transform process should produce.
	 *  
	 * @param amplUnit the new unit
	 */
	public void setAmplUnit(AMPL_UNIT amplUnit) {
		if (this.amplUnit != amplUnit) {
			this.amplUnit = amplUnit;
			newTransformRequired = true;
		}
	}

	public COLOR_SCHEME getColorScheme() {
		return colorScheme;
	}

	/**
	 * Set the color scheme to apply when creating the spectrogram image.
	 * 
	 * @param colorScheme the new color scheme
	 */
	public void setColorScheme(COLOR_SCHEME colorScheme) {
		if (this.colorScheme != colorScheme) {
			this.colorScheme = colorScheme;
			newImageRequired = true;
		}
	}

	public FREQ_CHANNEL getChannelMode() {
		return channelMode;
	}

	/**
	 * Sets which audio channel(s) should provide the data for the spectrogram.
	 * 
	 * @param channelMode the new channel mode
	 */
	public void setChannelMode(FREQ_CHANNEL channelMode) {
		if (this.channelMode != channelMode) {
			this.channelMode = channelMode;
			newDataRequired = true;
		}
	}

	public int getDataBufferType() {
		return dataBufferType;
	}

	/**
	 * Sets the data buffer type, {@link DataBuffer#TYPE_INT} by default.
	 * 
	 * @param dataBufferType the data buffer type to use
	 */
	public void setDataBufferType(int dataBufferType) {
		if (dataBufferType == DataBuffer.TYPE_DOUBLE || 
				dataBufferType == DataBuffer.TYPE_INT) {
			this.dataBufferType = dataBufferType;
			newImageRequired = true;
		}
	}

	public boolean isAdaptiveContrast() {
		return adaptiveContrast;
	}

	/**
	 * Sets whether the brightness/darkness of the spectrogram image adapts to
	 * the actual frequencies in the visible interval. {@code false} by default.  
	 * 
	 * @param adaptiveContrast if {@code true} the density is based on the 
	 * samples in current interval, otherwise the density is based on a fixed
	 * value range (determined by the frequency units)
	 */
	public void setAdaptiveContrast(boolean adaptiveContrast) {
		if (this.adaptiveContrast != adaptiveContrast) {
			this.adaptiveContrast = adaptiveContrast;
			newImageRequired = true;
		}
	}

	public double getAdaptiveMinimum() {
		return adaptiveMinimum;
	}

	/**
	 * Sets the possible minimum value depending on the amplitude units 
	 * produced by the transform.
	 * 
	 * @param adaptiveMinimum the new minimum value of the amplitude range
	 */
	public void setAdaptiveMinimum(double adaptiveMinimum) {
		if (this.adaptiveMinimum != adaptiveMinimum) {
			this.adaptiveMinimum = adaptiveMinimum;
			newImageRequired = true;
		}
	}

	public double getUpperValueLimit() {
		return upperValueLimit;
	}

	/**
	 * Sets the upper limit of the amplitude range depending on the
	 * selected units for amplitude/power.
	 * 
	 * @param upperValueLimit the new upper limit
	 */
	public void setUpperValueLimit(double upperValueLimit) {
		if (this.upperValueLimit != upperValueLimit) {
			this.upperValueLimit = upperValueLimit;
			newImageRequired = true;
		}
	}

	public double getLowerValueLimit() {
		return lowerValueLimit;
	}

	/**
	 * Sets the lower limit of the amplitude range depending on the
	 * selected units for amplitude/power.
	 * 
	 * @param lowerValueLimit the new lower limit
	 */
	public void setLowerValueLimit(double lowerValueLimit) {
		if (this.lowerValueLimit != lowerValueLimit) {
			this.lowerValueLimit = lowerValueLimit;
			newImageRequired = true;
		}
	}

	public double getUpperValueAdjustment() {
		return upperValueAdjustment;
	}

	public double getLowerValueAdjustment() {
		return lowerValueAdjustment;
	}

	/**
	 * Sets the adjustment or correction of the upper value of the amplitude
	 * range, as a percentage of the range. With the intention to the modify
	 * the density of the foreground color. Can be positive or negative. 
	 * 
	 * @param upperValueAdjustment the adjustment of the upper value limit
	 */
	public void setUpperValueAdjustment(double upperValueAdjustment) {
		if (this.upperValueAdjustment != upperValueAdjustment) {
			this.upperValueAdjustment = upperValueAdjustment;
			newImageRequired = true;
		}
	}

	/**
	 * Sets the adjustment or correction of the lower value of the amplitude
	 * range, as a percentage of the range. With the intention to the modify
	 * the density of the background color. Can be positive or negative. 
	 * 
	 * @param lowerValueAdjustment the adjustment of the lower value limit
	 */
	public void setLowerValueAdjustment(double lowerValueAdjustment) {
		if (this.lowerValueAdjustment != lowerValueAdjustment) {
			this.lowerValueAdjustment = lowerValueAdjustment;
			newImageRequired = true;
		}
	}

	public boolean isNormalizedInputData() {
		return normalizedInputData;
	}

	/**
	 * Sets whether the audio samples are normalized before applying the 
	 * transform, {@code true} by default.
	 * 
	 * @param normalizedInputData if {@code true} the audio samples are 
	 * normalized (divided by the possible maximum amplitude) before
	 * the Fourier transform
	 */
	public void setNormalizedInputData(boolean normalizedInputData) {
		this.normalizedInputData = normalizedInputData;
	}

	public String getWindowFunction() {
		return windowFunction;
	}

	/**
	 * Sets the window function to apply to the data of a window.
	 * 
	 * @param windowFunction a string representation of the window function
	 * to use
	 * @see WindowFunction
	 */
	public void setWindowFunction(String windowFunction) {
		if (this.windowFunction != null && !this.windowFunction.equals(windowFunction)) {
			this.windowFunction = windowFunction;
			newWindowDataRequired = true;
			newTransformRequired = true;// implied
		}
	}

	/**
	 * A flag that indicates that new audio samples have to be read and
	 * processed, e.g. after a change in visible interval.
	 * 
	 * @return {@code true} if new data have to be read, {@code false} 
	 * otherwise. Implies a new transform and new image.
	 */
	public boolean isNewDataRequired() {
		return newDataRequired;
	}
	
	/**
	 * A flag that indicates that new window function data have to be generated
	 * e.g. after a change in window function or window size.
	 * 
	 * @return {@code true} if new window function data have to be generated, 
	 * {@code false} otherwise. Implies a new transform and new image.
	 */
	public boolean isNewWindowDataRequired() {
		return newWindowDataRequired;
	}

	/**
	 * A flag that indicates that a new Fourier transform is required
	 * e.g. after loading new data or a change in window function or size.
	 * 
	 * @return {@code true} if a new transform of data is required, 
	 * {@code false} otherwise. Implies a new image.
	 */
	public boolean isNewTransformRequired() {
		return newTransformRequired;
	}
	
	/**
	 * A flag that indicates that a new image needs to be created, e.g. when
	 * new frequency data have been generated or after a change in view 
	 * settings.
	 * 
	 * @return {@code true} if a new image needs to be generated, 
	 * {@code false} otherwise
	 */
	public boolean isNewImageRequired() {
		return newImageRequired;
	}
	
	/**
	 * Sets all flags to {@code false} after performing required processing
	 * steps (loading new data, new transform etc.). 
	 */
	public void resetFlags() {
		newDataRequired = false;
		newWindowDataRequired = false;
		newTransformRequired = false;
		newImageRequired = false;
	}

}
