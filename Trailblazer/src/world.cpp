/*
 * CS 106B/X Trailblazer
 * This file contains implementations of the members of the World class.
 * See World.h for declarations and documentation of each member.
 *
 * Please do not modify this provided file. Your turned-in files should work
 * with an unmodified version of all provided code files.
 *
 * @author Marty Stepp, Keith Schwarz, et al
 * @version 2018/11/18
 * - 106B 18au initial version
 * @version 2017/11/16
 * - 17au version; fixed minor compiler warnings
 * @version 2014/11/19
 * - initial version for 14au
 */

#include "world.h"

/*static*/ World* World::_currentWorld = nullptr;

World::~World() {
    // empty
}

/*static*/ void World::setCurrentWorld(World* world) {
    _currentWorld = world;
}

/*static*/ double World::heuristicFunction(Vertex* from, Vertex* to) {
    if (!_currentWorld) {
        return 0.0;
    } else {
        return _currentWorld->heuristic(from, to);
    }
}

double heuristicFunction(Vertex* from, Vertex* to) {
    return World::heuristicFunction(from, to);
}
