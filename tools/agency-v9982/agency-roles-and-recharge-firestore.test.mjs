import {readFileSync} from 'node:fs';
import {initializeTestEnvironment, assertSucceeds, assertFails} from '@firebase/rules-unit-testing';
import {doc,collection,setDoc,updateDoc,getDoc,getDocsFromServer,
        query,where,serverTimestamp} from 'firebase/firestore';
const env=await initializeTestEnvironment({
 projectId:'demo-king-agency-9982',
 firestore:{host:'127.0.0.1',port:8080,rules:readFileSync('firestore.rules','utf8')}
});
const Admin=env.authenticatedContext('superadmin',{admin:true}).firestore(),
 Agency=env.authenticatedContext('agencyowner').firestore(),
 Agent=env.authenticatedContext('agent01').firestore(),
 OtherAgency=env.authenticatedContext('secondagency').firestore(),
 Rogue=env.authenticatedContext('rogueuser').firestore(),
 Customer=env.authenticatedContext('customer01').firestore();
const agency=(db,uid)=>doc(db,'king_agencies',uid);
const agent=(db,uid)=>doc(db,'king_agents',uid);
const order=(db,id)=>doc(db,'king_agent_recharge_requests',id);
const agencyData=(uid,name)=>({uid,name,active:true,approvedByUid:'superadmin',
   createdAt:serverTimestamp(),updatedAt:serverTimestamp()});
const agentData=(uid,agencyUid)=>({uid,agencyUid,active:true,
   createdAt:serverTimestamp(),updatedAt:serverTimestamp()});
const orderData=(agentUid,agencyUid,targetUid,diamonds,status='pending')=>({
 agentUid,agencyUid,targetUid,diamonds,status,createdAt:serverTimestamp()
});
try{
 await env.clearFirestore();
 // A normal user cannot grant themselves or friends Agency.
 await assertFails(setDoc(agency(Rogue,'rogueuser'),agencyData('rogueuser','Fake Agency')));
 await assertSucceeds(setDoc(agency(Admin,'agencyowner'),agencyData('agencyowner','Royal Agency')));
 await assertSucceeds(getDoc(agency(Agency,'agencyowner')));
 await assertFails(getDoc(agency(Rogue,'agencyowner')));
 await assertFails(setDoc(agent(Rogue,'rogueuser'),agentData('rogueuser','agencyowner')));
 await assertSucceeds(setDoc(agency(Admin,'secondagency'),agencyData('secondagency','Another Agency')));
 await assertSucceeds(setDoc(agent(Agency,'agent01'),agentData('agent01','agencyowner')));
 await assertSucceeds(getDoc(agent(Agent,'agent01')));
 // Agent cannot award himself agency or take over somebody else's agent.
 await assertFails(setDoc(agent(Agent,'otheragent'),agentData('otheragent','agencyowner')));
 await assertFails(updateDoc(agent(OtherAgency,'agent01'),{agencyUid:'secondagency',updatedAt:serverTimestamp()}));
 await assertFails(updateDoc(agent(Agent,'agent01'),{active:true,updatedAt:serverTimestamp()}));
 // Agent's real transaction is only a PENDING REQUEST, never wallet credit.
 await assertSucceeds(setDoc(order(Agent,'req-agent-valid'),orderData(
    'agent01','agencyowner','customer01',600)));
 const customerRequest=await assertSucceeds(getDoc(order(Agent,'req-agent-valid')));
 if(customerRequest.data().status!=='pending'||customerRequest.data().diamonds!==600)
    throw Error('Correct pending Diamond request missing');
 await assertFails(setDoc(order(Rogue,'req-rogue'),orderData(
    'rogueuser','agencyowner','customer01',1300)));
 await assertFails(setDoc(order(Agent,'req-impersonate'),orderData(
    'agencyowner','agencyowner','customer01',600)));
 await assertFails(setDoc(order(Agent,'req-fakepack'),orderData(
    'agent01','agencyowner','customer01',999999)));
 await assertFails(setDoc(order(Agent,'req-no-payment'),orderData(
    'agent01','agencyowner','customer01',600,'credited')));
 await assertFails(updateDoc(order(Agent,'req-agent-valid'),{status:'credited'}));
 await assertFails(updateDoc(order(Admin,'req-agent-valid'),{status:'approved'}));
 await assertFails(setDoc(doc(Agent,'wallets','customer01'),{coins:99999}));
 await assertFails(setDoc(doc(Admin,'wallets','customer01'),{coins:99999}));
 await assertFails(setDoc(doc(Agency,'wallet_ledger','fake-paid-credit'),{targetUid:'customer01',diamonds:600}));
 await assertSucceeds(setDoc(order(Agency,'req-owner'),orderData(
    'agencyowner','agencyowner','customer01',100)));
 const agentOnly=await assertSucceeds(getDocsFromServer(
     query(collection(Agent,'king_agent_recharge_requests'),where('agentUid','==','agent01'))));
 if(agentOnly.size!==1)throw Error('Agent should only see own recharge requests');
 const agencyOrders=await assertSucceeds(getDocsFromServer(
     query(collection(Agency,'king_agent_recharge_requests'),where('agencyUid','==','agencyowner'))));
 if(agencyOrders.size!==2)throw Error('Agency needs two authorized orders');
 // Agency suspension revokes recharge power even when agent doc is active.
 await assertSucceeds(updateDoc(agency(Admin,'agencyowner'),{active:false,
   updatedAt:serverTimestamp()}));
 await assertFails(setDoc(order(Agent,'req-after-agency-suspend'),orderData(
   'agent01','agencyowner','customer01',100)));
 await assertFails(setDoc(order(Agency,'req-owner-suspended'),orderData(
   'agencyowner','agencyowner','customer01',100)));
 await assertSucceeds(updateDoc(agency(Admin,'agencyowner'),{active:true,
   updatedAt:serverTimestamp()}));
 await assertSucceeds(updateDoc(agent(Agency,'agent01'),{active:false,
   updatedAt:serverTimestamp()}));
 await assertFails(setDoc(order(Agent,'req-after-agent-suspend'),orderData(
   'agent01','agencyowner','customer01',100)));
 await assertSucceeds(setDoc(order(Agency,'req-restored-owner'),orderData(
   'agencyowner','agencyowner','customer01',1300)));
 await assertSucceeds(getDoc(order(Admin,'req-agent-valid')));
 await assertFails(getDoc(order(Rogue,'req-agent-valid')));
 console.log('PASS Admin Agency grants; owner Agent grants/revokes; assigned Agent pending recharge; 2 Agency order views; forged credits/admin approvals denied; parent suspension blocks all Agent orders');
}finally{await env.cleanup();}
