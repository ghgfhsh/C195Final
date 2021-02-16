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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
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
public class CustomerScreenController implements Initializable {
    
    //get needed references for table
    @FXML
    private TableView<DataModel.Customer> customerTable;
    @FXML
    private TableColumn<DataModel.Customer, String> customerNameColumn;
    @FXML
    private TableColumn<DataModel.Customer, Integer> customerIdColumn;   
    ObservableList<DataModel.Customer> custList;
    
    //get customer info references
    @FXML TextField nameField;
    @FXML TextField addressField;
    @FXML TextField address2Field;
    @FXML ComboBox cityComboBox;
    @FXML TextField countryField;
    @FXML TextField postalCodeField;
    @FXML TextField phoneField;
    @FXML Label errorMessageLabel;
    
    
    //get sql needed
    DataModel.SQLDatabase sqldata = new DataModel.SQLDatabase();
    Statement stmt;
    ResultSet rs;

    public void populateCustomerList() throws SQLException {
        customerTable.getItems().clear();
        custList.clear();
        String search = "SELECT * FROM customer";

        rs = stmt.executeQuery(search);
        System.out.print("SQL Query: " + search + "\n"); //debugging 

        while (rs.next()) {
            int customerId = rs.getInt("customerId");
            String customerName = rs.getString("customerName");

            custList.add(new DataModel.Customer(customerId, customerName));
        }
        customerTable.getItems().setAll(custList);

    }
    
    public void populateCityChoices() throws SQLException{
        ObservableList<String> cityList = FXCollections.observableArrayList();
        String search = "SELECT city FROM city";
        rs = stmt.executeQuery(search);
        while(rs.next()){
            cityList.add(rs.getString("city"));
        }
        cityComboBox.getItems().setAll(cityList);
        
    }
    
    public void modButtonPushed(){
        errorMessageLabel.setText(""); // clears errors
        
        if(!customerTable.getSelectionModel().isEmpty()){ 
            try {
                // verifies that a selection has been made
                //get selected customer id
                int selectedCustomerId = customerTable.getSelectionModel().getSelectedItem().getCustomerId();

                //get city id from selection
                String searchCityId = "Select cityId from city where city = '" +cityComboBox.getSelectionModel().getSelectedItem().toString()+ "'";
                rs = stmt.executeQuery(searchCityId);
                System.out.println("SQL Query: " + searchCityId); //debugging
                rs.next();
                int cityId = rs.getInt("cityId");

                //get address id from selection
                String searchAddressId = "Select addressId from customer where customerId = " + selectedCustomerId;
                rs = stmt.executeQuery(searchAddressId);
                System.out.println("SQL Query: " + searchAddressId); //debugging
                rs.next();
                int addressId = rs.getInt("addressId");

                //get the string based fields
                String name = nameField.getText();
                String address = addressField.getText();
                String address2 = address2Field.getText();
                String postal = postalCodeField.getText();
                String phone = phoneField.getText();
                
                String getUserName = "SELECT userName FROM user where userId = " +C195Final.activeUserId;
                rs = stmt.executeQuery(getUserName);
                System.out.println("SQL Query: "+getUserName);
                rs.next();
                String userName = rs.getString("userName");

                if(validateEntries()){ //makes sure that user entries are valid before savings
                    //update customer name
                    String updateCustomer = "update customer set customerName = '" +name+ "', lastUpdateBy = '" +userName+ "' where customerId = " + selectedCustomerId;
                    stmt.execute(updateCustomer);
                    System.out.println("SQL Query: " + updateCustomer);

                    //update address
                    String updateAddress = "UPDATE address SET address = '" +address+ "',address2 = '" +address2+ "',postalCode = '" +postal+ "',phone = '" +phone+ "',cityId = '" +cityId+ "' WHERE addressId = " +addressId;
                    stmt.execute(updateAddress);
                    System.out.println("SQL Query: " + updateAddress);
                    populateCustomerList();
                } 
            } catch (Exception ex) {
                errorMessageLabel.setText("Invalid Entry!"); //this will catch general misentry errors not explicitly checked mainly ones that mess up the SQL query
            }
            
        } else{
            errorMessageLabel.setText("No Selection Made");
        }
    }
    
