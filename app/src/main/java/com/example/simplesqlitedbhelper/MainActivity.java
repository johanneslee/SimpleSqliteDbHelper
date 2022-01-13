package com.example.simplesqlitedbhelper;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.simplesqlitedbhelper.model.Vocabulary;
import com.example.simplesqlitedbhelper.util.DbHelper;

public class MainActivity extends AppCompatActivity {

    DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Vocabulary vocabulary = new Vocabulary();

        vocabulary.english = "apple";
        vocabulary.korean = "사과";

        DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
        dbHelper.addVocabulary(vocabulary);
        Log.d(TAG, "addVocabulary");

        for(Vocabulary listVocabulary : dbHelper.getAllVocabulary()){
            vocabulary.seq = listVocabulary.seq; // Get Last Seq

            Log.d(TAG, String.valueOf(listVocabulary.seq));
            Log.d(TAG, listVocabulary.english);
            Log.d(TAG, listVocabulary.korean);
        }

        vocabulary.english = "banana";
        vocabulary.korean = "바나나";
        dbHelper.updateVocabulary(vocabulary);
        Log.d(TAG, "updateVocabulary");

        for(Vocabulary listVocabulary : dbHelper.getAllVocabulary()){
            Log.d(TAG, String.valueOf(listVocabulary.seq));
            Log.d(TAG, listVocabulary.english);
            Log.d(TAG, listVocabulary.korean);
        }

        dbHelper.deleteAllVocabulary();
        Log.d(TAG, "deleteAllVocabulary");

        for(Vocabulary listVocabulary : dbHelper.getAllVocabulary()){
            vocabulary.seq = listVocabulary.seq;
            Log.d(TAG, String.valueOf(listVocabulary.seq));
            Log.d(TAG, listVocabulary.english);
            Log.d(TAG, listVocabulary.korean);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}