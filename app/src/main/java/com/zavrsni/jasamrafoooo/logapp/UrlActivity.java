package com.zavrsni.jasamrafoooo.logapp;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class UrlActivity extends Activity {

    DBHelper mydb;
    EditText editUrl;
    public String Url;
    public String pomUrl;
    public String pomIme;
    public String pomPrezime;
    public boolean imaVec;
    public static final String defaultUrl = "http://posttestserver.com/post.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url);
        mydb = new DBHelper(this);

        editUrl = (EditText) findViewById(R.id.editUrl);

        Cursor rs = mydb.getData(0);
        rs.moveToFirst();
        if (mydb.numberOfRows()==0){
            imaVec = false;
        }else{
            imaVec = true;
            pomUrl = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_PROSTORIJA));
            pomIme = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_DATUM));
            pomPrezime = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_VRIJEME_POCETAK));
            editUrl.setText(pomUrl);
        }
        if (!rs.isClosed()){
            rs.close();
        }

    }

    public void onButtonClicked(View view){
        Url = editUrl.getText().toString();
        if (imaVec == false) {
            //mydb.unesiOcitanje(0, pomIme, pomPrezime, Url, "nijeProstorija", pomIme + " " + pomPrezime, "ne");
            Toast.makeText(this, "Prvo unesite podatke o osobi!", Toast.LENGTH_LONG).show();
        }
        else
            mydb.updateUrl(Url);
        finish();
    }

    public void onClickDefault(View view){
        editUrl.setText(defaultUrl);
    }
}
