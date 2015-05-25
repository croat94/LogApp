package com.zavrsni.jasamrafoooo.logapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    DBHelper mydb;
    public static final String defaultUrl = "http://posttestserver.com/post.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mydb = new DBHelper(this);

        if (mydb.numberOfRows() == 0){
            mydb.unesiOcitanje(0,"/","/","/",defaultUrl,"/","/");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            return true;
        }else if (id == R.id.action_url) {
            Intent i = new Intent(MainActivity.this, UrlActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onButtonClickNovo(View view){
        Intent i = new Intent(MainActivity.this, NovoOcitanje.class);
        startActivity(i);
    }

    public void onButtonClickPregled(View view){
        Intent i = new Intent(MainActivity.this, PregledOcitanja.class);
        startActivity(i);
    }
    public void onButtonClickSync(View view){
        boolean network = isNetworkAvailable();
        if (network) {
            provjeriPosljednjeOcitanje();
        }else{
            Toast.makeText(this, "Uključite Internet!", Toast.LENGTH_LONG).show();
        }
    }
    public void onButtonClickDelete(View view){
        obrisiSlikeIXML();
        this.deleteDatabase("MyDBName.db");
        finish();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void obrisiSlikeIXML(){
        for (int i = 0; i < mydb.numberOfRows(); i++){
            Cursor rs = mydb.getData(i);
            rs.moveToFirst();
            String datum = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_DATUM));
            String vrijemeKraj = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_VRIJEME_KRAJ));
            String prostorija = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_PROSTORIJA));
            String path = Environment.getExternalStorageDirectory() + "/" + prostorija + "_"+
                    datum + "_" + vrijemeKraj + ".jpeg";
            File imgFile = new File(path);
            imgFile.delete();
            if (!rs.isClosed()){
                rs.close();
            }
        }
        mydb.close();
        String path = Environment.getExternalStorageDirectory() + "/" + "ocitanja.xml";
        File xmlFile = new File(path);
        xmlFile.delete();
    }
    public void provjeriPosljednjeOcitanje(){
        Cursor rs = mydb.getData(mydb.numberOfRows()-1);
        rs.moveToFirst();
        String posljednjeOcitanjeVrijemeKraj = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_VRIJEME_KRAJ));
        String tempUploadano = rs.getString(rs.getColumnIndex(DBHelper.READINGS_COLUMN_UPLOADANO));
        rs.close();
        if (posljednjeOcitanjeVrijemeKraj.equals("Nije završeno") && tempUploadano.equals("ne")){
            alertDialog("Upozorenje!",
                    "Posljednja aktivnost nije završena!" +
                    " Želite li nastaviti s prijenosom očitanja na server?");
        }else{
            Intent i = new Intent(MainActivity.this, UploadNaServer.class);
            startService(i);
        }
    }

    public void alertDialog(String title, String message){
        try {
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();
            alert.setCancelable(false);

            alert.setTitle(title);
            alert.setMessage(message);
            alert.setIcon(android.R.drawable.ic_dialog_alert);
                alert.setButton2("Nastavi", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(MainActivity.this, UploadNaServer.class);
                                startService(i);
                            }
                        }
                );
                alert.setButton("Odustani", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                );

            alert.show();
        }
        catch(Exception e){}
    }
}
