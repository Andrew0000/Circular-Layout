package crocodile8000.circularlayout;

import crocodile8000.circularlayout.lib.CircularLayout;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		addingItems();
	}

	

	// пример постепенной подгрузки картинок в CircularLayout
	private void addingItems(){
		
		final Handler handler = new Handler();
		
		final CircularLayout cl = (CircularLayout) findViewById(R.id.CircularLayout);

		new Thread(new Runnable() {
			public void run() {
				for (int a=0; a<3; a++){
					final int i = a;
					final ViewGroup v1 = (ViewGroup) getLayoutInflater().inflate(R.layout.some_item, null);
					final TextView tv = (TextView) v1.findViewById(R.id.tv1);
					final ImageView iv = (ImageView) v1.findViewById(R.id.imageView1);
					final ProgressBar pb = (ProgressBar) v1.findViewById(R.id.progressBar1);
					
					handler.post(new Runnable() { public void run() {
						tv.setText("item: "+i);
						tv.setTag("item: "+i);
						v1.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								Toast.makeText(getApplicationContext(), "Click on "+tv.getTag(), Toast.LENGTH_SHORT).show();
							}
						});
						cl.addItem(getApplicationContext(), v1);
					} });
					
							
					try {Thread.sleep(2000); } catch (InterruptedException e) {}
					
					handler.post(new Runnable() { public void run() {
						pb.setVisibility(View.GONE);
						tv.setVisibility(View.GONE);
						iv.setImageDrawable(getResources().getDrawable(getNeededPicId(i)));
					} });
				}
			}
		}).start();
		
	}
	
	
	
	private int getNeededPicId(int i){
		int drawableId = R.drawable.pic1;
		switch (i){
		case 1:
			drawableId = R.drawable.pic2;
			break;
		case 2:
			drawableId = R.drawable.pic3;
			break;
		}
		return drawableId;
	}
	

}
