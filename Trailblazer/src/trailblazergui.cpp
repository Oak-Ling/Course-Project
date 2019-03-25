/*
 * CS 106B/X Trailblazer
 * This file implements functions to perform drawing in the graphical user
 * interface (GUI).
 * See trailblazergui.h for documentation of each public function.
 *
 * Please do not modify this provided file. Your turned-in files should work
 * with an unmodified version of all provided code files.
 *
 * @author Marty Stepp, based on past code by Keith Schwarz
 * @version 2018/11/17
 * - 106B 18au version; refactored to use new Qt GUI subsystem
 * - added alt.path option
 * @version 2017/11/16
 * - 17au version; fixed minor compiler warnings
 * @version 2015/06/08
 * - refactored / improved pathVerify, runPathSearch code
 * @version 2014/11/19
 * - initial version for 14au
 */

#include "trailblazergui.h"
#include <cctype>
#include <cmath>
#include <iomanip>
#include <iostream>
#include <limits>
#include <string>
#include "error.h"
#include "filelib.h"
#include "gevents.h"
#include "gfilechooser.h"
#include "gobjects.h"
#include "goptionpane.h"
#include "gthread.h"
#include "gwindow.h"
#include "set.h"
#include "simpio.h"
#include "strlib.h"
#include "timer.h"
#include "vector.h"

#include "trailblazer.h"
#include "world.h"
#include "worldmap.h"
#include "worldmaze.h"
#include "worldterrain.h"

/*static*/ const int TrailblazerGUI::ANIMATION_DELAY_MIN = 1;
/*static*/ const int TrailblazerGUI::ANIMATION_DELAY_MAX = 2000;
/*static*/ const int TrailblazerGUI::ANIMATION_DELAY_DEFAULT = 200;
/*static*/ const double TrailblazerGUI::ALT_PATH_DIFFERENCE_DEFAULT = 0.2;
/*static*/ const std::string TrailblazerGUI::WINDOW_TITLE = "CS 106B Trailblazer";
/*static*/ const std::string TrailblazerGUI::GUI_STATE_FILE = "trailblazer-gui-state.sav";
/*static*/ const std::string TrailblazerGUI::OTHER_FILE_LABEL = "Other file ...";
/*static*/ const bool TrailblazerGUI::SHOULD_SAVE_GUI_STATE = true;
/*static*/ const bool TrailblazerGUI::RANDOM_MAZE_OPTION_ENABLED = true;
/*static*/ const bool TrailblazerGUI::RANDOM_USE_SEED = false;
/*static*/ const bool TrailblazerGUI::SHOW_RUNTIMES = false;

static const std::string ALGO_NAME_DFS = "DFS";
static const std::string ALGO_NAME_BFS = "BFS";
static const std::string ALGO_NAME_DIJKSTRA = "Dijkstra";
static const std::string ALGO_NAME_ASTAR = "A*";
static const std::string ALGO_NAME_ALT_PATH = "Alt.Path";
static const std::string ALGO_NAME_BIDI = "Bidirectional";
static const std::string EMPTY_LABEL_TEXT = " ";

// uncomment these to enable/disable bidirectional search in GUI
// #define BIDIRECTIONAL_SEARCH_ALGORITHM_ENABLED 1
#define ALTERNATE_PATH_ALGORITHM_ENABLED 1

// function implementations

/*
 * Initializes state of the GUI subsystem.
 */
