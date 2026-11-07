<%--
    Document   : nav.jsp
    Author     : Group 2
    Admin sees every link.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%-- here = the page being shown, used to highlight the current link --%>
<c:set var="here" value="${pageContext.request.requestURI}" />
<c:set var="role" value="${sessionScope.role}" />
<c:set var="isAdmin" value="${role == 'admin'}" />
<nav>
    <a class="brand" href="index.jsp">Computer<span>Store</span></a>
    <div class="links">
        <a href="home.jsp" class="${here.endsWith('/home.jsp') ? 'active' : ''}">Home</a>
        <a href="products.jsp" class="${here.endsWith('/products.jsp') ? 'active' : ''}">Products</a>
        <a href="pcbuilder.jsp" class="${here.endsWith('/pcbuilder.jsp') ? 'active' : ''}">PC Builder</a>

        <c:if test="${isAdmin || role == 'customer'}">
            <a href="customer.jsp" class="${here.endsWith('/customer.jsp') ? 'active' : ''}">Customer</a>
        </c:if>
        <c:if test="${isAdmin || role == 'customer' || role == 'cashier'}">
            <a href="transactions.jsp" class="${here.endsWith('/transactions.jsp') ? 'active' : ''}">Transactions</a>
        </c:if>
        <c:if test="${isAdmin || role == 'cashier'}">
            <a href="cashier.jsp" class="${here.endsWith('/cashier.jsp') ? 'active' : ''}">Cashier</a>
            <a href="refund.jsp" class="${here.endsWith('/refund.jsp') ? 'active' : ''}">Refund</a>
        </c:if>
        <c:if test="${isAdmin || role == 'inventory'}">
            <a href="inventory.jsp" class="${here.endsWith('/inventory.jsp') ? 'active' : ''}">Inventory</a>
            <a href="stock.jsp" class="${here.endsWith('/stock.jsp') ? 'active' : ''}">Stock</a>
            <a href="suppliers.jsp" class="${here.endsWith('/suppliers.jsp') ? 'active' : ''}">Suppliers</a>
        </c:if>
        <c:if test="${isAdmin || role == 'technician'}">
            <a href="technician.jsp" class="${here.endsWith('/technician.jsp') ? 'active' : ''}">Technician</a>
        </c:if>
        <c:if test="${isAdmin}">
            <a href="admin.jsp" class="${here.endsWith('/admin.jsp') ? 'active' : ''}">Admin</a>
            <a href="users.jsp" class="${here.endsWith('/users.jsp') ? 'active' : ''}">Users</a>
            <a href="reports.jsp" class="${here.endsWith('/reports.jsp') ? 'active' : ''}">Reports</a>
        </c:if>
    </div>
    <div class="account">
        <c:choose>
            <c:when test="${empty role}">
                <a href="login.jsp">Login</a>
                <a class="btn" href="add.jsp">Sign up</a>
            </c:when>
            <c:otherwise>
                <span>${sessionScope.name} <span class="badge">${role}</span></span>
                <a class="btn btn-ghost" href="do.logout">Logout</a>
            </c:otherwise>
        </c:choose>
    </div>
</nav>
