<template>
    <div class="bvm-metadata">
        <button class="bvm-metadata-close" @click="$emit('close', $event)">✕</button>
        <h3>Ledger {{ currentLedger.id }}</h3>
        <div>Cluster: {{ currentLedger.clusterName }}</div>
        <h2 v-if="currentLedger.description !== ''">{{ currentLedger.description }}</h2>
        <div>Age: {{ currentLedger.age }} minutes</div>
        <div>Created at: {{ $library.formatDate(currentLedger.ctime) }}</div>
        <div>State: {{ currentLedger.state }}</div>
        <div>Ensemble size: {{ currentLedger.ensembleSize }} (number of bookies)</div>
        <div>WriteQuorumSize: {{ currentLedger.writeQuorumSize }} (number of copies)</div>
        <div>AckQuorumSize: {{ currentLedger.ackQuorumSize }}</div>
        <div>Size: {{ $library.formatBytes(currentLedger.length) }}</div>
        <div>LastEntryId: {{ currentLedger.lastEntryId }}</div>
        <div>Password: {{ currentLedger.password }}</div>
        <div>DigestType: {{ currentLedger.digestType }}</div>
        <div>Closed: {{ currentLedger.closed }}</div>
        <div>MetadataFormatVersion: {{ currentLedger.metadataFormatVersion }}</div>
        <div class="bvm-metadata-row" v-for="(blist, key) in currentLedger.ensembles" :key="key">
            <b>Ensemble {{ key }}</b>: <span v-for="item in blist" :key="item">{{ item }}; </span>
        </div>
        <div class="bvm-metadata-row" v-for="(key, value) in currentLedger.metadata" :key="key">
            <strong>{{ value }}</strong>: {{ key }}
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
