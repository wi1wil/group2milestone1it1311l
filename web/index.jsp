<%-- 
    Document   : index.jsp
    Created on : Sep 29, 2026, 9:25:47 AM
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Index</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <img src="images/building.jpg" width="400" height="600" alt="alt"/>
        <h1>Computer Store POS and PC Building System</h1>

        <%-- Main menu: links to every page, grouped by role --%>
        <h3>Account</h3>
        <ul>
            <li><a href="add.jsp">Registration</a></li>
            <li><a href="login.jsp">Login</a></li>
            <li><a href="home.jsp">Home</a></li>
            <li><a href="do.logout">Logout</a></li>
        </ul>

        <h3>Customer</h3>
        <ul>
            <li><a href="customer.jsp">Customer page</a></li>
            <li><a href="products.jsp">Browse / search / buy products</a></li>
            <li><a href="pcbuilder.jsp">PC Builder and compatibility check</a></li>
            <li><a href="transactions.jsp">Transaction history</a></li>
        </ul>

        <h3>Cashier</h3>
        <ul>
            <li><a href="cashier.jsp">POS</a></li>
            <li><a href="refund.jsp">Returns and refunds</a></li>
        </ul>

        <h3>Inventory</h3>
        <ul>
            <li><a href="inventory.jsp">Inventory and purchase orders</a></li>
            <li><a href="stock.jsp">Stock levels and receiving stock</a></li>
            <li><a href="suppliers.jsp">Suppliers</a></li>
            <li><a href="verifyspec.jsp">AI spec verification</a></li>
        </ul>

        <h3>Technician</h3>
        <ul>
            <li><a href="technician.jsp">Job orders</a></li>
        </ul>

        <h3>Administrator</h3>
        <ul>
            <li><a href="admin.jsp">Admin page</a></li>
            <li><a href="users.jsp">User accounts</a></li>
            <li><a href="reports.jsp">Sales reports</a></li>
        </ul>
    </body>
</html>
