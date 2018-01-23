import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
/**
 *
 * @author Muhamed Rahman
 * OS202 Fall 2017 Gottlieb
 *
 */
public class Paging {
	static int machineSize;static int pageSize;static int processSize;static int jobMix;static int noReferences;
	static int debug;
	static String replacementAlgorithm;
	private static ArrayList<PagingProc> processes = new ArrayList<PagingProc>();
	static //private static Boolean[] finished;
	int finishedProc = 0;
	private static int curPage;
	static int foundFrame;
	static Scanner random;


	public static void main(String[] args) {
		//all the important information
		machineSize = Integer.parseInt(args[0]);
		pageSize = Integer.parseInt(args[1]);
		processSize = Integer.parseInt(args[2]);
		jobMix = Integer.parseInt(args[3]);
		noReferences = Integer.parseInt(args[4]);
		replacementAlgorithm = args[5];

		//prints the initial info
		System.out.printf("The machine size is %d.\nThe page size is %d.\n", machineSize, pageSize);
		System.out.printf("The process size is %d.\nThe job mix is %d.\n", processSize, jobMix);
		System.out.printf("The number of references per process is %d.\nThe replacement algorithm is %s.\n\n", noReferences, replacementAlgorithm);

		final int quantum = 3;

		setProcesses(jobMix, noReferences, processSize);

		int noFrames = machineSize / pageSize;

		//essential tables
		int[] frameTable = new int[noFrames];
		PagingProc[] frameProc = new PagingProc[noFrames];
		int[] residency = new int[noFrames];
		int[] tableTime = new int[noFrames];

		//will help insert from back
		int filledFrames = 0;

		//initialize all tables set up above
		for (int j = 0; j < frameTable.length; j++) {
			frameTable[j] = -1;
			frameProc[j] = null;
			residency[j] = 0;
			tableTime[j] = 0;
		}

		boolean inTable = false;
		int replaceIndex = 0;


		while(finishedProc < processes.size()) {

			for(int i = 0; i < processes.size(); i++) {

				for(int q = 0; q < quantum; q++) {

					//if finished, set to finish
					if(processes.get(i).getN() <= 0) {
						processes.get(i).setFinished(true);
						finishedProc++;
						break;
					}

					//current page
					curPage = processes.get(i).getCurWord() / pageSize;

					inTable = checkInTable(frameTable, frameProc, processes.get(i), curPage);

					//System.out.printf("Process %d %d using frame %d\n", i+1, processes.get(i).getCurWord(), noFrames-filledFrames - 1);

					if(inTable) {

						//if called again you want the time of that page in least recently used to  be reset
						if(replacementAlgorithm.equals("lru")) {
							tableTime[foundFrame] = 0;
						}

					}else {

						if(filledFrames <  noFrames) {
							//if there are free frames, fill them
							filledFrames++;
							int nextFrame = noFrames - filledFrames;
							frameTable[nextFrame] = curPage;
							frameProc[nextFrame] = processes.get(i);
							tableTime[nextFrame] = 0;
							residency[nextFrame] = 0;
							processes.get(i).incFaults();

						}else {
							//replacement algo
							switch(replacementAlgorithm) {
								case "lru":

								//run LRU
									replaceIndex = LRU(tableTime);
									break;

								case "lifo":

								//run LIFO
									replaceIndex = LIFO(tableTime);
									break;

								case "random":

									int random = getR();
									replaceIndex = random % frameTable.length;

								//get random and eject it

									break;
								default:
									System.out.println("ENTER A PROPER ALGORITHM PLEASE");
									finishedProc= processes.size();
									break;
							}
							//the commented stuff out was used to debug
							//int index = processes.indexOf(frameProc[replaceIndex]);
							//System.out.printf("Process %d evicted!\n", index + 1);

							frameProc[replaceIndex].incEvictions();
							frameProc[replaceIndex].incTotalTime(residency[replaceIndex]);

							//System.out.printf("Process %d had %d time\n", index + 1, frameProc[replaceIndex].getTotalTime());

							frameProc[replaceIndex] = processes.get(i);
							residency[replaceIndex] = 0;
							frameTable[replaceIndex] = curPage;
							tableTime[replaceIndex] = 0;
							processes.get(i).incFaults();

						}

					}

					//if there's something in a page, increment it
					for(int m = 0; m < frameTable.length; m++) {
						if(frameProc[m] != null) {
								tableTime[m]++;
								residency[m]++;
						}


					}

					//decrease references and get next word
					processes.get(i).decN();
					next(processes.get(i));
				}


			}

		}//System.out.print(filledFrames);

		//print info for each process
		for(int i = 0 ; i < processes.size(); i++) {
			//System.out.printf("Process %d had %d evictions and the time is %d\n", i + 1, processes.get(i).getEvictions(), processes.get(i).getTotalTime());
			if(processes.get(i).getEvictions() >0) {
				System.out.printf("Process %d had %d faults and the overall average residence is %.2f\n", i + 1, processes.get(i).getFaults(), processes.get(i).getAvgRes());
			}else {
				System.out.printf("Process %d had %d faults and the average residence is undefined \n", i + 1, processes.get(i).getFaults());

			}

		}

		//print total info
		printTotal();
	}

