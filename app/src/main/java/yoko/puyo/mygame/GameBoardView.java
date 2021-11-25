package yoko.puyo.mygame;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
public class GameBoardView extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private static final String TAG = "abc";

    //xhdpi(F05J:2倍),xxhdpi(Pixel5:3倍),xxxhdpi(4倍)により変更する必要有
    private static int ROCK_SIZE = 165;
    private static int BALL_SIZE = 100;
    private static int START_WIDTH = 300;
    private static int START_HEIGHT = 300;
    private static int GOAL_WIDTH = 300;
    private static int GOAL_HEIGHT = 300;
    private static int BALL_POS_W = ROCK_SIZE + GOAL_WIDTH/3;
    private static int BALL_POS_H = GOAL_HEIGHT/3;

    private SurfaceHolder mHolder;  //SurfaceViewにリスナ登録したり、描いたりするために必要
    private Ball mBall;
    private List<Obstacle> mRockList = new ArrayList<Obstacle>();
    private List<Region> mRegionRockList = new ArrayList<>();

    private Bitmap mBitmapBall;
    private Bitmap mBitmapObstacle;

    private int mWidth;
    private int mHeight;
    private Paint mPaint;
    private Canvas mCanvas;

    private Path mGoalZone;
    private Path mStartZone;

    private Region mRegionGoalZone;
    private Region mRegionStartZone;
    private Region mRegionWholeScreen;

    private Thread mThread = null;
    private boolean mIsAttached = false;
    private boolean mIsFailure = false;
    private boolean mIsSuccess = false;

    SurfaceView sv;

    //-------------------------------------------------- SurfaceViewにリスナ
    public GameBoardView(Context context, SurfaceView sv) {
        super(context);
        this.sv = sv;
        mHolder = sv.getHolder();
        mHolder.addCallback(this);
     }
    //--------------------------------------------------SurfaceViewがcreateされた
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);  //描画されるもののエッジを滑らかにする
        mWidth = sv.getWidth();
        mHeight = sv.getHeight();

        //SurfaceViewにタッチしたら最初からはじめる
        sv.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Log.d(TAG,"onTouch");
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    init();
                }
                return false;
            }
        });
        init();
    }
    //--------------------------------------------------SurfaceView初期化
    private void init() {
        Resources rsc = getResources();
        mBitmapBall = BitmapFactory.decodeResource(rsc,R.drawable.ic_launcher2);
        Log.d(TAG,"created mBitmapBall width,height" + mBitmapBall.getWidth()+","+mBitmapBall.getHeight());     //264*264
        mBitmapBall = Bitmap.createScaledBitmap(mBitmapBall,BALL_SIZE,BALL_SIZE,true);
        Log.d(TAG,"created mBitmapBall width,height" + mBitmapBall.getWidth()+","+mBitmapBall.getHeight());     //100*100
        mBitmapObstacle = BitmapFactory.decodeResource(rsc,R.drawable.rock);
        mBitmapObstacle = Bitmap.createScaledBitmap(mBitmapObstacle,ROCK_SIZE,ROCK_SIZE,true);
        Log.d(TAG,"created mBitmapObstacle=" + mBitmapObstacle.getWidth()+","+mBitmapObstacle.getHeight());

        zoneDecide();       //スタート、ゴールゾーン決める
        newBall();          //Ball作成
        newObstacle();      //障害物作成

        mIsSuccess = false;     //成功して終了した時trueにする
        mIsFailure = false;    //失敗して終了した時trueにする
        startThread();
    }
    //--------------------------------------------------描画処理用スレッドをスタート
    public void startThread() {
        stopThread();
        mThread = new Thread(this);
        mIsAttached = true;
        Log.d(TAG,"mIsAttached=true");
        mThread.start();
    }
    //-------------------------------------------------- 描画処理用スレッドをストップ
    public boolean stopThread() {
        if(mThread == null) {
            return false;
        }
        mIsAttached = false;    //スレッド上でループする条件
        mThread = null;
        return true;
    }
    //--------------------------------------------------スタート、ゴールゾーンを決める
    private void zoneDecide() {
        mRegionWholeScreen = new Region(0,0,mWidth,mHeight);

        mStartZone = new Path();
//        mGoalZone.addRect(50,100,200,300,Path.Direction.CW);
        mStartZone.addRect(ROCK_SIZE,0, ROCK_SIZE+START_WIDTH, START_HEIGHT,Path.Direction.CW);
        mRegionStartZone =new Region();
        mRegionStartZone.setPath(mStartZone,mRegionWholeScreen);

        mGoalZone = new Path();
//        mGoalZone.addRect(50,100,200,300,Path.Direction.CW);
        mGoalZone.addRect(mWidth-ROCK_SIZE-GOAL_WIDTH,0, mWidth-ROCK_SIZE, GOAL_HEIGHT,Path.Direction.CW);
        mRegionGoalZone =new Region();
        mRegionGoalZone.setPath(mGoalZone,mRegionWholeScreen);
    }
    private void newBall() {
        Log.d(TAG,"mBitmapBall=" + mBitmapBall);
//        mBall = new Ball(DROID_POS, 300, mBitmapBall.getWidth()/4, mBitmapBall.getHeight()/8);
        mBall = new Ball(BALL_POS_W, BALL_POS_H, BALL_SIZE, BALL_SIZE);
    }
    //--------------------------------------------------障害物を作成
    private void newObstacle() {
        mRockList = new ArrayList();        //これをやらないでひどい目に合った
        mRegionRockList = new ArrayList();  //これをやらないでひどい目に合った
        int width = mBitmapObstacle.getWidth();
        int height = mBitmapObstacle.getHeight();

        //障害物をリストに追加
        for(int i=0; i<12; i++) {
            Obstacle r = new Obstacle(0, i*ROCK_SIZE, width, height);
            mRockList.add(r);
            Region region = new Region(0,i*ROCK_SIZE,width,height+i*ROCK_SIZE);
            mRegionRockList.add(region);
        }
        for(int i=0; i<11; i++) {
            Obstacle r = new Obstacle(mWidth-ROCK_SIZE, i*ROCK_SIZE, width, height);
            mRockList.add(r);
            Region region = new Region(mWidth-ROCK_SIZE,i*ROCK_SIZE, mWidth,height+i*ROCK_SIZE);
            mRegionRockList.add(region);
        }
        for(int i=0; i<9; i++) {
            Obstacle r = new Obstacle(mWidth/2-ROCK_SIZE/2, i*ROCK_SIZE, width, height);
            mRockList.add(r);
            Region region = new Region(mWidth/2-ROCK_SIZE/2,i*ROCK_SIZE, width+mWidth/2-ROCK_SIZE/2,height+i*ROCK_SIZE);
            mRegionRockList.add(region);
        }
        for(int i=1;i<mWidth/ROCK_SIZE; i++) {
            Obstacle r = new Obstacle(ROCK_SIZE*i, START_HEIGHT+9*ROCK_SIZE, width, height);
            mRockList.add(r);
            Region region = new Region(ROCK_SIZE*i,START_HEIGHT+9*ROCK_SIZE, width+ROCK_SIZE*i,height+START_HEIGHT+9*ROCK_SIZE);
            mRegionRockList.add(region);
        }
    }
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    //--------------------------------------------------描画処理用スレッドのrun
    public void run() {
        while(mIsAttached) {
            drawGameBoard();
//            Log.d(TAG,"tate="+MainActivity.tate);
            mBall.move(MainActivity.yoko,-MainActivity.tate);
        }
    }
    //--------------------------------------------------SurfaceViewに描く
    private void drawGameBoard() {
        if(mBall.isColligion(mRegionRockList)) {
            Log.d(TAG,"colligion");
            Resources rsc = getResources();
            mBitmapBall = BitmapFactory.decodeResource(rsc, R.drawable.aka);
            Log.d(TAG, "created mBitmapBall width,height" + mBitmapBall.getWidth() + "," + mBitmapBall.getHeight());     //264*264
            mBitmapBall = Bitmap.createScaledBitmap(mBitmapBall, BALL_SIZE, BALL_SIZE, true);
            mIsAttached = false;
            mIsFailure = true;
        }

        if(mBall.isGoal(mRegionGoalZone)) {
            Log.d(TAG,"goal");
            mIsAttached = false;
            mIsSuccess = true;
        }


        mCanvas = mHolder.lockCanvas();     //ピクセルの編集を開始
        mCanvas.drawColor(Color.LTGRAY);    //画面全体を塗りつぶす

        mPaint.setColor(0x50005000);
        mCanvas.drawPath(mGoalZone, mPaint);
        mCanvas.drawPath(mStartZone, mPaint);
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(50);
        mCanvas.drawText("START",ROCK_SIZE,50,mPaint);
        mCanvas.drawText("GOAL",mWidth-ROCK_SIZE-GOAL_WIDTH,50,mPaint);

        mCanvas.drawBitmap(mBitmapBall,mBall.getLeft(), mBall.getTop(),null);
        for(Obstacle r : mRockList) {
            mCanvas.drawBitmap(mBitmapObstacle, r.getLeft(), r.getTop(), null);
        }
        if(mIsSuccess) {
            mPaint.setColor(Color.RED);
            mPaint.setTextSize(mWidth/5);
            mCanvas.drawText("おめでとう", 0, ROCK_SIZE*6, mPaint);
            mPaint.setTextSize(mWidth/14);
            mCanvas.drawText("画面をタッチすると再スタート", 0, ROCK_SIZE*6+mWidth/5, mPaint);
        }
        if(mIsFailure) {
            mPaint.setColor(Color.RED);
            mPaint.setTextSize(mWidth/5);
            mCanvas.drawText("残念でした", 0, ROCK_SIZE*6, mPaint);
            mPaint.setTextSize(mWidth/14);
            mCanvas.drawText("画面をタッチすると再スタート", 0, ROCK_SIZE*6+mWidth/5, mPaint);
        }

        mHolder.unlockCanvasAndPost(mCanvas);
    }
}
