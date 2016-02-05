package com.example.user.mediacodecexercise;

import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by user on 2016-02-04.
 */
public class MyExtractor
{
    private static String TAG = "MyExtractor";
    private File mFile;
    private DataInputStream mStream;

    public MyExtractor( String aFilePath )
    {
        mFile = new File( aFilePath );
        try {
            mStream = new DataInputStream( new FileInputStream( mFile ) );
        } catch (FileNotFoundException e) {
            Log.d(TAG, "MyExtractor failed: " + e.getMessage());
        } finally {

        }
    }

    public int readSampleData(ByteBuffer aInputBuf, int aOffset )
    {
        try {
            Log.d( TAG, "readSampleData - aInputBuf.limit(): " + aInputBuf.limit() );
            Log.d( TAG, "readSampleData - aInputBuf.position(): " + aInputBuf.position() );
            Log.d( TAG, "readSampleData - aInputBuf.capacity(): " + aInputBuf.capacity() );

            int chunkSize = Math.min( 100*1024, aInputBuf.limit() - aInputBuf.position() );;

            byte[] chunk = new byte[ chunkSize ];
            chunkSize = mStream.read(chunk, 0, chunkSize );
            if ( chunkSize > 0 ) {
                aInputBuf.put( chunk );
            }
            return chunkSize;
        } catch (IOException e) {
            Log.d(TAG, "readSampleData fail: " + e.getMessage());
        }

        return 0;
    }

    public long getSampleTime()
    {
        return 0;
    }

    public boolean advance()
    {
        return false;
    }

    public void seekTo(long timeUs, int mode)
    {
        return ;
    }
}
