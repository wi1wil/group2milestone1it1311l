<%-- 
    Document   : admin
    Created on : Oct 1, 2026, 5:13:34 PM
    Author     : Group 2
--%>


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Administrator</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>Administrator Page</h1>
        <ul>
            <li><a href="users.jsp">Manage user accounts</a></li>
            <li><a href="reports.jsp">View sales reports</a></li>
            <li><a href="inventory.jsp">Add items / manage inventory</a></li>
            <li><a href="stock.jsp">Stock levels</a></li>
            <li><a href="suppliers.jsp">Suppliers</a></li>
            <li><a href="cashier.jsp">Supervise selling / mimic buying (POS)</a></li>
            <li><a href="technician.jsp">Technician job orders</a></li>
            <li><a href="verifyspec.jsp">AI spec verification</a></li>
        </ul>

        <h3>Edit product price</h3>
        <%-- Change a product's price --%>
        <form action="do.editprice" method="POST">
            Product ID: <input type="text" name="productid">
            New price: <input type="text" name="newprice">
            <input type="submit" value="Update price">
        </form>

        <h3>Create promo / discount</h3>
        <%-- Create a promo / discount --%>
        <form action="do.setpromo" method="POST">
            Promo name: <input type="text" name="promoname">
            Percent off: <input type="text" name="percent">
            Product ID (blank = all): <input type="text" name="target">
            <input type="submit" value="Create promo">
        </form>

        </table>
        
        <a href="index.jsp">Back to index</a>
    </body>
</html>
