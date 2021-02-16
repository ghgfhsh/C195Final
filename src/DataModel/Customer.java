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

/**
 *
 * @author Jeremiah McElroy
 */
public class Customer {
    private int customerId;
    String customerName;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String phone;
    
    
    public Customer(int customerId, String customerName, String address, String city, String country, String postalCode, String phone){
        this.customerId = customerId;
        this.customerName = customerName;
        this.address = address;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
        this.phone = phone;
    }
    
    //overloading constructor to make it simpiler to display customer in appointments
    public Customer(int customerId, String customerName){
        this.customerId = customerId;
        this.customerName = customerName;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Getter and Setter Methods">
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    //</editor-fold>
    
    //this allows the table to show the customer name when propertyvaluefactory proccesses it
    @Override
    public String toString(){
        return customerName;
    }
    
}
