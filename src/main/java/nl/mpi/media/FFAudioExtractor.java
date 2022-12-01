package nl.mpi.media;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;


public class FFAudioExtractor implements AudioExtraction {
	private int formatTag = -1;
	private static boolean ffLibLoaded = false;
	private static boolean ffNativeLogLoaded = false;
	
	final static System.Logger LOG = System.getLogger("NativeLogger");
	/* the media file path or URL */
	private String mediaPath;
	/* the id (address) of the native counterpart */
	private long id;
	/* cache some fields that are unlikely to change during the lifetime of the extractor */
	private int    sampleFrequency = 0;
	private int    numberOfChannels = 0;
	private int    bitsPerSample = 0;
	private long   mediaDuration = 0;
	private double mediaDurationSec = 0.0d;
	private long   sampleBufferDuration = 0;
	private double sampleBufferDurationSec = 0.0d;
	private long   sampleBufferSize = 0;// is this a constant in all implementations?
	private double curMediaPosition;
	//private ReentrantLock samplerLock = new ReentrantLock();
	//private int failedLoadsCount = 0;
	
	// reuse the ByteBuffer
	private ByteBuffer curByteBuffer;
	private int curBufferSize = 4 * 1024 * 1024;// start with 4Mb
	private ReentrantLock bufferLock = new ReentrantLock();
	
