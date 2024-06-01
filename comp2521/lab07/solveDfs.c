// DFS maze solver

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "Cell.h"
#include "matrix.h"
#include "Maze.h"
#include "Stack.h"


Cell create_adj_cell(Cell c, int row, int col);
void check_adj_push(Maze m, Cell c, Stack s, 
                        int height, int width, bool **visited, Cell **path);
bool check_adj_cell(Maze m, Cell a);
bool exit_process(Stack s, bool **visited, Cell **path, bool found);
void mark_path(Maze m, Cell c, Cell **path);


bool solve(Maze m) {
    // TODO: Complete this function
    //       Feel free to add helper functions
    int maze_height = MazeHeight(m);
    int maze_width = MazeWidth(m);
    Cell start = MazeGetStart(m);

    if (MazeIsWall(m, start)) { return false; }
    if (MazeVisit(m, start)) { return true; }

    bool **visited = createBoolMatrix(maze_height, maze_width);
    Cell **path = createCellMatrix(maze_height, maze_width);
    Stack s = StackNew();
    StackPush(s, start);

    while (!StackIsEmpty(s)) {
		Cell c = StackPop(s);
        if (MazeVisit(m, c)) { 
            mark_path(m, c, path);
            return exit_process(s, visited, path, true);
        }
		if (visited[c.row][c.col]) { continue; }
		visited[c.row][c.col] = true;

        check_adj_push(m, c, s, 1, 0, visited, path);
        check_adj_push(m, c, s, -1, 0, visited, path);
        check_adj_push(m, c, s, 0, 1, visited, path);
        check_adj_push(m, c, s, 0, -1, visited, path);
    }
    return exit_process(s, visited, path, false);
}


bool exit_process(Stack s, bool **visited, Cell **path, bool found) {
    StackFree(s);
    freeBoolMatrix(visited);
    freeCellMatrix(path);
    return found; 
}


void mark_path(Maze m, Cell c, Cell **path) {
    Cell start = MazeGetStart(m);
    if (c.row == start.row && c.col == start.col) { 
        MazeMarkPath(m, c); 
        return; 
    }
    MazeMarkPath(m, c);
    mark_path(m, path[c.row][c.col], path);
}


void check_adj_push(Maze m, Cell c, Stack s, 
                        int height, int width, bool **visited, Cell **path) {

    Cell w = create_adj_cell(c, height, width);
    if (check_adj_cell(m, w) && !visited[w.row][w.col]) { 
        path[w.row][w.col] = c;
        StackPush(s, w);
    }
}


Cell create_adj_cell(Cell c, int row, int col) {
    Cell a;
    a.row = c.row + row;
    a.col = c.col + col;
    return a;
}


bool check_adj_cell(Maze m, Cell a) {
    if (a.row < 0 || a.row >= MazeHeight(m)) { return false; }
    else if (a.col < 0 || a.col >= MazeWidth(m)) { return false; }
    else if (MazeIsWall(m, a)) { return false; }
    return true;
}



