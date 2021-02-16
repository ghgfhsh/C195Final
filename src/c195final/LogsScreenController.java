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
package c195final;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Jeremiah McElroy
 */
public class LogsScreenController implements Initializable {
    
    @FXML TextArea textBlock;
    DataModel.SQLDatabase sqldata = new DataModel.SQLDatabase();
    
    ResultSet rs;
    Statement stmt;

    public void backButtonPushed(ActionEvent event) throws IOException{
        //open main screen
        Parent NextScreenParent = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
        Scene NextScreenScene = new Scene(NextScreenParent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(NextScreenScene);
        window.show();
    }
    
    public void custByConPushed() throws SQLException{
        textBlock.clear();
        String getUserIds = "SELECT userId, userName FROM user";

        Map<Integer, String> idMap = new HashMap<>();
        rs = stmt.executeQuery(getUserIds);
        System.out.print("SQL Query: " + getUserIds + "\n"); //debugging
        while (rs.next()) {
            idMap.put(rs.getInt("userId"),rs.getString("userName"));
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Assigned Customers Report(Based on who last updated customer info):\n");
        
        for(Map.Entry<Integer, String> id : idMap.entrySet()){
            
            String searchAppt = "SELECT customerId, customerName FROM customer WHERE lastUpdateBy = '" +id.getValue() + "'";
            rs = stmt.executeQuery(searchAppt);
            System.out.println("SQL Query: " +searchAppt);
            rs.next();
            
            builder.append(id.getValue()).append("'s Customers:\n");

            do{
                String customerId = rs.getString("customerId");
                String customerName = rs.getString("customerName");
                builder.append("ID: ").append(customerId).append(" Name: ").append(customerName).append("\n");
            }while(rs.next());
        }
        textBlock.setText(builder.toString());
    }
    
    public void SchedbyConPushed() throws SQLException{
        textBlock.clear();
        String getUserIds = "SELECT userId, userName FROM user";

        Map<Integer, String> idMap = new HashMap<Integer, String>();
        rs = stmt.executeQuery(getUserIds);
        System.out.print("SQL Query: " + getUserIds + "\n"); //debugging
        while (rs.next()) {
            idMap.put(rs.getInt("userId"),rs.getString("userName"));
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Appointments by Consultant:\n");
        
        for(Map.Entry<Integer, String> id : idMap.entrySet()){
            
            String searchAppt = "SELECT start, end, title, customerName FROM U06Dmr.appointment, U06Dmr.customer  WHERE appointment.customerId = customer.customerId and appointment.userId = " +id.getKey()+ " ORDER BY start ";
            rs = stmt.executeQuery(searchAppt);
            System.out.println("SQL Query: " + searchAppt);
            rs.next();
            
            builder.append(id.getValue()).append("'s Appointments:\n");

            do{ // using do while because rs.next() had to be called before the loop to preserve the order of the users
                String start = rs.getString("appointment.start");
                String end = rs.getString("appointment.end");
                String title = rs.getString("appointment.title");
                String customer = rs.getString("customerName");
                builder.append(title).append(": Starts at ").append(start).append(", Ends at ").append(end).append(", Customer Name: ").append(customer).append("\n");
            }while(rs.next());
        }
        textBlock.setText(builder.toString());
    }
    
    public void byMonthButtonPushed() throws SQLException{
        textBlock.clear();
        String search = "SELECT MONTHNAME(start) as 'Month', year(start) as 'Year', COUNT(*) as 'Total' FROM appointment GROUP BY MONTH(start) order by monthname(start)";
        rs = stmt.executeQuery(search);
        StringBuilder builder = new StringBuilder();
        
        builder.append("Total Appointments by Month:\n");
        
        
        while(rs.next()){
            builder.append(rs.getString("Month")).append(", ").append(rs.getString("Year")).append(": ").append(rs.getString("Total")).append("\n");
            
        }
        textBlock.setText(builder.toString());
        
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            stmt = sqldata.getConnection().createStatement();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }    
    
}
