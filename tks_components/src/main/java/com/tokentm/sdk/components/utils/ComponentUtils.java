package com.tokentm.sdk.components.utils;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Lifecycle;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;

import com.tokentm.sdk.components.cert.CompanyCertActivity;
import com.tokentm.sdk.components.cert.PropertyRightsTransferRecordsActivity;
import com.tokentm.sdk.components.cert.UserCertByIDCardActivity;
import com.tokentm.sdk.components.cert.model.CompanyCertParams;
import com.tokentm.sdk.components.cert.model.UserCertByIDCardParams;
import com.tokentm.sdk.components.common.BaseTitleBarActivity;
import com.tokentm.sdk.components.identitypwd.model.BindUDID;
import com.tokentm.sdk.components.identitypwd.view.CertificationDetailsActivity;
import com.tokentm.sdk.components.identitypwd.view.CertificationInstructionsActivity;
import com.tokentm.sdk.components.identitypwd.view.EnterpriseCertificationAlertDialog;
import com.tokentm.sdk.components.identitypwd.view.IdentityAuthenticationAlertDialog;
import com.tokentm.sdk.components.identitypwd.view.IdentityConfirmActivity;
import com.tokentm.sdk.components.identitypwd.view.IdentityPwdDecryptActivity;
import com.tokentm.sdk.components.identitypwd.view.IdentityPwdInputDialog;
import com.tokentm.sdk.model.ChainResult;
import com.tokentm.sdk.model.IdentityInfoStoreItem;
import com.tokentm.sdk.source.CertService;
import com.tokentm.sdk.source.IdentityService;
import com.tokentm.sdk.source.TokenTmClient;
import com.xxf.arch.XXF;
import com.xxf.arch.activity.XXFActivity;
import com.xxf.arch.core.activityresult.ActivityResult;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * @author youxuan  E-mail:xuanyouwu@163.com
 * @Description sdk UI组件工具
 */
public class ComponentUtils {

    /**
     * 启动身份确认页面
     *
     * @param activity
     * @param userPhone
     * @param consumer  返回uDID
     */
    @SuppressLint("CheckResult")
    public static void launchUserIdentityConfirmActivity(FragmentActivity activity, String userPhone, Consumer<BindUDID> consumer) {
        XXF.startActivityForResult(activity,
                IdentityConfirmActivity.getLauncher(activity, userPhone),
                7000)
                .filter(new Predicate<ActivityResult>() {
                    @Override
                    public boolean test(ActivityResult activityResult) throws Exception {
                        return activityResult.isOk();
                    }
                })
                .map(new Function<ActivityResult, BindUDID>() {
                    @Override
                    public BindUDID apply(ActivityResult activityResult) throws Exception {
                        return (BindUDID) activityResult.getData().getSerializableExtra(BaseTitleBarActivity.KEY_ACTIVITY_RESULT);
                    }
                })
                .take(1)
                .compose(XXF.bindUntilEvent(activity, Lifecycle.Event.ON_DESTROY))
                .compose(XXF.bindToErrorNotice())
                .subscribe(consumer);
    }


