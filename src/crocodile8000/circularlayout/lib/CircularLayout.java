package crocodile8000.circularlayout.lib;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


/**
 * Layout для зацикленной промотки вложенных View (если ширина вложений+1 больше ширины CircularLayout)
 */
public class CircularLayout extends RelativeLayout {
	
	public static boolean needLog;
	public static String logTag = "CircularLayout";
	
	private List<ViewGroup> itemList = new ArrayList<ViewGroup>();
	
	private int w, h;
	private int itemW, itemH;
	private int mPadding = 5 ;
	private int lastXtouch, currXonStartTouch , startTouchX;
	private boolean needCircular, canScroll;
	
	/** соотношение ширины одного итема (ViewGroup) к его высоте */
	public float ItemWRatio = 1.25f;
	
	// запоминание последних скоростей промотки пальцем
	private int stepSaveSpeedCnt = 5;
	private int[] lastStepsSpeed = new int[stepSaveSpeedCnt];
	
	/** нужно ли продолжение промотки после отпускания */
	public boolean needSpeed = true;
	
	private long lastUpdTime;
	
	private boolean isTouching;
	
	private int currItem;
	private Paint paint, paintShadow;
	
	
	
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

	
	
	/**
	 * Инициализация параметров и обновление позиций итемов
	 */
	private void init(){
		lastUpdTime = System.currentTimeMillis();

		if (Build.VERSION.SDK_INT >=14) requestDisallowInterceptTouchEvent(true);
		
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
				
				// считаем номер текущего итема как самый близкий к левому краю с небольшой коррекцией
				if (Math.abs(left-itemW/4)<mostLeftX){
					mostLeftX = Math.abs(left-itemW/4);
					currItem = i;
				}
				
			}else if(canScroll){
				if (currXonStartTouch > 0) currXonStartTouch=0;
				else if (currXonStartTouch < w -totalViewsLen -mPadding) currXonStartTouch= w -totalViewsLen -mPadding;
				left = currXonStartTouch+i*itemW+ i*mPadding + mPadding;
			}
			
			itemList.get(i).layout(left, mPadding, left+itemW, itemH);
		}
		if (needLog) Log.i(logTag, "currItem: "+currItem+ "   mostLeftX: "+mostLeftX);
	}
	
	
	
	
	
	private void initPaint(){
		if (paint==null){
			paint = new Paint();
			paint.setColor(Color.BLUE);
			paint.setAntiAlias(true);
		}
		if (paintShadow==null){
			paintShadow = new Paint();
			paintShadow.setColor(Color.argb(50, 0, 0, 0));
			paintShadow.setAntiAlias(true);
		}
	}
	
	
	
	
	public void onDraw(Canvas canv){
		super.onDraw(canv);

		// промотка по инерции после отпускания (если нужна)
		if (needSpeed){
			final int speedMult = 10;
			int slideSpeed = getSlideSpeed()*speedMult;
			if (slideSpeed != 0 && !isTouching){
				
				final int maxSpeed = 250;
				if (slideSpeed> maxSpeed) slideSpeed = maxSpeed;
				if (slideSpeed< -maxSpeed) slideSpeed = -maxSpeed;
				
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
		}
		drawBottomCircles(canv);
	}
	
	
	
	
	/**
	 * Прорисовка снизу точек с выделением текущего итема
	 */
	private void drawBottomCircles(Canvas canv){
		if (!needCircular) return;
		int pad = w / 25;
		int startX = w/2 - pad*itemList.size()/2;
		int y = h - (h/10) + (h/10)/2;
		int radius = h/50;
		for (int i=0; i<itemList.size(); i++){
			canv.drawCircle(startX+ pad*i, y, currItem==i? radius*2 : radius , paint);
		}
	}
	
	
	
	/**
	 * Добавление одного итема на layout
	 */
	public void addItem(Context context, ViewGroup view){
		view.setTag(itemList.size());
		itemList.add(view);
		init();
	}
	
	

	
	
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (needLog) Log.d(logTag, "onInterceptTouchEvent");
		
		if ( canScroll && event.getAction() == MotionEvent.ACTION_DOWN) {
			startTouch(event);
			return false;
		}
		
		if ( canScroll && event.getAction() == MotionEvent.ACTION_MOVE) {
			// сглаживание небольших проскальзываний пальца что бы пережать касание потомку, 
			// даже если, например был сдвиг касания на 1-2 пикселя
			int pathX = Math.abs( (int)event.getX() - startTouchX ) ;
			if (pathX>h/10) return true;
		}
		
	    return false;
	}
	

	
	private void startTouch(MotionEvent event){
		startTouchX = (int)event.getX();
		lastXtouch = startTouchX;
		isTouching = true;
		clearLastStepsSpeed();
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
			//TODO переделать на анимации что бы уменьшить нагрузку
			if (System.currentTimeMillis() - lastUpdTime >30) init();
		}
    	
		else if ( canScroll && event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
			if (needLog) Log.i(logTag, "currXonStartTouch: " +currXonStartTouch+ "  speed: "+getSlideSpeed());
			init();
			isTouching = false;
		}
    	
		return true;
    }
    
    
    
    
    /**
     * Обнулить массив последних скоростей промотки
     */
    private void clearLastStepsSpeed(){
    	for (int i =0; i< lastStepsSpeed.length; i++){
    		lastStepsSpeed[i] = 0;
    	}
    }
    
    
    /**
     * Обновить массив последних скоростей промотки
     */
    private void updLastStepsSpeed(int lastXSpeed){
    	for (int i =0; i< lastStepsSpeed.length-1; i++){
    		lastStepsSpeed[i] = lastStepsSpeed[i+1];
    	}
    	lastStepsSpeed[lastStepsSpeed.length-1] = lastXSpeed;
    }
    
    
    /**
     * Взять среднюю скорость последних шагов прокрутки
     */
    private int getSlideSpeed(){
    	int speed = 0;
    	for (int i =0; i< lastStepsSpeed.length; i++) speed+=lastStepsSpeed[i];
    	speed = speed/lastStepsSpeed.length;
    	return speed;
    }
	
	
    
    

}
