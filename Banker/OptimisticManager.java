package Banker;

import java.util.ArrayList;

public class OptimisticManager {
	/**
	 * all the essentials
	 */
	static int processes = 0;
	int resources = 0;
	private static int[] available;
	private static int[] init;
	private static int[] intermediateRec;
	static int[][] allocated;
	static boolean[] finished;
	int[] avail;
	int[] valid;
	private ArrayList<Task> block = new ArrayList<Task>();
	
	/**
	 * 
	 * @param proc --> processes
	 * @param rec  --> resources
	 */
	public OptimisticManager(int proc, int rec) {
		processes = proc;
		resources = rec;
		available = new int[rec];
		allocated  = new int[proc][rec];
		finished = new boolean[proc];
	}
	

	/**
	 * 
	 * @param tasks2
	 * 
	 * MAIN FUNCTION, EVERYTHING IS RUN HERE
	 */
	public void run(Task[] tasks2) {
		init = available.clone();
		intermediateRec = available.clone();
		
		int cycle = 0;
		
		//initiate matrices
		for(int i = 0; i < finished.length; i++) {finished[i] = false;}
		for(int i = 0; i < allocated.length; i++) {for(int j = 0; j < allocated[i].length; j++) {allocated[i][j] = 0;}}
		for(int i = 0; i < intermediateRec.length; i++) {intermediateRec[i] = 0;}
		
		boolean possibleDeadlock = false;
		
		
		//as long as not every function is finished, this runs
		while(!done()) {
			
			//System.out.printf("During %d - %d\n", cycle, cycle + 1);
			
			cycle++;
			int finishedTasks = 0;
			for(int i = 0; i < processes; i ++) {if(finished[i] == true) {finishedTasks++;}};
			
			//will track deadlocks
			possibleDeadlock = deadlockState(tasks2);
			
			//this means deadlock is for sure
			if(possibleDeadlock && block.size() == processes - finishedTasks) {
				
				for(int t = 0; t < processes; t++) {
					
					//abort whichever has lowest number
					if(!tasks2[t].isAborted() && !finished[t]) {
						tasks2[t].abort();
						
						//System.out.println(tasks2[t].getID() + " ABORTED");
						
						//release resources to intermediate resources
						for(int m = 0; m < resources; m++) {
							intermediateRec[m] += allocated[t][m];
							allocated[t][m] = 0;
						}
						
						block.remove(block.indexOf(tasks2[t]));
						finished[t] = true;
						possibleDeadlock = deadlockState(tasks2);
						
						if(!possibleDeadlock) {
							break;
						}
					}
					
				}
			}
			
			//transition resource released in previous cycle
			for(int r = 0; r < resources; r++) {
				available[r] += intermediateRec[r];
				intermediateRec[r] = 0;
			}
			
			//tracks if a function has run, helps make sure blocked tasks don't run twice
			boolean[] ran = new boolean[processes];
			for(int b = 0; b < processes; b++) {ran[b] = false;}
			
			//deal with blocked functions first
			if(block.size() > 0) {
				
				for(int b = 0; b < block.size(); b++) {
					
					Task temp = block.get(b);
					
					//break down tasks into necessities
					String[] tempIns = temp.getInstruction().split("\\s+");
					int id = temp.getID();
					int res = Integer.parseInt(tempIns[2]);
					int num = Integer.parseInt(tempIns[3]);
					
					if(tempIns[0].equals("request")) {
						
						//if there aren't enough, keep blocking
						if(num > available[res - 1]) {
							//System.out.printf("Task %d is still blocking\n", temp.getID());
							ran[temp.getID() - 1] = true;
							tasks2[id - 1].incWaitTime();
							tasks2[id - 1].setTimeTaken(tasks2[id - 1].getTimeTaken() + 1);
						}
						//if enough, run
						else{
							ran[temp.getID() - 1] = true;
							execute(tempIns[0], temp.getInstruction(), tasks2, temp.getID() - 1);
							block.remove(block.indexOf(temp));
							b--;
						}
					}
				}
			}
			
			//run the rest of the functions
			for(int i = 0; i < tasks2.length; i++) {
				if(!ran[i] && !finished[i]) {
					String[] temp = tasks2[i].getInstruction().split("\\s+");
					execute(temp[0], tasks2[i].getInstruction(), tasks2, i);
				}
			
			}
			possibleDeadlock = deadlockState(tasks2);
			
		}printSummary(tasks2);
	}
	
