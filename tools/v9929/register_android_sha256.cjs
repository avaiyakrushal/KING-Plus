const fs=require('fs');
const {GoogleAuth}=require('google-auth-library');
const normalize=s=>(s||'').toLowerCase().replace(/:/g,'');
(async()=>{
 const client=await new GoogleAuth({scopes:['https://www.googleapis.com/auth/cloud-platform']}).getClient();
 const base='https://firebase.googleapis.com/v1beta1/';
 const get=async u=>(await client.request({url:u})).data;
 const apps=(await get(base+'projects/king-plus-2f365/androidApps')).apps||[];
 const app=apps.filter(a=>a.packageName==='com.kingplus.social'&&a.state!=='DELETED');
 if(app.length!==1)throw Error('KING Firebase Android app identity ambiguous');
 const url=base+app[0].name+'/sha';
 const read=async()=>(await get(url)).certificates||[];
 const sha1=fs.readFileSync('/tmp/king-sha/sha1','utf8').trim();
 const sha256=fs.readFileSync('/tmp/king-sha/sha256','utf8').trim();
 const existing=await read();
 if(!existing.some(x=>x.certType==='SHA_1'&&normalize(x.shaHash)===sha1))
    throw Error('APK SHA1 not registered: refusing to change wrong Android app');
 if(!existing.some(x=>x.certType==='SHA_256'&&normalize(x.shaHash)===sha256)){
    await client.request({url,method:'POST',data:{name:'',shaHash:sha256,certType:'SHA_256'}});
    console.log('KING v9.9.29 APK SHA256 registered.');
 }
 if(!(await read()).some(x=>x.certType==='SHA_256'&&normalize(x.shaHash)===sha256))
    throw Error('SHA256 post-registration verification failed');
 console.log('PASS signed v9.9.29 APK SHA1 and SHA256 both registered.');
})().catch(e=>{console.error('SHA_CERT_CHECK:',e.response?.status||e.message);process.exitCode=1});
