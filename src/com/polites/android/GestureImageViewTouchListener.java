
package com.polites.android;

import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;


public class GestureImageViewTouchListener implements OnTouchListener {

  private final GestureImageView image;

  private final PointF current = new PointF();

  private final PointF last = new PointF();

  private final PointF next = new PointF();

  private final PointF midpoint = new PointF();

  private final VectorF scaleVector = new VectorF();

  private final VectorF pinchVector = new VectorF();

  boolean touched = false;

  private float initialDistance;

  private float lastScale = 1.0f;

  private float currentScale = 1.0f;

  private float boundaryLeft = 0;

  private float boundaryTop = 0;

  private float boundaryRight = 0;

  private float boundaryBottom = 0;

  private float maxScale = 5.0f;

  private float minScale = 0.25f;

  private float centerX = 0;

  private float centerY = 0;

  private float startingScale = 0;

  private boolean canDragX = false;

  private boolean canDragY = false;

  private boolean multiTouch = false;

  private final int displayWidth;

  private final int displayHeight;

  private final int imageWidth;

  private final int imageHeight;

  private final DoubleTapListener doubleTapListener;

  private final FlingListener flingListener;

  private final FlingAnimation flingAnimation;

  private final GestureDetector doubleTapDetector;

  private final GestureDetector flingDetector;

  private final GestureImageViewListener imageListener;


  public GestureImageViewTouchListener(final GestureImageView image, final int displayWidth, final int displayHeight) {
    super();

    this.image = image;

    this.displayWidth = displayWidth;
    this.displayHeight = displayHeight;

    this.centerX = displayWidth / 2.0f;
    this.centerY = displayHeight / 2.0f;

    this.imageWidth = image.getImageWidth();
    this.imageHeight = image.getImageHeight();

    startingScale = image.getScale();

    currentScale = startingScale;
    lastScale = startingScale;

    // Calc boundaries
    boundaryRight = displayWidth;
    boundaryBottom = displayHeight;
    boundaryLeft = 0;
    boundaryTop = 0;

    next.x = image.getX();
    next.y = image.getY();

    doubleTapListener = new DoubleTapListener(image);
    flingListener = new FlingListener();
    flingAnimation = new FlingAnimation();

    flingAnimation.setListener(new FlingAnimationListener() {

      public void onMove(final float x, final float y) {
        handleDrag(current.x + x, current.y + y);
      }
    });

    doubleTapDetector = new GestureDetector(doubleTapListener);
    flingDetector = new GestureDetector(flingListener);

    imageListener = image.getGestureImageViewListener();

    calculateBoundaries();
  }


  private void startFling() {
    flingAnimation.setVelocityX(flingListener.getVelocityX());
    flingAnimation.setVelocityY(flingListener.getVelocityY());
    image.animationStart(flingAnimation);
  }


  private void stopFling() {
    image.animationStop();
  }


