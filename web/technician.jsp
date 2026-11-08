<%--
    Document   : technician (EDITED)
    Author     : Group 2
    Changes    : the hardcoded job dropdown was replaced by the job order page (do.updatejob),
                 which lists the real job orders and lets the technician edit the build and the status.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Technician</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.jsp" />
        <h1>Technician - Job Orders</h1>

        <h3>Job orders</h3>
        <p><a href="do.updatejob">View and update job orders</a> (change the parts of a build, or its status)</p>

        <h3>Open a job directly</h3>
        <%-- Opens one job order by its number --%>
        <form action="do.updatejob" method="GET">
            <label>Job no. (e.g. 1 or JO-001): </label>
            <input type="text" name="jobid">
            <input type="submit" value="Open">
        </form>

        <p><a href="pcbuilder.jsp">Open PC Builder / compatibility check</a></p>
    </body>
</html>
