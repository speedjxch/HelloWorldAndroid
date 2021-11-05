package com.example.todolist_my;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.example.todolist_my.view.AddEditNoteActivity;
import com.example.todolist_my.view.AlarmReceiver;
import com.example.todolist_my.view.NoteAdapter;
import com.example.todolist_my.view.SwipeToDeleteCallback;
import com.example.todolist_my.viewmodel.NoteViewModel;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist_my.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;

import com.example.todolist_my.room.Note;
import com.example.todolist_my.R;
import com.example.todolist_my.viewmodel.NoteViewModel;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    public static final int ADD_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;
    public static final int EDIT_NOTE_done_REQUEST = 3;

    private NoteViewModel noteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                startActivityForResult(intent, ADD_NOTE_REQUEST);
            }
        });

        final RecyclerView recyclerView = findViewById(R.id.recycle_view);
        final NoteAdapter noteAdapter = new NoteAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(noteAdapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                //Update RecycleView
                noteAdapter.submitList(notes);
            }
        });

        // Make the note swappable
        final CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorLayout);
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                final Note item = noteAdapter.getNoteAt(viewHolder.getAdapterPosition());

                noteViewModel.deleteNote(noteAdapter.getNoteAt(position));
                noteAdapter.notifyItemRemoved(position);

                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Item was removed from the list.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        noteViewModel.insertNote(item);
                        recyclerView.scrollToPosition(position);
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
        // End Make the note swappable

        // Note Clickable
        noteAdapter.setOnItemClickListener(new NoteAdapter.onItemClickListener() {
            @Override
            public void onItemClick(final Note note) {
                Snackbar snackbar;
                if (note.getDate() != null && note.getTime() != null) {
                    snackbar = Snackbar.make(coordinatorLayout,
                            " Task : " + note.getTitle() + "\n Date : " + note.getDate() + "\n Time : " + note.getTime(),
                            Snackbar.LENGTH_LONG);
                } else {
                    snackbar = Snackbar.make(coordinatorLayout,
                            " Task : " + note.getTitle() + "\n Date : N/A \n Time : N/A",
                            Snackbar.LENGTH_LONG);
                }
                snackbar.setAction("EDIT", new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                        intent.putExtra(AddEditNoteActivity.EXTRA_ID, note.getId());
                        intent.putExtra(AddEditNoteActivity.EXTRA_TITLE, note.getTitle());
                        if (note.getDate() != null && note.getTime() != null) {
                            intent.putExtra(AddEditNoteActivity.EXTRA_SWITCH, Boolean.toString(true));
                            intent.putExtra(AddEditNoteActivity.EXTRA_DATE, note.getDate().toString());
                            intent.putExtra(AddEditNoteActivity.EXTRA_TIME, note.getTime().toString());
                        } else {
                            intent.putExtra(AddEditNoteActivity.EXTRA_SWITCH, Boolean.toString(false));
                            intent.putExtra(AddEditNoteActivity.EXTRA_DATE, LocalDate.now().toString());
                            java.util.Date now = new java.util.Date();
                            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                            String formattedTime = sdf.format(now);
                            intent.putExtra(AddEditNoteActivity.EXTRA_TIME, formattedTime + ":00");
                        }
                        intent.putExtra(AddEditNoteActivity.EXTRA_DESCRIPTION, note.getDetail());
                        startActivityForResult(intent, EDIT_NOTE_REQUEST);
                        //noteViewModel.insertNote(note);
                        //recyclerView.scrollToPosition(note.ge);
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                // to prevent Snackbar text from being truncated
                TextView snackbarTextView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                snackbarTextView.setMaxLines(3);
                snackbar.show();
            }
        });
        // End Note Clickable

    }

    // Collect data from add note && add save note
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK) {
            // Add Task
            String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
            String switchValue = data.getStringExtra(AddEditNoteActivity.EXTRA_SWITCH);
            String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
            if (Boolean.parseBoolean(switchValue)) {
                Date date = Date.valueOf(data.getStringExtra(AddEditNoteActivity.EXTRA_DATE));
                Time time = Time.valueOf(data.getStringExtra(AddEditNoteActivity.EXTRA_TIME));
                Note note = new Note(title, date, time, description);

                //Set Notification
                int notificationId = 1;
                Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                intent.putExtra(AddEditNoteActivity.EXTRA_NOTIFICATION_ID, notificationId);
                intent.putExtra(AddEditNoteActivity.EXTRA_TITLE, title);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Calendar calendar = javasqlToCalendar(date, time);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                //End Setting Notification

                noteViewModel.insertNote(note);
                return;
            }
            Note note = new Note(title, description);
            noteViewModel.insertNote(note);

        } else if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {
            //Edit Task
            int id = data.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1);
            if (id == -1) {
                Toast.makeText(getApplicationContext(), "Task Can't Be Updated", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
            String switchValue = data.getStringExtra(AddEditNoteActivity.EXTRA_SWITCH);
            String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
            if (Boolean.parseBoolean(switchValue)) {
                Date date = Date.valueOf(data.getStringExtra(AddEditNoteActivity.EXTRA_DATE));
                Time time = Time.valueOf(data.getStringExtra(AddEditNoteActivity.EXTRA_TIME));
                Note note = new Note(title, date, time, description);
                note.setId(id);

                //Set Notification
                int notificationId = 1;
                Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                intent.putExtra(AddEditNoteActivity.EXTRA_NOTIFICATION_ID, notificationId);
                intent.putExtra(AddEditNoteActivity.EXTRA_TITLE, title);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Calendar calendar = javasqlToCalendar(date, time);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                //End Setting Notification

                noteViewModel.updateNote(note);
                return;
            }
            Note note = new Note(title, description);
            note.setId(id);
            noteViewModel.updateNote(note);
        } else {
            Toast.makeText(getApplicationContext(), "Task Not Saved", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_notes:
                noteViewModel.deleteAllNotes();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // End Menu

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */

    /*
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    */
    //java.sql to Calendar
    private Calendar javasqlToCalendar(Date date, Time time) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);
        Calendar calendarTimeTemp = Calendar.getInstance();
        calendarTimeTemp.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, calendarTimeTemp.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendarTimeTemp.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendarTimeTemp.get(Calendar.SECOND));
        return calendar;
    }
}