TrailblazerGUI::TrailblazerGUI() {
    _world = nullptr;
    _animationDelay = 0;
    _gtfPositionText = EMPTY_LABEL_TEXT;
    _lastDifference = doubleToString(ALT_PATH_DIFFERENCE_DEFAULT);
    _pathSearchInProgress = false;
    
    // Calculate the intended width and height of the window based on the content
    // area size, the margin size, and the adjustment amount.
    int windowWidth = WorldGrid::WINDOW_WIDTH + 2 * WorldAbstract::WINDOW_MARGIN;
    int windowHeight = WorldGrid::WINDOW_HEIGHT + 2 * WorldAbstract::WINDOW_MARGIN;

    _window = new GWindow(windowWidth, windowHeight);
    _window->setWindowTitle(WINDOW_TITLE);
    _window->setExitOnClose(true);
    _window->setResizable(false);
    _window->center();
    _window->setMouseListener([this](GEvent event) {
        onMouseEvent(event);
    });
    _window->setWindowListener([this](GEvent event) {
        if (event.getEventType() == WINDOW_CLOSED) {
            shutdown();
        }
    });

    // Add the algorithms list.
    _gcAlgorithm = new GChooser();
    _gcAlgorithm->addItem(ALGO_NAME_DFS);
    _gcAlgorithm->addItem(ALGO_NAME_BFS);
    _gcAlgorithm->addItem(ALGO_NAME_DIJKSTRA);
    _gcAlgorithm->addItem(ALGO_NAME_ASTAR);
#ifdef ALTERNATE_PATH_ALGORITHM_ENABLED
    _gcAlgorithm->addItem(ALGO_NAME_ALT_PATH);
#endif // BIDIRECTIONAL_SEARCH_ALGORITHM_ENABLED
#ifdef BIDIRECTIONAL_SEARCH_ALGORITHM_ENABLED
    _gcAlgorithm->addItem(ALGO_NAME_BIDI);
#endif // BIDIRECTIONAL_SEARCH_ALGORITHM_ENABLED

    _gcbDelay = new GCheckBox("Delay");
    _gcbDelay->setChecked(true);
    _gcbDelay->setActionListener([this]() {
        // turn animation delay on/off
        setAnimationDelay(_gcbDelay->isChecked() ? _gsDelay->getValue() : 0);
    });
    _gsDelay = new GSlider(ANIMATION_DELAY_MIN, ANIMATION_DELAY_MAX, ANIMATION_DELAY_DEFAULT);

    _gtfPosition = new GTextField(7);
    _gtfPosition->setText(_gtfPositionText);
    _gtfPosition->setEditable(false);

    // Add in the list of existing world files.
    _gcWorld = new GChooser();
    Set<std::string> worldFiles =
            getFiles("maze") +
            getFiles("terrain") +
            getFiles("map");
    for (std::string worldFile : worldFiles) {
        _gcWorld->addItem(worldFile);
    }
    _gcWorld->addItem(OTHER_FILE_LABEL);
    if (RANDOM_MAZE_OPTION_ENABLED) {
        // gWorldChooser->addItem("Random Maze (tiny)");
        _gcWorld->addItem("Random Maze (small)");
        _gcWorld->addItem("Random Maze (medium)");
        _gcWorld->addItem("Random Maze (large)");
        _gcWorld->addItem("Random Maze (huge)");
        // gWorldChooser->addItem("Random Terrain (tiny)");
        // gWorldChooser->addItem("Random Terrain (small)");
        // gWorldChooser->addItem("Random Terrain (medium)");
        // gWorldChooser->addItem("Random Terrain (large)");
        // gWorldChooser->addItem("Random Terrain (huge)");
    }
    _gcWorld->setSelectedItem("maze01-tiny.txt");   // initially selected

    // north layout
    _gbRun = new GButton("Run", "play.gif");
    _gbRun->setTextPosition(GInteractor::TEXT_BESIDE_ICON);
    _gbRun->setActionListener([this]() {
        onClick_run();
    });

    _gbClear = new GButton("Clear", "cancel.gif");
    _gbClear->setTextPosition(GInteractor::TEXT_BESIDE_ICON);
    _gbClear->setActionListener([this]() {
        onClick_clear();
    });

    // south layout
    _glWorld = new GLabel("World:");

    _gbLoad = new GButton("Load", "load.gif");
    _gbLoad->setTextPosition(GInteractor::TEXT_BESIDE_ICON);
    _gbLoad->setActionListener([this]() {
        // load a world and update the UI
        onClick_load();
    });

    _window->addToRegion(_gcAlgorithm, GWindow::REGION_NORTH);
    _window->addToRegion(_gcbDelay, GWindow::REGION_NORTH);
    _window->addToRegion(_gsDelay, GWindow::REGION_NORTH);
    _window->addToRegion(_gbRun, GWindow::REGION_NORTH);
    _window->addToRegion(_gbClear, GWindow::REGION_NORTH);
    _window->addToRegion(_glWorld, GWindow::REGION_SOUTH);
    _window->addToRegion(_gcWorld, GWindow::REGION_SOUTH);
    _window->addToRegion(_gbLoad, GWindow::REGION_SOUTH);
    // gWindow->addToRegion(new GButton("Exit"), GWindow::REGION_SOUTH);
    _window->addToRegion(_gtfPosition, GWindow::REGION_SOUTH);

    setAnimationDelay(ANIMATION_DELAY_DEFAULT);
    _gsDelay->setValue(_animationDelay);
    if (SHOULD_SAVE_GUI_STATE) {
        stateLoad();
    }
    
    pause(100);
    _window->pack();   // correct the window size
    _window->center();

    intro();

    onClick_load();
}

