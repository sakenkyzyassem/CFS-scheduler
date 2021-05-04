package com.rbt.scheduler;

import java.io.PrintStream;

public class MyRedBlackTree {

    private RBTNode nil;
    private RBTNode root;
    protected RBTNode minNode;

    public MyRedBlackTree() {
        nil = new RBTNode();
        this.root = nil;
        this.minNode = nil;
    }

    public MyRedBlackTree(RBTNode root) {
        this.root = root;
        this.minNode = this.root;
    }

    public RBTNode getRoot() {
        return this.root;
    }

    public RBTNode getMin() {
        return this.minNode;
    }

    public boolean insert(Process node) { // return true on success; else false
        if ( node == null )
            throw new NullPointerException("Cannot insert null process");

        RBTNode temp = insert(node, this.root);
        if ( isNil(temp) ) {
            return false;
        } else {
            this.root.setBlack(true);
            if ( isNil(this.minNode) || temp.getProc().vruntime <= this.minNode.getProc().vruntime ) {
                this.minNode = temp;
            }
            return true;
        }
    }

    public  RBTNode delete(Process proc) {
        return delete(searchNode(proc));
    }

    public RBTNode delete(RBTNode node) { // return true on success; else false
        if( isNil(node) || node == null )
            throw new NullPointerException("Cannot delete null data");
        RBTNode nodeToBeDeleted = node, toReplace = nil;
        boolean initiallyBlack = node.isBlack();

        if ( isNil(node.getLeft()) ) {
            toReplace = node.getRight();
            assignParent(node, node.getRight());
        } else if ( isNil(node.getRight()) ) {
            toReplace = node.getLeft();
            assignParent(node, node.getLeft());
        } else {
            nodeToBeDeleted = findMin(node.getRight());
            initiallyBlack = nodeToBeDeleted.isBlack();

            toReplace = nodeToBeDeleted.getRight();
            if( nodeToBeDeleted.getParent() != node ) {
                assignParent(nodeToBeDeleted, nodeToBeDeleted.getRight());
                nodeToBeDeleted.setRight(node.getRight());
                nodeToBeDeleted.getRight().setParent(nodeToBeDeleted);
            } else {
                toReplace.setParent(nodeToBeDeleted);
            }
            assignParent(node, nodeToBeDeleted);
            nodeToBeDeleted.setLeft(node.getLeft());
            nodeToBeDeleted.getLeft().setParent(nodeToBeDeleted);
            nodeToBeDeleted.setBlack(node.isBlack());
        }
        if ( initiallyBlack ) {
            deletionFix(toReplace);
        }
        if ( node == this.minNode ) {
            this.minNode = findMin(this.root);
        }
        return nodeToBeDeleted;
    }

    public void print() {
        PrintStream os = System.out;
        StringBuilder sb = new StringBuilder();
        traversePreOrder(sb, "", "", this.root);
        os.println("---------------------------Tree-------------------------------");
        os.print(sb.toString());
        os.println("----------------------------------------------------------");
    }


    private boolean isNil(RBTNode node) {
        return node.getProc() == null && node.isBlack();
    }

    private void leftRotate(RBTNode node) {
        RBTNode right = node.getRight();
        right.setParent(node.getParent());

        node.setRight(right.getLeft());
        if ( !isNil(right.getLeft()) ) {
            right.getLeft().setParent(node);
        }

        if ( isNil(node.getParent()) ) {
            this.root = right;
        } else if ( node == node.getParent().getLeft() ) {
            node.getParent().setLeft(right);
        } else {
            node.getParent().setRight(right);
        }

        right.setLeft(node);
        node.setParent(right);
    }

    private void rightRotate(RBTNode node) {
        RBTNode left = node.getLeft();
        left.setParent(node.getParent());

        node.setLeft(left.getRight());
        if ( !isNil(left.getRight()) ) {
            left.getRight().setParent(node);
        }

        if ( isNil(node.getParent()) ) {
            this.root = left;
        } else if ( node == node.getParent().getRight() ) {
            node.getParent().setRight(left);
        } else {
            node.getParent().setLeft(left);
        }

        left.setRight(node);
        node.setParent(left);
    }

