package Banker;

import java.util.ArrayList;

public class Task {
	private ArrayList<String> instructions = new ArrayList<String>();
	private boolean isStopped = false;
	private boolean isComputing =false;
	private int cur = 0;
	private int waitTime = 0;
	private int timeTaken = 0;
	private int id;
	public int computeTime;
	
	
	public Task(int i) {
		id = i;
	}
	
	public void incWaitTime() {
		setWaitTime(getWaitTime() + 1);
	}
	
	public int getID() {
		return id;
	}
	
	public void addIns(String ins) {
		instructions.add(ins);
	}

	public boolean isFinished() {
		String[] check = getInstruction().split("\\s+");
		if(check[0].equals("terminate")) {
			return true;
		}
		return false;
	}

	public boolean isAborted() {
		if(isStopped == true) {
			return true;
		}
		return false;
	}

	public String getInstruction() {
		return instructions.get(cur);
	}
	
	public void nextInstruction() {
		cur++;
	}

	public void abort() {
		isStopped = true;
	}

	public int getWaitTime() {
		return waitTime;
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
	}

	public int getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(int timeTaken) {
		this.timeTaken = timeTaken;
	}

	public boolean isComputing() {
		return isComputing;
	}

	public void setComputing(boolean isComputing) {
		this.isComputing = isComputing;
	}
	
	public void reset() {
		isStopped = false;
		isComputing =false;
		cur = 0;
		waitTime = 0;
		timeTaken = 0;
	}
	
}
