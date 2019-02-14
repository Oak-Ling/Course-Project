// Board.java
package edu.stanford.cs108.tetris;

import java.util.Arrays;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
*/
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid;
	private boolean DEBUG = true;
	boolean committed;
	
	//widths, heights and maxHeight
	private int[] widths;
	private int[] heights;
	private int maxHeight;
	
	//Backups
	private boolean[][] xGrid;
	private int[] xWidths;
	private int[] xHeights;
	private int xMaxHeight;
	
	// Here a few trivial methods are provided:
	
	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		committed = true;
		
		// YOUR CODE HERE
		widths = new int[height];
		heights = new int[width];
		maxHeight = 0;
	}
	
	
	/**
	 Returns the width of the board in blocks.
	*/
	public int getWidth() {
		return width;
	}
	
	
	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}
	
	
	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {	 
		// YOUR CODE HERE
		return maxHeight; 
	}
	
	
	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			// YOUR CODE HERE
			//iterate the grid
			int[] widthsCheck = new int [height];
			int[] heightsCheck = new int [width];
			int maxHeightCheck = 0;
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (grid[i][j] == true) {
						widthsCheck[j]++;
						if (j + 1> heightsCheck[i]) {
							heightsCheck[i] = j + 1;
						}
					}
				}
				if (heightsCheck[i] > maxHeightCheck)
				maxHeightCheck = heightsCheck[i];
			}
			if (!Arrays.equals(widthsCheck, widths)) {
				throw new RuntimeException("widths array problem");
			}
			if (!Arrays.equals(heightsCheck, heights)) {
				throw new RuntimeException("heights array problem");
			}
			if (maxHeightCheck != maxHeight) {
				throw new RuntimeException("maxHeight problem");
			}
		}
	}
	
	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.
	 
	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {
		// YOUR CODE HERE
		int yMax = Integer.MIN_VALUE;
		int currY;
		int[] skirt = piece.getSkirt();
		for (int i = 0; i < piece.getWidth(); i++) {
			currY = heights[x + i] - skirt[i];
			if (currY > yMax) {
				yMax = currY;
			}
		}
		return yMax; 
	}
	
	
	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	public int getColumnHeight(int x) {
		// YOUR CODE HERE
		return heights[x]; 
	}
	
	
	/**
	 Returns the number of filled blocks in
	 the given row.
	*/
	public int getRowWidth(int y) {
		// YOUR CODE HERE
		return widths[y]; 
	}
	
	
	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		// YOUR CODE HERE
		// out of bound
		if (x < 0 || x > width - 1 || y < 0 || y > height - 1) {
			return true;
		}
		if (grid[x][y] == true) {
			return true;
		}
		return false;
	}
	
	
	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;
	
	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.
	 
	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");
			
		int result = PLACE_OK;
		
		//back up before place pieces
		xGrid = new boolean[width][height];
		xWidths = new int[height];
		xHeights = new int[width];
		xMaxHeight = 0;
		
		for (int i =0; i < width; i++) {
			System.arraycopy(grid[i], 0, xGrid[i], 0, height);
		}
		System.arraycopy(widths, 0, xWidths, 0, height);
		System.arraycopy(heights, 0, xHeights, 0, width);
		xMaxHeight = maxHeight;
		
		// YOUR CODE HERE
		committed = false;
		for (TPoint point : piece.getBody()) {
			int xAxis = point.x + x;
			int yAxis = point.y + y;
			
			//out of bound
			if (xAxis < 0 || xAxis > width - 1 || yAxis < 0 || yAxis > height - 1) {
				result = PLACE_OUT_BOUNDS;
				undo();
				return result;
			}
			
			//overlap
			if (getGrid(xAxis, yAxis)) {
				result = PLACE_BAD;
				undo(); //need to undo all the placed pieces
				return result;
			}
			
			// good to place
 			grid[xAxis][yAxis] = true;
 			widths[yAxis]++;
 			if (heights[xAxis] < yAxis + 1) {
 	 			heights[xAxis] = yAxis + 1;
 			}
 			if (maxHeight < yAxis + 1) {
 				maxHeight = yAxis + 1;
 			}
 			
 			//row filled
 			if (widths[yAxis] == width) {
 				result = PLACE_ROW_FILLED;
 			}
		}
		sanityCheck();
		return result;
	}
	
	
	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	*/
	public int clearRows() {
		int rowsCleared = 0;
		
		//haven't call place() first, need to back up
		if (committed) {
			//initialize back up variables
			xGrid = new boolean[width][height];
			xWidths = new int[height];
			xHeights = new int[width];
			xMaxHeight = 0;
			
			for (int i =0; i < width; i++) {
				System.arraycopy(grid[i], 0, xGrid[i], 0, height);
			}
			System.arraycopy(widths, 0, xWidths, 0, height);
			System.arraycopy(heights, 0, xHeights, 0, width);
			xMaxHeight = maxHeight;
		}
		
		// YOUR CODE HERE
		committed = false;
		int firstFilledRow = height;
		for (int j = 0; j < height; j++) {
			if (widths[j] == width) {
				rowsCleared++;
				if (j < firstFilledRow) {
					firstFilledRow = j;
				}
			}
		}
		if (firstFilledRow == height) {  // no need to clear the rows
			return rowsCleared;
		}
		
		/* try to shift down the grid column by column */
		for (int i = 0; i < width; i++) { 
			// two pointers decide how to copy rows
			int fromRow = firstFilledRow + 1;
			for (int toRow = firstFilledRow; toRow < heights[i]; toRow++) {
				// skip over filled rows and keep moving fromRow
				while (fromRow < heights[i]) {
					if (widths[fromRow] == width) {
						fromRow++;
					} else {
						break;
					}
				}
				//edge case : out of bound
				if (fromRow == heights[i]) {
					grid[i][toRow] = false;
					continue;
				}
				grid[i][toRow] = grid[i][fromRow];
				fromRow++;
			}
			//update heights array
			heights[i] -= rowsCleared;
			//edge case test
			while (heights[i] != 0 && grid[i][heights[i] - 1] == false) {
			    heights[i]--;
            }
		}
		
		//update maxHeight
		maxHeight -= rowsCleared;
		
		//update widths array
		int fromRow = firstFilledRow + 1;
		for (int toRow = firstFilledRow; toRow < height; toRow++) {
			while (fromRow < height) {
				if (widths[fromRow] == width) {
					fromRow++;
				} else {
					break;
				}
			}
			if (fromRow == height) {
				widths[toRow] = 0;
				continue;
			}
			widths[toRow] = widths[fromRow];
			fromRow++;
		}
		sanityCheck();
		return rowsCleared;
	}



	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		// YOUR CODE HERE
		if (committed) { // is already committed, no need to undo anything
			return;
		}
		//roll back to original state
		grid = xGrid;
		widths = xWidths;
		heights = xHeights;
		maxHeight = xMaxHeight;
		commit();
		sanityCheck();
	}
	
	
	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		committed = true;
	}


	
	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility) 
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}
}


