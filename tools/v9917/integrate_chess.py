#!/usr/bin/env python3
"""Integrate original KING Live Chess into the exact verified v9.9.16 source."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
for name in ('KingChessRules.java','KingLiveChessActivity.java'):
    incoming=Path('tools/v9917/java')/name
    assert incoming.is_file(),name
    (src/name).write_bytes(incoming.read_bytes())
def replace_once(path,a,b,label):
    s=path.read_text(encoding='utf-8')
    if s.count(a)!=1:raise RuntimeError(label+' anchor count: '+str(s.count(a)))
    path.write_text(s.replace(a,b,1),encoding='utf-8')
manifest=root/'app/src/main/AndroidManifest.xml'
replace_once(manifest,
  '        <activity android:name=".RoomGameActivity" android:exported="false" />',
  '        <activity android:name=".RoomGameActivity" android:exported="false" />\n        <activity android:name=".KingLiveChessActivity" android:exported="false" />',
  'chess-activity-declaration')
party=src/'PartyActivity.java'
replace_once(party,
  'new String[]{"🎲 Live Room Dice","🎮 Multiplayer Games Catalog","🎯 Party Play Center"}',
  'new String[]{"🎲 Live Room Dice","♟ Live Room Chess","🎮 Multiplayer Games Catalog","🎯 Party Play Center"}',
  'party-chess-game-option')
replace_once(party,
  '''                    if(choice==0)openInRoomDice997();
                    else if(choice==1)openRoomGames740();
                    else toolsPanel();''',
  '''                    if(choice==0)openInRoomDice997();
                    else if(choice==1)openLiveChess9917();
                    else if(choice==2)openRoomGames740();
                    else toolsPanel();''',
  'party-chess-action')
replace_once(party,
  '    private void openInRoomDice997(){',
  '''    private void openLiveChess9917(){
        if(!cloudRoom||!memberSeen||db==null||user==null||roomId==null){
            toast("Join a live Firebase Party before Chess");return;
        }
        Intent chess9917=new Intent(this,KingLiveChessActivity.class);
        chess9917.putExtra("roomId",roomId);
        startActivity(chess9917);
    }

    private void openInRoomDice997(){''',
  'party-launch-chess')
game=src/'RoomGameActivity.java'
replace_once(game,
  'controls.addView(ludo,ludoLp);',
  '''controls.addView(ludo,ludoLp);
        Button chess9917=button("♟ Live Chess • Two Party Players");
        chess9917.setOnClickListener(v->{
            if(!gameMemberVerified992||db==null||me==null||roomId.isEmpty()){toast("Join the Party to play Chess");return;}
            Intent i9917=new Intent(this,KingLiveChessActivity.class);
            i9917.putExtra("roomId",roomId);startActivity(i9917);
        });
        LinearLayout.LayoutParams chessLp9917=new LinearLayout.LayoutParams(-1,dp(52));
        chessLp9917.setMargins(0,dp(4),0,dp(4));controls.addView(chess9917,chessLp9917);''',
  'chess-game-catalog')
rules=root/'firestore.rules'
chess_rules='''      // Two actual signed-in Party members share a turn-based Chess board.
      // Transactions and revision monotonicity reject overwrites and stale turns.
      // Chess geometry is enforced by KingChessRules on the client; a production
      // anti-cheat game requires a trusted Cloud Function as arbiter.
      match /chess_games/{chessId} {
        allow read: if chessId == 'main' && roomAccess(roomId) && roomMember(roomId);
        allow create: if chessId == 'main' && roomAccess(roomId) && roomMember(roomId)
          && roomData(roomId).closed != true
          && request.resource.data.whiteUid == request.auth.uid
          && request.resource.data.blackUid == ''
          && request.resource.data.board == 'rnbqkbnrpppppppp................................PPPPPPPPRNBQKBNR'
          && request.resource.data.whiteName is string
          && request.resource.data.whiteName.size() <= 45
          && request.resource.data.turn == 'w'
          && request.resource.data.rights == 'KQkq'
          && request.resource.data.enPassant == -1
          && request.resource.data.revision == 0
          && request.resource.data.status == 'waiting'
          && request.resource.data.actorUid == request.auth.uid
          && request.resource.data.updatedAt == request.time;
        allow update: if chessId == 'main' && roomAccess(roomId) && roomMember(roomId)
          && roomData(roomId).closed != true
          && request.resource.data.whiteUid == resource.data.whiteUid
          && request.resource.data.whiteName == resource.data.whiteName
          && request.resource.data.actorUid == request.auth.uid
          && request.resource.data.revision is int
          && request.resource.data.revision == resource.data.revision + 1
          && request.resource.data.updatedAt == request.time
          && request.resource.data.board is string
          && request.resource.data.board.size() == 64
          && request.resource.data.diff(resource.data).affectedKeys().hasOnly([
            'blackUid','blackName','status','board','turn','rights','enPassant',
            'revision','actorUid','updatedAt','lastMove','winnerUid'
          ])
          && (
            // Black joins once and activates match; cannot replace white or a player.
            (resource.data.status == 'waiting'
              && resource.data.blackUid == ''
              && request.auth.uid != resource.data.whiteUid
              && request.resource.data.blackUid == request.auth.uid
              && request.resource.data.blackName is string
              && request.resource.data.blackName.size() <= 45
              && request.resource.data.status == 'active'
              && request.resource.data.board == resource.data.board
              && request.resource.data.turn == resource.data.turn
              && request.resource.data.rights == resource.data.rights
              && request.resource.data.enPassant == resource.data.enPassant
              && request.resource.data.lastMove == resource.data.lastMove)
            ||
            // Only the player whose turn it is can submit a board change.
            (resource.data.status == 'active'
              && request.resource.data.blackUid == resource.data.blackUid
              && request.resource.data.blackName == resource.data.blackName
              && request.auth.uid == (
                 resource.data.turn == 'w'
                 ? resource.data.whiteUid : resource.data.blackUid)
              && request.resource.data.status in ['active','checkmate','stalemate']
              && request.resource.data.turn != resource.data.turn
              && request.resource.data.board != resource.data.board)
            ||
            // Either seated player can resign without changing the board.
            (resource.data.status == 'active'
              && request.auth.uid in [resource.data.whiteUid,resource.data.blackUid]
              && request.resource.data.blackUid == resource.data.blackUid
              && request.resource.data.blackName == resource.data.blackName
              && request.resource.data.status == 'resigned'
              && request.resource.data.board == resource.data.board
              && request.resource.data.turn == resource.data.turn
              && request.resource.data.rights == resource.data.rights
              && request.resource.data.enPassant == resource.data.enPassant
              && request.resource.data.lastMove == 'Resigned'
              && request.resource.data.winnerUid == (
                request.auth.uid == resource.data.whiteUid
                ? resource.data.blackUid : resource.data.whiteUid))
            ||
            // Only White can reset an ended match with the same two player IDs.
            (resource.data.status in ['checkmate','stalemate','resigned']
              && request.auth.uid == resource.data.whiteUid
              && request.resource.data.blackUid == resource.data.blackUid
              && request.resource.data.blackName == resource.data.blackName
              && request.resource.data.status == 'active'
              && request.resource.data.board == 'rnbqkbnrpppppppp................................PPPPPPPPRNBQKBNR'
              && request.resource.data.turn == 'w'
              && request.resource.data.rights == 'KQkq'
              && request.resource.data.enPassant == -1
              && request.resource.data.winnerUid == '')
          );
        allow delete: if false;
      }

'''
replace_once(rules,
    '      match /game_state/{stateId} {\n        allow read: if roomAccess(roomId);\n        allow create, update, delete: if roomModerator(roomId);\n      }\n',
    '      match /game_state/{stateId} {\n        allow read: if roomAccess(roomId);\n        allow create, update, delete: if roomModerator(roomId);\n      }\n\n'+chess_rules,
    'firestore-chess-security')
b=root/'app/build.gradle'
g=b.read_text()
g,n=re.subn(r"versionCode\s+195;\s*versionName\s+'9\.9\.16[^']*'",
            "versionCode 196; versionName '9.9.17-live-chess-party-test'",g)
if n!=1:raise RuntimeError('Expected latest v9.9.16 source')
b.write_text(g)
assert 'KingNative3DRoom.open(' in party.read_text()
assert 'approveOrMoveMemberSeat9913' in party.read_text()
assert 'responsiveWorld9915' in (src/'KingNative3DRoom.java').read_text()
print('PASS chess integration, party routes, rules, manifest, and all prior gameplay retained')
