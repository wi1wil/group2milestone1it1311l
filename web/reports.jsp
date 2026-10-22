<%--
    Document   : reports
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Sales Reports</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>Sales Report</h1>
        <%-- Choose which report to show --%>
        <form action="do.reports" method="GET">
            <label>Report type: </label>
            <select name="type">
                <option value="summary">Sales summary</option>
                <option value="lowstock">Low stock items</option>
            </select>
            <input type="submit" value="View report">
        </form>
        <p><a href="transactions.jsp">See all transactions</a> | <a href="admin.jsp">Back to Admin</a></p>
    </body>
</html>
