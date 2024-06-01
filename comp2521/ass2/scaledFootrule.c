// COMP2521 Assignment 2

// Written by: Aidan Tan (z5360925)
// Date: Started 16/11/22, Last modified 18/11/22

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

#define MAX_SFD 99.9


void printScaledFootrule(List l, int *minPerm, double minSFD, int length);
void updateLists(List *list, List unionList, int length);
List getUnionList(List *listArray, int length);
int *getUnionArray(List l);

void swap(int *x, int *y);
int updatePagePos(Node c1, List unionList, int count);
int getUrlPos(List *list, Node unionListNode, int index);
void scaledFootrule(List *list, List l, int *perm, int *minPerm, 
                    double *minSFD, int length, int lo, int hi);


int main(int argc, char *argv[]) {
    
    // create an array of Lists storing urls
    // each array is a list representing a unique ranking, imported from a file
    int length = argc - 1;
    List *list = malloc(length * sizeof(List));
    for (int i = 1; i < argc; i++) {
        list[i-1] = getUrlList(argv[i]);
    }

    List unionList = getUnionList(list, length);
    updateLists(list, unionList, length);
    int permLength = unionList->size;
    int *perm = getUnionArray(unionList);
    int *minPerm = calloc(permLength, sizeof(int));
    double minSFD = MAX_SFD;     // placeholder value
    
    scaledFootrule(list, unionList, perm, minPerm, &minSFD, length, 0, permLength);
    printScaledFootrule(unionList, minPerm, minSFD, permLength);

    // free all allocated memory
    for (int i = 0; i < length; i++) {
        ListFree(list[i]);
    }
    free(list);
    ListFree(unionList);
    free(perm);
    free(minPerm);
    return 0;
}


// print the total scaled footrule distance and urls in minPerm order
void printScaledFootrule(List l, int *minPerm, double minSFD, int length) {
    printf("%.7lf\n", minSFD);
    for (int i = 0; i < length; i++) {
        for (Node curr = l->head; curr != NULL; curr = curr->next) {
            if (curr->page == minPerm[i]) {
                printf("%s\n", curr->s);
                break;
            }
        }
    }
}



int updatePagePos(Node c1, List unionList, int count) {
    int j = count;
    for (Node c2 = unionList->head; c2 != NULL; c2 = c2->next) {
        if (strcmp(c1->s, c2->s) == 0) {
            c1->page = c2->page;
            c1->pos = j++;
        }
    }
    return j;
}


// update original lists so the ids all match, but the positions don't
void updateLists(List *list, List unionList, int length) {
    for (int i = 0; i < length; i++) {
        int j = 1;
        for (Node c1 = list[i]->head; c1 != NULL; c1 = c1->next) {
            // additional function to prevent large indentation levels
            j = updatePagePos(c1, unionList, j);
        }
    }
}


// unionList will map urls to pageids
List getUnionList(List *listArray, int length) {
    List l = ListNew();
    bool found = false;
    for (int i = 0; i < length; i++) {
        // get each node in List and add to array
        for (Node c1 = listArray[i]->head; c1 != NULL; c1 = c1->next) {
            // check the id does not already exist in the List
            for (Node c2 = l->head; c2 != NULL; c2 = c2->next) {
                if (c2->page == c1->page) { found = true; break; }
                if (c2->next == NULL) { break; }
            }
            if (found) { found = false; break; }
            ListAppend(l, c1->s, 0.0);
        }
    }
    // sort the list alphabetically
    l->head = insertionSort(l, compareUrls);
    // the give each url in the list an id from 1 to l->size
    int j = 1;
    for (Node curr = l->head; curr != NULL; curr = curr->next) {
        curr->page = j;
        curr->pos = j;
        j++;
    }
    return l;
}


// unionArray will be used to generate permutations of positions
int *getUnionArray(List l) {
    // length of this array is same as length of list l which is length
    int *perm = calloc(l->size, sizeof(int));
    int i = 0;
    for (Node curr = l->head; curr != NULL; curr = curr->next) {
        perm[i++] = curr->pos;
    }
    return perm;
}


// swap two elements of an array around
void swap(int *x, int *y) {
    int temp = *x;
    *x = *y;
    *y = temp;
}


// get the url position in list l
int getUrlPos(List *list, Node unionListNode, int index) {
    int pos = -1;
    for (Node curr = list[index]->head; curr != NULL; curr = curr->next) {
        if (strcmp(unionListNode->s, curr->s) == 0) { 
            pos = curr->pos; 
            break; 
        }
    }
    return pos;
}


/* recursive function to calculate total scaled footrule distance (SFD)
- returns minSFD, the minimum scaled footrule distance
- list is an array of lists of url rankings, length is length of this array
- l stores urls in alphabetical order
- minPerm is an array storing the permutation that gives minSFD
- lo and hi are recursion counters and limits to trigger base case
*/
void scaledFootrule(List *list, List l, int *perm, int *minPerm, 
                    double *minSFD, int length, int lo, int hi) {
    if (lo != hi) {     // recursive case: generate permutations of urls
        for (int i = lo; i < hi; i++) {
            swap(perm + lo, perm + i);      // swap two array elements
            scaledFootrule(list, l, perm, minPerm, minSFD, length, lo + 1, hi);
            swap(perm + lo, perm + i);      // swap them back
        }
        return;
    }
    // base case: we have a permutation, now calculate SFD
    double totalSFD = 0.0;
    int j = 0;
    for (Node c1 = l->head; c1 != NULL; c1 = c1->next) {
        for (int i = 0; i < length; i++) {
            int pos = getUrlPos(list, c1, i);
            if (pos == -1) { break; }
            double scaledFootruleDistance = fabs(((double)pos / 
                                                (double)list[i]->size) - 
                                                ((double)perm[j] / 
                                                (double)hi));
            totalSFD += scaledFootruleDistance;
        } 
        j++;
    }
    // update minSFD if the scaled footrule distance is lowest
    if (totalSFD < *minSFD) {
        *minSFD = totalSFD;
        for (int i = 0; i < hi; i++) { minPerm[i] = perm[i]; }
    }
}

