// COMP2521 Assignment 2

// Written by: Aidan Tan (z5360925)
// Date: Started 14/11/22, Last modified 16/11/22

#include <assert.h>
#include <ctype.h>
#include <math.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "List.h"
#include "Graph.h"
#include "readData.h"


static int qsortStrcmp(const void *ptr1, const void *ptr2);
void printNumSearchTerms(List l);
void sortNumSearchTerms(List l);


int main(int argc, char *argv[]) {

    if (argc < 1) {
        fprintf(stderr, "Usage: %s search_terms...\n",
                argv[0]);
        return EXIT_FAILURE;
    }

    // store all search terms and number of search terms in separate arrays
    char **searchTerms = malloc((argc - 1) * sizeof(char *));
    for (int i = 1; i < argc; i++) {
        searchTerms[i - 1] = argv[i];
    }
    // sort the searchTerms array in alphabetical order
    qsort(searchTerms, argc - 1, sizeof(char *), qsortStrcmp);

    // get number of matching search terms for each node
    List urlList = getUrlList2();
    getSearchTerms(urlList, searchTerms, argc - 1);

    // sort and print
    sortNumSearchTerms(urlList);

    ListFree(urlList);
    free(searchTerms);
    return 0;
}


// The function below was taken from COMP2521 lab05
// https://cgi.cse.unsw.edu.au/~cs2521/22T3/lab/5/questions
// It is used in the qsort function to sort an array alphabetically
static int qsortStrcmp(const void *ptr1, const void *ptr2) {
    char *s1 = *(char **)ptr1;
    char *s2 = *(char **)ptr2;
    return strcmp(s1, s2);
}


// print output in descending order by weighted pagerank
void printNumSearchTerms(List l) {
    for (Node curr = l->head; curr != NULL; curr = curr->next) {
        if (curr->numSearchs == 0) { continue; }
        printf("%s\n", curr->s);
    }
}


// sort by numSearchs, using insertion sort algorithm on linked list
// then print relevant contents in list to the user
void sortNumSearchTerms(List l) {
    l->head = insertionSort(l, compareNumSearchTerms);
    printNumSearchTerms(l);
}


