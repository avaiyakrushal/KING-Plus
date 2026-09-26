package com.kingplus.social;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int MIC_REQUEST = 21;
    private static final int NAVY = 0xff0c1026, CARD = 0xff1b2141, PURPLE = 0xff7146ec;
    private static final int MUTED = 0xffc6bfdf;
    private final ArrayList<String> rooms = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout page;
    private AudioRecord recorder;
    private Thread meterThread;
    private volatile boolean recording;
    private TextView meter;
    private String currentRoom;
    private String displayName;
    private String screen = "login";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        rooms.add("Music & Friends"); rooms.add("KING Lounge"); rooms.add("Game Talk");
        Set<String> saved = getPreferences(0).getStringSet("rooms", new HashSet<>());
        for (String name : saved) if (!rooms.contains(name)) rooms.add(0, name);
        displayName = getPreferences(0).getString("name", "");
        if (displayName.isEmpty()) login(); else home();
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable background(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private void base(String title, String subtitle) {
        stopMic();
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(NAVY);
        page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(30), dp(22), dp(36)); scroll.addView(page); setContentView(scroll);
        text(title, 29, Color.WHITE, true);
        if (subtitle != null) text(subtitle, 15, MUTED, false);
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        t.setPadding(dp(3), dp(10), dp(3), dp(12)); page.addView(t); return t;
    }
    private void button(String title, int color, Runnable action) {
        Button b = new Button(this); b.setText(title); b.setTextColor(Color.WHITE); b.setAllCaps(false);
        b.setTextSize(16); b.setBackground(background(color, 18));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(56)); p.setMargins(0, dp(7), 0, dp(7));
        page.addView(b, p); b.setOnClickListener(v -> action.run());
    }
    private EditText input(String hint, String value) {
        EditText e = new EditText(this); e.setSingleLine(true); e.setHint(hint); e.setText(value);
        e.setTextColor(Color.WHITE); e.setHintTextColor(MUTED); e.setPadding(dp(16), 0, dp(16), 0);
        e.setBackground(background(CARD, 15));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(56)); p.setMargins(0, dp(16), 0, dp(12));
        page.addView(e, p); return e;
    }
    private void login() {
        screen = "login"; base("👑 KING Plus", "મિત્રો અને વૉઇસ રૂમની દુનિયામાં આપનું સ્વાગત છે");
        text("તમારું નામ લખીને શરૂ કરો", 20, Color.WHITE, true);
        EditText name = input("તમારું નામ", "");
        button("આગળ વધો", PURPLE, () -> {
            String value = name.getText().toString().trim();
            if (value.isEmpty()) { name.setError("નામ લખો"); return; }
            displayName = value;
            getPreferences(0).edit().putString("name", value).apply(); home();
        });
        text("આ નામ આ ફોનમાં જ સાચવાય છે. ઓનલાઈન એકાઉન્ટ અને Google લૉગિન હજી ઉપલબ્ધ નથી.", 14, MUTED, false);
    }
    private void home() {
        screen = "home"; base("👑 KING Plus", "નમસ્તે, " + displayName + " 👋");
        button("+ નવો રૂમ બનાવો", PURPLE, this::createRoom);
        text("વૉઇસ રૂમ", 22, Color.WHITE, true);
        for (String room : rooms) button("🎙  " + room + "   →", CARD, () -> room(room));
        button("મારી પ્રોફાઇલ", CARD, this::profile);
    }
    private void createRoom() {
        EditText e = new EditText(this); e.setSingleLine(true); e.setHint("રૂમનું નામ");
        LinearLayout wrap = new LinearLayout(this); wrap.setPadding(dp(20), 0, dp(20), 0); wrap.addView(e);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("નવો રૂમ").setView(wrap)
            .setNegativeButton("રદ કરો", null).setPositiveButton("બનાવો", null).create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(w -> {
            String name = e.getText().toString().trim();
            if (name.isEmpty()) { e.setError("રૂમનું નામ લખો"); return; }
            if (rooms.contains(name)) { e.setError("આ નામનો રૂમ પહેલેથી છે"); return; }
            rooms.add(0, name);
            getPreferences(0).edit().putStringSet("rooms", new HashSet<>(rooms)).apply();
            dialog.dismiss(); room(name);
        })); dialog.show();
    }
    private void room(String name) {
        screen = "room"; currentRoom = name;
        base("🎙 " + name, "તમારો માઇક તપાસો");
        text("સભ્ય: " + displayName, 18, Color.WHITE, true);
        meter = text("માઇક બંધ છે", 18, MUTED, false);
        button("માઇક ટેસ્ટ શરૂ કરો", PURPLE, this::requestMic);
        button("માઇક બંધ કરો", CARD, () -> { stopMic(); if (meter != null) meter.setText("માઇક બંધ છે"); });
        text("માઇક ટેસ્ટ ફક્ત આ ફોન પર અવાજનું સ્તર બતાવે છે. બીજા લોકો સુધી અવાજ પહોંચાડવા લાઇવ વૉઇસ સેવા જોડવી પડશે.", 14, MUTED, false);
        button("રૂમમાંથી બહાર નીકળો", CARD, this::home);
    }
    private void requestMic() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQUEST); return;
        }
        startMic();
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == MIC_REQUEST && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED && "room".equals(screen)) startMic();
        else if (requestCode == MIC_REQUEST) Toast.makeText(this, "માઇક પરવાનગી જરૂરી છે", Toast.LENGTH_SHORT).show();
    }
    private void startMic() {
        stopMic();
        int rate = 16000;
        int min = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (min <= 0) { Toast.makeText(this, "માઇક ઉપલબ્ધ નથી", Toast.LENGTH_SHORT).show(); return; }
        try {
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, Math.max(min, 2048));
            if (recorder.getState() != AudioRecord.STATE_INITIALIZED) { stopMic(); return; }
            recorder.startRecording(); recording = true;
            if (meter != null) meter.setText("માઇક ચાલુ છે • બોલીને તપાસો");
            AudioRecord source = recorder;
            meterThread = new Thread(() -> {
                short[] samples = new short[1024];
                while (recording) {
                    int count = source.read(samples, 0, samples.length);
                    if (count <= 0) break;
                    long sum = 0;
                    for (int i = 0; i < count; i++) sum += Math.abs((int)samples[i]);
                    int level = (int)Math.min(10, (sum / count) / 1800);
                    handler.post(() -> { if (recording && meter != null) meter.setText("માઇક ચાલુ છે  " + bars(level)); });
                }
            }); meterThread.start();
        } catch (Exception ex) { stopMic(); Toast.makeText(this, "માઇક શરૂ થઈ શક્યો નથી", Toast.LENGTH_SHORT).show(); }
    }
    private void stopMic() {
        recording = false;
        if (recorder != null) {
            try { recorder.stop(); } catch (Exception ignored) { }
            recorder.release(); recorder = null;
        }
        meterThread = null;
    }
    private String bars(int level) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 10; i++) result.append(i < level ? '▮' : '▯');
        return result.toString();
    }
    private void profile() {
        screen = "profile"; base("મારી પ્રોફાઇલ", "KING Plus સભ્ય");
        text("👑  " + displayName, 24, Color.WHITE, true);
        button("નામ બદલો", PURPLE, () -> {
            EditText e = new EditText(this); e.setText(displayName);
            new AlertDialog.Builder(this).setTitle("તમારું નામ").setView(e).setNegativeButton("રદ કરો", null)
                .setPositiveButton("સાચવો", (d, which) -> {
                    String value = e.getText().toString().trim();
                    if (!value.isEmpty()) { displayName = value; getPreferences(0).edit().putString("name", value).apply(); profile(); }
                }).show();
        });
        button("આ ફોનમાંથી સાઇન આઉટ", CARD, () -> new AlertDialog.Builder(this).setMessage("આ ફોનમાંથી પ્રોફાઇલ દૂર કરવી છે?")
            .setNegativeButton("રદ કરો", null).setPositiveButton("દૂર કરો", (d, which) -> {
                getPreferences(0).edit().remove("name").apply(); displayName = ""; login();
            }).show());
        button("પાછા જાઓ", CARD, this::home);
    }
    @Override public void onBackPressed() {
        if ("login".equals(screen) || "home".equals(screen)) super.onBackPressed(); else home();
    }
    @Override protected void onPause() { super.onPause(); stopMic(); }
    @Override protected void onDestroy() { stopMic(); super.onDestroy(); }
}
