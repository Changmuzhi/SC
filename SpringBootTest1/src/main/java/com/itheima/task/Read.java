package com.itheima.task;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.read.context.AnalysisContext;
import com.alibaba.excel.read.event.AnalysisEventListener;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.itheima.mapper.TargetMapper;
import com.itheima.pojo.YuanShiShuJu;
import com.itheima.util.ExcelReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class Read {
    @Autowired
    private TargetMapper targetMapper;
    private static String yuanshishuju = "D:\\hnhx\\yuanshishuju";//原始数据
    private static String shengchengshuju = "D:\\hnhx\\shengchengshuju\\";//原始数据
    protected static final Logger logger = LoggerFactory.getLogger(Read.class);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
    Calendar date = Calendar.getInstance();
    Integer year = Integer.parseInt(String.valueOf(date.get(Calendar.YEAR)));

    @Scheduled(cron = "0/5 * * * * *")
    public void start() {

        try {
            List<YuanShiShuJu> original = original();//拿到原始数据
            if (original != null && original.size() != 0) {

                //更新目标资源库信息可用状态
                targetMapper.updateFlagMaster();
                //生成输出表格集合
                List<YuanShiShuJu> excel = new ArrayList<>();
                //添加男女性别
                //开始遍历查询
                for (int i = 0; i < original.size(); i++) {
                    Map<String, String> map = new HashMap<>();
                    //拿到第一个需要完善的信息
                    YuanShiShuJu shuJu = original.get(i);
                    //第一种情况，姓名、乡镇、性别不为空
                    if (!StringUtils.isEmpty(shuJu.getName())
                            && !StringUtils.isEmpty(shuJu.getLocation())
                            && !StringUtils.isEmpty(shuJu.getSex())
                    ) {
                        this.queryinfo(map, shuJu, excel);
                    }//第二种情况，姓名、乡镇不为空
                    else if (!StringUtils.isEmpty(shuJu.getName())
                            && !StringUtils.isEmpty(shuJu.getLocation())) {
                        this.queryInfo2(map, shuJu, excel);
                    }
                    //第三种情况姓名 性别不为空
                    else if (!StringUtils.isEmpty(shuJu.getName())
                            && !StringUtils.isEmpty(shuJu.getSex())) {
                        this.queryInfo3(map, shuJu, excel);
                    }
                    //第四种情况 姓名不为空
                    else if (!StringUtils.isEmpty(shuJu.getName())) {
                        this.queryInfo4(map, shuJu, excel);
                    }
                    //都为空，跳出本次执行
                    else {
                        continue;
                    }
                }
                logger.info("----数据查询成功，共查询到:" + excel.size() + "条数据");
                logger.info("----开始生成excel表格----");
                writeWithMultiHead(excel);
                logger.info("----生成excel表格完成----");

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("内存溢出，请升级电脑配置");
            logger.info(e.getMessage());
        }
    }

    //第一种情况，姓名、乡镇、性别不为空
    public void queryinfo(Map map, YuanShiShuJu shuJu, List<YuanShiShuJu> excel) {
        //根据以下的条件查询数据库
        map.put("name", shuJu.getName());
        map.put("location", shuJu.getLocation());
        map.put("sex", shuJu.getSex());
        //根据这些条件查询出符合这些信息的人
        List<YuanShiShuJu> info = targetMapper.queryInfo(map);
        if (info == null || info.size() == 0) {
            //没有查询出信息
            this.queryInfo2(map, shuJu, excel);
            return;
        }
        //遍历这些符合条件人的信息
        for (int i1 = 0; i1 < info.size(); i1++) {
            YuanShiShuJu temp = info.get(i1);
            //判断身份证号码位数性别
            String id_card = temp.getId_card();
            int b;
            if (id_card.length() >= 17) {
                b = Integer.parseInt(id_card.substring(16, 17));//强制类型转换
            }else {
                break;
            }
            String a = "";
            //如果是男
            if (b % 2 == 0) {
                a = "女";
                //如果需要完善的信息也是男，则把从数控中读取到的信息给它
                if (temp.getSex().equals(a)) {
                    shuJu.setId_card(temp.getId_card());
                    shuJu.setBirthday(temp.getBirthday());
                    shuJu.setAddress(temp.getAddress());
                    excel.add(shuJu);
                    logger.info("已查询到第" + excel.size() + "条数据");
                    //将该数据标记已使用
                    targetMapper.updateFlag(temp.getId_card());
                    break;
                }
            } else {
                //如果是女
                a = "男";
                if (temp.getSex().equals(a)) {
                    if (temp.getSex().equals(a)) {
                        shuJu.setId_card(temp.getId_card());
                        shuJu.setBirthday(temp.getBirthday());
                        shuJu.setAddress(temp.getAddress());
                        targetMapper.updateFlag(temp.getId_card());
                        //添加到excel表格集合
                        excel.add(shuJu);
                        logger.info("已查询到第" + excel.size() + "条数据");
                        break;
                    }
                }
            }
        }
    }

    //第二种情况，姓名、乡镇不为空
    public void queryInfo2(Map map, YuanShiShuJu shuJu, List<YuanShiShuJu> excel) {
        map.put("name", shuJu.getName());
        map.put("location", shuJu.getLocation());
        List<YuanShiShuJu> info = targetMapper.queryInfo2(map);
        if (info != null && info.size() != 0) {
            YuanShiShuJu temp = info.get(0);
            shuJu.setId_card(temp.getId_card());
            shuJu.setBirthday(temp.getBirthday());
            shuJu.setSex(temp.getSex());
            shuJu.setAddress(temp.getAddress());
            //添加到excel表格集合
            excel.add(shuJu);
            logger.info("已查询到第" + excel.size() + "条数据");
            //将该数据标记已使用
            targetMapper.updateFlag(temp.getId_card());
        } else {
            this.queryInfo4(map, shuJu, excel);
        }
    }

    //第三种情况姓名 性别不为空
    public void queryInfo3(Map map, YuanShiShuJu shuJu, List<YuanShiShuJu> excel) {
        map.put("name", shuJu.getName());
        map.put("sex", shuJu.getSex());
        List<YuanShiShuJu> info = targetMapper.queryInfo3(map);
        if (info.size() != 0 && info != null) {
            //遍历这些符合条件人的信息
            for (int i1 = 0; i1 < info.size(); i1++) {
                YuanShiShuJu temp = info.get(i1);
                //判断身份证号码位数性别
                String id_card = temp.getId_card();
                int b;
                if (id_card.length() >= 17) {
                    b = Integer.parseInt(id_card.substring(16, 17));//强制类型转换
                }else {
                    break;
                }
                String a = "";
                //如果是男
                if (b % 2 == 0) {
                    a = "女";
                    //如果需要完善的信息也是男，则把从数控中读取到的信息给它
                    if (temp.getSex().equals(a)) {
                        shuJu.setId_card(temp.getId_card());
                        shuJu.setBirthday(temp.getBirthday());
                        shuJu.setAddress(temp.getAddress());
                        shuJu.setLocation(temp.getLocation());
                        excel.add(shuJu);
                        logger.info("已查询到第" + excel.size() + "条数据");
                        //将该数据标记已使用
                        targetMapper.updateFlag(temp.getId_card());
                        break;
                    }
                } else {
                    //如果是女
                    a = "男";
                    if (temp.getSex().equals(a)) {
                        if (temp.getSex().equals(a)) {
                            shuJu.setId_card(temp.getId_card());
                            shuJu.setBirthday(temp.getBirthday());
                            shuJu.setAddress(temp.getAddress());
                            shuJu.setLocation(temp.getLocation());
                            targetMapper.updateFlag(temp.getId_card());
                            excel.add(shuJu);
                            logger.info("已查询到第" + excel.size() + "条数据");
                            break;
                        }
                    }
                }

            }
        } else {
            this.queryInfo4(map, shuJu, excel);
        }

    }

    //第四种情况 姓名不为空
    public void queryInfo4(Map map, YuanShiShuJu shuJu, List<YuanShiShuJu> excel) {
        //如果名字只有一个字，触发模糊查询
        boolean flag = false;
        map.put("name", shuJu.getName());
        List<YuanShiShuJu> info = new ArrayList<>();
        if (shuJu.getName().length() == 1) {
            info = targetMapper.likequeryInfo4(map);
        } else {
            info = targetMapper.queryInfo4(map);
        }

        if (info.size() != 0 && info != null) {
            for (int i = 0; i < info.size(); i++) {
                YuanShiShuJu yuanShiShuJu = info.get(i);
//                int i1 = Integer.parseInt(yuanShiShuJu.getBirthday().split("-")[0].substring(0, 4));
//                i1 = year - i1;
                YuanShiShuJu temp = info.get(0);
                shuJu.setName(yuanShiShuJu.getName());
                shuJu.setId_card(temp.getId_card());
                String id_card = temp.getId_card();

                String birthday = id_card.substring(6, 14);

                try {
                    Date date = simpleDateFormat.parse(birthday);
                    birthday = simpleDateFormat1.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                    logger.info(e.getMessage());
                }
                shuJu.setBirthday(birthday);
                int b;
                if (id_card.length() >= 17) {
                    b = Integer.parseInt(id_card.substring(16, 17));//强制类型转换
                }else {
                    break;
                }
                String a = "";
                //如果是女
                if (b % 2 == 0) {
                    a = "女";
                } else {
                    a = "男";
                }
                shuJu.setSex(a);
                shuJu.setAddress(temp.getAddress());
                shuJu.setLocation(temp.getLocation());
                //添加到excel表格集合
                excel.add(shuJu);
                //将该数据标记已使用
                targetMapper.updateFlag(temp.getId_card());
                logger.info("已查询到第" + excel.size() + "条数据");
                break;

            }
        } else {
            //没有查询到信息
            excel.add(shuJu);
        }
    }

    /**
     * 原始数据
     *
     * @return
     * @throws Exception
     */
    public List<YuanShiShuJu> original() throws Exception {
        try {
            logger.info("----原始数据获取中----");
            //获取文件对象
            File file = new File(yuanshishuju);
            // 该目录下的所有文件
            File[] fileList = file.listFiles();
            //拿到了原始数据
            List<YuanShiShuJu> list = new ArrayList<>();
            //如果文件夹内的文件数量大于0，则证明有文件
            if (fileList != null && fileList.length != 0) {
                //遍历文件夹，拿到每一个文件path，并且调用工具类加载到list集合中
                for (int i = 0; i < fileList.length; i++) {
                    File file1 = fileList[i];
                    String filePath = file1.getPath();//拿到第i个文件路径
                    this.read(list, filePath);
                    //删除原始数据表格，避免重复读取
                    file1.delete();
                }
                //如果list集合大于0，则读取到原始数据
                if (list.size() != 0) {
                    logger.info("----原始数据获取成功----");
                    return list;
                } else {
                    //原始数据表格为空
                    logger.info("----原始数据表格为空,请检查原始数据----");
                }
            } else {
                logger.info("----原始数据文件为空----");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("----原始数据读取错误----");
            logger.info(e.getMessage());
        }
        return null;
    }


    /**
     * 读取excel工具类
     *
     * @param list     表格读取封装对象
     * @param filePath 表格路径
     * @throws Exception
     */

    public void read(List<YuanShiShuJu> list, String filePath) throws Exception {
        try (InputStream in = new FileInputStream(filePath);) {
            AnalysisEventListener<YuanShiShuJu> listener = new AnalysisEventListener<YuanShiShuJu>() {

                @Override
                public void invoke(YuanShiShuJu object, AnalysisContext context) {
                    if (object.getName() != null) {
                        list.add(object);
                        System.err.println("Row:" + context.getCurrentRowNum() + " Data:" + object);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    System.err.println("doAfterAllAnalysed...");
                }
            };
            ExcelReader excelReader = ExcelReaderFactory.getExcelReader(in, null, listener);
            // 第二个参数为表头行数，按照实际设置
            excelReader.read(new Sheet(1, 1, YuanShiShuJu.class));
        }
    }

    public void writeWithMultiHead(List<YuanShiShuJu> excel) throws IOException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileTime = format.format(new Date());

        OutputStream out = new FileOutputStream(shengchengshuju + "SC" + fileTime + ".XLSX");
        ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX);
        Sheet sheet1 = new Sheet(1, 0, ExcelProduce.class);
        sheet1.setSheetName("sheet1");
        List<ExcelProduce> data = new ArrayList<>();
        for (int i = 0; i < excel.size(); i++) {
            ExcelProduce item = new ExcelProduce();
            YuanShiShuJu shuJu = excel.get(i);
            item.name = shuJu.getName();
            item.location = shuJu.getLocation();
            item.sex = shuJu.getSex();
            item.birthday = shuJu.getBirthday();
            item.id_card = shuJu.getId_card();
            item.address = shuJu.getAddress();
            data.add(item);
        }
        writer.write(data, sheet1);
        writer.finish();

    }

    public static class ExcelProduce extends BaseRowModel {
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

}


