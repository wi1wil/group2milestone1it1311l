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
        <title>Returns and Refunds</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>Returns and Refunds</h1>
        <%-- Refund a returned product --%>
        <form action="do.refund" method="POST">
            <p>Product ID: <input type="text" name="productid"></p>
            <p>Quantity returned: <input type="text" name="quantity" value="1"></p>
            <p>Reason: <input type="text" name="reason"></p>
            <p><input type="submit" value="Process refund"></p>
        </form>
        <a href="cashier.jsp">Back to Cashier</a>
    </body>
</html>
