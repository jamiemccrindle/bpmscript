package org.bpmscript.benchmark;

public interface IBenchmarkResults {
	long getTotalTime();
	double getAverageTime();
    double getTps();
    double getTpm();
}
