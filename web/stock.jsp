<%--
    Document   : stock
    Author     : Group 2
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
        <jsp:include page="nav.html" />
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
        <%-- Receive stock from a purchase order --%>
        <form action="do.restock" method="POST">
            <%-- Purchase orders are hardcoded for now (temporary data) --%>
            <select name="po">
                <option value="PO-001">PO-001 (10 x CPU-001)</option>
                <option value="PO-002">PO-002 (20 x RAM-002)</option>
            </select>
            <input type="submit" value="Receive">
        </form>

        <p><a href="inventory.jsp">Back to Inventory</a> | <a href="index.jsp">Back to index</a></p>
    </body>
</html>
