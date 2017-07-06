package com.example.oczk.accelerometergame;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.lang.Math.abs;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;


public class AccActivity extends AppCompatActivity implements SensorEventListener {

    //android stuff
    private TextView lin1, lin2, lin3, lin4, msg, score;
    private Sensor accSensor, proxSensor, lightSensor;
    private SensorManager SM;

    String highscores = "0";

    //variables for acceleration in planes
    float x, y, z;

    //variable for "start" button
    boolean isStartClicked = false;

    //physics stuff;
    float acc = 0, distance, time;
    float mediumAcc = 0;

    //method for time counter
    boolean isCounterStarted = false;
    boolean isTiming = false;

    void startCounting() {
        isCounterStarted = true;
        isTiming = true;
    }

    //variables for time
    long startTime;
    long stopTime;

    //method for checking if device is in movement
    void isStarted() {
        if (acc > 1.5) {
            stopListener = true;
        }
    }

    boolean stopListener = false;

    float multiplier = 1;
    int points;

    //for proximity sensor
    boolean isCeilingReached = false;


    void showScore() {
        lin3.setTextSize(30);
        lin3.setText("Points: " + points);
        if (points < 10) {
            lin3.setTextColor(Color.rgb(51, 51, 51));
            msg.setTextSize(14);
            msg.setTextColor(Color.rgb(239, 255, 0));
            msg.setText("What was that?!");
        } else if (points >= 10 && points < 50) {
            lin3.setTextColor(Color.rgb(102, 102, 102));
            msg.setTextSize(16);
            msg.setTextColor(Color.rgb(185, 185, 0));
            msg.setText("You begin to learn!");
        } else if (points >= 50 && points < 150) {
            lin3.setTextColor(Color.rgb(153, 153, 153));
            msg.setTextSize(18);
            msg.setTextColor(Color.rgb(215, 226, 53));
            msg.setText("Try harder!");
        } else if (points >= 150 && points < 200) {
            lin3.setTextColor(Color.rgb(204, 204, 204));
            msg.setTextSize(20);
            msg.setTextColor(Color.rgb(228, 236, 109));
            msg.setText("Not bad!");
        } else if (points >= 200) {
            lin3.setTextColor(Color.rgb(255, 255, 255));
            msg.setTextSize(16);
            msg.setTextColor(Color.rgb(255, 255, 255));
            msg.setText("Hope your mobile is still in one piece...");
        }

        //if height should be on highscore, show Save button
        if (points > Integer.parseInt(highscores)) {
            buttonSave.setVisibility(View.VISIBLE);
            score.setText("Grats! You get yourself on the HighScore Table!");


        }


        if (isCeilingReached) {
            lin1.setText("Watch out for ceiling!");
        }
    }

    void resetMessages() {
        score.setText("");
        msg.setText("");
        lin1.setText("");
        lin2.setText("");
        lin3.setText("");
        lin4.setText("");
    }

    void resetValues() {
        isTiming = false;
        isCounterStarted = false;
        stopListener = false;
        isStartClicked = false;
        isCeilingReached = false;
        startTime = 0;
        stopTime = 0;
        time = 0;
        mediumAcc = 0;
        multiplier = 1;

    }

    Button buttonSave;
    Button buttonStart;


    public void writeScore(View view) {
        String msg = Integer.toString(points);
        String filename = "highscores";
        try {
            FileOutputStream fileOutputStream = openFileOutput(filename, MODE_PRIVATE);
            fileOutputStream.write(msg.getBytes());
            fileOutputStream.close();
            Toast.makeText(getApplicationContext(), "Score saved", Toast.LENGTH_LONG).show();
            highscores = msg;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        view.setVisibility(View.GONE);
    }

    public void readScore() {
        try {
            String line;
            FileInputStream fileInputStream = openFileInput("highscores");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line + "\n");
                highscores = line;
            }
            resetMessages();
            lin3.setTextColor(Color.rgb(0, 0, 0));
            lin3.setTextSize(42);
            lin3.setText("Best score: " + stringBuffer.toString());


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc);

        SM = (SensorManager) getSystemService(SENSOR_SERVICE);
        accSensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        proxSensor = SM.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        lightSensor = SM.getDefaultSensor(Sensor.TYPE_LIGHT);

        lin1 = (TextView) findViewById(R.id.text1);
        lin2 = (TextView) findViewById(R.id.text2);
        lin3 = (TextView) findViewById(R.id.text3);
        lin4 = (TextView) findViewById(R.id.text4);
        msg = (TextView) findViewById(R.id.textView);
        score = (TextView) findViewById(R.id.textScore);

        readScore();

        final Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setVisibility(View.GONE);


        final Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isStartClicked = true;
                startButton.setText("Try again");
            }
        });

        buttonSave = saveButton;
        buttonStart = startButton;

        final Button hsButton = (Button) findViewById(R.id.HSButton);
        hsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                readScore();
            }
        });
    }

    //unregister listener when app is paused
    @Override
    protected void onPause() {
        super.onPause();
        SM.unregisterListener(this);
    }

    //register listener
    @Override
    protected void onResume() {
        super.onResume();

        SM.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(this, proxSensor, SensorManager.SENSOR_DELAY_FASTEST);
        SM.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //it only works if "start" button is clicked
            if (isStartClicked) {
                buttonSave.setVisibility(View.GONE);
                resetMessages();
                //assign acceleration values to variables
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                //calculate acceleration
                acc = (float) abs(sqrt(x * x + y * y + z * z) - 9.7);
                mediumAcc = (mediumAcc + acc) / 2;

                isStarted();
                //works if device is in movement
                if (stopListener) {

                    //start counting time
                    if (!isCounterStarted) {
                        startCounting();
                        startTime = System.currentTimeMillis();
                    }

                    //works if device is not in movement anymore
                    if (acc < 1) {
                        if (isTiming) {
                            //set stop time
                            stopTime = System.currentTimeMillis();
                            //calculate movement time, still in Millis
                            time = (stopTime - startTime);

                            //calculate distance, divide by 2 to get half of distance - height, by 1kk to get seconds
                            distance = ((mediumAcc * time * time) / 2) / 2 / 2 / 1000000;

                            //calculate points
                            points = round(distance * multiplier * 100);

                            showScore();

                            resetValues();

                        }
                    }
                }
            }
        }
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            //if device reached ceiling (I suppose), add +0.5 to multiplier
            if (event.values[0] >= 0 && event.values[0] < 1) {
                //near
                if (!isCeilingReached) {
                    isCeilingReached = true;
                    multiplier += (float) 0.5;
                }
            } else {
                //far
            }
        }
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {

            //set screen brightness

            WindowManager.LayoutParams lp = getWindow().getAttributes();
            float brightness;
            if (event.values[0] == 0) {
                brightness = 0.1f;

            } else if (event.values[0] <= 20) {
                brightness = 0.3f;

            } else if (event.values[0] <= 90) {
                brightness = 0.5f;
            } else if (event.values[0] <= 230) {
                brightness = 0.7f;
            } else {
                brightness = 1.0f;
            }
            lp.screenBrightness = brightness;
            getWindow().setAttributes(lp);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not used
    }
}
