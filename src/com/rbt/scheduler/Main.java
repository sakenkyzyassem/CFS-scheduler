package com.rbt.scheduler;

// Indentation style: K&R

import java.io.IOException;
import java.util.ArrayList;

public class Main
{
    // Following data is constant (in microseconds)
    static final double sched_latency = 6.00;
    static final double sched_min_granularity = 0.75;
    static final double sched_wakeup_granularity = 1.00;

    static double getPeriod(int active_num_of_procs) {
        return Math.max(sched_latency, active_num_of_procs * sched_min_granularity);
    }

    static double roundTo(double num, int decimals) {
        return ((int) (num * Math.pow(10, decimals))) / Math.pow(10, decimals);
    }

    static double getVruntime(Process proc, double total_weight) {
        return roundTo(proc.vruntime + ( (Math.pow(2, 10) * proc.thisruntime) / proc.getWeight() ), 2);
    }

    static double getSlice(double weight, double total_weight, int procs_count) {
        return roundTo((weight / total_weight) * getPeriod(procs_count), 2);
    }

    static class Statistics {
        double ave_waiting_time, ave_response_time, ave_preemption;
        ArrayList<Process> processes;

        public Statistics( ArrayList<Process> processes, double ave_waiting_time, double ave_response_time, double ave_preemption) {
            this.ave_response_time = ave_response_time;
            this.ave_waiting_time = ave_waiting_time;
            this.ave_preemption = ave_preemption;
            this.processes = processes;
        }

        public void print() {
            // TODO: print final results
            System.out.println("\n------------------------------------------------------\nFinished tasks");
            System.out.println(this.processes.toString().replaceAll(", ", ""));
            System.out.printf("\nAverage response time: %.2f\nAverage wait time: %.2f\nAverage preemption time: %.2f", this.ave_response_time, this.ave_waiting_time, this.ave_preemption);
        }
    }
    /** User is expected to run file in two ways:
     1. With the number of programs to run (all other fields will be generated randomly)
     java CFSScheduler1 number_of_programs
     2. With the number of programs to run and the filename from which all information will be read
     java CFSScheduler number_of_programs file_name.txt

     File format expected to have:
     #process | priority | burst_time_ms | arrival_time_ms
     ------------------------------------------------------------- file file_name.txt
     1 	0 	325352 	1
     ...
     k 	0 	10036 	1339
     k+1 -10 12415 	1346
     ...
     n 	10 	141516 	25145
     -------------------------------------------------------------
     */
    public static void main(String args[]) throws IOException, NullPointerException
    {
        InputProcessor data = new InputProcessor();

        // first check number of arguments:
        // args.length == 0 -> ask user to type in the variables
        // args.length == 1 -> 1 case above
        // args.length == 2 -> 2 case above

        if ( args.length == 0 ) {
            data.readInput();
        } else if( args.length == 1 ) {
            data.generateInput(Integer.parseInt(args[0]));
        } else if ( args.length == 2 ) {
            data.readFile(Integer.parseInt(args[0]), args[1]);
        } else {
            throw new IOException("Incorrect input format");
        }

        MyRedBlackTree tree = new MyRedBlackTree();
        Statistics stats = schedule(data, tree);
        stats.print();
    }

