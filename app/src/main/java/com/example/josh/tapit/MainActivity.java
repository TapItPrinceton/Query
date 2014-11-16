package com.example.josh.tapit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends Activity {

    protected ProgressBar mProgressBar;
    protected TextView status;
    protected Button updateButton; // button to change state
    protected ListView listView; // listview to hold top questions
    private RelativeLayout relativeLayout;
    private static final int MAX = 100; // change when we know class size
    private boolean confused = true; // true = confused, false = not confused
    private String class_name = "";
    private static final int GREEN = Color.parseColor("#228822");
    private static final int RED = Color.parseColor("#FF4500");
    private static Date tempDate;
    private static final int Q_DURATION = 30;

    //======================
    private int mInterval = 500; // 0.5 seconds
    //======================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set tempDate
        Calendar cal = Calendar.getInstance();
        cal.set(2014, Calendar.NOVEMBER, 16); //Year, month and day of month
        tempDate = cal.getTime();



        // Starts LoginActivity class
        Intent resultIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(resultIntent, 1);

        // setup Parse
        Parse.initialize(this, "mHMTVYaNUnLIcYWO7OyCrPy0Xi9DQcQvS28GKDkH", "PWy5jdBSWXV9VuyBKW7lmvQcJsPecnOJezcMbwfT");

        // Makes progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar); // make the progress bar
        mProgressBar.setVisibility(View.VISIBLE); // not sure if necessary, but makes the bar visible
        mProgressBar.setMax(MAX);

        // List of top questions
        listView = (ListView) findViewById(R.id.listView);

        // New relative layout to be used to change background color
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);

        // Make the button and sets String inside
        updateButton = (Button) findViewById(R.id.button);
        updateButton.setText(getString(R.string.button_text));

        // Sets the initial status to positive
        status = (TextView) findViewById(R.id.textView);
        status.setText(getString(R.string.status_positive));


        // Input Question
        final EditText questionText = (EditText) findViewById(R.id.questionText);
        questionText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    sendQuestion(questionText.getText().toString());
                    questionText.setText("");
                    handled = true;
                }
                return handled;

            }
        });
    }

    // runs after LoginActivity, and starts the constant data fetching functions. Also sets up button
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == RESULT_OK) {
                    class_name = data.getStringExtra("class");
                    getConfusionData();
                    getQuestionData();
                    deleteOldQuestions();
                    buttonClicked();
                }
                break;
            }
        }
    }

    private void sendQuestion(String question) {
        ParseObject questions = new ParseObject("eco101Questions");
        questions.put("question", question);
        questions.put("votes", 1);
        questions.saveInBackground();
    }

    protected void buttonClicked() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("State");
                query.whereEqualTo("class", class_name);
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

                            } else {
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

    public void getConfusionData() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            public void run() {

                ParseQuery<ParseObject> query = ParseQuery.getQuery("State");
                query.whereEqualTo("class", class_name);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if (object == null) {
                            //failed
                        } else {
                            mProgressBar.setProgress(object.getInt("current"));
                        }
                    }
                });
                handler.postDelayed(this, mInterval);
            }
        }, mInterval);
    }

    public void getQuestionData() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            public void run() {

                ParseQuery<ParseObject> query = ParseQuery.getQuery(class_name + "Questions");
                query.orderByDescending("votes");
                query.setLimit(3);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, ParseException e) {
                        if (e == null) {
                            //post to screen
                            ArrayList<String> questions = new ArrayList<String>();
                            for (int i = 0; i < scoreList.size(); i++) {
                                questions.add(i+1+ ". " + scoreList.get(i).getString("question"));
                            }

                            postTopQuestions(questions);

                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                });
                handler.postDelayed(this, mInterval);
            }
        }, mInterval);
    }

    public void deleteOldQuestions() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            public void run() {

                ParseQuery<ParseObject> query = ParseQuery.getQuery(class_name + "Questions");
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, ParseException e) {
                        if (e == null) {
                            for (int i = 0; i < scoreList.size(); i++) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(scoreList.get(i).getCreatedAt());
                                cal.add(Calendar.SECOND, Q_DURATION);
                                cal.add(Calendar.MINUTE, 1);
                                cal.add(Calendar.SECOND, -10);
                                Date date = cal.getTime();
                                Date currentDate = Calendar.getInstance().getTime();

                                if (currentDate.after(date))
                                    scoreList.get(i).deleteInBackground();
                            }

                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                });
                handler.postDelayed(this, mInterval);
            }
        }, mInterval);

    }

    public void postTopQuestions(ArrayList<String> questions) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                        (this, android.R.layout.simple_list_item_1, questions);
        listView.setAdapter(adapter);
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