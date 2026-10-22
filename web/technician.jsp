<%--
    Document   : technician
    Author     : Group 2
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
        <jsp:include page="nav.html" />
        <h1>Technician - Job Orders</h1>
        <p><a href="do.joborders">View all job orders</a></p>

        <h3>Update a job order</h3>
        <%-- Update the status of a job order --%>
        <form action="do.updatejob" method="POST">
            <label>Job: </label>
            <%-- Job IDs are hardcoded for now (temporary data) --%>
            <select name="jobid">
                <option>JOB-001</option>
                <option>JOB-002</option>
                <option>JOB-003</option>
            </select>
            <label>Status: </label>
            <select name="status">
                <option>Pending</option>
                <option>In Progress</option>
                <option>Success</option>
            </select>
            <input type="submit" value="Update">
        </form>
        <p><a href="pcbuilder.jsp">Open PC Builder / compatibility check</a></p>
    </body>
</html>
