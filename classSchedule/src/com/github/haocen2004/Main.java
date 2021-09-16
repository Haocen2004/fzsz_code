package com.github.haocen2004;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class Main {
    private static String pw, ac, epw, req, key, sid, name, userid, term, year, feedback, scid;
    private static List<String> g1, g2, g3, major;
    private static List<String> tempList2 = new ArrayList<>();
    private static JSONArray tempList = new JSONArray();


    public static void main(String[] args) {
        genSCList(args);
    }

    public static void genSCList(String[] args) {
        if (args.length == 0) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Account: ");
            ac = scanner.next();
            System.out.print("Password：");
            pw = scanner.next();
            System.out.println("======================");
            System.out.println("Account: " + ac);
            System.out.println("Password: " + pw);
            epw = xzdEncrypt.encrypt(pw);
            System.out.println("Encrypted Password: " + epw);
            System.out.println("======================");


            JSONObject req_json = new JSONObject();
            JSONObject cond_json = new JSONObject();
            cond_json.put("USERNAME", ac);
            cond_json.put("PASSWORD", epw);
            cond_json.put("imei", "");
            cond_json.put("imsi", "");
            cond_json.put("iccid", "");
            cond_json.put("mac", "020000000000");
            req_json.put("request_type", "mobile_login_v2");
            req_json.put("cond", cond_json);
            System.out.println(cond_json.toString());
            System.out.println(req_json.toString());


            req = xzdEncrypt.sign(req_json);
            boolean noResp = true;
            JSONObject account_json = new JSONObject();
            while (noResp) {
                feedback = sendPost("http://m.fzsz.net/api_abc/api-s1.php", req);
                System.out.println(feedback);
                try {
                    account_json = new JSONObject(feedback);

                    noResp = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (account_json.getInt("code") == 0) {
                key = account_json.getString("key");
                sid = account_json.getString("sid");
                name = account_json.getString("name");
                userid = account_json.getString("userId");
                year = account_json.getString("cur_school_year");
                term = account_json.getString("cur_school_term");
                System.out.println("======================");
                System.out.println("Login Succeed");
                System.out.println("User Name: " + name);
                System.out.println("User Id: " + userid);
                System.out.println("Key: " + key);
                System.out.println("Sid: " + sid);
                System.out.println("======================");

            } else {
                System.out.println("======================");
                System.out.println("Login Failed");
                System.out.println("======================");
                return;
            }
        } else {
            // filled with cache data
            name = "";
            userid = "";
            key = "";
            sid = "";
            year = "2021";
            term = "1";
            System.out.println("======================");
            System.out.println("Load From Cache");
            System.out.println("User Name: " + name);
            System.out.println("User Id: " + userid);
            System.out.println("Key: " + key);
            System.out.println("Sid: " + sid);
            System.out.println("======================");
        }
        JSONObject getWeekJson = new JSONObject();
        JSONObject getClassJson = new JSONObject();
        JSONObject cond_json2 = new JSONObject();
        cond_json2.put("school_year", year);
        cond_json2.put("school_term", term);
        getWeekJson.put("cmd","get_school_week_list");
        getWeekJson.put("cond",cond_json2);
        getWeekJson.put("key",key);
        getWeekJson.put("sid",sid);
        cond_json2.put("user_id", userid);
        getClassJson.put("cmd", "get_user_class");
        getClassJson.put("key", key); 
        getClassJson.put("sid", sid);
        getClassJson.put("cond", cond_json2);

        //get_school_week_list school_calendar_id
        req = xzdEncrypt.sign(getWeekJson);
        feedback = sendPost("http://m.fzsz.net/api_abc/api-s1.php", req);
        System.out.println(feedback);
        JSONObject weekJson = new JSONObject(feedback);
        JSONArray weekListArray = weekJson.getJSONArray("results");
        for (int i = 0; i < weekListArray.length(); i++) {
            if (weekListArray.getJSONObject(i).getString("now_week").equals("1")) {
                scid = weekListArray.getJSONObject(i).getString("school_calendar_id");
                System.out.print("school_calendar_id: " + scid);
                break;
            }
        }

        //get_all_class
        req = xzdEncrypt.sign(getClassJson);

        feedback = sendPost("http://m.fzsz.net/api_abc/api-s1.php", req);
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
                        g1.add(jsonArray1.getString(0));
                        break;
                    case 2:
                        g2.add(jsonArray1.getString(0));
                        break;
                    case 3:
                        g3.add(jsonArray1.getString(0));
                        break;
                }
            }
        }
        major = new ArrayList<>();
        StringBuilder s = new StringBuilder();
        s.append("var courseList = [[");
