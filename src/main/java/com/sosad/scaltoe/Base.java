package com.sosad.scaltoe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

public class Base implements Cons {
	
	private static final int GAME_ID_LEN = 10;
	private static int gameIdBase = 0;
	
	private static Collection<ValueName> gameModeCol = new ArrayList<>();
	private static Set<String> gameModeSet = new HashSet<>();
	private static Map<String, ValueName> gameModeMap = new HashMap<>();
	protected static Collection<HttpSession> sessions = new ArrayList<>();
	
	static {
		addGameMode(
				new ValueName(
						GAME_MODE_AGAINST_YOURSELF,
						"Yourself"));
		addGameMode(
				new ValueName(
						GAME_MODE_AGAINST_OTHER_PLAYER,
						"Other Player"));
	}
	
	private static void addGameMode(ValueName vn) {
		String val = vn.getValue();
		if (gameModeSet.add(val)) {
			gameModeCol.add(vn);
			gameModeMap.put(val, vn);
		}
	}
	
	public static Collection<ValueName> getGameModeCol() {
		return gameModeCol;
	}
	
	public static boolean gameModeExist(String gm) {
		return gameModeSet.contains(gm);
	}
	
	public static String getGameModeName(String gm) {
		ValueName vn = gameModeMap.get(gm);
		return vn == null ? "" : vn.getName();
	}
	
	public static void registerSession(HttpSession session, Core core) {
		sessions.add(session);
		session.setAttribute(SESSION_KEY, core);
	}
	
	public static String generateNewGameId() {
		gameIdBase++;
		String gameId = String.valueOf(gameIdBase);
		StringBuilder sb = new StringBuilder(gameId);
		while (sb.length() < GAME_ID_LEN) {
			sb.insert(0, '0');
		}
		return sb.toString();
	}
}