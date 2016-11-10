package com.pbrane.mike.tictactoe;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// the tic-tac-toe game using minimax with alpha beta pruning AI

public class TicTacToe {

	static final int X_SCORE = 0;
	static final int O_SCORE = 1;
	private static final int lineWidth = 30;

	private final int nCells = 9;
	private Canvas canvas;
	private Point size;
	private int cell_size;
	private int currentCell;
	private int currentPlayer;
	private int[] board;
	private List<Point> layout = new ArrayList<>(nCells);
	private int theWinner;
	private int[] winning;
	private boolean AIsTurn;
	private int nodeLevel;
	private int[] score;

	// These are the 8 possible 3-in-a-row winning patterns...
	private int pattern[][] = {
			{0, 1, 2},
			{3, 4, 5},
			{6, 7, 8},
			{0, 3, 6},
			{1, 4, 7},
			{2, 5, 8},
			{2, 4, 6},
			{0, 4, 8}
	};


	private enum cellType {
		INVALID_CELL(-3),
		TTT_IN_PLAY(-2),
		TTT_TIE(-1),
		TTT_EMPTY(0),
		TTT_X(1),
		TTT_O(2);

		cellType(int i) {
			this.value = i;
		}

		public int value;
	}

	public TicTacToe(Canvas canvas, Point size) {
		this.canvas = canvas;
		this.size = size;
		this.cell_size = size.x / 3;
		currentCell = cellType.INVALID_CELL.value;
		currentPlayer = cellType.TTT_X.value;

		board = new int[nCells];
		Arrays.fill(board, cellType.TTT_EMPTY.value);

		score = new int[2]; // X and O score
		Arrays.fill(board, 0);

		theWinner = cellType.TTT_TIE.value;
		winning = new int[3];
		AIsTurn = false; // player goes first

		layout.add(new Point(0, 0));
		layout.add(new Point(cell_size, 0));
		layout.add(new Point(cell_size * 2, 0));
		layout.add(new Point(0, cell_size));
		layout.add(new Point(cell_size, cell_size));
		layout.add(new Point(cell_size * 2, cell_size));
		layout.add(new Point(0, cell_size * 2));
		layout.add(new Point(cell_size, cell_size * 2));
		layout.add(new Point(cell_size * 2, cell_size * 2));
	}

	int[] getScore() {
		return score;
	}

	public int getNodeLevel() {
		return nodeLevel;
	}

	public String getCurrentPlayerSymbol() {
		return currentPlayer == cellType.TTT_X.value ? "X" : "O";
	}

	public void toggleCurrentPlayer() {
		currentPlayer = (currentPlayer == cellType.TTT_O.value) ? cellType.TTT_X.value : cellType.TTT_O.value;
	}

	public void resetGame() {
		Arrays.fill(board, cellType.TTT_EMPTY.value);
		theWinner = cellType.TTT_TIE.value;
		AIsTurn = false; // player goes first
		nodeLevel = 0;
		currentCell = cellType.INVALID_CELL.value;
		currentPlayer = cellType.TTT_X.value;
	}

	public void drawBoard() {
		Paint paint = new Paint();
		paint.setColor(Color.rgb(32, 255, 192));
		paint.setStrokeWidth(lineWidth);
		float pts[] = new float[16];

		// first vertical line on left
		pts[0] = cell_size;
		pts[1] = 10.0f;
		pts[2] = cell_size;
		pts[3] = (cell_size * 3.0f) - 10.0f;
		// second vertical line on right
		pts[4] = cell_size * 2.0f;
		pts[5] = 10.0f;
		pts[6] = cell_size * 2.0f;
		pts[7] = (cell_size * 3.0f) - 10.0f;
		// first horizontal line on top
		pts[8] = 10.0f;
		pts[9] = cell_size;
		pts[10] = size.x - 10.0f;
		pts[11] = cell_size;
		// second horizontal line on bottom
		pts[12] = 10.0f;
		pts[13] = cell_size * 2.0f;
		pts[14] = size.x - 10.0f;
		pts[15] = cell_size * 2.0f;

		canvas.drawLines(pts, paint);
	}

	public int findCell(int x, int y) {
		int cell;

		cell = x / cell_size;
		cell += ((y / cell_size) * 3);

		return cell;
	}

	public void setCurrentCell(int cell) {
		currentCell = cell;
	}

	public boolean drawPlayer() {
		if (currentPlayer == cellType.TTT_X.value) {
			drawX(Color.rgb(0, 0, 192), layout.get(currentCell).x, layout.get(currentCell).y);
		} else {
			drawO(Color.rgb(0, 192, 0), layout.get(currentCell).x, layout.get(currentCell).y);
		}
		return true;
	}

	private boolean drawX(int color, int x, int y) {
		int cell = findCell(x, y);

		Paint paint = new Paint();
		paint.setColor(color);
		paint.setStrokeWidth(lineWidth);
		paint.setStyle(Paint.Style.STROKE);
		float segments[] = new float[8];
		float offset = cell_size * 0.1f;

		segments[0] = x + offset;
		segments[1] = y + offset;
		segments[2] = x + (cell_size - offset);
		segments[3] = y + (cell_size - offset);

		segments[4] = x + (cell_size - offset);
		segments[5] = y + offset;
		segments[6] = x + offset;
		segments[7] = y + (cell_size - offset);

		canvas.drawLines(segments, paint);
		board[cell] = cellType.TTT_X.value;
		return true;
	}

