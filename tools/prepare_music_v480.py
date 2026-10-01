from pathlib import Path
P=Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
s=P.read_text(encoding='utf-8')

# Firebase Storage imports for host-selected audio upload.
if 'import com.google.firebase.storage.FirebaseStorage;' not in s:
    s=s.replace('import com.google.firebase.firestore.WriteBatch;\n', 'import com.google.firebase.firestore.WriteBatch;\nimport com.google.firebase.storage.FirebaseStorage;\nimport com.google.firebase.storage.StorageReference;\n',1)

# State used by shared room playback.
s=s.replace('    private FirebaseFirestore db;\n', '    private FirebaseFirestore db;\n    private FirebaseStorage storage;\n',1)
s=s.replace('    private float roomMusicVolume = 1.0f;\n', '''    private float roomMusicVolume = 1.0f;\n    private final ArrayList<String> musicRemoteUrls = new ArrayList<>();\n    private ListenerRegistration musicStateListener;\n    private String sharedMusicUrl = "";\n    private boolean applyingSharedMusic;\n''',1)
s=s.replace('        try { db = FirebaseFirestore.getInstance(); } catch (Exception ignored) { db = null; }\n', '        try { db = FirebaseFirestore.getInstance(); } catch (Exception ignored) { db = null; }\n        try { storage = FirebaseStorage.getInstance(); } catch (Exception ignored) { storage = null; }\n',1)

# Ensure shared listener is cleaned up with room listeners.
s=s.replace('ListenerRegistration[] ls = {roomsListener,roomListener,seatsListener,membersListener,messagesListener,eventsListener,roleListener,selfMemberListener,roomBanListener,seatLocksListener,seatRequestsListener};',
            'ListenerRegistration[] ls = {roomsListener,roomListener,seatsListener,membersListener,messagesListener,eventsListener,roleListener,selfMemberListener,roomBanListener,seatLocksListener,seatRequestsListener,musicStateListener};',1)
s=s.replace('        seatLocksListener = seatRequestsListener = null;\n', '        seatLocksListener = seatRequestsListener = musicStateListener = null;\n',1)

# Upgrade playlist serialization to keep an optional uploaded URL while remaining backward-compatible with v4.7 rows.
start=s.index('    private String musicPrefsKey(){')
end=s.index('\n    private void musicPanel(){', start)
playlist_helpers=r'''    private String musicPrefsKey(){return "music_playlist_"+(user==null?"local":user.getUid());}
    private void loadMusicPlaylist(){
        musicUris.clear();musicNames.clear();musicRemoteUrls.clear();
        String raw=prefs==null?"":prefs.getString(musicPrefsKey(),"");
        if(raw==null||raw.isEmpty())return;
        for(String row:raw.split("\\n")){
            if(row==null||row.isEmpty())continue;String[] parts=row.split("\\t",-1);if(parts.length<2)continue;
            try{
                String u=Uri.decode(parts[0]);String n=Uri.decode(parts[1]);String remote=parts.length>2?Uri.decode(parts[2]):"";
                if(!u.isEmpty()){musicUris.add(u);musicNames.add(n.isEmpty()?"Unknown song":n);musicRemoteUrls.add(remote==null?"":remote);}
            }catch(Exception ignored){}
        }
    }
    private void saveMusicPlaylist(){
        if(prefs==null)return;StringBuilder out=new StringBuilder();
        while(musicRemoteUrls.size()<musicUris.size())musicRemoteUrls.add("");
        for(int i=0;i<musicUris.size();i++){
            if(i>0)out.append('\n');
            out.append(Uri.encode(musicUris.get(i))).append('\t').append(Uri.encode(musicNames.get(i))).append('\t').append(Uri.encode(musicRemoteUrls.get(i)==null?"":musicRemoteUrls.get(i)));
        }
        prefs.edit().putString(musicPrefsKey(),out.toString()).apply();
    }
'''
s=s[:start]+playlist_helpers+s[end:]

