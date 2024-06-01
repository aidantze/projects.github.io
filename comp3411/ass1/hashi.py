#!/usr/bin/python3
"""
COMP3411 Assignment 1
The following code is for Hashiwokakero

This is my approach to the assignment:
I start by scanning the file to a list of strings, which act as a 2D array but in
Python. This is used only to aid in printing output. Then I translate this to a
graph, which is a dict of dicts storing the following:
- Key: Index
- Values dict:
    - origNo: original no. on island
    - currNo: current no. on island after bridges added
    - adjNodes: list of all adjacent islands a bridge could be placed at
    - changed: list storing recursion number to determine resetting afer recursion

I also store all current bridges in an edges list, which is a list of dicts where
each dict stores the following:
- from: index bridge starts from
- to: index bridge ends at
- direction: horizontal or vertical
- numBridges: no. of bridges at this edge (1, 2 or 3)

I formerly used a backtrack search, which acted like a greedy search that would fail
if the end result was invalid. With 2 days left, I had to change my strategy.

Now I use forward checking: I loop through all islands and add bridges until the
index is 0. Each bridge added goes through all constraint checks. In the event that
multiple bridges can be added to multiple adjacent islands, I use a permutations
library to generate all possible permutations, and forward checking simply loops
through each permutation. If a permutation fails at one step in the recursion process,
we simply move to the next permutation.

After all islands visited or max bridges reached, we check for a solution. If the
solution is valid and passes all constraints, we print bridges from our list of
strings earlier.

— Aidan Tan (z5360925)
"""
import sys
import itertools

hashiMap = []   # used purely for text output
hashiGraph = {} # graph storing all nodes and possible edges
maxBridges = 0
numNodes = 0


# Prep...
def generateMapFromFile(file):
    """
    Takes text from a file and adds to hashiMap
    """
    filedata = open(file)
    hashiMap = filedata.read().split('\n')
    return hashiMap


def scanMap():
    """
    Scans text from stdin and adds to hashiMap
    """
    for line in sys.stdin:
        if line != '\n':
            hashiMap.append(line.strip())
    return hashiMap


def getGraphFromMap():
    """
    Takes all islands from map and adds them to a dict of dicts containing:
    - index position
    - node number
    - list of adjacent nodes where a bridge can be placed
    This list of dictionaries acts as a Graph data structure
    """
    for i in range(len(hashiMap)):
        for j in range(len(hashiMap[i])):
            if isIsland(i, j):
                adjacentNodes = findNearbyIslands(i, j)
                # for b in bridges:
                #     row, col = b["to"]
                #     adjacentNodes.append((row, col))

                if hashiMap[i][j] == 'a':
                    val = 10
                elif hashiMap[i][j] == 'b':
                    val = 11
                elif hashiMap[i][j] == 'c':
                    val = 12
                else:
                    val = int(hashiMap[i][j])

                adjNodeNo = []
                for a in adjacentNodes:
                    x, y = a
                    adjNodeNo.append(hashiMap[x][y])

                hashiGraph[(i, j)] = {
                    "origNo": val,  # original no. on island
                    "currNo": val,  # no. of available bridges remaining
                    "adjNodes": adjacentNodes,      # adjacent islands
                    "changed": [],    # used to track status across recursions
                }
                # "adjNodeNo": adjNodeNo  # no. on adjacent islands


def getMaxBridges():
    """
    Get number of bridges required to place, for the heuristic
    This is total sum of all numbers on hashiMap / 2,
    since each bridge between two nodes will subtract 1 from both nodes
    """
    count = 0
    for g in hashiGraph.values():
        count += g['origNo']
    return count / 2


def getNumNodes():
    """
    Get number of nodes (i.e. no. of islands) in the hashiMap
    """
    count = 0
    for i in range(len(hashiMap)):
        for j in range(len(hashiMap[i])):
            if isIsland(i, j):
                count += 1
    return count


def isIsland(row, col):
    """
    Checks if a given index is an island on the hashiMap

    :param row: row of index
    :param col: col of index
    :return: true if island exists, false otherwise
    """
    return hashiMap[row][col] != '.'


