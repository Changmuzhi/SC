package com.itheima.txt;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.itheima.mapper.TargetMapper;
import com.itheima.pojo.GH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class TxtTarget {
    private static String ghyuanshishuju = "D:\\hnhx\\ghyuanshishuju";//原始数据
    private static String ghshengchengshuju = "D:\\hnhx\\ghshengchengshuju\\";//生成数据
    @Autowired
    private TargetMapper targetMapper;
    protected static final Logger logger = LoggerFactory.getLogger(TxtTarget.class);

    @Scheduled(cron = "0/5 * * * * *")
    public void start() {
        try {
            List<String[]> list = target();//拿到每一行数据，根据数据的身份证号码去查询卡号
            if (list != null && list.size() != 0) {
                List<GH> ghList = toExcel(list);
                if(ghList!=null&&ghList.size()!=0){
                    produceExcel(ghList);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.info(e.getMessage());
            logger.info("服务器异常");
        }
    }

    public List<String[]> target() {
        try {
            //存放容器
            List<String[]> list = new ArrayList<>();
            logger.info("----公会原始数据读取中----");            //获取文件对象
            File file = new File(ghyuanshishuju);
            // 该目录下的所有文件
            File[] fileList = file.listFiles();
            //如果文件夹内的文件数量大于0，则证明有文件
            if (fileList != null && fileList.length != 0) {
                //读取文件
                BufferedReader br = null;
                for (int i = 0; i < fileList.length; i++) {
                    logger.info("----获取到工会原始数据文件----");
                    File file1 = fileList[i];
                    String filePath = file1.getPath();//拿到第i个文件路径
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "GB2312")); //这里可以控制编码
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        if (!line.equals("") || !line.equals(" ")) {
                            String[] split = line.split(",");
                            list.add(split);
                        }
                    }
                    br.close();
                    file1.delete();
                }
                logger.info("----公会原始数据读取到" + list.size() + "条----");
                return list;
            } else {
                logger.info("----公会原始数据文件夹为空----");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("----公会原始数据读取错误----");
            logger.info(e.getMessage());
        }
        return null;
    }

    //生成Excel实体Bean
    public List<GH> toExcel(List<String[]> list) {
        List<GH> ghList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String[] strings = list.get(i);
            if (strings.length>=10){
                //身份证不为空
                if (strings[4] != null && strings[4] != null) {
                    logger.info("----正在查询第"+(i+1)+"条数据卡号----");
                    String id_card = strings[4];
                    String money_cary = targetMapper.queryMoney_card(id_card);
                    //如果查询到他的卡号
                    if (money_cary != null) {
                        GH gh = new GH();
                        gh.setNumber(strings[0]);
                        gh.setName(strings[1]);
                        gh.setSex(strings[2]);
                        gh.setKc(strings[3]);
                        gh.setId_card(strings[4]);
                        gh.setKksj(strings[5]);
                        gh.setTksj(strings[6]);
                        gh.setTel(strings[7]);
                        gh.setSj(strings[8]);
                        gh.setCard_num(money_cary);
                        gh.setCard_unit(strings[10]);
                        ghList.add(gh);
                    } else {
                        //如果没有查询到
                        GH gh = new GH();
                        gh.setNumber(strings[0]);
                        gh.setName(strings[1]);
                        gh.setSex(strings[2]);
                        gh.setKc(strings[3]);
                        gh.setId_card(strings[4]);
                        gh.setKksj(strings[5]);
                        gh.setTksj(strings[6]);
                        gh.setTel(strings[7]);
                        gh.setSj(strings[8]);
                        gh.setCard_num(strings[9]);
                        gh.setCard_unit(strings[10]);
                        ghList.add(gh);
                    }
                } else {
                    //身份证为空
                    //如果没有查询到
                    GH gh = new GH();
                    gh.setNumber(strings[0]);
                    gh.setName(strings[1]);
                    gh.setSex(strings[2]);
                    gh.setKc(strings[3]);
                    gh.setId_card(strings[4]);
                    gh.setKksj(strings[5]);
                    gh.setTksj(strings[6]);
                    gh.setTel(strings[7]);
                    gh.setSj(strings[8]);
                    gh.setCard_num(strings[9]);
                    gh.setCard_unit(strings[10]);
                    ghList.add(gh);
                }
            }
        }
        logger.info("----共查询到"+ghList.size()+"条数据卡号----");
        return ghList;
    }
    private void produceExcel(List<GH> ghList) throws FileNotFoundException {
        logger.info("----开始生成工会卡号信息----");
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileTime = format.format(new Date());
        OutputStream out = new FileOutputStream(ghshengchengshuju + "SCGHKH" + fileTime + ".XLSX");
        ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX);
        Sheet sheet1 = new Sheet(1, 0, GHExcelProduce.class);
        sheet1.setSheetName("sheet1");
        List<GHExcelProduce> data = new ArrayList<>();
        for (int i = 0; i < ghList.size(); i++) {
            GHExcelProduce item = new GHExcelProduce();
            GH gh = ghList.get(i);
            item.number=gh.getNumber();
            item.name=gh.getName();
            item.sex=gh.getSex();
            item.kc=gh.getKc();
            item.id_card=gh.getId_card();
            item.kksj=gh.getKksj();
            item.tksj=gh.getTksj();
            item.tel=gh.getTel();
            item.card_num=gh.getCard_num();
            item.card_unit=gh.getCard_unit();
            item.sj=gh.getSj();
            data.add(item);
        }
        writer.write(data, sheet1);
        writer.finish();
        logger.info("----生成工会卡号信息成功----");
    }
    public static class GHExcelProduce extends BaseRowModel {
        @ExcelProperty(value = "序号", index = 0)
        private String number;
        @ExcelProperty(value = "姓名", index = 1)
        private String name;
        @ExcelProperty(value = "性别", index = 2)
        private String sex;
        @ExcelProperty(value = "卡次", index = 3)
        private String kc;
        @ExcelProperty(value = "身份证号", index = 4)
        private String id_card;
        @ExcelProperty(value = "开卡时间", index = 5)
        private String kksj;
        @ExcelProperty(value = "停卡时间", index = 6)
        private String tksj;
        @ExcelProperty(value = "手机号", index = 7)
        private String tel;
        @ExcelProperty(value = "电话号", index = 8)
        private String sj;
        @ExcelProperty(value = "卡号", index = 9)
        private String card_num;
        @ExcelProperty(value = "开卡单位", index = 10)
        private String card_unit;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public String getKc() {
            return kc;
        }

        public void setKc(String kc) {
            this.kc = kc;
        }

        public String getId_card() {
            return id_card;
        }

        public void setId_card(String id_card) {
            this.id_card = id_card;
        }

        public String getKksj() {
            return kksj;
        }

        public void setKksj(String kksj) {
            this.kksj = kksj;
        }

        public String getTksj() {
            return tksj;
        }

        public void setTksj(String tksj) {
            this.tksj = tksj;
        }

        public String getTel() {
            return tel;
        }

        public void setTel(String tel) {
            this.tel = tel;
        }

        public String getCard_num() {
            return card_num;
        }

        public void setCard_num(String card_num) {
            this.card_num = card_num;
        }

        public String getCard_unit() {
            return card_unit;
        }

        public void setCard_unit(String card_unit) {
            this.card_unit = card_unit;
        }

        public String getSj() {
            return sj;
        }

        public void setSj(String sj) {
            this.sj = sj;
        }
    }
}
