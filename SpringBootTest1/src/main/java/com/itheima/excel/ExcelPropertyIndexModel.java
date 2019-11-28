package com.itheima.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;

public class ExcelPropertyIndexModel extends BaseRowModel {
    @ExcelProperty(value = "日期", index = 0)
    private String date;
    @ExcelProperty(value = "姓名", index = 1)
    private String name;
    @ExcelProperty(value = "共收款", index = 2)
    private String sk;
    @ExcelProperty(value = "进货价", index = 3)
    private String jhj;
    @ExcelProperty(value = "进货方", index = 4)
    private String jhf;
    @ExcelProperty(value = "型号", index = 5)
    private String xh;
    @ExcelProperty(value = "内存", index = 6)
    private String nc;
    @ExcelProperty(value = "颜色", index = 7)
    private String ys;
    @ExcelProperty(value = "成色", index = 8)
    private String cs;
    @ExcelProperty(value = "IMEI", index = 9)
    private String imei;
    @ExcelProperty(value = "保修方式", index = 10)
    private String bxfs;
    @ExcelProperty(value = "备注", index = 11)
    private String bz;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public String getJhj() {
        return jhj;
    }

    public void setJhj(String jhj) {
        this.jhj = jhj;
    }

    public String getJhf() {
        return jhf;
    }

    public void setJhf(String jhf) {
        this.jhf = jhf;
    }

    public String getXh() {
        return xh;
    }

    public void setXh(String xh) {
        this.xh = xh;
    }

    public String getNc() {
        return nc;
    }

    public void setNc(String nc) {
        this.nc = nc;
    }

    public String getYs() {
        return ys;
    }

    public void setYs(String ys) {
        this.ys = ys;
    }

    public String getCs() {
        return cs;
    }

    public void setCs(String cs) {
        this.cs = cs;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getBxfs() {
        return bxfs;
    }

    public void setBxfs(String bxfs) {
        this.bxfs = bxfs;
    }

    public String getBz() {
        return bz;
    }

    public void setBz(String bz) {
        this.bz = bz;
    }
}
