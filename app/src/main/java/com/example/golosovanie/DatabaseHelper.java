package com.example.golosovanie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "golosovanie.db";
    private static final String TABLE_NAME = "users";
    private static final String COL_ID = "id";
    private static final String COL_EMAIL = "email";
    private static final String COL_TOKEN = "token";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EMAIL + " TEXT UNIQUE, " +
                COL_TOKEN + " TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS voted_votings (voting_id INTEGER PRIMARY KEY)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public boolean hasUserVoted(int votingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM voted_votings WHERE voting_id = ?", new String[]{String.valueOf(votingId)});
        boolean hasVoted = cursor.moveToFirst();
        cursor.close();
        return hasVoted;
    }

    public void markVotingAsVoted(int votingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("voting_id", votingId);
        db.insert("voted_votings", null, values);
    }

    public boolean addUser(String email, String token) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_EMAIL, email);
        contentValues.put(COL_TOKEN, token);

        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public String getUserToken(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COL_TOKEN}, COL_EMAIL + " = ?", new String[]{email}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String token = cursor.getString(cursor.getColumnIndexOrThrow(COL_TOKEN));
            cursor.close();
            return token;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public boolean hasVoted(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COL_EMAIL + " = ?", new String[]{email}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