TrailblazerGUI::~TrailblazerGUI() {
    if (_window) {
        // TODO: delete
//        delete _gcAlgorithm;
//        delete _gcWorld;
//        delete _gsDelay;
//        delete _gtfPosition;
//        delete _glDelay;
//        delete _glWorld;
//        delete _gbClear;
//        delete _gbLoad;
//        delete _gbRun;
//        delete _window;
        _gcAlgorithm = nullptr;
        _gcWorld = nullptr;
        _gsDelay = nullptr;
        _gtfPosition = nullptr;
        _glDelay = nullptr;
        _glWorld = nullptr;
        _gbClear = nullptr;
        _gbLoad = nullptr;
        _gbRun = nullptr;
        _window = nullptr;
    }
    if (_world) {
        delete _world;
        _world = nullptr;
    }
}

void TrailblazerGUI::generateRandomMaze(WorldSize size) {
    if (_world) {
        delete _world;
        _window->repaint();
    }

    printLogMessage();
    printLogMessage("Generating a random maze ...");
    setPathSearchInProgress(true);

    GThread::runInNewThreadAsync([this, size]() {
        WorldMaze* maze = new WorldMaze(_window, size);
        maze->createRandomMaze(size);
        _world = maze;
        World::setCurrentWorld(_world);
        snapConsoleLocation();

        printLogMessage("Preparing world model ...");
        _window->clearCanvas();
        _world->addObserver(this);
        _world->draw();

        printLogMessage("World model completed.");
        setPathSearchInProgress(false);
    });
}

Set<std::string> TrailblazerGUI::getFiles(std::string substr) {
    substr = toLowerCase(substr);
    Vector<std::string> files;
    listDirectory(".", files);
    Set<std::string> result;
    for (std::string file : files) {
        std::string fileLC = toLowerCase(file);
        if (startsWith(fileLC, substr) && endsWith(fileLC, ".txt")) {
            result.add(file);
        }
    }
    return result;
}

GWindow* TrailblazerGUI::getGWindow() const {
    return _window;
}

WorldSize TrailblazerGUI::getWorldSize(std::string worldText) {
    std::string worldLC = toLowerCase(worldText);
    if (worldLC.find("tiny") != std::string::npos) {
        return TINY_WORLD;
    } else if (worldLC.find("small") != std::string::npos) {
        return SMALL_WORLD;
    } else if (worldLC.find("medium") != std::string::npos) {
        return MEDIUM_WORLD;
    } else if (worldLC.find("large") != std::string::npos) {
        return LARGE_WORLD;
    } else if (worldLC.find("huge") != std::string::npos) {
        return HUGE_WORLD;
    } else {
        error("Invalid world size provided.");
        return SMALL_WORLD;
    }
}

