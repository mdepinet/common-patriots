<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
  <title>Common Patriots - Administration</title>
  <link type="text/css" rel="stylesheet" href="/stylesheets/common.css" />
  <link type="text/css" rel="stylesheet" href="/stylesheets/admin.css" />
  <link type="text/css" rel="stylesheet" href="/stylesheets/header.css" />
  
  <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
  <script>
function getLatLng() {
  return new google.maps.LatLng(${latitude}, ${longitude});
}
  </script>

  <script type="text/javascript" src="/javascript/common.js"></script>
  <script type="text/javascript" src="/javascript/adminMap.js"></script>
  <script type="text/javascript" src="/javascript/admin.js"></script>
  
</head>

<body>
<%@ include file="./header.jsp" %>
<div id="mainContent">
  <div id="polygons">
		<div id="mapEdit">
			<div id = "mapSearchBar">
				<form name="mapSearch" method="get" action="/" onsubmit="updateMap(); return false;">
					<input type="text" name="query" autocomplete="on" autofocus="autofocus" placeholder="806 Thompson Road, Richmond, TX 77469" />
					<input id="mapSearchButton" type="submit" value="Search" />
				</form>
			</div>
			<div id = "mapContainer">
				<%-- This is where the map goes --%>
				<div id = "map-canvas"></div>
			</div>
		</div>
		<div id="mapControls">
		  <div id="currentPolygon"></div>
		  <form name="addPolygon" method="post" action="/" onsubmit="return false;">
		    <input type="hidden" name="polygon" value="" />
		    Service Unit: <input type="text" name="serviceUnitName" placeholder="Troop 1000" />
		    <input type="submit" name="saveButton" value="Save" onclick="addCurrentPolygon();"/>
		    <input type="submit" name="deleteButton" value="Delete" onclick="deleteCurrentPolygon();"/>
		  </form>
		</div>
	</div>
	<div id="serviceUnits">
    <h2>Service Units:</h2>
    <table>
      <tr><th></th><th>Name</th><th>Color</th><th>Email</th><th>Phone</th><th>Street Address</th><th>City</th><th>State</th><th>Zip</th><th>Info URL</tr>
	    <c:forEach items="${serviceUnits}" var="serviceUnit">
	      <tr>
	        <td><input type="button" name="delete" value="Delete" onclick="deleteServiceUnit('${serviceUnit.id}');"/></td>
	        <td><input type="text" class="serviceUnitName" name="serviceUnitName" value="${serviceUnit.name}" onblur="updateServiceUnitName(event, '${serviceUnit.id}');" /></td>
	        <td><input type="text" class="serviceUnitColor" name="serviceUnitColor" value="${serviceUnit.color}" onblur="updateServiceUnitColor(event, '${serviceUnit.id}');" /></td>
	        <td><input type="text" class="serviceUnitEmail" name="serviceUnitEmail" value="${serviceUnit.contactInfo.email}" onblur="updateServiceUnitEmail(event, '${serviceUnit.id}');" /></td>
	        <td><input type="text" class="serviceUnitPhone" name="serviceUnitPhone" value="${serviceUnit.contactInfo.phone}" onblur="updateServiceUnitPhone(event, '${serviceUnit.id}');" /></td>
	        <td><input type="text" class="serviceUnitAddress" name="serviceUnitAddress" value="${serviceUnit.contactInfo.address}" onblur="updateServiceUnitAddress(event, '${serviceUnit.id}');" /></td>
	        <td><input type="text" class="serviceUnitCity" name="serviceUnitCity" value="${serviceUnit.contactInfo.city}" onblur="updateServiceUnitCity(event, '${serviceUnit.id}');" /></td>
	        <td><input type="text" class="serviceUnitState" name="serviceUnitState" value="${serviceUnit.contactInfo.state}" onblur="updateServiceUnitState(event, '${serviceUnit.id}');" /></td>
	        <td><input type="text" class="serviceUnitZip" name="serviceUnitZip" value="${serviceUnit.contactInfo.zip}" onblur="updateServiceUnitZip(event, '${serviceUnit.id}');" /></td>
	        <td><input type="text" class="serviceUnitInfoFrameLoc" name="serviceUnitInfoFrameLoc" value="${serviceUnit.infoFrameLoc}" onblur="updateServiceUnitInfoFrameLoc(event, '${serviceUnit.id}');" /></td>
	      </tr>
	    </c:forEach>
    </table>
    <input type="button" name="addServiceUnit" value="Add" onclick="createNewServiceUnit();" />
	</div>
	<%-- TODO: Other Admin functions go down here --%>
</div>
</body>
</html>