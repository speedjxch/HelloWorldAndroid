package com.example.todolist_my.view;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist_my.R;
import com.example.todolist_my.room.Note;
import com.example.todolist_my.viewmodel.NoteViewModel;

import java.util.HashMap;
import java.util.Map;


public class NoteAdapter extends ListAdapter<Note, NoteAdapter.NoteHolder> {

    private Map<Integer,Boolean> map_checkbox=new HashMap<>();

    private NoteViewModel noteViewModel;

    private static final DiffUtil.ItemCallback<Note> DIFF_CALLBACK = new DiffUtil.ItemCallback<Note>() {
        @Override
        public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getTitle().equals(newItem.getTitle());
                    /*&& oldItem.getDate().equals(newItem.getDate())
                    && oldItem.getTime().equals(newItem.getTime()) && oldItem.getDetail().equals(newItem.getDetail());*/
        }
    };
    private onItemClickListener listener;

    public NoteAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteHolder(itemView);
    }

    // The method below is for assign Title into titleTextView, Detail into detailTextView, etc...
    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note currentNote = getItem(position);
        holder.titleTextView.setText(currentNote.getTitle());

        if (currentNote.getDetail().trim().isEmpty())
            holder.detailTextView.setText("");
        else
            holder.detailTextView.setText(currentNote.getDetail());

        holder.taskState.setOnCheckedChangeListener(null); //reset the listener when the item is recreated when scrolling
        holder.taskState.setChecked(currentNote.getDone()); //update the display with the status of the task


        holder.taskState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //display a short message when we click the checkbox
                    //save the status of the task
                    currentNote.setDone(true);

                } else {
                    currentNote.setDone(false);
                }
            }
        });


        if(currentNote.getDone()){
            holder.titleTextView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }

        /*
        if (currentNote.getDate() != null)
            holder.dateTextView.setText(currentNote.getDate().toString());
        else
            holder.dateTextView.setText("N/A");

        if (currentNote.getTime() != null)
            holder.timeTextView.setText(currentNote.getTime().toString());
        else
            holder.timeTextView.setText("N/A");
        */
    }

    public Note getNoteAt(int position) {
        return getItem(position);
    }

    public void setOnItemClickListener(onItemClickListener listener) {
        this.listener = listener;
    }

    // the item to be clickable
    public interface onItemClickListener {
        void onItemClick(Note note);
    }

    class NoteHolder extends RecyclerView.ViewHolder {

        private TextView titleTextView;
        private TextView detailTextView;
        public CheckBox taskState;
        //private TextView dateTextView;
        //lprivate TextView timeTextView;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title);
            detailTextView = itemView.findViewById(R.id.detail);
            //dateTextView = itemView.findViewById(R.id.dateT);
            //timeTextView = itemView.findViewById(R.id.timeT);
            taskState = itemView.findViewById(R.id.task_status);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION)
                        listener.onItemClick(getItem(position));
                }
            });
        }
    }
}

