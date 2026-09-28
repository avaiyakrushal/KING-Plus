package com.kingplus.social;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

public class PrivateChatActivity extends Activity {
 private final SocialManager social=new SocialManager(); private String me,target,targetName; private FirebaseUser user; private LinearLayout messages; private ListenerRegistration listener;
 @Override public void onCreate(Bundle b){super.onCreate(b);user=FirebaseAuth.getInstance().getCurrentUser();if(user==null){finish();return;}me=user.getUid();target=getIntent().getStringExtra("targetUid");targetName=getIntent().getStringExtra("targetName");if(target==null||target.trim().isEmpty()||target.equals(me)){Toast.makeText(this,"Invalid chat user",Toast.LENGTH_LONG).show();finish();return;}render();}
 private void render(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(24,36,24,24);TextView title=new TextView(this);title.setText("Private Chat • "+(targetName==null?"KING User":targetName));title.setTextSize(22);root.addView(title);ScrollView scroll=new ScrollView(this);messages=new LinearLayout(this);messages.setOrientation(LinearLayout.VERTICAL);scroll.addView(messages);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));EditText input=new EditText(this);input.setHint("Write a private message");root.addView(input,new LinearLayout.LayoutParams(-1,-2));Button send=new Button(this);send.setText("Send");send.setOnClickListener(v->{String text=input.getText().toString().trim();if(text.length()>500){input.setError("Maximum 500 characters");return;}social.sendMessage(me,target,safeName(),text,(ok,msg)->runOnUiThread(()->{Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();if(ok)input.setText("");}));});root.addView(send);setContentView(root);listener=social.listenMessages(me,target,(snap,error)->{if(error!=null||snap==null)return;runOnUiThread(()->{messages.removeAllViews();for(DocumentSnapshot d:snap.getDocuments()){TextView row=new TextView(this);String sender=d.getString("senderUid");row.setText((me.equals(sender)?"Me":(d.getString("senderName")==null?"User":d.getString("senderName")))+": "+d.getString("text"));row.setPadding(8,10,8,10);messages.addView(row);}});});}
 private String safeName(){String n=user.getDisplayName();return n==null||n.trim().isEmpty()?"KING User":n.trim();}
 @Override protected void onDestroy(){if(listener!=null)listener.remove();super.onDestroy();}
}