def findNearbyIslands(row, col):
    """
    Finds the nearest available island in all four compass directions in the
    hashiMap around the island at the index given by row and col

    :param row: the row of the index we want to extend a bridge from
    :param col: the col of the index we want to extend a bridge from
    :returns list of dictionaries containing
    """
    nearbyIslands = []
    rowsLength = len(hashiMap)
    colsLength = len(hashiMap[0])   # assumption: hashiMap is a rectangle
    if row > 1:
        # iterate through rows backwards
        for r in range(row - 1, -1, -1):
            if isIsland(r, col):
                # addBridge(row, col, r, col)
                nearbyIslands.append((r, col))
                break
    if row < rowsLength - 1:
        # iterate through rows forwards
        for r in range(row + 1, rowsLength):
            if isIsland(r, col):
                # addBridge(row, col, r, col)
                nearbyIslands.append((r, col))
                break
    if col > 1:
        # iterate through cols backwards
        for c in range(col - 1, -1, -1):
            if isIsland(row, c):
                # addBridge(row, col, row, c)
                nearbyIslands.append((row, c))
                break
    if col < colsLength - 1:
        # iterate through cols forwards
        for c in range(col + 1, colsLength):
            if isIsland(row, c):
                # addBridge(row, col, row, c)
                nearbyIslands.append((row, c))
                break
    return nearbyIslands


def generateBridgeLocations():
    """
    Generates all bridge locations using the helper functions above
    """
    for i in range(len(hashiMap)):
        for j in range(len(hashiMap[i])):
            if isIsland(i, j):
                findNearbyIslands(i, j)


# Output...
def updateSymbol(row, col, vertical=True):
    """
    Update the symbol at the given index based on the bridge

    :param vertical: True if we are adding a vertical bridge, False for horizontal
    """
    if vertical:
        if hashiMap[row][col] == '.':
            # hashiMap[row][col].replace('.', '|')
            hashiMap[row] = hashiMap[row][:col] + '|' + hashiMap[row][col + 1:]
        elif hashiMap[row][col] == '|':
            # hashiMap[row][col].replace('|', '"')
            hashiMap[row] = hashiMap[row][:col] + '"' + hashiMap[row][col + 1:]
        elif hashiMap[row][col] == '"':
            # hashiMap[row][col].replace('"', '#')
            hashiMap[row] = hashiMap[row][:col] + '#' + hashiMap[row][col + 1:]
    else:
        if hashiMap[row][col] == '.':
            # hashiMap[row][col].replace('.', '-')
            hashiMap[row] = hashiMap[row][:col] + '-' + hashiMap[row][col + 1:]
        elif hashiMap[row][col] == '-':
            # hashiMap[row][col].replace('-', '=')
            hashiMap[row] = hashiMap[row][:col] + '=' + hashiMap[row][col + 1:]
        elif hashiMap[row][col] == '=':
            # hashiMap[row][col].replace('=', 'E')
            hashiMap[row] = hashiMap[row][:col] + 'E' + hashiMap[row][col + 1:]


def addBridgeFromIndexes(row1, col1, row2, col2):
    """
    Builds a bridge between two indexes by updating the hashiMap
    Each bridge must be vertical or horizontal.
    Precondition: col1!=col2 and row1==row2, or col1==col2 and row1!=row2
    """
    if row1 == row2:
        # horizontal bridge iterates over cols
        if col1 < col2:
            for c in range(col1 + 1, col2):
                updateSymbol(row1, c, False)
        else:
            for c in range(col2 + 1, col1):
                updateSymbol(row1, c, False)
    elif col1 == col2:
        # vertical bridge iterates over rows
        if row1 < row2:
            for r in range(row1 + 1, row2):
                updateSymbol(r, col1, True)
        else:
            for r in range(row2 + 1, row1):
                updateSymbol(r, col1, True)


