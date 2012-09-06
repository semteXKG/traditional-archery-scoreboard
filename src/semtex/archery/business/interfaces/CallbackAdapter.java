
package semtex.archery.business.interfaces;

public class CallbackAdapter<T> implements ICallback<T> {

  public void onSuccess(final T data) {
    // nothing to do
  } // onSuccess


  public void onFailure(final Throwable tr) {
    // nothing to do
  } // onFailure


  public void inProgress() {
    // nothing to do
  } // inProgress

} // CallbackAdapter
