package com.raycloud.demo.action;

import com.alibaba.fastjson.JSONObject;
import com.raycloud.demo.model.TradeModel;
import com.raycloud.demo.service.TradeService;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CrudController {
    private Log log = LogFactory.getLog(CrudController.class);
    @Resource
    private TradeService tradeService;

    //订单添加
    @ResponseBody
    @RequestMapping(value = {"/add/{splitDBName}"}, method = RequestMethod.GET)
    public Object add(@PathVariable("splitDBName") String splitDBName, String title) {
        TradeModel model = new TradeModel();
        long tid = System.currentTimeMillis();
        if (StringUtils.isEmpty(title)) title = "title is null" + tid;
        model.setTid(tid);
        model.setTitle(title);
        model.setSplitDBName(splitDBName);
        int size = tradeService.insertTrade(model);
        log.info("[add]" + JSONObject.toJSONString(model) + "\tsize:" + size);
        return model;
    }

    @ResponseBody
    @RequestMapping(value = {"/get/{splitDBName}/{tid}"}, method = RequestMethod.GET)
    public Object get(@PathVariable("splitDBName") String splitDBName, @PathVariable("tid") long tid) {
        return tradeService.getTradeByTid(tid, splitDBName);
    }

    @ResponseBody
    @RequestMapping(value = {"/list/{splitDBName}"}, method = RequestMethod.GET)
    public Object list(@PathVariable("splitDBName") String splitDBName) {
        List list = tradeService.getTradeList(splitDBName);
        log.info("[list]\tsize:" + list.size());
        return list;
    }

    //批量导入
    @ResponseBody
    @RequestMapping(value = {"/b1/{splitDBName}"}, method = RequestMethod.GET)
    public Object batch1(@PathVariable("splitDBName") String splitDBName, String title) {
        List<TradeModel> list = new ArrayList<TradeModel>();
        long tid = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            TradeModel model = new TradeModel();
            model.setTid(++tid);
            if (StringUtils.isEmpty(title)) title = "title is null" + tid;
            model.setTitle(title);
            model.setSplitDBName(splitDBName);
            list.add(model);
        }
        return tradeService.batchInsertTrade(list);
    }

    //批量导入
    @ResponseBody
    @RequestMapping(value = {"/b2/{splitDBName}"}, method = RequestMethod.GET)
    public Object batch2(@PathVariable("splitDBName") String splitDBName, String title) throws Exception {
        List<TradeModel> list = new ArrayList<TradeModel>();
        long tid = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            TradeModel model = new TradeModel();
            model.setTid(++tid);
            if (StringUtils.isEmpty(title)) title = "title is null" + tid;
            model.setTitle(title);
            model.setSplitDBName((i % 2)+1 + "");
            list.add(model);
        }
        return tradeService.batchInsertTrade2(list);
    }


    //批量导入
    @ResponseBody
    @RequestMapping(value = {"/b3/{splitDBName}"}, method = RequestMethod.GET)
    public Object batch3(@PathVariable("splitDBName") String splitDBName, String title) {
        List<TradeModel> list = new ArrayList<TradeModel>();
        long tid = System.currentTimeMillis();
        for (int i = 0; i < 110; i++) {
            TradeModel model = new TradeModel();
            model.setTid(++tid);
            if (StringUtils.isEmpty(title)) title = "title is null" + tid;
            model.setTitle(title);
            model.setSplitDBName((i % 2)+1 + "");
            list.add(model);
        }
        return tradeService.batchInsertTrade3(list);
    }
}