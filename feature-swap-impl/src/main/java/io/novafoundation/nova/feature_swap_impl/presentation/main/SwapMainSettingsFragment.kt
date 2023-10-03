package io.novafoundation.nova.feature_swap_impl.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import io.novafoundation.nova.common.base.BaseFragment
import io.novafoundation.nova.common.di.FeatureUtils
import io.novafoundation.nova.common.utils.applyStatusBarInsets
import io.novafoundation.nova.common.utils.bindTo
import io.novafoundation.nova.common.utils.setVisible
import io.novafoundation.nova.feature_swap_api.di.SwapFeatureApi
import io.novafoundation.nova.feature_swap_impl.R
import io.novafoundation.nova.feature_swap_impl.di.SwapFeatureComponent
import io.novafoundation.nova.feature_wallet_api.presentation.view.showAmount
import kotlinx.android.synthetic.main.fragment_main_swap_settings.swapMainSettingsContinue
import kotlinx.android.synthetic.main.fragment_main_swap_settings.swapMainSettingsDetails
import kotlinx.android.synthetic.main.fragment_main_swap_settings.swapMainSettingsDetailsNetworkFee
import kotlinx.android.synthetic.main.fragment_main_swap_settings.swapMainSettingsDetailsRate
import kotlinx.android.synthetic.main.fragment_main_swap_settings.swapMainSettingsFlip
import kotlinx.android.synthetic.main.fragment_main_swap_settings.swapMainSettingsMaxAmount
import kotlinx.android.synthetic.main.fragment_main_swap_settings.swapMainSettingsMaxAmountButton
import kotlinx.android.synthetic.main.fragment_main_swap_settings.swapMainSettingsPayInput
import kotlinx.android.synthetic.main.fragment_main_swap_settings.swapMainSettingsReceiveInput
import kotlinx.android.synthetic.main.fragment_main_swap_settings.swapMainSettingsToolbar

class SwapMainSettingsFragment : BaseFragment<SwapMainSettingsViewModel>() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_main_swap_settings, container, false)
    }

    override fun initViews() {
        swapMainSettingsToolbar.applyStatusBarInsets()

        swapMainSettingsMaxAmountButton.setOnClickListener { viewModel.maxTokens() }
        swapMainSettingsPayInput.setOnClickListener { viewModel.selectPayToken() }
        swapMainSettingsReceiveInput.setOnClickListener { viewModel.selectReceiveToken() }
        swapMainSettingsFlip.setOnClickListener { viewModel.flipAssets() }
        swapMainSettingsDetailsRate.setOnClickListener { viewModel.rateDetailsClicked() }
        swapMainSettingsDetailsNetworkFee.setOnClickListener { viewModel.networkFeeClicked() }
        swapMainSettingsContinue.setOnClickListener { viewModel.confirmButtonClicked() }
    }

    override fun inject() {
        FeatureUtils.getFeature<SwapFeatureComponent>(
            requireContext(),
            SwapFeatureApi::class.java
        )
            .swapMainSettings()
            .create(this)
            .inject(this)
    }

    override fun subscribe(viewModel: SwapMainSettingsViewModel) {
        swapMainSettingsPayInput.amountInput.bindTo(viewModel.payInput, lifecycleScope)
        swapMainSettingsPayInput.amountInput.bindTo(viewModel.receiveInput, lifecycleScope)

        viewModel.paymentTokenMaxAmount.observe { swapMainSettingsMaxAmount.text = it }
        viewModel.paymentAsset.observe { swapMainSettingsPayInput.setModel(it) }
        viewModel.receivingAsset.observe { swapMainSettingsReceiveInput.setModel(it) }
        viewModel.rateDetails.observe { swapMainSettingsDetailsRate.showValue(it) }
        viewModel.networkFee.observe { swapMainSettingsDetailsNetworkFee.showAmount(it) }
        viewModel.showDetails.observe { swapMainSettingsDetails.setVisible(it) }
        viewModel.buttonState.observe {
            swapMainSettingsContinue.text = it.text
            swapMainSettingsContinue.setState(it.state)
        }
    }
}
