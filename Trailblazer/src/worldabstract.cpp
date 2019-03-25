/*
 * CS 106B/X Trailblazer
 * This file contains implementations of the members of the World class.
 * See World.h for declarations and documentation of each member.
 *
 * Please do not modify this provided file. Your turned-in files should work
 * with an unmodified version of all provided code files.
 *
 * @author Marty Stepp, Keith Schwarz, et al
 * @version 2017/11/16
 * - 17au version; modified to support large maps/worlds
 * @version 2014/11/19
 * - initial version for 14au
 */

#include "worldabstract.h"
#include <fstream>
#include <iostream>
#include "error.h"

const int WorldAbstract::MAX_ROWS = 400;
const int WorldAbstract::MAX_COLS = 400;
const int WorldAbstract::WINDOW_MARGIN = 5;
const double WorldAbstract::PATH_LINE_WIDTH = 3.0;
const std::string WorldAbstract::COLOR_ALT_PATH_STR("#0000bb");
const std::string WorldAbstract::COLOR_BACKGROUND_STR("Black");
const std::string WorldAbstract::COLOR_HOVER_STR("#bbbb00");
const std::string WorldAbstract::COLOR_PATH_STR("Red");
const std::string WorldAbstract::COLOR_SELECTION_STR("Red");
const int WorldAbstract::COLOR_ALT_PATH = 0x0000bb;
const int WorldAbstract::COLOR_BACKGROUND = 0x0;
const int WorldAbstract::COLOR_HOVER = 0xbbbb00;
const int WorldAbstract::COLOR_PATH = 0xff0000;
const int WorldAbstract::COLOR_SELECTION = 0xff0000;
const int WorldAbstract::LARGE_WORLD_THRESHOLD = 500;

WorldAbstract::WorldAbstract(GWindow* gwnd)
        : _gwnd(gwnd),
          _windowWidth(0),
          _windowHeight(0),
          _graph(nullptr),
          _selectedStart(nullptr),
          _selectedEnd(nullptr) {
    _windowWidth = gwnd->getWidth() - 2 * WINDOW_MARGIN;
    _windowHeight = gwnd->getHeight() - 2 * WINDOW_MARGIN;
}

WorldAbstract::~WorldAbstract() {
    if (_graph) {
        delete _graph;
        _graph = nullptr;
    }
}


void WorldAbstract::clearPath(bool redraw) {
    bool repaintImm = _gwnd->isRepaintImmediately();
    if (isLargeWorld()) {
        _gwnd->setRepaintImmediately(false);
    }
    for (GLine* line : _highlightedPath) {
        _gwnd->remove(line);
        delete line;
    }
    _highlightedPath.clear();
    
    if (redraw) {
        draw();
        if (!_gwnd->isRepaintImmediately()) {
            _gwnd->repaint();
        }
    }

    if (isLargeWorld()) {
        _gwnd->setRepaintImmediately(repaintImm);
    }
}

void WorldAbstract::clearSelection(bool redraw) {
    setSelectedStart(nullptr);
    setSelectedEnd(nullptr);
    if (redraw) {
        draw();
        if (!_gwnd->isRepaintImmediately()) {
            _gwnd->repaint();
        }
    }
}

BasicGraph* WorldAbstract::getGraph() const {
    return _graph;
}

const GDimension& WorldAbstract::getPreferredSize() const {
    return _preferredSize;
}

Vertex* WorldAbstract::getSelectedStart() const {
    return _selectedStart;
}

Vertex* WorldAbstract::getSelectedEnd() const {
    return _selectedEnd;
}

bool WorldAbstract::hasSelectedVertexes() const {
    return _selectedStart != nullptr && _selectedEnd != nullptr;
}

bool WorldAbstract::isLargeWorld() const {
    return _graph->getVertexSet().size() > LARGE_WORLD_THRESHOLD
            || _graph->getEdgeSet().size() > LARGE_WORLD_THRESHOLD;
}

double WorldAbstract::pathComputeCost(Vector<Vertex*>& path) const {
    BasicGraph* graph = getGraph();
    double result = 0.0;
    for (int i = 1; i < path.size(); i++) {
        Edge* edge = graph->getEdge(path[i - 1], path[i]);
        if (edge) {
            result += edge->cost;
        } else {
            error("no edge found from " + path[i - 1]->name + " to " + path[i]->name);
        }
    }
    return result;
}

bool WorldAbstract::read(const std::string& filename) {
    std::ifstream input;
    input.open(filename.c_str());
    if (input.fail()) {
        return false;
    }
    return read(input);
}

void WorldAbstract::setSelectedStart(Vertex* v) {
    _selectedStart = v;
}

void WorldAbstract::setSelectedEnd(Vertex* v) {
    _selectedEnd = v;
}

bool WorldAbstract::getNonEmptyLine(std::istream& input, std::string& line) {
    std::string lineOut;
    while (getline(input, lineOut)) {
        trimInPlace(lineOut);
        if (!lineOut.empty() && lineOut[0] != '#') {
            line = lineOut;
            return true;
        }
    }
    return false;
}
