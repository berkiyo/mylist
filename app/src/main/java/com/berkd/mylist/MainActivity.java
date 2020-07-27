package com.berkd.mylist;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ArrayList<MainItem> mMainList;
    private RecyclerView mRecyclerView;
    private ExampleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    String[] FontSizes = {"Small", "Medium", "Large", "Extra Large"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadData();
        buildRecyclerView();
        setInsertButton();
        cardMover();
        loadFont();
        darkModeEnabled();

        /*
        SharedPreferences spinnerPref = getSharedPreferences("SpinnerData", MODE_PRIVATE);
        int spinnerValue = spinnerPref.getInt("userChoiceSpinner", -1);
        if (spinnerValue != -1) {
            mSpinner.setSelection(spinnerValue);
        }
*/

    }

    public void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ExampleAdapter(mMainList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new ExampleAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(final int position) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setTitle("Delete Item");
                mBuilder.setMessage("Are you sure?");


                mBuilder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeItem(position);
                        dialog.dismiss();
                    }
                });

                mBuilder.setPositiveButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });

                AlertDialog dialog = mBuilder.create();
                dialog.show();

            }

            @Override
            public void onDeleteClick(final int position) {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setTitle("Delete Item");
                mBuilder.setMessage("Are you sure?");


                mBuilder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeItem(position);
                        dialog.dismiss();
                    }
                });

                mBuilder.setPositiveButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });

                AlertDialog dialog = mBuilder.create();
                dialog.show();


            }

            @Override
            public void onEditClick(int position) { editItem(position); }
        });
    }

    /**
     * REMOVE ITEM METHOD - From list...
     */
    public void removeItem(int position) {
        mMainList.remove(position);
        mAdapter.notifyItemRemoved(position);
    }


    /**
     * CHANGE ITEM
     *  To be used to change the text, called after the dialog is closed (below)
     */
    public void changeItem(int position, String text) {
        mMainList.get(position).changeText1(text);
        mAdapter.notifyItemChanged(position);
    }

    /**
     * EDIT ITEM
     *  Edit the text!
     */
    public void editItem(final int position) {
/*
        final String mStringStore = mMainList.get(position).getText1();
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        intent.putExtra("mStringStore", mStringStore); //  passthru the string
        intent.putExtra("position", position);
        startActivity(intent);
*/
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this,android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        View mView = getLayoutInflater().inflate(R.layout.edit_popup, null);

        final EditText mEditText = (EditText) mView.findViewById(R.id.editTextField);
        final String mStringStore = mMainList.get(position).getText1();

        mEditText.setText(mStringStore); // set the text value to the value select from the position
        mBuilder.setView(mView);


        mBuilder.setTitle("Editing");

        mBuilder.setNegativeButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeItem(position, mEditText.getText().toString());
                saveData();
                dialog.dismiss();
            }
        });

        mBuilder.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });

        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }




    private void setInsertButton() {
        Button buttonInsert = findViewById(R.id.button_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText itemEntry = findViewById(R.id.itemEntry); // The text entry thingy
                /**
                 * Give error that textfield cannot be empty
                 */
                if (itemEntry.getText().toString().matches("")) {
                    itemEntry.setError("Enter a task first!");
                } else {
                    Calendar calendar = Calendar.getInstance(); // Get current time and make it the subtext
                    String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime()); // format date
                    insertItem(itemEntry.getText().toString(), currentDate);

                    saveData(); // save list once item is added
                    clearTextEntry(itemEntry); // clear the item entry field
                }

            }
        });
    }

    /**
     * SAVE_DATA
     *  Handles saving data to the GSON library & SharedPref lib
     */
    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mMainList); // Holds our list in JSON format
        editor.putString("task list", json);
        editor.apply();
    }


    /**
     * INSERT_ITEM
     *  Insert data into the list, self explanatory.
     */
    private void insertItem(String line1, String line2) {
        mMainList.add(new MainItem(line1, line2));
        mAdapter.notifyItemInserted(mMainList.size());
    }

    /**
     * Simply clear the text field
     */
    public void clearTextEntry(EditText e) {
        e.setText("");
        Toast.makeText(this, "Item Added!", Toast.LENGTH_SHORT).show(); // Had to put it here, just adds toast

    }


    /**
     * LOAD_DATA
     *  Loads the data on startup
     */
    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("task list", null);
        Type type = new TypeToken<ArrayList<MainItem>>() {}.getType();
        mMainList = gson.fromJson(json, type);

        // It can be null
        if (mMainList == null) {
            mMainList = new ArrayList<>();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * ESSENTIAL FOR MENU CREATION!
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    /** /////////////////////// M E N U ////////////////////////
     * WHEN OPTIONS MENU IS SELECT, CURRENTLY DOES NOT MUCH BUT WIP
     * TODO -> Get these functions things implemented (CURRENTLY WIP)
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            // item1 = Clear List
                // Standalone. Just does one thing.
            case R.id.item1:
                // Prompt the user if they really want to clear the list.
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setTitle("Clear List");
                mBuilder.setMessage("Are you sure?");


                mBuilder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMainList.clear();
                        mAdapter.notifyDataSetChanged();
                        saveData();
                        dialog.dismiss();
                    }
                });

                mBuilder.setPositiveButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });

                AlertDialog dialog = mBuilder.create();
                dialog.show();


                break;

            // item 2 = font settings
            case R.id.item2:
                settingsPopup();
                break;

            // item3 = about
            case R.id.item3:
                aboutPopup();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void saveSharedPreferences()
    {
        // create some junk data to populate the shared preferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();
        prefEdit.putBoolean("SomeBooleanValue_True", true);
        prefEdit.putInt("SomeIntValue_100", 100);
        prefEdit.putFloat("SomeFloatValue_1.11", 1.11f);
        prefEdit.putString("SomeStringValue_Unicorns", "Unicorns");
        prefEdit.commit();

        // BEGIN EXAMPLE
        File myPath = new File(Environment.getExternalStorageDirectory().toString());
        File myFile = new File(myPath, "MySharedPreferences");

        try
        {
            FileWriter fw = new FileWriter(myFile);
            PrintWriter pw = new PrintWriter(fw);

            Map<String,?> prefsMap = prefs.getAll();

            for(Map.Entry<String,?> entry : prefsMap.entrySet())
            {
                pw.println(entry.getKey() + ": " + entry.getValue().toString());
            }

            pw.close();
            fw.close();
        }
        catch (Exception e)
        {
            // what a terrible failure...
            Log.wtf(getClass().getName(), e.toString());
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * About_Popup
     *  Just initialise the about-popup
     */
    public void aboutPopup() {



        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.about_popup, null);
        mBuilder.setTitle("About");

        mBuilder.setNeutralButton("PROJECT PAGE", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri projectPage = Uri.parse("https://www.tekbyte.net/mylist-app");
                Intent intent = new Intent(Intent.ACTION_VIEW, projectPage);
                startActivity(intent);
            }
        });

        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }

    /**
     * Dark theme popup
     */
    public void darkThemePopup() {

        Toast.makeText(this, "Dark Theme Toggled", Toast.LENGTH_SHORT).show();
    }

    /**
     * Backup and restore popup (maybe intent?)
     */
    public void backupPopup() {


    }


    /**
     * Donate_Popup
     */
    public void donatePopup() {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.donate_popup, null);
        mBuilder.setTitle("Donate");

        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }


    public void darkModeEnabled() {
        int darkStatus = getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        ActionBar actionBar = getSupportActionBar();
        Button addButton = findViewById(R.id.button_insert);

        if (darkStatus == Configuration.UI_MODE_NIGHT_YES) {

        }


    }

    /**
     * Triggered when user is about to clear the mainlist
     */

    public void cardMover() {
        ItemTouchHelper touchHelps = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP |
                ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = dragged.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Collections.swap(mMainList, position_dragged, position_target);
                mAdapter.notifyItemMoved(position_dragged, position_target);

                return false;
            }

            /**
             * TAP FUNCTIONALITY
             *  When tapped, edit the task.
             *  TODO -> Need to implement this properly - for now only hold and drag works.
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder target, int direction) {
                /*
                int position = target.getAdapterPosition();
                mMainList.remove(position);
                mAdapter.notifyItemRemoved(position);
                */
            }
        });

        touchHelps.attachToRecyclerView(mRecyclerView);

    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * SAVING WHEN QUIT
     *  Save the program when user exits app but also prompt them to double press back button.
     */
    @Override
    public void onBackPressed() {
        saveData(); // Save the list! Very important.
        super.onBackPressed();
    }
    @Override
    public void onPause() {
        saveData();
        super.onPause();
    }
    @Override
    public void onStop() {
        saveData();
        super.onStop();
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * SETTINGS POPUP
     *  Handles the settings popup
     */

    public void settingsPopup() {
        final int defaultFont = 17;
        final int smallFont = 14;
        final int largeFont = 21;




        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.settings_popup, null);
        mBuilder.setTitle("Font Size");

        final Spinner mSpinner = mView.findViewById(R.id.font_spinner);

        final ArrayAdapter<String> sAdapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.fontsizes));

        sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(sAdapter);

        // Load the previously saved font selection!
        mSpinner.setSelection(loadSpinnerState());
        //

        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /////////////////////////////////////////////////////// HANDLING SPINNER SAVE


                /////////////////////////////////////////////////////

                if(mSpinner.getSelectedItem().toString().equalsIgnoreCase("default")) {
                    Toast.makeText(MainActivity.this, "Default Set", Toast.LENGTH_SHORT).show();

                    mAdapter.setTextSizes(defaultFont);
                    mAdapter.notifyDataSetChanged();

                    saveFont(defaultFont);
                    saveSpinnerState(0);

                    dialog.dismiss();
                }
                if(mSpinner.getSelectedItem().toString().equalsIgnoreCase("small")) {
                    Toast.makeText(MainActivity.this, "Small Set", Toast.LENGTH_SHORT).show();

                    mAdapter.setTextSizes(smallFont);
                    mAdapter.notifyDataSetChanged();

                    saveFont(smallFont);
                    saveSpinnerState(1);

                    dialog.dismiss();
                }
                if(mSpinner.getSelectedItem().toString().equalsIgnoreCase("large")) {
                    Toast.makeText(MainActivity.this, "Large Set", Toast.LENGTH_SHORT).show();

                    mAdapter.setTextSizes(largeFont);
                    mAdapter.notifyDataSetChanged();
                    saveFont(largeFont);
                    saveSpinnerState(2);


                    dialog.dismiss();
                }
            }
        });

        mBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }



