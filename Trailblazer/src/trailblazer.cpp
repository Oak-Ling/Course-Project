/*
 * Rui Ling
 * ruiling@stanford.edu
 * pair with
 * Jiushuang Guo
 * jguo18@stanford.edu
 * Sources of help: cs106b.stanford.edu
 *                  function shown in the class slides
 *                  stanford library to check out description and usage of the functions listed in the slides
 *                  c++ standard database
 */

#include "trailblazer.h"
#include "queue.h"
#include "priorityqueue.h"

using namespace std;

/**
 * helper function to find a path between two vertices by exploring each possible path as far as possible before backtracking, return true if a path can be found
 */
bool depthFirstSearchHelper(BasicGraph& graph, Vertex* start, Vertex* end, Vector <Vertex*>& path);

/**
 * find the minimum-weight path between a pair of vertices in a weighted directed graph(this helper function can do both dijkstrasAlgorithm and aStar)
 */
Vector<Vertex*> shortPathHelper(BasicGraph& graph, Vertex* start, Vertex* end, Edge* ignoredEdge, int methodNum);

/**
 * generate the path we find according to the map we have built
 */
void createPath(Map<Vertex*, Vertex*>& previousPath, Vector<Vertex*>& path, Vertex* start, Vertex* end);

/**
 * calculate the difference between two paths
 */
double differenceCount(Vector<Vertex*>& currPath, Set<Vertex*>& pathNode, double count2);

/**
 * calculate the total weight of a given path
 */
double weightCount(BasicGraph& graph, Vector<Vertex*>& currPath);

/**
 * to check whether the given two vertexes are connected to each other
 */
bool checkConnected(Vertex* start, Vertex* finish, Map<int,Set<Vertex*>>& cluster, int& serialNumber);

/**
 * to find which cluster does the given vertex belongs to
 */
int findVertex(Vertex* node, Map<int,Set<Vertex*>>& cluster);



//find a path between two vertices by exploring each possible path as far as possible before backtracking
Vector<Vertex*> depthFirstSearch(BasicGraph& graph, Vertex* start, Vertex* end) {  
    graph.resetData(); //to make sure that no stale data is left in the vertexes from some prior call
    Vector<Vertex*> path; //store the path for dfs
    depthFirstSearchHelper(graph, start, end, path);
    return path;
}

//helper function to find a path between two vertices by exploring each possible path as far as possible before backtracking, return true if a path can be found
bool depthFirstSearchHelper(BasicGraph& graph, Vertex* start, Vertex* end, Vector <Vertex*>& path){
    start->setColor(GREEN); //mark the start vertex as visited
    path.add(start); //add to path, choose
    if (start==end){ //base case: dfs succeed
        return true;
    } else {
        for (Vertex* n: graph.getNeighbors(start)){
            if (n->getColor()!=GREEN && n->getColor()!=GRAY){  //check whether the neighbors are unvisited
                if (depthFirstSearchHelper(graph,n,end,path)){ // explore
                    return true; // if a path has been found, exit without checking other neighbours
                }
            }
        }
        start->setColor(GRAY); //all neighbors fail, mark it as gray(visited but failed)
        path.pop_back(); //pop out from the path, unchoose
        return false;
    }
}

//find a path between two nodes by taking one step down all paths and then immediately backtracking
Vector<Vertex*> breadthFirstSearch(BasicGraph& graph, Vertex* start, Vertex* end) {
    graph.resetData(); //to make sure that no stale data is left in the vertexes from some prior call
    Vector<Vertex*> path; //store the path for bfs
    Queue<Vertex*> visit; //store the vertex to be visited
    Set<Vertex*> usedVertex; //record if the vertex has already be added into the queue
    Map<Vertex*, Vertex*> previousPath; //store a reference to a previous vertex

    visit.enqueue(start);  //initially store the start vertex
    usedVertex.add(start); //make a record of start vertex indicating that it has been added into the queue
    while (!visit.isEmpty()){ //check whether we can find the path
        Vertex* front=visit.dequeue();
        front->setColor(GREEN); //mark it visited visually
        if (front==end){ //if we find a path, break the while loop
            path.add(start);
            createPath(previousPath,path,start,end);//generate the path we find
            break;
        }
        for (Vertex* n: graph.getNeighbors(front)){ //add each unvisited neighbor n to the queue
            if (!usedVertex.contains(n)){ //check whether unvisited
                visit.enqueue(n);
                usedVertex.add(n); //record it visited
                n->setColor(YELLOW);
                previousPath.put(n,front); //store a reference to a previous vertex
            }
        }
    }
    return path;
}