def addBridgeFromEdge(edge):
    """
    Builds a bridge from a given edge by updating the hashiMap
    :param edge: the edge to build the bridge on
    """
    row1, col1 = edge['from']
    row2, col2 = edge['to']
    for i in range(edge["num"]):
        addBridgeFromIndexes(row1, col1, row2, col2)


# Solution Verification...
def checkSolution(edges):
    """
    Check if edges is a valid solution

    Constraints:
    1. all bridges must run horizontally or vertically
    2. bridges are not allowed to cross each other, or other islands
    3. there can be no more than three bridges connecting any pair of islands
    4. the total number of bridges connected to each island must be equal to the number on the island

    :returns True if valid solution, False otherwise
    """
    for b in edges:
        # verify that no bridge cross other bridges
        for c in edges:
            if c['from'] == b['from'] and c['to'] == b['to']:
                continue
            if c['direction'] == b['direction']:
                continue
            if checkCrossBridges(b, c):
                # solution has a bridge crossing another bridge
                return False

        # verify that every bridge count <= 3
        if b['num'] > 3:
            # solution has more than 3 bridges in 1 location
            return False

    for k, g in hashiGraph.items():
        # verify that no. bridges connected to each island = no. on the island
        if g['currNo'] < 0:
            # solution has an index with more bridges than what is allowed
            return False
        elif g['currNo'] > 0:
            # solution has an index with not enough bridges connected to it
            return False
    return True


def getBridgesFromPath(edges, solPath):
    """
    Creates a list of dictionaries storing edges, and no. of occurences for each bridge

    Precondition: len(solPath) > 1
    """
    for i in range(len(solPath) - 1):
        pos1 = solPath[i]
        pos2 = solPath[i + 1]
        if bridgeExists(edges, pos1, pos2):
            updateBridgeCount(edges, pos1, pos2)
            continue

        edges.append({
            "from": pos1,
            "to": pos2,
            "direction": getDirection(pos1, pos2),
            "num": 1
        })
    return edges


def getDirection(pos1, pos2):
    """
    Returns the direction of the bridge between pos1 and pos2
    Which is either horizontal or vertical
    """
    if pos1[0] == pos2[0]:
        return "horizontal"
    else:
        return "vertical"


def bridgeExists(edges, pos1, pos2):
    """
    True if bridge exists between pos1 and pos2, False otherwise
    """
    for e in edges:
        if e["from"] == pos1 and e["to"] == pos2:
            return True
        if e["from"] == pos2 and e["to"] == pos1:
            return True
    return False


def updateBridgeCount(edges, pos1, pos2, num=1):
    """
    Adds num to the edge between pos1 and pos2
    """
    for e in edges:
        if e["from"] == pos1 and e["to"] == pos2:
            e["num"] += num
        if e["from"] == pos2 and e["to"] == pos1:
            e["num"] += num
    return edges


def undoBridgeCount(edges, pos1, pos2, num=1):
    """
    Removes num to the edge between pos1 and pos2
    If num becomes 0, removes the edge completely
    """
    for e in edges:
        if e["from"] == pos1 and e["to"] == pos2:
            e["num"] -= num
            if e["num"] <= 0:
                edges.remove(e)
                break
        if e["from"] == pos2 and e["to"] == pos1:
            e["num"] -= num
            if e["num"] <= 0:
                edges.remove(e)
                break
    return edges


def checkCrossBridges(b1, b2):
    """
    Check if any two bridges cross each other
    :param b1: bridge 1
    :param b2: bridge 2
    :return: True if they cross, False otherwise
    """
    # get all indexes bridge 1 consumes
    b1Path = getBridgeIndexes(b1)
    # get all indexes bridge 2 consumes
    b2Path = getBridgeIndexes(b2)

    # check if any indexes are the same
    for i in b1Path[1:]:
        for j in b2Path[1:]:
            if i == j:
                return True
    return False


