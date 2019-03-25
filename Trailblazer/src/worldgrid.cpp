/*
 * CS 106B/X Trailblazer
 * This file contains implementations of the members of the WorldGrid class.
 * See WorldGrid.h for declarations and documentation of each member.
 *
 * Please do not modify this provided file. Your turned-in files should work
 * with an unmodified version of all provided code files.
 *
 * @author Marty Stepp, Keith Schwarz, et al
 * @version 2017/11/16
 * - 17au version; fixed minor compiler warnings
 * @version 2014/11/19
 * - initial version for 14au
 */

#include "worldgrid.h"
#include <fstream>
#include <iomanip>
#include "grid.h"
#include "gmath.h"
#include "color.h"

const int WorldGrid::WINDOW_HEIGHT = 500;
const int WorldGrid::WINDOW_WIDTH = 500;

WorldGrid::WorldGrid(GWindow* gwnd, int rowsCols)
        : WorldAbstract(gwnd),
          clickRect1(nullptr),
          clickRect2(nullptr) {
    _windowWidth = WINDOW_WIDTH;
    _windowHeight = WINDOW_HEIGHT;
    _preferredSize = GDimension(_windowWidth + 2 * WINDOW_MARGIN,
                               _windowHeight + 2 * WINDOW_MARGIN);
    worldGrid.resize(rowsCols, rowsCols);
    pxPerWidth = _windowWidth / rowsCols;
    pxPerHeight = _windowHeight / rowsCols;
}

WorldGrid::~WorldGrid() {
    // empty
    clearPath(false);
    clearSelection(false);
    
    // clean up any pointers to allocated data about each vertex/edge
    if (_graph) {
        for (Edge* e : _graph->getEdgeSet()) {
            if (e && e->extraData) {
                delete static_cast<GridLocationPair*>(e->extraData);
                e->extraData = nullptr;
            }
        }
        for (Vertex* v : *_graph) {
            if (v && v->extraData) {
                delete static_cast<GridLocation*>(v->extraData);
                v->extraData = nullptr;
            }
        }
    }
    
    if (_gwnd) {
        _gwnd = nullptr;
    }
}

void WorldGrid::clearSelection(bool redraw) {
    if (clickRect1) {
        _gwnd->remove(clickRect1);
        delete clickRect1;
        clickRect1 = nullptr;
    }
    if (clickRect2) {
        _gwnd->remove(clickRect2);
        delete clickRect2;
        clickRect2 = nullptr;
    }
    
    WorldAbstract::clearSelection(redraw);
}

void WorldGrid::draw() {
    // draw background (once)
    _gwnd->setColor(COLOR_BACKGROUND_STR);
    _gwnd->fillRect(0, 0, _gwnd->getWidth(), _gwnd->getHeight());

    // draw each cell
    bool repaintImm = _gwnd->isRepaintImmediately();
    _gwnd->setRepaintImmediately(false);
    for (int row = 0; row < worldGrid.numRows(); row++) {
        for (int col = 0; col < worldGrid.numCols(); col++) {
            // convert row/col to x/y
            double x, y;
            rowColToXY(row, col, x, y);
            _gwnd->setColor(grayscaleToString(worldGrid.get(row, col)));
            _gwnd->fillRect(x, y, pxPerWidth, pxPerHeight);
        }
    }

    // if (!repaintImm) {
        // issue a repaint so that we see the changes
        _gwnd->repaint();
    // }
    _gwnd->setRepaintImmediately(repaintImm);
}

/*
 * Given a path, draws that path on the screen.
 */
void WorldGrid::drawPath(Vector<Vertex*>& path, Color color) {
    int pathColor = (color == BLUE) ? COLOR_ALT_PATH :
            (color == RED) ? COLOR_PATH : static_cast<int>(color);
    for (int i = 1; i < path.size(); i++) {
        // highlight connection between path[i - 1] and path[i]
        GridLocation* loc1 = static_cast<GridLocation*>(path[i - 1]->extraData);
        GridLocation* loc2 = static_cast<GridLocation*>(path[i]->extraData);
        double srcX, srcY, dstX, dstY;
        findMidpoint(loc1->row, loc1->col, srcX, srcY);
        findMidpoint(loc2->row, loc2->col, dstX, dstY);
        
        GLine* connection = new GLine(srcX, srcY, dstX, dstY);
        connection->setColor(pathColor);
        connection->setLineWidth(PATH_LINE_WIDTH);
        _gwnd->add(connection);
        _highlightedPath.add(connection);
    }
}

/*
 * Given a logical grid location, returns the physical screen coordinates of
 * its midpoint.
 */
void WorldGrid::findMidpoint(int row, int col, double& x, double& y) const {
    rowColToXY(row, col, x, y);
    x += pxPerWidth / 2;
    y += pxPerHeight / 2;
}

std::string WorldGrid::getDescription(double x, double y) const {
    int row, col;
    xyToRowCol(x, y, row, col);
    if (worldGrid.inBounds(row, col)) {
        return vertexName(row, col);
    } else {
        return "";
    }
}

BasicGraph* WorldGrid::gridToGraph(const Grid<double>& world) {
    BasicGraph* bgraph = new BasicGraph();

    // add vertices
    int rows = world.numRows();
    int cols = world.numCols();
    
    for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
            std::string name = vertexName(r, c);
            Vertex* v = new Vertex(name);
            v->extraData = new GridLocation(r, c);
            v->addObserver(this);
            bgraph->addVertex(v);
        }
    }

    // add edges
    for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
            Vertex* v = bgraph->getVertex(vertexName(r, c));
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = r + dr;
                    int nc = c + dc;
                    if ((dr == 0 && dc == 0) || !world.inBounds(nr, nc)) {
                        continue;
                    }
                    Vertex* neighbor = bgraph->getVertex(vertexName(nr, nc));
                    double cost = costFunction(r, c, nr, nc);
                    if (cost < POSITIVE_INFINITY) {
                        Edge* e = new Edge(v, neighbor, cost);
                        e->extraData = new GridLocationPair(r, c, nr, nc);
                        bgraph->addEdge(e);
                    }
                }
            }
        }
    }

    return bgraph;
}

