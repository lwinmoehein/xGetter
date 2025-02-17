package com.htetznaing.xgetter.Core;
import com.htetznaing.xgetter.Model.XModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.htetznaing.xgetter.Utils.GDriveUtils.getCookie;
import static com.htetznaing.xgetter.Utils.GDriveUtils.getDRIVE_STREAM;
import static com.htetznaing.xgetter.Utils.GDriveUtils.getTitle;

public class GDrive {
    public static ArrayList<XModel> fetch(final String file_id){
        ArrayList<XModel> xModels = new ArrayList<>();
        URL obj = null;
        try {
            obj = new URL("https://drive.google.com/get_video_info?docid="+file_id);
            URLConnection conn = obj.openConnection();
            String header = conn.getHeaderFields().get("Set-Cookie").toString();
            String cookie = getDRIVE_STREAM(header)+getCookie(header);
            System.out.println(cookie);
            InputStream is = conn.getInputStream();
            int ptr = 0;
            String response = "";
            while ((ptr = is.read()) != -1) {
                response+=((char)ptr);
            }

            String convertStreamToString = response;
            String title = getTitle(convertStreamToString);

            if (convertStreamToString.contains("errorcode=100&reason=") || convertStreamToString.contains("errorcode=150&reason=")) {
                return null;
            }

            Matcher matcher = Pattern.compile(Pattern.quote("https") + "(.*?)" + Pattern.quote("quality")).matcher(convertStreamToString.split("url_encoded_fmt_stream_map")[1]);
            while (matcher.find()) {
                try {
                    convertStreamToString = URLDecoder.decode(matcher.group(1), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    convertStreamToString = null;
                }
                try {
                    convertStreamToString = URLDecoder.decode(convertStreamToString, "utf-8");
                } catch (UnsupportedEncodingException e2) {
                    e2.printStackTrace();
                    convertStreamToString = null;
                }
                try {
                    convertStreamToString = convertStreamToString.replace("; codecs=\"avc1.42001E, mp4a.40.2\"\\u0026", "");
                    if (convertStreamToString.contains("mp4")) {
                        if (convertStreamToString.contains("url_encoded_fmt_stream_map=itag=22&url")) {
                            convertStreamToString = convertStreamToString.split("url_encoded_fmt_stream_map=itag=22&url=")[1].replace("https", "");
                        }
                        convertStreamToString = "https" + convertStreamToString.split("&type=video/mp4")[0] + "&?/" + title;

                        if (convertStreamToString.contains("itag=37")){
                            putModel(convertStreamToString,"1080p",cookie,xModels);
                        }else if (convertStreamToString.contains("itag=22")){
                            putModel(convertStreamToString,"720p",cookie,xModels);
                        }else if (convertStreamToString.contains("itag=59")){
                            putModel(convertStreamToString,"480p",cookie,xModels);
                        }else if (convertStreamToString.contains("itag=18")){
                            putModel(convertStreamToString,"360p",cookie,xModels);
                        }else {
                            putModel(convertStreamToString,"480px",cookie,xModels);
                        }

                    }
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xModels;
    }



    private static void putModel(String url,String quality,String cookie,ArrayList<XModel> model){
        XModel xModel = new XModel();
        xModel.setUrl(url);
        xModel.setQuality(quality);
        xModel.setCookie(cookie);
        model.add(xModel);
    }
}
