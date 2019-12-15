package in.hocg.payment_app;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.alipay.sdk.app.EnvUtils;
import com.alipay.sdk.app.PayTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import in.hocg.payment_app.event.MessageEvent;

public class MainActivity extends AppCompatActivity {
  @BindView(R.id.urlInput)
  EditText urlInput;
  @BindView(R.id.payParamsInput)
  EditText payParamsInput;
  Unbinder unbinder;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    unbinder = ButterKnife.bind(this);
    EventBus.getDefault().register(this);
  }
  
  @OnClick(R.id.changeModel)
  protected void onChangeModel(CheckBox checkBox) {
    boolean checked = checkBox.isChecked();
    EnvUtils.EnvEnum envEnum = EnvUtils.EnvEnum.ONLINE;
    if (checked) {
      envEnum = EnvUtils.EnvEnum.SANDBOX;
    }
    EnvUtils.setEnv(envEnum);
    showToast(String.format("改变为 %s 模式", envEnum.name()));
  }
  
  @OnClick(R.id.paste)
  protected void onClickPaste() {
    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    CharSequence text = cm.getText();
    payParamsInput.setText(text);
  }
  
  @OnClick(R.id.goPayBtn)
  protected void onGoPayBtn() {
    final String text = payParamsInput.getText().toString();
    if (TextUtils.isEmpty(text)) {
      showToast("请输入正确的参数");
      return;
    }
    async(new Runnable() {
      @Override
      public void run() {
        PayTask alipay = new PayTask(MainActivity.this);
        Map<String, String> result = alipay.payV2(text, true);
        EventBus.getDefault().post(new MessageEvent(result));
      }
    });
    
    showToast("使用APP支付");
  }
  
  @OnClick(R.id.goBrowserBtn)
  protected void onGoBrowserBtn() {
    final String text = urlInput.getText().toString();
    if (TextUtils.isEmpty(text)) {
      showToast("请输入正确的参数");
      return;
    }
    
    WebView.setWebContentsDebuggingEnabled(true);
    Intent intent = new Intent(this, WebActivity.class);
    Bundle extras = new Bundle();
    extras.putString(WebActivity.BUNDLE_KEY_URL, text);
    intent.putExtras(extras);
    startActivity(intent);
    
    showToast("内置打开浏览器");
  }
  
  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onMessageEvent(MessageEvent event) {
    Map<String, Object> result = (Map<String, Object>) event.getData();
    Object memo = result.get("memo");
    Object resultStatus = result.get("resultStatus");
    Log.d("调用支付宝SDK", "调用结果: " + result);
    showToast("Code=" + resultStatus + ";支付结果=" + memo);
  }
  
  void showToast(String text) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
  }
  
  void async(Runnable runnable) {
    new Thread(runnable).start();
  }
  
  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbinder.unbind();
    EventBus.getDefault().unregister(this);
  }
}
