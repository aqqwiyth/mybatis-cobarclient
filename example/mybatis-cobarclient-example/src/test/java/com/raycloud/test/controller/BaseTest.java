package com.raycloud.test.controller;

import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.*;

/**
 * Description:
 * User: ouzhouyou@raycloud.com
 * Date: 14-5-12
 * Time: 上午11:14
 * Version: 1.0
 */
public class BaseTest {

    public static String host = "http://127.0.0.1:8080";
    public static Map<String, String> _map = new HashMap<String, String>();

    static {
        _map.put("api_name", "");
        _map.put("json", "json");
        _map.put("callback", "");
    }

    public static HttpClient client = new HttpClient();

//    static {
//        try {
//            doGet(host,null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    protected static String doGet(String url, Map<String, String> map) throws IOException {
        GetMethod method = new GetMethod(url);
        return execute(map, method);
    }

    protected static String doPost(String url, Map<String, String> map) throws IOException {
        PostMethod method = new PostMethod(url);
        return execute(map, method);
    }

    protected static String doPut(String url, Map<String, String> map) throws IOException {
        PutMethod method = new PutMethod(url);
        return execute(map, method);
    }

    protected static String doDelete(String url, Map<String, String> map) throws IOException {
        DeleteMethod method = new DeleteMethod(url);
        return execute(map, method);
    }

    protected static String doHead(String url, Map<String, String> map) throws IOException {
        HeadMethod method = new HeadMethod(url);
        return execute(map, method);
    }


    private static String execute(Map<String, String> map, HttpMethod method) throws IOException {
        if (MapUtils.isNotEmpty(map)) {
            NameValuePair[] nameValuePairs = new NameValuePair[map.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                nameValuePairs[i++] = new NameValuePair(entry.getKey(), entry.getValue());
            }
            method.setQueryString(nameValuePairs);
        }
        System.out.println(method.getURI());
        client.executeMethod(method);
        return method.getResponseBodyAsString().trim();
    }

    public static JSONObject executeJSON(Map<String, String> map, HttpMethod method) throws IOException {
        return JSONObject.parseObject(execute(map, method));
    }

//    public void baseAssert(JSONObject object) {
//        Assert.assertEquals(object.getString("message"), "读取信息成功");
//        Assert.assertEquals(object.getInteger("result"), Integer.valueOf(100));
//        Assert.assertNotNull(object.getJSONObject("data"));
//    }
//
//    public void baseAssertItems(JSONObject object) {
//
//    }
//    public void baseAssertReferrers(JSONObject object) {
//        Assert.assertNotNull(object.getJSONObject("data").getJSONArray("referrers"));
//    }
//
//    public void baseAssertPage(JSONObject object) {
//        Assert.assertNotNull(Integer.valueOf(object.getJSONObject("data").getInteger("total")));
//        Assert.assertNotNull(Integer.valueOf(object.getJSONObject("data").getInteger("page_num")));
//        Assert.assertNotNull(Integer.valueOf(object.getJSONObject("data").getInteger("page_size")));
//    }
}