	private void execute(String ins, String fullIns, Task[] tasks2, int l) {
		String[] temp= fullIns.split("\\s+");
		//task number
		int taskNo= Integer.parseInt(temp[1]);
		//resource number
		int res= Integer.parseInt(temp[2]);
		//number of resource requested
		int num= Integer.parseInt(temp[3]);
		
		if(ins.equals("request")) {
			
			//if enough of a resource, allocate
			if(num <= available[res - 1]) {
				//System.out.printf("Request by %d is safe!\n", tasks2[l].getID());
				allocated[l][res - 1] += num;
				available[res - 1] -= num;
				
				tasks2[l].nextInstruction();
			}
			//if not, block
			else {
				//System.out.printf("Task %d could not be granted\n", tasks2[l].getID());
				block.add(tasks2[l]);
				tasks2[l].incWaitTime();
			}
			
		}else if(ins.equals("release")) {

			//release resources to be placed into available during next cycle
			intermediateRec[res - 1] += allocated[l][res -1];
		
			//System.out.printf("Task %d released\n", tasks2[l].getID());
			
			allocated[l][res - 1] = 0;
			
			tasks2[l].nextInstruction();
			
			if(tasks2[l].isFinished()) {
				finished[l] = true;
			}
			
		}else if(ins.equals("compute")) {
		
			//comp is just set compute time
			
			int comp = Integer.parseInt(temp[2]);
			
			
			//make a process compute and decrease times
			if(!tasks2[l].isComputing()) {
				tasks2[l].computeTime = comp - 1;
				tasks2[l].setComputing(true);
				
			}else {
				tasks2[l].computeTime--;
				//only move on to next instruction after compute time is done
				
			}
			
			//if computing is done, move on
			if(tasks2[l].computeTime == 0) {
				tasks2[l].setComputing(false);
				tasks2[l].nextInstruction();
			}
			if(tasks2[l].isFinished()) {
				finished[l] = true;
			}
			
		}else if(ins.equals("initiate")) {
			//FIFO doesn't care about initiate
			
			tasks2[l].nextInstruction();
		}
		//increase totaltime
		tasks2[l].setTimeTaken(tasks2[l].getTimeTaken() + 1);
	}
	
	

	private Boolean deadlockState(Task[] tasks2) {
		for(Task t: tasks2) {
			String instruction = t.getInstruction();
			String[] insSplit = instruction.split("\\s+");
			
			if(!t.isAborted() && !t.isFinished() && insSplit[0].equals("request")) {
				
				int curRes = Integer.parseInt(insSplit[2]);
				int resReq = Integer.parseInt(insSplit[3]);
				//if something can finish, it is not deadlocked
				if(available[curRes - 1] + intermediateRec[curRes-1] >= resReq) {
					return false;
				}
			}
		}
		return true;
	}

	
	private boolean done() {
		//checks if each task is finished via checking booleans in finished
		boolean isDone = true;
		for(boolean b : finished) {
			if (b == false) {
				isDone = false;
			}
		}return isDone;
		
		//return finished.stream().some(b -> false);
	}



	public void setAvailable(int temp, int i) {
		available[i] = temp;
	}
	
	public void printSummary(Task[] tasks2) {
		//print tasks
		for(Task t: tasks2) {
			if(t.isAborted()) {
				System.out.printf("Task %d: %s\n", t.getID(), "ABORTED");
			}else {
				System.out.printf("Task %d: %d %d %d%%\n", t.getID(), t.getTimeTaken(), t.getWaitTime(), (int) ((float) t.getWaitTime()/t.getTimeTaken() * 100));
			}	
		}
		
		int totalTime = getTotalTime(tasks2);
		int totalWait = getTotalWait(tasks2);
		//print total
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
