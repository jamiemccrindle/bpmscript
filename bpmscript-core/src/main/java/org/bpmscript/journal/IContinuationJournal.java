package org.bpmscript.journal;

import java.util.Collection;

import org.bpmscript.IExecutorResult;
import org.bpmscript.ProcessState;

/**
 * A store for continuations.
 * 
 * @author jamie
 */
public interface IContinuationJournal {
    
    /**
     * Gets the latest version for the branch. Used to see whether cached
     * versions are the latest.
     * 
     * @param the branch to check
     * @return the latest version for that branch...
     */
    Object getVersionLatest(final String branch);
	
	/**
	 * Get the continuation associated with this branch
	 * @param branch the branch to use
	 * 
	 * @return the byte array that represents the continuation
	 */
    byte[] getContinuationLatest(String branch);
    
    /**
     * Create a branch at a particular version
     * 
     * @param version the version to branch at
     * @return the new branch name
     */
    String createBranch(String version);
    
//    /**
//     * Gets rid of a branch
//     * 
//     * @param branch the branch to kill
//     */
//    void killBranch(String branch);
    
    /**
     * Create the root
     * 
     * @param version the version to branch at
     * @return the new branch name
     */
    String createMainBranch(String pid);
    
    /**
     * Stores the result for a particular pid on a particular branch
     * 
     * @param pid the process instance id
     * @param branch the branch to use, it might be a new branch
     * @param continuation the serialized continuation
     * @param result the executor result against that branch
     */
	void storeResult(byte[] continuation, IExecutorResult result);
	
	/**
	 * Get the process state for a particular branch
	 * 
	 * @param branch
	 * @return the process state for a particular process at a particular branch
	 */
	ProcessState getProcessStateLatest(String branch);
	
//    /**
//     * Get the branches that happened after this version including the one it is on. This
//     * can be used when all branches are considered active. 
//     * 
//     * @param version
//     * @return
//     */
//    Collection<String> getBranchesForVersion(String version);

//    /**
//     * Get the latest branch for a process instance id, this can be used if only
//     * the latest branch is considered active. 
//     * 
//     * @param version
//     * @return
//     */
//    String getLatestBranch(String pid);
    
    /**
     * Create new version with associated branch and pid
     * @param pid
     * @param branch
     * @return
     */
    String createVersion(String pid, String branch);
    
    /**
     * Get the branches for this process instance id
     * @param pid
     * @return
     */
    Collection<String> getBranchesForPid(String pid);
    
    /**
     * Get live statuses for a particular process instance
     * 
     * @param pid
     * @return
     */
    Collection<IContinuationJournalEntry> getLiveResults(String pid);
    
    /**
     * Get all results for a particular process instance
     * @return
     */
    Collection<IContinuationJournalEntry> getEntriesForPid(String pid);
}
