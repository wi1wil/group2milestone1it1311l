<%--
    Document   : refund (EDITED)
    Author     : Group 2
    Changes    : refunds are now made per sale. The form looks up the sale (do.refund),
                 which lists its items from the database with a refund form for each.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Returns and Refunds</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.jsp" />
        <h1>Returns and Refunds</h1>
        <%-- Find the sale first; the refund page then shows its items --%>
        <form action="do.refund" method="GET">
            <p>Sale ID or receipt no. (e.g. 7 or TX-0007): <input type="text" name="saleid"></p>
            <p><input type="submit" value="Find sale"></p>
        </form>
        <p><a href="do.refund">Open the refund page</a> (also shows the last receipt of this session)</p>
        <a href="cashier.jsp">Back to Cashier</a>
    </body>
</html>
