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
        <title>Spec Verification</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>AI Product Specification Verification</h1>
        <p>Compare a spec stored in the system with the manufacturer's spec.</p>
        <%-- Compare the stored spec with the manufacturer's spec --%>
        <form action="do.verifyspec" method="POST">
            <p>Product name: <input type="text" name="product" value="AMD Ryzen 5 7600"></p>
            <p>Field:
                <select name="field">
                    <option>Socket</option>
                    <option>Memory type</option>
                    <option>TDP / Wattage</option>
                    <option>Form factor</option>
                </select></p>
            <p>Stored in system: <input type="text" name="stored" value="AM4"></p>
            <p>Manufacturer spec: <input type="text" name="manufacturer" value="AM5"></p>
            <p><input type="submit" value="Verify"></p>
        </form>
    </body>
</html>
