package com.myphuoc.noteapp.Activity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.myphuoc.noteapp.Adapter.FireStoreAdapter;
import com.myphuoc.noteapp.Adapter.NoteAdapter;
import com.myphuoc.noteapp.Database.NoteDatabase;
import com.myphuoc.noteapp.Entities.Note;
import com.myphuoc.noteapp.FirebaseDatabase.FireStore;
import com.myphuoc.noteapp.Listeners.NoteListener;
import com.myphuoc.noteapp.R;

import com.shreyaspatil.MaterialDialog.MaterialDialog;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.jvm.internal.Intrinsics;

public class MainActivity extends AppCompatActivity implements NoteListener, NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private ConstraintLayout contentView;
    private ImageView imageEmpty;
    private ImageButton navigationMenu;
    private TextView textEmpty;
    private EditText inputSearch;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton addNoteFloatingBtn;
    private ImageView ivUser;
    private TextView tvUserName;
    private TextView tvEmailFinal;
    private Button btnSignOut;


    private List<Note> noteList;
    private NoteAdapter notesAdapter;
    private RecyclerView notesRecyclerView;




    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    public static final int REQUEST_CODE_TAKE_PHOTO = 4;
    public static final int REQUEST_CODE_SELECT_IMAGE = 5;

    private int noteClickedPosition = -1;
    private androidx.appcompat.view.ActionMode actionMode;

    private AlertDialog dialogAddURL;
    private AlertDialog dialogAddImage;

    private static final float END_SCALE = 0.8f;
    public static final String TAG = "GoogleSignIn";

    private AppUpdateManager mAppUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;
    private int RC_APP_UPDATE = 123;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setNavigationBarColor(ContextCompat.getColor(MainActivity.this, R.color.colorQuickActionsBackground));

        initViews();
        setActionOnViews();
        setNavigationMenu();


        getNotes(REQUEST_CODE_SHOW_NOTES, false);

        View imageUser = navigationView.getHeaderView(0);
        ivUser = imageUser.findViewById(R.id.iv_name);

        View tvUser = navigationView.getHeaderView(0);
        tvUserName = tvUser.findViewById(R.id.tv_username);

      View tvEmail = navigationView.getHeaderView(0);
      tvEmailFinal = tvEmail.findViewById(R.id.tv_email);

      View signOut = navigationView.getHeaderView(0);
      btnSignOut = signOut.findViewById(R.id.btnSignOut);

        SharedPreferences preferences = this.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userName = preferences.getString("userName", "");
        String userEmail = preferences.getString("userEmail", "");
        String userImageUrl = preferences.getString("userPhoto","");

        tvUserName.setText(userName);
        tvEmailFinal.setText(userEmail);
        Glide.with(this).load(userImageUrl).into(ivUser);

        btnSignOut.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            gotoSignIn();
        });
    }

    private void gotoSignIn() {
        startActivity(new Intent(MainActivity.this, SignInActivity.class));
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.main_drawer_layout);
        navigationView = findViewById(R.id.main_navigation_menu);
        navigationMenu = findViewById(R.id.main_navigation);
        contentView = findViewById(R.id.content_view);
        inputSearch = findViewById(R.id.input_search);
        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        bottomAppBar = findViewById(R.id.main_bottom_app_bar);
        addNoteFloatingBtn = findViewById(R.id.floating_action_add_notes_btn);
    }

    private void setActionOnViews() {
        KeyboardVisibilityEvent.setEventListener(MainActivity.this, isOpen -> {
            if (!isOpen) {
                inputSearch.clearFocus();
            }
        });

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (noteList.size() != 0) {
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });

        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteList = new ArrayList<>();
        notesAdapter = new NoteAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);


        bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_add:
                    UIUtil.hideKeyboard(MainActivity.this);
                    Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    inputSearch.setText(null);
                    break;
                case R.id.menu_image:
                    UIUtil.hideKeyboard(MainActivity.this);
                    showAddImageDialog();
                    break;
                case R.id.menu_web_link:
                    showAddURLDialog();
                    break;
            }
            return false;
        });

        addNoteFloatingBtn.setOnClickListener(v -> {
            UIUtil.hideKeyboard(MainActivity.this);
            Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
            inputSearch.setText(null);
        });
    }

    private void setNavigationMenu() {
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(MainActivity.this);

        navigationMenu.setOnClickListener(v -> {
            UIUtil.hideKeyboard(MainActivity.this);
            if (drawerLayout.isDrawerVisible(GravityCompat.END))
                drawerLayout.closeDrawer(GravityCompat.END);
            else drawerLayout.openDrawer(GravityCompat.END);
        });

        animateNavigationDrawer();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_note:
                Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                inputSearch.setText(null);
                break;
            case R.id.menu_add_image:
                showAddImageDialog();
                break;
            case R.id.menu_add_url:
                showAddURLDialog();
                break;
            case R.id.menu_share_noted:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "NOTED - Don't Bother Remembering!");
                String app_url = "https://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName();
                shareIntent.putExtra(Intent.EXTRA_TEXT, app_url);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                inputSearch.setText(null);
                drawerLayout.closeDrawer(GravityCompat.END);
                break;
        }
        return false;
    }

    private void animateNavigationDrawer() {
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);

                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffsetDiff - xOffset;
                contentView.setTranslationX(xTranslation);
            }
        });
    }

    private void showAddImageDialog() {
        if (dialogAddImage == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_image,
                    (ViewGroup) findViewById(R.id.layout_add_image_container)
            );
            builder.setView(view);

            dialogAddImage = builder.create();
            if (dialogAddImage.getWindow() != null) {
                dialogAddImage.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.layout_take_photo).setOnClickListener(v -> {
                takePhoto();
                dialogAddImage.dismiss();
            });

            view.findViewById(R.id.layout_add_image).setOnClickListener(v -> {
                selectImage();
                dialogAddImage.dismiss();
            });
        }
        dialogAddImage.show();

    }

    private void takePhoto() {
        ImagePicker.Companion.with(MainActivity.this)
                .cameraOnly()
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_TAKE_PHOTO);
    }

    private void selectImage() {
        ImagePicker.Companion.with(MainActivity.this)
                .galleryOnly()
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start(REQUEST_CODE_SELECT_IMAGE);
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private void showAddURLDialog() {
        if (dialogAddURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layout_add_url_container)
            );
            builder.setView(view);

            dialogAddURL = builder.create();
            if (dialogAddURL.getWindow() != null) {
                dialogAddURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.input_url);
            inputURL.requestFocus();

            view.findViewById(R.id.dialog_add_btn).setOnClickListener(v -> {
                if (inputURL.getText().toString().trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString().trim()).matches()) {
                    Toast.makeText(MainActivity.this, "Enter Valid URL", Toast.LENGTH_SHORT).show();
                } else {
                    dialogAddURL.dismiss();
                    UIUtil.hideKeyboard(view.getContext(), inputURL);
                    Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                    intent.putExtra("isFromQuickActions", true);
                    intent.putExtra("quickActionType", "URL");
                    intent.putExtra("URL", inputURL.getText().toString().trim());
                    startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    inputSearch.setText(null);
                }
            });

            view.findViewById(R.id.dialog_cancel_btn).setOnClickListener(v -> {
                UIUtil.hideKeyboard(view.getContext(), inputURL);
                dialogAddURL.dismiss();
            });
        }
        dialogAddURL.setCancelable(false);
        dialogAddURL.show();
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return (List<Note>) NoteDatabase
                        .getDatabase(getApplicationContext())
                        .noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                    if (drawerLayout.isDrawerVisible(GravityCompat.END))
                        drawerLayout.closeDrawer(GravityCompat.END);
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                    if (drawerLayout.isDrawerVisible(GravityCompat.END))
                        drawerLayout.closeDrawer(GravityCompat.END);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);
                    if (isNoteDeleted) {
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                    if (drawerLayout.isDrawerVisible(GravityCompat.END))
                        drawerLayout.closeDrawer(GravityCompat.END);
                }
            }
        }

        new GetNotesTask().execute();
    }

    @Override
    public void onNoteClicked(View view, Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);

        inputSearch.setText(null);
    }


    @Override
    public void onNoteLongClicked(View view, Note note, int position) {
        noteClickedPosition = position;
        view.setForeground(getDrawable(R.drawable.foreground_selected_note));
        if (actionMode != null) {
            return;
        }

        actionMode = startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.note_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.note_menu_edit:
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isViewOrUpdate", true);
                        intent.putExtra("note", note);
                        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
                        inputSearch.setText(null);
                        mode.finish();
                        return true;
                    case R.id.note_menu_share:
                        if (note.getImagePath() == null) {
                            String content = note.getTitle() + "\n\n" + note.getSubtitle() + "\n\n" + note.getNoteText();
                            Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
                            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
                            startActivity(Intent.createChooser(shareIntent, "Share via"));
                        } else {
                            String textContent = note.getTitle() + "\n\n" + note.getSubtitle() + "\n\n" + note.getNoteText();
                            Bitmap bitmap = BitmapFactory.decodeFile(note.getImagePath());
                            String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title", null);
                            Uri bitmapUri = Uri.parse(bitmapPath);
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("image/png");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, textContent);
                            startActivity(Intent.createChooser(shareIntent, "Share"));
                        }
                        mode.finish();
                        return true;
                    case R.id.note_menu_delete:
                        showDeleteNoteDialog(note);
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
                view.setForeground(null);
            }
        });
    }

    private void showDeleteNoteDialog(Note note) {
        MaterialDialog materialDialog = new MaterialDialog.Builder(MainActivity.this)
                .setTitle("Are you sure?")
                .setMessage("Are you sure you want to delete this note?")
                .setAnimation(R.raw.lottie_delete)
                .setCancelable(false)
                .setPositiveButton("Delete", R.drawable.ic_material_dialog_delete, (dialogInterface, which) -> {
                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NoteDatabase.getDatabase(getApplicationContext()).noteDao()
                                    .deleteNote(note);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            noteList.remove(noteClickedPosition);
                            notesAdapter.notifyItemRemoved(noteClickedPosition);

                            if (noteList.size() != 0) {
                                imageEmpty.setVisibility(View.GONE);
                                textEmpty.setVisibility(View.GONE);
                            } else {
                                imageEmpty.setVisibility(View.VISIBLE);
                                textEmpty.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    new DeleteNoteTask().execute();
                    dialogInterface.dismiss();
                })
                .setNegativeButton("Cancel", R.drawable.ic_material_dialog_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                .build();
        materialDialog.show();
    }


    // Khi k???t qu??? ???????c tr??? v??? t??? Activity kh??c, h??m onActivityResult s??? ???????c g???i.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Ki???m tra requestCode c?? tr??ng v???i REQUEST_CODE v???a d??ng
        // resultCode ???????c set b???i CreateNoteActivity
        // RESULT_OK ch??? ra r???ng k???t qu??? n??y ???? th??nh c??ng
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }

        } else if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (data != null) {
                Uri takePhotoUri = data.getData();
                if (takePhotoUri != null) {
                    try {
                        String selectedImagePath = getPathFromUri(takePhotoUri);

                        // T???o m???t Intent ????? start CreateNoteActivity
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);

                        // Start CreateNoteActivity v???i request code v???a ???????c khai b??o tr?????c ????
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                        inputSearch.setText(null);
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        String selectedImagePath = getPathFromUri(selectedImageUri);

                        // T???o m???t Intent ????? start CreateNoteActivity
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickActions", true);
                        intent.putExtra("quickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                        inputSearch.setText(null);
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }  else if (requestCode == RC_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(MainActivity.this, "App Update Failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END);
        else finishAffinity();
    }
}