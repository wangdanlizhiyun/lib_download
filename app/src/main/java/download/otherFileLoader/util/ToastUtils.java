package download.otherFileLoader.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtils {

    private static Toast toast;

    /**
     * 日记的输出方法
     * 
     * @param msg
     */
    public static void log(Object msg) {
        Log.d("test", String.valueOf(msg));
    }
    private static int color = Color.parseColor("#3EA32B");
    /**
     * 显示常规的Toast
     * 
     * @param context
     *            上下文对象
     * @param text
     *            显示的内容
     */
    public static void showToast(Context context, String text) {
        showMessage(context, text, 15,color);
    }
    public static void showToast(Context context, int text) {
        showMessage(context, context.getResources().getString(text), 15,color);
    }
    public static void showToast(Context context, String text,int size) {
        showMessage(context, text, size,color);
    }
    public static void showToast(Context context, String text,int size,int color) {
        showMessage(context, text, size,color);
    }

    /**
     * 根据方位显示toast
     * 
     * @param context
     *            上下文对象
     * @param text
     *            显示的内容
     * @param gravity
     *            对齐方式
     */
    public static void makeToast(Context context, String text, int gravity) {
        Toast t = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        t.setGravity(gravity, 0, 0);
        t.show();

    }

    /**
     * 自定义Toast的View的显示样式
     * 
     * @param context
     *            上下文对象
     * @param view
     *            需要显示的布局视图
     */
    public static void makeToast(Context context, View view) {
        Toast t = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        t.setView(view);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }

    public static void showMessage(Context context ,String text,int size,int color){
        if (toast == null) {
            toast=new Toast(context);
        }
        if (toast != null && VERSION.SDK_INT < 14) {
            toast.cancel();
        }
        toast.setDuration(Toast.LENGTH_SHORT);
        TextView textView = new TextView(context);  
        textView.setText(text);  
        textView.setTextSize(size);
        textView.setTextColor(color);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundResource(android.R.drawable.toast_frame);
        toast.setView(textView);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
    public static void showFailedToast(Context context) {
        showMessage(context, "获取数据失败", 15,Color.parseColor("#0000C0"));
    }
}
