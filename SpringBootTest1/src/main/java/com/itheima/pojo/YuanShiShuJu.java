package com.itheima.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;

public class YuanShiShuJu extends BaseRowModel {
    @ExcelProperty(value = "姓名", index = 0)
    private String name;
    @ExcelProperty(value = "所在乡镇", index = 1)
    private String location;
    @ExcelProperty(value = "性别", index = 2)
    private String sex;
    @ExcelProperty(value = "出生日期", index = 3)
    private String birthday;
    @ExcelProperty(value = "身份证号", index = 4)
    private String id_card;
    @ExcelProperty(value = "住址", index = 5)
    private String address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getId_card() {
        return id_card;
    }

    public void setId_card(String id_card) {
        this.id_card = id_card;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
