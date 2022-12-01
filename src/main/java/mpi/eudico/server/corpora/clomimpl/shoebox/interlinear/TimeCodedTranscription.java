/*
 * Created on Dec 17, 2004
 */
package mpi.eudico.server.corpora.clomimpl.shoebox.interlinear;

import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;


/**
 * This interface is a sub-interface of Transcription. It contains only the
 * methods that are used in the interlinearizer package. Interlinearizer's
 * classes will use TimeCodedTranscription to retrieve their information about
 * the annotation document. The intended implementation will delegate method
 * calls to a wrapped Transcription, but allows to override methods when
 * necessary, for example to add time code tiers without modifying the wrapped
 * document.
 *
 * @author Hennie Brugman
 * @version Aug 2005 Identity removed
 */
public interface TimeCodedTranscription {
    /** name for a time code linguistic type */
    public static final String TC_LING_TYPE = "12nov2004_temp$LING$type"; // unlikely type name

    /** a time code tier name prefix */
    public static final String TC_TIER_PREFIX = "TC-";

    /**
     * @return the encapsulated transcription
     */
    public Transcription getTranscription();

    // for delegation and override
    /**
     * @return a list of tier objects
     */
    public List<Tier> getTiers();

    /**
     * @param theAnnot the annotation to get child annotations for
     *
     * @return a list of dependent annotations of {@code theAnnot}
     */
    public List<? extends Annotation> getChildAnnotationsOf(Annotation theAnnot);

    /**
     * @param theTier the input tier 
     *
     * @return the parent tier or null
     */
    public Tier getParentTier(Tier theTier);

    /**
     * @param forTier the input tier
     *
     * @return the top level ancestor tier of {@code forTier}
     */
    public Tier getRootTier(Tier forTier);

    /**
     * @param tier1 first input tier
     * @param tier2 second input tier
     *
     * @return true if {@code tier1} is an ancestor of {@code tier2}, false otherwise
     */
    public boolean isAncestorOf(Tier tier1, Tier tier2);

    /**
     * @param tier input tier
     *
     * @return a list of tiers that are part of the tier tree
     */
    public List<TierImpl> getTierTree(TierImpl tier);

    // to manage time code tiers
    /**
     * @param timeCodeStyle the time format, one of {@link Interlinearizer}'s constants
     * @param correctAnnotationTimes whether or not annotation times need to be 
     * corrected based on a media offset
     */
    public void prepareTimeCodeRendering(int timeCodeStyle, boolean correctAnnotationTimes);

    /**
     * Removes time code tiers
     */
    public void cleanupTimeCodeTiers();

    /**
     * @return a list of time code tiers
     */
    public List<Tier> getTimeCodeTiers();
}
