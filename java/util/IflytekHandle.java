package com.jikexueyuan.jike_chat.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;


import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * iflytek handle class
 */

public  abstract  class IflytekHandle {

   // using HashMap to store the result
    private HashMap<String,String> mIatResults = new LinkedHashMap<>();
    private static SpeechRecognizer mIat;

    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private AudioDecode audioDecode;

    public IflytekHandle(String filePath , Context context){
        voice2words(filePath,context);
    }

    public void voice2words (String filePath , Context context){
        mIatResults.clear();
        if(mIat == null){
            //create SpeechRecognizer instance
            mIat = SpeechRecognizer.createRecognizer(context,null);
            setParam();
            mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
            mIat.setParameter(SpeechConstant.SAMPLE_RATE, "8000");//Set the correct sampling rate
        }
        int ret = 0;
        ret = mIat.startListening(mRecognizerListener);

        if (ret != ErrorCode.SUCCESS) {

        } else {

            audioDecodeFun(filePath);
        }
    }

    //Dictation listener
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        //volume0~30
        @Override
        public void onVolumeChanged(int volume, byte[] bytes) {

        }
        //start recording

        @Override
        public void onBeginOfSpeech() {

        }

        @Override
        public void onEndOfSpeech() {

        }

        /**
         * Dictation result callback interface, return Json formatting results
         * the conversation is over when isLast equals true
         */
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            printResult(recognizerResult);
        }


        @Override
        public void onError(SpeechError speechError) {
            returnWords(speechError.getErrorDescription());
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle bundle) {

        }
    };

    private void printResult(RecognizerResult recognizerResult) {
        String text = JsonParser.parseIatResult(recognizerResult.getResultString());
        String sn = null;

        try {
            JSONObject resultJson = new JSONObject(recognizerResult.getResultString());
            sn = resultJson.optString("sn");
        }catch (Exception e){
            e.printStackTrace();
        }
        mIatResults.put(sn,text);
        StringBuilder sb = new StringBuilder();
        for (String key:mIatResults.keySet()){
            sb.append(mIatResults.get(key));
        }

        returnWords(sb.toString());
    }
    //return the text result
    public abstract void returnWords(String words);

    /**
     * tool class
     * @param audioPath
     */
    private void audioDecodeFun(String audioPath){
        audioDecode = AudioDecode.newInstance();
        audioDecode.setFilePath(audioPath);
        audioDecode.prepare();
        audioDecode.setOnCompleteListener(new AudioDecode.OnCompleteListener() {
            @Override
            public void completed(final ArrayList<byte[]> pcmData) {
                if(pcmData!=null){
                    //Write the audio file data, the data format must be the sampling rate of 8KHz or 16KHz (local recognition only supports 16K sampling rate, the clouds are supported), bit long 16bit, mono wav or pcm
                    //saved to local then can be analysis by iflytek

                    for (byte[] data : pcmData){
                        mIat.writeAudio(data, 0, data.length);
                    }
                    Log.d("-----------stop",System.currentTimeMillis()+"");
                    mIat.stopListening();
                }else{
                    mIat.cancel();
                }
                audioDecode.release();
            }
        });
        audioDecode.startAsync();
    }


    /**
     * setting
     */
    private void setParam(){
        //setting
        
        mIat.setParameter(SpeechConstant.DOMAIN,"iat");


        //mIat.setParameter(SpeechConstant.LANGUAGE，"mandarin");
        mIat.setParameter(SpeechConstant.LANGUAGE,"en_us");

        //mIat.setParameter(SpeechConstant.ACCENT,"mandarin");
        
        // set the dictation engine
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        
        //Set the voice front point: silent timeout, that is, how long the user does not speak as a timeout
        mIat.setParameter(SpeechConstant.VAD_BOS,"4000");
        
        // Set the voice end point: the back-end point mute detection time, that is, how long the user stops talking that is no longer enter, automatically stop recording
        mIat.setParameter(SpeechConstant.VAD_EOS,"1000");
        
        // Set punctuation, set to "0" return result no punctuation, set to "1" return result with punctuation
        mIat.setParameter(SpeechConstant.ASR_PTT,"1");
        
        // Set the audio save path, save the audio format support pcm, wav
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        
        //mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
        mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
    }

}
