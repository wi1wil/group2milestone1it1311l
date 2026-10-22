<%--
    Document   : users
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>User Accounts</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>User Accounts</h1>
        <%-- Filter the user list by role --%>
        <form action="do.users" method="GET">
            <label>Role: </label>
            <select name="role">
                <option value="all">All</option>
                <option>admin</option>
                <option>cashier</option>
                <option>inventory</option>
                <option>technician</option>
                <option>customer</option>
            </select>
            <input type="submit" value="Show users">
        </form>

        <h3>Remove a user</h3>
        <%-- Remove an account by username --%>
        <form action="do.removeuser" method="POST">
            Username: <input type="text" name="username">
            <input type="submit" value="Remove user">
        </form>

        <p><a href="add.jsp">Register a new account</a> | <a href="admin.jsp">Back to Admin</a></p>
    </body>
</html>
