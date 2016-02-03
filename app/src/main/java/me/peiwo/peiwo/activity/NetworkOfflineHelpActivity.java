package me.peiwo.peiwo.activity;

import me.peiwo.peiwo.R;
import me.peiwo.peiwo.util.TitleUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class NetworkOfflineHelpActivity extends BaseActivity {

	private TextView titleTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_offline_help);
		titleTextView = (TextView) findViewById(R.id.tv_offline_title);
		titleTextView.getPaint().setFakeBoldText(true);
		setTitleBar();
	}

	private void setTitleBar() {
		TitleUtil.setTitleBar(this, "网络检查", new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		}, null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

}
