package com.raycloud.demo.model;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * User: ouzhouyou@raycloud.com
 * Date: 14-2-27
 * Time: 下午11:19
 * Version: 1.0
 */
public class TradeModel extends BasePojo {
    private String title;
    private Long tid;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public static String getShopByUserId(Map<String, Object> para) {
        //获取路由信息查询 然后查询
        return "SELECT tid , title FROM trade"/**para.get("shopId")**/;
    }

    public static String batchInsertTrade(Map<String, Object> para) {
        int size = ((List) para.get("list")).size();
        StringBuffer buffer = new StringBuffer();
        buffer.append("INSERT INTO trade (tid, title)  VALUES ");
        MessageFormat format = new MessageFormat("" +
                "(" +
                " #'{'list[{0}].tid}," +
                " #'{'list[{0}].title}" +
                ")");
        for (int i = 0; i < size; i++) {
            buffer.append(format.format(new Object[]{i}));
            if (i < size - 1) {
                buffer.append(",");
            }
        }
        return buffer.toString();
    }

}
