
public class PagingProc {

	private double a;
	private double b;
	private double c;
	private int s;
	private int n;
	private int k;
	private boolean isFinished;
	private int curWord;
	private int faults;
	private int evictions;
	private int totalTime;

	public PagingProc(double a, double b, double c, int processSize, int noRefs, int k) {
		this.a = a;
		this.setB(b);
		this.setC(c);
		this.setS(processSize);
		this.setN(noRefs);
		this.setK(k);
		this.isFinished = false;
		this.curWord = (111*k + s) % s;
		this.setFaults(this.setEvictions(0));
		this.totalTime = 0;
	}

	public double getA() {
		return a;
	}

	public void setA(double a) {
		this.a = a;
	}

	public int getCurWord() {
		
		return curWord ;
	}
	
	public void setCurWord(int curWord) {
		this.curWord = curWord;
	}

	public double getB() {
		return b;
	}

	public void setB(double b) {
		this.b = b;
	}

	public double getC() {
		return c;
	}

	public void setC(double c) {
		this.c = c;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public int getFaults() {
		return faults;
	}

	public void setFaults(int faults) {
		this.faults = faults;
	}
	
	public void incFaults() {
		this.faults++;
		
	}
	
	public int getEvictions() {
		return evictions;
	}

	public int setEvictions(int evictions) {
		this.evictions = evictions;
		return evictions;
	}
	
	public void incEvictions() {
		this.evictions++;
		//return evictions;
	}
	
	public int getTotalTime() {
		return totalTime;
	}

	public void incTotalTime(int totalTime) {
		this.totalTime += totalTime;
		
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public void decN() {
		this.n--;
	}
	
	public int getS() {
		return s;
	}

	public void setS(int s) {
		this.s = s;
	}
	
	public double getAvgRes() {
		//System.out.printf("Total Time: %d, Evictions: %d\n", totalTime, evictions);
		//totalTime -= evictions;
		return (double) totalTime/evictions;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		
		this.isFinished = isFinished;
	}

}
