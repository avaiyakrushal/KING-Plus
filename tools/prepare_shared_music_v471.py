from pathlib import Path
P=Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
s=P.read_text(encoding='utf-8')

if 'import com.google.firebase.Timestamp;' not in s:
    s=s.replace('import com.google.firebase.auth.FirebaseAuth;\n', 'import com.google.firebase.Timestamp;\nimport com.google.firebase.auth.FirebaseAuth;\n', 1)

field_anchor='    private float roomMusicVolume = 1.0f;\n'
fields='''    private float roomMusicVolume = 1.0f;\n    private String sharedMusicUrl = "";\n    private boolean sharedMusicFromHost;\n    private boolean roomMusicPrepared;\n    private boolean remoteDesiredPlaying;\n    private int remoteTargetPositionMs;\n'''
if 'private String sharedMusicUrl' not in s:
    s=s.replace(field_anchor, fields, 1)

start=s.index('    private String musicPrefsKey()')
end=s.index('\n    private void openVoice()', start)
new_music=r'''    private String musicPrefsKey(){return "music_playlist_"+(user==null?"local":user.getUid());}
    private void loadMusicPlaylist(){
        musicUris.clear();musicNames.clear();
        String raw=prefs==null?"":prefs.getString(musicPrefsKey(),"");
        if(raw==null||raw.isEmpty())return;
        for(String row:raw.split("\\n")){
            if(row==null||row.isEmpty())continue;int cut=row.indexOf('\t');if(cut<1)continue;
            try{String u=Uri.decode(row.substring(0,cut));String n=Uri.decode(row.substring(cut+1));if(!u.isEmpty()){musicUris.add(u);musicNames.add(n.isEmpty()?"Unknown song":n);}}catch(Exception ignored){}
        }
    }
    private void saveMusicPlaylist(){
        if(prefs==null)return;StringBuilder out=new StringBuilder();
        for(int i=0;i<musicUris.size();i++){if(i>0)out.append('\n');out.append(Uri.encode(musicUris.get(i))).append('\t').append(Uri.encode(musicNames.get(i)));}
        prefs.edit().putString(musicPrefsKey(),out.toString()).apply();
    }
    private boolean isSharedMusicUri(String value){return value!=null&&value.trim().toLowerCase(java.util.Locale.ROOT).startsWith("https://");}
    private void musicPanel(){
        final AlertDialog[] holder=new AlertDialog[1];
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(0,dp(8),0,dp(10));root.setBackgroundColor(0xff18a88b);
        LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(12),0,dp(10),0);
        TextView back=tv("‹",38,Color.WHITE,true);back.setGravity(Gravity.CENTER);head.addView(back,new LinearLayout.LayoutParams(dp(54),dp(58)));
        TextView title=tv("Playlist ("+musicNames.size()+")",21,Color.WHITE,true);title.setGravity(Gravity.CENTER_VERTICAL);head.addView(title,new LinearLayout.LayoutParams(0,dp(58),1));
        final boolean[] editMode={false};
        TextView edit=tv("Edit",16,Color.WHITE,true);edit.setGravity(Gravity.CENTER);head.addView(edit,new LinearLayout.LayoutParams(dp(64),dp(58)));root.addView(head,new LinearLayout.LayoutParams(-1,dp(62)));

        LinearLayout searchBar=new LinearLayout(this);searchBar.setGravity(Gravity.CENTER_VERTICAL);searchBar.setPadding(dp(14),dp(4),dp(14),dp(8));searchBar.setBackgroundColor(0x22111111);
        EditText search=new EditText(this);search.setSingleLine(true);search.setHint("Search for songs");search.setHintTextColor(0x99ffffff);search.setTextColor(Color.WHITE);search.setBackgroundColor(Color.TRANSPARENT);searchBar.addView(search,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView go=pill("⌕",0x22000000,null);go.setTextSize(24);searchBar.addView(go,new LinearLayout.LayoutParams(dp(56),dp(46)));root.addView(searchBar,new LinearLayout.LayoutParams(-1,dp(66)));

        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView add=pill("ADD MUSIC",0xfff7f7f7,this::chooseMusicSource);add.setTextColor(0xff46a991);add.setTextSize(16);LinearLayout.LayoutParams alp=new LinearLayout.LayoutParams(-1,dp(58));alp.setMargins(dp(42),dp(10),dp(42),0);root.addView(add,alp);

        Runnable refresh=()->{title.setText("Playlist ("+musicNames.size()+")");renderMusicPlaylistRows(list,search.getText().toString(),editMode[0],holder[0]);};
        go.setOnClickListener(v->refresh.run());
        edit.setOnClickListener(v->{editMode[0]=!editMode[0];edit.setText(editMode[0]?"Done":"Edit");refresh.run();});
        AlertDialog dialog=new AlertDialog.Builder(this).setView(root).create();holder[0]=dialog;back.setOnClickListener(v->dialog.dismiss());
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setLayout(-1,-1);w.setGravity(Gravity.BOTTOM);w.setBackgroundDrawable(bg(0xff18a88b,0));}});
        refresh.run();dialog.show();
    }
    private void renderMusicPlaylistRows(LinearLayout list,String query,boolean editMode,AlertDialog dialog){
        list.removeAllViews();String q=query==null?"":query.trim().toLowerCase(java.util.Locale.ROOT);int shown=0;
        for(int i=0;i<musicNames.size();i++){
            String name=musicNames.get(i);if(!q.isEmpty()&&!name.toLowerCase(java.util.Locale.ROOT).contains(q))continue;shown++;final int index=i;
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(28),dp(10),dp(18),dp(10));row.setBackgroundColor(0x10000000);
            LinearLayout text=new LinearLayout(this);text.setOrientation(LinearLayout.VERTICAL);TextView n=tv(name,17,Color.WHITE,true);n.setSingleLine(true);text.addView(n,new LinearLayout.LayoutParams(-1,dp(34)));
            String source=musicUris.get(i);TextView sub=tv(isSharedMusicUri(source)?"🌐 Shared room audio":"📱 Device audio",12,0x99ffffff,true);text.addView(sub,new LinearLayout.LayoutParams(-1,dp(26)));row.addView(text,new LinearLayout.LayoutParams(0,dp(68),1));
            if(editMode){TextView del=pill("✕",0x33ffffff,()->{removeMusicAt(index);renderMusicPlaylistRows(list,query,true,dialog);});row.addView(del,new LinearLayout.LayoutParams(dp(46),dp(42)));}
            row.setOnClickListener(v->{if(editMode)return;playSong(index);if(dialog!=null)dialog.dismiss();showRoomMusicPlayer();});
            LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(86));rp.setMargins(0,0,0,dp(1));list.addView(row,rp);
        }
        if(shown==0){TextView empty=tv(musicNames.isEmpty()?"No songs added yet":"No matching songs",16,0xddffffff,true);empty.setGravity(Gravity.CENTER);list.addView(empty,new LinearLayout.LayoutParams(-1,dp(150)));}
    }
    private void removeMusicAt(int index){
        if(index<0||index>=musicUris.size())return;
        boolean playing=index==currentSongIndex;musicUris.remove(index);musicNames.remove(index);
        if(playing)stopRoomMusic();else if(currentSongIndex>index)currentSongIndex--;
        saveMusicPlaylist();toast("Song removed");
    }
    private void chooseMusicSource(){
        if(!isModerator()){toast("Host/co-host only");return;}
        String[] items={"📱 Add songs from phone","🌐 Add shared HTTPS music link"};
        new AlertDialog.Builder(this).setTitle("Add Music").setItems(items,(d,w)->{if(w==0)openMusicPicker();else addSharedMusicLink();}).setNegativeButton("Cancel",null).show();
    }
    private void addSharedMusicLink(){
        if(cloudRoom&&!isOwner()){toast("Room-wide shared music is controlled by the host");return;}
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),dp(6),dp(18),0);
        EditText name=new EditText(this);name.setHint("Song name");box.addView(name,new LinearLayout.LayoutParams(-1,dp(56)));
        EditText url=new EditText(this);url.setHint("https://... direct audio link");url.setSingleLine(true);box.addView(url,new LinearLayout.LayoutParams(-1,dp(56)));
        new AlertDialog.Builder(this).setTitle("Shared room music").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Add",(d,w)->{
            String u=url.getText().toString().trim();String n=name.getText().toString().trim();
            if(!isSharedMusicUri(u)){toast("Use a direct HTTPS audio link");return;}
            if(musicUris.contains(u)){toast("This song is already in the playlist");return;}
            if(n.isEmpty())n=inferSharedSongName(u);musicUris.add(u);musicNames.add(n);saveMusicPlaylist();toast("Shared song added and saved");musicPanel();
        }).show();
    }
    private String inferSharedSongName(String url){
        try{Uri u=Uri.parse(url);String x=u.getLastPathSegment();if(x!=null&&!x.trim().isEmpty())return Uri.decode(x);}catch(Exception ignored){}return "Shared song";
    }
    private void openMusicPicker(){
        if(!isModerator()){toast("Host/co-host only");return;}
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("audio/*");i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i,MUSIC_PICK_REQUEST);
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode!=MUSIC_PICK_REQUEST||resultCode!=RESULT_OK||data==null)return;
        ArrayList<Uri> picked=new ArrayList<>();
        if(data.getClipData()!=null){for(int i=0;i<data.getClipData().getItemCount();i++){Uri u=data.getClipData().getItemAt(i).getUri();if(u!=null)picked.add(u);}}
        else if(data.getData()!=null)picked.add(data.getData());
        int added=0;for(Uri uri:picked){String us=uri.toString();if(musicUris.contains(us))continue;try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}musicUris.add(us);musicNames.add(songName(uri));added++;}
        if(added>0){saveMusicPlaylist();toast(added+" song"+(added==1?"":"s")+" added and saved");musicPanel();}else toast("No new songs added");
    }
    private String songName(Uri uri){
        String name=null;Cursor c=null;try{c=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null);if(c!=null&&c.moveToFirst()){int col=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(col>=0)name=c.getString(col);}}catch(Exception ignored){}finally{if(c!=null)c.close();}
        if(name==null||name.trim().isEmpty())name=uri.getLastPathSegment();if(name==null||name.trim().isEmpty())name="Unknown song";return name;
    }
    private void playSong(int index){
        if(index<0||index>=musicUris.size())return;
        String source=musicUris.get(index);String name=musicNames.get(index);boolean shared=isSharedMusicUri(source);releaseMusicPlayer();
        currentSongIndex=index;currentSong=name;sharedMusicUrl=shared?source:"";sharedMusicFromHost=false;roomMusicPrepared=false;updateMusicStatus();
        try{
            roomMusicPlayer=new MediaPlayer();roomMusicPlayer.setDataSource(this,Uri.parse(source));roomMusicPlayer.setLooping(false);roomMusicPlayer.setVolume(roomMusicVolume,roomMusicVolume);
            roomMusicPlayer.setOnPreparedListener(mp->{roomMusicPrepared=true;mp.start();updateMusicStatus();if(cloudRoom&&isOwner()){if(shared)publishSharedMusic(true);else clearSharedMusicState();}addEvent("music",safeName()+" played 🎵 "+currentSong);});
            roomMusicPlayer.setOnCompletionListener(mp->{if(cloudRoom&&!isOwner()&&sharedMusicFromHost)return;playNextSong();});roomMusicPlayer.prepareAsync();
        }catch(Exception e){releaseMusicPlayer();toast("Music failed: "+msg(e));}
    }
    private void releaseMusicPlayer(){try{if(roomMusicPlayer!=null){roomMusicPlayer.stop();roomMusicPlayer.release();}}catch(Exception ignored){}roomMusicPlayer=null;roomMusicPrepared=false;}
    private void playNextSong(){if(cloudRoom&&!isOwner()&&sharedMusicFromHost){toast("Host controls room music");return;}if(musicUris.isEmpty()){stopRoomMusic();return;}int n=currentSongIndex<0?0:(currentSongIndex+1)%musicUris.size();playSong(n);}
    private void playPreviousSong(){if(cloudRoom&&!isOwner()&&sharedMusicFromHost){toast("Host controls room music");return;}if(musicUris.isEmpty()){toast("Playlist is empty");return;}int n=currentSongIndex<0?0:(currentSongIndex-1+musicUris.size())%musicUris.size();playSong(n);}
    private void toggleRoomMusic(){
        if(cloudRoom&&!isOwner()&&sharedMusicFromHost){toast("Host controls room music");return;}
        if(roomMusicPlayer==null){if(musicUris.isEmpty()){toast("Add music first");return;}playSong(currentSongIndex>=0?currentSongIndex:0);return;}
        try{if(roomMusicPlayer.isPlaying())roomMusicPlayer.pause();else roomMusicPlayer.start();if(cloudRoom&&isOwner()&&!sharedMusicUrl.isEmpty())publishSharedMusic(roomMusicPlayer.isPlaying());updateMusicStatus();}catch(Exception e){toast("Music control unavailable");}
    }
    private void showRoomMusicPlayer(){
        if(currentSongIndex<0&&musicUris.isEmpty()&&sharedMusicUrl.isEmpty()){musicPanel();return;}
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(14),dp(18),dp(18));root.setBackgroundColor(0xee202734);
        String shown=currentSongIndex>=0&&currentSongIndex<musicNames.size()?musicNames.get(currentSongIndex):(currentSong.isEmpty()?"Room Music":currentSong);
        TextView title=tv(shown,19,Color.WHITE,true);title.setSingleLine(true);root.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));
        if(cloudRoom&&sharedMusicFromHost&&!isOwner()){TextView sync=tv("🌐 Synced with host • playback controls are host-only",12,0xffcfc4df,true);root.addView(sync,new LinearLayout.LayoutParams(-1,dp(34)));}
        LinearLayout vol=new LinearLayout(this);vol.setGravity(Gravity.CENTER_VERTICAL);TextView speaker=tv("🔊",24,Color.WHITE,true);vol.addView(speaker,new LinearLayout.LayoutParams(dp(48),dp(48)));SeekBar seek=new SeekBar(this);seek.setMax(100);seek.setProgress(Math.round(roomMusicVolume*100));vol.addView(seek,new LinearLayout.LayoutParams(0,dp(48),1));TextView pct=tv(Math.round(roomMusicVolume*100)+"%",13,Color.WHITE,true);pct.setGravity(Gravity.CENTER);vol.addView(pct,new LinearLayout.LayoutParams(dp(58),dp(48)));root.addView(vol,new LinearLayout.LayoutParams(-1,dp(56)));
        LinearLayout controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER);TextView prev=pill("|◀",0x00302a3a,this::playPreviousSong);controls.addView(prev,new LinearLayout.LayoutParams(0,dp(62),1));TextView play=pill(roomMusicPlayer!=null&&roomMusicPrepared&&roomMusicPlayer.isPlaying()?"Ⅱ":"▶",0xfff7f7f7,null);play.setTextColor(0xff202734);play.setTextSize(26);controls.addView(play,new LinearLayout.LayoutParams(dp(74),dp(66)));TextView next=pill("▶|",0x00302a3a,this::playNextSong);controls.addView(next,new LinearLayout.LayoutParams(0,dp(62),1));TextView list=pill("☷",0x00302a3a,this::musicPanel);list.setTextSize(26);controls.addView(list,new LinearLayout.LayoutParams(0,dp(62),1));root.addView(controls,new LinearLayout.LayoutParams(-1,dp(78)));
        AlertDialog dialog=new AlertDialog.Builder(this).setView(root).create();
        play.setOnClickListener(v->{toggleRoomMusic();dialog.dismiss();showRoomMusicPlayer();});
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean from){roomMusicVolume=p/100f;pct.setText(p+"%");if(roomMusicPlayer!=null)try{roomMusicPlayer.setVolume(roomMusicVolume,roomMusicVolume);}catch(Exception ignored){}}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setLayout(-1,-2);w.setGravity(Gravity.BOTTOM);w.setBackgroundDrawable(bg(0xee202734,22));}});dialog.show();
    }
    private void publishSharedMusic(boolean playing){
        if(!cloudRoom||!isOwner()||db==null||roomId==null||sharedMusicUrl.isEmpty())return;
        long pos=0;try{if(roomMusicPlayer!=null&&roomMusicPrepared)pos=roomMusicPlayer.getCurrentPosition();}catch(Exception ignored){}
        Map<String,Object> m=new HashMap<>();m.put("currentSong",currentSong);m.put("musicUrl",sharedMusicUrl);m.put("musicPlaying",playing);m.put("musicPositionMs",pos);m.put("musicUpdatedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).update(m).addOnFailureListener(e->toast("Room music sync failed: "+msg(e)));
    }
    private void clearSharedMusicState(){
        if(!cloudRoom||!isOwner()||db==null||roomId==null)return;
        Map<String,Object> m=new HashMap<>();m.put("currentSong","");m.put("musicUrl","");m.put("musicPlaying",false);m.put("musicPositionMs",0);m.put("musicUpdatedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).update(m);
    }
    private void applySharedMusic(DocumentSnapshot doc){
        if(isOwner())return;
        String url=doc.getString("musicUrl");if(url==null)url="";String name=str(doc,"currentSong","Room Music");boolean playing=Boolean.TRUE.equals(doc.getBoolean("musicPlaying"));Long p=doc.getLong("musicPositionMs");long target=p==null?0:Math.max(0,p);
        Timestamp stamp=doc.getTimestamp("musicUpdatedAt");if(playing&&stamp!=null){long elapsed=Math.max(0,System.currentTimeMillis()-stamp.toDate().getTime());target+=elapsed;}
        if(url.isEmpty()){if(sharedMusicFromHost)stopRoomMusic(false);return;}
        if(!isSharedMusicUri(url))return;remoteDesiredPlaying=playing;remoteTargetPositionMs=(int)Math.min(Integer.MAX_VALUE,target);
        if(!sharedMusicFromHost||roomMusicPlayer==null||!url.equals(sharedMusicUrl)){playSharedFromHost(url,name,remoteTargetPositionMs,playing);return;}
        if(!roomMusicPrepared)return;
        try{int now=roomMusicPlayer.getCurrentPosition();if(Math.abs(now-remoteTargetPositionMs)>1800)roomMusicPlayer.seekTo(remoteTargetPositionMs);if(playing&&!roomMusicPlayer.isPlaying())roomMusicPlayer.start();else if(!playing&&roomMusicPlayer.isPlaying())roomMusicPlayer.pause();currentSong=name;updateMusicStatus();}catch(Exception ignored){}
    }
    private void playSharedFromHost(String url,String name,int position,boolean playing){
        releaseMusicPlayer();sharedMusicFromHost=true;sharedMusicUrl=url;currentSong=name;currentSongIndex=-1;remoteTargetPositionMs=Math.max(0,position);remoteDesiredPlaying=playing;updateMusicStatus();
        try{
            roomMusicPlayer=new MediaPlayer();roomMusicPlayer.setDataSource(this,Uri.parse(url));roomMusicPlayer.setVolume(roomMusicVolume,roomMusicVolume);roomMusicPlayer.setLooping(false);
            roomMusicPlayer.setOnPreparedListener(mp->{roomMusicPrepared=true;try{if(remoteTargetPositionMs>0)mp.seekTo(remoteTargetPositionMs);if(remoteDesiredPlaying)mp.start();}catch(Exception ignored){}updateMusicStatus();});
            roomMusicPlayer.setOnCompletionListener(mp->{});roomMusicPlayer.prepareAsync();
        }catch(Exception e){releaseMusicPlayer();sharedMusicFromHost=false;sharedMusicUrl="";toast("Shared music unavailable: "+msg(e));}
    }
    private void stopRoomMusic(){stopRoomMusic(true);}
    private void stopRoomMusic(boolean publish){releaseMusicPlayer();currentSongIndex=-1;currentSong="";sharedMusicUrl="";sharedMusicFromHost=false;updateMusicStatus();if(publish&&cloudRoom&&isOwner())clearSharedMusicState();}
    private void updateMusicStatus(){
        if(musicStatusLabel==null)return;String state=currentSong.isEmpty()?"":"🎵 "+currentSong+(roomMusicPlayer!=null&&roomMusicPrepared&&roomMusicPlayer.isPlaying()?" • Playing":" • Paused")+(sharedMusicFromHost?" • Room sync":"");
        runOnUiThread(()->{musicStatusLabel.setText(state);musicStatusLabel.setVisibility(state.isEmpty()?View.GONE:View.VISIBLE);});
    }
'''
s=s[:start]+new_music+s[end:]

needle='roomPrivate=Boolean.TRUE.equals(doc.getBoolean("isPrivate"));roomHasPassword=Boolean.TRUE.equals(doc.getBoolean("hasPassword"));if(announcementLabel!=null)'
if needle in s and 'applySharedMusic(doc);' not in s:
    s=s.replace(needle,'roomPrivate=Boolean.TRUE.equals(doc.getBoolean("isPrivate"));roomHasPassword=Boolean.TRUE.equals(doc.getBoolean("hasPassword"));applySharedMusic(doc);if(announcementLabel!=null)',1)
else:
    print('warning room listener anchor not replaced')

P.write_text(s,encoding='utf-8')
print('prepared shared music v4.7.1')
