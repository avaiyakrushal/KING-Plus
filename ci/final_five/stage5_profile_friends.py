#!/usr/bin/env python3
"""Stage 5: safe bounded public profile image downloads and no cached own
identity on Firebase publish. Account login/provider secrets are not fabricated.
Photo HTTP/private URLs are rejected; no Play/Billing auth changes.
"""
from pathlib import Path
import sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
f=p/'KingPublicProfileActivity.java'
s=f.read_text()
start=s.index('    private void loadProfilePhoto940(android.widget.ImageView image,TextView fallback,String url){')
end=s.index('    private void addPublicCounts940()',start)
orig=s[start:end]
assert 'new java.net.URL(source).openConnection()' in orig
replacement=r'''    private void loadProfilePhoto940(android.widget.ImageView image,
                                     TextView fallback,String url){
        final String source=url==null?"":url.trim();
        image.setTag(source);
        // Prevent loading insecure/plaintext/private-content URLs in another
        // user's profile. Firestore Storage photo links are HTTPS.
        if(!source.startsWith("https://")||source.length()>2048)return;
        new Thread(()->{
            java.net.HttpURLConnection c=null;
            try{
                java.net.URL safeUrl=new java.net.URL(source);
                if(!"https".equalsIgnoreCase(safeUrl.getProtocol()))return;
                java.net.URLConnection connection=safeUrl.openConnection();
                if(!(connection instanceof java.net.HttpURLConnection))return;
                c=(java.net.HttpURLConnection)connection;
                c.setConnectTimeout(6500);
                c.setReadTimeout(6500);
                c.setInstanceFollowRedirects(true);
                c.setRequestProperty("Accept","image/*");
                try(java.io.InputStream stream=c.getInputStream()){
                    if(!"https".equalsIgnoreCase(c.getURL().getProtocol()))return;
                    long declared=c.getContentLengthLong();
                    final int maxBytes=3*1024*1024;
                    if(declared>maxBytes)return;
                    java.io.ByteArrayOutputStream output=
                        new java.io.ByteArrayOutputStream(64*1024);
                    byte[] buffer=new byte[8192];
                    int received=0,n;
                    while((n=stream.read(buffer))!=-1){
                        received+=n;
                        if(received>maxBytes)return;
                        output.write(buffer,0,n);
                    }
                    byte[] bytes=output.toByteArray();
                    if(bytes.length==0)return;
                    android.graphics.BitmapFactory.Options bound=
                        new android.graphics.BitmapFactory.Options();
                    bound.inJustDecodeBounds=true;
                    android.graphics.BitmapFactory.decodeByteArray(
                        bytes,0,bytes.length,bound);
                    if(bound.outWidth<1||bound.outHeight<1
                        ||bound.outWidth>16000||bound.outHeight>16000)return;
                    int scale=1;
                    while(bound.outWidth/scale>640||bound.outHeight/scale>640)
                        scale*=2;
                    android.graphics.BitmapFactory.Options decoded=
                        new android.graphics.BitmapFactory.Options();
                    decoded.inSampleSize=scale;
                    final android.graphics.Bitmap photo=
                        android.graphics.BitmapFactory.decodeByteArray(
                            bytes,0,bytes.length,decoded);
                    if(photo==null)return;
                    runOnUiThread(()->{
                        if(!isFinishing()&&!isDestroyed()
                            &&source.equals(image.getTag())){
                            image.setImageBitmap(photo);
                            fallback.setVisibility(android.view.View.GONE);
                        }else if(!photo.isRecycled())photo.recycle();
                    });
                }
            }catch(Exception ignored){
                // Profile DP failure must not crash messaging, friends or Party.
            }finally{
                if(c!=null)c.disconnect();
            }
        },"KING-Safe-Public-DP").start();
    }

'''
s=s[:start]+replacement+s[end:]
assert 'showPublicCollection953' in s and 'addPublicCounts940' in s
f.write_text(s)
print('PASS public Profile DP HTTPS-only, max 3MB and downsampled to <=640px')
# Re-query own profile from SERVER to avoid a stale local cache overwriting
# another phone's user edits during publication.
social=p/'SocialActivity.java';s=social.read_text()
needle='profile970.get().addOnSuccessListener(existing970->'
assert s.count(needle)==1
s=s.replace(needle,'profile970.get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(existing970->',1)
assert 'followId(' in s and 'connectionTest996()' in s
social.write_text(s)
print('PASS publish own public displayName and KING ID based on SERVER version')
print('NOTE Mobile OTP/Facebook still need authorized real provider configuration and device tests')
