// This file was copied from COMP2521 lab 5.
// https://cgi.cse.unsw.edu.au/~cs2521/22T3/lab/5/questions
// It provides an interface to the String List ADT

#ifndef LIST_H
#define LIST_H

#include <stdbool.h>

typedef struct node *Node;
struct node {
    char   *s;              // all: full url string
    double  pageRank;       // part1 part2: stores pageRank for given url
    int     page;           // part1: stores the node id which is vertex of graph
    int     numSearchs;     // part2: stores number of matching search terms
    int     pos;            // part3: stores the original position of url in file
    Node    next;
};

typedef struct list *List;
struct list {
    Node   head;
    Node   tail;
    int    size;
};

// Creates a new empty list
// Complexity: O(1)
List ListNew(void);

// Frees all memory allocated for the given list
// Complexity: O(n)
void ListFree(List l);

// Adds a string to the end of the list. Makes a copy of the string
// before adding it to the list.
// Complexity: O(1)
void ListAppend(List l, char *s, double pageRank);

// Returns the number of items in the list
// Complexity: O(1)
int  ListSize(List l);

// Comparison functions used as function pointers to compare two types in sortedInsert
bool compareUrls(Node a, Node b);               // alphabetical order
bool comparePageRanks(Node a, Node b);          // pageRank descending order
bool compareNumSearchTerms(Node a, Node b);     // numSearchTerms descending order

// Sort list using insertion sort, specify field to sort using function pointers
Node sortedInsert(Node sorted, Node new_node, bool (compare)(Node a, Node b));
Node insertionSort(List l, bool (compare)(Node a, Node b));

// Prints the list, one string per line
// If the strings themselves contain newlines, too bad
void ListPrint(List l);


#endif
