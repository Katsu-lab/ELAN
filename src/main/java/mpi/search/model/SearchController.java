package mpi.search.model;

import mpi.search.query.model.Query;
import mpi.search.result.model.Result;

/**
 *
 * @author Alexander Klassmann
 * @version July 2004
 */
public interface SearchController {
    static public int INIT = 0;
    static public int ACTIVE = 1;
    static public int FINISHED = 2;
    static public int INTERRUPTED = 3;
    
    /**
     * Execute a query tree on behalf of forQManager.
     * 
     * @param query the query to execute
     */
    public void execute(Query query);
    
    /**
     * Checks if search is running.
     * 
     * @return true if in process
     */
    public boolean isExecuting();

    /**
     * Stop searching.
     */
    public void stopExecution();

	/**
	 * The result contains all matches found until stopped.
	 * 
	 * @return Result the search result
	 */
    public Result getResult();
    
    /**
     * Set a GUI for showing progress of search.
     * 
     * @param o the listener
     */
    public void setProgressListener(ProgressListener o);
    
    /**
     * Get current search duration.
     * 
     * @return search duration in ms
     */
    public long getSearchDuration();

}
