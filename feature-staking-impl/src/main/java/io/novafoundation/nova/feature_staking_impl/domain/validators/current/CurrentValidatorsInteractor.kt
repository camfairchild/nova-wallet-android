package io.novafoundation.nova.feature_staking_impl.domain.validators.current

import io.novafoundation.nova.common.list.GroupedList
import io.novafoundation.nova.common.list.emptyGroupedList
import io.novafoundation.nova.feature_staking_api.domain.api.StakingRepository
import io.novafoundation.nova.feature_staking_api.domain.api.getActiveElectedValidatorsExposures
import io.novafoundation.nova.feature_staking_api.domain.model.IndividualExposure
import io.novafoundation.nova.feature_staking_api.domain.model.NominatedValidator
import io.novafoundation.nova.feature_staking_api.domain.model.NominatedValidator.Status
import io.novafoundation.nova.feature_staking_api.domain.model.relaychain.StakingState
import io.novafoundation.nova.feature_staking_impl.data.repository.StakingConstantsRepository
import io.novafoundation.nova.feature_staking_impl.domain.common.isWaiting
import io.novafoundation.nova.feature_staking_impl.domain.validators.ValidatorProvider
import io.novafoundation.nova.feature_staking_impl.domain.validators.ValidatorSource
import io.novafoundation.nova.runtime.state.SingleAssetSharedState
import io.novafoundation.nova.runtime.state.chainAndAsset
import jp.co.soramitsu.fearless_utils.extensions.toHexString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

class CurrentValidatorsInteractor(
    private val stakingRepository: StakingRepository,
    private val stakingConstantsRepository: StakingConstantsRepository,
    private val validatorProvider: ValidatorProvider,
    private val selectedAssetSharedState: SingleAssetSharedState,
) {

    suspend fun nominatedValidatorsFlow(
        nominatorState: StakingState.Stash,
    ): Flow<GroupedList<Status.Group, NominatedValidator>> {
        if (nominatorState !is StakingState.Stash.Nominator) {
            return flowOf(emptyGroupedList())
        }

        val (chain, chainAsset) = selectedAssetSharedState.chainAndAsset()
        val chainId = chain.id

        return stakingRepository.observeActiveEraIndex(chainId).map { activeEra ->
            val stashId = nominatorState.stashId

            val exposures = stakingRepository.getActiveElectedValidatorsExposures(chainId)

            val activeNominations = exposures.mapValues { (_, exposure) ->
                exposure.others.firstOrNull { it.who.contentEquals(stashId) }
            }

            val nominatedValidatorIds = nominatorState.nominations.targets.mapTo(mutableSetOf(), ByteArray::toHexString)

            val isWaitingForNextEra = nominatorState.nominations.isWaiting(activeEra)

            val maxRewardedNominators = stakingConstantsRepository.maxRewardedNominatorPerValidator(chainId)

            val groupedByStatusClass = validatorProvider.getValidators(
                chain = chain,
                chainAsset = chainAsset,
                source = ValidatorSource.Custom(nominatedValidatorIds.toList()),
                cachedExposures = exposures
            )
                .map { validator ->
                    val userIndividualExposure = activeNominations[validator.accountIdHex]

                    val status = when {
                        userIndividualExposure != null -> {
                            // safe to !! here since non null nomination means that validator is elected
                            val userNominationIndex = validator.electedInfo!!.nominatorStakes
                                .sortedByDescending(IndividualExposure::value)
                                .indexOfFirst { it.who.contentEquals(stashId) }

                            val userNominationRank = userNominationIndex + 1

                            val willBeRewarded = userNominationRank < maxRewardedNominators

                            Status.Active(nomination = userIndividualExposure.value, willUserBeRewarded = willBeRewarded)
                        }
                        isWaitingForNextEra -> Status.WaitingForNextEra
                        exposures[validator.accountIdHex] != null -> Status.Elected
                        else -> Status.Inactive
                    }

                    NominatedValidator(validator, status)
                }
                .groupBy { it.status::class }

            val totalElectiveCount = with(groupedByStatusClass) { groupSize(Status.Active::class) + groupSize(Status.Elected::class) }
            val electedGroup = Status.Group.Active(totalElectiveCount)

            val waitingForNextEraGroup = Status.Group.WaitingForNextEra(
                maxValidatorsPerNominator = stakingConstantsRepository.maxValidatorsPerNominator(chainId),
                numberOfValidators = groupedByStatusClass.groupSize(Status.WaitingForNextEra::class)
            )

            groupedByStatusClass.mapKeys { (statusClass, validators) ->
                when (statusClass) {
                    Status.Active::class -> electedGroup
                    Status.Elected::class -> Status.Group.Elected(validators.size)
                    Status.Inactive::class -> Status.Group.Inactive(validators.size)
                    Status.WaitingForNextEra::class -> waitingForNextEraGroup
                    else -> throw IllegalArgumentException("Unknown status class: $statusClass")
                }
            }
                .toSortedMap(Status.Group.COMPARATOR)
        }
    }

    private fun Map<KClass<out Status>, List<NominatedValidator>>.groupSize(statusClass: KClass<out Status>): Int {
        return get(statusClass)?.size ?: 0
    }
}