  public boolean onTouch(final View v, final MotionEvent event) {

    if (event.getPointerCount() == 1 && flingDetector.onTouchEvent(event)) {
      startFling();
    }

    if (doubleTapDetector.onTouchEvent(event)) {
      initialDistance = 0;
      lastScale = startingScale;
      currentScale = startingScale;
      next.x = image.getX();
      next.y = image.getY();
      calculateBoundaries();
    }

    if (event.getAction() == MotionEvent.ACTION_UP) {

      multiTouch = false;

      initialDistance = 0;
      lastScale = currentScale;

      if (!canDragX) {
        next.x = centerX;
      }

      if (!canDragY) {
        next.y = centerY;
      }

      boundCoordinates();

      if (!canDragX && !canDragY) {
        currentScale = startingScale;
        lastScale = currentScale;
      }

      image.setScale(currentScale);
      image.setPosition(next.x, next.y);

      if (imageListener != null) {
        imageListener.onScale(currentScale);
        imageListener.onPosition(next.x, next.y);
      }

      image.redraw();
    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
      stopFling();

      last.x = event.getX();
      last.y = event.getY();

      if (imageListener != null) {
        imageListener.onTouch(last.x, last.y);
      }

      touched = true;
    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
      if (event.getPointerCount() > 1) {
        multiTouch = true;
        if (initialDistance > 0) {

          pinchVector.set(event);
          pinchVector.calculateLength();

          final float distance = pinchVector.length;

          if (initialDistance != distance) {

            // We have moved (scaled)
            currentScale = distance / initialDistance * lastScale;

            if (currentScale > maxScale) {
              currentScale = maxScale;
            } else if (currentScale < minScale) {
              currentScale = minScale;
            }

            calculateBoundaries();

            scaleVector.length *= currentScale;

            scaleVector.calculateEndPoint();

            scaleVector.length /= currentScale;

            next.x = scaleVector.end.x;
            next.y = scaleVector.end.y;

            image.setScale(currentScale);
            image.setPosition(next.x, next.y);

            if (imageListener != null) {
              imageListener.onScale(currentScale);
              imageListener.onPosition(next.x, next.y);
            }

            image.redraw();
          }
        } else {
          initialDistance = MathUtils.distance(event);

          MathUtils.midpoint(event, midpoint);

          scaleVector.setStart(midpoint);
          scaleVector.setEnd(next);

          scaleVector.calculateLength();
          scaleVector.calculateAngle();

          scaleVector.length /= lastScale;
        }
      } else {
        if (!touched) {
          touched = true;
          last.x = event.getX();
          last.y = event.getY();
          next.x = image.getX();
          next.y = image.getY();
        } else if (!multiTouch) {
          if (handleDrag(event.getX(), event.getY())) {
            image.redraw();
          }
        }
      }
    }

    return true;
  }


  protected boolean handleDrag(final float x, final float y) {
    current.x = x;
    current.y = y;

    final float diffX = current.x - last.x;
    final float diffY = current.y - last.y;

    if (diffX != 0 || diffY != 0) {

      if (canDragX) {
        next.x += diffX;
      }
      if (canDragY) {
        next.y += diffY;
      }

      boundCoordinates();

      last.x = current.x;
      last.y = current.y;

      if (canDragX || canDragY) {
        image.setPosition(next.x, next.y);

        if (imageListener != null) {
          imageListener.onPosition(next.x, next.y);
        }

        return true;
      }
    }

    return false;
  }


  public void reset() {
    currentScale = startingScale;
    next.x = centerX;
    next.y = centerY;
    calculateBoundaries();
    image.setScale(currentScale);
    image.setPosition(next.x, next.y);
    image.redraw();
  }


  public float getMaxScale() {
    return maxScale;
  }


  public void setMaxScale(final float maxScale) {
    this.maxScale = maxScale;
  }


  public float getMinScale() {
    return minScale;
  }


  public void setMinScale(final float minScale) {
    this.minScale = minScale;
  }


  protected void boundCoordinates() {
    if (next.x < boundaryLeft) {
      next.x = boundaryLeft;
    } else if (next.x > boundaryRight) {
      next.x = boundaryRight;
    }

    if (next.y < boundaryTop) {
      next.y = boundaryTop;
    } else if (next.y > boundaryBottom) {
      next.y = boundaryBottom;
    }
  }


  protected void calculateBoundaries() {

    final int effectiveWidth = Math.round(imageWidth * currentScale);
    final int effectiveHeight = Math.round(imageHeight * currentScale);

    canDragX = effectiveWidth > displayWidth;
    canDragY = effectiveHeight > displayHeight;

    if (canDragX) {
      final float diff = (effectiveWidth - displayWidth) / 2.0f;
      boundaryLeft = centerX - diff;
      boundaryRight = centerX + diff;
    }

    if (canDragY) {
      final float diff = (effectiveHeight - displayHeight) / 2.0f;
      boundaryTop = centerY - diff;
      boundaryBottom = centerY + diff;
    }
  }
}
