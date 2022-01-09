package com.glennappdev.gawain;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Subjects.db";
    public static final String TABLE_NAME = "TABLE_SUBJECTS";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "SUBJECT_TITLE";
    public static final String COL_3 = "SUBJECT_COLOR";
    public static int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, SUBJECT_TITLE TEXT, SUBJECT_COLOR TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    // INSERT DATA
    public boolean insertInfo(String subjectTitle, String subjectColor) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, subjectTitle);
        contentValues.put(COL_3, subjectColor);
        long result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();

        // check if data was inserted successfully
        return result != -1;
    }

    // READ DATA
    public Cursor getInfo() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("Select * from " + TABLE_NAME, null);
    }

    // Get subject color
    public Cursor getSubjColor(String subjTitle) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return sqLiteDatabase.rawQuery("Select SUBJECT_COLOR from " + TABLE_NAME + " WHERE SUBJECT_TITLE = ?", new String[]{subjTitle});
    }

    // UPDATE DATA
    public boolean updateInfo(String id, String userName, String password) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, userName);
        contentValues.put(COL_3, password);
        int result = sqLiteDatabase.update(TABLE_NAME, contentValues, "ID =?", new String[]{id});

        // check if data updated
        return result > 0;
    }

    // DELETE DATA
    public Integer deleteInfo(String id) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.delete(TABLE_NAME, "ID =?", new String[]{id});
    }

    // delete subject where subject title is ?
    public Integer deleteSubj(String subjTitle) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.delete(TABLE_NAME, "SUBJECT_TITLE =?", new String[]{subjTitle});
    }


}
