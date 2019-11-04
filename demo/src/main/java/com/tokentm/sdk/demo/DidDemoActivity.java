package com.tokentm.sdk.demo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;

import com.tokentm.sdk.components.ComponentUtils;
import com.tokentm.sdk.demo.databinding.DidActivityBinding;
import com.xxf.arch.utils.ToastUtils;

import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

public class DidDemoActivity extends FragmentActivity {
    DidActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DidActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    String did;

    @Override
    protected void onResume() {
        super.onResume();
        did = DemoSp.getInstance().getString("did");
        binding.didText.setText(TextUtils.isEmpty(did) ? "" : "已创建did:\n" + did);
    }

    private void initView() {

        binding.didBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComponentUtils.launchUserIdentityPwdActivity(
                        DidDemoActivity.this,
                        "17611639080",
                        new Consumer<String>() {
                            @Override
                            public void accept(String uDID) throws Exception {
                                //TODO 中心化系统和userId进行绑定
                                DemoSp.getInstance().putString("did", uDID);
                                binding.didText.setText("did:" + uDID);
                            }
                        });
            }
        });

        binding.updatePwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(did)) {
                    ToastUtils.showToast("先创建did");
                    return;
                }
                ComponentUtils
                        .launchUserIdentityPwdAReSetctivity(
                                DidDemoActivity.this,
                                did,
                                "17611639080",
                                new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean reseted) throws Exception {
                                        ToastUtils.showToast("重置成功?" + String.valueOf(reseted));
                                    }
                                });
            }
        });

        binding.checkPwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(did)) {
                    ToastUtils.showToast("先创建did");
                    return;
                }
                ComponentUtils.showIdentityPwdDialog(
                        DidDemoActivity.this,
                        did,
                        new BiConsumer<DialogInterface, String>() {
                            @Override
                            public void accept(DialogInterface dialogInterface, String identityPwd) throws Exception {
                                dialogInterface.dismiss();
                                //TODO 下一步
                            }
                        });
            }
        });
    }
}
