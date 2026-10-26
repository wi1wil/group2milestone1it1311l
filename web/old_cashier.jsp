<%-- 
    Document   : cashier
    Created on : Oct 11, 2026, 5:13:34 PM
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Cashier</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>Cashier POS System</h1>

        <%-- POS sale: Done completes it, Cancel discards it --%>
        <form action="do.donecashier" method="POST">
            <label>Input product ID: </label>
            <input type="text" name="productid"> <br> <br>

            <label>Input product price: </label>
            <input type="text" name="productprice"> <br> <br>

            <label>Input quantity: </label>
            <input type="text" name="productquantity"> <br> <br>

            <label>Discount (%): </label>
            <input type="text" name="discount" value="0"> <br> <br>

            <label>Amount paid: </label>
            <input type="text" name="paid"> <br> <br>

            <%-- Done completes the sale, Cancel discards it --%>
            <input type="submit" name="donebtn" value="Done">
            <input type="submit" name="cancelbtn" value="Cancel">
        </form>

        <p>Tip: product IDs and prices are listed on the <a href="products.jsp">Products</a> page.</p>
        <ul>
            <li><a href="refund.jsp">Returns and refunds</a></li>
            <li><a href="transactions.jsp">Transaction history</a></li>
        </ul>
    </body>
</html>
