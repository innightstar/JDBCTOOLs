package com.HomyStayWeb.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Home {
    private int id;
    private int num;
    private String h_Type;
    private String price;
    private String state;
    private String text;
    private String file;

    public Home(int num, String price, String h_Type, String state, String text, String file) {
        this.num = num;
        this.price = price;
        this.h_Type = h_Type;
        this.state = state;
        this.text = text;
        this.file = file;
    }
}
