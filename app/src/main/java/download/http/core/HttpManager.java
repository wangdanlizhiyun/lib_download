package download.http.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;

import download.http.request.Request;
import download.http.request.RequestBuilder;

public class HttpManager {
	private SmartExecutor executor;
	private static class InstanceHoler {
		private static final HttpManager instance = new HttpManager();
	}
	public static HttpManager getInstance() {
		return InstanceHoler.instance;
	}

	private final int threadCount = 3;

	private HttpManager() {
		executor = new SmartExecutor(threadCount, 200);
		executor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
		executor.setOverloadPolicy(OverloadPolicy.DiscardOldTaskInQueue);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void request(final Request request) {
		executor.execute(new HttpTask(request));
	}
}