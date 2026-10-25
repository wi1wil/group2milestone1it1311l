/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uap.asia.model;

import java.sql.*;
import java.util.ResourceBundle;

/**
 *
 * @author Andrei
 */
public class iosqlclass {    
    private String driver;
    private String url;
    private String user;
    private String pass;
    
    
    private boolean loadClass() {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    
    public void loadSQL(){
        ResourceBundle sqlrb = ResourceBundle.getBundle("asia.uap.model.group2sql");
        this.url = sqlrb.getString("url");
        this.user = sqlrb.getString("user");
        this.pass = sqlrb.getString("password");
        this.driver = sqlrb.getString("driver");
    }

    public iosqlclass(String driver, String url, String user, String pass) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public iosqlclass() {
        loadSQL();
    }
    
    /*
    private boolean exists(OUAPStudentBean row) throws SQLException
    {
        if(getStudentBySID(row.getStudentNumber()) != null)
        {
            return true;
        }
        return false;
    }
    */
    
    
    // method for inserts
    public int addstuff() {
        int rows = 0; // rows for later, needs exists method
        /*
        if(exists(row))
        {
            return -1;
        }        
        */
                
        loadClass();
        Connection conn = null;
        
        try {
            conn = DriverManager.getConnection(url, user, pass);
            String insertSQL = "insert statement blablabla"; // create your own insert statement
            PreparedStatement ps = conn.prepareStatement(insertSQL);
            
            /* setString seetInt if needed
            ps.SetString(1, row.getBlablabla());
            */
            
            rows = ps.executeUpdate();
            conn.commit();
                        
        } catch (SQLException ex) {
            // if(conn != null) conn.rollback();
            // throw ex; //rethrow so caller can handle the error
            ex.printStackTrace();
                       
        } finally {
            if(conn != null)
                try { 
                    conn.close();
                } catch (SQLException ex) {
                        ex.printStackTrace();
                }
        }

        return rows;
                
    }
    
    public void getStuff(String whatevoutput) throws SQLException { // void should be the object
        
                
        loadClass();
        Connection conn = null;
        
        // WhateverObjectThisIs myobj = null;
        
        try {
            conn = DriverManager.getConnection(url, user, pass);
            String selectSQL = "SELECT * FROM statement blablabla"; // create your own select statement
            PreparedStatement ps = conn.prepareStatement(selectSQL);
                        
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                // some code here where to save the stuff from sql db
            }
            
            ps.close();
                        
        } catch (SQLException ex) {
            ex.printStackTrace();
                       
        } finally {
            if(conn != null)
                try { 
                    conn.close();
                } catch (SQLException ex) {
                        ex.printStackTrace();
                }
        }
        
        // add update stuff here ifever
        
    }
    
    
    
    
    
}
