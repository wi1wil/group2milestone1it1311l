<%-- 
    Document   : add
    Created on : Sep 29, 2026, 9:31:52 AM
    Author     : Group 2
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Register account</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <img src="images/user.jpg" width="400" height="400" alt="alt"/>
        <h1>Register yourself!</h1>
        <%-- Registration: sends the new account details --%>
        <form method='POST' action='do.register'>
            <p><label>Input your username:</label>
            <input type='text' name='username'></p>

            <p><label>Input your password: </label>
            <input type='text' name='password'></p>

            <p><label>Role:</label>
            <%-- Role of the new account --%>
            <select name='role'>
                <option value='customer'>Customer</option>
                <option value='cashier'>Cashier</option>
                <option value='inventory'>Inventory</option>
                <option value='technician'>Technician</option>
                <option value='admin'>Administrator</option>
            </select></p>

            <%-- Pick an avatar image --%>
            <label>
                    <img src="images/usr1.jpg" width="200" height="200" alt="alt"/>
                    <input type='radio' name='idx' value='1'>
            </label>

            <label>
                    <img src="images/usr2.jpg" width="200" height="200" alt="alt"/>
                    <input type='radio' name='idx' value='2'>
            </label>

            <p><input type='submit' name='btnSubmit' value='Submit!'></p>
        </form>
        <a href='index.jsp'>Return</a>
        
    </body>
</html>
