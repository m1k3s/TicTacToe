package com.pbrane.mike.tictactoe;

import android.app.Activity;
//import android.app.AlertDialog;
import android.content.Context;
//import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private GameView tictactoeView;
	private Canvas canvas;
	private TicTacToe ttt;
	private TextView textView;
	private boolean singlePlayerMode;
//	private boolean networkPlayerMode;
	final Context context = this;
	private Button modeButton;
	private Button resetButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tictactoeView = (GameView)findViewById(R.id.GameView);
		Point size = new Point();
		Display display = getWindowManager().getDefaultDisplay();
		display.getSize(size);
		// the tic-tac-toe board is square, hence the size.x dimensions
		int dims = size.x;
		Bitmap bitmap = Bitmap.createBitmap(dims, dims, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bitmap);
		tictactoeView.setImageBitmap(bitmap);

		// Setup the textview widget
		textView = (TextView)findViewById(R.id.StatusView);
		textView.setTypeface(Typeface.MONOSPACE);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f);
		textView.setTextColor(Color.WHITE);
		textView.setHeight(size.x);

		singlePlayerMode = true; // default to playing the AI
//		networkPlayerMode = false;

		ttt = new TicTacToe(canvas, size);
		ttt.drawBoard();

		// this method happens when it is the players turn
		tictactoeView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				// check for winner or game over or AI's turn
				if (ttt.checkForWinner() || !ttt.hasMovesLeft() || ttt.isAIsTurn()) {
					if (!ttt.hasMovesLeft()) {
						resetButton.setEnabled(true);
					}
					return false;
				}
				switch (event.getAction()) {
					case MotionEvent.ACTION_UP:
						return updateGameState((int)event.getX(), (int)event.getY());
				}
				return true;
			}
		});

		resetButton = (Button)findViewById(R.id.reset_button);
		resetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				canvas.drawColor(Color.BLACK);
				ttt.resetGame();
				ttt.drawBoard();
				resetButton.setEnabled(false);
				displayInGameMessage(-1, ttt.getCurrentPlayerSymbol(), ttt.getNodeLevel(), singlePlayerMode);
				tictactoeView.postInvalidate();
			}
		});

		modeButton = (Button)findViewById(R.id.mode_button);
		modeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				singlePlayerMode = !singlePlayerMode;
				modeButton.setText(singlePlayerMode ? R.string.two_player : R.string.single_player);
				displayInGameMessage(-1, ttt.getCurrentPlayerSymbol(), ttt.getNodeLevel(), singlePlayerMode);
				tictactoeView.postInvalidate();
			}
		});

//		createStartupDialog();

		displayInGameMessage(-1, ttt.getCurrentPlayerSymbol(), ttt.getNodeLevel(), singlePlayerMode);
		modeButton.setText(singlePlayerMode ? R.string.two_player : R.string.single_player);
		modeButton.postInvalidate();
		resetButton.setEnabled(false);
	}

//	private void createStartupDialog() {
//		AlertDialog.Builder builder = new AlertDialog.Builder(context);
//		builder.setMessage("Choose a network game or single player").setTitle("Available Games");
//		builder.setPositiveButton("Network Game", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int id) {
//				networkPlayerMode = true;
//				singlePlayerMode = false;
//				displayInGameMessage(-1, ttt.getCurrentPlayerSymbol(), ttt.getNodeLevel(), singlePlayerMode);
//				modeButton.setText(singlePlayerMode ? R.string.two_player : R.string.single_player);
//				// get the chosen game from the list
//			}
//		});
//
//		builder.setNegativeButton(R.string.single_player, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialogInterface, int i) {
//				networkPlayerMode = false;
//				singlePlayerMode = true;
//				displayInGameMessage(-1, ttt.getCurrentPlayerSymbol(), ttt.getNodeLevel(), singlePlayerMode);
//				modeButton.setText(singlePlayerMode ? R.string.two_player : R.string.single_player);
//			}
//		});
//		AlertDialog dialog = builder.create();
//		dialog.show();
//	}

	private boolean updateGameState(int x, int y) {
		int cell = ttt.findCell(x, y);
		// check if cell is empty
		if (!ttt.cellIsEmpty(cell)) {
			return false;
		}
		// draw the current player symbol
		ttt.setCurrentCell(cell);
		if (ttt.drawPlayer()) {
			ttt.toggleCurrentPlayer();
		}
		// check for winner after this move
		if (ttt.checkForWinner()) {
			ttt.showWinner();
			// display message about the winner
			textView.setText("");
			textView.append(Html.fromHtml("<font color=#0505ff><b>" + String.format("%s has won the game!", ttt.getTheWinnersName()) + "</b></font><br>"));
			textView.append(Html.fromHtml("<font color=#00ff00><b>" + String.format("You're STUPID %s!", ttt.getTheLosersName()) + "</b></font><br>"));
			tictactoeView.postInvalidate();
			resetButton.setEnabled(true);
			return true;
		}
		// toggle player to the computer, and call method
		if (singlePlayerMode) {
			ttt.setAIsTurn(true);
			AIsTurn(x, y);
			if (ttt.checkForWinner()) {
				ttt.showWinner();
				// display message about the winner
				textView.setText("");
				textView.append(Html.fromHtml("<font color=#0505ff><b>" + String.format("%s has won the game!", ttt.getTheWinnersName()) + "</b></font><br>"));
				textView.append(Html.fromHtml("<font color=#00ff00><b>" + String.format("You're a LOSER %s!", ttt.getTheLosersName()) + "</b></font><br>"));
				tictactoeView.postInvalidate();
				resetButton.setEnabled(true);
				return true;
			} else {
				ttt.toggleCurrentPlayer();
			}
		}
		// check if this was the last move, otherwise keep playing
		if (!ttt.hasMovesLeft()) {
			// it's a tie game, display message about the two losers
			textView.setText("");
			textView.append(Html.fromHtml("<font color=#ff0505><b>You're both LOSERS!</b></font><br>"));
			tictactoeView.postInvalidate();
			resetButton.setEnabled(true);
			return true;
		}
		// set new player messages
		displayInGameMessage(cell, ttt.getCurrentPlayerSymbol(), ttt.getNodeLevel(), singlePlayerMode);
		tictactoeView.postInvalidate();
		return true;
	}

	private boolean AIsTurn(int x, int y) {
		boolean result = ttt.AIsMove(x, y);
		ttt.setAIsTurn(false);
		return result;
	}
	private void displayInGameMessage(int cell, String symbol, int node, boolean singlePlayer) {
		textView.setText("");
		textView.append(Html.fromHtml("<font color=#00bfff><b>" + String.format("Player %s's turn", symbol) + "</b></font><br>"));
		textView.append(Html.fromHtml("<font color=#ffff00><b>" + String.format("Cell: %d chosen", cell) + "</b></font><br>"));
		textView.append(Html.fromHtml("<font color=#00ff00><b>" + String.format("Scores:  X: %d   O: %d", ttt.getScore()[0], ttt.getScore()[1]) + "</b></font><br>"));
		textView.append(Html.fromHtml("<font color=#0000ff><b>" + String.format("Two player mode is %s", singlePlayer ? "disabled" : "enabled") + "</b></font><br>"));
		textView.append(Html.fromHtml("<font color=#ff0000><b>" + String.format("Nodes traversed: %d", node) + "</b></font><br>"));
	}

}
