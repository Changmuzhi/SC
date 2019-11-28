package com.itheima.task;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.read.context.AnalysisContext;
import com.alibaba.excel.read.event.AnalysisEventListener;
import com.itheima.mapper.TargetMapper;
import com.itheima.pojo.YuanShiShuJu;
import com.itheima.util.ExcelReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 读取资源文件
 */
@Component
public class Target {

    private static String mubiaoshuju = "D:\\hnhx\\mubiaoshuju";//目标数据
    @Autowired
    private TargetMapper targetMapper;
    protected static final Logger logger = LoggerFactory.getLogger(Target.class);


    @Scheduled(cron = "0/5 * * * * *")
    public void start() {
        try {
            List<YuanShiShuJu> list = target();

            //一次写入2000条数据
            if (list != null && list.size() != 0) {
                logger.info("----读取到目标数据"+list.size()+"条----");
                logger.info("----目标数据写入开始----");
                int rows = 0;
                for (int i = 0; i < list.size() / 2000 + 1; i++) {
                    int end = (i + 1) * 2000;
                    if (end > list.size()) {
                        end = list.size();
                    }
                    List<YuanShiShuJu> subList = list.subList(i * 2000, end);
                    rows += targetMapper.insertMsg(subList);
                    logger.info("----目标数据已写入"+(i * 2000)+"行----");
                }
                logger.info("----目标数据写入成功----");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
        }
    }

    //目标文件读取
    public List<YuanShiShuJu> target() {
        try {
            logger.info("----目标数据文件获取中----");
            //获取文件对象
            File file = new File(mubiaoshuju);
            // 该目录下的所有文件
            File[] fileList = file.listFiles();
            //拿到了原始数据
            List<YuanShiShuJu> list = new ArrayList<>();
            //如果文件夹内的文件数量大于0，则证明有文件
            if ( fileList != null&&fileList.length != 0 ) {
                for (int i = 0; i < fileList.length; i++) {
                    logger.info("----获取到目标数据文件----");
                    File file1 = fileList[i];
                    String filePath = file1.getPath();//拿到第i个文件路径
                    this.read(list, filePath);
                    //删除原始数据表格，避免重复读取
                    file1.delete();
                }
                //如果list集合大于0，则读取到原始数据
                if (list.size() != 0) {
                    logger.info("----未获取到目标数据文件----");
                    return list;
                } else {
                    //原始数据表格为空
                    logger.info("----目标数据表格为空,请检查目标数据----");
                }
            } else {
                logger.info("----目标数据文件夹为空----");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("----目标数据读取错误----");
            logger.info(e.getMessage());
        }
        return null;
    }

    /**
     * 读取excel工具类
     *
     * @param list 表格读取封装对象
     * @param filePath 表格路径
     * @throws Exception
     */
    private static String yuanshishuju = "D:\\hnxh\\yuanshishuju";//原始数据

    public void read(List<YuanShiShuJu> list, String filePath) throws Exception {
        try (InputStream in = new FileInputStream(filePath);) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
            AnalysisEventListener<YuanShiShuJu> listener = new AnalysisEventListener<YuanShiShuJu>() {

                @Override
                public void invoke(YuanShiShuJu object, AnalysisContext context) {
                    if (object.getId_card() != null && object.getId_card().length()==18) {
                        String id_card = object.getId_card();
                        String birthday = id_card.substring(6, 14);
                        try {
                            Date date = simpleDateFormat.parse(birthday);
                            birthday = simpleDateFormat1.format(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            logger.info(e.getMessage());
                        }
                        object.setBirthday(birthday);
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
}
