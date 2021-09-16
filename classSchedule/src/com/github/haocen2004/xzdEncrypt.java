package com.github.haocen2004;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class xzdEncrypt {

    private static String pw, ac, epw, req, key, sid, name, userid, term, year, feedback;
    private static List<String> g1, g2, g3;

    public static void main(String[] args) {
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

            feedback = sendPost("http://m.fzsz.net/api_abc/api-s1.php", req);
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
            name = "刘会坚";
            userid = "3674";
            key = "46da4060648b0555af03d74521376b11";
            sid = "F43E7ABA5723906B0011CB40DE5DB424";
            year = "2020";
            term = "2";
            System.out.println("======================");
            System.out.println("Load From Cache");
            System.out.println("User Name: " + name);
            System.out.println("User Id: " + userid);
            System.out.println("Key: " + key);
            System.out.println("Sid: " + sid);
            System.out.println("======================");
        }
        JSONObject getClassJson = new JSONObject();
        JSONObject cond_json2 = new JSONObject();
        cond_json2.put("school_year", year);
        cond_json2.put("school_term", term);
        cond_json2.put("user_id", userid);
        getClassJson.put("cmd", "get_user_class");
        getClassJson.put("key", key);
        getClassJson.put("sid", sid);
        getClassJson.put("cond", cond_json2);

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
        StringBuilder s = new StringBuilder();
        s.append("var courseList = [[");
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


        System.out.println("Final:" + s.toString());
    }

    public static String getClassSchedule(String grade_id) {


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
                b = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JSONObject gaoKaoJson = new JSONObject(feedback);
        String major = "";
        try {
            major = gaoKaoJson.getJSONArray("results").getJSONObject(0).getString("gaokao_major");
        } catch (Exception e) {
            major = "";
        }
        cond_json3.put("user_id", "0");
        cond_json3.put("gaokao_major", major);
        cond_json3.put("school_calendar_id", "446");
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
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "okhttp/3.14.1");
            conn.setRequestProperty("Accept-Encoding", "gzip");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
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
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return gout.toString();
    }

    private static String s1;

    private static String s2;

    private static int getInt(String paramString) {
        byte[] arrayOfByte = paramString.getBytes();
        int i = arrayOfByte.length;
        byte b = 0;
        int j = 0;
        while (b < i) {
            j += arrayOfByte[b];
            b++;
        }
        return j;
    }

    public static String sign(JSONObject req_json) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String str1 = simpleDateFormat.format(new Date());
            StringBuilder stringBuilder1 = new StringBuilder();
            stringBuilder1.append("request=");
            stringBuilder1.append(req_json.toString());
            String str3 = stringBuilder1.toString();
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("&time=");
            stringBuilder3.append(str1);
            String str4 = stringBuilder3.toString();
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append(str3);
            stringBuilder4.append(str4);
            byte[] arrayOfByte = xzdEncrypt.m.a(URLEncoder.encode(stringBuilder4.toString(), "UTF-8").replace("+", "%20"), "Abc^EagLe#SchooL@UnionCore%2014");
            String str2 = URLEncoder.encode((new xzdEncrypt.b()).a(arrayOfByte), "UTF-8");
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("request=");
            stringBuilder2.append(URLEncoder.encode(req_json.toString(), "UTF-8"));
            stringBuilder2.append("&time=");
            stringBuilder2.append(URLEncoder.encode(str1, "UTF-8"));
            stringBuilder2.append("&sign=");
            stringBuilder2.append(str2);
            return stringBuilder2.toString();
        } catch (Exception exception) {
            exception.printStackTrace();
            return "";
        }
    }

    public static String encrypt(String paramString) {
        int i = getInt(paramString);
        int j = i % 4;
        i %= 5;
        if (i != 0) {
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        if (i == 4)
                            s1 = "Conquestpriggishness";
                    } else {
                        s1 = "cephushandclasp";
                    }
                } else {
                    s1 = "fearsomeTogo";
                }
            } else {
                s1 = "Slubbingbilly";
            }
        } else {
            s1 = "Dawdlerubefacient";
        }
        if (j != 0) {
            if (j != 1) {
                if (j != 2) {
                    if (j == 3)
                        s2 = "Chivieddulles";
                } else {
                    s2 = "cartelhorsebarn";
                }
            } else {
                s2 = "GyroseAcclamation";
            }
        } else {
            s2 = "earnestnessClovehook";
        }
        String str = eString(s1);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(str);
        stringBuilder.append((new z()).b(paramString.getBytes()));
        paramString = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(paramString);
        stringBuilder.append(a(s2));
        paramString = stringBuilder.toString();
        return (new z()).b(paramString.getBytes());
    }

    public static String eString(String paramString) {
        char[] arrayOfChar = new char[16];
        arrayOfChar[0] = '0';
        arrayOfChar[1] = '1';
        arrayOfChar[2] = '2';
        arrayOfChar[3] = '3';
        arrayOfChar[4] = '4';
        arrayOfChar[5] = '5';
        arrayOfChar[6] = '6';
        arrayOfChar[7] = '7';
        arrayOfChar[8] = '8';
        arrayOfChar[9] = '9';
        arrayOfChar[10] = 'a';
        arrayOfChar[11] = 'b';
        arrayOfChar[12] = 'c';
        arrayOfChar[13] = 'd';
        arrayOfChar[14] = 'e';
        arrayOfChar[15] = 'f';
        try {
            byte[] arrayOfByte2 = paramString.getBytes();
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(arrayOfByte2);
            byte[] arrayOfByte1 = messageDigest.digest();
            char[] arrayOfChar1 = new char[arrayOfByte1.length * 2];
            int i = arrayOfByte1.length;
            byte b = 0;
            int j = 0;
            while (b < i) {
                byte b1 = arrayOfByte1[b];
                int k = j + 1;
                arrayOfChar1[j] = arrayOfChar[b1 >>> 4 & 0xF];
                j = k + 1;
                arrayOfChar1[k] = arrayOfChar[b1 & 0xF];
                b++;
            }
            return new String(arrayOfChar1);
        } catch (Exception exception) {
            return null;
        }
    }

    private static int a;

    private static int b;

    private static int c;

    private static int d;

    private static final int[] e = new int[16];

    private static final char[] f = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};

    private static int a(int paramInt1, int paramInt2) {
        return (paramInt2 == 0) ? paramInt1 : (paramInt1 >> 32 - paramInt2 & Integer.MAX_VALUE >> 31 - paramInt2 | paramInt1 << paramInt2);
    }

    private static int a(int paramInt1, int paramInt2, int paramInt3) {
        return (~paramInt1) & paramInt3 | paramInt2 & paramInt1;
    }

    private static int a(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6) {
        return a(paramInt1 + a(paramInt2, paramInt3, paramInt4) + e[paramInt5], paramInt6);
    }

    static String a(String paramString) {
        return b(a(paramString.getBytes()));
    }

    private static String a(byte[] paramArrayOfbyte, int paramInt) {
        char[] arrayOfChar = new char[paramInt * 2];
        int i = 0;
        int j = 0;
        while (i < paramInt) {
            byte b = paramArrayOfbyte[i];
            int k = j + 1;
            char[] arrayOfChar1 = f;
            arrayOfChar[j] = arrayOfChar1[b >>> 4 & 0xF];
            j = k + 1;
            arrayOfChar[k] = arrayOfChar1[b & 0xF];
            i++;
        }
        return new String(arrayOfChar);
    }

    private static void a(byte[] paramArrayOfbyte, int paramInt1, int paramInt2) {
        paramArrayOfbyte[paramInt1] = (byte) (paramInt2 & 0xFF);
        paramArrayOfbyte[paramInt1 + 1] = (byte) (paramInt2 >> 8 & 0xFF);
        paramArrayOfbyte[paramInt1 + 2] = (byte) (paramInt2 >> 16 & 0xFF);
        paramArrayOfbyte[paramInt1 + 3] = (byte) (paramInt2 >> 24 & 0xFF);
    }

    private static void a(int[] paramArrayOfint) {
        int i;
        for (i = 0; i < 16; i++)
            e[i] = paramArrayOfint[i];
        int m = a;
        int k = b;
        int j = c;
        i = d;
        int n = a(m, k, j, i, 0, 3);
        a = n;
        n = a(d, n, b, c, 1, 7);
        d = n;
        n = a(c, n, a, b, 2, 11);
        c = n;
        n = a(b, n, d, a, 3, 19);
        b = n;
        n = a(a, n, c, d, 4, 3);
        a = n;
        n = a(d, n, b, c, 5, 7);
        d = n;
        n = a(c, n, a, b, 6, 11);
        c = n;
        n = a(b, n, d, a, 7, 19);
        b = n;
        n = a(a, n, c, d, 8, 3);
        a = n;
        n = a(d, n, b, c, 9, 7);
        d = n;
        n = a(c, n, a, b, 10, 11);
        c = n;
        n = a(b, n, d, a, 11, 19);
        b = n;
        n = a(a, n, c, d, 12, 3);
        a = n;
        n = a(d, n, b, c, 13, 7);
        d = n;
        n = a(c, n, a, b, 14, 11);
        c = n;
        n = a(b, n, d, a, 15, 19);
        b = n;
        n = b(a, n, c, d, 0, 3);
        a = n;
        n = b(d, n, b, c, 4, 5);
        d = n;
        n = b(c, n, a, b, 8, 9);
        c = n;
        n = b(b, n, d, a, 12, 13);
        b = n;
        n = b(a, n, c, d, 1, 3);
        a = n;
        n = b(d, n, b, c, 5, 5);
        d = n;
        n = b(c, n, a, b, 9, 9);
        c = n;
        n = b(b, n, d, a, 13, 13);
        b = n;
        n = b(a, n, c, d, 2, 3);
        a = n;
        n = b(d, n, b, c, 6, 5);
        d = n;
        n = b(c, n, a, b, 10, 9);
        c = n;
        n = b(b, n, d, a, 14, 13);
        b = n;
        n = b(a, n, c, d, 3, 3);
        a = n;
        n = b(d, n, b, c, 7, 5);
        d = n;
        n = b(c, n, a, b, 11, 9);
        c = n;
        n = b(b, n, d, a, 15, 13);
        b = n;
        n = c(a, n, c, d, 0, 3);
        a = n;
        n = c(d, n, b, c, 8, 9);
        d = n;
        n = c(c, n, a, b, 4, 11);
        c = n;
        n = c(b, n, d, a, 12, 15);
        b = n;
        n = c(a, n, c, d, 2, 3);
        a = n;
        n = c(d, n, b, c, 10, 9);
        d = n;
        n = c(c, n, a, b, 6, 11);
        c = n;
        n = c(b, n, d, a, 14, 15);
        b = n;
        n = c(a, n, c, d, 1, 3);
        a = n;
        n = c(d, n, b, c, 9, 9);
        d = n;
        n = c(c, n, a, b, 5, 11);
        c = n;
        n = c(b, n, d, a, 13, 15);
        b = n;
        n = c(a, n, c, d, 3, 3);
        a = n;
        n = c(d, n, b, c, 11, 9);
        d = n;
        n = c(c, n, a, b, 7, 11);
        c = n;
        n = c(b, n, d, a, 15, 15);
        b = n;
        m = a + m;
        a = m;
        k = n + k;
        b = k;
        j = c + j;
        c = j;
        i = d + i;
        d = i;
    }

    private static void a(int[] paramArrayOfint, byte[] paramArrayOfbyte) {
        a(paramArrayOfint, paramArrayOfbyte, 0);
    }

    private static void a(int[] paramArrayOfint, byte[] paramArrayOfbyte, int paramInt) {
        for (int i = 0; i < 16; i++) {
            int j = i * 4 + paramInt;
            byte b = paramArrayOfbyte[j + 3];
            paramArrayOfint[i] = paramArrayOfbyte[j + 2] << 16 & 0xFF0000 | b << 24 & 0xFF000000 | paramArrayOfbyte[j + 1] << 8 & 0xFF00 | paramArrayOfbyte[j] & 0xFF;
        }
    }

    private static byte[] a(byte[] paramArrayOfbyte) {
        byte[] arrayOfByte1 = new byte[16];
        byte[] arrayOfByte2 = new byte[128];
        int i = paramArrayOfbyte.length;
        int[] arrayOfInt = new int[16];
        int m = i * 8;
        a = 1732584193;
        b = -271733879;
        c = -1732584194;
        d = 271733878;
        int j = 0;
        while (i > 64) {
            a(arrayOfInt, paramArrayOfbyte, j);
            a(arrayOfInt);
            j += 64;
            i -= 64;
        }
        int k;
        for (k = 0; k < 128; k++) {
//            boolean bool;
            int n = k + j;
            if (n < paramArrayOfbyte.length) {
                n = paramArrayOfbyte[n];
            } else {
                n = 0;
            }
            arrayOfByte2[k] = (byte) n;
        }
        arrayOfByte2[i] = Byte.MIN_VALUE;
        if (i <= 55) {
            a(arrayOfByte2, 56, m);
            a(arrayOfInt, arrayOfByte2);
            a(arrayOfInt);
        } else {
            a(arrayOfByte2, 120, m);
            a(arrayOfInt, arrayOfByte2);
            a(arrayOfInt);
            a(arrayOfInt, arrayOfByte2, 64);
            a(arrayOfInt);
        }
        for (i = 0; i < 128; i++)
            arrayOfByte2[i] = 0;
        a(arrayOfInt, arrayOfByte2);
        a(arrayOfByte1, 0, a);
        a(arrayOfByte1, 4, b);
        a(arrayOfByte1, 8, c);
        a(arrayOfByte1, 12, d);
        d = 0;
        c = 0;
        b = 0;
        a = 0;
        return arrayOfByte1;
    }

    private static int b(int paramInt1, int paramInt2, int paramInt3) {
        return paramInt1 & paramInt3 | paramInt1 & paramInt2 | paramInt2 & paramInt3;
    }

    private static int b(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6) {
        return a(paramInt1 + b(paramInt2, paramInt3, paramInt4) + e[paramInt5] + 1518500249, paramInt6);
    }

    private static String b(byte[] paramArrayOfbyte) {
        return a(paramArrayOfbyte, paramArrayOfbyte.length);
    }

    private static int c(int paramInt1, int paramInt2, int paramInt3) {
        return paramInt1 ^ paramInt2 ^ paramInt3;
    }

    private static int c(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6) {
        return a(paramInt1 + c(paramInt2, paramInt3, paramInt4) + e[paramInt5] + 1859775393, paramInt6);
    }

    static class z {

        private final int[] a = new int[]{1732584193, -271733879, -1732584194, 271733878, -1009589776};

        private final int[] b = new int[5];

        private final int[] c = new int[80];

        private int a(int paramInt1, int paramInt2) {
            return paramInt1 >>> 32 - paramInt2 | paramInt1 << paramInt2;
        }

        private int a(int paramInt1, int paramInt2, int paramInt3) {
            return (~paramInt1) & paramInt3 | paramInt2 & paramInt1;
        }

        private int a(byte[] paramArrayOfbyte, int paramInt) {
            byte b1 = paramArrayOfbyte[paramInt];
            byte b2 = paramArrayOfbyte[paramInt + 1];
            byte b3 = paramArrayOfbyte[paramInt + 2];
            return paramArrayOfbyte[paramInt + 3] & 0xFF | (b1 & 0xFF) << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8;
        }

        private static String a(byte paramByte) {
            char[] arrayOfChar = new char[16];
            arrayOfChar[0] = '0';
            arrayOfChar[1] = '1';
            arrayOfChar[2] = '2';
            arrayOfChar[3] = '3';
            arrayOfChar[4] = '4';
            arrayOfChar[5] = '5';
            arrayOfChar[6] = '6';
            arrayOfChar[7] = '7';
            arrayOfChar[8] = '8';
            arrayOfChar[9] = '9';
            arrayOfChar[10] = 'a';
            arrayOfChar[11] = 'b';
            arrayOfChar[12] = 'c';
            arrayOfChar[13] = 'd';
            arrayOfChar[14] = 'e';
            arrayOfChar[15] = 'f';
            return new String(new char[]{arrayOfChar[paramByte >>> 4 & 0xF], arrayOfChar[paramByte & 0xF]});
        }

        private void a() {
            byte b;
            for (b = 16; b <= 79; b++) {
                int[] arrayOfInt1 = this.c;
                arrayOfInt1[b] = a(arrayOfInt1[b - 3] ^ arrayOfInt1[b - 8] ^ arrayOfInt1[b - 14] ^ arrayOfInt1[b - 16], 1);
            }
            int[] arrayOfInt = new int[5];
            System.arraycopy(this.b, 0, arrayOfInt, 0, 5);
            for (b = 0; b <= 19; b++) {
                int i = a(arrayOfInt[0], 5);
                int j = a(arrayOfInt[1], arrayOfInt[2], arrayOfInt[3]);
                int k = arrayOfInt[4];
                int m = this.c[b];
                arrayOfInt[4] = arrayOfInt[3];
                arrayOfInt[3] = arrayOfInt[2];
                arrayOfInt[2] = a(arrayOfInt[1], 30);
                arrayOfInt[1] = arrayOfInt[0];
                arrayOfInt[0] = i + j + k + m + 1518500249;
            }
            for (b = 20; b <= 39; b++) {
                int m = a(arrayOfInt[0], 5);
                int k = b(arrayOfInt[1], arrayOfInt[2], arrayOfInt[3]);
                int i = arrayOfInt[4];
                int j = this.c[b];
                arrayOfInt[4] = arrayOfInt[3];
                arrayOfInt[3] = arrayOfInt[2];
                arrayOfInt[2] = a(arrayOfInt[1], 30);
                arrayOfInt[1] = arrayOfInt[0];
                arrayOfInt[0] = m + k + i + j + 1859775393;
            }
            for (b = 40; b <= 59; b++) {
                int i = a(arrayOfInt[0], 5);
                int m = c(arrayOfInt[1], arrayOfInt[2], arrayOfInt[3]);
                int k = arrayOfInt[4];
                int j = this.c[b];
                arrayOfInt[4] = arrayOfInt[3];
                arrayOfInt[3] = arrayOfInt[2];
                arrayOfInt[2] = a(arrayOfInt[1], 30);
                arrayOfInt[1] = arrayOfInt[0];
                arrayOfInt[0] = i + m + k + j - 1894007588;
            }
            for (b = 60; b <= 79; b++) {
                int m = a(arrayOfInt[0], 5);
                int j = b(arrayOfInt[1], arrayOfInt[2], arrayOfInt[3]);
                int i = arrayOfInt[4];
                int k = this.c[b];
                arrayOfInt[4] = arrayOfInt[3];
                arrayOfInt[3] = arrayOfInt[2];
                arrayOfInt[2] = a(arrayOfInt[1], 30);
                arrayOfInt[1] = arrayOfInt[0];
                arrayOfInt[0] = m + j + i + k - 899497514;
            }
            for (b = 0; b < 5; b++) {
                int[] arrayOfInt1 = this.b;
                arrayOfInt1[b] = arrayOfInt1[b] + arrayOfInt[b];
            }
            b = 0;
            while (true) {
                arrayOfInt = this.c;
                if (b < arrayOfInt.length) {
                    arrayOfInt[b] = 0;
                    b++;
                    continue;
                }
                break;
            }
        }

        private void a(int paramInt1, byte[] paramArrayOfbyte, int paramInt2) {
            paramArrayOfbyte[paramInt2] = (byte) (byte) (paramInt1 >>> 24);
            paramArrayOfbyte[paramInt2 + 1] = (byte) (byte) (paramInt1 >>> 16);
            paramArrayOfbyte[paramInt2 + 2] = (byte) (byte) (paramInt1 >>> 8);
            paramArrayOfbyte[paramInt2 + 3] = (byte) (byte) paramInt1;
        }

        private int b(int paramInt1, int paramInt2, int paramInt3) {
            return paramInt1 ^ paramInt2 ^ paramInt3;
        }

        private int c(int paramInt1, int paramInt2, int paramInt3) {
            return paramInt1 & paramInt3 | paramInt1 & paramInt2 | paramInt2 & paramInt3;
        }

        private byte[] c(final byte[] array) {
            final int length = array.length;
            final int n = length % 64;
            int n2 = 63;
            int n4;
            int n5;
            Label_0082:
            {
                int n3;
                if (n < 56) {
                    n2 = 55 - n;
                    n3 = length - n;
                } else {
                    if (n != 56) {
                        n4 = 63 - n + 56;
                        n5 = length + 64 - n + 64;
                        break Label_0082;
                    }
                    n3 = length + 8;
                }
                n5 = n3 + 64;
                n4 = n2;
            }
            final byte[] array2 = new byte[n5];
            System.arraycopy(array, 0, array2, 0, length);
            int n6 = length + 1;
            array2[length] = -128;
            for (int i = 0; i < n4; ++i, ++n6) {
                array2[n6] = 0;
            }
            final long n7 = length * 8L;
            final byte b = (byte) (n7 & 0xFFL);
            final byte b2 = (byte) (n7 >> 8 & 0xFFL);
            final byte b3 = (byte) (n7 >> 16 & 0xFFL);
            final byte b4 = (byte) (n7 >> 24 & 0xFFL);
            final byte b5 = (byte) (n7 >> 32 & 0xFFL);
            final byte b6 = (byte) (0L);
            final byte b7 = (byte) (0);
            final byte b8 = (byte) (0);
            final int n8 = n6 + 1;
            array2[n6] = b8;
            final int n9 = n8 + 1;
            array2[n8] = b7;
            final int n10 = n9 + 1;
            array2[n9] = b6;
            final int n11 = n10 + 1;
            array2[n10] = b5;
            final int n12 = n11 + 1;
            array2[n11] = b4;
            final int n13 = n12 + 1;
            array2[n12] = b3;
            array2[n13] = b2;
            array2[n13 + 1] = b;
            return array2;
        }

        private static String d(byte[] paramArrayOfbyte) {
            StringBuilder stringBuilder = new StringBuilder();
            for (byte value : paramArrayOfbyte) stringBuilder.append(a(value));
            return stringBuilder.toString();
        }

        private void e(byte[] paramArrayOfbyte) {
            int[] arrayOfInt = this.a;
            System.arraycopy(arrayOfInt, 0, this.b, 0, arrayOfInt.length);
            paramArrayOfbyte = c(paramArrayOfbyte);
            int i = paramArrayOfbyte.length / 64;
            for (byte b = 0; b < i; b++) {
                for (byte b1 = 0; b1 < 16; b1++)
                    this.c[b1] = a(paramArrayOfbyte, b * 64 + b1 * 4);
                a();
            }
        }

        byte[] a(byte[] paramArrayOfbyte) {
            e(paramArrayOfbyte);
            paramArrayOfbyte = new byte[20];
            byte b = 0;
            while (true) {
                int[] arrayOfInt = this.b;
                if (b < arrayOfInt.length) {
                    a(arrayOfInt[b], paramArrayOfbyte, b * 4);
                    b++;
                    continue;
                }
                return paramArrayOfbyte;
            }
        }

        String b(byte[] paramArrayOfbyte) {
            return d(a(paramArrayOfbyte));
        }
    }

    static class m {
        public static byte[] a(final String s, final String s2) {
            final byte[] array = new byte[64];
            final byte[] array2 = new byte[64];
            final byte[] array3 = new byte[64];
            int n = s2.length();
            final z z = new z();
            final int length = s2.length();
            final int n2 = 0;
            if (length > 64) {
                final byte[] a = z.a(s2.getBytes());
                n = a.length;
                System.arraycopy(a, 0, array3, 0, n);
            } else {
                final byte[] bytes = s2.getBytes();
                System.arraycopy(bytes, 0, array3, 0, bytes.length);
            }
            int i;
            while (true) {
                i = n2;
                if (n >= 64) {
                    break;
                }
                array3[n] = 0;
                ++n;
            }
            while (i < 64) {
                array[i] = (byte) (array3[i] ^ 0x36);
                array2[i] = (byte) (array3[i] ^ 0x5C);
                ++i;
            }
            return z.a(a(array2, z.a(a(array, s.getBytes()))));
        }

        private static byte[] a(byte[] paramArrayOfbyte1, byte[] paramArrayOfbyte2) {
            byte[] arrayOfByte = new byte[paramArrayOfbyte1.length + paramArrayOfbyte2.length];
            System.arraycopy(paramArrayOfbyte1, 0, arrayOfByte, 0, paramArrayOfbyte1.length);
            System.arraycopy(paramArrayOfbyte2, 0, arrayOfByte, paramArrayOfbyte1.length, paramArrayOfbyte2.length);
            return arrayOfByte;
        }
    }

    static class b {
        private static final char a;
        private static final char b;
        private static final char c;
        private static final char d;
        private static final char e;
        private static final char f;
        private static final char[] g;

        static {
            a = (char) Integer.parseInt("00000011", 2);
            b = (char) Integer.parseInt("00001111", 2);
            c = (char) Integer.parseInt("00111111", 2);
            d = (char) Integer.parseInt("11111100", 2);
            e = (char) Integer.parseInt("11110000", 2);
            f = (char) Integer.parseInt("11000000", 2);
            g = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
        }

        public String a(final byte[] array) {
            final double n = array.length;
            Double.isNaN(n);
            final StringBuilder sb = new StringBuilder((int) (n * 1.34) + 3);
            int i = 0;
            int j = 0;
            int n2 = 0;
            while (i < array.length) {
                for (j %= 8; j < 8; j += 6) {
                    Label_0214:
                    {
                        int n6;
                        if (j != 0) {
                            if (j != 2) {
                                char c;
                                int n4;
                                if (j != 4) {
                                    if (j != 6) {
                                        break Label_0214;
                                    }
                                    c = (char) ((char) (array[i] & a) << 4);
                                    final int n3 = i + 1;
                                    n2 = c;
                                    if (n3 >= array.length) {
                                        break Label_0214;
                                    }
                                    n4 = (array[n3] & e) >>> 4;
                                } else {
                                    c = (char) ((char) (array[i] & b) << 2);
                                    final int n5 = i + 1;
                                    n2 = c;
                                    if (n5 >= array.length) {
                                        break Label_0214;
                                    }
                                    n4 = (array[n5] & f) >>> 6;
                                }
                                n6 = (c | n4);
                            } else {
                                n6 = (array[i] & c);
                            }
                        } else {
                            n6 = (char) (array[i] & d) >>> 2;
                        }
                        n2 = (char) n6;
                    }
                    sb.append(g[n2]);
                }
                ++i;
            }
            if (sb.length() % 4 != 0) {
                for (int k = 4 - sb.length() % 4; k > 0; --k) {
                    sb.append("=");
                }
            }
            return sb.toString();
        }
    }

}
