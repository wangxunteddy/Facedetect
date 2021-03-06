package com.example.hzmt.facedetect.util;

import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;
import java.security.KeyStore;


import java.io.DataOutputStream;


/**
 * Created by xun on 2017/10/15.
 */

public class HttpsUtil {
    public static JSONObject JsonObjectRequest(InputStream certstream, JSONObject data, String urlstring){
        InputStream inputStream = null;
        HttpsURLConnection urlConnection = null;
        JSONObject resultJSON = null;
        try{
            if(null == certstream)
                HTTPSTrustManager.allowAllSSL();//信任所有证书

            URL url = new URL(urlstring);
            urlConnection = (HttpsURLConnection) url.openConnection();
            if(null != certstream) {
                //InputStream in = context.getAssets().open(certfile);
                urlConnection.setSSLSocketFactory(getSSLContext(certstream).getSocketFactory());
                //设置ip授权认证：如果已经安装该证书，可以不设置，否则需要设置
                urlConnection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }

            // 设置连接超时时间
            urlConnection.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            urlConnection.setReadTimeout(5 * 1000);

            /* optional request header */
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setRequestMethod("POST");
            // Post请求必须设置允许输出 默认false
            urlConnection.setDoOutput(true);
            //设置请求允许输入 默认是true
            urlConnection.setDoInput(true);
            //使用Post方式不能使用缓存
            urlConnection.setUseCaches(false);
            //urlConnection.connect();
            DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
            dos.writeBytes(data.toString());
            dos.flush();
            dos.close();
            // try to get response
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                inputStream = urlConnection.getInputStream();
                String result = streamToString(inputStream);
                //Log.e("JsonObjectRequest", result);
                resultJSON = new JSONObject(result);
            }
            else{
                Log.e("https response:", Integer.toString(statusCode));
            }
        }catch(Exception e) {
            //Log.e("JsonObjectRequest", e.toString());
            e.printStackTrace();
        }
        finally
        {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

            return resultJSON;
        }
    }

    /**
     * 将输入流转换成字符串
     *
     * @param is 从网络获取的输入流
     * @return
     */
    private static String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            Log.e("streamToString", e.toString());
            return null;
        }
    }

    public static SSLContext getSSLContext(InputStream in){
        SSLContext context = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca = cf.generateCertificate(in);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(null, null);
            keystore.setCertificateEntry("ca", ca);
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keystore);
            // Create an SSLContext that uses our TrustManager
            context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
        } catch (Exception e){
            e.printStackTrace();
        }
        return context;
    }
}


