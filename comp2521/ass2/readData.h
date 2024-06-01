// COMP2521 Assignment 2

// Written by: Aidan Tan (z5360925)
// Date: Started 1/11/22, Last modified 1/11/22

// The functions used in this file were inspired from How-to-implement-part-1
// https://cgi.cse.unsw.edu.au/~cs2521/22T3/assignments/ass2 

#include <assert.h>
#include <ctype.h>
#include <math.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "List.h"
#include "Graph.h"




// read from file "collection.txt" and add urls to a List, return list
List getUrlList(char *filename);

// create a new Adjacency Matrix graph and insert urls from collection into it
Graph getGraph(List l);

// find a url in the collection and return the id of the url found
int findUrlInList(List l, char *findUrl);

// insert urls into a graph
void insertUrls(FILE *file, Graph g, List l, Node curr);

// read from file "pageRankList.txt" and add urls and pageRanks to a List
List getUrlList2(void);


void incrNumSearchs(List l, char **searchTerms, char *ptr, 
                    int index, bool *match);

// read from file "invertedIndex.txt" and increment numSearchs
void getSearchTerms(List l, char **searchTerms, int length);

