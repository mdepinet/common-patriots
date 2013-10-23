<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <title>Common Patriots</title>
  <link rel="shortcut icon" href="img/shortcut_icon.jpg" />
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
<%@ include file="./mapFrame.jsp" %>
</body>
</html>