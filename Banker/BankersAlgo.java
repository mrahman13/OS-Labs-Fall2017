package Banker;

import java.util.ArrayList;

public class BankersAlgo {
	/**
	 * the essentials
	 */
	static int processes = 0;
	static int resources = 0;
	private static int[] available;
	private static int[] init;
	private static int[] intermediateRec;
	private static int[][] totalRec;
	static int[][] need;
	static int[][] allocated;
	static int[][] max;
	static boolean[] finished;
	int[] avail;
	int[] valid;
	private ArrayList<Task> block = new ArrayList<Task>();
	
	/**
	 * Constructor
	 * @param proc
	 * @param rec
	 */
	public BankersAlgo(int proc, int rec) {
		processes = proc;
		resources = rec;
		available = new int[rec];
		intermediateRec = new int[rec];
		need = new int[proc][rec];
		allocated  = new int[proc][rec];
		totalRec = new int[proc][rec];
		max = new int[proc][rec];
		finished = new boolean[proc];
	}
	
	/**
	 * MAIN FUNCTION
	 * @param tasks2
	 */
	public void run(Task[] tasks2) {
		init = available.clone();
		
		int cycle = 0;
		
		//initiate matrices
		for(int i = 0; i < finished.length; i++) {finished[i] = false;}
		for(int i = 0; i < allocated.length; i++) {for(int j = 0; j < allocated[i].length; j++) {allocated[i][j] = 0; totalRec[i][j] = 0;}}
		
		for(int i = 0; i < intermediateRec.length; i++) {intermediateRec[i] = 0;}
		
		
		while(!done()) {
			//System.out.printf("During %d - %d\n", cycle, cycle + 1);
		
			//transition resources from last cycle to available
			for(int r = 0; r < resources; r++) {
				available[r] += intermediateRec[r];
				
				intermediateRec[r] = 0;
			}
			
			//check blocked first and then run rest of unblocked processes
			//create array of booleans[] ran 
			boolean[] ran = new boolean[processes];
			for(int b = 0; b < processes; b++) {ran[b] = false;}
			
			//check blocked
			if(block.size() > 0) {
				
				for(int b = 0; b < block.size(); b++) {
					Task temp = block.get(b);
					
					String[] tempIns = temp.getInstruction().split("\\s+");
					int res = Integer.parseInt(tempIns[2]);
					int num = Integer.parseInt(tempIns[3]);
					
					currentNeed();
					
					if(tempIns[0].equals("request")) {
						//if not safe, don't run
						if(!isSafe(temp.getInstruction(), tasks2, temp.getID() - 1)) {
							ran[temp.getID() - 1] = true;
							tasks2[temp.getID() - 1].incWaitTime();
						}
						//if safe, run
						else{
							ran[temp.getID() - 1] = true;
							
							execute(tempIns[0], temp.getInstruction(), tasks2, temp.getID() - 1);
							block.remove(block.indexOf(temp));
							b--;
						}
					}
					
				}
			}
			
			//run rest of tasks
			for(int i = 0; i < tasks2.length; i++) {
				
				if(!finished[i]) {
					tasks2[i].setTimeTaken(tasks2[i].getTimeTaken() + 1);
				}
				
				if(!ran[i] && !finished[i]) {
					String[] temp = tasks2[i].getInstruction().split("\\s+");
					execute(temp[0], tasks2[i].getInstruction(), tasks2, i);
				}
								
			}
			
			cycle++;
		}printSummary(tasks2);
		
	}
	
	/*
	 * calculate current need
	 */
	public static void currentNeed() {
		for(int i = 0; i < need.length; i++) {
			
			for(int j = 0; j < need[0].length; j++) {
				
				need[i][j] = max[i][j] - allocated[i][j];
			}
			
		}
		
	}
	