    public void addButtonPushed(){
        try {
            errorMessageLabel.setText(""); // clears errors
            
            //get city id from selection
            String searchCityId = "Select cityId, countryId from city where city = '" +cityComboBox.getSelectionModel().getSelectedItem().toString()+ "'";
            rs = stmt.executeQuery(searchCityId);
            System.out.println("SQL Query: " + searchCityId); //debugging
            rs.next();
            int cityId = rs.getInt("cityId");
            int countryId = rs.getInt("countryId");
            
            int nextAddressId = findFirstAvailableAddressId();
            int nextCustomerId = findFirstAvailableCustomerId();
            
            //get the string based fields
            String name = nameField.getText();
            String address = addressField.getText();
            String address2 = address2Field.getText();
            String postal = postalCodeField.getText();
            String phone = phoneField.getText();
            
            String getUserName = "SELECT userName FROM user where userId = " +C195Final.activeUserId;
            rs = stmt.executeQuery(getUserName);
            System.out.println("SQL Query: "+getUserName);
            rs.next();
            String userName = rs.getString("userName");
            
            if(validateEntries()){ //makes sure that user entries are valid before savings
                //add address
                String addAddress = "INSERT INTO address VALUES (" +nextAddressId+ ", '" +address+ "', '" +address2+ "', '" +cityId+ "', '" +postal+
                        "', '" +phone+ "', '2019-01-01 00:00:00', '" +C195Final.activeUserId+ "', '2020-02-01 22:08:33', 'test')";
                stmt.execute(addAddress);
                System.out.println("SQL Query: " + addAddress);
                
                //add customer
                String addCustomer = "INSERT INTO customer VALUES (" +nextCustomerId+ ", '" +name+ "', " +nextAddressId+ ", '1', '2019-01-01 00:00:00', '" +userName+ "', '2019-01-01 00:00:00', '" +userName+ "')";
                stmt.execute(addCustomer);
                populateCustomerList();
            } 
        } catch (Exception ex) {
            errorMessageLabel.setText("Invalid Entry!"); //this will catch general misentry errors not explicitly checked mainly ones that mess up the SQL query
            System.out.println(ex.getMessage());
        }
            
    }
    
    public boolean validateEntries(){
        if(!(phoneField.getText().length() == 10 && phoneField.getText().matches("[0-9]+"))){
            errorMessageLabel.setText("Phone number must 9 digits no symbols or spaces");
            return false;
        }
        else if(postalCodeField.getText().length() != 5){
            errorMessageLabel.setText("Invalid Zip Code");
            return false;
        }
        else if(addressField.getText().isEmpty()){
            errorMessageLabel.setText("Address line 1 is required");
            return false;
        }
        else if(!(nameField.getText().matches("[A-Za-z0-9 ]+"))){
            errorMessageLabel.setText("Invalid Name");
            return false;
        }
        else{
            return true;
        }
    }
    
