package com.berkd.mylist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;



public class EditActivity extends AppCompatActivity{

    Button buttonGoBack;
    Button buttonSave;
    EditText editTextField;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        String mStringStore = intent.getExtras().getString("mStringStore"); // import the string
        //final int position = intent.getExtras().getInt("position");

        buttonGoBack = findViewById(R.id.buttonGoBack); // init button go back
        buttonSave = findViewById(R.id.buttonSave);
        editTextField = findViewById(R.id.editTextField); // init text field

        editTextField.setText(mStringStore);


        /**
         * GO BACK TO MAIN ACTIVITY!
         * This will do the trick.
         */
        buttonGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pass back the edited text to the main class
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                intent.putExtra("editedText", editTextField.getText().toString());
                startActivity(intent);
            }
        });
    }
}
