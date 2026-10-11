#!/usr/bin/env node
/* Read-only metadata check. Never print secrets or backend document contents. */
const {google}=require("googleapis");
const project="king-plus-2f365";
function status(e){
    const code=Number(e?.response?.status||e?.code||0);
    if(code===404)return "DEFAULT_DATABASE_NOT_FOUND";
    if(code===403)return "DATABASE_METADATA_IAM_DENIED";
    if(code===401)return "DATABASE_METADATA_AUTH_FAILED";
    if(code===429)return "DATABASE_METADATA_RATE_LIMIT";
    if(code===503 || code===504)return "DATABASE_METADATA_SERVER_UNAVAILABLE";
    return "DATABASE_METADATA_UNKNOWN_"+(Number.isSafeInteger(code)?code:"ERROR");
}
(async()=>{
    const hard=setTimeout(()=>{console.error("RESULT: METADATA_TIMEOUT");process.exit(3)},28000);
    const auth=new google.auth.GoogleAuth({scopes:["https://www.googleapis.com/auth/cloud-platform"]});
    const api=google.firestore({version:"v1",auth});
    try {
        const result=await api.projects.databases.get({
            name:"projects/"+project+"/databases/(default)"
        });
        if(result?.data?.name==="projects/"+project+"/databases/(default)"){
            console.log("RESULT: DEFAULT_DATABASE_EXISTS");
            console.log("NEXT: Compare with mobile client and Firestore data API access.");
        }else{
            console.log("RESULT: METADATA_RESPONSE_UNEXPECTED");
            process.exitCode=2;
        }
    } catch(e) {
        console.log("RESULT: "+status(e));
        console.log("NOTE: This audit made no database modifications.");
        process.exitCode=1;
    }finally{clearTimeout(hard)}
})().catch(()=>{console.log("RESULT: METADATA_SETUP_FAILED");process.exitCode=1});
