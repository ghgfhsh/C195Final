/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195final;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.sql.*;
import java.time.ZonedDateTime;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
/**
 * FXML Controller class
 *
 * @author Jeremiah McElroy
 */




public class LoginScreenController implements Initializable {

    //get reference to input fields
    @FXML TextField usrNameField;
    @FXML TextField passField;
    @FXML Label errorMessage;
    @FXML ChoiceBox<String> languageSelector;
    
    //get refrences for language switching
    @FXML Label loginLabel;
    @FXML Label usrNameLabel;
    @FXML Label passLabel;
    @FXML Button loginButton;
    @FXML Button exitButton;
    
    private Locale locale = Locale.ENGLISH;
    
    public void exitButtonPushed(ActionEvent event){
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.close();
    }
    
    private void logUserActivity(String activeUser) throws IOException{
        String filePath = "User Activity Log.txt";
        File file = new File(filePath);
        //if file doesn't exsist make it and write the header to it
        if(file.createNewFile()){
            System.out.println("New user activity log file created at: " +filePath);
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write("User Login Activity:");
            writer.close();
        }
        
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath,true));
        writer.append("\n[" + ZonedDateTime.now().toInstant() + "] Login by " +activeUser);
        writer.close();
        
        System.out.println("UserLogin: " +activeUser);
    }
    
    //login button pressed logic
    public void loginButtonPushed(ActionEvent event) throws IOException{
        DataModel.SQLDatabase database = new DataModel.SQLDatabase();
        database.connect();
        
        try{
            //grab information from the sql database
            String lookupUser = "SELECT userId, userName FROM U06Dmr.user WHERE userName = \"" + usrNameField.getText() + "\" and password = \"" + passField.getText() + "\"";
            Statement stmt =  database.getConnection().createStatement();
            System.out.print("SQL Query: " + lookupUser + "\n"); //debugging 
            ResultSet rs = stmt.executeQuery(lookupUser);
            
            
            //check whether was a result for the username password combination. There should be no duplicates, so checking for a result is 
            if(rs.next()){
                C195Final.activeUserId = rs.getInt("userId"); //set active user
                
                logUserActivity(rs.getString("userName"));
                
                //open the main window
                Parent NextScreenParent = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
                Scene NextScreenScene = new Scene(NextScreenParent);
                Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
                window.setScene(NextScreenScene);
                window.show();
                
            }else{
                ResourceBundle rb = ResourceBundle.getBundle("Languages/login", locale);
                errorMessage.setText(rb.getString("errormessage"));
            }
            
        }catch(SQLException e){
            System.out.printf(e.getMessage());
        }
            
    }
    
    public void languageUpdate(){
        //check language and set locale accordingly
        
        
        if(languageSelector.getSelectionModel().getSelectedItem().equals("English")){
            locale = Locale.ENGLISH;
        }
        else if(languageSelector.getSelectionModel().getSelectedItem().equals("Esperanto")){
            locale = new Locale.Builder().setLanguageTag("eo").build();
        }
        
        ResourceBundle rb = ResourceBundle.getBundle("Languages/login", locale);
        
        //setlables based on locale
        usrNameLabel.setText(rb.getString("username"));
        passLabel.setText(rb.getString("password"));
        loginLabel.setText(rb.getString("login"));
        loginButton.setText(rb.getString("login"));
        exitButton.setText(rb.getString("exit"));
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //setup dropdown
        languageSelector.setItems(FXCollections.observableArrayList("English", "Esperanto"));
        
        //setlanguage based off location
        if(Locale.getDefault().getLanguage().equals("eo")){
            languageSelector.setValue("Esperanto");
        }
        else {
            languageSelector.setValue("English");
        }
        languageUpdate();
        
        
        //updates language when choice is made. (add listener). Lambda is used here to make this much easier to read and interpret than overriding the changed method
        languageSelector.valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            languageUpdate();
        });
        
        
        
    }    
    
}
