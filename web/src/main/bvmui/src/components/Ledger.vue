<template>
    <div class="bvm-tile" @click="$emit('click', $event)">
        <div v-if="ledger.description !== ''" class="bvm-tile__descrow">
            <span>{{ ledger.description }}</span>
        </div>
        <div class="bvm-tile__row">
            <span>Ledger {{ ledger.id }} ({{ ledger.clusterName }})</span>
        </div>
        <div class="bvm-tile__row">
            <span>Size {{ $library.formatBytes(ledger.length) }}</span>
        </div>
        <div class="bvm-tile__row">
            <span>Replication {{ ledger.writeQuorumSize }}</span>
        </div>
        <div class="bvm-tile__row">
            <span>Age {{ computedAge }}</span>
        </div>
    </div>
</template>
<script>
export default {
    props: {
        ledger: Object
    },
    computed: {
        computedAge() {
            const countHours = Math.floor(this.ledger.age / 60);
            const countMinutes = this.ledger.age % 60;
            if (countHours >= 1) {
                return `${countHours} hours ${countMinutes} minutes`;
            } else {
                return `${countMinutes} minutes`;
            }
        }
    }
};
</script>