    public int findFirstAvailableCustomerId() throws SQLException{
        try{
            int nextId = 1;
            String getCustomerIds = "SELECT customerId FROM customer ORDER BY customerId";

            ObservableList<Integer> idList = FXCollections.observableArrayList();
            rs = stmt.executeQuery(getCustomerIds);
            System.out.println("SQL Query: " + getCustomerIds); //debugging
            while (rs.next()) {
                idList.add(rs.getInt("customerId"));
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
            System.out.print("Next Address Id: " + nextId + "\n");
            return nextId;
        } catch(ArrayIndexOutOfBoundsException e){
            //this will only be thrown if there are no addresses, so we return an Id of 1
            return 1;
        }
    }
    
    public int findFirstAvailableAddressId() throws SQLException{
        try{
            int nextId = 1;
            String getAddressIds = "SELECT addressId FROM address ORDER BY addressId";

            ObservableList<Integer> idList = FXCollections.observableArrayList();
            rs = stmt.executeQuery(getAddressIds);
            System.out.println("SQL Query: " + getAddressIds); //debugging
            while (rs.next()) {
                idList.add(rs.getInt("addressId"));
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
            System.out.print("Next Address Id: " + nextId + "\n");
            return nextId;
        } catch(ArrayIndexOutOfBoundsException e){
            //this will only be thrown if there are no addresses, so we return an Id of 1
            return 1;
        }
    }
    
    public void deleteButtonPushed() throws SQLException{
        errorMessageLabel.setText(""); // clears errors
        if(!customerTable.getSelectionModel().isEmpty()){ // verifies that a selection has been made
            //finds address id associated with customer
            String searchAddressId = "Select addressId from customer WHERE customerId = " + customerTable.getSelectionModel().getSelectedItem().getCustomerId();
            String deleteQuery = "DELETE customer.* FROM customer WHERE customerId = " + customerTable.getSelectionModel().getSelectedItem().getCustomerId();
            String deleteAddressEntry;
            int addressId;
            
            rs = stmt.executeQuery(searchAddressId);
            rs.next();
            addressId = rs.getInt("addressId");
            deleteAddressEntry = "DELETE address.* FROM address WHERE addressId = " + addressId;

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.showAndWait().ifPresent(result -> {
                if(result == ButtonType.OK){
                    try {
                    //deletes customer
                    stmt.execute(deleteQuery);
                    System.out.println("SQL Query: " + deleteQuery);
                    
                    //deletes the associated address with customer
                    
                    stmt.execute(deleteAddressEntry);  
                    System.out.println("SQL Query: " + deleteAddressEntry);
                    } catch (SQLException ex) {
                        Logger.getLogger(CustomerScreenController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            
            populateCustomerList();
        }   else{
            errorMessageLabel.setText("No Selection Made");
        }
    }
    
    public void updateCityChoice(){
        String searchCityId = "SELECT cityId FROM city WHERE city = '" +cityComboBox.getSelectionModel().getSelectedItem().toString()+ "'";
        
        try {
            rs = stmt.executeQuery(searchCityId);
            System.out.println("SQL Query: " + searchCityId);
            rs.next();
            int cityId = rs.getInt("cityId");
            
            String searchCountryName = "Select country.country from country, city where city.cityId = " +cityId+ " and city.countryId = country.countryId";
            rs = stmt.executeQuery(searchCountryName);
            System.out.println("SQL Query: " + searchCountryName);
            rs.next();
            
            countryField.setText(rs.getString("country.country"));
            
            
        } catch (SQLException ex) {
            Logger.getLogger(CustomerScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public void populateCustomerInfo() throws SQLException{
        DataModel.Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        
        //get customer addressId
        String search = "select customerName, address, address2, city, country, postalCode"+
                ", phone  from customer, address, city, country WHERE customer.addressId = address.addressId"+
                " and address.cityId = city.cityId and country.countryId = city.countryId and customerId = " + selectedCustomer.getCustomerId();
        rs = stmt.executeQuery(search);
        System.out.print("SQL Query: " + search + "\n"); //debugging
        rs.next();
        
        
        nameField.setText(selectedCustomer.getCustomerName());
        addressField.setText(rs.getString("address"));
        address2Field.setText(rs.getString("address2"));
        postalCodeField.setText(rs.getString("postalCode"));
        phoneField.setText(rs.getString("phone"));
        cityComboBox.getSelectionModel().select(rs.getString("city"));
    }
    
    public void backButtonPushed(ActionEvent event) throws IOException{
        //open main screen
        Parent NextScreenParent = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
        Scene NextScreenScene = new Scene(NextScreenParent);
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(NextScreenScene);
        window.show();
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
        
        //intialize table columns
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        //intialize table columns
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        
        custList = FXCollections.observableArrayList();
        
        try {
            populateCustomerList();
            populateCityChoices();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        //set default values
        cityComboBox.getSelectionModel().selectFirst();
        countryField.setText("US");
        
    }    
    
}
