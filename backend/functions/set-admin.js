'use strict';
// Owner-run maintenance command. Use Application Default Credentials, never ship keys.
const {initializeApp}=require('firebase-admin/app');
const {getAuth}=require('firebase-admin/auth');
const uid=process.argv[2],action=process.argv[3];
if(!uid||!['grant','revoke'].includes(action)){console.error('Usage: node set-admin.js FIREBASE_UID grant|revoke');process.exit(1);}
initializeApp();
(async()=>{const auth=getAuth(),u=await auth.getUser(uid),claims={...(u.customClaims||{})};if(action==='grant')claims.admin=true;else delete claims.admin;await auth.setCustomUserClaims(uid,claims);await auth.revokeRefreshTokens(uid);console.log('Admin claim updated. Sign in again to refresh access.');})().catch(()=>{console.error('Admin update failed. Check credentials, project and UID.');process.exitCode=1;});
