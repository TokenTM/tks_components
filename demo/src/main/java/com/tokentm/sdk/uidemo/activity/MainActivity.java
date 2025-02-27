package com.tokentm.sdk.uidemo.activity;

import android.content.DialogInterface;
import android.os.Bundle;

import com.tokentm.sdk.components.cert.model.CompanyCertParams;
import com.tokentm.sdk.components.cert.model.UserCertByIDCardParams;
import com.tokentm.sdk.components.common.BaseTitleBarActivity;
import com.tokentm.sdk.components.identitypwd.model.CertificationResultWrapper;
import com.tokentm.sdk.components.utils.ComponentUtils;
import com.tokentm.sdk.model.CertUserInfoStoreItem;
import com.tokentm.sdk.model.ChainCertResult;
import com.tokentm.sdk.model.CompanyCertInfoStoreItem;
import com.tokentm.sdk.model.CompanyType;
import com.tokentm.sdk.uidemo.DemoSp;
import com.tokentm.sdk.uidemo.databinding.ActivityMainBinding;
import com.tokentm.sdk.uidemo.dialog.InputCompanyNameFragmentDialog;
import com.tokentm.sdk.uidemo.dialog.InputGoodsIdDialog;
import com.tokentm.sdk.uidemo.dialog.InputIdentityAndCompanyParamsDialog;
import com.tokentm.sdk.uidemo.dialog.InputIdentityCompanyParams;
import com.tokentm.sdk.uidemo.prensenter.IMainPresenter;
import com.xxf.arch.utils.ToastUtils;

import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

/**
 * @author lqx  E-mail:herolqx@126.com
 * @Description 首页
 */
