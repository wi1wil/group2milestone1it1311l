<%-- 
    Document   : home
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Home</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.jsp) --%>
        <jsp:include page="nav.jsp" />
        <section class="hero">
            <div>
                <span class="eyebrow">Welcome</span>
                <h1>Everything you need for your next build</h1>
                <p class="lead">Compare parts, check compatibility and plan your PC in one place.</p>
                <div class="actions">
                    <c:choose>
                        <c:when test="${empty sessionScope.role}">
                            <a class="btn" href="login.jsp">Login</a>
                            <a class="btn btn-ghost" href="add.jsp">Register</a>
                        </c:when>
                        <c:otherwise>
                            <a class="btn" href="index.jsp">Open the menu</a>
                        </c:otherwise>
                    </c:choose>
                    <a class="btn btn-ghost" href="products.jsp">Browse products</a>
                </div>
            </div>
            <img src="images/office.jpg" alt="Office">
        </section>
    </body>
</html>
