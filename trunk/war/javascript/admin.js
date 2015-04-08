function getUpdateFunction(propertyName, id) {
	return function(event) {
		postServiceUnitChange(event.target, id, propertyName, event.target.value, true);
	}
}

function postServiceUnitChange(target, id, propertyName, propertyValue, allowRetry) {
	var conn = getAJAXConnection();
	conn.open("POST","/admin/serviceUnit",true);
	conn.setRequestHeader("Content-type","application/x-www-form-urlencoded");
	conn.onreadystatechange = function() {
		if (conn.readyState == 4 && conn.status == 200) {
			processServerResponse(conn.responseText, target, id, propertyName, propertyValue, allowRetry);
		}
	}
	var params = "id=" + id + "&" + propertyName + "=" + propertyValue;
	if (!allowRetry) {
		params = params + "&ignoreValidation=true";
	}
	conn.send(params);
}

function processServerResponse(responseText, target, id, propertyName, propertyValue, allowRetry) {
	/*
	 * 0 = Unchanged
	 * 1 = Success
	 * 2 = Bad request
	 */
	var valid = (responseText != "2");
	if (!valid) {
		target.style.backgroundColor = "#FF2525";
		if ("state" != propertyName
			&& allowRetry
			&& confirm("Are you sure you want to change " + propertyName + " to " + target.value  + "?"
					+ "\n\nCommon Patriots validation thinks your value is invalid, but since you're an admin "
					+ "you may ignore it if you're sure the value provided is correct.")) {
			postServiceUnitChange(target, id, propertyName, propertyValue, false);
		}
	} else {
		target.style.backgroundColor = "#FFFFFF";
	}
}

function updateServiceUnitName(event, id) {
	(getUpdateFunction("name", id))(event);
}

function updateServiceUnitEmail(event, id) {
	(getUpdateFunction("email", id))(event);
}

function updateServiceUnitColor(event, id) {
	(getUpdateFunction("color", id))(event);
}

function updateServiceUnitPhone(event, id) {
	(getUpdateFunction("phone", id))(event);
}

function updateServiceUnitAddress(event, id) {
	(getUpdateFunction("address", id))(event);
}

function updateServiceUnitCity(event, id) {
	(getUpdateFunction("city", id))(event);
}

function updateServiceUnitState(event, id) {
	(getUpdateFunction("state", id))(event);
}

function updateServiceUnitZip(event, id) {
	(getUpdateFunction("zip", id))(event);
}

function updateServiceUnitInfoFrameLoc(event, id) {
	(getUpdateFunction("infoFrameLoc", id))(event);
}

function getDeleteFunction(id) {
	return function(event) {
		if (confirm("Are you sure you want to delete this service unit?" +
				"\nThis will also delete the service unit's subscriptions and distribution zones." + 
				"\nThis action cannot be undone.")) {
			  var conn = getAJAXConnection();
			  conn.open("POST","/admin/serviceUnit",true);
			  conn.setRequestHeader("Content-type","application/x-www-form-urlencoded");
			  conn.send("id=" + id + "&delete=true");
			  var row = event.target.parentNode.parentNode;
			  row.parentNode.removeChild(row);
			}
	}
}
function deleteServiceUnit(id) {
	(getDeleteFunction(id))(event);
}

