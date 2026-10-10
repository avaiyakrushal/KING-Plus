package com.kingplus.social;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

/** On-phone read-only diagnostics for different-account, same-Party joins.
 * No Firebase test writes, no passwords, full UID, tokens or Google billing.
 */
public final class KingConnection983Activity extends Activity {
    private EditText requestedRoom;
    private TextView output;
    private final StringBuilder report=new StringBuilder();
    private int generation983;
    private boolean destroyed983;

    private int dp(int points){
        return (int)(points*getResources().getDisplayMetrics().density+0.5f);
    }
    private TextView paragraph(String value,int size,int color){
        TextView t=new TextView(this);
        t.setText(value);t.setTextSize(size);t.setTextColor(color);
        t.setPadding(dp(8),dp(10),dp(8),dp(10));
        return t;
    }
    private Button button(LinearLayout parent,String title){
        Button b=new Button(this);b.setText(title);b.setAllCaps(false);
        b.setTextColor(Color.WHITE);b.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(0xff5142ac));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));
        lp.setMargins(0,dp(8),0,0);parent.addView(b,lp);
        return b;
    }
    private String currentUid983(){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        return user==null?null:user.getUid();
    }
    private boolean current983(int gen,String uid){
        return KingConnectionDiagnostics983.active(
            gen,generation983,uid,currentUid983(),!destroyed983&&!isFinishing()&&!isDestroyed());
    }
    private String installedVersion983(){
        try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}
        catch(Exception e){return "unknown";}
    }
    private void addLine(String text){
        if(destroyed983||isFinishing()||isDestroyed())return;
        String row=KingConnectionDiagnostics983.safeLine(text,300);
        if(report.length()<4000)report.append(row).append('\n');
        output.setText(report.toString());
    }
    private void fail(int gen,String uid,Exception error,String operation){
        if(!current983(gen,uid))return;
        String code="UNKNOWN";
        if(error instanceof FirebaseFirestoreException)
            code=((FirebaseFirestoreException)error).getCode().name();
        addLine("✗ "+operation+": "+KingConnectionDiagnostics983.friendlyError(code));
        addLine("The test made no Room writes and charged no money.");
    }

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        ScrollView scroll=new ScrollView(this);
        LinearLayout column=new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(18),dp(14),dp(18),dp(30));
        column.setBackgroundColor(0xff11152b);
        scroll.addView(column);setContentView(scroll);

        TextView title=paragraph("‹    KING Plus • Firebase Test",21,Color.WHITE);
        title.setTypeface(null,Typeface.BOLD);
        title.setOnClickListener(v->finish());
        column.addView(title);

        column.addView(paragraph(
            "Test this on both phones. Use DIFFERENT signed-in Google/Firebase accounts and the SAME Party Room code. This test only reads Firebase; it does not join, leave or change a Room.",
            13,0xffc4c6d7));

        requestedRoom=new EditText(this);
        requestedRoom.setSingleLine(true);
        requestedRoom.setHint("6-digit Room Code or full Room ID (optional)");
        requestedRoom.setTextColor(Color.WHITE);
        requestedRoom.setHintTextColor(0xffaab1c7);
        requestedRoom.setInputType(InputType.TYPE_CLASS_TEXT);
        requestedRoom.setPadding(dp(8),dp(12),dp(8),dp(12));
        String preset=getIntent().getStringExtra("roomInput983");
        if(KingConnectionDiagnostics983.safeRoom(preset).length()>0)
            requestedRoom.setText(KingConnectionDiagnostics983.safeRoom(preset));
        column.addView(requestedRoom);

        Button test=button(column,"🔍 Run Firebase / Party Connection Test");
        Button copy=button(column,"📋 Copy Test Report");
        Button party=button(column,"🎤 Return to Party Room");

        output=paragraph("Tap Run to check Firebase sign-in and server connection.",14,0xfff0e7ad);
        output.setTextIsSelectable(true);
        column.addView(output);

        test.setOnClickListener(v->runTest983());
        copy.setOnClickListener(v->{
            try{
                ClipboardManager clipboard=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                if(clipboard==null)return;
                clipboard.setPrimaryClip(ClipData.newPlainText("KING Plus Firebase test",report.toString()));
                Toast.makeText(this,"Connection report copied. You may share it for support.",Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Toast.makeText(this,"Clipboard unavailable",Toast.LENGTH_SHORT).show();
            }
        });
        party.setOnClickListener(v->{
            Intent intent=new Intent(this,PartyActivity.class);
            startActivity(intent);finish();
        });
    }

    private void runTest983(){
        generation983++;
        final int gen=generation983;
        report.setLength(0);
        addLine("KING Plus v"+installedVersion983()+" • Firebase connection report");
        addLine("Android validated Internet: "+KingNetwork.online(this));
        final String uid=currentUid983();
        addLine("Firebase signed in: "+(uid!=null));
        addLine("Firebase UID prefix: "+KingConnectionDiagnostics983.uidPrefix(uid));
        if(!KingNetwork.online(this)){
            addLine("✗ Internet not validated. Turn on Wi-Fi or mobile data and retry.");
            return;
        }
        if(uid==null||uid.isEmpty()){
            addLine("✗ No Firebase account. Sign in with Google first.");
            return;
        }
        final String raw=requestedRoom.getText().toString().trim();
        final String roomCode=KingConnectionDiagnostics983.safeRoom(raw);
        if(!raw.isEmpty()&&roomCode.isEmpty()){
            addLine("✗ Room input should be a 6-digit Room Code or the exact full Room ID.");
            addLine("Your 6-digit KING User ID is NOT a Room Code.");
            return;
        }
        addLine("Room input: "+(roomCode.isEmpty()?"none":roomCode));
        addLine("1. Refreshing signed-in Firebase token…");
        final FirebaseUser auth=FirebaseAuth.getInstance().getCurrentUser();
        if(auth==null)return;
        auth.getIdToken(true)
            .addOnSuccessListener(token->{
                if(!current983(gen,uid))return;
                if(token==null||token.getToken()==null){
                    addLine("✗ Firebase token response was missing.");
                    return;
                }
                addLine("✓ Firebase account token refreshed.");
                FirebaseFirestore db=FirebaseFirestore.getInstance();
                addLine("2. Re-enabling Firestore network…");
                db.enableNetwork().addOnSuccessListener(unused->{
                    if(!current983(gen,uid))return;
                    addLine("✓ Firestore network enabled.");
                    checkFirestore983(db,uid,roomCode,gen);
                }).addOnFailureListener(error->fail(gen,uid,error,"Firestore network"));
            })
            .addOnFailureListener(error->{
                if(!current983(gen,uid))return;
                addLine("✗ Cannot refresh Firebase sign-in: "+
                    KingConnectionDiagnostics983.safeLine(error.getLocalizedMessage(),150));
                addLine("Sign out and sign in again if this keeps happening.");
            });
    }

    private void checkFirestore983(FirebaseFirestore db,String uid,String code,int gen){
        if(code.isEmpty()){
            addLine("3. Checking signed-in profile against Firestore SERVER…");
            db.collection("public_profiles").document(uid).get(Source.SERVER)
                .addOnSuccessListener(doc->{
                    if(!current983(gen,uid))return;
                    addLine("✓ Firestore server reachable. Own public profile: "+
                        (doc.exists()?"exists":"not created yet"));
                    addLine("Enter the OTHER phone's shared Room Code to test the Party itself.");
                }).addOnFailureListener(error->fail(gen,uid,error,"Firestore read"));
            return;
        }
        addLine("3. Looking up Party Room on Firestore SERVER…");
        if(KingConnectionDiagnostics983.code(code)){
            db.collection("live_rooms").whereEqualTo("joinCode",code).limit(5)
                .get(Source.SERVER).addOnSuccessListener(snapshot->{
                    if(!current983(gen,uid))return;
                    if(snapshot.isEmpty()){
                        addLine("✗ No Party Room has this code on Firebase server.");
                        addLine("Check that the host shared ROOM ID, not KING USER ID.");
                        return;
                    }
                    if(snapshot.size()>1){
                        addLine("⚠ Multiple rooms share the six-digit code. Use the full Room invitation.");
                        return;
                    }
                    inspectRoom983(snapshot.getDocuments().get(0),uid,gen);
                }).addOnFailureListener(error->fail(gen,uid,error,"Room Code lookup"));
        }else{
            db.collection("live_rooms").document(code).get(Source.SERVER)
                .addOnSuccessListener(doc->{
                    if(!current983(gen,uid))return;
                    inspectRoom983(doc,uid,gen);
                }).addOnFailureListener(error->fail(gen,uid,error,"Exact Room lookup"));
        }
    }

    private void inspectRoom983(DocumentSnapshot room,String uid,int gen){
        if(!current983(gen,uid))return;
        if(room==null||!room.exists()){
            addLine("✗ Room document does not exist on the server.");return;
        }
        addLine("✓ Party Room record exists on Firestore SERVER.");
        addLine("Host UID prefix: "+KingConnectionDiagnostics983.uidPrefix(room.getString("ownerUid")));
        addLine("Room is closed: "+Boolean.TRUE.equals(room.getBoolean("closed")));
        addLine("Room is private: "+Boolean.TRUE.equals(room.getBoolean("isPrivate")));
        if(Boolean.TRUE.equals(room.getBoolean("closed"))){
            addLine("✗ The Host closed this Room. Create/share a NEW room.");return;
        }
        if(Boolean.TRUE.equals(room.getBoolean("isPrivate"))){
            addLine("⚠ Private room may require an invitation/password before member access.");
        }
        addLine("4. Checking whether this Firebase UID is already a room member…");
        room.getReference().collection("members").document(uid).get(Source.SERVER)
            .addOnSuccessListener(member->{
                if(!current983(gen,uid))return;
                addLine("✓ Room member record: "+(member.exists()?"joined":"not joined yet"));
                if(!member.exists())addLine("Tap Join in the normal Party screen; this test never joins automatically.");
                addLine("Compare this report with the OTHER phone's report.");
            }).addOnFailureListener(error->fail(gen,uid,error,"Room membership read"));
    }

    @Override protected void onDestroy(){
        destroyed983=true;
        ++generation983;
        super.onDestroy();
    }
}
