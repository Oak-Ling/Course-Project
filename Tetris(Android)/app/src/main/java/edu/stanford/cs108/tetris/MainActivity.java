package edu.stanford.cs108.tetris;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements TetrisUIInterface {

    BoardView boardView;
    TetrisBrainLogic tetrisBrainLogic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boardView = (BoardView) findViewById(R.id.board_view);
        tetrisBrainLogic = new TetrisBrainLogic(this);
        boardView.setLogic(tetrisBrainLogic);
    }

    boolean gameRunning = false;

    @Override
    public void boardUpdated() {
        boardView.invalidate();
    }

    @Override
    public void dataUpdated() {
        int score = tetrisBrainLogic.getScore();
        TextView scoreText = (TextView) findViewById(R.id.score_text);
        scoreText.setText("Score: " + score);
    }

    @Override
    public void rigGameOver() {
        gameRunning = false;
        handler.removeCallbacks(tetrisTick);
        Button startButton = (Button) findViewById(R.id.start_btn);
        startButton.setEnabled(true);
        Button stopButton = (Button) findViewById(R.id.stop_btn);
        stopButton.setEnabled(false);

        CheckBox testCheckBox = (CheckBox) findViewById(R.id.test_check);
        testCheckBox.setEnabled(true);

        CheckBox brainCheckBox = (CheckBox) findViewById(R.id.brain_check);
        brainCheckBox.setEnabled(true);
    }

    @Override
    public void rigGameInProgress() {
        Button startButton = (Button) findViewById(R.id.start_btn);
        startButton.setEnabled(false);
        Button stopButton = (Button) findViewById(R.id.stop_btn);
        stopButton.setEnabled(true);

        CheckBox testCheckBox = (CheckBox) findViewById(R.id.test_check);
        testCheckBox.setEnabled(false);

        CheckBox brainCheckBox = (CheckBox) findViewById(R.id.brain_check);
        brainCheckBox.setEnabled(false);
    }

    private class TetrisTick implements Runnable {

        @Override
        public void run() {
            if (gameRunning) {
                tetrisBrainLogic.onTick();
                handler.postDelayed(this, getTickDelay());
            }
        }
    }

    Handler handler = new Handler();
    Runnable tetrisTick = new TetrisTick();

//    int tickDelay = 1000;

    int getTickDelay() {
        SeekBar seekBar = (SeekBar) findViewById(R.id.speed);
        int tickDelay = seekBar.getProgress();
        return tickDelay;
    }

    public void onStart(View view) {
        if (gameRunning) return;

        CheckBox testCheckBox = (CheckBox) findViewById(R.id.test_check);
        tetrisBrainLogic.setTestMode(testCheckBox.isChecked());

        CheckBox brainCheckBox = (CheckBox) findViewById(R.id.brain_check);
        tetrisBrainLogic.setBrainMode(brainCheckBox.isChecked());

        tetrisBrainLogic.onStartGame();
        gameRunning = true;
        handler.postDelayed(tetrisTick, getTickDelay());
    }

    public void onStop(View view) {
        tetrisBrainLogic.onStopGame();
        gameRunning = false;
        handler.removeCallbacks(tetrisTick);
    }

    public void handleLeft(View view) {
        if (!gameRunning) return;

        tetrisBrainLogic.onLeft();
    }
    public void handleRight(View view) {
        if (!gameRunning) return;

        tetrisBrainLogic.onRight();
    }
    public void handleRotate(View view) {
        if (!gameRunning) return;

        tetrisBrainLogic.onRotate();
    }
    public void handleDrop(View view) {
        if (!gameRunning) return;

        tetrisBrainLogic.onDrop();
    }

}
