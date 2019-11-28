package com.itheima.txt;

import com.itheima.mapper.TargetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TxtRead {
    private static String filePath = "D:\\hnhx\\kahao";//卡号存放路径
    @Autowired
    private TargetMapper targetMapper;
    protected static final Logger logger = LoggerFactory.getLogger(TxtRead.class);

    @Scheduled(cron = "0/5 * * * * *")
    public void start() {
        target();
    }

    //目标文件读取
    public void target() {
        try {
            logger.info("----卡号文件获取中----");

            //获取文件对象
            File file = new File(filePath);
            // 该目录下的所有文件
            File[] fileList = file.listFiles();

            //如果文件夹内的文件数量大于0，则证明有文件
            if (fileList != null && fileList.length != 0) {
                //读取文件
                BufferedReader br = null;
                StringBuffer sb = null;
                //map存放容器
                List<Map> list = new ArrayList<>();
                for (int i = 0; i < fileList.length; i++) {
                    logger.info("----获取到目标数据文件----");
                    File file1 = fileList[i];
                    String filePath = file1.getPath();//拿到第i个文件路径
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "GB2312")); //这里可以控制编码
                    String line = null;
                    Map<String, String> map = new HashMap<>();
                    int n = 1;
                    while ((line = br.readLine()) != null) {
                        line = removeAllBlank(line);
                        line = trim(line);
                        if ((line.equals(" ") || StringUtils.isEmpty(line))||line.equals("")) {
                            continue;
                        }
                        if (n == 3) {
                            n++;
                            map.put("id_card", line);
                            continue;
                        } else if (n == 5) {
                            map.put("money_card", line);
                            n++;
                            continue;
                        } else if (n == 7) {
                            list.add(map);
                            map = new HashMap<>();
                            n=1;
                        } else {
                            n ++;
                        }
                    }
                    br.close();
                    file1.delete();
                }
                if (list != null && list.size() != 0) {
                    logger.info("----卡号文件读取完毕----");
                        int rows = 0;
                        for (int i = 0; i < list.size() / 2000 + 1; i++) {
                            int end = (i + 1) * 2000;
                            if (end > list.size()) {
                                end = list.size();
                            }
                            List<Map> maps = list.subList(i * 2000, end);
                            rows += targetMapper.insertMoney_card(maps);
                            logger.info("----卡号文件数据写入中----");
                        }
                    logger.info("----卡号文件数据写入成功----");
                } else {
                    logger.info("----卡号文件内容为空----");
                }
            } else {
                logger.info("----卡号文件文件夹为空----");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("----卡号文件读取错误----");
            logger.info(e.getMessage());
        }
    }

    public  String removeAllBlank(String s){
        String result = "";
        if(null!=s && !"".equals(s)){
            result = s.replaceAll("[　*| *|&nbsp;*|//s*]*", "");
        }
        return result;
    }

    public  String trim(String s){
        String result = "";
        if(null!=s && !"".equals(s)){
            result = s.replaceAll("^[　*| *|&nbsp;*|//s*]*", "").replaceAll("[　*| *|&nbsp;*|//s*]*$", "");
        }
        return result;
    }

}
