package io.novafoundation.nova.feature_staking_impl.domain.staking.start.setupStakingType.direct

import io.novafoundation.nova.common.validation.ValidationStatus
import io.novafoundation.nova.common.validation.ValidationSystem
import io.novafoundation.nova.feature_staking_impl.data.StakingOption
import io.novafoundation.nova.feature_staking_impl.data.stakingType
import io.novafoundation.nova.feature_staking_impl.domain.staking.start.common.selection.store.StartMultiStakingSelectionStoreProvider
import io.novafoundation.nova.feature_staking_impl.domain.staking.start.common.types.StakingTypeDetailsInteractor
import io.novafoundation.nova.feature_staking_impl.domain.staking.start.common.types.StakingTypeDetailsProvider
import io.novafoundation.nova.feature_staking_impl.domain.staking.start.common.types.StakingTypeDetailsProviderFactory
import io.novafoundation.nova.feature_staking_impl.domain.staking.start.common.types.direct.StakingTypeDetailsInteractorFactory
import io.novafoundation.nova.feature_staking_impl.domain.staking.start.setupAmount.SingleStakingProperties
import io.novafoundation.nova.feature_staking_impl.domain.staking.start.setupAmount.SingleStakingPropertiesFactory
import io.novafoundation.nova.feature_staking_impl.domain.staking.start.setupAmount.SingleStakingRecommendation
import io.novafoundation.nova.feature_staking_impl.domain.staking.start.setupStakingType.model.EditableStakingType
import io.novafoundation.nova.runtime.multiNetwork.chain.model.Chain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DirectStakingTypeDetailsProviderFactory(
    private val stakingTypeDetailsInteractorFactory: StakingTypeDetailsInteractorFactory,
    private val singleStakingPropertiesFactory: SingleStakingPropertiesFactory,
    private val currentSelectionStoreProvider: StartMultiStakingSelectionStoreProvider
) : StakingTypeDetailsProviderFactory {

    override suspend fun create(
        stakingOption: StakingOption,
        coroutineScope: CoroutineScope,
        availableStakingTypes: List<Chain.Asset.StakingType>
    ): StakingTypeDetailsProvider {
        val singleStakingProperties = singleStakingPropertiesFactory.createProperties(coroutineScope, stakingOption)
        val validationSystem = ValidationSystem.editingStakingType(
            singleStakingProperties,
            availableStakingTypes
        )
        return DirectStakingTypeDetailsProvider(
            validationSystem,
            stakingTypeDetailsInteractorFactory.create(stakingOption, coroutineScope),
            stakingOption.stakingType,
            singleStakingProperties,
            currentSelectionStoreProvider,
            coroutineScope
        )
    }
}

class DirectStakingTypeDetailsProvider(
    private val validationSystem: EditingStakingTypeValidationSystem,
    stakingTypeDetailsInteractor: StakingTypeDetailsInteractor,
    override val stakingType: Chain.Asset.StakingType,
    private val singleStakingProperties: SingleStakingProperties,
    private val currentSelectionStoreProvider: StartMultiStakingSelectionStoreProvider,
    private val coroutineScope: CoroutineScope
) : StakingTypeDetailsProvider {

    override val recommendationProvider: SingleStakingRecommendation = singleStakingProperties.recommendation

    override val stakingTypeDetails: Flow<EditableStakingType> = stakingTypeDetailsInteractor.observeData()
        .map {
            EditableStakingType(
                isAvailable = validate() is ValidationStatus.Valid,
                stakingTypeDetails = it
            )
        }

    private suspend fun validate(): ValidationStatus<EditingStakingTypeFailure>? {
        val selectionStore = currentSelectionStoreProvider.getSelectionStore(coroutineScope)
        val selectedStake = selectionStore.currentSelection?.selection?.stake ?: return null
        val payload = EditingStakingTypePayload(selectedStake, stakingType)
        return validationSystem.validate(payload).getOrNull()
    }

    override fun getValidationSystem(): EditingStakingTypeValidationSystem {
        return validationSystem
    }
}
