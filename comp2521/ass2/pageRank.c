// COMP2521 Assignment 2

// Written by: Aidan Tan (z5360925)
// Date: Started 1/11/22, Last modified 14/11/22

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


void printPageRank(Graph g, List l);
void sortPageRank(Graph g, List l);

double incrSumSet(Graph g, Node curr);
void weightedPageRank(Graph g, List l, double d, 
                    double diffPR, int maxIterations);

double inWeight(Graph g, int v, int u);
double inWeightReferenceList(Graph g, int v);
double outWeight(Graph g, int v, int u);
double outWeightReferenceList(Graph g, int v);
void updateWeights(Graph g);


int main(int argc, char *argv[]) {
    if (argc != 4) {
        fprintf(stderr, "Usage: %s dampingFactor diffPR maxIterations\n",
                argv[0]);
        return EXIT_FAILURE;
    }

    double d = atof(argv[1]);
    double diffPR = atof(argv[2]);
    int maxIterations = atoi(argv[3]);

    // create list of urls, insert urls into graph, and calculate weights
    List urlList = getUrlList("collection.txt");
    Graph urlGraph = getGraph(urlList);
    updateWeights(urlGraph);

    // calculate the weighted pageRank, sort and print
    weightedPageRank(urlGraph, urlList, d, diffPR, maxIterations);
    sortPageRank(urlGraph, urlList);

    // free all memory
    ListFree(urlList);
    GraphFree(urlGraph);
    return 0;
}


// print output in descending order by weighted pagerank
void printPageRank(Graph g, List l) {
    for (Node curr = l->head; curr != NULL; curr = curr->next) {
        printf("%s %d %.7lf\n", curr->s, 
                g->outDegrees[curr->page], curr->pageRank);
    }
}


// sort by weighted pagerank, using insertion sort algorithm on linked list
// then print relevant contents in list to the user
void sortPageRank(Graph g, List l) {
    // insert pageRanks from graph array to List for sorting and printing
    for (Node curr = l->head; curr != NULL; curr = curr->next) {
        curr->pageRank = g->pageRanks[curr->page];
    }
    l->head = insertionSort(l, comparePageRanks);
    printPageRank(g, l);
}


double incrSumSet(Graph g, Node curr) {
    double sumSet = 0.0;
    for (int j = 0; j < g->nV; j++) {
        if (j == curr->page) { continue; }    // ignore self-loops
        if (g->inLinks[j][curr->page] > 0.0 && 
            g->outLinks[j][curr->page] > 0.0) {
            // calculate sum of PR(p_j, t) * W_in * W_out
            sumSet += ((double)g->inLinks[j][curr->page] * 
                        (double)g->outLinks[j][curr->page] * 
                        (double)g->pageRanks[j]);
        }
    }
    return sumSet;
}


// Calculate the weighted page rank of all values
void weightedPageRank(Graph g, List l, double d, 
                    double diffPR, int maxIterations) {
    int t = 0;          // t = iteration
    double diff = diffPR;
    while (t < maxIterations && diff >= diffPR) {
        double sumDiff = 0.0;
        for (Node curr = l->head; curr != NULL; curr = curr->next) {
            
            double sumSet = incrSumSet(g, curr);

            // calculate PR(p_i, t + 1)
            double pageRankPrev = g->pageRanks[curr->page];
            g->pageRanks[curr->page] = ((double)(1-d) / (double)l->size) + 
                                        (d * sumSet);

            // update diff for each url in collection
            sumDiff += fabs(g->pageRanks[curr->page] - pageRankPrev);
        }
        diff = sumDiff;
        t++;
    }
}


// Calculate in weight between two vertices v and u, which are actually urls
double inWeight(Graph g, int v, int u) {
    double pInlinks = inWeightReferenceList(g, v);
    double uInlinks = (double)g->inDegrees[u];
    return (double)uInlinks / (double)pInlinks;
}

// Calculate number of inlinks in list of reference pages to v
double inWeightReferenceList(Graph g, int v) {
    double numInLinks = 0;
    for (int i = 0; i < g->nV; i++) {
        if (i == v) { continue; }       // ignore self edges
        if (g->inLinks[v][i] > 0.0) {
            numInLinks += (double)g->inDegrees[i];
        }
    }
    return numInLinks;
}


// Calculate out weight between two vertices v and u
double outWeight(Graph g, int v, int u) {
    double pOutlinks = outWeightReferenceList(g, v);
    double uOutlinks = g->outDegrees[u] == 0 ? 0.5 : (double)g->outDegrees[u];
    // to avoid division by zero, return 0.5 if zero outlinks
    return (double)uOutlinks / (double)pOutlinks;
}

// Calculate number of outlinks in list of reference pages to v
double outWeightReferenceList(Graph g, int v) {
    double numOutLinks = 0;
    for (int i = 0; i < g->nV; i++) {
        if (i == v) { continue; }   // can't have an edge to itself!
        if (g->outLinks[v][i] > 0.0) { 
            numOutLinks += g->outDegrees[i] == 0 ? 0.5 :
                                                (double)g->outDegrees[i];
        }
    }
    return numOutLinks;
}


// update the in-weights and out-weights for all links in graph
// also acts as iteration 0 for calculating pageRank
void updateWeights(Graph g) {
    for (int i = 0; i < g->nV; i++) {
        g->pageRanks[i] = 1/g->nE;              // iteration 0
        for (int j = 0; j < g->nV; j++) {
            if (i == j) { continue; }
            // inLinks and outLinks have different values but both != 0
            if (g->inLinks[i][j] > 0.0) { 
                g->inLinks[i][j] = inWeight(g, i, j);
            }
            if (g->outLinks[i][j] > 0.0) {
                g->outLinks[i][j] = outWeight(g, i, j);
            }
        }
    }
}

