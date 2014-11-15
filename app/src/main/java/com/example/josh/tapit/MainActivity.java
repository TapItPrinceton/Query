package com.example.josh.tapit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;


public class MainActivity extends Activity {

    protected ProgressBar mProgressBar;
    private static final int MAX = 100; // change when we know class size, or keep as a percentage
    private boolean confused = false; // true = confused, false not confused
    private static final String CLASS_NAME = "Econ";
    private static final int GREEN = Color.parseColor("#228822");
    private static final int RED = Color.parseColor("#FF4500");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        // setup parse
        Parse.initialize(this, "mHMTVYaNUnLIcYWO7OyCrPy0Xi9DQcQvS28GKDkH", "PWy5jdBSWXV9VuyBKW7lmvQcJsPecnOJezcMbwfT");

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar); // make the progress bar
        mProgressBar.setVisibility(View.VISIBLE); // not sure if necessary, but makes the bar visible
        mProgressBar.setMax(MAX);

        // New relative layout to be used to change background color
        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);

        final Button updateButton = (Button) findViewById(R.id.button); // make the button

        final TextView status = (TextView) findViewById(R.id.textView);
        updateButton.setText(getString(R.string.button_text));
        status.setText(getString(R.string.status_positive));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("State");
                query.whereEqualTo("class", CLASS_NAME);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if (object == null) {
                            //failed
                        } else {
                            // retrieve success
                            if (confused) {
                                object.increment("current");
                                relativeLayout.setBackgroundColor(RED);
                                status.setText(getString(R.string.status_negative));

                            }
                            else {
                                object.increment("current", -1);
                                relativeLayout.setBackgroundColor(GREEN);
                                status.setText(getString(R.string.status_positive));
                            }

                            confused = !confused;
                            object.saveInBackground();
                            mProgressBar.setProgress(object.getInt("current"));
                        }
                    }
                });

            }
        };
        updateButton.setOnClickListener(listener);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}