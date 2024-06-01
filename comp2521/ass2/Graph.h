// This file was copied and modified from COMP2521 lab 8.
// https://cgi.cse.unsw.edu.au/~cs2521/22T3/lab/8/questions
// It provides an interface to the Undirected Weighted Graph ADT
// - Vertices are identified by integers between 0 and nV - 1,
//   where nV is the number of vertices in the graph
// - Weights are doubles and must be positive
// - Self-loops are not allowed

#ifndef GRAPH_H
#define GRAPH_H

#include <stdbool.h>
#include "List.h"


// edges are pairs of vertices (end-points)
typedef struct Edge {
    int v;               // in vertex
    int w;               // out vertex
    double inWeight;        // weight of link(v, w)
    double outWeight;       // weight of link(v, w)
} Edge;


typedef struct graph *Graph;
struct graph {
    int nV;                 // #vertices
    int nE;                 // #edges
    int *inDegrees;         // array storing inDegrees
    int *outDegrees;        // array storing outDegrees
    double *pageRanks;      // array storing pageRanks
    double **inLinks;       // adjacency matrix storing positive in-weights
                            // fields set to 0 if nodes not adjacent
    double **outLinks;      // adjacency matrix storing positive out-weights
};


/**
 * Creates a new instance of a graph
 */
Graph  GraphNew(int nV);

/**
 * Frees all memory associated with the given graph
 */
void   GraphFree(Graph g);

/**
 * Returns the number of vertices in the graph
 */
int    GraphNumVertices(Graph g);

/**
 * Inserts  an  edge into a graph. Does nothing if there is already an
 * edge between `e.v` and `e.w`. Returns true if successful, and false
 * if there was already an edge.
  */
bool   GraphInsertEdge(Graph g, Edge e);

/**
 * Removes an edge from a graph. Returns true if successful, and false
 * if the edge did not exist.
 */
bool   GraphRemoveEdge(Graph g, int v, int w);

/**
 * Returns the weight of the edge between `v` and `w` if it exists, or
 * 0.0 otherwise
 */
//double GraphIsAdjacent(Graph g, int v, int w);

/**
 * Returns true if the graph contains a cycle, and false otherwise
 */
//bool   GraphHasCycle(Graph g);

/**
 * Returns a minimum spanning tree of the given graph. The given graph
 * should not be modified. Returns NULL if the graph has no minimum 
 * spanning tree.
 */
//Graph  GraphMST(Graph g);

/**
 * Displays information about the graph
 */
//void   GraphShow(Graph g);

#endif
