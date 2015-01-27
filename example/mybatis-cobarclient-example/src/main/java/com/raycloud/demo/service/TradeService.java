package com.raycloud.demo.service;

import com.raycloud.demo.dao.TradeMapper;
import com.raycloud.demo.model.TradeModel;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Description:
 * User: ouzhouyou@raycloud.com
 * Date: 14-6-14
 * Time: 下午2:52
 * Version: 1.0
 */
@Service
public class TradeService {

    @Autowired
    TradeMapper tradeMapper;
    @Autowired
    private PlatformTransactionManager txManager;

    public List<TradeModel> getTradeList(String splitDBName) {
        return tradeMapper.getShopList(splitDBName);
    }

    public TradeModel getTradeByTid(Long tid, String splitDBName) {
        return tradeMapper.getTradeByTid(tid, splitDBName);
    }

    public Integer insertTrade(TradeModel model) {
        return tradeMapper.insertTrade(model);
    }

    public Integer updateTrade(TradeModel model) {
        return tradeMapper.updateTrade(model);
    }

    public Integer batchInsertTrade(List<TradeModel> model) {
        return tradeMapper.batchInsertTrade(model, model.get(0).getSplitDBName());
    }

    public Integer batchInsertTrade2(List<TradeModel> model) throws Exception {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txManager.getTransaction(def);
        try {
            tradeMapper.batchInsertTrade2(model);
            throw new RuntimeException("...");
        } catch (Exception e) {
            txManager.rollback(status);
            throw e;
        }
    }

    @Transactional
    public Integer batchInsertTrade3(List<TradeModel> model) {
       return tradeMapper.batchInsertTrade2(model);
//        throw new RuntimeException("...");
    }
}
