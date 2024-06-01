// This file was copied from COMP2521 lab 5.
// https://cgi.cse.unsw.edu.au/~cs2521/22T3/lab/5/questions
// It is an implementation of the String List ADT

// !!! DO NOT MODIFY THIS FILE !!!

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "List.h"

static Node newNode(List l, char *s, double pageRank);
static char *myStrdup(char *s);


////////////////////////////////////////////////////////////////////////

// Creates a new empty list
List ListNew(void) {
    List l = malloc(sizeof(*l));
    if (l == NULL) {
        fprintf(stderr, "error: out of memory\n");
        exit(EXIT_FAILURE);
    }

    l->head = NULL;
    l->tail = NULL;
    l->size = 0;
    return l;
}

// Frees all memory allocated for the given list
void ListFree(List l) {
    Node curr = l->head;
    while (curr != NULL) {
        Node temp = curr;
        curr = curr->next;
        free(temp->s);
        free(temp);
    }
    free(l);
}

// Adds a string to the end of the list
void ListAppend(List l, char *s, double pageRank) {
    Node n = newNode(l, s, pageRank);
    if (l->head == NULL) {
        l->head = n;
    } else {
        l->tail->next = n;
    }
    l->tail = n;
    l->size++;
}

static Node newNode(List l, char *s, double pageRank) {
    Node n = malloc(sizeof(*n));
    if (n == NULL) {
        fprintf(stderr, "error: out of memory\n");
        exit(EXIT_FAILURE);
    }
    
    n->s = myStrdup(s);
    n->pageRank = pageRank;
    n->page = l->size;
    n->numSearchs = 0;
    n->pos = 0;
    n->next = NULL;
    return n;
}

static char *myStrdup(char *s) {
    char *copy = malloc((strlen(s) + 1) * sizeof(char));
    if (copy == NULL) {
        fprintf(stderr, "error: out of memory\n");
        exit(EXIT_FAILURE);
    }
    return strcpy(copy, s);
}

// Returns the number of items in the list
int  ListSize(List l) {
    return l->size;
}


// The pseudocode for the following functions was taken from:
// https://www.enjoyalgorithms.com/blog/sort-linked-list-using-insertion-sort
// It implements the insertion sort algorithm to the List ADT
// It has been modified to use function pointers for generic type comparisons

// alphabetical order
bool compareUrls(Node a, Node b) { return strcmp(a->s, b->s) >= 0; }
// pageRank descending, then alphabetical order
bool comparePageRanks(Node a, Node b) { 
    if (a->pageRank != b->pageRank) { return a->pageRank<b->pageRank; }
    return compareUrls(a, b);
}
// numSearchTerms descending, then pageRank descending, then alphabetical order
bool compareNumSearchTerms(Node a, Node b) {
    if (a->numSearchs != b->numSearchs) { return a->numSearchs<b->numSearchs; }
    return comparePageRanks(a, b);
}


Node sortedInsert(Node sorted, Node new_node, bool (compare)(Node a, Node b)) {
    // special case for the head end
    if (sorted == NULL || compare(sorted, new_node)) {
        new_node->next = sorted;
        sorted = new_node;
    } else {
        Node curr = sorted;
        // locate the node before the point of insertion
        while (curr->next != NULL && !compare(curr->next, new_node)) {
            curr = curr->next;
        }
        new_node->next = curr->next;
        curr->next = new_node;
    }
    return sorted;
}
  

Node insertionSort(List l, bool (compare)(Node a, Node b)) {
    Node sorted = NULL;
    Node curr = l->head;
    while (curr != NULL) {
        Node next = curr->next;
        sorted = sortedInsert(sorted, curr, compare);
        curr = next;
    }
    return sorted;
}


// Prints the list, one string per line
// If the strings themselves contain newlines, too bad
void ListPrint(List l) {
    for (Node n = l->head; n != NULL; n = n->next) {
        printf("%s\n", n->s);
    }
}

