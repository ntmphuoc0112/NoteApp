package com.myphuoc.noteapp.Repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.room.Database;

import com.myphuoc.noteapp.Database.NoteDatabase;
import com.myphuoc.noteapp.Entities.Note;
import com.myphuoc.noteapp.dao.NoteDao;

import java.util.List;

public class DataRepository {
    private NoteDatabase noteDatabase;
    private LiveData<List<Note>> getAllNote;

    public DataRepository(Application application){
        noteDatabase = NoteDatabase.getDatabase(application);
        getAllNote = (LiveData<List<Note>>) noteDatabase.noteDao().getAllNotes();
    }

    public void insert(List<Note> noteList){
        new InsertAsynTask(noteDatabase).execute(noteList);

    }

    public LiveData<List<Note>> getAllNote(){
        return getAllNote;
    }

    static class InsertAsynTask extends AsyncTask<List<Note>, Void, Void>{

        private NoteDao noteDao;

        InsertAsynTask(NoteDatabase noteDatabase){
            noteDao = noteDatabase.noteDao();
        }

        @Override
        protected Void doInBackground(List<Note>... lists) {
            noteDao.insertNote((Note) lists[0]);
            return null;
        }
    }
}
