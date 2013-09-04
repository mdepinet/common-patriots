<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div id="header">
	<div id="banner">
		<a href="/"><img id="bannerImg" src="/img/banner.jpg" alt="Common Patriots" /></a>
	</div>
	<div id="accountInfo">
<%
	// We should really avoid using scriptlets like this since they make debugging/reusing more difficult,
	// but this seems like a reasonable situation to use them.
	
	if (request.getAttribute("username") == null || "".equals(request.getAttribute("username"))) {
%>
	<span id="greeting">Hello!</span>
    <%-- This has onmouseout/onmouseover flipped on purpose because it looks better --%>
    <a href="${loginURL}"><img class="button" src="/img/login.png" alt="Logout" onmouseout="buttonOver(this)" onmouseover="buttonReset(this)"/></a>
<%} else { %>
		<span id="greeting">Hello, ${fn:escapeXml(username)}!</span>
		<%-- These have onmouseout/onmouseover flipped on purpose because it looks better --%>
		<a href="/account"><img class="button" src="/img/manageAccount.png" alt="Manage Account" onmouseout="buttonOver(this)"onmouseover="buttonReset(this)"/></a>
		<a href="${logoutURL}"><img class="button" src="/img/logout.png" alt="Logout" onmouseout="buttonOver(this)" onmouseover="buttonReset(this)"/></a>
<%}%>
	</div>
</div>