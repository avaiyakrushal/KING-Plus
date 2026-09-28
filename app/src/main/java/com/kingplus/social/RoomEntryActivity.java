package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

/** Verifies private/password room access before opening room controls. */
public class RoomEntryActivity extends Activity {
    private final RoomAccessManager access=new RoomAccessManager();
    private String roomId,roomName,ownerUid,name,uid;
    @Override public void onCreate(Bundle b){super.onCreate(b);roomId=getIntent().getStringExtra("roomId");roomName=getIntent().getStringExtra("roomName");ownerUid=getIntent().getStringExtra("ownerUid");name=getIntent().getStringExtra("name");uid=FirebaseAuth.getInstance().getUid();if(uid==null){Toast.makeText(this,"Sign in required",Toast.LENGTH_LONG).show();finish();return;}verify("");}
    private void verify(String password){access.check(roomId,uid,password,(allowed,message)->runOnUiThread(()->{if(allowed)openRoom();else passwordDialog(message);}));}
    private void passwordDialog(String message){EditText p=new EditText(this);p.setHint("Room password");p.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);new AlertDialog.Builder(this).setTitle("Room access").setMessage(message+"\nEnter password if you have one.").setView(p).setCancelable(false).setNegativeButton("Cancel",(d,w)->finish()).setPositiveButton("Enter",(d,w)->verify(p.getText().toString())).show();}
    private void openRoom(){Intent i=new Intent(this,RoomControlActivity.class);i.putExtra("roomId",roomId);i.putExtra("roomName",roomName);i.putExtra("ownerUid",ownerUid);i.putExtra("name",name);startActivity(i);finish();}
}
