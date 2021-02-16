/*
 * Copyright (C) 2020 Jeremiah McElroy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package DataModel;

import java.sql.*;

/**
 *
 * @author Jeremiah McElroy
 */
public class SQLDatabase {
    private final String URL = "jdbc:mysql://3.227.166.251/U06Dmr";
    private final String Username = "U06Dmr";
    private final String Pass = "53688731885";
    private static Connection conn;
    
    public void connect(){
        try{
        conn = DriverManager.getConnection(URL, Username, Pass);
        System.out.print("SQL Connection: " + conn + "\n");
        }catch(Exception e){
           System.out.print(e.getMessage() + "\n");
        }
    }
    
    public void disconnect(){
        try{
            conn.close();
            System.out.printf("Closing SQL Database\n");
        }catch(SQLException e){
            System.out.printf(e.getMessage() + "\n");
        }
    }
    
    public Connection getConnection(){
        return conn;
    }
    
}
