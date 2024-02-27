<%@ page language="java" contentType="text/html; charset=UTF-8"
		pageEncoding="UTF-8"%>
<%@ page import="com.sosad.scaltoe.Core"%>
<%@ page import="com.sosad.scaltoe.ValueName"%>
<%@ page import="java.util.Iterator"%>
<%
	Core core = Core.register(session);
	core.processRequest(request);

	String cp = request.getContextPath();
	
	String gameMode = core.getGameMode();
	String gameId = core.getGameId();
	boolean aop = gameMode.equals(Core.GAME_MODE_AGAINST_OTHER_PLAYER);
	String otherType = core.getOtherType();
	boolean challenged = aop && Core.OTHER_TYPE_CHALLENGER.equals(otherType);
	boolean challenger = aop && Core.OTHER_TYPE_CHALLENGED.equals(otherType);
	String otherId = core.getOtherId();
	String clrId = challenger ? otherId : "";
	
	if (challenger) {
		core = Core.getCore(otherId);
	}
	
	String yourPiece = core.getYourPiece();
	if (challenger) {
		if (Core.PIECE_O.equals(yourPiece)) {
			yourPiece = Core.PIECE_X;
		} else {
			yourPiece = Core.PIECE_O;
		}
	}
	
	boolean gameIsEnded = core.isGameEnded();
	String waitingPiece = core.getWaitingPiece();
	int boardSize = core.getBoardSize();
	int winSize = core.getWinSize();
	
	boolean notYourTurn = !waitingPiece.equals(yourPiece);
	notYourTurn = notYourTurn && !Core.GAME_MODE_AGAINST_YOURSELF.equals(gameMode);
	String lastTs = core.getLastTs();
%>   
<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>Scaltoe</title>
	</head>
	<body<%=aop?" onload=\"delayRefresh()\"":""%>>
		<b>Scalable TicTacToe</b><br>
		<br>
		<form name="configForm" action="<%=cp%>/jsp/Index.jsp" method="post">
			<input type="hidden" name="flow" value="cf">
			<table border="1">
				<tr bgcolor="#DDDDDD">
					<td rowspan="2" align="center">Name</td>
					<td colspan="2" align="center">Value</td>
					<td colspan="2" align="center">Range</td>
					<td rowspan="2" align="center">Additional Info</td>
				</tr>	
				<tr bgcolor="#EEEEEE">
					<td align="center">Current</td>
					<td align="center">New</td>
					<td align="center">Min</td>
					<td align="center">Max</td>
				</tr>
				<tr>
					<td>Board Size</td>
					<td align="right"><%=boardSize%></td>
					<td align="center">
						<input type="text" name="bs" value="<%=boardSize%>"
								size="<%=Core.BOARD_SIZE_MAX_LEN%>"
								maxlength="<%=Core.BOARD_SIZE_MAX_LEN%>"
								style="text-align:right"></td>
					<td align="right"><%=Core.BOARD_SIZE_MIN%></td>
					<td align="right"><%=Core.BOARD_SIZE_MAX%></td>
					<td>Must be odd value</td>
				</tr>
				<tr>
					<td>Winning Size</td>
					<td align="right"><%=winSize%></td>
					<td align="center">
						<input type="text" name="ws" value="<%=winSize%>"
								size="<%=Core.BOARD_SIZE_MAX_LEN%>"
								maxlength="<%=Core.BOARD_SIZE_MAX_LEN%>"
								style="text-align:right"></td>
					<td align="right"><%=Core.WIN_SIZE_MIN%></td>
					<td>Board Size</td>
					<td>Must be odd value</td>
				</tr>
				<tr>
					<td>Playing Against</td>
					<td><%=Core.getGameModeName(gameMode) %></td>
					<td align="center">
						<select name="gm">
<%
	for (Iterator<ValueName> it = Core.getGameModeCol().iterator(); it.hasNext();) {
		ValueName vn = it.next();
		String val = vn.getValue();
%>
							<option value="<%=val%>"<%=val.equals(gameMode)?" selected":""%>>
								<%=vn.getName()%>
							</option>
<%		
	}
%>						
						</select>
					</td>
					<td colspan="2" align="center">N/A</td>
					<td>
						Your Game ID: <%=gameId%>
<%
	if (aop) {
%>
						<br>Your Piece: <%=yourPiece%>
<%
		if (challenged) {
%>
						<br>Challenger ID: <%=otherId%>
<%
		}
	}
%>						
					</td>
				</tr>
				<tr>
					<td>Challenging</td>
					<td><%=clrId%></td>
					<td align="center">
						<select name="gi">
							<option></option>
<%
	for (Iterator<String> it = Core.getOpenGameCol(gameId, clrId).iterator(); it.hasNext();) {
		String ogi = it.next();
%>
							<option value="<%=ogi%>"<%=ogi.equals(clrId)?" selected":""%>><%=ogi%></option>
<%		
	}
