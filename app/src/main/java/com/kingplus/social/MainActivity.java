package com.kingplus.social;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private final ArrayList<String> rooms = new ArrayList<>();
    private LinearLayout page;
    private final int navy = Color.rgb(12, 16, 38), purple = Color.rgb(113, 70, 236);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        rooms.add("🎤  Music & Friends"); rooms.add("👑  KING Lounge"); rooms.add("🎮  Game Talk");
        home();
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private void base(String title) {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(navy);
        page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(22), dp(32), dp(22), dp(22));
        scroll.addView(page); setContentView(scroll);
        label(title, 29, Color.WHITE, true);
    }
    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        t.setPadding(0, dp(10), 0, dp(12)); page.addView(t); return t;
    }
    private void button(String title, Runnable action) {
        Button b = new Button(this); b.setText(title); b.setTextColor(Color.WHITE); b.setAllCaps(false);
        b.setBackground(bg(purple, 18)); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(54));
        p.setMargins(0, dp(9), 0, dp(9)); page.addView(b, p); b.setOnClickListener(v -> action.run());
    }
    private void home() {
        base("👑 KING Plus"); label("મિત્રો સાથે મળો અને રૂમ શોધો", 16, 0xffc6bfdf, false);
        button("+ નવો રૂમ બનાવો", this::createRoom);
        label("વૉઇસ રૂમ", 22, Color.WHITE, true);
        for (String room : rooms) button(room + "   →", () -> room(room));
        button("મારી પ્રોફાઇલ", this::profile);
    }
    private void createRoom() {
        EditText input = new EditText(this); input.setSingleLine(true); input.setHint("રૂમનું નામ");
        new AlertDialog.Builder(this).setTitle("નવો રૂમ").setView(input)
            .setNegativeButton("રદ કરો", null).setPositiveButton("બનાવો", (dialog, which) -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty()) { Toast.makeText(this, "રૂમનું નામ લખો", Toast.LENGTH_SHORT).show(); return; }
                rooms.add(0, "🎤  " + name); home();
            }).show();
    }
    private void room(String name) {
        base(name); label("રૂમમાં આપનું સ્વાગત છે", 19, Color.WHITE, true);
        label("આ પ્રાથમિક સ્ક્રીન છે. અન્ય લોકો સાથે લાઇવ અવાજ જોડવા માટે ઓડિયો સર્વર જરૂરી છે.", 16, 0xffc6bfdf, false);
        button("માઇક વિશે", () -> new AlertDialog.Builder(this).setMessage("લાઇવ વૉઇસ સેવા જોડાયા પછી માઇક ચાલુ કરી શકાશે.").setPositiveButton("ઠીક", null).show());
        button("રૂમમાંથી બહાર નીકળો", this::home);
    }
    private void profile() {
        base("મારી પ્રોફાઇલ"); label("👑  KING Plus સભ્ય", 22, Color.WHITE, true);
        label("આગામી વર્ઝનમાં એકાઉન્ટ, ફોટો અને વ્યક્તિગત ID ઉમેરાશે.", 16, 0xffc6bfdf, false);
        button("પાછા જાઓ", this::home);
    }
    @Override public void onBackPressed() { home(); }
}
