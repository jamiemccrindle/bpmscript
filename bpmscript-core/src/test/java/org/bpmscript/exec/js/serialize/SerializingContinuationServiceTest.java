package org.bpmscript.exec.js.serialize;

import java.util.HashMap;

import org.bpmscript.exec.CompletedResult;
import org.bpmscript.exec.js.serialize.JavascriptSerializingContinuationService;
import org.bpmscript.journal.memory.MemoryContinuationJournal;

import junit.framework.TestCase;

public class SerializingContinuationServiceTest extends TestCase {
    public void testSerializingContinuationService() throws Exception {
        JavascriptSerializingContinuationService service = new JavascriptSerializingContinuationService();
        MemoryContinuationJournal continuationJournal = new MemoryContinuationJournal();
        service.setContinuationStore(continuationJournal);
        HashMap<String, Object> invocationContext = new HashMap<String, Object>();
        String pid = "asdf";
        String branch = continuationJournal.createMainBranch(pid);
        String version = continuationJournal.createVersion(pid, branch);
        service.storeResult(invocationContext, new String[] {}, new CompletedResult(pid, branch, version, "result"));
    }
}
