package mpi.search.result.model;

import java.util.Comparator;


/**
 * A comparator for search matches.
 * 
 * @author klasal
 */
@SuppressWarnings("rawtypes")
public class MatchComparator implements Comparator {
    /**
     *
     * @param o1 first match
     * @param o2 second match
     *
     * @return comparison of values
     * 
     * @see String#compareTo(String)
     */
    @Override
	public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Match) || !(o2 instanceof Match)) {
            return 0;
        }

        return (((Match) o1).getValue().compareTo(((Match) o2).getValue()));
    }
}