function createNewServiceUnit() {
	var conn = getAJAXConnection();
	conn.open("POST","/admin/serviceUnit",true);
	conn.setRequestHeader("Content-type","application/x-www-form-urlencoded");
	conn.onreadystatechange = function() {
		if (conn.readyState == 4 && conn.status == 200) {
			var id = conn.responseText;
			var row = document.createElement("tr");
			var del = document.createElement("td");
			var delButton = document.createElement("input");
			delButton.type = "button";
			delButton.name = "delete";
			delButton.value = "Delete";
			delButton.onclick = getDeleteFunction(id);
			del.appendChild(delButton);
			row.appendChild(del);
			var name = document.createElement("td");
			var nameInput = document.createElement("input");
			nameInput.type = "text";
			nameInput.setAttribute("class", "serviceUnitName");
			nameInput.name = "serviceUnitName";
			nameInput.onblur = getUpdateFunction("name", id);
			name.appendChild(nameInput);
			row.appendChild(name);
			var color = document.createElement("td");
			var colorInput = document.createElement("input");
			colorInput.type = "text";
			colorInput.setAttribute("class", "serviceUnitColor");
			colorInput.name = "serviceUnitColor";
			colorInput.onblur = getUpdateFunction("color", id);
			color.appendChild(colorInput);
			row.appendChild(color);
			var email = document.createElement("td");
			var emailInput = document.createElement("input");
			emailInput.type = "text";
			emailInput.setAttribute("class", "serviceUnitEmail");
			emailInput.name = "serviceUnitEmail";
			emailInput.onblur = getUpdateFunction("email", id);
			email.appendChild(emailInput);
			row.appendChild(email);
			var phone = document.createElement("td");
			var phoneInput = document.createElement("input");
			phoneInput.type = "text";
			phoneInput.setAttribute("class", "serviceUnitPhone");
			phoneInput.name = "serviceUnitPhone";
			phoneInput.onblur = getUpdateFunction("phone", id);
			phone.appendChild(phoneInput);
			row.appendChild(phone);
			var address = document.createElement("td");
			var addressInput = document.createElement("input");
			addressInput.type = "text";
			addressInput.setAttribute("class", "serviceUnitAddress");
			addressInput.name = "serviceUnitAddress";
			addressInput.onblur = getUpdateFunction("address", id);
			address.appendChild(addressInput);
			row.appendChild(address);
			var city = document.createElement("td");
			var cityInput = document.createElement("input");
			cityInput.type = "text";
			cityInput.setAttribute("class", "serviceUnitCity");
			cityInput.name = "serviceUnitCity";
			cityInput.onblur = getUpdateFunction("city", id);
			city.appendChild(cityInput);
			row.appendChild(city);
			var state = document.createElement("td");
			var stateInput = document.createElement("input");
			stateInput.type = "text";
			stateInput.setAttribute("class", "serviceUnitState");
			stateInput.name = "serviceUnitState";
			stateInput.onblur = getUpdateFunction("state", id);
			state.appendChild(stateInput);
			row.appendChild(state);
			var zip = document.createElement("td");
			var zipInput = document.createElement("input");
			zipInput.type = "text";
			zipInput.setAttribute("class", "serviceUnitZip");
			zipInput.name = "serviceUnitZip";
			zipInput.onblur = getUpdateFunction("zip", id);
			zip.appendChild(zipInput);
			row.appendChild(zip);
			var infoFrameLoc = document.createElement("td");
			var infoFrameLocInput = document.createElement("input");
			infoFrameLocInput.type = "text";
			infoFrameLocInput.setAttribute("class", "serviceUnitInfoFrameLoc");
			infoFrameLocInput.name = "serviceUnitInfoFrameLoc";
			infoFrameLocInput.onblur = getUpdateFunction("infoFrameLoc", id);
			infoFrameLoc.appendChild(infoFrameLocInput);
			row.appendChild(infoFrameLoc);
			document.getElementById("serviceUnits").getElementsByTagName("table")[0].appendChild(row);
		}
	}
	conn.send("");
}

function getMakeActivePolygonFn(polygon) {
	return function() {
		var polyDiv = document.getElementById("currentPolygon");
		if (polyDiv.firstChild) {
			polyDiv.removeChild(polyDiv.firstChild);
		}
		var polyText = document.createTextNode(polygonToText(polygon));
		polyDiv.appendChild(polyText);
		var oldActive = document.forms["addPolygon"]["polygon"].polygon;
		if (oldActive) {
			oldActive.setOptions({
				fillOpacity: 0.3
			});
		}
		polygon.setOptions({
			fillOpacity: 0.7
		});

		google.maps.event.addListener(polygon.getPath(), 'set_at', getMakeActivePolygonFn(polygon));
		google.maps.event.addListener(polygon.getPath(), 'insert_at', getMakeActivePolygonFn(polygon));
		google.maps.event.addListener(polygon.getPath(), 'remove_at', getMakeActivePolygonFn(polygon));
		google.maps.event.addListener(polygon, 'click', getMakeActivePolygonFn(polygon));

		document.forms["addPolygon"]["polygon"].polygon = polygon;
		if (polygon.unitName) {
			document.forms["addPolygon"]["serviceUnitName"].value = polygon.unitName;
		}
	}
}
function makeActivePolygon(polygon) {
	(getMakeActivePolygonFn(polygon))();
}

function addCurrentPolygon() {
	var conn = getAJAXConnection();
	conn.open("POST","/admin/polygon",true);
	conn.setRequestHeader("Content-type","application/x-www-form-urlencoded");
	conn.onreadystatechange = function() {
		if (conn.readyState == 4 && conn.status == 200) {
			var invalid = (conn.responseText == "false");
			if (invalid) {
				document.forms["addPolygon"]["serviceUnitName"].style.backgroundColor = "#FF2525";
			} else {
				document.forms["addPolygon"]["serviceUnitName"].style.backgroundColor = "#FFFFFF";
				document.forms["addPolygon"]["polygon"].polygon.setOptions({
					fillColor: conn.responseText
				});
			}
		}
	}
	var currentPoly = document.getElementById("currentPolygon");
	conn.send("name=" + document.forms["addPolygon"]["serviceUnitName"].value + "&polygon=" +
		 (currentPoly.textContent || currentPoly.innerText));
}

function deleteCurrentPolygon() {
	var conn = getAJAXConnection();
	conn.open("POST","/admin/polygon",true);
	conn.setRequestHeader("Content-type","application/x-www-form-urlencoded");
	conn.onreadystatechange = function() {
		if (conn.readyState == 4 && conn.status == 200) {
			var valid = (conn.responseText == "true");
			if (!valid) {
				document.forms["addPolygon"]["serviceUnitName"].style.backgroundColor = "#FF2525";
			} else {
				document.forms["addPolygon"]["serviceUnitName"].style.backgroundColor = "#FFFFFF";
				document.forms["addPolygon"]["polygon"].polygon.setMap(null);
			}
		}
	}
	var currentPoly = document.getElementById("currentPolygon");
	conn.send("name=" + document.forms["addPolygon"]["serviceUnitName"].value + "&delete=true&polygon=" +
		 (currentPoly.textContent || currentPoly.innerText));
}