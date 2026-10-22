<%--
    Document   : suppliers
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Suppliers</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>Suppliers</h1>

        <h3>Supplier list</h3>
        <p><a href="do.suppliers">View suppliers</a></p>

        <h3>Add supplier</h3>
        <%-- Add a supplier --%>
        <form action="do.addsupplier" method="POST">
            Name: <input type="text" name="name">
            Contact: <input type="text" name="contact">
            <input type="submit" value="Add supplier">
        </form>

        <p><a href="inventory.jsp">Back to Inventory</a> | <a href="index.jsp">Back to index</a></p>
    </body>
</html>
