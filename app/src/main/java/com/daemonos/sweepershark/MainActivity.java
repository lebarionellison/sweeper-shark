package com.daemonos.sweepershark;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class MainActivity extends Activity {

    static final int BG = Color.rgb(6,8,13);
    static final int CARD = Color.rgb(15,20,29);
    static final int TEXT = Color.rgb(238,245,250);
    static final int MUTED = Color.rgb(145,160,175);
    static final int CYAN = Color.rgb(85,214,255);
    static final int RED = Color.rgb(255,105,125);
    static final int AMBER = Color.rgb(255,190,80);
    static final int GREEN = Color.rgb(105,230,165);

    TextView result;
    TextView status;
    TextView history;
    Button scan;
    Button quarantine;
    Button keep;

    SharedPreferences prefs;
    Analysis lastAnalysis;
    String lastMessage;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        prefs = getSharedPreferences("sweeper_shark", MODE_PRIVATE);

        build();
    }

    TextView text(String s, float size, int color) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setPadding(0, 8, 0, 8);
        return v;
    }

    Button button(String label, int color) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(BG);
        b.setBackgroundColor(color);
        return b;
    }

    void build() {

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 30, 28, 28);

        TextView brand = text(
            "DAEMON OS  /  LOCAL INTELLIGENCE",
            12,
            CYAN
        );
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(brand);

        TextView title = text(
            "Sweeper Shark",
            34,
            TEXT
        );
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title);

        root.addView(text(
            "Private email threat detection that analyzes messages on this device.",
            16,
            MUTED
        ));

        status = text(
            "READY",
            13,
            CYAN
        );
        status.setPadding(0, 26, 0, 8);
        root.addView(status);

        scan = button(
            "CHOOSE EMAIL TO SCAN",
            CYAN
        );
        scan.setOnClickListener(v -> choose());
        root.addView(
            scan,
            new LinearLayout.LayoutParams(
                -1,
                58
            )
        );

        result = text(
            "Choose an .eml, .txt, or .html message.\n\n" +
            "Sweeper Shark will analyze it locally and explain the signals it finds.",
            16,
            TEXT
        );
        result.setPadding(0, 26, 0, 14);
        root.addView(result);

        keep = button(
            "KEEP MESSAGE",
            GREEN
        );
        keep.setVisibility(View.GONE);
        keep.setOnClickListener(v -> keepMessage());
        root.addView(
            keep,
            new LinearLayout.LayoutParams(
                -1,
                54
            )
        );

        quarantine = button(
            "QUARANTINE MESSAGE",
            RED
        );
        quarantine.setVisibility(View.GONE);
        quarantine.setOnClickListener(v -> quarantineMessage());
        root.addView(
            quarantine,
            new LinearLayout.LayoutParams(
                -1,
                54
            )
        );

        root.addView(text(
            "\nCLEANUP REPORT",
            13,
            CYAN
        ));

        root.addView(text(
            "Messages are never automatically deleted from a real mailbox by this local-only build. " +
            "Quarantine stores the scanned message locally so you can review or restore it.",
            14,
            MUTED
        ));

        history = text(
            buildHistory(),
            14,
            TEXT
        );
        history.setPadding(0, 18, 0, 10);
        root.addView(history);

        root.addView(text(
            "\nWHAT SWEeper SHARK CHECKS\n\n" +
            "• Suspicious and unusual links\n" +
            "• Look-alike or unusual domains\n" +
            "• Urgent and pressure language\n" +
            "• Credential and password requests\n" +
            "• Financial and payment requests\n" +
            "• Common phishing patterns\n\n" +
            "THREAT SCORE\n" +
            "The score is an explainable signal, not a guarantee. " +
            "Always verify unexpected requests through a trusted channel.",
            14,
            MUTED
        ));

        root.addView(text(
            "\nPRIVACY\n\n" +
            "No account. No cloud upload. No ads. No analytics SDK. " +
            "No Google Play Services. No mandatory network connection.",
            14,
            MUTED
        ));

        root.addView(text(
            "\nSWEEPER SHARK PRO\n\n" +
            "Advanced mailbox protection, automated cleanup, " +
            "advanced Daemon Intelligence, sender/domain controls, " +
            "and additional protection features are planned for the commercial edition.",
            14,
            AMBER
        ));

        scroll.addView(root);
        setContentView(scroll);
    }

    void choose() {

        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(i, 42);
    }

    @Override
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        );

        if (
            requestCode != 42 ||
            resultCode != RESULT_OK ||
            data == null
        ) {
            return;
        }

        Uri uri = data.getData();

        if (uri == null) {
            return;
        }

        scan.setEnabled(false);
        keep.setVisibility(View.GONE);
        quarantine.setVisibility(View.GONE);

        status.setText("ANALYZING LOCALLY…");

        new Thread(() -> {

            try {

                InputStream input =
                    getContentResolver().openInputStream(uri);

                String message = read(input);

                Analysis analysis = analyze(message);

                lastMessage = message;
                lastAnalysis = analysis;

                runOnUiThread(() -> {

                    status.setText(analysis.label);
                    result.setText(analysis.report);

                    keep.setVisibility(View.VISIBLE);
                    quarantine.setVisibility(View.VISIBLE);

                    scan.setEnabled(true);
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    status.setText("ERROR");

                    result.setText(
                        "Could not read that file.\n\n" +
                        e.getMessage()
                    );

                    scan.setEnabled(true);
                });
            }

        }).start();
    }

    String read(InputStream input) throws Exception {

        if (input == null) {
            throw new IOException("Unable to open the selected file.");
        }

        BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(input)
            );

        StringBuilder builder = new StringBuilder();

        String line;
        int count = 0;

        while (
            (line = reader.readLine()) != null &&
            count++ < 20000
        ) {
            builder
                .append(line)
                .append('\n');
        }

        reader.close();

        return builder.toString();
    }

    Analysis analyze(String message) {

        String x =
            message.toLowerCase(Locale.US);

        int score = 0;

        ArrayList<String> reasons =
            new ArrayList<>();

        String[] urgent = {
            "urgent",
            "immediately",
            "act now",
            "final notice",
            "account suspended",
            "verify your account",
            "last warning",
            "within 24 hours"
        };

        for (String signal : urgent) {

            if (x.contains(signal)) {

                score += 10;

                reasons.add(
                    "Pressure language: " + signal
                );
            }
        }

        String[] sensitive = {
            "gift card",
            "wire transfer",
            "bitcoin",
            "crypto",
            "payment due",
            "bank account",
            "credit card",
            "routing number",
            "password",
            "login",
            "one-time code",
            "otp",
            "security code",
            "social security"
        };

        for (String signal : sensitive) {

            if (x.contains(signal)) {

                score += 8;

                reasons.add(
                    "Sensitive or financial request: " +
                    signal
                );
            }
        }

        Matcher matcher =
            Pattern.compile(
                "https?://[^\\s<>\"]+",
                Pattern.CASE_INSENSITIVE
            ).matcher(message);

        int links = 0;

        while (matcher.find()) {

            links++;

            String url =
                matcher.group();

            if (
                url.contains("@") ||
                url.matches(
                    ".*https?://\\d+\\.\\d+\\.\\d+\\.\\d+.*"
                )
            ) {

                score += 15;

                reasons.add(
                    "Unusual link format detected"
                );
            }

            if (
                url.contains("bit.ly") ||
                url.contains("tinyurl.com") ||
                url.contains("t.co/") ||
                url.contains("goo.gl")
            ) {

                score += 10;

                reasons.add(
                    "URL shortener detected"
                );
            }
        }

        if (links > 3) {

            score += 10;

            reasons.add(
                "Many links in message"
            );
        }

        score =
            Math.min(score, 100);

        String label;

        if (score >= 60) {

            label = "HIGH RISK";

        } else if (score >= 30) {

            label = "SUSPICIOUS";

        } else {

            label = "LOW SIGNAL";
        }

        StringBuilder report =
            new StringBuilder();

        report.append(
            "THREAT SCORE  "
        );

        report.append(score);

        report.append(
            " / 100\n\n"
        );

        if (score >= 60) {

            report.append(
                "Hazardous signals require review before interacting with this message.\n\n"
            );

        } else if (score >= 30) {

            report.append(
                "Suspicious signals were detected. Review before responding or opening links.\n\n"
            );

        } else {

            report.append(
                "No strong phishing signals were detected by the current local ruleset.\n\n"
            );
        }

        if (!reasons.isEmpty()) {

            report.append(
                "WHY IT WAS FLAGGED\n"
            );

            for (String reason : reasons) {

                report
                    .append("• ")
                    .append(reason)
                    .append('\n');
            }

        } else {

            report.append(
                "WHY IT WAS FLAGGED\n" +
                "• No matching high-confidence rules\n"
            );
        }

        report.append(
            "\nLINKS FOUND: "
        );

        report.append(links);

        report.append(
            "\n\nDaemon Intelligence\n"
        );

        report.append(
            "Analysis completed locally. " +
            "Message contents were not uploaded."
        );

        return new Analysis(
            label,
            report.toString(),
            score,
            reasons.size(),
            links
        );
    }

    void keepMessage() {

        if (lastAnalysis == null) {
            return;
        }

        recordHistory(
            "KEPT",
            lastAnalysis.score
        );

        status.setText("MESSAGE KEPT");

        result.setText(
            "MESSAGE KEPT\n\n" +
            "Sweeper Shark found a threat signal but the message was kept.\n\n" +
            "No copy was moved to quarantine.\n\n" +
            buildHistory()
        );

        keep.setVisibility(View.GONE);
        quarantine.setVisibility(View.GONE);

        refreshHistory();
    }

    void quarantineMessage() {

        if (
            lastAnalysis == null ||
            lastMessage == null
        ) {
            return;
        }

        String key =
            "quarantine_" +
            System.currentTimeMillis();

        prefs.edit()
            .putString(
                key,
                lastMessage
            )
            .apply();

        recordHistory(
            "QUARANTINED",
            lastAnalysis.score
        );

        status.setText("QUARANTINED");

        result.setText(
            "MESSAGE QUARANTINED\n\n" +
            "The scanned message has been stored locally for review.\n\n" +
            "It has not been deleted from a mailbox.\n\n" +
            "THREAT SCORE: " +
            lastAnalysis.score +
            " / 100\n\n" +
            buildHistory()
        );

        keep.setVisibility(View.GONE);
        quarantine.setVisibility(View.GONE);

        refreshHistory();
    }

    void recordHistory(
        String action,
        int score
    ) {

        int scanned =
            prefs.getInt(
                "scanned",
                0
            ) + 1;

        int quarantined =
            prefs.getInt(
                "quarantined",
                0
            );

        int kept =
            prefs.getInt(
                "kept",
                0
            );

        if (action.equals("QUARANTINED")) {
            quarantined++;
        }

        if (action.equals("KEPT")) {
            kept++;
        }

        int threats =
            prefs.getInt(
                "threats",
                0
            );

        if (score >= 30) {
            threats++;
        }

        prefs.edit()
            .putInt("scanned", scanned)
            .putInt("quarantined", quarantined)
            .putInt("kept", kept)
            .putInt("threats", threats)
            .apply();
    }

    String buildHistory() {

        int scanned =
            prefs.getInt(
                "scanned",
                0
            );

        int threats =
            prefs.getInt(
                "threats",
                0
            );

        int quarantined =
            prefs.getInt(
                "quarantined",
                0
            );

        int kept =
            prefs.getInt(
                "kept",
                0
            );

        return
            "CLEANUP HISTORY\n\n" +
            "Messages scanned     " + scanned + "\n" +
            "Threats detected     " + threats + "\n" +
            "Quarantined          " + quarantined + "\n" +
            "Kept                 " + kept;
    }

    void refreshHistory() {

        if (history != null) {
            history.setText(
                buildHistory()
            );
        }
    }

    static class Analysis {

        String label;
        String report;
        int score;
        int signals;
        int links;

        Analysis(
            String label,
            String report,
            int score,
            int signals,
            int links
        ) {
            this.label = label;
            this.report = report;
            this.score = score;
            this.signals = signals;
            this.links = links;
        }
    }
}
