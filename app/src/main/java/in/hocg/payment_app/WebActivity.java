package in.hocg.payment_app;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alipay.sdk.app.H5PayCallback;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by hocgin on 2019/12/14.
 */

public class WebActivity extends AppCompatActivity {
  public static final String BUNDLE_KEY_URL = "url";
  
  @BindView(R.id.webview)
  WebView webView;
  
  
  Unbinder unbinder;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_webview);
    unbinder = ButterKnife.bind(this);
    _initData();
  }
  
  private void _initData() {
    Bundle extras = getIntent().getExtras();
    String url = extras.getString(WebActivity.BUNDLE_KEY_URL);
    
    // WebView
    WebSettings settings = webView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setJavaScriptCanOpenWindowsAutomatically(true);
    CookieManager.getInstance().setAcceptCookie(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
    }
    settings.setDomStorageEnabled(true);
    webView.setVerticalScrollbarOverlay(true);
    webView.setWebViewClient(new MyWebViewClient());
    webView.loadUrl(url);
    
    // 启用 WebView 调试模式。
    // 注意：请勿在实际 App 中打开！
    WebView.setWebContentsDebuggingEnabled(true);
  }
  
  @Override
  public void onBackPressed() {
    if (webView.canGoBack()) {
      webView.goBack();
    } else {
      finish();
    }
  }
  
  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbinder.unbind();
  }
  
  
  /**
   * 拦截所有被点击的 URL 来判定是否调用支付宝进行支付
   */
  private class MyWebViewClient extends WebViewClient {
    
    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
      if (!(url.startsWith("http") || url.startsWith("https"))) {
        return true;
      }
      
      // 支付宝 SDK 判定是否拦截
      Log.d("调用支付宝SDK", "支付宝 SDK 判定是否拦截URL=" + url);
      final PayTask task = new PayTask(WebActivity.this);
      boolean isIntercepted = task.payInterceptorWithUrl(url, true, new H5PayCallback() {
        @Override
        public void onPayResult(final H5PayResultModel result) {
          Log.d("调用支付宝SDK", "打开URL=" + url + "; 执行结果=" + result.getResultCode());
          final String url = result.getReturnUrl();
          if (!TextUtils.isEmpty(url)) {
            WebActivity.this.runOnUiThread(new Runnable() {
              @Override
              public void run() {
                view.loadUrl(url);
              }
            });
          }
        }
      });
      
      // 若成功拦截，则无需继续加载该URL；否则继续加载
      if (!isIntercepted) {
        view.loadUrl(url);
      }
      return true;
    }
  }
  
}
