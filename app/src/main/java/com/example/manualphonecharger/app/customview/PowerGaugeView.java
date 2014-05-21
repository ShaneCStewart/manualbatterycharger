package com.example.manualphonecharger.app.customview;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.manualphonecharger.app.network.UploadMotionTimeService;
import com.example.manualphonecharger.app.util.MotionData;

import java.util.HashMap;

public final class PowerGaugeView extends View implements SensorEventListener {

    private static final String SUPER_STATE_TAG = "superState";
    private static final String HAND_INITIALIZED_TAG = "handInitialized";
    private static final String HAND_POSITION_TAG = "handPosition";
    private static final String HAND_TARGET_TAG = "handTarget";
    private static final String TAG = "PowerGaugeView";

    public static final String CURRENT_MOTION_STATE_TAG = "currentMotionState";
    public static final String RATE_OF_MOTION_STATE = "rateOfMotionState";
    public static final String TOTAL_MOTION_STATE = "totalMotionState";
    public static final String TIME_MOTION_TAG = "timeMotionTag";

    private static final int MAX_POWER_LEVEL = 18000;
    private static final int HAND_POSITION_MIN_DEGREES = -90;
    private static final int HAND_POSITION_MAX_DEGREES = 90;
    private static final int VIEW_PERFERRED_SIZE = 300;
    private long startTimeMili;

    private String mCurrentMotionState;

    private RectF rimRect;
    private RectF faceRect;
    private RectF scaleRect;

    private Paint rimPaint;
    private Paint rimCirclePaint;
    private Paint facePaint;
    private Paint rimShadowPaint;
    private Paint motionValuePaint;
    private Paint handPaint;
    private Path handPath;
    private Paint handScrewPaint;
    private Paint backgroundPaint;
    private Paint mColorCodePaint;

    //200 so the message character limit of 1800 isn't exceeded
    private static final int MAX_MOTION_MAP_SIZE = 200;
    //time elapsed since start paired with each rateOfMotion occurrence
    private HashMap<Integer,Integer> timeMotionMap
            = new HashMap<Integer, Integer>(MAX_MOTION_MAP_SIZE);

    private Path mColorCodePath;

    //**width() and height() =  1f, to allow for easy scaling
    private static final float VIEW_CENTER = .5f;

    private static final float COLOR_SCALE_SIZE = .1f;

    //color scale locations
    private static final float RED_ARCH_START = .01f;
    private static final float RED_ARCH_END = .09f;
    private static final float ORANGE_ARCH_START = .095f;
    private static final float ORANGE_ARCH_END = .175f;
    private static final float BLUE_ARCH_START = .18f;
    private static final float BLUE_ARCH_END = .26f;
    private static final float YELLOW_ARCH_START = .265f;
    private static final float YELLOW_ARCH_END = .345f;
    private static final float GREEN_ARCH_START = .35f;
    private static final float GREEN_ARCH_END = .43f;

    private static final float HAND_START_Y = .62f;
    private static final float HAND_BACK_LEFT_X = .49f;
    private static final float HAND_BACK_Y = .613f;
    private static final float HAND_TOP_LEFT_X = .498f;
    private static final float HAND_TOP_Y = .24f;
    private static final float HAND_TOP_RIGHT_X = .502f;
    private static final float HAND_BACK_RIGHT_X = .51f;
    private static final float HAND_CIRCLE_RADIUS = .025f;

    private static final float HAND_SHADOW_RADIUS = .01f;
    private static final float HAND_SHADOW_DX = -.005f;
    private static final float HAND_SHADOW_DY = -.005f;
    private static final int HAND_SHADOW_COLOR = 0x7f000000;

    private static final int GRAY = Color.argb(0x4f, 0x33, 0x36, 0x33);
    private static final int OFF_WHITE = Color.rgb(0xf0, 0xf5, 0xf0);
    private static final int LIGHT_GRAY = Color.rgb(0x30, 0x31, 0x30);

    private static final int BLACK_VOID = 0x00000000;
    private static final int LIGHT_BLACK = 0x00000500;
    private static final int BOLD_GRAY = 0x50000500;
    private static final float RIM_SHADOW_BLACK_VOID_POSITION = .96f;
    private static final float RIM_SHADOW_LIGHT_BLACK_POSITION = .96f;
    private static final float RIM_SHADOW_GRAY_POSITION = .99f;

