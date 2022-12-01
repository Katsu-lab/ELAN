package mpi.eudico.server.corpora.clomimpl.html;

import java.util.List;

import mpi.eudico.server.corpora.clom.EncoderInfo;
import mpi.eudico.util.TimeFormatter;

/**
 * A class to hold properties and settings for export to time aligned
 * interlinear text (e.g. in html).
 */
public class TAIEncoderInfo implements EncoderInfo {
	/** a list of tiers to export and the style information for each tier,
	 * should not be null or empty */
	private List<TAITierSetting> tierSettings;
    /**
     * 0 or the begin value of the selection
     */
    private long beginTime = 0;

    /**
     * The end of the selection or the duration of the media file
     */
    private long endTime = Long.MAX_VALUE;
    /** width for one time unit, the number of milliseconds represented by one character position */
    private int timeUnit;
    /** maximal space for one block of annotations, in number of characters */
    private int blockSpace;
    /** space for tier names, number of characters */
    private int leftMargin;
    /** font size in HTML */
    private int fontSize;
    /** alignment of the annotation value within the area of its time span, left or right */
    private int textAlignment;// left or right
    /** wrap lines within one block (if reference tier is used) */
    private boolean wrapWithinBlock;
    /** whether or not a line should be added for time codes and a time line */
    private boolean showTimeLine;
    /** if time values are to be printed, use this format */
    private TimeFormatter.TIME_FORMAT timeFormat;
    /** if annotations are not underlined, mark boundaries of annotations with special characters */
    private boolean showAnnotationBoundaries; // for non-underlined annotations
    /** the name of the reference tier, in case of output based on a reference tier, null otherwise */
    private String refTierName;
    
    /**
     * Constructor.
     */
	public TAIEncoderInfo() {
		super();
	}

	public void setTierSettings(List<TAITierSetting> tierSettings) {
		this.tierSettings = tierSettings;
	}
	
	public List<TAITierSetting> getTierSettings() {
		return tierSettings;
	}
	
    public String getRefTierName() {
		return refTierName;
	}

	public void setRefTierName(String refTierName) {
		this.refTierName = refTierName;
	}

	/**
     * Returns the (selection) begin time.
     *
     * @return Returns the begin time.
     */
    public long getBeginTime() {
        return beginTime;
    }

    /**
     * Sets the (selection) begin time.
     *
     * @param beginTime The begin time to set.
     */
    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    /**
     * Returns the (selection) end time.
     *
     * @return Returns the end time.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Sets the (selection) end time.
     *
     * @param endTime The end time to set.
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

	public int getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(int timeUnit) {
		this.timeUnit = timeUnit;
	}
	
	public TimeFormatter.TIME_FORMAT getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(TimeFormatter.TIME_FORMAT timeFormat) {
		this.timeFormat = timeFormat;
	}

	public int getBlockSpace() {
		return blockSpace;
	}

	public void setBlockSpace(int blockSpace) {
		this.blockSpace = blockSpace;
	}

	public int getLeftMargin() {
		return leftMargin;
	}

	public void setLeftMargin(int leftMargin) {
		this.leftMargin = leftMargin;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public boolean isWrapWithinBlock() {
		return wrapWithinBlock;
	}

	public void setWrapWithinBlock(boolean wrapWithinBlock) {
		this.wrapWithinBlock = wrapWithinBlock;
	}

	public int getTextAlignment() {
		return textAlignment;
	}

	public void setTextAlignment(int textAlignment) {
		this.textAlignment = textAlignment;
	}

	public boolean isShowTimeLine() {
		return showTimeLine;
	}

	public void setShowTimeLine(boolean showTimeLine) {
		this.showTimeLine = showTimeLine;
	}

	public boolean isShowAnnotationBoundaries() {
		return showAnnotationBoundaries;
	}

	public void setShowAnnotationBoundaries(boolean showAnnotationBoundaries) {
		this.showAnnotationBoundaries = showAnnotationBoundaries;
	}
    
}
