package com.example.todolist_my.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Note.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class NoteDatabase extends RoomDatabase {

    private static NoteDatabase instance;

    // synchronized : to be accessed once a time while multithreading
    public static synchronized NoteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), NoteDatabase.class,
                    "note_database").fallbackToDestructiveMigration().build();
        }
        return instance;
    }

    public abstract NoteDao noteDao();

}