    private static final float RIM_RECT_LEFT = .1f;
    private static final float RIM_RECT_TOP = .1f;
    private static final float RIM_RECT_RIGHT = .9f;
    private static final float RIM_RECT_BOTTOM = .9f;

    private static final float RIM_PAINT_GRADIENT_X0 = .4f;
    private static final float RIM_PAINT_GRADIENT_Y0 = .0f;
    private static final float RIM_PAINT_GRADIENT_X1 = .6f;
    private static final float RIM_PAINT_GRADIENT_Y1 = .1f;

    private static final float COLOR_SCALE_START_DEGREES = 0;
    private static final float COLOR_SCALE_END_DEGREES = 180f;
    private static final float MOTION_HAND_CIRCLE_RADIUS = .01f;
    private static final float MOTION_TEXT_Y = .7f;
    private static final float RIM_CIRCLE_WIDTH = .005f;
    private static final float MOTION_RATE_TEXT_SIZE = .1f;

    //color scale, repeated values create white gaps between colors
    private float[] mColorCodePositions = {RED_ARCH_START, RED_ARCH_END,RED_ARCH_END,
            ORANGE_ARCH_START, ORANGE_ARCH_START, ORANGE_ARCH_END, ORANGE_ARCH_END,
            BLUE_ARCH_START, BLUE_ARCH_START, BLUE_ARCH_END, BLUE_ARCH_END,
            YELLOW_ARCH_START,YELLOW_ARCH_START, YELLOW_ARCH_END,YELLOW_ARCH_END,
            GREEN_ARCH_START, GREEN_ARCH_START, GREEN_ARCH_END};

    private float mAccelMaxRange;
    private float handPosition;
    private float handTarget;

    private Bitmap background;

    private boolean handInitialized = false;
    private boolean mIsFirstData = true;

    private MotionData mMotionData;

    private SensorManager mSensorMgr;

    private Context mContext;

    //stores color sequences for the color scale
    private int mColorCodeColors[] = new int[18];
    //previous x,y,z for accelerometer
    private int mLastX;
    private int mLastY;
    private int mLastZ;

    public PowerGaugeView(Context context) {
        super(context);
        init();
    }

