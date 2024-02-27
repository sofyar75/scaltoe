package com.sosad.scaltoe;

public interface Cons {

	String SESSION_KEY = "com.sosad.scaltoe.SESSION_KEY";
	int BOARD_SIZE_DEFAULT  = 3;
	int WIN_SIZE_DEFAULT  = BOARD_SIZE_DEFAULT;
	
	int BOARD_SIZE_MIN = BOARD_SIZE_DEFAULT;
	int WIN_SIZE_MIN  = WIN_SIZE_DEFAULT;
	
	int BOARD_SIZE_MAX = 46339;
	int BOARD_SIZE_MAX_LEN = String.valueOf(BOARD_SIZE_MAX).length();
	
	String GAME_MODE_AGAINST_YOURSELF = "1";
	String GAME_MODE_AGAINST_OTHER_PLAYER = "2";
	String GAME_MODE_DEFAULT = GAME_MODE_AGAINST_YOURSELF;
	
	String PIECE_X = "X";
	String PIECE_O = "O";
	
	String OTHER_TYPE_CHALLENGER = "CLR";
	String OTHER_TYPE_CHALLENGED = "CLD";
	
	char XY_SEPARATOR  = '_';
}
