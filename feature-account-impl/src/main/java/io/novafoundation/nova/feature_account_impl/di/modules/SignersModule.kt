package io.novafoundation.nova.feature_account_impl.di.modules

import dagger.Module
import dagger.Provides
import io.novafoundation.nova.common.data.secrets.v2.SecretStoreV2
import io.novafoundation.nova.common.di.scope.FeatureScope
import io.novafoundation.nova.feature_account_api.data.signer.SignerProvider
import io.novafoundation.nova.feature_account_api.presenatation.account.watchOnly.WatchOnlyMissingKeysPresenter
import io.novafoundation.nova.feature_account_impl.data.signer.RealSignerProvider
import io.novafoundation.nova.feature_account_impl.data.signer.secrets.SecretsSignerFactory
import io.novafoundation.nova.feature_account_impl.data.signer.watchOnly.WatchOnlySigner

@Module
class SignersModule {

    @Provides
    @FeatureScope
    fun provideSecretsSignerFactory(secretStoreV2: SecretStoreV2) = SecretsSignerFactory(secretStoreV2)

    @Provides
    @FeatureScope
    fun provideWatchOnlySigner(
        watchOnlySigningPresenter: WatchOnlyMissingKeysPresenter
    ) = WatchOnlySigner(watchOnlySigningPresenter)

    @Provides
    @FeatureScope
    fun provideSignerProvider(
        secretsSignerFactory: SecretsSignerFactory,
        watchOnlySigner: WatchOnlySigner
    ): SignerProvider = RealSignerProvider(secretsSignerFactory, watchOnlySigner)
}