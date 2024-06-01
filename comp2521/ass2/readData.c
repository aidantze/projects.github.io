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

#define INIT_CAPACITY   100
#define SEARCH_CAPACITY 1000


// Read data from file given by filename and create a List of urls
// pos used for part3 only, to add the position of url in file to List
List getUrlList(char *filename) {
    List l = ListNew();
    FILE *file = fopen(filename, "r");
    if (file == NULL) {
        perror(filename);
        return NULL;
    }
    char s[INIT_CAPACITY];    // urls at most 100 characters in length
    while (fscanf(file, "%s", s) != EOF) {
        // last param of ListAppend for part3 only
        ListAppend(l, s, 0.0);
    }
    fclose(file);
    return l;
}


// find a url in the list and return its id
int findUrlInList(List l, char *findUrl) {
    for (Node curr = l->head; curr != NULL; curr = curr->next) {
        if (strcmp(curr->s, findUrl) == 0) {
            return curr->page;
        }
    }
    return -1;
}


// insert urls into graph g
void insertUrls(FILE *file, Graph g, List l, Node curr) {
    char t[INIT_CAPACITY];
    while (fscanf(file, "%s", t) != EOF) {
        // assume format for each url file is identical
        if (strcmp(t, "#start") == 0 || strcmp(t, "Section-1") == 0) { 
            continue;
        }
        else if (strcmp(t, "#end") == 0) { break; }
        if (strcmp(t, curr->s) == 0) { continue; }  // ignore self-edges
        
        int w = findUrlInList(l, t);    // check for existance
        if (w == -1) {
            fprintf(stderr, "error: url not found\n");
            exit(EXIT_FAILURE);
        }
        if (g->inLinks[curr->page][w] > 0) { continue; }    // ignore duplicates

        Edge e = (Edge){curr->page, w, 0.5, 0.5};
        GraphInsertEdge(g, e);
        g->inDegrees[w]++;
        g->outDegrees[curr->page]++;
    }
}


// Read data from each <url>.txt file, create graph and add vertices/edges
Graph getGraph(List l) {
    Graph g = GraphNew(l->size);
    Node curr = l->head;
    while (curr != NULL) {
        // urls at most 100 characters long, +4 for the appended ".txt"
        char openFile[INIT_CAPACITY + 4] = "";
        strcat(openFile, curr->s);
        strcat(openFile, ".txt");
        // open associated url.txt file, insert link between urls into graph
        FILE *file = fopen(openFile, "r");
        if (file == NULL) {
            perror(openFile);
            return NULL;
        }
        insertUrls(file, g, l, curr);
        fclose(file);
        curr = curr->next;
    }
    return g;
}


// Read data from file "pageRankList.txt" and construct a List of urls
List getUrlList2(void) {
    List l = ListNew();
    FILE *file = fopen("pageRankList.txt", "r");
    if (file == NULL) {
        perror("pageRankList.txt");
        return NULL;
    }
    char url[INIT_CAPACITY];      // tempoarily store urls in separate string
    char s[INIT_CAPACITY];        // urls at most 100 characters in length
    int i = 0;
    double pageRank = 0.0;
    while (fscanf(file, "%s", s) != EOF) {
        if (i == 0) {
            strcpy(url, s);
            i++;
        } else if (i == 1) { i++; } 
        else if (i == 2) {
            pageRank = atof(s);
            ListAppend(l, url, pageRank);
            i = 0;
        } else {
            fprintf(stderr, "error: invalid file format");
            exit(EXIT_FAILURE);
        }
    }
    l->head = insertionSort(l, compareUrls);
    fclose(file);
    return l;
}


void incrNumSearchs(List l, char **searchTerms, char *ptr, 
                    int index, bool *match) {
    while (ptr != NULL) {
        // increment index if we need to ignore a non-existing search term 
        char delim[] = " ";
        if (strcmp(ptr, searchTerms[index]) != 0) { 
            if (strcmp(ptr, searchTerms[index]) > 0) { index++; }
            break;
        }
        *match = true;
        ptr = strtok(NULL, delim);  // increment the pointer
        for (Node curr = l->head; curr != NULL; curr = curr->next) {
            if (strcmp(ptr, curr->s) == 0) {
                curr->numSearchs++;
                ptr = strtok(NULL, delim);
            }
            if (ptr == NULL) { break; }
        }
    }
}


/* Read data from file "invertexIndex.txt" and count frequency of matching
words for each url in List. Function uses lines strtok() to split a string
using whitespace. This method is not mine, and was copied and modified from
https://www.codingame.com/playgrounds/14213/
*/
void getSearchTerms(List l, char **searchTerms, int length) {
    FILE *file = fopen("invertedIndex.txt", "r");
    if (file == NULL) {
        perror("invertedIndex.txt");
        return;
    }

    char s[SEARCH_CAPACITY];       // lines at most 1000 characters in length
    char delim[] = " ";
    int index = 0;          // index for searchTerms, which is already sorted
    
    while (fgets(s, SEARCH_CAPACITY, file) != NULL) {
        bool match = false;     // if ptr matches searchTerms, increment index
        s[strcspn(s, "\n")] = 0;    // strip the newline char
	    char *ptr = strtok(s, delim);   // split string into words
        
        incrNumSearchs(l, searchTerms, ptr, index, &match);
        if (match) { index++; }
        if (index == length) { break; }
    }

    fclose(file);
}

