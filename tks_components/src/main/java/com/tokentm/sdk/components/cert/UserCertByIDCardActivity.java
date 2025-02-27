package com.tokentm.sdk.components.cert;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.ObservableField;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;

import com.tokentm.sdk.components.R;
import com.tokentm.sdk.components.cert.model.UserCertByIDCardParams;
import com.tokentm.sdk.components.common.BaseTitleBarActivity;
import com.tokentm.sdk.components.databinding.TksComponentsActivityCertByIdcardBinding;
import com.tokentm.sdk.components.identitypwd.dialog.IdentityPwdInputDialog;
import com.tokentm.sdk.components.identitypwd.view.WebViewActivity;
import com.tokentm.sdk.components.identitypwd.viewmodel.UserCertByIDCardVM;
import com.tokentm.sdk.components.utils.IgnoreSpacesInputFilter;
import com.tokentm.sdk.components.utils.compress_picture.CompressPictureUtil;
import com.tokentm.sdk.crop.Crop;
import com.tokentm.sdk.crop.util.CropUtils;
import com.tokentm.sdk.model.CertUserInfoStoreItem;
import com.tokentm.sdk.model.ChainCertResult;
import com.tokentm.sdk.model.CompanyCertInfoStoreItem;
import com.tokentm.sdk.source.CertService;
import com.tokentm.sdk.source.TokenTmClient;
import com.xxf.arch.XXF;
import com.xxf.arch.core.activityresult.ActivityResult;
import com.xxf.arch.rxjava.transformer.ProgressHUDTransformerImpl;
import com.xxf.view.actiondialog.SystemUtils;
import com.xxf.view.utils.RAUtils;

import java.io.File;

import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

/**
 * @author youxuan  E-mail:xuanyouwu@163.com
 * @Description 用户身份证认证
 * 返回值 @{@link ChainCertResult< CompanyCertInfoStoreItem >}
 */
public class UserCertByIDCardActivity extends BaseTitleBarActivity implements UserCertByIDCardPresenter {
    /**
     * 认证参数
     */
    private static final String KEY_CERT_PARAMS = "userCertParams";

    public static void launch(Context context, UserCertByIDCardParams userCertByIDCardParams) {
        context.startActivity(getLauncher(context, userCertByIDCardParams));
    }

    public static Intent getLauncher(Context context, UserCertByIDCardParams userCertByIDCardParams) {
        return new Intent(context, UserCertByIDCardActivity.class)
                .putExtra(KEY_CERT_PARAMS, userCertByIDCardParams);
    }


    TksComponentsActivityCertByIdcardBinding binding;
    UserCertByIDCardVM viewModel;
    UserCertByIDCardParams certByIDCardParams;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TksComponentsActivityCertByIdcardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        certByIDCardParams = (UserCertByIDCardParams) getIntent().getSerializableExtra(KEY_CERT_PARAMS);
        getTitleBar().setTitleBarTitle("身份证认证")
                .setTitleBarRightIcon(R.mipmap.tks_components_ic_help, new Action() {
                    @Override
                    public void run() throws Exception {
                        WebViewActivity.launch(getActivity(), "https://bcard.tokentm.net/h5/share/auth-reason.html");
                    }
                });
        viewModel = ViewModelProviders.of(this).get(UserCertByIDCardVM.class);
        binding.setViewModel(viewModel);
        binding.setPresenter(this);
        viewModel.userName.set(certByIDCardParams.getUserName());
        viewModel.userIDCard.set(certByIDCardParams.getUserIDCard());
        //禁止空格
        binding.etIdentityName.setFilters(new InputFilter[]{new IgnoreSpacesInputFilter(), new InputFilter.LengthFilter(50)});
    }

    @Override
    public void onSelectPic(ObservableField<String> pic) {
        //直接进行拍照
        SystemUtils.doTakePhoto(this, new Consumer<String>() {
            @Override
            public void accept(String url) throws Exception {
                File compressPictureWithSize = CompressPictureUtil.getCompressPictureWithSize(url, 1024 * 1000);
                startUCrop(UserCertByIDCardActivity.this, compressPictureWithSize.getAbsolutePath(), pic);
            }
        });
        //弹出相册和拍照选框
//        new BottomPicSelectDialog(this, new Consumer<String>() {
//            @Override
//            public void accept(String url) throws Exception {
//                startUCrop(UserCertByIDCardActivity.this, url, pic);
//
//            }
//        }).show();
    }

    /**
     * 启动裁剪
     *
     * @param activity       上下文
     * @param sourceFilePath 需要裁剪图片的绝对路径
     * @return //
     */
    @SuppressLint("CheckResult")
    public void startUCrop(final FragmentActivity activity, String sourceFilePath,
                           ObservableField<String> pic) {
        XXF.startActivityForResult(activity, CropUtils.getUCropLauncher(activity, sourceFilePath), Crop.REQUEST_CROP)
                .filter(new Predicate<ActivityResult>() {
                    @Override
                    public boolean test(ActivityResult activityResult) throws Exception {
                        return activityResult.isOk();
                    }
                }).
                take(1)
                .subscribe(new Consumer<ActivityResult>() {
                    @Override
                    public void accept(ActivityResult activityResult) throws Exception {
                        Uri resultUri = Crop.getOutput(activityResult.getData());
                        if (resultUri != null) {
                            pic.set(resultUri.getPath());
                        }
                    }
                });
    }


    @Override
    public void onUserCert(ObservableField<String> userName, ObservableField<String> userIDCard, ObservableField<String> userIDCardFrontPic, ObservableField<String> userIDCardBackPic, ObservableField<String> userIDCardHandedPic) {
        if (!RAUtils.isLegalDefault()) {
            return;
        }
        IdentityPwdInputDialog.showUserIdentityPwdInputDialogNoStampAnim(this, certByIDCardParams.getuDid(), new BiConsumer<DialogInterface, String>() {
            @Override
            public void accept(DialogInterface dialogInterface, String identityPwd) throws Exception {
                dialogInterface.dismiss();
                TokenTmClient.getService(CertService.class)
                        .userCert(
                                certByIDCardParams.getuDid(),
                                identityPwd,
                                userName.get(),
                                userIDCard.get(),
                                new File(userIDCardFrontPic.get()),
                                new File(userIDCardBackPic.get()),
                                new File(userIDCardHandedPic.get())
                        )
                        .compose(XXF.bindToLifecycle(UserCertByIDCardActivity.this))
                        .compose(XXF.bindToProgressHud(new ProgressHUDTransformerImpl.Builder(UserCertByIDCardActivity.this)))
                        .subscribe(new Consumer<ChainCertResult<CertUserInfoStoreItem>>() {
                            @Override
                            public void accept(ChainCertResult<CertUserInfoStoreItem> certUserInfoStoreItemChainCertResult) throws Exception {
                                setResult(Activity.RESULT_OK, getIntent().putExtra(KEY_ACTIVITY_RESULT, certUserInfoStoreItemChainCertResult));
                                finish();
                            }
                        });
            }
        }).show();
    }
}
