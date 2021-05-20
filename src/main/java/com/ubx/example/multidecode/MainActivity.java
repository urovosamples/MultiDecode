package com.ubx.example.multidecode;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();

    TextView sym_nametv;
    private TextView mBarcodeResult;
    private ScanManager mScanManager;
    int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
    String[] action_value_buf = new String[]{ScanManager.ACTION_DECODE, ScanManager.BARCODE_STRING_TAG};
    int[] multiConfigbuf = new int[]{PropertyID.MULTI_DECODE_MODE, PropertyID.FULL_READ_MODE, PropertyID.BAR_CODES_TO_READ};
    int[] multiConfigval = new int[3];
    private class AsyncDataUpdate extends AsyncTask<String, Void, String> {

        private ArrayList<TableRow> rows;
        AsyncDataUpdate(ArrayList<TableRow> rows){
            this.rows = rows;
        }

        @Override
        protected String doInBackground(String... params) {

            return params[0];
        }

        @Override
        protected void onPostExecute(String decodeType) {
            //sym_nametv.setText(decodeType);
            TableLayout tl = (TableLayout) findViewById(R.id.tableView);

            tl.removeAllViews();
            for (TableRow row : rows) {
                tl.addView(row);
            }
        }
    }
    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            mBarcodeResult.setText("");
            sym_nametv.setText("");
            String[] datas = intent.getStringArrayExtra(action_value_buf[1]+"Lists");
            if(datas != null) {
                int[] lengthList = intent.getIntArrayExtra("lengthLists");
                String[] hexdatas = intent.getStringArrayExtra(action_value_buf[1]+"HexLists");
                ArrayList<TableRow> rows = new ArrayList<TableRow>();

                // Adding header row
                TableRow row= new TableRow(MainActivity.this);
                row.setBackgroundColor(Color.BLACK);
                row.setPadding(1, 1, 1, 1);

                TableRow.LayoutParams llp = new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.MATCH_PARENT);
                llp.setMargins(0, 0, 2, 0);

                TextView lengthText = new TextView(MainActivity.this);
                lengthText.setPadding(5, 5, 5, 5);
                lengthText.setLayoutParams(llp);
                lengthText.setBackgroundColor(Color.WHITE);
                lengthText.setText("Length");
                row.addView(lengthText);
                TextView keyText = new TextView(MainActivity.this);
                keyText.setPadding(5, 5, 5, 5);
                keyText.setLayoutParams(llp);
                keyText.setBackgroundColor(Color.WHITE);
                keyText.setText("Data");
                row.addView(keyText);

                TextView valueText = new TextView(MainActivity.this);
                valueText.setPadding(5, 5, 5, 5);
                valueText.setBackgroundColor(Color.WHITE);
                valueText.setText("HEX Data");
                row.addView(valueText);

                rows.add(row);
                for(int i =0; i < datas.length; i++) {
                    row= new TableRow(MainActivity.this);
                    row.setBackgroundColor(Color.BLACK);
                    row.setPadding(1, 1, 1, 1);
                    lengthText = new TextView(MainActivity.this);
                    lengthText.setPadding(5, 5, 5, 5);
                    lengthText.setLayoutParams(llp);
                    lengthText.setBackgroundColor(Color.WHITE);
                    lengthText.setText(""+lengthList[i]);
                    row.addView(lengthText);

                    keyText = new TextView(MainActivity.this);
                    keyText.setPadding(5, 5, 5, 5);
                    keyText.setLayoutParams(llp);
                    keyText.setBackgroundColor(Color.WHITE);
                    keyText.setText(""+datas[i]);
                    row.addView(keyText);

                    valueText = new TextView(MainActivity.this);
                    valueText.setPadding(5, 5, 5, 5);
                    valueText.setBackgroundColor(Color.WHITE);
                    valueText.setLayoutParams(llp);
                    valueText.setText(""+hexdatas[i]);
                    row.addView(valueText);

                    rows.add(row);
                }
                new AsyncDataUpdate(rows).execute("");
            } else {
                byte[] barcodeData = intent.getByteArrayExtra("barcode");
                int dataLength = intent.getIntExtra("length", 0);
                sym_nametv.setText("Data:\n" + (new String(barcodeData, 0, dataLength)));
            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if(mScanManager != null) {
            mScanManager.stopDecode();
        }
        unregisterReceiver(mScanReceiver);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mScanManager = new ScanManager();
        action_value_buf = mScanManager.getParameterString(idbuf);
        IntentFilter filter = new IntentFilter();
        filter.addAction(action_value_buf[0]);
        registerReceiver(mScanReceiver, filter);
        int[] id = new int[]{PropertyID.WEDGE_KEYBOARD_ENABLE,PropertyID.SEND_GOOD_READ_BEEP_ENABLE};
        int[] val = mScanManager.getParameterInts(id);
        if(val[0] == 1) {
            val[0] = 0;
            val[1] = 1;
            mScanManager.setPropertyInts(id, val);
        }
        multiConfigval = mScanManager.getParameterInts(multiConfigbuf);
        multiConfigval[0] = 1;//0 disable multi decode, 1 enable  multi decode
        /**
         *
         *Select when to generate a decode event to the calling application when Multi Decode Mode is enabled.
         * • 0 - Generate a decode event after one or more bar codes are decoded.
         * • *1 - Only generate the callback to app when at least the number of bar codes set in
         * Bar Codes to Read are decoded.
         */
        multiConfigval[1] = 1;
        //This parameter sets the number of bar codes to read when Multi Decode Mode is enabled. The range is 1 to
        //     * 10 bar codes. The default is 1.
        multiConfigval[2] = 4;
        mScanManager.setParameterInts(multiConfigbuf, multiConfigval);
        sym_nametv = (TextView) findViewById(R.id.sym_name);
        mBarcodeResult = (TextView) findViewById(R.id.barcode_result);
    }
}