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
        <jsp:include page="nav.jsp" />
        <h1>Customer Page</h1>
        <%-- Customer shortcuts --%>
        <ul>
            <li><a href="products.jsp">Browse products</a></li>
            <li><a href="pcbuilder.jsp">Plan a PC build, check compatibility and save it for the cashier</a></li>
            <li><a href="transactions.jsp">My transaction history</a></li>
        </ul>

        <h3>My PC build orders</h3>
        <%-- EDITED: the job order page now knows who is logged in, so no username is typed --%>
        <form action="do.joborders" method="GET">
            <input type="submit" value="Show my build orders">
        </form>
    </body>
</html>