//find the minimum-weight path between a pair of vertices in a weighted directed graph
Vector<Vertex*> dijkstrasAlgorithm(BasicGraph& graph, Vertex* start, Vertex* end) {
    return shortPathHelper(graph, start, end, nullptr, 0); //nullptr means no edge need to be ignored, 0 means no need to consider heuristic value
}

//a modified version of Dijkstra's algorithm that uses a heuristic function to guide its order of path exploration
Vector<Vertex*> aStar(BasicGraph& graph, Vertex* start, Vertex* end) {
    return shortPathHelper(graph, start, end, nullptr, 1);//nullptr means no edge need to be ignored, 1 means we need to consider heuristic value
}

//return the lowest weight alternate route that is sufficiently different from the original best path given by aStar algorithm
Vector<Vertex*> alternatePath(BasicGraph& graph, Vertex* start, Vertex* end, double difference) {
    graph.resetData();
    //path to store the best path we find/tempPath to store the current path with one ignored edge/alterPath to store the final alternative path
    Vector<Vertex*> path,tempPath,alterPath;
    path=shortPathHelper(graph, start, end, nullptr, 1); //search for best path with aStar algorithm
    double totalWeight=POSITIVE_INFINITY; //store the minimum total weight of candidate path we find
    Set<Vertex*> pathNode; //store the all the nodes in the best path
    double count=0; //count how many nodes are in the best path
    for (Vertex* node:path){
        pathNode.add(node);
        count++;
    }

    for (int i=0; i<path.size()-1; i++){ //start to find candidate path by ignore some edges in the best path
        Edge* ignoredEdge=graph.getEdge(path[i],path[i+1]);
        tempPath=shortPathHelper(graph, start, end, ignoredEdge, 1);
        double currWeight=weightCount(graph, tempPath); //count the total weight for the current path
        if (differenceCount(tempPath,pathNode,count)>difference && currWeight<totalWeight){ //record the path which meets the requirement
            alterPath=tempPath;
            totalWeight=currWeight;
        }
    }
    return alterPath;
}

//calculate the difference between two paths
double differenceCount(Vector<Vertex*>& currPath, Set<Vertex*>& pathNode, double count2){
    double count1=0; //count how many nodes in the current path that are not in best path
    for (Vertex* node:currPath){
        if (!pathNode.contains(node)){
            count1++;
        }
    }
    return count1/count2; //difference (P,B)=(nodes in P that are not in B)/(nodes in B)
}

//calculate the total weight of a given path
double weightCount(BasicGraph& graph, Vector<Vertex*>& currPath){
    double weightSum=0; //count the total weight of the current path
    for (int i=0; i<currPath.size()-1; i++){
        Edge* e=graph.getEdge(currPath[i],currPath[i+1]);
        weightSum+=e->weight;
    }
    return weightSum;
}

//find the minimum-weight path between a pair of vertices in a weighted directed graph(this helper function can do both dijkstrasAlgorithm and aStar)
Vector<Vertex*> shortPathHelper(BasicGraph& graph, Vertex* start, Vertex* end, Edge* ignoredEdge, int methodNum){
    graph.resetData(); //to make sure that no stale data is left in the vertexes from some prior call
    Vector<Vertex*> path; //store the final path
    PriorityQueue<Vertex*> pq; //store the vertex to be visited with priority
    Map<Vertex*, double> vertexCost;//keep records of the cost of each vertex
    Map<Vertex*, Vertex*> previousPath; //store a reference to a previous vertex

    vertexCost.put(start,0);//initialize the start vertex with a cost of 0
    double heuristicValue=0; //if it's for dijkstra algorithm, no need to consider heuristic value
    if (methodNum) { //aStar or alterPath (with methodNum=1), dijkstrasAlgorithm (with methodNum=0)
        heuristicValue=heuristicFunction(start,end);
    }
    pq.enqueue(start,vertexCost[start]+heuristicValue);  //put the start vertex into the priority queue
    while (!pq.isEmpty()){ //check whether we can find the path
        Vertex* front=pq.dequeue();
        front->setColor(GREEN); //mark it visited visually
        if (front==end){ //if we find a path, break the while loop
            path.add(start);
            createPath(previousPath,path,start,end);
            break;
        }
        for (Vertex* n: graph.getNeighbors(front)){ //add each unvisited neighbor n to the priority queue
            if (n->getColor()!=GREEN){ //check whether unvisited
                Edge* e=graph.getEdge(front,n);
                if (e==ignoredEdge) {continue;} //check whether current edge is the edge which needs to be ignored, if so, skip the edge
                if (methodNum) { //aStar or alterPath
                    heuristicValue=heuristicFunction(n,end);
                }
                if (vertexCost.containsKey(n)){ //if neighbor n is already in the queue
                    if (vertexCost[n]>vertexCost[front]+e->weight){ //if the new cost is cheaper than n's current cost
                        vertexCost[n]=vertexCost[front]+e->weight; //change the current cost
                        pq.changePriority(n,vertexCost[n]+heuristicValue); //change the current priority
                        previousPath.put(n,front); //store a reference to a previous vertex
                    }
                } else { //if the neighbor n is not in the queue
                    vertexCost[n]=vertexCost[front]+e->weight;//set the cost
                    pq.enqueue(n,vertexCost[n]+heuristicValue);
                    n->setColor(YELLOW);
                    previousPath.put(n,front); //store a reference to a previous vertex
                }
            }
        }
    }
    return path;
}

