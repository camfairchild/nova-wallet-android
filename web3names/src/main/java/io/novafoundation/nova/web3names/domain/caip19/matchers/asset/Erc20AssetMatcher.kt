package io.novafoundation.nova.web3names.domain.caip19.matchers.asset

import io.novafoundation.nova.runtime.multiNetwork.chain.model.Chain
import io.novafoundation.nova.runtime.multiNetwork.chain.model.Chain.Asset.Type.Evm
import io.novafoundation.nova.web3names.domain.caip19.identifiers.AssetIdentifier
import io.novafoundation.nova.web3names.domain.caip19.identifiers.AssetIdentifier.Erc20

class Erc20AssetMatcher(private val chainAsset: Chain.Asset) : AssetMatcher {

    // TODO support native EVM
    override fun match(assetIdentifier: AssetIdentifier): Boolean {
        val chainAssetType = chainAsset.type
        return assetIdentifier is Erc20 &&
            chainAssetType is Evm &&
            assetIdentifier.contractAddress == chainAssetType.contractAddress
    }
}
