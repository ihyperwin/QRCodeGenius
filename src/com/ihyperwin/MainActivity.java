package com.ihyperwin;

import com.ihyperwin.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
  
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        Button scanQRCodeButton = (Button) this.findViewById(R.id.btn_scan_qrcode);
        scanQRCodeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent scanQRCodeIntent = new Intent(MainActivity.this,ScanQRCodeActivity.class);
				startActivityForResult(scanQRCodeIntent, 0);
			}
		});
        
        
        Button genarateQRCodeButton = (Button) this.findViewById(R.id.btn_generate_qrcode);
        genarateQRCodeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent generateQRCodeIntent = new Intent(MainActivity.this,GenerateQRCodeActivity.class);
				startActivityForResult(generateQRCodeIntent, 0);
			}
		});
        
    }

}