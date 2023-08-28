package io.novafoundation.nova.feature_staking_impl.presentation.staking.start.setupAmount.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.novafoundation.nova.common.di.viewmodel.ViewModelKey
import io.novafoundation.nova.common.di.viewmodel.ViewModelModule
import io.novafoundation.nova.common.resources.ResourceManager
import io.novafoundation.nova.common.validation.ValidationExecutor
import io.novafoundation.nova.feature_staking_impl.di.staking.startMultiStaking.MultiStakingSelectionStoreProviderKey
import io.novafoundation.nova.feature_staking_impl.domain.staking.start.common.StartMultiStakingInteractor
import io.novafoundation.nova.feature_staking_impl.domain.staking.start.common.selection.store.StartMultiStakingSelectionStoreProvider
import io.novafoundation.nova.feature_staking_impl.domain.staking.start.setupAmount.selectionType.MultiStakingSelectionTypeProviderFactory
import io.novafoundation.nova.feature_staking_impl.presentation.StartMultiStakingRouter
import io.novafoundation.nova.feature_staking_impl.presentation.staking.start.common.MultiStakingSelectionFormatter
import io.novafoundation.nova.feature_staking_impl.presentation.staking.start.common.di.CommonMultiStakingModule
import io.novafoundation.nova.feature_staking_impl.presentation.staking.start.setupAmount.SetupAmountMultiStakingPayload
import io.novafoundation.nova.feature_staking_impl.presentation.staking.start.setupAmount.SetupAmountMultiStakingViewModel
import io.novafoundation.nova.feature_wallet_api.domain.ArbitraryAssetUseCase
import io.novafoundation.nova.feature_wallet_api.presentation.mixin.amountChooser.AmountChooserMixin
import io.novafoundation.nova.feature_wallet_api.presentation.mixin.fee.FeeLoaderMixin

@Module(includes = [ViewModelModule::class, CommonMultiStakingModule::class])
class SetupAmountMultiStakingModule {

    @Provides
    @IntoMap
    @ViewModelKey(SetupAmountMultiStakingViewModel::class)
    fun provideViewModel(
        multiStakingSelectionFormatter: MultiStakingSelectionFormatter,
        resourceManager: ResourceManager,
        router: StartMultiStakingRouter,
        multiStakingSelectionTypeProviderFactory: MultiStakingSelectionTypeProviderFactory,
        assetUseCase: ArbitraryAssetUseCase,
        amountChooserMixinFactory: AmountChooserMixin.Factory,
        @MultiStakingSelectionStoreProviderKey selectionStoreProvider: StartMultiStakingSelectionStoreProvider,
        startMultiStakingInteractor: StartMultiStakingInteractor,
        payload: SetupAmountMultiStakingPayload,
        feeLoaderMixinFactory: FeeLoaderMixin.Factory,
        validationExecutor: ValidationExecutor
    ): ViewModel {
        return SetupAmountMultiStakingViewModel(
            multiStakingSelectionFormatter = multiStakingSelectionFormatter,
            resourceManager = resourceManager,
            router = router,
            multiStakingSelectionTypeProviderFactory = multiStakingSelectionTypeProviderFactory,
            assetUseCase = assetUseCase,
            amountChooserMixinFactory = amountChooserMixinFactory,
            selectionStoreProvider = selectionStoreProvider,
            payload = payload,
            validationExecutor = validationExecutor,
            interactor = startMultiStakingInteractor,
            feeLoaderMixinFactory = feeLoaderMixinFactory
        )
    }

    @Provides
    fun provideViewModelCreator(
        fragment: Fragment,
        viewModelFactory: ViewModelProvider.Factory
    ): SetupAmountMultiStakingViewModel {
        return ViewModelProvider(fragment, viewModelFactory).get(SetupAmountMultiStakingViewModel::class.java)
    }
}
