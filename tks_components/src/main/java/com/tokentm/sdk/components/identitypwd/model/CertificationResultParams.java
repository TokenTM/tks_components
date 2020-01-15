package com.tokentm.sdk.components.identitypwd.model;

import android.support.annotation.NonNull;

import com.tokentm.sdk.model.ChainCertResult;
import com.tokentm.sdk.model.ChainResult;
import com.tokentm.sdk.model.CompanyCertInfoStoreItem;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author lqx  E-mail:herolqx@126.com
 * @Description 当同时需要身份认证并且企业认证的时候, 要返回多个认证结果
 * 如果结果为null,说明没有认证后的数据
 */
public class CertificationResultParams implements Serializable {

    public static class Builder {

        private ChainResult identityCertificationResult;
        private ChainCertResult<CompanyCertInfoStoreItem> companyCertificationResult;

        public Builder(@NonNull ChainResult identityCertificationResult, @NonNull ChainCertResult<CompanyCertInfoStoreItem> companyCertificationResult) {
            this.identityCertificationResult = Objects.requireNonNull(identityCertificationResult);
            this.companyCertificationResult = Objects.requireNonNull(companyCertificationResult);
        }

        public Builder() {

        }

        public Builder(CertificationResultParams certificationResultParams) {
            this.identityCertificationResult = certificationResultParams.identityCertificationResult;
            this.companyCertificationResult = certificationResultParams.companyCertificationResult;
        }

        public Builder setCompanyCertificationResult(ChainCertResult<CompanyCertInfoStoreItem> companyCertificationResult) {
            this.companyCertificationResult = companyCertificationResult;
            return this;
        }

        public Builder setIdentityCertificationResult(ChainResult identityCertificationResult) {
            this.identityCertificationResult = identityCertificationResult;
            return this;
        }

        public CertificationResultParams build() {
            return new CertificationResultParams(identityCertificationResult, companyCertificationResult);
        }
    }

    /**
     * 身份认证结果
     */
    private ChainResult identityCertificationResult;
    /**
     * 企业认证结果
     */
    private ChainCertResult<CompanyCertInfoStoreItem> companyCertificationResult;


    private CertificationResultParams(ChainResult identityCertificationResult, ChainCertResult<CompanyCertInfoStoreItem> companyCertificationResult) {
        this.identityCertificationResult = identityCertificationResult;
        this.companyCertificationResult = companyCertificationResult;
    }

    public ChainResult getIdentityCertificationResult() {
        return identityCertificationResult;
    }

    public ChainCertResult<CompanyCertInfoStoreItem> getCompanyCertificationResult() {
        return companyCertificationResult;
    }

}