def getBridgeIndexes(edge):
    """
    Get list of all the indexes in the hashiMap that the edge consumes
    """
    path = []
    if edge['direction'] == "horizontal":
        # horizontal bridge iterates over cols
        r = edge['from'][0]   # col always the same
        if edge['from'][1] > edge['to'][1]:
            for i in range(edge['to'][1], edge['from'][1], -1):
                path.append((r, i))
        else:
            for i in range(edge['from'][1], edge['to'][1]):
                path.append((r, i))
    else:
        # vertical bridge iterates over rows
        c = edge['from'][1]
        if edge['from'][0] > edge['to'][0]:
            for i in range(edge['to'][0], edge['from'][0], -1):
                path.append((i, c))
        else:
            for i in range(edge['from'][0], edge['to'][0]):
                path.append((i, c))
    return path


# Constraint Satisfaction...
# Attempt 1 — Backtracking (this algorithm did not work)
def backtrack(graph, c, pos, edges):
    """
    Adapted from the following pseudocode (for backtrack search):
    https://en.wikipedia.org/wiki/Backtracking

    procedure backtrack(P, c) is
        if reject(P, c) then return
        if accept(P, c) then output(P, c)
        s ← first(P, c)
        while s ≠ NULL do
            backtrack(P, s)
            s ← next(P, s)

    c = list of nodes in a path, bridges are formed between adjacent nodes in c
    s updates this list
    sol is a completed list from a later recursion
    first() = first element in adjNodes from graph node
    next() = next element in adjNodes from graph node
    This path list gets converted to bridges in edges list, which is stored across
    multiple backtracks

    :return: tuple containing the following:
    - atLimit: True if solution found, False otherwise
    - goToNext: True if we should terminate and restart from another node, false otherwise
    - c, r or sol: list of tuples that outline the solution path
    """
    nBridges = countTotalBridges(edges)
    # -1 because len(c) == 2 : 1 bridge, len(c) == 3 : 2 bridges, etc.
    if len(c) - 1 + nBridges == maxBridges:
        return True, False, c

    # check if this index cannot make room for a new bridge
    if graph[pos]['currNo'] <= 0:
        return False, False, c
    graph[pos]['currNo'] -= 1   # -1 for bridge placed

    s, pos1, i = first(graph, c, pos, edges)
    r = []
    # i = counter for index of adjNodes in next()
    while s is not None:
        r = s
        graph[pos1]['currNo'] -= 1  # -1 for bridge placed
        atLimit, goToNext, sol = backtrack(graph, s, pos1, edges)

        # solution was found in a later recursion
        if atLimit:
            return True, False, sol
        if goToNext:
            return False, True, sol

        # graph[pos1]['currNo'] += 1  # undo bridge placement
        pos2 = pos1
        s, pos1, i = next(graph, s, pos, i, edges)
        if s is not None:
            graph[pos2]['currNo'] += 1  # undo bridge placement

    # graph[pos]['currNo'] += 1   # undo bridge placement
    if r == []:
        # no other nodes to navigate to (dead end)
        return False, True, c
    return False, True, r


def first(graph, c, pos, edges):
    """
    Adapted from the following pseudocode (for backtrack search):
    https://en.wikipedia.org/wiki/Backtracking

    function first(P, c) is
        k ← length(c)
        if k = n then
            return NULL
        else
            return (c[1], c[2], ..., c[k], 1)

    """
    if len(set(c)) == numNodes + 1:
        return None, pos, 0

    i = 0
    for x in graph[pos]['adjNodes']:
        # check for indexes on either side
        if graph[x]['currNo'] == 0:
            i += 1
            continue

        # check for bridge limit
        if countBridges(edges, c, pos, x) >= 3:
            i += 1
            continue

        # check for intersecting bridges
        if bridgeCrossing(c, pos, x, edges):
            i += 1
            continue
        break

    if i >= len(graph[pos]['adjNodes']):
        return None, pos, 0
    pos1 = graph[pos]['adjNodes'][i]

    v = c
    v.append(pos1)
    return v, pos1, i + 1


