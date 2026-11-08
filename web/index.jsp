<%-- 
    Document   : index.jsp
    Created on : Sep 29, 2026, 9:25:47 AM
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- JSTL core tags: c:if / c:choose show their content only when the test is true --%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Computer Store</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
         <%-- Shared navigation bar (nav.jsp) --%>
        <jsp:include page="nav.jsp" />

        <%-- Hero: the first thing visitors see --%>
        <section class="hero">
            <div>
                <span class="eyebrow">Computer Store POS and PC Building System</span>
                <h1>Build your dream PC</h1>
                <p class="lead">Browse parts, check that they work together and let AI verify the specs.
                    Staff run sales, stock and repairs from the same place.</p>
                <ul class="chips">
                    <li>Parts compatibility check</li>
                    <li>AI spec verification</li>
                    <li>Live stock and prices</li>
                    <li>Point of sale</li>
                </ul>
                <div class="actions">
                    <a class="btn" href="pcbuilder.jsp">Start building</a>
                    <c:choose>
                        <c:when test="${empty sessionScope.role}">
                            <a class="btn btn-ghost" href="login.jsp">Login</a>
                            <a class="btn btn-ghost" href="add.jsp">Create account</a>
                        </c:when>
                        <c:otherwise>
                            <a class="btn btn-ghost" href="products.jsp">Browse products</a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <img src="images/building.jpg" alt="Computer store">
        </section>

        <%-- Cards: each one shows only for the roles that can open its pages --%>
        <h3>Where to?</h3>
        <section class="grid">
            <div class="card">
                <h3>Shop</h3>
                <p>Browse parts and check compatibility.</p>
                <ul>
                    <li><a href="products.jsp">Browse and search</a></li>
                    <li><a href="pcbuilder.jsp">PC Builder</a></li>
                </ul>
            </div>

            <c:if test="${sessionScope.role == 'customer' || sessionScope.role == 'admin'}">
                <div class="card">
                    <h3>Customer</h3>
                    <p>Your orders and repairs.</p>
                    <ul>
                        <li><a href="customer.jsp">Customer page</a></li>
                        <li><a href="transactions.jsp">Transaction history</a></li>
                    </ul>
                </div>
            </c:if>

            <c:if test="${sessionScope.role == 'cashier' || sessionScope.role == 'admin'}">
                <div class="card">
                    <h3>Cashier</h3>
                    <p>Sell at the counter and handle returns.</p>
                    <ul>
                        <li><a href="cashier.jsp">Point of sale</a></li>
                        <li><a href="refund.jsp">Returns and refunds</a></li>
                        <li><a href="transactions.jsp">Transaction history</a></li>
                    </ul>
                </div>
            </c:if>

            <c:if test="${sessionScope.role == 'inventory' || sessionScope.role == 'admin'}">
                <div class="card">
                    <h3>Inventory</h3>
                    <p>Products, stock and purchase orders.</p>
                    <ul>
                        <li><a href="inventory.jsp">Inventory and purchase orders</a></li>
                        <li><a href="stock.jsp">Stock levels and receiving</a></li>
                        <li><a href="suppliers.jsp">Suppliers</a></li>
                        <li><a href="verifyspec.jsp">AI spec verification</a></li>
                    </ul>
                </div>
            </c:if>

            <c:if test="${sessionScope.role == 'technician' || sessionScope.role == 'admin'}">
                <div class="card">
                    <h3>Technician</h3>
                    <p>Repair and build job orders.</p>
                    <ul>
                        <li><a href="technician.jsp">Job orders</a></li>
                    </ul>
                </div>
            </c:if>

            <c:if test="${sessionScope.role == 'admin'}">
                <div class="card">
                    <h3>Administrator</h3>
                    <p>Accounts, prices, promos and reports.</p>
                    <ul>
                        <li><a href="admin.jsp">Admin page</a></li>
                        <li><a href="users.jsp">User accounts</a></li>
                        <li><a href="addemployee.jsp">Add an employee</a></li>
                        <li><a href="reports.jsp">Sales reports</a></li>
                    </ul>
                </div>
            </c:if>
        </section>
    </body>
</html>
