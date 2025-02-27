package com.tokentm.sdk.components.identitypwd.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;

import com.tokentm.sdk.components.R;
import com.tokentm.sdk.components.common.BaseActivity;
import com.tokentm.sdk.components.databinding.TksComponentsActivityIdentityPwdDecryptBinding;
import com.tokentm.sdk.components.identitypwd.model.StepModel;
import com.tokentm.sdk.components.identitypwd.presenter.IdentityPwdDecryptPresenter;
import com.tokentm.sdk.components.identitypwd.viewmodel.IdentityPwdDecryptVm;
import com.tokentm.sdk.components.utils.SuperStatusBarUtils;
import com.tokentm.sdk.model.IdentityInfoStoreItem;
import com.tokentm.sdk.model.NodeServiceDecryptedPartItem;
import com.tokentm.sdk.model.NodeServiceEncryptedPartItem;
import com.tokentm.sdk.source.BasicService;
import com.tokentm.sdk.source.IdentityService;
import com.tokentm.sdk.source.TokenTmClient;
import com.xxf.arch.XXF;
import com.xxf.arch.core.activityresult.ActivityResult;
import com.xxf.arch.rxjava.transformer.ProgressHUDTransformerImpl;
import com.xxf.arch.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;


/**
 * @author youxuan  E-mail:xuanyouwu@163.com
 * @Description 身份密码找回
 */
public class IdentityPwdDecryptActivity extends BaseActivity implements IdentityPwdDecryptPresenter {
    //倒计时60秒
    private static final int SMS_DELAY = 60;
    private static final String KEY_DID_INFO = "did_info";
    private static final String KEY_PHONE = "phone";

    public static void launch(@NonNull Context context, @NonNull IdentityInfoStoreItem identityInfoStoreItem, @Nullable String phone) {
        context.startActivity(getLauncher(context, identityInfoStoreItem, phone));
    }

    public static Intent getLauncher(@NonNull Context context, @NonNull IdentityInfoStoreItem identityInfoStoreItem, @Nullable String phone) {
        return new Intent(context, IdentityPwdDecryptActivity.class)
                .putExtra(KEY_DID_INFO, identityInfoStoreItem)
                .putExtra(KEY_PHONE, phone);
    }