def next(graph, s, pos, i, edges):
    """
    Adapted from the following pseudocode (for backtrack search):
    https://en.wikipedia.org/wiki/Backtracking

    function next(P, s) is
        k ← length(s)
        if s[k] = m then
            return NULL
        else
            return (s[1], s[2], ..., s[k − 1], 1 + s[k])

    """
    graphNodes = graph[pos]['adjNodes']
    if i >= len(graphNodes):
        return None, pos, i

    j = i
    while j < len(graphNodes):
        x = graphNodes[j]
        # check for indexes on either side
        if graph[x]['currNo'] == 0:
            j += 1
            continue

        # check for bridge limit
        if countBridges(edges, s, pos, x) >= 3:
            j += 1
            continue

        # check for intersecting bridges
        if bridgeCrossing(s, pos, x, edges):
            j += 1
            continue
        break

    if j >= len(graphNodes):
        return None, pos, i
    pos1 = graph[pos]['adjNodes'][j]

    v = s
    v[-1] = pos1
    return v, pos1, j + 1


def search(graphKeys):
    """
    Call backtrack and logic to perform path search and constraint checking
    :param graphKeys: list of the keys of the graph dict
    :return: list of edges that each represent bridges
    """
    graph = dict(hashiGraph)
    firstPos = graphKeys[0]
    c = [firstPos]
    edges = []
    atLimit, goToNext, path = backtrack(graph, c, firstPos, edges)
    # print(path)
    edges = getBridgesFromPath(edges, path)

    if not atLimit:
        i = 1
        while i < len(graphKeys):
            # for pos in graphKeys[1:]:
            pos = graphKeys[i]
            c = [pos]
            atLimit, goToNext, newPath = backtrack(graph, c, pos, edges)
            # print(newPath)
            if len(newPath) > 1:
                edges = getBridgesFromPath(edges, newPath)
            else:
                i += 1

            # check solution
            if atLimit:
                break

    # print(edges)
    return edges


