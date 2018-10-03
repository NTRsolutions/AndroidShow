package ekoolab.com.show.activities;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.rey.material.widget.ProgressView;

import java.util.HashMap;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.TextPicture;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.views.EasyPopup;
import ekoolab.com.show.views.HorizontalGravity;

public class PostTextActivity extends BaseActivity implements View.OnClickListener {

    private TextView tv_name, tv_cancel, tv_save, tv_permission;
    private EditText et_content;
    private EasyPopup easyPopup;
    private ProgressView progressView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_post_text;
    }

    @Override
    protected void initViews() {
        super.initViews();
        tv_name = findViewById(R.id.tv_name);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_save = findViewById(R.id.tv_save);
        tv_permission = findViewById(R.id.tv_permission);
        progressView = findViewById(R.id.progress_bar);
        et_content = findViewById(R.id.et_content);
        tv_name.setText(getResources().getString(R.string.moment));
        tv_cancel.setOnClickListener(this);
        tv_save.setOnClickListener(this);
        tv_permission.setOnClickListener(this);
    }

    private void postText() {
        tv_save.setVisibility(View.INVISIBLE);
        progressView.setVisibility(View.VISIBLE);
        progressView.start();
        setViewClickable(false);
        HashMap<String, String> map = new HashMap<>(4);
        map.put("body", et_content.getText().toString());
        map.put("type", "text");
        map.put("permission", tv_permission.getText().toString().toLowerCase());
        map.put("token", AuthUtils.getInstance(PostTextActivity.this).getApiToken());
        ApiServer.baseUploadRequest(this, Constants.TextPost, map, null,
                new TypeToken<ResponseData<TextPicture>>() {
                })
                .subscribe(new NetworkSubscriber<TextPicture>() {
                    @Override
                    protected void onSuccess(TextPicture textPicture) {
                        try {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                            PostTextActivity.this.finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        tv_save.setVisibility(View.VISIBLE);
                        setViewClickable(true);
                        progressView.setVisibility(View.GONE);
                        progressView.stop();
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void setViewClickable(boolean clickable) {
        et_content.setClickable(clickable);
        et_content.setEnabled(clickable);
        tv_permission.setClickable(clickable);
    }

    @Override
    protected void initData() {
        super.initData();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                PostTextActivity.this.finish();
                break;
            case R.id.tv_save:
                postText();
                break;
            case R.id.tv_permission:
                if (easyPopup == null) {
                    View contentView = getLayoutInflater().inflate(R.layout.popup_post_permission_moment, null);
                    TextView tvPublic = contentView.findViewById(R.id.tv_public);
                    TextView tvFriend = contentView.findViewById(R.id.tv_friend);
                    TextView tvPrivate = contentView.findViewById(R.id.tv_private);
                    tvPublic.setOnClickListener(this);
                    tvFriend.setOnClickListener(this);
                    tvPrivate.setOnClickListener(this);
                    easyPopup = new EasyPopup(this)
                            .setContentView(contentView)
                            .setFocusAndOutsideEnable(true)
                            .createPopup();
                }
                easyPopup.setHorizontalGravity(HorizontalGravity.RIGHT);
                easyPopup.showAsDropDown(tv_permission);
                break;
            case R.id.tv_public:
            case R.id.tv_friend:
            case R.id.tv_private:
                tv_permission.setText(((TextView) view).getText());
                tv_permission.setHint(((TextView) view).getHint());
                easyPopup.dismiss();
                break;
        }
    }
}
