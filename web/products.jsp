<%--
    Document   : products
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Products</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>Product Catalog</h1>

        <h3>Search</h3>
        <%-- Search products by text --%>
        <form action="do.searchproduct" method="POST">
            Search: <input type="text" name="q">
            <input type="submit" value="Search">
        </form>

        <h3>Browse products</h3>
        <%-- Browse products by category --%>
        <form action="do.products" method="GET">
            Category:
            <select name="category">
                <option value="all">All</option>
                <option>CPU</option>
                <option>Motherboard</option>
                <option>RAM</option>
                <option>GPU</option>
                <option>Storage</option>
                <option>Power Supply</option>
                <option>Case</option>
                <option>CPU Cooler</option>
            </select>
            <input type="submit" value="Show products">
        </form>

        <h3>Buy a product</h3>
        <%-- Buy a product by ID and quantity --%>
        <form action="do.buyproduct" method="POST">
            Product ID: <input type="text" name="productid">
            Quantity: <input type="text" name="quantity" value="1">
            <input type="submit" value="Buy">
        </form>
    </body>
</html>
