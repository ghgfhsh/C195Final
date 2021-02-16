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

import DataModel.SQLDatabase;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.*;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Jeremiah McElroy
 */
public class MainScreenController implements Initializable{
    
    SQLDatabase sqldata = new SQLDatabase();
    private static boolean isFirstLogin = true;
    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    
    public void appButtonPushed(ActionEvent event) throws IOException{
            //open the appointment screen
            Parent NextScreenParent = FXMLLoader.load(getClass().getResource("AppointmentScreen.fxml"));
            Scene NextScreenScene = new Scene(NextScreenParent);
            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(NextScreenScene);
            window.show();
    }
    
    public void exitButtonPushed(ActionEvent event){
        DataModel.SQLDatabase tempsql = new DataModel.SQLDatabase();
        tempsql.disconnect();
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.close();
    }
    
    public void custButtonPushed(ActionEvent event) throws IOException{
        //open the appointment screen
        Parent NextScreenParent = FXMLLoader.load(getClass().getResource("CustomerScreen.fxml"));
        Scene NextScreenScene = new Scene(NextScreenParent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(NextScreenScene);
        window.show();
    }
    
    public void generateLogsPushed(ActionEvent event) throws IOException{
        //open the logs screen
        Parent NextScreenParent = FXMLLoader.load(getClass().getResource("LogsScreen.fxml"));
        Scene NextScreenScene = new Scene(NextScreenParent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(NextScreenScene);
        window.show();
    }
    
    private void checkForSoonAppointment(){
        try {
            ZoneId userZone = ZoneId.of(TimeZone.getDefault().getID());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nowPlus15 = now.plusMinutes(15);
            
            String searchAppt = "SELECT start FROM U06Dmr.appointment, U06Dmr.customer  WHERE appointment.customerId = customer.customerId and userId = " +C195Final.activeUserId+ " ORDER BY start";
            Statement stmt = sqldata.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(searchAppt);
            
            while(rs.next()){
                Timestamp tempstart = rs.getTimestamp("appointment.start");
                LocalDateTime start = tempstart.toLocalDateTime().toInstant(ZoneOffset.UTC).atZone(userZone).toLocalDateTime(); //converts the appointment start to a local date time. 
                
                if(start.isAfter(now) && start.isBefore(nowPlus15)){
                    Alert alert = new Alert(AlertType.INFORMATION, "Appointment in 15 minutes", ButtonType.OK);
                    alert.showAndWait();
                }
            }
            
            isFirstLogin = false;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        
        
    }
    
    public void openLogButtonPressed() throws IOException{
        String filePath = "User Activity Log.txt";
        
        Desktop.getDesktop().open(new File("User Activity Log.txt"));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        if(isFirstLogin) checkForSoonAppointment();
    }
}
