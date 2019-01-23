package com.example.qazi.nearfield;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 1;
    private FusedLocationProviderClient client;
    private double latitude = -1, longitude = -1;
    private FirebaseListAdapter<ChatMessage> adapter;
    private String group_no = "";
    private MobileServiceClient mClient;

    @SuppressLint("Missing Permission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            //Toast.makeText(MainActivity.this, "new user", Toast.LENGTH_LONG).show();
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
            func();

        } else {
            // User is already signed in. Therefore, display
            // a welcome Toast
            Toast.makeText(this,
                    "Welcome " + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();

            // Load chat room contents
            func();
        }
        FloatingActionButton fab =
                findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                group_no = Integer.toString((int)(latitude*100)) + Integer.toString((int)(longitude*100));
                EditText input = findViewById(R.id.input);
                String empty = new String();
                if (input.getText().toString().equals(empty) == false) {
                    // Read the input field and push a new instance
                    // of ChatMessage to the Firebase database
                    ChatMessage msg = new ChatMessage(input.getText().toString(),
                            FirebaseAuth.getInstance()
                                    .getCurrentUser().
                                    getDisplayName(),
                            latitude,
                            longitude);

                    FirebaseDatabase.getInstance()
                            .getReference("messages/group" + group_no)
                            .push()
                            .setValue(msg);

                    // Clear the input
                    input.setText("");
                }
            }
        });
        /*try {
            mClient = new MobileServiceClient(
                    "https://nearfieldcodefundo.azurewebsites.net",
                    this
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        TodoItem item = new TodoItem();
        item.Text = "user added";
        mClient.getTable(TodoItem.class).insert(item, new TableOperationCallback<TodoItem>() {
            public void onCompleted(TodoItem entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    // Insert succeeded
                } else {
                    // Insert failed
                }
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Close activity
                            finish();
                        }
                    });
        }
        return true;
    }

    private void displayChatMessages() {
        //Toast.makeText(MainActivity.this, "Display chat", Toast.LENGTH_LONG).show();
        group_no = Integer.toString((int)(latitude*100))  + Integer.toString((int)(longitude*100));
        //Toast.makeText(MainActivity.this, "Group: "+group_no, Toast.LENGTH_LONG).show();
        Query query = FirebaseDatabase.getInstance().getReference().child("messages/group" + group_no);
//The error said the constructor expected FirebaseListOptions - here you create them:
        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .setLayout(R.layout.message)
                .build();
        //Finally you pass them to the constructor here:
        adapter = new FirebaseListAdapter<ChatMessage>(options){
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xmlf
                TextView messageText = v.findViewById(R.id.message_text);
                TextView messageTime = v.findViewById(R.id.message_time);
                TextView messageUser = v.findViewById(R.id.message_user);
                TextView messagelatitude = v.findViewById(R.id.message_latitude);
                TextView messagelongitude = v.findViewById(R.id.message_longitude);

                // Set their text
                messageText.setText(" " + model.getMessageText());
                // Format the date before showing it
                messageTime.setText(" " + DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));
                messageUser.setText(model.getMessageUser() + " ");
                messagelatitude.setText("(" + Double.toString(model.getLatitude()) + ", ");
                messagelongitude.setText(Double.toString(model.getLongitude()) + ")");
            }
        };

        ListView listOfMessages = findViewById(R.id.list_of_messages);
        listOfMessages.setAdapter(adapter);

        adapter.startListening();
}

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Successfully signed in. Welcome!",
                        Toast.LENGTH_LONG)
                        .show();
                displayChatMessages();
            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();
                // Close the app
                finish();
            }
        }
    }

    public void func() {
        client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if(location!=null)
                {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Toast.makeText(MainActivity.this, "Coordinates: "+latitude + " " + longitude, Toast.LENGTH_LONG).show();
                    setActionBar("NearField | (" + Double.toString(Math.round(latitude*100d)/100d) + ", " + Double.toString(Math.round(longitude*100d)/100d) + ")");
                    displayChatMessages();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setActionBar(String heading) {
        // TODO Auto-generated method stub

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setHomeButtonEnabled(true);
        //actionBar.setDisplayHomeAsUpEnabled(false);
        //actionBar.setDisplayShowHomeEnabled(false);
        //actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.title_bar_gray)));
        actionBar.setTitle(heading);
        actionBar.show();

    }
}