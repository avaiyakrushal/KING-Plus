'use strict';
const {test}=require('node:test');
const assert=require('node:assert/strict');
const vm=require('node:vm'),fs=require('node:fs');
const script=fs.readFileSync('app/src/main/assets/king_cloudflare_voice_10066.js','utf8');
const tick=()=>new Promise(resolve=>setImmediate(resolve));
function endpoint(enabled=true){
 let rtc, captures=0, stopped=0, closes=0;
 const calls=[],states=[];
 class Peer {
  constructor(){rtc=this;this.iceGatheringState='complete';}
  addTransceiver(track){return {mid:'0',sender:{getParameters:()=>({encodings:[{}]}),setParameters:async()=>{},replaceTrack:async()=>{}}};}
  async createOffer(){return {type:'offer',sdp:'v=0\r\nm=audio 9 RTP/AVP 111\r\n'};}
  async setLocalDescription(d){this.localDescription={...d,toJSON:()=>d};}
  async setRemoteDescription(){}
  close(){closes++;}
 }
 const context=vm.createContext({setTimeout,clearTimeout,setInterval,clearInterval,RTCPeerConnection:Peer,
  navigator:{mediaDevices:{getUserMedia:async()=>{captures++;return {getAudioTracks:()=>[{stop:()=>stopped++,enabled:true}]};}}},
  KingVoice:{state:(...args)=>states.push(args),call:(id,raw)=>{
   const data=JSON.parse(raw);calls.push(data);
   const result=data.action==='config'?{enabled,message:'Account pending'}:
    data.action==='join'?{handle:'handle'}:data.action==='tracks'?{tracks:[]}:
    data.action==='publish'?{result:{sessionDescription:{type:'answer',sdp:'v=0\r\n'}}}:{ok:true};
   queueMicrotask(()=>context.endpoint.reply(id,result,null));
  }}});
 vm.runInContext(script+'\nglobalThis.endpoint=KingRtc;',context);
 return {api:context.endpoint,calls,states,get captures(){return captures;},get stopped(){return stopped;},get closes(){return closes;},get pc(){return rtc;}};
}
test('audience can join without microphone capture and join is not connection proof',async()=>{
 const f=endpoint();f.api.start();await tick();assert.equal(f.captures,0);assert.equal(f.states.length,0);
 f.pc.connectionState='connected';f.pc.onconnectionstatechange();assert.equal(f.states[0][0],true);
 f.api.close();await tick();assert.equal(f.closes,1);assert(f.calls.some(c=>c.action==='leave'));
});
test('only explicit microphone consent captures audio; repeated state does not recapture',async()=>{
 const f=endpoint();f.api.start();await tick();f.api.microphone(true,1);await tick();
 assert.equal(f.captures,1);assert.equal(f.calls.filter(c=>c.action==='publish').length,1);
 f.api.microphone(true,1);await tick();assert.equal(f.captures,1);
 f.api.microphone(false,1);assert.equal(f.stopped,1);f.api.close();
});
test('disabled server makes no join or capture attempt',async()=>{
 const f=endpoint(false);f.api.start();await tick();
 assert.deepEqual(f.calls.map(c=>c.action),['config']);assert.equal(f.captures,0);
 assert.equal(f.states[0][0],false);
});
