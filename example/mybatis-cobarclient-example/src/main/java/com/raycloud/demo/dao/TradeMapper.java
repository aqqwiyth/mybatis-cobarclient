package com.raycloud.demo.dao;

import com.raycloud.demo.model.TradeModel;
import java.util.List;
import org.apache.ibatis.annotations.*;

/**
 * Description:存注解形势
 * User: ouzhouyou@raycloud.com
 * Date: 14-6-14
 * Time: 下午2:52
 * Version: 1.0
 */

public interface TradeMapper {

    @Select("SELECT tid , title FROM trade where tid=#{tid}")
    TradeModel getTradeByTid(@Param("tid") Long tid, @Param("splitDBName") String splitDBName);

    @Select("SELECT tid , title FROM trade")
    List<TradeModel> getShopList(@Param("splitDBName") String splitDBName);

    @Insert("INSERT INTO trade (tid, title)  VALUES (#{tid}, #{title})")
    Integer insertTrade(TradeModel tradeModel);

    @Update("UPDATE trade set title = #{title} where tid =#{tid}")
    Integer updateTrade(TradeModel tradeModel);

    @Delete("DELETE from trade where tid =#{tid}")
    Integer deleteTrade(@Param("tid") Long tid, @Param("splitDBName") String splitDBName);

    /**
     * xxxProvider注解用于动态生成SQL语句
     * xxxProvide指定一个Class及其方法，并且通过调用Class上的这个方法来获得sql语句。 常见的场景:比如分表
     */
    @InsertProvider(type = TradeModel.class, method = "batchInsertTrade")
    Integer batchInsertTrade(@Param("list") List<TradeModel> tradeModel, @Param("splitDBName") String splitDBName);

    @Insert("INSERT INTO trade (tid, title)  VALUES (#{tid}, #{title})")
    Integer batchInsertTrade2(List<TradeModel> tradeModel);

}
