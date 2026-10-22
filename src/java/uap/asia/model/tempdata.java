/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uap.asia.model;

/**
 * TEMPORARY sample data shared by the view servlets.
 * Replace with session / database data later.
 *
 * @author Group 2
 */
public class tempdata {

    // username, role
    public static final String[][] USERS = {
        {"admin", "admin"},
        {"cashier", "cashier"},
        {"inventory", "inventory"},
        {"technician", "technician"},
        {"customer", "customer"}
    };

    // id, name, category, price, stock
    public static final String[][] PRODUCTS = {
        {"CPU-001", "AMD Ryzen 5 7600", "CPU", "12500", "15"},
        {"CPU-002", "Intel Core i5-12400F", "CPU", "8900", "3"},
        {"MB-001", "ASRock B650M Pro RS", "Motherboard", "8200", "8"},
        {"MB-002", "MSI PRO B660M-A DDR4", "Motherboard", "6900", "4"},
        {"RAM-001", "Kingston Fury 16GB DDR5-5600", "RAM", "3200", "20"},
        {"RAM-002", "Corsair Vengeance 16GB DDR4-3200", "RAM", "2300", "2"},
        {"GPU-001", "RTX 4060 8GB", "GPU", "19500", "6"},
        {"SSD-001", "Samsung 980 1TB", "Storage", "4200", "12"},
        {"PSU-001", "Corsair CV650 650W", "Power Supply", "3800", "9"},
        {"CASE-001", "Tecware Forge M2 (mATX)", "Case", "2900", "5"},
        {"COOL-001", "Deepcool AK400", "CPU Cooler", "1500", "10"}
    };

    // id, date, type, product, qty, amount, made by
    public static final String[][] TRANSACTIONS = {
        {"TX-0001", "2026-10-01", "SALE", "CPU-001", "1", "12500.00", "cashier"},
        {"TX-0002", "2026-10-02", "SALE", "RAM-001", "2", "6400.00", "cashier"},
        {"TX-0003", "2026-10-03", "REFUND", "RAM-001", "1", "-3200.00", "cashier"},
        {"TX-0004", "2026-10-05", "SALE", "GPU-001", "1", "19500.00", "customer"}
    };

    // name, contact
    public static final String[][] SUPPLIERS = {
        {"TechSource Manila", "0917-111-2222"},
        {"PC Parts Hub", "0918-333-4444"}
    };

    // po, supplier, product, qty, status
    public static final String[][] PURCHASEORDERS = {
        {"PO-001", "TechSource Manila", "CPU-001", "10", "Ordered"},
        {"PO-002", "PC Parts Hub", "RAM-002", "20", "Ordered"},
        {"PO-003", "TechSource Manila", "SSD-001", "5", "Received"}
    };

    // id, customer, description, status
    public static final String[][] JOBS = {
        {"JOB-001", "customer", "Ryzen 5 7600 gaming build", "Pending"},
        {"JOB-002", "customer", "Office PC build", "In Progress"},
        {"JOB-003", "juan", "Intel i5 budget build", "Success"}
    };
}
