/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uap.asia.functionalclasses;

import java.sql.*;
import java.util.ResourceBundle;

/**
 *
 * @author Andrei
 */
public class Sqlc {
    
    private static ResourceBundle rb = ResourceBundle.getBundle("uap.asia.model.group2sql");
    private static String driver = rb.getString("driver");
    private static String dbUrl = rb.getString("url");
    private static String user = rb.getString("user");
    private static String pass = rb.getString("password");
    
    
    private static boolean loadClass() {
        try {
            Class.forName(driver);
            return true;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return false;
        }        
    }

    
    public static Double checkPrice(Integer product_id){
        loadClass();    
        Connection conn = null;
        
        try {
            conn = DriverManager.getConnection(dbUrl, user, pass);
            String selectSQL = "SELECT price FROM products WHERE product_id = ?";
            PreparedStatement ps = conn.prepareStatement(selectSQL);
            
            ps.setInt(1, product_id);
            
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getDouble("product_id");
            }
            else {
                return null;
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return -9.999999;
    }
    
    public static boolean checkProductId(Integer id) {
        loadClass();
        Connection conn = null;
        
        try {
            conn = DriverManager.getConnection(dbUrl, user, pass);
            String selectSQL = "SELECT COUNT(*) FROM products WHERE product_id = ?";
            PreparedStatement ps = conn.prepareStatement(selectSQL);
            
            ps.setInt(1, id);
            
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return true;
            }
            else {
                return false;
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return false;
        
    }
    
    
    public static String checkProductName(Integer id) {
        loadClass();
        Connection conn = null;
        
        try {
            conn = DriverManager.getConnection(dbUrl, user, pass);
            String selectSQL = "SELECT name FROM products WHERE product_id = ? LIMIT 1";
            PreparedStatement ps = conn.prepareStatement(selectSQL);
            
            ps.setInt(1, id);
            
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getString("name");
            }
            else {
                return null;
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return null;
        
    }
    
    
}
