<%--
    Document   : customer
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Customer</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>Customer Page</h1>
        <%-- Customer shortcuts --%>
        <ul>
            <li><a href="products.jsp">Browse and buy products</a></li>
            <li><a href="pcbuilder.jsp">Create a PC build / check parts compatibility</a></li>
            <li><a href="transactions.jsp">My transaction history</a></li>
        </ul>

        <h3>My PC build orders</h3>
        <%-- Show the job orders of the entered customer --%>
        <form action="do.joborders" method="GET">
            <label>Username: </label>
            <input type="text" name="customer" value="customer">
            <input type="submit" value="Show my build orders">
        </form>
    </body>
</html>
