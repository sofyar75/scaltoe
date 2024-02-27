package com.sosad.scaltoe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class Core extends Base {
	
	private Map<String, String> boardMap;
	private Set<String> winSet;
	
	private int boardSize;
	private int cellCount;
	private int winSize;
	private String gameId;
	private String waitingPiece;
	private String gameMode;
	private String otherType;
	private String otherId;
	private String yourPiece;
	private String lastTs;
	
	public static Core register(HttpSession session) {
		Core core = (Core) session.getAttribute(SESSION_KEY);
		if (core == null) {
			core = new Core();
			core.setBoardSize(BOARD_SIZE_DEFAULT);
			core.winSize = WIN_SIZE_DEFAULT;
			core.gameId = generateNewGameId();
			core.waitingPiece = PIECE_X; // X goes first
			core.gameMode = GAME_MODE_DEFAULT;
			core.boardMap = new HashMap<>();
			core.winSet = new HashSet<>();
			registerSession(session, core);
		}
		return core;
	}

	private void setBoardSize(HttpServletRequest request) {
		String bs = request.getParameter("bs");
		int boardSize = this.boardSize;
		try {
			boardSize = Integer.parseInt(bs);
		} catch (NumberFormatException e) {
		}
		if (boardSize < BOARD_SIZE_MIN) {
			boardSize = BOARD_SIZE_MIN;
		}
		if (boardSize > BOARD_SIZE_MAX) {
			boardSize = BOARD_SIZE_MAX;
		}
		if (boardSize % 2 == 0) {
			boardSize = this.boardSize;
		}
		setBoardSize(boardSize);
	}
	
	private void setBoardSize(int boardSize) {
		this.boardSize = boardSize;
		this.cellCount = boardSize * boardSize;
	}

	private void setWinSize(HttpServletRequest request) {
		String ws = request.getParameter("ws");
		int winSize = this.boardSize;
		try {
			winSize = Integer.parseInt(ws);
		} catch (NumberFormatException e) {
		}
		if (winSize < WIN_SIZE_MIN) {
			winSize = WIN_SIZE_MIN;
		}
		if (winSize > boardSize) {
			winSize = boardSize;
		}
		if (winSize % 2 == 0) {
			winSize = this.winSize;
		}
		this.winSize = winSize;
	}
	
	private void setGameMode(HttpServletRequest request) {
		String gm = request.getParameter("gm");
		String gi = request.getParameter("gi");
		if (gi == null) {
			gi = "";
		}
		Core ic = getCore(gi);
		if (gameModeExist(gm)) {
			if (gm.equals(GAME_MODE_AGAINST_YOURSELF)) {
				if (GAME_MODE_AGAINST_OTHER_PLAYER.equals(gameMode)) {
					if (otherId != null) {
						Core oc = getCore(otherId);
						if (oc != null) {
							oc.otherId = null;
							oc.otherType = null;
						}
					}
					otherId = null;
					otherType = null;
					yourPiece = PIECE_X;
					newGame();
				}
			} else if (gm.equals(GAME_MODE_AGAINST_OTHER_PLAYER)) {
				boolean oidDif = !gi.equals(otherId);
				if (otherId != null && oidDif) {
					Core oc = getCore(otherId);
					if (oc != null) {
						oc.otherId = null;
						oc.otherType = null;
					}
				}
				if (ic != null) {
					if (oidDif) {
						otherId = gi;
						otherType = OTHER_TYPE_CHALLENGED;
						ic.otherId = gameId;
						ic.otherType = OTHER_TYPE_CHALLENGER;
					}
				} else {
					otherId = null;
					otherType = null;
					yourPiece = PIECE_X;
					newGame();
				}
			}
			gameMode = gm;
		}
	}
	
	private void newGame() {
		boardMap.clear();
		waitingPiece = PIECE_X;
		winSet.clear();
		
		if (GAME_MODE_AGAINST_OTHER_PLAYER.equals(gameMode)) {
			if (otherId != null && OTHER_TYPE_CHALLENGED.equals(otherType)) {
				Core oc = getCore(otherId);
				oc.boardMap.clear();
				oc.winSet.clear();
				oc.waitingPiece = PIECE_X;
			}
		}
	}
	
	private void setLastTs() {
		lastTs = String.valueOf(System.currentTimeMillis());
	}
	
	public String getLastTs() {
		String lastTs = null;
		if (otherId != null) {
			if (OTHER_TYPE_CHALLENGED.equals(otherType)) {
				Core oc = getCore(otherId);
				if (oc != null) {
					lastTs = oc.lastTs;
				}
			} else {
				lastTs = this.lastTs;
			}
		} else {
			lastTs = this.lastTs;
		}
		if (lastTs == null) {
			lastTs = "";
		}
		return lastTs;
	}
	
	public void processRequest(HttpServletRequest request) {
		String flow = request.getParameter("flow");
		if ("cf".equals(flow)) {
			setBoardSize(request);
			setWinSize(request);
			setGameMode(request);
			newGame();
		} else if ("ng".equals(flow)) {
			newGame();
		} else if ("pp".equals(flow)) {
			putPiece(request);
		} else if ("sp".equals(flow)) {
			switchPiece();
		}
		if (GAME_MODE_AGAINST_OTHER_PLAYER.equals(gameMode)) {
			if (flow != null && !flow.isBlank()) {
				if (otherId != null) {
					if (OTHER_TYPE_CHALLENGED.equals(otherType)) {
						Core oc = getCore(otherId);
						if (oc != null) {
							oc.setLastTs();
						}
					} else {
						setLastTs();
					}
				} else {
					setLastTs();
				}
			}
		}
	}
	
	public static Collection<String> getOpenGameCol(String excludeGameId,
			String includeGameId) {
		Collection<String> gameIdCol = new ArrayList<String>();
		Collection<HttpSession> sessionsToRemove = new HashSet<HttpSession>();
		for (Iterator<HttpSession> it = sessions.iterator();it.hasNext();) {
			HttpSession session = it.next();
			Core core;
			try {
				core = (Core) session.getAttribute(SESSION_KEY);
			} catch (Exception e) {
				sessionsToRemove.add(session);
				continue;
			}
			if (core != null) {
				String gameId = core.gameId;
				if (GAME_MODE_AGAINST_OTHER_PLAYER
						.equals(core.gameMode)) {
					if (gameId.equals(includeGameId)) {
						gameIdCol.add(gameId);
					} else if (!gameId.equals(excludeGameId)) {
						if (core.otherId == null) {
								gameIdCol.add(gameId);
						}
					}
				}
			} else {
				try {
					session.invalidate();
				} catch (Exception e) {
				}
				sessionsToRemove.add(session);
			}
		}
		sessions.removeAll(sessionsToRemove);
		return gameIdCol;
	}
	
	public static Core getCore(String gameId) {
		Collection<HttpSession> sessionsToRemove = new HashSet<HttpSession>();
		Core core = null;
		for (Iterator<HttpSession> it = sessions.iterator();it.hasNext();) {
			HttpSession session = it.next();
			core = null;
			try {
				core = (Core) session.getAttribute(SESSION_KEY);
			} catch (Exception e) {
				sessionsToRemove.add(session);
				continue;
			}
			if (core != null) {
				String cGameId = core.gameId;
				if (cGameId.equals(gameId)) {
					break;
				} else {
					core = null;
				}
			} else {
				try {
					session.invalidate();
				} catch (Exception e) {
				}
				sessionsToRemove.add(session);
			}
		}
		sessions.removeAll(sessionsToRemove);
		return core;
	}
	
	public boolean isDraw() {
		return winSet.isEmpty()
				&& boardMap.size() == cellCount;
	}
	
	public boolean isGameEnded() {
		return !winSet.isEmpty();
	}
	
	public String getWinnerPiece() {
		if (winSet.isEmpty()) {
			return null;
		} else {
			String key = winSet.iterator().next();
			return boardMap.get(key);
		}
	}
	
	public String getPiece(int x, int y) {
		String key = new StringBuilder()
				.append(x)
				.append('_')
				.append(y)
				.toString();
		String piece = boardMap.get(key);
		return piece;
	}
	
	public boolean checkWinMark(int x, int y) {
		String key = new StringBuilder()
				.append(x)
				.append('_')
				.append(y)
				.toString();
		return winSet.contains(key);
	}
	
	private void putPiece(HttpServletRequest request) {
		String xv = request.getParameter("xv");
		String yv = request.getParameter("yv");
		int x = Integer.parseInt(xv);
		int y = Integer.parseInt(yv);
		String key = new StringBuilder()
				.append(x)
				.append(XY_SEPARATOR)
				.append(y)
				.toString();
		
		Map<String, String> boardMap = this.boardMap;
		Set<String> winSet = this.winSet;
		int boardSize = this.boardSize;
		int winSize = this.winSize;
		String waitingPiece = this.waitingPiece;
		String yourPiece = this.yourPiece;
		
		boolean other = otherId != null
				&& OTHER_TYPE_CHALLENGED.equals(otherType);
		Core cldCore = null;
		
		if (other) {
			cldCore = getCore(otherId);
			boardMap = cldCore.boardMap;
			winSet = cldCore.winSet;
			boardSize = cldCore.boardSize;
			winSize = cldCore.winSize;
			waitingPiece = cldCore.getWaitingPiece();
			yourPiece = cldCore.getYourPiece();
			if (PIECE_O.equals(yourPiece)) {
				yourPiece = PIECE_X;
			} else {
				yourPiece = PIECE_O;
			}
			if (!yourPiece.equals(waitingPiece)) {
				return; 
			}
		}
		
		String piece = boardMap.get(key);
		if (piece == null) {
			boardMap.put(key, waitingPiece);
			if (PIECE_O.equals(waitingPiece)) {
				waitingPiece = PIECE_X;
			} else {
				waitingPiece = PIECE_O;
			}
		}
		
		if (other) {
			cldCore.waitingPiece = waitingPiece;
		} else {
			this.waitingPiece = waitingPiece;
		}
		
		// cek apakah sudah ada pemenang atau belum
		// pengecekan pemenang dilakukan dengan iterate piece yang sudah
		// diset
		// 1. ambil dari titik tersebut ke kanan
		//    jika x berada di titik lebih besar dari board size - win size lanjut next
		// 2. ambil dari titik tersebut ke bawah
		//    jika y berada di titik lebih besar dari board size - win size lanjut next
		// 3. ambil dari titik tersebut ke kanan atas miring
		//    jika x berada di titik lebih besar dari board size - win size lanjut next
		//    jika y berada di titik lebih kecil dari win size - 1 lanjut next
		// 4. ambil dari titik tersebut ke kanan bawah miring 
		//    jika x berada di titik lebih besar dari board size - win size lanjut next
		//    jika y berada di titik lebih besar dari board size - win size lanjut next
		
		int th1 = boardSize - winSize;
		int th2 = winSize - 1;
		winSet.clear();
		
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> it = boardMap.keySet().iterator(); it.hasNext();) {
			key = it.next();
			int sidx = key.indexOf(XY_SEPARATOR);
			xv = key.substring(0, sidx);
			x = Integer.parseInt(xv);
			yv = key.substring(sidx + 1);
			y = Integer.parseInt(yv);
			piece = boardMap.get(key);
			
			boolean xlteth1 = x <= th1;
			boolean ylteth1 = y <= th1;
			boolean ygteth2 = y >= th2;
			
			if (xlteth1) {
				// p1 check
				winSet.add(key);
				boolean win = true;
				int i = x + 1;
				int wc = x + winSize;
				for (; i < wc; i++) {
					sb.setLength(0);
					sb.append(i);
					sb.append(XY_SEPARATOR);
					sb.append(y);
					String ck = sb.toString();
					String cp = boardMap.get(ck);
					if (!piece.equals(cp)) {
						win = false;
						break;
					}
					winSet.add(ck);
				}
				if (!win) {
					winSet.clear();
				} else {
					break;
				}

				if (ylteth1) {
					// p3 check
					winSet.add(key);
					win = true;
					i = x + 1;
					wc = x + winSize;
					int j = y + 1;
					for (;i < wc;) {
						sb.setLength(0);
						sb.append(i);
						sb.append(XY_SEPARATOR);
						sb.append(j);
						String ck = sb.toString();
						String cp = boardMap.get(ck);
						if (!piece.equals(cp)) {
							win = false;
							break;
						}
						winSet.add(ck);
						i++;
						j++;
					}
					if (!win) {
						winSet.clear();
					} else {
						break;
					}
				}

				if (ygteth2){
					// p4 check
					winSet.add(key);
					win = true;
					i = x + 1;
					wc = x + winSize;
					int j = y - 1;
					for (;i < wc;) {
						sb.setLength(0);
						sb.append(i);
						sb.append(XY_SEPARATOR);
						sb.append(j);
						String ck = sb.toString();
						String cp = boardMap.get(ck);
						if (!piece.equals(cp)) {
							win = false;
							break;
						}
						winSet.add(ck);
						i++;
						j--;
					}
					if (!win) {
						winSet.clear();
					} else {
						break;
					}
				}
			}
			
			if (ylteth1) {
				// p2 check
				winSet.add(key);
				boolean win = true;
				int j = y + 1;
				int wc = y + winSize;
				for (; j < wc; j++) {
					sb.setLength(0);
					sb.append(x);
					sb.append(XY_SEPARATOR);
					sb.append(j);
					String ck = sb.toString();
					String cp = boardMap.get(ck);
					if (!piece.equals(cp)) {
						win = false;
						break;
					}
					winSet.add(ck);
				}
				if (!win) {
					winSet.clear();
				} else {
					break;
				}
			}
		}
	}
	
	private void switchPiece() {
		if (gameMode.equals(GAME_MODE_AGAINST_OTHER_PLAYER)) {
			Core cc = null;
			if (otherId != null && OTHER_TYPE_CHALLENGED.equals(otherType)) {
				cc = Core.getCore(otherId);
				if (cc != null) {
					String yourPiece = cc.yourPiece;
					if (PIECE_O.equals(yourPiece)) {
						cc.yourPiece = PIECE_X;
					} else {
						cc.yourPiece = PIECE_O;
					}
				}
			} else {
				if (PIECE_O.equals(yourPiece)) {
					yourPiece = PIECE_X;
				} else {
					yourPiece = PIECE_O;
				}
			}
		}
	}
	
	public int getBoardSize() {
		return boardSize;
	}
	
	public int getWinSize() {
		return winSize;
	}
	
	public String getGameMode() {
		return gameMode;
	}
	
	public String getGameId() {
		return gameId;
	}
	
	public String getOtherType() {
		return otherType;
	}
	
	public String getOtherId() {
		return otherId;
	}
	
	public String getWaitingPiece() {
		return waitingPiece;
	}
	
	public String getYourPiece() {
		return yourPiece;
	}
}