void TrailblazerGUI::intro() {
    printLogMessage("Welcome to CS 106B/X Trailblazer!");
    printLogMessage("This program searches for paths through graphs");
    printLogMessage("representing maps, mazes, and rocky terrains.");
    printLogMessage("It demonstrates several graph algorithms for");
    printLogMessage("finding paths, such as depth-first search (DFS),");
    printLogMessage("breadth-first search (BFS), Dijkstra's Algorithm,");
    printLogMessage("and A* search. You can also generate random mazes");
    printLogMessage("using Kruskal's algorithm.");
}

void TrailblazerGUI::loadWorld(std::string worldFile) {
    if (worldFile.empty() || !fileExists(worldFile)) {
        printLogMessage("File not found; aborting.", /* isError */ true);
        return;
    }

    if (_world) {
        delete _world;
        _world = nullptr;
        World::setCurrentWorld(nullptr);
        _window->repaint();
    }

    printLogMessage("Loading world from " + getTail(worldFile) + " ...");
//    _gbLoad->setIcon("progress.gif");
//    _gbLoad->setTextPosition(GInteractor::TEXT_BESIDE_ICON);
    setPathSearchInProgress(true);

    GThread::runInNewThreadAsync([this, worldFile]() {
        loadWorld_inThread(worldFile);
    });
}

void TrailblazerGUI::loadWorld_inThread(std::string worldFile) {
    if (stringContains(worldFile, "maze")) {
        WorldSize size = getWorldSize(worldFile);
        _world = new WorldMaze(_window, size);
    } else if (stringContains(worldFile, "terrain")) {
        WorldSize size = getWorldSize(worldFile);
        _world = new WorldTerrain(_window, size);
    } else if (stringContains(worldFile, "map")) {
        // WorldSize not needed for maps
        _world = new WorldMap(_window);
    }
    World::setCurrentWorld(_world);

    Timer tim;
    if (SHOW_RUNTIMES) {
        tim.start();
    }

    bool readSuccessful = _world->read(worldFile);
    if (readSuccessful) {
        if (SHOW_RUNTIMES) {
            tim.stop();
            printLogMessage("Finished loading file in " + std::to_string(tim.elapsed()) + " ms.");
            printLogMessage("Drawing graphical display of world ...");
            tim.start();
        } else {
            printLogMessage("Preparing world model ...");
        }
        snapConsoleLocation();

        // _window->clearCanvas();
        _world->addObserver(this);
//        world->clearSelection(/* redraw */ false);
//        world->clearPath(/* redraw */ true);
        GThread::runOnQtGuiThreadAsync([this]() {
            _world->draw();
        });

        if (SHOW_RUNTIMES) {
            tim.stop();
            printLogMessage("Finished drawing in " + std::to_string(tim.elapsed()) + " ms.");
        }
        printLogMessage("World model completed.");
    } else {
        printLogMessage(worldFile + " is not a valid world file.", /* isError */ true);
    }

//    _gbLoad->setIcon("load.gif");
//    _gbLoad->setTextPosition(GInteractor::TEXT_BESIDE_ICON);
    _window->repaint();
    setPathSearchInProgress(false);
}

void TrailblazerGUI::onClick_clear() {
    // clearing the display just sets us back to the fresh state
    if (!_world || _pathSearchInProgress) {
        return;
    }

    _world->clearSelection(/* redraw */ false);
    _world->clearPath(/* redraw */ true);
}

void TrailblazerGUI::onClick_load() {
    if (_pathSearchInProgress) {
        return;
    }

    std::string worldFile = _gcWorld->getSelectedItem();
    printLogMessage();
    if (worldFile == OTHER_FILE_LABEL) {
        // prompt for file name
        worldFile = GFileChooser::showOpenDialog(getCurrentDirectory());
    }

    if (!worldFile.empty()) {
        if (startsWith(worldFile, "Random")) {
            // generate a random maze
            WorldSize size = getWorldSize(worldFile);
            generateRandomMaze(size);
        } else {
            // non-random world; just load from a file
            loadWorld(worldFile);
        }
    }
}

