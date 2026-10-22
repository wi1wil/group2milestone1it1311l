<%--
    Document   : inventory
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Inventory</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>Inventory Management</h1>

        <h3>Add product</h3>
        <%-- Add a new product --%>
        <form action="do.addproduct" method="POST">
            ID: <input type="text" name="productid">
            Name: <input type="text" name="name">
            Category: <input type="text" name="category">
            Price: <input type="text" name="price">
            Stock: <input type="text" name="stock" value="0">
            <input type="submit" value="Add product">
        </form>

        <h3>Create purchase order</h3>
        <%-- Create a purchase order --%>
        <form action="do.createpo" method="POST">
            Supplier:
            <%-- Suppliers are hardcoded for now (temporary data) --%>
            <select name="supplier">
                <option>TechSource Manila</option>
                <option>PC Parts Hub</option>
            </select>
            Product ID: <input type="text" name="productid">
            Quantity: <input type="text" name="quantity" value="10">
            <input type="submit" value="Create PO">
        </form>

        <h3>Purchase orders</h3>
        <%-- Show purchase orders by status --%>
        <form action="do.purchaseorders" method="GET">
            Status:
            <select name="status">
                <option value="all">All</option>
                <option>Ordered</option>
                <option>Received</option>
            </select>
            <input type="submit" value="Show purchase orders">
        </form>

        <h3>Other tools</h3>
        <ul>
            <li><a href="pcbuilder.jsp">Parts compatibility checking</a></li>
            <li><a href="verifyspec.jsp">AI product spec verification</a></li>
        </ul>
    </body>
</html>
