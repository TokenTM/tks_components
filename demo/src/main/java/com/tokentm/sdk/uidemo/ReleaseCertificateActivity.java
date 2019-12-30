package com.tokentm.sdk.uidemo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.tokentm.sdk.components.common.BaseTitleBarActivity;
import com.tokentm.sdk.components.utils.ComponentUtils;
import com.tokentm.sdk.model.CertificateInitiateResultDTO;
import com.tokentm.sdk.source.CertificateService;
import com.tokentm.sdk.source.TokenTmClient;
import com.tokentm.sdk.uidemo.databinding.ReleaseCertificateBinding;
import com.xxf.arch.XXF;
import com.xxf.arch.rxjava.transformer.ProgressHUDTransformerImpl;
import com.xxf.arch.utils.ToastUtils;

import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

import static com.tokentm.sdk.uidemo.DemoSp.SP_KEY_CERTIFICATE_CONTENT;
import static com.tokentm.sdk.uidemo.DemoSp.SP_KEY_CERTIFICATE_EXTRA_DATA;
import static com.tokentm.sdk.uidemo.DemoSp.SP_KEY_CERTIFICATE_ID;
import static com.tokentm.sdk.uidemo.DemoSp.SP_KEY_TO_DID;

/**
 * @author lqx  E-mail:herolqx@126.com
 * @Description 发布证书demo
 */
public class ReleaseCertificateActivity extends BaseTitleBarActivity {

    private static final String KEY_DID = "did";

    private String did;

    public static Intent getLauncher(Context context, String did) {
        return new Intent(context, ReleaseCertificateActivity.class)
                .putExtra(KEY_DID, did);
    }

    ReleaseCertificateBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ReleaseCertificateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        setTitle("发布证书");
        did = getIntent().getStringExtra(KEY_DID);
        binding.tvSendCertificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //弹出校验身份密码
                    ComponentUtils.showIdentityPwdDialog(
                            ReleaseCertificateActivity.this,
                            did,
                            new BiConsumer<DialogInterface, String>() {
                                @Override
                                public void accept(DialogInterface dialogInterface, String identityPwd) throws Exception {
                                    dialogInterface.dismiss();
                                    initiate(did
                                            , identityPwd
                                            , binding.etCertificateType.getText().toString()
                                            , binding.etCertificateContent.getText().toString()
                                            , binding.etCertificateOtherContent.getText().toString()
                                            , System.currentTimeMillis()
                                            , did);
                                }
                            });
                } catch (NumberFormatException e) {
                    ToastUtils.showToast("商品数量为数字");
                }

            }
        });
    }

    /**
     * 发起认证
     *
     * @param uDID
     * @param identityPwd 身份密码
     * @param type        证书类型
     * @param content     证书内容
     * @param extraData   额外存证内容 可空
     * @param expiryDate  存证失效日期(0L长期有效)
     * @param toDID       指定下一级接受人  可以先模拟自己给自己发
     */
    private void initiate(String uDID, String identityPwd, String type, String content, String extraData, long expiryDate, String toDID) {
        DemoSp.getInstance().putString(SP_KEY_CERTIFICATE_CONTENT, content);
        DemoSp.getInstance().putString(SP_KEY_CERTIFICATE_EXTRA_DATA, extraData);
        DemoSp.getInstance().putString(SP_KEY_TO_DID, toDID);
        TokenTmClient.getService(CertificateService.class)
                .initiate(uDID, identityPwd, type, content, extraData, expiryDate, toDID)
                .compose(XXF.bindToLifecycle(this))
                .compose(XXF.bindToProgressHud(new ProgressHUDTransformerImpl.Builder(ReleaseCertificateActivity.this)))
                .subscribe(new Consumer<CertificateInitiateResultDTO>() {
                    @Override
                    public void accept(CertificateInitiateResultDTO certificateInitiateResultDTO) throws Exception {
                        //存储certificate_id
                        DemoSp.getInstance().putString(SP_KEY_CERTIFICATE_ID, certificateInitiateResultDTO.getId());
                        finish();
                    }
                });
    }
}
