package com.zavrsni.jasamrafoooo.logapp;

public class Ocitanje {

    private String date;
    private String startTime;
    private String endTime;
    private String room;

    public Ocitanje(){
        date = "E";
        startTime = "E";
        endTime = "E";
        room = "E";
    }

    public Ocitanje(String datum, String vrijemePocetak, String vrijemeKraj, String prostorija) {
        date =  datum;
        startTime = vrijemePocetak;
        endTime = vrijemeKraj;
        room = prostorija;
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getRoom() {
        return room;
    }
}
