package com.HomyStayWeb.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 关键修复
public class Vip {
    private int id;
    private String name;
    private String sex;
    private String card;
    private String phone;
    private String v_type;
    private String startTime;
    private String endTime;
    private LocalDateTime creattime;
    private LocalDateTime updatetime;

    public Vip(String name, String sex, String card, String phone, String v_type, String startTime, String endTime) {
        this.name = name;
        this.sex = sex;
        this.card = card;
        this.phone = phone;
        this.v_type = v_type;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}
