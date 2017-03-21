package cn.comassistant.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

public class Welcome extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		TextView clink = (TextView) findViewById(R.id.Clink);
   this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		   WindowManager.LayoutParams.FLAG_FULLSCREEN);//ÊµÏÖÈ«ÆÁ
   clink.setOnClickListener(new OnClickListener() {
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent= new Intent(Welcome.this,MainActivity.class);
		startActivity(intent);
		finish();
		
	}
});
	}

}
