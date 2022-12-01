package mpi.eudico.client.annotator.gui;

/**
 * Interface that defines the Layout behaviour of elements in the ELAN
 * universe. To be implemented by all viewers and other components that are to
 * be managed by the LayoutManager
 */
public interface Layoutable {
    /**
     * A component can indicate it wants to occupy as much space as available.
     *
     * @return {@code true} if the component wants all available space
     */
    public boolean wantsAllAvailableSpace(); // uses all free space in horizontal direction

    /**
     * A component can indicate it is optional and can be hidden, if needed.
     *
     * @return {@code true} if the component is optional
     */
    public boolean isOptional(); // can be shown/hidden. If hidden, dimensions are (0,0), position in layout is kept

    /**
     * A component can indicate it can be detached from the main window. 
     *
     * @return {@code true} if the component may be detached
     */
    public boolean isDetachable(); // can be detached, re-attached from main document window

    /**
     * Returns whether the width of the component is fixed or flexible.
     *
     * @return {@code true} if the width may be modified by the manager
     */
    public boolean isWidthChangeable();

    /**
     * Returns whether the height of the component is fixed or flexible.
     *
     * @return {@code true} if the height may be modified by the manager
     */
    public boolean isHeightChangeable();

    /**
     * Returns the least required width of the component.
     * 
     * @return the minimal width for the component
     */
    public int getMinimalWidth();

    /**
     * Returns the least required height of the component.
     * 
     * @return the minimal height for the component
     */
    public int getMinimalHeight();

    /**
     * Returns the offset or margin of the components origin to the contents
     * (image) of the component.
     * It is the position of image with respect to Layoutable's origin, to 
     * be used for spatial alignment.
     *
     * @return the offset or margin
     */
    public int getImageOffset(); // position of image wrt Layoutable's origin, to be used for spatial alignment
}
