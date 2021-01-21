package student_player;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.*;

import static Saboteur.SaboteurBoardState.originPos;

import java.util.ArrayList;
import java.util.Arrays;

public class MyTools {
	private static boolean[] hiddenRevealed = {false,false,false};
	public static final int[][] hiddenPos = {{originPos+7,originPos-2},{originPos+7,originPos},{originPos+7,originPos+2}};
	public static final ArrayList<String> notDeadEnd = new ArrayList<String>(Arrays.asList("0", "5", "5_flip", "6", "6_flip", 
			"7", "7_flip", "8", "9", "9_flip", "10"));
	public static int minDistance = getDistFromMove(new int[] {originPos, originPos}, new int[] {originPos+7,originPos});

	public static double evaluatePosition(SaboteurBoardState boardState, SaboteurMove move, int turnPlayer) {
		if(boardState.gameOver()) {
			if(boardState.getWinner() == turnPlayer) {
				return 10000 - boardState.getTurnNumber();
			} else {
				return -10000;
			}
		}
		//initialize value
		double value = 1000;

		//initialize board
		SaboteurTile[][] board = boardState.getHiddenBoard();

		//get position and type of move
		int[] pos = move.getPosPlayed();
		SaboteurCard card = move.getCardPlayed();

		//play map if we don't know the location of the nugget
		if(card instanceof SaboteurMap) {
			int numRevealed = 0;
			for(boolean isRevealed: hiddenRevealed) {
				if(isRevealed) {
					numRevealed++;
				}
			}
			if(numRevealed < 2 && getNuggetPos(boardState)[0] == -1) {
				value *= 1000;
			} else {
				value = 0;
			}
		}

		//play tile if a good move exists
		if(card instanceof SaboteurTile) {
			String idx = ((SaboteurTile) card).getIdx();
			
			//minimize distance to goal
			//find goal and calculate distance
			int[] goal = getNuggetPos(boardState);
			
			//if nugget location unknown use middle
			if(goal[0] == -1) {
				goal = new int[] {12, 5};
			}
			if(getDistFromMove(pos, goal) < minDistance) {
				value = value * Math.abs(minDistance - getDistFromMove(pos, goal))*10000;
				minDistance = getDistFromMove(pos, goal);
			} else {
				value -= Math.abs(minDistance - getDistFromMove(pos, goal))*100;
			}
			
			//reward moves that go lower
			if(pos[0] > 5 && pos[0] < 13) {
				value += pos[0]*1000;
			}
			
			//only play tiles that link possible objective tiles in goal row
			if(pos[0] == 12) {
				if (idx.equals("8") || idx.equals("9") || idx.equals("9_flip") || idx.equals("10")) {
					value *= 20000;
				} else {
					value -= 2000;
				}
				
			//favour non-dead end tiles
			} else if (notDeadEnd.contains(idx)) {
				value *= 1000;
			} else {
				value = 100;
			}
		}

		//play bonus if we have a malus on us
		if(card instanceof SaboteurBonus) {
			if(boardState.getNbMalus(turnPlayer) > 0) {
				value *= 10000;
			} else {
				value = 0;
			}
		}

		//use malus if close to goal but can't win and to prevent opponent from messing up path
		if(card instanceof SaboteurMalus) {
			if(boardState.getNbMalus(1 - turnPlayer) == 0) {
				value *= 10000;
			}
			value += boardState.getTurnNumber() * 1000;
			value += Math.abs(minDistance - 15)*100;
		}

		//destroy dead ends
		if(card instanceof SaboteurDestroy) {
			int x = move.getPosPlayed()[0];
			int y = move.getPosPlayed()[1];
			SaboteurTile toDestroy = board[x][y];
			
			if(!notDeadEnd.contains(((SaboteurTile) toDestroy).getIdx())) {
				if(x > 4) {
					value = value * 100 * x;
				}
			}
		}
		
		//drop non-useful tiles (dead ends, malus, extra maps/bonuses)
		if(card instanceof SaboteurDrop) {
			int index = move.getPosPlayed()[0];
			SaboteurCard toDrop = boardState.getCurrentPlayerCards().get(index);
			if(toDrop instanceof SaboteurTile) {
				if(!notDeadEnd.contains(((SaboteurTile) toDrop).getIdx())) {
					value = value*20;
				}
			} else if(toDrop instanceof SaboteurBonus) {
				value = value*10;
			} else if(toDrop instanceof SaboteurMap) {
				if(getNuggetPos(boardState)[0] != -1) {
					value = value*15;
				}
			} else if (toDrop instanceof SaboteurMalus) {
				value += Math.abs(boardState.getTurnNumber() - 50) * 10;
			} else if(toDrop instanceof SaboteurDestroy) {
				value = value*10;
			} else if(toDrop instanceof SaboteurMap) {
				if(getNuggetPos(boardState)[0] == -1) {
					value = 0;
				} else {
					value = value*10;
				}
			}
		}
		return value;
	}

	private static boolean isConnectedFromEntrance(SaboteurTile[][] board, SaboteurMove move) {
		// TODO Auto-generated method stub
		return false;
	}

	private static int[] getNuggetPos(SaboteurBoardState boardState) {
		//number of hidden objectives found
		int hiddenFound = 0;

		//see which hidden objectives have been revealed, return if nugget has
		for(int i = 0; i < 3; i++) {
			String idx = boardState.getHiddenBoard()[hiddenPos[i][0]][hiddenPos[i][1]].getIdx();
			if(idx.equals("nugget")) {
				hiddenRevealed[i] = true;
				return hiddenPos[i];
			} else if (idx.equals("hidden1") || idx.equals("hidden2")) {
				hiddenRevealed[i] = true;
				hiddenFound++;
			}
		}

		//if both other objectives have been revealed, we know the third is the nugget
		if(hiddenFound == 2) {
			for(int i = 0; i < 3; i++) {
				if(boardState.getHiddenBoard()[hiddenPos[i][0]][hiddenPos[i][1]].getIdx().equals("8")) {
					return hiddenPos[i];
				}
			}
		}
		//if nugget hasn't been revealed, return -1
		return new int[] {-1, -1};
	}
	
	//find distance from move we're about to play to goal
	private static int getDistFromMove(int[] movePos, int[] goal) {
		return Math.abs(movePos[0] - goal[0]) + Math.abs(movePos[1] - goal[1]);
	}
}