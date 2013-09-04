var map;
var geocoder;
var marker;
var drawingManager;

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
		  polygon.setOptions({
			  editable : true
		  })
		  google.maps.event.addListener(polygon.getPath(), 'set_at', getMakeActivePolygonFn(polygon));
		  google.maps.event.addListener(polygon.getPath(), 'insert_at', getMakeActivePolygonFn(polygon));
		  google.maps.event.addListener(polygon.getPath(), 'remove_at', getMakeActivePolygonFn(polygon));
		  google.maps.event.addListener(polygon, 'click', getMakeActivePolygonFn(polygon));
		  polygon.setMap(map);
	  }
  });

  drawingManager = new google.maps.drawing.DrawingManager({
	  drawingMode: null,
	  drawingControl: true,
	  drawingControlOptions: {
	    position: google.maps.ControlPosition.RIGHT_TOP,
	    drawingModes: [
	      google.maps.drawing.OverlayType.POLYGON
	    ]
	  },
	  polygonOptions: {
	    fillColor: '#ffff00',
	    fillOpacity: 0.7,
	    strokeWeight: 5,
	    clickable: true,
	    zIndex: 1,
	    editable: true
	  }
	});
  drawingManager.setMap(map);

  google.maps.event.addListener(drawingManager, 'polygoncomplete', makeActivePolygon);
}

var loadMapsAPI = function() {
  var script = document.createElement("script");
  script.type = "text/javascript";
  script.src = "https://maps.googleapis.com/maps/api/js?libraries=drawing&key=AIzaSyBanPX_Vnws7Xc4_U9Fx_ApgLTbgDDrl14&sensor=true&callback=initialize&v=3";
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