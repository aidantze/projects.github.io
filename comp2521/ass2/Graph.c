// This file was copied and modified from COMP2521 lab 8.
// https://cgi.cse.unsw.edu.au/~cs2521/22T3/lab/8/questions
// It is an implementation of the Weighted Graph ADT

#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>

#include "Graph.h"
#include "List.h"


double **getAdjacencyMatrix(Graph g, int nV);
int *getGraphArray(Graph g, int nV);
static int validVertex(Graph g, int v);


////////////////////////////////////////////////////////////////////////

Graph GraphNew(int nV) {
    assert(nV > 0);

    Graph g = malloc(sizeof(*g));
    if (g == NULL) {
        fprintf(stderr, "error: out of memory\n");
        exit(EXIT_FAILURE);
    }

    g->nV = nV;
    g->nE = 0;

    g->inLinks = getAdjacencyMatrix(g, nV);
    g->outLinks = getAdjacencyMatrix(g, nV);

    g->inDegrees = getGraphArray(g, nV);
    g->outDegrees = getGraphArray(g, nV);

    g->pageRanks = calloc(nV, nV * sizeof(double));
    if (g->pageRanks == NULL) {
        fprintf(stderr, "error: out of memory\n");
        exit(EXIT_FAILURE);
    }

    return g;
}


double **getAdjacencyMatrix(Graph g, int nV) {
    double **adjMatrix = malloc(nV * sizeof(double *));
    if (adjMatrix == NULL) {
        fprintf(stderr, "error: out of memory\n");
        exit(EXIT_FAILURE);
    }
    for (int i = 0; i < nV; i++) {
        adjMatrix[i] = calloc(nV, sizeof(double));
        if (adjMatrix[i] == NULL) {
            fprintf(stderr, "error: out of memory\n");
            exit(EXIT_FAILURE);
        }
    }
    return adjMatrix;
}


int *getGraphArray(Graph g, int nV) {
    int *array = calloc(nV, nV * sizeof(int));
    if (array == NULL) {
        fprintf(stderr, "error: out of memory\n");
        exit(EXIT_FAILURE);
    }
    return array;
}


void GraphFree(Graph g) {
    for (int i = 0; i < g->nV; i++) {
        free(g->inLinks[i]);
        free(g->outLinks[i]);
    }
    free(g->inLinks);
    free(g->outLinks);
    free(g->inDegrees);
    free(g->outDegrees);
    free(g->pageRanks);
    free(g);
}

////////////////////////////////////////////////////////////////////////

int GraphNumVertices(Graph g) {
    return g->nV;
}

int GraphNumEdges(Graph g) {
    return g->nE;
}

bool GraphInsertEdge(Graph g, Edge e) {
    assert(validVertex(g, e.v));
    assert(validVertex(g, e.w));
    assert(e.v != e.w);
    assert(e.inWeight > 0.0);
    assert(e.outWeight > 0.0);

    if (g->inLinks[e.v][e.w] == 0.0 && g->outLinks[e.v][e.w] == 0.0) {
        g->inLinks[e.v][e.w] = e.inWeight;
        g->outLinks[e.v][e.w] = e.outWeight;
        g->nE++;
        return true;
    } else {
        return false;
    }
}

bool GraphRemoveEdge(Graph g, int v, int w) {
    assert(validVertex(g, v));
    assert(validVertex(g, w));

    if (g->inLinks[v][w] != 0.0 && g->outLinks[v][w] != 0.0) {
        g->inLinks[v][w] = 0.0;
        g->outLinks[v][w] = 0.0;
        g->nE--;
        return true;
    } else {
        return false;
    }
}


static int validVertex(Graph g, int v) {
    return v >= 0 && v < g->nV;
}


/*
double GraphIsAdjacent(Graph g, int v, int w) {
    assert(validVertex(g, v));
    assert(validVertex(g, w));

    return g->inLinks[v][w];
}


void GraphShow(Graph g) {
    printf("Number of vertices: %d\n", g->nV);
    printf("Number of edges: %d\n", g->nE);
    for (int v = 0; v < g->nV; v++) {
        for (int w = v + 1; w < g->nV; w++) {
            if (g->inLinks[v][w] != 0.0) {
                printf("Edge %d - %d: %lf\n", v, w, g->inLinks[v][w]);
            }
        }
    }
}


static int validVertex(Graph g, int v) {
    return v >= 0 && v < g->nV;
}
*/

