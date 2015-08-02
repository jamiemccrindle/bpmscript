package org.bpmscript.journal.memory;

import junit.framework.TestCase;

import org.bpmscript.exec.PausedResult;

public class MemoryContinuationJournalTest extends TestCase {
    public void testInMemoryContinuationJournalNoContinuation() throws Exception {
        MemoryContinuationJournal store = new MemoryContinuationJournal();
        String mainBranch = store.createMainBranch("pid");
        byte[] latestContinuation = store.getContinuationLatest(mainBranch);
        assertNull(latestContinuation);
    }
    public void testInMemoryContinuationJournalLatestContinuation() throws Exception {
        MemoryContinuationJournal store = new MemoryContinuationJournal();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        String version = store.createVersion(pid, mainBranch);
        store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
        assertNotNull(version);
        byte[] latestContinuation = store.getContinuationLatest(mainBranch);
        assertNotNull(latestContinuation);
    }
    public void testInMemoryContinuationJournalContinuationVersion() throws Exception {
        MemoryContinuationJournal store = new MemoryContinuationJournal();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        String version = store.createVersion(pid, mainBranch);
        store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
        assertNotNull(version);
        byte[] latestContinuation = store.getContinuationLatest(mainBranch);
        assertNotNull(latestContinuation);
    }
    public void testGetLatestBranch() throws Exception {
        MemoryContinuationJournal store = new MemoryContinuationJournal();
        String pid = "pid";
        String mainBranch = store.createMainBranch(pid);
        String version = store.createVersion(pid, mainBranch);
        store.storeResult(new byte[] { 1 }, new PausedResult(pid, mainBranch, version, "sdf"));
        assertNotNull(version);
        String newBranch = store.createBranch(version);
        assertNotNull(newBranch);
    }
}
