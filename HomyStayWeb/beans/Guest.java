package com.HomyStayWeb.beans;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Guest {
    private int id;
    private String name;
    private String sex;
    private String card;
    private String phone;
    private String enterTime;
    private String exitTime;
    private String h_Type;
    private int num;

    public Guest(String name, String sex, String card, String phone, String enterTime, String exitTime, String h_Type, int num) {
        this.name = name;
        this.sex = sex;
        this.card = card;
        this.phone = phone;
        this.enterTime = enterTime;
        this.exitTime = exitTime;
        this.h_Type = h_Type;
        this.num = num;
    }
}
