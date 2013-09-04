<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
  <head>
    <title>Common Patriots - Account</title>
    <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
    <link type="text/css" rel="stylesheet" href="/stylesheets/header.css" />
    <script type="text/javascript" src="/javascript/common.js"></script>
    <script type="text/javascript" src="/javascript/manageAccount.js"></script>
  </head>

  <body onload="showOrHideSUIdAndConfirmed()">
<%@ include file="./header.jsp" %>  
<p>This is what we think we know about you:</p>

<form name="userData" action="/account" method="post" onSubmit="return validateUserDataInput()">
	User Type:<br />
	<c:forEach items="${types}" var="type">
		<input type="radio" name="userType" onchange="showOrHideSUIdAndConfirmed()" value="${type.first.first}" ${type.second} />${type.first.second} <br />
    </c:forEach>
    <span id="serviceUnit">Service Unit: <input type="text" name="serviceUnitName" value="${serviceUnit}" /><br /></span>
    <span id="confirmed">Confirmed: <span id="confirmedVal">${confirmed}</span></span><br />
    Email: <input type="email" name="email" value="${user.email}" /><br />
    Phone: <input type="tel" name="phone" value="${user.phone}" /><br />
    Address: <input type="text" name="address" value="${user.address}" /><br />
    City: <input type="text" name="city" value="${user.city}" /><br />
    State: <input type="text" name="state" value="${user.stateString}" /><br />
    Zip: <input type="number" name="zip" value="${user.zip}" /><br />
    <input type="submit" value="Update" />
</form>

  </body>
</html>