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

import DataModel.Customer;
import DataModel.SQLDatabase;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import DataModel.Appointment;
import java.io.IOException;
import java.util.TimeZone;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Jeremiah McElroy
 */
public class AppointmentScreenController implements Initializable {
    
    //start up sql connection
    SQLDatabase sqldata = new SQLDatabase();
    
    
    //get references to FXML sheet
    @FXML private TableView<Appointment> apptTableView;
    @FXML private TableColumn<Appointment, ZonedDateTime> startColumn;
    @FXML private TableColumn<Appointment, LocalDateTime> endColumn;
    @FXML private TableColumn<Appointment, String> titleColumn;
    @FXML private TableColumn<Appointment, String> descColumn;
    @FXML private TableColumn<Appointment, Customer> custColumn;
    @FXML private TableColumn<Appointment, String> consultColmn;
    
    @FXML private RadioButton monthRadioButton;
    @FXML private RadioButton weekRadioButton;
    private ToggleGroup radiotogglegroup;
    
    ObservableList<Appointment> appList;
    private final DateTimeFormatter dFormater = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT); //gonna have to format a lot
            
    public void newApptButtonPushed(ActionEvent event) throws IOException{
        //open new appointment screen
        Parent NextScreenParent = FXMLLoader.load(getClass().getResource("AddAppointmentScreen.fxml"));
        Scene NextScreenScene = new Scene(NextScreenParent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(NextScreenScene);
        window.show();
        
    }
    
    public void editApptButtonPushed(ActionEvent event) throws IOException{
        if(!apptTableView.getSelectionModel().isEmpty()){
            //open new appointment screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddAppointmentScreen.fxml"));
            Parent NextScreenParent = loader.load();
            Scene NextScreenScene = new Scene(NextScreenParent);

            //pass selected appointment over
            AddAppointmentScreenController controller = loader.getController();
            controller.editData(apptTableView.getSelectionModel().getSelectedItem(), true);
            

            Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
            window.setScene(NextScreenScene);
            window.show();
        }
    }
    
    public void backButtonPushed(ActionEvent event) throws IOException{
        //open main screen
        Parent NextScreenParent = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
        Scene NextScreenScene = new Scene(NextScreenParent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(NextScreenScene);
        window.show();
    }
    
    public void monthSelected(){
        //repopulate the app list with all appointments so it can be properly filtered
        try {
            populateAppList();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        LocalDate now = LocalDate.now();
        LocalDate monthaway = now.plusMonths(1);
        
        //filter
        FilteredList<Appointment> filtered = new FilteredList<>(appList);
        filtered.setPredicate(row -> {
            LocalDate rowDate = LocalDate.parse(row.getStart(), dFormater);       
            return rowDate.isAfter(now.minusDays(1)) && rowDate.isBefore(monthaway);
        });
        apptTableView.getItems().setAll(filtered);
    }
    
    public void weekSelected(){
        //repopulate the app list with all appointments so it can be properly filtered
        try {
            populateAppList();
        } catch (SQLException e) {
            System.out.print(e.getMessage() + "\n");
        }
        
        LocalDate now = LocalDate.now();
        LocalDate sevendaysaway = now.plusDays(7);
        
        //filter
        FilteredList<Appointment> filtered = new FilteredList<>(appList);
        filtered.setPredicate(row -> {
            LocalDate rowDate = LocalDate.parse(row.getStart(), dFormater);
            return rowDate.isAfter(now.minusDays(1)) && rowDate.isBefore(sevendaysaway);
        });
        apptTableView.getItems().setAll(filtered);
        
    }
    
    public void deleteApptButtonPushed(){
        Appointment selectedApp = apptTableView.getSelectionModel().getSelectedItem();
        String deletecmd = "DELETE appointment.* FROM U06Dmr.appointment WHERE appointmentId = " + selectedApp.getAppointmentId();
        
        if(selectedApp != null){
            //confirm you want to delete
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.showAndWait().ifPresent((response -> { //lambda is used here to reduce the amount of code being written
                if(response == ButtonType.OK){
                    try {
                        Statement stmt = sqldata.getConnection().createStatement();
                        stmt.executeUpdate(deletecmd);
                        System.out.print("SQL Query: " + deletecmd + "\n"); //debugging 
                        populateAppList();

                    } catch (SQLException e) {
                        System.out.print(e.getMessage() + "\n");
                    }
                }  
            }));
        }
    }

    private void populateAppList() throws SQLException{
        String searchAppt = "SELECT * FROM U06Dmr.appointment, U06Dmr.customer  WHERE appointment.customerId = customer.customerId and userId = " +C195Final.activeUserId+ " ORDER BY start";
        
        Statement stmt = sqldata.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(searchAppt);
        
        ZoneId userZone = ZoneId.of(TimeZone.getDefault().getID()); // get system time zone
        
        appList.clear();
        while(rs.next()){
            int appointmentId = rs.getInt("appointment.appointmentId");
            
            //get timestamp from SQL server then convert to UTC, then adjust to the users time. 
            Timestamp tempstart = rs.getTimestamp("appointment.start");
            ZonedDateTime start = tempstart.toLocalDateTime().toInstant(ZoneOffset.UTC).atZone(userZone);
            Timestamp tempend = rs.getTimestamp("appointment.end");
            ZonedDateTime end = tempend.toLocalDateTime().toInstant(ZoneOffset.UTC).atZone(userZone);
            
            String title = rs.getString("appointment.title");
            String desc = rs.getString("appointment.type");
            
            Customer customer = new Customer(rs.getInt("appointment.customerId"), rs.getString("customerName"));
            
            String user = rs.getString("appointment.createdBy");
            
            
            appList.add(new Appointment(appointmentId, start.format(dFormater), end.format(dFormater), title, desc, customer, user));
        }
        apptTableView.getItems().setAll(appList);
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //initalize app list
        appList = FXCollections.observableArrayList();
        
        //setup radio buttons
        radiotogglegroup = new ToggleGroup();
        this.weekRadioButton.setToggleGroup(radiotogglegroup);
        this.monthRadioButton.setToggleGroup(radiotogglegroup);
        
        //setup table
        startColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        custColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        consultColmn.setCellValueFactory(new PropertyValueFactory<>("userName"));
     
        try {
            populateAppList();
        } catch (SQLException e) {
            System.out.print(e.getMessage() + "\n");
        }
        
    }    
    
}
