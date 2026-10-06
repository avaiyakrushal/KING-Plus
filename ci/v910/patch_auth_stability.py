from pathlib import Path
import sys
root=Path(sys.argv[1]); p=root/'app/src/main/java/com/kingplus/social/MainActivity.java'; s=p.read_text()

old="""    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        installCrashReport();
"""
new="""    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        KingStability.install(this);
        installCrashReport();
"""
assert old in s
s=s.replace(old,new,1)
s=s.replace('button("📱  Continue with Mobile Number • TEST", PURPLE, this::mobileLogin);','button("📱  Continue with Mobile Number", PURPLE, this::mobileLogin);',1)
s=s.replace('text("Google / Gmail uses real Firebase Authentication. Mobile stays in FREE TEST mode with OTP 123456. Facebook still requires Meta provider setup.", 12, MUTED, false);','text("Google uses Firebase. Mobile offers real Firebase SMS OTP plus FREE TEST OTP. Facebook/WhatsApp OTP require provider setup and are not faked.", 12, MUTED, false);',1)

old="""    private void mobileLogin() {
        final EditText phone = new EditText(this);
        phone.setHint("Mobile number, e.g. +919876543210");
        phone.setSingleLine(true);
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        new AlertDialog.Builder(this)
            .setTitle("Mobile login • FREE TEST MODE")
            .setMessage("No SMS will be sent. Enter any valid mobile number, then use OTP 123456.")
            .setView(phone)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue", (d,w) -> {
                String number = normalizePhone(phone.getText().toString());
                if (number == null) {
                    Toast.makeText(this, "Enter a valid mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }
                showTestOtpDialog(number);
            }).show();
    }
"""
new="""    private void mobileLogin() {
        final EditText phone = new EditText(this);
        phone.setHint("Mobile number, e.g. +919876543210");
        phone.setSingleLine(true);
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        new AlertDialog.Builder(this).setTitle("Mobile login")
            .setMessage("After entering your number choose real Firebase SMS OTP or FREE TEST OTP.")
            .setView(phone).setNegativeButton("Cancel", null).setPositiveButton("Continue", (d,w) -> {
                String number = normalizePhone(phone.getText().toString());
                if (number == null) { Toast.makeText(this, "Enter a valid mobile number", Toast.LENGTH_SHORT).show(); return; }
                String[] modes={"📩 Real SMS OTP (Firebase)","🧪 FREE TEST OTP 123456","ℹ WhatsApp OTP status"};
                new AlertDialog.Builder(this).setTitle(number).setItems(modes,(x,which)->{
                    if(which==0){if(ensureFirebaseReady())sendRealOtp(number);}
                    else if(which==1)showTestOtpDialog(number);
                    else new AlertDialog.Builder(this).setTitle("WhatsApp OTP").setMessage("WhatsApp OTP needs an approved provider and secure backend. No fake WhatsApp OTP is generated.").setPositiveButton("OK",null).show();
                }).show();
            }).show();
    }
"""
assert old in s
s=s.replace(old,new,1)

old="""    private void troubleLoginPage() {
        screen = "login_help";
        base("Trouble logging in?", "KING Plus sign-in help");
        text("Mobile • FREE TEST MODE", 18, Color.WHITE, true);
        text("Enter a valid mobile number and use OTP 123456. No SMS is sent and no billing is required.", 15, MUTED, false);
        text("Google", 18, Color.WHITE, true);
        text("Google error 10 means the installed APK signing SHA-1 is not registered for the Firebase/Google OAuth Android client.", 15, MUTED, false);
        text("Facebook", 18, Color.WHITE, true);
        text("Facebook sign-in stays disabled until Meta App credentials and the Firebase Facebook provider are configured.", 15, MUTED, false);
        button("Back to Login", PURPLE, this::login);
    }
"""
new="""    private void troubleLoginPage() {
        screen = "login_help";
        base("Trouble logging in?", "KING Plus sign-in help");
        text("Mobile OTP", 18, Color.WHITE, true);
        text("Use Real SMS OTP when Firebase Phone Authentication is enabled. FREE TEST OTP 123456 remains available and sends no SMS.", 15, MUTED, false);
        text("Google", 18, Color.WHITE, true);
        text("Google error 10 means the APK signing SHA-1 is not registered for the Firebase/Google OAuth Android client.", 15, MUTED, false);
        text("Facebook / WhatsApp OTP", 18, Color.WHITE, true);
        text("Facebook needs Meta/Firebase provider setup. WhatsApp OTP needs an approved provider/backend.", 15, MUTED, false);
        button("Run login diagnostics", CARD, this::authDiagnostics910);
        button("Back to Login", PURPLE, this::login);
    }
"""
assert old in s
s=s.replace(old,new,1)

marker="    private boolean ensureFirebaseReady() {\n"
helpers="""    private void authDiagnostics910(){
        String project="unavailable";try{project=String.valueOf(com.google.firebase.FirebaseApp.getInstance().getOptions().getProjectId());}catch(Exception ignored){}
        int googleId=getResources().getIdentifier("default_web_client_id","string",getPackageName());
        int facebookId=getResources().getIdentifier("facebook_app_id","string",getPackageName());
        boolean signed=firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null;String line=System.lineSeparator();
        String text="Firebase project: "+project+line+"Firebase signed in: "+(signed?"YES":"NO")+line+"Google OAuth client: "+(googleId!=0?"present":"missing")+line+"Facebook App ID resource: "+(facebookId!=0?"present":"missing")+line+"Real SMS OTP code path: present"+line+"WhatsApp OTP provider: not configured";
        new AlertDialog.Builder(this).setTitle("Login diagnostics").setMessage(text).setPositiveButton("Test mobile login",(d,w)->mobileLogin()).setNeutralButton("Google login",(d,w)->googleLogin()).setNegativeButton("Close",null).show();
    }
    private void shareDiagnostics910(){
        try{java.io.File f=KingStability.logFile(this);if(f==null||!f.exists()){Toast.makeText(this,"No crash diagnostics recorded",Toast.LENGTH_SHORT).show();return;}String raw=new String(java.nio.file.Files.readAllBytes(f.toPath()),java.nio.charset.StandardCharsets.UTF_8);if(raw.length()>12000)raw=raw.substring(raw.length()-12000);Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,"KING Plus diagnostics"+System.lineSeparator()+raw);startActivity(Intent.createChooser(send,"Share diagnostics"));}catch(Exception e){Toast.makeText(this,"Diagnostics could not be shared",Toast.LENGTH_SHORT).show();}
    }

"""
assert marker in s
s=s.replace(marker,helpers+marker,1)
s=s.replace('button("Login & OTP help",CARD,()->new AlertDialog.Builder(this).setTitle("Login & OTP").setMessage("FREE TEST MODE: enter a valid phone number and use OTP 123456. No SMS is sent. Production SMS OTP can be restored after Firebase provider setup.").setPositiveButton("OK",null).show());','button("Login & OTP help",CARD,this::authDiagnostics910);button("Share crash diagnostics",CARD,this::shareDiagnostics910);button("Clear crash diagnostics",CARD,()->{KingStability.clear(this);Toast.makeText(this,"Diagnostics cleared",Toast.LENGTH_SHORT).show();});',1)
p.write_text(s)
print("auth/stability patched")