void TrailblazerGUI::onClick_run() {
    // rerunning the search is only possible if we already did a search
    if (!_world) {
        printLogMessage("You must load a graph from a file first.");
        return;
    }
    if (_pathSearchInProgress) {
        return;
    }
    if (!_world->hasSelectedVertexes()) {
        printLogMessage("You must select a start and end vertex first.");
        return;
    }

    if (_gcAlgorithm->getSelectedItem() == ALGO_NAME_ALT_PATH) {
        _world->getGraph()->resetData();
        _world->draw();
    } else {
        _world->clearPath();
    }

    runPathSearch();
}

void TrailblazerGUI::onMouseEvent(GEvent e) {
    if (!_world) {
        return;
    }
    if (e.getEventType() == MOUSE_CLICKED) {
        _world->handleClick(e.getX(), e.getY());
    } else if (e.getEventType() == MOUSE_MOVED) {
        // update display of current mouse row/col position to aid testing
        _world->handleMove(e.getX(), e.getY());
        std::string desc = _world->getDescription(e.getX(), e.getY());
        if (desc != _gtfPositionText) {
            _gtfPositionText = desc;
            _gtfPosition->setText(desc);
        }
    }
}

void TrailblazerGUI::pathDisplayInfo(Vector<Vertex*>& path) {
    printLogMessage("Path length: " + std::to_string(path.size()));
    printLogMessage("Path cost: " + realToString(_world->pathComputeCost(path)));
    int greenGray = 0;
    int yellow = 0;
    for (Vertex* v : *(_world->getGraph())) {
        Color color = v->getColor();
        if (color == GREEN || color == GRAY) {
            greenGray++;
        } else if (color == YELLOW) {
            yellow++;
        }
    }
    printLogMessage("Locations visited: " + std::to_string(greenGray));
}

bool TrailblazerGUI::pathVerify(Vector<Vertex*>& path, Vertex* start, Vertex* end) {
    BasicGraph* graph = _world->getGraph();
    
    Vertex* studentStart = path[0];
    Vertex* studentEnd = path[path.size() - 1];
    if (path[0] != start) {
        printLogMessage("Warning: Start of path is not the start location.");
        printLogMessage("         (Expected " + start->name + ", found "
                        + (studentStart ? studentStart->name : std::string("nullptr")), /* isError */ true);
        return false;
    }
    if (path[path.size() - 1] != end) {
        printLogMessage("Warning: End of path is not the end location.");
        printLogMessage("         (Expected " + end->name + ", found "
                        + (studentEnd ? studentEnd->name : std::string("nullptr")), /* isError */ true);
        return false;
    }

    for (int i = 0; i < path.size(); i++) {
        Vertex* v = path[i];
        if (!v) {
            printLogMessage("Invalid path: null vertex at index " + std::to_string(i));
            return false;
        }
        Vertex* vcheck = graph->getVertex(v->name);
        if (vcheck != v) {
            printLogMessage("Invalid path: vertex " + v->name + " at index " + std::to_string(i)
                            + " points to memory not found in the graph", /* isError */ true);
            return false;
        }
        
        if (i > 0) {
            Vertex* prev = path[i - 1];
            Edge* edge = graph->getEdge(prev, v);
            if (!edge) {
                printLogMessage("Invalid path: no edge exists from " + prev->name
                                + " to " + v->name, /* isError */ true);
                return false;
            }
        }
    }
    return true;
}

void TrailblazerGUI::printLogMessage(const std::string& message, bool /*isError*/) {
    std::cout << message << std::endl;
}

