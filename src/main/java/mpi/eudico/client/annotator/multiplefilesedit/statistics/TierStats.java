package mpi.eudico.client.annotator.multiplefilesedit.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder for tier statistics for multiple files.
 * 
 * @author Han Sloetjes
 */
public 	class TierStats {
	private String tierName;
	public List<Long> durations;// should every individual duration be in the list or only the unique durations?
	public int numFiles;
	public int numAnnotations;
	public long minDur;
	public long maxDur;
	public long totalDur;
	public long latency;
	
	/**
	 * Creates a new instance for the specified tier.
	 * 
	 * @param tierName the tier name
	 */
	public TierStats(String tierName) {
		super();
		this.tierName = tierName;
		durations = new ArrayList<Long>();
	}
	
	/**
	 * 
	 * @return the tier name
	 */
	public String getTierName() {
		return tierName;
	}
	
}
