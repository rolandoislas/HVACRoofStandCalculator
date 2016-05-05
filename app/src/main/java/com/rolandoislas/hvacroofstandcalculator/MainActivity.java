package com.rolandoislas.hvacroofstandcalculator;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.renderscript.Double2;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private EditText editTextFrame;
    private EditText editTextLeg;
    private EditText editTextSlope;
    private TextView output;
    private Canvas canvas;
    private DisplayMetrics metrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create canvas
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Bitmap bg =  Bitmap.createBitmap(metrics.widthPixels, metrics.widthPixels, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bg);
        canvas.drawColor(Color.WHITE);
        LinearLayout ll = (LinearLayout) findViewById(R.id.canvasContainer);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            ll.setBackgroundDrawable(new BitmapDrawable(this.getResources(), bg));
        else
            ll.setBackground(new BitmapDrawable(this.getResources(), bg));
        // Text update listener
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                calculate();
            }
        };
        editTextFrame = (EditText) findViewById(R.id.editTextFrame);
        editTextLeg = (EditText) findViewById(R.id.editTextLeg);
        editTextSlope = (EditText) findViewById(R.id.editTextSlope);
        output = (TextView) findViewById(R.id.textViewOutput);
        editTextFrame.addTextChangedListener(textWatcher);
        editTextLeg.addTextChangedListener(textWatcher);
        editTextSlope.addTextChangedListener(textWatcher);
    }

    private void calculate() {
        if (editTextFrame.getText().toString().equals("") || editTextLeg.getText().toString().equals("") ||
                editTextSlope.getText().toString().equals("") || editTextFrame.getText().toString().equals(".") ||
                editTextLeg.getText().toString().equals(".") || editTextSlope.getText().toString().equals(".")) {
            output.setText("");
            canvas.drawColor(Color.WHITE);
            return;
        }
        double frame = Double.parseDouble(editTextFrame.getText().toString());
        double shortLeg = Double.parseDouble(editTextLeg.getText().toString());
        double slope = Double.parseDouble(editTextSlope.getText().toString());
        if (slope > 45 || slope < 1) {
            showSlopeOutOfBoundsDialog();
            editTextSlope.setText("");
            return;
        }
        slope = Math.abs(Math.tan(Math.toRadians(slope)));
        double longLeg = Math.abs(-slope * frame - shortLeg);
        DecimalFormat df = new DecimalFormat("#.###");
        output.setText(getString(R.string.output, df.format(longLeg)));
        drawDiagram(frame, shortLeg, slope, longLeg);
    }

    private void drawDiagram(double frame, double shortLeg, double slope, double longLeg) {
        // Color
        Paint brown = new Paint();
        brown.setColor(Color.rgb(145, 115, 67));
        brown.setStrokeWidth(10);
        Paint gray = new Paint();
        gray.setColor(Color.GRAY);
        gray.setStrokeWidth(10);
        Paint yellow = new Paint();
        yellow.setColor(Color.rgb(237, 234, 206));
        yellow.setStrokeWidth(10);
        Paint black = new Paint();
        black.setColor(Color.BLACK);
        black.setTextSize((float) (metrics.widthPixels * .05));
        // Scale
        double scale = metrics.widthPixels * .8 / (frame > longLeg ? frame : longLeg);
        frame *= scale;
        shortLeg *= scale;
        longLeg *= scale;
        float margin = (float) (metrics.widthPixels * .1);
        // Reset
        canvas.drawColor(Color.WHITE);
        // Roof
        canvas.drawLine(0, (float) (shortLeg + margin), metrics.widthPixels,
                (float) (slope * metrics.widthPixels + shortLeg + margin), brown);
        // Frame
        canvas.drawLine(margin, margin, (float) (margin + frame), margin, gray);
        canvas.drawText(String.valueOf(frame / scale), (float) (margin + frame / 2), margin, black);
        // Short leg
        canvas.drawLine(margin, margin, margin, (float) (margin + shortLeg), gray);
        canvas.drawText(String.valueOf(shortLeg / scale), margin, (float) (margin + shortLeg / 2), black);
        // Long leg
        canvas.drawLine((float) (margin + frame), margin, (float) (margin + frame), (float) (margin + longLeg), gray);
        canvas.drawText(String.valueOf(longLeg / scale), (float) (margin + frame), (float) (margin + longLeg / 2), black);
    }

    private void showSlopeOutOfBoundsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setMessage(R.string.slopeBoundsError).setTitle(R.string.textRoof);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
