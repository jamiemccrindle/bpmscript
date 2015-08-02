package org.bpmscript.benchmark;


public interface IBenchmark {
	IBenchmarkResults execute(final int total, IBenchmarkCallback callable, IWaitForCallback waitFor, boolean warmup) throws Exception;
}