	private static void printTotal() {
		double avgRes = 0;
		int tot = 0;
		int evictions = 0;
		int faults = 0;
		for(int i = 0; i < processes.size(); i++) {
			faults += processes.get(i).getFaults();
			evictions += processes.get(i).getEvictions();
			tot += processes.get(i).getTotalTime();
		}

		avgRes = (double) tot/evictions;
		if(evictions >0) {
			System.out.printf("The total number of faults is %d and the overall average residence is %f.", faults, avgRes);
		}else {
			System.out.printf("The total number of faults is %d and the overall average residence is undefined.", faults);

		}
	}

	private static void setProcesses(int j, int n, int s) {
		PagingProc p0, p1, p2, p3;
		if(j == 1) {
			p0 = new PagingProc(1.0, 0.0, 0.0, s, n, 1);
			processes.add(p0);
		}else if (j == 2){
			p0 = new PagingProc(1.0, 0.0, 0.0, s, n, 1);
			p1 = new PagingProc(1.0, 0.0, 0.0, s, n, 2);
			p2 = new PagingProc(1.0, 0.0, 0.0, s, n, 3);
			p3 = new PagingProc(1.0, 0.0, 0.0, s, n, 4);
			processes.add(p0); processes.add(p1); processes.add(p2); processes.add(p3);
		}else if (j == 3) {
			p0 = new PagingProc(0.0, 0.0, 0.0, s, n, 1);
			p1 = new PagingProc(0.0, 0.0, 0.0, s, n, 2);
			p2 = new PagingProc(0.0, 0.0, 0.0, s, n, 3);
			p3 = new PagingProc(0.0, 0.0, 0.0, s, n, 4);
			processes.add(p0); processes.add(p1); processes.add(p2); processes.add(p3);
		}else if (j == 4) {
			p0 = new PagingProc(0.75, 0.25, 0.0, s, n, 1);
			p1 = new PagingProc(0.75, 0.0, 0.25, s, n, 2);
			p2 = new PagingProc(0.75, 0.125, 0.125, s, n, 3);
			p3 = new PagingProc(0.5, 0.125, 0.125, s, n, 4);
			processes.add(p0); processes.add(p1); processes.add(p2); processes.add(p3);
		}

	}

	//gets random
	private static int getR() {
		if(random == null) {
			try {
				File f = new File("random.txt");
				random = new Scanner(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}

		int r = random.nextInt();
		return r;
	}


	//checks if a page is in a table
	private static boolean checkInTable(int[] table, PagingProc[] pTable, PagingProc p, int page) {
		for (int j = 0; j < table.length; j++) {

			if(page == table[j] && p == pTable[j]) {
				foundFrame = j;
				return true;
			}
		}
		return false;
	}

	private static int LIFO(int[] table) {
		int lifo = 0;

		for(int i = 0; i < table.length; i ++) {
			//System.out.printf("Index %d: LRU: %d\n", i, table[i]);
			if(table[lifo] > table[i]) {
				lifo = i;
			}
		}

		return lifo;
	}

	private static int LRU(int[] table) {
		int lru = 0;

		for(int i = 0; i < table.length; i ++) {
			//System.out.printf("Index %d: LRU: %d\n", i, table[i]);
			if(table[lru] < table[i]) {
				lru = i;
			}
		}

		return lru;
	}

	private static void next(PagingProc p) {

		double a = p.getA();
		double b = p.getB();
		double c = p.getC();

		int curWord = p.getCurWord();
		//get random number
		int r = getR();

		double random = r /  (Integer.MAX_VALUE + 1d);

		if(random < a){
			p.setCurWord((curWord + 1 + p.getS()) % p.getS());
		}
		else if(random < a + b){
			p.setCurWord((curWord - 5 + p.getS()) % p.getS());
		}
		else if(random < a + b + c){
			p.setCurWord((curWord + 4 + p.getS()) % p.getS());
		}
		else{
			p.setCurWord(getR() % p.getS());
		}
		//System.out.println(r);
		//System.out.println(p.getCurWord());
		//System.out.println(a);
	}

}
