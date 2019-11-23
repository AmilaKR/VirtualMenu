package com.amila.dev.virtualmenu;

import android.content.SharedPreferences;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.util.ArrayList;
import java.util.HashMap;

public class Common {

    private static Common common;

    private static final String MyPREFERENCES = "MyPrefs";
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;

    private String sheetId;
    private ArrayList<ListItem> menuList;
    private ArrayList<String> urlList;
    private ArrayList<String> infoList;
    private HashMap<String, String> scheduleList;
    private String shopName;
    private boolean autoScroll;
    private boolean webUrl;
    private GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { SheetsScopes.SPREADSHEETS_READONLY };

    private Common(){
        menuList = new ArrayList<>();
        infoList = new ArrayList<>();
        urlList = new ArrayList<>();
        scheduleList = new HashMap<String, String>();
    }

    static Common getCommon(){
        if(common == null){
            common = new Common();
        }
        return common;
    }

    static String getMyPREFERENCES() {
        return MyPREFERENCES;
    }

    SharedPreferences getSharedpreferences() {
        return sharedpreferences;
    }

    void setSharedpreferences(SharedPreferences sharedpreferences) {
        this.sharedpreferences = sharedpreferences;
    }

    SharedPreferences.Editor getEditor() {
        return editor;
    }

    void setEditor(SharedPreferences.Editor editor) {
        this.editor = editor;
    }

    String getSheetId() {
        return sheetId;
    }

    void setSheetId(String sheetId) {
        this.sheetId = sheetId;
    }

    ArrayList<ListItem> getMenuList() {
        return menuList;
    }

    public void setMenuList(ArrayList<ListItem> menuList) {
        this.menuList = menuList;
    }

    public ArrayList<String> getUrlList() {
        return urlList;
    }

    public void setUrlList(ArrayList<String> urlList) {
        this.urlList = urlList;
    }

    ArrayList<String> getInfoList() {
        return infoList;
    }

    public void setInfoList(ArrayList<String> infoList) {
        this.infoList = infoList;
    }

    public HashMap<String, String> getScheduleList() {
        return scheduleList;
    }

    public void setScheduleList(HashMap<String, String> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    boolean isAutoScroll() {
        return autoScroll;
    }

    void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    boolean isWebUrl() {
        return webUrl;
    }

    void setWebUrl(boolean webUrl) {
        this.webUrl = webUrl;
    }

    GoogleAccountCredential getmCredential() {
        return mCredential;
    }

    void setmCredential(GoogleAccountCredential mCredential) {
        this.mCredential = mCredential;
    }

    static String[] getSCOPES() {
        return SCOPES;
    }

    void addUrl(String url){
        urlList.add(url);
    }

    void addSchedule(String date, String url){
        scheduleList.put(date, url);
    }

    void addMenuItem(String name, String price){
        menuList.add((new ListItem(name,price)));
    }

    void addInfoItem(String item){
        infoList.add(item);
    }

    public int listSize(){
        return menuList.size();
    }

    public int infoSize(){
        return infoList.size();
    }

    void clearMenu(){
        menuList.clear();
    }

    void clearInfo(){
        infoList.clear();
    }

}