    private void insertionFix(RBTNode node) {
        // Further need to recolor or rotate the tree to remain balanced. 4 Cases possible:
        // CASE 0: node is root -> no need for action as root will always be colored black

        while ( !node.getParent().isBlack() && node.getParent() != this.root ) {
            RBTNode uncle, grandparent = node.getParent().getParent(), parent = node.getParent();
            if ( parent == grandparent.getRight() ) {
                uncle = grandparent.getLeft();
            } else {
                uncle = grandparent.getRight();
            }

            // CASE 1: node's uncle is RED -> recolor parent, grandparent and uncle
            if ( !uncle.isBlack() ) {
                parent.setBlack(true);
                grandparent.setBlack(false);
                uncle.setBlack(true);
                node = grandparent;
            }
            // CASE 2: node's uncle is BLACK and has a triangle relationship
            else if ( uncle == grandparent.getLeft() && node == parent.getLeft() ) {
                node = parent;
                rightRotate(node);
                parent = node.getParent();
                parent.setBlack(true);
                grandparent.setBlack(false);
                leftRotate(grandparent);
            } else if ( uncle == grandparent.getRight() && node == parent.getRight() ){
                node = parent;
                leftRotate(node);
                parent = node.getParent();
                parent.setBlack(true);
                grandparent.setBlack(false);
                rightRotate(grandparent);
            }
            // CASE 3: node's uncle is BLACK and has a line relationship
            else if ( node == parent.getRight() && uncle == grandparent.getLeft() ) {
                parent.setBlack(true);
                grandparent.setBlack(false);
                leftRotate(grandparent);
            } else if ( node == parent.getLeft() && uncle == grandparent.getRight() ) {
                parent.setBlack(true);
                grandparent.setBlack(false);
                rightRotate(grandparent);
            }
        }
    }

    private RBTNode insert(Process proc, RBTNode root) {
        if ( isNil(root) ) {
            this.root = new RBTNode(proc);
            return this.root;
        }
        RBTNode temp = root, parent = nil;

        while( !isNil(temp) ) {
            parent = temp;

            int comparator = temp.getProc().compareTo(proc);

            if ( temp.getProc().getProcess_number() == proc.getProcess_number() ) {
                return nil;
            } else if ( comparator >= 0 ) { // root is greater than the node to be inserted -> insert to left
                temp = temp.getLeft();
            } else { // root is less or equal to the node to be inserted -> insert to the right
                temp = temp.getRight();
            }
            // NOTE: as there might be multiple processes with the same priority,
            // I am not going to ignore nodes with the same vruntime, but insert them to the right subtree
        }
        // Found a place where to insert the process_node
        RBTNode node = new RBTNode(proc);
        node.setParent(parent);

        if ( parent.getProc().compareTo(node.getProc()) >= 0 ) {
            parent.setLeft(node);
        } else {
            parent.setRight(node);
        }

        insertionFix(node);

        return node;
    }

    private RBTNode searchNode(Process proc) {
        RBTNode temp = this.root;

        while( !isNil(temp) ) {
            if( temp.getProc().getProcess_number() == proc.getProcess_number() ) {
                return temp;
            } else if ( temp.getProc().compareTo(proc) > 0 ) {
                temp = temp.getLeft();
            } else if ( temp.getProc().compareTo(proc) < 0 ) {
                temp = temp.getRight();
            } else { // if thier vruntimes are the same the node can be either left or right child, so further search by procNumber
                temp = searchByProcNumber(temp, proc);
            }
        }

        return nil;
    }

    private RBTNode searchByProcNumber(RBTNode node, Process proc) {
        if ( node.getProc().getProcess_number() == proc.getProcess_number() ) {
            return node;
        }

        if( node.getProc().compareTo(proc) != 0 ) {
            return nil;
        } else {
            RBTNode left = searchByProcNumber(node.getLeft(), proc);
            RBTNode right = searchByProcNumber(node.getRight(), proc);

            if( isNil(right) ) {
                return left;
            } else {
                return right;
            }
        }
    }

