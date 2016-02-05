package com.example.user.mediacodecexercise;


import android.graphics.Color;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    static String TAG = "MediaCodecExercise";
    private String[] mEncodersNames;
    private String[] mFilesNames;
    public static String mSelectedEncoder;
    public static String mSelectedFile;
    public static SurfaceView mSurfaceView;
    public static String BASE_FOLDER = "/storage/sdcard0/DCIM/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
//        mSurfaceView.setBackgroundColor(Color.RED);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            private VideoPlayer mVideoPlayer = null;

            @Override
            public void onClick(View view) {
                if ( mVideoPlayer != null ) {
                    mVideoPlayer.stop();
                }

                mVideoPlayer = new VideoPlayer(
                        MainActivity.mSelectedEncoder,
                        mSurfaceView.getHolder().getSurface(),
                        BASE_FOLDER + MainActivity.mSelectedFile );
                VideoPlayer.PlayTask task = new VideoPlayer.PlayTask( mVideoPlayer );
                task.execute();

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        List< String > decodersNames = new ArrayList< String >();
        for ( int i = 0; i < MediaCodecList.getCodecCount(); ++i )
        {
            MediaCodecInfo info = MediaCodecList.getCodecInfoAt( i );

            Log.d(TAG, "onCreate: info " + i + info.getName()  );

            if ( ! info.isEncoder() &&
                    ( info.getName().contains("avc") ||
                      info.getName().contains("AVC") ||
                      info.getName().contains("h264") ||
                      info.getName().contains("H264") ) ) {
                decodersNames.add( info.getName() );
            }

//            mEncodersNames. = info.getName();
//            String[] types = info.getSupportedTypes();
//            String allTypes = "";
//            for ( String type: types )
//            {
//                allTypes += type + "; ";
//            }

//            Log.d(TAG, "XXXXXX - " +
//                    "Name: " + info.getName() + ", " +
//                    "Types: " + allTypes + ", " +
//                    (info.isEncoder() ? "Encoder" : "Decoder"));
        }

        {
            mEncodersNames = new String[ decodersNames.size() ];
            decodersNames.toArray(mEncodersNames);

            Spinner spinner = (Spinner) findViewById(R.id.listEncoders);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, mEncodersNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner.
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
        }

        {
            File f = new File( new String( BASE_FOLDER ) );
            File filesDir = f;   //getFilesDir()
            Log.d(TAG, "searching for files in " + filesDir.toString() );
            mFilesNames = com.android.grafika.MiscUtils.getFiles( filesDir, "*" );
            Log.d(TAG, "files: " + mFilesNames.toString() );
            for ( String file: mFilesNames ) {
                Log.d(TAG, file );
            }


            Spinner spinner = (Spinner) findViewById(R.id.listFiles);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, mFilesNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner.
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        if ( parent.getId() == R.id.listEncoders ) {
            mSelectedEncoder = mEncodersNames[spinner.getSelectedItemPosition()];
            Log.d(TAG, "onItemSelected - encoder: " + mSelectedEncoder );
        }
        else if ( parent.getId() == R.id.listFiles ) {
            mSelectedFile = mFilesNames[spinner.getSelectedItemPosition()];
            Log.d(TAG, "onItemSelected - file: " + mSelectedFile );
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
