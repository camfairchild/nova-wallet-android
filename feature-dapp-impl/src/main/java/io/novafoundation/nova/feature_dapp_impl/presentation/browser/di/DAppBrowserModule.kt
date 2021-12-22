package io.novafoundation.nova.feature_dapp_impl.presentation.browser.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.novafoundation.nova.common.di.viewmodel.ViewModelKey
import io.novafoundation.nova.common.di.viewmodel.ViewModelModule
import io.novafoundation.nova.feature_account_api.domain.interfaces.AccountRepository
import io.novafoundation.nova.feature_dapp_impl.DAppRouter
import io.novafoundation.nova.feature_dapp_impl.domain.browser.DappBrowserInteractor
import io.novafoundation.nova.feature_dapp_impl.presentation.browser.DAppBrowserViewModel
import io.novafoundation.nova.feature_dapp_impl.web3.polkadotJs.PolkadotJsExtensionFactory
import io.novafoundation.nova.runtime.multiNetwork.ChainRegistry

@Module(includes = [ViewModelModule::class])
class DAppBrowserModule {

    @Provides
    fun provideInteractor(
        chainRegistry: ChainRegistry,
        accountRepository: AccountRepository
    ) = DappBrowserInteractor(chainRegistry, accountRepository)

    @Provides
    internal fun provideViewModel(fragment: Fragment, factory: ViewModelProvider.Factory): DAppBrowserViewModel {
        return ViewModelProvider(fragment, factory).get(DAppBrowserViewModel::class.java)
    }

    @Provides
    @IntoMap
    @ViewModelKey(DAppBrowserViewModel::class)
    fun provideViewModel(
        router: DAppRouter,
        polkadotJsExtensionFactory: PolkadotJsExtensionFactory,
        interactor: DappBrowserInteractor
    ): ViewModel {
        return DAppBrowserViewModel(
            router = router,
            polkadotJsExtensionFactory = polkadotJsExtensionFactory,
            interactor = interactor
        )
    }
}