    public static Statistics schedule(InputProcessor dataFromInput, MyRedBlackTree tree) {
        Process running_proc = null;
        int current_running_procs_count = 0, num_of_finished_procs = 0;
        double min_vruntime = 0.00, weight_sum = 0.00;

        // needed for future calculations
        long total_wait_time = 0;
        long total_response_time = 0;
        long total_preemption = 0;

        System.out.printf("Starting the scheduler with %d processes running in total for %d ms\n", dataFromInput.num_of_procs, dataFromInput.total_runtime);

        for( int curr_time = 0; num_of_finished_procs != dataFromInput.num_of_procs; curr_time++ ) {
            System.out.println("---------------------------------------------------------------------------------\nCPU TIME = " + curr_time);
            // 1. adding arrived processes to tree
            for( Process proc: dataFromInput.processes.getOrDefault(curr_time, new ArrayList<Process>())) {
                proc.vruntime = min_vruntime;
                weight_sum += roundTo(proc.getWeight(), 2);
                current_running_procs_count++;
                System.out.println("Adding process " + proc.getProcess_number() + " with vruntime = " + proc.vruntime + " and weight = " + proc.getWeight() + " to a tree");
                tree.insert(proc);
            }

            // 1.a. update process slice (running time for each execution)
            System.out.printf("\nUpdating slices of %d processes with total weight %.2f\n", current_running_procs_count, weight_sum);
            updateSlices(tree.getRoot(), weight_sum, current_running_procs_count);
            if ( running_proc != null ) {
                running_proc.slice = getSlice(running_proc.getWeight(), weight_sum, current_running_procs_count);
                System.out.println("Process "+running_proc.getProcess_number() + ": slice = " + running_proc.slice);
            }
            System.out.println("");

            // 2. if the running process has run its slice time
            if ( running_proc != null && running_proc.vruntime >= min_vruntime &&
                    running_proc.thisruntime >= sched_wakeup_granularity && running_proc.slice <= running_proc.thisruntime ) {
                System.out.println("Process "+running_proc.getProcess_number()+" finished its timeslice and inserted back to tree with vruntime = "+running_proc.vruntime );
                running_proc.lastpreempted = curr_time;
                running_proc.setIsBeingPreempted();
                tree.insert(running_proc);

                running_proc = null;
            }

            // 3. if no task is running, update variables
            if ( running_proc == null && tree.getRoot().getProc() != null ) {
                System.out.println("No task is running");
                running_proc = tree.delete(tree.getMin()).getProc();
                running_proc.setWait_time( (int) (curr_time - running_proc.lastpreempted) + running_proc.getWait_time() );
                running_proc.thisruntime = 0;

                if ( running_proc.getResponse_time() == -1 ) {
                    int response = curr_time - running_proc.getArrival_time();
                    running_proc.setResponse_time(response);
                    total_response_time += response;
                }

                if ( tree.getRoot().getProc() != null ) {
                    min_vruntime = tree.getMin().getProc().vruntime;
                    System.out.println("Updating min_vruntime to = "+ min_vruntime + " of proccess = " + tree.getMin().getProc().getProcess_number());
                }

                System.out.println("Executing process " + running_proc.getProcess_number() + " with response time = " + running_proc.getResponse_time() + " and slice = " + running_proc.slice);
            }

            // 4. For a task running -> update variables
            if ( running_proc != null ) {
                running_proc.thisruntime++;
                running_proc.truntime++;
                running_proc.vruntime = getVruntime(running_proc, weight_sum);
                System.out.println("Current running task is " + running_proc.getProcess_number() + " with runtime = " + running_proc.truntime + " and vruntime = " + running_proc.vruntime);

                // 4a. if the running process has finished running
                if( running_proc.truntime >= running_proc.getBurst_time() ) {
                    total_wait_time += running_proc.getWait_time();
                    current_running_procs_count--;
                    weight_sum -= roundTo(running_proc.getWeight(), 2);
                    System.out.println("Finished running " + running_proc.getProcess_number() + " process");
                    num_of_finished_procs++;
                    total_preemption += running_proc.getPreempted_count();
                    running_proc = null;
                }
            }
        }

        return new Statistics( dataFromInput.processes_list,(double) total_wait_time/dataFromInput.num_of_procs,
                (double) total_response_time/ dataFromInput.num_of_procs,
                (double) total_preemption/ dataFromInput.num_of_procs);
    }

    static void updateSlices(RBTNode node, double total_weight, int num_procs) {
        if( node == null ) {
            return;
        } else if ( node.getProc() != null ) {
            node.getProc().slice = getSlice(node.getProc().getWeight(), total_weight, num_procs);
            System.out.println("Process "+node.getProc().getProcess_number() + ": slice = " + node.getProc().slice);
            updateSlices(node.getLeft(), total_weight, num_procs);
            updateSlices(node.getRight(), total_weight, num_procs);
        }
    }
}
