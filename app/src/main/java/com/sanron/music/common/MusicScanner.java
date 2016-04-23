package com.sanron.music.common;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;

public class MusicScanner {

    private Context mContext;
    private MediaScannerConnection mScannerConnection;
    private MediaScannerConnection.MediaScannerConnectionClient mScannerClient;

    private boolean mIsScanning = false;

    private TraverseThread mTraverseThread;

    private final Object mLock = new Object();

    public static final String TAG = MusicScanner.class.getSimpleName();


    public interface OnScanMediaListener {
        /**
         * 扫描开始,ui线程
         */
        void onStart();

        /**
         * 扫描到音乐文件时,非ui线程
         *
         * @param path
         * @param uri
         */
        void onProgress(String path, Uri uri);

        /**
         * 扫描完成时，非ui线程
         *
         * @param fromStop 是否停止导致完成
         */
        void onCompleted(boolean fromStop);
    }

    public MusicScanner(Context context) {
        this.mContext = context;
    }

    public boolean isScanning() {
        return mIsScanning;
    }


    public void scan(final OnScanMediaListener listener, String... paths) {
        if (mIsScanning) {
            return;
        }

        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }

        mIsScanning = true;
        mTraverseThread = new TraverseThread(listener, paths);
        mScannerClient = new MediaScannerConnection.MediaScannerConnectionClient() {

            @Override
            public void onMediaScannerConnected() {
                mTraverseThread.start();
                listener.onStart();
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                listener.onProgress(path, uri);
                synchronized (mLock) {
                    mLock.notify();
                }
            }
        };
        mScannerConnection = new MediaScannerConnection(mContext, mScannerClient);
        mScannerConnection.connect();
    }

    public void stopScan() {
        if (mTraverseThread != null) {
            mTraverseThread.stopRun();
        }
    }

    /**
     * 遍历文件线程
     */
    private class TraverseThread extends Thread {

        private String[] mPaths;
        private boolean mFlagStop = false;
        private OnScanMediaListener mListener;

        public TraverseThread(OnScanMediaListener listener, String... paths) {
            this.mPaths = paths;
            this.mListener = listener;
        }

        public void stopRun() {
            mFlagStop = true;
            synchronized (mLock) {
                mLock.notify();
            }
        }

        @Override
        public void run() {
            traversePaths(mPaths);
            mScannerConnection.disconnect();
            mListener.onCompleted(mFlagStop);
            mIsScanning = false;
            mTraverseThread = null;
            mListener = null;
            MyLog.d(TAG, "scan completed");
        }

        public void traversePaths(String[] paths) {
            for (int i = 0; i < paths.length; i++) {
                traverse(paths[i]);
            }
        }

        /**
         * 遍历文件
         *
         * @param path
         */
        private void traverse(String path) {
            if (mFlagStop) {
                return;
            }

            File file = new File(path);
            if (file.isDirectory()) {
                File[] childs = file.listFiles();
                for (File child : childs) {
                    if (mFlagStop) {
                        return;
                    }
                    traverse(child.getAbsolutePath());
                }
            } else {
                if (path.startsWith(".")) {
                    //隐藏文件不扫描
                    return;
                }
                if (judgeExtension(path)) {
                    synchronized (mLock) {
                        mScannerConnection.scanFile(path, "audio/*");
                        try {
                            mLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private boolean judgeExtension(String path) {
            return path.endsWith(".mp3")
                    || path.endsWith(".wav")
                    || path.endsWith(".m4a")
                    || path.endsWith(".aac");
        }
    }
}