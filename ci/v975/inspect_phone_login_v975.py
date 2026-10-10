#!/usr/bin/env python3
from pathlib import Path
import re,sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
files=sorted(p.glob('*.java'))
print('login_candidates',[f.name for f in files if any(q in f.name.lower() for q in ['login','sign','auth','otp','recharge'])])
for f in files:
 s=f.read_text(errors='replace')
 if 'PhoneAuthProvider.verifyPhoneNumber' in s or 'phoneOtp' in s or 'mobileOtp' in s or 'btnPhone' in s or 'Send OTP' in s or 'Phone Number' in s:
  print('HIT',f.name)
  lines=s.splitlines()
  for i,row in enumerate(lines):
   if any(k in row for k in ['PhoneAuthProvider','phoneOtp','mobileOtp','btnPhone','Send OTP','Phone Number','verifyPhoneNumber','LoginActivity','signInWithCustomToken']):
    print(f'{i+1}: {row[:1400]}')
for f in files:
 if 'login' in f.name.lower() or 'auth' in f.name.lower():
  rows=f.read_text(errors='replace').splitlines()
  print('=== LOGIN FILE',f.name,'lines',len(rows))
  for i,row in enumerate(rows[:380],1):print(f'{i}: {row[:1250]}')
print('===== ANDROID MANIFEST =====')
m=Path(sys.argv[1])/'app/src/main/AndroidManifest.xml'
for i,row in enumerate(m.read_text().splitlines(),1):
 if 'activity ' in row or 'uses-permission' in row or 'intent-filter' in row or 'LAUNCHER' in row or 'MAIN' in row:
  print(f'{i}: {row}')
