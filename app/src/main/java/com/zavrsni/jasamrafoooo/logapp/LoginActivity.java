package com.zavrsni.jasamrafoooo.logapp;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public class LoginActivity extends Activity {

    DBHelper mydb;
    EditText editIme;
    EditText editPrezime;
    public String ime;
    public String prezime;
    public String pomIme;
    public String pomPrezime;
    public boolean imaVec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mydb = new DBHelper(this);

        editIme = (EditText) findViewById(R.id.editIme);
        editPrezime = (EditText) findViewById(R.id.editPrezime);

        Cursor rs = mydb.getData(0);
        rs.moveToFirst();
        if (mydb.numberOfRows()==1 && rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_DATUM)).equals("/")){
            imaVec = false;
        }else{
            imaVec = true;
            pomIme = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_DATUM));
            pomPrezime = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_VRIJEME_POCETAK));
            editIme.setText(pomIme);
            editPrezime.setText(pomPrezime);
        }
        if (!rs.isClosed()){
            rs.close();
        }

    }

    public void onButtonClicked(View view){
        ime = editIme.getText().toString();
        prezime = editPrezime.getText().toString();
        mydb.updateImeIPrezime(ime, prezime);
        finish();
    }
}