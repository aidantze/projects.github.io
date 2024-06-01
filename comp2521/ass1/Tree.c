// Implementation of the Tree ADT

#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "List.h"
#include "Record.h"
#include "Tree.h"

// constants
#define MAX_DAY         6
#define MAX_HOUR        23
#define MAX_MIN         59
#define MAX_DURATION    60*24

typedef struct node *Node;
struct node {
    Record  rec;
    Node    left;
    Node    right;

    // IMPORTANT: Do not modify the fields above
    // You may add additional fields below if necessary
    int     height;     // used to store height of node

};

struct tree {
    Node root;
    int (*compare)(Record, Record);

    // IMPORTANT: Do not modify the fields above
    // You may add additional fields below if necessary
};

static void doTreeFree(Node n, bool freeRecords);

Node new_node(Record rec);
Node do_tree_insert(Tree t, Node n, Record rec, bool *res);

Node rotate_left(Node n1);
Node rotate_right(Node n2);
int get_height(Node n);

Record do_tree_search(Tree t, Node n, Record rec);
void do_tree_search_between(Tree t, Node n, Record lower, Record upper, List l);

Node do_tree_next(Tree t, Node n, Record lower);


////////////////////////////////////////////////////////////////////////
// Provided functions
// !!! DO NOT MODIFY THESE FUNCTIONS !!!

Tree TreeNew(int (*compare)(Record, Record)) {
    Tree t = malloc(sizeof(*t));
    if (t == NULL) {
        fprintf(stderr, "error: out of memory\n");
        exit(EXIT_FAILURE);
    }

    t->root = NULL;
    t->compare = compare;
    return t;
}

void TreeFree(Tree t, bool freeRecords) {
    doTreeFree(t->root, freeRecords);
    free(t);
}

static void doTreeFree(Node n, bool freeRecords) {
    if (n != NULL) {
        doTreeFree(n->left, freeRecords);
        doTreeFree(n->right, freeRecords);
        if (freeRecords) {
            RecordFree(n->rec);
        }
        free(n);
    }
}

////////////////////////////////////////////////////////////////////////
// Functions you need to implement

bool TreeInsert(Tree t, Record rec) {
    bool res = false;  // reports whether record was inserted successfully
    t->root = do_tree_insert(t, t->root, rec, &res);
    return res;
}


// The pseudocode for this program was taken from
// https://www.cse.unsw.edu.au/~cs2521/lecs/avl/slides.html
// it inserts nodes into an AVL tree recursively and rebalances the tree
Node do_tree_insert(Tree t, Node n, Record rec, bool *res) {
    if (n == NULL) {
        *res = true;
        return new_node(rec);
    }

    int cmp = t->compare(n->rec, rec);
    if (cmp > 0) { n->left = do_tree_insert(t, n->left, rec, res); }
    else if (cmp < 0) { n->right = do_tree_insert(t, n->right, rec, res); }
    else { return n; }

    // perform height rebalancing via rotations: there are 4 possible cases
    int left_height = get_height(n->left);
    int right_height = get_height(n->right);
    if (left_height - right_height > 1) {
        int left_cmp = t->compare(n->left->rec, rec);
        if (left_cmp > 0) {         // left-right case: rotate left then right
            n->left = rotate_left(n->left);
        }
        n = rotate_right(n);        // left-left case: rotate right
    } else if (right_height - left_height > 1) {
        int right_cmp = t->compare(n->right->rec, rec);
        if (right_cmp < 0) {        // right-left case: rotate right then left
            n->right = rotate_right(n->right);
        }
        n = rotate_left(n);         // right-right case: rotate left
    }
    return n;
}


// helper function: create a new node and return it
Node new_node(Record rec) {
    Node new = malloc(sizeof(*new));
    if (new == NULL) {
        fprintf(stderr, "error: out of memory\n");
        exit(EXIT_FAILURE);
    }
    new->left = NULL;
    new->right = NULL;
    new->rec = rec;
    new->height = 0;
    return new;
}

// helper function: get the current height of a node and return it
int get_height(Node n) {
    if (n == NULL) { return 0; }

    int n1_height = n->left == NULL ? 0 : n->left->height;
    int n2_height = n->right == NULL ? 0 : n->right->height;
    return n1_height > n2_height ? n1_height : n2_height;
}

