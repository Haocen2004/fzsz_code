package com.github.haocen2004;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {
    private static String  pw,ac,epw,req,key,sid,name,userid,term,year;
    private static List<String> g1,g2,g3;
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Account: ");
        ac = scanner.next();
        System.out.print("Password：");
        pw = scanner.next();
        System.out.println("======================");
        System.out.println("Account: " +ac);
        System.out.println("Password: " +pw);
        epw = xzdEncrypt.encrypt(pw);
        System.out.println("Encrypted Password: " +epw);
        System.out.println("======================");

        JSONObject req_json = new JSONObject();
        JSONObject cond_json = new JSONObject();
        cond_json.put("USERNAME",ac);
        cond_json.put("PASSWORD",epw);
        cond_json.put("imei","");
        cond_json.put("imsi","");
        cond_json.put("iccid","");
        cond_json.put("mac","020000000000");
        req_json.put("request_type","mobile_login_v2");
        req_json.put("cond",cond_json);
        System.out.println(cond_json.toString());
        System.out.println(req_json.toString());


        req = xzdEncrypt.sign(req_json);

        String feedback = sendPost("http://m.fzsz.net/api_abc/api-s1.php",req);
        System.out.println(feedback);

        JSONObject account_json = new JSONObject(feedback);
        if (account_json.getInt("code") == 0) {
            key = account_json.getString("key");
            sid = account_json.getString("sid");
            name = account_json.getString("name");
            userid = account_json.getString("userId");
            year = account_json.getString("cur_school_year");
            term = account_json.getString("cur_school_term");
            System.out.println("======================");
            System.out.println("Login Succeed");
            System.out.println("User Name: " +name);
            System.out.println("User Id: " +userid);
            System.out.println("Key: " +key);
            System.out.println("Sid: " +sid);
            System.out.println("======================");

        } else {
            System.out.println("======================");
            System.out.println("Login Failed");
            System.out.println("======================");
            return;
        }

        JSONObject getClassJson = new JSONObject();
        JSONObject cond_json2 = new JSONObject();
        cond_json2.put("school_year",year);
        cond_json2.put("school_term",term);
        cond_json2.put("user_id",userid);
        getClassJson.put("cmd","get_user_class");
        getClassJson.put("key",key);
        getClassJson.put("sid",sid);
        getClassJson.put("cond",cond_json2);

        req = xzdEncrypt.sign(getClassJson);

        feedback = sendPost("http://m.fzsz.net/api_abc/api-s1.php",req);
        System.out.println(feedback);
        JSONObject scheduleJson = new JSONObject(feedback);
        JSONObject scheduleJson2 = scheduleJson.getJSONObject("results");
        JSONArray jsonArray = scheduleJson2.getJSONArray("Data");
        g1 = new ArrayList<>();
        g2 = new ArrayList<>();
        g3 = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray jsonArray1 = jsonArray.getJSONArray(i);
            if (jsonArray1.getInt(2) <= 20) {
                switch (jsonArray1.getInt(4)) {
                    case 1:
                        g1.add(jsonArray1.getString(3));
                        break;
                    case 2:
                        g2.add(jsonArray1.getString(3));
                        break;
                    case 3:
                        g3.add(jsonArray1.getString(3));
                        break;
                }
            }
        }

        System.out.println(g1);
        System.out.println(g2);
        System.out.println(g3);

        System.out.println(getClassSchedule(g1.get(0)));

    }

    public static String getClassSchedule(String grade_id){
        JSONObject getSchJson = new JSONObject();
        JSONObject cond_json3 = new JSONObject();
        JSONObject page = new JSONObject();
        cond_json3.put("school_year",year);
        cond_json3.put("school_term",term);
        cond_json3.put("user_id",userid);
        cond_json3.put("grade_id",grade_id);
        cond_json3.put("gaokao_major","");
        cond_json3.put("school_calendar_id","446");
        page.put("limit","100");
        page.put("pageIdx","1");

        getSchJson.put("cmd","get_school_courses_schedule");
        getSchJson.put("key",key);
        getSchJson.put("sid",sid);
        getSchJson.put("cond",cond_json3);
        getSchJson.put("page",page);

        String gcsReq = xzdEncrypt.sign(getSchJson);
        String feedback = sendPost("http://m.fzsz.net/api_abc/api-s1.php",gcsReq);
        System.out.println(feedback);
        JSONObject feedbackJson = new JSONObject(feedback);
        if (feedbackJson.getInt("code") != 0) {
            return "null";
        }
        JSONObject resultJson = feedbackJson.getJSONObject("results");
        JSONArray dataJsonArray = resultJson.getJSONArray("Data");


        return dataJsonArray.toString();
    }

    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
//            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "bh3_scanner");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            Logger.getLogger("test").info("sendPost: Failed.");
            e.printStackTrace();
            return null;
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result.toString();
    }
}
