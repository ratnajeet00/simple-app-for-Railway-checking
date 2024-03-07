package com.example.madmicroproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://vulture-on-treefrog.ngrok-free.app"; // Replace with your ngrok URL
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private EditText etSearch;
    private LinearLayout flightsContainer;
    private OkHttpClient client = new OkHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSearch = findViewById(R.id.etSearch);
        flightsContainer = findViewById(R.id.flightsContainer);

        FloatingActionButton fabAdd = findViewById(R.id.btnAddTicket);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTrainDialog();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Handle search as the user types
                filterTickets(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        new FetchTrainsTask().execute();
    }

    private void filterTickets(String ticketId) {
        for (int i = 0; i < flightsContainer.getChildCount(); i++) {
            View view = flightsContainer.getChildAt(i);
            if (view instanceof CardView) {
                CardView cardView = (CardView) view;
                TextView ticketIdTextView = cardView.findViewById(R.id.etSearch);
                if (ticketIdTextView != null) {
                    String currentTicketId = ticketIdTextView.getText().toString().replace("Ticket ID: ", "");
                    if (currentTicketId.contains(ticketId)) {
                        cardView.setVisibility(View.VISIBLE);
                    } else {
                        cardView.setVisibility(View.GONE);
                    }
                }
            }
        }
    }


    private void showAddTrainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Train");

        // Set up the layout for the dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        // Add EditTexts for train details
        EditText editTextName = new EditText(this);
        editTextName.setHint("Name");
        layout.addView(editTextName);

        EditText editTextTrainNo = new EditText(this);
        editTextTrainNo.setHint("Train No");
        layout.addView(editTextTrainNo);

        EditText editTextDestination = new EditText(this);
        editTextDestination.setHint("Destination");
        layout.addView(editTextDestination);

        // Set up the positive and negative buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editTextName.getText().toString();
                String trainNo = editTextTrainNo.getText().toString();
                String destination = editTextDestination.getText().toString();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(trainNo) && !TextUtils.isEmpty(destination)) {
                    // Call your method to send data to API
                    sendDataToApi(name, trainNo, destination, null);
                } else {
                    Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setView(layout);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class FetchTrainsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Use OkHttp to make a GET request to your server
                Request request = new Request.Builder()
                        .url(BASE_URL + "/traintickets")
                        .build();

                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);

                // Check if the "tickets" key is present in the JSON response
                if (jsonObject.has("tickets")) {
                    JSONArray ticketsArray = jsonObject.getJSONArray("tickets");

                    // Process each ticket in the array
                    for (int i = 0; i < ticketsArray.length(); i++) {
                        JSONObject ticketObject = ticketsArray.getJSONObject(i);

                        String name = ticketObject.getString("Name");
                        String trainNo = ticketObject.getString("Train_no");
                        String destination = ticketObject.getString("Train_destination");
                        String ticketId = String.valueOf(ticketObject.getInt("id"));

                        // Create a CardView for each ticket
                        createCardView(name, trainNo, destination, ticketId);
                    }
                } else {
                    // Handle the case when the "tickets" key is not present
                    Log.e("FetchTrainsTask", "No 'tickets' key in the JSON response");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private CardView createCardView(String name, String trainNo, String destination, String ticketId) {
        CardView cardView = new CardView(MainActivity.this);
        LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardLayoutParams.setMargins(0, 0, 0, 16);
        cardView.setLayoutParams(cardLayoutParams);
        cardView.setRadius(8);
        cardView.setContentPadding(16, 16, 16, 16);

        LinearLayout mainLayout = new LinearLayout(MainActivity.this);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // Add TextViews for train details
        TextView nameTextView = new TextView(MainActivity.this);
        nameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        nameTextView.setText("Name: " + name);

        TextView trainNoTextView = new TextView(MainActivity.this);
        trainNoTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        trainNoTextView.setText("Train No: " + trainNo);

        TextView destinationTextView = new TextView(MainActivity.this);
        destinationTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        destinationTextView.setText("Destination: " + destination);

        TextView statusTextView = new TextView(MainActivity.this);
        statusTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        statusTextView.setText("Status: notcheckedin");

        // Add this TextView for Ticket ID
        TextView ticketIdTextView = new TextView(MainActivity.this);
        ticketIdTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        // Set it to visible
        ticketIdTextView.setVisibility(View.VISIBLE);
        ticketIdTextView.setText("Ticket ID: " + ticketId);

        // Add the check mark button
        Button btnTick = new Button(MainActivity.this);
        LinearLayout.LayoutParams btnTickLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnTickLayoutParams.gravity = Gravity.END;
        btnTickLayoutParams.rightMargin = 8;
        btnTick.setLayoutParams(btnTickLayoutParams);
        btnTick.setText("✔");

        // Add the cross button
        Button btnCross = new Button(MainActivity.this);
        LinearLayout.LayoutParams btnCrossLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnCrossLayoutParams.gravity = Gravity.END;
        btnCross.setLayoutParams(btnCrossLayoutParams);
        btnCross.setText("X");

        // Set the onClickListener for check-in button
        btnTick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Perform check-in using the ticketId
                performCheckIn(ticketId);
            }
        });

        mainLayout.addView(nameTextView);
        mainLayout.addView(trainNoTextView);
        mainLayout.addView(destinationTextView);
        mainLayout.addView(statusTextView);
        mainLayout.addView(ticketIdTextView);
        mainLayout.addView(btnTick);
        mainLayout.addView(btnCross);

        cardView.addView(mainLayout);

        flightsContainer.addView(cardView);
        return cardView;
    }

    private void performCheckIn(String ticketId) {
        // Prepare JSON data for the check-in request
        JSONObject json = new JSONObject();
        try {
            json.put("ticketId", ticketId);
            json.put("status", "checkedin");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Build the request
        RequestBody requestBody = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/checkin")
                .post(requestBody)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // Handle failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle successful check-in
                    // You may want to update the UI or perform other actions
                    Log.d("Check-In", "Successful Check-In");
                } else {
                    // Handle unsuccessful check-in
                    Log.e("Check-In", "Unsuccessful Check-In");
                }
            }
        });
    }

    private void sendDataToApi(String name, String trainNo, String destination, TextView textViewTicketId) {
        // Prepare JSON data for the API request
        JSONObject json = new JSONObject();
        try {
            json.put("Name", name);
            json.put("Train_no", trainNo);
            json.put("Train_destination", destination);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Build the request
        RequestBody requestBody = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "/getdata")
                .post(requestBody)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // Handle failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle successful data submission
                    // You may want to update the UI or perform other actions
                    Log.d("Data Submission", "Successful Data Submission");
                    Toast.makeText(MainActivity.this, "data added", Toast.LENGTH_SHORT).show();
                    new FetchTrainsTask().execute();
                } else {
                    // Handle unsuccessful data submission
                    Log.e("Data Submission", "Unsuccessful Data Submission");
                }
            }
        });
    }
}
