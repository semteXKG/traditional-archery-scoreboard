
package semtex.archery.business.interfaces;

public interface ICallback<T> {

  public void onSuccess(T data);


  public void onFailure(Throwable tr);


  public void inProgress();
} // ICallback
