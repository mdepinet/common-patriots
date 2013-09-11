function buttonOver(button) {
	button.style.borderStyle = "outset";
}

function buttonReset(button) {
	button.style.borderStyle = "inset";
}

function getAJAXConnection() {
	if (typeof XMLHttpRequest === "undefined") {
	  XMLHttpRequest = function () {
	    try { return new ActiveXObject("Msxml2.XMLHTTP.6.0"); }
	    catch (e) {}
	    try { return new ActiveXObject("Msxml2.XMLHTTP.3.0"); }
	    catch (e) {}
	    try { return new ActiveXObject("Microsoft.XMLHTTP"); }
	    catch (e) {}
	    throw new Error("This browser does not support XMLHttpRequest.");
	  };
	}
	return new XMLHttpRequest();
}

function polygonToText(polygon) {
	var path = polygon.getPath(); // No donuts or discontinuities please
	var text = "";
	path.forEach(function(point, pointIndex) {
		text += point.lat() + " " + point.lng() + "\n";
	});
	if (polygon.id) {
		text += "=" + polygon.id;
	}
	return text;
}

function textToPolygon(text) {
	var lines = text.split("\n");
	var points = Array();
	for (i = 0; i < lines.length; i++) {
		var vals = lines[i].split(" ");
		points[i] = new google.maps.LatLng(parseFloat(vals[0]), parseFloat(vals[1]));
	}
	var polygon = new google.maps.Polygon();
	polygon.setPath(points);
	return polygon;
}

function getPolygons(callbackFn, id, lat, lng) {
	var conn = getAJAXConnection();
	if (id) {
		conn.open("GET", "/polygons?id=" + id, true);
	} else if (lat && lng) {
		conn.open("GET", "/polygons?lat=" + lat + "&lng=" + lng, true);
	} else {
		conn.open("GET", "/polygons", true);
	}
	conn.onreadystatechange = function() {
		if (conn.readyState == 4 && conn.status == 200) {
			var result = Array();
			var polygons = conn.responseText.split(";");
			for (var i = 0; i < polygons.length - 1; i++) { // Account for ending ;
				var polygonUnitColorId = polygons[i].split("=");
				var points = polygonUnitColorId[0].split("\n");
				var unitName = polygonUnitColorId[1];
				var color = polygonUnitColorId[2];
				var id = polygonUnitColorId[3];
				var path = Array();
				for (var j = 0; j < points.length - 1; j++) { // Account for ending \n
					var latAndLng = points[j].split(" ");
					path[j] = new google.maps.LatLng(parseFloat(latAndLng[0]), parseFloat(latAndLng[1]));
				}
				var polygon = new google.maps.Polygon({
					fillColor : color,
					fillOpacity : 0.3
				});
				polygon.setPath(path);
				polygon.id = id;
				polygon.unitName = unitName;
				result[i] = polygon;
			}
			callbackFn(result);
		}
	}
	conn.send();
}

function maybeShowUnsupportedBrowserError() {
	navigator.sayswho = (function() {
		var N = navigator.appName, ua= navigator.userAgent, tem;
		var M = ua.match(/(opera|chrome|safari|firefox|msie)\/?\s*(\.?\d+(\.\d+)*)/i);
		if (M && (tem = ua.match(/version\/([\.\d]+)/i)) != null) M[2]= tem[1];
		M = M ? [M[1], M[2]]: [N, navigator.appVersion,'-?'];
		return M;
	})(); // Go ahead and execute it.  The value won't change, so we don't really need to keep the function.

	// Display alert for unsupported browser
	var browser = navigator.sayswho[0];
	var version = navigator.sayswho[1];
	if (browser == "msie" && version < 10) {
		alert("Common Patriots requires Chrome, Firefox, Safari, Opera, or the latest version of Internet Explorer." +
				"\nPlease update or replace your browser.");
	}
}

maybeShowUnsupportedBrowserError();