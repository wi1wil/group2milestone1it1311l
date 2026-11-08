<%--
    Document   : inventory (EDITED)
    Author     : Group 2
    Changes    : the purchase order form now opens the full purchase order page (do.createpo),
                 Cancelled was added to the status filter, and a link to receive stock was added.
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
        <jsp:include page="nav.jsp" />
        <h1>Inventory Management</h1>

        <h3>Add product</h3>
        <%-- Add a new product --%>
        <form action="do.addproduct" method="POST">
            ID: <input type="text" name="productid">
            Name: <input type="text" name="name">
            Category: <input type="text" name="category">
            Price: <input type="text" name="price">
            Stock: <input type="text" name="stock" value="0">
            <%-- EDITED: picture file name in web/images/productimg/ (blank = <ID>.jpg) --%>
            Picture file: <input type="text" name="image" placeholder="e.g. CPU-001.jpg">
            <input type="submit" value="Add product">
        </form>

        <h3>Change a product picture</h3>
        <%-- EDITED: put the picture in web/images/productimg/ first, then save its file name here.
             Blank file name = remove the picture (shows "No image available"). --%>
        <form action="do.setimage" method="POST">
            Product ID: <input type="text" name="productid">
            Picture file: <input type="text" name="image" placeholder="e.g. CPU-001.png">
            <input type="submit" value="Save picture">
        </form>
        <p><small>Pictures go in <b>web/images/productimg/</b>. Name them after the product ID (for example CPU-001.jpg).
            Allowed: .jpg, .jpeg, .png, .webp, .gif. A product whose file is missing shows "No image available".</small></p>

        <h3>Purchase orders (restocking)</h3>
        <%-- The purchase order page lists suppliers and low stock items from the database --%>
        <ul>
            <li><a href="do.createpo">Create a purchase order</a> (existing or new products)</li>
            <li><a href="do.restock">Receive stock</a> (admin approves the delivery)</li>
        </ul>

        <%-- Show purchase orders by status --%>
        <form action="do.purchaseorders" method="GET">
            Status:
            <select name="status">
                <option value="all">All</option>
                <option>Ordered</option>
                <option>Received</option>
                <option>Cancelled</option>
            </select>
            <input type="submit" value="Show purchase orders">
        </form>

        <h3>Other tools</h3>
        <ul>
            <li><a href="pcbuilder.jsp">Parts compatibility checking</a></li>
            <li><a href="verifyspec.jsp">AI product spec verification</a> (under construction)</li>
        </ul>
    </body>
</html>
