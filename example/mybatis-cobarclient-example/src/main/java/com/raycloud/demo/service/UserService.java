package com.raycloud.demo.service;

import com.raycloud.demo.dao.TradeMapper;
import com.raycloud.demo.dao.UserMapper;
import com.raycloud.demo.model.TradeModel;
import com.raycloud.demo.model.UserModel;
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
public class UserService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    TradeMapper tradeMapper;
    @Autowired
    private PlatformTransactionManager txManager;

    public List<UserModel> getUserList() {
        return userMapper.getUserList();
    }

    public void insertUser(UserModel userModel) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txManager.getTransaction(def);
        try {
            userMapper.insertUser(userModel);
            TradeModel tradeModel = new TradeModel();
            tradeModel.setTid(10086L);
            tradeModel.setTitle("手动事务测试");
            tradeModel.setSplitDBName("1");
            tradeMapper.insertTrade(tradeModel);
            txManager.commit(status);
        } catch (Exception e) {
            e.printStackTrace();
            txManager.rollback(status);
        }
    }

    @Transactional
    public void insertUserAnnotations(UserModel userModel) {
        userMapper.insertUser(userModel);
        TradeModel tradeModel = new TradeModel();
        tradeModel.setTid(10086L);
        tradeModel.setTitle("注解事务测试");
        tradeModel.setSplitDBName("1");
        tradeMapper.insertTrade(tradeModel);
    }

    public void clean(String userNick) {
        userMapper.deleteUser(userNick);
        tradeMapper.deleteTrade(10086L,"1");
    }
}
