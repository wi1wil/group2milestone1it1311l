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
        <title>Logins</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>User login</h1>
        <%-- Login: sends the username and password --%>
        <form method="POST" action="do.authenticate">
            <p><label>Input your username: <input type='text' name='usn'></label></p>
            <p><label>Input your password: <input type='text' name='pass'></label></p>
            <p><input type='submit' name='btnSubmit' value='Submit'></p>
        </form>
        <p>Sample accounts (username / password): admin / admin, cashier / cashier,
           inventory / inventory, technician / technician, customer / customer</p>
        
        <a href='index.jsp' class='indexbtn'>Back to index</a>
    </body>
</html>