	/**
	 * execute tasks
	 * @param ins
	 * @param fullIns
	 * @param tasks2
	 * @param l
	 */
	private void execute(String ins, String fullIns, Task[] tasks2, int l) {
		String[] temp= fullIns.split("\\s+");
		//task number
		int taskNo= Integer.parseInt(temp[1]);
		//resource number
		int res= Integer.parseInt(temp[2]);
		//number of resource requested
		int num= Integer.parseInt(temp[3]);
		
		
		if(ins.equals("request")) {
			
			//if task requests more than it claims, abort 
			if(num > max[l][res - 1] || totalRec[l][res - 1] + num > max[l][res - 1]) {
				
				System.out.printf("Task %d aborted (requested more than its claim)\n", tasks2[l].getID());
				tasks2[l].abort();
				
				//release resources
				for(int m = 0; m < resources; m++) {
					totalRec[l][m] = 0;
					intermediateRec[m] += allocated[l][m];
					allocated[l][m] = 0;
				}
				//set finished
				finished[l] = true; 
			}
			//if a request is safe, allocate
			else if(isSafe(fullIns, tasks2, l)) {
				//allocate
				allocated[l][res - 1] += num;
				totalRec[l][res - 1] += num;
				available[res - 1] -= num;

				tasks2[l].nextInstruction();
				
			}else {
				//block if not enough
				block.add(tasks2[l]);
				tasks2[l].incWaitTime();
			}
		}
		
		else if(ins.equals("release")) {
			
			//release resource, add to intermediate resource array to transfer during next cycle
			int release = allocated[l][res - 1];
			totalRec[l][res - 1] -= release;
			intermediateRec[res - 1] += release;
			allocated[l][res - 1] = 0;
			
			tasks2[l].nextInstruction();
			if(tasks2[l].isFinished()) {
				finished[l] = true;
			}
			
			
		}
		
		else if(ins.equals("compute")) {
			
			//parse numbers
			taskNo = Integer.parseInt(temp[1]);
			int comp = Integer.parseInt(temp[2]);
			num = Integer.parseInt(temp[3]);
			
			//make a process compute and decrease times
			if(!tasks2[l].isComputing()) {
				tasks2[l].computeTime = comp - 1;
				tasks2[l].setComputing(true);
				
			}else {
				tasks2[l].computeTime--;
				//only move on to next instruction after compute time is done
				
			}
			//if done move on to next instruction
			if(tasks2[l].computeTime == 0) {
				tasks2[l].setComputing(false);
				tasks2[l].nextInstruction();
			}
			if(tasks2[l].isFinished()) {
				finished[l] = true;
			}

			
		}//if the task is initiate
		else if(ins.equals("initiate")) {
			
			//allow claims if possible
			if(num  <= available[res-1]) {
				//
				need[l][res - 1] = num;
				max[l][res - 1] = num;
				allocated[l][res - 1] = 0;
				tasks2[l].nextInstruction();
			}
			//if process claims more than available 
			else {
				System.out.printf("Task %d aborted (claim exceeds total available in system)\n", tasks2[l].getID());
				tasks2[l].abort();
				for(int m = 0; m < resources; m++) {
					
					intermediateRec[m] += allocated[l][m];
					allocated[l][m] = 0;
				}
				finished[l] = true;
			}
			
		}
	
	}

	


	public static Boolean isSafe(String fullIns, Task[] tasks2, int l) {
		int[] workingSet = available.clone();
		int[][] fakeNews = need.clone();
		String[] temp = fullIns.split("\\s+");
		int res = Integer.parseInt(temp[2]);
		//duplicates of available and need respectively, just wanted to have some fun with the second name
		workingSet[Integer.parseInt(temp[2]) - 1] -= Integer.parseInt(temp[3]);
		fakeNews[l][res - 1] -= Integer.parseInt(temp[3]);
		
			boolean allocated = false;
			
			//go through processes
			for(int i  = 0; i <  tasks2.length; i++) {
				
				
				if(!finished[i]) {
						//check if enough of each process
						boolean intermediate = true;
						for(int r = 0; r < resources; r++) {
							if(fakeNews[i][r] > workingSet[r]) {
								intermediate = false;
							}
						}
						if(fakeNews[i][res - 1] <= workingSet[res-1]) {
							allocated = intermediate;
						}
					}
				}
			
			return allocated;
	}


	public void setAvailable(int temp, int i) {
		available[i] = temp;
	}
	
	/**
	 * checks if each function is done
	 * @return
	 */
	private boolean done() {
		boolean isDone = true;
		for(boolean b : finished) {
			if (b == false) {
				isDone = false;
			}
		}return isDone;
		
		//return finished.stream().some(b -> false);
	}
	
	public void printSummary(Task[] tasks2) {
		//calculate each task
		for(Task t: tasks2) {
			if(t.isAborted()) {
				System.out.printf("Task %d: %s\n", t.getID(), "ABORTED");
			}else {
				System.out.printf("Task %d: %d %d %d%%\n", t.getID(), t.getTimeTaken(), t.getWaitTime(), (int) ((float) t.getWaitTime()/t.getTimeTaken() * 100));
			}	
		}
		
		int totalTime = getTotalTime(tasks2);
		int totalWait = getTotalWait(tasks2);
		//calculate total
		System.out.printf("Total %d %d %.2f%%\n", totalTime, totalWait, ((float) totalWait/totalTime * 100));
	}

	private int getTotalTime(Task[] tasks2) {
		int tot = 0;
		for(Task t : tasks2) {
			if(!t.isAborted()) {
				tot += t.getTimeTaken();
			}
			
		}
		return tot;
	}
	
	private int getTotalWait(Task[] tasks2) {
		int tot = 0;
		for(Task t : tasks2) {
			if(!t.isAborted()) {
				tot += t.getWaitTime();
			}
			
		}
		return tot;
	}

}
