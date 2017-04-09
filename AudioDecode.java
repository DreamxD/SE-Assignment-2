package com.jikexueyuan.jike_chat.util;


import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 *
 * convert AMR into PCM data stream
 */
public class AudioDecode {

    private static final String TAG = "AudioDecode";
    private String srcPath;//voice message local path
    private MediaCodec mediaDecode;
    private MediaExtractor mediaExtractor;
    private ByteBuffer[] decodeInputBuffers;
    private ByteBuffer[] decodeOutputBuffers;
    private MediaCodec.BufferInfo decodeBufferInfo;
    private ArrayList<byte[]> chunkPCMDataContainer;//PCM data block container
    private OnCompleteListener onCompleteListener;
    private boolean codeOver = false;
    private byte[] pcmData;//the data stream after decode
    private Thread decoderThread;

    public static AudioDecode newInstance() {
        return new AudioDecode();
    }

    /**
     * set the file path
     *
     * @param srcPath
     */
    public void setFilePath(String srcPath) {
        this.srcPath = srcPath;
    }

    /**
     *
     */
    public void prepare() {

        if (srcPath == null) {
            throw new IllegalArgumentException("srcPath can't be null");
        }
        chunkPCMDataContainer = new ArrayList<>();
        initMediaDecode();
    }

    /**
     * init the decoder
     */
    private void initMediaDecode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mediaExtractor = new MediaExtractor();
                mediaExtractor.setDataSource(srcPath);
                for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                    MediaFormat format = mediaExtractor.getTrackFormat(i);;
                    format.setInteger(MediaFormat.KEY_BIT_RATE, AudioFormat.ENCODING_PCM_16BIT);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime.startsWith("audio")) {
                        mediaExtractor.selectTrack(i);
                        mediaDecode = MediaCodec.createDecoderByType(mime);
                        mediaDecode.configure(format, null, null, 0);
                        break;
                    }
                }
                if (mediaDecode == null) {
                    Log.e(TAG, "create mediaDecode failed");
                    return;
                }
                mediaDecode.start();
                decodeInputBuffers = mediaDecode.getInputBuffers();
                decodeOutputBuffers = mediaDecode.getOutputBuffers();
                decodeBufferInfo = new MediaCodec.BufferInfo();
                showLog("buffers:" + decodeInputBuffers.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * begin decode
     * sound data decode into PCM format
     */
    public void startAsync() {
        decoderThread = new Thread(new DecodeRunnable());
        decoderThread.start();
    }

    /**
     * save PCMdata into{@link #chunkPCMDataContainer}
     *
     * @param pcmChunk PCM data block
     */
    private void putPCMData(byte[] pcmChunk) {
        synchronized (AudioDecode.class) {
            chunkPCMDataContainer.add(pcmChunk);
        }
    }

    /**
     * {@link #chunkPCMDataContainer}take out PCM data from container
     *
     * @return PCM data block
     */
    private byte[] getPCMData() {
        synchronized (AudioDecode.class) {
            if (chunkPCMDataContainer.isEmpty()) {
                return null;
            }

            byte[] pcmChunk = chunkPCMDataContainer.get(0);
            chunkPCMDataContainer.remove(pcmChunk);
            return pcmChunk;
        }
    }


    /**
     * decode {@link #srcPath}
     *
     * @return
     */
    private void srcAudioFormatToPCM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if(decodeInputBuffers!=null){
                try {
                    for (int i = 0; i < decodeInputBuffers.length - 1; i++) {
                        int inputIndex = 0;
                        inputIndex = mediaDecode.dequeueInputBuffer(-1);
                        if (inputIndex < 0) {
                            codeOver = true;
                            return;
                        }
                        ByteBuffer inputBuffer = decodeInputBuffers[inputIndex];
                        inputBuffer.clear();
                        int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
                        if (sampleSize < 0) {
                            codeOver = true;
                        } else {
                            mediaDecode.queueInputBuffer(inputIndex, 0, sampleSize, 0, 0);
                            mediaExtractor.advance();
                        }
                    }


                    int outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, 10000);

                    ByteBuffer outputBuffer;
                    byte[] chunkPCM;
                    while (outputIndex >= 0) {
                        outputBuffer = decodeOutputBuffers[outputIndex];
                        chunkPCM = new byte[decodeBufferInfo.size];
                        outputBuffer.get(chunkPCM);
                        outputBuffer.clear();
                        putPCMData(chunkPCM);
                        mediaDecode.releaseOutputBuffer(outputIndex, false);
                        outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, 10000);
                    }

                    if(codeOver){
                        if (onCompleteListener != null) {
                            onCompleteListener.completed(chunkPCMDataContainer);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    codeOver = true;
                    if (onCompleteListener != null) {
                        onCompleteListener.completed(chunkPCMDataContainer);
                    }
                }
            }
        }
    }

    /**
     * android 5.0
     */
    private void srcAudioFormatToPCMHigherApi() {
        if (Build.VERSION.SDK_INT >= 21){
            boolean sawOutputEOS = false;
            final long kTimeOutUs = 10000;
            long presentationTimeUs = 0;
            while (!sawOutputEOS){
                try{
                    int inputIndex = mediaDecode.dequeueInputBuffer(-1);
                    if (inputIndex >= 0){
                        ByteBuffer inputBuffer = mediaDecode.getInputBuffer(inputIndex);
                        if(inputBuffer!=null){
                            int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
                            if (sampleSize < 0) {
                                sawOutputEOS = true;
                                codeOver = true;
                                break;
                            }else{
                                presentationTimeUs = mediaExtractor.getSampleTime();
                                mediaDecode.queueInputBuffer(inputIndex, 0, sampleSize, presentationTimeUs, 0);
                                mediaExtractor.advance();
                            }
                        }
                    }else{
                        sawOutputEOS = true;
                        codeOver = true;
                    }
                    int outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, kTimeOutUs);
                    ByteBuffer outputBuffer ;//= mediaDecode.getOutputBuffer(outputIndex);
                    while (outputIndex >= 0){
                        outputBuffer = mediaDecode.getOutputBuffer(outputIndex);
                        boolean doRender = (decodeBufferInfo.size != 0);
                        if(doRender && outputBuffer!=null){
                            outputBuffer.position(decodeBufferInfo.offset);
                            outputBuffer.limit(decodeBufferInfo.offset + decodeBufferInfo.size);
                            byte[] chunkPCM = new byte[decodeBufferInfo.size];
                            outputBuffer.get(chunkPCM);
                            outputBuffer.clear();
                            putPCMData(chunkPCM);
                            mediaDecode.releaseOutputBuffer(outputIndex, false);
                            outputIndex = mediaDecode.dequeueOutputBuffer(decodeBufferInfo, kTimeOutUs);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    sawOutputEOS = true;
                    codeOver = true;
                }
            }
            if(codeOver){
                if (onCompleteListener != null) {
                    onCompleteListener.completed(chunkPCMDataContainer);
                }
            }
        }
    }

    /**
     * release resource
     */
    public void release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){

            try{
                if(decoderThread!=null && decoderThread.isAlive()){
                    decoderThread.interrupt();
                    codeOver = true;
                }

                if (mediaDecode != null) {
                    mediaDecode.stop();
                    mediaDecode.release();
                    mediaDecode = null;
                }

                if (mediaExtractor != null) {
                    mediaExtractor.release();
                    mediaExtractor = null;
                }

                if (onCompleteListener != null) {
                    onCompleteListener = null;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    private class DecodeRunnable implements Runnable {

        @Override
        public void run() {
            while (!codeOver) {
                if(Build.VERSION.SDK_INT>=21){
                    srcAudioFormatToPCMHigherApi();
                }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                    srcAudioFormatToPCM();
                }
            }
        }
    }

    /**
     *
     */
    public interface OnCompleteListener {
        void completed(ArrayList<byte[]> chunkPCMDataContainer);
    }

    /**
     *
     * @param onCompleteListener
     */
    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    private void showLog(String msg) {
        Log.e("AudioCodec", msg);
    }
}



