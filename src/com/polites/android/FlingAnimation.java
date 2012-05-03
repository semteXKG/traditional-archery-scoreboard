
package com.polites.android;

/**
 * @author Jason Polites
 * 
 */
public class FlingAnimation implements Animation {

  private float velocityX;

  private float velocityY;

  private float factor = 0.9f;

  private final float threshold = 10;

  private FlingAnimationListener listener;


  public boolean update(final GestureImageView view, final long time) {
    final float seconds = time / 1000.0f;

    final float dx = velocityX * seconds;
    final float dy = velocityY * seconds;

    velocityX *= factor;
    velocityY *= factor;

    if (listener != null) {
      listener.onMove(dx, dy);
    }

    return Math.abs(velocityX) > threshold && Math.abs(velocityY) > threshold;
  }


  public void setVelocityX(final float velocityX) {
    this.velocityX = velocityX;
  }


  public void setVelocityY(final float velocityY) {
    this.velocityY = velocityY;
  }


  public void setFactor(final float factor) {
    this.factor = factor;
  }


  public void setListener(final FlingAnimationListener listener) {
    this.listener = listener;
  }
}
