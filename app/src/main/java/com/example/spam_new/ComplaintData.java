package com.example.spam_new;

import java.io.Serializable;

public class ComplaintData implements Serializable {

    public String phoneNumber;
    public String scamType;
    public String description;
    public String amountLost;

    public ComplaintData(String phoneNumber, String scamType,
                         String description, String amountLost) {
        this.phoneNumber = phoneNumber;
        this.scamType = scamType;
        this.description = description;
        this.amountLost = amountLost;
    }
}
