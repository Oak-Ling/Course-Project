/*
 * CS 106B/X Trailblazer
 * World is an interface (pure virtual class) representing different types
 * of world graphs. Each type of world is a subclass of this class.
 *
 * Please do not modify this provided file. Your turned-in files should work
 * with an unmodified version of all provided code files.
 *
 * @author Marty Stepp, Keith Schwarz, et al
 * @version 2018/11/18
 * - 106B 18au initial version
 * @version 2014/11/19
 * - initial version for 14au
 */

#ifndef _world_h
#define _world_h

#include "basicgraph.h"

/**
 * Returns a 'heuristic' value, or rough estimation, of the distance between
 * this vertex and the given other vertex, in the current world.
 * The heuristic function is guaranteed to be an 'admissible heuristic',
 * meaning that it is never an overestimation of the distance.
 */
double heuristicFunction(Vertex* from, Vertex* to);

class TrailblazerGUI;   // forward declaration

/**
 * World is an interface (pure virtual class) representing different types
 * of world graphs. Each type of world is a subclass of this class.
 */
class World {
public:
    /**
     * Destructor; frees the memory used by the world.
     */
    virtual ~World();

    /**
     * Returns the graph that represents this world.
     */
    virtual BasicGraph* getGraph() const = 0;
    
    /**
     * Returns an estimation or 'heuristic' about the distance
     * between vertices v1 and v2.
     * The heuristic function is guaranteed to be an 'admissible heuristic',
     * meaning that it is never an overestimation of the distance.
     * This can be used in path-search algorithms such as A*.
     */
    virtual double heuristic(Vertex* v1, Vertex* v2) = 0;

private:
    /**
     * Returns a 'heuristic' value, or rough estimation, of the distance between
     * this vertex and the given other vertex, in the current world.
     * The heuristic function is guaranteed to be an 'admissible heuristic',
     * meaning that it is never an overestimation of the distance.
     * (The word 'extern' means this function is defined elsewhere.
     *  You do not need to worry about it.)
     */
    static double heuristicFunction(Vertex* from, Vertex* to);

    /**
     * Sets current world in use so that heuristic function will work.
     */
    static void setCurrentWorld(World* world);

    // pointer to currently selected world
    static World* _currentWorld;

    // GUI is allowed to access world private stuff
    friend class TrailblazerGUI;

    // global heuristic function is allowed to call World's
    friend double ::heuristicFunction(Vertex* from, Vertex* to);
};

#endif // _world_h
