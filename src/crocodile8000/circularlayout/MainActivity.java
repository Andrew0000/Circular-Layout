package crocodile8000.circularlayout;

import crocodile8000.circularlayout.lib.CircularLayout;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		CircularLayout cl = (CircularLayout) findViewById(R.id.CircularLayout);
		
		ViewGroup v1 = (ViewGroup) getLayoutInflater().inflate(R.layout.some_item, null);
		TextView tv1 = (TextView) v1.findViewById(R.id.tv1);
		tv1.setText("1");
		v1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "v1", Toast.LENGTH_SHORT).show();
			}
		});
		
		ViewGroup v2 = (ViewGroup) getLayoutInflater().inflate(R.layout.some_item, null);
		v2.setBackgroundColor(Color.BLUE);
		TextView tv2 = (TextView) v2.findViewById(R.id.tv1);
		tv2.setText("2");
		v2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "v2", Toast.LENGTH_SHORT).show();
			}
		});
		
		ViewGroup v3 = (ViewGroup) getLayoutInflater().inflate(R.layout.some_item, null);
		v3.setBackgroundColor(Color.YELLOW);
		TextView tv3 = (TextView) v3.findViewById(R.id.tv1);
		tv3.setText("3");

		cl.addItem(getApplicationContext(), v1);
		cl.addItem(getApplicationContext(), v2);
		cl.addItem(getApplicationContext(), v3);
		

	}

	

}
