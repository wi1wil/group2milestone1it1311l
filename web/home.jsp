<%--
    Document   : home
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Home</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <img src="images/office.jpg" width="400" alt="alt"/>
        <h1>Welcome!</h1>
        <p><a href="login.jsp">Login</a> or <a href="add.jsp">Register</a></p>

        <h3>Go to a page</h3>
        <%-- Links to each role's page --%>
        <ul>
            <li><a href="customer.jsp">Customer</a></li>
            <li><a href="cashier.jsp">Cashier</a></li>
            <li><a href="inventory.jsp">Inventory</a></li>
            <li><a href="technician.jsp">Technician</a></li>
            <li><a href="admin.jsp">Administrator</a></li>
        </ul>
        <a href='index.jsp'>Return...</a>
    </body>
</html>
