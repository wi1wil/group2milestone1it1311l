<%--
    Document   : transactions
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Transaction History</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>Transaction History</h1>
        <%-- Filter the transaction list --%>
        <form action="do.transactions" method="GET">
            <label>Type: </label>
            <select name="type">
                <option value="ALL">All</option>
                <option value="SALE">Sales</option>
                <option value="REFUND">Refunds</option>
            </select>
            <label>Made by (blank = everyone): </label>
            <input type="text" name="by">
            <input type="submit" value="Show transactions">
        </form>
        <p>Make a sale on the <a href="cashier.jsp">Cashier</a> page.</p>
    </body>
</html>