# Keep remote URL list aligned on remove and add.
s=s.replace('musicUris.remove(index);musicNames.remove(index);', 'musicUris.remove(index);musicNames.remove(index);if(index<musicRemoteUrls.size())musicRemoteUrls.remove(index);',1)
s=s.replace('musicUris.add(us);musicNames.add(songName(uri));added++;', 'musicUris.add(us);musicNames.add(songName(uri));musicRemoteUrls.add("");added++;',1)

# Replace playback core with shared upload + event sync while preserving local-room behavior.
start=s.index('    private void playSong(int index){')
end=s.index('\n    private void showRoomMusicPlayer(){', start)
playback=r'''    private void playSong(int index){
        if(index<0||index>=musicUris.size())return;
        if(cloudRoom&&isModerator()){shareSongAndPlay(index);return;}
        playLocalSong(index);
    }
    private void playLocalSong(int index){
        if(index<0||index>=musicUris.size())return;releaseMusicPlayer();applyingSharedMusic=false;
        try{
            Uri uri=Uri.parse(musicUris.get(index));roomMusicPlayer=MediaPlayer.create(this,uri);
            if(roomMusicPlayer==null){toast("Could not open this audio file");return;}
            currentSongIndex=index;currentSong=musicNames.get(index);roomMusicPlayer.setLooping(false);roomMusicPlayer.setVolume(roomMusicVolume,roomMusicVolume);
            roomMusicPlayer.setOnCompletionListener(mp->{if(cloudRoom&&isModerator())playNextSong();else if(!cloudRoom)playNextSong();});
            roomMusicPlayer.start();updateMusicStatus();addEvent("music",safeName()+" played 🎵 "+currentSong);
        }catch(Exception e){releaseMusicPlayer();toast("Music failed: "+msg(e));}
    }
    private String safeStorageName(String name){
        String x=name==null?"song":name.replaceAll("[^A-Za-z0-9._-]","_");if(x.length()>80)x=x.substring(x.length()-80);return x.isEmpty()?"song":x;
    }
    private long audioSize(Uri uri){
        Cursor c=null;try{c=getContentResolver().query(uri,new String[]{OpenableColumns.SIZE},null,null,null);if(c!=null&&c.moveToFirst()){int col=c.getColumnIndex(OpenableColumns.SIZE);if(col>=0&&!c.isNull(col))return c.getLong(col);}}catch(Exception ignored){}finally{if(c!=null)c.close();}return -1;
    }
    private void shareSongAndPlay(int index){
        if(index<0||index>=musicUris.size())return;if(!isModerator()){toast("Host/co-host controls room music");return;}
        while(musicRemoteUrls.size()<musicUris.size())musicRemoteUrls.add("");
        String known=musicRemoteUrls.get(index);
        if(known!=null&&!known.isEmpty()){
            playLocalSong(index);sharedMusicUrl=known;publishMusicSync(known,musicNames.get(index),0,true);return;
        }
        if(storage==null||user==null||roomId==null){toast("Shared upload unavailable • playing on this phone only");playLocalSong(index);return;}
        playLocalSong(index);
        Uri local=Uri.parse(musicUris.get(index));long bytes=audioSize(local);if(bytes>=12L*1024L*1024L){toast("Song is over the 12 MB shared-music limit • local playback continues");return;}
        toast("Uploading room music…");
        String path="chat_media/"+user.getUid()+"/room_music_"+roomId+"/"+System.currentTimeMillis()+"_"+safeStorageName(musicNames.get(index));
        StorageReference ref=storage.getReference().child(path);
        ref.putFile(local).continueWithTask(task->{if(!task.isSuccessful()){Exception ex=task.getException();if(ex!=null)throw ex;throw new RuntimeException("upload failed");}return ref.getDownloadUrl();})
            .addOnSuccessListener(uri->{if(index>=musicRemoteUrls.size())return;String url=uri.toString();musicRemoteUrls.set(index,url);saveMusicPlaylist();if(currentSongIndex==index){sharedMusicUrl=url;boolean nowPlaying=false;try{nowPlaying=roomMusicPlayer!=null&&roomMusicPlayer.isPlaying();}catch(Exception ignored){}publishMusicSync(url,musicNames.get(index),safePlayerPosition(),nowPlaying);toast("Room music shared");}})
            .addOnFailureListener(e->toast("Shared upload failed • local playback continues: "+msg(e)));
    }
    private int safePlayerPosition(){try{return roomMusicPlayer==null?0:Math.max(0,roomMusicPlayer.getCurrentPosition());}catch(Exception ignored){return 0;}}
    private void publishMusicSync(String url,String name,long positionMs,boolean playing){
        if(!cloudRoom||!isModerator()||db==null||user==null||roomId==null)return;
        Map<String,Object>d=new HashMap<>();d.put("actorUid",user.getUid());d.put("actorName",safeName());d.put("type","music_sync");d.put("text","Room music sync");
        d.put("musicUrl",url==null?"":url);d.put("musicName",name==null?"":name);d.put("musicPlaying",playing);d.put("musicPositionMs",Math.max(0,positionMs));d.put("createdAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("events").add(d).addOnFailureListener(e->toast("Music sync failed: "+msg(e)));
    }
    private void attachMusicSync(DocumentReference room){
        musicStateListener=room.collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(30).addSnapshotListener((snap,e)->{
            if(e!=null||snap==null)return;
            for(DocumentSnapshot d:snap.getDocuments()){
                if(!"music_sync".equals(d.getString("type")))continue;
                String actor=d.getString("actorUid");if(user!=null&&user.getUid().equals(actor))return;
                applyMusicSync(d);return;
            }
        });
    }
    private void applyMusicSync(DocumentSnapshot d){
        String url=str(d,"musicUrl","");String name=str(d,"musicName","");boolean playing=Boolean.TRUE.equals(d.getBoolean("musicPlaying"));Long posObj=d.getLong("musicPositionMs");long pos=posObj==null?0:Math.max(0,posObj);
        com.google.firebase.Timestamp ts=d.getTimestamp("createdAt");if(playing&&ts!=null)pos+=Math.max(0,System.currentTimeMillis()-ts.toDate().getTime());
        if(url.isEmpty()){releaseMusicPlayer();sharedMusicUrl="";currentSong=name;currentSongIndex=-1;updateMusicStatus();return;}
        if(!url.equals(sharedMusicUrl)||roomMusicPlayer==null){startSharedStream(url,name,pos,playing);return;}
        currentSong=name;try{int now=roomMusicPlayer.getCurrentPosition();if(Math.abs(now-pos)>1800)roomMusicPlayer.seekTo((int)Math.min(Integer.MAX_VALUE,pos));if(playing&&!roomMusicPlayer.isPlaying())roomMusicPlayer.start();else if(!playing&&roomMusicPlayer.isPlaying())roomMusicPlayer.pause();}catch(Exception ignored){}updateMusicStatus();
    }
    private void startSharedStream(String url,String name,long positionMs,boolean playing){
        releaseMusicPlayer();sharedMusicUrl=url;currentSong=name;currentSongIndex=-1;applyingSharedMusic=true;updateMusicStatus();
        try{
            MediaPlayer p=new MediaPlayer();roomMusicPlayer=p;p.setDataSource(url);p.setVolume(roomMusicVolume,roomMusicVolume);p.setLooping(false);
            final long target=Math.max(0,positionMs);p.setOnPreparedListener(mp->{try{if(target>0)mp.seekTo((int)Math.min(Integer.MAX_VALUE,target));if(playing)mp.start();}catch(Exception ignored){}updateMusicStatus();});
            p.setOnCompletionListener(mp->{currentSong="";sharedMusicUrl="";updateMusicStatus();});
            p.setOnErrorListener((mp,what,extra)->{toast("Shared music could not play");return false;});p.prepareAsync();
        }catch(Exception e){releaseMusicPlayer();toast("Shared music failed: "+msg(e));}
    }
    private void releaseMusicPlayer(){try{if(roomMusicPlayer!=null){roomMusicPlayer.stop();roomMusicPlayer.release();}}catch(Exception ignored){}roomMusicPlayer=null;}
    private void playNextSong(){if(cloudRoom&&!isModerator()){toast("Host/co-host controls room music");return;}if(musicUris.isEmpty()){stopRoomMusic();return;}int n=currentSongIndex<0?0:(currentSongIndex+1)%musicUris.size();playSong(n);}
    private void playPreviousSong(){if(cloudRoom&&!isModerator()){toast("Host/co-host controls room music");return;}if(musicUris.isEmpty()){toast("Playlist is empty");return;}int n=currentSongIndex<0?0:(currentSongIndex-1+musicUris.size())%musicUris.size();playSong(n);}
    private void toggleRoomMusic(){
        if(cloudRoom&&!isModerator()){toast("Host/co-host controls room music");return;}
        if(roomMusicPlayer==null){if(musicUris.isEmpty()){toast("Add music first");return;}playSong(currentSongIndex>=0?currentSongIndex:0);return;}
        try{if(roomMusicPlayer.isPlaying()){int p=safePlayerPosition();roomMusicPlayer.pause();if(cloudRoom)publishMusicSync(sharedMusicUrl,currentSong,p,false);}else{int p=safePlayerPosition();roomMusicPlayer.start();if(cloudRoom)publishMusicSync(sharedMusicUrl,currentSong,p,true);}}catch(Exception e){toast("Music control failed");}
        updateMusicStatus();
    }
'''
s=s[:start]+playback+s[end:]