//        System.out.println(getClassSchedule(g2.get(4)));
        s.append("\n/*高一*/\n");
        for (String grade_id : g1) {
            String info = getClassSchedule(grade_id);
            if (info.equals("")) {
                System.out.println("Error: getClassSchedule " + grade_id);
            } else {
                s.append("\n").append(info).append(",");
            }
        }
        s.delete(s.length() - 1, s.length()).append("],\n\n[");
        major.add(tempList2.toString());
        tempList2.clear();
        s.append("\n/*高二*/\n");
        for (String grade_id : g2) {
            String info = getClassSchedule(grade_id);
            if (info.equals("")) {
                System.out.println("Error: getClassSchedule " + grade_id);
            } else {
                s.append("\n").append(info).append(",");
            }
        }
        s.delete(s.length() - 1, s.length()).append("],\n\n[");
        major.add(tempList2.toString());
        tempList2.clear();
        s.append("\n/*高三*/\n");
        for (String grade_id : g3) {
            String info = getClassSchedule(grade_id);
            if (info.equals("")) {
                System.out.println("Error: getClassSchedule " + grade_id);
            } else {
                s.append("\n").append(info).append(",");
            }
        }
        s.delete(s.length() - 1, s.length()).append("]];");
        major.add(tempList2.toString());
        tempList2.clear();
        System.out.println("\n\n\nFinal data.js Output:\n\n");
        System.out.println("var gaoKaoMajorList ="+major);
        System.out.println(s.toString());

    }

    public static String getClassSchedule(String grade_id) {
        tempList = new JSONArray();

        JSONObject getSchJson = new JSONObject();
        JSONObject getGaokaoJson = new JSONObject();
        JSONObject cond_json3 = new JSONObject();
        JSONObject page = new JSONObject();
        String feedback = null;
        cond_json3.put("school_year", year);
        cond_json3.put("school_term", term);
        cond_json3.put("grade_id", grade_id);

        getGaokaoJson.put("cmd", "get_class_gaokao_major");
        getGaokaoJson.put("cond", cond_json3);
        getGaokaoJson.put("key", key);
        getGaokaoJson.put("sid", sid);
        String gcsReq = xzdEncrypt.sign(getGaokaoJson);
        boolean b = true;
        while (b) {
            try {
                System.out.println("Getting Gao Kao Major..." + grade_id);
                feedback = sendPost("http://m.fzsz.net/api_abc/api-s1.php", gcsReq);
                System.out.println(feedback);
                b = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JSONObject gaoKaoJson = new JSONObject(feedback);
        String major = "";
        if (gaoKaoJson.getInt("rows_affected") <= 1) {
            if (gaoKaoJson.getInt("rows_affected") == 1) {
                major = gaoKaoJson.getJSONArray("results").getJSONObject(0).getString("gaokao_major");
            }
            cond_json3.put("user_id", "0");
            cond_json3.put("gaokao_major", major);
            cond_json3.put("school_calendar_id", scid);
            page.put("limit", "100");
            page.put("pageIdx", "1");

            getSchJson.put("cmd", "get_school_courses_schedule");
            getSchJson.put("page", page);
            getSchJson.put("cond", cond_json3);
            getSchJson.put("key", key);
            getSchJson.put("sid", sid);
            gcsReq = xzdEncrypt.sign(getSchJson);
            b = true;

            while (b) {
                try {
                    System.out.println("Getting Class Schedule..." + grade_id);
                    feedback = sendPost("http://m.fzsz.net/api_abc/api-s1.php", gcsReq);
                    b = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            tempList.put("");
            tempList2.add(tempList.toString());

            return parseSingleSchedule(feedback);
        }
        List<String> allSchedules = new ArrayList<String>();
        for (int i = 0; i < gaoKaoJson.getJSONArray("results").length(); i++) {
            try {
                major = gaoKaoJson.getJSONArray("results").getJSONObject(i).getString("gaokao_major");
                tempList.put(major);
            } catch (Exception e) {
                major = "";
            }
            cond_json3.put("user_id", "0");
            cond_json3.put("gaokao_major", major);
            cond_json3.put("school_calendar_id", scid);
            page.put("limit", "100");
            page.put("pageIdx", "1");

            getSchJson.put("cmd", "get_school_courses_schedule");
            getSchJson.put("page", page);
            getSchJson.put("cond", cond_json3);
            getSchJson.put("key", key);
            getSchJson.put("sid", sid);
            gcsReq = xzdEncrypt.sign(getSchJson);
            b = true;

            while (b) {
                try {
                    System.out.println("Getting Class Schedule..." + grade_id);
                    feedback = sendPost("http://m.fzsz.net/api_abc/api-s1.php", gcsReq);
                    b = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            allSchedules.add(parseSingleSchedule(feedback));

        }
        tempList2.add(tempList.toString());

        return allSchedules.toString();


    }

    public static String parseSingleSchedule(String feedback) {
        JSONObject feedbackJson = new JSONObject(feedback);
        if (feedbackJson.getInt("code") != 0) {
            return "null";
        }
        JSONArray dataJsonArray;
        JSONObject resultJson;
        HashMap<String, String> d1 = new HashMap<>(), d2 = new HashMap<>(), d3 = new HashMap<>(), d4 = new HashMap<>(), d5 = new HashMap<>();
        try {
            resultJson = feedbackJson.getJSONObject("results");
            dataJsonArray = resultJson.getJSONArray("Data");
            for (int i = 0; i < dataJsonArray.length(); i++) {
                JSONArray dailyJsonArray = dataJsonArray.getJSONArray(i);
                for (int j = 0; j < dailyJsonArray.length(); j++) {
                    switch (dailyJsonArray.getString(3)) {
                        case "1":
                            d1.put(dailyJsonArray.getString(5), dailyJsonArray.getString(7));
                            break;
                        case "2":
                            d2.put(dailyJsonArray.getString(5), dailyJsonArray.getString(7));
                            break;
                        case "3":
                            d3.put(dailyJsonArray.getString(5), dailyJsonArray.getString(7));
                            break;
                        case "4":
                            d4.put(dailyJsonArray.getString(5), dailyJsonArray.getString(7));
                            break;
                        case "5":
                            d5.put(dailyJsonArray.getString(5), dailyJsonArray.getString(7));
                            break;
                    }
                }
            }
        } catch (Exception ignore) {
            return "";
        }

        if (d1.size() < 8) {
            for (int i = 0; i < (8 - d1.size()); i++) {
                d1.put("20", "学科拓展课程");
            }
        }
        if (d2.size() < 8) {
            for (int i = 0; i < (8 - d2.size()); i++) {
                d2.put("20", "学科拓展课程");
            }
        }
        if (d3.size() < 8) {
            for (int i = 0; i < (8 - d3.size()); i++) {
                d3.put("20", "学科拓展课程");
            }
        }
        if (d4.size() < 8) {
            for (int i = 0; i < (8 - d4.size()); i++) {
                d4.put("20", "学科拓展课程");
            }
        }
        if (d5.size() < 8) {
            for (int i = 0; i < (8 - d5.size()); i++) {
                d5.put("20", "学科拓展课程");
            }
        }
        StringBuilder result = new StringBuilder();
        result.append("[[");
        for (String className : d1.values()) {
            result.append("\"").append(className).append("\",");
        }
        result.delete(result.length() - 1, result.length()).append("],");
        result.append("[");
        for (String className : d2.values()) {
            result.append("\"").append(className).append("\",");
        }
        result.delete(result.length() - 1, result.length()).append("],");
        result.append("[");
        for (String className : d3.values()) {
            result.append("\"").append(className).append("\",");
        }
        result.delete(result.length() - 1, result.length()).append("],");
        result.append("[");
        for (String className : d4.values()) {
            result.append("\"").append(className).append("\",");
        }
        result.delete(result.length() - 1, result.length()).append("],");
        result.append("[");
        for (String className : d5.values()) {
            result.append("\"").append(className).append("\",");
        }
        result.delete(result.length() - 1, result.length()).append("]]");
        System.out.println(result.toString());
        return result.toString();
    }

    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        ByteArrayOutputStream gout = new ByteArrayOutputStream();
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
//            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestMethod("POST");
//            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "okhttp/3.14.1");
            conn.setRequestProperty("Accept-Encoding", "gzip");
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
            try {
                GZIPInputStream ungzip = new GZIPInputStream(conn.getInputStream());
                byte[] buffer = new byte[256];
                int n;
                while ((n = ungzip.read(buffer)) >= 0) {
                    gout.write(buffer, 0, n);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Logger.getLogger("test").info("sendPost: Failed.");
            e.printStackTrace();
            return null;
        }

        finally {
            if (out != null) {
                out.close();
            }
        }
        return gout.toString();
    }
}
