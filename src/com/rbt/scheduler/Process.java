package com.rbt.scheduler;

public class Process implements Comparable<Process>{
    protected long truntime;
    protected long thisruntime;
    protected long lastpreempted;
    protected double vruntime;
    protected double slice;
    private double weight;
    private final int process_number;
    private final int priority;
    private final int burst_time; // in microseconds
    private final int arrival_time; // in microseconds
    private int wait_time; // in microseconds
    private int response_time; // in microseconds
    private int preempted_count;

    public Process( int id, int priority, int burst_time, int arrival_time) {
        this.truntime = 0;
        this.thisruntime = 0;
        this.vruntime = 0;
        this.preempted_count = 0;

        this.process_number = id;
        this.priority = priority;
        this.burst_time = burst_time;
        this.arrival_time = arrival_time;

        this.response_time = -1;
        this.lastpreempted = 0;

        this.weight = calcWeight(this.priority);
    }

    public Process( String id, String priority, String burst_time, String arrival_time) {
        this.vruntime = 0;
        this.preempted_count = 0;
        this.truntime = 0;
        this.thisruntime = 0;

        this.process_number = Integer.parseInt(id);
        this.priority = Integer.parseInt(priority);
        this.burst_time = Integer.parseInt(burst_time);
        this.arrival_time = Integer.parseInt(arrival_time);

        this.response_time = -1;
        this.lastpreempted = this.arrival_time;

        this.weight = calcWeight(this.priority);
    }

    private double calcWeight(int priority) {
        return (double) ((int)(Math.pow(2, 10) * Math.pow(1.25, -priority) * 100)) / 100;
    }

    // Setters
    public void setResponse_time(int response_time) {
        this.response_time = response_time;
    }

    public void setWait_time(int wait_time) {
        this.wait_time = wait_time;
    }

    public void setIsBeingPreempted() {
        this.preempted_count += 1;
    }

    // Getters
    public int getProcess_number() {
        return process_number;
    }

    public double getWeight() {
        return this.weight;
    }

    public int getArrival_time() {
        return arrival_time;
    }

    public int getBurst_time() {
        return burst_time;
    }

    public int getResponse_time() {
        return response_time;
    }

    public int getWait_time() {
        return wait_time;
    }

    public int getPreempted_count() {
        return preempted_count;
    }

    @Override
    public String toString() {
        return "\nProcess {" +
                "process_number=" + process_number +
                "   priority=" + priority +
                "   burst_time=" + burst_time +
                "   arrival_time=" + arrival_time +
                "   weight=" + weight +
                "   wait_time=" + wait_time +
                "   response_time=" + response_time +
                "   preempted_count=" + preempted_count +
                "}";
    }

    @Override
    public int compareTo(Process o) {
        return (int) (this.vruntime*100 - o.vruntime*100);
    }
}