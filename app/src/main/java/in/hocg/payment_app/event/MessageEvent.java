package in.hocg.payment_app.event;

/**
 * Created by hocgin on 2019/12/14.
 */
public class MessageEvent {
  private Object data;
  
  public MessageEvent(Object data) {
    this.data = data;
  }
  
  public Object getData() {
    return data;
  }
}
