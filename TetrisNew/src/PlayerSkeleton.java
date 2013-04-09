public class PlayerSkeleton {
	private static int[][] currentField = new int[State.ROWS][State.COLS];
	private static int[] currentTop = new int[State.COLS];
	private static int landingHeight = 0;
	private static int numRowCleared = 0;
	private static double[] weight = new double[GeneticOptimization.NUM_OF_WEIGHT];
	
	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		int numOfLegalMoves = legalMoves.length;
		int bestMove = 0;
		simulateMove(s, 0);
		double currentMaxWeight = computeUtility();
		double weight;
		for(int i = 1; i < numOfLegalMoves; i++) {
//			System.out.printf("turn = %d	i = %d \n", s.getTurnNumber(), i);
			if(simulateMove(s, i) == false) {
//				printCurrentField();
				weight = computeUtility();
				if(weight > currentMaxWeight) {
					currentMaxWeight = weight; 
					bestMove = i;
//					System.out.printf("turn %d weight %f\n", s.getTurnNumber(), weight);
				}
//				System.out.printf("\n");
			}
		}
		return bestMove;
	}
	
	private double computeUtility() {
		double landingHeight = landingHeightUtility();
		double rowsEliminated = rowsEliminatedUtility();
		double rowsTransition = rowTransitionUtility();
		double columnTransition = columnTransitionUtility();
		double numOfHoles = numOfHolesUtility();
		double wellSums = wellSumsUtility();
		double heightDiff = heightDifferenceUtility();
		double maxHeightDiff = maxAdjacentHeightDifferenceUtility();
		return landingHeight + rowsEliminated + columnTransition + numOfHoles + wellSums + rowsTransition + heightDiff + maxHeightDiff; 
	}
	
	
	// simulate a move to a State s without changing the current State. 
	// The field and top updated after the move is copied to currentField and currentTop for the calculation of different utility
	private boolean simulateMove(State s, int move) {
		int nextPiece = s.getNextPiece();
		int[][] field = new int[State.ROWS][State.COLS];
		copyField(s.getField(), field);
		int[][] legalMoves = s.legalMoves();
		int[] moveInfo = legalMoves[move];
		int slot = moveInfo[State.SLOT];
		int orient = moveInfo[State.ORIENT];
		int[][][] pBottom = s.getpBottom();
		int[][] pWidth = s.getpWidth();
		int[] top = new int[State.COLS];
		copyTop(s.getTop(), top);
		int[][] pHeight = s.getpHeight();
		int turn = s.getTurnNumber() + 1;
		int[][][] pTop = s.getpTop();
		boolean lost = false;
		int height = top[slot]-pBottom[nextPiece][orient][0];
		//for each column beyond the first in the piece
		
		int contactSlot = 0;
		for(int c = 1; c < pWidth[nextPiece][orient];c++) {
			if(top[slot+c]-pBottom[nextPiece][orient][c] > height) {
				height = top[slot+c]-pBottom[nextPiece][orient][c];
				contactSlot = c;
			}
			
		}
		
		landingHeight = top[slot + contactSlot] + pHeight[nextPiece][orient];
		
		
		//check if game ended
		if(height+pHeight[nextPiece][orient] >= State.ROWS) {
			lost = true;
			return false;
		}

		
		//for each column in the piece - fill in the appropriate blocks
		for(int i = 0; i < pWidth[nextPiece][orient]; i++) {
			
			//from bottom to top of brick
			for(int h = height+pBottom[nextPiece][orient][i]; h < height+pTop[nextPiece][orient][i]; h++) {
				field[h][i+slot] = turn;
			}
		}
		
		//adjust top
		for(int c = 0; c < pWidth[nextPiece][orient]; c++) {
			top[slot+c]=height+pTop[nextPiece][orient][c];
		}
		
		numRowCleared = 0;
		
		//check for full rows - starting at the top
		for(int r = height+pHeight[nextPiece][orient]-1; r >= height; r--) {
			//check all columns in the row
			boolean full = true;
			for(int c = 0; c < State.COLS; c++) {
				if(field[r][c] == 0) {
					full = false;
					break;
				}
			}
			//if the row was full - remove it and slide above stuff down
			if(full) {
				numRowCleared++;
				//for each column
				for(int c = 0; c < State.COLS; c++) {

					//slide down all bricks
					for(int i = r; i < top[c]; i++) {
						field[i][c] = field[i+1][c];
					}
					//lower the top
					top[c]--;
					while(top[c]>=1 && field[top[c]-1][c]==0)	top[c]--;
				}
			}
		}
		
		// copy field and top for the calculation of different utility
		copyField(field, currentField);
		copyTop(top, currentTop);
		
		return lost;
	}
	
	
	// to make a hard copy of field to currentField
	private void copyField(int[][] field, int[][] currentField) {
		for(int i = 0; i < State.ROWS; i++) {
			for (int j = 0; j < State.COLS; j++) {
				currentField[i][j] = field[i][j];
			}
		}
	}
	
	// to make a hard copy of top to currentTop
	private void copyTop(int[] top, int[] currentTop) {
		for(int i = 0; i < State.COLS; i++) {
			currentTop[i] = top[i];
		}
	}
	
	private void printCurrentField() {
		for(int i = currentField.length -1 ; i >= 0; i--) {
			for(int j = 0; j < State.COLS; j++) {
				if(currentField[i][j] != 0) {
					System.out.printf("X");
				} else {
					System.out.printf("O");
				}
			}
			System.out.printf("\n");
		}
	}
	
	private double heightDifferenceUtility() {
		int heightDiff = 0;
		for(int i = 0; i < State.COLS - 1; i++) {
			heightDiff = heightDiff + Math.abs(currentTop[i] - currentTop[i+1]);
			
		}
		
		return heightDiff * weight[6];
	}
	
	private double maxAdjacentHeightDifferenceUtility() {
		int heightDiff = 0;
		for(int i = 0; i < State.COLS - 1; i++) {
			int height = Math.abs(currentTop[i] - currentTop[i+1]);
			if(heightDiff < height) {
				heightDiff = height;
			}
		}
		return heightDiff * weight[7];
	}
	
	
	private double landingHeightUtility() {
//		System.out.printf("landingHeight %d %f\n", landingHeight, weight[0]);
		return landingHeight * weight[0];
	}
	
	private double rowsEliminatedUtility() {
		
//		System.out.printf("numRowCleared %d %f \n", numRowCleared, weight[1]);
		return numRowCleared * weight[1];
	}
	
	private double rowTransitionUtility() {
	
		int rowTransition = 0;
		for(int j = 0; j < State.COLS - 1; j++) {
			for(int i = 0; i < currentTop[j]; i++) {
				if((currentField[i][j] == 0 && currentField[i][j+1] != 0) || (currentField[i][j] != 0 && currentField[i][j+1] == 0)) {
					rowTransition++;
				}
			}
		}
//		System.out.printf("rowTransition %d %f\n", rowTransition, weight[2]);
		return rowTransition * weight[2];
	}

	private double columnTransitionUtility() {
		int[] top = currentTop;
		int[][] field = currentField;
		int colTransition = 0;
		for(int i = 0; i < State.COLS; i++) {
			for(int j = 0; j < top[i] - 1; j++) {
				if((field[j][i] == 0 && field[j+1][i] != 0) || (field[j][i] != 0 && field[j+1][i] == 0)) {
					colTransition++;
				}
			}
		}
		
//		System.out.printf("colTransition %d %f\n", colTransition, weight[3]);
		return colTransition * weight[3];
	}
	
	private double numOfHolesUtility() {
		int[][] field = currentField;
		int[] top = currentTop;
		int numOfHoles = 0;
		for(int i = 0; i < State.COLS; i++) {
			for(int j = 0; j < top[i] - 1; j++) {
				if(field[j][i] == 0) {
					numOfHoles++;
				}
			}
		}
//		System.out.printf("numOfHoles %d %f\n", numOfHoles, weight[4]);
		return numOfHoles * weight[4];
	}
	
	private double wellSumsUtility() {
		int wellSums = 0;
		for(int i = 0; i < State.COLS - 1; i++) {
			for(int j = 0; j < State.ROWS - 1; j++) {
				if(isAWell(j,i)) {
	 				while(isAWell(j,i)) {
						j++;
						if(j == State.ROWS) {
							break;
						}
					}
					wellSums++;
				}				
			}
		}
//		System.out.printf("wellSums %d %f\n", wellSums, weight[5]);
		return wellSums * weight[5];
	}
	
	private boolean isAWell(int row, int col) {
		int[][] field = currentField;
		if(col >= 1 && col < State.COLS - 1) {
			if(field[row][col] == 0 && field[row][col - 1] != 0 && field[row][col + 1] != 0) {
				return true;
			}
		} else if(col == 0) {
			if(field[row][col] == 0 && field[row][col + 1] != 0) {
				return true;
			}
		} else if(col == State.COLS - 1) {
			if(field[row][col] == 0 && field[row][col - 1] != 0) {
				return true;
			}
		}
		return false;
	}
	
	// This method is called from the optimization class. Input is a test weight in String.
	// The method will run a game with the input test weight and return the number of rows cleared.
	public int runGame(String testWeight) {
		getWeightFromString(testWeight);
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
			System.out.printf("row Cleared: %d\n", s.getRowsCleared());
		return s.getRowsCleared();
	}
	
	// return a set of weight from the input String
	private void getWeightFromString(String testWeight) {
		for(int i = 0; i < GeneticOptimization.NUM_OF_WEIGHT; i++) {
			weight[i] = Double.parseDouble(testWeight.substring(4*i, 4*i+4))/100.0;
			if(i != 1) {
				weight[i] = -weight[i];
			}
		}
	}
	
	public static void main(String[] args) {
//		SET 1 -- un-comment to run 10 games
//		weight[0] = -20.39;
//		weight[1] = 47.5;
//		weight[2] = -29.83;
//		weight[3] = -74.49;
//		weight[4] = -82.38;
//		weight[5] = -74.57;
//		weight[6] = -29.79;
//		weight[7] = -0.67;

		
//		SET 2 -- un-comment to run 10 games
//		weight[0] = -20.35;
//		weight[1] = 84.50;
//		weight[2] = -29.83;
//		weight[3] = -74.49;
//		weight[4] = -82.38;
//		weight[5] = -74.57;
//		weight[6] = -29.79;
//		weight[7] = -13.86;

		for(int i = 0; i < 10; i++) {
			State s = new State();
			new TFrame(s);
			PlayerSkeleton p = new PlayerSkeleton();
			while(!s.hasLost()) {
				s.makeMove(p.pickMove(s,s.legalMoves()));
				s.draw();
				s.drawNext(0,0);
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		}
	}
}
