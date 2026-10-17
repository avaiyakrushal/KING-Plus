// Read-only production inventory. Never enables services, billing or subscriptions.
const fs=require('node:fs');
const crypto=require('node:crypto');
const path=require('node:path');
const {initializeApp,cert}=require('firebase-admin/app');
const {getSecurityRules}=require('firebase-admin/security-rules');
const {GoogleAuth}=require('google-auth-library');
const PROJECT='king-plus-2f365';
const output=process.env.KING_AUDIT_OUTPUT||'/tmp/king-backend-audit';
const report={project:PROJECT,checkedAt:new Date().toISOString(),readOnly:true,checks:{}};
function failure(e){return {status:'blocked',code:String(e.code||e.status||'unknown').slice(0,80),httpStatus:e.response?.status||null,reason:String(e.response?.data?.error?.message||e.message||'').replace(/[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}/gi,'[account]').slice(0,300)};}
async function check(key,task){try{report.checks[key]={status:'read',...await task()};}catch(e){report.checks[key]=failure(e);}console.log(key,JSON.stringify(report.checks[key]));}
(async()=>{
 fs.mkdirSync(output,{recursive:true});
 if(!process.env.FIREBASE_SERVICE_ACCOUNT_JSON){report.checks.credentials={status:'missing'};fs.writeFileSync(path.join(output,'readiness.json'),JSON.stringify(report,null,2));return;}
 const credentials=JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_JSON);
 if(credentials.project_id!==PROJECT)throw Error('Credential project mismatch');
 initializeApp({credential:cert(credentials),projectId:PROJECT});
 const auth=new GoogleAuth({credentials,scopes:['https://www.googleapis.com/auth/cloud-platform']});
 const client=await auth.getClient();
 async function get(url){return (await client.request({url,method:'GET',timeout:20000})).data;}
 await check('firestoreRules',async()=>{
  const rules=await getSecurityRules().getFirestoreRuleset();
  fs.writeFileSync(path.join(output,'production-firestore-rules.json'),JSON.stringify({name:rules.name,source:rules.source},null,2));
  return {ruleset:rules.name,files:rules.source.map(f=>({name:f.name,sha256:crypto.createHash('sha256').update(f.content).digest('hex')}))};
 });
 await check('storageRules',async()=>{
  const rules=await getSecurityRules().getStorageRuleset('king-plus-2f365.firebasestorage.app');
  fs.writeFileSync(path.join(output,'production-storage-rules.json'),JSON.stringify({name:rules.name,source:rules.source},null,2));
  return {ruleset:rules.name,files:rules.source.map(f=>({name:f.name,sha256:crypto.createHash('sha256').update(f.content).digest('hex')}))};
 });
 for(const group of ['wallet_ledger','cashfree_recharge_orders'])await check('indexes:'+group,async()=>{
  const r=await get(`https://firestore.googleapis.com/v1/projects/${PROJECT}/databases/(default)/collectionGroups/${group}/indexes?pageSize=100`);
  return {indexes:(r.indexes||[]).map(i=>({collectionGroup:group,state:i.state,queryScope:i.queryScope,fields:i.fields})),hasMore:!!r.nextPageToken};
 });
 await check('storageBucket',async()=>{
  const r=await get('https://storage.googleapis.com/storage/v1/b/king-plus-2f365.firebasestorage.app?fields=name,location');
  return {exists:!!r.name,location:r.location};
 });
 await check('functions',async()=>{
  const result=await get(`https://cloudfunctions.googleapis.com/v2/projects/${PROJECT}/locations/-/functions`);
  return {functions:(result.functions||[]).map(f=>({name:f.name.split('/').at(-1),region:f.name.split('/')[3],state:f.state,environment:f.environment})),unreachable:result.unreachable||[]};
 });
 await check('billing',async()=>{
  const result=await get(`https://cloudbilling.googleapis.com/v1/projects/${PROJECT}/billingInfo`);
  return {enabled:result.billingEnabled===true};
 });
 for(const service of ['cloudfunctions.googleapis.com','cloudbuild.googleapis.com','artifactregistry.googleapis.com','firebaserules.googleapis.com']){
  await check('service:'+service,async()=>{const r=await get(`https://serviceusage.googleapis.com/v1/projects/${PROJECT}/services/${service}`);return {state:r.state};});
 }
 await check('voiceCredentialPresence',async()=>{
  const result=await get(`https://secretmanager.googleapis.com/v1/projects/${PROJECT}/secrets?pageSize=100`);
  const names=(result.secrets||[]).map(s=>s.name.split('/').at(-1));
  return {appId:names.includes('CLOUDFLARE_SFU_APP_ID'),appSecret:names.includes('CLOUDFLARE_SFU_APP_SECRET'),hasMore:!!result.nextPageToken};
 });
 await check('authProviders',async()=>{
  const r=await get(`https://identitytoolkit.googleapis.com/admin/v2/projects/${PROJECT}/config`);
  return {anonymous:r.signIn?.anonymous?.enabled===true,phone:r.signIn?.phoneNumber?.enabled===true,email:r.signIn?.email?.enabled===true};
 });
 fs.writeFileSync(path.join(output,'readiness.json'),JSON.stringify(report,null,2));
})().catch(e=>{console.error('Read-only audit failed',String(e.code||e.name||'unknown'));process.exitCode=1;});
