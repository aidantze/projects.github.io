// BFS maze solver

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "Cell.h"
#include "matrix.h"
#include "Maze.h"
#include "Queue.h"


Cell create_adj_cell(Cell c, int row, int col);
void check_adj_enqueue(Maze m, Cell c, Queue q, 
                        int height, int width, bool **visited, Cell **path);
bool check_adj_cell(Maze m, Cell a);
bool exit_process(Queue q, bool **visited, Cell **path, bool found);
void mark_path(Maze m, Cell c, Cell **path);


bool solve(Maze m) {
    // TODO: Complete this function
    //       Feel free to add helper functions
    int maze_height = MazeHeight(m);        // O(1)
    int maze_width = MazeWidth(m);          
    Cell start = MazeGetStart(m);           // O(1)

    if (MazeIsWall(m, start)) { return false; }     // O(1)
    if (MazeVisit(m, start)) { return true; }       // O(1)

    bool **visited = createBoolMatrix(maze_height, maze_width); // O(n)
    Cell **path = createCellMatrix(maze_height, maze_width);    // O(n)
    Queue q = QueueNew();                   // O(1)
    QueueEnqueue(q, start);                 // O(1)

    while (!QueueIsEmpty(q)) {              // worst case O(n)
		Cell c = QueueDequeue(q);           // O(1)
        if (MazeVisit(m, c)) {              // O(1)
            mark_path(m, c, path);          // O(n)
            return exit_process(q, visited, path, true);    // O(n)
        }
		if (visited[c.row][c.col]) { continue; }
		visited[c.row][c.col] = true;

        check_adj_enqueue(m, c, q, 1, 0, visited, path);        // O(1)
        check_adj_enqueue(m, c, q, -1, 0, visited, path);
        check_adj_enqueue(m, c, q, 0, 1, visited, path);
        check_adj_enqueue(m, c, q, 0, -1, visited, path);
    }
    return exit_process(q, visited, path, false);           // O(n)
}


bool exit_process(Queue q, bool **visited, Cell **path, bool found) {
    QueueFree(q);               // O(n)
    freeBoolMatrix(visited);    // O(height)
    freeCellMatrix(path);       // O(height)
    return found;
}


void mark_path(Maze m, Cell c, Cell **path) {
    Cell start = MazeGetStart(m);           // O(1)
    if (c.row == start.row && c.col == start.col) { 
        MazeMarkPath(m, c);                 // O(1)
        return; 
    }
    MazeMarkPath(m, c);                     // O(1)
    mark_path(m, path[c.row][c.col], path);     // O(n)
}


void check_adj_enqueue(Maze m, Cell c, Queue q, 
                        int height, int width, bool **visited, Cell **path) {

    Cell w = create_adj_cell(c, height, width);     // O(1)
    if (check_adj_cell(m, w) && !visited[w.row][w.col]) {       // O(1)
        path[w.row][w.col] = c;
        QueueEnqueue(q, w);                         // O(1)
    }
}


Cell create_adj_cell(Cell c, int row, int col) {
    Cell a;
    a.row = c.row + row;
    a.col = c.col + col;
    return a;
}


bool check_adj_cell(Maze m, Cell a) {
    if (a.row < 0 || a.row >= MazeHeight(m)) { return false; }          // O(1)
    else if (a.col < 0 || a.col >= MazeWidth(m)) { return false; }      // O(1)
    else if (MazeIsWall(m, a)) { return false; }                        // O(1)
    return true;
}