# Attempt 2 — Forward Checking (this algorithm works)
def forwardCheck(graph, edges, pos, r):
    """
    Performs forward checking

    :param graph: graph storing indexes
    :param edges: list storing currently-added bridges
    :param pos: current index to add bridges to
    :param r: int = depth of recursion
    :return:
    - isFound: True if solution is valid, False otherwise
    - edgeResult: resulting edges list, empty if not isFound
    """
    # get all adjacent nodes and their number
    adjNodes = {}
    numHere = graph[pos]['currNo']
    canExtend = False
    for a in range(len(graph[pos]['adjNodes'])):
        node = graph[pos]['adjNodes'][a]

        # perform every constraint check to verify bridge locations
        if graph[node]['currNo'] <= 0:
            continue

        if bridgeCrossing([], pos, node, edges):
            continue

        tmpEdge = getEdge(edges, pos, node)
        if tmpEdge is not None and tmpEdge['num'] >= 3:
            continue

        adjNodes[node] = graph[node]['currNo']
        canExtend = True

    if not canExtend or numHere == 0:
        # jump to solution checking, recurse anyways
        nextPos = getNextPos(graph, pos)
        if nextPos is None or countTotalBridges(edges) == maxBridges:
            # check for solution
            if checkSolution(edges):
                return True, edges
            return False, []

        isFound, edgeResult = forwardCheck(graph, edges, nextPos, r+1)
        if isFound:
            return True, edgeResult
        return False, []

    if len(adjNodes) * 3 < numHere:
        # no permutations can ever be fulfilled
        return False, []
    # generate all permutations (length of adjNodes always <= 4)
    vals = [0, 1, 2, 3]  # no. bridges cannot exceed 3
    com = list(itertools.product(vals, repeat=len(adjNodes)))
    com = filter(lambda x: sum(x) == numHere, com)
    for c in com:
        # update permutation to temp edges and graph
        invalid = False
        for i in range(len(adjNodes)):
            if c[i] == 0:
                continue
            if c[i] > numHere:
                invalid = True
                break

            adjKeys = list(adjNodes.keys())
            p = adjKeys[i]

            # constraint check if room available to add bridge
            if graph[p]['currNo'] - c[i] < 0:
                invalid = True
                break

            # constraint check if bridge crosses another bridge
            if bridgeCrossing([], pos, p, edges):
                invalid = True
                break

            if getEdge(edges, pos, p) is None:
                # create new edge
                edges.append({
                    'from': pos,
                    'to': p,
                    'direction': getDirection(pos, p),
                    'num': c[i]
                })
                # update temp graph
                graph[pos]['currNo'] -= c[i]
                graph[p]['currNo'] -= c[i]
                graph[pos]['changed'].append(r)
                graph[p]['changed'].append(r)
                continue

            # constraint check if adding extra bridges reaches bridge limit
            if getEdge(edges, pos, p)['num'] + c[i] > 3:
                invalid = True
                break

            # update temp edges and temp graph
            updateBridgeCount(edges, pos, p, c[i])
            graph[pos]['currNo'] -= c[i]
            graph[p]['currNo'] -= c[i]
            graph[pos]['changed'].append(r)
            graph[p]['changed'].append(r)

        if invalid:
            # reset
            for i in range(len(adjNodes)):
                adjKeys = list(adjNodes.keys())
                p = adjKeys[i]
                if r in graph[p]['changed']:
                    undoBridgeCount(edges, pos, p, c[i])
                    graph[pos]['currNo'] += c[i]
                    graph[p]['currNo'] += c[i]
                    graph[pos]['changed'].remove(r)
                    graph[p]['changed'].remove(r)
            continue

        # check if there is a next position to recurse
        nextPos = getNextPos(graph, pos)
        if nextPos is None or countTotalBridges(edges) >= maxBridges:
            # check for solution
            if checkSolution(edges):
                return True, edges
            continue

        isFound, edgeResult = forwardCheck(graph, edges, nextPos, r+1)
        if isFound:
            return True, edgeResult

        # reset
        for i in range(len(adjNodes)):
            adjKeys = list(adjNodes.keys())
            p = adjKeys[i]
            if r in graph[p]['changed']:
                undoBridgeCount(edges, pos, p, c[i])
                graph[pos]['currNo'] += c[i]
                graph[p]['currNo'] += c[i]
                graph[pos]['changed'].remove(r)
                graph[p]['changed'].remove(r)

    return False, []


def getNextPos(graph, pos):
    """
    Return next position in the graph, None if at end of graph
    """
    graphList = list(graph.keys())
    for i in range(len(graphList) - 1):
        if graphList[i] == pos:
            return graphList[i + 1]
    return None


# Other helper functions...
def bridgeCrossing(path, pos1, pos2, edges):
    """
    Check if any bridges cross the bridge between pos1 and pos2
    We are smarter about this so we don't have to check every bridge:
    For edges:
    - Only checks bridges of opposite direction
    - With index values lying between pos1 and pos2 in that direction
    - Ignores identical bridges
    For path:
    - Only checks bridges not adjacent to this bridge
    - Only checks bridges in opposite direction
    - Ignores identical bridges

    Note: path was only used for attempt 1 (backtracking)

    :return: True if bridges cross, False otherwise
    """
    tmpEdge1 = {
        "from": pos1,
        "to": pos2,
        "direction": getDirection(pos1, pos2),
        "numBridges": 1
    }
    for e in edges:
        # ignore identical bridges
        if e['from'] == pos1 and e['to'] == pos2:
            continue
        if e['from'] == pos2 and e['to'] == pos1:
            continue

        # ignore bridges in same direction
        if e['direction'] == tmpEdge1['direction']:
            continue

        # ignore bridges that do not lie in same bounds as this bridge
        if (
            e['direction'] == "horizontal" and (
                e['from'][0] < tmpEdge1['from'][0] < e['to'][0] or
                e['from'][0] > tmpEdge1['from'][0] > e['to'][0]
        )) or (
            e['direction'] == "vertical" and (
                e['from'][1] < tmpEdge1['from'][1] < e['to'][1] or
                e['from'][1] > tmpEdge1['from'][1] > e['to'][1]
        )):
            continue

        # check if bridges cross
        if checkCrossBridges(tmpEdge1, e):
            return True

    if path == []:
        return False
    for i in range(len(path) - 1):
        tmpEdge2 = {
            "from": path[i],
            "to": path[i + 1],
            "direction": getDirection(path[i], path[i + 1]),
            "numBridges": 1
        }

        # ignore identical bridges
        if tmpEdge2['from'] == pos1 and tmpEdge2['to'] == pos2:
            continue

        # ignore bridges in same direction
        if tmpEdge2['direction'] == tmpEdge1['direction']:
            continue

        # ignore bridges that do not lie in same bounds as this bridge
        if (
                tmpEdge2['direction'] == "horizontal" and (
                tmpEdge2['from'][0] < tmpEdge1['from'][0] < tmpEdge2['to'][0] or
                tmpEdge2['from'][0] > tmpEdge1['from'][0] > tmpEdge2['to'][0]
        )) or (
                tmpEdge2['direction'] == "vertical" and (
                tmpEdge2['from'][1] < tmpEdge1['from'][1] < tmpEdge2['to'][1] or
                tmpEdge2['from'][1] > tmpEdge1['from'][1] > tmpEdge2['to'][1]
        )):
            continue

        # check if bridges cross
        if checkCrossBridges(tmpEdge1, tmpEdge2):
            return True
    return False


