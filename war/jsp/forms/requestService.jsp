<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <title>Common Patriots - Request Service</title>
  <link type="text/css" rel="stylesheet" href="/stylesheets/common.css" />
  <link type="text/css" rel="stylesheet" href="/stylesheets/form.css" />

  <script type="text/javascript" src="/javascript/common.js"></script>
  <script type="text/javascript" src="/javascript/form.js"></script>
</head>

<body>
  <h2>There were no results...</h2>
  <p>Fill out the form below to request service at your location.  An administrator will reply to you as soon as possible.</p>
  <form action="/commonpatriots/forms/requestServiceSubmit" method="post">
    <input type="hidden" name="lat" value="${lat}" />
    <input type="hidden" name="lng" value="${lng}" />
    <input type="hidden" name="query" value="${query}" />
    Name: <input type="text" name="name" value="${nickname}" /><br />
    Email: <input type="email" name="email" value="${email}" /><br />
    Address requesting service: <input type="text" name="addr" value="${query}"/><br />
    <input type="submit" value="Request Service" />
  </form>
</body>
</html>