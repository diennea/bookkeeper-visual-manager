<template>
    <div class="bvm-metadata-container">
        <button class="bvm-metadata-close" @click="$emit('close', $event)">✕</button>
        <h2>Ledger {{currentLedger.id}}</h2>
        <div>Age: {{currentLedger.age}} hours</div>
        <div>Created at: {{new Date(currentLedger.ctime)}}</div>
        <div>State: {{currentLedger.state}}</div>
        <div>Ensemble size: {{currentLedger.ensembleSize}} (number of bookies)</div>
        <div>WriteQuorumSize: {{currentLedger.writeQuorumSize}} (number of copies)</div>
        <div>AckQuorumSize: {{currentLedger.ackQuorumSize}}</div>
        <div>Size: {{currentLedger.length}} bytes</div>
        <div>LastEntryId: {{currentLedger.lastEntryId}}</div>
        <div>Password: {{currentLedger.password}}</div>
        <div>DigestType: {{currentLedger.digestType}}</div>
        <div>Closed: {{currentLedger.closed}}</div>
        <div>MetadataFormatVersion: {{currentLedger.metadataFormatVersion}}</div>
        <div class="bvm-metadata-row" v-for="(key, value) in  currentLedger.metadata" :key="key">
            <strong>{{value}}</strong>: {{key}}
        </div>
    </div>
</template>
<script>
export default {
    props: {
        currentLedger: Object
    }
};
</script>

