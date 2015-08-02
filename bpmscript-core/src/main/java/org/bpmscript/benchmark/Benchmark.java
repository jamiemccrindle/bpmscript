package org.bpmscript.benchmark;

import org.springframework.util.StopWatch;


public class Benchmark implements IBenchmark {

	public IBenchmarkResults execute(final int total, IBenchmarkCallback callback, IWaitForCallback waitFor, boolean warmup) throws Exception {
		if(warmup) {
		    callback.execute(-1);
		}
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start("random");
		for(int i = 0; i < total; i++) {
		    callback.execute(i);
		}
		if(waitFor != null) {
			waitFor.call();
		}
		stopWatch.stop();
		return new IBenchmarkResults(){
            public double getTpm() {
                return (double) total / (double) stopWatch.getTotalTimeMillis() * 60000;
            }
        
            public double getTps() {
                return (double) total / (double) stopWatch.getTotalTimeMillis() * 1000;
            }
        
			public long getTotalTime() {
				return stopWatch.getTotalTimeMillis();
			}
		
			public double getAverageTime() {
				return (double) stopWatch.getTotalTimeMillis() / (double) total;
			}
		};
	}
    public IBenchmarkResults execute(final int total, IBenchmarkCallback callback) throws Exception {
        return execute(total, callback, null, false);
    }
}
