/*
 * CS 106B/X Trailblazer
 * WorldMap is a class representing map-based types of world graphs
 * that draw an image background with circular vertices on top.
 * See WorldMap.cpp for implementation of each member.
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

#ifndef _worldmap_h
#define _worldmap_h

#include "gbufferedimage.h"
#include "gobjects.h"

#include "worldabstract.h"

/**
 * WorldMap is a class representing map-based types of world graphs
 * that draw an image background with circular vertices on top.
 */
class WorldMap : public WorldAbstract {
public:
    /* constants for vertex/edge colors, sizes, thickness, fonts, etc. */
    static const std::string EDGE_COLOR;
    static const double EDGE_LINE_WIDTH;
    static const double VERTEX_LINE_WIDTH;
    static const double VERTEX_RADIUS;
    static const double VERTEX_RADIUS_LARGE_MAP;
    static const std::string VERTEX_FILL_COLOR;
    static const std::string VERTEX_FILL_COLOR_HIGHLIGHTED;
    static const std::string LABEL_FONT_NAME;
    static const int LABEL_FONT_SIZE;
    static const std::string LABEL_FONT_STRING;
    
    /**
     * Constructor; creates a new empty world on the given graphical window.
     */
    WorldMap(GWindow* gwnd);

    /**
     * Destructor; frees the memory used by the world.
     */
    virtual ~WorldMap() Q_DECL_OVERRIDE;
    
    // implementations of pure virtual functions from World class
    // (see world.h for documentation)
    virtual void draw() Q_DECL_OVERRIDE;
    virtual void drawPath(Vector<Vertex*>& path, Color color = RED) Q_DECL_OVERRIDE;
    virtual std::string getDescription(double x, double y) const Q_DECL_OVERRIDE;
    virtual std::string getType() const Q_DECL_OVERRIDE;
    virtual void handleClick(double x, double y) Q_DECL_OVERRIDE;
    virtual void handleMove(double x, double y) Q_DECL_OVERRIDE;
    virtual double heuristic(Vertex* v1, Vertex* v2) Q_DECL_OVERRIDE;
    virtual bool read(std::istream& input) Q_DECL_OVERRIDE;
    virtual void update(Observable<WorldEvent>* obs, const WorldEvent& arg) Q_DECL_OVERRIDE;

private:
    GImage* _backgroundImage;   // background image to draw behind vertices
    bool _heuristicEnabled;     // whether to allow heuristic function (default false)
    
    /**
     * Draws the given edge as a line with arrowhead at the end.
     */
    void drawEdgeArrow(Edge* e) const;

    /**
     * Draws the given vertex as a possibly filled circle of a given color.
     */
    void drawVertexCircle(Vertex* v, std::string color, bool fill = true);

    /**
     * Draws the name/label for a given vertex.
     */
    void drawVertexLabel(Vertex* v);

    /**
     * Maps from x/y positions on screen to vertices in the graph.
     */
    Vertex* getVertex(double x, double y) const;

    /**
     * Returns the radius in px to draw vertexes.
     */
    int getVertexRadius() const;
};

#endif // _worldmap_h
