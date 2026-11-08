<%--
    Document   : stock (EDITED)
    Author     : Group 2
    Changes    : the hardcoded PO dropdown was replaced by a link to the receive stock page (do.restock),
                 which lists the real purchase orders from the database.
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Stock</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.jsp" />
        <h1>Stock</h1>

        <h3>Stock levels (low stock alert at 5 or fewer)</h3>
        <%-- Show stock levels --%>
        <form action="do.stock" method="GET">
            Show:
            <select name="filter">
                <option value="all">All products</option>
                <option value="low">Low stock only</option>
            </select>
            <input type="submit" value="Show stock">
        </form>

        <h3>Receive stock from a purchase order</h3>
        <%-- The receive page lists the purchase orders that are waiting (status Ordered) --%>
        <p><a href="do.restock">Open the receive stock page</a> (the admin approves each delivery)</p>
        <p><a href="do.createpo">Create a purchase order</a></p>

        <p><a href="inventory.jsp">Back to Inventory</a></p><p><a href="index.jsp" class="indexbtn">Back to menu</a></p>
    </body>
</html>
