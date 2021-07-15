package com.myphuoc.noteapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.myphuoc.noteapp.Entities.Note;
import com.myphuoc.noteapp.R;

import org.w3c.dom.Text;

public class FireStoreAdapter extends FirestoreRecyclerAdapter<Note, FireStoreAdapter.NoteHolder> {
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FireStoreAdapter(@NonNull FirestoreRecyclerOptions<Note> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteHolder holder, int position, @NonNull Note model) {
        holder.tvTittle.setText(model.getTitle());
        holder.tvSubTitle.setText(model.getSubtitle());
        holder.tvDateTime.setText(model.getDateTime());

    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note, parent, false);
        return new NoteHolder(v);
    }

    class NoteHolder extends RecyclerView.ViewHolder{
        TextView tvTittle;
        TextView tvSubTitle;
        TextView tvDateTime;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            tvTittle = itemView.findViewById(R.id.item_note_title);
            tvSubTitle = itemView.findViewById(R.id.item_note_subtitle);
            tvDateTime = itemView.findViewById(R.id.item_note_date_time);
        }
    }
}