    IdentityInfoStoreItem identityInfoStoreItem;
    TksComponentsActivityIdentityPwdDecryptBinding binding;
    StepAdapter stepAdapter;
    List<List<NodeServiceEncryptedPartItem>> encryptIdentityPwdParts = new ArrayList<>();
    final SparseArray<NodeServiceDecryptedPartItem> decryptedPartItemSparseArray = new SparseArray<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TksComponentsActivityIdentityPwdDecryptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
        loadData();
    }


    private void initView() {
        //状态栏以图片为背景
        SuperStatusBarUtils.setTransparent(this, getDrawable(R.drawable.tks_components_bg_00cac0_00a6da));
        //导航栏白色
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().setNavigationBarColor(Color.WHITE);
        setTitle("身份密码找回");
        identityInfoStoreItem = (IdentityInfoStoreItem) getIntent().getSerializableExtra(KEY_DID_INFO);
        String phone = getIntent().getStringExtra(KEY_PHONE);

        binding.setViewModel(ViewModelProviders.of(this).get(IdentityPwdDecryptVm.class));
        binding.setPresenter(this);

        binding.getViewModel().phone.set(phone);

        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.recyclerView.setAdapter(stepAdapter = new StepAdapter());
        stepAdapter.bindData(true, generateSteps(3));


        binding.getViewModel().step.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                stepAdapter.setStep(binding.getViewModel().step.get());
                binding.getViewModel().smsCode.set("");
            }
        });

    }

    private List<StepModel> generateSteps(int stepCount) {
        List<StepModel> stepModels = new ArrayList<>();
        for (int i = 0; i < stepCount; i++) {
            stepModels.add(new StepModel(R.drawable.tks_components_step_cheked, R.drawable.tks_components_step_uncheked));
        }
        return stepModels;
    }


    private void loadData() {


    }

    @Override
    public void onSendSMSCode(ObservableField<String> phone, ObservableLong smsCountdown) {
        if (phone.get() == null || Objects.equals(phone.get(), "")) {
            ToastUtils.showToast("请输入手机号");
            return;
        }
        TokenTmClient.getService(BasicService.class)
                .sendSmsCode(phone.get())
                .compose(XXF.bindToLifecycle(getActivity()))
                .compose(XXF.<Boolean>bindToProgressHud(
                        new ProgressHUDTransformerImpl.Builder(IdentityPwdDecryptActivity.this)
                                .setLoadingNotice("发送中..."))
                )
                .flatMap(new Function<Boolean, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(Boolean aBoolean) throws Exception {
                        return io.reactivex.Observable.interval(0, 1, TimeUnit.SECONDS)
                                .take(SMS_DELAY + 1)
                                .map(new Function<Long, Long>() {
                                    @Override
                                    public Long apply(Long aLong) throws Exception {
                                        return SMS_DELAY - aLong;
                                    }
                                })
                                .observeOn(AndroidSchedulers.mainThread());
                    }
                })
                .compose(XXF.bindToLifecycle(getActivity()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        smsCountdown.set(aLong);
                    }
                });
    }

    @Override
    public void onIdentityPwdDecrypt(ObservableInt step, ObservableField<String> phone, ObservableField<String> smsCode, ObservableField<String> decryptNodeName) {
        io.reactivex.Observable
                .defer(new Callable<ObservableSource<List<List<NodeServiceEncryptedPartItem>>>>() {
                    @Override
                    public ObservableSource<List<List<NodeServiceEncryptedPartItem>>> call() throws Exception {
                        if (encryptIdentityPwdParts == null || encryptIdentityPwdParts.isEmpty()) {
                            return TokenTmClient.getService(IdentityService.class)
                                    .getEncryptIdentityPwdParts(identityInfoStoreItem.getDid(), phone.get(), smsCode.get())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnNext(new Consumer<List<List<NodeServiceEncryptedPartItem>>>() {
                                        @Override
                                        public void accept(List<List<NodeServiceEncryptedPartItem>> lists) throws Exception {
                                            encryptIdentityPwdParts = lists;

                                            //刷新UI
                                            stepAdapter.clearData();
                                            stepAdapter.setStep(0);
                                            stepAdapter.bindData(true, generateSteps(lists.get(0).size()));
                                        }
                                    });
                        }
                        return io.reactivex.Observable
                                .just(encryptIdentityPwdParts);


                    }
                })
                .flatMap(new Function<List<List<NodeServiceEncryptedPartItem>>, ObservableSource<NodeServiceDecryptedPartItem>>() {
                    @Override
                    public ObservableSource<NodeServiceDecryptedPartItem> apply(List<List<NodeServiceEncryptedPartItem>> lists) throws Exception {
                        NodeServiceEncryptedPartItem nodeServiceEncryptedPartItem = lists.get(0).get(step.get());
                        NodeServiceEncryptedPartItem nodeServiceEncryptedPartItemCopy = lists.get(1).get(step.get());

                        return decryptIdentityPwdPart(nodeServiceEncryptedPartItem, nodeServiceEncryptedPartItemCopy, phone.get(), smsCode.get());
                    }
                })
                .compose(XXF.bindToLifecycle(this))
                .compose(XXF.bindToProgressHud(new ProgressHUDTransformerImpl.Builder(this)))
                .subscribe(new Consumer<NodeServiceDecryptedPartItem>() {
                    @Override
                    public void accept(NodeServiceDecryptedPartItem nodeServiceDecryptedPartItem) throws Exception {
                        decryptedPartItemSparseArray.put(step.get(), nodeServiceDecryptedPartItem);
                        decryptNodeName.set(nodeServiceDecryptedPartItem.getServiceName());
                        step.set(step.get() + 1);


                        if (decryptedPartItemSparseArray.size() == encryptIdentityPwdParts.get(0).size()) {
                            ArrayList<NodeServiceDecryptedPartItem> decryptedPartItems = new ArrayList<>();
                            for (int i = 0; i < decryptedPartItemSparseArray.size(); i++) {
                                decryptedPartItems.add(decryptedPartItemSparseArray.get(i));
                            }
                            gotoPwdUpdatePage(decryptedPartItems);
                        }
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void gotoPwdUpdatePage(ArrayList<NodeServiceDecryptedPartItem> decryptedPartItems) {
        XXF.startActivityForResult(this,
                IdentityPwdUpdateActivity.getLauncher(
                        this,
                        identityInfoStoreItem,
                        decryptedPartItems), 2001)
                .filter(new Predicate<ActivityResult>() {
                    @Override
                    public boolean test(ActivityResult activityResult) throws Exception {
                        return activityResult.isOk();
                    }
                })
                .take(1)
                .map(new Function<ActivityResult, Boolean>() {
                    @Override
                    public Boolean apply(ActivityResult activityResult) throws Exception {
                        return activityResult.getData().getBooleanExtra(KEY_ACTIVITY_RESULT, false);
                    }
                })
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        setResult(Activity.RESULT_OK, getIntent().putExtra(KEY_ACTIVITY_RESULT, aBoolean));
                        finish();
                    }
                });
    }

    /**
     * @param encryptedPartItem
     * @param encryptedPartItemCopy 第二备份
     * @param phone
     * @param smsCode
     * @return
     */
    private io.reactivex.Observable<NodeServiceDecryptedPartItem> decryptIdentityPwdPart(@NonNull NodeServiceEncryptedPartItem encryptedPartItem,
                                                                                         @Nullable NodeServiceEncryptedPartItem encryptedPartItemCopy,
                                                                                         @NonNull String phone, @NonNull String smsCode) {
        return TokenTmClient.getService(IdentityService.class)
                .decryptIdentityPwdPart(identityInfoStoreItem.getDid(), encryptedPartItem, phone, smsCode)
                .onErrorResumeNext(new Function<Throwable, ObservableSource<? extends NodeServiceDecryptedPartItem>>() {
                    @Override
                    public ObservableSource<? extends NodeServiceDecryptedPartItem> apply(Throwable throwable) throws Exception {
                        if (encryptedPartItemCopy != null) {
                            return TokenTmClient.getService(IdentityService.class)
                                    .decryptIdentityPwdPart(identityInfoStoreItem.getDid(), encryptedPartItemCopy, phone, smsCode);
                        }
                        return io.reactivex.Observable.error(throwable);
                    }
                });
    }
}