	static {
		// ffmpeg libs
		try {
			//System.loadLibrary("avformat");
			System.loadLibrary("avutil");
			System.loadLibrary("swresample");
			System.loadLibrary("avcodec");
			System.loadLibrary("avformat");
		} catch (UnsatisfiedLinkError ule) {
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Could not load a native FFMPEG library ([dll/dylib/so]): " + ule.getMessage());
			}
		} catch (Throwable t) {
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Could not load a native FFMPEG library ([dll/dylib/so]): " + t.getMessage());
			}
		}
		try {
			System.loadLibrary("JNIUtil");
			ffNativeLogLoaded = true;
		} catch (UnsatisfiedLinkError ule) {
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Could not load native utility library (libJNIUtil.[dll/dylib/so]): " + ule.getMessage());
			}
		} catch (Throwable t) {
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Could not load native utility library (libJNIUtil.[dll/dylib/so]): " + t.getMessage());
			}
		}
		try {
			// load native AudioExtractor
			System.loadLibrary("FFAudioExtractor");
			ffLibLoaded = true;
		} catch (Throwable t) {
			//t.printStackTrace();
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Error loading native library: " + t.getMessage());
			}
		}
		// here add separate set and get methods for debug mode for FFAudioExtractor
		if (ffLibLoaded && ffNativeLogLoaded) {
			try {
				FFAudioExtractor.initLog("nl/mpi/jni/NativeLogger", "nlog");
				if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
					FFAudioExtractor.setDebugMode(true);
				}
			} catch (Throwable t) {
				if (LOG.isLoggable(System.Logger.Level.WARNING)) {
					LOG.log(System.Logger.Level.WARNING, "Error while configuring the FFAudioExtractor: " + t.getMessage());
				}
			}
		}
		
	}
	
	public FFAudioExtractor(String mediaPath) throws UnsupportedMediaException {
		super();
		if (!ffLibLoaded) {
			throw new UnsupportedMediaException("A native library was not found or could not be loaded");
		}
		this.mediaPath = mediaPath;
		
		id = initNativeFF(mediaPath);
		
		if (id > 0) {
			if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
				LOG.log(System.Logger.Level.DEBUG, "The native FFAudioExtractor was initialized successfully");
			}
		} else {
			// failure to initialize
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "The native FFAudioExtractor could not be initialized");
			}
			throw new UnsupportedMediaException("The native FFAudioExtractor could not be initialized.");
		}
	}
	
	@Override
	public int getSampleFrequency() {
		if (sampleFrequency == 0) {
			sampleFrequency = getSampleFrequencyFF(id); 
		}
		
		return sampleFrequency;
	}

	@Override
	public int getBitsPerSample() {
		if (bitsPerSample == 0) {
			bitsPerSample = getBitsPerSampleFF(id);
			
			if (bitsPerSample == 0) {
				// calculate based on other properties
			}
		}
		
		return bitsPerSample;
	}

	@Override
	public int getNumberOfChannels() {
		if (numberOfChannels == 0) {
			numberOfChannels = getNumberOfChannelsFF(id); 
		}
		
		return  numberOfChannels;
	}

	@Override
	public int getFormatTag() {
		if (formatTag < 0) {
			formatTag = this.getFormatTagFF(id);
		}
		
		return formatTag;
	}

	@Override
	public long getDuration() {
		if (mediaDuration == 0) {
			mediaDuration = getDurationMsFF(id);
		}
		
		return mediaDuration;
	}

	@Override
	public double getDurationSec() {
		if (mediaDurationSec == 0) {
			mediaDurationSec = getDurationSecFF(id);
		}
		
		return mediaDurationSec;
	}

	@Override
	public long getSampleBufferSize() {
		if (sampleBufferSize <= 1) {
			sampleBufferSize = getSampleBufferSizeFF(id);
		}
		
		return sampleBufferSize;
	}

	@Override
	public long getSampleBufferDurationMs() {
		if (sampleBufferDuration == 0) {
			sampleBufferDuration = getSampleBufferDurationMsFF(id);
		}
		
		return sampleBufferDuration;
	}

	@Override
	public double getSampleBufferDurationSec() {
		if (sampleBufferDurationSec == 0.0) {
			sampleBufferDurationSec = getSampleBufferDurationSecFF(id);
		}
		
		return sampleBufferDurationSec;
	}

	@Override
	public byte[] getSamples(double fromTime, double toTime) {
		if (id == 0) {
			return null;
		}
		if (toTime <= fromTime) {
			return null;
		}
		if (fromTime >= getDurationSec()) {
			return null;
		}
		if (toTime > getDurationSec()) {
			toTime = getDurationSec();
		}
		double timeSpan = toTime - fromTime;
		int numBytes = 0;
		double bufferDur = getSampleBufferDurationSec();
		
		if (bufferDur > 0) {
			double numBuffers = timeSpan / bufferDur;
			int nb = (int) Math.round((numBuffers * getSampleBufferSize() * getNumberOfChannels()));
			numBytes = nb + (int) getSampleBufferSize();// add some bytes extra?
		} else {
			// log error?
			numBytes = (int) (timeSpan * (getSampleFrequency() * 
					(getBitsPerSample() / 8) * getNumberOfChannels()));
			numBytes += 1024;
		}
		
		bufferLock.lock();
		try {
			//ByteBuffer byteBuffer = ByteBuffer.allocateDirect(numBytes);
			ByteBuffer byteBuffer = getByteBuffer(numBytes);
			int numRead = getSamplesFF(id, fromTime, toTime, byteBuffer);
			curMediaPosition = toTime + getSampleBufferDurationSec();// an approximation of the current read position of the source reader
			byte[] ba = new byte[numRead];
			byteBuffer.get(ba, 0, numRead);
			
			return ba;
		} finally {
			bufferLock.unlock();
		}
	}

	@Override
	public byte[] getSample(double forTime) {
		if (id == 0) {
			return null;
		}
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) getSampleBufferSize());
		
		int numBytes = getSampleFF(id, forTime, byteBuffer);
		curMediaPosition = forTime + getSampleBufferDurationSec();
		
		byte[] ba = new byte[numBytes];
		byteBuffer.get(ba, 0, numBytes);
		return ba;
	}

	@Override
	public double getPositionSec() {
		return curMediaPosition;
	}

	@Override
	public void setPositionSec(double seekPositionSec) {
		if (id == 0) {
			return;
		}
		curMediaPosition = seekPositionSec;
		setPositionSecFF(id, seekPositionSec);
	}

	@Override
	public void release() {
		if (id > 0) {
			releaseFF(id);
		}
	}
	
	/**
	 * Checks if the current buffer is {@code null} or is smaller than the
	 * requested buffer size. Creates a new buffer in both cases and 
	 * returns the current buffer in all other cases.
	 * 
	 * @param minSize the minimal size for the buffer
	 * @return the current or a new new buffer
	 */
	private ByteBuffer getByteBuffer(int minSize) {
		if (curByteBuffer == null) {
			if (minSize > curBufferSize) {
				curBufferSize = minSize;
			}
			curByteBuffer = ByteBuffer.allocateDirect(curBufferSize);
			return curByteBuffer;
		} else {
			if (minSize > curBufferSize) {
				curBufferSize = minSize;
				// the old buffer is dereferenced and can be garbage collected
				curByteBuffer = ByteBuffer.allocateDirect(curBufferSize);
			} else {
				curByteBuffer.clear();
			}
			return curByteBuffer;
		}
	}

	// global native debug setting
	public static native void setDebugMode(boolean debugMode);
	public static native boolean isDebugMode();
	/**
	 * Tells the native counterpart which class and method to use for
	 * logging messages to the Java logger.
	 * 
	 * @param clDescriptor the class descriptor, 
	 * 		e.g. {@code nl/mpi/jni/NativeLog}
	 * 	   
	 * @param methodName the name of the {@code static void} method to call, 
	 * e.g. {@code nlog}, a method which accepts one {@code String}
	 */
	public static native void initLog(String clDescriptor, String methodName);
	
	// could use the same method names as in the super class, but this can 
	// maybe reduce confusion
	private native long initNativeFF(String mediaPath);
	private native int getSampleFrequencyFF(long id);
	private native int getBitsPerSampleFF(long id);
	private native int getNumberOfChannelsFF(long id);
	private native int getFormatTagFF(long id);
	private native long getDurationMsFF(long id);
	private native double getDurationSecFF(long id);
	private native long getSampleBufferSizeFF(long id);
	private native long getSampleBufferDurationMsFF(long id);
	private native double getSampleBufferDurationSecFF(long id);
	private native int getSamplesFF(long id, double fromTime, double toTime, ByteBuffer buffer);
	private native int getSampleFF(long id, double fromTime, ByteBuffer buffer);
	private native boolean setPositionSecFF(long id, double seekTime);
	private native void releaseFF(long id);
}