    /**
     * 忘记身份密码
     *
     * @param activity
     * @param uDID
     */
    public static void launchForgotIdentityPwd(FragmentActivity activity, String uDID, Consumer<Boolean> consumer) {
        TokenTmClient.getService(IdentityService.class)
                .getUDID(uDID)
                .compose(XXF.bindToErrorNotice())
                .subscribe(new Consumer<IdentityInfoStoreItem>() {
                    @Override
                    public void accept(IdentityInfoStoreItem identityInfoStoreItem) throws Exception {
                        XXF.startActivityForResult(
                                activity,
                                IdentityPwdDecryptActivity.getLauncher(activity, identityInfoStoreItem, null),
                                1007)
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
                                        return (Boolean) activityResult.getData().getSerializableExtra(XXFActivity.KEY_ACTIVITY_RESULT);
                                    }
                                })
                                .subscribe(consumer);
                    }
                });
    }


    /**
     * show 身份密码输入框dialog
     *
     * @param activity
     * @param uDID
     * @param dialogConsumer
     */
    public static void showIdentityPwdDialog(FragmentActivity activity, String uDID, BiConsumer<DialogInterface, String> dialogConsumer) {
        IdentityPwdInputDialog.showUserIdentityPwdInputDialogNoStampAnim(activity, uDID, dialogConsumer)
                .show();
    }

    /**
     * show 企业认证 dialog
     *
     * @param activity
     * @param dialogConsumer
     */
    public static void showEnterpriseCertificationAlertDialog(FragmentActivity activity, BiConsumer<DialogInterface, Boolean> dialogConsumer) {
        new EnterpriseCertificationAlertDialog(activity, dialogConsumer).show();
    }

    /**
     * show 身份认证 dialog
     *
     * @param activity
     * @param dialogConsumer
     */
    public static void showIdentityAuthenticationAlertDialog(FragmentActivity activity, BiConsumer<DialogInterface, Boolean> dialogConsumer) {
        new IdentityAuthenticationAlertDialog(activity, dialogConsumer).show();
    }

    /**
     * 启动用户实名认证 页面
     *
     * @param activity
     * @param userCertByIDCardParams
     */
    @SuppressLint("CheckResult")
    public static void launchUserCertActivity(FragmentActivity activity, UserCertByIDCardParams userCertByIDCardParams, Consumer<ChainResult> consumer) {
        XXF.startActivityForResult(
                activity,
                UserCertByIDCardActivity.getLauncher(activity,
                        userCertByIDCardParams
                ),
                7100)
                .filter(new Predicate<ActivityResult>() {
                    @Override
                    public boolean test(ActivityResult activityResult) throws Exception {
                        return activityResult.isOk();
                    }
                })
                .map(new Function<ActivityResult, ChainResult>() {
                    @Override
                    public ChainResult apply(ActivityResult activityResult) throws Exception {
                        return (ChainResult) activityResult.getData().getSerializableExtra(XXFActivity.KEY_ACTIVITY_RESULT);
                    }
                })
                .take(1)
                .compose(XXF.bindUntilEvent(activity, Lifecycle.Event.ON_DESTROY))
                .compose(XXF.bindToErrorNotice())
                .subscribe(consumer);
    }

    /**
     * 启动企业/组织认证 页面
     *
     * @param activity
     * @param companyCertParams
     * @param consumer
     */
    @SuppressLint("CheckResult")
    public static void launchCompanyCertActivity(FragmentActivity activity, CompanyCertParams companyCertParams, Consumer<ChainResult> consumer) {
        XXF.startActivityForResult(
                activity,
                CompanyCertActivity.getLauncher(
                        activity,
                        companyCertParams
                ),
                7101)
                .filter(new Predicate<ActivityResult>() {
                    @Override
                    public boolean test(ActivityResult activityResult) throws Exception {
                        return activityResult.isOk();
                    }
                })
                .map(new Function<ActivityResult, ChainResult>() {
                    @Override
                    public ChainResult apply(ActivityResult activityResult) throws Exception {
                        return (ChainResult) activityResult.getData().getSerializableExtra(XXFActivity.KEY_ACTIVITY_RESULT);
                    }
                })
                .take(1)
                .compose(XXF.bindUntilEvent(activity, Lifecycle.Event.ON_DESTROY))
                .compose(XXF.bindToErrorNotice())
                .subscribe(consumer);
    }

    /**
     * 启动 物权转移 页面
     *
     * @param activity
     */
    @SuppressLint("CheckResult")
    public static void launchPropertyRightsTransferRecordsActivity(FragmentActivity activity, String id) {
        PropertyRightsTransferRecordsActivity.launch(activity, id);
    }

    /**
     * 启动 认证说明 页面
     *
     * @param activity
     */
    @SuppressLint("CheckResult")
    public static void launchCertificationInstructionsActivity(FragmentActivity activity, String did) {
        CertificationInstructionsActivity.launch(activity, did);
    }

    /**
     * 启动 认证详情 页面
     *
     * @param activity
     */
    @SuppressLint("CheckResult")
    public static void launchCertificationDetailsActivity(FragmentActivity activity, String did) {
        CertificationDetailsActivity.launch(activity, did);
    }

    /**
     * 是否显示
     */
    public static void isShowIdentityDescription(FragmentActivity activity, String did, Consumer<Boolean> consumer) {
        TokenTmClient.getService(CertService.class)
                .isUserCert(did)
                .compose(XXF.bindToLifecycle(activity))
                .compose(XXF.bindToErrorNotice())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);

    }
}
