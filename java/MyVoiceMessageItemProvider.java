package com.jikexueyuan.jike_chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.jikexueyuan.jike_chat.util.FileUtils;
import com.jikexueyuan.jike_chat.util.IflytekHandle;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.ConversationKey;
import io.rong.imkit.widget.ArraysDialogFragment;
import io.rong.imkit.widget.provider.VoiceMessageItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.PublicServiceInfo;
import io.rong.imlib.model.UserInfo;
import io.rong.message.VoiceMessage;

/**
 * conversation handle class
 */
public class MyVoiceMessageItemProvider extends VoiceMessageItemProvider {
    private  Context context;
    //after transform then show the text
    private  TextView textView;

    public MyVoiceMessageItemProvider(Context context) {
        super(context);
        this.context = context;
    }


    //handle long click on voice messages
    @Override
    public void onItemLongClick(View view, int position, final VoiceMessage content, final Message message) {
        String name = null;
        if(!message.getConversationType().getName().equals(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName()) && !message.getConversationType().getName().equals(Conversation.ConversationType.PUBLIC_SERVICE.getName())) {
            UserInfo items1 = (UserInfo)RongContext.getInstance().getUserInfoCache().get(message.getSenderUserId());
            if(items1 != null) {
                name = items1.getName();
            }
        } else {
            ConversationKey items = ConversationKey.obtain(message.getTargetId(), message.getConversationType());
            PublicServiceInfo info = (PublicServiceInfo)RongContext.getInstance().getPublicServiceInfoCache().get(items.getKey());
            if(info != null) {
                name = info.getName();
            }
        }

        String[] items2 = new String[]{view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_delete),
                view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_convert)};
        ArraysDialogFragment.newInstance(name, items2).setArraysDialogItemListener(new ArraysDialogFragment.OnArraysDialogItemListener() {
            public void OnArraysDialogItemClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    RongIM.getInstance().getRongIMClient().deleteMessages(new int[]{message.getMessageId()}, (RongIMClient.ResultCallback)null);
                }
                else if(which == 1){

                    //init voice message transforming  layout
                    LayoutInflater factory = LayoutInflater.from(context);
                    RelativeLayout view = (RelativeLayout)factory.inflate(R.layout.convert_dialog, null);
                    final AlertDialog dlg = new AlertDialog.Builder(context).create();
                    textView = new TextView(context);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.CENTER_IN_PARENT);
                    textView.setLayoutParams(params);
                    textView.setTextColor(Color.BLACK);
                    textView.setTextSize(20f);
                    textView.setText("waiting...");
                    view.addView(textView);
                    if ( !dlg.isShowing()) {
                        dlg.show();
                    }

                    dlg.setContentView(view);

                    Activity activity = (Activity) context;
                    String voicePath = FileUtils.uri2File(activity,content.getUri());

                    //use iflyket sdk to analysis the voice message
                    new IflytekHandle(voicePath,context){
                        @Override
                        public  void returnWords(String words){
                            textView.setText(words);
                        }
                    };
                }

            }
        }).show(((FragmentActivity)view.getContext()).getSupportFragmentManager());
    }


}
