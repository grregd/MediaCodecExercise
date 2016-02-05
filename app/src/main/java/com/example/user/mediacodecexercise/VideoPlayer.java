package com.example.user.mediacodecexercise;


import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2016-02-03.
 */
public class VideoPlayer {
    static String TAG = "VideoPlayer";
    private MediaCodec mDecoder;
    private MyExtractor mExtractor;
    private boolean mStopped = false;
    private ReentrantLock mDecoderLock = new ReentrantLock();
    private FileOutputStream mFile;
    private Surface mSurface;

    VideoPlayer( String codecName, Surface aSurfaceView, String fileName ) {
        mSurface = aSurfaceView;
        try {
            Log.d(TAG, "MediaCodec.createByCodecName(" + codecName + ")"  );
            mDecoder = MediaCodec.createByCodecName(codecName);
            Log.d(TAG, "new MyExtractor(" + fileName + ")"  );
            mExtractor = new MyExtractor( fileName );
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "MediaCodec.createByCodecName(" + codecName + ") failed: " + e.getMessage() );
        } catch (IOException e) {
            Log.d(TAG, "MediaCodec.createByCodecName(" + codecName + ") failed: " + e.getMessage() );
        }

        MediaFormat format = MediaFormat.createVideoFormat( "video/avc", 1280, 720 );
//        MediaFormat format = new MediaFormat();
//        format.setString(MediaFormat.KEY_MIME, "video/avc");

        Log.d(TAG, "mDecoder.configure"  );
        mDecoderLock.lock();
        mDecoder.configure(format, aSurfaceView, null, 0);
        mDecoderLock.unlock();
        Log.d(TAG, "mDecoder.configure OK");

        if ( mSurface == null ) {
            try {
                Log.d(TAG, "XXXXXXXXXX - creating file /storage/sdcard0/DCIM/myvideooutput");
                File file = new File("/storage/sdcard0/DCIM/myvideooutput");
                mFile = new FileOutputStream(file);
            } catch (IOException e) {
                Log.d(TAG, "XXXXXXXXXX - " + e.getMessage());
            }
        }
    }

    public boolean isPlaying() { return ! mStopped; }

    public void stop() {
        mStopped = true;
    }

    public void play( ) {
        final int TIMEOUT_USEC = -1;//10*1000;

        mDecoderLock.lock();
        mDecoder.start();
        mDecoderLock.unlock();

        boolean endOfInput = false;
        boolean endOfOutput = false;

        int chunkSize = 0;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        do {
            if ( ! endOfInput ) {
                int inputBufIndex = mDecoder.dequeueInputBuffer(TIMEOUT_USEC);
                Log.d(TAG, "mDecoder.dequeueInputBuffer: " + inputBufIndex);
                if (inputBufIndex >= 0) {
                    mDecoderLock.lock();
                    ByteBuffer[] decoderInputBuffers = mDecoder.getInputBuffers();
                    mDecoderLock.unlock();
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    chunkSize = mExtractor.readSampleData(inputBuf, 0);
                    mDecoderLock.lock();
                    Log.d(TAG, "queueInputBuffer - chunkSize: " + chunkSize);
                    mDecoder.queueInputBuffer(inputBufIndex, 0, chunkSize > 0 ? chunkSize : 0, 0, chunkSize <= 0 ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                    mDecoderLock.unlock();
                    if (chunkSize <= 0) {
                        endOfInput = true;
                    }
                }
            }

//            if ( ! endOfOutput ) {
//                int decoderStatus = mDecoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC*7);
//                Log.d(TAG, "mDecoder.dequeueOutputBuffer: " + decoderStatus);
//                if (decoderStatus >= 0) {
//                    mDecoder.releaseOutputBuffer(decoderStatus, bufferInfo.size > 0);
//                    endOfOutput = endOfInput && (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
//                    if ( endOfOutput ) {
//                        Log.d( TAG, "endOfOutput" );
//                    }
//                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                    MediaFormat newFormat = mDecoder.getOutputFormat();
//                    Log.d(TAG, "decoder.dequeueOutputBuffer - INFO_OUTPUT_FORMAT_CHANGED: " + newFormat);
//                }
//            }

        } while ( ! mStopped && ( /*! endOfInput ||*/ ! endOfInput) );

//        mStopped = true;
//        mDecoder.flush();
//        mDecoder.release();
        Log.d(TAG, "END OF INPUT");
    }

    public void output() {
        boolean endOfOutput = false;
        final int TIMEOUT_USEC = -1; //34*1000;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        try {
            Thread.sleep(133, 0);
        } catch (InterruptedException e) {
        }

        while ( ! mStopped && ! endOfOutput ) {
            int decoderStatus = mDecoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            Log.d(TAG, "mDecoder.dequeueOutputBuffer: " + decoderStatus);
            if (decoderStatus >= 0) {
                mDecoderLock.lock();
                if ( mSurface == null ) {
                    try {
                        ByteBuffer buffers[] = mDecoder.getOutputBuffers();
                        ByteBuffer outputData = buffers[ decoderStatus ];

                        // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
                        outputData.position(bufferInfo.offset);
                        outputData.limit(bufferInfo.offset + bufferInfo.size);

                        byte[] data = new byte[ bufferInfo.size ];
                        outputData.get(data);
                        outputData.position( bufferInfo.offset );

                        mFile.write( data );
                    } catch (IOException e) {
                        Log.d(TAG, "XXXXXXXXXX - " + e.getMessage());
                    }
                }

                mDecoder.releaseOutputBuffer(decoderStatus, bufferInfo.size > 0 && mSurface != null);

                mDecoderLock.unlock();

                if ( mSurface != null ) {
                    try {
                        Thread.sleep(33, 0);
                    } catch (InterruptedException e) {
                    }
                }

                endOfOutput = /*endOfInput && */ (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
                if ( endOfOutput ) {
                    Log.d( TAG, "endOfOutput" );
                }
            } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mDecoder.getOutputFormat();
                Log.d(TAG, "decoder.dequeueOutputBuffer - INFO_OUTPUT_FORMAT_CHANGED: " + newFormat);
            }
        }

        mStopped = true;
        mDecoderLock.lock();
        mDecoder.flush();
        mDecoder.release();
        mDecoderLock.unlock();

        Log.d(TAG, "END OF OUTPUT");
    }

    public static class OutputTask implements Runnable {
        private Thread mThread;
        private VideoPlayer mPlayer;

        OutputTask( VideoPlayer videoPlayer ) { mPlayer = videoPlayer; }

        public void execute() {
            mThread = new Thread(this, "Movie Player");

            mThread.start();
        }

        @Override
        public void run() {
            mPlayer.output();
        }
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
