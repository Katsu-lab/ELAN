package mpi.eudico.server.corpora.clomimpl.shoebox;

import mpi.eudico.server.corpora.clom.Annotation;

import java.awt.Rectangle;


/**
 * 
 */
public class AnnotationSizeContainer implements Comparable {
    public static int PIXELS = 1;
    public static int SPACES = 2;
    private int _size;
    private Annotation _ann;
    private int _type = 0;
    private long _stime = 0;

    public Long _lstime = null;
    private long _etime = 0;
    private Rectangle _rect = null;

    /**
     * Creates a new AnnotationSizeContainer instance
     * 
     * @param ann the annotation
     * @param size the size
     * @param st start time
     * @param et end time
     * @param type one of {@code #PIXELS} or {@code #SPACES}
     */
    public AnnotationSizeContainer(Annotation ann, Integer size, long st,
        long et, int type) {
        _ann = ann;
        _size = size.intValue();
        _type = type;
        _stime = st;
        _etime = et;
        _lstime = Long.valueOf(_stime);
    }

    /**
     * Creates a new AnnotationSizeContainer instance
     *
     * @param ann the annotation
     * @param size the size
     * @param type one of {@code #PIXELS} or {@code #SPACES}
     */
    public AnnotationSizeContainer(Annotation ann, Integer size, int type) {
        _ann = ann;
        _size = size.intValue();
        _type = type;

        if (ann != null) {
            _lstime = Long.valueOf(ann.getBeginTimeBoundary());
        }
    }

    /**
     * Creates a new AnnotationSizeContainer instance
     *
     * @param ann the annotation
     * @param size the size
     * @param type one of {@code #PIXELS} or {@code #SPACES}
     */
    public AnnotationSizeContainer(Annotation ann, int size, int type) {
        _ann = ann;
        _size = size;
        _type = type;

        if (ann != null) {
            _lstime = Long.valueOf(ann.getBeginTimeBoundary());
        }
    }

    /**
     * @param rect the Rectangle for this container
     */
    public void setRect(Rectangle rect) {
        _rect = rect;
    }

    public Annotation getAnnotation() {
        return _ann;
    }

    public int getSize() {
        return _size;
    }

    public int getType() {
        return _type;
    }

    public long getStartTime() {
        return _stime;
    }

    public long getEndTime() {
        return _etime;
    }

    // compare to interface
    @Override
	public int compareTo(Object o) {
        if (_lstime == null) {
            System.out.println("NULL STIME");

            return -1;
        }

        Long l = null;
        Long l1 = null;

        if (_ann != null) {
            l1 = Long.valueOf(_ann.getBeginTimeBoundary());
        }

        if (o instanceof Long) {
            l = (Long) o;
        } else {
            l = Long.valueOf(((AnnotationSizeContainer) o).getAnnotation()
                          .getBeginTimeBoundary());
        }

        System.out.println(l1 + " " + l1);

        return l1.compareTo(l);
    }
}