def countBridges(edges, path, pos1, pos2):
    """
    Count number of bridges at location between pos1 and pos2 in path

    1. Counts no. of bridges at location in list of edges (from prev. paths)
    2. Counts no. of bridges at location in current path
    """
    nBridges = getEdge(edges, pos1, pos2)
    if nBridges is None:
        nBridges = 0
    else:
        nBridges = nBridges['num']

    count = 0
    for i in range(len(path) - 1):
        if path[i] == pos1 and path[i + 1] == pos2:
            count += 1
        elif path[i] == pos2 and path[i + 1] == pos1:
            count += 1
    return count + nBridges


def countTotalBridges(edges):
    """
    Count number of bridges in edges list in total
    """
    count = 0
    for e in edges:
        count += e['num']
    return count


def getEdge(edges, pos1, pos2):
    """
    Get the edge dict storing the edge from pos1 to pos2
    """
    for e in edges:
        if e['from'] == pos1 and e['to'] == pos2:
            return e
        if e['to'] == pos1 and e['from'] == pos2:
            return e
    return None


# main...
if __name__ == '__main__':
    if len(sys.argv) > 2:
        print("Usage: python3 hashi.py <filename>")
        exit(1)

    # len(sys.argv) == 2: file passed in as file argument
    # len(sys.argv) == 1: file passed in through std.in
    if len(sys.argv) == 2:
        hashiMap = generateMapFromFile(sys.argv[1])
    else:
        hashiMap = scanMap()

    getGraphFromMap()

    if len(hashiGraph.keys()) < 2:
        # no bridges to be added
        exit()

    numNodes = getNumNodes()
    maxBridges = getMaxBridges()
    # graph theory: if total sum of numbers in hashiMap is odd
    # then there cannot be a valid solution
    if (maxBridges * 2) % 2 != 0:
        # No solution exists
        exit()

    # if an index cannot connect to any other islands, then no valid solution
    for g in hashiGraph.values():
        if len(g['adjNodes']) == 0:
            # No solution exists
            exit()

    firstPos = list(hashiGraph.keys())[0]
    hashiEdges = []
    isFound, hashiEdges = forwardCheck(hashiGraph, hashiEdges, firstPos, 0)

    if not isFound:
        # No solution exists
        exit()

    # add bridges to the hashiMap now that a valid solution is found
    # symbols: horizontal: - = E, vertical: | " #
    for he in hashiEdges:
        addBridgeFromEdge(he)

    # add spaces to the hashiMap for all leftover dots
    for i in range(len(hashiMap)):
        hashiMap[i] = hashiMap[i].replace('.', ' ')

    for x in hashiMap:
        print(x)