    private RBTNode assignParent(RBTNode target, RBTNode node) {
        if ( isNil(target.getParent()) ) {
            this.root = node;
        } else if ( target == target.getParent().getLeft() ) {
            target.getParent().setLeft(node);
        } else {
            target.getParent().setRight(node);
        }
        node.setParent(target.getParent());
        return node;
    }

    private RBTNode findMin(RBTNode node) {
        if ( isNil(node) ) {
            return nil;
        }
        while ( !isNil(node.getLeft()) ) {
            node = node.getLeft();
        }
        return node;
    }

    private void deletionFix(RBTNode node) {
        while ( node != this.root && node.isBlack() ) {
            RBTNode sibling;
            if (node == node.getParent().getLeft()) {
                sibling = node.getParent().getRight();

                if( isNil(sibling) ) {
                    node.setBlack(true);
                    node = node.getParent();
                    continue;
                }

                // CASE1: sibling is RED
                if (!sibling.isBlack()) {
                    sibling.setBlack(true);
                    node.getParent().setBlack(false);
                    leftRotate(node.getParent());
                    sibling = node.getParent().getRight();
                }

                //CASE2: Sibling is BLACK and both children are BLACK
                if (sibling.getLeft().isBlack() && sibling.getRight().isBlack()) {
                    sibling.setBlack(false);
                    node = node.getParent();
                    continue;
                }
                // CASE3: sibling is black and right child is BLACK
                else if (sibling.getRight().isBlack()) {
                    sibling.getLeft().setBlack(true);
                    sibling.setBlack(false);
                    rightRotate(sibling);
                    sibling = node.getParent().getRight();
                }
                // CASE4: sibling is BLACK and right is RED
                if (!sibling.getRight().isBlack()) {
                    sibling.setBlack(node.getParent().isBlack());
                    node.getParent().setBlack(true);
                    sibling.getRight().setBlack(true);
                    leftRotate(node.getParent());
                    node = this.root;
                }
            } else {
                sibling = node.getParent().getLeft();

                if( isNil(sibling) ) {
                    node.setBlack(true);
                    node = node.getParent();
                    continue;
                }

                // CASE1: sibling is RED
                if (!sibling.isBlack()) {
                    sibling.setBlack(true);
                    node.getParent().setBlack(false);
                    rightRotate(node.getParent());
                    sibling = node.getParent().getLeft();
                }

                //CASE2: Sibling is BLACK and both children are BLACK
                if (sibling.getRight().isBlack() && sibling.getLeft().isBlack()) {
                    sibling.setBlack(false);
                    node = node.getParent();
                    continue;
                } // CASE3: sibling is black and left child is BLACK
                else if (sibling.getLeft().isBlack()) {
                    sibling.getRight().setBlack(true);
                    sibling.setBlack(false);
                    leftRotate(sibling);
                    sibling = node.getParent().getLeft();
                }
                // CASE4: sibling is BLACK and left is RED
                if (!sibling.getLeft().isBlack()) {
                    sibling.setBlack(node.getParent().isBlack());
                    node.getParent().setBlack(true);
                    sibling.getLeft().setBlack(true);
                    rightRotate(node.getParent());
                    node = this.root;
                }
            }
        }
    }

    private void traversePreOrder(StringBuilder sb, String padding, String pointer, RBTNode node) {
        if ( !isNil(node) ) {
            sb.append(padding);
            sb.append(pointer);
            sb.append(node.getProc().getProcess_number()+" ["+node.getProc().vruntime+"] ");
            sb.append("\n");

            StringBuilder paddingBuilder = new StringBuilder(padding);
            paddingBuilder.append("│  ");

            String paddingForBoth = paddingBuilder.toString();
            String pointerForRight = "└──";
            String pointerForLeft = ( !isNil(node.getRight()) ) ? "├──" : "└──";

            traversePreOrder(sb, paddingForBoth, pointerForLeft, node.getLeft());
            traversePreOrder(sb, paddingForBoth, pointerForRight, node.getRight());
        }
    }
}
