package mpi.eudico.server.corpora.clom;

import mpi.eudico.server.corpora.event.ParentAnnotationListener;

/**
 * A ParentAnnotation is supposed to notify it's listening child annotations
 * after some modification.
 */
public interface ParentAnnotation {
    /**
     * 
     * @param l the listener to add
     */
    public void addParentAnnotationListener(ParentAnnotationListener l);

    /**
     *
     * @param l the listener to remove
     */
    public void removeParentAnnotationListener(ParentAnnotationListener l);

    /**
     * Notifies parent listeners.
     */
    public void notifyParentListeners();
}