%>						
						</select>
					</td>
					<td colspan="2" align="center">N/A</td>
					<td>Based on Game ID</td>
				</tr>
			</table><br>
			<input type="button" value="Apply New Setting" onclick="applyNewSetting()">
			<input type="reset">
		</form>
		<br>
		<form name="gameForm" action="<%=cp%>/jsp/Index.jsp" method="post">
			<input type="hidden" name="flow">
			<input type="hidden" name="xv">
			<input type="hidden" name="yv">
			<table>
				<tr valign="middle">
<%
	String img;
	if (core.isDraw()) {
%>
					<td>Draw</td>
<%		
	} else {
		if (gameIsEnded) {
			img = Core.PIECE_O.equals(core.getWinnerPiece()) ? "wo" : "wx";
		} else {
			img = Core.PIECE_O.equals(waitingPiece) ? "po" : "px";
		}
%>
					<td><img src="<%=cp%>/img/<%=img%>.png"></td><td><%=gameIsEnded?"win":"turn"%></td>
<%		

	}
%>			
					<td>
<%
	boolean againstOp = Core.GAME_MODE_AGAINST_OTHER_PLAYER.equals(gameMode); 
	if (againstOp) {
%>			
						<input type="button" value="Switch Piece" name="sp" onclick="switchPiece()">
<%
	}
%>					
						<input type="button" value="New Game" name="ng" onclick="newGame()">
						<input type="button" value="Refresh" onclick="refresh()">
					</td>				
				</tr>
			</table>
			<table border="1">
<%
	for (int y = 0; y < boardSize; y++) {
%>
				<tr>
<%
		for (int x = 0; x < boardSize; x++) {
			String piece = core.getPiece(x, y);
			boolean wf = core.checkWinMark(x, y);
			String onclick;
			if (piece == null) {
				if (gameIsEnded || notYourTurn) {
					onclick = "";
				} else {
					onclick = new StringBuilder()
							.append(" onclick=\"putPiece(")
							.append(x)
							.append(',')
							.append(y)
							.append(")\"")
							.toString();
				}
				img = "cl";
			} else {
				onclick = "";
				if (Core.PIECE_O.equals(piece)) {
					img = wf ? "wo" : "po";
				} else {
					img = wf ? "wx" : "px";
				}
			}
%>
					<td id="<%=x%>_<%=y%>"><img src="<%=cp%>/img/<%=img%>.png"<%=onclick%>></td>
<%			
		}
%>
				</tr>
<%			
	}
%>
			</table>
		</form>
	</body>
</html>
<script>

	function applyNewSetting() {
		var field = configForm.bs;
		var val = field.value;
		var nbs = parseInt(val);
		if (isNaN(nbs)
				|| nbs < <%=Core.BOARD_SIZE_MIN%>
				|| nbs > <%=Core.BOARD_SIZE_MAX%>
				|| nbs % 2 == 0) {
			field.focus();
			alert("Board Size must be filled with odd integer value range <%=Core.BOARD_SIZE_MIN%> to <%=Core.BOARD_SIZE_MAX%>")
			return;
		}
		
		field = configForm.ws;
		val = field.value;
		var nws = parseInt(val);
		if (isNaN(nws)
				|| nws < <%=Core.WIN_SIZE_MIN%>
				|| nws > nbs
				|| nws % 2 == 0) {
			field.focus();
			alert("Win Size must be filled with odd integer value range <%=Core.BOARD_SIZE_MIN%> to " + nbs)
			return;
		}
		
		field = configForm.gm;
		var gmVal = field.value;
		
		field = configForm.gi;
		var giVal = field.value;
		
		if (giVal != "" && gmVal != "<%=Core.GAME_MODE_AGAINST_OTHER_PLAYER%>") {
			alert("Invalid Playing Against and Chalenging values combination");
			return false;
		}
		
		configForm.submit();
	}
	
	function disableForm(form) {
		var length = form.elements.length, i;
		for (i = 0; i < length; i++) {
			form.elements[i].disabled = true;
		}
	}
	
	var refreshFlag = true;
	
	function refresh() {
		if (refreshFlag) {
			refreshFlag = false;
			disableForm(configForm);
			configForm.submit();
		}
	}

<%
	if (aop) {
%>					
	function delayRefresh() {
		setTimeout(checkLastTs, 1000);
	}

	function checkLastTs() {
		var xhttp = new XMLHttpRequest();
		xhttp.onload = function() {checkLastTsXml(this);}
		xhttp.open("GET", "<%=cp%>/jsp/StatCheck.jsp");
		xhttp.send();
	}
	
	function checkLastTsXml(xml) {
		var xmlText = xml.responseText.trim();
		if (xmlText != <%=lastTs%>) {
			refresh();
		} else {
			delayRefresh();
		}
	}
	
<%
	}
%>
	
	function putPiece(x, y) {
		gameForm.flow.value = "pp";
		gameForm.xv.value = x;
		gameForm.yv.value = y;
		gameForm.submit();
	}
	
	function newGame() {
		gameForm.flow.value = "ng";
		gameForm.submit();
	}
	
<%
	if (againstOp) {
%>					
	function switchPiece() {
		gameForm.flow.value = "sp";
		gameForm.submit();
	}
<%
	}
%>
</script>