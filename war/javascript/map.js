var map;
var geocoder;
var marker;

var initialize = function() {
  // Enable the visual refresh
  google.maps.visualRefresh = true;
  geocoder = new google.maps.Geocoder();

  var mapOptions = {
    zoom: 13,
    center: getLatLng(),
    mapTypeId: google.maps.MapTypeId.ROADMAP
  }
  map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);

  getPolygons(function(results) {
	  for (var i = 0; i < results.length; i++) {
		  var polygon = results[i];
		  google.maps.event.addListener(polygon, 'click', getMakeActivePolygonFn(polygon));
		  polygon.setMap(map);
	  }
  });
}

var loadMapsAPI = function() {
  var script = document.createElement("script");
  script.type = "text/javascript";
  script.src = "https://maps.googleapis.com/maps/api/js?key=AIzaSyBanPX_Vnws7Xc4_U9Fx_ApgLTbgDDrl14&sensor=true&callback=initialize&v=3";
  document.body.appendChild(script);
}

window.onload = loadMapsAPI;

var updateMap = function() {
	var address = document.forms["mapSearch"]["query"].value;
    geocoder.geocode(
    		{'address': address,
    		 'bounds' : new google.maps.LatLngBounds(
    				 new google.maps.LatLng(29.19664, -96.26969), //sw - El Campo, TX
    				 new google.maps.LatLng(30.05799, -94.79548)) //ne - Liberty, TX
    		},
    	function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        map.setCenter(results[0].geometry.location);
        if (!marker) {
        	marker = new google.maps.Marker({
                map: map,
                position: results[0].geometry.location
            });
        } else {
        	marker.setPosition(results[0].geometry.location);
        }
        marker.setTitle(address);
      } else {
        alert("Geocode was not successful for the following reason: " + status);
      }
    });
}

function getMakeActivePolygonFn(polygon) {
	return function() {
		var activePolygonContainer = document.getElementById("activePolygon");
		if (activePolygonContainer && activePolygonContainer.polygon) {
			activePolygonContainer.polygon.setOptions({
				fillOpacity: 0.3
			});
		}
		polygon.setOptions({
			fillOpacity: 0.7
		});

		if (!activePolygonContainer) {
			activePolygonContainer = document.createElement("polygon");
			activePolygonContainer.setAttribute("id", "activePolygon");
			document.body.appendChild(activePolygonContainer);
		}
		activePolygonContainer.polygon = polygon;
		if (marker) {
			var pos = marker.getPosition();
			setActiveServiceUnit(polygon.unitName, pos.lat(), pos.lng());
		} else {
			setActiveServiceUnit(polygon.unitName);
		}
	}
}

function setActiveServiceUnit(unitName, lat, lng) {
	var conn = getAJAXConnection();
	conn.open("GET","/serviceUnit?name=" + unitName + "&lat=" + lat + "&lng=" + lng, true);
	conn.setRequestHeader("Content-type","application/x-www-form-urlencoded");
	conn.onreadystatechange = function() {
		if (conn.readyState == 4 && conn.status == 200) {
			var resultParts = conn.responseText.split(";");
			document.getElementById("serviceUnitInfo").style.borderStyle = "solid";
			document.getElementById("serviceUnitInfo").style.borderColor = "black";
			if ("true" != resultParts[0]) {
				var warnSpan = document.getElementById("warnSpan");
				if (!warnSpan) {
					warnSpan = document.createElement("span");
					warnSpan.setAttribute("id", "warnSpan");
					warnSpan.style.color = "red";
					warnSpan.appendChild(document.createTextNode(
							"Based on your last search, you are not in an area served by this service unit!"));
					document.getElementById("serviceUnitInfo").appendChild(warnSpan);
				}
			} else {
				var warnSpan = document.getElementById("warnSpan");
				if (warnSpan) {
					warnSpan.parentNode.removeChild(warnSpan);
				}
			}
			makeOnlyChild(document.getElementById("serviceUnitName"), document.createTextNode(resultParts[1]));
			var emailAnchor = document.createElement("a");
			emailAnchor.setAttribute("href", "mailto:" + resultParts[2]);
			makeOnlyChild(emailAnchor, document.createTextNode(resultParts[2]));
			makeOnlyChild(document.getElementById("serviceUnitEmail"), emailAnchor);
			makeOnlyChild(document.getElementById("serviceUnitPhone"), document.createTextNode(resultParts[3]));
			makeOnlyChild(document.getElementById("serviceUnitAddress"), document.createTextNode(resultParts[4]));
			makeOnlyChild(document.getElementById("serviceUnitCityStateZip"), document.createTextNode(resultParts[5]));
			var troopPageAnchor = document.createElement("a");
			troopPageAnchor.setAttribute("href", resultParts[6]);
			troopPageAnchor.setAttribute("target", "_blank");
			makeOnlyChild(troopPageAnchor, document.createTextNode(resultParts[1] + "'s webpage"));
			makeOnlyChild(document.getElementById("serviceUnitInfoURL"), troopPageAnchor);
		}
	}
	conn.send();
}

function makeOnlyChild(parent, child) {
	while (parent.firstChild) {
		parent.removeChild(parent.firstChild);
	}
	parent.appendChild(child);
}


