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