    public PowerGaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PowerGaugeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachToSensor();
    }

    @Override
    protected void onDetachedFromWindow() {
        detachFromSensor();
        if(timeMotionMap != null && timeMotionMap.size() > 0) {
            sendPubnubMessage();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Parcelable superState = bundle.getParcelable(SUPER_STATE_TAG);
        super.onRestoreInstanceState(superState);
        handInitialized = bundle.getBoolean(HAND_INITIALIZED_TAG);
        handPosition = bundle.getFloat(HAND_POSITION_TAG);
        handTarget = bundle.getFloat(HAND_TARGET_TAG);
        mCurrentMotionState = bundle.getString(CURRENT_MOTION_STATE_TAG);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle state = new Bundle();
        state.putParcelable(SUPER_STATE_TAG, superState);
        state.putBoolean(HAND_INITIALIZED_TAG, handInitialized);
        state.putFloat(HAND_POSITION_TAG, handPosition);
        state.putFloat(HAND_TARGET_TAG, handTarget);
        state.putString(CURRENT_MOTION_STATE_TAG, mCurrentMotionState);
        return state;
    }

    private void init() {
        setLayerType(PowerGaugeView.LAYER_TYPE_SOFTWARE, null);
        mMotionData = MotionData.getInstance();
        mContext = getContext();
        handPosition = HAND_POSITION_MIN_DEGREES;
        initDrawingTools();
    }

    public void attachToSensor() {
        mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer =  mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccelMaxRange = accelerometer.getMaximumRange();
        mSensorMgr.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_GAME);
    }


    public void detachFromSensor() {
        mSensorMgr.unregisterListener(this);
    }

    private void initDrawingTools() {
        initRimRectPaint();
        initRimCirclePaint();
        initFaceRect();
        initFacePaint();
        initRimShadowPaint();
        initScaleRect();
        initHandPaint();
        initMotionValuePaint();
        initHandPath();
        initHandScrewPaint();
        initColorCodeColors();
        initColorCodePathPaint();
        initBackgroundPaint();
        initStartTime();
    }

    private void initStartTime() {
        startTimeMili = System.currentTimeMillis();
    }

    private void initBackgroundPaint() {
        backgroundPaint = new Paint();
        backgroundPaint.setFilterBitmap(true);
    }

    private void initColorCodePathPaint() {
        mColorCodePath = new Path();
        mColorCodePaint = new Paint();
        mColorCodePaint.setStyle(Paint.Style.STROKE);
        mColorCodePaint.setStrokeCap(Paint.Cap.BUTT);
        mColorCodePaint.setStrokeWidth(COLOR_SCALE_SIZE);
        mColorCodePaint.setAntiAlias(true);
    }

    private void initColorCodeColors() {
        Resources res = getResources();
        int red = res.getColor(android.R.color.holo_red_dark);
        int orange = res.getColor(android.R.color.holo_orange_dark);
        int blue = res.getColor(android.R.color.holo_blue_bright);
        int yellow = Color.YELLOW;
        int green = res.getColor(android.R.color.holo_green_light);
        int transparent = res.getColor(android.R.color.transparent);

        mColorCodeColors[0] = mColorCodeColors[1] = red;
        mColorCodeColors[2] = mColorCodeColors[3] = transparent;
        mColorCodeColors[4] = mColorCodeColors[5] = orange;
        mColorCodeColors[6] = mColorCodeColors[7] = transparent;
        mColorCodeColors[8] = mColorCodeColors[9] = blue;
        mColorCodeColors[10] = mColorCodeColors[11] = transparent;
        mColorCodeColors[12] = mColorCodeColors[13] = yellow;
        mColorCodeColors[14] = mColorCodeColors[15] = transparent;
        mColorCodeColors[16] = mColorCodeColors[17] = green;
    }

    private void initHandScrewPaint() {
        handScrewPaint = new Paint();
        handScrewPaint.setAntiAlias(true);
        handScrewPaint.setColor(Color.BLACK);
        handScrewPaint.setStyle(Paint.Style.FILL);
    }

    private void initHandPath() {
        handPath = new Path();
        handPath.moveTo(VIEW_CENTER, HAND_START_Y);
        handPath.lineTo(HAND_BACK_LEFT_X, HAND_BACK_Y);
        handPath.lineTo(HAND_TOP_LEFT_X, HAND_TOP_Y);
        handPath.lineTo(HAND_TOP_RIGHT_X,HAND_TOP_Y);
        handPath.lineTo(HAND_BACK_RIGHT_X, HAND_BACK_Y);
        handPath.lineTo(VIEW_CENTER, HAND_START_Y);
        handPath.addCircle(VIEW_CENTER, VIEW_CENTER, HAND_CIRCLE_RADIUS, Path.Direction.CW);
    }

    private void initMotionValuePaint() {

        motionValuePaint = new Paint();
        motionValuePaint.setTextSize(MOTION_RATE_TEXT_SIZE);
        motionValuePaint.setColor(Color.BLACK);
        motionValuePaint.setLinearText(true);
    }

    private void initHandPaint() {
        handPaint = new Paint();
        handPaint.setAntiAlias(true);
        handPaint.setColor(Color.BLACK);
        handPaint.setShadowLayer(HAND_SHADOW_RADIUS, HAND_SHADOW_DX,
                HAND_SHADOW_DY,HAND_SHADOW_COLOR);
        handPaint.setStyle(Paint.Style.FILL);
    }

    private void initScaleRect() {
        float scalePosition = 0.10f;
        scaleRect = new RectF();
        scaleRect.set(faceRect.left + scalePosition, faceRect.top + scalePosition,
                faceRect.right - scalePosition, faceRect.bottom - scalePosition);
    }

    private void initRimShadowPaint() {
        rimShadowPaint = new Paint();
        float rimShadowRadius = faceRect.width() / 2.0f;


        rimShadowPaint.setShader(new RadialGradient(VIEW_CENTER, VIEW_CENTER, rimShadowRadius,
                new int[]{BLACK_VOID, LIGHT_BLACK, BOLD_GRAY},
                new float[]{RIM_SHADOW_BLACK_VOID_POSITION,
                        RIM_SHADOW_LIGHT_BLACK_POSITION,
                        RIM_SHADOW_GRAY_POSITION},
                Shader.TileMode.MIRROR));
        rimShadowPaint.setStyle(Paint.Style.FILL);
    }

    private void initFacePaint() {
        facePaint = new Paint();
        facePaint.setFilterBitmap(true);
        facePaint.setColor(Color.WHITE);
    }

    private void initFaceRect() {
        float rimSize = 0.02f;
        faceRect = new RectF();
        faceRect.set(rimRect.left + rimSize, rimRect.top + rimSize,
                rimRect.right - rimSize, rimRect.bottom - rimSize);
    }

    private void initRimCirclePaint() {
        rimCirclePaint = new Paint();
        rimCirclePaint.setAntiAlias(true);
        rimCirclePaint.setStyle(Paint.Style.STROKE);
        rimCirclePaint.setColor(GRAY);
        rimCirclePaint.setStrokeWidth(RIM_CIRCLE_WIDTH);
    }

    private void initRimRectPaint() {
        rimRect = new RectF(RIM_RECT_LEFT, RIM_RECT_TOP,
                RIM_RECT_RIGHT, RIM_RECT_BOTTOM);
        rimPaint = new Paint();
        rimPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        rimPaint.setShader(new LinearGradient(RIM_PAINT_GRADIENT_X0,
                RIM_PAINT_GRADIENT_Y0, RIM_PAINT_GRADIENT_X1, RIM_PAINT_GRADIENT_Y1,
                OFF_WHITE, LIGHT_GRAY,Shader.TileMode.CLAMP));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int chosenWidth = chooseDimension(widthMode, widthSize);
        int chosenHeight = chooseDimension(heightMode, heightSize);
        int chosenDimension = Math.min(chosenWidth, chosenHeight);
        setMeasuredDimension(chosenDimension, chosenDimension);
    }

    private int chooseDimension(int mode, int size) {
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
            return size;
        } else {
            return getPreferredSize();
        }
    }

    private int getPreferredSize() {
        return VIEW_PERFERRED_SIZE;
    }

    private void drawRim(Canvas canvas) {
        canvas.drawOval(rimRect, rimPaint);
        canvas.drawOval(rimRect, rimCirclePaint);
    }

    private void drawFace(Canvas canvas) {
        canvas.drawOval(faceRect, facePaint);
        canvas.drawOval(faceRect, rimCirclePaint);
        canvas.drawOval(faceRect, rimShadowPaint);
    }

    private void drawColorCode(Canvas canvas) {
        SweepGradient colorCodeSweepGrade = new SweepGradient(scaleRect.centerX(),
                scaleRect.centerY(), mColorCodeColors, mColorCodePositions);
        mColorCodePaint.setShader(colorCodeSweepGrade);
        mColorCodePath.reset();
        mColorCodePath.arcTo(new RectF(scaleRect),
                COLOR_SCALE_START_DEGREES, COLOR_SCALE_END_DEGREES, true);
        canvas.save();
        canvas.rotate(180, scaleRect.centerX(), scaleRect.centerY());
        canvas.drawPath(mColorCodePath, mColorCodePaint);
        canvas.restore();
    }

    private void drawMotionHand(Canvas canvas) {

        if (handInitialized) {
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.rotate(handTarget, VIEW_CENTER, VIEW_CENTER);
            canvas.drawPath(handPath, handPaint);
            canvas.restore();
            canvas.drawCircle(VIEW_CENTER, VIEW_CENTER,
                    MOTION_HAND_CIRCLE_RADIUS, handScrewPaint);
        }
    }

    private void drawBackground(Canvas canvas) {
        if (background == null) {
            Log.d(TAG, "Background not created");
        } else {
            canvas.drawBitmap(background, 0, 0, backgroundPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        float scale = (float) getWidth();
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(scale, scale);
        drawMotionHand(canvas);
        drawMotionValue(canvas);
        canvas.restore();
    }

    private void drawMotionValue(Canvas canvas) {
        motionValuePaint.setTextAlign(Paint.Align.CENTER);
        String motionValue = getMotionValue();
        canvas.drawText(motionValue, VIEW_CENTER, MOTION_TEXT_Y, motionValuePaint);
    }

    private String getMotionValue() {
        if(mCurrentMotionState.equals(RATE_OF_MOTION_STATE)) {
            return Integer.toString(mMotionData.getRateOfMotion());
        } else{
            int totalMotion = mMotionData.getTotalMotion();
            if(totalMotion < MAX_POWER_LEVEL){
                return totalMotion+"";
            }
            return MAX_POWER_LEVEL+"";
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        regenerateBackground();
    }

    private void regenerateBackground() {
        if (background != null) {
            background.recycle();
        }
        background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas backgroundCanvas = new Canvas(background);
        float scale = (float) getWidth();
        backgroundCanvas.scale(scale, scale);
        drawRim(backgroundCanvas);
        drawFace(backgroundCanvas);
        drawColorCode(backgroundCanvas);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //set each value  accelemoter axis from 0-180
        //(180 for easy math with the half circle range of values)
        int x = (int)((sensorEvent.values[0]/mAccelMaxRange)*180);
        int y = (int)((sensorEvent.values[1]/mAccelMaxRange)*180);
        int z = (int)((sensorEvent.values[2]/mAccelMaxRange)*180);
        int rateOfMotion = setRateOfMotion(x, y, z);
        setLastXYZ(x, y, z);
        storeRateOfMotion(rateOfMotion);
        if(timeMotionMap.size() == MAX_MOTION_MAP_SIZE) {
           sendPubnubMessage();
           timeMotionMap.clear();
        }
        mMotionData.setRateOfMotion(rateOfMotion);
        mMotionData.updateTotalMotion(rateOfMotion);
        if(mCurrentMotionState.equals(RATE_OF_MOTION_STATE)) {
            //rate of motion can be 0-180, hand position can be -90 to 90
            //subtract 90 to convert motion to hand position
            setHandTarget((rateOfMotion - 90));
        } else if(mCurrentMotionState.equals(TOTAL_MOTION_STATE)){
            //dividing by 100 means the total motion must be
            //100 times larger then 180 to reach the max hand position
            setHandTarget((mMotionData.getTotalMotion() / 100) - 90);
        }
    }

    private void storeRateOfMotion(int rateOfMotion) {
        Long currentTimeMili = System.currentTimeMillis();
        int timeSinceStart =(int)(currentTimeMili-startTimeMili);
        timeMotionMap.put(timeSinceStart,rateOfMotion);
    }

    private int setRateOfMotion(int x, int y, int z) {
        int rateOfMotion;
        if(mIsFirstData){
            //rateOfMotion = average of x, y, and z
            rateOfMotion = (x + y + z) / 3;
            mIsFirstData = false;
        } else{
            //rateOfMotion = average of the amount of change in
            //x y and z
            rateOfMotion = (Math.abs(mLastX-x) + Math.abs(mLastY-y)
                    + Math.abs(mLastZ-z)) / 3;
        }
        return rateOfMotion;
    }

    private void sendPubnubMessage() {
            Intent uploadMotionTimeIntent = new Intent(getContext(),
                    UploadMotionTimeService.class);
            uploadMotionTimeIntent.putExtra(TIME_MOTION_TAG, timeMotionMap);
            getContext().startService(uploadMotionTimeIntent);
    }

    private void setLastXYZ(int x, int y, int z) {
        mLastX = x;
        mLastY = y;
        mLastZ = z;
    }

    private void setHandTarget(float motionInDegrees) {
        if (motionInDegrees < HAND_POSITION_MIN_DEGREES) {
            motionInDegrees = HAND_POSITION_MIN_DEGREES;
        } else if (motionInDegrees > HAND_POSITION_MAX_DEGREES) {
            motionInDegrees = HAND_POSITION_MAX_DEGREES;
        }
        handTarget = motionInDegrees;
        handInitialized = true;
        invalidate();
    }

    public void setMotionState(String currentMotionState){
        mCurrentMotionState = currentMotionState;
    }

    public String getMotionState(){
        if(mCurrentMotionState == null){
            mCurrentMotionState = TOTAL_MOTION_STATE;
            return TOTAL_MOTION_STATE;
        }
        return mCurrentMotionState;
    }

    public void flipMotionState(){
        if(mCurrentMotionState.equals(RATE_OF_MOTION_STATE)){
            mCurrentMotionState = TOTAL_MOTION_STATE;
        } else{
            mCurrentMotionState = RATE_OF_MOTION_STATE;
        }
    }
}

