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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Jeremiah McElroy
 */
public class AddAppointmentScreenController implements Initializable {

    //get user entry references
    @FXML
    private TextField titleField;
    @FXML
    private ComboBox typeComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField startField;
    @FXML
    private ComboBox startComboBox;
    @FXML
    private TextField endField;
    @FXML
    private ComboBox endComboBox;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private TextField custSearchField;
    
    @FXML
    private Label titleMessage;

    //reference to buttons
    @FXML
    private Button cancelButton;

    //get table refrences
    @FXML
    private TableView<DataModel.Customer> customerSelectTableView;
    @FXML
    private TableColumn<DataModel.Customer, String> customerNameColumn;
    ObservableList<DataModel.Customer> custList;

    //get create instance of SQLDatabase to access static variable conn
    DataModel.SQLDatabase sqldata = new DataModel.SQLDatabase();

    //dateformatters
    private final SimpleDateFormat tFormatter = new SimpleDateFormat("hh:mm a");
    private final DateTimeFormatter sqltFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //statement and result set that can be reused
    Statement stmt;
    ResultSet rs;
    
    //gets user timezone
    private final ZoneId userZone = ZoneId.of(TimeZone.getDefault().getID()); 
    
    //returns whether an edit or a new appointment is being make
    boolean isEdit = false;
    int editAppId;
    
    private boolean validateAppointment() {
        if (titleField.getText().isEmpty()) {
            errorMessageLabel.setText("Title Cannot Be Empty");
            return false;
        } else if (typeComboBox.getSelectionModel().isEmpty()) {
            errorMessageLabel.setText("Select Appointment Type");
            return false;
        } else if (datePicker.getValue() == null) {
            errorMessageLabel.setText("Select Date");
            return false;
        } else if (customerSelectTableView.getSelectionModel().isEmpty()) {
            errorMessageLabel.setText("Select Customer");
            return false;
        } else if (!titleField.getText().matches("[A-Za-z0-9 ]+")){
            errorMessageLabel.setText("Title Must be Alphanumeric");
            return false;
        }
        return true;
    }
    
