package mpi.eudico.server.corpora.clomimpl.json;

import java.util.List;

import mpi.eudico.server.corpora.clom.EncoderInfo;

/**
 * Encoder info class for export to (WebAnnotation) JSON text. 
 */
public class JSONWAEncoderInfo implements EncoderInfo {
	/** a list of names of tiers to export */
	private List<String> selectedTiers;
	
    /** 0 or the begin value of the selection */
    private long beginTime = 0;
    /** The end of the selection or the duration of the media file */
    private long endTime = Long.MAX_VALUE;
    /** generate incremental id's  or use ELAN annotation id's */
    private boolean incrementalIDs;
    /** e.g. text/plain or text/html */
    private String bodyFormat;
    /** determines how time intervals should be formatted, using a FragmentSelector type or not */
    private boolean fragmentSelector;
    /** the name of the generator of the output */
    private String generator;
    /** an optional string to add as the purpose of annotations */
    private String purpose;
    /** sets the indentation level of the resulting json */
    private int indentationLevel;
    /** create a target for the first media file only or for all media files */
    private boolean singleTargetExport;
    
    /**
     * Constructor
     */
	public JSONWAEncoderInfo() {
		super();
	}

	public List<String> getSelectedTiers() {
		return selectedTiers;
	}

	public void setSelectedTiers(List<String> selectedTiers) {
		this.selectedTiers = selectedTiers;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean isIncrementalIDs() {
		return incrementalIDs;
	}

	public void setIncrementalIDs(boolean incrementalIDs) {
		this.incrementalIDs = incrementalIDs;
	}

	public String getBodyFormat() {
		return bodyFormat;
	}

	public void setBodyFormat(String bodyFormat) {
		this.bodyFormat = bodyFormat;
	}

	public boolean isFragmentSelector() {
		return fragmentSelector;
	}

	public void setFragmentSelector(boolean fragmentSelector) {
		this.fragmentSelector = fragmentSelector;
	}

	public String getGenerator() {
		return generator;
	}

	public void setGenerator(String generator) {
		this.generator = generator;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public int getIndentationLevel() {
		return indentationLevel;
	}

	public void setIndentationLevel(int indentationLevel) {
		this.indentationLevel = indentationLevel;
	}

	public boolean isSingleTargetExport() {
		return singleTargetExport;
	}

	public void setSingleTargetExport(boolean singleTargetExport) {
		this.singleTargetExport = singleTargetExport;
	}
    
}