/////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * SAVE_FONT
     *  Used to set the font size and save it to a shared-preference db
     */
    public void saveFont(int size){
        SharedPreferences sharedPref = getSharedPreferences("fontsize", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("fontsize",size);
        editor.commit();
    }

    /**
     * LOAD_FONT
     *  Used to load the saved font at start-up
     */
    public void loadFont(){
        SharedPreferences sharedPref = getSharedPreferences("fontsize", Context.MODE_PRIVATE);
        int prevFont = sharedPref.getInt("fontsize", -1);
        mAdapter.setTextSizes(prevFont);
        mAdapter.notifyDataSetChanged();

    }

    /**
     * SAVE_SPINNER_STATE
     *  Store the position of the spinner
     */
    public void saveSpinnerState(int userChoice) {
        SharedPreferences sharedPref = getSharedPreferences("FileName",0);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putInt("userChoiceSpinner", userChoice);
        prefEditor.commit();
    }

    /**
     * LOAD_SPINNER_STATE
     *  Return the value of the previously selected font size, return integer array value
     */
    public int loadSpinnerState() {
        SharedPreferences sharedPref = getSharedPreferences("FileName",MODE_PRIVATE);
        int spinnerValue = sharedPref.getInt("userChoiceSpinner",-1);
        return spinnerValue;
    }

}
