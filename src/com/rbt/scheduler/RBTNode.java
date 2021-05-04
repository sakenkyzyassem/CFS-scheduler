package com.rbt.scheduler;

public class RBTNode {
    private final Process proc;
    private RBTNode parent;
    private RBTNode left;
    private RBTNode right;
    private boolean isBlack;

    public  RBTNode() {
        proc = null;
        isBlack = true;
    }

    public RBTNode(Process proc) {
        this.proc = proc;
        RBTNode nil = new RBTNode();
        this.parent = nil;
        this.left = nil;
        this.right = nil;
        this.isBlack = false;
    }

    // Getters

    public RBTNode getParent() {
        return parent;
    }

    public Process getProc() {
        return proc;
    }

    public RBTNode getLeft() {
        return left;
    }

    public RBTNode getRight() {
        return right;
    }

    public boolean isBlack() {
        return isBlack;
    }

    // Setters

    public void setParent(RBTNode parent) {
        this.parent = parent;
    }

    public void setBlack(boolean black) {
        isBlack = black;
    }

    public void setLeft(RBTNode left) {
        this.left = left;
    }

    public void setRight(RBTNode right) {
        this.right = right;
    }
}
