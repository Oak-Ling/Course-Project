package edu.stanford.cs108.tetris;

public class TetrisBrainLogic extends TetrisLogic {

    private DefaultBrain defaultBrain;
//    private  MyCleverBrain myCleverBrain;
    protected boolean brainMode = false;

    public TetrisBrainLogic(TetrisUIInterface uiInterface) {
        super(uiInterface);
        defaultBrain = new DefaultBrain();
//        myCleverBrain = new MyCleverBrain();
    }

    @Override
    protected void tick(int verb) {
//		System.out.println("In super.tick: " + verb);
        if (brainMode) {
            if (verb != DOWN) {
                super.tick(verb);
            } else {
                board.undo();
//                Brain.Move move = myCleverBrain.bestMove(board, currentPiece, HEIGHT, null);
                Brain.Move move = defaultBrain.bestMove(board, currentPiece, HEIGHT, null);
                if (move != null) {
                    if (!move.piece.equals(currentPiece)) {
                        super.tick(ROTATE);
                    }

                    if (currentX < move.x) {
                        super.tick(RIGHT);
                    } else if (currentX > move.x) {
                        super.tick(LEFT);
                    }
                }
                super.tick(DOWN);
            }
        } else {
            super.tick(verb);
        }
    }

    public void setBrainMode(boolean brain) {
        brainMode = brain;
    }
}
