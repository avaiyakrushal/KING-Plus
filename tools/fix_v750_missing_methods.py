from pathlib import Path

# Room multiplayer completion fix.
rp = Path('app/src/main/java/com/kingplus/social/RoomGameActivity.java')
s = rp.read_text()

if 'private ListenerRegistration stateListener,movesListener,readyListener;' not in s:
    s = s.replace(
        'private ListenerRegistration stateListener,movesListener;',
        'private ListenerRegistration stateListener,movesListener,readyListener;'
    )
if 'private LinearLayout page,controls,movesBox,readyBox;' not in s:
    s = s.replace(
        'private LinearLayout page,controls,movesBox;',
        'private LinearLayout page,controls,movesBox,readyBox;'
    )
if 'if(readyListener!=null)readyListener.remove();' not in s:
    s = s.replace(
        'if(movesListener!=null)movesListener.remove();super.onDestroy();',
        'if(movesListener!=null)movesListener.remove();if(readyListener!=null)readyListener.remove();super.onDestroy();'
    )
if 'TextView readyTitle=tv("Players ready"' not in s:
    anchor = 'TextView live=tv("Live players / moves",16,Color.WHITE,true);'
    ready = (
        'TextView readyTitle=tv("Players ready",16,Color.WHITE,true);'
        'readyTitle.setPadding(dp(2),dp(14),0,dp(4));page.addView(readyTitle);'
        'readyBox=new LinearLayout(this);readyBox.setOrientation(LinearLayout.VERTICAL);'
        'page.addView(readyBox,new LinearLayout.LayoutParams(-1,-2));\n        '
    )
    if anchor not in s:
        raise SystemExit('Room ready UI anchor missing')
    s = s.replace(anchor, ready + anchor, 1)

if 'private void listenReady750(){' not in s:
    anchor = '    private void renderMoves(){'
    block = '''    private void listenReady750(){
        if(readyListener!=null)readyListener.remove();
        readyListener=room().collection("game_ready").addSnapshotListener((q,e)->{
            readyBox.removeAllViews();
            if(e!=null){readyBox.addView(tv("Ready list unavailable",13,MUTED,false));return;}
            List<DocumentSnapshot> docs=q==null?new ArrayList<>():new ArrayList<>(q.getDocuments());
            Collections.sort(docs,(a,b)->s(a.getString("name")).compareToIgnoreCase(s(b.getString("name"))));
            if(docs.isEmpty()){readyBox.addView(tv("Nobody is ready yet",13,MUTED,false));return;}
            for(DocumentSnapshot d:docs){String name=s(d.getString("name"));TextView row=tv("✅ "+(name.isEmpty()?"Player":name),14,Color.WHITE,true);row.setBackground(bg(CARD,12));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(44));lp.setMargins(0,dp(3),0,dp(3));readyBox.addView(row,lp);}
        });
    }
    private void toggleReady750(){
        DocumentReference r=room().collection("game_ready").document(me.getUid());
        r.get().addOnSuccessListener(d->{
            if(d.exists())r.delete().addOnSuccessListener(v->toast("Not ready"));
            else{Map<String,Object> m=new HashMap<>();m.put("uid",me.getUid());m.put("name",displayName);m.put("ready",true);m.put("updatedAt",FieldValue.serverTimestamp());r.set(m).addOnSuccessListener(v->toast("Ready ✓")).addOnFailureListener(e->toast("Ready failed: "+e.getMessage()));}
        }).addOnFailureListener(e->toast("Ready failed: "+e.getMessage()));
    }
    private void clearReady750(){
        if(!moderator)return;
        room().collection("game_ready").get().addOnSuccessListener(q->{for(DocumentSnapshot d:q.getDocuments())d.getReference().delete();toast("Ready list cleared");});
    }
    private void rematch750(){
        if(!moderator)return;
        String game=type.isEmpty()?"rps":type;
        startRoundNow750(game);
    }

'''
    if anchor not in s:
        raise SystemExit('Room method anchor missing')
    s = s.replace(anchor, block + anchor, 1)

rp.write_text(s)

# Community visitors completion fix.
cp = Path('app/src/main/java/com/kingplus/social/CommunityHubActivity.java')
c = cp.read_text()
if 'import com.google.firebase.firestore.Query;' not in c:
    c = c.replace(
        'import com.google.firebase.firestore.QuerySnapshot;',
        'import com.google.firebase.firestore.Query;\nimport com.google.firebase.firestore.QuerySnapshot;'
    )
if 'private void visitors750(){' not in c:
    anchor = '    private void collection(){'
    block = '''    private void visitors750(){
        body.addView(tv("Profile Visitors",21,DARK,true));
        if(!cloud()){body.addView(tv("Sign in to see profile visitors.",14,MUTED,false));return;}
        db.collection("public_profiles").document(me.getUid()).collection("visitors")
          .orderBy("updatedAt",Query.Direction.DESCENDING).limit(50).get()
          .addOnSuccessListener(q->{
              if(q.isEmpty()){body.addView(tv("No profile visitors yet.",14,MUTED,false));return;}
              for(DocumentSnapshot d:q.getDocuments()){
                  String uid=safe(d.getString("uid"),d.getId()),name=safe(d.getString("name"),"KING User");
                  TextView row=tv("👁  "+name,14,DARK,true);row.setBackground(bg(CARD,14));
                  row.setOnClickListener(v->openChat(uid,name));
                  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.setMargins(0,dp(5),0,dp(5));body.addView(row,lp);
              }
          }).addOnFailureListener(e->body.addView(tv("Visitors unavailable right now.",14,MUTED,false)));
    }

'''
    if anchor not in c:
        raise SystemExit('Community collection anchor missing')
    c = c.replace(anchor, block + anchor, 1)
cp.write_text(c)

print('v7.5 missing method bodies fixed')
