/*
 * CS 106B/X Trailblazer
 * This file declares functions to perform drawing in the graphical user
 * interface (GUI).
 * See trailblazergui.cpp for implementation of each function.
 *
 * Please do not modify this provided file. Your turned-in files should work
 * with an unmodified version of all provided code files.
 *
 * @author Marty Stepp, based on past code by Keith Schwarz
 * @version 2017/11/16
 * - 17au version; fixed minor compiler warnings
 * @version 2014/11/19
 * - initial version for 14au
 */

#ifndef _trailblazergui_h
#define _trailblazergui_h

#include "gevent.h"
#include "gbutton.h"
#include "gcheckbox.h"
#include "gchooser.h"
#include "glabel.h"
#include "gslider.h"
#include "gtextfield.h"
#include "gwindow.h"
#include "observable.h"

#include "worldabstract.h"

class TrailblazerGUI : public Observer<WorldEvent> {
public:
    /*
     * Settings for min, max, and initial animation delay in ms.
     */
    static const int ANIMATION_DELAY_MIN;
    static const int ANIMATION_DELAY_MAX;
    static const int ANIMATION_DELAY_DEFAULT;
    static const double ALT_PATH_DIFFERENCE_DEFAULT;

    /*
     * Constructor: Initializes state of the GUI subsystem and shows it on screen.
     */
    TrailblazerGUI();
    
    /*
     * Destructor: Frees up memory used by the GUI.
     */
    virtual ~TrailblazerGUI();
    
    /*
     * Returns access to the GUI's graphical window.
     */
    GWindow* getGWindow() const;
    
    /*
     * Implementation of Observer interface.
     * Called when world is ready to do a path search.
     */
    virtual void update(Observable<WorldEvent>* obs, const WorldEvent& arg);

private:
    // internal constants
    static const std::string WINDOW_TITLE;
    static const std::string GUI_STATE_FILE;
    static const std::string OTHER_FILE_LABEL;
    static const bool SHOULD_SAVE_GUI_STATE;
    static const bool RANDOM_MAZE_OPTION_ENABLED;
    static const bool RANDOM_USE_SEED;   // true to get predictable random mazes
    static const bool SHOW_RUNTIMES;
    
    /*
     * Called when the user wants to create a randomly generated maze.
     */
    void generateRandomMaze(WorldSize size);
    
    /*
     * Returns all files in the current directory that start with the given
     * substring prefix.  Used to find all maze and/or terrain files to display.
     */
    Set<std::string> getFiles(std::string substr);
    
    /*
     * Given the contents of the world size selector, returns a WorldSize encoding
     * the desired world size.
     */
    WorldSize getWorldSize(std::string worldText);

    /*
     * Displays an introductory message.
     */
    void intro();
    
    /*
     * Loads a world graph from the given input file.
     */
    void loadWorld(std::string worldFile);
    void loadWorld_inThread(std::string worldFile);

    /*
     * Functions called when various buttons are clicked.
     */
    void onClick_clear();
    void onClick_load();
    void onClick_run();
    
    /*
     * Displays information about the length, cost, etc. of a given path.
     */
    void pathDisplayInfo(Vector<Vertex*>& path);
    
    /*
     * Checks to make sure that a given path is valid on the current world graph.
     * That is, that all vertices are found in the graph, that they are connected
     * by edges, that it starts/ends with the expected vertices, and so on.
     */
    bool pathVerify(Vector<Vertex*>& path, Vertex* start, Vertex* end);

    /*
     * Displays a log message that shows on the GUI and on the plain text console.
     */
    void printLogMessage(const std::string& message = "", bool isError = false);
    
    /*
     * Reacts to a mouse event in the window.
     */
    void onMouseEvent(GEvent e);

    /*
     * Runs the currently selected path search algorithm.
     */
    void runPathSearch();
    void runPathSearch_inThread(const std::string& algorithmLabel,
                                Vector<Vertex*>& path,
                                Vertex* start, Vertex* end);

    /*
     * Sets the delay between animation frames to the given value.
     */
    void setAnimationDelay(int delayMS);

    void setPathSearchInProgress(bool inProgress = true);

    /*
     * Closes the console and GUI, saves current world state, and exits
     * the program.
     */
    void shutdown();

    /*
     * Sets the console to be aligned directly to the right of the GUI.
     */
    void snapConsoleLocation();

    /*
     * Restores the previously saved GUI state, including which algorithm is
     * currently selected, the animation delay, and the world file chosen.
     * If the saved state does not exist or is corrupt, returns false and
     * uses a default initial state.
     */
    bool stateLoad();
    
    /*
     * Saves the GUI's current state, including which algorithm is
     * currently selected, the animation delay, and the world file chosen.
     * If the saved state does not exist or is corrupt, returns false and
     * uses a default initial state.
     */
    bool stateSave();
    
    /*
     * Reads the delay slider and uses its value to decide on an animation delay.
     */
    void updateAnimationDelayFromSlider();

    // member variables to store various graphical interactors in the GUI
    GWindow* _window;
    GChooser* _gcAlgorithm;
    GChooser* _gcWorld;
    GCheckBox* _gcbDelay;
    GSlider* _gsDelay;
    GTextField* _gtfPosition;   // text field so its width won't change
    GLabel* _glDelay;
    GLabel* _glWorld;
    GButton* _gbClear;
    GButton* _gbLoad;
    GButton* _gbRun;
    WorldAbstract* _world;   // current world being displayed on screen
    int _animationDelay;   // current animation delay in MS between redraws
    std::string _gtfPositionText;   // text to display in gtfPosition (cached)
    std::string _lastDifference;    // last value used as 'difference' for alt.path
    bool _pathSearchInProgress;
};

#endif // _trailblazergui_h
