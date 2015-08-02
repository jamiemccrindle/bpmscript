package org.bpmscript.benchmark;

public interface IBenchmarkPrinter {
	
	public static final IBenchmarkPrinter STDOUT = new IBenchmarkPrinter() {
	
		public void print(IBenchmarkResults results) {
			System.out.println("tot: " + results.getTotalTime() + " ms");
			System.out.println("avg: " + results.getAverageTime() + " ms");
            System.out.println("tps: " + results.getTps() + " tpm");
            System.out.println("tpm: " + results.getTpm() + " tpm");
		}
	
	};
	
	void print(IBenchmarkResults results);
}