    private boolean validateTime(LocalDateTime start, LocalDateTime end) {
        LocalDateTime time = start;
        if (end.isBefore(start)) {
                errorMessageLabel.setText("End cannot be before start time");
                return false;
            }
        
        //run twice
        for (int i = 0; i < 2; i++) {
            LocalTime openTime = LocalTime.of(7, 59);
            LocalTime closeTime = LocalTime.of(20, 59);
            LocalTime enteredTime = time.toLocalTime();

            //checks if time is within buisness hours
            if (!(enteredTime.isAfter(openTime) && enteredTime.isBefore(closeTime))) {
                errorMessageLabel.setText("Time Must Be Within Buisness Hours");
                return false;
            }

            

            String search = "SELECT start, end FROM appointment WHERE userID = " + C195Final.activeUserId;

            //if editing appointment change search string to exclude the appointment being edited
            if (isEdit) {
                search = "SELECT start, end FROM appointment WHERE userID = " + C195Final.activeUserId + " and not appointmentId = " + editAppId;
            }

            try {
                rs = stmt.executeQuery(search);
                System.out.println("SQL Query: " + search);
                while (rs.next()) {
                    //convert the time to the current timezone and back into a LocalDateTime so it can be evualuated. 
                    LocalDateTime sStart = rs.getTimestamp("start").toLocalDateTime().toInstant(ZoneOffset.UTC).atZone(userZone).toLocalDateTime();
                    LocalDateTime sEnd = rs.getTimestamp("end").toLocalDateTime().toInstant(ZoneOffset.UTC).atZone(userZone).toLocalDateTime();
                    System.out.println("Start ==" + sStart);
                    System.out.println("End ==" + sEnd);
                    System.out.println("Time == " + time);

                    if (sEnd.isBefore(sStart)) {
                        errorMessageLabel.setText("End time cannot be before start time");
                        return false;
                    }

                    if (start.isBefore(sEnd) && sStart.isBefore(end)) {
                        errorMessageLabel.setText("Overlapping Appointment!");
                        return false;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(AddAppointmentScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
            time = end;
        }
        return true;
    }

    public void saveButtonPressed() throws SQLException {
        if (validateAppointment()) {
            //SQL Statements
            stmt = sqldata.getConnection().createStatement();

            //get all user entries
            String title = titleField.getText();
            String type = typeComboBox.getValue().toString();
            DataModel.Customer customerSelection = customerSelectTableView.getSelectionModel().getSelectedItem();
            LocalDateTime startDate = datePicker.getValue().atStartOfDay(); //value you will save
            LocalDateTime endDate = startDate; //value you will save
            
            //Add AM/PM marker to time
            String rawstartTime = startField.getText();
            String rawstartAMPM = startComboBox.getSelectionModel().getSelectedItem().toString();
            String addedstartTime = rawstartTime + " " + rawstartAMPM;
            String rawendTime = endField.getText();
            String rawendAMPM = endComboBox.getSelectionModel().getSelectedItem().toString();
            String addedendTime = rawendTime + " " + rawendAMPM;

            
            try {
                //convert timesting to time, then to SQL format so it can be saved. In try block to prevent SQL save upon specific exceptions
                java.util.Date starttime;
                java.util.Date endTime;
                
                starttime = tFormatter.parse(addedstartTime);
                startDate = startDate.plusHours(starttime.getHours());
                startDate = startDate.plusMinutes(starttime.getMinutes());
                System.out.print("Start Time: " + startDate + "\n");

                endTime = tFormatter.parse(addedendTime);
                endDate = endDate.plusHours(endTime.getHours());
                endDate = endDate.plusMinutes(endTime.getMinutes());
                System.out.print("End Time: " + endDate + "\n");
                
                //convert local time to UTC
                ZonedDateTime startDateZoned = startDate.atZone(userZone).withZoneSameInstant(ZoneOffset.UTC);
                ZonedDateTime endDateZoned = endDate.atZone(userZone).withZoneSameInstant(ZoneOffset.UTC);
                System.out.print("Start ZonedTime: " + startDateZoned + "\n");
                System.out.print("End ZonedTime: " + endDateZoned + "\n");
                
                //only get next Id if this is not an edit. Otherwise it likes to throw errors. 
                int nextId = 0;
                if(!isEdit) nextId = findFirstAvailableId();
                
                //makes sure that the enterred date is within buisness hours and doesn't overlap with another appointment. 
                if(validateTime(startDate, endDate)){
                    System.out.print("Time Validated!\n");
                    //gets the current users info for createdBy entry
                    rs = stmt.executeQuery("Select * FROM user WHERE userId = " + C195Final.activeUserId);
                    rs.next();
                    System.out.print("SQL Query: Select * FROM user WHERE userId = " + C195Final.activeUserId + "\n"); //debugging

                    //checks whether save is an edit or new appointment and changes the SQL query string being used accordingly
                    String saveData;
                    if(isEdit){
                        saveData = "UPDATE appointment SET customerId = " +customerSelection.getCustomerId()+ ",userId = " +C195Final.activeUserId+ 
                                ",title = '" +title+ "',type = '" +type+ "',start = '" +startDateZoned.format(sqltFormatter)+ "',end = '" +endDateZoned.format(sqltFormatter)+ "', lastUpdate = '" +LocalDateTime.now().format(sqltFormatter)+ 
                                "', lastUpdateBy = '" +rs.getString("userName")+ "' WHERE appointmentId = " +editAppId;
                    }else{
                        
                        saveData = "INSERT INTO appointment VALUES (" + nextId + "," + customerSelection.getCustomerId()
                                + "," + C195Final.activeUserId + ",'" + title + "','not needed','not needed','not needed','" + type
                                + "','not needed','" + startDateZoned.format(sqltFormatter) + "','" + endDateZoned.format(sqltFormatter) + "','" + LocalDateTime.now().format(sqltFormatter)
                                + "','" + rs.getString("userName") + "','" + LocalDateTime.now().format(sqltFormatter) + "','" + rs.getString("userName") + "')";
                    }
                    //save Appointment to SQL Database
                    System.out.print("SQL Query: " + saveData + "\n"); //debugging
                    stmt.execute(saveData); 
                    cancelButton.fire();
                }
            } catch (ParseException e) {
                errorMessageLabel.setText("Invalid Time Entry\n");
            }
        }     
    }
    
    //get all appointment ids so the next available can be found
    private int findFirstAvailableId() throws SQLException {
        try{
            int nextId = 1;
            String getAppointmentIds = "SELECT appointmentId FROM appointment ORDER BY appointmentId";

            ObservableList<Integer> idList = FXCollections.observableArrayList();
            rs = stmt.executeQuery(getAppointmentIds);
            System.out.print("SQL Query: " + getAppointmentIds + "\n"); //debugging
            while (rs.next()) {
                idList.add(rs.getInt("appointmentId"));
            }
            for (Integer num : idList) {
                if (nextId != num) {
                    break;
                }
                nextId++;
            }
            if (nextId == idList.get(idList.size() - 1)) {
                nextId = idList.get(idList.size());
            }
            System.out.print("Next Appointment Id: " + nextId + "\n");
            return nextId;
        } catch(ArrayIndexOutOfBoundsException e){
            //this will only be thrown if there are no available appointments, so we return an Id of 1
            return 1;
        }
    }
    
    public void searchFieldUpdated(){
        System.out.print("updating");
        //filter
        FilteredList<DataModel.Customer> filtered = new FilteredList<>(custList);
        filtered.setPredicate(row -> {
            return row.getCustomerName().contains(custSearchField.getText());
        });
        customerSelectTableView.getItems().setAll(filtered);
       
    }

    //populates customer table with list
    private void populateCustomerList() throws SQLException {
        String search = "SELECT * FROM customer";

        rs = stmt.executeQuery(search);
        System.out.print("SQL Query: " + search + "\n"); //debugging 

        while (rs.next()) {
            int customerId = rs.getInt("customerId");
            String customerName = rs.getString("customerName");

            custList.add(new DataModel.Customer(customerId, customerName));
        }
        customerSelectTableView.getItems().setAll(custList);

    }

    private void populateType() {
        ObservableList<String> typeList = FXCollections.observableArrayList();
        typeList.addAll("Consultation", "Open Account", "Close Account");
        typeComboBox.getItems().setAll(typeList);
    }

    private void populateTimeCombos() {
        ObservableList<String> typeList = FXCollections.observableArrayList();
        typeList.addAll("AM", "PM");
        startComboBox.getItems().setAll(typeList);
        endComboBox.getItems().setAll(typeList);

        startComboBox.getSelectionModel().selectFirst();
        endComboBox.getSelectionModel().selectFirst();
    }

    public void cancelButtonPushed(ActionEvent event) throws IOException {
        //goes back to appointment screen
        Parent NextScreenParent = FXMLLoader.load(getClass().getResource("AppointmentScreen.fxml"));
        Scene NextScreenScene = new Scene(NextScreenParent);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(NextScreenScene);
        window.show();
    }
    
    //sets data pulled over if edit data was pressed
    public void editData(DataModel.Appointment selectedApp, boolean isEdit){
        if(isEdit){
            titleMessage.setText("Edit Appointment");
        }
        
        
        this.isEdit = isEdit;
        editAppId = selectedApp.getAppointmentId();
        titleField.setText(selectedApp.getTitle());
        typeComboBox.getSelectionModel().select(selectedApp.getDescription());
        
        //format time and set it to the entry fields
        DateTimeFormatter getformatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        DateTimeFormatter printtimeFormatter = DateTimeFormatter.ofPattern("hh:mm");
        DateTimeFormatter printampmFormatter = DateTimeFormatter.ofPattern("a");
        
        LocalDateTime start = LocalDateTime.parse(selectedApp.getStart(), getformatter);
        LocalDateTime end = LocalDateTime.parse(selectedApp.getEnd(), getformatter);
        
        datePicker.setValue(start.toLocalDate());
        
        startField.setText(start.format(printtimeFormatter));
        startComboBox.getSelectionModel().select(start.format(printampmFormatter));
        
        endField.setText(end.format(printtimeFormatter));
        endComboBox.getSelectionModel().select(end.format(printampmFormatter));
        
        //selects the correct customer
        for(DataModel.Customer c : customerSelectTableView.getItems()){
            if(c.getCustomerId() == selectedApp.getCustomer().getCustomerId()){
                customerSelectTableView.getSelectionModel().select(c);
                break;
            }
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        custList = FXCollections.observableArrayList();
        //intialize table columns
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        //initalize ComboBoxes
        populateType();
        populateTimeCombos();

        try {
            stmt = sqldata.getConnection().createStatement();
            populateCustomerList();
        } catch (SQLException e) {
            System.out.print(e.getMessage() + "\n");
        }
    }

}
