<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <title>Common Patriots</title>
  <link type="text/css" rel="stylesheet" href="/stylesheets/common.css" />
  <link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
  <link type="text/css" rel="stylesheet" href="/stylesheets/header.css" />
  
  <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
  <script>
function getLatLng() {
  return new google.maps.LatLng(${latitude}, ${longitude});
}
  </script>

  <script type="text/javascript" src="/javascript/common.js"></script>
  <script type="text/javascript" src="/javascript/map.js"></script>
  
</head>

<body>
<%@ include file="./header.jsp" %>
<div id="mainPage">
	<div id="mainPageLeft">
		<div id = "mapSearchBar">
			<form name="mapSearch" method="get" action="/commonpatriots" onsubmit="updateMap(); return false;">
				<input type="text" name="query" autocomplete="on" autofocus="autofocus" placeholder="806 Thompson Road, Richmond, TX 77469" />
				<input id="mapSearchButton" type="submit" value="Search" />
			</form>
		</div>
		<div id = "mapContainer">
			<%-- This is where the map goes --%>
			<div id = "map-canvas"></div>
		</div>
	</div>
	<div id="mainPageRight">
		<div id="serviceUnitInfo">
			<span id="serviceUnitName">${suName}</span><br />
			<span id="serviceUnitEmail">${suEmail}</span><br />
			<span id="serviceUnitPhone">${suPhone}</span><br />
			<span id="serviceUnitAddress">${suAddr}</span><br />
			<span id="serviceUnitCityStateZip">${suAddr2}</span><br />
			<span id="serviceUnitInfoURL">${suInfoURL}</span>
		</div>
	</div>
</div>
</body>
</html>