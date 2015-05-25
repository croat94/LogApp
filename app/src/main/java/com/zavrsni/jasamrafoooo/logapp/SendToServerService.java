package com.zavrsni.jasamrafoooo.logapp;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Patterns;
import android.webkit.URLUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.IOException;

public class SendToServerService extends IntentService {

    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;
    String Url;
    DBHelper mydb;
    boolean flagDoneUploading = false;

    public SendToServerService() {
        super("SendToServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mydb = new DBHelper(this);

        Cursor rs = mydb.getData(0);
        rs.moveToFirst();
        Url = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_PROSTORIJA));
        if (!rs.isClosed()){
            rs.close();
        }
        if(Patterns.WEB_URL.matcher(Url).matches()) {
            AndroidHttpClient http = AndroidHttpClient.newInstance("MyApp");
            HttpPost method = new HttpPost(Url);

            method.setEntity(new FileEntity(new File(Environment.getExternalStorageDirectory(),
                    "ocitanja.xml"), "application/json"));

            try {
                HttpResponse response = http.execute(method);
                if (response.getStatusLine().getStatusCode() == 200) {
                    flagDoneUploading = true;
                    buildNotification("Prijenos podataka završen", "Podatci poslani na server",
                            "Prijenos podataka završen");
                } else {
                    buildNotification("Prijenos podataka nije uspio", "Prijenos podataka nije uspio",
                            "Prijenos podataka neuspješan");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            http.close();
        }else{
            buildNotification("Prijenos podataka nije uspio", "Prijenos podataka nije uspio",
                    "URL nije valjan");
        }
    }

    public void buildNotification(String ticker, String contentTitle, String contentText){
        notification = new NotificationCompat.Builder(this);
        if (flagDoneUploading) {
            mydb.uploadNaServer(1);// 1 je tu bezveze
            notification.setSmallIcon(R.drawable.ic_stat_checkmark_icon);
        }else{
            notification.setSmallIcon(R.drawable.ic_stat_not_uploaded);
        }
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notification.setSound(alarmSound);
        notification.setAutoCancel(true);
        notification.setWhen(System.currentTimeMillis());
        notification.setTicker(ticker);
        notification.setContentTitle(contentTitle);
        notification.setContentText(contentText);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(uniqueID, notification.build());
    }
}
