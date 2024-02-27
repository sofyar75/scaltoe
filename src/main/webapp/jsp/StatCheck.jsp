<%@ page language="java" contentType="text/html; charset=UTF-8"
		pageEncoding="UTF-8"%>
<%@ page import="com.sosad.scaltoe.Core"%>
<%
	Core core = Core.register(session);
%>
<%=core.getLastTs()%>