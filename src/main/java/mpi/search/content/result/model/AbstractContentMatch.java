package mpi.search.content.result.model;

/**
 * An abstract class for content matches or "hits".
 *
 * @author klasal
 */
@SuppressWarnings("serial")
public abstract class AbstractContentMatch implements ContentMatch {
    protected String fileName = "";
    protected String leftContext = "";
    protected String rightContext = "";
    //add parent and children context mod. Coralie Villes
    protected String parentContext="";
    protected String childrenContext="";
    protected String tierName = "";
    protected int[][] matchedSubstringIndices;
    protected int indexWithinTier = -1;
    protected long beginTime;
    protected long endTime;

    @Override
	public long getBeginTimeBoundary() {
        return beginTime;
    }

    @Override
	public long getEndTimeBoundary() {
        return endTime;
    }

    @Override
	public String getFileName() {
        return fileName;
    }

    /**
     * @param i the index of a match (annotation) in the tier
     */
    public void setIndex(int i) {
        indexWithinTier = i;
    }

    /** 
     * @return the index of the match in the tier
     */
    @Override
	public int getIndex() {
        return indexWithinTier;
    }

    @Override
	public String getLeftContext() {
        return leftContext;
    }

    @Override
	public int[][] getMatchedSubstringIndices() {
        return matchedSubstringIndices;
    }

    @Override
	public String getRightContext() {
        return rightContext;
    }

    @Override
	public String getTierName() {
        return tierName;
    }
}
