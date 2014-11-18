package com.example.josh.tapit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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


public class MainActivity extends Activity implements GestureDetector.OnGestureListener {

    protected ProgressBar mProgressBar;
    protected TextView status;
    protected Button updateButton; // button to change state
    protected ListView listView; // listview to hold top questions
    protected TextView queueView;
    private RelativeLayout relativeLayout;
    private static final int MAX = 20; // change when we know class size
    private boolean confused = true; // true = confused, false = not confused
    private String class_name = "";
    private static final int GREEN = Color.parseColor("#4dff38");
    private static final int RED = Color.parseColor("#FF4500");
    private static Date lastDate;
    private static final int Q_DURATION = 12000000;
    private GestureDetector mDetector;
    private ArrayList<String[]> Question_queue;

    //======================
    private int mInterval = 2000; // 1 seconds
    //======================
    //looooool this is josh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        lastDate = cal.getTime();


        Question_queue = new ArrayList<String[]>();

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
        //updateButton.setText(getString(R.string.button_text));

        // Sets the initial status to positive
        //status = (TextView) findViewById(R.id.textView);
        //status.setText(getString(R.string.status_positive));
        updateButton.setText(getString(R.string.status_positive));
        updateButton.setTextColor(Color.parseColor("#ffffff"));

        // Setup queue question view
        queueView = (TextView) findViewById(R.id.currentQuestion);
        queueView.setText("");

        final InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);

        // Input Question
        final EditText questionText = (EditText) findViewById(R.id.questionText);

        questionText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    sendQuestion(questionText.getText().toString());
                    questionText.setText("");
                    questionText.clearFocus();
                    imm.hideSoftInputFromWindow(questionText.getWindowToken(), 0);
                    handled = true;
                }

                return handled;

            }
        });


        // Swipey thing
        mDetector = new GestureDetector(this, this);
        listView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent me) {
                mDetector.onTouchEvent(me);
                return mDetector.onTouchEvent(me);
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
                    updateQuestionQueue();
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
        Toast.makeText(this, "Your question has been submitted.", Toast.LENGTH_SHORT).show();
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
                                object.increment("current", -1);
                                //relativeLayout.setBackgroundColor(RED);
                                updateButton.setText(getString(R.string.status_negative));
                                //updateButton.setBackgroundColor(RED);
                                updateButton.setTextColor(Color.parseColor("#FFFFFF"));

                            } else {
                                object.increment("current");
                                //relativeLayout.setBackgroundColor(GREEN);
                                updateButton.setText(getString(R.string.status_positive));
                                //updateButton.setBackgroundColor(GREEN);
                                updateButton.setTextColor(Color.parseColor("#FFFFFF"));
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
                                questions.add(scoreList.get(i).getString("question"));
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

    public void updateQuestionQueue() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            public void run() {

                ParseQuery<ParseObject> query = ParseQuery.getQuery(class_name + "Questions");
                query.orderByAscending("createdAt");
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> scoreList, ParseException e) {
                        if (e == null) {
                            for (int i = 0; i < scoreList.size(); i++) {

                                Date date = scoreList.get(i).getCreatedAt();

                                if (lastDate.before(date))
                                {
                                    Question_queue.add(new String[]{scoreList.get(i).getString("question"),
                                            scoreList.get(i).getObjectId()});
                                }
                            }
                            if (!scoreList.isEmpty()) {
                                lastDate = scoreList.get(scoreList.size() - 1).getCreatedAt();
                                if ((queueView.getText().equals("No more new questions!") ||
                                        queueView.getText().equals("")) && !Question_queue.isEmpty())
                                {
                                    queueView.setText(Question_queue.get(0)[0]);
                                }
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
    public boolean onDown(MotionEvent arg0) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        float sensitvity = 1;
        if (!Question_queue.isEmpty()) {
            if ((e1.getX() - e2.getX()) > sensitvity && e1.getY() > queueView.getTop() - 150 && e2.getY() > queueView.getTop() - 150) {
                //left fling
                if (e1.getY() < queueView.getTop() + queueView.getHeight() + 150 && e2.getY() < queueView.getTop() + queueView.getHeight() + 150) {

                    ParseQuery<ParseObject> query = ParseQuery.getQuery(class_name + "Questions");
                    query.whereEqualTo("objectId", Question_queue.get(0)[1]);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            if (object == null) {
                                //failed
                            } else {
                                // retrieve success
                                object.increment("votes", -1);
                                object.saveInBackground();
                            }
                        }
                    });
                    Question_queue.remove(0);
                    if (!Question_queue.isEmpty())
                        queueView.setText(Question_queue.get(0)[0]);
                    else
                        queueView.setText("No more new questions!");
                }
            } else if ((e2.getX() - e1.getX()) > sensitvity && e1.getY() > queueView.getTop() - 150 && e2.getY() > queueView.getTop() - 150) {
                //right fling
                if (e1.getY() < queueView.getTop() + queueView.getHeight() + 150 && e2.getY() < queueView.getTop() + queueView.getHeight() + 150) {

                    ParseQuery<ParseObject> query = ParseQuery.getQuery(class_name + "Questions");
                    query.whereEqualTo("objectId", Question_queue.get(0)[1]);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            if (object == null) {
                                //failed
                            } else {
                                // retrieve success
                                object.increment("votes");
                                object.saveInBackground();
                            }
                        }
                    });
                    Question_queue.remove(0);
                    if (!Question_queue.isEmpty())
                        queueView.setText(Question_queue.get(0)[0]);
                    else
                        queueView.setText("No more new questions!");
                }
            }
        }

        else {
            queueView.setText("No more new questions!");
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
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