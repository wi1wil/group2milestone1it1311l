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
        <jsp:include page="nav.jsp" />
        <h1>Register yourself!</h1>
        <p>New accounts are customer accounts. Staff accounts are created by an administrator.</p>
        <%-- Registration: sends the new account details --%>
        <form method='POST' action='do.register'>
            <p><label>Input your username:</label>
            <input type='text' name='username'></p>

            <p><label>Input your password: </label>
            <input type='text' name='password'></p>

            <p><label>First name:</label>
            <input type='text' name='firstname'></p>

            <p><label>Last name:</label>
            <input type='text' name='lastname'></p>

            <%-- Contact number and address are optional (saved for customers) --%>
            <p><label>Contact number (optional):</label>
            <input type='text' name='contact'></p>

            <p><label>Address (optional):</label>
            <input type='text' name='address'></p>

            <p><input type='submit' name='btnSubmit' value='Submit!'></p>
        </form>
        <a href='index.jsp'>Return</a>
        
    </body>
</html>
