package com.zavrsni.jasamrafoooo.logapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String READINGS_TABLE_NAME = "ocitanja";
    public static final String READINGS_COLUMN_ID = "_id";
    public static final String READINGS_COLUMN_PROSTORIJA = "prostorija";
    public static final String READINGS_COLUMN_VRIJEME_POCETAK = "vrijemepocetak";
    public static final String READINGS_COLUMN_DATUM = "datum";
    public static final String READINGS_COLUMN_VRIJEME_KRAJ = "vrijemekraj";
    public static final String READINGS_COLUMN_UPLOADANO = "uploadano";
    public static final String READINGS_COLUMN_USERID = "userid";

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
        "create table 'ocitanja' " +
        "(_id integer primary key, " + "datum text, " + "vrijemepocetak text, " +
                "prostorija text, " + "vrijemekraj text, " + "userid text, " + "uploadano text);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ocitanja");
        onCreate(db);
    }

    public boolean unesiOcitanje  (int id, String datum, String vrijemePocetak,
                                   String vrijemeKraj, String prostorija, String userid, String uploadano)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("_id", id);
        contentValues.put("datum", datum);
        contentValues.put("vrijemepocetak", vrijemePocetak);
        contentValues.put("vrijemekraj", vrijemeKraj);
        contentValues.put("prostorija", prostorija);
        contentValues.put("userid", userid);
        contentValues.put("uploadano", uploadano);

        db.insert("ocitanja", null, contentValues);
        return true;
    }
    public boolean uploadNaServer (int i){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE ocitanja SET uploadano='da' WHERE _id='"+i+"'");
        return true;
    }

    public boolean updateImeIPrezime(String ime, String prezime){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE ocitanja SET datum='"+ime+"', vrijemePocetak='"+prezime+"'WHERE _id="+0+"");
        return true;
    }

    public boolean updateUrl(String url){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE ocitanja SET prostorija='"+url+"' WHERE _id="+0+"");
        return true;
    }

    public boolean updateKraj(int brojRedova, String time){
        SQLiteDatabase db = this.getWritableDatabase();
        brojRedova--;
        db.execSQL("UPDATE ocitanja SET vrijemekraj='"+ time +"' WHERE _id="+ brojRedova +"");
        return true;
    }

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from ocitanja where _id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, READINGS_TABLE_NAME);
        return numRows;
    }
}