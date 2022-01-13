package com.example.simplesqlitedbhelper.util;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.simplesqlitedbhelper.BuildConfig;
import com.example.simplesqlitedbhelper.model.Vocabulary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    private static DbHelper singletonInstance;
    private Context context;

    // Database Info
    private static final String DATABASE_NAME = "sqlite_file.sqlite";
    private static final String DATABASE_PATH = "/data/data/" + BuildConfig.APPLICATION_ID + "/databases" + DATABASE_NAME;
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_VOCABULARY = "VOCABULARY";

    // Vocabulary Table Columns
    private static final String KEY_VOCABULARY_SEQ = "SEQ";
    private static final String KEY_VOCABULARY_ENGLISH = "ENGLISH";
    private static final String KEY_VOCABULARY_KOREAN = "KOREAN";

    public static synchronized DbHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (singletonInstance == null) {
            singletonInstance = new DbHelper(context);
        }
        return singletonInstance;
    }

    // DbHelper 생성
    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);    // db 명과 버전만 정의 한다.
        // TODO Auto-generated constructor stub

        this.context = context;
        initDbHelper();
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
        this.onUpgrade(db, 1, 2);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_VOCABULARY_TABLE = "CREATE TABLE " + TABLE_VOCABULARY +
                "(" +
                KEY_VOCABULARY_SEQ + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_VOCABULARY_ENGLISH + " TEXT," +
                KEY_VOCABULARY_KOREAN + " TEXT" +
                ")";
        db.execSQL(CREATE_VOCABULARY_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOCABULARY);
            onCreate(db);
        }
    }

    private void initDbHelper() {
        boolean checkDb = checkDbExist();
        if(checkDb) {
            System.out.println("Database Exists");
        } else {
            onCreate(this.getReadableDatabase());
        }
    }

    private boolean checkDbExist() {
        // Create and/or open the database for writing
        boolean checkDb = false;
        try {
            File db = context.getDatabasePath(DATABASE_PATH);
            checkDb = db.exists();
        } catch(SQLiteException e) {
            System.out.println("Database doesn't exist");
        }
        return checkDb;
    }

    // Insert a post into the database
    public void addVocabulary(Vocabulary vocabulary) {
        // Create and/or open the database for writing
        SQLiteDatabase db = this.getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_VOCABULARY_ENGLISH, vocabulary.english);
            values.put(KEY_VOCABULARY_KOREAN, vocabulary.korean);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_VOCABULARY, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add vocabulary to database");
        } finally {
            db.endTransaction();
        }
    }

    public List<Vocabulary> getAllVocabulary() {
        List<Vocabulary> vocabularies = new ArrayList<>();

        // SELECT * FROM VOCABULARY
        String VOCABULARY_SELECT_QUERY = String.format("SELECT * FROM %s", TABLE_VOCABULARY);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(VOCABULARY_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Vocabulary vocabulary = new Vocabulary();
                    vocabulary.seq = cursor.getInt(cursor.getColumnIndex(KEY_VOCABULARY_SEQ));
                    vocabulary.english = cursor.getString(cursor.getColumnIndex(KEY_VOCABULARY_ENGLISH));
                    vocabulary.korean = cursor.getString(cursor.getColumnIndex(KEY_VOCABULARY_KOREAN));
                    vocabularies.add(vocabulary);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return vocabularies;
    }

    // Update the user's profile picture url
    public int updateVocabulary(Vocabulary vocabulary) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_VOCABULARY_ENGLISH, vocabulary.english);
        values.put(KEY_VOCABULARY_KOREAN, vocabulary.korean);

        // Updating profile picture url for user with that userName
        return db.update(TABLE_VOCABULARY, values, KEY_VOCABULARY_SEQ + " = ?",
                new String[] { String.valueOf(vocabulary.seq) });
    }

    public void deleteAllVocabulary() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_VOCABULARY, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all posts and users");
        } finally {
            db.endTransaction();
        }
    }
}
