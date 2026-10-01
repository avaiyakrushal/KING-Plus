from pathlib import Path
import re
P=Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
s=P.read_text(encoding='utf-8')

if 'import android.database.Cursor;' not in s:
    s=s.replace('import android.content.SharedPreferences;\n', 'import android.content.SharedPreferences;\nimport android.database.Cursor;\n',1)
if 'import android.provider.OpenableColumns;' not in s:
    s=s.replace('import android.os.Bundle;\n', 'import android.os.Bundle;\nimport android.provider.OpenableColumns;\n',1)
if 'import android.widget.SeekBar;' not in s:
    s=s.replace('import android.widget.ScrollView;\n', 'import android.widget.ScrollView;\nimport android.widget.SeekBar;\n',1)

field_anchor='    private MediaPlayer roomMusicPlayer;\n'
fields='''    private MediaPlayer roomMusicPlayer;\n    private final ArrayList<String> musicUris = new ArrayList<>();\n    private final ArrayList<String> musicNames = new ArrayList<>();\n    private int currentSongIndex = -1;\n    private float roomMusicVolume = 1.0f;\n'''
if 'private final ArrayList<String> musicUris' not in s:
    s=s.replace(field_anchor,fields,1)

s=s.replace('        prefs = getSharedPreferences("king_party", MODE_PRIVATE);\n', '        prefs = getSharedPreferences("king_party", MODE_PRIVATE);\n        loadMusicPlaylist();\n',1)
s=s.replace('musicStatusLabel.setOnClickListener(v->musicPanel());', 'musicStatusLabel.setOnClickListener(v->showRoomMusicPlayer());')

