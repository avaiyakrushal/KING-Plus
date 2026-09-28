package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/** Host-only room privacy settings. */
public class RoomSettingsActivity extends Activity {
 private final RoomAccessManager access=new RoomAccessManager(); private String roomId,uid,ownerUid;
 @Override public void onCreate(Bundle b){super.onCreate(b);roomId=getIntent().getStringExtra("roomId");uid=FirebaseAuth.getInstance().getUid();ownerUid=getIntent().getStringExtra("ownerUid");if(uid==null||ownerUid==null||!uid.equals(ownerUid)){Toast.makeText(this,"Host only",Toast.LENGTH_LONG).show();finish();return;}render();}
 private void render(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(32,48,32,32);TextView t=new TextView(this);t.setText("Room Privacy Settings");t.setTextSize(24);root.addView(t);add(root,"Public room",()->access.configure(roomId,uid,false,"",this::result));add(root,"Private room",()->access.configure(roomId,uid,true,"",this::result));add(root,"Set / Change Password",this::passwordDialog);add(root,"Allow User by UID",this::allowDialog);add(root,"Back",this::finish);setContentView(root);}
 private void passwordDialog(){EditText p=new EditText(this);p.setHint("New room password");p.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);new AlertDialog.Builder(this).setTitle("Password room").setView(p).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{String s=p.getText().toString();if(s.length()<4){result(false,"Password must be at least 4 characters");return;}access.configure(roomId,uid,false,s,this::result);}).show();}
 private void allowDialog(){EditText e=new EditText(this);e.setHint("Firebase UID");new AlertDialog.Builder(this).setTitle("Allow user into private room").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Allow",(d,w)->{String id=e.getText().toString().trim();if(id.isEmpty()){result(false,"Enter UID");return;}access.allowUser(roomId,id,this::result);}).show();}
 private void add(LinearLayout r,String text,final Runnable run){Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setOnClickListener(v->run.run());LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=14;r.addView(b,p);}
 private void result(boolean ok,String message){runOnUiThread(()->Toast.makeText(this,(ok?"✓ ":"⚠ ")+message,Toast.LENGTH_LONG).show());}
}
