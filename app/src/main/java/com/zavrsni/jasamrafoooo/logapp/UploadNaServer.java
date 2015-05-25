package com.zavrsni.jasamrafoooo.logapp;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Patterns;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.File;
import java.io.ByteArrayOutputStream;

public class UploadNaServer extends IntentService {

    DBHelper mydb;
    public int brojRedova;
    public int brojNeUploadanih;
    String datum;
    int i;
    String vrijemePocetak;
    String vrijemeKraj;
    String prostorija;
    String userid;
    String kodiranaSlika;
    String uploadano;
    boolean flag = true;
    boolean flagDoneUploading = false;
    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;
    public String url;

    public UploadNaServer() {
        super("UploadNaServer");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        mydb = new DBHelper(this);
        brojRedova = mydb.numberOfRows();

        Cursor rs = mydb.getData(0);
        rs.moveToFirst();
        url = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_PROSTORIJA));
        rs.close();

        if(Patterns.WEB_URL.matcher(url).matches()) {

            buildNotification("Prijenos podataka na server...", "Slanje podataka na server",
                    "...");
            flag = false;
            try {

                // 1. create HttpClient
                for (i = 1; i < brojRedova; i++) {
                    rs = mydb.getData(i);
                    rs.moveToFirst();
                    uploadano = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_UPLOADANO));
                    if (uploadano.equals("ne")) {
                        brojNeUploadanih++;
                    }
                    if (!rs.isClosed()) {
                        rs.close();
                    }
                }
                for (i = brojRedova - brojNeUploadanih; i < brojRedova; i++) {
                    rs = mydb.getData(i);
                    rs.moveToFirst();
                    HttpClient httpclient = new DefaultHttpClient();

                    HttpPost httpPost = new HttpPost(url);

                    String json = "";
                    datum = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_DATUM));
                    vrijemeKraj = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_VRIJEME_KRAJ));
                    vrijemePocetak = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_VRIJEME_POCETAK));
                    prostorija = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_PROSTORIJA));
                    userid = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_USERID));
                    kodiranaSlika = kodirajSliku(prostorija + "_" + datum + "_" + vrijemeKraj + ".jpeg");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.accumulate("datum", datum);
                    jsonObject.accumulate("picture", kodiranaSlika);
                    jsonObject.accumulate("prostorija", prostorija);
                    jsonObject.accumulate("timeEnd", vrijemeKraj);
                    jsonObject.accumulate("timeStart", vrijemePocetak);
                    jsonObject.accumulate("username", userid);

                    json = jsonObject.toString();
                    StringEntity se = new StringEntity(json, HTTP.UTF_8);

                    httpPost.setEntity(se);

                    httpPost.setHeader("Accept", "application/json");
                    httpPost.setHeader("Content-type", "application/json");

                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() != 200) {
                        flag = false;
                        buildNotification("Prekid prijenosa", "Prekid prijenosa",
                                "Prijenos podataka nije dovršen!");
                        break;
                    }
                    mydb.uploadNaServer(i);
                    flag = true;
                    int brojUploadanih = (i - brojRedova + brojNeUploadanih + 1);
                    buildNotification("Poslano "+ brojUploadanih  + "/" + brojNeUploadanih,
                            "Slanje",
                            "Poslano: " + brojUploadanih + "/" + brojNeUploadanih);
                    if (!rs.isClosed()) {
                        rs.close();
                    }

                }

            } catch (Exception e) {
            }

            if (i == brojRedova && i > 1) {
                flagDoneUploading = true;
                flag = false;
                buildNotification("Prijenos podataka završen", "Podatci poslani na server",
                        "Prijenos podataka završen");
            } else {
                flagDoneUploading = false;
                flag = false;
                buildNotification("Prijenos nije uspio", "Prijenos nije uspio",
                        "Pokušajte ponovno");
            }
        }else{
            flagDoneUploading = false;
            flag = false;
            buildNotification("Prijenos podataka nije uspio", "Prijenos podataka nije uspio",
                    "URL nije valjan");
        }
    }

    public String kodirajSliku(String put){
        String path = Environment.getExternalStorageDirectory() + "/" + put;
        File imgFile = new File(path);
        String encodedImage ="Nema slike";
        if(imgFile.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] b = baos.toByteArray();
            encodedImage = Base64.encodeToString(b, Base64.NO_WRAP);
        }
        return encodedImage;
    }

    public void buildNotification(String ticker, String contentTitle, String contentText){
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);
        if (flagDoneUploading){
            notification.setProgress(0, 0, false);
            notification.setSmallIcon(R.drawable.ic_stat_checkmark_icon);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notification.setSound(alarmSound);
        }else if (!flag){
            notification.setProgress(0, 0, false);
            notification.setSmallIcon(R.drawable.ic_stat_not_uploaded);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notification.setSound(alarmSound);
        }
        if (flag) {
            notification.setSmallIcon(R.drawable.ic_stat_upload_icon);
        }
        notification.setWhen(System.currentTimeMillis());
        notification.setTicker(ticker);
        notification.setContentTitle(contentTitle);
        notification.setContentText(contentText);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(uniqueID, notification.build());
    }
}