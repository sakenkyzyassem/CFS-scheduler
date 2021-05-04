package com.rbt.scheduler;

import java.util.*;
import java.io.*;

public class InputProcessor {
    protected int num_of_procs;
    protected long total_runtime;
    protected ArrayList<Process> processes_list;
    protected Map<Integer, ArrayList<Process>> processes;
    private Scanner scanner;
    private BufferedReader br;

    public InputProcessor() {
        this.processes = new HashMap<Integer, ArrayList<Process>>();
        this.processes_list = new ArrayList<Process>();
    }

    private int getRandomNumber(int min, int max) {
        return (int) ( Math.random() * (max - min) ) + min;
    }

    // CASE: No input given
    public void readInput() {
        scanner = new Scanner(System.in);
        System.out.println("Enter number of processes to run: ");
        this.num_of_procs = scanner.nextInt();
        System.out.println("Would you like to read data from a file or randomly generate them?");
        System.out.println("Choose an option:\n(1) -> Randomly generated processes\n(2) -> Read from a file");
        int next_step = scanner.nextInt();
        scanner.nextLine();

        switch (next_step) {
            case 1:
                scanner.close();
                generateInput(this.num_of_procs);
                break;
            case 2:
                System.out.println("Please enter the name of the file (with relative path)");
                System.out.println("NOTE: File format expected to have:\n" +
                        "         #process priority burst_time_ms arrival_time_ms\n" +
                        "         Example:\n" +
                        "         ------------------------------------------------------------- file file_name.txt\n" +
                        "         1 \t0 \t325352 \t1\n" +
                        "         ...\n" +
                        "         k \t0 \t10036 \t1339\n" +
                        "         k+1 -10 12415 \t1346\n" +
                        "         ...\n" +
                        "         n \t10 \t141516 \t25145\n" +
                        "         -------------------------------------------------------------");
                String filename = scanner.next();
                scanner.nextLine();
                readFile(this.num_of_procs, filename);
                break;
            default:
                System.out.println("Wrong input");
                System.out.println("Would you like to try again? (1 -> Yes | 0 -> No");
                next_step = scanner.nextInt();
                if( next_step == 1 ) {
                    readInput();
                } else {
                    System.out.println("Exiting the program\nGoodbye!");
                }
                break;
        }


    }

    // CASE: User provides number of processes (procs_count) and filename
    public void readFile(int procs_count, String filename) {
        this.num_of_procs = procs_count;

        try {
            br = new BufferedReader( new FileReader(filename));
            String line;

            while ( (line = br.readLine()) != null ) {
                String[] data = line.split(" ");
                Process proc = new Process(data[0], data[1], data[2], data[3]);

                ArrayList list = this.processes.getOrDefault(Integer.parseInt(data[3]), new ArrayList<>());
                list.add( proc );
                this.processes.put(Integer.parseInt(data[3]), list);

                this.processes_list.add( proc );

                this.total_runtime += Integer.parseInt(data[2]);
            }
        } catch (FileNotFoundException e) {
            this.scanner = new Scanner(System.in);
            System.out.println("File was not found. Are you sure you wrote filename correct?");
            System.out.println("Would you like to try again? (1 -> Yes | 0 -> No)");
            int res = scanner.nextInt();
            if( res == 1 ) {
                readInput();
            } else {
                System.out.println("You choose not to continue. Goodbye!");
                e.printStackTrace();
            }
        } catch ( IOException e ) {
            System.out.println("Ooops, seems like there was a problem while reading the file");
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // CASE: User only gives number of processes to run -> have to generate processes randomly
    public void generateInput(int num_of_procs) {
        this.num_of_procs = num_of_procs;

        for( int i = 0; i < this.num_of_procs; i++ ) {
            int priority = getRandomNumber(-20, 20);
            int burst_time = getRandomNumber(1, this.num_of_procs);
            int arrival_time = getRandomNumber(0, this.num_of_procs*2);
            Process proc = new Process(i+1, priority, burst_time, arrival_time);

            ArrayList list = this.processes.getOrDefault(arrival_time, new ArrayList<>());
            list.add( proc );
            processes.put(arrival_time, list);

            this.processes_list.add(proc);

            this.total_runtime += burst_time;
        }

        System.out.printf("Finished generating %d processes\n", this.num_of_procs);
        System.out.printf("Total runtime is %d\n", this.total_runtime);
    }

}
