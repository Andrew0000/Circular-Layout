package crocodile8000.circularlayout.lib;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Layout ��� ����������� �������� ��������� View (���� ������ ��������+1 ������ ������ CircularLayout)
 */
public class CircularLayout extends RelativeLayout {
	
	public static String logTag = "CircularLayout";
	
	private List<ViewGroup> itemList = new ArrayList<ViewGroup>();
	private int w, h;
	private int itemW, itemH;
	private int mPadding = 5 ;
	private int lastXtouch, currXonStartTouch , startTouchX;
	private boolean needCircular, canScroll;
	public float ItemWRatio = 1.25f;
	
	private int stepSaveSpeedCnt =5;
	private int[] lastStepsSpeed = new int[stepSaveSpeedCnt];
	
	private long lastUpdTime;
	private CircularViewClickListener listener;
	
	private boolean isTouching;
	
	private int currItem;
	private Paint paint;
	
	
	
	public CircularLayout(Context context) {
		super(context);
	}

	public CircularLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CircularLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout( changed,  l,  t,  r,  b);
		init();
	}

	
	
	
	private void init(){
		lastUpdTime = System.currentTimeMillis();
		
		initPaint();
		
		removeAllViews();
		w = getWidth();
		h = getHeight();
		itemH = h/10*9;
		itemW = (int) (itemH*ItemWRatio);
		
		int totalViewsLen = (itemW+mPadding)*itemList.size();
		if (totalViewsLen < w) canScroll = false;
		else canScroll = true;
		if (totalViewsLen-(itemW+mPadding)>w) needCircular = true;
		else needCircular = false;
		int mostLeftX = w;
		
		for (int i=0; i<itemList.size(); i++){
			this.addView(itemList.get(i));

			int left = currXonStartTouch+i*itemW+ i*mPadding + mPadding;
			
			if (needCircular){
				
				if (left+itemW+mPadding < 0) 
					while(left+itemW+mPadding<0) 
						left += totalViewsLen;
				
				if (left-mPadding>w) 
					while(left-mPadding > w) 
						left -= totalViewsLen;
				
				// ������� ����� �������� ����� ��� ����� ������� � ������ ���� � ��������� ����������
				if (Math.abs(left-itemW/4)<mostLeftX){
					mostLeftX = Math.abs(left-itemW/4);
					currItem = i;
				}
				
			}else{
				if (currXonStartTouch > 0) currXonStartTouch=0;
				else if (currXonStartTouch < w -totalViewsLen -mPadding) currXonStartTouch= w -totalViewsLen -mPadding;
				left = currXonStartTouch+i*itemW+ i*mPadding + mPadding;
			}
			
			itemList.get(i).layout(left, mPadding, left+itemW, itemH);
		}
		Log.i(logTag, "currItem: "+currItem+ "   mostLeftX: "+mostLeftX);
	}
	
	
	
	
	
	private void initPaint(){
		if (paint==null){
			paint = new Paint();
			paint.setColor(Color.BLUE);
			paint.setAntiAlias(true);
		}
	}
	
	
	
	
	public void onDraw(Canvas canv){
		super.onDraw(canv);
		
		final int speedMult = 10;
		int slideSpeed = getSlideSpeed()*speedMult;
		if (slideSpeed != 0 && !isTouching){
			if (slideSpeed>0){
				slideSpeed--;
			}
			else{
				slideSpeed++;
			}
			updLastStepsSpeed(slideSpeed/speedMult);
			currXonStartTouch+=slideSpeed/speedMult;
			init();
		}
		drawBottomCircles(canv);
	}
	
	
	
	
	/**
	 * ���������� ����� ����� � ���������� �������� �����
	 */
	private void drawBottomCircles(Canvas canv){
		if (!needCircular) return;
		int pad = w / 25;
		int startX = w/2 - pad*itemList.size()/2;
		int y = h - (h/10) + (h/10)/2;
		for (int i=0; i<itemList.size(); i++){
			canv.drawCircle(startX+ pad*i, y, currItem==i? 6 : 3 , paint);
		}
	}
	
	
	
	
	public void addItem(Context context, ViewGroup view){
		view.setTag(itemList.size());
		itemList.add(view);
		init();
	}
	
	

	
	
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		Log.d(logTag, "onInterceptTouchEvent");
		if (listener!=null) listener.onClick(""+event.getAction());
		
		if ( canScroll && event.getAction() == MotionEvent.ACTION_DOWN) {
			startTouch(event);
			return false;
		}
		
		if ( canScroll && event.getAction() == MotionEvent.ACTION_MOVE) return true;
		
	    return super.onInterceptTouchEvent(event);
	}
	

	
	private void startTouch(MotionEvent event){
		startTouchX = (int)event.getX();
		lastXtouch = startTouchX;
		isTouching = true;
	}
	
	
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
		if ( canScroll && event.getAction() == MotionEvent.ACTION_DOWN) {
			startTouch(event);
		}
    	
		else if ( canScroll && event.getAction() == MotionEvent.ACTION_MOVE) {
			currXonStartTouch += ( event.getX() - lastXtouch );
			updLastStepsSpeed( (int) (event.getX() - lastXtouch));
			lastXtouch = (int)event.getX() ;
			//TODO ���������� �� �������� ��� �� ��������� ��������
			if (System.currentTimeMillis() - lastUpdTime >30) init();
		}
    	
		else if ( canScroll && event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
			Log.i(logTag, "currXonStartTouch: " +currXonStartTouch+ "  speed: "+getSlideSpeed());
			init();
			isTouching = false;
		}
    	
		return true;
    }
    
    
    
    
    /**
     * �������� ������ ��������� ��������� ��������
     */
    private void updLastStepsSpeed(int lastXSpeed){
    	for (int i =0; i< lastStepsSpeed.length-1; i++){
    		lastStepsSpeed[i] = lastStepsSpeed[i+1];
    	}
    	lastStepsSpeed[lastStepsSpeed.length-1] = lastXSpeed;
    }
    
    
    
    /**
     * ����� ������� �������� ��������� ����� ���������
     */
    private int getSlideSpeed(){
    	int speed = 0;
    	for (int i =0; i< lastStepsSpeed.length; i++) speed+=lastStepsSpeed[i];
    	speed = speed/lastStepsSpeed.length;
    	return speed;
    }
	
	
    
    

}
