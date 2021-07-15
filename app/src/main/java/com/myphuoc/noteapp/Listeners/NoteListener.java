package com.myphuoc.noteapp.Listeners;

import android.view.View;

import com.myphuoc.noteapp.Entities.Note;

public interface NoteListener {
    void onNoteClicked(View view, Note note, int position);

    void onNoteLongClicked(View view, Note note, int position);
}