start=s.index('    private void musicPanel(){')
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
        TextView add=pill("ADD MUSIC",0xfff7f7f7,this::openMusicPicker);add.setTextColor(0xff46a991);add.setTextSize(16);LinearLayout.LayoutParams alp=new LinearLayout.LayoutParams(-1,dp(58));alp.setMargins(dp(42),dp(10),dp(42),0);root.addView(add,alp);

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
            LinearLayout text=new LinearLayout(this);text.setOrientation(LinearLayout.VERTICAL);TextView n=tv(name,17,Color.WHITE,true);n.setSingleLine(true);text.addView(n,new LinearLayout.LayoutParams(-1,dp(34)));TextView sub=tv("<unknown>",12,0x99ffffff,true);text.addView(sub,new LinearLayout.LayoutParams(-1,dp(26)));row.addView(text,new LinearLayout.LayoutParams(0,dp(68),1));
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
        if(index<0||index>=musicUris.size())return;releaseMusicPlayer();
        try{Uri uri=Uri.parse(musicUris.get(index));roomMusicPlayer=MediaPlayer.create(this,uri);if(roomMusicPlayer==null){toast("Could not open this audio file");return;}currentSongIndex=index;currentSong=musicNames.get(index);roomMusicPlayer.setLooping(false);roomMusicPlayer.setVolume(roomMusicVolume,roomMusicVolume);roomMusicPlayer.setOnCompletionListener(mp->playNextSong());roomMusicPlayer.start();updateMusicStatus();if(cloudRoom&&isModerator())setRoomValue("currentSong",currentSong);addEvent("music",safeName()+" played 🎵 "+currentSong);}catch(Exception e){releaseMusicPlayer();toast("Music failed: "+msg(e));}
    }
    private void releaseMusicPlayer(){try{if(roomMusicPlayer!=null){roomMusicPlayer.stop();roomMusicPlayer.release();}}catch(Exception ignored){}roomMusicPlayer=null;}
    private void playNextSong(){if(musicUris.isEmpty()){stopRoomMusic();return;}int n=currentSongIndex<0?0:(currentSongIndex+1)%musicUris.size();playSong(n);}
    private void playPreviousSong(){if(musicUris.isEmpty()){toast("Playlist is empty");return;}int n=currentSongIndex<0?0:(currentSongIndex-1+musicUris.size())%musicUris.size();playSong(n);}
    private void toggleRoomMusic(){if(roomMusicPlayer==null){if(musicUris.isEmpty()){toast("Add music first");return;}playSong(currentSongIndex>=0?currentSongIndex:0);return;}if(roomMusicPlayer.isPlaying())roomMusicPlayer.pause();else roomMusicPlayer.start();updateMusicStatus();}
    private void showRoomMusicPlayer(){
        if(currentSongIndex<0&&musicUris.isEmpty()){musicPanel();return;}
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(14),dp(18),dp(18));root.setBackgroundColor(0xee202734);
        TextView title=tv(currentSongIndex>=0&&currentSongIndex<musicNames.size()?musicNames.get(currentSongIndex):"Room Music",19,Color.WHITE,true);title.setSingleLine(true);root.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));
        LinearLayout vol=new LinearLayout(this);vol.setGravity(Gravity.CENTER_VERTICAL);TextView speaker=tv("🔊",24,Color.WHITE,true);vol.addView(speaker,new LinearLayout.LayoutParams(dp(48),dp(48)));SeekBar seek=new SeekBar(this);seek.setMax(100);seek.setProgress(Math.round(roomMusicVolume*100));vol.addView(seek,new LinearLayout.LayoutParams(0,dp(48),1));TextView pct=tv(Math.round(roomMusicVolume*100)+"%",13,Color.WHITE,true);pct.setGravity(Gravity.CENTER);vol.addView(pct,new LinearLayout.LayoutParams(dp(58),dp(48)));root.addView(vol,new LinearLayout.LayoutParams(-1,dp(56)));
        LinearLayout controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER);TextView prev=pill("|◀",0x00302a3a,this::playPreviousSong);controls.addView(prev,new LinearLayout.LayoutParams(0,dp(62),1));TextView play=pill(roomMusicPlayer!=null&&roomMusicPlayer.isPlaying()?"Ⅱ":"▶",0xfff7f7f7,null);play.setTextColor(0xff202734);play.setTextSize(26);controls.addView(play,new LinearLayout.LayoutParams(dp(74),dp(66)));TextView next=pill("▶|",0x00302a3a,this::playNextSong);controls.addView(next,new LinearLayout.LayoutParams(0,dp(62),1));TextView list=pill("☷",0x00302a3a,this::musicPanel);list.setTextSize(26);controls.addView(list,new LinearLayout.LayoutParams(0,dp(62),1));root.addView(controls,new LinearLayout.LayoutParams(-1,dp(78)));
        AlertDialog dialog=new AlertDialog.Builder(this).setView(root).create();
        play.setOnClickListener(v->{toggleRoomMusic();dialog.dismiss();showRoomMusicPlayer();});
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int p,boolean from){roomMusicVolume=p/100f;pct.setText(p+"%");if(roomMusicPlayer!=null)roomMusicPlayer.setVolume(roomMusicVolume,roomMusicVolume);}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}});
        dialog.setOnShowListener(v->{android.view.Window w=dialog.getWindow();if(w!=null){w.setLayout(-1,-2);w.setGravity(Gravity.BOTTOM);w.setBackgroundDrawable(bg(0xee202734,22));}});dialog.show();
    }
    private void stopRoomMusic(){releaseMusicPlayer();currentSongIndex=-1;currentSong="";updateMusicStatus();if(cloudRoom&&isModerator())setRoomValue("currentSong","");}
    private void updateMusicStatus(){
        if(musicStatusLabel==null)return;String state=currentSong.isEmpty()?"":"🎵 "+currentSong+(roomMusicPlayer!=null&&roomMusicPlayer.isPlaying()?" • Playing":" • Paused");
        runOnUiThread(()->{musicStatusLabel.setText(state);musicStatusLabel.setVisibility(state.isEmpty()?View.GONE:View.VISIBLE);});
    }
'''
s=s[:start]+new_music+s[end:]
P.write_text(s,encoding='utf-8')
print('prepared music v4.7.0')
