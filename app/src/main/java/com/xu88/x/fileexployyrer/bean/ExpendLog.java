package com.xu88.x.fileexployyrer.bean;

import java.io.Serializable;

public class ExpendLog implements Serializable {

    private Integer id;
    private Double amount0;
    private Integer year;
    private Integer month;
    private Integer day;
    private String time;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getAmount0() {
        return amount0;
    }

    public void setAmount0(Double amount0) {
        this.amount0 = amount0;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "ExpendLog{" +
                "id=" + id +
                ", amount0=" + amount0 +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", time='" + time + '\'' +
                '}';
    }
}
