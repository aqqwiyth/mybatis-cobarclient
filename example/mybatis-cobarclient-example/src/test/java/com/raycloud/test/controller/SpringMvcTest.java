package com.raycloud.test.controller;

import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Description:
 * User: ouzhouyou@raycloud.com
 * Date: 14-5-12
 * Time: 上午11:14
 * Version: 1.0
 */
public class SpringMvcTest extends BaseTest {
    static Long t1;
    static Long t2;
    static Integer s1;
    static Integer s2;

    @Test
    public void test001() throws IOException {
        String title1 = "addTest_to_slave_1";
        String title2 = "addTest_to_slave_2";
        System.out.println("get/add测试....开始");
        addTest(title1, title2);
        getTest(title1, title2);
        System.out.println("get/add测试....通过");
    }

    @Test
    public void test002() throws IOException {
        System.out.println("list/adds测试....开始");
        listTest();
        System.out.println("list/adds测试....通过");
    }

    @Test
    public void test003() throws IOException {
    }


    public static void addTest(String title1, String title2) throws IOException {
        String url1 = "/add/1.json?title=" + title1;
        String url2 = "/add/2.json?title=" + title2;
        String obj1 = doGet(host + url1, null);
        String obj2 = doGet(host + url2, null);
        System.out.println("======slave1_ADD=====:" + obj1);
        System.out.println("======slave2_ADD=====:" + obj2);
        Assert.assertEquals(JSONObject.parseObject(obj1).get("title"), title1);
        Assert.assertEquals(JSONObject.parseObject(obj2).get("title"), title2);
        t1 = (Long) JSONObject.parseObject(obj1).get("tid");
        t2 = (Long) JSONObject.parseObject(obj2).get("tid");
    }

    public static void getTest(String title1, String title2) throws IOException {
        String url1 = "/get/1/" + t1 + ".json";
        String url2 = "/get/2/" + t2 + ".json";
        String obj1 = doGet(host + url1, null);
        String obj2 = doGet(host + url2, null);
        System.out.println("======slave1_GET=====:" + obj1);
        System.out.println("======slave2_GET=====:" + obj2);
        Assert.assertEquals(JSONObject.parseObject(obj1).get("title"), title1);
        Assert.assertEquals(JSONObject.parseObject(obj2).get("title"), title2);
        t1 = (Long) JSONObject.parseObject(obj1).get("tid");
        t2 = (Long) JSONObject.parseObject(obj2).get("tid");
    }

    public static void listTest() throws IOException {
        String url1 = "/list/1.json";
        String url2 = "/list/2.json";
        String obj1 = doGet(host + url1, null);
        String obj2 = doGet(host + url2, null);
        Assert.assertNotNull(JSONObject.parseArray(obj1));
        Assert.assertNotNull(JSONObject.parseArray(obj2));
        s1 = JSONObject.parseArray(obj1).size();
        s2 = JSONObject.parseArray(obj2).size();
        System.out.println("======slave1_LIST=====:" + s1);
        System.out.println("======slave2_LIST=====:" + s2);
    }

    public static void batchTest() throws IOException {
//        String url1 = "/list/1.json";
//        String url2 = "/list/2.json";
//        String obj1 = doGet(host + url1, null);
//        String obj2 = doGet(host + url2, null);
//        Assert.assertNotNull(JSONObject.parseArray(obj1));
//        Assert.assertNotNull(JSONObject.parseArray(obj2));
//        s1 = JSONObject.parseArray(obj1).size();
//        s2 = JSONObject.parseArray(obj2).size();
//        System.out.println("======slave1_LIST=====:" + s1);
//        System.out.println("======slave2_LIST=====:" + s2);
    }


}