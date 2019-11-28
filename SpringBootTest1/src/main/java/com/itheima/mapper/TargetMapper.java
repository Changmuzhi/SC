package com.itheima.mapper;

import com.itheima.pojo.YuanShiShuJu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface TargetMapper {
   Integer insertMsg(List<YuanShiShuJu> list);

   List<YuanShiShuJu> queryInfo(Map<String, String> map);

   List<YuanShiShuJu> queryInfo2(Map<String, String> map);

   List<YuanShiShuJu> queryInfo3(Map<String, String> map);

   List<YuanShiShuJu> queryInfo4(Map<String, String> map);

   void updateFlag(String id_card);

   void updateFlagMaster();

   List<YuanShiShuJu> likequeryInfo4(Map map);

   Integer insertMoney_card(List<Map> maps);

   String queryMoney_card(String id_card);
}