	private boolean drawO(int color, int x, int y) {
		int cell = findCell(x, y);

		Paint paint = new Paint();
		paint.setColor(color);
		paint.setStrokeWidth(lineWidth);
		paint.setStyle(Paint.Style.STROKE);
		int offset = cell_size / 2; // the center of the cell

		canvas.drawCircle(x + offset, y + offset, cell_size / 2.5f, paint);
		board[cell] = cellType.TTT_O.value;
		return true;
	}

	public boolean checkForWinner() {
		for (int[] aPattern : pattern) {
			if (board[aPattern[0]] == board[aPattern[1]] && board[aPattern[1]] == board[aPattern[2]]) {
				if (board[aPattern[0]] != cellType.TTT_EMPTY.value) {
					theWinner = board[aPattern[0]];
					if (theWinner == cellType.TTT_X.value) {
						score[0]++;
					} else {
						score[1]++;
					}
					winning[0] = aPattern[0];
					winning[1] = aPattern[1];
					winning[2] = aPattern[2];
					return true;
				}
			}
		}
		return false;
	}

	public int getTheWinner() {
		return theWinner;
	}

	public boolean isAIsTurn() {
		return AIsTurn;
	}

	public void setAIsTurn(boolean mode) {
		AIsTurn = mode;
	}

	public boolean hasMovesLeft() {
		for (int cell = 0; cell < nCells; cell++) {
			if (board[cell] == cellType.TTT_EMPTY.value) {
				return true;
			}
		}
		return false;
	}

	public boolean cellIsEmpty(int cell) {
		return board[cell] == cellType.TTT_EMPTY.value;
	}

	public String getTheWinnersName() {
		return theWinner == cellType.TTT_X.value ? "Meatbag" : "AlIce";
	}

	public String getTheLosersName() {
		return theWinner == cellType.TTT_X.value ? "AlIce" : "Meatbag";
	}

	public boolean AIsMove(int x, int y) {
		if (AIsTurn && !checkForWinner() && hasMovesLeft()) {
			// maybe have a delay here?
//			try { Thread.sleep(250); } catch (Exception e) { System.out.println(e); }
			currentCell = findBestMove(board);
			drawPlayer();
			return true;
		}
		return false;
	}

	private int findBestMove(int[] board) {
		int bestVal = Integer.MIN_VALUE;
		int bestMove = cellType.INVALID_CELL.value;
		int depth = 0;
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		nodeLevel = 0;

		for (int cell = 0; cell < nCells; cell++) {
			if (board[cell] == cellType.TTT_EMPTY.value) {
				board[cell] = cellType.TTT_O.value;
				int moveVal = miniMaxAB(board, depth, alpha, beta, true);
				board[cell] = cellType.TTT_EMPTY.value;
				if (moveVal > bestVal) {
					bestMove = cell;
					bestVal = moveVal;
				}
			}
		}
		return bestMove;
	}

	private int miniMaxAB(int[] board, int depth, int alpha, int beta, boolean isHumansMove) {
		nodeLevel++;
		switch (evaluate(board)) {
			case 0: // player has won
				return -1;
			case 1: // computer has won
				return 1;
			case -1: // its a tie game
				return 0;
		}
		if (isHumansMove) { // humans move
			int best = Integer.MAX_VALUE;
			for (int cell = 0; cell < nCells; cell++) {
				if (board[cell] == cellType.TTT_EMPTY.value) {
					board[cell] = cellType.TTT_X.value;
					best = Math.min(best, miniMaxAB(board, depth++, alpha, beta, !isHumansMove));
					board[cell] = cellType.TTT_EMPTY.value;
					beta = Math.max(beta, best);
					if (beta <= alpha) {
						break;
					}
				}
			}
			return best;
		} else { // AI's move
			int best = Integer.MIN_VALUE;
			for (int cell = 0; cell < nCells; cell++) {
				if (board[cell] == cellType.TTT_EMPTY.value) {
					board[cell] = cellType.TTT_O.value;
					best = Math.max(best, miniMaxAB(board, depth++, alpha, beta, !isHumansMove));
					board[cell] = cellType.TTT_EMPTY.value;
					beta = Math.max(alpha, best);
					if (beta <= alpha) {
						break;
					}
				}
			}
			return best;
		}
	}

	private int evaluate(int[] board) {
		for (int[] aPattern : pattern) {
			if (board[aPattern[0]] == board[aPattern[1]] && board[aPattern[1]] == board[aPattern[2]]) {
				if (board[aPattern[0]] == cellType.TTT_X.value) {
					return 0;
				} else if (board[aPattern[0]] == cellType.TTT_O.value) {
					return 1;
				}
			}
		}
		return (hasMovesLeft() ? cellType.TTT_IN_PLAY.value : cellType.TTT_TIE.value);
	}

	// draw the winning move in red
	public void showWinner() {
		for (int cell = 0; cell < 3; cell++) {
			if (getTheWinner() == cellType.TTT_X.value) {
				drawX(Color.rgb(192, 0, 0), layout.get(winning[cell]).x, layout.get(winning[cell]).y);
			} else {
				drawO(Color.rgb(192, 0, 0), layout.get(winning[cell]).x, layout.get(winning[cell]).y);
			}
		}
	}

}
