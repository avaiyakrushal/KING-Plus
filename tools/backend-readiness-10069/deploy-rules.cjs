// The user authorized finishing the existing app. Rules deployment only; no billing or data mutation.
const fs=require('node:fs'),crypto=require('node:crypto'),path=require('node:path');
const {initializeApp,cert}=require('firebase-admin/app');
const {getSecurityRules}=require('firebase-admin/security-rules');
const PROJECT='king-plus-2f365';
const EXPECTED_BEFORE='29459a61a5d14461f808570d538a484cb79030af5f027429fbf27dafbdfe3ad0';
const sha=s=>crypto.createHash('sha256').update(s).digest('hex');
(async()=>{
 const credentials=JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_JSON||'{}');
 if(credentials.project_id!==PROJECT)throw Error('Credential project mismatch');
 const validation=JSON.parse(fs.readFileSync('/tmp/king-production-validation/rules-validation.json','utf8'));
 if(!validation.total||validation.passed!==validation.total)throw Error('Production rules tests must all pass');
 initializeApp({credential:cert(credentials),projectId:PROJECT});
 const service=getSecurityRules(),before=await service.getFirestoreRuleset();
 if(before.source.length!==1)throw Error('Unexpected production source shape');
 const source=fs.readFileSync('firestore.rules','utf8'),hash=sha(source);
 const out='/tmp/king-production-validation';
 fs.writeFileSync(path.join(out,'rules-before.json'),JSON.stringify({name:before.name,source:before.source},null,2));
 if(sha(before.source[0].content)!==EXPECTED_BEFORE&&sha(before.source[0].content)!==hash)
  throw Error('Production rules changed since the read-only audit; refusing overwrite');
 if(sha(before.source[0].content)!==hash)await service.releaseFirestoreRulesetFromSource(source);
 const after=await service.getFirestoreRuleset();
 if(after.source.length!==1||sha(after.source[0].content)!==hash)throw Error('Deployed rules do not match validated source');
 const report={project:PROJECT,deployed:true,verifiedAt:new Date().toISOString(),beforeRuleset:before.name,afterRuleset:after.name,beforeSha256:sha(before.source[0].content),afterSha256:hash,testsPassed:validation.passed,billingChanged:false,userDataWritten:false};
 fs.writeFileSync(path.join(out,'deployment.json'),JSON.stringify(report,null,2));console.log(JSON.stringify(report));
})().catch(e=>{console.error('Rules deployment failed:',e.code||e.name,e.message);process.exitCode=1;});