void WorldGrid::handleClick(double x, double y) {
    // double-convert to get top/left corner of nearest grid square
    int row, col;
    xyToRowCol(x, y, row, col);
    rowColToXY(row, col, x, y);
    if (!worldGrid.inBounds(row, col)) {
        return;
    }
    std::string name = vertexName(row, col);
    
    GRect* rect = new GRect(x, y, pxPerWidth, pxPerHeight);
    rect->setColor(COLOR_SELECTION);
    rect->setLineWidth(2);
    _gwnd->add(rect);
    rect->sendToFront();
    if (!_gwnd->isRepaintImmediately()) {
        _gwnd->repaint();
    }
    
    if (!clickRect1) {
        // first of two clicks
        clickRect1 = rect;
        _selectedStart = _graph->getVertex(name);
    } else if (!clickRect2) {
        // second of two clicks
        clickRect2 = rect;
        _selectedEnd = _graph->getVertex(name);
        
        // both click rectangles are ready now, so notify GUI
        // so that it can do its path search
        notifyObservers(EVENT_PATH_SELECTION_READY);
    } else {
        // already did both clicks; third click starts over
        clearSelection(false);
        clearPath(true);
        clickRect1 = rect;
        _selectedStart = _graph->getVertex(name);
    }
}

void WorldGrid::handleMove(double /*x*/, double /*y*/) {
    // empty
}

double WorldGrid::heuristic(Vertex* v1, Vertex* v2) {
    GridLocation* loc1 = (GridLocation*) v1->extraData;
    GridLocation* loc2 = (GridLocation*) v2->extraData;
    return heuristic(loc1->row, loc1->col, loc2->row, loc2->col);
}

bool WorldGrid::read(std::istream& input) {
    std::string worldType = getType();
    double min = 0.0;
    double max = 1.0;
    try {
        // Enable exceptions on the stream so that we can handle errors using try-
        // catch rather than continuously testing everything.
        input.exceptions(std::ios::failbit | std::ios::badbit);

        // The file line of the file identifies the type, which should be either
        // "terrain" or "maze."
        std::string type;
        input >> type;

        if (type != worldType) {
            std::cerr << "world file must contain type '" << worldType << "' as first line." << std::endl;
            return false;
        }

        // Read the size of the world.
        int numRows, numCols;
        input >> numRows >> numCols;

        if (numRows <= 0 || numCols <= 0 ||
                numRows >= WorldAbstract::MAX_ROWS || numCols >= WorldAbstract::MAX_COLS) {
            std::cerr << "world file contains invalid number of rows/cols: "
                 << numRows << "," << numCols << std::endl;
            return false;
        }

        worldGrid.resize(numRows, numCols);

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                double value;
                if (!(input >> value)) {
                    std::cerr << "Illegal input file format; row #" << (row+1)
                         << "does not contain " << numCols << " valid numbers" << std::endl;
                    return false;
                }

                // Validate the input based on the type of world.
                if (value < min || value > max) {
                    std::cerr << "world file contains invalid square value of " << value
                         << ", must be " << min << " - " << max << std::endl;
                    return false;
                }
                worldGrid.set(row, col, value);
            }
        }
        
        // now convert the newly read grid into an equivalent BasicGraph
        _graph = gridToGraph(worldGrid);

        return true;
    } catch (...) {
        // Something went wrong, so report an error.
        std::cerr << "exception thrown while reading world file" << std::endl;
        return false;
    }
}

void WorldGrid::rowColToXY(int row, int col, double& x, double& y) const {
    x = col * pxPerWidth + WINDOW_MARGIN;
    y = row * pxPerHeight + WINDOW_MARGIN;
}

void WorldGrid::update(Observable<WorldEvent>* obs, const WorldEvent& /*arg*/) {
    Vertex* v = (Vertex*) obs;
    if (!v) {
        return;
    }
    GridLocation* loc = (GridLocation*) v->extraData;
    if (!loc || !worldGrid.inBounds(loc->row, loc->col)) {
        return;
    }
    int r, g, b;
    colorToRGB(v->getColor(), r, g, b);
    scaleBrightness(worldGrid[loc->row][loc->col], r, g, b);
    std::string color = rgbToColor(r, g, b);
    double x, y;
    rowColToXY(loc->row, loc->col, x, y);
    _gwnd->setColor(color);
    _gwnd->fillRect(x, y, pxPerWidth, pxPerHeight);
    notifyObservers(EVENT_VERTEX_COLORED);
}

std::string WorldGrid::vertexName(int row, int col, int digits) const {
    if (digits <= 0) {
        digits = countDigits(std::max(worldGrid.numRows(), worldGrid.numCols()));
    }
    std::ostringstream out;
    out << 'r' << std::setw(digits) << std::setfill('0') << row
        << 'c' << std::setw(digits) << std::setfill('0') << col;
    return out.str();
}

void WorldGrid::xyToRowCol(double x, double y, int& row, int& col) const {
    row = (int) ((y - WINDOW_MARGIN) / pxPerHeight);
    col = (int) ((x - WINDOW_MARGIN) / pxPerWidth);
}