//generate the path we find according to the map we have built
void createPath(Map<Vertex*, Vertex*>& previousPath, Vector<Vertex*>& path, Vertex* start, Vertex* end){
    Vertex* key; //retrieve the path from the map
    key=end;
    Stack<Vertex*> reversePath; //store the path into a stack in order to change the order of the path
    while (key!=start){
        reversePath.push(key); //store the path
        key=previousPath[key];
    }
    while (!reversePath.isEmpty()){
        path.add(reversePath.pop()); //change the order of the path and store them into the vector
    }
}

//find a minimum spanning tree in a given graph
Set<Edge*> kruskal(BasicGraph& graph) {
    graph.resetData(); //to make sure that no stale data is left in the vertexes from some prior call
    Set<Edge*> mst; //a set of pointers to edges in the graph such that those edges would connect the graph's vertexes into a minimum spanning tree
    PriorityQueue<Edge*> pq; //store the edge with weight as their priority
    Map<int,Set<Vertex*>> cluster; //a numbered map to store the clusters of vertices
    for (Edge* e:graph.getEdgeSet()){
        pq.enqueue(e,e->weight);
    }
    int serialNumber=1; /*
                         * this variable is used in the checkConnected function to mark the serial number of cluster in a map
                         * the map will start store the cluster of vertices in the number 1
                         * this gives each cluster a unique number so that we can trace the vertex later
                         */
    while (!pq.isEmpty()){ //check all the edge in the priority queue
        Edge* e=pq.dequeue();
        if (!checkConnected(e->start,e->finish,cluster,serialNumber)){ //check whether the given vertices are already connected
            mst.add(e);
        }
    }
    return mst;
}

//to check whether the given two vertexes are connected to each other
bool checkConnected(Vertex* start, Vertex* finish, Map<int,Set<Vertex*>>& cluster, int& serialNumber){
    int number1,number2; //store which cluster does the given vertex belongs to

    number1=findVertex(start, cluster);
    number2=findVertex(finish, cluster);
    if (number1==0 && number2==0){ //the given two vertices are not in any cluster, add them into one cluster
        Set<Vertex*> tempCluster; //put the given two vertices into one set
        tempCluster.add(start);
        tempCluster.add(finish);
        cluster[serialNumber]=tempCluster; //put the set into a map
        serialNumber++;
        return false;
    } else if (number1==0 && number2!=0){ //the first vertex is not in any cluster and the second is in one of the clusters
        cluster[number2].add(start);//add first vertex to the second one's cluster
        return false;
    } else if (number1!=0 && number2==0){//the second vertex is not in any cluster and the first is in one of the clusters
        cluster[number1].add(finish);//add second vertex to the first one's cluster
        return false;
    } else { // number1!=0 && number2!=0, both vertices are in one of the clusters
        if (number1==number2){ //two vertices are in the same cluster, connected
            return true;
        } else { //two vertices are in different clusters, merge two clusters
            cluster[number1]+=cluster[number2];
            cluster.remove(number2);
            return false;
        }
    }
}

//to find which cluster does the given vertex belongs to
int findVertex(Vertex* node, Map<int,Set<Vertex*>>& cluster){
    for (int i: cluster.keys()){
        if (cluster[i].contains(node)){
            return i; //return the serial number of the cluster which the vertex belongs to
        }
    }
    return 0; //the given vertex is not in any cluster
}
