package com.example.user.mediacodecexercise;


import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by user on 2016-02-03.
 */
public class VideoPlayer {
    static String TAG = "VideoPlayer";
    private MediaCodec mDecoder;
    private MyExtractor mExtractor;
    private boolean mStopped = false;

    VideoPlayer( String codecName, Surface aSurfaceView, String fileName ) {
        try {
            Log.d(TAG, "MediaCodec.createByCodecName(" + codecName + ")"  );
            mDecoder = MediaCodec.createByCodecName( codecName );
            Log.d(TAG, "new MyExtractor(" + fileName + ")"  );
            mExtractor = new MyExtractor( fileName );
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "MediaCodec.createByCodecName(" + codecName + ") failed: " + e.getMessage() );
        } catch (IOException e) {
            Log.d(TAG, "MediaCodec.createByCodecName(" + codecName + ") failed: " + e.getMessage() );
        }

        MediaFormat format = MediaFormat.createVideoFormat( "video/avc", 1280, 736 );
//        MediaFormat format = new MediaFormat();
//        format.setString(MediaFormat.KEY_MIME, "video/avc");

        Log.d(TAG, "mDecoder.configure"  );
        mDecoder.configure(format, aSurfaceView, null, 0);
        Log.d(TAG, "mDecoder.configure OK");
    }

    public void stop() {
        mStopped = true;
    }

    public void play( ) {
        final int TIMEOUT_USEC = 500*1000;

        mDecoder.start();

        boolean endOfInput = false;
        boolean endOfOutput = false;

        int chunkSize = 0;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        do {
            if ( ! endOfInput ) {
                int inputBufIndex = mDecoder.dequeueInputBuffer(TIMEOUT_USEC);
                Log.d(TAG, "mDecoder.dequeueInputBuffer: " + inputBufIndex);
                if (inputBufIndex >= 0) {
                    ByteBuffer[] decoderInputBuffers = mDecoder.getInputBuffers();
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    chunkSize = mExtractor.readSampleData(inputBuf, 0);
                    mDecoder.queueInputBuffer(inputBufIndex, 0, chunkSize > 0 ? chunkSize : 0, 0, chunkSize <= 0 ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0 );
                    if (chunkSize <= 0) {
                        endOfInput = true;
                    }
                }
            }

            if ( ! endOfOutput ) {
                int decoderStatus = mDecoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                Log.d(TAG, "mDecoder.dequeueOutputBuffer: " + decoderStatus);
                if (decoderStatus >= 0) {
                    mDecoder.releaseOutputBuffer(decoderStatus, true);
                    endOfOutput = endOfInput && (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                    if ( endOfOutput ) {
                        Log.d( TAG, "endOfOutput" );
                    }
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = mDecoder.getOutputFormat();
                    Log.d(TAG, "decoder.dequeueOutputBuffer - INFO_OUTPUT_FORMAT_CHANGED: " + newFormat);
                }
            }

        } while ( ! mStopped || ! endOfInput || ! endOfInput );

        mDecoder.flush();
        mDecoder.release();
    }

    public static class PlayTask implements Runnable {
        private VideoPlayer mPlayer;
        private Thread mThread;

        PlayTask( VideoPlayer videoPlayer ) {
            mPlayer = videoPlayer;
        }

        public void execute() {
            mThread = new Thread(this, "Movie Player");
            mThread.start();
        }


        @Override
        public void run() {
            mPlayer.play();
        }
    }
}
