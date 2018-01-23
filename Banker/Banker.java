package Banker;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Banker {
	static Scanner sc;
	static ArrayList<String> elements = new ArrayList<String>();
	static String[] info = new String[3]; 
	static int[] available;
	//static String[][] instructions;
	static int[][] need;
	static int[][] allocated;
	static int[][] max;
	static Task[] tasks;
	static int taskNo = 0;
	static int resources = 0;
	static BankersAlgo Bankers;
	static OptimisticManager OM;
	static int id = 1;
	//private static int[] curProcess;
	//private static String[] activities;
	
	public static void main(String[] args) {
		File f = new File(args[args.length-1]);
		try {
			sc = new Scanner(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		taskNo = sc.nextInt();
		resources = sc.nextInt();
		
		available = new int[resources];
		tasks = new Task[taskNo];
		
		
		
		for(int i = 0; i < tasks.length; i++) {
			tasks[i] = new Task(i + 1);
		}
		
		Bankers = new BankersAlgo(taskNo, resources);
		OM = new OptimisticManager(taskNo, resources);
		
		Bankers = new BankersAlgo(taskNo, resources);
		OM = new OptimisticManager(taskNo, resources);
		
		for(int i = 0; i < resources; i++) {
			int temp = sc.nextInt();
			
			OM.setAvailable(temp, i);
			Bankers.setAvailable(temp, i);
		}
		
		while(sc.hasNextLine()) {
			String temp = sc.nextLine();
			//System.out.println(temp);
			if(temp.length() > 2) {
				String[] tempSplit = temp.split("\\s+");
				int tempTask = Integer.parseInt(tempSplit[1]);
				
				tasks[tempTask - 1].addIns(temp);

			}
			
			elements.add(temp);
			
		}
		
		System.out.println("FIFO\n");
		OM.run(tasks);
		
		for(Task t: tasks) {t.reset();}
		
		System.out.println("\nBanker's Algorithm\n");
		Bankers.run(tasks);
		
		

	}

}
