from pathlib import Path

p = Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
s = p.read_text()

old_open = '''    private void openRealLogin(){
        Intent back=new Intent(this,MainActivity.class);back.putExtra("forceLogin",true);startActivity(back);finish();
    }
'''
new_open = '''    private void openRealLogin(){
        try {
            Intent back=new Intent(this,MainActivity.class);
            back.putExtra("forceLogin",true);
            startActivity(back);
        } catch (Exception e) {
            toast("Could not open sign in: " + msg(e));
        }
    }
    private void requireSignInForCreate(){
        new AlertDialog.Builder(this)
            .setTitle("Sign in required")
            .setMessage("Sign in first to create a real KING Plus Party room. Your Party screen will stay open so you can return after login.")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Sign in",(d,w)->openRealLogin())
            .show();
    }
'''
if old_open not in s:
    raise SystemExit('openRealLogin block not found')
s = s.replace(old_open, new_open, 1)

old_actions = '''        new AlertDialog.Builder(this).setItems(new String[]{"＋ Create Party","↻ Refresh live rooms"},(d,w)->{
            if(w==0){if(user!=null&&db!=null)createRoomDialog();else openRealLogin();}
            else renderLobby(selected);
        }).show();
'''
new_actions = '''        new AlertDialog.Builder(this).setItems(new String[]{"＋ Create Party","↻ Refresh live rooms"},(d,w)->{
            if(w==0){if(user!=null&&db!=null)createRoomDialog();else requireSignInForCreate();}
            else renderLobby(selected);
        }).show();
'''
if old_actions not in s:
    raise SystemExit('showPartyActions block not found')
s = s.replace(old_actions, new_actions, 1)

old_dialog = '''    private void createRoomDialog() {
        final EditText e = new EditText(this); e.setHint("Party room name"); e.setSingleLine(true);
'''
new_dialog = '''    private void createRoomDialog() {
        if(user==null||db==null){requireSignInForCreate();return;}
        final EditText e = new EditText(this); e.setHint("Party room name"); e.setSingleLine(true);
'''
if old_dialog not in s:
    raise SystemExit('createRoomDialog block not found')
s = s.replace(old_dialog, new_dialog, 1)

p.write_text(s)
print('Applied v4.1.1 Create Party/login safety fix')
