package com.samsung.atlas.views;

import com.samsung.atlas.atlas3dv2.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class FastDraggingView extends LinearLayout {
  private static final String TAG = "dragging";
  private static Integer height = 128;
  private static Integer width = 128;
  private OnDraggingListener m_listener;
  
  private final float m_vertical_padding;
  private final float m_horizontal_padding;
  
  private final ImageView m_left_image, m_mid_image, m_right_image;
  private AnimationDrawable m_left_anim, m_mid_anim, m_right_anim;
  
  private enum Direction { RIGHT, LEFT }
  private Direction m_direction;
  
  private boolean m_pointer_down;
  private float m_down_x;

  
  public FastDraggingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setOrientation(HORIZONTAL);
    setGravity(Gravity.CENTER);
    setWeightSum(1.0f);
    
    LayoutInflater.from(context).inflate(R.layout.dragging_view_layout, this, true);
    
    TypedArray attributes_array = context.obtainStyledAttributes(attrs, R.styleable.DraggingView, 0, 0);
    m_vertical_padding = attributes_array.getDimension(R.styleable.DraggingView_vertical_padding, 0.0f);
    m_horizontal_padding = attributes_array.getDimension(R.styleable.DraggingView_horizontal_padding, 0.0f);
    int direction = attributes_array.getInteger(R.styleable.DraggingView_direction, 0);
    m_direction = Direction.values()[direction];
    attributes_array.recycle();
    
    m_left_image = (ImageView) findViewById(R.id.left_draggingview_image);
    m_mid_image = (ImageView) findViewById(R.id.mid_draggingview_image);
    m_right_image = (ImageView) findViewById(R.id.right_draggingview_image);
    
    m_left_image.setPadding((int) m_horizontal_padding, (int) m_vertical_padding, (int) m_horizontal_padding, (int) m_vertical_padding);
    m_mid_image.setPadding((int) m_horizontal_padding, (int) m_vertical_padding, (int) m_horizontal_padding, (int) m_vertical_padding);
    m_right_image.setPadding((int) m_horizontal_padding, (int) m_vertical_padding, (int) m_horizontal_padding, (int) m_vertical_padding);
    
    initImagesState();
    initAnimationState();
    
    m_pointer_down = false;
    m_down_x = 0.0f;
  }
  
  public void setOnDraggingListener(OnDraggingListener listener) {
    m_listener = listener;
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        m_down_x = event.getX();
        m_pointer_down = true;
        setAnimationVisibility(View.VISIBLE);
        startAnimation();
        m_listener.onDraggingStarted();
        break;
      case MotionEvent.ACTION_MOVE:
        if (m_pointer_down && Math.abs(event.getX() - m_down_x) >= 100) {
          m_listener.onDraggingProgress();
        }
        break;
      case MotionEvent.ACTION_UP:
        m_down_x = 0.0f;
        m_pointer_down = false;
        stopAnimation();
        initAnimationState();
        initImagesState();
        initVisibility();
        m_listener.onDraggingStopped();
        break;
      default:
        break;
    }
    return true;
  }

  
  /* Private methods */
  // --------------------------------------------------------------------------
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
      height = 128;
    } else if (getLayoutParams().height == LayoutParams.MATCH_PARENT ||
               getLayoutParams().height == LayoutParams.FILL_PARENT) {
      height = MeasureSpec.getSize(heightMeasureSpec);
    } else {
      height = getLayoutParams().height;
    }
    if (getLayoutParams().width == LayoutParams.WRAP_CONTENT) {
      width = 128;
    } else if (getLayoutParams().width == LayoutParams.MATCH_PARENT ||
               getLayoutParams().width == LayoutParams.FILL_PARENT) {
      width = MeasureSpec.getSize(widthMeasureSpec);
    } else {
      width = getLayoutParams().width;
    }
    setMeasuredDimension(width | MeasureSpec.EXACTLY, height | MeasureSpec.EXACTLY);
    super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
  }
  
  private void startAnimation() {
    m_left_anim.start();
    m_mid_anim.start();
    m_right_anim.start();
  }
  
  private void stopAnimation() {
    m_left_anim.stop();
    m_mid_anim.stop();
    m_right_anim.stop();
  }
  
  private void setAnimationVisibility(int visibility) {
    m_left_image.setVisibility(visibility);
    m_mid_image.setVisibility(visibility);
    m_right_image.setVisibility(visibility);
  }
  
  private void initVisibility() {
    switch (m_direction) {
      case RIGHT:
        m_left_image.setVisibility(View.VISIBLE);
        m_mid_image.setVisibility(View.INVISIBLE);
        m_right_image.setVisibility(View.INVISIBLE);
        break;
      case LEFT:
        m_left_image.setVisibility(View.INVISIBLE);
        m_mid_image.setVisibility(View.INVISIBLE);
        m_right_image.setVisibility(View.VISIBLE);
        break;
      default:
        break;
    }
  }
  
  private void initImagesState() {
    switch (m_direction) {
      case RIGHT:
        m_left_image.setBackgroundResource(R.drawable.drag_anim_right_left);
        m_mid_image.setBackgroundResource(R.drawable.drag_anim_right_mid);
        m_right_image.setBackgroundResource(R.drawable.drag_anim_right_right);
        
        m_left_image.setVisibility(View.VISIBLE);
        break;
      case LEFT:
        m_left_image.setBackgroundResource(R.drawable.drag_anim_left_left);
        m_mid_image.setBackgroundResource(R.drawable.drag_anim_left_mid);
        m_right_image.setBackgroundResource(R.drawable.drag_anim_left_right);
        
        m_right_image.setVisibility(View.VISIBLE);
        break;
      default:
        break;
    }
  }
  
  private void initAnimationState() {
    m_left_anim = (AnimationDrawable) m_left_image.getBackground();
    m_mid_anim = (AnimationDrawable) m_mid_image.getBackground();
    m_right_anim = (AnimationDrawable) m_right_image.getBackground();
  }
}
