#!/usr/bin/env node
/* Owner-authorized once-only Firestore creation.
 * Guards: first database ONLY; project king-plus-2f365;
 * (default); Mumbai asia-south1; Standard Native.
 * NO billing enablement, existing DB mutation or credential logging.
 */
const {google}=require('googleapis');
const project='king-plus-2f365';
const dbId='(default)';
const expected='projects/'+project+'/databases/'+dbId;
const region='asia-south1';
function fail(code,e){
  const status=String(e?.response?.status||e?.code||'').replace(/[^0-9]/g,'');
  console.error('RESULT: '+code+(status?' HTTP_'+status:''));
  process.exitCode=1;
}
const wait=ms=>new Promise(r=>setTimeout(r,ms));
(async()=>{
  const auth=new google.auth.GoogleAuth({scopes:['https://www.googleapis.com/auth/cloud-platform']});
  const api=google.firestore({version:'v1',auth});
  let listing;
  try{
    listing=await api.projects.databases.list({parent:'projects/'+project});
  }catch(e){
    fail('BLOCKED_DATABASE_LIST_PERMISSION_OR_API',e);
    return;
  }
  const databases=listing?.data?.databases||[];
  console.log('CHECK: database count='+databases.length);
  if(databases.some(db=>db.name===expected)){
    const already=databases.find(db=>db.name===expected);
    console.log('RESULT: DEFAULT_ALREADY_PRESENT');
    console.log('LOCATION: '+String(already.locationId||'unknown'));
    console.log('EDITION: '+String(already.databaseEdition||'unknown'));
    console.log('No new database was created.');
    return;
  }
  // Existing named database could be the free one: do not create a second
  // database (potentially chargeable).
  if(databases.length!==0){
    console.log('RESULT: BLOCKED_OTHER_DATABASE_EXISTS');
    console.log('No additional database was created to avoid billing.');
    process.exitCode=2;
    return;
  }
  console.log('CHECK: no existing databases. Request FIRST Standard Native default database in Mumbai.');
  try{
    const response=await api.projects.databases.create({
      parent:'projects/'+project,
      databaseId:dbId,
      requestBody:{
        locationId:region,
        type:'FIRESTORE_NATIVE',
        databaseEdition:'STANDARD',
        deleteProtectionState:'DELETE_PROTECTION_ENABLED'
      }
    });
    if(!response?.data?.name){
      console.log('RESULT: CREATE_RESPONSE_MISSING_OPERATION');
      process.exitCode=1;
      return;
    }
    console.log('RESULT: CREATE_REQUEST_ACCEPTED');
  }catch(e){
    const status=Number(e?.response?.status||e?.code||0);
    if(status===403)fail('BLOCKED_DATABASE_CREATE_IAM_PERMISSION',e);
    else if(status===400)fail('BLOCKED_DATABASE_CREATE_CONFIGURATION',e);
    else if(status===409)fail('BLOCKED_DATABASE_CREATE_CONFLICT_RECHECK',e);
    else fail('BLOCKED_DATABASE_CREATE_API',e);
    return;
  }
  for(let n=0;n<18;n++){
    let db;
    try{
      const r=await api.projects.databases.get({name:expected});
      db=r.data;
    }catch(e){
      if(n===17){fail('CREATE_PENDING_METADATA_READ_FAILED',e);return;}
    }
    if(db?.name===expected){
      console.log('CHECK: database region='+String(db.locationId));
      console.log('CHECK: database edition='+String(db.databaseEdition));
      console.log('CHECK: database type='+String(db.type));
      console.log('CHECK: delete protection='+String(db.deleteProtectionState));
      if(db.locationId!==region||db.type!=='FIRESTORE_NATIVE'||
        (db.databaseEdition&&db.databaseEdition!=='STANDARD')){
        console.log('RESULT: CREATE_CONFIG_MISMATCH_CONTACT_OWNER');
        process.exitCode=2;
        return;
      }
      if(db.freeTier===true) console.log('CHECK: first database freeTier true');
      console.log('RESULT: DEFAULT_DATABASE_EXISTS_MUMBAI');
      console.log('NEXT: verify server reads and secure Firestore rules');
      return;
    }
    await wait(10000);
  }
  console.log('RESULT: CREATE_PENDING_RECHECK');
  process.exitCode=2;
})().catch(e=>fail('UNEXPECTED_CREATE_CHECK_FAILURE',e));
