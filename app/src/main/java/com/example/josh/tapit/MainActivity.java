package com.example.josh.tapit;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.parse.Parse;
import com.parse.ParseObject;


public class MainActivity extends Activity {

    protected ProgressBar mProgressBar;
    private int current;
    private static final int MAX = 100; // change when we know class size, or keep as a percentage
    private boolean confused = false; // true = confused, false not confused

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup parse
        Parse.initialize(this, "mHMTVYaNUnLIcYWO7OyCrPy0Xi9DQcQvS28GKDkH", "PWy5jdBSWXV9VuyBKW7lmvQcJsPecnOJezcMbwfT");

        final ParseObject state = new ParseObject("State");
        state.put("current", 0);
        state.saveInBackground();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar); // make the progress bar
        mProgressBar.setVisibility(View.VISIBLE); // not sure if necessary, but makes the bar visible
        mProgressBar.setMax(MAX);

        // New relative layout to be used to change background color
        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);

        final Button updateButton = (Button) findViewById(R.id.button); // make the button

        // parse branch comment
        
        /*
        ParseQuery<ParseObject> query = ParseQuery.getQuery("State");
        query.getInBackground("mHMTVYaNUnLIcYWO7OyCrPy0Xi9DQcQvS28GKDkH", new GetCallback<query>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    object.increment("current");
                    object.saveInBackground();
                } else {
                    // something went wrong
                }
            }
        });*/

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                current++; // increment count
                mProgressBar.setProgress(current);
                state.increment("current");
                state.saveInBackground();

                confused = !confused; // Switch confused state

                if(confused) {
                    relativeLayout.setBackgroundColor(Color.RED);
                    updateButton.setText("I Get It.");
                }
                else {
                    relativeLayout.setBackgroundColor(Color.GREEN);
                    updateButton.setText("I'm Confused!");
                }


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
