#!/usr/bin/env python3
"""Stage 9 final security: prevent non-owner Family members modifying their own
role, preserving normal profile edits and owner moderation. Firebase emulator
tests and authenticated production deployment are STILL required.
"""
from pathlib import Path
import sys
r=Path(sys.argv[1])/'firestore.rules'
s=r.read_text()
old='''        allow update: if activeUser()
          && (memberUid == request.auth.uid || familyOwner(familyCode))
          && request.resource.data.uid == memberUid;'''
new='''        allow update: if activeUser()
          && (memberUid == request.auth.uid || familyOwner(familyCode))
          && resource.data.uid == memberUid
          && request.resource.data.uid == memberUid
          // Ordinary Family members cannot escalate own role in Firestore.
          && (familyOwner(familyCode)
              || request.resource.data.role == resource.data.role);'''
assert s.count(old)==1,f'Member role rule anchor count {s.count(old)}, fail closed'
s=s.replace(old,new,1)
r.write_text(s)
print('PASS Family roles cannot be elevated by untrusted member self-update')
print('WARNING: Source patch is not deployed to the production Firebase project')
