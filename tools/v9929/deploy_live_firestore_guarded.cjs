/* KING Plus production Firestore rules deployment: guarded, backed up and reversible.
   Never touches Auth, Storage, functions, payments or Firestore documents. */
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const {createRequire} = require('module');
const sourceRequire=createRequire(path.join(process.cwd(),'package.json'));
const {GoogleAuth} = sourceRequire('google-auth-library');
const {initializeApp,applicationDefault} = sourceRequire('firebase-admin/app');
const {getSecurityRules} = sourceRequire('firebase-admin/security-rules');

const project='king-plus-2f365';
const verifiedHash='29459a61a5d14461f808570d538a484cb79030af5f027429fbf27dafbdfe3ad0';
const backupDir='/tmp/king-prod-firestore-backup';
const target=process.env.KING_VERIFIED_RULES_PATH||'/tmp/king9929prod/firestore.rules';
const out=message=>console.log('KING_FIRESTORE_DEPLOY:',message);
const sha=s=>crypto.createHash('sha256').update(s).digest('hex');
function paths(text){return new Set([...text.matchAll(/\bmatch\s+(\/[^\s]+)\s*\{/g)].map(m=>m[1]));}
function requireCondition(cond,message){if(!cond)throw new Error(message);}
(async()=>{
  fs.mkdirSync(backupDir,{recursive:true});
  const expected=fs.readFileSync(target,'utf8');
  requireCondition(sha(expected)===verifiedHash,'Release rules fingerprint changed; refusing deployment');
  requireCondition(expected.includes('match /avatar_motion/{motionUid}'),'Verified 3D motion rule absent');
  const auth=new GoogleAuth({scopes:['https://www.googleapis.com/auth/cloud-platform']});
  const client=await auth.getClient();
  const api=async(url)=>(await client.request({url})).data;
  const releaseApi='https://firebaserules.googleapis.com/v1/projects/'+project+'/releases/cloud.firestore';
  const release=await api(releaseApi);
  requireCondition(String(release.name||'').endsWith('/releases/cloud.firestore'),'Unexpected production Firestore release ID');
  const oldSet=await api('https://firebaserules.googleapis.com/v1/'+release.rulesetName);
  const before=(oldSet.source?.files||[]).map(f=>f.content||'').join('\n');
  requireCondition(before.includes('service cloud.firestore'),'Production source could not be verified');
  fs.writeFileSync(path.join(backupDir,'live-before.rules'),before);
  fs.writeFileSync(path.join(backupDir,'live-before.sha256'),sha(before)+'\n');
  fs.writeFileSync(path.join(backupDir,'release-before.txt'),release.rulesetName+'\n');
  out('Production source backup captured; original deployed SHA256='+sha(before));
  if(before.trim()===expected.trim()){
    out('Already matching v9.9.29. No deployment needed.');
    return;
  }
  const oldPaths=paths(before),newPaths=paths(expected);
  const removed=[...oldPaths].filter(s=>!newPaths.has(s));
  fs.writeFileSync(path.join(backupDir,'preflight-path-summary.txt'),
    'oldPaths='+oldPaths.size+'\nnewPaths='+newPaths.size+
    '\nremovedPaths='+removed.length+'\nremoved='+removed.join(',')+'\n');
  out('Rule section comparison old='+oldPaths.size+' new='+newPaths.size+' removed='+removed.length);
  requireCondition(removed.length===0,
    'New rules remove existing production match paths; refusing deployment. Review backup first.');
  // Do not claim a full semantic diff. CI-emulated two-user tests are also mandatory.
  initializeApp({credential:applicationDefault(),projectId:project});
  const admin=getSecurityRules();
  let attempted=false;
  try{
    attempted=true;
    const response=await admin.releaseFirestoreRulesetFromSource(expected);
    out('New Firestore rules release accepted by Firebase: '+response.name);
    const active=await api(releaseApi);
    const activeSet=await api('https://firebaserules.googleapis.com/v1/'+active.rulesetName);
    const after=(activeSet.source?.files||[]).map(f=>f.content||'').join('\n');
    requireCondition(after.trim()===expected.trim(),
      'Post-deploy readback does not match exact approved v9.9.29 rules');
    fs.writeFileSync(path.join(backupDir,'release-after.txt'),active.rulesetName+'\n');
    fs.writeFileSync(path.join(backupDir,'live-after.sha256'),sha(after)+'\n');
    out('SUCCESS live Firestore rules match v9.9.29 SHA256='+sha(after));
  }catch(e){
    if(attempted){
      out('Deploy or verification failed; attempting to restore backup rules.');
      try{
        await admin.releaseFirestoreRulesetFromSource(before);
        const active=await api(releaseApi);
        const current=await api('https://firebaserules.googleapis.com/v1/'+active.rulesetName);
        const restored=(current.source?.files||[]).map(f=>f.content||'').join('\n');
        requireCondition(restored.trim()===before.trim(),'Rollback did not match original rules');
        out('Rollback to original rules SUCCESS');
      }catch(rollbackError){
        out('CRITICAL: rollback requires manual review; rollback error: '+(rollbackError.code||rollbackError.message));
      }
    }
    throw e;
  }
})().catch(e=>{console.error('KING_FIRESTORE_PROD_DEPLOY_FAILED:',e.code||e.message);process.exitCode=1});
