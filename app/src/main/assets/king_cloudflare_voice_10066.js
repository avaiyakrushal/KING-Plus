'use strict';
// One serialized media connection per room visit. No API secrets or Firebase tokens in JS.
const KingRtc = (() => {
  let pc, handle, stopped=false, timer, microphoneTrack, sender, wanted=false, seat=-1;
  let queue=Promise.resolve(), serial=0, subscriptionCount=0;
  let micGeneration=0;
  const pending=new Map(), sources=new Set(), audio=new Map();
  function call(action, data={}) {
    if(stopped && action!=='leave') return Promise.reject(Error('Voice closed'));
    return new Promise((resolve,reject)=>{
      const id=String(++serial);
      const timeout=setTimeout(()=>{pending.delete(id);reject(Error('Voice server timed out'));},18000);
      pending.set(id,{resolve,reject,timeout});
      KingVoice.call(id,JSON.stringify({action,handle,...data}));
    });
  }
  function reply(id,result,error) {
    const item=pending.get(id);if(!item)return;pending.delete(id);clearTimeout(item.timeout);
    if(error)item.reject(Error(error));else item.resolve(result);
  }
  function enqueue(fn) {
    queue=queue.then(()=>stopped?undefined:fn()).catch(error=>{
      if(!stopped){mute();KingVoice.state(false,error.message||'Voice connection failed');close();}
    });return queue;
  }
  async function gathered() {
    if(pc.iceGatheringState==='complete')return;
    await new Promise((resolve,reject)=>{
      const timeout=setTimeout(()=>{pc.removeEventListener('icegatheringstatechange',changed);reject(Error('Voice network timed out'));},10000);
      function changed(){if(pc.iceGatheringState==='complete'){
        clearTimeout(timeout);pc.removeEventListener('icegatheringstatechange',changed);resolve();}}
      pc.addEventListener('icegatheringstatechange',changed);changed();
    });
  }
  async function start() {
    enqueue(async()=>{
      const config=await call('config');
      if(!config.enabled)throw Error(config.message||'Cloudflare setup is pending');
      handle=(await call('join')).handle;
      if(stopped)return;
      pc=new RTCPeerConnection({iceServers:[{urls:'stun:stun.cloudflare.com:3478'}]});
      pc.onconnectionstatechange=()=>{
        if(stopped)return;
        if(pc.connectionState==='connected')KingVoice.state(true,'Cloudflare voice connected');
        if(['failed','disconnected','closed'].includes(pc.connectionState)){
          mute();KingVoice.state(false,'Voice disconnected • tap to reconnect');close();
        }
      };
      pc.ontrack=event=>{
        const element=document.createElement('audio');element.autoplay=true;
        element.srcObject=new MediaStream([event.track]);document.body.appendChild(element);
        audio.set(event.transceiver.mid,element);
        element.play().catch(()=>{KingVoice.state(false,'Audio playback blocked • reconnect voice');close();});
      };
      await sync();
      timer=setInterval(()=>enqueue(sync),10000);
    });
  }
  function mute(){if(microphoneTrack){microphoneTrack.enabled=false;microphoneTrack.stop();microphoneTrack=null;}}
  function microphone(allowed,nextSeat){
    if(wanted===!!allowed&&seat===nextSeat)return;
    wanted=!!allowed;seat=nextSeat;
    const generation=++micGeneration;
    if(!wanted)mute();
    enqueue(async()=>{
      if(!wanted||!pc)return;
      const stream=await navigator.mediaDevices.getUserMedia({audio:{echoCancellation:true,
        noiseSuppression:true,autoGainControl:true},video:false});
      const track=stream.getAudioTracks()[0];
      if(stopped||!wanted||generation!==micGeneration){track.stop();return;}
      if(microphoneTrack)microphoneTrack.stop();microphoneTrack=track;
      if(sender){await sender.replaceTrack(track);return;}
      const transceiver=pc.addTransceiver(track,{direction:'sendonly'});sender=transceiver.sender;
      const parameters=sender.getParameters();
      if(parameters.encodings&&parameters.encodings.length){parameters.encodings[0].maxBitrate=32000;
        await sender.setParameters(parameters);}
      await pc.setLocalDescription(await pc.createOffer());await gathered();
      const response=await call('publish',{seat,mid:transceiver.mid,description:pc.localDescription.toJSON()});
      if(stopped)return;
      await pc.setRemoteDescription(response.result.sessionDescription);
      if(!wanted)mute();
    });
  }
  async function sync(){
    if(!pc||stopped)return;
    const result=await call('tracks');
    const fresh=result.tracks.map(t=>t.handle).filter(h=>!sources.has(h));
    if(!fresh.length)return;
    // Bounded track accumulation: require a fresh session rather than leaking transceivers indefinitely.
    if(subscriptionCount+fresh.length>32)throw Error('Voice connection needs refresh • tap to reconnect');
    const response=await call('listen',{sources:fresh});
    await pc.setRemoteDescription(response.result.sessionDescription);
    await pc.setLocalDescription(await pc.createAnswer());await gathered();
    await call('answer',{description:pc.localDescription.toJSON()});
    fresh.forEach(h=>sources.add(h));subscriptionCount+=fresh.length;
  }
  function close(){
    if(stopped)return;stopped=true;clearInterval(timer);mute();
    if(pc){pc.ontrack=null;pc.onconnectionstatechange=null;pc.close();}
    audio.forEach(element=>{element.pause();element.srcObject=null;element.remove();});audio.clear();
    pending.forEach(item=>{clearTimeout(item.timeout);item.reject(Error('Voice closed'));});pending.clear();
    if(handle)call('leave').catch(()=>{});
  }
  return {start,reply,microphone,close};
})();
