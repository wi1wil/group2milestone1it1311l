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
        <title>PC Builder</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        <%-- Shared navigation bar (nav.html) --%>
        <jsp:include page="nav.html" />
        <h1>PC Builder</h1>
        <p>Pick one part per category. Try an AM5 CPU with an LGA1700 motherboard to see an incompatible result.</p>

        <%-- Check compatibility or place a build order --%>
        <form action="do.checkbuild" method="POST">
            <%-- Each option value is "name|spec|...|price"; checkbuild splits it at the "|" --%>
            <p>CPU:
                <select name="cpu">
                    <option value="AMD Ryzen 5 7600|AM5|65|12500">AMD Ryzen 5 7600 (AM5) - 12500</option>
                    <option value="Intel Core i5-12400F|LGA1700|65|8900">Intel Core i5-12400F (LGA1700) - 8900</option>
                    <option value="AMD Ryzen 7 7800X3D|AM5|120|24500">AMD Ryzen 7 7800X3D (AM5) - 24500</option>
                </select></p>

            <p>Motherboard:
                <select name="motherboard">
                    <option value="ASRock B650M Pro RS|AM5|DDR5|mATX|8200">ASRock B650M Pro RS (AM5, DDR5, mATX) - 8200</option>
                    <option value="MSI PRO B660M-A DDR4|LGA1700|DDR4|mATX|6900">MSI PRO B660M-A (LGA1700, DDR4, mATX) - 6900</option>
                    <option value="ASUS ROG Strix X670E-E|AM5|DDR5|ATX|21000">ASUS ROG Strix X670E-E (AM5, DDR5, ATX) - 21000</option>
                </select></p>

            <p>RAM:
                <select name="ram">
                    <option value="Kingston Fury 16GB DDR5-5600|DDR5|3200">Kingston Fury 16GB DDR5 - 3200</option>
                    <option value="Corsair Vengeance 16GB DDR4-3200|DDR4|2300">Corsair Vengeance 16GB DDR4 - 2300</option>
                </select></p>

            <p>Graphics card:
                <select name="gpu">
                    <option value="None (integrated)|0|0">None (integrated) - 0</option>
                    <option value="RTX 4060 8GB|115|19500">RTX 4060 8GB - 19500</option>
                    <option value="RTX 4070 Super 12GB|220|38000">RTX 4070 Super 12GB - 38000</option>
                </select></p>

            <p>Storage:
                <select name="storage">
                    <option value="Samsung 980 1TB|4200">Samsung 980 1TB - 4200</option>
                    <option value="WD Blue 500GB|2200">WD Blue 500GB - 2200</option>
                </select></p>

            <p>Power supply:
                <select name="psu">
                    <option value="Corsair CV450 450W|450|3000">Corsair CV450 450W - 3000</option>
                    <option value="Corsair CV650 650W|650|3800">Corsair CV650 650W - 3800</option>
                    <option value="Corsair RM850e 850W|850|8500">Corsair RM850e 850W - 8500</option>
                </select></p>

            <p>Case:
                <select name="pccase">
                    <option value="Tecware Forge M2 (mATX)|mATX|2900">Tecware Forge M2 (mATX) - 2900</option>
                    <option value="NZXT H5 Flow (ATX)|ATX|5500">NZXT H5 Flow (ATX) - 5500</option>
                    <option value="Cooler Master Elite 110 (ITX)|ITX|3500">Cooler Master Elite 110 (ITX) - 3500</option>
                </select></p>

            <p>CPU cooler:
                <select name="cooler">
                    <option value="Deepcool AK400|1500">Deepcool AK400 - 1500</option>
                    <option value="Stock cooler|0">Stock cooler - 0</option>
                </select></p>

            <p>
                <%-- check = only test compatibility, order = also place the build order --%>
                <button type="submit" name="action" value="check">Check compatibility</button>
                <button type="submit" name="action" value="order">Place build order</button>
            </p>
        </form>
    </body>
</html>
