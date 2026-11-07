<%-- 
    Document   : addemployee
    Author     : Group 2
    Admin only (checked by accessfilter)
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Add employee</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.jsp) --%>
        <jsp:include page="nav.jsp" />
        <h1>Add an employee or another account</h1>
        <%-- Same servlet as the registration; it only accepts a role from a logged-in admin --%>
        <form method='POST' action='do.register'>
            <input type='hidden' name='kind' value='employee'>

            <p><label>Username:</label>
            <input type='text' name='username'></p>

            <p><label>Password:</label>
            <input type='text' name='password'></p>

            <p><label>First name:</label>
            <input type='text' name='firstname'></p>

            <p><label>Last name:</label>
            <input type='text' name='lastname'></p>

            <p><label>Role:</label>
            <%-- The role is chosen here; leaving the first option means cashier --%>
            <select name='role'>
                <option value=''>Employee (cashier)</option>
                <option value='cashier'>Cashier</option>
                <option value='inventory'>Inventory</option>
                <option value='technician'>Technician</option>
                <option value='admin'>Administrator</option>
                <option value='customer'>Customer</option>
            </select></p>

            <p><input type='submit' value='Create account'></p>
        </form>
        <a href='users.jsp'>Back to Users</a>
    </body>
</html>