void TrailblazerGUI::runPathSearch() {
    updateAnimationDelayFromSlider();

    Vertex* start = _world->getSelectedStart();
    Vertex* end = _world->getSelectedEnd();
    if (!start || !end) {
        return;
    }
    
    setPathSearchInProgress(true);
    std::string algorithmLabel = _gcAlgorithm->getSelectedItem();
    
    printLogMessage();
    printLogMessage("Looking for a path from " + start->name
                    + " to " + end->name + ".");

    // run student's path searching algorithms (in a new thread)
    GThread::runInNewThreadAsync([this, algorithmLabel, start, end]() {
        Vector<Vertex*> path;
        if (algorithmLabel == ALGO_NAME_DFS) {
            printLogMessage("Executing depth-first search algorithm ...");
            path = depthFirstSearch(*_world->getGraph(), start, end);
        } else if (algorithmLabel == ALGO_NAME_BFS) {
            printLogMessage("Executing breadth-first search algorithm ...");
            path = breadthFirstSearch(*_world->getGraph(), start, end);
        } else if (algorithmLabel == ALGO_NAME_DIJKSTRA) {
            printLogMessage("Executing Dijkstra's algorithm ...");
            path = dijkstrasAlgorithm(*_world->getGraph(), start, end);
        } else if (algorithmLabel == ALGO_NAME_ASTAR) {
            printLogMessage("Executing A* algorithm ...");
            path = aStar(*_world->getGraph(), start, end);
    #ifdef ALTERNATE_PATH_ALGORITHM_ENABLED
        } else if (algorithmLabel == ALGO_NAME_ALT_PATH) {
            std::string differenceStr = trim(GOptionPane::showInputDialog("Difference?", "Difference?",
                                         /* initial value */ _lastDifference));
            printLogMessage("Difference? " + differenceStr);
            if (differenceStr.empty() || !stringIsDouble(differenceStr)) {
                setPathSearchInProgress(false);
                return;
            }

            printLogMessage("Executing Alternate Path algorithm ...");
            _lastDifference = differenceStr;
            double difference = stringToDouble(differenceStr);
            path = alternatePath(*_world->getGraph(), start, end, difference);
    #endif // BIDIRECTIONAL_SEARCH_ALGORITHM_ENABLED
    #ifdef BIDIRECTIONAL_SEARCH_ALGORITHM_ENABLED
        } else if (algorithmLabel == ALGO_NAME_BIDI) {
            printLogMessage("Executing Bidirectional Search algorithm ...");
            path = bidirectionalSearch(*_world->getGraph(), start, end);
    #endif // BIDIRECTIONAL_SEARCH_ALGORITHM_ENABLED
        }

        // update the UI to show the path as a line
        runPathSearch_inThread(algorithmLabel, path, start, end);
    });
}

void TrailblazerGUI::runPathSearch_inThread(const std::string& algorithmLabel,
                                            Vector<Vertex*>& path,
                                            Vertex* start, Vertex* end) {
    printLogMessage("Algorithm complete.");

    bool shouldDraw = true;
    if (path.isEmpty()) {
        printLogMessage("No path was found. (The returned path is empty.)");
        shouldDraw = false;
    } else {
        shouldDraw = pathVerify(path, start, end);
    }

    if (shouldDraw) {
        GThread::runOnQtGuiThreadAsync([this, algorithmLabel, &path]() {
            Color color = algorithmLabel == ALGO_NAME_ALT_PATH ? BLUE : RED;
            _world->drawPath(path, color);
            if (_animationDelay == 0) {
                // GUI will not have repainted itself to show the path being drawn;
                // manually repaint it
                _window->repaint();
            }
        });
    }

    pathDisplayInfo(path);
    setPathSearchInProgress(false);
}

void TrailblazerGUI::setAnimationDelay(int delayMS) {
    int oldDelay = _animationDelay;
    _animationDelay = delayMS;

    // tell the window whether or not to repaint on every square colored
    if ((_animationDelay == 0) != (oldDelay == 0)) {
        if (_window) {
            _window->setRepaintImmediately(_animationDelay != 0);
        }
    }
}

void TrailblazerGUI::setPathSearchInProgress(bool inProgress) {
    _pathSearchInProgress = inProgress;
    _gcAlgorithm->setEnabled(!inProgress);
    _gbRun->setEnabled(!inProgress);
    _gbClear->setEnabled(!inProgress);
    _gcWorld->setEnabled(!inProgress);
    _gbLoad->setEnabled(!inProgress);
}

