# KING Plus v9.7.8 — Startup "Killed by signal" diagnostics

## Device report received
- Installed: `9.7.5-cloudflare-mobile-otp-preparation`
- Phone: vivo Android 16 (screenshot)
- New install: Oct 10, 2026 07:54:29
- Android exit: Oct 10, 2026 07:54:39
- Reporter displayed "Killed by signal", without status number, with "last screen (not recorded for this process)" and PSS/RSS both zero.
- The reporter modal was visible over a currently RUNNING login Activity. A past process exit is not proof that the new Activity just crashed.

## Source-confirmed bug
`KingCrashWatch958.showPreviousExit` treated **all** `ApplicationExitInfo.REASON_SIGNALED` records as definite abnormal crashes. Android's `ApplicationExitInfo.getStatus()` provides the real signal number; on some devices SIGKILL may also stand for the OS killing a process due to memory pressure instead of a code fault. Installer/app-update lifecycle can also kill processes. The prior popup title incorrectly claimed "new crash report" with no actual signal status.

Official Android documentation:
https://developer.android.com/reference/android/app/ApplicationExitInfo

## v9.7.8 fix
- Classifies `getStatus()` signals before showing an intrusive report.
- `SIGKILL(9)`, `SIGTERM(15)` and missing status no longer automatically generate a false "new crash" modal, although the last OS signal status and time are saved locally.
- `SIGSEGV(11)`, `SIGABRT(6)`, `SIGBUS(7)`, `SIGILL(4)` and `SIGFPE(8)` still produce actionable alerts with their actual signal number, even for an early process with no recorded screen.
- Other nonstandard signals produce alerts only when attributable to the previously tracked Activity PID.
- **Android native crash reason, ANR, initialization failures and explicit LOW_MEMORY reasons continue to be reported.**
- Does not clear installed app data or modify Firebase/OTP/wallet, Party, Ludo or Mic.
- Prevents false reports only; not proof that the native memory problem is fixed.

## Testing instructions on Android phone
1. Install `v9.7.8` over v9.7.5/7 if Android allows same signer. Avoid uninstalling so login/settings persist.
2. Open the app: if login appears and stays responsive, verify that a previous `SIGKILL` history does not trigger a "new crash" popup. Click Sign in with Google and navigate to Party.
3. Reopen after moving app to background and returning. Keep it open for 15 minutes; observe whether app truly closes.
4. Test Party join with three different accounts, Mic toggle, Wallet screen and Mobile OTP page; verify each opens, including handling a missing SMS provider as an error rather than faking login.
5. If the app **actually disappears** and restarts, press Copy Details on the new report, capture **Actual signal** and **OS status code**, along with crash time and exact screen. A complete `logcat` or Android native tombstone is still needed to identify the responsible library.
6. Do not assume a zero RSS/PSS field rules out a native crash; Android does not guarantee memory samples in process exit entries.

## Risks and limitations
- Android may report a real low-memory eviction as SIGKILL on devices without REASON_LOW_MEMORY support. This version avoids blaming an application crash based only on SIGKILL; lower-memory pressure may still need separate profiling.
- GitHub Actions Gradle compile and unit tests are not hardware testing. Phone acceptance must still be performed.
