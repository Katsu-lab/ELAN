package nl.mpi.media;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class to extract audio data from a video file (or from an audio file other
 * than a WAVE file) using the platform's default native media framework
 * (on Windows that is Microsoft Media Foundation, on macOS AV Foundation).
 * 
 * @author Han Sloetjes
 */
public class AudioExtractor implements AudioExtraction {
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
	private ReentrantLock samplerLock = new ReentrantLock();
	private int failedLoadsCount = 0;
	
	private static boolean threadedSampleLoading = false;
	private static boolean nativeLibLoaded = false;
	private static boolean nativeLogLoaded = false;
	// reuse the ByteBuffer
	private ByteBuffer curByteBuffer;
	private int curBufferSize = 4 * 1024 * 1024;// start with 4Mb
	private ReentrantLock bufferLock = new ReentrantLock();
	
	static {
		try {
			System.loadLibrary("JNIUtil");
			nativeLogLoaded = true;
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
			System.loadLibrary("AudioExtractor");
			nativeLibLoaded = true;
		} catch (Throwable t) {
			//t.printStackTrace();
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "Error loading native library: " + t.getMessage());
			}
		}
		
		if (nativeLibLoaded && nativeLogLoaded) {
			try {
				AudioExtractor.initLog("nl/mpi/jni/NativeLogger", "nlog");
				if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
					AudioExtractor.setDebugMode(true);
				}
			} catch (Throwable t) {
				if (LOG.isLoggable(System.Logger.Level.WARNING)) {
					LOG.log(System.Logger.Level.WARNING, "Error while configuring the AudioExtractor: " + t.getMessage());
				}
			}
		}
		String threadLoad = System.getProperty("AudioExtractor.LoadSamplesThreaded");
		if (threadLoad != null && threadLoad.equalsIgnoreCase("true")) {
			threadedSampleLoading = true;
		}
		
	}
		
	/**
	 * Constructor, initializes the native counterpart of the extractor.
	 * 
	 * @param mediaPath the path or URL of the file
	 * 
	 * @throws UnsupportedMediaException if a media file is not supported or if the native
	 * library could not be loaded
	 */
	public AudioExtractor(String mediaPath) throws UnsupportedMediaException {
		if (!nativeLibLoaded) {
			throw new UnsupportedMediaException("The native library was not found or could not be loaded");
		}
		this.mediaPath = mediaPath;
		
		id = initNative(mediaPath);
		
		if (id > 0) {
			if (LOG.isLoggable(System.Logger.Level.DEBUG)) {
				LOG.log(System.Logger.Level.DEBUG, "The native AudioExtractor was initialized successfully");
			}
		} else {
			// failure to initialize
			if (LOG.isLoggable(System.Logger.Level.WARNING)) {
				LOG.log(System.Logger.Level.WARNING, "The native AudioExtractor could not be initialized");
			}
			throw new UnsupportedMediaException("The native AudioExtractor could not be initialized.");
		}
	}
	
	@Override
	public int getSampleFrequency() {
		if (sampleFrequency == 0) {
			sampleFrequency = getSampleFrequency(id); 
		}
		
		return sampleFrequency;
	}
	
	@Override
	public int getBitsPerSample() {
		if (bitsPerSample == 0) {
			bitsPerSample = getBitsPerSample(id);
			
			if (bitsPerSample == 0) {
				// calculate based on other properties
			}
		}
		
		return bitsPerSample;
	}
	
	@Override
	public int getNumberOfChannels() {
		if (numberOfChannels == 0) {
			numberOfChannels = getNumberOfChannels(id); 
		}
		
		return  numberOfChannels;
	}
	
	/**
	 * Returns {@code 1 "PCM (uncompressed)"}, the same as uncompressed WAVE
	 * files. 
	 *  
	 * @return {@code 1}, the native framework delivers uncompressed PCM data
	 * as integers
	 */
	@Override
	public int getFormatTag() {
		return 1;// format uncompressed PCM, samples as integers
	}
	
	@Override
	public long getDuration() {
		if (mediaDuration == 0) {
			mediaDuration = getDurationMs(id);
		}
		
		return mediaDuration; 
	}
	
	@Override
	public double getDurationSec() {
		if (mediaDurationSec == 0) {
			mediaDurationSec = getDurationSec(id);
		}
		
		return mediaDurationSec; 
	}
	
	@Override
	public long getSampleBufferSize() {
		if (sampleBufferSize <= 1) {
			sampleBufferSize = getSampleBufferSize(id);
		}
		
		return sampleBufferSize;
	}
	
	@Override
	public long getSampleBufferDurationMs() {
		if (sampleBufferDuration == 0) {
			sampleBufferDuration = getSampleBufferDurationMs(id);
		}
		
		return sampleBufferDuration;
	}

	@Override
	public double getSampleBufferDurationSec() {
		if (sampleBufferDurationSec == 0.0) {
			sampleBufferDurationSec = getSampleBufferDurationSec(id);
		}
		
		return sampleBufferDurationSec;
	}
	
	/**
	 * Initializes a direct {@code ByteBuffer} of sufficient size for the bytes
	 * of the specified time span. There is currently no built-in limit to the
	 * size of the buffer. The caller is responsible for deciding whether an 
	 * interval can be loaded in a single action or should be split into 
	 * multiple calls.
	 * In the current implementation a new {@code ByteBuffer} is created for
	 * every call, making this method thread safe, but this might change to
	 * re-using an existing buffer if it is large enough or the interval.
	 *   
	 * @param fromTime the time of the first sample in seconds
	 * @param toTime the time of the last sample in seconds
	 * @return an array containing the decoded samples for the specified interval
	 */
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
			int nb = (int) (numBuffers * getSampleBufferSize());
			numBytes = nb + (int) getSampleBufferSize();// add some bytes extra?
		} else {
			// log error?
			numBytes = (int) (timeSpan * (getSampleFrequency() * 
					(getBitsPerSample() / 8) * getNumberOfChannels()));
			numBytes += 1024;
		}
		
		if (!threadedSampleLoading) {
			bufferLock.lock();
			try {
				//ByteBuffer byteBuffer = ByteBuffer.allocateDirect(numBytes);
				ByteBuffer byteBuffer = getByteBuffer(numBytes);
				int numRead = getSamples(id, fromTime, toTime, byteBuffer);
				curMediaPosition = toTime + getSampleBufferDurationSec();// an approximation of the current read position of the source reader
				byte[] ba = new byte[numRead];
				byteBuffer.get(ba, 0, numRead);
				
				return ba;
			} finally {
				bufferLock.unlock();
			}
		}
		
		try {
			if (samplerLock.tryLock(20, TimeUnit.MILLISECONDS)) {
				try {
					ByteBuffer byteBuffer = ByteBuffer.allocateDirect(numBytes);
					//int numRead = getSamples(id, fromTime, toTime, byteBuffer);
					LoadRunner lr = new LoadRunner(fromTime, toTime, byteBuffer);
					Thread loadThr = new Thread(lr);
					try {		
						loadThr.start();
						loadThr.join(100);
					} catch (InterruptedException ie) {
						
					}
					int numRead = lr.numRead;
					
					if (numRead == 0) {
						//error, maybe deadlock in native code
						if (loadThr.isAlive()) {
							loadThr.interrupt();
						}
						
						failedLoadsCount++;
						if (failedLoadsCount > 5) {
							id = 0;// stop trying
						}
						
						return null;
					}
					curMediaPosition = toTime + getSampleBufferDurationSec();// an approximation of the current read position of the source reader
					byte[] ba = new byte[numRead];
					byteBuffer.get(ba, 0, numRead);
					
					return ba; 
				} finally {
					samplerLock.unlock();
				}
			}
		} catch (InterruptedException ie) {
			// ignore
		}
		return null;
	}
	
	/**
	 * A Runnable to load samples on a separate thread (so that a native thread
	 * that never returns (e.g. in AVFoundation) does not freeze the AWT event
	 * thread).
	 */
	private class LoadRunner implements Runnable {
		int numRead = 0;
		double from;
		double to;
		ByteBuffer bb;
		
		LoadRunner(double fromTime, double toTime, ByteBuffer byteBuffer) {
			from = fromTime;
			to = toTime;
			bb = byteBuffer;
		}
		
		@Override
		public void run() {
			if (id != 0) {
				if (to > from) {
					numRead = getSamples(id, from, to, bb);
				} else if (to == from){
					numRead = getSample(id, from, bb);
				}
			}
		}
	}
	
	/**
	 * Returns the sample for the specified time plus the following samples that
	 * fit in the default native buffer.
	 *   
	 * @param forTime the time to get the sample for 
	 * @return an array of bytes, starting with the sample at the requested time
	 */
	@Override
	public byte[] getSample(double forTime) {
		if (id == 0) {
			return null;
		}
		if (!threadedSampleLoading) {
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) getSampleBufferSize());
			
			int numBytes = getSample(id, forTime, byteBuffer);
			curMediaPosition = forTime + getSampleBufferDurationSec();
			
			byte[] ba = new byte[numBytes];
			byteBuffer.get(ba, 0, numBytes);
			return ba;
		} else {
			try {
				if (samplerLock.tryLock(20, TimeUnit.MILLISECONDS)) {
					try {
						ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) getSampleBufferSize());
						//int numRead = getSamples(id, fromTime, toTime, byteBuffer);
						LoadRunner lr = new LoadRunner(forTime, forTime, byteBuffer);
						Thread loadThr = new Thread(lr);
						try {		
							loadThr.start();
							loadThr.join(100);
						} catch (InterruptedException ie) {
							
						}
						int numRead = lr.numRead;
						
						if (numRead == 0) {
							//error, maybe deadlock in native code
							if (loadThr.isAlive()) {
								loadThr.interrupt();
							}
							
							failedLoadsCount++;
							if (failedLoadsCount > 5) {
								id = 0;// stop trying
							}
							
							return null;
						}
						curMediaPosition = forTime + getSampleBufferDurationSec();// an approximation of the current read position of the source reader
						byte[] ba = new byte[numRead];
						byteBuffer.get(ba, 0, numRead);
						
						return ba; 
					} finally {
						samplerLock.unlock();
					}
				}
			} catch (InterruptedException ie) {
				// ignore
			}
			return null;
		}
	}
	
	@Override
	public double getPositionSec() {
		return curMediaPosition;
	}
	
	/**
	 * Sets the position of the audio source reader, in seconds.
	 * This value is stored locally in case the native framework does not 
	 * support to get the current position.
	 * 
	 * @param seekPositionSec the seek position for the reader
	 */
	@Override
	public void setPositionSec(double seekPositionSec) {
		if (id == 0) {
			return;
		}
		curMediaPosition = seekPositionSec;
		setPositionSec(id, seekPositionSec);
//		if (setPositionSec(id, seekPositionSec)) {
//			curMediaPosition = seekPositionSec;
//		}
	}
	
	@Override
	public void release() {
		if (id > 0) {
			release(id);
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
	static native void initLog(String clDescriptor, String methodName);
	
	private native long initNative(String mediaPath);
	private native int getSampleFrequency(long id);
	private native int getBitsPerSample(long id);
	private native int getNumberOfChannels(long id);
	private native long getDurationMs(long id);
	private native double getDurationSec(long id);
	private native long getSampleBufferSize(long id);
	private native long getSampleBufferDurationMs(long id);
	private native double getSampleBufferDurationSec(long id);
	private native int getSamples(long id, double fromTime, double toTime, ByteBuffer buffer);
	private native int getSample(long id, double fromTime, ByteBuffer buffer);
	private native boolean setPositionSec(long id, double seekTime);
	private native void release(long id);
	
	
	// test main
	public static void main(String[] args) {
		if (args.length >= 1) {
			try {
				AudioExtractor.setDebugMode(true);
				AudioExtraction ae = new AudioExtractor(args[0]);
				
				System.out.println(ae.getSampleFrequency());
				System.out.println(ae.getBitsPerSample());
				System.out.println(ae.getDuration());
				System.out.println(ae.getNumberOfChannels());
				System.out.println("Sample: " + ae.getSample(2.33d).length);
				System.out.println("Sample 2: " + ae.getSample(52.119d).length);
				System.out.println("Time span: " + ae.getSamples(4.52,  6.899).length);
				System.out.println("Time span: " + ae.getSamples(2.12, 4.90).length);
			} catch (UnsupportedMediaException ume) {
				System.out.println(ume.getMessage());
			}
		}
	}
}
