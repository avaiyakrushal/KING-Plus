package com.kingplus.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/** Search a KING Plus user by Firebase UID and open social actions. */
public class UserSocialActivity extends Activity {
 private final SocialManager social=new SocialManager(); private String me; private LinearLayout root; private EditText search;
 @Override public void onCreate(Bundle b){super.onCreate(b);me=FirebaseAuth.getInstance().getUid();if(me==null){finish();return;}renderSearch();}
 private void renderSearch(){root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(32,48,32,32);TextView t=new TextView(this);t.setText("Find KING Plus User");t.setTextSize(24);root.addView(t);search=new EditText(this);search.setHint("Firebase UID");root.addView(search);Button b=new Button(this);b.setText("Search User");b.setOnClickListener(v->find(search.getText().toString().trim()));root.addView(b);setContentView(root);}
 private void find(String uid){if(uid.isEmpty()||uid.equals(me)){Toast.makeText(this,"Enter another user's UID",Toast.LENGTH_SHORT).show();return;}FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener(doc->showUser(uid,doc)).addOnFailureListener(e->Toast.makeText(this,"User lookup failed",Toast.LENGTH_LONG).show());}
 private void showUser(String uid,DocumentSnapshot doc){root.removeAllViews();String name=doc.getString("displayName");if(name==null)name=doc.getString("name");if(name==null)name="KING User";final String finalName=name;TextView t=new TextView(this);t.setText(finalName+"\nUID: "+uid);t.setTextSize(22);root.addView(t);add("Follow",()->social.follow(me,uid,this::result));add("Unfollow",()->social.unfollow(me,uid,this::result));add("Add Friend",()->social.addFriend(me,uid,this::result));add("Private Message",()->{Intent i=new Intent(this,PrivateChatActivity.class);i.putExtra("targetUid",uid);i.putExtra("targetName",finalName);startActivity(i);});add("Search Another User",this::renderSearch);}
 private void add(String text,Runnable action){Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setOnClickListener(v->action.run());root.addView(b,new LinearLayout.LayoutParams(-1,-2));}
 private void result(boolean ok,String message){runOnUiThread(()->Toast.makeText(this,(ok?"✓ ":"⚠ ")+message,Toast.LENGTH_SHORT).show());}
}