public class MainActivity extends BaseTitleBarActivity implements IMainPresenter {


    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.setPresenter(this);
        initView();
    }

    String did;

    @Override
    protected void onResume() {
        super.onResume();
        did = DemoSp.getInstance().getLoginDID();
    }

    private void initView() {
        setTitle("首页");
    }

    @Override
    public void onIdentityCertification() {
        ComponentUtils.launchUserCertActivity(
                getActivity()
                , new UserCertByIDCardParams.Builder(did)
                        .setUserName("")
                        .setUserIDCard("")
                        .build(),
                new Consumer<ChainCertResult<CertUserInfoStoreItem>>() {
                    @Override
                    public void accept(ChainCertResult<CertUserInfoStoreItem> chainResult) throws Exception {
                        saveIdentityCertification(chainResult);
                    }
                });
    }

    @Override
    public void onCompanyCertification() {
        new InputCompanyNameFragmentDialog(getActivity(), new BiConsumer<DialogInterface, String>() {
            @Override
            public void accept(DialogInterface dialogInterface, String companyName) throws Exception {
                ComponentUtils.launchCompanyCertActivity(getActivity(),
                        new CompanyCertParams.Builder(did, companyName)
                                .setCompanyType(CompanyType.TYPE_COMPANY)
                                .build()
                        , new Consumer<ChainCertResult<CompanyCertInfoStoreItem>>() {
                            @Override
                            public void accept(ChainCertResult<CompanyCertInfoStoreItem> chainResult) throws Exception {
                                saveICompanyCertification(chainResult);
                            }
                        });
            }
        }).show();
    }

    @Override
    public void onOpenChainCertification() {
        String uTxHash = DemoSp.getInstance().getString(DemoSp.SP_KEY_IDENTITY_TX_HASH);
        String cTxHash = DemoSp.getInstance().getString(DemoSp.SP_KEY_COMPANY_TX_HASH);
        String uDid = DemoSp.getInstance().getString(DemoSp.SP_KEY_IDENTITY_DID);
        String cDid = DemoSp.getInstance().getString(DemoSp.SP_KEY_COMPANY_DID);
        ComponentUtils.launchChainCertificationActivity(getActivity(),
                uTxHash, cTxHash, uDid, cDid, new Consumer<CertificationResultWrapper>() {
                    @Override
                    public void accept(CertificationResultWrapper certificationResultWrapper) throws Exception {
                        saveIdentityCertification(certificationResultWrapper.getIdentityCertificationResult());
                        saveICompanyCertification(certificationResultWrapper.getCompanyCertificationResult());
                    }
                });
    }

    @Override
    public void onOpenChainCertificationOther() {
        new InputIdentityAndCompanyParamsDialog(getActivity(), new BiConsumer<DialogInterface, InputIdentityCompanyParams>() {
            @Override
            public void accept(DialogInterface dialogInterface, InputIdentityCompanyParams inputIdentityCompanyParams) throws Exception {
                ComponentUtils.launchChainCertificationOther(getActivity()
                        , inputIdentityCompanyParams.getIdentityTxHash()
                        , inputIdentityCompanyParams.getCompanyTxHash()
                        , inputIdentityCompanyParams.getIdentityDid()
                        , inputIdentityCompanyParams.getCompanyDid());
            }
        }).show();
    }

    @Override
    public void onShowIdentityCertificationDialog() {
        ComponentUtils.showIdentityCertificationDialogNotForce(getActivity(), new UserCertByIDCardParams.Builder(did).build(), new Consumer<ChainCertResult<CertUserInfoStoreItem>>() {
            @Override
            public void accept(ChainCertResult<CertUserInfoStoreItem> chainResult) throws Exception {
                saveIdentityCertification(chainResult);
            }
        });
    }

    @Override
    public void onShowIdentityAndCompanyCertificationDialog() {
        ComponentUtils.showIdentityAndCompanyCertificationDialogNotForce(getActivity()
                , new UserCertByIDCardParams.Builder(did).build()
                , new CompanyCertParams.Builder(did, "").build()
                , new Consumer<CertificationResultWrapper>() {
                    @Override
                    public void accept(CertificationResultWrapper certificationResultWrapper) throws Exception {
                        saveIdentityCertification(certificationResultWrapper.getIdentityCertificationResult());
                        saveICompanyCertification(certificationResultWrapper.getCompanyCertificationResult());
                    }
                });
    }

    @Override
    public void onDeliverGoods() {
        //开启发货页面
        DeliverGoodsActivity.launch(getActivity(), did);
    }

    @Override
    public void onReceiveGoods() {
        new InputGoodsIdDialog(getActivity(), new BiConsumer<DialogInterface, String>() {
            @Override
            public void accept(DialogInterface dialogInterface, String goodsId) throws Exception {
                ReceiveGoodsActivity.launch(getActivity(), did, goodsId);
            }
        }).show();
    }

    @Override
    public void onTransferGoods() {
        new InputGoodsIdDialog(getActivity(), new BiConsumer<DialogInterface, String>() {
            @Override
            public void accept(DialogInterface dialogInterface, String goodsId) throws Exception {
                TransferGoodsActivity.launch(getActivity(), did, goodsId);
            }
        }).show();
    }

    @Override
    public void onGoodsRecord() {
        new InputGoodsIdDialog(getActivity(), new BiConsumer<DialogInterface, String>() {
            @Override
            public void accept(DialogInterface dialogInterface, String goodsId) throws Exception {
                ComponentUtils.launchGoodsTransferRecordsActivity(getActivity(), goodsId);
            }
        }).show();
    }

    @Override
    public void onCertification() {
        //开启发证页面
        ReleaseCertificateActivity.launch(getActivity(), did);
    }

    @Override
    public void onConfirmCertificate() {
        ConfirmCertificateActivity.launch(getActivity(), did);
    }

    @Override
    public void onDisabledCertificate() {
        CancelCertificateActivity.launch(getActivity(), did);
    }

    @Override
    public void onBackup() {
        BackupActivity.launch(getActivity(), did);
    }

    @Override
    public void onGetBackup() {
        GetBackupActivity.launch(getActivity(), did);
    }

    @Override
    public void onLogout() {
        DemoSp.getInstance().logout();
        LoginOrRegisterActivity.launch(getActivity());
        finish();
    }

    private void saveIdentityCertification(ChainCertResult<CertUserInfoStoreItem> identityCertificationResult) {
        if (identityCertificationResult != null) {
            if (identityCertificationResult.getTxHash() != null && !"".equals(identityCertificationResult.getTxHash())) {
                DemoSp.getInstance().putString(DemoSp.SP_KEY_IDENTITY_TX_HASH, identityCertificationResult.getTxHash());
                DemoSp.getInstance().putString(DemoSp.SP_KEY_IDENTITY_DID, identityCertificationResult.getDid());
                ToastUtils.showToast("身份认证成功");
            } else {
                ToastUtils.showToast("身份认证失败");
            }
        }
    }

    private void saveICompanyCertification(ChainCertResult<CompanyCertInfoStoreItem> companyCertificationResult) {
        if (companyCertificationResult != null) {
            if (companyCertificationResult.getTxHash() != null && !"".equals(companyCertificationResult.getTxHash())) {
                DemoSp.getInstance().putString(DemoSp.SP_KEY_COMPANY_TX_HASH, companyCertificationResult.getTxHash());
                DemoSp.getInstance().putString(DemoSp.SP_KEY_COMPANY_DID, companyCertificationResult.getDid());
                ToastUtils.showToast("企业认证成功");
            } else {
                ToastUtils.showToast("企业认证失败");
            }
        }
    }

}
