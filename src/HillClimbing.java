package student_player;

import java.util.ArrayList;
import java.util.Random;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import boardgame.Move;

public class HillClimbing {
	public static Move getBestMove(SaboteurBoardState boardState) {
		double maxValue = Integer.MIN_VALUE;
		ArrayList<SaboteurMove> bestMoves = new ArrayList<>();
		ArrayList<SaboteurMove> legalMoves = boardState.getAllLegalMoves();
		
		for(SaboteurMove move: boardState.getAllLegalMoves()) {
			double value = MyTools.evaluatePosition(boardState, move, boardState.getTurnPlayer());
			if(value > maxValue) {
				maxValue = value;
				//better group of bestMoves
				bestMoves.clear();
				bestMoves.add(move);
			} else if(value == maxValue) {
				//if move is equally good add it to list
				bestMoves.add(move);
			}
		}

		//if more than 1 best move pick randomly
		if(bestMoves.size() > 0) {
			Random rand = new Random();
			Move bestMove =  bestMoves.get(rand.nextInt(bestMoves.size()));
			System.out.println("Move: " + bestMove.toPrettyString() + " Value: " + maxValue);
			System.out.println("Legal Moves: " + legalMoves.size() + " Best Moves:" + bestMoves.size());
			return bestMove;
		} else {
			return boardState.getRandomMove();
		}
	}
}
