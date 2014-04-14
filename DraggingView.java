package com.samsung.atlas.views;

import com.samsung.atlas.atlas3dv2.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;


public class DraggingView extends View {
  private static final String TAG = "dragging";
  private static Integer height = 128;
  private static Integer width = 128;
  private static long timelag = 100;
  private OnDraggingListener m_listener;
  
  private final float m_vertical_padding;
  private final float m_horizontal_padding;
  
  private final Drawable m_leader_image, m_medium_image, m_slave_image;
  
  private enum Direction { RIGHT, LEFT }
  private enum AnimationState { NONE, Dim_Dim_Dim, Ordinary_Dim_Dim, Highlight_Ordinary_Dim, Ordinary_Highlight_Ordinary, Dim_Ordinary_Highlight, Dim_Dim_Ordinary }
  
  private Direction m_direction;
  private int m_left_origin, m_left_end, m_mid_origin, m_mid_end, m_right_origin, m_right_end;
  
  private AnimationState m_anim_state;
  private boolean m_anim_interrupted;
  private Handler handler;
  
  private boolean m_pointer_down;
  private float m_down_x;
  
  
  /* Public API */
  // --------------------------------------------------------------------------
  public DraggingView(Context context, AttributeSet attrs) {
    super(context, attrs);
    
    TypedArray attributes_array = context.obtainStyledAttributes(attrs, R.styleable.DraggingView, 0, 0);
    m_vertical_padding = attributes_array.getDimension(R.styleable.DraggingView_vertical_padding, 0.0f);
    m_horizontal_padding = attributes_array.getDimension(R.styleable.DraggingView_horizontal_padding, 0.0f);
    int direction = attributes_array.getInteger(R.styleable.DraggingView_direction, 0);
    m_direction = Direction.values()[direction];
    attributes_array.recycle();
    
    int ids = 0;
    switch (m_direction) {
      case RIGHT:
        ids = R.array.next_image_ids;
        m_left_origin = (int) m_horizontal_padding;
        m_left_end = m_mid_origin = width / 3;
        m_mid_end = m_right_origin = 2 * width / 3;
        m_right_end = width - (int) m_horizontal_padding;
        break;
      case LEFT:
        ids = R.array.previous_image_ids;
        m_left_origin = m_mid_end = 2 * width / 3;
        m_left_end = width - (int) m_horizontal_padding;
        m_right_end = m_mid_origin = width / 3;
        m_right_origin = (int) m_horizontal_padding;
        break;
      default:
        break;
    }
    TypedArray images = getResources().obtainTypedArray(ids);
    m_leader_image = images.getDrawable(1);
    m_medium_image = images.getDrawable(0);
    m_slave_image = images.getDrawable(2);
    images.recycle();
    
    m_anim_state = AnimationState.NONE;
    m_anim_interrupted = false;
    handler = new Handler();
    
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
        m_anim_state = AnimationState.Dim_Dim_Dim;
        m_anim_interrupted = false;
        invalidate();
        m_listener.onDraggingStarted();
        break;
      case MotionEvent.ACTION_MOVE:
        handleMove(event);
        break;
      case MotionEvent.ACTION_UP:
        m_down_x = 0.0f;
        m_pointer_down = false;
        m_anim_state = AnimationState.NONE;
        m_anim_interrupted = true;
        invalidate();
        m_listener.onDraggingStopped();
        break;
    }
    return true;
  }
  
  @Override
  public void onDraw(Canvas canvas) {
    switch (m_anim_state) {
      case NONE:
        m_slave_image.setBounds(m_left_origin, (int) m_vertical_padding, m_left_end, height - (int) m_vertical_padding);
        m_slave_image.draw(canvas);
        break;
      case Dim_Dim_Dim:
        m_slave_image.setBounds(m_left_origin, (int) m_vertical_padding, m_left_end, height - (int) m_vertical_padding);
        m_slave_image.draw(canvas);
        m_slave_image.setBounds(m_mid_origin, (int) m_vertical_padding, m_mid_end, height - (int) m_vertical_padding);
        m_slave_image.draw(canvas);
        m_slave_image.setBounds(m_right_origin, (int) m_vertical_padding, m_right_end, height - (int) m_vertical_padding);
        m_slave_image.draw(canvas);
        break;
      case Ordinary_Dim_Dim:
        m_medium_image.setBounds(m_left_origin, (int) m_vertical_padding, m_left_end, height - (int) m_vertical_padding);
        m_medium_image.draw(canvas);
        m_slave_image.setBounds(m_mid_origin, (int) m_vertical_padding, m_mid_end, height - (int) m_vertical_padding);
        m_slave_image.draw(canvas);
        m_slave_image.setBounds(m_right_origin, (int) m_vertical_padding, m_right_end, height - (int) m_vertical_padding);
        m_slave_image.draw(canvas);
        break;
      case Highlight_Ordinary_Dim:
        m_leader_image.setBounds(m_left_origin, (int) m_vertical_padding, m_left_end, height - (int) m_vertical_padding);
        m_leader_image.draw(canvas);
        m_medium_image.setBounds(m_mid_origin, (int) m_vertical_padding, m_mid_end, height - (int) m_vertical_padding);
        m_medium_image.draw(canvas);
        m_slave_image.setBounds(m_right_origin, (int) m_vertical_padding, m_right_end, height - (int) m_vertical_padding);
        m_slave_image.draw(canvas);
        break;
      case Ordinary_Highlight_Ordinary:
        m_medium_image.setBounds(m_left_origin, (int) m_vertical_padding, m_left_end, height - (int) m_vertical_padding);
        m_medium_image.draw(canvas);
        m_leader_image.setBounds(m_mid_origin, (int) m_vertical_padding, m_mid_end, height - (int) m_vertical_padding);
        m_leader_image.draw(canvas);
        m_medium_image.setBounds(m_right_origin, (int) m_vertical_padding, m_right_end, height - (int) m_vertical_padding);
        m_medium_image.draw(canvas);
        break;
      case Dim_Ordinary_Highlight:
        m_slave_image.setBounds(m_left_origin, (int) m_vertical_padding, m_left_end, height - (int) m_vertical_padding);
        m_slave_image.draw(canvas);
        m_medium_image.setBounds(m_mid_origin, (int) m_vertical_padding, m_mid_end, height - (int) m_vertical_padding);
        m_medium_image.draw(canvas);
        m_leader_image.setBounds(m_right_origin, (int) m_vertical_padding, m_right_end, height - (int) m_vertical_padding);
        m_leader_image.draw(canvas);
        break;
      case Dim_Dim_Ordinary:
        m_slave_image.setBounds(m_left_origin, (int) m_vertical_padding, m_left_end, height - (int) m_vertical_padding);
        m_slave_image.draw(canvas);
        m_slave_image.setBounds(m_mid_origin, (int) m_vertical_padding, m_mid_end, height - (int) m_vertical_padding);
        m_slave_image.draw(canvas);
        m_medium_image.setBounds(m_right_origin, (int) m_vertical_padding, m_right_end, height - (int) m_vertical_padding);
        m_medium_image.draw(canvas);
        break;
      default:
        break;
    }
  }

  
  /* Private methods */
  // --------------------------------------------------------------------------
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
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
  }
  
  public void handleMove(final MotionEvent event) {
    switch (m_anim_state) {
      case NONE:
        invalidate();
        break;
      case Dim_Dim_Dim:
        handler.postDelayed(new Runnable() {
          public void run() {
            m_anim_state = m_anim_interrupted ? AnimationState.NONE : AnimationState.Ordinary_Dim_Dim;
            invalidate();
          }
        }, timelag);
        break;
      case Ordinary_Dim_Dim:
        handler.postDelayed(new Runnable() {
          public void run() {
            m_anim_state = m_anim_interrupted ? AnimationState.NONE : AnimationState.Highlight_Ordinary_Dim;
            invalidate();
          }
        }, timelag);
        break;
      case Highlight_Ordinary_Dim:
        handler.postDelayed(new Runnable() {
          public void run() {
            m_anim_state = m_anim_interrupted ? AnimationState.NONE : AnimationState.Ordinary_Highlight_Ordinary;
            invalidate();
          }
        }, timelag);
        break;
      case Ordinary_Highlight_Ordinary:
        handler.postDelayed(new Runnable() {
          public void run() {
            m_anim_state = m_anim_interrupted ? AnimationState.NONE : AnimationState.Dim_Ordinary_Highlight;
            invalidate();
          }
        }, timelag);
        break;
      case Dim_Ordinary_Highlight:
        handler.postDelayed(new Runnable() {
          public void run() {
            m_anim_state = m_anim_interrupted ? AnimationState.NONE : AnimationState.Dim_Dim_Ordinary;
            invalidate();
          }
        }, timelag);
        break;
      case Dim_Dim_Ordinary:
        handler.postDelayed(new Runnable() {
          public void run() {
            m_anim_state = m_anim_interrupted ? AnimationState.NONE : AnimationState.Dim_Dim_Dim;
            invalidate();
          }
        }, timelag);
        break;
      default:
        break;
    }
    
    if (m_pointer_down && Math.abs(event.getX() - m_down_x) >= 100) {
      m_listener.onDraggingProgress();
    }
  }
}
