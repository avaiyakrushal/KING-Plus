const fs=require('node:fs'),path=require('node:path'),cp=require('node:child_process');
const output='/tmp/king-production-validation';fs.mkdirSync(output,{recursive:true});
const priority=['ludo-rules.test.mjs','party-ludo-firestore.test.mjs','entrance-firestore.test.mjs','games-gifts-firestore.test.mjs'];
const all=fs.readdirSync('tests/firestore').filter(f=>f.endsWith('.test.mjs')).sort();
const files=[...priority,...all.filter(f=>!priority.includes(f))];
const results=[];
for(const file of files){
 const start=Date.now();const r=cp.spawnSync(process.execPath,[path.join('tests/firestore',file)],{encoding:'utf8',timeout:90000,maxBuffer:32*1024*1024});
 const log=(r.stdout||'')+(r.stderr||'');fs.writeFileSync(path.join(output,file+'.log'),log);
 const result={test:file,passed:r.status===0,millis:Date.now()-start,status:r.status,error:r.error?.code};results.push(result);
 console.log(JSON.stringify(result));if(r.status!==0)console.log(log.slice(-4000));
 if(r.status!==0&&priority.includes(file))break;
}
fs.writeFileSync(path.join(output,'rules-validation.json'),JSON.stringify({total:files.length,passed:results.filter(r=>r.passed).length,results},null,2));
process.exitCode=results.length===files.length&&results.every(r=>r.passed)?0:1;