void TrailblazerGUI::shutdown() {
    printLogMessage();
    printLogMessage("Exiting.");
    if (SHOULD_SAVE_GUI_STATE) {
        stateSave();
    }
    _window->close();
}

void TrailblazerGUI::snapConsoleLocation() {
    _gtfPositionText = EMPTY_LABEL_TEXT;
    _gtfPosition->setText(_gtfPositionText);
    GDimension size = _world->getPreferredSize();
    _window->setCanvasSize(size.getWidth(), size.getHeight());
}

bool TrailblazerGUI::stateLoad() {
    std::ifstream input;
    input.open(GUI_STATE_FILE.c_str());
    if (input.fail()) {
        return false;
    }
    std::string algorithm;
    getline(input, algorithm);
    if (input.fail()) {
        return false;
    }

    std::string line;
    getline(input, line);
    if (input.fail()) {
        return false;
    }
    setAnimationDelay(stringToInteger(line));

    std::string worldFile;
    getline(input, worldFile);
    if (input.fail()) {
        return false;
    }
    input.close();

    // delete the save state file in case there is a crash loading a world
    deleteFile(GUI_STATE_FILE);

    _gcAlgorithm->setSelectedItem(algorithm);
    _gsDelay->setValue(_animationDelay);
    if (worldFile != OTHER_FILE_LABEL) {
        _gcWorld->setSelectedItem(worldFile);
    }
    
    return true;
}

bool TrailblazerGUI::stateSave() {
    std::string algorithm = _gcAlgorithm->getSelectedItem();
    int delay = _gsDelay->getValue();
    std::string worldFile = _gcWorld->getSelectedItem();
    std::ofstream output;
    output.open(GUI_STATE_FILE.c_str());
    if (output.fail()) {
        return false;
    }
    output << algorithm << std::endl;
    output << delay << std::endl;
    output << worldFile << std::endl;
    if (output.fail()) {
        return false;
    }
    output.flush();
    output.close();
    return true;
}

void TrailblazerGUI::update(Observable<WorldEvent>* /*obs*/, const WorldEvent& arg) {
    if (arg == EVENT_VERTEX_COLORED) {
        if (_animationDelay > 0) {
            updateAnimationDelayFromSlider();
            pause(_animationDelay);
        }
    } else if (arg == EVENT_PATH_SELECTION_READY) {
        if (_world && _world->hasSelectedVertexes()) {
            runPathSearch();
        }
    }
}

void TrailblazerGUI::updateAnimationDelayFromSlider() {
    // don't set delay from slider if user doesn't want delay
    if (!_gcbDelay->isChecked()) {
        setAnimationDelay(0);
        return;
    }
    int delay = _gsDelay->getValue();
    double percent = 100.0 * delay / ANIMATION_DELAY_MAX;
    
    // convert scale so delays don't increase so rapidly; a bit kludgy
    if (percent <= 0.0) {
        delay = 1;
    } else if (percent <= 10) {
        delay = ANIMATION_DELAY_MAX / 1000;
    } else if (percent <= 20) {
        delay = ANIMATION_DELAY_MAX / 500;
    } else if (percent <= 30) {
        delay = ANIMATION_DELAY_MAX / 200;
    } else if (percent <= 40) {
        delay = ANIMATION_DELAY_MAX / 100;
    } else if (percent <= 50) {
        delay = ANIMATION_DELAY_MAX / 50;
    } else if (percent <= 60) {
        delay = ANIMATION_DELAY_MAX / 25;
    } else if (percent <= 70) {
        delay = ANIMATION_DELAY_MAX / 10;
    } else if (percent <= 80) {
        delay = ANIMATION_DELAY_MAX / 5;
    } else if (percent <= 90) {
        delay = ANIMATION_DELAY_MAX / 2;
    } else {  // percent > 90
        delay = ANIMATION_DELAY_MAX;
    }

    setAnimationDelay(delay);
}


/*
 * Main program.
 */
int main() {
    /*TrailblazerGUI* gui =*/ new TrailblazerGUI();
    return 0;
}
