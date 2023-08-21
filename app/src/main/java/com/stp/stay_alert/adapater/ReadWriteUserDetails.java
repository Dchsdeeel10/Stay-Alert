package com.stp.stay_alert.adapater;

public class ReadWriteUserDetails {
    public  String password, contact, address;


    public ReadWriteUserDetails(){};

    public ReadWriteUserDetails(String txtpass, String txtcon, String txtadd){
        this.password = txtpass;
        this.contact = txtcon;
        this.address = txtadd;
    }
}
