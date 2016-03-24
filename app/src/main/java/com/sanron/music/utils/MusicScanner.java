package com.sanron.music.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import java.io.File;

public class MusicScanner {

    private Context context;
    private MediaScannerConnection connection;
    private MediaScannerConnection.MediaScannerConnectionClient client;

    private boolean isScanning = false;

    private TraverseThread traverseThread;

    private final Object lock = new Object();

    public static final String TAG = MusicScanner.class.getSimpleName();


    public interface OnScanMediaListener {
        /**
         * 扫描开始,ui线程内
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
        this.context = context;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void scan(String[] paths,final OnScanMediaListener listener){

    }

    public void scan(final OnScanMediaListener listener,String... paths) {
        if (isScanning) {
            return;
        }

        if (listener == null) {
            throw new IllegalArgumentException("must set listenr");
        }

        isScanning = true;
        traverseThread = new TraverseThread(listener,paths);
        client = new MediaScannerConnection.MediaScannerConnectionClient() {

            @Override
            public void onMediaScannerConnected() {
                traverseThread.start();
                listener.onStart();
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                listener.onProgress(path, uri);
                synchronized (lock) {
                    lock.notify();
                }
            }
        };
        connection = new MediaScannerConnection(context, client);
        connection.connect();
    }

    public void stopScan() {
        if (traverseThread != null) {
            traverseThread.stopRun();
        }
    }

    /**
     * 遍历文件线程
     */
    private class TraverseThread extends Thread {

        private String[] paths;
        private boolean flagStop = false;
        private OnScanMediaListener listener;

        public TraverseThread(OnScanMediaListener listener,String... paths) {
            this.paths = paths;
            this.listener = listener;
        }

        public void stopRun() {
            flagStop = true;
        }

        @Override
        public void run() {
            traversePaths(paths);
            connection.disconnect();
            listener.onCompleted(flagStop);
            isScanning = false;
            traverseThread = null;
            listener = null;
            MyLog.d(TAG, "scan completed");
        }

        public void traversePaths(String[] paths){
            for(int i=0 ;i<paths.length; i++){
                traverse(paths[i]);
            }
        }

        /**
         * 遍历文件
         *
         * @param path
         */
        private void traverse(String path) {
            if (flagStop) {
                return;
            }

            File file = new File(path);
            if (file.isDirectory()) {
                File[] childs = file.listFiles();
                for (File child : childs) {
                    if (flagStop) {
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
                    synchronized (lock) {
                        connection.scanFile(path, "audio/*");
                        try {
                            lock.wait();
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