# Replace stop behavior so moderators broadcast stop; listeners stop locally without rebroadcasting.
old='    private void stopRoomMusic(){releaseMusicPlayer();currentSongIndex=-1;currentSong="";updateMusicStatus();if(cloudRoom&&isModerator())setRoomValue("currentSong","");}\n'
new='''    private void stopRoomMusic(){
        if(cloudRoom&&!isModerator()){releaseMusicPlayer();sharedMusicUrl="";currentSongIndex=-1;currentSong="";updateMusicStatus();return;}
        releaseMusicPlayer();currentSongIndex=-1;currentSong="";sharedMusicUrl="";updateMusicStatus();if(cloudRoom&&isModerator())publishMusicSync("","",0,false);
    }
'''
if old not in s: raise SystemExit('stopRoomMusic template not found')
s=s.replace(old,new,1)

# Attach the shared music listener to every cloud room.
anchor='        if(isModerator()){\n            seatRequestsListener=room.collection("seat_requests").addSnapshotListener((snap,e)->{'
if anchor not in s: raise SystemExit('attachCloudRoom anchor not found')
s=s.replace(anchor,'        attachMusicSync(room);\n'+anchor,1)

# Status label should distinguish synced playback for guests while retaining the same compact UI.
s=s.replace('String state=currentSong.isEmpty()?"":"🎵 "+currentSong+(roomMusicPlayer!=null&&roomMusicPlayer.isPlaying()?" • Playing":" • Paused");',
            'String state=currentSong.isEmpty()?"":"🎵 "+currentSong+(roomMusicPlayer!=null&&roomMusicPlayer.isPlaying()?" • Playing":" • Paused")+(cloudRoom&&!isModerator()?" • Room sync":"");',1)

# Open the shared player even when a guest has no local playlist.
s=s.replace('if(currentSongIndex<0&&musicUris.isEmpty()){musicPanel();return;}', 'if(currentSongIndex<0&&musicUris.isEmpty()&&currentSong.isEmpty()){musicPanel();return;}',1)

# Guests see the shared song title and cannot edit the host playlist from the player sheet.
s=s.replace('currentSongIndex>=0&&currentSongIndex<musicNames.size()?musicNames.get(currentSongIndex):"Room Music"', 'currentSongIndex>=0&&currentSongIndex<musicNames.size()?musicNames.get(currentSongIndex):(currentSong.isEmpty()?"Room Music":currentSong)',1)
s=s.replace('TextView list=pill("☷",0x00302a3a,this::musicPanel);', 'TextView list=pill("☷",0x00302a3a,cloudRoom&&!isModerator()?()->toast("Host/co-host controls the room playlist"):this::musicPanel);',1)

P.write_text(s,encoding='utf-8')
print('prepared music v4.8.0 shared room sync')