// helper function: The pseudocode for the below functions was taken from
// https://www.cse.unsw.edu.au/~cs2521/lecs/trees2/slides.html
// It rotates nodes in an AVL tree either left or right, and updates its height
Node rotate_left(Node n1) {
    if (n1 == NULL || n1->left == NULL) {
        return n1;
    }
    Node n2 = n1->right;
    Node tmp = n2->left;
    n2->left = n1;
    n1->right = tmp;

    // update height
    n1->height = get_height(n1);
    n2->height = get_height(n2);

    return n2;
}

Node rotate_right(Node n2) {
    if (n2 == NULL || n2->left == NULL) {
        return n2;
    }
    Node n1 = n2->left;
    Node tmp = n1->right;
    n1->right = n2;
    n2->left = tmp;

    // update height
    n1->height = get_height(n1);
    n2->height = get_height(n2);

    return n1;
}


Record TreeSearch(Tree t, Record rec) {
    return do_tree_search(t, t->root, rec);
}


// helper function: recursively search for nodes within the AVL tree
Record do_tree_search(Tree t, Node n, Record rec) {
    if (n == NULL) { return NULL; }
    int cmp = t->compare(n->rec, rec);
    if (cmp > 0) { return do_tree_search(t, n->left, rec); } 
    else if (cmp < 0) { return do_tree_search(t, n->right, rec); }
    else { return n->rec; }
}


List TreeSearchBetween(Tree t, Record lower, Record upper) {
    List record_list = ListNew();
    if (t->compare(lower, upper) > 0) {
        // search from lower to end of week, then start of week to upper
        Record dummy1 = RecordNew("", "", "",
                                    MAX_DAY, MAX_HOUR, MAX_MIN, MAX_DURATION);
        Record dummy2 = RecordNew("", "", "", 0, 0, 0, 0);
        do_tree_search_between(t, t->root, lower, dummy1, record_list);
        do_tree_search_between(t, t->root, dummy2, upper, record_list);
        RecordFree(dummy1);
        RecordFree(dummy2);
    } else {
        // search from lower to upper as normal
        do_tree_search_between(t, t->root, lower, upper, record_list);
    }
    return record_list;
}


// helper function: search for all records between lower and upper, return list
void do_tree_search_between(Tree t, Node n, Record lower,
                                Record upper, List l) {
    if (n == NULL) { return; }

    int lowerCompare = t->compare(n->rec, lower);
    int upperCompare = t->compare(n->rec, upper);
    // perform in-order traversal: left subtree, then root, then right subtree
    if (lowerCompare > 0) {
        do_tree_search_between(t, n->left, lower, upper, l); 
    }
    if (lowerCompare >= 0 && upperCompare <= 0) { 
        ListAppend(l, n->rec); 
    }
    if (upperCompare < 0) {
        do_tree_search_between(t, n->right, lower, upper, l); 
    }
}


Record TreeNext(Tree t, Record rec) {
    // initial base case makes implementing do_tree_next() easier 
    if (t == NULL) { return NULL; }
    //bool record_not_exist = false;  // account for > in search using this var
    Node n = do_tree_next(t, t->root, rec);
    if (n == NULL) {
        // search wraps around to the next week: search from start of tree
        Record dummy = RecordNew(RecordGetFlightNumber(rec), "", "", 
                                0, 0, 0, 0);
        Node n1 = do_tree_next(t, t->root, dummy);
        RecordFree(dummy);
        return n1->rec;
    }
    return n->rec;
}


// helper function: find the first record >= given record and return the node
Node do_tree_next(Tree t, Node n, Record lower) {
    int cmp = t->compare(n->rec, lower);
    if (cmp == 0) { return n; }
    else if (cmp > 0) {
        if (n->left == NULL) { return n; }
        Node n1 = do_tree_next(t, n->left, lower);
        // cannot use strcmp since we cannot import additional libraries!
        if (n1 == NULL && strcmp(RecordGetFlightNumber(n->rec), 
                                RecordGetFlightNumber(lower)) == 0) { 
            return n;
        }
        return n1;
        
    } else {
        if (n->right == NULL) { return NULL; }
        return do_tree_next(t, n->right, lower);
    }
}


////////////////////////////////////////////////////////////////////////
