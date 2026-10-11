#!/usr/bin/env node
/* KING Plus Firestore server-side READ-ONLY health check.
 * Do not print credentials, user data, any room ID, or document contents.
 * This process must not write, create, update, delete or deploy anything.
 */
const {initializeApp, applicationDefault} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");

const EXPECTED_PROJECT="king-plus-2f365";
function classify(error){
    const msg=String(error?.message||"").toLowerCase();
    const code=String(error?.code??"UNKNOWN").toUpperCase();
    if(msg.includes("database")&&(msg.includes("does not exist")||msg.includes("not found")))
        return "DATABASE_MISSING";
    if(msg.includes("serviceusage")||msg.includes("api has not been used")||
        msg.includes("api is disabled"))
        return "API_DISABLED";
    if(code==="7"||code==="PERMISSION_DENIED"||msg.includes("permission_denied"))
        return "SERVICE_ACCOUNT_IAM_DENIED";
    if(code==="16"||code==="UNAUTHENTICATED")return "SERVER_CREDENTIAL_REJECTED";
    if(code==="5"||code==="NOT_FOUND")return "RESOURCE_NOT_FOUND";
    if(code==="14"||code==="UNAVAILABLE")return "FIRESTORE_UNAVAILABLE";
    if(code==="4"||code==="DEADLINE_EXCEEDED")return "FIRESTORE_TIMEOUT";
    if(code==="8"||code==="RESOURCE_EXHAUSTED")return "QUOTA_OR_RATE_LIMIT";
    return "SERVER_READ_ERROR_"+code.replace(/[^A-Z0-9_]/g,"").slice(0,30);
}

(async()=>{
    const hardTimeout=setTimeout(()=>{
        process.stderr.write("RESULT: FIRESTORE_SERVER_READ_TIMEOUT\n");
        process.exit(2);
    },30000);
    const project=String(process.env.GOOGLE_CLOUD_PROJECT||"");
    if(project!==EXPECTED_PROJECT){
        process.stderr.write("RESULT: WRONG_PROJECT_CONFIGURATION\n");
        process.exit(3);
    }
    initializeApp({credential:applicationDefault(),projectId:EXPECTED_PROJECT});
    const db=getFirestore();
    try{
        // An Admin SDK read doesn't prove mobile network or client Rules work.
        // It does prove Firestore data API reachability from GitHub CI.
        await db.collection("public_profiles").limit(1).get();
        process.stdout.write("RESULT: ADMIN_SERVER_READ_OK\n");
        process.stdout.write("NEXT: Compare Android SERVER profile read on same project and Room read; audit client rules/network if phone fails.\n");
    }catch(error){
        process.stderr.write("RESULT: "+classify(error)+"\n");
        process.stderr.write("NEXT: Inspect Firebase console database/API or IAM; no automatic Rules changes were made.\n");
        process.exitCode=1;
    }finally{
        clearTimeout(hardTimeout);
        await db.terminate().catch(()=>{});
    }
})().catch(err=>{
    process.stderr.write("RESULT: ADMIN_CHECK_SETUP_ERROR\n");
    process.exitCode=1;
});
